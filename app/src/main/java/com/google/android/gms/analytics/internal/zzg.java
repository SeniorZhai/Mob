package com.google.android.gms.analytics.internal;

import android.content.Context;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzlb;
import com.google.android.gms.internal.zzld;
import com.google.android.gms.internal.zzns;

public class zzg {
    private final Context zzJR;
    private final Context zzqw;

    public zzg(Context context) {
        zzu.zzu(context);
        Object applicationContext = context.getApplicationContext();
        zzu.zzb(applicationContext, (Object) "Application context can't be null");
        this.zzqw = applicationContext;
        this.zzJR = applicationContext;
    }

    public Context getApplicationContext() {
        return this.zzqw;
    }

    protected zzns zzW(Context context) {
        return zzns.zzaB(context);
    }

    protected zzu zza(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzu(com_google_android_gms_analytics_internal_zzf);
    }

    protected zzk zzb(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzk(com_google_android_gms_analytics_internal_zzf);
    }

    protected zza zzc(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zza(com_google_android_gms_analytics_internal_zzf);
    }

    protected zzn zzd(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzn(com_google_android_gms_analytics_internal_zzf);
    }

    protected zzan zze(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzan(com_google_android_gms_analytics_internal_zzf);
    }

    protected zzaf zzf(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzaf(com_google_android_gms_analytics_internal_zzf);
    }

    protected zzr zzg(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzr(com_google_android_gms_analytics_internal_zzf);
    }

    protected zzlb zzh(zzf com_google_android_gms_analytics_internal_zzf) {
        return zzld.zzoQ();
    }

    protected GoogleAnalytics zzi(zzf com_google_android_gms_analytics_internal_zzf) {
        return new GoogleAnalytics(com_google_android_gms_analytics_internal_zzf);
    }

    public Context zzic() {
        return this.zzJR;
    }

    zzl zzj(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzl(com_google_android_gms_analytics_internal_zzf, this);
    }

    zzag zzk(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzag(com_google_android_gms_analytics_internal_zzf);
    }

    protected zzb zzl(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzb(com_google_android_gms_analytics_internal_zzf, this);
    }

    public zzj zzm(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzj(com_google_android_gms_analytics_internal_zzf);
    }

    public zzah zzn(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzah(com_google_android_gms_analytics_internal_zzf);
    }

    public zzi zzo(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzi(com_google_android_gms_analytics_internal_zzf);
    }

    public zzv zzp(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzv(com_google_android_gms_analytics_internal_zzf);
    }

    public zzai zzq(zzf com_google_android_gms_analytics_internal_zzf) {
        return new zzai(com_google_android_gms_analytics_internal_zzf);
    }
}
