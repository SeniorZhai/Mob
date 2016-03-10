package com.google.android.gms.common.api;

import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api.Client;
import com.google.android.gms.common.api.zza.zza;

public class zzf implements zzh {
    private final zzg zzWK;

    public zzf(zzg com_google_android_gms_common_api_zzg) {
        this.zzWK = com_google_android_gms_common_api_zzg;
    }

    public void begin() {
        this.zzWK.zzmL();
    }

    public void connect() {
        this.zzWK.zzmM();
    }

    public String getName() {
        return "DISCONNECTED";
    }

    public void onConnected(Bundle connectionHint) {
    }

    public void onConnectionSuspended(int cause) {
    }

    public <A extends Client, R extends Result, T extends zza<R, A>> T zza(T t) {
        this.zzWK.zzXo.add(t);
        return t;
    }

    public void zza(ConnectionResult connectionResult, Api<?> api, int i) {
    }

    public void zzaV(int i) {
        if ((i == -1 ? 1 : null) != null) {
            for (zze cancel : this.zzWK.zzXo) {
                cancel.cancel();
            }
            this.zzWK.zzXo.clear();
            this.zzWK.zzmK();
            this.zzWK.zzXv.clear();
        }
    }

    public <A extends Client, T extends zza<? extends Result, A>> T zzb(T t) {
        throw new IllegalStateException("GoogleApiClient is not connected yet.");
    }
}
