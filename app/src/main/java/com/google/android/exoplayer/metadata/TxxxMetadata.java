package com.google.android.exoplayer.metadata;

public class TxxxMetadata {
    public static final String TYPE = "TXXX";
    public final String description;
    public final String value;

    public TxxxMetadata(String description, String value) {
        this.description = description;
        this.value = value;
    }
}
