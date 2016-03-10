package com.google.android.gms.ads.identifier;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.common.stats.zzb;
import com.google.android.gms.internal.zzav;
import com.mobcrush.mobcrush.Constants;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AdvertisingIdClient {
    private final Context mContext;
    com.google.android.gms.common.zza zznX;
    zzav zznY;
    boolean zznZ;
    Object zzoa;
    zza zzob;
    final long zzoc;

    public static final class Info {
        private final String zzoh;
        private final boolean zzoi;

        public Info(String advertisingId, boolean limitAdTrackingEnabled) {
            this.zzoh = advertisingId;
            this.zzoi = limitAdTrackingEnabled;
        }

        public String getId() {
            return this.zzoh;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.zzoi;
        }

        public String toString() {
            return "{" + this.zzoh + "}" + this.zzoi;
        }
    }

    static class zza extends Thread {
        private WeakReference<AdvertisingIdClient> zzod;
        private long zzoe;
        CountDownLatch zzof = new CountDownLatch(1);
        boolean zzog = false;

        public zza(AdvertisingIdClient advertisingIdClient, long j) {
            this.zzod = new WeakReference(advertisingIdClient);
            this.zzoe = j;
            start();
        }

        private void disconnect() {
            AdvertisingIdClient advertisingIdClient = (AdvertisingIdClient) this.zzod.get();
            if (advertisingIdClient != null) {
                advertisingIdClient.finish();
                this.zzog = true;
            }
        }

        public void cancel() {
            this.zzof.countDown();
        }

        public void run() {
            try {
                if (!this.zzof.await(this.zzoe, TimeUnit.MILLISECONDS)) {
                    disconnect();
                }
            } catch (InterruptedException e) {
                disconnect();
            }
        }

        public boolean zzaK() {
            return this.zzog;
        }
    }

    public AdvertisingIdClient(Context context) {
        this(context, Constants.BROADCASTS_LIVE_TIME);
    }

    public AdvertisingIdClient(Context context, long timeoutInMillis) {
        this.zzoa = new Object();
        zzu.zzu(context);
        this.mContext = context;
        this.zznZ = false;
        this.zzoc = timeoutInMillis;
    }

    public static Info getAdvertisingIdInfo(Context context) throws IOException, IllegalStateException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        AdvertisingIdClient advertisingIdClient = new AdvertisingIdClient(context, -1);
        try {
            advertisingIdClient.zzb(false);
            Info info = advertisingIdClient.getInfo();
            return info;
        } finally {
            advertisingIdClient.finish();
        }
    }

    static zzav zza(Context context, com.google.android.gms.common.zza com_google_android_gms_common_zza) throws IOException {
        try {
            return com.google.android.gms.internal.zzav.zza.zzb(com_google_android_gms_common_zza.zzmh());
        } catch (InterruptedException e) {
            throw new IOException("Interrupted exception");
        }
    }

    private void zzaJ() {
        synchronized (this.zzoa) {
            if (this.zzob != null) {
                this.zzob.cancel();
                try {
                    this.zzob.join();
                } catch (InterruptedException e) {
                }
            }
            if (this.zzoc > 0) {
                this.zzob = new zza(this, this.zzoc);
            }
        }
    }

    static com.google.android.gms.common.zza zzo(Context context) throws IOException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        try {
            context.getPackageManager().getPackageInfo(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE, 0);
            try {
                GooglePlayServicesUtil.zzY(context);
                ServiceConnection com_google_android_gms_common_zza = new com.google.android.gms.common.zza();
                Intent intent = new Intent(AdvertisingInfoServiceStrategy.GOOGLE_PLAY_SERVICES_INTENT);
                intent.setPackage(AdvertisingInfoServiceStrategy.GOOGLE_PLAY_SERVICES_INTENT_PACKAGE_NAME);
                if (zzb.zzoO().zza(context, intent, com_google_android_gms_common_zza, 1)) {
                    return com_google_android_gms_common_zza;
                }
                throw new IOException("Connection failure");
            } catch (Throwable e) {
                throw new IOException(e);
            }
        } catch (NameNotFoundException e2) {
            throw new GooglePlayServicesNotAvailableException(9);
        }
    }

    protected void finalize() throws Throwable {
        finish();
        super.finalize();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void finish() {
        /*
        r3 = this;
        r0 = "Calling this from your main thread can lead to deadlock";
        com.google.android.gms.common.internal.zzu.zzbZ(r0);
        monitor-enter(r3);
        r0 = r3.mContext;	 Catch:{ all -> 0x002a }
        if (r0 == 0) goto L_0x000e;
    L_0x000a:
        r0 = r3.zznX;	 Catch:{ all -> 0x002a }
        if (r0 != 0) goto L_0x0010;
    L_0x000e:
        monitor-exit(r3);	 Catch:{ all -> 0x002a }
    L_0x000f:
        return;
    L_0x0010:
        r0 = r3.zznZ;	 Catch:{ IllegalArgumentException -> 0x002d }
        if (r0 == 0) goto L_0x001f;
    L_0x0014:
        r0 = com.google.android.gms.common.stats.zzb.zzoO();	 Catch:{ IllegalArgumentException -> 0x002d }
        r1 = r3.mContext;	 Catch:{ IllegalArgumentException -> 0x002d }
        r2 = r3.zznX;	 Catch:{ IllegalArgumentException -> 0x002d }
        r0.zza(r1, r2);	 Catch:{ IllegalArgumentException -> 0x002d }
    L_0x001f:
        r0 = 0;
        r3.zznZ = r0;	 Catch:{ all -> 0x002a }
        r0 = 0;
        r3.zznY = r0;	 Catch:{ all -> 0x002a }
        r0 = 0;
        r3.zznX = r0;	 Catch:{ all -> 0x002a }
        monitor-exit(r3);	 Catch:{ all -> 0x002a }
        goto L_0x000f;
    L_0x002a:
        r0 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x002a }
        throw r0;
    L_0x002d:
        r0 = move-exception;
        r1 = "AdvertisingIdClient";
        r2 = "AdvertisingIdClient unbindService failed.";
        android.util.Log.i(r1, r2, r0);	 Catch:{ all -> 0x002a }
        goto L_0x001f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.ads.identifier.AdvertisingIdClient.finish():void");
    }

    public Info getInfo() throws IOException {
        Info info;
        zzu.zzbZ("Calling this from your main thread can lead to deadlock");
        synchronized (this) {
            if (!this.zznZ) {
                synchronized (this.zzoa) {
                    if (this.zzob == null || !this.zzob.zzaK()) {
                        throw new IOException("AdvertisingIdClient is not connected.");
                    }
                }
                try {
                    zzb(false);
                    if (!this.zznZ) {
                        throw new IOException("AdvertisingIdClient cannot reconnect.");
                    }
                } catch (Throwable e) {
                    Log.i("AdvertisingIdClient", "GMS remote exception ", e);
                    throw new IOException("Remote exception");
                } catch (Throwable e2) {
                    throw new IOException("AdvertisingIdClient cannot reconnect.", e2);
                }
            }
            zzu.zzu(this.zznX);
            zzu.zzu(this.zznY);
            info = new Info(this.zznY.getId(), this.zznY.zzc(true));
        }
        zzaJ();
        return info;
    }

    public void start() throws IOException, IllegalStateException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        zzb(true);
    }

    protected void zzb(boolean z) throws IOException, IllegalStateException, GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        zzu.zzbZ("Calling this from your main thread can lead to deadlock");
        synchronized (this) {
            if (this.zznZ) {
                finish();
            }
            this.zznX = zzo(this.mContext);
            this.zznY = zza(this.mContext, this.zznX);
            this.zznZ = true;
            if (z) {
                zzaJ();
            }
        }
    }
}
