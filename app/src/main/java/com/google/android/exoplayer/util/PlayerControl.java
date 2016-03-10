package com.google.android.exoplayer.util;

import android.widget.MediaController.MediaPlayerControl;
import com.google.android.exoplayer.ExoPlayer;

public class PlayerControl implements MediaPlayerControl {
    private final ExoPlayer exoPlayer;

    public PlayerControl(ExoPlayer exoPlayer) {
        this.exoPlayer = exoPlayer;
    }

    public boolean canPause() {
        return true;
    }

    public boolean canSeekBackward() {
        return true;
    }

    public boolean canSeekForward() {
        return true;
    }

    public int getAudioSessionId() {
        throw new UnsupportedOperationException();
    }

    public int getBufferPercentage() {
        return this.exoPlayer.getBufferedPercentage();
    }

    public int getCurrentPosition() {
        return this.exoPlayer.getDuration() == -1 ? 0 : (int) this.exoPlayer.getCurrentPosition();
    }

    public int getDuration() {
        return this.exoPlayer.getDuration() == -1 ? 0 : (int) this.exoPlayer.getDuration();
    }

    public boolean isPlaying() {
        return this.exoPlayer.getPlayWhenReady();
    }

    public void start() {
        this.exoPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        this.exoPlayer.setPlayWhenReady(false);
    }

    public void seekTo(int timeMillis) {
        this.exoPlayer.seekTo(this.exoPlayer.getDuration() == -1 ? 0 : (long) Math.min(Math.max(0, timeMillis), getDuration()));
    }
}
