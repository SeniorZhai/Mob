package com.google.android.gms.analytics;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.google.android.gms.analytics.internal.zzab;
import com.google.android.gms.analytics.internal.zzad;
import com.google.android.gms.analytics.internal.zzal;
import com.google.android.gms.analytics.internal.zzam;
import com.google.android.gms.analytics.internal.zzd;
import com.google.android.gms.analytics.internal.zze;
import com.google.android.gms.analytics.internal.zzf;
import com.google.android.gms.analytics.internal.zzh;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zznx;
import com.helpshift.HSFunnel;
import com.helpshift.res.values.HSConsts;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class Tracker extends zzd {
    private boolean zzIH;
    private final Map<String, String> zzII = new HashMap();
    private final zzad zzIJ;
    private final zza zzIK;
    private ExceptionReporter zzIL;
    private zzal zzIM;
    private final Map<String, String> zzyn = new HashMap();

    private class zza extends zzd implements zza {
        final /* synthetic */ Tracker zzIU;
        private boolean zzIV;
        private int zzIW;
        private long zzIX = -1;
        private boolean zzIY;
        private long zzIZ;

        protected zza(Tracker tracker, zzf com_google_android_gms_analytics_internal_zzf) {
            this.zzIU = tracker;
            super(com_google_android_gms_analytics_internal_zzf);
        }

        private void zzhr() {
            if (this.zzIX >= 0 || this.zzIV) {
                zzhg().zza(this.zzIU.zzIK);
            } else {
                zzhg().zzb(this.zzIU.zzIK);
            }
        }

        public void enableAutoActivityTracking(boolean enabled) {
            this.zzIV = enabled;
            zzhr();
        }

        public void setSessionTimeout(long sessionTimeout) {
            this.zzIX = sessionTimeout;
            zzhr();
        }

        protected void zzhn() {
        }

        public synchronized boolean zzhq() {
            boolean z;
            z = this.zzIY;
            this.zzIY = false;
            return z;
        }

        boolean zzhs() {
            return zzhP().elapsedRealtime() >= this.zzIZ + Math.max(1000, this.zzIX);
        }

        public void zzn(Activity activity) {
            if (this.zzIW == 0 && zzhs()) {
                this.zzIY = true;
            }
            this.zzIW++;
            if (this.zzIV) {
                Intent intent = activity.getIntent();
                if (intent != null) {
                    this.zzIU.setCampaignParamsOnNextHit(intent.getData());
                }
                Map hashMap = new HashMap();
                hashMap.put("&t", "screenview");
                this.zzIU.set("&cd", this.zzIU.zzIM != null ? this.zzIU.zzIM.zzq(activity) : activity.getClass().getCanonicalName());
                if (TextUtils.isEmpty((CharSequence) hashMap.get("&dr"))) {
                    CharSequence zzp = Tracker.zzp(activity);
                    if (!TextUtils.isEmpty(zzp)) {
                        hashMap.put("&dr", zzp);
                    }
                }
                this.zzIU.send(hashMap);
            }
        }

        public void zzo(Activity activity) {
            this.zzIW--;
            this.zzIW = Math.max(0, this.zzIW);
            if (this.zzIW == 0) {
                this.zzIZ = zzhP().elapsedRealtime();
            }
        }
    }

    Tracker(zzf analytics, String trackingId, zzad rateLimiter) {
        super(analytics);
        if (trackingId != null) {
            this.zzyn.put("&tid", trackingId);
        }
        this.zzyn.put("useSecure", HSConsts.STATUS_INPROGRESS);
        this.zzyn.put("&a", Integer.toString(new Random().nextInt(ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED) + 1));
        if (rateLimiter == null) {
            this.zzIJ = new zzad("tracking");
        } else {
            this.zzIJ = rateLimiter;
        }
        this.zzIK = new zza(this, analytics);
    }

    private static void zza(Map<String, String> map, Map<String, String> map2) {
        zzu.zzu(map2);
        if (map != null) {
            for (Entry entry : map.entrySet()) {
                String zzb = zzb(entry);
                if (zzb != null) {
                    map2.put(zzb, entry.getValue());
                }
            }
        }
    }

    private static boolean zza(Entry<String, String> entry) {
        String str = (String) entry.getKey();
        String str2 = (String) entry.getValue();
        return str.startsWith("&") && str.length() >= 2;
    }

    private static String zzb(Entry<String, String> entry) {
        return !zza((Entry) entry) ? null : ((String) entry.getKey()).substring(1);
    }

    private static void zzb(Map<String, String> map, Map<String, String> map2) {
        zzu.zzu(map2);
        if (map != null) {
            for (Entry entry : map.entrySet()) {
                String zzb = zzb(entry);
                if (!(zzb == null || map2.containsKey(zzb))) {
                    map2.put(zzb, entry.getValue());
                }
            }
        }
    }

    private boolean zzho() {
        return this.zzIL != null;
    }

    static String zzp(Activity activity) {
        zzu.zzu(activity);
        Intent intent = activity.getIntent();
        if (intent == null) {
            return null;
        }
        CharSequence stringExtra = intent.getStringExtra("android.intent.extra.REFERRER_NAME");
        return !TextUtils.isEmpty(stringExtra) ? stringExtra : null;
    }

    public void enableAdvertisingIdCollection(boolean enabled) {
        this.zzIH = enabled;
    }

    public void enableAutoActivityTracking(boolean enabled) {
        this.zzIK.enableAutoActivityTracking(enabled);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void enableExceptionReporting(boolean r4) {
        /*
        r3 = this;
        monitor-enter(r3);
        r0 = r3.zzho();	 Catch:{ all -> 0x0026 }
        if (r0 != r4) goto L_0x0009;
    L_0x0007:
        monitor-exit(r3);	 Catch:{ all -> 0x0026 }
    L_0x0008:
        return;
    L_0x0009:
        if (r4 == 0) goto L_0x0029;
    L_0x000b:
        r0 = r3.getContext();	 Catch:{ all -> 0x0026 }
        r1 = java.lang.Thread.getDefaultUncaughtExceptionHandler();	 Catch:{ all -> 0x0026 }
        r2 = new com.google.android.gms.analytics.ExceptionReporter;	 Catch:{ all -> 0x0026 }
        r2.<init>(r3, r1, r0);	 Catch:{ all -> 0x0026 }
        r3.zzIL = r2;	 Catch:{ all -> 0x0026 }
        r0 = r3.zzIL;	 Catch:{ all -> 0x0026 }
        java.lang.Thread.setDefaultUncaughtExceptionHandler(r0);	 Catch:{ all -> 0x0026 }
        r0 = "Uncaught exceptions will be reported to Google Analytics";
        r3.zzaT(r0);	 Catch:{ all -> 0x0026 }
    L_0x0024:
        monitor-exit(r3);	 Catch:{ all -> 0x0026 }
        goto L_0x0008;
    L_0x0026:
        r0 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0026 }
        throw r0;
    L_0x0029:
        r0 = r3.zzIL;	 Catch:{ all -> 0x0026 }
        r0 = r0.zzhh();	 Catch:{ all -> 0x0026 }
        java.lang.Thread.setDefaultUncaughtExceptionHandler(r0);	 Catch:{ all -> 0x0026 }
        r0 = "Uncaught exceptions will not be reported to Google Analytics";
        r3.zzaT(r0);	 Catch:{ all -> 0x0026 }
        goto L_0x0024;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.analytics.Tracker.enableExceptionReporting(boolean):void");
    }

    public String get(String key) {
        zzia();
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        if (this.zzyn.containsKey(key)) {
            return (String) this.zzyn.get(key);
        }
        if (key.equals("&ul")) {
            return zzam.zza(Locale.getDefault());
        }
        if (key.equals("&cid")) {
            return zzhV().zziP();
        }
        if (key.equals("&sr")) {
            return zzhY().zzjF();
        }
        if (key.equals("&aid")) {
            return zzhX().zzix().zzsK();
        }
        if (key.equals("&an")) {
            return zzhX().zzix().zzjL();
        }
        if (key.equals("&av")) {
            return zzhX().zzix().zzjN();
        }
        return key.equals("&aiid") ? zzhX().zzix().zzwi() : null;
    }

    public void send(Map<String, String> params) {
        final long currentTimeMillis = zzhP().currentTimeMillis();
        if (zzhg().getAppOptOut()) {
            zzaU("AppOptOut is set to true. Not sending Google Analytics hit");
            return;
        }
        final boolean isDryRunEnabled = zzhg().isDryRunEnabled();
        final Map hashMap = new HashMap();
        zza(this.zzyn, hashMap);
        zza(params, hashMap);
        final boolean zze = zzam.zze((String) this.zzyn.get("useSecure"), true);
        zzb(this.zzII, hashMap);
        this.zzII.clear();
        final String str = (String) hashMap.get("t");
        if (TextUtils.isEmpty(str)) {
            zzhQ().zzg(hashMap, "Missing hit type parameter");
            return;
        }
        final String str2 = (String) hashMap.get("tid");
        if (TextUtils.isEmpty(str2)) {
            zzhQ().zzg(hashMap, "Missing tracking id parameter");
            return;
        }
        final boolean zzhp = zzhp();
        synchronized (this) {
            if ("screenview".equalsIgnoreCase(str) || "pageview".equalsIgnoreCase(str) || "appview".equalsIgnoreCase(str) || TextUtils.isEmpty(str)) {
                int parseInt = Integer.parseInt((String) this.zzyn.get("&a")) + 1;
                if (parseInt >= ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED) {
                    parseInt = 1;
                }
                this.zzyn.put("&a", Integer.toString(parseInt));
            }
        }
        zzhS().zze(new Runnable(this) {
            final /* synthetic */ Tracker zzIU;

            public void run() {
                boolean z = true;
                if (this.zzIU.zzIK.zzhq()) {
                    hashMap.put("sc", "start");
                }
                zzam.zzc(hashMap, "cid", this.zzIU.zzhg().getClientId());
                String str = (String) hashMap.get("sf");
                if (str != null) {
                    double zza = zzam.zza(str, 100.0d);
                    if (zzam.zza(zza, (String) hashMap.get("cid"))) {
                        this.zzIU.zzb("Sampling enabled. Hit sampled out. sample rate", Double.valueOf(zza));
                        return;
                    }
                }
                com.google.android.gms.analytics.internal.zza zzb = this.zzIU.zzhW();
                if (zzhp) {
                    zzam.zzb(hashMap, "ate", zzb.zzhy());
                    zzam.zzb(hashMap, "adid", zzb.zzhC());
                } else {
                    hashMap.remove("ate");
                    hashMap.remove("adid");
                }
                zznx zzix = this.zzIU.zzhX().zzix();
                zzam.zzb(hashMap, "an", zzix.zzjL());
                zzam.zzb(hashMap, "av", zzix.zzjN());
                zzam.zzb(hashMap, "aid", zzix.zzsK());
                zzam.zzb(hashMap, "aiid", zzix.zzwi());
                hashMap.put("v", HSConsts.STATUS_INPROGRESS);
                hashMap.put("_v", zze.zzJB);
                zzam.zzb(hashMap, "ul", this.zzIU.zzhY().zzjE().getLanguage());
                zzam.zzb(hashMap, HSFunnel.START_CSAT_RATING, this.zzIU.zzhY().zzjF());
                boolean z2 = str.equals("transaction") || str.equals("item");
                if (z2 || this.zzIU.zzIJ.zzkb()) {
                    long zzbj = zzam.zzbj((String) hashMap.get("ht"));
                    if (zzbj == 0) {
                        zzbj = currentTimeMillis;
                    }
                    if (isDryRunEnabled) {
                        this.zzIU.zzhQ().zzc("Dry run enabled. Would have sent hit", new zzab(this.zzIU, hashMap, zzbj, zze));
                        return;
                    }
                    String str2 = (String) hashMap.get("cid");
                    Map hashMap = new HashMap();
                    zzam.zza(hashMap, "uid", hashMap);
                    zzam.zza(hashMap, "an", hashMap);
                    zzam.zza(hashMap, "aid", hashMap);
                    zzam.zza(hashMap, "av", hashMap);
                    zzam.zza(hashMap, "aiid", hashMap);
                    String str3 = str2;
                    if (TextUtils.isEmpty((CharSequence) hashMap.get("adid"))) {
                        z = false;
                    }
                    hashMap.put("_s", String.valueOf(this.zzIU.zzhl().zza(new zzh(0, str2, str3, z, 0, hashMap))));
                    this.zzIU.zzhl().zza(new zzab(this.zzIU, hashMap, zzbj, zze));
                    return;
                }
                this.zzIU.zzhQ().zzg(hashMap, "Too many hits sent too quickly, rate limiting invoked");
            }
        });
    }

    public void set(String key, String value) {
        zzu.zzb((Object) key, (Object) "Key should be non-null");
        if (!TextUtils.isEmpty(key)) {
            this.zzyn.put(key, value);
        }
    }

    public void setAnonymizeIp(boolean anonymize) {
        set("&aip", zzam.zzH(anonymize));
    }

    public void setAppId(String appId) {
        set("&aid", appId);
    }

    public void setAppInstallerId(String appInstallerId) {
        set("&aiid", appInstallerId);
    }

    public void setAppName(String appName) {
        set("&an", appName);
    }

    public void setAppVersion(String appVersion) {
        set("&av", appVersion);
    }

    public void setCampaignParamsOnNextHit(Uri uri) {
        if (uri != null && !uri.isOpaque()) {
            Object queryParameter = uri.getQueryParameter("referrer");
            if (!TextUtils.isEmpty(queryParameter)) {
                Uri parse = Uri.parse("http://hostname/?" + queryParameter);
                String queryParameter2 = parse.getQueryParameter("utm_id");
                if (queryParameter2 != null) {
                    this.zzII.put("&ci", queryParameter2);
                }
                queryParameter2 = parse.getQueryParameter("anid");
                if (queryParameter2 != null) {
                    this.zzII.put("&anid", queryParameter2);
                }
                queryParameter2 = parse.getQueryParameter("utm_campaign");
                if (queryParameter2 != null) {
                    this.zzII.put("&cn", queryParameter2);
                }
                queryParameter2 = parse.getQueryParameter("utm_content");
                if (queryParameter2 != null) {
                    this.zzII.put("&cc", queryParameter2);
                }
                queryParameter2 = parse.getQueryParameter("utm_medium");
                if (queryParameter2 != null) {
                    this.zzII.put("&cm", queryParameter2);
                }
                queryParameter2 = parse.getQueryParameter("utm_source");
                if (queryParameter2 != null) {
                    this.zzII.put("&cs", queryParameter2);
                }
                queryParameter2 = parse.getQueryParameter("utm_term");
                if (queryParameter2 != null) {
                    this.zzII.put("&ck", queryParameter2);
                }
                queryParameter2 = parse.getQueryParameter("dclid");
                if (queryParameter2 != null) {
                    this.zzII.put("&dclid", queryParameter2);
                }
                queryParameter2 = parse.getQueryParameter("gclid");
                if (queryParameter2 != null) {
                    this.zzII.put("&gclid", queryParameter2);
                }
                String queryParameter3 = parse.getQueryParameter("aclid");
                if (queryParameter3 != null) {
                    this.zzII.put("&aclid", queryParameter3);
                }
            }
        }
    }

    public void setClientId(String clientId) {
        set("&cid", clientId);
    }

    public void setEncoding(String encoding) {
        set("&de", encoding);
    }

    public void setHostname(String hostname) {
        set("&dh", hostname);
    }

    public void setLanguage(String language) {
        set("&ul", language);
    }

    public void setLocation(String location) {
        set("&dl", location);
    }

    public void setPage(String page) {
        set("&dp", page);
    }

    public void setReferrer(String referrer) {
        set("&dr", referrer);
    }

    public void setSampleRate(double sampleRate) {
        set("&sf", Double.toString(sampleRate));
    }

    public void setScreenColors(String screenColors) {
        set("&sd", screenColors);
    }

    public void setScreenName(String screenName) {
        set("&cd", screenName);
    }

    public void setScreenResolution(int width, int height) {
        if (width >= 0 || height >= 0) {
            set("&sr", width + HSFunnel.OPEN_INBOX + height);
        } else {
            zzaW("Invalid width or height. The values should be non-negative.");
        }
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.zzIK.setSessionTimeout(1000 * sessionTimeout);
    }

    public void setTitle(String title) {
        set("&dt", title);
    }

    public void setUseSecure(boolean useSecure) {
        set("useSecure", zzam.zzH(useSecure));
    }

    public void setViewportSize(String viewportSize) {
        set("&vp", viewportSize);
    }

    void zza(zzal com_google_android_gms_analytics_internal_zzal) {
        zzaT("Loading Tracker config values");
        this.zzIM = com_google_android_gms_analytics_internal_zzal;
        if (this.zzIM.zzky()) {
            String trackingId = this.zzIM.getTrackingId();
            set("&tid", trackingId);
            zza("trackingId loaded", trackingId);
        }
        if (this.zzIM.zzkz()) {
            trackingId = Double.toString(this.zzIM.zzkA());
            set("&sf", trackingId);
            zza("Sample frequency loaded", trackingId);
        }
        if (this.zzIM.zzkB()) {
            int sessionTimeout = this.zzIM.getSessionTimeout();
            setSessionTimeout((long) sessionTimeout);
            zza("Session timeout loaded", Integer.valueOf(sessionTimeout));
        }
        if (this.zzIM.zzkC()) {
            boolean zzkD = this.zzIM.zzkD();
            enableAutoActivityTracking(zzkD);
            zza("Auto activity tracking loaded", Boolean.valueOf(zzkD));
        }
        if (this.zzIM.zzkE()) {
            zzkD = this.zzIM.zzkF();
            if (zzkD) {
                set("&aip", HSConsts.STATUS_INPROGRESS);
            }
            zza("Anonymize ip loaded", Boolean.valueOf(zzkD));
        }
        enableExceptionReporting(this.zzIM.zzkG());
    }

    protected void zzhn() {
        this.zzIK.zza();
        String zzjL = zzhm().zzjL();
        if (zzjL != null) {
            set("&an", zzjL);
        }
        zzjL = zzhm().zzjN();
        if (zzjL != null) {
            set("&av", zzjL);
        }
    }

    boolean zzhp() {
        return this.zzIH;
    }
}
