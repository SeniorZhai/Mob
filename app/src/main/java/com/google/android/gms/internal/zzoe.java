package com.google.android.gms.internal;

import android.text.TextUtils;
import com.facebook.internal.NativeProtocol;
import java.util.HashMap;
import java.util.Map;

public final class zzoe extends zznq<zzoe> {
    public String zzaER;
    public String zzaES;
    public String zzuO;

    public String getAction() {
        return this.zzuO;
    }

    public String getTarget() {
        return this.zzaES;
    }

    public String toString() {
        Map hashMap = new HashMap();
        hashMap.put("network", this.zzaER);
        hashMap.put(NativeProtocol.WEB_DIALOG_ACTION, this.zzuO);
        hashMap.put("target", this.zzaES);
        return zznq.zzy(hashMap);
    }

    public void zza(zzoe com_google_android_gms_internal_zzoe) {
        if (!TextUtils.isEmpty(this.zzaER)) {
            com_google_android_gms_internal_zzoe.zzdL(this.zzaER);
        }
        if (!TextUtils.isEmpty(this.zzuO)) {
            com_google_android_gms_internal_zzoe.zzdH(this.zzuO);
        }
        if (!TextUtils.isEmpty(this.zzaES)) {
            com_google_android_gms_internal_zzoe.zzdM(this.zzaES);
        }
    }

    public void zzdH(String str) {
        this.zzuO = str;
    }

    public void zzdL(String str) {
        this.zzaER = str;
    }

    public void zzdM(String str) {
        this.zzaES = str;
    }

    public String zzwI() {
        return this.zzaER;
    }
}
