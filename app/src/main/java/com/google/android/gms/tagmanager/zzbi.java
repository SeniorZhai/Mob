package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzbi extends zzak {
    private static final String ID = zzad.LOWERCASE_STRING.toString();
    private static final String zzaLE = zzae.ARG0.toString();

    public zzbi() {
        super(ID, zzaLE);
    }

    public zza zzE(Map<String, zza> map) {
        return zzdf.zzI(zzdf.zzg((zza) map.get(zzaLE)).toLowerCase());
    }

    public boolean zzyh() {
        return true;
    }
}
