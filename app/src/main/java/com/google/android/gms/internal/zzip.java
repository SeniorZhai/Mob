package com.google.android.gms.internal;

import android.text.TextUtils;
import com.google.android.gms.common.internal.zzu;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import java.util.HashMap;
import java.util.Map;

public final class zzip extends zznq<zzip> {
    private String zzEO;
    private String zzJc;
    private String zzJd;
    private String zzJe;
    private boolean zzJf;
    private String zzJg;
    private boolean zzJh;
    private double zzJi;

    public String getClientId() {
        return this.zzJd;
    }

    public String getUserId() {
        return this.zzEO;
    }

    public void setClientId(String clientId) {
        this.zzJd = clientId;
    }

    public void setSampleRate(double percentage) {
        boolean z = percentage >= 0.0d && percentage <= 100.0d;
        zzu.zzb(z, (Object) "Sample rate must be between 0% and 100%");
        this.zzJi = percentage;
    }

    public void setUserId(String userId) {
        this.zzEO = userId;
    }

    public String toString() {
        Map hashMap = new HashMap();
        hashMap.put("hitType", this.zzJc);
        hashMap.put("clientId", this.zzJd);
        hashMap.put(ChatMessage.USER_ID, this.zzEO);
        hashMap.put("androidAdId", this.zzJe);
        hashMap.put("AdTargetingEnabled", Boolean.valueOf(this.zzJf));
        hashMap.put("sessionControl", this.zzJg);
        hashMap.put("nonInteraction", Boolean.valueOf(this.zzJh));
        hashMap.put("sampleRate", Double.valueOf(this.zzJi));
        return zznq.zzy(hashMap);
    }

    public void zzE(boolean z) {
        this.zzJf = z;
    }

    public void zzF(boolean z) {
        this.zzJh = z;
    }

    public void zza(zzip com_google_android_gms_internal_zzip) {
        if (!TextUtils.isEmpty(this.zzJc)) {
            com_google_android_gms_internal_zzip.zzaN(this.zzJc);
        }
        if (!TextUtils.isEmpty(this.zzJd)) {
            com_google_android_gms_internal_zzip.setClientId(this.zzJd);
        }
        if (!TextUtils.isEmpty(this.zzEO)) {
            com_google_android_gms_internal_zzip.setUserId(this.zzEO);
        }
        if (!TextUtils.isEmpty(this.zzJe)) {
            com_google_android_gms_internal_zzip.zzaO(this.zzJe);
        }
        if (this.zzJf) {
            com_google_android_gms_internal_zzip.zzE(true);
        }
        if (!TextUtils.isEmpty(this.zzJg)) {
            com_google_android_gms_internal_zzip.zzaP(this.zzJg);
        }
        if (this.zzJh) {
            com_google_android_gms_internal_zzip.zzF(this.zzJh);
        }
        if (this.zzJi != 0.0d) {
            com_google_android_gms_internal_zzip.setSampleRate(this.zzJi);
        }
    }

    public void zzaN(String str) {
        this.zzJc = str;
    }

    public void zzaO(String str) {
        this.zzJe = str;
    }

    public void zzaP(String str) {
        this.zzJg = str;
    }

    public boolean zzhA() {
        return this.zzJh;
    }

    public double zzhB() {
        return this.zzJi;
    }

    public String zzhw() {
        return this.zzJc;
    }

    public String zzhx() {
        return this.zzJe;
    }

    public boolean zzhy() {
        return this.zzJf;
    }

    public String zzhz() {
        return this.zzJg;
    }
}
