package com.google.android.gms.tagmanager;

import android.content.Context;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzb extends zzak {
    private static final String ID = zzad.ADVERTISER_ID.toString();
    private final zza zzaKo;

    public zzb(Context context) {
        this(zza.zzaE(context));
    }

    zzb(zza com_google_android_gms_tagmanager_zza) {
        super(ID, new String[0]);
        this.zzaKo = com_google_android_gms_tagmanager_zza;
    }

    public zza zzE(Map<String, zza> map) {
        String zzyd = this.zzaKo.zzyd();
        return zzyd == null ? zzdf.zzzQ() : zzdf.zzI(zzyd);
    }

    public boolean zzyh() {
        return false;
    }
}
