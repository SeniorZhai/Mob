package com.google.android.gms.internal;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzqf.zzc;
import com.google.android.gms.internal.zzqf.zzg;
import com.google.android.gms.tagmanager.zzbg;

public abstract class zzqn {
    private zzqd zzaPU;
    private zzqb zzaPV;
    private zzlb zzpw;

    public enum zza {
        NOT_AVAILABLE,
        IO_ERROR,
        SERVER_ERROR
    }

    public class zzb {
        private final com.google.android.gms.internal.zzqe.zza.zza zzaPp;
        private final long zzaPr;
        private final Object zzaQa;

        public Object zzAH() {
            return this.zzaQa;
        }

        public com.google.android.gms.internal.zzqe.zza.zza zzAh() {
            return this.zzaPp;
        }

        public long zzAl() {
            return this.zzaPr;
        }
    }

    public zzqn(zzqd com_google_android_gms_internal_zzqd, zzqb com_google_android_gms_internal_zzqb) {
        this(com_google_android_gms_internal_zzqd, com_google_android_gms_internal_zzqb, zzld.zzoQ());
    }

    public zzqn(zzqd com_google_android_gms_internal_zzqd, zzqb com_google_android_gms_internal_zzqb, zzlb com_google_android_gms_internal_zzlb) {
        boolean z = true;
        if (com_google_android_gms_internal_zzqd.zzAf().size() != 1) {
            z = false;
        }
        zzu.zzV(z);
        this.zzaPU = com_google_android_gms_internal_zzqd;
        this.zzaPV = com_google_android_gms_internal_zzqb;
        this.zzpw = com_google_android_gms_internal_zzlb;
    }

    protected abstract zzb zza(zzpy com_google_android_gms_internal_zzpy);

    protected abstract void zza(zzqe com_google_android_gms_internal_zzqe);

    public void zza(zza com_google_android_gms_internal_zzqn_zza) {
        zzbg.zzaz("ResourceManager: Failed to download a resource: " + com_google_android_gms_internal_zzqn_zza.name());
        zzpy com_google_android_gms_internal_zzpy = (zzpy) this.zzaPU.zzAf().get(0);
        zzb zza = zza(com_google_android_gms_internal_zzpy);
        com.google.android.gms.internal.zzqe.zza com_google_android_gms_internal_zzqe_zza = (zza == null || !(zza.zzAH() instanceof zzc)) ? new com.google.android.gms.internal.zzqe.zza(Status.zzXR, com_google_android_gms_internal_zzpy, com.google.android.gms.internal.zzqe.zza.zza.NETWORK) : new com.google.android.gms.internal.zzqe.zza(Status.zzXP, com_google_android_gms_internal_zzpy, null, (zzc) zza.zzAH(), zza.zzAh(), zza.zzAl());
        zza(new zzqe(com_google_android_gms_internal_zzqe_zza));
    }

    public void zzu(byte[] bArr) {
        Object zzt;
        long j;
        com.google.android.gms.internal.zzqe.zza.zza com_google_android_gms_internal_zzqe_zza_zza;
        Object obj;
        com.google.android.gms.internal.zzqe.zza com_google_android_gms_internal_zzqe_zza;
        zzbg.zzaB("ResourceManager: Resource downloaded from Network: " + this.zzaPU.getId());
        zzpy com_google_android_gms_internal_zzpy = (zzpy) this.zzaPU.zzAf().get(0);
        com.google.android.gms.internal.zzqe.zza.zza com_google_android_gms_internal_zzqe_zza_zza2 = com.google.android.gms.internal.zzqe.zza.zza.NETWORK;
        zzb zza;
        try {
            zzt = this.zzaPV.zzt(bArr);
            long currentTimeMillis = this.zzpw.currentTimeMillis();
            if (zzt == null) {
                zzbg.zzaA("Parsed resource from network is null");
                zza = zza(com_google_android_gms_internal_zzpy);
                if (zza != null) {
                    zzt = zza.zzAH();
                    com_google_android_gms_internal_zzqe_zza_zza2 = zza.zzAh();
                    currentTimeMillis = zza.zzAl();
                }
            }
            j = currentTimeMillis;
            com_google_android_gms_internal_zzqe_zza_zza = com_google_android_gms_internal_zzqe_zza_zza2;
            obj = zzt;
        } catch (zzg e) {
            zzbg.zzaA("Resource from network is corrupted");
            zza = zza(com_google_android_gms_internal_zzpy);
            if (zza != null) {
                zzt = zza.zzAH();
                j = 0;
                com_google_android_gms_internal_zzqe_zza_zza = zza.zzAh();
                obj = zzt;
            } else {
                j = 0;
                com_google_android_gms_internal_zzqe_zza_zza = com_google_android_gms_internal_zzqe_zza_zza2;
                obj = null;
            }
        }
        if (obj != null) {
            com_google_android_gms_internal_zzqe_zza = new com.google.android.gms.internal.zzqe.zza(Status.zzXP, com_google_android_gms_internal_zzpy, bArr, (zzc) obj, com_google_android_gms_internal_zzqe_zza_zza, j);
        } else {
            com_google_android_gms_internal_zzqe_zza = new com.google.android.gms.internal.zzqe.zza(Status.zzXR, com_google_android_gms_internal_zzpy, com.google.android.gms.internal.zzqe.zza.zza.NETWORK);
        }
        zza(new zzqe(com_google_android_gms_internal_zzqe_zza));
    }
}
