package com.google.android.exoplayer;

public final class TrackInfo {
    public final long durationUs;
    public final String mimeType;

    public TrackInfo(String mimeType, long durationUs) {
        this.mimeType = mimeType;
        this.durationUs = durationUs;
    }
}
