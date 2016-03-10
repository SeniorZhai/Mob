package com.google.android.exoplayer.text.webvtt;

import android.text.Layout.Alignment;
import com.google.android.exoplayer.text.Cue;

final class WebvttCue extends Cue {
    public final long endTime;
    public final long startTime;

    public WebvttCue(CharSequence text) {
        this(-1, -1, text);
    }

    public WebvttCue(long startTime, long endTime, CharSequence text) {
        this(startTime, endTime, text, -1, -1, null, -1);
    }

    public WebvttCue(long startTime, long endTime, CharSequence text, int line, int position, Alignment alignment, int size) {
        super(text, line, position, alignment, size);
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isNormalCue() {
        return this.line == -1 && this.position == -1;
    }
}
