package com.google.android.gms.analytics.internal;

import com.google.android.gms.internal.zznx;

public class zzk extends zzd {
    private final zznx zzKm = new zznx();

    zzk(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf);
    }

    public void zzhi() {
        zzan zzhm = zzhm();
        String zzjL = zzhm.zzjL();
        if (zzjL != null) {
            this.zzKm.setAppName(zzjL);
        }
        String zzjN = zzhm.zzjN();
        if (zzjN != null) {
            this.zzKm.setAppVersion(zzjN);
        }
    }

    protected void zzhn() {
        zzhS().zzwc().zza(this.zzKm);
        zzhi();
    }

    public zznx zzix() {
        zzia();
        return this.zzKm;
    }
}
