package com.google.android.gms.analytics.internal;

import android.content.Context;
import android.content.Intent;
import com.google.android.gms.analytics.AnalyticsReceiver;
import com.google.android.gms.analytics.AnalyticsService;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzns;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class zzb extends zzd {
    private final zzl zzJq;

    public zzb(zzf com_google_android_gms_analytics_internal_zzf, zzg com_google_android_gms_analytics_internal_zzg) {
        super(com_google_android_gms_analytics_internal_zzf);
        zzu.zzu(com_google_android_gms_analytics_internal_zzg);
        this.zzJq = com_google_android_gms_analytics_internal_zzg.zzj(com_google_android_gms_analytics_internal_zzf);
    }

    void onServiceConnected() {
        zzhO();
        this.zzJq.onServiceConnected();
    }

    public void setLocalDispatchPeriod(final int dispatchPeriodInSeconds) {
        zzia();
        zzb("setLocalDispatchPeriod (sec)", Integer.valueOf(dispatchPeriodInSeconds));
        zzhS().zze(new Runnable(this) {
            final /* synthetic */ zzb zzJs;

            public void run() {
                this.zzJs.zzJq.zzs(((long) dispatchPeriodInSeconds) * 1000);
            }
        });
    }

    public void start() {
        this.zzJq.start();
    }

    public void zzG(final boolean z) {
        zza("Network connectivity status changed", Boolean.valueOf(z));
        zzhS().zze(new Runnable(this) {
            final /* synthetic */ zzb zzJs;

            public void run() {
                this.zzJs.zzJq.zzG(z);
            }
        });
    }

    public long zza(zzh com_google_android_gms_analytics_internal_zzh) {
        zzia();
        zzu.zzu(com_google_android_gms_analytics_internal_zzh);
        zzhO();
        long zza = this.zzJq.zza(com_google_android_gms_analytics_internal_zzh, true);
        if (zza == 0) {
            this.zzJq.zzc(com_google_android_gms_analytics_internal_zzh);
        }
        return zza;
    }

    public void zza(final zzab com_google_android_gms_analytics_internal_zzab) {
        zzu.zzu(com_google_android_gms_analytics_internal_zzab);
        zzia();
        zzb("Hit delivery requested", com_google_android_gms_analytics_internal_zzab);
        zzhS().zze(new Runnable(this) {
            final /* synthetic */ zzb zzJs;

            public void run() {
                this.zzJs.zzJq.zza(com_google_android_gms_analytics_internal_zzab);
            }
        });
    }

    public void zza(final zzw com_google_android_gms_analytics_internal_zzw) {
        zzia();
        zzhS().zze(new Runnable(this) {
            final /* synthetic */ zzb zzJs;

            public void run() {
                this.zzJs.zzJq.zzb(com_google_android_gms_analytics_internal_zzw);
            }
        });
    }

    public void zza(final String str, final Runnable runnable) {
        zzu.zzh(str, "campaign param can't be empty");
        zzhS().zze(new Runnable(this) {
            final /* synthetic */ zzb zzJs;

            public void run() {
                this.zzJs.zzJq.zzbb(str);
                if (runnable != null) {
                    runnable.run();
                }
            }
        });
    }

    public void zzhG() {
        zzia();
        zzhN();
        zzhS().zze(new Runnable(this) {
            final /* synthetic */ zzb zzJs;

            {
                this.zzJs = r1;
            }

            public void run() {
                this.zzJs.zzJq.zzhG();
            }
        });
    }

    public void zzhH() {
        zzia();
        Context context = getContext();
        if (AnalyticsReceiver.zzT(context) && AnalyticsService.zzU(context)) {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction("com.google.android.gms.analytics.ANALYTICS_DISPATCH");
            context.startService(intent);
            return;
        }
        zza(null);
    }

    public boolean zzhI() {
        zzia();
        try {
            zzhS().zzb(new Callable<Void>(this) {
                final /* synthetic */ zzb zzJs;

                {
                    this.zzJs = r1;
                }

                public /* synthetic */ Object call() throws Exception {
                    return zzgk();
                }

                public Void zzgk() throws Exception {
                    this.zzJs.zzJq.zziF();
                    return null;
                }
            }).get();
            return true;
        } catch (InterruptedException e) {
            zzd("syncDispatchLocalHits interrupted", e);
            return false;
        } catch (ExecutionException e2) {
            zze("syncDispatchLocalHits failed", e2);
            return false;
        }
    }

    public void zzhJ() {
        zzia();
        zzns.zzhO();
        this.zzJq.zzhJ();
    }

    public void zzhK() {
        zzaT("Radio powered up");
        zzhH();
    }

    void zzhL() {
        zzhO();
        this.zzJq.zzhL();
    }

    protected void zzhn() {
        this.zzJq.zza();
    }
}
