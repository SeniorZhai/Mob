package com.google.android.gms.analytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import com.google.android.gms.analytics.internal.zzaf;
import com.google.android.gms.analytics.internal.zzam;
import com.google.android.gms.analytics.internal.zzf;
import com.google.android.gms.common.internal.zzu;

public final class AnalyticsReceiver extends BroadcastReceiver {
    static WakeLock zzIc;
    static Boolean zzId;
    static Object zzoW = new Object();

    public static boolean zzT(Context context) {
        zzu.zzu(context);
        if (zzId != null) {
            return zzId.booleanValue();
        }
        boolean zza = zzam.zza(context, AnalyticsReceiver.class, false);
        zzId = Boolean.valueOf(zza);
        return zza;
    }

    public void onReceive(Context context, Intent intent) {
        zzf zzV = zzf.zzV(context);
        zzaf zzhQ = zzV.zzhQ();
        String action = intent.getAction();
        if (zzV.zzhR().zziW()) {
            zzhQ.zza("Device AnalyticsReceiver got", action);
        } else {
            zzhQ.zza("Local AnalyticsReceiver got", action);
        }
        if ("com.google.android.gms.analytics.ANALYTICS_DISPATCH".equals(action)) {
            boolean zzU = AnalyticsService.zzU(context);
            Intent intent2 = new Intent(context, AnalyticsService.class);
            intent2.setAction("com.google.android.gms.analytics.ANALYTICS_DISPATCH");
            synchronized (zzoW) {
                context.startService(intent2);
                if (zzU) {
                    try {
                        PowerManager powerManager = (PowerManager) context.getSystemService("power");
                        if (zzIc == null) {
                            zzIc = powerManager.newWakeLock(1, "Analytics WakeLock");
                            zzIc.setReferenceCounted(false);
                        }
                        zzIc.acquire(1000);
                    } catch (SecurityException e) {
                        zzhQ.zzaW("Analytics service at risk of not starting. For more reliable analytics, add the WAKE_LOCK permission to your manifest. See http://goo.gl/8Rd3yj for instructions.");
                    }
                    return;
                }
            }
        }
    }
}
