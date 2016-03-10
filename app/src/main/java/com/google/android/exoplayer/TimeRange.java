package com.google.android.exoplayer;

public final class TimeRange {
    public static final int TYPE_SNAPSHOT = 0;
    private final long endTimeUs;
    private final long startTimeUs;
    public final int type;

    public TimeRange(int type, long startTimeUs, long endTimeUs) {
        this.type = type;
        this.startTimeUs = startTimeUs;
        this.endTimeUs = endTimeUs;
    }

    public long[] getCurrentBoundsMs(long[] out) {
        out = getCurrentBoundsUs(out);
        out[0] = out[0] / 1000;
        out[1] = out[1] / 1000;
        return out;
    }

    public long[] getCurrentBoundsUs(long[] out) {
        if (out == null || out.length < 2) {
            out = new long[2];
        }
        out[0] = this.startTimeUs;
        out[1] = this.endTimeUs;
        return out;
    }

    public int hashCode() {
        return (int) (((long) (0 | (this.type << 30))) | (((this.startTimeUs + this.endTimeUs) / 1000) & 1073741823));
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof TimeRange)) {
            return false;
        }
        TimeRange otherTimeRange = (TimeRange) other;
        if (otherTimeRange.type == this.type && otherTimeRange.startTimeUs == this.startTimeUs && otherTimeRange.endTimeUs == this.endTimeUs) {
            return true;
        }
        return false;
    }
}
