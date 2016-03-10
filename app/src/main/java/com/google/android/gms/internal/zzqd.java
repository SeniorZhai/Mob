package com.google.android.gms.internal;

import com.google.android.gms.common.internal.zzu;
import java.util.ArrayList;
import java.util.List;

public class zzqd {
    private final List<zzpy> zzaPn = new ArrayList();

    public String getId() {
        StringBuilder stringBuilder = new StringBuilder();
        Object obj = 1;
        for (zzpy com_google_android_gms_internal_zzpy : this.zzaPn) {
            if (obj != null) {
                obj = null;
            } else {
                stringBuilder.append("#");
            }
            stringBuilder.append(com_google_android_gms_internal_zzpy.getContainerId());
        }
        return stringBuilder.toString();
    }

    public List<zzpy> zzAf() {
        return this.zzaPn;
    }

    public zzqd zzb(zzpy com_google_android_gms_internal_zzpy) throws IllegalArgumentException {
        zzu.zzu(com_google_android_gms_internal_zzpy);
        for (zzpy containerId : this.zzaPn) {
            if (containerId.getContainerId().equals(com_google_android_gms_internal_zzpy.getContainerId())) {
                throw new IllegalArgumentException("The container is already being requested. " + com_google_android_gms_internal_zzpy.getContainerId());
            }
        }
        this.zzaPn.add(com_google_android_gms_internal_zzpy);
        return this;
    }
}
