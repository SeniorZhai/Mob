package com.google.android.gms.tagmanager;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzag.zza;
import com.helpshift.HSFunnel;
import java.util.Map;

class zzcg extends zzak {
    private static final String ID = zzad.RESOLUTION.toString();
    private final Context mContext;

    public zzcg(Context context) {
        super(ID, new String[0]);
        this.mContext = context;
    }

    public zza zzE(Map<String, zza> map) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMetrics(displayMetrics);
        int i = displayMetrics.widthPixels;
        return zzdf.zzI(i + HSFunnel.OPEN_INBOX + displayMetrics.heightPixels);
    }

    public boolean zzyh() {
        return true;
    }
}
