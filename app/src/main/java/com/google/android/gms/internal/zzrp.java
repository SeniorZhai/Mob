package com.google.android.gms.internal;

import java.io.IOException;
import java.util.Arrays;

final class zzrp {
    final int tag;
    final byte[] zzaWg;

    zzrp(int i, byte[] bArr) {
        this.tag = i;
        this.zzaWg = bArr;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzrp)) {
            return false;
        }
        zzrp com_google_android_gms_internal_zzrp = (zzrp) o;
        return this.tag == com_google_android_gms_internal_zzrp.tag && Arrays.equals(this.zzaWg, com_google_android_gms_internal_zzrp.zzaWg);
    }

    public int hashCode() {
        return ((this.tag + 527) * 31) + Arrays.hashCode(this.zzaWg);
    }

    int zzB() {
        return (0 + zzrg.zzkO(this.tag)) + this.zzaWg.length;
    }

    void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
        com_google_android_gms_internal_zzrg.zzkN(this.tag);
        com_google_android_gms_internal_zzrg.zzD(this.zzaWg);
    }
}
