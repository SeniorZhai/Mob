package com.google.android.exoplayer.chunk;

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
import com.google.android.exoplayer.extractor.DefaultTrackOutput;
import com.google.android.exoplayer.upstream.Loader;
import com.google.android.exoplayer.upstream.Loader.Callback;
import com.google.android.exoplayer.upstream.Loader.Loadable;
import com.google.android.exoplayer.util.Assertions;
import com.mobcrush.mobcrush.Constants;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChunkSampleSource implements SampleSource, SampleSourceReader, Callback {
    public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT = 3;
    private static final int NO_RESET_PENDING = -1;
    private static final int STATE_ENABLED = 3;
    private static final int STATE_IDLE = 0;
    private static final int STATE_INITIALIZED = 1;
    private static final int STATE_PREPARED = 2;
    private final int bufferSizeContribution;
    private final ChunkSource chunkSource;
    private long currentLoadStartTimeMs;
    private IOException currentLoadableException;
    private int currentLoadableExceptionCount;
    private long currentLoadableExceptionTimestamp;
    private final ChunkOperationHolder currentLoadableHolder;
    private Format downstreamFormat;
    private MediaFormat downstreamMediaFormat;
    private long downstreamPositionUs;
    private final Handler eventHandler;
    private final EventListener eventListener;
    private final int eventSourceId;
    private final boolean frameAccurateSeeking;
    private long lastPerformedBufferOperation;
    private long lastSeekPositionUs;
    private final LoadControl loadControl;
    private Loader loader;
    private boolean loadingFinished;
    private final LinkedList<BaseMediaChunk> mediaChunks;
    private final int minLoadableRetryCount;
    private boolean pendingDiscontinuity;
    private long pendingResetPositionUs;
    private final List<BaseMediaChunk> readOnlyMediaChunks;
    private final DefaultTrackOutput sampleQueue;
    private int state;

    public interface EventListener extends BaseChunkSampleSourceEventListener {
    }

    public ChunkSampleSource(ChunkSource chunkSource, LoadControl loadControl, int bufferSizeContribution, boolean frameAccurateSeeking) {
        this(chunkSource, loadControl, bufferSizeContribution, frameAccurateSeeking, null, null, STATE_IDLE);
    }

    public ChunkSampleSource(ChunkSource chunkSource, LoadControl loadControl, int bufferSizeContribution, boolean frameAccurateSeeking, Handler eventHandler, EventListener eventListener, int eventSourceId) {
        this(chunkSource, loadControl, bufferSizeContribution, frameAccurateSeeking, eventHandler, eventListener, eventSourceId, STATE_ENABLED);
    }

    public ChunkSampleSource(ChunkSource chunkSource, LoadControl loadControl, int bufferSizeContribution, boolean frameAccurateSeeking, Handler eventHandler, EventListener eventListener, int eventSourceId, int minLoadableRetryCount) {
        this.chunkSource = chunkSource;
        this.loadControl = loadControl;
        this.bufferSizeContribution = bufferSizeContribution;
        this.frameAccurateSeeking = frameAccurateSeeking;
        this.eventHandler = eventHandler;
        this.eventListener = eventListener;
        this.eventSourceId = eventSourceId;
        this.minLoadableRetryCount = minLoadableRetryCount;
        this.currentLoadableHolder = new ChunkOperationHolder();
        this.mediaChunks = new LinkedList();
        this.readOnlyMediaChunks = Collections.unmodifiableList(this.mediaChunks);
        this.sampleQueue = new DefaultTrackOutput(loadControl.getAllocator());
        this.state = STATE_IDLE;
        this.pendingResetPositionUs = -1;
    }

    public SampleSourceReader register() {
        Assertions.checkState(this.state == 0);
        this.state = STATE_INITIALIZED;
        return this;
    }

    public boolean prepare(long positionUs) {
        boolean z = this.state == STATE_INITIALIZED || this.state == STATE_PREPARED;
        Assertions.checkState(z);
        if (this.state != STATE_PREPARED) {
            this.loader = new Loader("Loader:" + this.chunkSource.getTrackInfo().mimeType);
            this.state = STATE_PREPARED;
        }
        return true;
    }

    public int getTrackCount() {
        boolean z = this.state == STATE_PREPARED || this.state == STATE_ENABLED;
        Assertions.checkState(z);
        return STATE_INITIALIZED;
    }

    public TrackInfo getTrackInfo(int track) {
        boolean z;
        boolean z2 = true;
        if (this.state == STATE_PREPARED || this.state == STATE_ENABLED) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        if (track != 0) {
            z2 = false;
        }
        Assertions.checkState(z2);
        return this.chunkSource.getTrackInfo();
    }

    public void enable(int track, long positionUs) {
        boolean z = true;
        Assertions.checkState(this.state == STATE_PREPARED);
        if (track != 0) {
            z = false;
        }
        Assertions.checkState(z);
        this.state = STATE_ENABLED;
        this.chunkSource.enable();
        this.loadControl.register(this, this.bufferSizeContribution);
        this.downstreamFormat = null;
        this.downstreamMediaFormat = null;
        this.downstreamPositionUs = positionUs;
        this.lastSeekPositionUs = positionUs;
        this.pendingDiscontinuity = false;
        restartFrom(positionUs);
    }

    public void disable(int track) {
        boolean z = true;
        Assertions.checkState(this.state == STATE_ENABLED);
        if (track != 0) {
            z = false;
        }
        Assertions.checkState(z);
        this.state = STATE_PREPARED;
        try {
            this.chunkSource.disable(this.mediaChunks);
        } finally {
            this.loadControl.unregister(this);
            if (this.loader.isLoading()) {
                this.loader.cancelLoading();
            } else {
                this.sampleQueue.clear();
                this.mediaChunks.clear();
                clearCurrentLoadable();
                this.loadControl.trimAllocator();
            }
        }
    }

    public boolean continueBuffering(long positionUs) throws IOException {
        boolean z;
        boolean haveSamples;
        if (this.state == STATE_ENABLED) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        this.downstreamPositionUs = positionUs;
        this.chunkSource.continueBuffering(positionUs);
        updateLoadControl();
        if (this.sampleQueue.isEmpty()) {
            haveSamples = false;
        } else {
            haveSamples = true;
        }
        if (!haveSamples) {
            maybeThrowLoadableException();
        }
        if (this.loadingFinished || haveSamples) {
            return true;
        }
        return false;
    }

    public int readData(int track, long positionUs, MediaFormatHolder formatHolder, SampleHolder sampleHolder, boolean onlyReadDiscontinuity) throws IOException {
        Assertions.checkState(this.state == STATE_ENABLED);
        Assertions.checkState(track == 0);
        this.downstreamPositionUs = positionUs;
        if (this.pendingDiscontinuity) {
            this.pendingDiscontinuity = false;
            return -5;
        } else if (onlyReadDiscontinuity) {
            return -2;
        } else {
            if (isPendingReset()) {
                maybeThrowLoadableException();
                return -2;
            }
            boolean haveSamples = !this.sampleQueue.isEmpty();
            BaseMediaChunk currentChunk = (BaseMediaChunk) this.mediaChunks.getFirst();
            while (haveSamples && this.mediaChunks.size() > STATE_INITIALIZED && ((BaseMediaChunk) this.mediaChunks.get(STATE_INITIALIZED)).getFirstSampleIndex() == this.sampleQueue.getReadIndex()) {
                this.mediaChunks.removeFirst();
                currentChunk = (BaseMediaChunk) this.mediaChunks.getFirst();
            }
            if (this.downstreamFormat == null || !this.downstreamFormat.equals(currentChunk.format)) {
                notifyDownstreamFormatChanged(currentChunk.format, currentChunk.trigger, currentChunk.startTimeUs);
                this.downstreamFormat = currentChunk.format;
            }
            if (haveSamples || currentChunk.isMediaFormatFinal) {
                MediaFormat mediaFormat = currentChunk.getMediaFormat();
                if (!mediaFormat.equals(this.downstreamMediaFormat, true)) {
                    this.chunkSource.getMaxVideoDimensions(mediaFormat);
                    formatHolder.format = mediaFormat;
                    formatHolder.drmInitData = currentChunk.getDrmInitData();
                    this.downstreamMediaFormat = mediaFormat;
                    return -4;
                }
            }
            if (haveSamples) {
                if (this.sampleQueue.getSample(sampleHolder)) {
                    boolean decodeOnly = this.frameAccurateSeeking && sampleHolder.timeUs < this.lastSeekPositionUs;
                    sampleHolder.flags = (decodeOnly ? C.SAMPLE_FLAG_DECODE_ONLY : STATE_IDLE) | sampleHolder.flags;
                    onSampleRead(currentChunk, sampleHolder);
                    return -3;
                }
                maybeThrowLoadableException();
                return -2;
            } else if (this.loadingFinished) {
                return NO_RESET_PENDING;
            } else {
                maybeThrowLoadableException();
                return -2;
            }
        }
    }

    public void seekToUs(long positionUs) {
        boolean z;
        if (this.state == STATE_ENABLED) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        long currentPositionUs = isPendingReset() ? this.pendingResetPositionUs : this.downstreamPositionUs;
        this.downstreamPositionUs = positionUs;
        this.lastSeekPositionUs = positionUs;
        if (currentPositionUs != positionUs) {
            boolean seekInsideBuffer;
            if (isPendingReset() || !this.sampleQueue.skipToKeyframeBefore(positionUs)) {
                seekInsideBuffer = false;
            } else {
                seekInsideBuffer = true;
            }
            if (seekInsideBuffer) {
                boolean haveSamples;
                if (this.sampleQueue.isEmpty()) {
                    haveSamples = false;
                } else {
                    haveSamples = true;
                }
                while (haveSamples && this.mediaChunks.size() > STATE_INITIALIZED && ((BaseMediaChunk) this.mediaChunks.get(STATE_INITIALIZED)).getFirstSampleIndex() <= this.sampleQueue.getReadIndex()) {
                    this.mediaChunks.removeFirst();
                }
            } else {
                restartFrom(positionUs);
            }
            this.pendingDiscontinuity = true;
        }
    }

    private void maybeThrowLoadableException() throws IOException {
        if (this.currentLoadableException != null && this.currentLoadableExceptionCount > this.minLoadableRetryCount) {
            throw this.currentLoadableException;
        } else if (this.sampleQueue.isEmpty() && this.currentLoadableHolder.chunk == null) {
            IOException chunkSourceException = this.chunkSource.getError();
            if (chunkSourceException != null) {
                throw chunkSourceException;
            }
        }
    }

    public long getBufferedPositionUs() {
        Assertions.checkState(this.state == STATE_ENABLED);
        if (isPendingReset()) {
            return this.pendingResetPositionUs;
        }
        if (this.loadingFinished) {
            return -3;
        }
        long largestParsedTimestampUs = this.sampleQueue.getLargestParsedTimestampUs();
        return largestParsedTimestampUs == Long.MIN_VALUE ? this.downstreamPositionUs : largestParsedTimestampUs;
    }

    public void release() {
        Assertions.checkState(this.state != STATE_ENABLED);
        if (this.loader != null) {
            this.loader.release();
            this.loader = null;
        }
        this.state = STATE_IDLE;
    }

    public void onLoadCompleted(Loadable loadable) {
        long now = SystemClock.elapsedRealtime();
        long loadDurationMs = now - this.currentLoadStartTimeMs;
        Chunk currentLoadable = this.currentLoadableHolder.chunk;
        this.chunkSource.onChunkLoadCompleted(currentLoadable);
        if (isMediaChunk(currentLoadable)) {
            MediaChunk mediaChunk = (MediaChunk) currentLoadable;
            notifyLoadCompleted(currentLoadable.bytesLoaded(), mediaChunk.type, mediaChunk.trigger, mediaChunk.format, mediaChunk.startTimeUs, mediaChunk.endTimeUs, now, loadDurationMs);
            this.loadingFinished = ((BaseMediaChunk) currentLoadable).isLastChunk;
        } else {
            notifyLoadCompleted(currentLoadable.bytesLoaded(), currentLoadable.type, currentLoadable.trigger, currentLoadable.format, -1, -1, now, loadDurationMs);
        }
        clearCurrentLoadable();
        updateLoadControl();
    }

    public void onLoadCanceled(Loadable loadable) {
        notifyLoadCanceled(this.currentLoadableHolder.chunk.bytesLoaded());
        clearCurrentLoadable();
        if (this.state == STATE_ENABLED) {
            restartFrom(this.pendingResetPositionUs);
            return;
        }
        this.sampleQueue.clear();
        this.mediaChunks.clear();
        clearCurrentLoadable();
        this.loadControl.trimAllocator();
    }

    public void onLoadError(Loadable loadable, IOException e) {
        this.currentLoadableException = e;
        this.currentLoadableExceptionCount += STATE_INITIALIZED;
        this.currentLoadableExceptionTimestamp = SystemClock.elapsedRealtime();
        notifyLoadError(e);
        this.chunkSource.onChunkLoadError(this.currentLoadableHolder.chunk, e);
        updateLoadControl();
    }

    protected void onSampleRead(MediaChunk mediaChunk, SampleHolder sampleHolder) {
    }

    private void restartFrom(long positionUs) {
        this.pendingResetPositionUs = positionUs;
        this.loadingFinished = false;
        if (this.loader.isLoading()) {
            this.loader.cancelLoading();
            return;
        }
        this.sampleQueue.clear();
        this.mediaChunks.clear();
        clearCurrentLoadable();
        updateLoadControl();
    }

    private void clearCurrentLoadable() {
        this.currentLoadableHolder.chunk = null;
        clearCurrentLoadableException();
    }

    private void clearCurrentLoadableException() {
        this.currentLoadableException = null;
        this.currentLoadableExceptionCount = STATE_IDLE;
    }

    private void updateLoadControl() {
        long nextLoadPositionUs;
        boolean nextLoader;
        long now = SystemClock.elapsedRealtime();
        long nextLoadPositionUs2 = getNextLoadPositionUs();
        boolean isBackedOff = this.currentLoadableException != null;
        boolean loadingOrBackedOff = this.loader.isLoading() || isBackedOff;
        if (!loadingOrBackedOff && ((this.currentLoadableHolder.chunk == null && nextLoadPositionUs2 != -1) || now - this.lastPerformedBufferOperation > 2000)) {
            this.lastPerformedBufferOperation = now;
            this.currentLoadableHolder.queueSize = this.readOnlyMediaChunks.size();
            this.chunkSource.getChunkOperation(this.readOnlyMediaChunks, this.pendingResetPositionUs, this.downstreamPositionUs, this.currentLoadableHolder);
            boolean chunksDiscarded = discardUpstreamMediaChunks(this.currentLoadableHolder.queueSize);
            if (this.currentLoadableHolder.chunk == null) {
                nextLoadPositionUs = -1;
            } else if (chunksDiscarded) {
                nextLoadPositionUs = getNextLoadPositionUs();
            }
            nextLoader = this.loadControl.update(this, this.downstreamPositionUs, nextLoadPositionUs, loadingOrBackedOff, false);
            if (isBackedOff) {
                if (!this.loader.isLoading() && nextLoader) {
                    maybeStartLoading();
                    return;
                }
            } else if (now - this.currentLoadableExceptionTimestamp >= getRetryDelayMillis((long) this.currentLoadableExceptionCount)) {
                resumeFromBackOff();
            }
        }
        nextLoadPositionUs = nextLoadPositionUs2;
        nextLoader = this.loadControl.update(this, this.downstreamPositionUs, nextLoadPositionUs, loadingOrBackedOff, false);
        if (isBackedOff) {
            if (!this.loader.isLoading()) {
            }
        } else if (now - this.currentLoadableExceptionTimestamp >= getRetryDelayMillis((long) this.currentLoadableExceptionCount)) {
            resumeFromBackOff();
        }
    }

    private long getNextLoadPositionUs() {
        if (isPendingReset()) {
            return this.pendingResetPositionUs;
        }
        BaseMediaChunk lastMediaChunk = (BaseMediaChunk) this.mediaChunks.getLast();
        return lastMediaChunk.isLastChunk ? -1 : lastMediaChunk.endTimeUs;
    }

    private void resumeFromBackOff() {
        this.currentLoadableException = null;
        Chunk backedOffChunk = this.currentLoadableHolder.chunk;
        if (!isMediaChunk(backedOffChunk)) {
            this.currentLoadableHolder.queueSize = this.readOnlyMediaChunks.size();
            this.chunkSource.getChunkOperation(this.readOnlyMediaChunks, this.pendingResetPositionUs, this.downstreamPositionUs, this.currentLoadableHolder);
            discardUpstreamMediaChunks(this.currentLoadableHolder.queueSize);
            if (this.currentLoadableHolder.chunk == backedOffChunk) {
                this.loader.startLoading(backedOffChunk, this);
                return;
            }
            notifyLoadCanceled(backedOffChunk.bytesLoaded());
            maybeStartLoading();
        } else if (backedOffChunk == this.mediaChunks.getFirst()) {
            this.loader.startLoading(backedOffChunk, this);
        } else {
            Chunk removedChunk = (BaseMediaChunk) this.mediaChunks.removeLast();
            Assertions.checkState(backedOffChunk == removedChunk);
            this.currentLoadableHolder.queueSize = this.readOnlyMediaChunks.size();
            this.chunkSource.getChunkOperation(this.readOnlyMediaChunks, this.pendingResetPositionUs, this.downstreamPositionUs, this.currentLoadableHolder);
            this.mediaChunks.add(removedChunk);
            if (this.currentLoadableHolder.chunk == backedOffChunk) {
                this.loader.startLoading(backedOffChunk, this);
                return;
            }
            notifyLoadCanceled(backedOffChunk.bytesLoaded());
            discardUpstreamMediaChunks(this.currentLoadableHolder.queueSize);
            clearCurrentLoadableException();
            maybeStartLoading();
        }
    }

    private void maybeStartLoading() {
        Chunk currentLoadable = this.currentLoadableHolder.chunk;
        if (currentLoadable != null) {
            this.currentLoadStartTimeMs = SystemClock.elapsedRealtime();
            if (isMediaChunk(currentLoadable)) {
                BaseMediaChunk mediaChunk = (BaseMediaChunk) currentLoadable;
                mediaChunk.init(this.sampleQueue);
                this.mediaChunks.add(mediaChunk);
                if (isPendingReset()) {
                    this.pendingResetPositionUs = -1;
                }
                notifyLoadStarted(mediaChunk.dataSpec.length, mediaChunk.type, mediaChunk.trigger, mediaChunk.format, mediaChunk.startTimeUs, mediaChunk.endTimeUs);
            } else {
                notifyLoadStarted(currentLoadable.dataSpec.length, currentLoadable.type, currentLoadable.trigger, currentLoadable.format, -1, -1);
            }
            this.loader.startLoading(currentLoadable, this);
        }
    }

    private boolean discardUpstreamMediaChunks(int queueLength) {
        if (this.mediaChunks.size() <= queueLength) {
            return false;
        }
        long startTimeUs = 0;
        long endTimeUs = ((BaseMediaChunk) this.mediaChunks.getLast()).endTimeUs;
        BaseMediaChunk removed = null;
        while (this.mediaChunks.size() > queueLength) {
            removed = (BaseMediaChunk) this.mediaChunks.removeLast();
            startTimeUs = removed.startTimeUs;
        }
        this.sampleQueue.discardUpstreamSamples(removed.getFirstSampleIndex());
        notifyUpstreamDiscarded(startTimeUs, endTimeUs);
        return true;
    }

    private boolean isMediaChunk(Chunk chunk) {
        return chunk instanceof BaseMediaChunk;
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
                    ChunkSampleSource.this.eventListener.onLoadStarted(ChunkSampleSource.this.eventSourceId, j, i, i2, format2, ChunkSampleSource.this.usToMs(j2), ChunkSampleSource.this.usToMs(j3));
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
                    ChunkSampleSource.this.eventListener.onLoadCompleted(ChunkSampleSource.this.eventSourceId, j, i, i2, format2, ChunkSampleSource.this.usToMs(j2), ChunkSampleSource.this.usToMs(j3), j4, j5);
                }
            });
        }
    }

    private void notifyLoadCanceled(final long bytesLoaded) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    ChunkSampleSource.this.eventListener.onLoadCanceled(ChunkSampleSource.this.eventSourceId, bytesLoaded);
                }
            });
        }
    }

    private void notifyLoadError(final IOException e) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    ChunkSampleSource.this.eventListener.onLoadError(ChunkSampleSource.this.eventSourceId, e);
                }
            });
        }
    }

    private void notifyUpstreamDiscarded(long mediaStartTimeUs, long mediaEndTimeUs) {
        if (this.eventHandler != null && this.eventListener != null) {
            final long j = mediaStartTimeUs;
            final long j2 = mediaEndTimeUs;
            this.eventHandler.post(new Runnable() {
                public void run() {
                    ChunkSampleSource.this.eventListener.onUpstreamDiscarded(ChunkSampleSource.this.eventSourceId, ChunkSampleSource.this.usToMs(j), ChunkSampleSource.this.usToMs(j2));
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
                    ChunkSampleSource.this.eventListener.onDownstreamFormatChanged(ChunkSampleSource.this.eventSourceId, format2, i, ChunkSampleSource.this.usToMs(j));
                }
            });
        }
    }
}
