package com.google.android.gms.analytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.TextUtils;
import com.google.android.gms.analytics.internal.zzaf;
import com.google.android.gms.analytics.internal.zzam;
import com.google.android.gms.analytics.internal.zzf;
import com.google.android.gms.common.internal.zzu;

public class CampaignTrackingReceiver extends BroadcastReceiver {
    static WakeLock zzIc;
    static Boolean zzId;
    static Object zzoW = new Object();

    public static boolean zzT(Context context) {
        zzu.zzu(context);
        if (zzId != null) {
            return zzId.booleanValue();
        }
        boolean zza = zzam.zza(context, CampaignTrackingReceiver.class, true);
        zzId = Boolean.valueOf(zza);
        return zza;
    }

    public void onReceive(Context context, Intent intent) {
        zzf zzV = zzf.zzV(context);
        zzaf zzhQ = zzV.zzhQ();
        String stringExtra = intent.getStringExtra("referrer");
        String action = intent.getAction();
        zzhQ.zza("CampaignTrackingReceiver received", action);
        if (!"com.android.vending.INSTALL_REFERRER".equals(action) || TextUtils.isEmpty(stringExtra)) {
            zzhQ.zzaW("CampaignTrackingReceiver received unexpected intent without referrer extra");
            return;
        }
        boolean zzU = CampaignTrackingService.zzU(context);
        if (!zzU) {
            zzhQ.zzaW("CampaignTrackingService not registered or disabled. Installation tracking not possible. See http://goo.gl/8Rd3yj for instructions.");
        }
        zzaL(stringExtra);
        if (zzV.zzhR().zziW()) {
            zzhQ.zzaX("Received unexpected installation campaign on package side");
            return;
        }
        Class zzhf = zzhf();
        zzu.zzu(zzhf);
        Intent intent2 = new Intent(context, zzhf);
        intent2.putExtra("referrer", stringExtra);
        synchronized (zzoW) {
            context.startService(intent2);
            if (zzU) {
                try {
                    PowerManager powerManager = (PowerManager) context.getSystemService("power");
                    if (zzIc == null) {
                        zzIc = powerManager.newWakeLock(1, "Analytics campaign WakeLock");
                        zzIc.setReferenceCounted(false);
                    }
                    zzIc.acquire(1000);
                } catch (SecurityException e) {
                    zzhQ.zzaW("CampaignTrackingService service at risk of not starting. For more reliable installation campaign reports, add the WAKE_LOCK permission to your manifest. See http://goo.gl/8Rd3yj for instructions.");
                }
                return;
            }
        }
    }

    protected void zzaL(String str) {
    }

    protected Class<? extends CampaignTrackingService> zzhf() {
        return CampaignTrackingService.class;
    }
}
