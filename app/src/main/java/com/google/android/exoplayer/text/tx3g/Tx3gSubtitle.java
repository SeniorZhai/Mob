package com.google.android.exoplayer.text.tx3g;

import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.Subtitle;
import com.google.android.exoplayer.util.Assertions;
import java.util.Collections;
import java.util.List;

final class Tx3gSubtitle implements Subtitle {
    private final List<Cue> cues;
    private final long startTimeUs;

    public Tx3gSubtitle(long startTimeUs, Cue cue) {
        this.startTimeUs = startTimeUs;
        this.cues = Collections.singletonList(cue);
    }

    public long getStartTime() {
        return this.startTimeUs;
    }

    public int getNextEventTimeIndex(long timeUs) {
        return timeUs < this.startTimeUs ? 0 : -1;
    }

    public int getEventTimeCount() {
        return 1;
    }

    public long getEventTime(int index) {
        Assertions.checkArgument(index == 0);
        return this.startTimeUs;
    }

    public long getLastEventTime() {
        return this.startTimeUs;
    }

    public List<Cue> getCues(long timeUs) {
        return timeUs >= this.startTimeUs ? this.cues : Collections.emptyList();
    }
}
