package com.google.android.gms.tagmanager;

import android.os.Build.VERSION;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzbx extends zzak {
    private static final String ID = zzad.OS_VERSION.toString();

    public zzbx() {
        super(ID, new String[0]);
    }

    public zza zzE(Map<String, zza> map) {
        return zzdf.zzI(VERSION.RELEASE);
    }

    public boolean zzyh() {
        return true;
    }
}
