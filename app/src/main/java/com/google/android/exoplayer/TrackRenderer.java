package com.google.android.exoplayer;

import com.google.android.exoplayer.ExoPlayer.ExoPlayerComponent;
import com.google.android.exoplayer.util.Assertions;

public abstract class TrackRenderer implements ExoPlayerComponent {
    public static final long END_OF_TRACK_US = -3;
    public static final long MATCH_LONGEST_US = -2;
    protected static final int STATE_ENABLED = 2;
    protected static final int STATE_IGNORE = -1;
    protected static final int STATE_PREPARED = 1;
    protected static final int STATE_RELEASED = -2;
    protected static final int STATE_STARTED = 3;
    protected static final int STATE_UNPREPARED = 0;
    public static final long UNKNOWN_TIME_US = -1;
    private int state;

    protected abstract int doPrepare(long j) throws ExoPlaybackException;

    protected abstract void doSomeWork(long j, long j2) throws ExoPlaybackException;

    protected abstract long getBufferedPositionUs();

    protected abstract long getDurationUs();

    protected abstract boolean isEnded();

    protected abstract boolean isReady();

    protected abstract void seekTo(long j) throws ExoPlaybackException;

    protected MediaClock getMediaClock() {
        return null;
    }

    protected final int getState() {
        return this.state;
    }

    final int prepare(long positionUs) throws ExoPlaybackException {
        boolean z = false;
        Assertions.checkState(this.state == 0);
        this.state = doPrepare(positionUs);
        if (this.state == 0 || this.state == STATE_PREPARED || this.state == STATE_IGNORE) {
            z = true;
        }
        Assertions.checkState(z);
        return this.state;
    }

    final void enable(long positionUs, boolean joining) throws ExoPlaybackException {
        boolean z = true;
        if (this.state != STATE_PREPARED) {
            z = false;
        }
        Assertions.checkState(z);
        this.state = STATE_ENABLED;
        onEnabled(positionUs, joining);
    }

    protected void onEnabled(long positionUs, boolean joining) throws ExoPlaybackException {
    }

    final void start() throws ExoPlaybackException {
        Assertions.checkState(this.state == STATE_ENABLED);
        this.state = STATE_STARTED;
        onStarted();
    }

    protected void onStarted() throws ExoPlaybackException {
    }

    final void stop() throws ExoPlaybackException {
        Assertions.checkState(this.state == STATE_STARTED);
        this.state = STATE_ENABLED;
        onStopped();
    }

    protected void onStopped() throws ExoPlaybackException {
    }

    final void disable() throws ExoPlaybackException {
        Assertions.checkState(this.state == STATE_ENABLED);
        this.state = STATE_PREPARED;
        onDisabled();
    }

    protected void onDisabled() throws ExoPlaybackException {
    }

    final void release() throws ExoPlaybackException {
        boolean z = (this.state == STATE_ENABLED || this.state == STATE_STARTED || this.state == STATE_RELEASED) ? false : true;
        Assertions.checkState(z);
        this.state = STATE_RELEASED;
        onReleased();
    }

    protected void onReleased() throws ExoPlaybackException {
    }

    public void handleMessage(int what, Object object) throws ExoPlaybackException {
    }
}
