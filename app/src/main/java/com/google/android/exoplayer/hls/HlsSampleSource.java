package com.google.android.exoplayer.hls;

import android.os.Handler;
import android.os.SystemClock;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.MediaFormatHolder;
import com.google.android.exoplayer.SampleHolder;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.SampleSource.SampleSourceReader;
import com.google.android.exoplayer.TrackInfo;
import com.google.android.exoplayer.chunk.BaseChunkSampleSourceEventListener;
import com.google.android.exoplayer.chunk.Chunk;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.upstream.Loader;
import com.google.android.exoplayer.upstream.Loader.Callback;
import com.google.android.exoplayer.upstream.Loader.Loadable;
import com.google.android.exoplayer.util.Assertions;
import com.mobcrush.mobcrush.Constants;
import java.io.IOException;
import java.util.LinkedList;

public class HlsSampleSource implements SampleSource, SampleSourceReader, Callback {
    public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT = 3;
    private static final int NO_RESET_PENDING = -1;
    private final int bufferSizeContribution;
    private final HlsChunkSource chunkSource;
    private long currentLoadStartTimeMs;
    private Chunk currentLoadable;
    private IOException currentLoadableException;
    private int currentLoadableExceptionCount;
    private long currentLoadableExceptionTimestamp;
    private TsChunk currentTsLoadable;
    private Format downstreamFormat;
    private MediaFormat[] downstreamMediaFormats;
    private long downstreamPositionUs;
    private int enabledTrackCount;
    private final Handler eventHandler;
    private final EventListener eventListener;
    private final int eventSourceId;
    private final LinkedList<HlsExtractorWrapper> extractors;
    private final boolean frameAccurateSeeking;
    private long lastSeekPositionUs;
    private final LoadControl loadControl;
    private boolean loadControlRegistered;
    private Loader loader;
    private boolean loadingFinished;
    private final int minLoadableRetryCount;
    private boolean[] pendingDiscontinuities;
    private long pendingResetPositionUs;
    private boolean prepared;
    private TsChunk previousTsLoadable;
    private int remainingReleaseCount;
    private int trackCount;
    private boolean[] trackEnabledStates;
    private TrackInfo[] trackInfos;

    public interface EventListener extends BaseChunkSampleSourceEventListener {
    }

    public HlsSampleSource(HlsChunkSource chunkSource, LoadControl loadControl, int bufferSizeContribution, boolean frameAccurateSeeking) {
        this(chunkSource, loadControl, bufferSizeContribution, frameAccurateSeeking, null, null, 0);
    }

    public HlsSampleSource(HlsChunkSource chunkSource, LoadControl loadControl, int bufferSizeContribution, boolean frameAccurateSeeking, Handler eventHandler, EventListener eventListener, int eventSourceId) {
        this(chunkSource, loadControl, bufferSizeContribution, frameAccurateSeeking, eventHandler, eventListener, eventSourceId, DEFAULT_MIN_LOADABLE_RETRY_COUNT);
    }

    public HlsSampleSource(HlsChunkSource chunkSource, LoadControl loadControl, int bufferSizeContribution, boolean frameAccurateSeeking, Handler eventHandler, EventListener eventListener, int eventSourceId, int minLoadableRetryCount) {
        this.chunkSource = chunkSource;
        this.loadControl = loadControl;
        this.bufferSizeContribution = bufferSizeContribution;
        this.frameAccurateSeeking = frameAccurateSeeking;
        this.minLoadableRetryCount = minLoadableRetryCount;
        this.eventHandler = eventHandler;
        this.eventListener = eventListener;
        this.eventSourceId = eventSourceId;
        this.pendingResetPositionUs = -1;
        this.extractors = new LinkedList();
    }

    public SampleSourceReader register() {
        this.remainingReleaseCount++;
        return this;
    }

    public boolean prepare(long positionUs) throws IOException {
        if (this.prepared) {
            return true;
        }
        if (!this.extractors.isEmpty()) {
            HlsExtractorWrapper extractor = getCurrentExtractor();
            if (extractor.isPrepared()) {
                this.trackCount = extractor.getTrackCount();
                this.trackEnabledStates = new boolean[this.trackCount];
                this.pendingDiscontinuities = new boolean[this.trackCount];
                this.downstreamMediaFormats = new MediaFormat[this.trackCount];
                this.trackInfos = new TrackInfo[this.trackCount];
                for (int i = 0; i < this.trackCount; i++) {
                    this.trackInfos[i] = new TrackInfo(extractor.getMediaFormat(i).mimeType, this.chunkSource.getDurationUs());
                }
                this.prepared = true;
                return true;
            }
        }
        if (this.loader == null) {
            this.loader = new Loader("Loader:HLS");
        }
        if (!this.loadControlRegistered) {
            this.loadControl.register(this, this.bufferSizeContribution);
            this.loadControlRegistered = true;
        }
        if (!this.loader.isLoading()) {
            this.pendingResetPositionUs = positionUs;
            this.downstreamPositionUs = positionUs;
        }
        maybeStartLoading();
        maybeThrowLoadableException();
        return false;
    }

    public int getTrackCount() {
        Assertions.checkState(this.prepared);
        return this.trackCount;
    }

    public TrackInfo getTrackInfo(int track) {
        Assertions.checkState(this.prepared);
        return this.trackInfos[track];
    }

    public void enable(int track, long positionUs) {
        Assertions.checkState(this.prepared);
        Assertions.checkState(!this.trackEnabledStates[track]);
        this.enabledTrackCount++;
        this.trackEnabledStates[track] = true;
        this.downstreamMediaFormats[track] = null;
        this.downstreamFormat = null;
        if (!this.loadControlRegistered) {
            this.loadControl.register(this, this.bufferSizeContribution);
            this.loadControlRegistered = true;
        }
        if (this.enabledTrackCount == 1) {
            seekToUs(positionUs);
        }
        this.pendingDiscontinuities[track] = false;
    }

    public void disable(int track) {
        Assertions.checkState(this.prepared);
        Assertions.checkState(this.trackEnabledStates[track]);
        this.enabledTrackCount += NO_RESET_PENDING;
        this.trackEnabledStates[track] = false;
        if (this.enabledTrackCount == 0) {
            this.downstreamPositionUs = Long.MIN_VALUE;
            if (this.loadControlRegistered) {
                this.loadControl.unregister(this);
                this.loadControlRegistered = false;
            }
            if (this.loader.isLoading()) {
                this.loader.cancelLoading();
                return;
            }
            clearState();
            this.loadControl.trimAllocator();
        }
    }

    public boolean continueBuffering(long playbackPositionUs) throws IOException {
        Assertions.checkState(this.prepared);
        Assertions.checkState(this.enabledTrackCount > 0);
        this.downstreamPositionUs = playbackPositionUs;
        if (!this.extractors.isEmpty()) {
            discardSamplesForDisabledTracks(getCurrentExtractor(), this.downstreamPositionUs);
        }
        if (this.loadingFinished || continueBufferingInternal()) {
            return true;
        }
        return false;
    }

    private boolean continueBufferingInternal() throws IOException {
        boolean haveSamples = false;
        maybeStartLoading();
        if (!(isPendingReset() || this.extractors.isEmpty())) {
            if (this.prepared && haveSamplesForEnabledTracks(getCurrentExtractor())) {
                haveSamples = true;
            }
            if (!haveSamples) {
                maybeThrowLoadableException();
            }
        }
        return haveSamples;
    }

    public int readData(int track, long playbackPositionUs, MediaFormatHolder formatHolder, SampleHolder sampleHolder, boolean onlyReadDiscontinuity) throws IOException {
        Assertions.checkState(this.prepared);
        this.downstreamPositionUs = playbackPositionUs;
        if (this.pendingDiscontinuities[track]) {
            this.pendingDiscontinuities[track] = false;
            return -5;
        } else if (onlyReadDiscontinuity) {
            return -2;
        } else {
            if (isPendingReset()) {
                maybeThrowLoadableException();
                return -2;
            }
            HlsExtractorWrapper extractor = getCurrentExtractor();
            if (extractor.isPrepared()) {
                if (this.downstreamFormat == null || !this.downstreamFormat.equals(extractor.format)) {
                    notifyDownstreamFormatChanged(extractor.format, extractor.trigger, extractor.startTimeUs);
                    this.downstreamFormat = extractor.format;
                }
                if (this.extractors.size() > 1) {
                    extractor.configureSpliceTo((HlsExtractorWrapper) this.extractors.get(1));
                }
                int extractorIndex = 0;
                while (this.extractors.size() > extractorIndex + 1 && !extractor.hasSamples(track)) {
                    extractorIndex++;
                    extractor = (HlsExtractorWrapper) this.extractors.get(extractorIndex);
                    if (!extractor.isPrepared()) {
                        maybeThrowLoadableException();
                        return -2;
                    }
                }
                MediaFormat mediaFormat = extractor.getMediaFormat(track);
                if (mediaFormat != null && !mediaFormat.equals(this.downstreamMediaFormats[track], true)) {
                    this.chunkSource.getMaxVideoDimensions(mediaFormat);
                    formatHolder.format = mediaFormat;
                    this.downstreamMediaFormats[track] = mediaFormat;
                    return -4;
                } else if (extractor.getSample(track, sampleHolder)) {
                    boolean decodeOnly = this.frameAccurateSeeking && sampleHolder.timeUs < this.lastSeekPositionUs;
                    sampleHolder.flags = (decodeOnly ? C.SAMPLE_FLAG_DECODE_ONLY : 0) | sampleHolder.flags;
                    return -3;
                } else if (this.loadingFinished) {
                    return NO_RESET_PENDING;
                } else {
                    maybeThrowLoadableException();
                    return -2;
                }
            }
            maybeThrowLoadableException();
            return -2;
        }
    }

    public void seekToUs(long positionUs) {
        Assertions.checkState(this.prepared);
        Assertions.checkState(this.enabledTrackCount > 0);
        long currentPositionUs = isPendingReset() ? this.pendingResetPositionUs : this.downstreamPositionUs;
        this.downstreamPositionUs = positionUs;
        this.lastSeekPositionUs = positionUs;
        if (currentPositionUs != positionUs) {
            this.downstreamPositionUs = positionUs;
            for (int i = 0; i < this.pendingDiscontinuities.length; i++) {
                this.pendingDiscontinuities[i] = true;
            }
            restartFrom(positionUs);
        }
    }

    public long getBufferedPositionUs() {
        Assertions.checkState(this.prepared);
        Assertions.checkState(this.enabledTrackCount > 0);
        if (isPendingReset()) {
            return this.pendingResetPositionUs;
        }
        if (this.loadingFinished) {
            return -3;
        }
        long largestParsedTimestampUs = ((HlsExtractorWrapper) this.extractors.getLast()).getLargestParsedTimestampUs();
        return largestParsedTimestampUs == Long.MIN_VALUE ? this.downstreamPositionUs : largestParsedTimestampUs;
    }

    public void release() {
        Assertions.checkState(this.remainingReleaseCount > 0);
        int i = this.remainingReleaseCount + NO_RESET_PENDING;
        this.remainingReleaseCount = i;
        if (i == 0 && this.loader != null) {
            this.loader.release();
            this.loader = null;
        }
    }

    public void onLoadCompleted(Loadable loadable) {
        boolean z;
        boolean z2 = true;
        if (loadable == this.currentLoadable) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        long now = SystemClock.elapsedRealtime();
        long loadDurationMs = now - this.currentLoadStartTimeMs;
        this.chunkSource.onChunkLoadCompleted(this.currentLoadable);
        if (isTsChunk(this.currentLoadable)) {
            if (this.currentLoadable != this.currentTsLoadable) {
                z2 = false;
            }
            Assertions.checkState(z2);
            this.loadingFinished = this.currentTsLoadable.isLastChunk;
            this.previousTsLoadable = this.currentTsLoadable;
            notifyLoadCompleted(this.currentLoadable.bytesLoaded(), this.currentTsLoadable.type, this.currentTsLoadable.trigger, this.currentTsLoadable.format, this.currentTsLoadable.startTimeUs, this.currentTsLoadable.endTimeUs, now, loadDurationMs);
        } else {
            notifyLoadCompleted(this.currentLoadable.bytesLoaded(), this.currentLoadable.type, this.currentLoadable.trigger, this.currentLoadable.format, -1, -1, now, loadDurationMs);
        }
        clearCurrentLoadable();
        if (this.enabledTrackCount > 0 || !this.prepared) {
            maybeStartLoading();
        }
    }

    public void onLoadCanceled(Loadable loadable) {
        notifyLoadCanceled(this.currentLoadable.bytesLoaded());
        if (this.enabledTrackCount > 0) {
            restartFrom(this.pendingResetPositionUs);
            return;
        }
        clearState();
        this.loadControl.trimAllocator();
    }

    public void onLoadError(Loadable loadable, IOException e) {
        if (this.chunkSource.onChunkLoadError(this.currentLoadable, e)) {
            if (this.previousTsLoadable == null && !isPendingReset()) {
                this.pendingResetPositionUs = this.lastSeekPositionUs;
            }
            clearCurrentLoadable();
        } else {
            this.currentLoadableException = e;
            this.currentLoadableExceptionCount++;
            this.currentLoadableExceptionTimestamp = SystemClock.elapsedRealtime();
        }
        notifyLoadError(e);
        maybeStartLoading();
    }

    private HlsExtractorWrapper getCurrentExtractor() {
        HlsExtractorWrapper extractor = (HlsExtractorWrapper) this.extractors.getFirst();
        while (this.extractors.size() > 1 && !haveSamplesForEnabledTracks(extractor)) {
            ((HlsExtractorWrapper) this.extractors.removeFirst()).clear();
            extractor = (HlsExtractorWrapper) this.extractors.getFirst();
        }
        return extractor;
    }

    private void discardSamplesForDisabledTracks(HlsExtractorWrapper extractor, long timeUs) {
        if (extractor.isPrepared()) {
            for (int i = 0; i < this.trackEnabledStates.length; i++) {
                if (!this.trackEnabledStates[i]) {
                    extractor.discardUntil(i, timeUs);
                }
            }
        }
    }

    private boolean haveSamplesForEnabledTracks(HlsExtractorWrapper extractor) {
        if (!extractor.isPrepared()) {
            return false;
        }
        int i = 0;
        while (i < this.trackEnabledStates.length) {
            if (this.trackEnabledStates[i] && extractor.hasSamples(i)) {
                return true;
            }
            i++;
        }
        return false;
    }

    private void maybeThrowLoadableException() throws IOException {
        if (this.currentLoadableException != null && this.currentLoadableExceptionCount > this.minLoadableRetryCount) {
            throw this.currentLoadableException;
        }
    }

    private void restartFrom(long positionUs) {
        this.pendingResetPositionUs = positionUs;
        this.loadingFinished = false;
        if (this.loader.isLoading()) {
            this.loader.cancelLoading();
            return;
        }
        clearState();
        maybeStartLoading();
    }

    private void clearState() {
        for (int i = 0; i < this.extractors.size(); i++) {
            ((HlsExtractorWrapper) this.extractors.get(i)).clear();
        }
        this.extractors.clear();
        clearCurrentLoadable();
        this.previousTsLoadable = null;
    }

    private void clearCurrentLoadable() {
        this.currentTsLoadable = null;
        this.currentLoadable = null;
        this.currentLoadableException = null;
        this.currentLoadableExceptionCount = 0;
    }

    private void maybeStartLoading() {
        long now = SystemClock.elapsedRealtime();
        long nextLoadPositionUs = getNextLoadPositionUs();
        boolean isBackedOff = this.currentLoadableException != null;
        boolean loadingOrBackedOff = this.loader.isLoading() || isBackedOff;
        boolean nextLoader = this.loadControl.update(this, this.downstreamPositionUs, nextLoadPositionUs, loadingOrBackedOff, false);
        if (isBackedOff) {
            if (now - this.currentLoadableExceptionTimestamp >= getRetryDelayMillis((long) this.currentLoadableExceptionCount)) {
                this.currentLoadableException = null;
                this.loader.startLoading(this.currentLoadable, this);
            }
        } else if (!this.loader.isLoading() && nextLoader) {
            Chunk nextLoadable = this.chunkSource.getChunkOperation(this.previousTsLoadable, this.pendingResetPositionUs, this.downstreamPositionUs);
            if (nextLoadable != null) {
                this.currentLoadStartTimeMs = now;
                this.currentLoadable = nextLoadable;
                if (isTsChunk(this.currentLoadable)) {
                    TsChunk tsChunk = (TsChunk) this.currentLoadable;
                    if (isPendingReset()) {
                        this.pendingResetPositionUs = -1;
                    }
                    HlsExtractorWrapper extractorWrapper = tsChunk.extractorWrapper;
                    if (this.extractors.isEmpty() || this.extractors.getLast() != extractorWrapper) {
                        extractorWrapper.init(this.loadControl.getAllocator());
                        this.extractors.addLast(extractorWrapper);
                    }
                    notifyLoadStarted(tsChunk.dataSpec.length, tsChunk.type, tsChunk.trigger, tsChunk.format, tsChunk.startTimeUs, tsChunk.endTimeUs);
                    this.currentTsLoadable = tsChunk;
                } else {
                    notifyLoadStarted(this.currentLoadable.dataSpec.length, this.currentLoadable.type, this.currentLoadable.trigger, this.currentLoadable.format, -1, -1);
                }
                this.loader.startLoading(this.currentLoadable, this);
            }
        }
    }

    private long getNextLoadPositionUs() {
        if (isPendingReset()) {
            return this.pendingResetPositionUs;
        }
        if (this.currentTsLoadable != null) {
            if (this.currentTsLoadable.isLastChunk) {
                return -1;
            }
            return this.currentTsLoadable.endTimeUs;
        } else if (this.previousTsLoadable.isLastChunk) {
            return -1;
        } else {
            return this.previousTsLoadable.endTimeUs;
        }
    }

    private boolean isTsChunk(Chunk chunk) {
        return chunk instanceof TsChunk;
    }

    private boolean isPendingReset() {
        return this.pendingResetPositionUs != -1;
    }

    private long getRetryDelayMillis(long errorCount) {
        return Math.min((errorCount - 1) * 1000, Constants.NOTIFICATION_BANNER_TIMEOUT);
    }

    protected final int usToMs(long timeUs) {
        return (int) (timeUs / 1000);
    }

    private void notifyLoadStarted(long length, int type, int trigger, Format format, long mediaStartTimeUs, long mediaEndTimeUs) {
        if (this.eventHandler != null && this.eventListener != null) {
            final long j = length;
            final int i = type;
            final int i2 = trigger;
            final Format format2 = format;
            final long j2 = mediaStartTimeUs;
            final long j3 = mediaEndTimeUs;
            this.eventHandler.post(new Runnable() {
                public void run() {
                    HlsSampleSource.this.eventListener.onLoadStarted(HlsSampleSource.this.eventSourceId, j, i, i2, format2, HlsSampleSource.this.usToMs(j2), HlsSampleSource.this.usToMs(j3));
                }
            });
        }
    }

    private void notifyLoadCompleted(long bytesLoaded, int type, int trigger, Format format, long mediaStartTimeUs, long mediaEndTimeUs, long elapsedRealtimeMs, long loadDurationMs) {
        if (this.eventHandler != null && this.eventListener != null) {
            final long j = bytesLoaded;
            final int i = type;
            final int i2 = trigger;
            final Format format2 = format;
            final long j2 = mediaStartTimeUs;
            final long j3 = mediaEndTimeUs;
            final long j4 = elapsedRealtimeMs;
            final long j5 = loadDurationMs;
            this.eventHandler.post(new Runnable() {
                public void run() {
                    HlsSampleSource.this.eventListener.onLoadCompleted(HlsSampleSource.this.eventSourceId, j, i, i2, format2, HlsSampleSource.this.usToMs(j2), HlsSampleSource.this.usToMs(j3), j4, j5);
                }
            });
        }
    }

    private void notifyLoadCanceled(final long bytesLoaded) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    HlsSampleSource.this.eventListener.onLoadCanceled(HlsSampleSource.this.eventSourceId, bytesLoaded);
                }
            });
        }
    }

    private void notifyLoadError(final IOException e) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    HlsSampleSource.this.eventListener.onLoadError(HlsSampleSource.this.eventSourceId, e);
                }
            });
        }
    }

    private void notifyDownstreamFormatChanged(Format format, int trigger, long positionUs) {
        if (this.eventHandler != null && this.eventListener != null) {
            final Format format2 = format;
            final int i = trigger;
            final long j = positionUs;
            this.eventHandler.post(new Runnable() {
                public void run() {
                    HlsSampleSource.this.eventListener.onDownstreamFormatChanged(HlsSampleSource.this.eventSourceId, format2, i, HlsSampleSource.this.usToMs(j));
                }
            });
        }
    }
}
