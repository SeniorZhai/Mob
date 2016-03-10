package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzv extends zzak {
    private static final String ID = zzad.CUSTOM_VAR.toString();
    private static final String NAME = zzae.NAME.toString();
    private static final String zzaLo = zzae.DEFAULT_VALUE.toString();
    private final DataLayer zzaKz;

    public zzv(DataLayer dataLayer) {
        super(ID, NAME);
        this.zzaKz = dataLayer;
    }

    public zza zzE(Map<String, zza> map) {
        Object obj = this.zzaKz.get(zzdf.zzg((zza) map.get(NAME)));
        if (obj != null) {
            return zzdf.zzI(obj);
        }
        zza com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaLo);
        return com_google_android_gms_internal_zzag_zza != null ? com_google_android_gms_internal_zzag_zza : zzdf.zzzQ();
    }

    public boolean zzyh() {
        return false;
    }
}
