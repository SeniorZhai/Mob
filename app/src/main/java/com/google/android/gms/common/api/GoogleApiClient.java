package com.google.android.gms.common.api;

import android.accounts.Account;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api.ApiOptions;
import com.google.android.gms.common.api.Api.ApiOptions.HasOptions;
import com.google.android.gms.common.api.Api.ApiOptions.NotRequiredOptions;
import com.google.android.gms.common.api.Api.Client;
import com.google.android.gms.common.api.Api.ClientKey;
import com.google.android.gms.common.api.Api.zza;
import com.google.android.gms.common.internal.zze;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzpq;
import com.google.android.gms.internal.zzps;
import com.google.android.gms.internal.zzpt;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface GoogleApiClient {

    public static final class Builder {
        private final Context mContext;
        private Account zzMY;
        private String zzOd;
        private String zzOe;
        private FragmentActivity zzWA;
        private int zzWB;
        private int zzWC;
        private OnConnectionFailedListener zzWD;
        private zza<? extends zzps, zzpt> zzWE;
        private final Set<ConnectionCallbacks> zzWF;
        private final Set<OnConnectionFailedListener> zzWG;
        private zzpt.zza zzWH;
        private Looper zzWt;
        private final Set<Scope> zzWv;
        private int zzWw;
        private View zzWx;
        private final Map<Api<?>, zze.zza> zzWy;
        private final Map<Api<?>, ApiOptions> zzWz;

        public Builder(Context context) {
            this.zzWv = new HashSet();
            this.zzWy = new HashMap();
            this.zzWz = new HashMap();
            this.zzWB = -1;
            this.zzWC = -1;
            this.zzWF = new HashSet();
            this.zzWG = new HashSet();
            this.zzWH = new zzpt.zza();
            this.mContext = context;
            this.zzWt = context.getMainLooper();
            this.zzOe = context.getPackageName();
            this.zzOd = context.getClass().getName();
            this.zzWE = zzpq.zzNY;
        }

        public Builder(Context context, ConnectionCallbacks connectedListener, OnConnectionFailedListener connectionFailedListener) {
            this(context);
            zzu.zzb((Object) connectedListener, (Object) "Must provide a connected listener");
            this.zzWF.add(connectedListener);
            zzu.zzb((Object) connectionFailedListener, (Object) "Must provide a connection failed listener");
            this.zzWG.add(connectionFailedListener);
        }

        private void zza(Api<?> api, int i, Scope... scopeArr) {
            boolean z = true;
            int i2 = 0;
            if (i != 1) {
                if (i == 2) {
                    z = false;
                } else {
                    throw new IllegalArgumentException("Invalid resolution mode: '" + i + "', use a constant from GoogleApiClient.ResolutionMode");
                }
            }
            Set hashSet = new HashSet(api.zzmr());
            int length = scopeArr.length;
            while (i2 < length) {
                hashSet.add(scopeArr[i2]);
                i2++;
            }
            this.zzWy.put(api, new zze.zza(hashSet, z));
        }

        private GoogleApiClient zzmy() {
            zzm zza = zzm.zza(this.zzWA);
            GoogleApiClient com_google_android_gms_common_api_zzg = new zzg(this.mContext.getApplicationContext(), this.zzWt, zzmx(), this.zzWE, this.zzWz, this.zzWF, this.zzWG, this.zzWB, -1);
            zza.zza(this.zzWB, com_google_android_gms_common_api_zzg, this.zzWD);
            return com_google_android_gms_common_api_zzg;
        }

        private GoogleApiClient zzmz() {
            zzn zzb = zzn.zzb(this.zzWA);
            GoogleApiClient zzbc = zzb.zzbc(this.zzWC);
            if (zzbc == null) {
                zzbc = new zzg(this.mContext.getApplicationContext(), this.zzWt, zzmx(), this.zzWE, this.zzWz, this.zzWF, this.zzWG, -1, this.zzWC);
            }
            zzb.zza(this.zzWC, zzbc, this.zzWD);
            return zzbc;
        }

        public Builder addApi(Api<? extends NotRequiredOptions> api) {
            this.zzWz.put(api, null);
            this.zzWv.addAll(api.zzmr());
            return this;
        }

        public <O extends HasOptions> Builder addApi(Api<O> api, O options) {
            zzu.zzb((Object) options, (Object) "Null options are not permitted for this Api");
            this.zzWz.put(api, options);
            this.zzWv.addAll(api.zzmr());
            return this;
        }

        public <O extends HasOptions> Builder addApiIfAvailable(Api<O> api, O options, Scope... scopes) {
            zzu.zzb((Object) options, (Object) "Null options are not permitted for this Api");
            this.zzWz.put(api, options);
            zza(api, 1, scopes);
            return this;
        }

        public Builder addApiIfAvailable(Api<? extends NotRequiredOptions> api, Scope... scopes) {
            this.zzWz.put(api, null);
            zza(api, 1, scopes);
            return this;
        }

        public Builder addConnectionCallbacks(ConnectionCallbacks listener) {
            this.zzWF.add(listener);
            return this;
        }

        public Builder addOnConnectionFailedListener(OnConnectionFailedListener listener) {
            this.zzWG.add(listener);
            return this;
        }

        public Builder addScope(Scope scope) {
            this.zzWv.add(scope);
            return this;
        }

        public GoogleApiClient build() {
            zzu.zzb(!this.zzWz.isEmpty(), (Object) "must call addApi() to add at least one API");
            return this.zzWB >= 0 ? zzmy() : this.zzWC >= 0 ? zzmz() : new zzg(this.mContext, this.zzWt, zzmx(), this.zzWE, this.zzWz, this.zzWF, this.zzWG, -1, -1);
        }

        public Builder enableAutoManage(FragmentActivity fragmentActivity, int clientId, OnConnectionFailedListener unresolvedConnectionFailedListener) {
            zzu.zzb(clientId >= 0, (Object) "clientId must be non-negative");
            this.zzWB = clientId;
            this.zzWA = (FragmentActivity) zzu.zzb((Object) fragmentActivity, (Object) "Null activity is not permitted.");
            this.zzWD = unresolvedConnectionFailedListener;
            return this;
        }

        public Builder requestServerAuthCode(String serverClientId, ServerAuthCodeCallbacks callbacks) {
            this.zzWH.zza(serverClientId, callbacks);
            return this;
        }

        public Builder setAccountName(String accountName) {
            this.zzMY = accountName == null ? null : new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            return this;
        }

        public Builder setGravityForPopups(int gravityForPopups) {
            this.zzWw = gravityForPopups;
            return this;
        }

        public Builder setHandler(Handler handler) {
            zzu.zzb((Object) handler, (Object) "Handler must not be null");
            this.zzWt = handler.getLooper();
            return this;
        }

        public Builder setViewForPopups(View viewForPopups) {
            this.zzWx = viewForPopups;
            return this;
        }

        public Builder useDefaultAccount() {
            return setAccountName("<<default account>>");
        }

        public zze zzmx() {
            return new zze(this.zzMY, this.zzWv, this.zzWy, this.zzWw, this.zzWx, this.zzOe, this.zzOd, this.zzWH.zzyc());
        }
    }

    public interface ConnectionCallbacks {
        public static final int CAUSE_NETWORK_LOST = 2;
        public static final int CAUSE_SERVICE_DISCONNECTED = 1;

        void onConnected(Bundle bundle);

        void onConnectionSuspended(int i);
    }

    public interface ConnectionProgressReportCallbacks {
        void onReportAccountValidation(ConnectionResult connectionResult);

        void onReportServiceBinding(ConnectionResult connectionResult);
    }

    public interface OnConnectionFailedListener {
        void onConnectionFailed(ConnectionResult connectionResult);
    }

    public interface ServerAuthCodeCallbacks {

        public static class CheckResult {
            private boolean zzWI;
            private Set<Scope> zzWJ;

            private CheckResult(boolean requiresNewAuthCode, Set<Scope> requiredScopes) {
                this.zzWI = requiresNewAuthCode;
                this.zzWJ = requiredScopes;
            }

            public static CheckResult newAuthNotRequiredResult() {
                return new CheckResult(false, null);
            }

            public static CheckResult newAuthRequiredResult(Set<Scope> requiredScopes) {
                boolean z = (requiredScopes == null || requiredScopes.isEmpty()) ? false : true;
                zzu.zzb(z, (Object) "A non-empty scope set is required if further auth is needed.");
                return new CheckResult(true, requiredScopes);
            }

            public boolean zzmA() {
                return this.zzWI;
            }

            public Set<Scope> zzmB() {
                return this.zzWJ;
            }
        }

        CheckResult onCheckServerAuthorization(String str, Set<Scope> set);

        boolean onUploadServerAuthCode(String str, String str2);
    }

    ConnectionResult blockingConnect();

    ConnectionResult blockingConnect(long j, TimeUnit timeUnit);

    PendingResult<Status> clearDefaultAccountAndReconnect();

    void connect();

    void disconnect();

    void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

    ConnectionResult getConnectionResult(Api<?> api);

    Context getContext();

    Looper getLooper();

    int getSessionId();

    boolean hasConnectedApi(Api<?> api);

    boolean isConnected();

    boolean isConnecting();

    boolean isConnectionCallbacksRegistered(ConnectionCallbacks connectionCallbacks);

    boolean isConnectionFailedListenerRegistered(OnConnectionFailedListener onConnectionFailedListener);

    void reconnect();

    void registerConnectionCallbacks(ConnectionCallbacks connectionCallbacks);

    void registerConnectionFailedListener(OnConnectionFailedListener onConnectionFailedListener);

    void stopAutoManage(FragmentActivity fragmentActivity);

    void unregisterConnectionCallbacks(ConnectionCallbacks connectionCallbacks);

    void unregisterConnectionFailedListener(OnConnectionFailedListener onConnectionFailedListener);

    <C extends Client> C zza(ClientKey<C> clientKey);

    <A extends Client, R extends Result, T extends zza.zza<R, A>> T zza(T t);

    boolean zza(Api<?> api);

    boolean zza(Scope scope);

    <A extends Client, T extends zza.zza<? extends Result, A>> T zzb(T t);

    <L> zzi<L> zzo(L l);
}
