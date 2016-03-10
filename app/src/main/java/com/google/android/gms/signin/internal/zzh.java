package com.google.android.gms.signin.internal;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.GoogleApiClient.ServerAuthCodeCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.ServerAuthCodeCallbacks.CheckResult;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.AuthAccountRequest;
import com.google.android.gms.common.internal.BinderWrapper;
import com.google.android.gms.common.internal.IAccountAccessor;
import com.google.android.gms.common.internal.ResolveAccountRequest;
import com.google.android.gms.common.internal.ResolveAccountResponse;
import com.google.android.gms.common.internal.zze;
import com.google.android.gms.common.internal.zzi;
import com.google.android.gms.common.internal.zzq;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzps;
import com.google.android.gms.internal.zzpt;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class zzh extends zzi<zzf> implements zzps {
    private final zze zzXa;
    private final zzpt zzZU;
    private Integer zzZV;
    private final boolean zzaKa;
    private final ExecutorService zzaKb;

    private static class zza extends com.google.android.gms.signin.internal.zzd.zza {
        private final zzpt zzZU;
        private final ExecutorService zzaKb;

        public zza(zzpt com_google_android_gms_internal_zzpt, ExecutorService executorService) {
            this.zzZU = com_google_android_gms_internal_zzpt;
            this.zzaKb = executorService;
        }

        private ServerAuthCodeCallbacks zzyb() throws RemoteException {
            return this.zzZU.zzyb();
        }

        public void zza(final String str, final String str2, final zzf com_google_android_gms_signin_internal_zzf) throws RemoteException {
            this.zzaKb.submit(new Runnable(this) {
                final /* synthetic */ zza zzaKf;

                public void run() {
                    try {
                        com_google_android_gms_signin_internal_zzf.zzal(this.zzaKf.zzyb().onUploadServerAuthCode(str, str2));
                    } catch (Throwable e) {
                        Log.e("SignInClientImpl", "RemoteException thrown when processing uploadServerAuthCode callback", e);
                    }
                }
            });
        }

        public void zza(final String str, final List<Scope> list, final zzf com_google_android_gms_signin_internal_zzf) throws RemoteException {
            this.zzaKb.submit(new Runnable(this) {
                final /* synthetic */ zza zzaKf;

                public void run() {
                    try {
                        CheckResult onCheckServerAuthorization = this.zzaKf.zzyb().onCheckServerAuthorization(str, Collections.unmodifiableSet(new HashSet(list)));
                        com_google_android_gms_signin_internal_zzf.zza(new CheckServerAuthResult(onCheckServerAuthorization.zzmA(), onCheckServerAuthorization.zzmB()));
                    } catch (Throwable e) {
                        Log.e("SignInClientImpl", "RemoteException thrown when processing checkServerAuthorization callback", e);
                    }
                }
            });
        }
    }

    public zzh(Context context, Looper looper, boolean z, zze com_google_android_gms_common_internal_zze, zzpt com_google_android_gms_internal_zzpt, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, ExecutorService executorService) {
        super(context, looper, 44, connectionCallbacks, onConnectionFailedListener, com_google_android_gms_common_internal_zze);
        this.zzaKa = z;
        this.zzXa = com_google_android_gms_common_internal_zze;
        this.zzZU = com_google_android_gms_internal_zzpt;
        this.zzZV = com_google_android_gms_common_internal_zze.zznC();
        this.zzaKb = executorService;
    }

    public static Bundle zza(zzpt com_google_android_gms_internal_zzpt, Integer num, ExecutorService executorService) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("com.google.android.gms.signin.internal.offlineAccessRequested", com_google_android_gms_internal_zzpt.zzxZ());
        bundle.putBoolean("com.google.android.gms.signin.internal.idTokenRequested", com_google_android_gms_internal_zzpt.zzya());
        bundle.putString("com.google.android.gms.signin.internal.serverClientId", com_google_android_gms_internal_zzpt.zzxt());
        if (com_google_android_gms_internal_zzpt.zzyb() != null) {
            bundle.putParcelable("com.google.android.gms.signin.internal.signInCallbacks", new BinderWrapper(new zza(com_google_android_gms_internal_zzpt, executorService).asBinder()));
        }
        if (num != null) {
            bundle.putInt("com.google.android.gms.common.internal.ClientSettings.sessionId", num.intValue());
        }
        return bundle;
    }

    public void connect() {
        connect(new zzf(this));
    }

    protected String getServiceDescriptor() {
        return "com.google.android.gms.signin.internal.ISignInService";
    }

    protected String getStartServiceAction() {
        return "com.google.android.gms.signin.service.START";
    }

    public boolean requiresSignIn() {
        return this.zzaKa;
    }

    protected /* synthetic */ IInterface zzT(IBinder iBinder) {
        return zzdE(iBinder);
    }

    public void zza(IAccountAccessor iAccountAccessor, Set<Scope> set, zze com_google_android_gms_signin_internal_zze) {
        zzu.zzb((Object) com_google_android_gms_signin_internal_zze, (Object) "Expecting a valid ISignInCallbacks");
        try {
            ((zzf) zznM()).zza(new AuthAccountRequest(iAccountAccessor, set), com_google_android_gms_signin_internal_zze);
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when authAccount is called");
            try {
                com_google_android_gms_signin_internal_zze.zza(new ConnectionResult(8, null), new AuthAccountResult());
            } catch (RemoteException e2) {
                Log.wtf("SignInClientImpl", "ISignInCallbacks#onAuthAccount should be executed from the same process, unexpected RemoteException.");
            }
        }
    }

    public void zza(IAccountAccessor iAccountAccessor, boolean z) {
        try {
            ((zzf) zznM()).zza(iAccountAccessor, this.zzZV.intValue(), z);
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when saveDefaultAccount is called");
        }
    }

    public void zza(zzq com_google_android_gms_common_internal_zzq) {
        zzu.zzb((Object) com_google_android_gms_common_internal_zzq, (Object) "Expecting a valid IResolveAccountCallbacks");
        try {
            ((zzf) zznM()).zza(new ResolveAccountRequest(this.zzXa.zznt(), this.zzZV.intValue()), com_google_android_gms_common_internal_zzq);
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when resolveAccount is called");
            try {
                com_google_android_gms_common_internal_zzq.zzb(new ResolveAccountResponse(8));
            } catch (RemoteException e2) {
                Log.wtf("SignInClientImpl", "IResolveAccountCallbacks#onAccountResolutionComplete should be executed from the same process, unexpected RemoteException.");
            }
        }
    }

    protected zzf zzdE(IBinder iBinder) {
        return com.google.android.gms.signin.internal.zzf.zza.zzdD(iBinder);
    }

    protected Bundle zzkR() {
        Bundle zza = zza(this.zzZU, this.zzXa.zznC(), this.zzaKb);
        if (!getContext().getPackageName().equals(this.zzXa.zzny())) {
            zza.putString("com.google.android.gms.signin.internal.realClientPackageName", this.zzXa.zzny());
        }
        return zza;
    }

    public void zzxY() {
        try {
            ((zzf) zznM()).zziQ(this.zzZV.intValue());
        } catch (RemoteException e) {
            Log.w("SignInClientImpl", "Remote service probably died when clearAccountFromSessionStore is called");
        }
    }
}
