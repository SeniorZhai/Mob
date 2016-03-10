package com.google.android.exoplayer;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import com.google.android.exoplayer.ExoPlayer.ExoPlayerComponent;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.PriorityHandlerThread;
import com.google.android.exoplayer.util.TraceUtil;
import java.util.ArrayList;
import java.util.List;

final class ExoPlayerImplInternal implements Callback {
    private static final int IDLE_INTERVAL_MS = 1000;
    private static final int MSG_CUSTOM = 9;
    private static final int MSG_DO_SOME_WORK = 7;
    public static final int MSG_ERROR = 4;
    private static final int MSG_INCREMENTAL_PREPARE = 2;
    private static final int MSG_PREPARE = 1;
    public static final int MSG_PREPARED = 1;
    private static final int MSG_RELEASE = 5;
    private static final int MSG_SEEK_TO = 6;
    private static final int MSG_SET_PLAY_WHEN_READY = 3;
    public static final int MSG_SET_PLAY_WHEN_READY_ACK = 3;
    private static final int MSG_SET_RENDERER_ENABLED = 8;
    public static final int MSG_STATE_CHANGED = 2;
    private static final int MSG_STOP = 4;
    private static final int PREPARE_INTERVAL_MS = 10;
    private static final int RENDERING_INTERVAL_MS = 10;
    private static final String TAG = "ExoPlayerImplInternal";
    private volatile long bufferedPositionUs;
    private int customMessagesProcessed = 0;
    private int customMessagesSent = 0;
    private volatile long durationUs;
    private long elapsedRealtimeUs;
    private final List<TrackRenderer> enabledRenderers;
    private final Handler eventHandler;
    private final Handler handler;
    private final HandlerThread internalPlaybackThread;
    private final long minBufferUs;
    private final long minRebufferUs;
    private boolean playWhenReady;
    private volatile long positionUs;
    private boolean rebuffering;
    private boolean released;
    private final boolean[] rendererEnabledFlags;
    private MediaClock rendererMediaClock;
    private TrackRenderer rendererMediaClockSource;
    private TrackRenderer[] renderers;
    private final StandaloneMediaClock standaloneMediaClock;
    private int state;

    public ExoPlayerImplInternal(Handler eventHandler, boolean playWhenReady, boolean[] rendererEnabledFlags, int minBufferMs, int minRebufferMs) {
        this.eventHandler = eventHandler;
        this.playWhenReady = playWhenReady;
        this.rendererEnabledFlags = new boolean[rendererEnabledFlags.length];
        this.minBufferUs = ((long) minBufferMs) * 1000;
        this.minRebufferUs = ((long) minRebufferMs) * 1000;
        for (int i = 0; i < rendererEnabledFlags.length; i += MSG_PREPARED) {
            this.rendererEnabledFlags[i] = rendererEnabledFlags[i];
        }
        this.state = MSG_PREPARED;
        this.durationUs = -1;
        this.bufferedPositionUs = -1;
        this.standaloneMediaClock = new StandaloneMediaClock();
        this.enabledRenderers = new ArrayList(rendererEnabledFlags.length);
        this.internalPlaybackThread = new PriorityHandlerThread(getClass().getSimpleName() + ":Handler", -16);
        this.internalPlaybackThread.start();
        this.handler = new Handler(this.internalPlaybackThread.getLooper(), this);
    }

    public Looper getPlaybackLooper() {
        return this.internalPlaybackThread.getLooper();
    }

    public long getCurrentPosition() {
        return this.positionUs / 1000;
    }

    public long getBufferedPosition() {
        return this.bufferedPositionUs == -1 ? -1 : this.bufferedPositionUs / 1000;
    }

    public long getDuration() {
        return this.durationUs == -1 ? -1 : this.durationUs / 1000;
    }

    public void prepare(TrackRenderer... renderers) {
        this.handler.obtainMessage(MSG_PREPARED, renderers).sendToTarget();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        int i;
        Handler handler = this.handler;
        if (playWhenReady) {
            i = MSG_PREPARED;
        } else {
            i = 0;
        }
        handler.obtainMessage(MSG_SET_PLAY_WHEN_READY_ACK, i, 0).sendToTarget();
    }

    public void seekTo(long positionMs) {
        this.handler.obtainMessage(MSG_SEEK_TO, Long.valueOf(positionMs)).sendToTarget();
    }

    public void stop() {
        this.handler.sendEmptyMessage(MSG_STOP);
    }

    public void setRendererEnabled(int index, boolean enabled) {
        this.handler.obtainMessage(MSG_SET_RENDERER_ENABLED, index, enabled ? MSG_PREPARED : 0).sendToTarget();
    }

    public void sendMessage(ExoPlayerComponent target, int messageType, Object message) {
        this.customMessagesSent += MSG_PREPARED;
        this.handler.obtainMessage(MSG_CUSTOM, messageType, 0, Pair.create(target, message)).sendToTarget();
    }

    public synchronized void blockingSendMessage(ExoPlayerComponent target, int messageType, Object message) {
        if (this.released) {
            Log.w(TAG, "Sent message(" + messageType + ") after release. Message ignored.");
        } else {
            int messageNumber = this.customMessagesSent;
            this.customMessagesSent = messageNumber + MSG_PREPARED;
            this.handler.obtainMessage(MSG_CUSTOM, messageType, 0, Pair.create(target, message)).sendToTarget();
            while (this.customMessagesProcessed <= messageNumber) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public synchronized void release() {
        if (!this.released) {
            this.handler.sendEmptyMessage(MSG_RELEASE);
            while (!this.released) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            this.internalPlaybackThread.quit();
        }
    }

    public boolean handleMessage(Message msg) {
        boolean z = false;
        try {
            switch (msg.what) {
                case MSG_PREPARED /*1*/:
                    prepareInternal((TrackRenderer[]) msg.obj);
                    return true;
                case MSG_STATE_CHANGED /*2*/:
                    incrementalPrepareInternal();
                    return true;
                case MSG_SET_PLAY_WHEN_READY_ACK /*3*/:
                    if (msg.arg1 != 0) {
                        z = true;
                    }
                    setPlayWhenReadyInternal(z);
                    return true;
                case MSG_STOP /*4*/:
                    stopInternal();
                    return true;
                case MSG_RELEASE /*5*/:
                    releaseInternal();
                    return true;
                case MSG_SEEK_TO /*6*/:
                    seekToInternal(((Long) msg.obj).longValue());
                    return true;
                case MSG_DO_SOME_WORK /*7*/:
                    doSomeWork();
                    return true;
                case MSG_SET_RENDERER_ENABLED /*8*/:
                    int i = msg.arg1;
                    if (msg.arg2 != 0) {
                        z = true;
                    }
                    setRendererEnabledInternal(i, z);
                    return true;
                case MSG_CUSTOM /*9*/:
                    sendMessageInternal(msg.arg1, msg.obj);
                    return true;
                default:
                    return false;
            }
        } catch (ExoPlaybackException e) {
            Log.e(TAG, "Internal track renderer error.", e);
            this.eventHandler.obtainMessage(MSG_STOP, e).sendToTarget();
            stopInternal();
            return true;
        } catch (Throwable e2) {
            Log.e(TAG, "Internal runtime error.", e2);
            this.eventHandler.obtainMessage(MSG_STOP, new ExoPlaybackException(e2, true)).sendToTarget();
            stopInternal();
            return true;
        }
    }

    private void setState(int state) {
        if (this.state != state) {
            this.state = state;
            this.eventHandler.obtainMessage(MSG_STATE_CHANGED, state, 0).sendToTarget();
        }
    }

    private void prepareInternal(TrackRenderer[] renderers) throws ExoPlaybackException {
        resetInternal();
        this.renderers = renderers;
        for (int i = 0; i < renderers.length; i += MSG_PREPARED) {
            MediaClock mediaClock = renderers[i].getMediaClock();
            if (mediaClock != null) {
                Assertions.checkState(this.rendererMediaClock == null);
                this.rendererMediaClock = mediaClock;
                this.rendererMediaClockSource = renderers[i];
            }
        }
        setState(MSG_STATE_CHANGED);
        incrementalPrepareInternal();
    }

    private void incrementalPrepareInternal() throws ExoPlaybackException {
        long operationStartTimeMs = SystemClock.elapsedRealtime();
        boolean prepared = true;
        int i = 0;
        while (i < this.renderers.length) {
            if (this.renderers[i].getState() == 0 && this.renderers[i].prepare(this.positionUs) == 0) {
                prepared = false;
            }
            i += MSG_PREPARED;
        }
        if (prepared) {
            long durationUs = 0;
            boolean allRenderersEnded = true;
            boolean allRenderersReadyOrEnded = true;
            boolean[] rendererHasMediaFlags = new boolean[this.renderers.length];
            for (int rendererIndex = 0; rendererIndex < this.renderers.length; rendererIndex += MSG_PREPARED) {
                TrackRenderer renderer = this.renderers[rendererIndex];
                rendererHasMediaFlags[rendererIndex] = renderer.getState() == MSG_PREPARED;
                if (rendererHasMediaFlags[rendererIndex]) {
                    if (durationUs != -1) {
                        long trackDurationUs = renderer.getDurationUs();
                        if (trackDurationUs == -1) {
                            durationUs = -1;
                        } else if (trackDurationUs != -2) {
                            durationUs = Math.max(durationUs, trackDurationUs);
                        }
                    }
                    if (this.rendererEnabledFlags[rendererIndex]) {
                        renderer.enable(this.positionUs, false);
                        this.enabledRenderers.add(renderer);
                        allRenderersEnded = allRenderersEnded && renderer.isEnded();
                        if (allRenderersReadyOrEnded && rendererReadyOrEnded(renderer)) {
                            allRenderersReadyOrEnded = true;
                        } else {
                            allRenderersReadyOrEnded = false;
                        }
                    }
                }
            }
            this.durationUs = durationUs;
            if (!allRenderersEnded || (durationUs != -1 && durationUs > this.positionUs)) {
                this.state = allRenderersReadyOrEnded ? MSG_STOP : MSG_SET_PLAY_WHEN_READY_ACK;
            } else {
                this.state = MSG_RELEASE;
            }
            this.eventHandler.obtainMessage(MSG_PREPARED, this.state, 0, rendererHasMediaFlags).sendToTarget();
            if (this.playWhenReady && this.state == MSG_STOP) {
                startRenderers();
            }
            this.handler.sendEmptyMessage(MSG_DO_SOME_WORK);
            return;
        }
        scheduleNextOperation(MSG_STATE_CHANGED, operationStartTimeMs, 10);
    }

    private boolean rendererReadyOrEnded(TrackRenderer renderer) {
        boolean z = false;
        if (renderer.isEnded()) {
            return true;
        }
        if (!renderer.isReady()) {
            return false;
        }
        if (this.state == MSG_STOP) {
            return true;
        }
        long rendererDurationUs = renderer.getDurationUs();
        long rendererBufferedPositionUs = renderer.getBufferedPositionUs();
        long minBufferDurationUs = this.rebuffering ? this.minRebufferUs : this.minBufferUs;
        if (minBufferDurationUs <= 0 || rendererBufferedPositionUs == -1 || rendererBufferedPositionUs == -3 || rendererBufferedPositionUs >= this.positionUs + minBufferDurationUs || !(rendererDurationUs == -1 || rendererDurationUs == -2 || rendererBufferedPositionUs < rendererDurationUs)) {
            z = true;
        }
        return z;
    }

    private void setPlayWhenReadyInternal(boolean playWhenReady) throws ExoPlaybackException {
        try {
            this.rebuffering = false;
            this.playWhenReady = playWhenReady;
            if (!playWhenReady) {
                stopRenderers();
                updatePositionUs();
            } else if (this.state == MSG_STOP) {
                startRenderers();
                this.handler.sendEmptyMessage(MSG_DO_SOME_WORK);
            } else if (this.state == MSG_SET_PLAY_WHEN_READY_ACK) {
                this.handler.sendEmptyMessage(MSG_DO_SOME_WORK);
            }
            this.eventHandler.obtainMessage(MSG_SET_PLAY_WHEN_READY_ACK).sendToTarget();
        } catch (Throwable th) {
            this.eventHandler.obtainMessage(MSG_SET_PLAY_WHEN_READY_ACK).sendToTarget();
        }
    }

    private void startRenderers() throws ExoPlaybackException {
        this.rebuffering = false;
        this.standaloneMediaClock.start();
        for (int i = 0; i < this.enabledRenderers.size(); i += MSG_PREPARED) {
            ((TrackRenderer) this.enabledRenderers.get(i)).start();
        }
    }

    private void stopRenderers() throws ExoPlaybackException {
        this.standaloneMediaClock.stop();
        for (int i = 0; i < this.enabledRenderers.size(); i += MSG_PREPARED) {
            ensureStopped((TrackRenderer) this.enabledRenderers.get(i));
        }
    }

    private void updatePositionUs() {
        if (this.rendererMediaClock == null || !this.enabledRenderers.contains(this.rendererMediaClockSource) || this.rendererMediaClockSource.isEnded()) {
            this.positionUs = this.standaloneMediaClock.getPositionUs();
        } else {
            this.positionUs = this.rendererMediaClock.getPositionUs();
            this.standaloneMediaClock.setPositionUs(this.positionUs);
        }
        this.elapsedRealtimeUs = SystemClock.elapsedRealtime() * 1000;
    }

    private void doSomeWork() throws ExoPlaybackException {
        TraceUtil.beginSection("doSomeWork");
        long operationStartTimeMs = SystemClock.elapsedRealtime();
        long bufferedPositionUs = this.durationUs != -1 ? this.durationUs : Long.MAX_VALUE;
        boolean allRenderersEnded = true;
        boolean allRenderersReadyOrEnded = true;
        updatePositionUs();
        for (int i = 0; i < this.enabledRenderers.size(); i += MSG_PREPARED) {
            TrackRenderer renderer = (TrackRenderer) this.enabledRenderers.get(i);
            renderer.doSomeWork(this.positionUs, this.elapsedRealtimeUs);
            allRenderersEnded = allRenderersEnded && renderer.isEnded();
            allRenderersReadyOrEnded = allRenderersReadyOrEnded && rendererReadyOrEnded(renderer);
            if (bufferedPositionUs != -1) {
                long rendererDurationUs = renderer.getDurationUs();
                long rendererBufferedPositionUs = renderer.getBufferedPositionUs();
                if (rendererBufferedPositionUs == -1) {
                    bufferedPositionUs = -1;
                } else if (rendererBufferedPositionUs != -3 && (rendererDurationUs == -1 || rendererDurationUs == -2 || rendererBufferedPositionUs < rendererDurationUs)) {
                    bufferedPositionUs = Math.min(bufferedPositionUs, rendererBufferedPositionUs);
                }
            }
        }
        this.bufferedPositionUs = bufferedPositionUs;
        if (allRenderersEnded && (this.durationUs == -1 || this.durationUs <= this.positionUs)) {
            setState(MSG_RELEASE);
            stopRenderers();
        } else if (this.state == MSG_SET_PLAY_WHEN_READY_ACK && allRenderersReadyOrEnded) {
            setState(MSG_STOP);
            if (this.playWhenReady) {
                startRenderers();
            }
        } else if (this.state == MSG_STOP && !allRenderersReadyOrEnded) {
            this.rebuffering = this.playWhenReady;
            setState(MSG_SET_PLAY_WHEN_READY_ACK);
            stopRenderers();
        }
        this.handler.removeMessages(MSG_DO_SOME_WORK);
        if ((this.playWhenReady && this.state == MSG_STOP) || this.state == MSG_SET_PLAY_WHEN_READY_ACK) {
            scheduleNextOperation(MSG_DO_SOME_WORK, operationStartTimeMs, 10);
        } else if (!this.enabledRenderers.isEmpty()) {
            scheduleNextOperation(MSG_DO_SOME_WORK, operationStartTimeMs, 1000);
        }
        TraceUtil.endSection();
    }

    private void scheduleNextOperation(int operationType, long thisOperationStartTimeMs, long intervalMs) {
        long nextOperationDelayMs = (thisOperationStartTimeMs + intervalMs) - SystemClock.elapsedRealtime();
        if (nextOperationDelayMs <= 0) {
            this.handler.sendEmptyMessage(operationType);
        } else {
            this.handler.sendEmptyMessageDelayed(operationType, nextOperationDelayMs);
        }
    }

    private void seekToInternal(long positionMs) throws ExoPlaybackException {
        this.rebuffering = false;
        this.positionUs = 1000 * positionMs;
        this.standaloneMediaClock.stop();
        this.standaloneMediaClock.setPositionUs(this.positionUs);
        if (this.state != MSG_PREPARED && this.state != MSG_STATE_CHANGED) {
            for (int i = 0; i < this.enabledRenderers.size(); i += MSG_PREPARED) {
                TrackRenderer renderer = (TrackRenderer) this.enabledRenderers.get(i);
                ensureStopped(renderer);
                renderer.seekTo(this.positionUs);
            }
            setState(MSG_SET_PLAY_WHEN_READY_ACK);
            this.handler.sendEmptyMessage(MSG_DO_SOME_WORK);
        }
    }

    private void stopInternal() {
        resetInternal();
        setState(MSG_PREPARED);
    }

    private void releaseInternal() {
        resetInternal();
        setState(MSG_PREPARED);
        synchronized (this) {
            this.released = true;
            notifyAll();
        }
    }

    private void resetInternal() {
        this.handler.removeMessages(MSG_DO_SOME_WORK);
        this.handler.removeMessages(MSG_STATE_CHANGED);
        this.rebuffering = false;
        this.standaloneMediaClock.stop();
        if (this.renderers != null) {
            for (int i = 0; i < this.renderers.length; i += MSG_PREPARED) {
                TrackRenderer renderer = this.renderers[i];
                stopAndDisable(renderer);
                release(renderer);
            }
            this.renderers = null;
            this.rendererMediaClock = null;
            this.rendererMediaClockSource = null;
            this.enabledRenderers.clear();
        }
    }

    private void stopAndDisable(TrackRenderer renderer) {
        try {
            ensureStopped(renderer);
            if (renderer.getState() == MSG_STATE_CHANGED) {
                renderer.disable();
            }
        } catch (ExoPlaybackException e) {
            Log.e(TAG, "Stop failed.", e);
        } catch (RuntimeException e2) {
            Log.e(TAG, "Stop failed.", e2);
        }
    }

    private void release(TrackRenderer renderer) {
        try {
            renderer.release();
        } catch (ExoPlaybackException e) {
            Log.e(TAG, "Release failed.", e);
        } catch (RuntimeException e2) {
            Log.e(TAG, "Release failed.", e2);
        }
    }

    private <T> void sendMessageInternal(int what, Object obj) throws ExoPlaybackException {
        try {
            Pair<ExoPlayerComponent, Object> targetAndMessage = (Pair) obj;
            ((ExoPlayerComponent) targetAndMessage.first).handleMessage(what, targetAndMessage.second);
            synchronized (this) {
                this.customMessagesProcessed += MSG_PREPARED;
                notifyAll();
            }
            if (this.state != MSG_PREPARED && this.state != MSG_STATE_CHANGED) {
                this.handler.sendEmptyMessage(MSG_DO_SOME_WORK);
            }
        } catch (Throwable th) {
            synchronized (this) {
                this.customMessagesProcessed += MSG_PREPARED;
                notifyAll();
            }
        }
    }

    private void setRendererEnabledInternal(int rendererIndex, boolean enabled) throws ExoPlaybackException {
        boolean playing = true;
        if (this.rendererEnabledFlags[rendererIndex] != enabled) {
            this.rendererEnabledFlags[rendererIndex] = enabled;
            if (this.state != MSG_PREPARED && this.state != MSG_STATE_CHANGED) {
                TrackRenderer renderer = this.renderers[rendererIndex];
                int rendererState = renderer.getState();
                if (rendererState != MSG_PREPARED && rendererState != MSG_STATE_CHANGED && rendererState != MSG_SET_PLAY_WHEN_READY_ACK) {
                    return;
                }
                if (enabled) {
                    if (!(this.playWhenReady && this.state == MSG_STOP)) {
                        playing = false;
                    }
                    renderer.enable(this.positionUs, playing);
                    this.enabledRenderers.add(renderer);
                    if (playing) {
                        renderer.start();
                    }
                    this.handler.sendEmptyMessage(MSG_DO_SOME_WORK);
                    return;
                }
                if (renderer == this.rendererMediaClockSource) {
                    this.standaloneMediaClock.setPositionUs(this.rendererMediaClock.getPositionUs());
                }
                ensureStopped(renderer);
                this.enabledRenderers.remove(renderer);
                renderer.disable();
            }
        }
    }

    private void ensureStopped(TrackRenderer renderer) throws ExoPlaybackException {
        if (renderer.getState() == MSG_SET_PLAY_WHEN_READY_ACK) {
            renderer.stop();
        }
    }
}
