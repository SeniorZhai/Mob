package com.google.android.gms.common.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.Api.zzb;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;

public class zzz<T extends IInterface> extends zzi<T> {
    private final zzb<T> zzabf;

    public zzz(Context context, Looper looper, int i, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, zze com_google_android_gms_common_internal_zze, zzb com_google_android_gms_common_api_Api_zzb) {
        super(context, looper, i, connectionCallbacks, onConnectionFailedListener, com_google_android_gms_common_internal_zze);
        this.zzabf = com_google_android_gms_common_api_Api_zzb;
    }

    protected String getServiceDescriptor() {
        return this.zzabf.getServiceDescriptor();
    }

    protected String getStartServiceAction() {
        return this.zzabf.getStartServiceAction();
    }

    protected T zzT(IBinder iBinder) {
        return this.zzabf.zzT(iBinder);
    }
}
