package com.google.android.gms.internal;

import android.content.Context;
import com.mobcrush.mobcrush.Constants;
import java.util.regex.Pattern;

public final class zzkz {
    private static Pattern zzacJ = null;

    public static boolean zzai(Context context) {
        return context.getPackageManager().hasSystemFeature("android.hardware.type.watch");
    }

    public static int zzbN(int i) {
        return i / Constants.UPDATE_COFIG_INTERVAL;
    }

    public static int zzbO(int i) {
        return (i % Constants.UPDATE_COFIG_INTERVAL) / 100;
    }

    public static boolean zzbP(int i) {
        return zzbO(i) == 3;
    }
}
