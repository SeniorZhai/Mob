package com.google.android.gms.internal;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import com.google.android.gms.common.internal.zze;
import com.google.android.gms.playlog.internal.LogEvent;
import com.google.android.gms.playlog.internal.PlayLoggerContext;
import com.google.android.gms.playlog.internal.zzd;
import com.google.android.gms.playlog.internal.zzf;

@Deprecated
public class zzoz {
    private final zzf zzaGD;
    private PlayLoggerContext zzaGE;

    public interface zza {
        void zzf(PendingIntent pendingIntent);

        void zzxl();

        void zzxm();
    }

    public zzoz(Context context, int i, String str, String str2, zza com_google_android_gms_internal_zzoz_zza, boolean z, String str3) {
        String packageName = context.getPackageName();
        int i2 = 0;
        try {
            i2 = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
        } catch (NameNotFoundException e) {
            Log.wtf("PlayLogger", "This can't happen.");
        }
        this.zzaGE = new PlayLoggerContext(packageName, i2, i, str, str2, z);
        this.zzaGD = new zzf(context, context.getMainLooper(), new zzd(com_google_android_gms_internal_zzoz_zza), new zze(null, null, null, 49, null, packageName, str3, null));
    }

    public void start() {
        this.zzaGD.start();
    }

    public void stop() {
        this.zzaGD.stop();
    }

    public void zza(long j, String str, byte[] bArr, String... strArr) {
        this.zzaGD.zzb(this.zzaGE, new LogEvent(j, str, bArr, strArr));
    }

    public void zzb(String str, byte[] bArr, String... strArr) {
        zza(System.currentTimeMillis(), str, bArr, strArr);
    }
}
