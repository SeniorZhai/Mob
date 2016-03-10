package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzlb;

class zzbe implements zzcd {
    private final long zzMf;
    private final int zzMg;
    private double zzMh;
    private long zzMi;
    private final Object zzMj = new Object();
    private final long zzaMh;
    private final zzlb zzpw;
    private final String zzuO;

    public zzbe(int i, long j, long j2, String str, zzlb com_google_android_gms_internal_zzlb) {
        this.zzMg = i;
        this.zzMh = (double) this.zzMg;
        this.zzMf = j;
        this.zzaMh = j2;
        this.zzuO = str;
        this.zzpw = com_google_android_gms_internal_zzlb;
    }

    public boolean zzkb() {
        boolean z = false;
        synchronized (this.zzMj) {
            long currentTimeMillis = this.zzpw.currentTimeMillis();
            if (currentTimeMillis - this.zzMi < this.zzaMh) {
                zzbg.zzaC("Excessive " + this.zzuO + " detected; call ignored.");
            } else {
                if (this.zzMh < ((double) this.zzMg)) {
                    double d = ((double) (currentTimeMillis - this.zzMi)) / ((double) this.zzMf);
                    if (d > 0.0d) {
                        this.zzMh = Math.min((double) this.zzMg, d + this.zzMh);
                    }
                }
                this.zzMi = currentTimeMillis;
                if (this.zzMh >= 1.0d) {
                    this.zzMh -= 1.0d;
                    z = true;
                } else {
                    zzbg.zzaC("Excessive " + this.zzuO + " detected; call ignored.");
                }
            }
        }
        return z;
    }
}
