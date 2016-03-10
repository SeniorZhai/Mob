package com.google.android.exoplayer;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.CodecException;
import android.media.MediaCodec.CryptoException;
import android.media.MediaCodec.CryptoInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.SystemClock;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.SampleSource.SampleSourceReader;
import com.google.android.exoplayer.drm.DrmInitData;
import com.google.android.exoplayer.drm.DrmSessionManager;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.TraceUtil;
import com.google.android.exoplayer.util.Util;
import io.fabric.sdk.android.BuildConfig;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@TargetApi(16)
public abstract class MediaCodecTrackRenderer extends TrackRenderer {
    private static final long MAX_CODEC_HOTSWAP_TIME_MS = 1000;
    private static final int RECONFIGURATION_STATE_NONE = 0;
    private static final int RECONFIGURATION_STATE_QUEUE_PENDING = 2;
    private static final int RECONFIGURATION_STATE_WRITE_PENDING = 1;
    private static final int REINITIALIZATION_STATE_NONE = 0;
    private static final int REINITIALIZATION_STATE_SIGNAL_END_OF_STREAM = 1;
    private static final int REINITIALIZATION_STATE_WAIT_END_OF_STREAM = 2;
    protected static final int SOURCE_STATE_NOT_READY = 0;
    protected static final int SOURCE_STATE_READY = 1;
    protected static final int SOURCE_STATE_READY_READ_MAY_FAIL = 2;
    private MediaCodec codec;
    public final CodecCounters codecCounters;
    private boolean codecHasQueuedBuffers;
    private long codecHotswapTimeMs;
    private boolean codecIsAdaptive;
    private boolean codecNeedsEndOfStreamWorkaround;
    private int codecReconfigurationState;
    private boolean codecReconfigured;
    private int codecReinitializationState;
    private final List<Long> decodeOnlyPresentationTimestamps;
    private DrmInitData drmInitData;
    private final DrmSessionManager drmSessionManager;
    protected final Handler eventHandler;
    private final EventListener eventListener;
    private MediaFormat format;
    private final MediaFormatHolder formatHolder;
    private ByteBuffer[] inputBuffers;
    private int inputIndex;
    private boolean inputStreamEnded;
    private boolean openedDrmSession;
    private final BufferInfo outputBufferInfo;
    private ByteBuffer[] outputBuffers;
    private int outputIndex;
    private boolean outputStreamEnded;
    private final boolean playClearSamplesWithoutKeys;
    private final SampleHolder sampleHolder;
    private final SampleSourceReader source;
    private int sourceState;
    private int trackIndex;
    private boolean waitingForFirstSyncFrame;
    private boolean waitingForKeys;

    public interface EventListener {
        void onCryptoError(CryptoException cryptoException);

        void onDecoderInitializationError(DecoderInitializationException decoderInitializationException);

        void onDecoderInitialized(String str, long j, long j2);
    }

    public static class DecoderInitializationException extends Exception {
        private static final int CUSTOM_ERROR_CODE_BASE = -50000;
        private static final int DECODER_QUERY_ERROR = -49998;
        private static final int NO_SUITABLE_DECODER_ERROR = -49999;
        public final String decoderName;
        public final String diagnosticInfo;

        public DecoderInitializationException(MediaFormat mediaFormat, Throwable cause, int errorCode) {
            super("Decoder init failed: [" + errorCode + "], " + mediaFormat, cause);
            this.decoderName = null;
            this.diagnosticInfo = buildCustomDiagnosticInfo(errorCode);
        }

        public DecoderInitializationException(MediaFormat mediaFormat, Throwable cause, String decoderName) {
            super("Decoder init failed: " + decoderName + ", " + mediaFormat, cause);
            this.decoderName = decoderName;
            this.diagnosticInfo = Util.SDK_INT >= 21 ? getDiagnosticInfoV21(cause) : null;
        }

        @TargetApi(21)
        private static String getDiagnosticInfoV21(Throwable cause) {
            if (cause instanceof CodecException) {
                return ((CodecException) cause).getDiagnosticInfo();
            }
            return null;
        }

        private static String buildCustomDiagnosticInfo(int errorCode) {
            return "com.google.android.exoplayer.MediaCodecTrackRenderer_" + (errorCode < 0 ? "neg_" : BuildConfig.FLAVOR) + Math.abs(errorCode);
        }
    }

    protected abstract boolean processOutputBuffer(long j, long j2, MediaCodec mediaCodec, ByteBuffer byteBuffer, BufferInfo bufferInfo, int i, boolean z) throws ExoPlaybackException;

    public MediaCodecTrackRenderer(SampleSource source, DrmSessionManager drmSessionManager, boolean playClearSamplesWithoutKeys, Handler eventHandler, EventListener eventListener) {
        Assertions.checkState(Util.SDK_INT >= 16);
        this.source = source.register();
        this.drmSessionManager = drmSessionManager;
        this.playClearSamplesWithoutKeys = playClearSamplesWithoutKeys;
        this.eventHandler = eventHandler;
        this.eventListener = eventListener;
        this.codecCounters = new CodecCounters();
        this.sampleHolder = new SampleHolder(SOURCE_STATE_NOT_READY);
        this.formatHolder = new MediaFormatHolder();
        this.decodeOnlyPresentationTimestamps = new ArrayList();
        this.outputBufferInfo = new BufferInfo();
        this.codecReconfigurationState = SOURCE_STATE_NOT_READY;
        this.codecReinitializationState = SOURCE_STATE_NOT_READY;
    }

    protected int doPrepare(long positionUs) throws ExoPlaybackException {
        try {
            if (!this.source.prepare(positionUs)) {
                return SOURCE_STATE_NOT_READY;
            }
            for (int i = SOURCE_STATE_NOT_READY; i < this.source.getTrackCount(); i += SOURCE_STATE_READY) {
                if (handlesMimeType(this.source.getTrackInfo(i).mimeType)) {
                    this.trackIndex = i;
                    return SOURCE_STATE_READY;
                }
            }
            return -1;
        } catch (Throwable e) {
            throw new ExoPlaybackException(e);
        }
    }

    protected boolean handlesMimeType(String mimeType) {
        return true;
    }

    protected void onEnabled(long positionUs, boolean joining) {
        this.source.enable(this.trackIndex, positionUs);
        seekToInternal();
    }

    protected DecoderInfo getDecoderInfo(String mimeType, boolean requiresSecureDecoder) throws DecoderQueryException {
        return MediaCodecUtil.getDecoderInfo(mimeType, requiresSecureDecoder);
    }

    protected void configureCodec(MediaCodec codec, String codecName, MediaFormat format, MediaCrypto crypto) {
        codec.configure(format, null, crypto, SOURCE_STATE_NOT_READY);
    }

    protected final void maybeInitCodec() throws ExoPlaybackException {
        if (shouldInitCodec()) {
            String mimeType = this.format.mimeType;
            MediaCrypto mediaCrypto = null;
            boolean requiresSecureDecoder = false;
            if (this.drmInitData != null) {
                if (this.drmSessionManager == null) {
                    throw new ExoPlaybackException("Media requires a DrmSessionManager");
                }
                if (!this.openedDrmSession) {
                    this.drmSessionManager.open(this.drmInitData);
                    this.openedDrmSession = true;
                }
                int drmSessionState = this.drmSessionManager.getState();
                if (drmSessionState == 0) {
                    throw new ExoPlaybackException(this.drmSessionManager.getError());
                } else if (drmSessionState == 3 || drmSessionState == 4) {
                    mediaCrypto = this.drmSessionManager.getMediaCrypto();
                    requiresSecureDecoder = this.drmSessionManager.requiresSecureDecoderComponent(mimeType);
                } else {
                    return;
                }
            }
            DecoderInfo decoderInfo = null;
            try {
                decoderInfo = getDecoderInfo(mimeType, requiresSecureDecoder);
            } catch (Throwable e) {
                notifyAndThrowDecoderInitError(new DecoderInitializationException(this.format, e, -49998));
            }
            if (decoderInfo == null) {
                notifyAndThrowDecoderInitError(new DecoderInitializationException(this.format, null, -49999));
            }
            String decoderName = decoderInfo.name;
            this.codecIsAdaptive = decoderInfo.adaptive;
            this.codecNeedsEndOfStreamWorkaround = codecNeedsEndOfStreamWorkaround(decoderName);
            try {
                long codecInitializingTimestamp = SystemClock.elapsedRealtime();
                TraceUtil.beginSection("createByCodecName(" + decoderName + ")");
                this.codec = MediaCodec.createByCodecName(decoderName);
                TraceUtil.endSection();
                TraceUtil.beginSection("configureCodec");
                configureCodec(this.codec, decoderName, this.format.getFrameworkMediaFormatV16(), mediaCrypto);
                TraceUtil.endSection();
                TraceUtil.beginSection("codec.start()");
                this.codec.start();
                TraceUtil.endSection();
                long codecInitializedTimestamp = SystemClock.elapsedRealtime();
                notifyDecoderInitialized(decoderName, codecInitializedTimestamp, codecInitializedTimestamp - codecInitializingTimestamp);
                this.inputBuffers = this.codec.getInputBuffers();
                this.outputBuffers = this.codec.getOutputBuffers();
            } catch (Throwable e2) {
                notifyAndThrowDecoderInitError(new DecoderInitializationException(this.format, e2, decoderName));
            }
            this.codecHotswapTimeMs = getState() == 3 ? SystemClock.elapsedRealtime() : -1;
            this.inputIndex = -1;
            this.outputIndex = -1;
            this.waitingForFirstSyncFrame = true;
            CodecCounters codecCounters = this.codecCounters;
            codecCounters.codecInitCount += SOURCE_STATE_READY;
        }
    }

    private void notifyAndThrowDecoderInitError(DecoderInitializationException e) throws ExoPlaybackException {
        notifyDecoderInitializationError(e);
        throw new ExoPlaybackException((Throwable) e);
    }

    protected boolean shouldInitCodec() {
        return this.codec == null && this.format != null;
    }

    protected final boolean codecInitialized() {
        return this.codec != null;
    }

    protected final boolean haveFormat() {
        return this.format != null;
    }

    protected void onDisabled() {
        this.format = null;
        this.drmInitData = null;
        try {
            releaseCodec();
            try {
                if (this.openedDrmSession) {
                    this.drmSessionManager.close();
                    this.openedDrmSession = false;
                }
                this.source.disable(this.trackIndex);
            } catch (Throwable th) {
                this.source.disable(this.trackIndex);
            }
        } catch (Throwable th2) {
            this.source.disable(this.trackIndex);
        }
    }

    protected void releaseCodec() {
        if (this.codec != null) {
            this.codecHotswapTimeMs = -1;
            this.inputIndex = -1;
            this.outputIndex = -1;
            this.waitingForKeys = false;
            this.decodeOnlyPresentationTimestamps.clear();
            this.inputBuffers = null;
            this.outputBuffers = null;
            this.codecReconfigured = false;
            this.codecHasQueuedBuffers = false;
            this.codecIsAdaptive = false;
            this.codecNeedsEndOfStreamWorkaround = false;
            this.codecReconfigurationState = SOURCE_STATE_NOT_READY;
            this.codecReinitializationState = SOURCE_STATE_NOT_READY;
            CodecCounters codecCounters = this.codecCounters;
            codecCounters.codecReleaseCount += SOURCE_STATE_READY;
            try {
                this.codec.stop();
                try {
                    this.codec.release();
                } finally {
                    this.codec = null;
                }
            } catch (Throwable th) {
                this.codec.release();
            } finally {
                this.codec = null;
            }
        }
    }

    protected void onReleased() {
        this.source.release();
    }

    protected long getDurationUs() {
        return this.source.getTrackInfo(this.trackIndex).durationUs;
    }

    protected long getBufferedPositionUs() {
        return this.source.getBufferedPositionUs();
    }

    protected void seekTo(long positionUs) throws ExoPlaybackException {
        this.source.seekToUs(positionUs);
        seekToInternal();
    }

    private void seekToInternal() {
        this.sourceState = SOURCE_STATE_NOT_READY;
        this.inputStreamEnded = false;
        this.outputStreamEnded = false;
    }

    protected void onStarted() {
    }

    protected void onStopped() {
    }

    protected void doSomeWork(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
        int i = SOURCE_STATE_READY;
        try {
            if (!this.source.continueBuffering(positionUs)) {
                i = SOURCE_STATE_NOT_READY;
            } else if (this.sourceState != 0) {
                i = this.sourceState;
            }
            this.sourceState = i;
            checkForDiscontinuity(positionUs);
            if (this.format == null) {
                readFormat(positionUs);
            }
            if (this.codec == null && shouldInitCodec()) {
                maybeInitCodec();
            }
            if (this.codec != null) {
                TraceUtil.beginSection("drainAndFeed");
                do {
                } while (drainOutputBuffer(positionUs, elapsedRealtimeUs));
                if (feedInputBuffer(positionUs, true)) {
                    do {
                    } while (feedInputBuffer(positionUs, false));
                }
                TraceUtil.endSection();
            }
            this.codecCounters.ensureUpdated();
        } catch (Throwable e) {
            throw new ExoPlaybackException(e);
        }
    }

    private void readFormat(long positionUs) throws IOException, ExoPlaybackException {
        if (this.source.readData(this.trackIndex, positionUs, this.formatHolder, this.sampleHolder, false) == -4) {
            onInputFormatChanged(this.formatHolder);
        }
    }

    private void checkForDiscontinuity(long positionUs) throws IOException, ExoPlaybackException {
        if (this.codec != null) {
            if (this.source.readData(this.trackIndex, positionUs, this.formatHolder, this.sampleHolder, true) == -5) {
                flushCodec();
            }
        }
    }

    private void flushCodec() throws ExoPlaybackException {
        this.codecHotswapTimeMs = -1;
        this.inputIndex = -1;
        this.outputIndex = -1;
        this.waitingForFirstSyncFrame = true;
        this.waitingForKeys = false;
        this.decodeOnlyPresentationTimestamps.clear();
        if (Util.SDK_INT < 18 || this.codecReinitializationState != 0) {
            releaseCodec();
            maybeInitCodec();
        } else {
            this.codec.flush();
            this.codecHasQueuedBuffers = false;
        }
        if (this.codecReconfigured && this.format != null) {
            this.codecReconfigurationState = SOURCE_STATE_READY;
        }
    }

    private boolean feedInputBuffer(long positionUs, boolean firstFeed) throws IOException, ExoPlaybackException {
        if (this.inputStreamEnded || this.codecReinitializationState == SOURCE_STATE_READY_READ_MAY_FAIL) {
            return false;
        }
        if (this.inputIndex < 0) {
            this.inputIndex = this.codec.dequeueInputBuffer(0);
            if (this.inputIndex < 0) {
                return false;
            }
            this.sampleHolder.data = this.inputBuffers[this.inputIndex];
            this.sampleHolder.data.clear();
        }
        if (this.codecReinitializationState == SOURCE_STATE_READY) {
            if (!this.codecNeedsEndOfStreamWorkaround) {
                this.codec.queueInputBuffer(this.inputIndex, SOURCE_STATE_NOT_READY, SOURCE_STATE_NOT_READY, 0, 4);
                this.inputIndex = -1;
            }
            this.codecReinitializationState = SOURCE_STATE_READY_READ_MAY_FAIL;
            return false;
        }
        int result;
        if (this.waitingForKeys) {
            result = -3;
        } else {
            if (this.codecReconfigurationState == SOURCE_STATE_READY) {
                for (int i = SOURCE_STATE_NOT_READY; i < this.format.initializationData.size(); i += SOURCE_STATE_READY) {
                    this.sampleHolder.data.put((byte[]) this.format.initializationData.get(i));
                }
                this.codecReconfigurationState = SOURCE_STATE_READY_READ_MAY_FAIL;
            }
            result = this.source.readData(this.trackIndex, positionUs, this.formatHolder, this.sampleHolder, false);
            if (firstFeed && this.sourceState == SOURCE_STATE_READY && result == -2) {
                this.sourceState = SOURCE_STATE_READY_READ_MAY_FAIL;
            }
        }
        if (result == -2) {
            return false;
        }
        if (result == -5) {
            flushCodec();
            return true;
        } else if (result == -4) {
            if (this.codecReconfigurationState == SOURCE_STATE_READY_READ_MAY_FAIL) {
                this.sampleHolder.data.clear();
                this.codecReconfigurationState = SOURCE_STATE_READY;
            }
            onInputFormatChanged(this.formatHolder);
            return true;
        } else if (result == -1) {
            if (this.codecReconfigurationState == SOURCE_STATE_READY_READ_MAY_FAIL) {
                this.sampleHolder.data.clear();
                this.codecReconfigurationState = SOURCE_STATE_READY;
            }
            this.inputStreamEnded = true;
            try {
                if (!this.codecNeedsEndOfStreamWorkaround) {
                    this.codec.queueInputBuffer(this.inputIndex, SOURCE_STATE_NOT_READY, SOURCE_STATE_NOT_READY, 0, 4);
                    this.inputIndex = -1;
                }
                return false;
            } catch (Throwable e) {
                notifyCryptoError(e);
                throw new ExoPlaybackException(e);
            }
        } else {
            if (this.waitingForFirstSyncFrame) {
                if (this.sampleHolder.isSyncFrame()) {
                    this.waitingForFirstSyncFrame = false;
                } else {
                    this.sampleHolder.data.clear();
                    if (this.codecReconfigurationState == SOURCE_STATE_READY_READ_MAY_FAIL) {
                        this.codecReconfigurationState = SOURCE_STATE_READY;
                    }
                    return true;
                }
            }
            boolean sampleEncrypted = this.sampleHolder.isEncrypted();
            this.waitingForKeys = shouldWaitForKeys(sampleEncrypted);
            if (this.waitingForKeys) {
                return false;
            }
            try {
                int bufferSize = this.sampleHolder.data.position();
                int adaptiveReconfigurationBytes = bufferSize - this.sampleHolder.size;
                long presentationTimeUs = this.sampleHolder.timeUs;
                if (this.sampleHolder.isDecodeOnly()) {
                    this.decodeOnlyPresentationTimestamps.add(Long.valueOf(presentationTimeUs));
                }
                if (sampleEncrypted) {
                    this.codec.queueSecureInputBuffer(this.inputIndex, SOURCE_STATE_NOT_READY, getFrameworkCryptoInfo(this.sampleHolder, adaptiveReconfigurationBytes), presentationTimeUs, SOURCE_STATE_NOT_READY);
                } else {
                    this.codec.queueInputBuffer(this.inputIndex, SOURCE_STATE_NOT_READY, bufferSize, presentationTimeUs, SOURCE_STATE_NOT_READY);
                }
                this.inputIndex = -1;
                this.codecHasQueuedBuffers = true;
                this.codecReconfigurationState = SOURCE_STATE_NOT_READY;
                return true;
            } catch (Throwable e2) {
                notifyCryptoError(e2);
                throw new ExoPlaybackException(e2);
            }
        }
    }

    private static CryptoInfo getFrameworkCryptoInfo(SampleHolder sampleHolder, int adaptiveReconfigurationBytes) {
        CryptoInfo cryptoInfo = sampleHolder.cryptoInfo.getFrameworkCryptoInfoV16();
        if (adaptiveReconfigurationBytes != 0) {
            if (cryptoInfo.numBytesOfClearData == null) {
                cryptoInfo.numBytesOfClearData = new int[SOURCE_STATE_READY];
            }
            int[] iArr = cryptoInfo.numBytesOfClearData;
            iArr[SOURCE_STATE_NOT_READY] = iArr[SOURCE_STATE_NOT_READY] + adaptiveReconfigurationBytes;
        }
        return cryptoInfo;
    }

    private boolean shouldWaitForKeys(boolean sampleEncrypted) throws ExoPlaybackException {
        if (!this.openedDrmSession) {
            return false;
        }
        int drmManagerState = this.drmSessionManager.getState();
        if (drmManagerState == 0) {
            throw new ExoPlaybackException(this.drmSessionManager.getError());
        } else if (drmManagerState == 4) {
            return false;
        } else {
            if (sampleEncrypted || !this.playClearSamplesWithoutKeys) {
                return true;
            }
            return false;
        }
    }

    protected void onInputFormatChanged(MediaFormatHolder formatHolder) throws ExoPlaybackException {
        MediaFormat oldFormat = this.format;
        this.format = formatHolder.format;
        this.drmInitData = formatHolder.drmInitData;
        if (this.codec != null && canReconfigureCodec(this.codec, this.codecIsAdaptive, oldFormat, this.format)) {
            this.codecReconfigured = true;
            this.codecReconfigurationState = SOURCE_STATE_READY;
        } else if (this.codecHasQueuedBuffers) {
            this.codecReinitializationState = SOURCE_STATE_READY;
        } else {
            releaseCodec();
            maybeInitCodec();
        }
    }

    protected void onOutputFormatChanged(MediaFormat inputFormat, MediaFormat outputFormat) {
    }

    protected boolean canReconfigureCodec(MediaCodec codec, boolean codecIsAdaptive, MediaFormat oldFormat, MediaFormat newFormat) {
        return false;
    }

    protected boolean isEnded() {
        return this.outputStreamEnded;
    }

    protected boolean isReady() {
        return (this.format == null || this.waitingForKeys || (this.sourceState == 0 && this.outputIndex < 0 && !isWithinHotswapPeriod())) ? false : true;
    }

    protected final int getSourceState() {
        return this.sourceState;
    }

    private boolean isWithinHotswapPeriod() {
        return SystemClock.elapsedRealtime() < this.codecHotswapTimeMs + MAX_CODEC_HOTSWAP_TIME_MS;
    }

    protected long getDequeueOutputBufferTimeoutUs() {
        return 0;
    }

    private boolean drainOutputBuffer(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
        if (this.outputStreamEnded) {
            return false;
        }
        if (this.outputIndex < 0) {
            this.outputIndex = this.codec.dequeueOutputBuffer(this.outputBufferInfo, getDequeueOutputBufferTimeoutUs());
        }
        CodecCounters codecCounters;
        if (this.outputIndex == -2) {
            onOutputFormatChanged(this.format, this.codec.getOutputFormat());
            codecCounters = this.codecCounters;
            codecCounters.outputFormatChangedCount += SOURCE_STATE_READY;
            return true;
        } else if (this.outputIndex == -3) {
            this.outputBuffers = this.codec.getOutputBuffers();
            codecCounters = this.codecCounters;
            codecCounters.outputBuffersChangedCount += SOURCE_STATE_READY;
            return true;
        } else if (this.outputIndex < 0) {
            if (!this.codecNeedsEndOfStreamWorkaround || (!this.inputStreamEnded && this.codecReinitializationState != SOURCE_STATE_READY_READ_MAY_FAIL)) {
                return false;
            }
            processEndOfStream();
            return true;
        } else if ((this.outputBufferInfo.flags & 4) != 0) {
            processEndOfStream();
            return false;
        } else {
            int decodeOnlyIndex = getDecodeOnlyIndex(this.outputBufferInfo.presentationTimeUs);
            if (!processOutputBuffer(positionUs, elapsedRealtimeUs, this.codec, this.outputBuffers[this.outputIndex], this.outputBufferInfo, this.outputIndex, decodeOnlyIndex != -1)) {
                return false;
            }
            if (decodeOnlyIndex != -1) {
                this.decodeOnlyPresentationTimestamps.remove(decodeOnlyIndex);
            }
            this.outputIndex = -1;
            return true;
        }
    }

    private void processEndOfStream() throws ExoPlaybackException {
        if (this.codecReinitializationState == SOURCE_STATE_READY_READ_MAY_FAIL) {
            releaseCodec();
            maybeInitCodec();
            return;
        }
        this.outputStreamEnded = true;
    }

    private void notifyDecoderInitializationError(final DecoderInitializationException e) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    MediaCodecTrackRenderer.this.eventListener.onDecoderInitializationError(e);
                }
            });
        }
    }

    private void notifyCryptoError(final CryptoException e) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    MediaCodecTrackRenderer.this.eventListener.onCryptoError(e);
                }
            });
        }
    }

    private void notifyDecoderInitialized(String decoderName, long initializedTimestamp, long initializationDuration) {
        if (this.eventHandler != null && this.eventListener != null) {
            final String str = decoderName;
            final long j = initializedTimestamp;
            final long j2 = initializationDuration;
            this.eventHandler.post(new Runnable() {
                public void run() {
                    MediaCodecTrackRenderer.this.eventListener.onDecoderInitialized(str, j, j2);
                }
            });
        }
    }

    private int getDecodeOnlyIndex(long presentationTimeUs) {
        int size = this.decodeOnlyPresentationTimestamps.size();
        for (int i = SOURCE_STATE_NOT_READY; i < size; i += SOURCE_STATE_READY) {
            if (((Long) this.decodeOnlyPresentationTimestamps.get(i)).longValue() == presentationTimeUs) {
                return i;
            }
        }
        return -1;
    }

    private static boolean codecNeedsEndOfStreamWorkaround(String name) {
        return Util.SDK_INT <= 17 && "ht7s3".equals(Util.DEVICE) && "OMX.rk.video_decoder.avc".equals(name);
    }
}
