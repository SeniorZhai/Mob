package com.google.android.gms.auth.api;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.auth.api.credentials.internal.CredentialsClientImpl;
import com.google.android.gms.auth.api.credentials.internal.zzc;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.Api.ApiOptions.NoOptions;
import com.google.android.gms.common.api.Api.ApiOptions.Optional;
import com.google.android.gms.common.api.Api.Client;
import com.google.android.gms.common.api.Api.ClientKey;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.zze;
import com.google.android.gms.internal.zzje;
import com.google.android.gms.internal.zzjf;
import com.google.android.gms.internal.zzjg;
import com.google.android.gms.internal.zzjj;
import com.google.android.gms.internal.zzjm;
import com.google.android.gms.internal.zzjn;
import com.google.android.gms.internal.zzjp;
import com.google.android.gms.internal.zzjq;

public final class Auth {
    public static final ClientKey<CredentialsClientImpl> CLIENT_KEY_CREDENTIALS_API = new ClientKey();
    public static final Api<NoOptions> CREDENTIALS_API = new Api("Auth.CREDENTIALS_API", zzOI, CLIENT_KEY_CREDENTIALS_API, new Scope[0]);
    public static final CredentialsApi CredentialsApi = new zzc();
    public static final ClientKey<zzjj> zzOE = new ClientKey();
    public static final ClientKey<zzjg> zzOF = new ClientKey();
    public static final ClientKey<zzjq> zzOG = new ClientKey();
    private static final com.google.android.gms.common.api.Api.zza<zzjj, zza> zzOH = new com.google.android.gms.common.api.Api.zza<zzjj, zza>() {
        public int getPriority() {
            return ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        }

        public zzjj zza(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, zza com_google_android_gms_auth_api_Auth_zza, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzjj(context, looper, com_google_android_gms_common_internal_zze, com_google_android_gms_auth_api_Auth_zza, connectionCallbacks, onConnectionFailedListener);
        }
    };
    private static final com.google.android.gms.common.api.Api.zza<CredentialsClientImpl, NoOptions> zzOI = new com.google.android.gms.common.api.Api.zza<CredentialsClientImpl, NoOptions>() {
        public int getPriority() {
            return ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        }

        public /* synthetic */ Client zza(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, Object obj, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return zzd(context, looper, com_google_android_gms_common_internal_zze, (NoOptions) obj, connectionCallbacks, onConnectionFailedListener);
        }

        public CredentialsClientImpl zzd(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, NoOptions noOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new CredentialsClientImpl(context, looper, connectionCallbacks, onConnectionFailedListener);
        }
    };
    private static final com.google.android.gms.common.api.Api.zza<zzjg, NoOptions> zzOJ = new com.google.android.gms.common.api.Api.zza<zzjg, NoOptions>() {
        public int getPriority() {
            return ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        }

        public /* synthetic */ Client zza(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, Object obj, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return zze(context, looper, com_google_android_gms_common_internal_zze, (NoOptions) obj, connectionCallbacks, onConnectionFailedListener);
        }

        public zzjg zze(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, NoOptions noOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzjg(context, looper, com_google_android_gms_common_internal_zze, connectionCallbacks, onConnectionFailedListener);
        }
    };
    private static final com.google.android.gms.common.api.Api.zza<zzjq, NoOptions> zzOK = new com.google.android.gms.common.api.Api.zza<zzjq, NoOptions>() {
        public int getPriority() {
            return ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        }

        public /* synthetic */ Client zza(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, Object obj, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return zzf(context, looper, com_google_android_gms_common_internal_zze, (NoOptions) obj, connectionCallbacks, onConnectionFailedListener);
        }

        public zzjq zzf(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, NoOptions noOptions, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
            return new zzjq(context, looper, connectionCallbacks, onConnectionFailedListener);
        }
    };
    public static final Api<zza> zzOL = new Api("Auth.PROXY_API", zzOH, zzOE, new Scope[0]);
    public static final Api<NoOptions> zzOM = new Api("Auth.SIGN_IN_API", zzOK, zzOG, new Scope[0]);
    public static final Api<NoOptions> zzON = new Api("Auth.ACCOUNT_STATUS_API", zzOJ, zzOF, new Scope[0]);
    public static final com.google.android.gms.auth.api.proxy.zza zzOO = new zzjm();
    public static final zzje zzOP = new zzjf();
    public static final zzjn zzOQ = new zzjp();

    public static final class zza implements Optional {
        private final Bundle zzOR;

        public Bundle zzkY() {
            return new Bundle(this.zzOR);
        }
    }

    private Auth() {
    }
}
