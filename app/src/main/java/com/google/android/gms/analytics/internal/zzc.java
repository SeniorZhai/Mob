package com.google.android.gms.analytics.internal;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.internal.ServerProtocol;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzlb;
import com.google.android.gms.internal.zzns;
import io.fabric.sdk.android.BuildConfig;

public class zzc {
    private final zzf zzJy;

    protected zzc(zzf com_google_android_gms_analytics_internal_zzf) {
        zzu.zzu(com_google_android_gms_analytics_internal_zzf);
        this.zzJy = com_google_android_gms_analytics_internal_zzf;
    }

    private void zza(int i, String str, Object obj, Object obj2, Object obj3) {
        zzaf com_google_android_gms_analytics_internal_zzaf = null;
        if (this.zzJy != null) {
            com_google_android_gms_analytics_internal_zzaf = this.zzJy.zzid();
        }
        if (com_google_android_gms_analytics_internal_zzaf != null) {
            com_google_android_gms_analytics_internal_zzaf.zza(i, str, obj, obj2, obj3);
            return;
        }
        String str2 = (String) zzy.zzLb.get();
        if (Log.isLoggable(str2, i)) {
            Log.println(i, str2, zzc(str, obj, obj2, obj3));
        }
    }

    protected static String zzc(String str, Object obj, Object obj2, Object obj3) {
        if (str == null) {
            Object obj4 = BuildConfig.FLAVOR;
        }
        Object zzi = zzi(obj);
        Object zzi2 = zzi(obj2);
        Object zzi3 = zzi(obj3);
        StringBuilder stringBuilder = new StringBuilder();
        String str2 = BuildConfig.FLAVOR;
        if (!TextUtils.isEmpty(obj4)) {
            stringBuilder.append(obj4);
            str2 = ": ";
        }
        if (!TextUtils.isEmpty(zzi)) {
            stringBuilder.append(str2);
            stringBuilder.append(zzi);
            str2 = ", ";
        }
        if (!TextUtils.isEmpty(zzi2)) {
            stringBuilder.append(str2);
            stringBuilder.append(zzi2);
            str2 = ", ";
        }
        if (!TextUtils.isEmpty(zzi3)) {
            stringBuilder.append(str2);
            stringBuilder.append(zzi3);
            str2 = ", ";
        }
        return stringBuilder.toString();
    }

    private static String zzi(Object obj) {
        if (obj == null) {
            return BuildConfig.FLAVOR;
        }
        if (obj instanceof String) {
            return (String) obj;
        }
        if (!(obj instanceof Boolean)) {
            return obj instanceof Throwable ? ((Throwable) obj).toString() : obj.toString();
        } else {
            return obj == Boolean.TRUE ? ServerProtocol.DIALOG_RETURN_SCOPES_TRUE : "false";
        }
    }

    protected Context getContext() {
        return this.zzJy.getContext();
    }

    public void zza(String str, Object obj) {
        zza(2, str, obj, null, null);
    }

    public void zza(String str, Object obj, Object obj2) {
        zza(2, str, obj, obj2, null);
    }

    public void zza(String str, Object obj, Object obj2, Object obj3) {
        zza(3, str, obj, obj2, obj3);
    }

    public void zzaT(String str) {
        zza(2, str, null, null, null);
    }

    public void zzaU(String str) {
        zza(3, str, null, null, null);
    }

    public void zzaV(String str) {
        zza(4, str, null, null, null);
    }

    public void zzaW(String str) {
        zza(5, str, null, null, null);
    }

    public void zzaX(String str) {
        zza(6, str, null, null, null);
    }

    public void zzb(String str, Object obj) {
        zza(3, str, obj, null, null);
    }

    public void zzb(String str, Object obj, Object obj2) {
        zza(3, str, obj, obj2, null);
    }

    public void zzb(String str, Object obj, Object obj2, Object obj3) {
        zza(5, str, obj, obj2, obj3);
    }

    public void zzc(String str, Object obj) {
        zza(4, str, obj, null, null);
    }

    public void zzc(String str, Object obj, Object obj2) {
        zza(5, str, obj, obj2, null);
    }

    public void zzd(String str, Object obj) {
        zza(5, str, obj, null, null);
    }

    public void zzd(String str, Object obj, Object obj2) {
        zza(6, str, obj, obj2, null);
    }

    public void zze(String str, Object obj) {
        zza(6, str, obj, null, null);
    }

    public zzf zzhM() {
        return this.zzJy;
    }

    protected void zzhN() {
        if (zzhR().zziW()) {
            throw new IllegalStateException("Call only supported on the client side");
        }
    }

    protected void zzhO() {
        this.zzJy.zzhO();
    }

    protected zzlb zzhP() {
        return this.zzJy.zzhP();
    }

    protected zzaf zzhQ() {
        return this.zzJy.zzhQ();
    }

    protected zzr zzhR() {
        return this.zzJy.zzhR();
    }

    protected zzns zzhS() {
        return this.zzJy.zzhS();
    }

    protected zzv zzhT() {
        return this.zzJy.zzhT();
    }

    protected zzai zzhU() {
        return this.zzJy.zzhU();
    }

    protected zzn zzhV() {
        return this.zzJy.zzih();
    }

    protected zza zzhW() {
        return this.zzJy.zzig();
    }

    protected zzk zzhX() {
        return this.zzJy.zzhX();
    }

    protected zzu zzhY() {
        return this.zzJy.zzhY();
    }

    public boolean zzhZ() {
        return Log.isLoggable((String) zzy.zzLb.get(), 2);
    }

    public GoogleAnalytics zzhg() {
        return this.zzJy.zzie();
    }

    protected zzb zzhl() {
        return this.zzJy.zzhl();
    }

    protected zzan zzhm() {
        return this.zzJy.zzhm();
    }
}
