package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzcy extends zzcz {
    private static final String ID = zzad.STARTS_WITH.toString();

    public zzcy() {
        super(ID);
    }

    protected boolean zza(String str, String str2, Map<String, zza> map) {
        return str.startsWith(str2);
    }
}
