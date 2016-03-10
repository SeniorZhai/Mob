package com.google.android.gms.internal;

import android.content.Context;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class zzam extends zzal {
    private static AdvertisingIdClient zznn = null;
    private static CountDownLatch zzno = new CountDownLatch(1);
    private static boolean zznp;
    private boolean zznq;

    class zza {
        private String zznr;
        private boolean zzns;
        final /* synthetic */ zzam zznt;

        public zza(zzam com_google_android_gms_internal_zzam, String str, boolean z) {
            this.zznt = com_google_android_gms_internal_zzam;
            this.zznr = str;
            this.zzns = z;
        }

        public String getId() {
            return this.zznr;
        }

        public boolean isLimitAdTrackingEnabled() {
            return this.zzns;
        }
    }

    private static final class zzb implements Runnable {
        private Context zznu;

        public zzb(Context context) {
            this.zznu = context.getApplicationContext();
            if (this.zznu == null) {
                this.zznu = context;
            }
        }

        public void run() {
            try {
                AdvertisingIdClient advertisingIdClient = new AdvertisingIdClient(this.zznu);
                advertisingIdClient.start();
                synchronized (zzam.class) {
                    if (zzam.zznn == null) {
                        zzam.zznn = advertisingIdClient;
                    } else {
                        advertisingIdClient.finish();
                    }
                }
            } catch (GooglePlayServicesNotAvailableException e) {
                zzam.zznp = true;
            } catch (IOException e2) {
            } catch (GooglePlayServicesRepairableException e3) {
            }
            zzam.zzno.countDown();
        }
    }

    protected zzam(Context context, zzap com_google_android_gms_internal_zzap, zzaq com_google_android_gms_internal_zzaq, boolean z) {
        super(context, com_google_android_gms_internal_zzap, com_google_android_gms_internal_zzaq);
        this.zznq = z;
    }

    public static zzam zza(String str, Context context, boolean z) {
        zzap com_google_android_gms_internal_zzah = new zzah();
        zzal.zza(str, context, com_google_android_gms_internal_zzah);
        if (z) {
            synchronized (zzam.class) {
                if (zznn == null) {
                    new Thread(new zzb(context)).start();
                }
            }
        }
        return new zzam(context, com_google_android_gms_internal_zzah, new zzas(239), z);
    }

    zza zzY() throws IOException {
        synchronized (zzam.class) {
            try {
                zza com_google_android_gms_internal_zzam_zza;
                if (!zzno.await(2, TimeUnit.SECONDS)) {
                    com_google_android_gms_internal_zzam_zza = new zza(this, null, false);
                    return com_google_android_gms_internal_zzam_zza;
                } else if (zznn == null) {
                    com_google_android_gms_internal_zzam_zza = new zza(this, null, false);
                    return com_google_android_gms_internal_zzam_zza;
                } else {
                    Info info = zznn.getInfo();
                    return new zza(this, zzk(info.getId()), info.isLimitAdTrackingEnabled());
                }
            } catch (InterruptedException e) {
                return new zza(this, null, false);
            }
        }
    }

    protected void zzc(Context context) {
        super.zzc(context);
        try {
            if (zznp || !this.zznq) {
                zza(24, zzal.zze(context));
                return;
            }
            zza zzY = zzY();
            String id = zzY.getId();
            if (id != null) {
                zza(28, zzY.isLimitAdTrackingEnabled() ? 1 : 0);
                zza(26, 5);
                zza(24, id);
            }
        } catch (IOException e) {
        } catch (zza e2) {
        }
    }
}
