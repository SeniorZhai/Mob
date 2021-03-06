package com.google.android.exoplayer.text;

import java.util.List;

public interface Subtitle {
    List<Cue> getCues(long j);

    long getEventTime(int i);

    int getEventTimeCount();

    long getLastEventTime();

    int getNextEventTimeIndex(long j);

    long getStartTime();
}
