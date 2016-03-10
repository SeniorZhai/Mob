package com.google.android.gms.common.api;

import com.google.android.gms.common.data.DataHolder;

public abstract class zzc implements Releasable, Result {
    protected final Status zzOt;
    protected final DataHolder zzWu;

    protected zzc(DataHolder dataHolder) {
        this(dataHolder, new Status(dataHolder.getStatusCode()));
    }

    protected zzc(DataHolder dataHolder, Status status) {
        this.zzOt = status;
        this.zzWu = dataHolder;
    }

    public Status getStatus() {
        return this.zzOt;
    }

    public void release() {
        if (this.zzWu != null) {
            this.zzWu.close();
        }
    }
}
