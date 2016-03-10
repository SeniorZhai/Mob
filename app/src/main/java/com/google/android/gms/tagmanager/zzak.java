package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzag.zza;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

abstract class zzak {
    private final Set<String> zzaLI;
    private final String zzaLJ;

    public zzak(String str, String... strArr) {
        this.zzaLJ = str;
        this.zzaLI = new HashSet(strArr.length);
        for (Object add : strArr) {
            this.zzaLI.add(add);
        }
    }

    public abstract zza zzE(Map<String, zza> map);

    boolean zzg(Set<String> set) {
        return set.containsAll(this.zzaLI);
    }

    public String zzyM() {
        return this.zzaLJ;
    }

    public Set<String> zzyN() {
        return this.zzaLI;
    }

    public abstract boolean zzyh();
}
