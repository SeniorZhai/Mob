package com.google.android.exoplayer.dash;

import android.os.Handler;
import com.google.android.exoplayer.BehindLiveWindowException;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.TrackInfo;
import com.google.android.exoplayer.chunk.Chunk;
import com.google.android.exoplayer.chunk.ChunkExtractorWrapper;
import com.google.android.exoplayer.chunk.ChunkOperationHolder;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.chunk.ContainerMediaChunk;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.Format.DecreasingBandwidthComparator;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.chunk.FormatEvaluator.Evaluation;
import com.google.android.exoplayer.chunk.InitializationChunk;
import com.google.android.exoplayer.chunk.MediaChunk;
import com.google.android.exoplayer.chunk.SingleSampleMediaChunk;
import com.google.android.exoplayer.dash.mpd.AdaptationSet;
import com.google.android.exoplayer.dash.mpd.ContentProtection;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescription;
import com.google.android.exoplayer.dash.mpd.Period;
import com.google.android.exoplayer.dash.mpd.RangedUri;
import com.google.android.exoplayer.dash.mpd.Representation;
import com.google.android.exoplayer.drm.DrmInitData;
import com.google.android.exoplayer.drm.DrmInitData.Mapped;
import com.google.android.exoplayer.extractor.ChunkIndex;
import com.google.android.exoplayer.extractor.mp4.FragmentedMp4Extractor;
import com.google.android.exoplayer.extractor.webm.WebmExtractor;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.util.Clock;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.SystemClock;
import com.mobcrush.mobcrush.Constants;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class DashChunkSource implements ChunkSource {
    public static final int USE_ALL_TRACKS = -1;
    private final int adaptationSetIndex;
    private MediaPresentationDescription currentManifest;
    private final DataSource dataSource;
    private DrmInitData drmInitData;
    private final long elapsedRealtimeOffsetUs;
    private final Evaluation evaluation;
    private final Handler eventHandler;
    private final EventListener eventListener;
    private IOException fatalError;
    private boolean finishedCurrentManifest;
    private int firstAvailableSegmentNum;
    private final FormatEvaluator formatEvaluator;
    private final Format[] formats;
    private final StringBuilder headerBuilder;
    private int lastAvailableSegmentNum;
    private boolean lastChunkWasInitialization;
    private final long liveEdgeLatencyUs;
    private final ManifestFetcher<MediaPresentationDescription> manifestFetcher;
    private final int maxHeight;
    private final int maxWidth;
    private final HashMap<String, RepresentationHolder> representationHolders;
    private final int[] representationIndices;
    private TimeRange seekRange;
    private long[] seekRangeValues;
    private boolean startAtLiveEdge;
    private final Clock systemClock;
    private final TrackInfo trackInfo;

    public interface EventListener {
        void onSeekRangeChanged(TimeRange timeRange);
    }

    public static class NoAdaptationSetException extends IOException {
        public NoAdaptationSetException(String message) {
            super(message);
        }
    }

    private static class RepresentationHolder {
        public final ChunkExtractorWrapper extractorWrapper;
        public MediaFormat format;
        public final Representation representation;
        public DashSegmentIndex segmentIndex;
        public int segmentNumShift;
        public byte[] vttHeader;
        public long vttHeaderOffsetUs;

        public RepresentationHolder(Representation representation, ChunkExtractorWrapper extractorWrapper) {
            this.representation = representation;
            this.extractorWrapper = extractorWrapper;
            this.segmentIndex = representation.getIndex();
        }
    }

    public DashChunkSource(DataSource dataSource, FormatEvaluator formatEvaluator, Representation... representations) {
        this(buildManifest(Arrays.asList(representations)), 0, null, dataSource, formatEvaluator);
    }

    public DashChunkSource(DataSource dataSource, FormatEvaluator formatEvaluator, List<Representation> representations) {
        this(buildManifest(representations), 0, null, dataSource, formatEvaluator);
    }

    public DashChunkSource(MediaPresentationDescription manifest, int adaptationSetIndex, int[] representationIndices, DataSource dataSource, FormatEvaluator formatEvaluator) {
        this(null, manifest, adaptationSetIndex, representationIndices, dataSource, formatEvaluator, new SystemClock(), 0, 0, false, null, null);
    }

    public DashChunkSource(ManifestFetcher<MediaPresentationDescription> manifestFetcher, int adaptationSetIndex, int[] representationIndices, DataSource dataSource, FormatEvaluator formatEvaluator, long liveEdgeLatencyMs, long elapsedRealtimeOffsetMs, Handler eventHandler, EventListener eventListener) {
        this(manifestFetcher, (MediaPresentationDescription) manifestFetcher.getManifest(), adaptationSetIndex, representationIndices, dataSource, formatEvaluator, new SystemClock(), liveEdgeLatencyMs * 1000, elapsedRealtimeOffsetMs * 1000, true, eventHandler, eventListener);
    }

    public DashChunkSource(ManifestFetcher<MediaPresentationDescription> manifestFetcher, int adaptationSetIndex, int[] representationIndices, DataSource dataSource, FormatEvaluator formatEvaluator, long liveEdgeLatencyMs, long elapsedRealtimeOffsetMs, boolean startAtLiveEdge, Handler eventHandler, EventListener eventListener) {
        this(manifestFetcher, (MediaPresentationDescription) manifestFetcher.getManifest(), adaptationSetIndex, representationIndices, dataSource, formatEvaluator, new SystemClock(), liveEdgeLatencyMs * 1000, elapsedRealtimeOffsetMs * 1000, startAtLiveEdge, eventHandler, eventListener);
    }

    DashChunkSource(ManifestFetcher<MediaPresentationDescription> manifestFetcher, MediaPresentationDescription initialManifest, int adaptationSetIndex, int[] representationIndices, DataSource dataSource, FormatEvaluator formatEvaluator, Clock systemClock, long liveEdgeLatencyUs, long elapsedRealtimeOffsetUs, boolean startAtLiveEdge, Handler eventHandler, EventListener eventListener) {
        long periodDurationUs;
        this.manifestFetcher = manifestFetcher;
        this.currentManifest = initialManifest;
        this.adaptationSetIndex = adaptationSetIndex;
        this.representationIndices = representationIndices;
        this.dataSource = dataSource;
        this.formatEvaluator = formatEvaluator;
        this.systemClock = systemClock;
        this.liveEdgeLatencyUs = liveEdgeLatencyUs;
        this.elapsedRealtimeOffsetUs = elapsedRealtimeOffsetUs;
        this.startAtLiveEdge = startAtLiveEdge;
        this.eventHandler = eventHandler;
        this.eventListener = eventListener;
        this.evaluation = new Evaluation();
        this.headerBuilder = new StringBuilder();
        this.seekRangeValues = new long[2];
        this.drmInitData = getDrmInitData(this.currentManifest, adaptationSetIndex);
        Representation[] representations = getFilteredRepresentations(this.currentManifest, adaptationSetIndex, representationIndices);
        if (representations[0].periodDurationMs == -1) {
            periodDurationUs = -1;
        } else {
            periodDurationUs = representations[0].periodDurationMs * 1000;
        }
        this.trackInfo = new TrackInfo(representations[0].format.mimeType, periodDurationUs);
        this.formats = new Format[representations.length];
        this.representationHolders = new HashMap();
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < representations.length; i++) {
            this.formats[i] = representations[i].format;
            maxWidth = Math.max(this.formats[i].width, maxWidth);
            maxHeight = Math.max(this.formats[i].height, maxHeight);
            this.representationHolders.put(this.formats[i].id, new RepresentationHolder(representations[i], new ChunkExtractorWrapper(mimeTypeIsWebm(this.formats[i].mimeType) ? new WebmExtractor() : new FragmentedMp4Extractor())));
        }
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        Arrays.sort(this.formats, new DecreasingBandwidthComparator());
    }

    public final void getMaxVideoDimensions(MediaFormat out) {
        if (this.trackInfo.mimeType.startsWith(MimeTypes.BASE_TYPE_VIDEO)) {
            out.setMaxVideoDimensions(this.maxWidth, this.maxHeight);
        }
    }

    public final TrackInfo getTrackInfo() {
        return this.trackInfo;
    }

    TimeRange getSeekRange() {
        return this.seekRange;
    }

    public void enable() {
        this.fatalError = null;
        this.formatEvaluator.enable();
        if (this.manifestFetcher != null) {
            this.manifestFetcher.enable();
        }
        DashSegmentIndex segmentIndex = ((RepresentationHolder) this.representationHolders.get(this.formats[0].id)).representation.getIndex();
        if (segmentIndex == null) {
            this.seekRange = new TimeRange(0, 0, this.currentManifest.duration * 1000);
            notifySeekRangeChanged(this.seekRange);
            return;
        }
        long nowUs = getNowUs();
        updateAvailableSegmentBounds(segmentIndex, nowUs);
        updateSeekRange(segmentIndex, nowUs);
    }

    public void disable(List<? extends MediaChunk> list) {
        this.formatEvaluator.disable();
        if (this.manifestFetcher != null) {
            this.manifestFetcher.disable();
        }
        this.seekRange = null;
    }

    public void continueBuffering(long playbackPositionUs) {
        if (this.manifestFetcher != null && this.currentManifest.dynamic && this.fatalError == null) {
            MediaPresentationDescription newManifest = (MediaPresentationDescription) this.manifestFetcher.getManifest();
            if (!(this.currentManifest == newManifest || newManifest == null)) {
                Representation[] newRepresentations = getFilteredRepresentations(newManifest, this.adaptationSetIndex, this.representationIndices);
                for (Representation representation : newRepresentations) {
                    RepresentationHolder representationHolder = (RepresentationHolder) this.representationHolders.get(representation.format.id);
                    DashSegmentIndex oldIndex = representationHolder.segmentIndex;
                    int oldIndexLastSegmentNum = oldIndex.getLastSegmentNum();
                    long oldIndexEndTimeUs = oldIndex.getTimeUs(oldIndexLastSegmentNum) + oldIndex.getDurationUs(oldIndexLastSegmentNum);
                    DashSegmentIndex newIndex = representation.getIndex();
                    int newIndexFirstSegmentNum = newIndex.getFirstSegmentNum();
                    long newIndexStartTimeUs = newIndex.getTimeUs(newIndexFirstSegmentNum);
                    if (oldIndexEndTimeUs < newIndexStartTimeUs) {
                        this.fatalError = new BehindLiveWindowException();
                        return;
                    }
                    int segmentNumShift;
                    if (oldIndexEndTimeUs == newIndexStartTimeUs) {
                        segmentNumShift = (oldIndex.getLastSegmentNum() + 1) - newIndexFirstSegmentNum;
                    } else {
                        segmentNumShift = oldIndex.getSegmentNum(newIndexStartTimeUs) - newIndexFirstSegmentNum;
                    }
                    representationHolder.segmentNumShift += segmentNumShift;
                    representationHolder.segmentIndex = newIndex;
                }
                this.currentManifest = newManifest;
                this.finishedCurrentManifest = false;
                long nowUs = getNowUs();
                updateAvailableSegmentBounds(newRepresentations[0].getIndex(), nowUs);
                updateSeekRange(newRepresentations[0].getIndex(), nowUs);
            }
            long minUpdatePeriod = this.currentManifest.minUpdatePeriod;
            if (minUpdatePeriod == 0) {
                minUpdatePeriod = Constants.NOTIFICATION_BANNER_TIMEOUT;
            }
            if (this.finishedCurrentManifest && android.os.SystemClock.elapsedRealtime() > this.manifestFetcher.getManifestLoadTimestamp() + minUpdatePeriod) {
                this.manifestFetcher.requestRefresh();
            }
        }
    }

    public final void getChunkOperation(List<? extends MediaChunk> queue, long seekPositionUs, long playbackPositionUs, ChunkOperationHolder out) {
        if (this.fatalError != null) {
            out.chunk = null;
            return;
        }
        this.evaluation.queueSize = queue.size();
        if (this.evaluation.format == null || !this.lastChunkWasInitialization) {
            this.formatEvaluator.evaluate(queue, playbackPositionUs, this.formats, this.evaluation);
        }
        Format selectedFormat = this.evaluation.format;
        out.queueSize = this.evaluation.queueSize;
        if (selectedFormat == null) {
            out.chunk = null;
        } else if (out.queueSize != queue.size() || out.chunk == null || !out.chunk.format.equals(selectedFormat)) {
            out.chunk = null;
            RepresentationHolder representationHolder = (RepresentationHolder) this.representationHolders.get(selectedFormat.id);
            Representation selectedRepresentation = representationHolder.representation;
            DashSegmentIndex segmentIndex = representationHolder.segmentIndex;
            ChunkExtractorWrapper extractorWrapper = representationHolder.extractorWrapper;
            RangedUri pendingInitializationUri = null;
            RangedUri pendingIndexUri = null;
            if (representationHolder.format == null) {
                pendingInitializationUri = selectedRepresentation.getInitializationUri();
            }
            if (segmentIndex == null) {
                pendingIndexUri = selectedRepresentation.getIndexUri();
            }
            if (pendingInitializationUri == null && pendingIndexUri == null) {
                int segmentNum;
                boolean indexUnbounded = segmentIndex.getLastSegmentNum() == USE_ALL_TRACKS;
                if (indexUnbounded) {
                    long nowUs = getNowUs();
                    int oldFirstAvailableSegmentNum = this.firstAvailableSegmentNum;
                    int oldLastAvailableSegmentNum = this.lastAvailableSegmentNum;
                    updateAvailableSegmentBounds(segmentIndex, nowUs);
                    if (!(oldFirstAvailableSegmentNum == this.firstAvailableSegmentNum && oldLastAvailableSegmentNum == this.lastAvailableSegmentNum)) {
                        updateSeekRange(segmentIndex, nowUs);
                    }
                }
                if (queue.isEmpty()) {
                    if (this.currentManifest.dynamic) {
                        this.seekRangeValues = this.seekRange.getCurrentBoundsUs(this.seekRangeValues);
                        if (this.startAtLiveEdge) {
                            this.startAtLiveEdge = false;
                            seekPositionUs = this.seekRangeValues[1];
                        } else {
                            seekPositionUs = Math.min(Math.max(seekPositionUs, this.seekRangeValues[0]), this.seekRangeValues[1]);
                        }
                    }
                    segmentNum = segmentIndex.getSegmentNum(seekPositionUs);
                    if (indexUnbounded) {
                        segmentNum = Math.min(segmentNum, this.lastAvailableSegmentNum);
                    }
                } else {
                    MediaChunk previous = (MediaChunk) queue.get(out.queueSize + USE_ALL_TRACKS);
                    segmentNum = previous.isLastChunk ? USE_ALL_TRACKS : (previous.chunkIndex + 1) - representationHolder.segmentNumShift;
                }
                if (this.currentManifest.dynamic) {
                    if (segmentNum < this.firstAvailableSegmentNum) {
                        this.fatalError = new BehindLiveWindowException();
                        return;
                    } else if (segmentNum > this.lastAvailableSegmentNum) {
                        this.finishedCurrentManifest = !indexUnbounded;
                        return;
                    } else if (!indexUnbounded && segmentNum == this.lastAvailableSegmentNum) {
                        this.finishedCurrentManifest = true;
                    }
                }
                if (segmentNum != USE_ALL_TRACKS) {
                    Chunk nextMediaChunk = newMediaChunk(representationHolder, this.dataSource, segmentNum, this.evaluation.trigger);
                    this.lastChunkWasInitialization = false;
                    out.chunk = nextMediaChunk;
                    return;
                }
                return;
            }
            Chunk initializationChunk = newInitializationChunk(pendingInitializationUri, pendingIndexUri, selectedRepresentation, extractorWrapper, this.dataSource, this.evaluation.trigger);
            this.lastChunkWasInitialization = true;
            out.chunk = initializationChunk;
        }
    }

    public IOException getError() {
        if (this.fatalError != null) {
            return this.fatalError;
        }
        return this.manifestFetcher != null ? this.manifestFetcher.getError() : null;
    }

    public void onChunkLoadCompleted(Chunk chunk) {
        if (chunk instanceof InitializationChunk) {
            InitializationChunk initializationChunk = (InitializationChunk) chunk;
            RepresentationHolder representationHolder = (RepresentationHolder) this.representationHolders.get(initializationChunk.format.id);
            if (initializationChunk.hasFormat()) {
                representationHolder.format = initializationChunk.getFormat();
            }
            if (initializationChunk.hasSeekMap()) {
                representationHolder.segmentIndex = new DashWrappingSegmentIndex((ChunkIndex) initializationChunk.getSeekMap(), initializationChunk.dataSpec.uri.toString(), representationHolder.representation.periodStartMs * 1000);
            }
            if (this.drmInitData == null && initializationChunk.hasDrmInitData()) {
                this.drmInitData = initializationChunk.getDrmInitData();
            }
        }
    }

    public void onChunkLoadError(Chunk chunk, Exception e) {
    }

    private void updateAvailableSegmentBounds(DashSegmentIndex segmentIndex, long nowUs) {
        int indexFirstAvailableSegmentNum = segmentIndex.getFirstSegmentNum();
        int indexLastAvailableSegmentNum = segmentIndex.getLastSegmentNum();
        if (indexLastAvailableSegmentNum == USE_ALL_TRACKS) {
            long liveEdgeTimestampUs = nowUs - (this.currentManifest.availabilityStartTime * 1000);
            if (this.currentManifest.timeShiftBufferDepth != -1) {
                indexFirstAvailableSegmentNum = Math.max(indexFirstAvailableSegmentNum, segmentIndex.getSegmentNum(liveEdgeTimestampUs - (this.currentManifest.timeShiftBufferDepth * 1000)));
            }
            indexLastAvailableSegmentNum = segmentIndex.getSegmentNum(liveEdgeTimestampUs) + USE_ALL_TRACKS;
        }
        this.firstAvailableSegmentNum = indexFirstAvailableSegmentNum;
        this.lastAvailableSegmentNum = indexLastAvailableSegmentNum;
    }

    private void updateSeekRange(DashSegmentIndex segmentIndex, long nowUs) {
        long earliestSeekPosition = segmentIndex.getTimeUs(this.firstAvailableSegmentNum);
        long latestSeekPosition = segmentIndex.getTimeUs(this.lastAvailableSegmentNum) + segmentIndex.getDurationUs(this.lastAvailableSegmentNum);
        if (this.currentManifest.dynamic) {
            long liveEdgeTimestampUs;
            if (segmentIndex.getLastSegmentNum() == USE_ALL_TRACKS) {
                liveEdgeTimestampUs = nowUs - (this.currentManifest.availabilityStartTime * 1000);
            } else {
                liveEdgeTimestampUs = segmentIndex.getTimeUs(segmentIndex.getLastSegmentNum()) + segmentIndex.getDurationUs(segmentIndex.getLastSegmentNum());
                if (!segmentIndex.isExplicit()) {
                    liveEdgeTimestampUs = Math.min(liveEdgeTimestampUs, nowUs - (this.currentManifest.availabilityStartTime * 1000));
                }
            }
            latestSeekPosition = Math.max(earliestSeekPosition, liveEdgeTimestampUs - this.liveEdgeLatencyUs);
        }
        TimeRange newSeekRange = new TimeRange(0, earliestSeekPosition, latestSeekPosition);
        if (this.seekRange == null || !this.seekRange.equals(newSeekRange)) {
            this.seekRange = newSeekRange;
            notifySeekRangeChanged(this.seekRange);
        }
    }

    private static boolean mimeTypeIsWebm(String mimeType) {
        return mimeType.startsWith(MimeTypes.VIDEO_WEBM) || mimeType.startsWith(MimeTypes.AUDIO_WEBM);
    }

    private Chunk newInitializationChunk(RangedUri initializationUri, RangedUri indexUri, Representation representation, ChunkExtractorWrapper extractor, DataSource dataSource, int trigger) {
        RangedUri requestUri;
        if (initializationUri != null) {
            requestUri = initializationUri.attemptMerge(indexUri);
            if (requestUri == null) {
                requestUri = initializationUri;
            }
        } else {
            requestUri = indexUri;
        }
        return new InitializationChunk(dataSource, new DataSpec(requestUri.getUri(), requestUri.start, requestUri.length, representation.getCacheKey()), trigger, representation.format, extractor);
    }

    private Chunk newMediaChunk(RepresentationHolder representationHolder, DataSource dataSource, int segmentNum, int trigger) {
        Representation representation = representationHolder.representation;
        DashSegmentIndex segmentIndex = representationHolder.segmentIndex;
        long startTimeUs = segmentIndex.getTimeUs(segmentNum);
        long endTimeUs = startTimeUs + segmentIndex.getDurationUs(segmentNum);
        int absoluteSegmentNum = segmentNum + representationHolder.segmentNumShift;
        boolean isLastSegment = !this.currentManifest.dynamic && segmentNum == segmentIndex.getLastSegmentNum();
        RangedUri segmentUri = segmentIndex.getSegmentUrl(segmentNum);
        DataSpec dataSpec = new DataSpec(segmentUri.getUri(), segmentUri.start, segmentUri.length, representation.getCacheKey());
        long sampleOffsetUs = (representation.periodStartMs * 1000) - representation.presentationTimeOffsetUs;
        if (representation.format.mimeType.equals(MimeTypes.TEXT_VTT)) {
            if (representationHolder.vttHeaderOffsetUs != sampleOffsetUs) {
                this.headerBuilder.setLength(0);
                this.headerBuilder.append(C.WEBVTT_EXO_HEADER).append("=").append(C.WEBVTT_EXO_HEADER_OFFSET).append(sampleOffsetUs).append("\n");
                representationHolder.vttHeader = this.headerBuilder.toString().getBytes();
                representationHolder.vttHeaderOffsetUs = sampleOffsetUs;
            }
            return new SingleSampleMediaChunk(dataSource, dataSpec, 1, representation.format, startTimeUs, endTimeUs, absoluteSegmentNum, isLastSegment, MediaFormat.createTextFormat(MimeTypes.TEXT_VTT), null, representationHolder.vttHeader);
        }
        return new ContainerMediaChunk(dataSource, dataSpec, trigger, representation.format, startTimeUs, endTimeUs, absoluteSegmentNum, isLastSegment, sampleOffsetUs, representationHolder.extractorWrapper, representationHolder.format, this.drmInitData, true);
    }

    private long getNowUs() {
        if (this.elapsedRealtimeOffsetUs != 0) {
            return (this.systemClock.elapsedRealtime() * 1000) + this.elapsedRealtimeOffsetUs;
        }
        return System.currentTimeMillis() * 1000;
    }

    private static Representation[] getFilteredRepresentations(MediaPresentationDescription manifest, int adaptationSetIndex, int[] representationIndices) {
        List<Representation> representations = ((AdaptationSet) ((Period) manifest.periods.get(0)).adaptationSets.get(adaptationSetIndex)).representations;
        if (representationIndices == null) {
            Representation[] filteredRepresentations = new Representation[representations.size()];
            representations.toArray(filteredRepresentations);
            return filteredRepresentations;
        }
        filteredRepresentations = new Representation[representationIndices.length];
        for (int i = 0; i < representationIndices.length; i++) {
            filteredRepresentations[i] = (Representation) representations.get(representationIndices[i]);
        }
        return filteredRepresentations;
    }

    private static DrmInitData getDrmInitData(MediaPresentationDescription manifest, int adaptationSetIndex) {
        AdaptationSet adaptationSet = (AdaptationSet) ((Period) manifest.periods.get(0)).adaptationSets.get(adaptationSetIndex);
        String drmInitMimeType = mimeTypeIsWebm(((Representation) adaptationSet.representations.get(0)).format.mimeType) ? MimeTypes.VIDEO_WEBM : MimeTypes.VIDEO_MP4;
        if (adaptationSet.contentProtections.isEmpty()) {
            return null;
        }
        DrmInitData drmInitData = null;
        for (ContentProtection contentProtection : adaptationSet.contentProtections) {
            if (!(contentProtection.uuid == null || contentProtection.data == null)) {
                if (drmInitData == null) {
                    drmInitData = new Mapped(drmInitMimeType);
                }
                drmInitData.put(contentProtection.uuid, contentProtection.data);
            }
        }
        return drmInitData;
    }

    private static MediaPresentationDescription buildManifest(List<Representation> representations) {
        Representation firstRepresentation = (Representation) representations.get(0);
        return new MediaPresentationDescription(-1, firstRepresentation.periodDurationMs - firstRepresentation.periodStartMs, -1, false, -1, -1, null, null, Collections.singletonList(new Period(null, firstRepresentation.periodStartMs, firstRepresentation.periodDurationMs, Collections.singletonList(new AdaptationSet(0, USE_ALL_TRACKS, representations)))));
    }

    private void notifySeekRangeChanged(final TimeRange seekRange) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    DashChunkSource.this.eventListener.onSeekRangeChanged(seekRange);
                }
            });
        }
    }
}
