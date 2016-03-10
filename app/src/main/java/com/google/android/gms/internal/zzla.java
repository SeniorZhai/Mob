package com.google.android.gms.internal;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Process;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.gms.common.internal.zzd;

public class zzla {
    public static boolean zzi(Context context, String str) {
        try {
            return (context.getPackageManager().getApplicationInfo(str, 0).flags & AccessibilityNodeInfoCompat.ACTION_SET_TEXT) != 0;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static boolean zziW() {
        return zzd.zzZR && zzkf.isInitialized() && zzkf.zzmY() == Process.myUid();
    }
}
