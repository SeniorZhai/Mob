package com.google.android.gms.analytics.internal;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class zzan extends zzd {
    protected boolean zzIx;
    protected int zzKR;
    protected String zzLU;
    protected String zzLV;
    protected int zzLX;
    protected boolean zzML;
    protected boolean zzMM;
    protected boolean zzMN;

    public zzan(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf);
    }

    private static int zzbo(String str) {
        String toLowerCase = str.toLowerCase();
        return "verbose".equals(toLowerCase) ? 0 : "info".equals(toLowerCase) ? 1 : "warning".equals(toLowerCase) ? 2 : Extra.ERROR.equals(toLowerCase) ? 3 : -1;
    }

    public int getLogLevel() {
        zzia();
        return this.zzKR;
    }

    void zza(zzaa com_google_android_gms_analytics_internal_zzaa) {
        int zzbo;
        zzaT("Loading global XML config values");
        if (com_google_android_gms_analytics_internal_zzaa.zzjK()) {
            String zzjL = com_google_android_gms_analytics_internal_zzaa.zzjL();
            this.zzLU = zzjL;
            zzb("XML config - app name", zzjL);
        }
        if (com_google_android_gms_analytics_internal_zzaa.zzjM()) {
            zzjL = com_google_android_gms_analytics_internal_zzaa.zzjN();
            this.zzLV = zzjL;
            zzb("XML config - app version", zzjL);
        }
        if (com_google_android_gms_analytics_internal_zzaa.zzjO()) {
            zzbo = zzbo(com_google_android_gms_analytics_internal_zzaa.zzjP());
            if (zzbo >= 0) {
                this.zzKR = zzbo;
                zza("XML config - log level", Integer.valueOf(zzbo));
            }
        }
        if (com_google_android_gms_analytics_internal_zzaa.zzjQ()) {
            zzbo = com_google_android_gms_analytics_internal_zzaa.zzjR();
            this.zzLX = zzbo;
            this.zzMM = true;
            zzb("XML config - dispatch period (sec)", Integer.valueOf(zzbo));
        }
        if (com_google_android_gms_analytics_internal_zzaa.zzjS()) {
            boolean zzjT = com_google_android_gms_analytics_internal_zzaa.zzjT();
            this.zzIx = zzjT;
            this.zzMN = true;
            zzb("XML config - dry run", Boolean.valueOf(zzjT));
        }
    }

    protected void zzhn() {
        zzkI();
    }

    public String zzjL() {
        zzia();
        return this.zzLU;
    }

    public String zzjN() {
        zzia();
        return this.zzLV;
    }

    public boolean zzjO() {
        zzia();
        return this.zzML;
    }

    public boolean zzjQ() {
        zzia();
        return this.zzMM;
    }

    public boolean zzjS() {
        zzia();
        return this.zzMN;
    }

    public boolean zzjT() {
        zzia();
        return this.zzIx;
    }

    public int zzkH() {
        zzia();
        return this.zzLX;
    }

    protected void zzkI() {
        ApplicationInfo applicationInfo;
        Context context = getContext();
        try {
            applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 129);
        } catch (NameNotFoundException e) {
            zzd("PackageManager doesn't know about the app package", e);
            applicationInfo = null;
        }
        if (applicationInfo == null) {
            zzaW("Couldn't get ApplicationInfo to load global config");
            return;
        }
        Bundle bundle = applicationInfo.metaData;
        if (bundle != null) {
            int i = bundle.getInt("com.google.android.gms.analytics.globalConfigResource");
            if (i > 0) {
                zzaa com_google_android_gms_analytics_internal_zzaa = (zzaa) new zzz(zzhM()).zzab(i);
                if (com_google_android_gms_analytics_internal_zzaa != null) {
                    zza(com_google_android_gms_analytics_internal_zzaa);
                }
            }
        }
    }
}
