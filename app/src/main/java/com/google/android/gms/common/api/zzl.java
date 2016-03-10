package com.google.android.gms.common.api;

import android.os.Looper;

public class zzl extends AbstractPendingResult<Status> {
    public zzl(Looper looper) {
        super(looper);
    }

    protected /* synthetic */ Result createFailedResult(Status x0) {
        return zzb(x0);
    }

    protected Status zzb(Status status) {
        return status;
    }
}
