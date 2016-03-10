package com.mobcrush.mobcrush.player;

import android.media.MediaCodec.CryptoException;
import android.os.SystemClock;
import android.util.Log;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.audio.AudioTrack.InitializationException;
import com.google.android.exoplayer.audio.AudioTrack.WriteException;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.util.VerboseLogUtil;
import com.mobcrush.mobcrush.player.Player.InfoListener;
import com.mobcrush.mobcrush.player.Player.InternalErrorListener;
import com.mobcrush.mobcrush.player.Player.Listener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class EventLogger implements Listener, InfoListener, InternalErrorListener {
    private static final String TAG = "EventLogger";
    private static final NumberFormat TIME_FORMAT = NumberFormat.getInstance(Locale.US);
    private long[] loadStartTimeMs = new long[4];
    private long[] seekRangeValuesUs;
    private long sessionStartTimeMs;

    static {
        TIME_FORMAT.setMinimumFractionDigits(2);
        TIME_FORMAT.setMaximumFractionDigits(2);
    }

    public void startSession() {
        this.sessionStartTimeMs = SystemClock.elapsedRealtime();
        Log.d(TAG, "start [0]");
    }

    public void endSession() {
        Log.d(TAG, "end [" + getSessionTimeString() + "]");
    }

    public void onStateChanged(boolean playWhenReady, int state) {
        Log.d(TAG, "state [" + getSessionTimeString() + ", " + playWhenReady + ", " + getStateString(state) + "]");
    }

    public void onError(Exception e) {
        Log.e(TAG, "playerFailed [" + getSessionTimeString() + "]", e);
    }

    public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {
        Log.d(TAG, "videoSizeChanged [" + width + ", " + height + ", " + pixelWidthHeightRatio + "]");
    }

    public void onBandwidthSample(int elapsedMs, long bytes, long bitrateEstimate) {
        Log.d(TAG, "bandwidth [" + getSessionTimeString() + ", " + bytes + ", " + getTimeString((long) elapsedMs) + ", " + bitrateEstimate + "]");
    }

    public void onDroppedFrames(int count, long elapsed) {
        Log.d(TAG, "droppedFrames [" + getSessionTimeString() + ", " + count + "]");
    }

    public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, int mediaStartTimeMs, int mediaEndTimeMs) {
        this.loadStartTimeMs[sourceId] = SystemClock.elapsedRealtime();
        if (VerboseLogUtil.isTagEnabled(TAG)) {
            Log.v(TAG, "loadStart [" + getSessionTimeString() + ", " + sourceId + ", " + type + ", " + mediaStartTimeMs + ", " + mediaEndTimeMs + "]");
        }
    }

    public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, int mediaStartTimeMs, int mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {
        if (VerboseLogUtil.isTagEnabled(TAG)) {
            Log.v(TAG, "loadEnd [" + getSessionTimeString() + ", " + sourceId + ", " + (SystemClock.elapsedRealtime() - this.loadStartTimeMs[sourceId]) + "]");
        }
    }

    public void onVideoFormatEnabled(Format format, int trigger, int mediaTimeMs) {
        Log.d(TAG, "videoFormat [" + getSessionTimeString() + ", " + format.id + ", " + Integer.toString(trigger) + "]");
    }

    public void onAudioFormatEnabled(Format format, int trigger, int mediaTimeMs) {
        Log.d(TAG, "audioFormat [" + getSessionTimeString() + ", " + format.id + ", " + Integer.toString(trigger) + "]");
    }

    public void onLoadError(int sourceId, IOException e) {
        printInternalError("loadError", e);
    }

    public void onRendererInitializationError(Exception e) {
        printInternalError("rendererInitError", e);
    }

    public void onDrmSessionManagerError(Exception e) {
        printInternalError("drmSessionManagerError", e);
    }

    public void onDecoderInitializationError(DecoderInitializationException e) {
        printInternalError("decoderInitializationError", e);
    }

    public void onAudioTrackInitializationError(InitializationException e) {
        printInternalError("audioTrackInitializationError", e);
    }

    public void onAudioTrackWriteError(WriteException e) {
        printInternalError("audioTrackWriteError", e);
    }

    public void onCryptoError(CryptoException e) {
        printInternalError("cryptoError", e);
    }

    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
        Log.d(TAG, "decoderInitialized [" + getSessionTimeString() + ", " + decoderName + "]");
    }

    public void onSeekRangeChanged(TimeRange seekRange) {
        this.seekRangeValuesUs = seekRange.getCurrentBoundsUs(this.seekRangeValuesUs);
        Log.d(TAG, "seekRange [ " + seekRange.type + ", " + this.seekRangeValuesUs[0] + ", " + this.seekRangeValuesUs[1] + "]");
    }

    private void printInternalError(String type, Exception e) {
        Log.e(TAG, "internalError [" + getSessionTimeString() + ", " + type + "]", e);
    }

    private String getStateString(int state) {
        switch (state) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return "I";
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return "P";
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return "B";
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                return "R";
            case Player.STATE_ENDED /*5*/:
                return "E";
            default:
                return "?";
        }
    }

    private String getSessionTimeString() {
        return getTimeString(SystemClock.elapsedRealtime() - this.sessionStartTimeMs);
    }

    private String getTimeString(long timeMs) {
        return TIME_FORMAT.format((double) (((float) timeMs) / 1000.0f));
    }
}
