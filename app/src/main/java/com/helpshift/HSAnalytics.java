package com.helpshift;

import android.app.Activity;
import com.helpshift.Helpshift.HelpshiftDelegate;

final class HSAnalytics {
    private static HSApiData data = null;
    protected static boolean decomp;
    private static boolean isForeground;
    private static int started;
    private static int stopped;

    HSAnalytics() {
    }

    protected static boolean appIsInForeground() {
        return isForeground;
    }

    public static void onActivityStarted(Activity activity) {
        if (data == null) {
            data = new HSApiData(activity);
        }
        started++;
        if (!isForeground) {
            if (decomp) {
                HSFunnel.pushEvent(HSFunnel.LIBRARY_OPENED_DECOMP);
            } else {
                HSFunnel.pushEvent(HSFunnel.LIBRARY_OPENED);
            }
            HelpshiftDelegate delegate = Helpshift.getDelegate();
            if (delegate != null) {
                delegate.helpshiftSessionBegan();
            }
        }
        isForeground = true;
    }

    public static void onActivityStopped(Activity activity) {
        stopped++;
        if (started == stopped) {
            isForeground = false;
            HSFunnel.pushEvent(HSFunnel.LIBRARY_QUIT);
            data.reportActionEvents();
            HelpshiftDelegate delegate = Helpshift.getDelegate();
            if (delegate != null) {
                delegate.helpshiftSessionEnded();
            }
        }
    }
}
