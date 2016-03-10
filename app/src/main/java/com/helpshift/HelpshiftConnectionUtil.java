package com.helpshift;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public final class HelpshiftConnectionUtil {
    protected static boolean isOnline(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
