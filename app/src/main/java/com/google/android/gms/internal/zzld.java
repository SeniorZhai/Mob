package com.google.android.gms.internal;

import android.os.SystemClock;

public final class zzld implements zzlb {
    private static zzld zzacK;

    public static synchronized zzlb zzoQ() {
        zzlb com_google_android_gms_internal_zzlb;
        synchronized (zzld.class) {
            if (zzacK == null) {
                zzacK = new zzld();
            }
            com_google_android_gms_internal_zzlb = zzacK;
        }
        return com_google_android_gms_internal_zzlb;
    }

    public long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public long elapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }
}
