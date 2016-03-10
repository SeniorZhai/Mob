package com.google.android.gms.internal;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public final class zzof extends zznq<zzof> {
    public String mCategory;
    public String zzaEH;
    public String zzaET;
    public long zzaEU;

    public String getLabel() {
        return this.zzaEH;
    }

    public long getTimeInMillis() {
        return this.zzaEU;
    }

    public void setTimeInMillis(long milliseconds) {
        this.zzaEU = milliseconds;
    }

    public String toString() {
        Map hashMap = new HashMap();
        hashMap.put("variableName", this.zzaET);
        hashMap.put("timeInMillis", Long.valueOf(this.zzaEU));
        hashMap.put("category", this.mCategory);
        hashMap.put("label", this.zzaEH);
        return zznq.zzy(hashMap);
    }

    public void zza(zzof com_google_android_gms_internal_zzof) {
        if (!TextUtils.isEmpty(this.zzaET)) {
            com_google_android_gms_internal_zzof.zzdN(this.zzaET);
        }
        if (this.zzaEU != 0) {
            com_google_android_gms_internal_zzof.setTimeInMillis(this.zzaEU);
        }
        if (!TextUtils.isEmpty(this.mCategory)) {
            com_google_android_gms_internal_zzof.zzdG(this.mCategory);
        }
        if (!TextUtils.isEmpty(this.zzaEH)) {
            com_google_android_gms_internal_zzof.zzdI(this.zzaEH);
        }
    }

    public void zzdG(String str) {
        this.mCategory = str;
    }

    public void zzdI(String str) {
        this.zzaEH = str;
    }

    public void zzdN(String str) {
        this.zzaET = str;
    }

    public String zzwJ() {
        return this.zzaET;
    }

    public String zzwy() {
        return this.mCategory;
    }
}
