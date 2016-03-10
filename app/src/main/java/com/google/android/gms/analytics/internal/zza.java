package com.google.android.gms.analytics.internal;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Locale;

public class zza extends zzd {
    public static boolean zzJk;
    private Info zzJl;
    private final zzaj zzJm;
    private String zzJn;
    private boolean zzJo = false;
    private Object zzJp = new Object();

    zza(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf);
        this.zzJm = new zzaj(com_google_android_gms_analytics_internal_zzf.zzhP());
    }

    private boolean zza(Info info, Info info2) {
        String str = null;
        String id = info2 == null ? null : info2.getId();
        if (TextUtils.isEmpty(id)) {
            return true;
        }
        String zziP = zzhV().zziP();
        synchronized (this.zzJp) {
            if (!this.zzJo) {
                this.zzJn = zzhF();
                this.zzJo = true;
            } else if (TextUtils.isEmpty(this.zzJn)) {
                if (info != null) {
                    str = info.getId();
                }
                if (str == null) {
                    boolean zzaS = zzaS(id + zziP);
                    return zzaS;
                }
                this.zzJn = zzaR(str + zziP);
            }
            Object zzaR = zzaR(id + zziP);
            if (TextUtils.isEmpty(zzaR)) {
                return false;
            } else if (zzaR.equals(this.zzJn)) {
                return true;
            } else {
                if (TextUtils.isEmpty(this.zzJn)) {
                    str = zziP;
                } else {
                    zzaT("Resetting the client id because Advertising Id changed.");
                    str = zzhV().zziQ();
                    zza("New client Id", str);
                }
                zzaS = zzaS(id + str);
                return zzaS;
            }
        }
    }

    private static String zzaR(String str) {
        if (zzam.zzbl(CommonUtils.MD5_INSTANCE) == null) {
            return null;
        }
        return String.format(Locale.US, "%032X", new Object[]{new BigInteger(1, zzam.zzbl(CommonUtils.MD5_INSTANCE).digest(str.getBytes()))});
    }

    private boolean zzaS(String str) {
        try {
            String zzaR = zzaR(str);
            zzaT("Storing hashed adid.");
            FileOutputStream openFileOutput = getContext().openFileOutput("gaClientIdData", 0);
            openFileOutput.write(zzaR.getBytes());
            openFileOutput.close();
            this.zzJn = zzaR;
            return true;
        } catch (IOException e) {
            zze("Error creating hash file", e);
            return false;
        }
    }

    private synchronized Info zzhD() {
        if (this.zzJm.zzv(1000)) {
            this.zzJm.start();
            Info zzhE = zzhE();
            if (zza(this.zzJl, zzhE)) {
                this.zzJl = zzhE;
            } else {
                zzaX("Failed to reset client id on adid change. Not using adid");
                this.zzJl = new Info(BuildConfig.FLAVOR, false);
            }
        }
        return this.zzJl;
    }

    public String zzhC() {
        zzia();
        Info zzhD = zzhD();
        CharSequence id = zzhD != null ? zzhD.getId() : null;
        return TextUtils.isEmpty(id) ? null : id;
    }

    protected Info zzhE() {
        Info info = null;
        try {
            info = AdvertisingIdClient.getAdvertisingIdInfo(getContext());
        } catch (IllegalStateException e) {
            zzaW("IllegalStateException getting Ad Id Info. If you would like to see Audience reports, please ensure that you have added '<meta-data android:name=\"com.google.android.gms.version\" android:value=\"@integer/google_play_services_version\" />' to your application manifest file. See http://goo.gl/naFqQk for details.");
        } catch (Throwable th) {
            if (!zzJk) {
                zzJk = true;
                zzd("Error getting advertiser id", th);
            }
        }
        return info;
    }

    protected String zzhF() {
        Object obj;
        String str = null;
        try {
            FileInputStream openFileInput = getContext().openFileInput("gaClientIdData");
            byte[] bArr = new byte[AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS];
            int read = openFileInput.read(bArr, 0, AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            if (openFileInput.available() > 0) {
                zzaW("Hash file seems corrupted, deleting it.");
                openFileInput.close();
                getContext().deleteFile("gaClientIdData");
                return null;
            } else if (read <= 0) {
                zzaT("Hash file is empty.");
                openFileInput.close();
                return null;
            } else {
                String str2 = new String(bArr, 0, read);
                try {
                    openFileInput.close();
                    return str2;
                } catch (FileNotFoundException e) {
                    return str2;
                } catch (IOException e2) {
                    IOException iOException = e2;
                    str = str2;
                    IOException iOException2 = iOException;
                    zzd("Error reading Hash file, deleting it", obj);
                    getContext().deleteFile("gaClientIdData");
                    return str;
                }
            }
        } catch (FileNotFoundException e3) {
            return null;
        } catch (IOException e4) {
            obj = e4;
            zzd("Error reading Hash file, deleting it", obj);
            getContext().deleteFile("gaClientIdData");
            return str;
        }
    }

    protected void zzhn() {
    }

    public boolean zzhy() {
        zzia();
        Info zzhD = zzhD();
        return (zzhD == null || zzhD.isLimitAdTrackingEnabled()) ? false : true;
    }
}
