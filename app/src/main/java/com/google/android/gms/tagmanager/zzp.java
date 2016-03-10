package com.google.android.gms.tagmanager;

import android.content.Context;
import android.os.Looper;
import com.google.android.gms.common.api.AbstractPendingResult;
import com.google.android.gms.common.api.Releasable;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.internal.zzaf.zzj;
import com.google.android.gms.internal.zzlb;
import com.google.android.gms.internal.zzld;
import com.google.android.gms.internal.zzqa;
import com.google.android.gms.internal.zzqe;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.BuildConfig;

public class zzp extends AbstractPendingResult<ContainerHolder> {
    private final Context mContext;
    private final Looper zzWt;
    private long zzaKD;
    private final TagManager zzaKK;
    private final zzd zzaKN;
    private final zzcd zzaKO;
    private final int zzaKP;
    private zzf zzaKQ;
    private zzqa zzaKR;
    private volatile zzo zzaKS;
    private volatile boolean zzaKT;
    private zzj zzaKU;
    private String zzaKV;
    private zze zzaKW;
    private zza zzaKX;
    private final String zzaKy;
    private final zzlb zzpw;

    interface zze extends Releasable {
        void zza(zzbf<zzj> com_google_android_gms_tagmanager_zzbf_com_google_android_gms_internal_zzaf_zzj);

        void zzem(String str);

        void zzf(long j, String str);
    }

    interface zzf extends Releasable {
        void zza(zzbf<com.google.android.gms.internal.zzpx.zza> com_google_android_gms_tagmanager_zzbf_com_google_android_gms_internal_zzpx_zza);

        void zzb(com.google.android.gms.internal.zzpx.zza com_google_android_gms_internal_zzpx_zza);

        com.google.android.gms.internal.zzqf.zzc zziR(int i);

        void zzyw();
    }

    interface zza {
        boolean zzb(Container container);
    }

    private class zzb implements zzbf<com.google.android.gms.internal.zzpx.zza> {
        final /* synthetic */ zzp zzaKZ;

        private zzb(zzp com_google_android_gms_tagmanager_zzp) {
            this.zzaKZ = com_google_android_gms_tagmanager_zzp;
        }

        public void zza(com.google.android.gms.internal.zzpx.zza com_google_android_gms_internal_zzpx_zza) {
            zzj com_google_android_gms_internal_zzaf_zzj;
            if (com_google_android_gms_internal_zzpx_zza.zzaPa != null) {
                com_google_android_gms_internal_zzaf_zzj = com_google_android_gms_internal_zzpx_zza.zzaPa;
            } else {
                com.google.android.gms.internal.zzaf.zzf com_google_android_gms_internal_zzaf_zzf = com_google_android_gms_internal_zzpx_zza.zziO;
                com_google_android_gms_internal_zzaf_zzj = new zzj();
                com_google_android_gms_internal_zzaf_zzj.zziO = com_google_android_gms_internal_zzaf_zzf;
                com_google_android_gms_internal_zzaf_zzj.zziN = null;
                com_google_android_gms_internal_zzaf_zzj.zziP = com_google_android_gms_internal_zzaf_zzf.version;
            }
            this.zzaKZ.zza(com_google_android_gms_internal_zzaf_zzj, com_google_android_gms_internal_zzpx_zza.zzaOZ, true);
        }

        public void zza(com.google.android.gms.tagmanager.zzbf.zza com_google_android_gms_tagmanager_zzbf_zza) {
            if (!this.zzaKZ.zzaKT) {
                this.zzaKZ.zzR(0);
            }
        }

        public void zzyv() {
        }

        public /* synthetic */ void zzz(Object obj) {
            zza((com.google.android.gms.internal.zzpx.zza) obj);
        }
    }

    private class zzc implements zzbf<zzj> {
        final /* synthetic */ zzp zzaKZ;

        private zzc(zzp com_google_android_gms_tagmanager_zzp) {
            this.zzaKZ = com_google_android_gms_tagmanager_zzp;
        }

        public void zza(com.google.android.gms.tagmanager.zzbf.zza com_google_android_gms_tagmanager_zzbf_zza) {
            synchronized (this.zzaKZ) {
                if (!this.zzaKZ.isReady()) {
                    if (this.zzaKZ.zzaKS != null) {
                        this.zzaKZ.setResult(this.zzaKZ.zzaKS);
                    } else {
                        this.zzaKZ.setResult(this.zzaKZ.zzaU(Status.zzXS));
                    }
                }
            }
            this.zzaKZ.zzR(3600000);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void zzb(com.google.android.gms.internal.zzaf.zzj r6) {
            /*
            r5 = this;
            r1 = r5.zzaKZ;
            monitor-enter(r1);
            r0 = r6.zziO;	 Catch:{ all -> 0x0066 }
            if (r0 != 0) goto L_0x002a;
        L_0x0007:
            r0 = r5.zzaKZ;	 Catch:{ all -> 0x0066 }
            r0 = r0.zzaKU;	 Catch:{ all -> 0x0066 }
            r0 = r0.zziO;	 Catch:{ all -> 0x0066 }
            if (r0 != 0) goto L_0x0020;
        L_0x0011:
            r0 = "Current resource is null; network resource is also null";
            com.google.android.gms.tagmanager.zzbg.zzaz(r0);	 Catch:{ all -> 0x0066 }
            r0 = r5.zzaKZ;	 Catch:{ all -> 0x0066 }
            r2 = 3600000; // 0x36ee80 float:5.044674E-39 double:1.7786363E-317;
            r0.zzR(r2);	 Catch:{ all -> 0x0066 }
            monitor-exit(r1);	 Catch:{ all -> 0x0066 }
        L_0x001f:
            return;
        L_0x0020:
            r0 = r5.zzaKZ;	 Catch:{ all -> 0x0066 }
            r0 = r0.zzaKU;	 Catch:{ all -> 0x0066 }
            r0 = r0.zziO;	 Catch:{ all -> 0x0066 }
            r6.zziO = r0;	 Catch:{ all -> 0x0066 }
        L_0x002a:
            r0 = r5.zzaKZ;	 Catch:{ all -> 0x0066 }
            r2 = r5.zzaKZ;	 Catch:{ all -> 0x0066 }
            r2 = r2.zzpw;	 Catch:{ all -> 0x0066 }
            r2 = r2.currentTimeMillis();	 Catch:{ all -> 0x0066 }
            r4 = 0;
            r0.zza(r6, r2, r4);	 Catch:{ all -> 0x0066 }
            r0 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0066 }
            r0.<init>();	 Catch:{ all -> 0x0066 }
            r2 = "setting refresh time to current time: ";
            r0 = r0.append(r2);	 Catch:{ all -> 0x0066 }
            r2 = r5.zzaKZ;	 Catch:{ all -> 0x0066 }
            r2 = r2.zzaKD;	 Catch:{ all -> 0x0066 }
            r0 = r0.append(r2);	 Catch:{ all -> 0x0066 }
            r0 = r0.toString();	 Catch:{ all -> 0x0066 }
            com.google.android.gms.tagmanager.zzbg.zzaB(r0);	 Catch:{ all -> 0x0066 }
            r0 = r5.zzaKZ;	 Catch:{ all -> 0x0066 }
            r0 = r0.zzyu();	 Catch:{ all -> 0x0066 }
            if (r0 != 0) goto L_0x0064;
        L_0x005f:
            r0 = r5.zzaKZ;	 Catch:{ all -> 0x0066 }
            r0.zza(r6);	 Catch:{ all -> 0x0066 }
        L_0x0064:
            monitor-exit(r1);	 Catch:{ all -> 0x0066 }
            goto L_0x001f;
        L_0x0066:
            r0 = move-exception;
            monitor-exit(r1);	 Catch:{ all -> 0x0066 }
            throw r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.tagmanager.zzp.zzc.zzb(com.google.android.gms.internal.zzaf$zzj):void");
        }

        public void zzyv() {
        }

        public /* synthetic */ void zzz(Object obj) {
            zzb((zzj) obj);
        }
    }

    private class zzd implements com.google.android.gms.tagmanager.zzo.zza {
        final /* synthetic */ zzp zzaKZ;

        private zzd(zzp com_google_android_gms_tagmanager_zzp) {
            this.zzaKZ = com_google_android_gms_tagmanager_zzp;
        }

        public void zzej(String str) {
            this.zzaKZ.zzej(str);
        }

        public String zzyo() {
            return this.zzaKZ.zzyo();
        }

        public void zzyq() {
            if (this.zzaKZ.zzaKO.zzkb()) {
                this.zzaKZ.zzR(0);
            }
        }
    }

    zzp(Context context, TagManager tagManager, Looper looper, String str, int i, zzf com_google_android_gms_tagmanager_zzp_zzf, zze com_google_android_gms_tagmanager_zzp_zze, zzqa com_google_android_gms_internal_zzqa, zzlb com_google_android_gms_internal_zzlb, zzcd com_google_android_gms_tagmanager_zzcd) {
        super(looper == null ? Looper.getMainLooper() : looper);
        this.mContext = context;
        this.zzaKK = tagManager;
        if (looper == null) {
            looper = Looper.getMainLooper();
        }
        this.zzWt = looper;
        this.zzaKy = str;
        this.zzaKP = i;
        this.zzaKQ = com_google_android_gms_tagmanager_zzp_zzf;
        this.zzaKW = com_google_android_gms_tagmanager_zzp_zze;
        this.zzaKR = com_google_android_gms_internal_zzqa;
        this.zzaKN = new zzd();
        this.zzaKU = new zzj();
        this.zzpw = com_google_android_gms_internal_zzlb;
        this.zzaKO = com_google_android_gms_tagmanager_zzcd;
        if (zzyu()) {
            zzej(zzcb.zzzf().zzzh());
        }
    }

    public zzp(Context context, TagManager tagManager, Looper looper, String str, int i, zzs com_google_android_gms_tagmanager_zzs) {
        this(context, tagManager, looper, str, i, new zzcn(context, str), new zzcm(context, str, com_google_android_gms_tagmanager_zzs), new zzqa(context), zzld.zzoQ(), new zzbe(30, 900000, Constants.NOTIFICATION_BANNER_TIMEOUT, "refreshing", zzld.zzoQ()));
        this.zzaKR.zzeU(com_google_android_gms_tagmanager_zzs.zzyx());
    }

    private synchronized void zzR(long j) {
        if (this.zzaKW == null) {
            zzbg.zzaC("Refresh requested, but no network load scheduler.");
        } else {
            this.zzaKW.zzf(j, this.zzaKU.zziP);
        }
    }

    private synchronized void zza(zzj com_google_android_gms_internal_zzaf_zzj) {
        if (this.zzaKQ != null) {
            com.google.android.gms.internal.zzpx.zza com_google_android_gms_internal_zzpx_zza = new com.google.android.gms.internal.zzpx.zza();
            com_google_android_gms_internal_zzpx_zza.zzaOZ = this.zzaKD;
            com_google_android_gms_internal_zzpx_zza.zziO = new com.google.android.gms.internal.zzaf.zzf();
            com_google_android_gms_internal_zzpx_zza.zzaPa = com_google_android_gms_internal_zzaf_zzj;
            this.zzaKQ.zzb(com_google_android_gms_internal_zzpx_zza);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void zza(com.google.android.gms.internal.zzaf.zzj r9, long r10, boolean r12) {
        /*
        r8 = this;
        r6 = 43200000; // 0x2932e00 float:2.1626111E-37 double:2.1343636E-316;
        monitor-enter(r8);
        if (r12 == 0) goto L_0x000c;
    L_0x0006:
        r0 = r8.zzaKT;	 Catch:{ all -> 0x006a }
        if (r0 == 0) goto L_0x000c;
    L_0x000a:
        monitor-exit(r8);
        return;
    L_0x000c:
        r0 = r8.isReady();	 Catch:{ all -> 0x006a }
        if (r0 == 0) goto L_0x0016;
    L_0x0012:
        r0 = r8.zzaKS;	 Catch:{ all -> 0x006a }
        if (r0 != 0) goto L_0x0016;
    L_0x0016:
        r8.zzaKU = r9;	 Catch:{ all -> 0x006a }
        r8.zzaKD = r10;	 Catch:{ all -> 0x006a }
        r0 = 0;
        r2 = 43200000; // 0x2932e00 float:2.1626111E-37 double:2.1343636E-316;
        r4 = r8.zzaKD;	 Catch:{ all -> 0x006a }
        r4 = r4 + r6;
        r6 = r8.zzpw;	 Catch:{ all -> 0x006a }
        r6 = r6.currentTimeMillis();	 Catch:{ all -> 0x006a }
        r4 = r4 - r6;
        r2 = java.lang.Math.min(r2, r4);	 Catch:{ all -> 0x006a }
        r0 = java.lang.Math.max(r0, r2);	 Catch:{ all -> 0x006a }
        r8.zzR(r0);	 Catch:{ all -> 0x006a }
        r0 = new com.google.android.gms.tagmanager.Container;	 Catch:{ all -> 0x006a }
        r1 = r8.mContext;	 Catch:{ all -> 0x006a }
        r2 = r8.zzaKK;	 Catch:{ all -> 0x006a }
        r2 = r2.getDataLayer();	 Catch:{ all -> 0x006a }
        r3 = r8.zzaKy;	 Catch:{ all -> 0x006a }
        r4 = r10;
        r6 = r9;
        r0.<init>(r1, r2, r3, r4, r6);	 Catch:{ all -> 0x006a }
        r1 = r8.zzaKS;	 Catch:{ all -> 0x006a }
        if (r1 != 0) goto L_0x006d;
    L_0x0049:
        r1 = new com.google.android.gms.tagmanager.zzo;	 Catch:{ all -> 0x006a }
        r2 = r8.zzaKK;	 Catch:{ all -> 0x006a }
        r3 = r8.zzWt;	 Catch:{ all -> 0x006a }
        r4 = r8.zzaKN;	 Catch:{ all -> 0x006a }
        r1.<init>(r2, r3, r0, r4);	 Catch:{ all -> 0x006a }
        r8.zzaKS = r1;	 Catch:{ all -> 0x006a }
    L_0x0056:
        r1 = r8.isReady();	 Catch:{ all -> 0x006a }
        if (r1 != 0) goto L_0x000a;
    L_0x005c:
        r1 = r8.zzaKX;	 Catch:{ all -> 0x006a }
        r0 = r1.zzb(r0);	 Catch:{ all -> 0x006a }
        if (r0 == 0) goto L_0x000a;
    L_0x0064:
        r0 = r8.zzaKS;	 Catch:{ all -> 0x006a }
        r8.setResult(r0);	 Catch:{ all -> 0x006a }
        goto L_0x000a;
    L_0x006a:
        r0 = move-exception;
        monitor-exit(r8);
        throw r0;
    L_0x006d:
        r1 = r8.zzaKS;	 Catch:{ all -> 0x006a }
        r1.zza(r0);	 Catch:{ all -> 0x006a }
        goto L_0x0056;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.tagmanager.zzp.zza(com.google.android.gms.internal.zzaf$zzj, long, boolean):void");
    }

    private void zzam(final boolean z) {
        this.zzaKQ.zza(new zzb());
        this.zzaKW.zza(new zzc());
        com.google.android.gms.internal.zzqf.zzc zziR = this.zzaKQ.zziR(this.zzaKP);
        if (zziR != null) {
            this.zzaKS = new zzo(this.zzaKK, this.zzWt, new Container(this.mContext, this.zzaKK.getDataLayer(), this.zzaKy, 0, zziR), this.zzaKN);
        }
        this.zzaKX = new zza(this) {
            final /* synthetic */ zzp zzaKZ;

            public boolean zzb(Container container) {
                return z ? container.getLastRefreshTime() + 43200000 >= this.zzaKZ.zzpw.currentTimeMillis() : !container.isDefault();
            }
        };
        if (zzyu()) {
            this.zzaKW.zzf(0, BuildConfig.FLAVOR);
        } else {
            this.zzaKQ.zzyw();
        }
    }

    private boolean zzyu() {
        zzcb zzzf = zzcb.zzzf();
        return (zzzf.zzzg() == zza.CONTAINER || zzzf.zzzg() == zza.CONTAINER_DEBUG) && this.zzaKy.equals(zzzf.getContainerId());
    }

    protected /* synthetic */ Result createFailedResult(Status x0) {
        return zzaU(x0);
    }

    public void load(final String resourceIdParameterName) {
        this.zzaKR.zza(this.zzaKy, this.zzaKP != -1 ? Integer.valueOf(this.zzaKP) : null, resourceIdParameterName, new com.google.android.gms.internal.zzqa.zza(this) {
            final /* synthetic */ zzp zzaKZ;

            public void zza(zzqe com_google_android_gms_internal_zzqe) {
                if (com_google_android_gms_internal_zzqe.getStatus() != Status.zzXP) {
                    zzbg.zzaz("Load request failed for the container " + this.zzaKZ.zzaKy);
                    this.zzaKZ.setResult(this.zzaKZ.zzaU(Status.zzXR));
                    return;
                }
                com.google.android.gms.internal.zzqf.zzc zzAk = com_google_android_gms_internal_zzqe.zzAg().zzAk();
                if (zzAk == null) {
                    String str = "Response doesn't have the requested container";
                    zzbg.zzaz(str);
                    this.zzaKZ.setResult(this.zzaKZ.zzaU(new Status(8, str, null)));
                    return;
                }
                this.zzaKZ.zzaKS = new zzo(this.zzaKZ.zzaKK, this.zzaKZ.zzWt, new Container(this.zzaKZ.mContext, this.zzaKZ.zzaKK.getDataLayer(), this.zzaKZ.zzaKy, com_google_android_gms_internal_zzqe.zzAg().zzAl(), zzAk), new com.google.android.gms.tagmanager.zzo.zza(this) {
                    final /* synthetic */ AnonymousClass1 zzaLa;

                    {
                        this.zzaLa = r1;
                    }

                    public void zzej(String str) {
                        this.zzaLa.zzaKZ.zzej(str);
                    }

                    public String zzyo() {
                        return this.zzaLa.zzaKZ.zzyo();
                    }

                    public void zzyq() {
                        if (this.zzaLa.zzaKZ.zzaKO.zzkb()) {
                            this.zzaLa.zzaKZ.load(resourceIdParameterName);
                        }
                    }
                });
                this.zzaKZ.setResult(this.zzaKZ.zzaKS);
            }
        });
    }

    protected ContainerHolder zzaU(Status status) {
        if (this.zzaKS != null) {
            return this.zzaKS;
        }
        if (status == Status.zzXS) {
            zzbg.zzaz("timer expired: setting result to failure");
        }
        return new zzo(status);
    }

    synchronized void zzej(String str) {
        this.zzaKV = str;
        if (this.zzaKW != null) {
            this.zzaKW.zzem(str);
        }
    }

    synchronized String zzyo() {
        return this.zzaKV;
    }

    public void zzyr() {
        com.google.android.gms.internal.zzqf.zzc zziR = this.zzaKQ.zziR(this.zzaKP);
        if (zziR != null) {
            setResult(new zzo(this.zzaKK, this.zzWt, new Container(this.mContext, this.zzaKK.getDataLayer(), this.zzaKy, 0, zziR), new com.google.android.gms.tagmanager.zzo.zza(this) {
                final /* synthetic */ zzp zzaKZ;

                {
                    this.zzaKZ = r1;
                }

                public void zzej(String str) {
                    this.zzaKZ.zzej(str);
                }

                public String zzyo() {
                    return this.zzaKZ.zzyo();
                }

                public void zzyq() {
                    zzbg.zzaC("Refresh ignored: container loaded as default only.");
                }
            }));
        } else {
            String str = "Default was requested, but no default container was found";
            zzbg.zzaz(str);
            setResult(zzaU(new Status(10, str, null)));
        }
        this.zzaKW = null;
        this.zzaKQ = null;
    }

    public void zzys() {
        zzam(false);
    }

    public void zzyt() {
        zzam(true);
    }
}
