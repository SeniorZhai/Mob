package com.google.android.gms.tagmanager;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzh extends zzak {
    private static final String ID = zzad.APP_VERSION.toString();
    private final Context mContext;

    public zzh(Context context) {
        super(ID, new String[0]);
        this.mContext = context;
    }

    public zza zzE(Map<String, zza> map) {
        try {
            return zzdf.zzI(Integer.valueOf(this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0).versionCode));
        } catch (NameNotFoundException e) {
            zzbg.zzaz("Package name " + this.mContext.getPackageName() + " not found. " + e.getMessage());
            return zzdf.zzzQ();
        }
    }

    public boolean zzyh() {
        return true;
    }
}
