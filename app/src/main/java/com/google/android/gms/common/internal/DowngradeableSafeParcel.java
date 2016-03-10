package com.google.android.gms.common.internal;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public abstract class DowngradeableSafeParcel implements SafeParcelable {
    private static final Object zzZZ = new Object();
    private static ClassLoader zzaaa = null;
    private static Integer zzaab = null;
    private boolean zzaac = false;

    private static boolean zza(Class<?> cls) {
        boolean z = false;
        try {
            z = SafeParcelable.NULL.equals(cls.getField("NULL").get(null));
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e2) {
        }
        return z;
    }

    protected static boolean zzca(String str) {
        ClassLoader zznD = zznD();
        if (zznD == null) {
            return true;
        }
        try {
            return zza(zznD.loadClass(str));
        } catch (Exception e) {
            return false;
        }
    }

    protected static ClassLoader zznD() {
        ClassLoader classLoader;
        synchronized (zzZZ) {
            classLoader = zzaaa;
        }
        return classLoader;
    }

    protected static Integer zznE() {
        Integer num;
        synchronized (zzZZ) {
            num = zzaab;
        }
        return num;
    }

    protected boolean zznF() {
        return this.zzaac;
    }
}
