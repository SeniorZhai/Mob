package com.google.android.gms.analytics.internal;

public abstract class zzd extends zzc {
    private boolean zzJA;
    private boolean zzJz;

    protected zzd(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf);
    }

    public boolean isInitialized() {
        return this.zzJz && !this.zzJA;
    }

    public void zza() {
        zzhn();
        this.zzJz = true;
    }

    protected abstract void zzhn();

    protected void zzia() {
        if (!isInitialized()) {
            throw new IllegalStateException("Not initialized");
        }
    }
}
