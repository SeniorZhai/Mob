package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class zzt extends zzak {
    private static final String ID = zzad.FUNCTION_CALL.toString();
    private static final String zzaKr = zzae.ADDITIONAL_PARAMS.toString();
    private static final String zzaLd = zzae.FUNCTION_CALL_NAME.toString();
    private final zza zzaLe;

    public interface zza {
        Object zzd(String str, Map<String, Object> map);
    }

    public zzt(zza com_google_android_gms_tagmanager_zzt_zza) {
        super(ID, zzaLd);
        this.zzaLe = com_google_android_gms_tagmanager_zzt_zza;
    }

    public com.google.android.gms.internal.zzag.zza zzE(Map<String, com.google.android.gms.internal.zzag.zza> map) {
        String zzg = zzdf.zzg((com.google.android.gms.internal.zzag.zza) map.get(zzaLd));
        Map hashMap = new HashMap();
        com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza = (com.google.android.gms.internal.zzag.zza) map.get(zzaKr);
        if (com_google_android_gms_internal_zzag_zza != null) {
            Object zzl = zzdf.zzl(com_google_android_gms_internal_zzag_zza);
            if (zzl instanceof Map) {
                for (Entry entry : ((Map) zzl).entrySet()) {
                    hashMap.put(entry.getKey().toString(), entry.getValue());
                }
            } else {
                zzbg.zzaC("FunctionCallMacro: expected ADDITIONAL_PARAMS to be a map.");
                return zzdf.zzzQ();
            }
        }
        try {
            return zzdf.zzI(this.zzaLe.zzd(zzg, hashMap));
        } catch (Exception e) {
            zzbg.zzaC("Custom macro/tag " + zzg + " threw exception " + e.getMessage());
            return zzdf.zzzQ();
        }
    }

    public boolean zzyh() {
        return false;
    }
}
