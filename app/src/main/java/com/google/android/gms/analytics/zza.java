package com.google.android.gms.analytics;

import android.net.Uri;
import android.text.TextUtils;
import com.google.android.gms.analytics.internal.zzf;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzip;
import com.google.android.gms.internal.zzno;
import com.google.android.gms.internal.zznr;
import com.google.android.gms.internal.zznu;
import java.util.ListIterator;

public class zza extends zznr<zza> {
    private final zzf zzIa;
    private boolean zzIb;

    public zza(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf.zzhS(), com_google_android_gms_analytics_internal_zzf.zzhP());
        this.zzIa = com_google_android_gms_analytics_internal_zzf;
    }

    public void enableAdvertisingIdCollection(boolean enable) {
        this.zzIb = enable;
    }

    protected void zza(zzno com_google_android_gms_internal_zzno) {
        zzip com_google_android_gms_internal_zzip = (zzip) com_google_android_gms_internal_zzno.zze(zzip.class);
        if (TextUtils.isEmpty(com_google_android_gms_internal_zzip.getClientId())) {
            com_google_android_gms_internal_zzip.setClientId(this.zzIa.zzih().zziP());
        }
        if (this.zzIb && TextUtils.isEmpty(com_google_android_gms_internal_zzip.zzhx())) {
            com.google.android.gms.analytics.internal.zza zzig = this.zzIa.zzig();
            com_google_android_gms_internal_zzip.zzaO(zzig.zzhC());
            com_google_android_gms_internal_zzip.zzE(zzig.zzhy());
        }
    }

    public void zzaI(String str) {
        zzu.zzcj(str);
        zzaJ(str);
        zzwb().add(new zzb(this.zzIa, str));
    }

    public void zzaJ(String str) {
        Uri zzaK = zzb.zzaK(str);
        ListIterator listIterator = zzwb().listIterator();
        while (listIterator.hasNext()) {
            if (zzaK.equals(((zznu) listIterator.next()).zzhe())) {
                listIterator.remove();
            }
        }
    }

    zzf zzhb() {
        return this.zzIa;
    }

    public zzno zzhc() {
        zzno zzvP = zzwa().zzvP();
        zzvP.zzb(this.zzIa.zzhX().zzix());
        zzvP.zzb(this.zzIa.zzhY().zzjE());
        zzd(zzvP);
        return zzvP;
    }
}
