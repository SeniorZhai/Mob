package com.google.android.gms.internal;

import android.content.Context;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.tagmanager.zzbg;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class zzqa {
    private final Context mContext;
    private String zzaLc;
    private final zzqh zzaPe;
    Map<String, zzc<com.google.android.gms.internal.zzqf.zzc>> zzaPf;
    private final Map<String, zzqp> zzaPg;
    private final zzlb zzpw;

    public interface zza {
        void zza(zzqe com_google_android_gms_internal_zzqe);
    }

    class zzb extends zzqn {
        final /* synthetic */ zzqa zzaPj;
        private final zza zzaPk;

        zzb(zzqa com_google_android_gms_internal_zzqa, zzqd com_google_android_gms_internal_zzqd, zzqb com_google_android_gms_internal_zzqb, zza com_google_android_gms_internal_zzqa_zza) {
            this.zzaPj = com_google_android_gms_internal_zzqa;
            super(com_google_android_gms_internal_zzqd, com_google_android_gms_internal_zzqb);
            this.zzaPk = com_google_android_gms_internal_zzqa_zza;
        }

        protected com.google.android.gms.internal.zzqn.zzb zza(zzpy com_google_android_gms_internal_zzpy) {
            return null;
        }

        protected void zza(zzqe com_google_android_gms_internal_zzqe) {
            com.google.android.gms.internal.zzqe.zza zzAg = com_google_android_gms_internal_zzqe.zzAg();
            this.zzaPj.zza(zzAg);
            if (zzAg.getStatus() != Status.zzXP || zzAg.zzAh() != com.google.android.gms.internal.zzqe.zza.zza.NETWORK || zzAg.zzAi() == null || zzAg.zzAi().length <= 0) {
                zzbg.zzaB("Response status: " + (zzAg.getStatus().isSuccess() ? "SUCCESS" : "FAILURE"));
                if (zzAg.getStatus().isSuccess()) {
                    zzbg.zzaB("Response source: " + zzAg.zzAh().toString());
                    zzbg.zzaB("Response size: " + zzAg.zzAi().length);
                }
                this.zzaPj.zza(zzAg.zzAj(), this.zzaPk);
                return;
            }
            this.zzaPj.zzaPe.zze(zzAg.zzAj().zzAb(), zzAg.zzAi());
            zzbg.zzaB("Resource successfully load from Network.");
            this.zzaPk.zza(com_google_android_gms_internal_zzqe);
        }
    }

    static class zzc<T> {
        private T mData;
        private Status zzOt;
        private long zzaPl;

        public zzc(Status status, T t, long j) {
            this.zzOt = status;
            this.mData = t;
            this.zzaPl = j;
        }

        public long zzAe() {
            return this.zzaPl;
        }

        public void zzO(T t) {
            this.mData = t;
        }

        public void zzU(long j) {
            this.zzaPl = j;
        }

        public void zzaV(Status status) {
            this.zzOt = status;
        }
    }

    public zzqa(Context context) {
        this(context, new HashMap(), new zzqh(context), zzld.zzoQ());
    }

    zzqa(Context context, Map<String, zzqp> map, zzqh com_google_android_gms_internal_zzqh, zzlb com_google_android_gms_internal_zzlb) {
        this.zzaLc = null;
        this.zzaPf = new HashMap();
        this.mContext = context;
        this.zzpw = com_google_android_gms_internal_zzlb;
        this.zzaPe = com_google_android_gms_internal_zzqh;
        this.zzaPg = map;
    }

    private void zza(zzqd com_google_android_gms_internal_zzqd, zza com_google_android_gms_internal_zzqa_zza) {
        boolean z = true;
        List zzAf = com_google_android_gms_internal_zzqd.zzAf();
        if (zzAf.size() != 1) {
            z = false;
        }
        zzu.zzV(z);
        zza((zzpy) zzAf.get(0), com_google_android_gms_internal_zzqa_zza);
    }

    void zza(final zzpy com_google_android_gms_internal_zzpy, final zza com_google_android_gms_internal_zzqa_zza) {
        this.zzaPe.zza(com_google_android_gms_internal_zzpy.zzAb(), com_google_android_gms_internal_zzpy.zzzZ(), zzqc.zzaPm, new zzqg(this) {
            final /* synthetic */ zzqa zzaPj;

            public void zza(Status status, Object obj, Integer num, long j) {
                com.google.android.gms.internal.zzqe.zza com_google_android_gms_internal_zzqe_zza;
                if (status.isSuccess()) {
                    com_google_android_gms_internal_zzqe_zza = new com.google.android.gms.internal.zzqe.zza(Status.zzXP, com_google_android_gms_internal_zzpy, null, (com.google.android.gms.internal.zzqf.zzc) obj, num == zzqh.zzaPM ? com.google.android.gms.internal.zzqe.zza.zza.DEFAULT : com.google.android.gms.internal.zzqe.zza.zza.DISK, j);
                } else {
                    com_google_android_gms_internal_zzqe_zza = new com.google.android.gms.internal.zzqe.zza(new Status(16, "There is no valid resource for the container: " + com_google_android_gms_internal_zzpy.getContainerId()), null, com.google.android.gms.internal.zzqe.zza.zza.DISK);
                }
                com_google_android_gms_internal_zzqa_zza.zza(new zzqe(com_google_android_gms_internal_zzqe_zza));
            }
        });
    }

    void zza(zzqd com_google_android_gms_internal_zzqd, zza com_google_android_gms_internal_zzqa_zza, zzqn com_google_android_gms_internal_zzqn) {
        Object obj = null;
        for (zzpy com_google_android_gms_internal_zzpy : com_google_android_gms_internal_zzqd.zzAf()) {
            zzc com_google_android_gms_internal_zzqa_zzc = (zzc) this.zzaPf.get(com_google_android_gms_internal_zzpy.getContainerId());
            obj = (com_google_android_gms_internal_zzqa_zzc != null ? com_google_android_gms_internal_zzqa_zzc.zzAe() : this.zzaPe.zzfa(com_google_android_gms_internal_zzpy.getContainerId())) + 900000 < this.zzpw.currentTimeMillis() ? 1 : obj;
        }
        if (obj != null) {
            zzqp com_google_android_gms_internal_zzqp;
            zzqp com_google_android_gms_internal_zzqp2 = (zzqp) this.zzaPg.get(com_google_android_gms_internal_zzqd.getId());
            if (com_google_android_gms_internal_zzqp2 == null) {
                com_google_android_gms_internal_zzqp2 = this.zzaLc == null ? new zzqp() : new zzqp(this.zzaLc);
                this.zzaPg.put(com_google_android_gms_internal_zzqd.getId(), com_google_android_gms_internal_zzqp2);
                com_google_android_gms_internal_zzqp = com_google_android_gms_internal_zzqp2;
            } else {
                com_google_android_gms_internal_zzqp = com_google_android_gms_internal_zzqp2;
            }
            com_google_android_gms_internal_zzqp.zza(this.mContext, com_google_android_gms_internal_zzqd, 0, com_google_android_gms_internal_zzqn);
            return;
        }
        zza(com_google_android_gms_internal_zzqd, com_google_android_gms_internal_zzqa_zza);
    }

    void zza(com.google.android.gms.internal.zzqe.zza com_google_android_gms_internal_zzqe_zza) {
        String containerId = com_google_android_gms_internal_zzqe_zza.zzAj().getContainerId();
        Status status = com_google_android_gms_internal_zzqe_zza.getStatus();
        com.google.android.gms.internal.zzqf.zzc zzAk = com_google_android_gms_internal_zzqe_zza.zzAk();
        if (this.zzaPf.containsKey(containerId)) {
            zzc com_google_android_gms_internal_zzqa_zzc = (zzc) this.zzaPf.get(containerId);
            com_google_android_gms_internal_zzqa_zzc.zzU(this.zzpw.currentTimeMillis());
            if (status == Status.zzXP) {
                com_google_android_gms_internal_zzqa_zzc.zzaV(status);
                com_google_android_gms_internal_zzqa_zzc.zzO(zzAk);
                return;
            }
            return;
        }
        this.zzaPf.put(containerId, new zzc(status, zzAk, this.zzpw.currentTimeMillis()));
    }

    public void zza(String str, Integer num, String str2, zza com_google_android_gms_internal_zzqa_zza) {
        zzqd zzb = new zzqd().zzb(new zzpy(str, num, str2, false));
        zza(zzb, com_google_android_gms_internal_zzqa_zza, new zzb(this, zzb, zzqc.zzaPm, com_google_android_gms_internal_zzqa_zza));
    }

    public void zzeU(String str) {
        this.zzaLc = str;
    }
}
