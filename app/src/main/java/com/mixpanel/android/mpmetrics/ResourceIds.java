package com.mixpanel.android.mpmetrics;

public interface ResourceIds {
    int idFromName(String str);

    boolean knownIdName(String str);

    String nameForId(int i);
}
