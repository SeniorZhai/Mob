package com.google.android.gms.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.text.TextUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.Auth.zza;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zze;
import com.google.android.gms.common.internal.zzi;

public final class zzjj extends zzi<zzjl> {
    private final Bundle zzOR;

    public zzjj(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, zza com_google_android_gms_auth_api_Auth_zza, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, looper, 16, connectionCallbacks, onConnectionFailedListener, com_google_android_gms_common_internal_zze);
        this.zzOR = com_google_android_gms_auth_api_Auth_zza == null ? new Bundle() : com_google_android_gms_auth_api_Auth_zza.zzkY();
    }

    protected String getServiceDescriptor() {
        return "com.google.android.gms.auth.api.internal.IAuthService";
    }

    protected String getStartServiceAction() {
        return "com.google.android.gms.auth.service.START";
    }

    public boolean requiresSignIn() {
        zze zznK = zznK();
        return (TextUtils.isEmpty(zznK.getAccountName()) || zznK.zzb(Auth.zzOL).isEmpty()) ? false : true;
    }

    protected /* synthetic */ IInterface zzT(IBinder iBinder) {
        return zzar(iBinder);
    }

    protected zzjl zzar(IBinder iBinder) {
        return zzjl.zza.zzat(iBinder);
    }

    protected Bundle zzkR() {
        return this.zzOR;
    }
}
