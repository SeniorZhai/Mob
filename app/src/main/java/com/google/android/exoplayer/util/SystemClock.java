package com.google.android.exoplayer.util;

public final class SystemClock implements Clock {
    public long elapsedRealtime() {
        return android.os.SystemClock.elapsedRealtime();
    }
}
