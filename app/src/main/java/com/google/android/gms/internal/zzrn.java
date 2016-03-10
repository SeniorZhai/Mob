package com.google.android.gms.internal;

import java.io.IOException;

public abstract class zzrn {
    protected volatile int zzaWf = -1;

    public static final <T extends zzrn> T zza(T t, byte[] bArr) throws zzrm {
        return zzb(t, bArr, 0, bArr.length);
    }

    public static final void zza(zzrn com_google_android_gms_internal_zzrn, byte[] bArr, int i, int i2) {
        try {
            zzrg zzb = zzrg.zzb(bArr, i, i2);
            com_google_android_gms_internal_zzrn.zza(zzb);
            zzb.zzBH();
        } catch (Throwable e) {
            throw new RuntimeException("Serializing to a byte array threw an IOException (should never happen).", e);
        }
    }

    public static final <T extends zzrn> T zzb(T t, byte[] bArr, int i, int i2) throws zzrm {
        try {
            zzrf zza = zzrf.zza(bArr, i, i2);
            t.zzb(zza);
            zza.zzkz(0);
            return t;
        } catch (zzrm e) {
            throw e;
        } catch (IOException e2) {
            throw new RuntimeException("Reading from a byte array threw an IOException (should never happen).");
        }
    }

    public static final byte[] zzf(zzrn com_google_android_gms_internal_zzrn) {
        byte[] bArr = new byte[com_google_android_gms_internal_zzrn.zzBV()];
        zza(com_google_android_gms_internal_zzrn, bArr, 0, bArr.length);
        return bArr;
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return zzBK();
    }

    public String toString() {
        return zzro.zzg(this);
    }

    protected int zzB() {
        return 0;
    }

    public zzrn zzBK() throws CloneNotSupportedException {
        return (zzrn) super.clone();
    }

    public int zzBU() {
        if (this.zzaWf < 0) {
            zzBV();
        }
        return this.zzaWf;
    }

    public int zzBV() {
        int zzB = zzB();
        this.zzaWf = zzB;
        return zzB;
    }

    public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
    }

    public abstract zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException;
}
