package com.google.android.gms.internal;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.internal.zzu;
import com.helpshift.storage.ProfilesDBHelper;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import java.util.HashMap;
import java.util.Map;

public final class zzog implements ActivityLifecycleCallbacks {
    private final zznw zzaEV;
    private final Map<Activity, zzod> zzaEW = new HashMap();

    public zzog(zznw com_google_android_gms_internal_zznw) {
        zzu.zzu(com_google_android_gms_internal_zznw);
        this.zzaEV = com_google_android_gms_internal_zznw;
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle("com.google.android.gms.measurement.screen_view");
            if (bundle != null) {
                int i = bundle.getInt(DBLikedChannelsHelper.KEY_ID);
                if (i <= 0) {
                    Log.w("com.google.android.gms.measurement.internal.ActivityLifecycleTracker", "Invalid screenId in saved activity state");
                    return;
                }
                zzod zza = zza(activity, i);
                zza.setScreenName(bundle.getString(ProfilesDBHelper.COLUMN_NAME));
                zza.zzhL(bundle.getInt("referrer_id"));
                zza.zzdJ(bundle.getString("referrer_name"));
                zza.zzai(bundle.getBoolean("interstitial"));
                zza.zzwF();
            }
        }
    }

    public void onActivityDestroyed(Activity activity) {
        this.zzaEW.remove(activity);
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        if (outState != null) {
            zzod com_google_android_gms_internal_zzod = (zzod) this.zzaEW.get(activity);
            if (com_google_android_gms_internal_zzod != null) {
                Bundle bundle = new Bundle();
                bundle.putInt(DBLikedChannelsHelper.KEY_ID, com_google_android_gms_internal_zzod.zzbn());
                bundle.putString(ProfilesDBHelper.COLUMN_NAME, com_google_android_gms_internal_zzod.zzwB());
                bundle.putInt("referrer_id", com_google_android_gms_internal_zzod.zzwC());
                bundle.putString("referrer_name", com_google_android_gms_internal_zzod.zzwD());
                bundle.putBoolean("interstitial", com_google_android_gms_internal_zzod.zzwG());
                outState.putBundle("com.google.android.gms.measurement.screen_view", bundle);
            }
        }
    }

    public void onActivityStarted(Activity activity) {
        this.zzaEV.zzb(zza(activity, 0), activity);
    }

    public void onActivityStopped(Activity activity) {
    }

    zzod zza(Activity activity, int i) {
        zzu.zzu(activity);
        zzod com_google_android_gms_internal_zzod = (zzod) this.zzaEW.get(activity);
        if (com_google_android_gms_internal_zzod == null) {
            com_google_android_gms_internal_zzod = i == 0 ? new zzod(true) : new zzod(true, i);
            com_google_android_gms_internal_zzod.setScreenName(activity.getClass().getCanonicalName());
            this.zzaEW.put(activity, com_google_android_gms_internal_zzod);
        }
        return com_google_android_gms_internal_zzod;
    }
}
