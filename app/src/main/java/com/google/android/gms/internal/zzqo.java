package com.google.android.gms.internal;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzqn.zza;
import com.google.android.gms.tagmanager.zzbg;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

public class zzqo implements Runnable {
    private final Context mContext;
    private final zzqm zzaMS;
    private final zzqd zzaPU;
    private final zzqn zzaQb;
    private final zzqi zzaQc;

    public zzqo(Context context, zzqd com_google_android_gms_internal_zzqd, zzqn com_google_android_gms_internal_zzqn) {
        this(context, com_google_android_gms_internal_zzqd, com_google_android_gms_internal_zzqn, new zzqm(), new zzqi());
    }

    zzqo(Context context, zzqd com_google_android_gms_internal_zzqd, zzqn com_google_android_gms_internal_zzqn, zzqm com_google_android_gms_internal_zzqm, zzqi com_google_android_gms_internal_zzqi) {
        zzu.zzu(context);
        zzu.zzu(com_google_android_gms_internal_zzqn);
        this.mContext = context;
        this.zzaPU = com_google_android_gms_internal_zzqd;
        this.zzaQb = com_google_android_gms_internal_zzqn;
        this.zzaMS = com_google_android_gms_internal_zzqm;
        this.zzaQc = com_google_android_gms_internal_zzqi;
    }

    public zzqo(Context context, zzqd com_google_android_gms_internal_zzqd, zzqn com_google_android_gms_internal_zzqn, String str) {
        this(context, com_google_android_gms_internal_zzqd, com_google_android_gms_internal_zzqn, new zzqm(), new zzqi());
        this.zzaQc.zzeU(str);
    }

    public void run() {
        zzeH();
    }

    boolean zzAI() {
        if (!zzba("android.permission.INTERNET")) {
            zzbg.zzaz("Missing android.permission.INTERNET. Please add the following declaration to your AndroidManifest.xml: <uses-permission android:name=\"android.permission.INTERNET\" />");
            return false;
        } else if (zzba("android.permission.ACCESS_NETWORK_STATE")) {
            NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.mContext.getSystemService("connectivity")).getActiveNetworkInfo();
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                return true;
            }
            zzbg.zzaC("NetworkLoader: No network connectivity - Offline");
            return false;
        } else {
            zzbg.zzaz("Missing android.permission.ACCESS_NETWORK_STATE. Please add the following declaration to your AndroidManifest.xml: <uses-permission android:name=\"android.permission.ACCESS_NETWORK_STATE\" />");
            return false;
        }
    }

    boolean zzba(String str) {
        return this.mContext.getPackageManager().checkPermission(str, this.mContext.getPackageName()) == 0;
    }

    void zzeH() {
        String zzt;
        if (zzAI()) {
            zzbg.zzaB("NetworkLoader: Starting to load resource from Network.");
            zzql zzAG = this.zzaMS.zzAG();
            try {
                zzt = this.zzaQc.zzt(this.zzaPU.zzAf());
                InputStream zzfd = zzAG.zzfd(zzt);
                try {
                    OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    zzlg.zza(zzfd, byteArrayOutputStream);
                    this.zzaQb.zzu(byteArrayOutputStream.toByteArray());
                    zzbg.zzaB("NetworkLoader: Resource loaded.");
                } catch (Throwable e) {
                    zzbg.zzb("NetworkLoader: Error when parsing downloaded resources from url: " + zzt + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + e.getMessage(), e);
                    this.zzaQb.zza(zza.SERVER_ERROR);
                    zzAG.close();
                }
            } catch (FileNotFoundException e2) {
                zzbg.zzaz("NetworkLoader: No data is retrieved from the given url: " + zzt);
                this.zzaQb.zza(zza.SERVER_ERROR);
            } catch (Throwable e3) {
                zzbg.zzb("NetworkLoader: Error when loading resource from url: " + zzt + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + e3.getMessage(), e3);
                this.zzaQb.zza(zza.IO_ERROR);
            } finally {
                zzAG.close();
            }
        } else {
            this.zzaQb.zza(zza.NOT_AVAILABLE);
        }
    }
}
