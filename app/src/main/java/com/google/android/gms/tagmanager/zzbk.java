package com.google.android.gms.tagmanager;

import android.content.Context;
import android.provider.Settings.Secure;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import java.util.Map;

class zzbk extends zzak {
    private static final String ID = zzad.MOBILE_ADWORDS_UNIQUE_ID.toString();
    private final Context mContext;

    public zzbk(Context context) {
        super(ID, new String[0]);
        this.mContext = context;
    }

    public zza zzE(Map<String, zza> map) {
        String zzaG = zzaG(this.mContext);
        return zzaG == null ? zzdf.zzzQ() : zzdf.zzI(zzaG);
    }

    protected String zzaG(Context context) {
        return Secure.getString(context.getContentResolver(), "android_id");
    }

    public boolean zzyh() {
        return true;
    }
}
