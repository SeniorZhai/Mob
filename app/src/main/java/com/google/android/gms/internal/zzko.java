package com.google.android.gms.internal;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;

abstract class zzko<R extends Result> extends com.google.android.gms.common.api.zza.zza<R, zzkp> {

    static abstract class zza extends zzko<Status> {
        public zza(GoogleApiClient googleApiClient) {
            super(googleApiClient);
        }

        public /* synthetic */ Result createFailedResult(Status x0) {
            return zzb(x0);
        }

        public Status zzb(Status status) {
            return status;
        }
    }

    public zzko(GoogleApiClient googleApiClient) {
        super(zzkl.zzNX, googleApiClient);
    }
}
