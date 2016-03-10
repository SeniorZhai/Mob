package com.google.android.exoplayer.text.ttml;

import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.Subtitle;
import com.google.android.exoplayer.util.Util;
import java.util.Collections;
import java.util.List;

public final class TtmlSubtitle implements Subtitle {
    private final long[] eventTimesUs;
    private final TtmlNode root;
    private final long startTimeUs;

    public TtmlSubtitle(TtmlNode root, long startTimeUs) {
        this.root = root;
        this.startTimeUs = startTimeUs;
        this.eventTimesUs = root.getEventTimesUs();
    }

    public long getStartTime() {
        return this.startTimeUs;
    }

    public int getNextEventTimeIndex(long timeUs) {
        int index = Util.binarySearchCeil(this.eventTimesUs, timeUs - this.startTimeUs, false, false);
        return index < this.eventTimesUs.length ? index : -1;
    }

    public int getEventTimeCount() {
        return this.eventTimesUs.length;
    }

    public long getEventTime(int index) {
        return this.eventTimesUs[index] + this.startTimeUs;
    }

    public long getLastEventTime() {
        return (this.eventTimesUs.length == 0 ? -1 : this.eventTimesUs[this.eventTimesUs.length - 1]) + this.startTimeUs;
    }

    public List<Cue> getCues(long timeUs) {
        CharSequence cueText = this.root.getText(timeUs - this.startTimeUs);
        if (cueText == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new Cue(cueText));
    }
}
