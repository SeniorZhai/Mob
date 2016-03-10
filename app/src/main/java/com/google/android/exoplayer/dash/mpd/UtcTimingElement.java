package com.google.android.exoplayer.dash.mpd;

public class UtcTimingElement {
    public final String schemeIdUri;
    public final String value;

    public UtcTimingElement(String schemeIdUri, String value) {
        this.schemeIdUri = schemeIdUri;
        this.value = value;
    }

    public String toString() {
        return this.schemeIdUri + ", " + this.value;
    }
}
