package com.google.android.gms.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzi;
import com.google.android.gms.internal.zzkr.zza;

public class zzkp extends zzi<zzkr> {
    public zzkp(Context context, Looper looper, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, looper, 39, connectionCallbacks, onConnectionFailedListener);
    }

    protected String getServiceDescriptor() {
        return "com.google.android.gms.common.internal.service.ICommonService";
    }

    public String getStartServiceAction() {
        return "com.google.android.gms.common.service.START";
    }

    protected /* synthetic */ IInterface zzT(IBinder iBinder) {
        return zzaK(iBinder);
    }

    protected zzkr zzaK(IBinder iBinder) {
        return zza.zzaM(iBinder);
    }
}
