package com.google.android.gms.common.api;

import com.google.android.gms.common.data.DataHolder;

public abstract class zzb<L> implements com.google.android.gms.common.api.zzi.zzb<L> {
    private final DataHolder zzWu;

    protected zzb(DataHolder dataHolder) {
        this.zzWu = dataHolder;
    }

    protected abstract void zza(L l, DataHolder dataHolder);

    public void zzmw() {
        if (this.zzWu != null) {
            this.zzWu.close();
        }
    }

    public final void zzn(L l) {
        zza(l, this.zzWu);
    }
}
