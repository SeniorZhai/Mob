package com.google.android.exoplayer;

public final class DecoderInfo {
    public final boolean adaptive;
    public final String name;

    DecoderInfo(String name, boolean adaptive) {
        this.name = name;
        this.adaptive = adaptive;
    }
}
