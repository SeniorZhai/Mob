package com.facebook.internal;

public class InternalSettings {
    private static volatile String mCustomUserAgent;

    public static void setCustomUserAgent(String customUserAgent) {
        mCustomUserAgent = customUserAgent;
    }

    public static String getCustomUserAgent() {
        return mCustomUserAgent;
    }
}
