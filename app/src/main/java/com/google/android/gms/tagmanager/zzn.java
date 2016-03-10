package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzn extends zzak {
    private static final String ID = zzad.CONSTANT.toString();
    private static final String VALUE = zzae.VALUE.toString();

    public zzn() {
        super(ID, VALUE);
    }

    public static String zzyk() {
        return ID;
    }

    public static String zzyl() {
        return VALUE;
    }

    public zza zzE(Map<String, zza> map) {
        return (zza) map.get(VALUE);
    }

    public boolean zzyh() {
        return true;
    }
}
