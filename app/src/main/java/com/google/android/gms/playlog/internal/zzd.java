package com.google.android.gms.playlog.internal;

import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.internal.zzoz.zza;

public class zzd implements ConnectionCallbacks, OnConnectionFailedListener {
    private zzf zzaGD = null;
    private final zza zzaGN;
    private boolean zzaGO = true;

    public zzd(zza com_google_android_gms_internal_zzoz_zza) {
        this.zzaGN = com_google_android_gms_internal_zzoz_zza;
    }

    public void onConnected(Bundle connectionHint) {
        this.zzaGD.zzak(false);
        if (this.zzaGO && this.zzaGN != null) {
            this.zzaGN.zzxl();
        }
        this.zzaGO = false;
    }

    public void onConnectionFailed(ConnectionResult result) {
        this.zzaGD.zzak(true);
        if (this.zzaGO && this.zzaGN != null) {
            if (result.hasResolution()) {
                this.zzaGN.zzf(result.getResolution());
            } else {
                this.zzaGN.zzxm();
            }
        }
        this.zzaGO = false;
    }

    public void onConnectionSuspended(int cause) {
        this.zzaGD.zzak(true);
    }

    public void zza(zzf com_google_android_gms_playlog_internal_zzf) {
        this.zzaGD = com_google_android_gms_playlog_internal_zzf;
    }

    public void zzaj(boolean z) {
        this.zzaGO = z;
    }
}
