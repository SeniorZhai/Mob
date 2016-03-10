package com.google.android.gms.common.api;

import android.os.Bundle;
import android.os.DeadObjectException;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api.Client;
import com.google.android.gms.common.api.zza.zza;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class zzd implements zzh {
    private final zzg zzWK;

    public zzd(zzg com_google_android_gms_common_api_zzg) {
        this.zzWK = com_google_android_gms_common_api_zzg;
    }

    private <A extends Client> void zza(zze<A> com_google_android_gms_common_api_zzg_zze_A) throws DeadObjectException {
        this.zzWK.zzb((zze) com_google_android_gms_common_api_zzg_zze_A);
        Client zza = this.zzWK.zza(com_google_android_gms_common_api_zzg_zze_A.zzms());
        if (zza.isConnected() || !this.zzWK.zzXv.containsKey(com_google_android_gms_common_api_zzg_zze_A.zzms())) {
            com_google_android_gms_common_api_zzg_zze_A.zzb(zza);
        } else {
            com_google_android_gms_common_api_zzg_zze_A.zzr(new Status(17));
        }
    }

    public void begin() {
        while (!this.zzWK.zzXo.isEmpty()) {
            try {
                zza((zze) this.zzWK.zzXo.remove());
            } catch (Throwable e) {
                Log.w("GoogleApiClientConnected", "Service died while flushing queue", e);
            }
        }
    }

    public void connect() {
    }

    public String getName() {
        return "CONNECTED";
    }

    public void onConnected(Bundle connectionHint) {
    }

    public void onConnectionSuspended(int cause) {
        switch (cause) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                this.zzWK.zzmQ();
                zzaV(cause);
                return;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                zzaV(cause);
                this.zzWK.connect();
                return;
            default:
                return;
        }
    }

    public <A extends Client, R extends Result, T extends zza<R, A>> T zza(T t) {
        return zzb(t);
    }

    public void zza(ConnectionResult connectionResult, Api<?> api, int i) {
    }

    public void zzaV(int i) {
        Object obj = i == -1 ? 1 : null;
        if (obj != null) {
            this.zzWK.zzmK();
            this.zzWK.zzXv.clear();
        } else {
            for (zze forceFailureUnlessReady : this.zzWK.zzXA) {
                forceFailureUnlessReady.forceFailureUnlessReady(new Status(8, "The connection to Google Play services was lost"));
            }
        }
        this.zzWK.zze(null);
        if (obj == null) {
            this.zzWK.zzXn.zzbu(i);
        }
        this.zzWK.zzXn.zznT();
    }

    public <A extends Client, T extends zza<? extends Result, A>> T zzb(T t) {
        try {
            zza((zze) t);
        } catch (DeadObjectException e) {
            zzaV(1);
        }
        return t;
    }
}
