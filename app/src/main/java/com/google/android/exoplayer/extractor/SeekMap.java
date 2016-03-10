package com.google.android.exoplayer.extractor;

public interface SeekMap {
    long getPosition(long j);

    boolean isSeekable();
}
