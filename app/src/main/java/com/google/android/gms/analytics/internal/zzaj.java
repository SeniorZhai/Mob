package com.google.android.gms.analytics.internal;

import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzlb;

class zzaj {
    private long zzMC;
    private final zzlb zzpw;

    public zzaj(zzlb com_google_android_gms_internal_zzlb) {
        zzu.zzu(com_google_android_gms_internal_zzlb);
        this.zzpw = com_google_android_gms_internal_zzlb;
    }

    public zzaj(zzlb com_google_android_gms_internal_zzlb, long j) {
        zzu.zzu(com_google_android_gms_internal_zzlb);
        this.zzpw = com_google_android_gms_internal_zzlb;
        this.zzMC = j;
    }

    public void clear() {
        this.zzMC = 0;
    }

    public void start() {
        this.zzMC = this.zzpw.elapsedRealtime();
    }

    public boolean zzv(long j) {
        return this.zzMC == 0 || this.zzpw.elapsedRealtime() - this.zzMC > j;
    }
}
