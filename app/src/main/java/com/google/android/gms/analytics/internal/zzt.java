package com.google.android.gms.analytics.internal;

import android.os.Handler;
import android.os.Looper;
import com.google.android.gms.common.internal.zzu;

abstract class zzt {
    private static volatile Handler zzKS;
    private final zzf zzJy;
    private volatile long zzKT;
    private boolean zzKU;
    private final Runnable zzx = new Runnable(this) {
        final /* synthetic */ zzt zzKV;

        {
            this.zzKV = r1;
        }

        public void run() {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                this.zzKV.zzJy.zzhS().zze((Runnable) this);
                return;
            }
            boolean zzbp = this.zzKV.zzbp();
            this.zzKV.zzKT = 0;
            if (zzbp && !this.zzKV.zzKU) {
                this.zzKV.run();
            }
        }
    };

    zzt(zzf com_google_android_gms_analytics_internal_zzf) {
        zzu.zzu(com_google_android_gms_analytics_internal_zzf);
        this.zzJy = com_google_android_gms_analytics_internal_zzf;
    }

    private Handler getHandler() {
        if (zzKS != null) {
            return zzKS;
        }
        Handler handler;
        synchronized (zzt.class) {
            if (zzKS == null) {
                zzKS = new Handler(this.zzJy.getContext().getMainLooper());
            }
            handler = zzKS;
        }
        return handler;
    }

    public void cancel() {
        this.zzKT = 0;
        getHandler().removeCallbacks(this.zzx);
    }

    public abstract void run();

    public boolean zzbp() {
        return this.zzKT != 0;
    }

    public long zzjD() {
        return this.zzKT == 0 ? 0 : Math.abs(this.zzJy.zzhP().currentTimeMillis() - this.zzKT);
    }

    public void zzt(long j) {
        cancel();
        if (j >= 0) {
            this.zzKT = this.zzJy.zzhP().currentTimeMillis();
            if (!getHandler().postDelayed(this.zzx, j)) {
                this.zzJy.zzhQ().zze("Failed to schedule delayed post. time", Long.valueOf(j));
            }
        }
    }

    public void zzu(long j) {
        long j2 = 0;
        if (!zzbp()) {
            return;
        }
        if (j < 0) {
            cancel();
            return;
        }
        long abs = j - Math.abs(this.zzJy.zzhP().currentTimeMillis() - this.zzKT);
        if (abs >= 0) {
            j2 = abs;
        }
        getHandler().removeCallbacks(this.zzx);
        if (!getHandler().postDelayed(this.zzx, j2)) {
            this.zzJy.zzhQ().zze("Failed to adjust delayed post. time", Long.valueOf(j2));
        }
    }
}
