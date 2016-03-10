package com.google.android.gms.analytics.internal;

import android.util.Log;
import com.google.android.gms.analytics.Logger;

@Deprecated
public class zzae {
    private static volatile Logger zzMk;

    static {
        setLogger(new zzs());
    }

    public static Logger getLogger() {
        return zzMk;
    }

    public static void setLogger(Logger logger) {
        zzMk = logger;
    }

    public static boolean zzL(int i) {
        return getLogger() != null && getLogger().getLogLevel() <= i;
    }

    public static void zzaA(String str) {
        zzaf zzkc = zzaf.zzkc();
        if (zzkc != null) {
            zzkc.zzaV(str);
        } else if (zzL(1)) {
            Log.i((String) zzy.zzLb.get(), str);
        }
        Logger logger = zzMk;
        if (logger != null) {
            logger.info(str);
        }
    }

    public static void zzaB(String str) {
        zzaf zzkc = zzaf.zzkc();
        if (zzkc != null) {
            zzkc.zzaT(str);
        } else if (zzL(0)) {
            Log.v((String) zzy.zzLb.get(), str);
        }
        Logger logger = zzMk;
        if (logger != null) {
            logger.verbose(str);
        }
    }

    public static void zzaC(String str) {
        zzaf zzkc = zzaf.zzkc();
        if (zzkc != null) {
            zzkc.zzaW(str);
        } else if (zzL(2)) {
            Log.w((String) zzy.zzLb.get(), str);
        }
        Logger logger = zzMk;
        if (logger != null) {
            logger.warn(str);
        }
    }

    public static void zzf(String str, Object obj) {
        zzaf zzkc = zzaf.zzkc();
        if (zzkc != null) {
            zzkc.zze(str, obj);
        } else if (zzL(3)) {
            Log.e((String) zzy.zzLb.get(), obj != null ? str + ":" + obj : str);
        }
        Logger logger = zzMk;
        if (logger != null) {
            logger.error(str);
        }
    }
}
