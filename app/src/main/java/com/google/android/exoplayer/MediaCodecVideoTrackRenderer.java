package com.google.android.exoplayer;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCrypto;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Surface;
import com.android.volley.DefaultRetryPolicy;
import com.google.android.exoplayer.drm.DrmSessionManager;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.TraceUtil;
import com.google.android.exoplayer.util.Util;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.nio.ByteBuffer;

@TargetApi(16)
public class MediaCodecVideoTrackRenderer extends MediaCodecTrackRenderer {
    private static final String KEY_CROP_BOTTOM = "crop-bottom";
    private static final String KEY_CROP_LEFT = "crop-left";
    private static final String KEY_CROP_RIGHT = "crop-right";
    private static final String KEY_CROP_TOP = "crop-top";
    public static final int MSG_SET_SURFACE = 1;
    private final long allowedJoiningTimeUs;
    private int currentHeight;
    private float currentPixelWidthHeightRatio;
    private int currentWidth;
    private long droppedFrameAccumulationStartTimeMs;
    private int droppedFrameCount;
    private final EventListener eventListener;
    private final FrameReleaseTimeHelper frameReleaseTimeHelper;
    private long joiningDeadlineUs;
    private int lastReportedHeight;
    private float lastReportedPixelWidthHeightRatio;
    private int lastReportedWidth;
    private final int maxDroppedFrameCountToNotify;
    private float pendingPixelWidthHeightRatio;
    private boolean renderedFirstFrame;
    private boolean reportedDrawnToSurface;
    private Surface surface;
    private final int videoScalingMode;

    public interface EventListener extends com.google.android.exoplayer.MediaCodecTrackRenderer.EventListener {
        void onDrawnToSurface(Surface surface);

        void onDroppedFrames(int i, long j);

        void onVideoSizeChanged(int i, int i2, float f);
    }

    public interface FrameReleaseTimeHelper {
        long adjustReleaseTime(long j, long j2);

        void disable();

        void enable();
    }

    public MediaCodecVideoTrackRenderer(SampleSource source, int videoScalingMode) {
        this(source, null, true, videoScalingMode);
    }

    public MediaCodecVideoTrackRenderer(SampleSource source, DrmSessionManager drmSessionManager, boolean playClearSamplesWithoutKeys, int videoScalingMode) {
        this(source, drmSessionManager, playClearSamplesWithoutKeys, videoScalingMode, 0);
    }

    public MediaCodecVideoTrackRenderer(SampleSource source, int videoScalingMode, long allowedJoiningTimeMs) {
        this(source, null, true, videoScalingMode, allowedJoiningTimeMs);
    }

    public MediaCodecVideoTrackRenderer(SampleSource source, DrmSessionManager drmSessionManager, boolean playClearSamplesWithoutKeys, int videoScalingMode, long allowedJoiningTimeMs) {
        this(source, drmSessionManager, playClearSamplesWithoutKeys, videoScalingMode, allowedJoiningTimeMs, null, null, null, -1);
    }

    public MediaCodecVideoTrackRenderer(SampleSource source, int videoScalingMode, long allowedJoiningTimeMs, Handler eventHandler, EventListener eventListener, int maxDroppedFrameCountToNotify) {
        this(source, null, true, videoScalingMode, allowedJoiningTimeMs, null, eventHandler, eventListener, maxDroppedFrameCountToNotify);
    }

    public MediaCodecVideoTrackRenderer(SampleSource source, DrmSessionManager drmSessionManager, boolean playClearSamplesWithoutKeys, int videoScalingMode, long allowedJoiningTimeMs, FrameReleaseTimeHelper frameReleaseTimeHelper, Handler eventHandler, EventListener eventListener, int maxDroppedFrameCountToNotify) {
        super(source, drmSessionManager, playClearSamplesWithoutKeys, eventHandler, eventListener);
        this.videoScalingMode = videoScalingMode;
        this.allowedJoiningTimeUs = 1000 * allowedJoiningTimeMs;
        this.frameReleaseTimeHelper = frameReleaseTimeHelper;
        this.eventListener = eventListener;
        this.maxDroppedFrameCountToNotify = maxDroppedFrameCountToNotify;
        this.joiningDeadlineUs = -1;
        this.currentWidth = -1;
        this.currentHeight = -1;
        this.currentPixelWidthHeightRatio = -1.0f;
        this.pendingPixelWidthHeightRatio = -1.0f;
        this.lastReportedWidth = -1;
        this.lastReportedHeight = -1;
        this.lastReportedPixelWidthHeightRatio = -1.0f;
    }

    protected boolean handlesMimeType(String mimeType) {
        return MimeTypes.isVideo(mimeType) && super.handlesMimeType(mimeType);
    }

    protected void onEnabled(long positionUs, boolean joining) {
        super.onEnabled(positionUs, joining);
        this.renderedFirstFrame = false;
        if (joining && this.allowedJoiningTimeUs > 0) {
            this.joiningDeadlineUs = (SystemClock.elapsedRealtime() * 1000) + this.allowedJoiningTimeUs;
        }
        if (this.frameReleaseTimeHelper != null) {
            this.frameReleaseTimeHelper.enable();
        }
    }

    protected void seekTo(long positionUs) throws ExoPlaybackException {
        super.seekTo(positionUs);
        this.renderedFirstFrame = false;
        this.joiningDeadlineUs = -1;
    }

    protected boolean isReady() {
        if (super.isReady() && (this.renderedFirstFrame || !codecInitialized() || getSourceState() == 2)) {
            this.joiningDeadlineUs = -1;
            return true;
        } else if (this.joiningDeadlineUs == -1) {
            return false;
        } else {
            if (SystemClock.elapsedRealtime() * 1000 < this.joiningDeadlineUs) {
                return true;
            }
            this.joiningDeadlineUs = -1;
            return false;
        }
    }

    protected void onStarted() {
        super.onStarted();
        this.droppedFrameCount = 0;
        this.droppedFrameAccumulationStartTimeMs = SystemClock.elapsedRealtime();
    }

    protected void onStopped() {
        this.joiningDeadlineUs = -1;
        maybeNotifyDroppedFrameCount();
        super.onStopped();
    }

    public void onDisabled() {
        this.currentWidth = -1;
        this.currentHeight = -1;
        this.currentPixelWidthHeightRatio = -1.0f;
        this.pendingPixelWidthHeightRatio = -1.0f;
        this.lastReportedWidth = -1;
        this.lastReportedHeight = -1;
        this.lastReportedPixelWidthHeightRatio = -1.0f;
        if (this.frameReleaseTimeHelper != null) {
            this.frameReleaseTimeHelper.disable();
        }
        super.onDisabled();
    }

    public void handleMessage(int messageType, Object message) throws ExoPlaybackException {
        if (messageType == MSG_SET_SURFACE) {
            setSurface((Surface) message);
        } else {
            super.handleMessage(messageType, message);
        }
    }

    private void setSurface(Surface surface) throws ExoPlaybackException {
        if (this.surface != surface) {
            this.surface = surface;
            this.reportedDrawnToSurface = false;
            int state = getState();
            if (state == 2 || state == 3) {
                releaseCodec();
                maybeInitCodec();
            }
        }
    }

    protected boolean shouldInitCodec() {
        return super.shouldInitCodec() && this.surface != null && this.surface.isValid();
    }

    protected void configureCodec(MediaCodec codec, String codecName, MediaFormat format, MediaCrypto crypto) {
        codec.configure(format, this.surface, crypto, 0);
        codec.setVideoScalingMode(this.videoScalingMode);
    }

    protected void onInputFormatChanged(MediaFormatHolder holder) throws ExoPlaybackException {
        super.onInputFormatChanged(holder);
        this.pendingPixelWidthHeightRatio = holder.format.pixelWidthHeightRatio == -1.0f ? DefaultRetryPolicy.DEFAULT_BACKOFF_MULT : holder.format.pixelWidthHeightRatio;
    }

    protected final boolean haveRenderedFirstFrame() {
        return this.renderedFirstFrame;
    }

    protected void onOutputFormatChanged(MediaFormat inputFormat, MediaFormat outputFormat) {
        boolean hasCrop = outputFormat.containsKey(KEY_CROP_RIGHT) && outputFormat.containsKey(KEY_CROP_LEFT) && outputFormat.containsKey(KEY_CROP_BOTTOM) && outputFormat.containsKey(KEY_CROP_TOP);
        this.currentWidth = hasCrop ? (outputFormat.getInteger(KEY_CROP_RIGHT) - outputFormat.getInteger(KEY_CROP_LEFT)) + MSG_SET_SURFACE : outputFormat.getInteger(SettingsJsonConstants.ICON_WIDTH_KEY);
        this.currentHeight = hasCrop ? (outputFormat.getInteger(KEY_CROP_BOTTOM) - outputFormat.getInteger(KEY_CROP_TOP)) + MSG_SET_SURFACE : outputFormat.getInteger(SettingsJsonConstants.ICON_HEIGHT_KEY);
        this.currentPixelWidthHeightRatio = this.pendingPixelWidthHeightRatio;
    }

    protected boolean canReconfigureCodec(MediaCodec codec, boolean codecIsAdaptive, MediaFormat oldFormat, MediaFormat newFormat) {
        return newFormat.mimeType.equals(oldFormat.mimeType) && (codecIsAdaptive || (oldFormat.width == newFormat.width && oldFormat.height == newFormat.height));
    }

    protected boolean processOutputBuffer(long positionUs, long elapsedRealtimeUs, MediaCodec codec, ByteBuffer buffer, BufferInfo bufferInfo, int bufferIndex, boolean shouldSkip) {
        if (shouldSkip) {
            skipOutputBuffer(codec, bufferIndex);
            return true;
        }
        long adjustedReleaseTimeNs;
        long earlyUs = (bufferInfo.presentationTimeUs - positionUs) - ((SystemClock.elapsedRealtime() * 1000) - elapsedRealtimeUs);
        long systemTimeNs = System.nanoTime();
        long unadjustedFrameReleaseTimeNs = systemTimeNs + (1000 * earlyUs);
        if (this.frameReleaseTimeHelper != null) {
            adjustedReleaseTimeNs = this.frameReleaseTimeHelper.adjustReleaseTime(bufferInfo.presentationTimeUs, unadjustedFrameReleaseTimeNs);
            earlyUs = (adjustedReleaseTimeNs - systemTimeNs) / 1000;
        } else {
            adjustedReleaseTimeNs = unadjustedFrameReleaseTimeNs;
        }
        if (earlyUs < -30000) {
            dropOutputBuffer(codec, bufferIndex);
            return true;
        } else if (!this.renderedFirstFrame) {
            renderOutputBufferImmediate(codec, bufferIndex);
            return true;
        } else if (getState() != 3) {
            return false;
        } else {
            if (Util.SDK_INT >= 21) {
                if (earlyUs < 50000) {
                    renderOutputBufferTimedV21(codec, bufferIndex, adjustedReleaseTimeNs);
                    return true;
                }
            } else if (earlyUs < Constants.BROADCASTS_LIVE_TIME) {
                if (earlyUs > 11000) {
                    try {
                        Thread.sleep((earlyUs - 10000) / 1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                renderOutputBufferImmediate(codec, bufferIndex);
                return true;
            }
            return false;
        }
    }

    protected void skipOutputBuffer(MediaCodec codec, int bufferIndex) {
        TraceUtil.beginSection("skipVideoBuffer");
        codec.releaseOutputBuffer(bufferIndex, false);
        TraceUtil.endSection();
        CodecCounters codecCounters = this.codecCounters;
        codecCounters.skippedOutputBufferCount += MSG_SET_SURFACE;
    }

    protected void dropOutputBuffer(MediaCodec codec, int bufferIndex) {
        TraceUtil.beginSection("dropVideoBuffer");
        codec.releaseOutputBuffer(bufferIndex, false);
        TraceUtil.endSection();
        CodecCounters codecCounters = this.codecCounters;
        codecCounters.droppedOutputBufferCount += MSG_SET_SURFACE;
        this.droppedFrameCount += MSG_SET_SURFACE;
        if (this.droppedFrameCount == this.maxDroppedFrameCountToNotify) {
            maybeNotifyDroppedFrameCount();
        }
    }

    protected void renderOutputBufferImmediate(MediaCodec codec, int bufferIndex) {
        maybeNotifyVideoSizeChanged();
        TraceUtil.beginSection("renderVideoBufferImmediate");
        codec.releaseOutputBuffer(bufferIndex, true);
        TraceUtil.endSection();
        CodecCounters codecCounters = this.codecCounters;
        codecCounters.renderedOutputBufferCount += MSG_SET_SURFACE;
        this.renderedFirstFrame = true;
        maybeNotifyDrawnToSurface();
    }

    @TargetApi(21)
    protected void renderOutputBufferTimedV21(MediaCodec codec, int bufferIndex, long releaseTimeNs) {
        maybeNotifyVideoSizeChanged();
        TraceUtil.beginSection("releaseOutputBufferTimed");
        codec.releaseOutputBuffer(bufferIndex, releaseTimeNs);
        TraceUtil.endSection();
        CodecCounters codecCounters = this.codecCounters;
        codecCounters.renderedOutputBufferCount += MSG_SET_SURFACE;
        this.renderedFirstFrame = true;
        maybeNotifyDrawnToSurface();
    }

    private void maybeNotifyVideoSizeChanged() {
        if (this.eventHandler != null && this.eventListener != null) {
            if (this.lastReportedWidth != this.currentWidth || this.lastReportedHeight != this.currentHeight || this.lastReportedPixelWidthHeightRatio != this.currentPixelWidthHeightRatio) {
                final int currentWidth = this.currentWidth;
                final int currentHeight = this.currentHeight;
                final float currentPixelWidthHeightRatio = this.currentPixelWidthHeightRatio;
                this.eventHandler.post(new Runnable() {
                    public void run() {
                        MediaCodecVideoTrackRenderer.this.eventListener.onVideoSizeChanged(currentWidth, currentHeight, currentPixelWidthHeightRatio);
                    }
                });
                this.lastReportedWidth = currentWidth;
                this.lastReportedHeight = currentHeight;
                this.lastReportedPixelWidthHeightRatio = currentPixelWidthHeightRatio;
            }
        }
    }

    private void maybeNotifyDrawnToSurface() {
        if (this.eventHandler != null && this.eventListener != null && !this.reportedDrawnToSurface) {
            final Surface surface = this.surface;
            this.eventHandler.post(new Runnable() {
                public void run() {
                    MediaCodecVideoTrackRenderer.this.eventListener.onDrawnToSurface(surface);
                }
            });
            this.reportedDrawnToSurface = true;
        }
    }

    private void maybeNotifyDroppedFrameCount() {
        if (this.eventHandler != null && this.eventListener != null && this.droppedFrameCount != 0) {
            long now = SystemClock.elapsedRealtime();
            final int countToNotify = this.droppedFrameCount;
            final long elapsedToNotify = now - this.droppedFrameAccumulationStartTimeMs;
            this.eventHandler.post(new Runnable() {
                public void run() {
                    MediaCodecVideoTrackRenderer.this.eventListener.onDroppedFrames(countToNotify, elapsedToNotify);
                }
            });
            this.droppedFrameCount = 0;
            this.droppedFrameAccumulationStartTimeMs = now;
        }
    }
}
