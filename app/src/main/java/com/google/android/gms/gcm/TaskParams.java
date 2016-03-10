package com.google.android.gms.gcm;

public class TaskParams {
    private final String tag;

    public TaskParams(String tag) {
        this.tag = tag;
    }

    public String getTag() {
        return this.tag;
    }
}
