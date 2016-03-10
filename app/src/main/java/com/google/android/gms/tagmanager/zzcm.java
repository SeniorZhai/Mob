package com.google.android.gms.tagmanager;

import android.content.Context;
import com.google.android.gms.internal.zzaf.zzj;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class zzcm implements zze {
    private boolean mClosed;
    private final Context mContext;
    private String zzaKV;
    private final String zzaKy;
    private zzbf<zzj> zzaMU;
    private zzs zzaMV;
    private final ScheduledExecutorService zzaMX;
    private final zza zzaMY;
    private ScheduledFuture<?> zzaMZ;

    interface zzb {
        ScheduledExecutorService zzzm();
    }

    interface zza {
        zzcl zza(zzs com_google_android_gms_tagmanager_zzs);
    }

    public zzcm(Context context, String str, zzs com_google_android_gms_tagmanager_zzs) {
        this(context, str, com_google_android_gms_tagmanager_zzs, null, null);
    }

    zzcm(Context context, String str, zzs com_google_android_gms_tagmanager_zzs, zzb com_google_android_gms_tagmanager_zzcm_zzb, zza com_google_android_gms_tagmanager_zzcm_zza) {
        this.zzaMV = com_google_android_gms_tagmanager_zzs;
        this.mContext = context;
        this.zzaKy = str;
        if (com_google_android_gms_tagmanager_zzcm_zzb == null) {
            com_google_android_gms_tagmanager_zzcm_zzb = new zzb(this) {
                final /* synthetic */ zzcm zzaNa;

                {
                    this.zzaNa = r1;
                }

                public ScheduledExecutorService zzzm() {
                    return Executors.newSingleThreadScheduledExecutor();
                }
            };
        }
        this.zzaMX = com_google_android_gms_tagmanager_zzcm_zzb.zzzm();
        if (com_google_android_gms_tagmanager_zzcm_zza == null) {
            this.zzaMY = new zza(this) {
                final /* synthetic */ zzcm zzaNa;

                {
                    this.zzaNa = r1;
                }

                public zzcl zza(zzs com_google_android_gms_tagmanager_zzs) {
                    return new zzcl(this.zzaNa.mContext, this.zzaNa.zzaKy, com_google_android_gms_tagmanager_zzs);
                }
            };
        } else {
            this.zzaMY = com_google_android_gms_tagmanager_zzcm_zza;
        }
    }

    private zzcl zzeC(String str) {
        zzcl zza = this.zzaMY.zza(this.zzaMV);
        zza.zza(this.zzaMU);
        zza.zzem(this.zzaKV);
        zza.zzeB(str);
        return zza;
    }

    private synchronized void zzzl() {
        if (this.mClosed) {
            throw new IllegalStateException("called method after closed");
        }
    }

    public synchronized void release() {
        zzzl();
        if (this.zzaMZ != null) {
            this.zzaMZ.cancel(false);
        }
        this.zzaMX.shutdown();
        this.mClosed = true;
    }

    public synchronized void zza(zzbf<zzj> com_google_android_gms_tagmanager_zzbf_com_google_android_gms_internal_zzaf_zzj) {
        zzzl();
        this.zzaMU = com_google_android_gms_tagmanager_zzbf_com_google_android_gms_internal_zzaf_zzj;
    }

    public synchronized void zzem(String str) {
        zzzl();
        this.zzaKV = str;
    }

    public synchronized void zzf(long j, String str) {
        zzbg.zzaB("loadAfterDelay: containerId=" + this.zzaKy + " delay=" + j);
        zzzl();
        if (this.zzaMU == null) {
            throw new IllegalStateException("callback must be set before loadAfterDelay() is called.");
        }
        if (this.zzaMZ != null) {
            this.zzaMZ.cancel(false);
        }
        this.zzaMZ = this.zzaMX.schedule(zzeC(str), j, TimeUnit.MILLISECONDS);
    }
}
