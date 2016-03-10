package com.google.android.gms.tagmanager;

import android.content.Context;
import java.net.URLEncoder;
import org.apache.http.protocol.HTTP;

class zzz implements zzar {
    private static final Object zzaKl = new Object();
    private static zzz zzaLA;
    private zzcd zzaKO;
    private String zzaLB;
    private String zzaLC;
    private zzas zzaLD;

    private zzz(Context context) {
        this(zzat.zzaH(context), new zzcs());
    }

    zzz(zzas com_google_android_gms_tagmanager_zzas, zzcd com_google_android_gms_tagmanager_zzcd) {
        this.zzaLD = com_google_android_gms_tagmanager_zzas;
        this.zzaKO = com_google_android_gms_tagmanager_zzcd;
    }

    public static zzar zzaF(Context context) {
        zzar com_google_android_gms_tagmanager_zzar;
        synchronized (zzaKl) {
            if (zzaLA == null) {
                zzaLA = new zzz(context);
            }
            com_google_android_gms_tagmanager_zzar = zzaLA;
        }
        return com_google_android_gms_tagmanager_zzar;
    }

    public boolean zzes(String str) {
        if (this.zzaKO.zzkb()) {
            if (!(this.zzaLB == null || this.zzaLC == null)) {
                try {
                    str = this.zzaLB + "?" + this.zzaLC + "=" + URLEncoder.encode(str, HTTP.UTF_8);
                    zzbg.zzaB("Sending wrapped url hit: " + str);
                } catch (Throwable e) {
                    zzbg.zzd("Error wrapping URL for testing.", e);
                    return false;
                }
            }
            this.zzaLD.zzew(str);
            return true;
        }
        zzbg.zzaC("Too many urls sent too quickly with the TagManagerSender, rate limiting invoked.");
        return false;
    }
}
