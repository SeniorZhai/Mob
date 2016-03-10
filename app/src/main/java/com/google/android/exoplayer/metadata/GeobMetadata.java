package com.google.android.exoplayer.metadata;

public class GeobMetadata {
    public static final String TYPE = "GEOB";
    public final byte[] data;
    public final String description;
    public final String filename;
    public final String mimeType;

    public GeobMetadata(String mimeType, String filename, String description, byte[] data) {
        this.mimeType = mimeType;
        this.filename = filename;
        this.description = description;
        this.data = data;
    }
}
