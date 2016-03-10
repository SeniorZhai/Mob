package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

class zzcf extends zzcz {
    private static final String ID = zzad.REGEX.toString();
    private static final String zzaMQ = zzae.IGNORE_CASE.toString();

    public zzcf() {
        super(ID);
    }

    protected boolean zza(String str, String str2, Map<String, zza> map) {
        try {
            return Pattern.compile(str2, zzdf.zzk((zza) map.get(zzaMQ)).booleanValue() ? 66 : 64).matcher(str).find();
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
}
