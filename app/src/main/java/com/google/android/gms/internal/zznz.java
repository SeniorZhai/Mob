package com.google.android.gms.internal;

import android.text.TextUtils;
import java.util.HashMap;
import java.util.Map;

public final class zznz extends zznq<zznz> {
    private String zzRA;
    public int zzaEE;
    public int zzaEF;
    public int zzaEG;
    public int zzyW;
    public int zzyX;

    public String getLanguage() {
        return this.zzRA;
    }

    public void setLanguage(String language) {
        this.zzRA = language;
    }

    public String toString() {
        Map hashMap = new HashMap();
        hashMap.put("language", this.zzRA);
        hashMap.put("screenColors", Integer.valueOf(this.zzaEE));
        hashMap.put("screenWidth", Integer.valueOf(this.zzyW));
        hashMap.put("screenHeight", Integer.valueOf(this.zzyX));
        hashMap.put("viewportWidth", Integer.valueOf(this.zzaEF));
        hashMap.put("viewportHeight", Integer.valueOf(this.zzaEG));
        return zznq.zzy(hashMap);
    }

    public void zza(zznz com_google_android_gms_internal_zznz) {
        if (this.zzaEE != 0) {
            com_google_android_gms_internal_zznz.zzhF(this.zzaEE);
        }
        if (this.zzyW != 0) {
            com_google_android_gms_internal_zznz.zzhG(this.zzyW);
        }
        if (this.zzyX != 0) {
            com_google_android_gms_internal_zznz.zzhH(this.zzyX);
        }
        if (this.zzaEF != 0) {
            com_google_android_gms_internal_zznz.zzhI(this.zzaEF);
        }
        if (this.zzaEG != 0) {
            com_google_android_gms_internal_zznz.zzhJ(this.zzaEG);
        }
        if (!TextUtils.isEmpty(this.zzRA)) {
            com_google_android_gms_internal_zznz.setLanguage(this.zzRA);
        }
    }

    public void zzhF(int i) {
        this.zzaEE = i;
    }

    public void zzhG(int i) {
        this.zzyW = i;
    }

    public void zzhH(int i) {
        this.zzyX = i;
    }

    public void zzhI(int i) {
        this.zzaEF = i;
    }

    public void zzhJ(int i) {
        this.zzaEG = i;
    }

    public int zzwp() {
        return this.zzaEE;
    }

    public int zzwq() {
        return this.zzyW;
    }

    public int zzwr() {
        return this.zzyX;
    }

    public int zzws() {
        return this.zzaEF;
    }

    public int zzwt() {
        return this.zzaEG;
    }
}
