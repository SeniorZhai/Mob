package com.google.android.exoplayer;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.android.exoplayer.ExoPlayer.ExoPlayerComponent;
import com.google.android.exoplayer.ExoPlayer.Listener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArraySet;

final class ExoPlayerImpl implements ExoPlayer {
    private static final String TAG = "ExoPlayerImpl";
    private final Handler eventHandler;
    private final ExoPlayerImplInternal internalPlayer;
    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet();
    private int pendingPlayWhenReadyAcks;
    private boolean playWhenReady = false;
    private int playbackState = 1;
    private final boolean[] rendererEnabledFlags;
    private final boolean[] rendererHasMediaFlags;

    @SuppressLint({"HandlerLeak"})
    public ExoPlayerImpl(int rendererCount, int minBufferMs, int minRebufferMs) {
        Log.i(TAG, "Init 1.3.3");
        this.rendererHasMediaFlags = new boolean[rendererCount];
        this.rendererEnabledFlags = new boolean[rendererCount];
        for (int i = 0; i < this.rendererEnabledFlags.length; i++) {
            this.rendererEnabledFlags[i] = true;
        }
        this.eventHandler = new Handler() {
            public void handleMessage(Message msg) {
                ExoPlayerImpl.this.handleEvent(msg);
            }
        };
        this.internalPlayer = new ExoPlayerImplInternal(this.eventHandler, this.playWhenReady, this.rendererEnabledFlags, minBufferMs, minRebufferMs);
    }

    public Looper getPlaybackLooper() {
        return this.internalPlayer.getPlaybackLooper();
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public int getPlaybackState() {
        return this.playbackState;
    }

    public void prepare(TrackRenderer... renderers) {
        Arrays.fill(this.rendererHasMediaFlags, false);
        this.internalPlayer.prepare(renderers);
    }

    public boolean getRendererHasMedia(int rendererIndex) {
        return this.rendererHasMediaFlags[rendererIndex];
    }

    public void setRendererEnabled(int rendererIndex, boolean enabled) {
        if (this.rendererEnabledFlags[rendererIndex] != enabled) {
            this.rendererEnabledFlags[rendererIndex] = enabled;
            this.internalPlayer.setRendererEnabled(rendererIndex, enabled);
        }
    }

    public boolean getRendererEnabled(int rendererIndex) {
        return this.rendererEnabledFlags[rendererIndex];
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        if (this.playWhenReady != playWhenReady) {
            this.playWhenReady = playWhenReady;
            this.pendingPlayWhenReadyAcks++;
            this.internalPlayer.setPlayWhenReady(playWhenReady);
            Iterator i$ = this.listeners.iterator();
            while (i$.hasNext()) {
                ((Listener) i$.next()).onPlayerStateChanged(playWhenReady, this.playbackState);
            }
        }
    }

    public boolean getPlayWhenReady() {
        return this.playWhenReady;
    }

    public boolean isPlayWhenReadyCommitted() {
        return this.pendingPlayWhenReadyAcks == 0;
    }

    public void seekTo(long positionMs) {
        this.internalPlayer.seekTo(positionMs);
    }

    public void stop() {
        this.internalPlayer.stop();
    }

    public void release() {
        this.internalPlayer.release();
        this.eventHandler.removeCallbacksAndMessages(null);
    }

    public void sendMessage(ExoPlayerComponent target, int messageType, Object message) {
        this.internalPlayer.sendMessage(target, messageType, message);
    }

    public void blockingSendMessage(ExoPlayerComponent target, int messageType, Object message) {
        this.internalPlayer.blockingSendMessage(target, messageType, message);
    }

    public long getDuration() {
        return this.internalPlayer.getDuration();
    }

    public long getCurrentPosition() {
        return this.internalPlayer.getCurrentPosition();
    }

    public long getBufferedPosition() {
        return this.internalPlayer.getBufferedPosition();
    }

    public int getBufferedPercentage() {
        long j = 100;
        long bufferedPosition = getBufferedPosition();
        long duration = getDuration();
        if (bufferedPosition == -1 || duration == -1) {
            return 0;
        }
        if (duration != 0) {
            j = (100 * bufferedPosition) / duration;
        }
        return (int) j;
    }

    void handleEvent(Message msg) {
        Iterator i$;
        switch (msg.what) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                boolean[] rendererHasMediaFlags = (boolean[]) msg.obj;
                System.arraycopy(rendererHasMediaFlags, 0, this.rendererHasMediaFlags, 0, rendererHasMediaFlags.length);
                this.playbackState = msg.arg1;
                i$ = this.listeners.iterator();
                while (i$.hasNext()) {
                    ((Listener) i$.next()).onPlayerStateChanged(this.playWhenReady, this.playbackState);
                }
                return;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                this.playbackState = msg.arg1;
                i$ = this.listeners.iterator();
                while (i$.hasNext()) {
                    ((Listener) i$.next()).onPlayerStateChanged(this.playWhenReady, this.playbackState);
                }
                return;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                this.pendingPlayWhenReadyAcks--;
                if (this.pendingPlayWhenReadyAcks == 0) {
                    i$ = this.listeners.iterator();
                    while (i$.hasNext()) {
                        ((Listener) i$.next()).onPlayWhenReadyCommitted();
                    }
                    return;
                }
                return;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                ExoPlaybackException exception = msg.obj;
                i$ = this.listeners.iterator();
                while (i$.hasNext()) {
                    ((Listener) i$.next()).onPlayerError(exception);
                }
                return;
            default:
                return;
        }
    }
}
