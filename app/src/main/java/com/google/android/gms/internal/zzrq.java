package com.google.android.gms.internal;

import java.io.IOException;

public final class zzrq {
    public static final int[] zzaWh = new int[0];
    public static final long[] zzaWi = new long[0];
    public static final float[] zzaWj = new float[0];
    public static final double[] zzaWk = new double[0];
    public static final boolean[] zzaWl = new boolean[0];
    public static final String[] zzaWm = new String[0];
    public static final byte[][] zzaWn = new byte[0][];
    public static final byte[] zzaWo = new byte[0];

    static int zzD(int i, int i2) {
        return (i << 3) | i2;
    }

    public static final int zzb(zzrf com_google_android_gms_internal_zzrf, int i) throws IOException {
        int i2 = 1;
        int position = com_google_android_gms_internal_zzrf.getPosition();
        com_google_android_gms_internal_zzrf.zzkA(i);
        while (com_google_android_gms_internal_zzrf.zzBr() == i) {
            com_google_android_gms_internal_zzrf.zzkA(i);
            i2++;
        }
        com_google_android_gms_internal_zzrf.zzkE(position);
        return i2;
    }

    static int zzkU(int i) {
        return i & 7;
    }

    public static int zzkV(int i) {
        return i >>> 3;
    }
}
