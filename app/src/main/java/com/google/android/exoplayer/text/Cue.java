package com.google.android.exoplayer.text;

import android.text.Layout.Alignment;

public class Cue {
    public static final int UNSET_VALUE = -1;
    public final Alignment alignment;
    public final int line;
    public final int position;
    public final int size;
    public final CharSequence text;

    public Cue() {
        this(null);
    }

    public Cue(CharSequence text) {
        this(text, UNSET_VALUE, UNSET_VALUE, null, UNSET_VALUE);
    }

    public Cue(CharSequence text, int line, int position, Alignment alignment, int size) {
        this.text = text;
        this.line = line;
        this.position = position;
        this.alignment = alignment;
        this.size = size;
    }
}
