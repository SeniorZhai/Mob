package com.google.android.gms.internal;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zze;
import com.google.android.gms.common.internal.zzi;
import com.google.android.gms.internal.zzji.zza;

public class zzjg extends zzi<zzji> {
    public zzjg(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        super(context, looper, 74, connectionCallbacks, onConnectionFailedListener, com_google_android_gms_common_internal_zze);
    }

    protected String getServiceDescriptor() {
        return "com.google.android.gms.auth.api.accountstatus.internal.IAccountStatusService";
    }

    protected String getStartServiceAction() {
        return "com.google.android.gms.auth.api.accountstatus.START";
    }

    protected /* synthetic */ IInterface zzT(IBinder iBinder) {
        return zzam(iBinder);
    }

    protected zzji zzam(IBinder iBinder) {
        return zza.zzao(iBinder);
    }
}
