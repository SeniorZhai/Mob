package com.google.android.gms.internal;

import android.text.TextUtils;
import com.facebook.internal.NativeProtocol;
import java.util.HashMap;
import java.util.Map;

public final class zzob extends zznq<zzob> {
    private String mCategory;
    private String zzaEH;
    private long zzaoL;
    private String zzuO;

    public String getAction() {
        return this.zzuO;
    }

    public String getLabel() {
        return this.zzaEH;
    }

    public long getValue() {
        return this.zzaoL;
    }

    public String toString() {
        Map hashMap = new HashMap();
        hashMap.put("category", this.mCategory);
        hashMap.put(NativeProtocol.WEB_DIALOG_ACTION, this.zzuO);
        hashMap.put("label", this.zzaEH);
        hashMap.put("value", Long.valueOf(this.zzaoL));
        return zznq.zzy(hashMap);
    }

    public void zzM(long j) {
        this.zzaoL = j;
    }

    public void zza(zzob com_google_android_gms_internal_zzob) {
        if (!TextUtils.isEmpty(this.mCategory)) {
            com_google_android_gms_internal_zzob.zzdG(this.mCategory);
        }
        if (!TextUtils.isEmpty(this.zzuO)) {
            com_google_android_gms_internal_zzob.zzdH(this.zzuO);
        }
        if (!TextUtils.isEmpty(this.zzaEH)) {
            com_google_android_gms_internal_zzob.zzdI(this.zzaEH);
        }
        if (this.zzaoL != 0) {
            com_google_android_gms_internal_zzob.zzM(this.zzaoL);
        }
    }

    public void zzdG(String str) {
        this.mCategory = str;
    }

    public void zzdH(String str) {
        this.zzuO = str;
    }

    public void zzdI(String str) {
        this.zzaEH = str;
    }

    public String zzwy() {
        return this.mCategory;
    }
}
