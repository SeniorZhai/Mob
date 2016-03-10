package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;
import java.util.Set;

public abstract class zzca extends zzak {
    private static final String zzaLE = zzae.ARG0.toString();
    private static final String zzaMC = zzae.ARG1.toString();

    public zzca(String str) {
        super(str, zzaLE, zzaMC);
    }

    public zza zzE(Map<String, zza> map) {
        for (zza com_google_android_gms_internal_zzag_zza : map.values()) {
            if (com_google_android_gms_internal_zzag_zza == zzdf.zzzQ()) {
                return zzdf.zzI(Boolean.valueOf(false));
            }
        }
        zza com_google_android_gms_internal_zzag_zza2 = (zza) map.get(zzaLE);
        zza com_google_android_gms_internal_zzag_zza3 = (zza) map.get(zzaMC);
        boolean zza = (com_google_android_gms_internal_zzag_zza2 == null || com_google_android_gms_internal_zzag_zza3 == null) ? false : zza(com_google_android_gms_internal_zzag_zza2, com_google_android_gms_internal_zzag_zza3, map);
        return zzdf.zzI(Boolean.valueOf(zza));
    }

    protected abstract boolean zza(zza com_google_android_gms_internal_zzag_zza, zza com_google_android_gms_internal_zzag_zza2, Map<String, zza> map);

    public /* bridge */ /* synthetic */ String zzyM() {
        return super.zzyM();
    }

    public /* bridge */ /* synthetic */ Set zzyN() {
        return super.zzyN();
    }

    public boolean zzyh() {
        return true;
    }
}
