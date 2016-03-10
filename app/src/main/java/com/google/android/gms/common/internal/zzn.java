package com.google.android.gms.common.internal;

import android.content.Context;
import android.util.Log;
import com.google.android.gms.internal.zzoy;

public final class zzn {
    public static final int zzaaX = (23 - " PII_LOG".length());
    private static final String zzaaY = null;
    private final String zzaaZ;
    private final String zzaba;

    public zzn(String str) {
        this(str, zzaaY);
    }

    public zzn(String str, String str2) {
        zzu.zzb((Object) str, (Object) "log tag cannot be null");
        zzu.zzb(str.length() <= 23, "tag \"%s\" is longer than the %d character maximum", str, Integer.valueOf(23));
        this.zzaaZ = str;
        if (str2 == null || str2.length() <= 0) {
            this.zzaba = zzaaY;
        } else {
            this.zzaba = str2;
        }
    }

    private String zzch(String str) {
        return this.zzaba == null ? str : this.zzaba.concat(str);
    }

    public void zza(Context context, String str, String str2, Throwable th) {
        StackTraceElement[] stackTrace = th.getStackTrace();
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        while (i < stackTrace.length && i < 2) {
            stringBuilder.append(stackTrace[i].toString());
            stringBuilder.append("\n");
            i++;
        }
        zzoy com_google_android_gms_internal_zzoy = new zzoy(context, 10);
        com_google_android_gms_internal_zzoy.zza("GMS_WTF", null, "GMS_WTF", stringBuilder.toString());
        com_google_android_gms_internal_zzoy.send();
        if (zzbv(7)) {
            Log.e(str, zzch(str2), th);
            Log.wtf(str, zzch(str2), th);
        }
    }

    public void zza(String str, String str2, Throwable th) {
        if (zzbv(4)) {
            Log.i(str, zzch(str2), th);
        }
    }

    public void zzb(String str, String str2, Throwable th) {
        if (zzbv(5)) {
            Log.w(str, zzch(str2), th);
        }
    }

    public boolean zzbv(int i) {
        return Log.isLoggable(this.zzaaZ, i);
    }

    public void zzc(String str, String str2, Throwable th) {
        if (zzbv(6)) {
            Log.e(str, zzch(str2), th);
        }
    }

    public void zzt(String str, String str2) {
        if (zzbv(3)) {
            Log.d(str, zzch(str2));
        }
    }

    public void zzu(String str, String str2) {
        if (zzbv(5)) {
            Log.w(str, zzch(str2));
        }
    }

    public void zzv(String str, String str2) {
        if (zzbv(6)) {
            Log.e(str, zzch(str2));
        }
    }
}
