package com.helpshift.util;

import android.app.Activity;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.helpshift.HSSearch;

public final class HSActivityUtil {
    private static final String TAG = "HelpShiftDebug";

    public static Boolean isFullScreen(Activity a) {
        return Boolean.valueOf((a.getWindow().getAttributes().flags & AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT) == AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
    }

    public static void forceNotFullscreen(Activity a) {
        if (Boolean.valueOf(a.getIntent().getExtras().getBoolean("showInFullScreen")).booleanValue()) {
            a.getWindow().clearFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
            a.getWindow().addFlags(AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT);
        }
    }

    public static void restoreFullscreen(Activity a) {
        if (Boolean.valueOf(a.getIntent().getExtras().getBoolean("showInFullScreen")).booleanValue()) {
            a.getWindow().clearFlags(AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT);
            a.getWindow().addFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
        }
    }

    public static void sessionEnding() {
        HSSearch.deinit();
    }

    public static void sessionBeginning() {
        HSSearch.init();
    }
}
