package com.google.android.gms.analytics;

import com.google.android.gms.analytics.internal.zzae;
import io.fabric.sdk.android.BuildConfig;

public final class zzc {
    public static String zzN(int i) {
        return zzc("&cd", i);
    }

    public static String zzO(int i) {
        return zzc("cd", i);
    }

    public static String zzP(int i) {
        return zzc("&cm", i);
    }

    public static String zzQ(int i) {
        return zzc("cm", i);
    }

    public static String zzR(int i) {
        return zzc("&pr", i);
    }

    public static String zzS(int i) {
        return zzc("pr", i);
    }

    public static String zzT(int i) {
        return zzc("&promo", i);
    }

    public static String zzU(int i) {
        return zzc(NotificationCompatApi21.CATEGORY_PROMO, i);
    }

    public static String zzV(int i) {
        return zzc("pi", i);
    }

    public static String zzW(int i) {
        return zzc("&il", i);
    }

    public static String zzX(int i) {
        return zzc("il", i);
    }

    public static String zzY(int i) {
        return zzc("cd", i);
    }

    public static String zzZ(int i) {
        return zzc("cm", i);
    }

    private static String zzc(String str, int i) {
        if (i >= 1) {
            return str + i;
        }
        zzae.zzf("index out of range for prefix", str);
        return BuildConfig.FLAVOR;
    }
}
