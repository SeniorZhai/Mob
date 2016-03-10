package com.google.android.gms.internal;

import java.io.IOException;

public abstract class zzrh<M extends zzrh<M>> extends zzrn {
    protected zzrj zzaVU;

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return zzBJ();
    }

    protected int zzB() {
        int i = 0;
        if (this.zzaVU == null) {
            return 0;
        }
        int i2 = 0;
        while (i < this.zzaVU.size()) {
            i2 += this.zzaVU.zzkS(i).zzB();
            i++;
        }
        return i2;
    }

    protected final int zzBI() {
        return (this.zzaVU == null || this.zzaVU.isEmpty()) ? 0 : this.zzaVU.hashCode();
    }

    public M zzBJ() throws CloneNotSupportedException {
        zzrh com_google_android_gms_internal_zzrh = (zzrh) super.zzBK();
        zzrl.zza(this, com_google_android_gms_internal_zzrh);
        return com_google_android_gms_internal_zzrh;
    }

    public /* synthetic */ zzrn zzBK() throws CloneNotSupportedException {
        return zzBJ();
    }

    public final <T> T zza(zzri<M, T> com_google_android_gms_internal_zzri_M__T) {
        if (this.zzaVU == null) {
            return null;
        }
        zzrk zzkR = this.zzaVU.zzkR(zzrq.zzkV(com_google_android_gms_internal_zzri_M__T.tag));
        return zzkR != null ? zzkR.zzb(com_google_android_gms_internal_zzri_M__T) : null;
    }

    public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
        if (this.zzaVU != null) {
            for (int i = 0; i < this.zzaVU.size(); i++) {
                this.zzaVU.zzkS(i).zza(com_google_android_gms_internal_zzrg);
            }
        }
    }

    protected final boolean zza(zzrf com_google_android_gms_internal_zzrf, int i) throws IOException {
        int position = com_google_android_gms_internal_zzrf.getPosition();
        if (!com_google_android_gms_internal_zzrf.zzkA(i)) {
            return false;
        }
        int zzkV = zzrq.zzkV(i);
        zzrp com_google_android_gms_internal_zzrp = new zzrp(i, com_google_android_gms_internal_zzrf.zzx(position, com_google_android_gms_internal_zzrf.getPosition() - position));
        zzrk com_google_android_gms_internal_zzrk = null;
        if (this.zzaVU == null) {
            this.zzaVU = new zzrj();
        } else {
            com_google_android_gms_internal_zzrk = this.zzaVU.zzkR(zzkV);
        }
        if (com_google_android_gms_internal_zzrk == null) {
            com_google_android_gms_internal_zzrk = new zzrk();
            this.zzaVU.zza(zzkV, com_google_android_gms_internal_zzrk);
        }
        com_google_android_gms_internal_zzrk.zza(com_google_android_gms_internal_zzrp);
        return true;
    }

    protected final boolean zza(M m) {
        return (this.zzaVU == null || this.zzaVU.isEmpty()) ? m.zzaVU == null || m.zzaVU.isEmpty() : this.zzaVU.equals(m.zzaVU);
    }
}
