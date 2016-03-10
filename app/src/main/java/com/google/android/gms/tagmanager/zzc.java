package com.google.android.gms.tagmanager;

import android.content.Context;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzc extends zzak {
    private static final String ID = zzad.ADVERTISING_TRACKING_ENABLED.toString();
    private final zza zzaKo;

    public zzc(Context context) {
        this(zza.zzaE(context));
    }

    zzc(zza com_google_android_gms_tagmanager_zza) {
        super(ID, new String[0]);
        this.zzaKo = com_google_android_gms_tagmanager_zza;
    }

    public zza zzE(Map<String, zza> map) {
        return zzdf.zzI(Boolean.valueOf(!this.zzaKo.isLimitAdTrackingEnabled()));
    }

    public boolean zzyh() {
        return false;
    }
}
