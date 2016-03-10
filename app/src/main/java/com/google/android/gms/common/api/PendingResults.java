package com.google.android.gms.common.api;

import android.os.Looper;
import com.google.android.gms.common.internal.zzu;

public final class PendingResults {

    private static final class zza<R extends Result> extends AbstractPendingResult<R> {
        private final R zzXN;

        public zza(R r) {
            super(Looper.getMainLooper());
            this.zzXN = r;
        }

        protected R createFailedResult(Status status) {
            if (status.getStatusCode() == this.zzXN.getStatus().getStatusCode()) {
                return this.zzXN;
            }
            throw new UnsupportedOperationException("Creating failed results is not supported");
        }
    }

    private static final class zzb<R extends Result> extends AbstractPendingResult<R> {
        public zzb() {
            super(Looper.getMainLooper());
        }

        protected R createFailedResult(Status status) {
            throw new UnsupportedOperationException("Creating failed results is not supported");
        }
    }

    private PendingResults() {
    }

    public static PendingResult<Status> canceledPendingResult() {
        PendingResult com_google_android_gms_common_api_zzl = new zzl(Looper.getMainLooper());
        com_google_android_gms_common_api_zzl.cancel();
        return com_google_android_gms_common_api_zzl;
    }

    public static <R extends Result> PendingResult<R> canceledPendingResult(R result) {
        zzu.zzb((Object) result, (Object) "Result must not be null");
        zzu.zzb(result.getStatus().getStatusCode() == 16, (Object) "Status code must be CommonStatusCodes.CANCELED");
        PendingResult com_google_android_gms_common_api_PendingResults_zza = new zza(result);
        com_google_android_gms_common_api_PendingResults_zza.cancel();
        return com_google_android_gms_common_api_PendingResults_zza;
    }

    public static <R extends Result> PendingResult<R> immediatePendingResult(R result) {
        zzu.zzb((Object) result, (Object) "Result must not be null");
        PendingResult com_google_android_gms_common_api_PendingResults_zzb = new zzb();
        com_google_android_gms_common_api_PendingResults_zzb.setResult(result);
        return com_google_android_gms_common_api_PendingResults_zzb;
    }

    public static PendingResult<Status> immediatePendingResult(Status result) {
        zzu.zzb((Object) result, (Object) "Result must not be null");
        PendingResult com_google_android_gms_common_api_zzl = new zzl(Looper.getMainLooper());
        com_google_android_gms_common_api_zzl.setResult(result);
        return com_google_android_gms_common_api_zzl;
    }
}
