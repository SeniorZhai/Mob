package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzdh extends zzak {
    private static final String ID = zzad.UPPERCASE_STRING.toString();
    private static final String zzaLE = zzae.ARG0.toString();

    public zzdh() {
        super(ID, zzaLE);
    }

    public zza zzE(Map<String, zza> map) {
        return zzdf.zzI(zzdf.zzg((zza) map.get(zzaLE)).toUpperCase());
    }

    public boolean zzyh() {
        return true;
    }
}
