package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

abstract class zzcz extends zzca {
    public zzcz(String str) {
        super(str);
    }

    protected boolean zza(zza com_google_android_gms_internal_zzag_zza, zza com_google_android_gms_internal_zzag_zza2, Map<String, zza> map) {
        String zzg = zzdf.zzg(com_google_android_gms_internal_zzag_zza);
        String zzg2 = zzdf.zzg(com_google_android_gms_internal_zzag_zza2);
        return (zzg == zzdf.zzzP() || zzg2 == zzdf.zzzP()) ? false : zza(zzg, zzg2, (Map) map);
    }

    protected abstract boolean zza(String str, String str2, Map<String, zza> map);
}
