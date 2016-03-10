package com.google.android.gms.internal;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build.VERSION;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.common.internal.zzu;
import java.util.ArrayList;
import java.util.List;

public class zznw {
    private static final zza[] zzaEq = new zza[0];
    private static zznw zzaEr;
    private final Application zzaEs;
    private zzod zzaEt;
    private final List<zza> zzaEu = new ArrayList();
    private zzog zzaEv;

    public interface zza {
        void zza(zzod com_google_android_gms_internal_zzod);

        void zza(zzod com_google_android_gms_internal_zzod, Activity activity);
    }

    private zznw(Application application) {
        zzu.zzu(application);
        this.zzaEs = application;
    }

    public static zznw zzaC(Context context) {
        zznw com_google_android_gms_internal_zznw;
        zzu.zzu(context);
        Application application = (Application) context.getApplicationContext();
        zzu.zzu(application);
        synchronized (zznw.class) {
            if (zzaEr == null) {
                zzaEr = new zznw(application);
            }
            com_google_android_gms_internal_zznw = zzaEr;
        }
        return com_google_android_gms_internal_zznw;
    }

    private zza[] zzwh() {
        zza[] com_google_android_gms_internal_zznw_zzaArr;
        synchronized (this.zzaEu) {
            if (this.zzaEu.isEmpty()) {
                com_google_android_gms_internal_zznw_zzaArr = zzaEq;
            } else {
                com_google_android_gms_internal_zznw_zzaArr = (zza[]) this.zzaEu.toArray(new zza[this.zzaEu.size()]);
            }
        }
        return com_google_android_gms_internal_zznw_zzaArr;
    }

    public void zza(zza com_google_android_gms_internal_zznw_zza) {
        zzu.zzu(com_google_android_gms_internal_zznw_zza);
        synchronized (this.zzaEu) {
            this.zzaEu.remove(com_google_android_gms_internal_zznw_zza);
            this.zzaEu.add(com_google_android_gms_internal_zznw_zza);
        }
    }

    public void zzaf(boolean z) {
        if (VERSION.SDK_INT < 14) {
            Log.i("com.google.android.gms.measurement.ScreenViewService", "AutoScreeViewTracking is not supported on API 14 or earlier devices");
        } else if (zzwg() == z) {
        } else {
            if (z) {
                this.zzaEv = new zzog(this);
                this.zzaEs.registerActivityLifecycleCallbacks(this.zzaEv);
                return;
            }
            this.zzaEs.unregisterActivityLifecycleCallbacks(this.zzaEv);
            this.zzaEv = null;
        }
    }

    public void zzb(zzod com_google_android_gms_internal_zzod, Activity activity) {
        int i = 0;
        zzu.zzu(com_google_android_gms_internal_zzod);
        zza[] com_google_android_gms_internal_zznw_zzaArr = null;
        if (com_google_android_gms_internal_zzod.isMutable()) {
            if (activity instanceof zznv) {
                ((zznv) activity).zzb(com_google_android_gms_internal_zzod);
            }
            if (this.zzaEt != null) {
                com_google_android_gms_internal_zzod.zzhL(this.zzaEt.zzbn());
                com_google_android_gms_internal_zzod.zzdJ(this.zzaEt.zzwB());
            }
            zza[] zzwh = zzwh();
            for (zza zza : zzwh) {
                zza.zza(com_google_android_gms_internal_zzod, activity);
            }
            com_google_android_gms_internal_zzod.zzwF();
            if (!TextUtils.isEmpty(com_google_android_gms_internal_zzod.zzwB())) {
                com_google_android_gms_internal_zznw_zzaArr = zzwh;
            } else {
                return;
            }
        }
        if (this.zzaEt == null || this.zzaEt.zzbn() != com_google_android_gms_internal_zzod.zzbn()) {
            zzwf();
            this.zzaEt = com_google_android_gms_internal_zzod;
            if (com_google_android_gms_internal_zznw_zzaArr == null) {
                com_google_android_gms_internal_zznw_zzaArr = zzwh();
            }
            while (i < com_google_android_gms_internal_zznw_zzaArr.length) {
                com_google_android_gms_internal_zznw_zzaArr[i].zza(com_google_android_gms_internal_zzod);
                i++;
            }
            return;
        }
        this.zzaEt = com_google_android_gms_internal_zzod;
    }

    public zzod zzwe() {
        return this.zzaEt;
    }

    public void zzwf() {
        this.zzaEt = null;
    }

    public boolean zzwg() {
        return this.zzaEv != null;
    }
}
