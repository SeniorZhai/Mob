package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

public class zzaf extends zzcz {
    private static final String ID = zzad.EQUALS.toString();

    public zzaf() {
        super(ID);
    }

    protected boolean zza(String str, String str2, Map<String, zza> map) {
        return str.equals(str2);
    }
}
