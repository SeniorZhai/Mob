package com.google.android.gms.internal;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public final class zznx extends zznq<zznx> {
    private String zzLU;
    private String zzLV;
    private String zzaEw;
    private String zzaEx;

    public void setAppId(String value) {
        this.zzaEw = value;
    }

    public void setAppInstallerId(String value) {
        this.zzaEx = value;
    }

    public void setAppName(String value) {
        this.zzLU = value;
    }

    public void setAppVersion(String value) {
        this.zzLV = value;
    }

    public String toString() {
        Map hashMap = new HashMap();
        hashMap.put("appName", this.zzLU);
        hashMap.put("appVersion", this.zzLV);
        hashMap.put("appId", this.zzaEw);
        hashMap.put("appInstallerId", this.zzaEx);
        return zznq.zzy(hashMap);
    }

    public void zza(zznx com_google_android_gms_internal_zznx) {
        if (!TextUtils.isEmpty(this.zzLU)) {
            com_google_android_gms_internal_zznx.setAppName(this.zzLU);
        }
        if (!TextUtils.isEmpty(this.zzLV)) {
            com_google_android_gms_internal_zznx.setAppVersion(this.zzLV);
        }
        if (!TextUtils.isEmpty(this.zzaEw)) {
            com_google_android_gms_internal_zznx.setAppId(this.zzaEw);
        }
        if (!TextUtils.isEmpty(this.zzaEx)) {
            com_google_android_gms_internal_zznx.setAppInstallerId(this.zzaEx);
        }
    }

    public String zzjL() {
        return this.zzLU;
    }

    public String zzjN() {
        return this.zzLV;
    }

    public String zzsK() {
        return this.zzaEw;
    }

    public String zzwi() {
        return this.zzaEx;
    }
}
