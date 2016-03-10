package com.google.android.exoplayer.dash;

import com.google.android.exoplayer.dash.mpd.RangedUri;

public class DashSingleSegmentIndex implements DashSegmentIndex {
    private final long durationUs;
    private final long startTimeUs;
    private final RangedUri uri;

    public DashSingleSegmentIndex(long startTimeUs, long durationUs, RangedUri uri) {
        this.startTimeUs = startTimeUs;
        this.durationUs = durationUs;
        this.uri = uri;
    }

    public int getSegmentNum(long timeUs) {
        return 0;
    }

    public long getTimeUs(int segmentNum) {
        return this.startTimeUs;
    }

    public long getDurationUs(int segmentNum) {
        return this.durationUs;
    }

    public RangedUri getSegmentUrl(int segmentNum) {
        return this.uri;
    }

    public int getFirstSegmentNum() {
        return 0;
    }

    public int getLastSegmentNum() {
        return 0;
    }

    public boolean isExplicit() {
        return true;
    }
}
