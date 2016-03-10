package com.google.android.gms.internal;

import com.google.android.gms.common.internal.zzu;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class zzno {
    private final zznr zzaDU;
    private boolean zzaDV;
    private long zzaDW;
    private long zzaDX;
    private long zzaDY;
    private long zzaDZ;
    private long zzaEa;
    private boolean zzaEb;
    private final Map<Class<? extends zznq>, zznq> zzaEc;
    private final List<zznu> zzaEd;
    private final zzlb zzpw;

    zzno(zzno com_google_android_gms_internal_zzno) {
        this.zzaDU = com_google_android_gms_internal_zzno.zzaDU;
        this.zzpw = com_google_android_gms_internal_zzno.zzpw;
        this.zzaDW = com_google_android_gms_internal_zzno.zzaDW;
        this.zzaDX = com_google_android_gms_internal_zzno.zzaDX;
        this.zzaDY = com_google_android_gms_internal_zzno.zzaDY;
        this.zzaDZ = com_google_android_gms_internal_zzno.zzaDZ;
        this.zzaEa = com_google_android_gms_internal_zzno.zzaEa;
        this.zzaEd = new ArrayList(com_google_android_gms_internal_zzno.zzaEd);
        this.zzaEc = new HashMap(com_google_android_gms_internal_zzno.zzaEc.size());
        for (Entry entry : com_google_android_gms_internal_zzno.zzaEc.entrySet()) {
            zznq zzf = zzf((Class) entry.getKey());
            ((zznq) entry.getValue()).zza(zzf);
            this.zzaEc.put(entry.getKey(), zzf);
        }
    }

    zzno(zznr com_google_android_gms_internal_zznr, zzlb com_google_android_gms_internal_zzlb) {
        zzu.zzu(com_google_android_gms_internal_zznr);
        zzu.zzu(com_google_android_gms_internal_zzlb);
        this.zzaDU = com_google_android_gms_internal_zznr;
        this.zzpw = com_google_android_gms_internal_zzlb;
        this.zzaDZ = 1800000;
        this.zzaEa = 3024000000L;
        this.zzaEc = new HashMap();
        this.zzaEd = new ArrayList();
    }

    private static <T extends zznq> T zzf(Class<T> cls) {
        try {
            return (zznq) cls.newInstance();
        } catch (Throwable e) {
            throw new IllegalArgumentException("dataType doesn't have default constructor", e);
        } catch (Throwable e2) {
            throw new IllegalArgumentException("dataType default constructor is not accessible", e2);
        }
    }

    public void zzL(long j) {
        this.zzaDX = j;
    }

    public void zzb(zznq com_google_android_gms_internal_zznq) {
        zzu.zzu(com_google_android_gms_internal_zznq);
        Class cls = com_google_android_gms_internal_zznq.getClass();
        if (cls.getSuperclass() != zznq.class) {
            throw new IllegalArgumentException();
        }
        com_google_android_gms_internal_zznq.zza(zze(cls));
    }

    public <T extends zznq> T zzd(Class<T> cls) {
        return (zznq) this.zzaEc.get(cls);
    }

    public <T extends zznq> T zze(Class<T> cls) {
        zznq com_google_android_gms_internal_zznq = (zznq) this.zzaEc.get(cls);
        if (com_google_android_gms_internal_zznq != null) {
            return com_google_android_gms_internal_zznq;
        }
        T zzf = zzf(cls);
        this.zzaEc.put(cls, zzf);
        return zzf;
    }

    public zzno zzvP() {
        return new zzno(this);
    }

    public Collection<zznq> zzvQ() {
        return this.zzaEc.values();
    }

    public List<zznu> zzvR() {
        return this.zzaEd;
    }

    public long zzvS() {
        return this.zzaDW;
    }

    public void zzvT() {
        zzvX().zze(this);
    }

    public boolean zzvU() {
        return this.zzaDV;
    }

    void zzvV() {
        this.zzaDY = this.zzpw.elapsedRealtime();
        if (this.zzaDX != 0) {
            this.zzaDW = this.zzaDX;
        } else {
            this.zzaDW = this.zzpw.currentTimeMillis();
        }
        this.zzaDV = true;
    }

    zznr zzvW() {
        return this.zzaDU;
    }

    zzns zzvX() {
        return this.zzaDU.zzvX();
    }

    boolean zzvY() {
        return this.zzaEb;
    }

    void zzvZ() {
        this.zzaEb = true;
    }
}
