package com.google.android.gms.internal;

import com.google.android.gms.common.internal.zzu;
import java.util.ArrayList;
import java.util.List;

public abstract class zznr<T extends zznr> {
    private final zzns zzaEe;
    protected final zzno zzaEf;
    private final List<zznp> zzaEg = new ArrayList();

    protected zznr(zzns com_google_android_gms_internal_zzns, zzlb com_google_android_gms_internal_zzlb) {
        zzu.zzu(com_google_android_gms_internal_zzns);
        this.zzaEe = com_google_android_gms_internal_zzns;
        zzno com_google_android_gms_internal_zzno = new zzno(this, com_google_android_gms_internal_zzlb);
        com_google_android_gms_internal_zzno.zzvZ();
        this.zzaEf = com_google_android_gms_internal_zzno;
    }

    protected void zza(zzno com_google_android_gms_internal_zzno) {
    }

    protected void zzd(zzno com_google_android_gms_internal_zzno) {
        for (zznp zza : this.zzaEg) {
            zza.zza(this, com_google_android_gms_internal_zzno);
        }
    }

    public zzno zzhc() {
        zzno zzvP = this.zzaEf.zzvP();
        zzd(zzvP);
        return zzvP;
    }

    protected zzns zzvX() {
        return this.zzaEe;
    }

    public zzno zzwa() {
        return this.zzaEf;
    }

    public List<zznu> zzwb() {
        return this.zzaEf.zzvR();
    }
}
