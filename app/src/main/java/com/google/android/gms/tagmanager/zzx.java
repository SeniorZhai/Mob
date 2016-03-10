package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.List;
import java.util.Map;

class zzx extends zzdd {
    private static final String ID = zzad.DATA_LAYER_WRITE.toString();
    private static final String VALUE = zzae.VALUE.toString();
    private static final String zzaLz = zzae.CLEAR_PERSISTENT_DATA_LAYER_PREFIX.toString();
    private final DataLayer zzaKz;

    public zzx(DataLayer dataLayer) {
        super(ID, VALUE);
        this.zzaKz = dataLayer;
    }

    private void zza(zza com_google_android_gms_internal_zzag_zza) {
        if (com_google_android_gms_internal_zzag_zza != null && com_google_android_gms_internal_zzag_zza != zzdf.zzzK()) {
            String zzg = zzdf.zzg(com_google_android_gms_internal_zzag_zza);
            if (zzg != zzdf.zzzP()) {
                this.zzaKz.zzen(zzg);
            }
        }
    }

    private void zzb(zza com_google_android_gms_internal_zzag_zza) {
        if (com_google_android_gms_internal_zzag_zza != null && com_google_android_gms_internal_zzag_zza != zzdf.zzzK()) {
            Object zzl = zzdf.zzl(com_google_android_gms_internal_zzag_zza);
            if (zzl instanceof List) {
                for (Object zzl2 : (List) zzl2) {
                    if (zzl2 instanceof Map) {
                        this.zzaKz.push((Map) zzl2);
                    }
                }
            }
        }
    }

    public void zzG(Map<String, zza> map) {
        zzb((zza) map.get(VALUE));
        zza((zza) map.get(zzaLz));
    }
}
