package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

abstract class zzbv extends zzca {
    public zzbv(String str) {
        super(str);
    }

    protected boolean zza(zza com_google_android_gms_internal_zzag_zza, zza com_google_android_gms_internal_zzag_zza2, Map<String, zza> map) {
        zzde zzh = zzdf.zzh(com_google_android_gms_internal_zzag_zza);
        zzde zzh2 = zzdf.zzh(com_google_android_gms_internal_zzag_zza2);
        return (zzh == zzdf.zzzO() || zzh2 == zzdf.zzzO()) ? false : zza(zzh, zzh2, (Map) map);
    }

    protected abstract boolean zza(zzde com_google_android_gms_tagmanager_zzde, zzde com_google_android_gms_tagmanager_zzde2, Map<String, zza> map);
}
