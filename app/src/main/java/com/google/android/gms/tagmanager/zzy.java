package com.google.android.gms.tagmanager;

import android.util.Log;

public class zzy implements zzbh {
    private int zzKR = 5;

    public void setLogLevel(int logLevel) {
        this.zzKR = logLevel;
    }

    public void zzaA(String str) {
        if (this.zzKR <= 4) {
            Log.i("GoogleTagManager", str);
        }
    }

    public void zzaB(String str) {
        if (this.zzKR <= 2) {
            Log.v("GoogleTagManager", str);
        }
    }

    public void zzaC(String str) {
        if (this.zzKR <= 5) {
            Log.w("GoogleTagManager", str);
        }
    }

    public void zzay(String str) {
        if (this.zzKR <= 3) {
            Log.d("GoogleTagManager", str);
        }
    }

    public void zzaz(String str) {
        if (this.zzKR <= 6) {
            Log.e("GoogleTagManager", str);
        }
    }

    public void zzb(String str, Throwable th) {
        if (this.zzKR <= 6) {
            Log.e("GoogleTagManager", str, th);
        }
    }

    public void zzd(String str, Throwable th) {
        if (this.zzKR <= 5) {
            Log.w("GoogleTagManager", str, th);
        }
    }
}
