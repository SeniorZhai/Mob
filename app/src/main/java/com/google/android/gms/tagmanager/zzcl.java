package com.google.android.gms.tagmanager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.android.gms.internal.zzaf.zzj;
import com.google.android.gms.internal.zzqf;
import com.google.android.gms.internal.zzql;
import com.google.android.gms.internal.zzqm;
import com.google.android.gms.tagmanager.zzbf.zza;
import io.fabric.sdk.android.BuildConfig;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

class zzcl implements Runnable {
    private final Context mContext;
    private volatile String zzaKV;
    private final String zzaKy;
    private final zzqm zzaMS;
    private final String zzaMT;
    private zzbf<zzj> zzaMU;
    private volatile zzs zzaMV;
    private volatile String zzaMW;

    zzcl(Context context, String str, zzqm com_google_android_gms_internal_zzqm, zzs com_google_android_gms_tagmanager_zzs) {
        this.mContext = context;
        this.zzaMS = com_google_android_gms_internal_zzqm;
        this.zzaKy = str;
        this.zzaMV = com_google_android_gms_tagmanager_zzs;
        this.zzaMT = "/r?id=" + str;
        this.zzaKV = this.zzaMT;
        this.zzaMW = null;
    }

    public zzcl(Context context, String str, zzs com_google_android_gms_tagmanager_zzs) {
        this(context, str, new zzqm(), com_google_android_gms_tagmanager_zzs);
    }

    private boolean zzzi() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
            return true;
        }
        zzbg.zzaB("...no network connectivity");
        return false;
    }

    private void zzzj() {
        if (zzzi()) {
            zzbg.zzaB("Start loading resource from network ...");
            String zzzk = zzzk();
            zzql zzAG = this.zzaMS.zzAG();
            try {
                InputStream zzfd = zzAG.zzfd(zzzk);
                try {
                    OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    zzqf.zzc(zzfd, byteArrayOutputStream);
                    zzj zzd = zzj.zzd(byteArrayOutputStream.toByteArray());
                    zzbg.zzaB("Successfully loaded supplemented resource: " + zzd);
                    if (zzd.zziO == null && zzd.zziN.length == 0) {
                        zzbg.zzaB("No change for container: " + this.zzaKy);
                    }
                    this.zzaMU.zzz(zzd);
                    zzbg.zzaB("Load resource from network finished.");
                } catch (Throwable e) {
                    zzbg.zzd("Error when parsing downloaded resources from url: " + zzzk + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + e.getMessage(), e);
                    this.zzaMU.zza(zza.SERVER_ERROR);
                    zzAG.close();
                }
            } catch (FileNotFoundException e2) {
                zzbg.zzaC("No data is retrieved from the given url: " + zzzk + ". Make sure container_id: " + this.zzaKy + " is correct.");
                this.zzaMU.zza(zza.SERVER_ERROR);
            } catch (Throwable e3) {
                zzbg.zzd("Error when loading resources from url: " + zzzk + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + e3.getMessage(), e3);
                this.zzaMU.zza(zza.IO_ERROR);
            } finally {
                zzAG.close();
            }
        } else {
            this.zzaMU.zza(zza.NOT_AVAILABLE);
        }
    }

    public void run() {
        if (this.zzaMU == null) {
            throw new IllegalStateException("callback must be set before execute");
        }
        this.zzaMU.zzyv();
        zzzj();
    }

    void zza(zzbf<zzj> com_google_android_gms_tagmanager_zzbf_com_google_android_gms_internal_zzaf_zzj) {
        this.zzaMU = com_google_android_gms_tagmanager_zzbf_com_google_android_gms_internal_zzaf_zzj;
    }

    void zzeB(String str) {
        zzbg.zzay("Setting previous container version: " + str);
        this.zzaMW = str;
    }

    void zzem(String str) {
        if (str == null) {
            this.zzaKV = this.zzaMT;
            return;
        }
        zzbg.zzay("Setting CTFE URL path: " + str);
        this.zzaKV = str;
    }

    String zzzk() {
        String str = this.zzaMV.zzyx() + this.zzaKV + "&v=a65833898";
        if (!(this.zzaMW == null || this.zzaMW.trim().equals(BuildConfig.FLAVOR))) {
            str = str + "&pv=" + this.zzaMW;
        }
        return zzcb.zzzf().zzzg().equals(zza.CONTAINER_DEBUG) ? str + "&gtm_debug=x" : str;
    }
}
