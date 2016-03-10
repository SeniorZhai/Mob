package com.google.android.gms.internal;

import android.graphics.drawable.Drawable;
import com.google.android.gms.common.internal.zzt;

public final class zzkj extends zzku<zza, Drawable> {

    public static final class zza {
        public final int zzZL;
        public final int zzZM;

        public zza(int i, int i2) {
            this.zzZL = i;
            this.zzZM = i2;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof zza)) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            zza com_google_android_gms_internal_zzkj_zza = (zza) obj;
            return com_google_android_gms_internal_zzkj_zza.zzZL == this.zzZL && com_google_android_gms_internal_zzkj_zza.zzZM == this.zzZM;
        }

        public int hashCode() {
            return zzt.hashCode(Integer.valueOf(this.zzZL), Integer.valueOf(this.zzZM));
        }
    }

    public zzkj() {
        super(10);
    }
}
