package com.google.android.gms.internal;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.internal.zzqf.zzg;
import com.google.android.gms.tagmanager.zzbg;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class zzqh {
    public static final Integer zzaPM = Integer.valueOf(0);
    public static final Integer zzaPN = Integer.valueOf(1);
    private final Context mContext;
    private final ExecutorService zzaNb;

    public zzqh(Context context) {
        this(context, Executors.newSingleThreadExecutor());
    }

    zzqh(Context context, ExecutorService executorService) {
        this.mContext = context;
        this.zzaNb = executorService;
    }

    private String zzfc(String str) {
        return "resource_" + str;
    }

    private byte[] zzm(InputStream inputStream) {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            zzlg.zza(inputStream, byteArrayOutputStream);
        } catch (IOException e) {
            zzbg.zzaC("Failed to read the resource from disk");
            return byteArrayOutputStream.toByteArray();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e2) {
                zzbg.zzaC("Error closing stream for reading resource from disk");
                return null;
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    public void zza(String str, Integer num, zzqb com_google_android_gms_internal_zzqb, zzqg com_google_android_gms_internal_zzqg) {
        final String str2 = str;
        final Integer num2 = num;
        final zzqb com_google_android_gms_internal_zzqb2 = com_google_android_gms_internal_zzqb;
        final zzqg com_google_android_gms_internal_zzqg2 = com_google_android_gms_internal_zzqg;
        this.zzaNb.execute(new Runnable(this) {
            final /* synthetic */ zzqh zzaPS;

            public void run() {
                this.zzaPS.zzb(str2, num2, com_google_android_gms_internal_zzqb2, com_google_android_gms_internal_zzqg2);
            }
        });
    }

    void zzb(String str, Integer num, zzqb com_google_android_gms_internal_zzqb, zzqg com_google_android_gms_internal_zzqg) {
        Object zzt;
        zzbg.zzaB("DiskLoader: Starting to load resource from Disk.");
        try {
            zzt = com_google_android_gms_internal_zzqb.zzt(zzm(new FileInputStream(zzfb(str))));
            if (zzt != null) {
                zzbg.zzaB("Saved resource loaded: " + zzfc(str));
                com_google_android_gms_internal_zzqg.zza(Status.zzXP, zzt, zzaPN, zzfa(str));
                return;
            }
        } catch (FileNotFoundException e) {
            zzbg.zzaz("Saved resource not found: " + zzfc(str));
        } catch (zzg e2) {
            zzbg.zzaz("Saved resource is corrupted: " + zzfc(str));
        }
        if (num == null) {
            com_google_android_gms_internal_zzqg.zza(Status.zzXR, null, null, 0);
            return;
        }
        try {
            InputStream openRawResource = this.mContext.getResources().openRawResource(num.intValue());
            if (openRawResource != null) {
                zzt = com_google_android_gms_internal_zzqb.zzt(zzm(openRawResource));
                if (zzt != null) {
                    zzbg.zzaB("Default resource loaded: " + this.mContext.getResources().getResourceEntryName(num.intValue()));
                    com_google_android_gms_internal_zzqg.zza(Status.zzXP, zzt, zzaPM, 0);
                    return;
                }
            }
        } catch (NotFoundException e3) {
            zzbg.zzaz("Default resource not found. ID: " + num);
        } catch (zzg e4) {
            zzbg.zzaz("Default resource resource is corrupted: " + num);
        }
        com_google_android_gms_internal_zzqg.zza(Status.zzXR, null, null, 0);
    }

    public void zze(final String str, final byte[] bArr) {
        this.zzaNb.execute(new Runnable(this) {
            final /* synthetic */ zzqh zzaPS;

            public void run() {
                this.zzaPS.zzf(str, bArr);
            }
        });
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    void zzf(java.lang.String r4, byte[] r5) {
        /*
        r3 = this;
        r0 = r3.zzfb(r4);
        r1 = new java.io.FileOutputStream;	 Catch:{ FileNotFoundException -> 0x002c }
        r1.<init>(r0);	 Catch:{ FileNotFoundException -> 0x002c }
        r1.write(r5);	 Catch:{ IOException -> 0x003a }
        r1.close();	 Catch:{ IOException -> 0x0033 }
        r0 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0033 }
        r0.<init>();	 Catch:{ IOException -> 0x0033 }
        r1 = "Resource ";
        r0 = r0.append(r1);	 Catch:{ IOException -> 0x0033 }
        r0 = r0.append(r4);	 Catch:{ IOException -> 0x0033 }
        r1 = " saved on Disk.";
        r0 = r0.append(r1);	 Catch:{ IOException -> 0x0033 }
        r0 = r0.toString();	 Catch:{ IOException -> 0x0033 }
        com.google.android.gms.tagmanager.zzbg.zzaB(r0);	 Catch:{ IOException -> 0x0033 }
    L_0x002b:
        return;
    L_0x002c:
        r0 = move-exception;
        r0 = "Error opening resource file for writing";
        com.google.android.gms.tagmanager.zzbg.zzaz(r0);
        goto L_0x002b;
    L_0x0033:
        r0 = move-exception;
        r0 = "Error closing stream for writing resource to disk";
        com.google.android.gms.tagmanager.zzbg.zzaz(r0);
        goto L_0x002b;
    L_0x003a:
        r2 = move-exception;
        r2 = "Error writing resource to disk. Removing resource from disk";
        com.google.android.gms.tagmanager.zzbg.zzaz(r2);	 Catch:{ all -> 0x006a }
        r0.delete();	 Catch:{ all -> 0x006a }
        r1.close();	 Catch:{ IOException -> 0x0063 }
        r0 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0063 }
        r0.<init>();	 Catch:{ IOException -> 0x0063 }
        r1 = "Resource ";
        r0 = r0.append(r1);	 Catch:{ IOException -> 0x0063 }
        r0 = r0.append(r4);	 Catch:{ IOException -> 0x0063 }
        r1 = " saved on Disk.";
        r0 = r0.append(r1);	 Catch:{ IOException -> 0x0063 }
        r0 = r0.toString();	 Catch:{ IOException -> 0x0063 }
        com.google.android.gms.tagmanager.zzbg.zzaB(r0);	 Catch:{ IOException -> 0x0063 }
        goto L_0x002b;
    L_0x0063:
        r0 = move-exception;
        r0 = "Error closing stream for writing resource to disk";
        com.google.android.gms.tagmanager.zzbg.zzaz(r0);
        goto L_0x002b;
    L_0x006a:
        r0 = move-exception;
        r1.close();	 Catch:{ IOException -> 0x008b }
        r1 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x008b }
        r1.<init>();	 Catch:{ IOException -> 0x008b }
        r2 = "Resource ";
        r1 = r1.append(r2);	 Catch:{ IOException -> 0x008b }
        r1 = r1.append(r4);	 Catch:{ IOException -> 0x008b }
        r2 = " saved on Disk.";
        r1 = r1.append(r2);	 Catch:{ IOException -> 0x008b }
        r1 = r1.toString();	 Catch:{ IOException -> 0x008b }
        com.google.android.gms.tagmanager.zzbg.zzaB(r1);	 Catch:{ IOException -> 0x008b }
    L_0x008a:
        throw r0;
    L_0x008b:
        r1 = move-exception;
        r1 = "Error closing stream for writing resource to disk";
        com.google.android.gms.tagmanager.zzbg.zzaz(r1);
        goto L_0x008a;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzqh.zzf(java.lang.String, byte[]):void");
    }

    public long zzfa(String str) {
        File zzfb = zzfb(str);
        return zzfb.exists() ? zzfb.lastModified() : 0;
    }

    File zzfb(String str) {
        return new File(this.mContext.getDir("google_tagmanager", 0), zzfc(str));
    }
}
