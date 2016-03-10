package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzq extends zzak {
    private static final String ID = zzad.CONTAINER_VERSION.toString();
    private final String zzTQ;

    public zzq(String str) {
        super(ID, new String[0]);
        this.zzTQ = str;
    }

    public zza zzE(Map<String, zza> map) {
        return this.zzTQ == null ? zzdf.zzzQ() : zzdf.zzI(this.zzTQ);
    }

    public boolean zzyh() {
        return true;
    }
}
