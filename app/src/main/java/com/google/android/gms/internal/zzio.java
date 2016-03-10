package com.google.android.gms.internal;

import com.google.android.gms.common.internal.zzu;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class zzio extends zznq<zzio> {
    private final Map<String, Object> zzyn = new HashMap();

    private String zzaM(String str) {
        zzu.zzcj(str);
        if (str != null && str.startsWith("&")) {
            str = str.substring(1);
        }
        zzu.zzh(str, "Name can not be empty or \"&\"");
        return str;
    }

    public void set(String name, String value) {
        this.zzyn.put(zzaM(name), value);
    }

    public String toString() {
        return zznq.zzy(this.zzyn);
    }

    public void zza(zzio com_google_android_gms_internal_zzio) {
        zzu.zzu(com_google_android_gms_internal_zzio);
        com_google_android_gms_internal_zzio.zzyn.putAll(this.zzyn);
    }

    public Map<String, Object> zzhv() {
        return Collections.unmodifiableMap(this.zzyn);
    }
}
