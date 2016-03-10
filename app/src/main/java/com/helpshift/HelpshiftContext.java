package com.helpshift;

import android.content.Context;

public class HelpshiftContext {
    private static Context context;
    private static String viewState;

    private HelpshiftContext() {
    }

    public static void setApplicationContext(Context c) {
        context = c;
    }

    public static Context getApplicationContext() {
        return context;
    }

    public static void setViewState(String viewState) {
        viewState = viewState;
    }

    public static String getViewState() {
        return viewState;
    }
}
