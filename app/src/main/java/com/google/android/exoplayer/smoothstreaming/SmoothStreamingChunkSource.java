package com.google.android.exoplayer.smoothstreaming;

import android.net.Uri;
import android.os.SystemClock;
import android.util.Base64;
import android.util.SparseArray;
import com.google.android.exoplayer.BehindLiveWindowException;
import com.google.android.exoplayer.MediaFormat;
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
import com.google.android.exoplayer.chunk.MediaChunk;
import com.google.android.exoplayer.drm.DrmInitData;
import com.google.android.exoplayer.drm.DrmInitData.Mapped;
import com.google.android.exoplayer.extractor.mp4.FragmentedMp4Extractor;
import com.google.android.exoplayer.extractor.mp4.Track;
import com.google.android.exoplayer.extractor.mp4.TrackEncryptionBox;
import com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifest.ProtectionElement;
import com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifest.StreamElement;
import com.google.android.exoplayer.smoothstreaming.SmoothStreamingManifest.TrackElement;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.util.CodecSpecificDataUtil;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.MimeTypes;
import com.mobcrush.mobcrush.Constants;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SmoothStreamingChunkSource implements ChunkSource {
    private static final int INITIALIZATION_VECTOR_SIZE = 8;
    private static final int MINIMUM_MANIFEST_REFRESH_PERIOD_MS = 5000;
    private SmoothStreamingManifest currentManifest;
    private int currentManifestChunkOffset;
    private final DataSource dataSource;
    private final DrmInitData drmInitData;
    private final Evaluation evaluation;
    private final SparseArray<ChunkExtractorWrapper> extractorWrappers;
    private IOException fatalError;
    private boolean finishedCurrentManifest;
    private final FormatEvaluator formatEvaluator;
    private final Format[] formats;
    private final long liveEdgeLatencyUs;
    private final ManifestFetcher<SmoothStreamingManifest> manifestFetcher;
    private final int maxHeight;
    private final int maxWidth;
    private final SparseArray<MediaFormat> mediaFormats;
    private final int streamElementIndex;
    private final TrackInfo trackInfo;

    public SmoothStreamingChunkSource(ManifestFetcher<SmoothStreamingManifest> manifestFetcher, int streamElementIndex, int[] trackIndices, DataSource dataSource, FormatEvaluator formatEvaluator, long liveEdgeLatencyMs) {
        this(manifestFetcher, (SmoothStreamingManifest) manifestFetcher.getManifest(), streamElementIndex, trackIndices, dataSource, formatEvaluator, liveEdgeLatencyMs);
    }

    public SmoothStreamingChunkSource(SmoothStreamingManifest manifest, int streamElementIndex, int[] trackIndices, DataSource dataSource, FormatEvaluator formatEvaluator) {
        this(null, manifest, streamElementIndex, trackIndices, dataSource, formatEvaluator, 0);
    }

    private SmoothStreamingChunkSource(ManifestFetcher<SmoothStreamingManifest> manifestFetcher, SmoothStreamingManifest initialManifest, int streamElementIndex, int[] trackIndices, DataSource dataSource, FormatEvaluator formatEvaluator, long liveEdgeLatencyMs) {
        this.manifestFetcher = manifestFetcher;
        this.streamElementIndex = streamElementIndex;
        this.currentManifest = initialManifest;
        this.dataSource = dataSource;
        this.formatEvaluator = formatEvaluator;
        this.liveEdgeLatencyUs = 1000 * liveEdgeLatencyMs;
        StreamElement streamElement = getElement(initialManifest);
        this.trackInfo = new TrackInfo(streamElement.tracks[0].format.mimeType, initialManifest.durationUs);
        this.evaluation = new Evaluation();
        TrackEncryptionBox[] trackEncryptionBoxes = null;
        ProtectionElement protectionElement = initialManifest.protectionElement;
        if (protectionElement != null) {
            trackEncryptionBoxes = new TrackEncryptionBox[]{new TrackEncryptionBox(true, INITIALIZATION_VECTOR_SIZE, getKeyId(protectionElement.data))};
            Mapped drmInitData = new Mapped(MimeTypes.VIDEO_MP4);
            drmInitData.put(protectionElement.uuid, protectionElement.data);
            this.drmInitData = drmInitData;
        } else {
            this.drmInitData = null;
        }
        int trackCount = trackIndices != null ? trackIndices.length : streamElement.tracks.length;
        this.formats = new Format[trackCount];
        this.extractorWrappers = new SparseArray();
        this.mediaFormats = new SparseArray();
        int maxWidth = 0;
        int maxHeight = 0;
        for (int i = 0; i < trackCount; i++) {
            int trackIndex;
            if (trackIndices != null) {
                trackIndex = trackIndices[i];
            } else {
                trackIndex = i;
            }
            this.formats[i] = streamElement.tracks[trackIndex].format;
            maxWidth = Math.max(maxWidth, this.formats[i].width);
            maxHeight = Math.max(maxHeight, this.formats[i].height);
            MediaFormat mediaFormat = getMediaFormat(streamElement, trackIndex);
            int trackType = streamElement.type == 1 ? Track.TYPE_VIDEO : Track.TYPE_AUDIO;
            FragmentedMp4Extractor extractor = new FragmentedMp4Extractor(1);
            extractor.setTrack(new Track(trackIndex, trackType, streamElement.timescale, initialManifest.durationUs, mediaFormat, trackEncryptionBoxes, trackType == Track.TYPE_VIDEO ? 4 : -1));
            this.extractorWrappers.put(trackIndex, new ChunkExtractorWrapper(extractor));
            this.mediaFormats.put(trackIndex, mediaFormat);
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

    public void enable() {
        this.fatalError = null;
        this.formatEvaluator.enable();
        if (this.manifestFetcher != null) {
            this.manifestFetcher.enable();
        }
    }

    public void disable(List<? extends MediaChunk> list) {
        this.formatEvaluator.disable();
        if (this.manifestFetcher != null) {
            this.manifestFetcher.disable();
        }
    }

    public void continueBuffering(long playbackPositionUs) {
        if (this.manifestFetcher != null && this.currentManifest.isLive && this.fatalError == null) {
            SmoothStreamingManifest newManifest = (SmoothStreamingManifest) this.manifestFetcher.getManifest();
            if (!(this.currentManifest == newManifest || newManifest == null)) {
                StreamElement currentElement = getElement(this.currentManifest);
                int currentElementChunkCount = currentElement.chunkCount;
                StreamElement newElement = getElement(newManifest);
                if (currentElementChunkCount == 0 || newElement.chunkCount == 0) {
                    this.currentManifestChunkOffset += currentElementChunkCount;
                } else {
                    long currentElementEndTimeUs = currentElement.getStartTimeUs(currentElementChunkCount - 1) + currentElement.getChunkDurationUs(currentElementChunkCount - 1);
                    long newElementStartTimeUs = newElement.getStartTimeUs(0);
                    if (currentElementEndTimeUs <= newElementStartTimeUs) {
                        this.currentManifestChunkOffset += currentElementChunkCount;
                    } else {
                        this.currentManifestChunkOffset += currentElement.getChunkIndex(newElementStartTimeUs);
                    }
                }
                this.currentManifest = newManifest;
                this.finishedCurrentManifest = false;
            }
            if (this.finishedCurrentManifest && SystemClock.elapsedRealtime() > this.manifestFetcher.getManifestLoadTimestamp() + Constants.NOTIFICATION_BANNER_TIMEOUT) {
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
        this.formatEvaluator.evaluate(queue, playbackPositionUs, this.formats, this.evaluation);
        Format selectedFormat = this.evaluation.format;
        out.queueSize = this.evaluation.queueSize;
        if (selectedFormat == null) {
            out.chunk = null;
        } else if (out.queueSize != queue.size() || out.chunk == null || !out.chunk.format.equals(selectedFormat)) {
            out.chunk = null;
            StreamElement streamElement = getElement(this.currentManifest);
            if (streamElement.chunkCount == 0) {
                this.finishedCurrentManifest = true;
                return;
            }
            int chunkIndex;
            if (queue.isEmpty()) {
                if (this.currentManifest.isLive) {
                    seekPositionUs = getLiveSeekPosition();
                }
                chunkIndex = streamElement.getChunkIndex(seekPositionUs);
            } else {
                MediaChunk previous = (MediaChunk) queue.get(out.queueSize - 1);
                chunkIndex = previous.isLastChunk ? -1 : (previous.chunkIndex + 1) - this.currentManifestChunkOffset;
            }
            if (this.currentManifest.isLive) {
                if (chunkIndex < 0) {
                    this.fatalError = new BehindLiveWindowException();
                    return;
                } else if (chunkIndex >= streamElement.chunkCount) {
                    this.finishedCurrentManifest = true;
                    return;
                } else if (chunkIndex == streamElement.chunkCount - 1) {
                    this.finishedCurrentManifest = true;
                }
            }
            if (chunkIndex != -1) {
                boolean isLastChunk = !this.currentManifest.isLive && chunkIndex == streamElement.chunkCount - 1;
                long chunkStartTimeUs = streamElement.getStartTimeUs(chunkIndex);
                long chunkEndTimeUs = isLastChunk ? -1 : chunkStartTimeUs + streamElement.getChunkDurationUs(chunkIndex);
                int currentAbsoluteChunkIndex = chunkIndex + this.currentManifestChunkOffset;
                int trackIndex = getTrackIndex(selectedFormat);
                out.chunk = newMediaChunk(selectedFormat, streamElement.buildRequestUri(trackIndex, chunkIndex), null, (ChunkExtractorWrapper) this.extractorWrappers.get(trackIndex), this.drmInitData, this.dataSource, currentAbsoluteChunkIndex, isLastChunk, chunkStartTimeUs, chunkEndTimeUs, this.evaluation.trigger, (MediaFormat) this.mediaFormats.get(trackIndex));
            }
        }
    }

    public IOException getError() {
        if (this.fatalError != null) {
            return this.fatalError;
        }
        return this.manifestFetcher != null ? this.manifestFetcher.getError() : null;
    }

    public void onChunkLoadCompleted(Chunk chunk) {
    }

    public void onChunkLoadError(Chunk chunk, Exception e) {
    }

    private long getLiveSeekPosition() {
        long liveEdgeTimestampUs = Long.MIN_VALUE;
        for (StreamElement streamElement : this.currentManifest.streamElements) {
            if (streamElement.chunkCount > 0) {
                liveEdgeTimestampUs = Math.max(liveEdgeTimestampUs, streamElement.getStartTimeUs(streamElement.chunkCount - 1) + streamElement.getChunkDurationUs(streamElement.chunkCount - 1));
            }
        }
        return liveEdgeTimestampUs - this.liveEdgeLatencyUs;
    }

    private StreamElement getElement(SmoothStreamingManifest manifest) {
        return manifest.streamElements[this.streamElementIndex];
    }

    private int getTrackIndex(Format format) {
        TrackElement[] tracks = this.currentManifest.streamElements[this.streamElementIndex].tracks;
        for (int i = 0; i < tracks.length; i++) {
            if (tracks[i].format.equals(format)) {
                return i;
            }
        }
        throw new IllegalStateException("Invalid format: " + format);
    }

    private static MediaFormat getMediaFormat(StreamElement streamElement, int trackIndex) {
        TrackElement trackElement = streamElement.tracks[trackIndex];
        Format trackFormat = trackElement.format;
        String mimeType = trackFormat.mimeType;
        if (streamElement.type == 1) {
            MediaFormat format = MediaFormat.createVideoFormat(mimeType, -1, trackFormat.width, trackFormat.height, Arrays.asList(trackElement.csd));
            format.setMaxVideoDimensions(streamElement.maxWidth, streamElement.maxHeight);
            return format;
        } else if (streamElement.type == 0) {
            List<byte[]> csd;
            if (trackElement.csd != null) {
                csd = Arrays.asList(trackElement.csd);
            } else {
                csd = Collections.singletonList(CodecSpecificDataUtil.buildAacAudioSpecificConfig(trackFormat.audioSamplingRate, trackFormat.numChannels));
            }
            return MediaFormat.createAudioFormat(mimeType, -1, trackFormat.numChannels, trackFormat.audioSamplingRate, csd);
        } else if (streamElement.type == 2) {
            return MediaFormat.createTextFormat(trackFormat.mimeType);
        } else {
            return null;
        }
    }

    private static MediaChunk newMediaChunk(Format formatInfo, Uri uri, String cacheKey, ChunkExtractorWrapper extractorWrapper, DrmInitData drmInitData, DataSource dataSource, int chunkIndex, boolean isLast, long chunkStartTimeUs, long chunkEndTimeUs, int trigger, MediaFormat mediaFormat) {
        return new ContainerMediaChunk(dataSource, new DataSpec(uri, 0, -1, cacheKey), trigger, formatInfo, chunkStartTimeUs, chunkEndTimeUs, chunkIndex, isLast, chunkStartTimeUs, extractorWrapper, mediaFormat, drmInitData, true);
    }

    private static byte[] getKeyId(byte[] initData) {
        StringBuilder initDataStringBuilder = new StringBuilder();
        for (int i = 0; i < initData.length; i += 2) {
            initDataStringBuilder.append((char) initData[i]);
        }
        String initDataString = initDataStringBuilder.toString();
        byte[] keyId = Base64.decode(initDataString.substring(initDataString.indexOf("<KID>") + 5, initDataString.indexOf("</KID>")), 0);
        swap(keyId, 0, 3);
        swap(keyId, 1, 2);
        swap(keyId, 4, 5);
        swap(keyId, 6, 7);
        return keyId;
    }

    private static void swap(byte[] data, int firstPosition, int secondPosition) {
        byte temp = data[firstPosition];
        data[firstPosition] = data[secondPosition];
        data[secondPosition] = temp;
    }
}
