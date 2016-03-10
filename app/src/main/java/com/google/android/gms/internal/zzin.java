package com.google.android.gms.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public final class zzin extends zznq<zzin> {
    private Map<Integer, Double> zzJb = new HashMap(4);

    public String toString() {
        Map hashMap = new HashMap();
        for (Entry entry : this.zzJb.entrySet()) {
            hashMap.put("metric" + entry.getKey(), entry.getValue());
        }
        return zznq.zzy(hashMap);
    }

    public void zza(zzin com_google_android_gms_internal_zzin) {
        com_google_android_gms_internal_zzin.zzJb.putAll(this.zzJb);
    }

    public Map<Integer, Double> zzhu() {
        return Collections.unmodifiableMap(this.zzJb);
    }
}
