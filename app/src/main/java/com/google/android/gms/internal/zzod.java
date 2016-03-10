package com.google.android.gms.internal;

import android.text.TextUtils;
import android.util.Log;
import com.facebook.internal.AnalyticsEvents;
import com.google.android.gms.common.internal.zzu;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class zzod extends zznq<zzod> {
    private String zzaEJ;
    private int zzaEK;
    private int zzaEL;
    private String zzaEM;
    private String zzaEN;
    private boolean zzaEO;
    private boolean zzaEP;
    private boolean zzaEQ;

    public zzod() {
        this(false);
    }

    public zzod(boolean z) {
        this(z, zzwA());
    }

    public zzod(boolean z, int i) {
        zzu.zzbw(i);
        this.zzaEK = i;
        this.zzaEP = z;
    }

    static int zzwA() {
        UUID randomUUID = UUID.randomUUID();
        int leastSignificantBits = (int) (randomUUID.getLeastSignificantBits() & 2147483647L);
        if (leastSignificantBits != 0) {
            return leastSignificantBits;
        }
        leastSignificantBits = (int) (randomUUID.getMostSignificantBits() & 2147483647L);
        if (leastSignificantBits != 0) {
            return leastSignificantBits;
        }
        Log.e("GAv4", "UUID.randomUUID() returned 0.");
        return ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
    }

    private void zzwH() {
        if (this.zzaEQ) {
            throw new IllegalStateException("ScreenViewInfo is immutable");
        }
    }

    public boolean isMutable() {
        return !this.zzaEQ;
    }

    public void setScreenName(String screenName) {
        zzwH();
        this.zzaEJ = screenName;
    }

    public String toString() {
        Map hashMap = new HashMap();
        hashMap.put("screenName", this.zzaEJ);
        hashMap.put("interstitial", Boolean.valueOf(this.zzaEO));
        hashMap.put(AnalyticsEvents.PARAMETER_SHARE_DIALOG_SHOW_AUTOMATIC, Boolean.valueOf(this.zzaEP));
        hashMap.put("screenId", Integer.valueOf(this.zzaEK));
        hashMap.put("referrerScreenId", Integer.valueOf(this.zzaEL));
        hashMap.put("referrerScreenName", this.zzaEM);
        hashMap.put("referrerUri", this.zzaEN);
        return zznq.zzy(hashMap);
    }

    public /* synthetic */ void zza(zznq com_google_android_gms_internal_zznq) {
        zzc((zzod) com_google_android_gms_internal_zznq);
    }

    public void zzah(boolean z) {
        zzwH();
        this.zzaEP = z;
    }

    public void zzai(boolean z) {
        zzwH();
        this.zzaEO = z;
    }

    public int zzbn() {
        return this.zzaEK;
    }

    public void zzc(zzod com_google_android_gms_internal_zzod) {
        if (!TextUtils.isEmpty(this.zzaEJ)) {
            com_google_android_gms_internal_zzod.setScreenName(this.zzaEJ);
        }
        if (this.zzaEK != 0) {
            com_google_android_gms_internal_zzod.zzhK(this.zzaEK);
        }
        if (this.zzaEL != 0) {
            com_google_android_gms_internal_zzod.zzhL(this.zzaEL);
        }
        if (!TextUtils.isEmpty(this.zzaEM)) {
            com_google_android_gms_internal_zzod.zzdJ(this.zzaEM);
        }
        if (!TextUtils.isEmpty(this.zzaEN)) {
            com_google_android_gms_internal_zzod.zzdK(this.zzaEN);
        }
        if (this.zzaEO) {
            com_google_android_gms_internal_zzod.zzai(this.zzaEO);
        }
        if (this.zzaEP) {
            com_google_android_gms_internal_zzod.zzah(this.zzaEP);
        }
    }

    public void zzdJ(String str) {
        zzwH();
        this.zzaEM = str;
    }

    public void zzdK(String str) {
        zzwH();
        if (TextUtils.isEmpty(str)) {
            this.zzaEN = null;
        } else {
            this.zzaEN = str;
        }
    }

    public void zzhK(int i) {
        zzwH();
        this.zzaEK = i;
    }

    public void zzhL(int i) {
        zzwH();
        this.zzaEL = i;
    }

    public String zzwB() {
        return this.zzaEJ;
    }

    public int zzwC() {
        return this.zzaEL;
    }

    public String zzwD() {
        return this.zzaEM;
    }

    public String zzwE() {
        return this.zzaEN;
    }

    public void zzwF() {
        this.zzaEQ = true;
    }

    public boolean zzwG() {
        return this.zzaEO;
    }
}
