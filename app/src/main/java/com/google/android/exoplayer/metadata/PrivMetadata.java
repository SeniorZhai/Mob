package com.google.android.exoplayer.metadata;

public class PrivMetadata {
    public static final String TYPE = "PRIV";
    public final String owner;
    public final byte[] privateData;

    public PrivMetadata(String owner, byte[] privateData) {
        this.owner = owner;
        this.privateData = privateData;
    }
}
