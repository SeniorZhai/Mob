package com.google.android.gms.auth.api.credentials.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.auth.api.credentials.internal.ICredentialsService.zza;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzi;

public final class CredentialsClientImpl extends zzi<ICredentialsService> {
    public static final String ACTION_START_SERVICE = "com.google.android.gms.auth.api.credentials.service.START";

    public CredentialsClientImpl(Context context, Looper looper, ConnectionCallbacks connectionListener, OnConnectionFailedListener connectionFailedListener) {
        super(context, looper, 68, connectionListener, connectionFailedListener);
    }

    protected ICredentialsService createServiceInterface(IBinder binder) {
        return zza.zzaq(binder);
    }

    protected String getServiceDescriptor() {
        return "com.google.android.gms.auth.api.credentials.internal.ICredentialsService";
    }

    protected String getStartServiceAction() {
        return ACTION_START_SERVICE;
    }

    protected /* synthetic */ IInterface zzT(IBinder iBinder) {
        return createServiceInterface(iBinder);
    }
}
