package com.google.android.gms.internal;

import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.internal.zzqf.zzc;

public class zzqe implements Result {
    private final zza zzaPo;

    public static class zza {
        private final Status zzOt;
        private final zza zzaPp;
        private final byte[] zzaPq;
        private final long zzaPr;
        private final zzpy zzaPs;
        private final zzc zzaPt;

        public enum zza {
            NETWORK,
            DISK,
            DEFAULT
        }

        public zza(Status status, zzpy com_google_android_gms_internal_zzpy, zza com_google_android_gms_internal_zzqe_zza_zza) {
            this(status, com_google_android_gms_internal_zzpy, null, null, com_google_android_gms_internal_zzqe_zza_zza, 0);
        }

        public zza(Status status, zzpy com_google_android_gms_internal_zzpy, byte[] bArr, zzc com_google_android_gms_internal_zzqf_zzc, zza com_google_android_gms_internal_zzqe_zza_zza, long j) {
            this.zzOt = status;
            this.zzaPs = com_google_android_gms_internal_zzpy;
            this.zzaPq = bArr;
            this.zzaPt = com_google_android_gms_internal_zzqf_zzc;
            this.zzaPp = com_google_android_gms_internal_zzqe_zza_zza;
            this.zzaPr = j;
        }

        public Status getStatus() {
            return this.zzOt;
        }

        public zza zzAh() {
            return this.zzaPp;
        }

        public byte[] zzAi() {
            return this.zzaPq;
        }

        public zzpy zzAj() {
            return this.zzaPs;
        }

        public zzc zzAk() {
            return this.zzaPt;
        }

        public long zzAl() {
            return this.zzaPr;
        }
    }

    public zzqe(zza com_google_android_gms_internal_zzqe_zza) {
        this.zzaPo = com_google_android_gms_internal_zzqe_zza;
    }

    public Status getStatus() {
        return this.zzaPo.getStatus();
    }

    public zza zzAg() {
        return this.zzaPo;
    }
}
