package com.google.android.gms.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzi;
import com.google.android.gms.internal.zzjo.zza;

public class zzjq extends zzi<zzjo> {
    public zzjq(Context context, Looper looper, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, looper, 87, connectionCallbacks, onConnectionFailedListener);
    }

    protected String getServiceDescriptor() {
        return "com.google.android.gms.auth.api.signin.internal.ISignInService";
    }

    protected String getStartServiceAction() {
        return "com.google.android.gms.auth.api.signin.service.START";
    }

    protected /* synthetic */ IInterface zzT(IBinder iBinder) {
        return zzav(iBinder);
    }

    protected zzjo zzav(IBinder iBinder) {
        return zza.zzau(iBinder);
    }
}
