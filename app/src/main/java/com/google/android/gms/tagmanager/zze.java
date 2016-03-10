package com.google.android.gms.tagmanager;

import android.content.Context;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zze extends zzak {
    private static final String ID = zzad.ADWORDS_CLICK_REFERRER.toString();
    private static final String zzaKp = zzae.COMPONENT.toString();
    private static final String zzaKq = zzae.CONVERSION_ID.toString();
    private final Context zzpH;

    public zze(Context context) {
        super(ID, zzaKq);
        this.zzpH = context;
    }

    public zza zzE(Map<String, zza> map) {
        zza com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaKq);
        if (com_google_android_gms_internal_zzag_zza == null) {
            return zzdf.zzzQ();
        }
        String zzg = zzdf.zzg(com_google_android_gms_internal_zzag_zza);
        com_google_android_gms_internal_zzag_zza = (zza) map.get(zzaKp);
        String zzf = zzax.zzf(this.zzpH, zzg, com_google_android_gms_internal_zzag_zza != null ? zzdf.zzg(com_google_android_gms_internal_zzag_zza) : null);
        return zzf != null ? zzdf.zzI(zzf) : zzdf.zzzQ();
    }

    public boolean zzyh() {
        return true;
    }
}
