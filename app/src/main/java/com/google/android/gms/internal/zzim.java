package com.google.android.gms.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class zzim extends zznq<zzim> {
    private Map<Integer, String> zzJa = new HashMap(4);

    public String toString() {
        Map hashMap = new HashMap();
        for (Entry entry : this.zzJa.entrySet()) {
            hashMap.put("dimension" + entry.getKey(), entry.getValue());
        }
        return zznq.zzy(hashMap);
    }

    public void zza(zzim com_google_android_gms_internal_zzim) {
        com_google_android_gms_internal_zzim.zzJa.putAll(this.zzJa);
    }

    public Map<Integer, String> zzht() {
        return Collections.unmodifiableMap(this.zzJa);
    }
}
