package com.google.android.gms.internal;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders.ScreenViewBuilder;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.tagmanager.Container;

class zzpu {
    private final Context mContext;
    private final zzpv zzoY;

    static class zza implements com.google.android.gms.internal.zznw.zza {
        private final Tracker zzIq;

        zza(Tracker tracker) {
            this.zzIq = tracker;
        }

        public void zza(zzod com_google_android_gms_internal_zzod) {
            this.zzIq.setScreenName(com_google_android_gms_internal_zzod.zzwB());
            ScreenViewBuilder screenViewBuilder = new ScreenViewBuilder();
            screenViewBuilder.set("&a", String.valueOf(com_google_android_gms_internal_zzod.zzbn()));
            this.zzIq.send(screenViewBuilder.build());
        }

        public void zza(zzod com_google_android_gms_internal_zzod, Activity activity) {
        }
    }

    public zzpu(Context context, Container container, zzpv com_google_android_gms_internal_zzpv) {
        this.mContext = context;
        this.zzoY = zza(container, com_google_android_gms_internal_zzpv);
        zzzS();
    }

    static zzpv zza(Container container, zzpv com_google_android_gms_internal_zzpv) {
        if (container == null || container.isDefault()) {
            return com_google_android_gms_internal_zzpv;
        }
        com.google.android.gms.internal.zzpv.zza com_google_android_gms_internal_zzpv_zza = new com.google.android.gms.internal.zzpv.zza(com_google_android_gms_internal_zzpv.zzzT());
        com_google_android_gms_internal_zzpv_zza.zzeS(container.getString("trackingId")).zzap(container.getBoolean("trackScreenViews")).zzaq(container.getBoolean("collectAdIdentifiers"));
        return com_google_android_gms_internal_zzpv_zza.zzzW();
    }

    private void zzzS() {
        if (this.zzoY.zzzU() && !TextUtils.isEmpty(this.zzoY.getTrackingId())) {
            Tracker zzeR = zzeR(this.zzoY.getTrackingId());
            zzeR.enableAdvertisingIdCollection(this.zzoY.zzzV());
            zzb(new zza(zzeR));
        }
    }

    void zzb(com.google.android.gms.internal.zznw.zza com_google_android_gms_internal_zznw_zza) {
        zzu.zzu(com_google_android_gms_internal_zznw_zza);
        zznw zzaC = zznw.zzaC(this.mContext);
        zzaC.zzaf(true);
        zzaC.zza(com_google_android_gms_internal_zznw_zza);
    }

    Tracker zzeR(String str) {
        return GoogleAnalytics.getInstance(this.mContext).newTracker(str);
    }

    public zzpv zzzR() {
        return this.zzoY;
    }
}
