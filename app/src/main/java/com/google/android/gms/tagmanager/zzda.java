package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzda extends zzak {
    private static final String ID = zzad.TIME.toString();

    public zzda() {
        super(ID, new String[0]);
    }

    public zza zzE(Map<String, zza> map) {
        return zzdf.zzI(Long.valueOf(System.currentTimeMillis()));
    }

    public boolean zzyh() {
        return false;
    }
}
