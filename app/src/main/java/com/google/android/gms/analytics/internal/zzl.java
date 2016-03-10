package com.google.android.gms.analytics.internal;

import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;
import android.util.Pair;
import com.google.android.gms.analytics.AnalyticsReceiver;
import com.google.android.gms.analytics.AnalyticsService;
import com.google.android.gms.analytics.CampaignTrackingReceiver;
import com.google.android.gms.analytics.CampaignTrackingService;
import com.google.android.gms.analytics.zza;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzio;
import com.google.android.gms.internal.zzip;
import com.google.android.gms.internal.zzno;
import com.google.android.gms.internal.zzns;
import com.google.android.gms.internal.zznx;
import com.google.android.gms.internal.zzny;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class zzl extends zzd {
    private boolean mStarted;
    private final zzj zzKn;
    private final zzah zzKo;
    private final zzag zzKp;
    private final zzi zzKq;
    private long zzKr = Long.MIN_VALUE;
    private final zzt zzKs;
    private final zzt zzKt;
    private final zzaj zzKu;
    private long zzKv;
    private boolean zzKw;

    protected zzl(zzf com_google_android_gms_analytics_internal_zzf, zzg com_google_android_gms_analytics_internal_zzg) {
        super(com_google_android_gms_analytics_internal_zzf);
        zzu.zzu(com_google_android_gms_analytics_internal_zzg);
        this.zzKp = com_google_android_gms_analytics_internal_zzg.zzk(com_google_android_gms_analytics_internal_zzf);
        this.zzKn = com_google_android_gms_analytics_internal_zzg.zzm(com_google_android_gms_analytics_internal_zzf);
        this.zzKo = com_google_android_gms_analytics_internal_zzg.zzn(com_google_android_gms_analytics_internal_zzf);
        this.zzKq = com_google_android_gms_analytics_internal_zzg.zzo(com_google_android_gms_analytics_internal_zzf);
        this.zzKu = new zzaj(zzhP());
        this.zzKs = new zzt(this, com_google_android_gms_analytics_internal_zzf) {
            final /* synthetic */ zzl zzKx;

            public void run() {
                this.zzKx.zziA();
            }
        };
        this.zzKt = new zzt(this, com_google_android_gms_analytics_internal_zzf) {
            final /* synthetic */ zzl zzKx;

            public void run() {
                this.zzKx.zziB();
            }
        };
    }

    private void zza(zzh com_google_android_gms_analytics_internal_zzh, zzny com_google_android_gms_internal_zzny) {
        zzu.zzu(com_google_android_gms_analytics_internal_zzh);
        zzu.zzu(com_google_android_gms_internal_zzny);
        zza com_google_android_gms_analytics_zza = new zza(zzhM());
        com_google_android_gms_analytics_zza.zzaI(com_google_android_gms_analytics_internal_zzh.zzij());
        com_google_android_gms_analytics_zza.enableAdvertisingIdCollection(com_google_android_gms_analytics_internal_zzh.zzik());
        zzno zzhc = com_google_android_gms_analytics_zza.zzhc();
        zzip com_google_android_gms_internal_zzip = (zzip) zzhc.zze(zzip.class);
        com_google_android_gms_internal_zzip.zzaN(MPDbAdapter.KEY_DATA);
        com_google_android_gms_internal_zzip.zzF(true);
        zzhc.zzb(com_google_android_gms_internal_zzny);
        zzio com_google_android_gms_internal_zzio = (zzio) zzhc.zze(zzio.class);
        zznx com_google_android_gms_internal_zznx = (zznx) zzhc.zze(zznx.class);
        for (Entry entry : com_google_android_gms_analytics_internal_zzh.zzn().entrySet()) {
            String str = (String) entry.getKey();
            String str2 = (String) entry.getValue();
            if ("an".equals(str)) {
                com_google_android_gms_internal_zznx.setAppName(str2);
            } else if ("av".equals(str)) {
                com_google_android_gms_internal_zznx.setAppVersion(str2);
            } else if ("aid".equals(str)) {
                com_google_android_gms_internal_zznx.setAppId(str2);
            } else if ("aiid".equals(str)) {
                com_google_android_gms_internal_zznx.setAppInstallerId(str2);
            } else if ("uid".equals(str)) {
                com_google_android_gms_internal_zzip.setUserId(str2);
            } else {
                com_google_android_gms_internal_zzio.set(str, str2);
            }
        }
        zzb("Sending installation campaign to", com_google_android_gms_analytics_internal_zzh.zzij(), com_google_android_gms_internal_zzny);
        zzhc.zzL(zzhU().zzkk());
        zzhc.zzvT();
    }

    private boolean zzba(String str) {
        return getContext().checkCallingOrSelfPermission(str) == 0;
    }

    private void zziA() {
        zzb(new zzw(this) {
            final /* synthetic */ zzl zzKx;

            {
                this.zzKx = r1;
            }

            public void zzc(Throwable th) {
                this.zzKx.zziG();
            }
        });
    }

    private void zziB() {
        try {
            this.zzKn.zzis();
            zziG();
        } catch (SQLiteException e) {
            zzd("Failed to delete stale hits", e);
        }
        this.zzKt.zzt(zzhR().zzjy());
    }

    private boolean zziH() {
        return this.zzKw ? false : (!zzhR().zziW() || zzhR().zziX()) && zziN() > 0;
    }

    private void zziI() {
        zzv zzhT = zzhT();
        if (zzhT.zzjG() && !zzhT.zzbp()) {
            long zzit = zzit();
            if (zzit != 0 && Math.abs(zzhP().currentTimeMillis() - zzit) <= zzhR().zzjg()) {
                zza("Dispatch alarm scheduled (ms)", Long.valueOf(zzhR().zzjf()));
                zzhT.zzjH();
            }
        }
    }

    private void zziJ() {
        zziI();
        long zziN = zziN();
        long zzkm = zzhU().zzkm();
        if (zzkm != 0) {
            zzkm = zziN - Math.abs(zzhP().currentTimeMillis() - zzkm);
            if (zzkm <= 0) {
                zzkm = Math.min(zzhR().zzjd(), zziN);
            }
        } else {
            zzkm = Math.min(zzhR().zzjd(), zziN);
        }
        zza("Dispatch scheduled (ms)", Long.valueOf(zzkm));
        if (this.zzKs.zzbp()) {
            this.zzKs.zzu(Math.max(1, zzkm + this.zzKs.zzjD()));
            return;
        }
        this.zzKs.zzt(zzkm);
    }

    private void zziK() {
        zziL();
        zziM();
    }

    private void zziL() {
        if (this.zzKs.zzbp()) {
            zzaT("All hits dispatched or no network/service. Going to power save mode");
        }
        this.zzKs.cancel();
    }

    private void zziM() {
        zzv zzhT = zzhT();
        if (zzhT.zzbp()) {
            zzhT.cancel();
        }
    }

    private void zziy() {
        Context context = zzhM().getContext();
        if (!AnalyticsReceiver.zzT(context)) {
            zzaW("AnalyticsReceiver is not registered or is disabled. Register the receiver for reliable dispatching on non-Google Play devices. See http://goo.gl/8Rd3yj for instructions.");
        } else if (!AnalyticsService.zzU(context)) {
            zzaX("AnalyticsService is not registered or is disabled. Analytics service at risk of not starting. See http://goo.gl/8Rd3yj for instructions.");
        }
        if (!CampaignTrackingReceiver.zzT(context)) {
            zzaW("CampaignTrackingReceiver is not registered, not exported or is disabled. Installation campaign tracking is not possible. See http://goo.gl/8Rd3yj for instructions.");
        } else if (!CampaignTrackingService.zzU(context)) {
            zzaW("CampaignTrackingService is not registered or is disabled. Installation campaign tracking is not possible. See http://goo.gl/8Rd3yj for instructions.");
        }
    }

    protected void onServiceConnected() {
        zzhO();
        if (!zzhR().zziW()) {
            zziD();
        }
    }

    void start() {
        zzia();
        zzu.zza(!this.mStarted, (Object) "Analytics backend already started");
        this.mStarted = true;
        if (!zzhR().zziW()) {
            zziy();
        }
        zzhS().zze(new Runnable(this) {
            final /* synthetic */ zzl zzKx;

            {
                this.zzKx = r1;
            }

            public void run() {
                this.zzKx.zziz();
            }
        });
    }

    public void zzG(boolean z) {
        zziG();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long zza(com.google.android.gms.analytics.internal.zzh r6, boolean r7) {
        /*
        r5 = this;
        com.google.android.gms.common.internal.zzu.zzu(r6);
        r5.zzia();
        r5.zzhO();
        r0 = r5.zzKn;	 Catch:{ SQLiteException -> 0x0049 }
        r0.beginTransaction();	 Catch:{ SQLiteException -> 0x0049 }
        r0 = r5.zzKn;	 Catch:{ SQLiteException -> 0x0049 }
        r2 = r6.zzii();	 Catch:{ SQLiteException -> 0x0049 }
        r1 = r6.getClientId();	 Catch:{ SQLiteException -> 0x0049 }
        r0.zza(r2, r1);	 Catch:{ SQLiteException -> 0x0049 }
        r0 = r5.zzKn;	 Catch:{ SQLiteException -> 0x0049 }
        r2 = r6.zzii();	 Catch:{ SQLiteException -> 0x0049 }
        r1 = r6.getClientId();	 Catch:{ SQLiteException -> 0x0049 }
        r4 = r6.zzij();	 Catch:{ SQLiteException -> 0x0049 }
        r0 = r0.zza(r2, r1, r4);	 Catch:{ SQLiteException -> 0x0049 }
        if (r7 != 0) goto L_0x0042;
    L_0x002f:
        r6.zzn(r0);	 Catch:{ SQLiteException -> 0x0049 }
    L_0x0032:
        r2 = r5.zzKn;	 Catch:{ SQLiteException -> 0x0049 }
        r2.zzb(r6);	 Catch:{ SQLiteException -> 0x0049 }
        r2 = r5.zzKn;	 Catch:{ SQLiteException -> 0x0049 }
        r2.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x0049 }
        r2 = r5.zzKn;	 Catch:{ SQLiteException -> 0x005e }
        r2.endTransaction();	 Catch:{ SQLiteException -> 0x005e }
    L_0x0041:
        return r0;
    L_0x0042:
        r2 = 1;
        r2 = r2 + r0;
        r6.zzn(r2);	 Catch:{ SQLiteException -> 0x0049 }
        goto L_0x0032;
    L_0x0049:
        r0 = move-exception;
        r1 = "Failed to update Analytics property";
        r5.zze(r1, r0);	 Catch:{ all -> 0x0065 }
        r0 = -1;
        r2 = r5.zzKn;	 Catch:{ SQLiteException -> 0x0057 }
        r2.endTransaction();	 Catch:{ SQLiteException -> 0x0057 }
        goto L_0x0041;
    L_0x0057:
        r2 = move-exception;
        r3 = "Failed to end transaction";
        r5.zze(r3, r2);
        goto L_0x0041;
    L_0x005e:
        r2 = move-exception;
        r3 = "Failed to end transaction";
        r5.zze(r3, r2);
        goto L_0x0041;
    L_0x0065:
        r0 = move-exception;
        r1 = r5.zzKn;	 Catch:{ SQLiteException -> 0x006c }
        r1.endTransaction();	 Catch:{ SQLiteException -> 0x006c }
    L_0x006b:
        throw r0;
    L_0x006c:
        r1 = move-exception;
        r2 = "Failed to end transaction";
        r5.zze(r2, r1);
        goto L_0x006b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.analytics.internal.zzl.zza(com.google.android.gms.analytics.internal.zzh, boolean):long");
    }

    public void zza(zzab com_google_android_gms_analytics_internal_zzab) {
        zzu.zzu(com_google_android_gms_analytics_internal_zzab);
        zzns.zzhO();
        zzia();
        if (this.zzKw) {
            zzaU("Hit delivery not possible. Missing network permissions. See http://goo.gl/8Rd3yj for instructions");
        } else {
            zza("Delivering hit", com_google_android_gms_analytics_internal_zzab);
        }
        zzab zzf = zzf(com_google_android_gms_analytics_internal_zzab);
        zziC();
        if (this.zzKq.zzb(zzf)) {
            zzaU("Hit sent to the device AnalyticsService for delivery");
        } else if (zzhR().zziW()) {
            zzhQ().zza(zzf, "Service unavailable on package side");
        } else {
            try {
                this.zzKn.zzc(zzf);
                zziG();
            } catch (SQLiteException e) {
                zze("Delivery failed to save hit to a database", e);
                zzhQ().zza(zzf, "deliver: failed to insert hit to database");
            }
        }
    }

    public void zza(final zzw com_google_android_gms_analytics_internal_zzw, final long j) {
        zzns.zzhO();
        zzia();
        long j2 = -1;
        long zzkm = zzhU().zzkm();
        if (zzkm != 0) {
            j2 = Math.abs(zzhP().currentTimeMillis() - zzkm);
        }
        zzb("Dispatching local hits. Elapsed time since last dispatch (ms)", Long.valueOf(j2));
        if (!zzhR().zziW()) {
            zziC();
        }
        try {
            if (zziE()) {
                zzhS().zze(new Runnable(this) {
                    final /* synthetic */ zzl zzKx;

                    public void run() {
                        this.zzKx.zza(com_google_android_gms_analytics_internal_zzw, j);
                    }
                });
                return;
            }
            zzhU().zzkn();
            zziG();
            if (com_google_android_gms_analytics_internal_zzw != null) {
                com_google_android_gms_analytics_internal_zzw.zzc(null);
            }
            if (this.zzKv != j) {
                this.zzKp.zzkf();
            }
        } catch (Throwable th) {
            zze("Local dispatch failed", th);
            zzhU().zzkn();
            zziG();
            if (com_google_android_gms_analytics_internal_zzw != null) {
                com_google_android_gms_analytics_internal_zzw.zzc(th);
            }
        }
    }

    public void zzb(zzw com_google_android_gms_analytics_internal_zzw) {
        zza(com_google_android_gms_analytics_internal_zzw, this.zzKv);
    }

    public void zzbb(String str) {
        zzu.zzcj(str);
        zzhO();
        zzhN();
        zzny zza = zzam.zza(zzhQ(), str);
        if (zza == null) {
            zzd("Parsing failed. Ignoring invalid campaign data", str);
            return;
        }
        CharSequence zzko = zzhU().zzko();
        if (str.equals(zzko)) {
            zzaW("Ignoring duplicate install campaign");
        } else if (TextUtils.isEmpty(zzko)) {
            zzhU().zzbf(str);
            if (zzhU().zzkl().zzv(zzhR().zzjB())) {
                zzd("Campaign received too late, ignoring", zza);
                return;
            }
            zzb("Received installation campaign", zza);
            for (zzh zza2 : this.zzKn.zzr(0)) {
                zza(zza2, zza);
            }
        } else {
            zzd("Ignoring multiple install campaigns. original, new", zzko, str);
        }
    }

    protected void zzc(zzh com_google_android_gms_analytics_internal_zzh) {
        zzhO();
        zzb("Sending first hit to property", com_google_android_gms_analytics_internal_zzh.zzij());
        if (!zzhU().zzkl().zzv(zzhR().zzjB())) {
            String zzko = zzhU().zzko();
            if (!TextUtils.isEmpty(zzko)) {
                zzny zza = zzam.zza(zzhQ(), zzko);
                zzb("Found relevant installation campaign", zza);
                zza(com_google_android_gms_analytics_internal_zzh, zza);
            }
        }
    }

    zzab zzf(zzab com_google_android_gms_analytics_internal_zzab) {
        if (!TextUtils.isEmpty(com_google_android_gms_analytics_internal_zzab.zzka())) {
            return com_google_android_gms_analytics_internal_zzab;
        }
        Pair zzks = zzhU().zzkp().zzks();
        if (zzks == null) {
            return com_google_android_gms_analytics_internal_zzab;
        }
        Long l = (Long) zzks.second;
        String str = l + ":" + ((String) zzks.first);
        Map hashMap = new HashMap(com_google_android_gms_analytics_internal_zzab.zzn());
        hashMap.put("_m", str);
        return zzab.zza(this, com_google_android_gms_analytics_internal_zzab, hashMap);
    }

    public void zzhG() {
        zzns.zzhO();
        zzia();
        if (!zzhR().zziW()) {
            zzaT("Delete all hits from local store");
            try {
                this.zzKn.zziq();
                this.zzKn.zzir();
                zziG();
            } catch (SQLiteException e) {
                zzd("Failed to delete hits from store", e);
            }
        }
        zziC();
        if (this.zzKq.zzim()) {
            zzaT("Device service unavailable. Can't clear hits stored on the device service.");
        }
    }

    public void zzhJ() {
        zzns.zzhO();
        zzia();
        zzaT("Service disconnected");
    }

    void zzhL() {
        zzhO();
        this.zzKv = zzhP().currentTimeMillis();
    }

    protected void zzhn() {
        this.zzKn.zza();
        this.zzKo.zza();
        this.zzKq.zza();
    }

    protected void zziC() {
        if (!this.zzKw && zzhR().zziY() && !this.zzKq.isConnected()) {
            if (this.zzKu.zzv(zzhR().zzjt())) {
                this.zzKu.start();
                zzaT("Connecting to service");
                if (this.zzKq.connect()) {
                    zzaT("Connected to service");
                    this.zzKu.clear();
                    onServiceConnected();
                }
            }
        }
    }

    public void zziD() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:42)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:66)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:280)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:167)
*/
        /*
        r6 = this;
        com.google.android.gms.internal.zzns.zzhO();
        r6.zzia();
        r6.zzhN();
        r0 = r6.zzhR();
        r0 = r0.zziY();
        if (r0 != 0) goto L_0x0018;
    L_0x0013:
        r0 = "Service client disabled. Can't dispatch local hits to device AnalyticsService";
        r6.zzaW(r0);
    L_0x0018:
        r0 = r6.zzKq;
        r0 = r0.isConnected();
        if (r0 != 0) goto L_0x0026;
    L_0x0020:
        r0 = "Service not connected";
        r6.zzaT(r0);
    L_0x0025:
        return;
    L_0x0026:
        r0 = r6.zzKn;
        r0 = r0.isEmpty();
        if (r0 != 0) goto L_0x0025;
    L_0x002e:
        r0 = "Dispatching local hits to device AnalyticsService";
        r6.zzaT(r0);
    L_0x0033:
        r0 = r6.zzKn;	 Catch:{ SQLiteException -> 0x004c }
        r1 = r6.zzhR();	 Catch:{ SQLiteException -> 0x004c }
        r1 = r1.zzjh();	 Catch:{ SQLiteException -> 0x004c }
        r2 = (long) r1;	 Catch:{ SQLiteException -> 0x004c }
        r1 = r0.zzp(r2);	 Catch:{ SQLiteException -> 0x004c }
        r0 = r1.isEmpty();	 Catch:{ SQLiteException -> 0x004c }
        if (r0 == 0) goto L_0x0062;	 Catch:{ SQLiteException -> 0x004c }
    L_0x0048:
        r6.zziG();	 Catch:{ SQLiteException -> 0x004c }
        goto L_0x0025;
    L_0x004c:
        r0 = move-exception;
        r1 = "Failed to read hits from store";
        r6.zze(r1, r0);
        r6.zziK();
        goto L_0x0025;
    L_0x0056:
        r1.remove(r0);
        r2 = r6.zzKn;	 Catch:{ SQLiteException -> 0x007b }
        r4 = r0.zzjV();	 Catch:{ SQLiteException -> 0x007b }
        r2.zzq(r4);	 Catch:{ SQLiteException -> 0x007b }
    L_0x0062:
        r0 = r1.isEmpty();
        if (r0 != 0) goto L_0x0033;
    L_0x0068:
        r0 = 0;
        r0 = r1.get(r0);
        r0 = (com.google.android.gms.analytics.internal.zzab) r0;
        r2 = r6.zzKq;
        r2 = r2.zzb(r0);
        if (r2 != 0) goto L_0x0056;
    L_0x0077:
        r6.zziG();
        goto L_0x0025;
    L_0x007b:
        r0 = move-exception;
        r1 = "Failed to remove hit that was send for delivery";
        r6.zze(r1, r0);
        r6.zziK();
        goto L_0x0025;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.analytics.internal.zzl.zziD():void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected boolean zziE() {
        /*
        r12 = this;
        r1 = 1;
        r2 = 0;
        com.google.android.gms.internal.zzns.zzhO();
        r12.zzia();
        r0 = "Dispatching a batch of local hits";
        r12.zzaT(r0);
        r0 = r12.zzKq;
        r0 = r0.isConnected();
        if (r0 != 0) goto L_0x0032;
    L_0x0015:
        r0 = r12.zzhR();
        r0 = r0.zziW();
        if (r0 != 0) goto L_0x0032;
    L_0x001f:
        r0 = r1;
    L_0x0020:
        r3 = r12.zzKo;
        r3 = r3.zzkg();
        if (r3 != 0) goto L_0x0034;
    L_0x0028:
        if (r0 == 0) goto L_0x0036;
    L_0x002a:
        if (r1 == 0) goto L_0x0036;
    L_0x002c:
        r0 = "No network or service available. Will retry later";
        r12.zzaT(r0);
    L_0x0031:
        return r2;
    L_0x0032:
        r0 = r2;
        goto L_0x0020;
    L_0x0034:
        r1 = r2;
        goto L_0x0028;
    L_0x0036:
        r0 = r12.zzhR();
        r0 = r0.zzjh();
        r1 = r12.zzhR();
        r1 = r1.zzji();
        r0 = java.lang.Math.max(r0, r1);
        r6 = (long) r0;
        r3 = new java.util.ArrayList;
        r3.<init>();
        r4 = 0;
    L_0x0052:
        r0 = r12.zzKn;	 Catch:{ all -> 0x01ee }
        r0.beginTransaction();	 Catch:{ all -> 0x01ee }
        r3.clear();	 Catch:{ all -> 0x01ee }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x00d3 }
        r8 = r0.zzp(r6);	 Catch:{ SQLiteException -> 0x00d3 }
        r0 = r8.isEmpty();	 Catch:{ SQLiteException -> 0x00d3 }
        if (r0 == 0) goto L_0x0083;
    L_0x0066:
        r0 = "Store is empty, nothing to dispatch";
        r12.zzaT(r0);	 Catch:{ SQLiteException -> 0x00d3 }
        r12.zziK();	 Catch:{ SQLiteException -> 0x00d3 }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x0079 }
        r0.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x0079 }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x0079 }
        r0.endTransaction();	 Catch:{ SQLiteException -> 0x0079 }
        goto L_0x0031;
    L_0x0079:
        r0 = move-exception;
        r1 = "Failed to commit local dispatch transaction";
        r12.zze(r1, r0);
        r12.zziK();
        goto L_0x0031;
    L_0x0083:
        r0 = "Hits loaded from store. count";
        r1 = r8.size();	 Catch:{ SQLiteException -> 0x00d3 }
        r1 = java.lang.Integer.valueOf(r1);	 Catch:{ SQLiteException -> 0x00d3 }
        r12.zza(r0, r1);	 Catch:{ SQLiteException -> 0x00d3 }
        r1 = r8.iterator();	 Catch:{ all -> 0x01ee }
    L_0x0094:
        r0 = r1.hasNext();	 Catch:{ all -> 0x01ee }
        if (r0 == 0) goto L_0x00f3;
    L_0x009a:
        r0 = r1.next();	 Catch:{ all -> 0x01ee }
        r0 = (com.google.android.gms.analytics.internal.zzab) r0;	 Catch:{ all -> 0x01ee }
        r10 = r0.zzjV();	 Catch:{ all -> 0x01ee }
        r0 = (r10 > r4 ? 1 : (r10 == r4 ? 0 : -1));
        if (r0 != 0) goto L_0x0094;
    L_0x00a8:
        r0 = "Database contains successfully uploaded hit";
        r1 = java.lang.Long.valueOf(r4);	 Catch:{ all -> 0x01ee }
        r3 = r8.size();	 Catch:{ all -> 0x01ee }
        r3 = java.lang.Integer.valueOf(r3);	 Catch:{ all -> 0x01ee }
        r12.zzd(r0, r1, r3);	 Catch:{ all -> 0x01ee }
        r12.zziK();	 Catch:{ all -> 0x01ee }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x00c8 }
        r0.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x00c8 }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x00c8 }
        r0.endTransaction();	 Catch:{ SQLiteException -> 0x00c8 }
        goto L_0x0031;
    L_0x00c8:
        r0 = move-exception;
        r1 = "Failed to commit local dispatch transaction";
        r12.zze(r1, r0);
        r12.zziK();
        goto L_0x0031;
    L_0x00d3:
        r0 = move-exception;
        r1 = "Failed to read hits from persisted store";
        r12.zzd(r1, r0);	 Catch:{ all -> 0x01ee }
        r12.zziK();	 Catch:{ all -> 0x01ee }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x00e8 }
        r0.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x00e8 }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x00e8 }
        r0.endTransaction();	 Catch:{ SQLiteException -> 0x00e8 }
        goto L_0x0031;
    L_0x00e8:
        r0 = move-exception;
        r1 = "Failed to commit local dispatch transaction";
        r12.zze(r1, r0);
        r12.zziK();
        goto L_0x0031;
    L_0x00f3:
        r0 = r12.zzKq;	 Catch:{ all -> 0x01ee }
        r0 = r0.isConnected();	 Catch:{ all -> 0x01ee }
        if (r0 == 0) goto L_0x0205;
    L_0x00fb:
        r0 = r12.zzhR();	 Catch:{ all -> 0x01ee }
        r0 = r0.zziW();	 Catch:{ all -> 0x01ee }
        if (r0 != 0) goto L_0x0205;
    L_0x0105:
        r0 = "Service connected, sending hits to the service";
        r12.zzaT(r0);	 Catch:{ all -> 0x01ee }
    L_0x010a:
        r0 = r8.isEmpty();	 Catch:{ all -> 0x01ee }
        if (r0 != 0) goto L_0x0205;
    L_0x0110:
        r0 = 0;
        r0 = r8.get(r0);	 Catch:{ all -> 0x01ee }
        r0 = (com.google.android.gms.analytics.internal.zzab) r0;	 Catch:{ all -> 0x01ee }
        r1 = r12.zzKq;	 Catch:{ all -> 0x01ee }
        r1 = r1.zzb(r0);	 Catch:{ all -> 0x01ee }
        if (r1 != 0) goto L_0x0148;
    L_0x011f:
        r0 = r4;
    L_0x0120:
        r4 = r12.zzKo;	 Catch:{ all -> 0x01ee }
        r4 = r4.zzkg();	 Catch:{ all -> 0x01ee }
        if (r4 == 0) goto L_0x0199;
    L_0x0128:
        r4 = r12.zzKo;	 Catch:{ all -> 0x01ee }
        r9 = r4.zzf(r8);	 Catch:{ all -> 0x01ee }
        r10 = r9.iterator();	 Catch:{ all -> 0x01ee }
        r4 = r0;
    L_0x0133:
        r0 = r10.hasNext();	 Catch:{ all -> 0x01ee }
        if (r0 == 0) goto L_0x018d;
    L_0x0139:
        r0 = r10.next();	 Catch:{ all -> 0x01ee }
        r0 = (java.lang.Long) r0;	 Catch:{ all -> 0x01ee }
        r0 = r0.longValue();	 Catch:{ all -> 0x01ee }
        r4 = java.lang.Math.max(r4, r0);	 Catch:{ all -> 0x01ee }
        goto L_0x0133;
    L_0x0148:
        r10 = r0.zzjV();	 Catch:{ all -> 0x01ee }
        r4 = java.lang.Math.max(r4, r10);	 Catch:{ all -> 0x01ee }
        r8.remove(r0);	 Catch:{ all -> 0x01ee }
        r1 = "Hit sent do device AnalyticsService for delivery";
        r12.zzb(r1, r0);	 Catch:{ all -> 0x01ee }
        r1 = r12.zzKn;	 Catch:{ SQLiteException -> 0x016d }
        r10 = r0.zzjV();	 Catch:{ SQLiteException -> 0x016d }
        r1.zzq(r10);	 Catch:{ SQLiteException -> 0x016d }
        r0 = r0.zzjV();	 Catch:{ SQLiteException -> 0x016d }
        r0 = java.lang.Long.valueOf(r0);	 Catch:{ SQLiteException -> 0x016d }
        r3.add(r0);	 Catch:{ SQLiteException -> 0x016d }
        goto L_0x010a;
    L_0x016d:
        r0 = move-exception;
        r1 = "Failed to remove hit that was send for delivery";
        r12.zze(r1, r0);	 Catch:{ all -> 0x01ee }
        r12.zziK();	 Catch:{ all -> 0x01ee }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x0182 }
        r0.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x0182 }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x0182 }
        r0.endTransaction();	 Catch:{ SQLiteException -> 0x0182 }
        goto L_0x0031;
    L_0x0182:
        r0 = move-exception;
        r1 = "Failed to commit local dispatch transaction";
        r12.zze(r1, r0);
        r12.zziK();
        goto L_0x0031;
    L_0x018d:
        r8.removeAll(r9);	 Catch:{ all -> 0x01ee }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x01b6 }
        r0.zzd(r9);	 Catch:{ SQLiteException -> 0x01b6 }
        r3.addAll(r9);	 Catch:{ SQLiteException -> 0x01b6 }
        r0 = r4;
    L_0x0199:
        r4 = r3.isEmpty();	 Catch:{ all -> 0x01ee }
        if (r4 == 0) goto L_0x01d6;
    L_0x019f:
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x01ab }
        r0.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x01ab }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x01ab }
        r0.endTransaction();	 Catch:{ SQLiteException -> 0x01ab }
        goto L_0x0031;
    L_0x01ab:
        r0 = move-exception;
        r1 = "Failed to commit local dispatch transaction";
        r12.zze(r1, r0);
        r12.zziK();
        goto L_0x0031;
    L_0x01b6:
        r0 = move-exception;
        r1 = "Failed to remove successfully uploaded hits";
        r12.zze(r1, r0);	 Catch:{ all -> 0x01ee }
        r12.zziK();	 Catch:{ all -> 0x01ee }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x01cb }
        r0.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x01cb }
        r0 = r12.zzKn;	 Catch:{ SQLiteException -> 0x01cb }
        r0.endTransaction();	 Catch:{ SQLiteException -> 0x01cb }
        goto L_0x0031;
    L_0x01cb:
        r0 = move-exception;
        r1 = "Failed to commit local dispatch transaction";
        r12.zze(r1, r0);
        r12.zziK();
        goto L_0x0031;
    L_0x01d6:
        r4 = r12.zzKn;	 Catch:{ SQLiteException -> 0x01e3 }
        r4.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x01e3 }
        r4 = r12.zzKn;	 Catch:{ SQLiteException -> 0x01e3 }
        r4.endTransaction();	 Catch:{ SQLiteException -> 0x01e3 }
        r4 = r0;
        goto L_0x0052;
    L_0x01e3:
        r0 = move-exception;
        r1 = "Failed to commit local dispatch transaction";
        r12.zze(r1, r0);
        r12.zziK();
        goto L_0x0031;
    L_0x01ee:
        r0 = move-exception;
        r1 = r12.zzKn;	 Catch:{ SQLiteException -> 0x01fa }
        r1.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x01fa }
        r1 = r12.zzKn;	 Catch:{ SQLiteException -> 0x01fa }
        r1.endTransaction();	 Catch:{ SQLiteException -> 0x01fa }
        throw r0;
    L_0x01fa:
        r0 = move-exception;
        r1 = "Failed to commit local dispatch transaction";
        r12.zze(r1, r0);
        r12.zziK();
        goto L_0x0031;
    L_0x0205:
        r0 = r4;
        goto L_0x0120;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.analytics.internal.zzl.zziE():boolean");
    }

    public void zziF() {
        zzns.zzhO();
        zzia();
        zzaU("Sync dispatching local hits");
        long j = this.zzKv;
        if (!zzhR().zziW()) {
            zziC();
        }
        do {
            try {
            } catch (Throwable th) {
                zze("Sync local dispatch failed", th);
                zziG();
                return;
            }
        } while (zziE());
        zzhU().zzkn();
        zziG();
        if (this.zzKv != j) {
            this.zzKp.zzkf();
        }
    }

    public void zziG() {
        zzhM().zzhO();
        zzia();
        if (!zziH()) {
            this.zzKp.unregister();
            zziK();
        } else if (this.zzKn.isEmpty()) {
            this.zzKp.unregister();
            zziK();
        } else {
            boolean z;
            if (((Boolean) zzy.zzLI.get()).booleanValue()) {
                z = true;
            } else {
                this.zzKp.zzkd();
                z = this.zzKp.isConnected();
            }
            if (z) {
                zziJ();
                return;
            }
            zziK();
            zziI();
        }
    }

    public long zziN() {
        if (this.zzKr != Long.MIN_VALUE) {
            return this.zzKr;
        }
        return zzhm().zzjQ() ? ((long) zzhm().zzkH()) * 1000 : zzhR().zzje();
    }

    public void zziO() {
        zzia();
        zzhO();
        this.zzKw = true;
        this.zzKq.disconnect();
        zziG();
    }

    public long zzit() {
        zzns.zzhO();
        zzia();
        try {
            return this.zzKn.zzit();
        } catch (SQLiteException e) {
            zze("Failed to get min/max hit times from local store", e);
            return 0;
        }
    }

    protected void zziz() {
        zzia();
        zzhU().zzkk();
        if (!zzba("android.permission.ACCESS_NETWORK_STATE")) {
            zzaX("Missing required android.permission.ACCESS_NETWORK_STATE. Google Analytics disabled. See http://goo.gl/8Rd3yj for instructions");
            zziO();
        }
        if (!zzba("android.permission.INTERNET")) {
            zzaX("Missing required android.permission.INTERNET. Google Analytics disabled. See http://goo.gl/8Rd3yj for instructions");
            zziO();
        }
        if (AnalyticsService.zzU(getContext())) {
            zzaT("AnalyticsService registered in the app manifest and enabled");
        } else if (zzhR().zziW()) {
            zzaX("Device AnalyticsService not registered! Hits will not be delivered reliably.");
        } else {
            zzaW("AnalyticsService not registered in the app manifest. Hits might not be delivered reliably. See http://goo.gl/8Rd3yj for instructions.");
        }
        if (!(this.zzKw || zzhR().zziW() || this.zzKn.isEmpty())) {
            zziC();
        }
        zziG();
    }

    public void zzs(long j) {
        zzns.zzhO();
        zzia();
        if (j < 0) {
            j = 0;
        }
        this.zzKr = j;
        zziG();
    }
}
