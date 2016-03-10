package com.google.android.gms.analytics.internal;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import com.google.android.gms.analytics.AnalyticsReceiver;
import com.google.android.gms.common.internal.zzu;

public class zzv extends zzd {
    private boolean zzKW;
    private boolean zzKX;
    private AlarmManager zzKY = ((AlarmManager) getContext().getSystemService(NotificationCompatApi21.CATEGORY_ALARM));

    protected zzv(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf);
    }

    private PendingIntent zzjI() {
        Intent intent = new Intent(getContext(), AnalyticsReceiver.class);
        intent.setAction("com.google.android.gms.analytics.ANALYTICS_DISPATCH");
        return PendingIntent.getBroadcast(getContext(), 0, intent, 0);
    }

    public void cancel() {
        zzia();
        this.zzKX = false;
        this.zzKY.cancel(zzjI());
    }

    public boolean zzbp() {
        return this.zzKX;
    }

    protected void zzhn() {
        try {
            this.zzKY.cancel(zzjI());
            if (zzhR().zzjf() > 0) {
                ActivityInfo receiverInfo = getContext().getPackageManager().getReceiverInfo(new ComponentName(getContext(), AnalyticsReceiver.class), 2);
                if (receiverInfo != null && receiverInfo.enabled) {
                    zzaT("Receiver registered. Using alarm for local dispatch.");
                    this.zzKW = true;
                }
            }
        } catch (NameNotFoundException e) {
        }
    }

    public boolean zzjG() {
        return this.zzKW;
    }

    public void zzjH() {
        zzia();
        zzu.zza(zzjG(), (Object) "Receiver not registered");
        long zzjf = zzhR().zzjf();
        if (zzjf > 0) {
            cancel();
            long elapsedRealtime = zzhP().elapsedRealtime() + zzjf;
            this.zzKX = true;
            this.zzKY.setInexactRepeating(2, elapsedRealtime, 0, zzjI());
        }
    }
}
