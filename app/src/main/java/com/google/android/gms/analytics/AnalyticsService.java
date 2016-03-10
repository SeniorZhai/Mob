package com.google.android.gms.analytics;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import com.google.android.gms.analytics.internal.zzaf;
import com.google.android.gms.analytics.internal.zzam;
import com.google.android.gms.analytics.internal.zzf;
import com.google.android.gms.analytics.internal.zzw;
import com.google.android.gms.common.internal.zzu;

public final class AnalyticsService extends Service {
    private static Boolean zzIe;
    private final Handler mHandler = new Handler();

    public static boolean zzU(Context context) {
        zzu.zzu(context);
        if (zzIe != null) {
            return zzIe.booleanValue();
        }
        boolean zza = zzam.zza(context, AnalyticsService.class);
        zzIe = Boolean.valueOf(zza);
        return zza;
    }

    private void zzhd() {
        try {
            synchronized (AnalyticsReceiver.zzoW) {
                WakeLock wakeLock = AnalyticsReceiver.zzIc;
                if (wakeLock != null && wakeLock.isHeld()) {
                    wakeLock.release();
                }
            }
        } catch (SecurityException e) {
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        zzf zzV = zzf.zzV(this);
        zzaf zzhQ = zzV.zzhQ();
        if (zzV.zzhR().zziW()) {
            zzhQ.zzaT("Device AnalyticsService is starting up");
        } else {
            zzhQ.zzaT("Local AnalyticsService is starting up");
        }
    }

    public void onDestroy() {
        zzf zzV = zzf.zzV(this);
        zzaf zzhQ = zzV.zzhQ();
        if (zzV.zzhR().zziW()) {
            zzhQ.zzaT("Device AnalyticsService is shutting down");
        } else {
            zzhQ.zzaT("Local AnalyticsService is shutting down");
        }
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, final int startId) {
        zzhd();
        final zzf zzV = zzf.zzV(this);
        final zzaf zzhQ = zzV.zzhQ();
        String action = intent.getAction();
        if (zzV.zzhR().zziW()) {
            zzhQ.zza("Device AnalyticsService called. startId, action", Integer.valueOf(startId), action);
        } else {
            zzhQ.zza("Local AnalyticsService called. startId, action", Integer.valueOf(startId), action);
        }
        if ("com.google.android.gms.analytics.ANALYTICS_DISPATCH".equals(action)) {
            zzV.zzhl().zza(new zzw(this) {
                final /* synthetic */ AnalyticsService zzIi;

                public void zzc(Throwable th) {
                    this.zzIi.mHandler.post(new Runnable(this) {
                        final /* synthetic */ AnonymousClass1 zzIj;

                        {
                            this.zzIj = r1;
                        }

                        public void run() {
                            if (!this.zzIj.zzIi.stopSelfResult(startId)) {
                                return;
                            }
                            if (zzV.zzhR().zziW()) {
                                zzhQ.zzaT("Device AnalyticsService processed last dispatch request");
                            } else {
                                zzhQ.zzaT("Local AnalyticsService processed last dispatch request");
                            }
                        }
                    });
                }
            });
        }
        return 2;
    }
}
