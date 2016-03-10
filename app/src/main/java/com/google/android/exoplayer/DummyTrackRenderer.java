package com.google.android.exoplayer;

public class DummyTrackRenderer extends TrackRenderer {
    protected int doPrepare(long positionUs) throws ExoPlaybackException {
        return -1;
    }

    protected boolean isEnded() {
        throw new IllegalStateException();
    }

    protected boolean isReady() {
        throw new IllegalStateException();
    }

    protected void seekTo(long positionUs) {
        throw new IllegalStateException();
    }

    protected void doSomeWork(long positionUs, long elapsedRealtimeUs) {
        throw new IllegalStateException();
    }

    protected long getDurationUs() {
        throw new IllegalStateException();
    }

    protected long getBufferedPositionUs() {
        throw new IllegalStateException();
    }
}
