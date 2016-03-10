package com.google.android.exoplayer;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Handler;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.audio.AudioTrack;
import com.google.android.exoplayer.audio.AudioTrack.InitializationException;
import com.google.android.exoplayer.audio.AudioTrack.WriteException;
import com.google.android.exoplayer.drm.DrmSessionManager;
import com.google.android.exoplayer.util.MimeTypes;
import java.nio.ByteBuffer;

@TargetApi(16)
public class MediaCodecAudioTrackRenderer extends MediaCodecTrackRenderer implements MediaClock {
    public static final int MSG_SET_VOLUME = 1;
    private static final String RAW_DECODER_NAME = "OMX.google.raw.decoder";
    private boolean allowPositionDiscontinuity;
    private int audioSessionId;
    private final AudioTrack audioTrack;
    private long currentPositionUs;
    private final EventListener eventListener;

    public interface EventListener extends com.google.android.exoplayer.MediaCodecTrackRenderer.EventListener {
        void onAudioTrackInitializationError(InitializationException initializationException);

        void onAudioTrackWriteError(WriteException writeException);
    }

    public MediaCodecAudioTrackRenderer(SampleSource source) {
        this(source, null, true);
    }

    public MediaCodecAudioTrackRenderer(SampleSource source, DrmSessionManager drmSessionManager, boolean playClearSamplesWithoutKeys) {
        this(source, drmSessionManager, playClearSamplesWithoutKeys, null, null);
    }

    public MediaCodecAudioTrackRenderer(SampleSource source, Handler eventHandler, EventListener eventListener) {
        this(source, null, true, eventHandler, eventListener);
    }

    public MediaCodecAudioTrackRenderer(SampleSource source, DrmSessionManager drmSessionManager, boolean playClearSamplesWithoutKeys, Handler eventHandler, EventListener eventListener) {
        super(source, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener);
        this.eventListener = eventListener;
        this.audioSessionId = 0;
        this.audioTrack = new AudioTrack();
    }

    protected DecoderInfo getDecoderInfo(String mimeType, boolean requiresSecureDecoder) throws DecoderQueryException {
        if (MimeTypes.isPassthroughAudio(mimeType)) {
            return new DecoderInfo(RAW_DECODER_NAME, true);
        }
        return super.getDecoderInfo(mimeType, requiresSecureDecoder);
    }

    protected void configureCodec(MediaCodec codec, String codecName, MediaFormat format, MediaCrypto crypto) {
        if (RAW_DECODER_NAME.equals(codecName)) {
            String mimeType = format.getString("mime");
            format.setString("mime", MimeTypes.AUDIO_RAW);
            codec.configure(format, null, crypto, 0);
            format.setString("mime", mimeType);
            return;
        }
        codec.configure(format, null, crypto, 0);
    }

    protected MediaClock getMediaClock() {
        return this;
    }

    protected boolean handlesMimeType(String mimeType) {
        return MimeTypes.isAudio(mimeType) && super.handlesMimeType(mimeType);
    }

    protected void onEnabled(long positionUs, boolean joining) {
        super.onEnabled(positionUs, joining);
        seekToInternal(positionUs);
    }

    protected void onOutputFormatChanged(MediaFormat inputFormat, MediaFormat outputFormat) {
        if (MimeTypes.isPassthroughAudio(inputFormat.mimeType)) {
            this.audioTrack.reconfigure(inputFormat.getFrameworkMediaFormatV16());
        } else {
            this.audioTrack.reconfigure(outputFormat);
        }
    }

    protected void onAudioSessionId(int audioSessionId) {
    }

    protected void onStarted() {
        super.onStarted();
        this.audioTrack.play();
    }

    protected void onStopped() {
        this.audioTrack.pause();
        super.onStopped();
    }

    protected boolean isEnded() {
        return super.isEnded() && !(this.audioTrack.hasPendingData() && this.audioTrack.hasEnoughDataToBeginPlayback());
    }

    protected boolean isReady() {
        return this.audioTrack.hasPendingData() || (super.isReady() && getSourceState() == 2);
    }

    public long getPositionUs() {
        long newCurrentPositionUs = this.audioTrack.getCurrentPositionUs(isEnded());
        if (newCurrentPositionUs != Long.MIN_VALUE) {
            if (!this.allowPositionDiscontinuity) {
                newCurrentPositionUs = Math.max(this.currentPositionUs, newCurrentPositionUs);
            }
            this.currentPositionUs = newCurrentPositionUs;
            this.allowPositionDiscontinuity = false;
        }
        return this.currentPositionUs;
    }

    protected void onDisabled() {
        this.audioSessionId = 0;
        try {
            this.audioTrack.release();
        } finally {
            super.onDisabled();
        }
    }

    protected void seekTo(long positionUs) throws ExoPlaybackException {
        super.seekTo(positionUs);
        seekToInternal(positionUs);
    }

    private void seekToInternal(long positionUs) {
        this.audioTrack.reset();
        this.currentPositionUs = positionUs;
        this.allowPositionDiscontinuity = true;
    }

    protected boolean processOutputBuffer(long positionUs, long elapsedRealtimeUs, MediaCodec codec, ByteBuffer buffer, BufferInfo bufferInfo, int bufferIndex, boolean shouldSkip) throws ExoPlaybackException {
        if (shouldSkip) {
            codec.releaseOutputBuffer(bufferIndex, false);
            CodecCounters codecCounters = this.codecCounters;
            codecCounters.skippedOutputBufferCount += MSG_SET_VOLUME;
            this.audioTrack.handleDiscontinuity();
            return true;
        }
        if (!this.audioTrack.isInitialized()) {
            try {
                if (this.audioSessionId != 0) {
                    this.audioTrack.initialize(this.audioSessionId);
                } else {
                    this.audioSessionId = this.audioTrack.initialize();
                    onAudioSessionId(this.audioSessionId);
                }
                if (getState() == 3) {
                    this.audioTrack.play();
                }
            } catch (Throwable e) {
                notifyAudioTrackInitializationError(e);
                throw new ExoPlaybackException(e);
            }
        }
        try {
            int handleBufferResult = this.audioTrack.handleBuffer(buffer, bufferInfo.offset, bufferInfo.size, bufferInfo.presentationTimeUs);
            if ((handleBufferResult & MSG_SET_VOLUME) != 0) {
                handleDiscontinuity();
                this.allowPositionDiscontinuity = true;
            }
            if ((handleBufferResult & 2) == 0) {
                return false;
            }
            codec.releaseOutputBuffer(bufferIndex, false);
            codecCounters = this.codecCounters;
            codecCounters.renderedOutputBufferCount += MSG_SET_VOLUME;
            return true;
        } catch (Throwable e2) {
            notifyAudioTrackWriteError(e2);
            throw new ExoPlaybackException(e2);
        }
    }

    protected void handleDiscontinuity() {
    }

    public void handleMessage(int messageType, Object message) throws ExoPlaybackException {
        if (messageType == MSG_SET_VOLUME) {
            this.audioTrack.setVolume(((Float) message).floatValue());
        } else {
            super.handleMessage(messageType, message);
        }
    }

    private void notifyAudioTrackInitializationError(final InitializationException e) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    MediaCodecAudioTrackRenderer.this.eventListener.onAudioTrackInitializationError(e);
                }
            });
        }
    }

    private void notifyAudioTrackWriteError(final WriteException e) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    MediaCodecAudioTrackRenderer.this.eventListener.onAudioTrackWriteError(e);
                }
            });
        }
    }
}
