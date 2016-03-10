package com.google.android.exoplayer.extractor;

import android.net.Uri;
import android.os.SystemClock;
import android.util.SparseArray;
import com.facebook.internal.NativeProtocol;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.MediaFormatHolder;
import com.google.android.exoplayer.SampleHolder;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.SampleSource.SampleSourceReader;
import com.google.android.exoplayer.TrackInfo;
import com.google.android.exoplayer.drm.DrmInitData;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.Loader;
import com.google.android.exoplayer.upstream.Loader.Callback;
import com.google.android.exoplayer.upstream.Loader.Loadable;
import com.google.android.exoplayer.util.Assertions;
import com.mobcrush.mobcrush.Constants;
import java.io.IOException;

public class ExtractorSampleSource implements SampleSource, SampleSourceReader, ExtractorOutput, Callback {
    public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT_LIVE = 6;
    public static final int DEFAULT_MIN_LOADABLE_RETRY_COUNT_ON_DEMAND = 3;
    private static final int MIN_RETRY_COUNT_DEFAULT_FOR_MEDIA = -1;
    private static final int NO_RESET_PENDING = -1;
    private final Allocator allocator;
    private IOException currentLoadableException;
    private int currentLoadableExceptionCount;
    private long currentLoadableExceptionTimestamp;
    private final DataSource dataSource;
    private long downstreamPositionUs;
    private volatile DrmInitData drmInitData;
    private int enabledTrackCount;
    private int extractedSampleCount;
    private int extractedSampleCountAtStartOfLoad;
    private final Extractor extractor;
    private final boolean frameAccurateSeeking;
    private boolean havePendingNextSampleUs;
    private long lastSeekPositionUs;
    private ExtractingLoadable loadable;
    private Loader loader;
    private boolean loadingFinished;
    private long maxTrackDurationUs;
    private final int minLoadableRetryCount;
    private boolean[] pendingDiscontinuities;
    private boolean[] pendingMediaFormat;
    private long pendingNextSampleUs;
    private long pendingResetPositionUs;
    private boolean prepared;
    private int remainingReleaseCount;
    private final int requestedBufferSize;
    private final SparseArray<InternalTrackOutput> sampleQueues;
    private long sampleTimeOffsetUs;
    private volatile SeekMap seekMap;
    private boolean[] trackEnabledStates;
    private TrackInfo[] trackInfos;
    private volatile boolean tracksBuilt;
    private final Uri uri;

    private static class ExtractingLoadable implements Loadable {
        private final Allocator allocator;
        private final DataSource dataSource;
        private final Extractor extractor;
        private volatile boolean loadCanceled;
        private boolean pendingExtractorSeek;
        private final PositionHolder positionHolder = new PositionHolder();
        private final int requestedBufferSize;
        private final Uri uri;

        public ExtractingLoadable(Uri uri, DataSource dataSource, Extractor extractor, Allocator allocator, int requestedBufferSize, long position) {
            this.uri = (Uri) Assertions.checkNotNull(uri);
            this.dataSource = (DataSource) Assertions.checkNotNull(dataSource);
            this.extractor = (Extractor) Assertions.checkNotNull(extractor);
            this.allocator = (Allocator) Assertions.checkNotNull(allocator);
            this.requestedBufferSize = requestedBufferSize;
            this.positionHolder.position = position;
            this.pendingExtractorSeek = true;
        }

        public void cancelLoad() {
            this.loadCanceled = true;
        }

        public boolean isLoadCanceled() {
            return this.loadCanceled;
        }

        public void load() throws IOException, InterruptedException {
            Throwable th;
            if (this.pendingExtractorSeek) {
                this.extractor.seek();
                this.pendingExtractorSeek = false;
            }
            int result = 0;
            while (result == 0 && !this.loadCanceled) {
                ExtractorInput input;
                try {
                    long position = this.positionHolder.position;
                    long length = this.dataSource.open(new DataSpec(this.uri, position, -1, null));
                    if (length != -1) {
                        length += position;
                    }
                    input = new DefaultExtractorInput(this.dataSource, position, length);
                    while (result == 0) {
                        try {
                            if (this.loadCanceled) {
                                break;
                            }
                            this.allocator.blockWhileTotalBytesAllocatedExceeds(this.requestedBufferSize);
                            result = this.extractor.read(input, this.positionHolder);
                        } catch (Throwable th2) {
                            th = th2;
                        }
                    }
                    if (result == 1) {
                        result = 0;
                    } else if (input != null) {
                        this.positionHolder.position = input.getPosition();
                    }
                    this.dataSource.close();
                } catch (Throwable th3) {
                    th = th3;
                    input = null;
                }
            }
            return;
            if (result != 1) {
                if (input != null) {
                    this.positionHolder.position = input.getPosition();
                }
            }
            this.dataSource.close();
            throw th;
        }
    }

    private class InternalTrackOutput extends DefaultTrackOutput {
        public InternalTrackOutput(Allocator allocator) {
            super(allocator);
        }

        public void sampleMetadata(long timeUs, int flags, int size, int offset, byte[] encryptionKey) {
            super.sampleMetadata(timeUs, flags, size, offset, encryptionKey);
            ExtractorSampleSource.this.extractedSampleCount = ExtractorSampleSource.this.extractedSampleCount + 1;
        }
    }

    @Deprecated
    public ExtractorSampleSource(Uri uri, DataSource dataSource, Extractor extractor, int requestedBufferSize) {
        this(uri, dataSource, extractor, new DefaultAllocator(NativeProtocol.MESSAGE_GET_ACCESS_TOKEN_REQUEST), requestedBufferSize);
    }

    public ExtractorSampleSource(Uri uri, DataSource dataSource, Extractor extractor, Allocator allocator, int requestedBufferSize) {
        this(uri, dataSource, extractor, allocator, requestedBufferSize, NO_RESET_PENDING);
    }

    @Deprecated
    public ExtractorSampleSource(Uri uri, DataSource dataSource, Extractor extractor, int requestedBufferSize, int minLoadableRetryCount) {
        this(uri, dataSource, extractor, new DefaultAllocator(NativeProtocol.MESSAGE_GET_ACCESS_TOKEN_REQUEST), requestedBufferSize, minLoadableRetryCount);
    }

    public ExtractorSampleSource(Uri uri, DataSource dataSource, Extractor extractor, Allocator allocator, int requestedBufferSize, int minLoadableRetryCount) {
        this.uri = uri;
        this.dataSource = dataSource;
        this.extractor = extractor;
        this.allocator = allocator;
        this.requestedBufferSize = requestedBufferSize;
        this.minLoadableRetryCount = minLoadableRetryCount;
        this.sampleQueues = new SparseArray();
        this.pendingResetPositionUs = -1;
        this.frameAccurateSeeking = true;
        extractor.init(this);
    }

    public SampleSourceReader register() {
        this.remainingReleaseCount++;
        return this;
    }

    public boolean prepare(long positionUs) throws IOException {
        if (this.prepared) {
            return true;
        }
        if (this.loader == null) {
            this.loader = new Loader("Loader:ExtractorSampleSource");
        }
        continueBufferingInternal();
        if (this.seekMap != null && this.tracksBuilt && haveFormatsForAllTracks()) {
            int trackCount = this.sampleQueues.size();
            this.trackEnabledStates = new boolean[trackCount];
            this.pendingDiscontinuities = new boolean[trackCount];
            this.pendingMediaFormat = new boolean[trackCount];
            this.trackInfos = new TrackInfo[trackCount];
            this.maxTrackDurationUs = -1;
            for (int i = 0; i < trackCount; i++) {
                MediaFormat format = ((InternalTrackOutput) this.sampleQueues.valueAt(i)).getFormat();
                this.trackInfos[i] = new TrackInfo(format.mimeType, format.durationUs);
                if (format.durationUs != -1 && format.durationUs > this.maxTrackDurationUs) {
                    this.maxTrackDurationUs = format.durationUs;
                }
            }
            this.prepared = true;
            return true;
        }
        maybeThrowLoadableException();
        return false;
    }

    public int getTrackCount() {
        return this.sampleQueues.size();
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
        this.pendingMediaFormat[track] = true;
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
            if (this.loader.isLoading()) {
                this.loader.cancelLoading();
                return;
            }
            clearState();
            this.allocator.trim(0);
        }
    }

    public boolean continueBuffering(long playbackPositionUs) throws IOException {
        boolean z;
        Assertions.checkState(this.prepared);
        if (this.enabledTrackCount > 0) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        this.downstreamPositionUs = playbackPositionUs;
        discardSamplesForDisabledTracks(this.downstreamPositionUs);
        if (this.loadingFinished || continueBufferingInternal()) {
            return true;
        }
        return false;
    }

    public int readData(int track, long playbackPositionUs, MediaFormatHolder formatHolder, SampleHolder sampleHolder, boolean onlyReadDiscontinuity) throws IOException {
        this.downstreamPositionUs = playbackPositionUs;
        if (this.pendingDiscontinuities[track]) {
            this.pendingDiscontinuities[track] = false;
            return -5;
        } else if (onlyReadDiscontinuity || isPendingReset()) {
            maybeThrowLoadableException();
            return -2;
        } else {
            InternalTrackOutput sampleQueue = (InternalTrackOutput) this.sampleQueues.valueAt(track);
            if (this.pendingMediaFormat[track]) {
                formatHolder.format = sampleQueue.getFormat();
                formatHolder.drmInitData = this.drmInitData;
                this.pendingMediaFormat[track] = false;
                return -4;
            } else if (sampleQueue.getSample(sampleHolder)) {
                boolean decodeOnly;
                int i;
                if (!this.frameAccurateSeeking || sampleHolder.timeUs >= this.lastSeekPositionUs) {
                    decodeOnly = false;
                } else {
                    decodeOnly = true;
                }
                int i2 = sampleHolder.flags;
                if (decodeOnly) {
                    i = C.SAMPLE_FLAG_DECODE_ONLY;
                } else {
                    i = 0;
                }
                sampleHolder.flags = i | i2;
                if (this.havePendingNextSampleUs) {
                    this.sampleTimeOffsetUs = this.pendingNextSampleUs - sampleHolder.timeUs;
                    this.havePendingNextSampleUs = false;
                }
                sampleHolder.timeUs += this.sampleTimeOffsetUs;
                return -3;
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
        Assertions.checkState(this.prepared);
        if (this.enabledTrackCount > 0) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        if (!this.seekMap.isSeekable()) {
            positionUs = 0;
        }
        long currentPositionUs = isPendingReset() ? this.pendingResetPositionUs : this.downstreamPositionUs;
        this.downstreamPositionUs = positionUs;
        this.lastSeekPositionUs = positionUs;
        if (currentPositionUs != positionUs) {
            boolean seekInsideBuffer;
            if (isPendingReset()) {
                seekInsideBuffer = false;
            } else {
                seekInsideBuffer = true;
            }
            int i = 0;
            while (seekInsideBuffer && i < this.sampleQueues.size()) {
                seekInsideBuffer &= ((InternalTrackOutput) this.sampleQueues.valueAt(i)).skipToKeyframeBefore(positionUs);
                i++;
            }
            if (!seekInsideBuffer) {
                restartFrom(positionUs);
            }
            for (i = 0; i < this.pendingDiscontinuities.length; i++) {
                this.pendingDiscontinuities[i] = true;
            }
        }
    }

    public long getBufferedPositionUs() {
        if (this.loadingFinished) {
            return -3;
        }
        if (isPendingReset()) {
            return this.pendingResetPositionUs;
        }
        long largestParsedTimestampUs = Long.MIN_VALUE;
        for (int i = 0; i < this.sampleQueues.size(); i++) {
            largestParsedTimestampUs = Math.max(largestParsedTimestampUs, ((InternalTrackOutput) this.sampleQueues.valueAt(i)).getLargestParsedTimestampUs());
        }
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
        this.loadingFinished = true;
    }

    public void onLoadCanceled(Loadable loadable) {
        if (this.enabledTrackCount > 0) {
            restartFrom(this.pendingResetPositionUs);
            return;
        }
        clearState();
        this.allocator.trim(0);
    }

    public void onLoadError(Loadable ignored, IOException e) {
        this.currentLoadableException = e;
        this.currentLoadableExceptionCount = this.extractedSampleCount > this.extractedSampleCountAtStartOfLoad ? 1 : this.currentLoadableExceptionCount + 1;
        this.currentLoadableExceptionTimestamp = SystemClock.elapsedRealtime();
        maybeStartLoading();
    }

    public TrackOutput track(int id) {
        InternalTrackOutput sampleQueue = (InternalTrackOutput) this.sampleQueues.get(id);
        if (sampleQueue != null) {
            return sampleQueue;
        }
        sampleQueue = new InternalTrackOutput(this.allocator);
        this.sampleQueues.put(id, sampleQueue);
        return sampleQueue;
    }

    public void endTracks() {
        this.tracksBuilt = true;
    }

    public void seekMap(SeekMap seekMap) {
        this.seekMap = seekMap;
    }

    public void drmInitData(DrmInitData drmInitData) {
        this.drmInitData = drmInitData;
    }

    private boolean continueBufferingInternal() throws IOException {
        boolean haveSamples = false;
        maybeStartLoading();
        if (!isPendingReset()) {
            if (this.prepared && haveSampleForOneEnabledTrack()) {
                haveSamples = true;
            }
            if (!haveSamples) {
                maybeThrowLoadableException();
            }
        }
        return haveSamples;
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

    private void maybeStartLoading() {
        boolean z = false;
        if (!this.loadingFinished && !this.loader.isLoading()) {
            if (this.currentLoadableException != null) {
                if (this.loadable != null) {
                    z = true;
                }
                Assertions.checkState(z);
                if (SystemClock.elapsedRealtime() - this.currentLoadableExceptionTimestamp >= getRetryDelayMillis((long) this.currentLoadableExceptionCount)) {
                    this.currentLoadableException = null;
                    int i;
                    if (!this.prepared) {
                        for (i = 0; i < this.sampleQueues.size(); i++) {
                            ((InternalTrackOutput) this.sampleQueues.valueAt(i)).clear();
                        }
                        this.loadable = createLoadableFromStart();
                    } else if (!this.seekMap.isSeekable()) {
                        for (i = 0; i < this.sampleQueues.size(); i++) {
                            ((InternalTrackOutput) this.sampleQueues.valueAt(i)).clear();
                        }
                        this.loadable = createLoadableFromStart();
                        this.pendingNextSampleUs = this.downstreamPositionUs;
                        this.havePendingNextSampleUs = true;
                    }
                    this.extractedSampleCountAtStartOfLoad = this.extractedSampleCount;
                    this.loader.startLoading(this.loadable, this);
                    return;
                }
                return;
            }
            this.sampleTimeOffsetUs = 0;
            this.havePendingNextSampleUs = false;
            if (this.prepared) {
                Assertions.checkState(isPendingReset());
                if (this.maxTrackDurationUs == -1 || this.pendingResetPositionUs < this.maxTrackDurationUs) {
                    this.loadable = createLoadableFromPositionUs(this.pendingResetPositionUs);
                    this.pendingResetPositionUs = -1;
                } else {
                    this.loadingFinished = true;
                    this.pendingResetPositionUs = -1;
                    return;
                }
            }
            this.loadable = createLoadableFromStart();
            this.extractedSampleCountAtStartOfLoad = this.extractedSampleCount;
            this.loader.startLoading(this.loadable, this);
        }
    }

    private void maybeThrowLoadableException() throws IOException {
        if (this.currentLoadableException != null) {
            int minLoadableRetryCountForMedia;
            if (this.minLoadableRetryCount != NO_RESET_PENDING) {
                minLoadableRetryCountForMedia = this.minLoadableRetryCount;
            } else {
                minLoadableRetryCountForMedia = (this.seekMap == null || this.seekMap.isSeekable()) ? DEFAULT_MIN_LOADABLE_RETRY_COUNT_ON_DEMAND : DEFAULT_MIN_LOADABLE_RETRY_COUNT_LIVE;
            }
            if (this.currentLoadableExceptionCount > minLoadableRetryCountForMedia) {
                throw this.currentLoadableException;
            }
        }
    }

    private ExtractingLoadable createLoadableFromStart() {
        return new ExtractingLoadable(this.uri, this.dataSource, this.extractor, this.allocator, this.requestedBufferSize, 0);
    }

    private ExtractingLoadable createLoadableFromPositionUs(long positionUs) {
        return new ExtractingLoadable(this.uri, this.dataSource, this.extractor, this.allocator, this.requestedBufferSize, this.seekMap.getPosition(positionUs));
    }

    private boolean haveFormatsForAllTracks() {
        for (int i = 0; i < this.sampleQueues.size(); i++) {
            if (!((InternalTrackOutput) this.sampleQueues.valueAt(i)).hasFormat()) {
                return false;
            }
        }
        return true;
    }

    private boolean haveSampleForOneEnabledTrack() {
        int i = 0;
        while (i < this.trackEnabledStates.length) {
            if (this.trackEnabledStates[i] && !((InternalTrackOutput) this.sampleQueues.valueAt(i)).isEmpty()) {
                return true;
            }
            i++;
        }
        return false;
    }

    private void discardSamplesForDisabledTracks(long timeUs) {
        for (int i = 0; i < this.trackEnabledStates.length; i++) {
            if (!this.trackEnabledStates[i]) {
                ((InternalTrackOutput) this.sampleQueues.valueAt(i)).discardUntil(timeUs);
            }
        }
    }

    private void clearState() {
        for (int i = 0; i < this.sampleQueues.size(); i++) {
            ((InternalTrackOutput) this.sampleQueues.valueAt(i)).clear();
        }
        this.loadable = null;
        this.currentLoadableException = null;
        this.currentLoadableExceptionCount = 0;
    }

    private boolean isPendingReset() {
        return this.pendingResetPositionUs != -1;
    }

    private long getRetryDelayMillis(long errorCount) {
        return Math.min((errorCount - 1) * 1000, Constants.NOTIFICATION_BANNER_TIMEOUT);
    }
}
