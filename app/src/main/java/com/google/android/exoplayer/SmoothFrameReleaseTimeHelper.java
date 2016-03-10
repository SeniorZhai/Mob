package com.google.android.exoplayer;

import android.annotation.TargetApi;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer.FrameReleaseTimeHelper;

@TargetApi(16)
public class SmoothFrameReleaseTimeHelper implements FrameReleaseTimeHelper, FrameCallback {
    private static final long CHOREOGRAPHER_SAMPLE_DELAY_MILLIS = 500;
    private static final long MAX_ALLOWED_DRIFT_NS = 20000000;
    private static final int MIN_FRAMES_FOR_ADJUSTMENT = 6;
    private static final long VSYNC_OFFSET_PERCENTAGE = 80;
    private long adjustedLastFrameTimeNs;
    private Choreographer choreographer;
    private int frameCount;
    private boolean haveSync;
    private long lastUnadjustedFrameTimeUs;
    private long pendingAdjustedFrameTimeNs;
    private long sampledVsyncTimeNs;
    private long syncFrameTimeNs;
    private long syncReleaseTimeNs;
    private final boolean usePrimaryDisplayVsync;
    private final long vsyncDurationNs;
    private final long vsyncOffsetNs;

    public SmoothFrameReleaseTimeHelper(float primaryDisplayRefreshRate, boolean usePrimaryDisplayVsync) {
        this.usePrimaryDisplayVsync = usePrimaryDisplayVsync;
        if (usePrimaryDisplayVsync) {
            this.vsyncDurationNs = (long) (1.0E9d / ((double) primaryDisplayRefreshRate));
            this.vsyncOffsetNs = (this.vsyncDurationNs * VSYNC_OFFSET_PERCENTAGE) / 100;
            return;
        }
        this.vsyncDurationNs = -1;
        this.vsyncOffsetNs = -1;
    }

    public void enable() {
        this.haveSync = false;
        if (this.usePrimaryDisplayVsync) {
            this.sampledVsyncTimeNs = 0;
            this.choreographer = Choreographer.getInstance();
            this.choreographer.postFrameCallback(this);
        }
    }

    public void disable() {
        if (this.usePrimaryDisplayVsync) {
            this.choreographer.removeFrameCallback(this);
            this.choreographer = null;
        }
    }

    public void doFrame(long vsyncTimeNs) {
        this.sampledVsyncTimeNs = vsyncTimeNs;
        this.choreographer.postFrameCallbackDelayed(this, CHOREOGRAPHER_SAMPLE_DELAY_MILLIS);
    }

    public long adjustReleaseTime(long unadjustedFrameTimeUs, long unadjustedReleaseTimeNs) {
        long unadjustedFrameTimeNs = unadjustedFrameTimeUs * 1000;
        long adjustedFrameTimeNs = unadjustedFrameTimeNs;
        long adjustedReleaseTimeNs = unadjustedReleaseTimeNs;
        if (this.haveSync) {
            if (unadjustedFrameTimeUs != this.lastUnadjustedFrameTimeUs) {
                this.frameCount++;
                this.adjustedLastFrameTimeNs = this.pendingAdjustedFrameTimeNs;
            }
            if (this.frameCount >= MIN_FRAMES_FOR_ADJUSTMENT) {
                long candidateAdjustedFrameTimeNs = this.adjustedLastFrameTimeNs + ((unadjustedFrameTimeNs - this.syncFrameTimeNs) / ((long) this.frameCount));
                if (isDriftTooLarge(candidateAdjustedFrameTimeNs, unadjustedReleaseTimeNs)) {
                    this.haveSync = false;
                } else {
                    adjustedFrameTimeNs = candidateAdjustedFrameTimeNs;
                    adjustedReleaseTimeNs = (this.syncReleaseTimeNs + adjustedFrameTimeNs) - this.syncFrameTimeNs;
                }
            } else if (isDriftTooLarge(unadjustedFrameTimeNs, unadjustedReleaseTimeNs)) {
                this.haveSync = false;
            }
        }
        if (!this.haveSync) {
            this.syncFrameTimeNs = unadjustedFrameTimeNs;
            this.syncReleaseTimeNs = unadjustedReleaseTimeNs;
            this.frameCount = 0;
            this.haveSync = true;
            onSynced();
        }
        this.lastUnadjustedFrameTimeUs = unadjustedFrameTimeUs;
        this.pendingAdjustedFrameTimeNs = adjustedFrameTimeNs;
        return this.sampledVsyncTimeNs == 0 ? adjustedReleaseTimeNs : closestVsync(adjustedReleaseTimeNs, this.sampledVsyncTimeNs, this.vsyncDurationNs) - this.vsyncOffsetNs;
    }

    protected void onSynced() {
    }

    private boolean isDriftTooLarge(long frameTimeNs, long releaseTimeNs) {
        return Math.abs((releaseTimeNs - this.syncReleaseTimeNs) - (frameTimeNs - this.syncFrameTimeNs)) > MAX_ALLOWED_DRIFT_NS;
    }

    private static long closestVsync(long releaseTime, long sampledVsyncTime, long vsyncDuration) {
        long snappedBeforeNs;
        long snappedAfterNs;
        long snappedTimeNs = sampledVsyncTime + (vsyncDuration * ((releaseTime - sampledVsyncTime) / vsyncDuration));
        if (releaseTime <= snappedTimeNs) {
            snappedBeforeNs = snappedTimeNs - vsyncDuration;
            snappedAfterNs = snappedTimeNs;
        } else {
            snappedBeforeNs = snappedTimeNs;
            snappedAfterNs = snappedTimeNs + vsyncDuration;
        }
        if (snappedAfterNs - releaseTime < releaseTime - snappedBeforeNs) {
            return snappedAfterNs;
        }
        return snappedBeforeNs;
    }
}
