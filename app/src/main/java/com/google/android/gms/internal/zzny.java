package com.google.android.gms.internal;

import android.text.TextUtils;
import com.facebook.share.internal.ShareConstants;
import com.helpshift.storage.ProfilesDBHelper;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import java.util.HashMap;
import java.util.Map;

public final class zzny extends zznq<zzny> {
    private String mName;
    private String zzKI;
    private String zzaEA;
    private String zzaEB;
    private String zzaEC;
    private String zzaED;
    private String zzaEy;
    private String zzaEz;
    private String zzazL;
    private String zzuU;

    public String getContent() {
        return this.zzuU;
    }

    public String getId() {
        return this.zzKI;
    }

    public String getName() {
        return this.mName;
    }

    public String getSource() {
        return this.zzazL;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String toString() {
        Map hashMap = new HashMap();
        hashMap.put(ProfilesDBHelper.COLUMN_NAME, this.mName);
        hashMap.put(ShareConstants.FEED_SOURCE_PARAM, this.zzazL);
        hashMap.put("medium", this.zzaEy);
        hashMap.put("keyword", this.zzaEz);
        hashMap.put("content", this.zzuU);
        hashMap.put(DBLikedChannelsHelper.KEY_ID, this.zzKI);
        hashMap.put("adNetworkId", this.zzaEA);
        hashMap.put("gclid", this.zzaEB);
        hashMap.put("dclid", this.zzaEC);
        hashMap.put("aclid", this.zzaED);
        return zznq.zzy(hashMap);
    }

    public void zza(zzny com_google_android_gms_internal_zzny) {
        if (!TextUtils.isEmpty(this.mName)) {
            com_google_android_gms_internal_zzny.setName(this.mName);
        }
        if (!TextUtils.isEmpty(this.zzazL)) {
            com_google_android_gms_internal_zzny.zzdx(this.zzazL);
        }
        if (!TextUtils.isEmpty(this.zzaEy)) {
            com_google_android_gms_internal_zzny.zzdy(this.zzaEy);
        }
        if (!TextUtils.isEmpty(this.zzaEz)) {
            com_google_android_gms_internal_zzny.zzdz(this.zzaEz);
        }
        if (!TextUtils.isEmpty(this.zzuU)) {
            com_google_android_gms_internal_zzny.zzdA(this.zzuU);
        }
        if (!TextUtils.isEmpty(this.zzKI)) {
            com_google_android_gms_internal_zzny.zzdB(this.zzKI);
        }
        if (!TextUtils.isEmpty(this.zzaEA)) {
            com_google_android_gms_internal_zzny.zzdC(this.zzaEA);
        }
        if (!TextUtils.isEmpty(this.zzaEB)) {
            com_google_android_gms_internal_zzny.zzdD(this.zzaEB);
        }
        if (!TextUtils.isEmpty(this.zzaEC)) {
            com_google_android_gms_internal_zzny.zzdE(this.zzaEC);
        }
        if (!TextUtils.isEmpty(this.zzaED)) {
            com_google_android_gms_internal_zzny.zzdF(this.zzaED);
        }
    }

    public void zzdA(String str) {
        this.zzuU = str;
    }

    public void zzdB(String str) {
        this.zzKI = str;
    }

    public void zzdC(String str) {
        this.zzaEA = str;
    }

    public void zzdD(String str) {
        this.zzaEB = str;
    }

    public void zzdE(String str) {
        this.zzaEC = str;
    }

    public void zzdF(String str) {
        this.zzaED = str;
    }

    public void zzdx(String str) {
        this.zzazL = str;
    }

    public void zzdy(String str) {
        this.zzaEy = str;
    }

    public void zzdz(String str) {
        this.zzaEz = str;
    }

    public String zzwj() {
        return this.zzaEy;
    }

    public String zzwk() {
        return this.zzaEz;
    }

    public String zzwl() {
        return this.zzaEA;
    }

    public String zzwm() {
        return this.zzaEB;
    }

    public String zzwn() {
        return this.zzaEC;
    }

    public String zzwo() {
        return this.zzaED;
    }
}
