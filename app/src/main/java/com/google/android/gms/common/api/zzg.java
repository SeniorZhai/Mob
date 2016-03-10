package com.google.android.gms.common.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import com.google.android.exoplayer.chunk.FormatEvaluator.AdaptiveEvaluator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api.ApiOptions;
import com.google.android.gms.common.api.Api.Client;
import com.google.android.gms.common.api.Api.ClientKey;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzj;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.common.internal.zzz;
import com.google.android.gms.internal.zzkl;
import com.google.android.gms.internal.zzps;
import com.google.android.gms.internal.zzpt;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

final class zzg implements GoogleApiClient {
    private final Context mContext;
    private final int zzWB;
    private final int zzWC;
    final com.google.android.gms.common.api.Api.zza<? extends zzps, zzpt> zzWE;
    private final Lock zzWL = new ReentrantLock();
    private final Looper zzWt;
    final Set<zze<?>> zzXA = Collections.newSetFromMap(new ConcurrentHashMap(16, AdaptiveEvaluator.DEFAULT_BANDWIDTH_FRACTION, 2));
    private final zzc zzXB = new zzc(this) {
        final /* synthetic */ zzg zzXE;

        {
            this.zzXE = r1;
        }

        public void zzc(zze<?> com_google_android_gms_common_api_zzg_zze_) {
            this.zzXE.zzXA.remove(com_google_android_gms_common_api_zzg_zze_);
        }
    };
    private final ConnectionCallbacks zzXC = new zzd(this) {
        final /* synthetic */ zzg zzXE;

        {
            this.zzXE = r1;
        }

        public void onConnected(Bundle connectionHint) {
            this.zzXE.zzXx.onConnected(connectionHint);
        }
    };
    private final com.google.android.gms.common.internal.zzj.zza zzXD = new com.google.android.gms.common.internal.zzj.zza(this) {
        final /* synthetic */ zzg zzXE;

        {
            this.zzXE = r1;
        }

        public boolean isConnected() {
            return this.zzXE.isConnected();
        }

        public Bundle zzlM() {
            return null;
        }
    };
    final com.google.android.gms.common.internal.zze zzXa;
    final Map<Api<?>, Integer> zzXb;
    private final Condition zzXm;
    final zzj zzXn;
    final Queue<zze<?>> zzXo = new LinkedList();
    private volatile boolean zzXp;
    private long zzXq = 120000;
    private long zzXr = Constants.NOTIFICATION_BANNER_TIMEOUT;
    final zza zzXs;
    BroadcastReceiver zzXt;
    final Map<ClientKey<?>, Client> zzXu = new HashMap();
    final Map<ClientKey<?>, ConnectionResult> zzXv = new HashMap();
    Set<Scope> zzXw = new HashSet();
    private volatile zzh zzXx;
    private ConnectionResult zzXy = null;
    private final Set<zzi<?>> zzXz = Collections.newSetFromMap(new WeakHashMap());

    interface zze<A extends Client> {
        void cancel();

        void forceFailureUnlessReady(Status status);

        void zza(zzc com_google_android_gms_common_api_zzg_zzc);

        void zzb(A a) throws DeadObjectException;

        ClientKey<A> zzms();

        int zzmv();

        void zzr(Status status);
    }

    interface zzc {
        void zzc(zze<?> com_google_android_gms_common_api_zzg_zze_);
    }

    public abstract class zzd implements ConnectionCallbacks {
        final /* synthetic */ zzg zzXE;

        public zzd(zzg com_google_android_gms_common_api_zzg) {
            this.zzXE = com_google_android_gms_common_api_zzg;
        }

        public void onConnectionSuspended(int cause) {
            this.zzXE.zzWL.lock();
            try {
                this.zzXE.zzXx.onConnectionSuspended(cause);
            } finally {
                this.zzXE.zzWL.unlock();
            }
        }
    }

    final class zza extends Handler {
        final /* synthetic */ zzg zzXE;

        zza(zzg com_google_android_gms_common_api_zzg, Looper looper) {
            this.zzXE = com_google_android_gms_common_api_zzg;
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    this.zzXE.zzmP();
                    return;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    this.zzXE.resume();
                    return;
                default:
                    Log.w("GoogleApiClientImpl", "Unknown message id: " + msg.what);
                    return;
            }
        }
    }

    private static class zzb extends BroadcastReceiver {
        private WeakReference<zzg> zzXK;

        zzb(zzg com_google_android_gms_common_api_zzg) {
            this.zzXK = new WeakReference(com_google_android_gms_common_api_zzg);
        }

        public void onReceive(Context context, Intent intent) {
            Uri data = intent.getData();
            String str = null;
            if (data != null) {
                str = data.getSchemeSpecificPart();
            }
            if (str != null && str.equals(AdvertisingInfoServiceStrategy.GOOGLE_PLAY_SERVICES_INTENT_PACKAGE_NAME)) {
                zzg com_google_android_gms_common_api_zzg = (zzg) this.zzXK.get();
                if (com_google_android_gms_common_api_zzg != null) {
                    com_google_android_gms_common_api_zzg.resume();
                }
            }
        }
    }

    public zzg(Context context, Looper looper, com.google.android.gms.common.internal.zze com_google_android_gms_common_internal_zze, com.google.android.gms.common.api.Api.zza<? extends zzps, zzpt> com_google_android_gms_common_api_Api_zza__extends_com_google_android_gms_internal_zzps__com_google_android_gms_internal_zzpt, Map<Api<?>, ApiOptions> map, Set<ConnectionCallbacks> set, Set<OnConnectionFailedListener> set2, int i, int i2) {
        this.mContext = context;
        this.zzXn = new zzj(looper, this.zzXD);
        this.zzWt = looper;
        this.zzXs = new zza(this, looper);
        this.zzWB = i;
        this.zzWC = i2;
        this.zzXb = new HashMap();
        this.zzXm = this.zzWL.newCondition();
        this.zzXx = new zzf(this);
        for (ConnectionCallbacks registerConnectionCallbacks : set) {
            this.zzXn.registerConnectionCallbacks(registerConnectionCallbacks);
        }
        for (OnConnectionFailedListener registerConnectionFailedListener : set2) {
            this.zzXn.registerConnectionFailedListener(registerConnectionFailedListener);
        }
        Map zznx = com_google_android_gms_common_internal_zze.zznx();
        for (Api api : map.keySet()) {
            int i3;
            Object obj = map.get(api);
            if (zznx.get(api) != null) {
                i3 = ((com.google.android.gms.common.internal.zze.zza) zznx.get(api)).zzZW ? 1 : 2;
            } else {
                i3 = 0;
            }
            this.zzXb.put(api, Integer.valueOf(i3));
            this.zzXu.put(api.zzms(), api.zzmt() ? zza(api.zzmq(), obj, context, looper, com_google_android_gms_common_internal_zze, this.zzXC, zza(api, i3)) : zza(api.zzmp(), obj, context, looper, com_google_android_gms_common_internal_zze, this.zzXC, zza(api, i3)));
        }
        this.zzXa = com_google_android_gms_common_internal_zze;
        this.zzWE = com_google_android_gms_common_api_Api_zza__extends_com_google_android_gms_internal_zzps__com_google_android_gms_internal_zzpt;
    }

    private void resume() {
        this.zzWL.lock();
        try {
            if (zzmO()) {
                connect();
            }
            this.zzWL.unlock();
        } catch (Throwable th) {
            this.zzWL.unlock();
        }
    }

    private static <C extends Client, O> C zza(com.google.android.gms.common.api.Api.zza<C, O> com_google_android_gms_common_api_Api_zza_C__O, Object obj, Context context, Looper looper, com.google.android.gms.common.internal.zze com_google_android_gms_common_internal_zze, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        return com_google_android_gms_common_api_Api_zza_C__O.zza(context, looper, com_google_android_gms_common_internal_zze, obj, connectionCallbacks, onConnectionFailedListener);
    }

    private final OnConnectionFailedListener zza(final Api<?> api, final int i) {
        return new OnConnectionFailedListener(this) {
            final /* synthetic */ zzg zzXE;

            public void onConnectionFailed(ConnectionResult result) {
                this.zzXE.zzXx.zza(result, api, i);
            }
        };
    }

    private static <C extends com.google.android.gms.common.api.Api.zzb, O> zzz zza(com.google.android.gms.common.api.Api.zzc<C, O> com_google_android_gms_common_api_Api_zzc_C__O, Object obj, Context context, Looper looper, com.google.android.gms.common.internal.zze com_google_android_gms_common_internal_zze, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        return new zzz(context, looper, com_google_android_gms_common_api_Api_zzc_C__O.zzmu(), connectionCallbacks, onConnectionFailedListener, com_google_android_gms_common_internal_zze, com_google_android_gms_common_api_Api_zzc_C__O.zzl(obj));
    }

    private void zza(final GoogleApiClient googleApiClient, final zzl com_google_android_gms_common_api_zzl, final boolean z) {
        zzkl.zzabj.zzc(googleApiClient).setResultCallback(new ResultCallback<Status>(this) {
            final /* synthetic */ zzg zzXE;

            public /* synthetic */ void onResult(Result x0) {
                zzm((Status) x0);
            }

            public void zzm(Status status) {
                if (status.isSuccess() && this.zzXE.isConnected()) {
                    this.zzXE.reconnect();
                }
                com_google_android_gms_common_api_zzl.setResult(status);
                if (z) {
                    googleApiClient.disconnect();
                }
            }
        });
    }

    private void zzaY(int i) {
        this.zzWL.lock();
        try {
            this.zzXx.zzaV(i);
        } finally {
            this.zzWL.unlock();
        }
    }

    private void zzmP() {
        this.zzWL.lock();
        try {
            if (zzmR()) {
                connect();
            }
            this.zzWL.unlock();
        } catch (Throwable th) {
            this.zzWL.unlock();
        }
    }

    public ConnectionResult blockingConnect() {
        ConnectionResult connectionResult;
        zzu.zza(Looper.myLooper() != Looper.getMainLooper(), (Object) "blockingConnect must not be called on the UI thread");
        this.zzWL.lock();
        try {
            connect();
            while (isConnecting()) {
                this.zzXm.await();
            }
            if (isConnected()) {
                connectionResult = ConnectionResult.zzVG;
            } else if (this.zzXy != null) {
                connectionResult = this.zzXy;
                this.zzWL.unlock();
            } else {
                connectionResult = new ConnectionResult(13, null);
                this.zzWL.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            connectionResult = new ConnectionResult(15, null);
        } finally {
            this.zzWL.unlock();
        }
        return connectionResult;
    }

    public ConnectionResult blockingConnect(long timeout, TimeUnit unit) {
        ConnectionResult connectionResult;
        zzu.zza(Looper.myLooper() != Looper.getMainLooper(), (Object) "blockingConnect must not be called on the UI thread");
        this.zzWL.lock();
        try {
            connect();
            long toNanos = unit.toNanos(timeout);
            while (isConnecting()) {
                toNanos = this.zzXm.awaitNanos(toNanos);
                if (toNanos <= 0) {
                    connectionResult = new ConnectionResult(14, null);
                    break;
                }
            }
            if (isConnected()) {
                connectionResult = ConnectionResult.zzVG;
                this.zzWL.unlock();
            } else if (this.zzXy != null) {
                connectionResult = this.zzXy;
                this.zzWL.unlock();
            } else {
                connectionResult = new ConnectionResult(13, null);
                this.zzWL.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            connectionResult = new ConnectionResult(15, null);
        } finally {
            this.zzWL.unlock();
        }
        return connectionResult;
    }

    public PendingResult<Status> clearDefaultAccountAndReconnect() {
        zzu.zza(isConnected(), (Object) "GoogleApiClient is not connected yet.");
        final zzl com_google_android_gms_common_api_zzl = new zzl(this.zzWt);
        if (this.zzXu.containsKey(zzkl.zzNX)) {
            zza(this, com_google_android_gms_common_api_zzl, false);
        } else {
            final AtomicReference atomicReference = new AtomicReference();
            ConnectionCallbacks anonymousClass5 = new ConnectionCallbacks(this) {
                final /* synthetic */ zzg zzXE;

                public void onConnected(Bundle connectionHint) {
                    this.zzXE.zza((GoogleApiClient) atomicReference.get(), com_google_android_gms_common_api_zzl, true);
                }

                public void onConnectionSuspended(int cause) {
                }
            };
            GoogleApiClient build = new Builder(this.mContext).addApi(zzkl.API).addConnectionCallbacks(anonymousClass5).addOnConnectionFailedListener(new OnConnectionFailedListener(this) {
                final /* synthetic */ zzg zzXE;

                public void onConnectionFailed(ConnectionResult result) {
                    com_google_android_gms_common_api_zzl.setResult(new Status(8));
                }
            }).setHandler(this.zzXs).build();
            atomicReference.set(build);
            build.connect();
        }
        return com_google_android_gms_common_api_zzl;
    }

    public void connect() {
        this.zzWL.lock();
        try {
            this.zzXx.connect();
        } finally {
            this.zzWL.unlock();
        }
    }

    public void disconnect() {
        zzmR();
        zzaY(-1);
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        writer.append(prefix).append("mState=").append(this.zzXx.getName());
        writer.append(" mResuming=").print(this.zzXp);
        writer.append(" mWorkQueue.size()=").print(this.zzXo.size());
        writer.append(" mUnconsumedRunners.size()=").println(this.zzXA.size());
        String str = prefix + "  ";
        for (Api api : this.zzXb.keySet()) {
            writer.append(prefix).append(api.getName()).println(":");
            ((Client) this.zzXu.get(api.zzms())).dump(str, fd, writer, args);
        }
    }

    public ConnectionResult getConnectionResult(Api<?> api) {
        ClientKey zzms = api.zzms();
        this.zzWL.lock();
        try {
            if (!isConnected() && !zzmO()) {
                throw new IllegalStateException("Cannot invoke getConnectionResult unless GoogleApiClient is connected");
            } else if (this.zzXu.containsKey(zzms)) {
                ConnectionResult connectionResult;
                if (((Client) this.zzXu.get(zzms)).isConnected()) {
                    connectionResult = ConnectionResult.zzVG;
                } else if (this.zzXv.containsKey(zzms)) {
                    connectionResult = (ConnectionResult) this.zzXv.get(zzms);
                    this.zzWL.unlock();
                } else {
                    Log.wtf("GoogleApiClientImpl", api.getName() + " requested in getConnectionResult" + " is not connected but is not present in the failed connections map");
                    connectionResult = new ConnectionResult(8, null);
                    this.zzWL.unlock();
                }
                return connectionResult;
            } else {
                this.zzWL.unlock();
                throw new IllegalArgumentException(api.getName() + " was never registered with GoogleApiClient");
            }
        } finally {
            this.zzWL.unlock();
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public Looper getLooper() {
        return this.zzWt;
    }

    public int getSessionId() {
        return System.identityHashCode(this);
    }

    public boolean hasConnectedApi(Api<?> api) {
        Client client = (Client) this.zzXu.get(api.zzms());
        return client == null ? false : client.isConnected();
    }

    public boolean isConnected() {
        return this.zzXx instanceof zzd;
    }

    public boolean isConnecting() {
        return this.zzXx instanceof zze;
    }

    public boolean isConnectionCallbacksRegistered(ConnectionCallbacks listener) {
        return this.zzXn.isConnectionCallbacksRegistered(listener);
    }

    public boolean isConnectionFailedListenerRegistered(OnConnectionFailedListener listener) {
        return this.zzXn.isConnectionFailedListenerRegistered(listener);
    }

    public void reconnect() {
        disconnect();
        connect();
    }

    public void registerConnectionCallbacks(ConnectionCallbacks listener) {
        this.zzXn.registerConnectionCallbacks(listener);
    }

    public void registerConnectionFailedListener(OnConnectionFailedListener listener) {
        this.zzXn.registerConnectionFailedListener(listener);
    }

    public void stopAutoManage(FragmentActivity lifecycleActivity) {
        if (this.zzWB >= 0) {
            zzm.zza(lifecycleActivity).zzbb(this.zzWB);
        } else if (this.zzWC >= 0) {
            zzn.zzb(lifecycleActivity).zzbb(this.zzWC);
        } else {
            throw new IllegalStateException("Called stopAutoManage but automatic lifecycle management is not enabled.");
        }
    }

    public void unregisterConnectionCallbacks(ConnectionCallbacks listener) {
        this.zzXn.unregisterConnectionCallbacks(listener);
    }

    public void unregisterConnectionFailedListener(OnConnectionFailedListener listener) {
        this.zzXn.unregisterConnectionFailedListener(listener);
    }

    public <C extends Client> C zza(ClientKey<C> clientKey) {
        Object obj = (Client) this.zzXu.get(clientKey);
        zzu.zzb(obj, (Object) "Appropriate Api was not requested.");
        return obj;
    }

    public <A extends Client, R extends Result, T extends com.google.android.gms.common.api.zza.zza<R, A>> T zza(T t) {
        zzu.zzb(t.zzms() != null, (Object) "This task can not be enqueued (it's probably a Batch or malformed)");
        zzu.zzb(this.zzXu.containsKey(t.zzms()), (Object) "GoogleApiClient is not configured to use the API required for this call.");
        this.zzWL.lock();
        try {
            T zza = this.zzXx.zza(t);
            return zza;
        } finally {
            this.zzWL.unlock();
        }
    }

    public boolean zza(Api<?> api) {
        return this.zzXu.containsKey(api.zzms());
    }

    public boolean zza(Scope scope) {
        this.zzWL.lock();
        try {
            boolean z = isConnected() && this.zzXw.contains(scope);
            this.zzWL.unlock();
            return z;
        } catch (Throwable th) {
            this.zzWL.unlock();
        }
    }

    public <A extends Client, T extends com.google.android.gms.common.api.zza.zza<? extends Result, A>> T zzb(T t) {
        zzu.zzb(t.zzms() != null, (Object) "This task can not be executed (it's probably a Batch or malformed)");
        this.zzWL.lock();
        try {
            if (zzmO()) {
                this.zzXo.add(t);
                while (!this.zzXo.isEmpty()) {
                    zze com_google_android_gms_common_api_zzg_zze = (zze) this.zzXo.remove();
                    zzb(com_google_android_gms_common_api_zzg_zze);
                    com_google_android_gms_common_api_zzg_zze.zzr(Status.zzXR);
                }
            } else {
                t = this.zzXx.zzb(t);
                this.zzWL.unlock();
            }
            return t;
        } finally {
            this.zzWL.unlock();
        }
    }

    <A extends Client> void zzb(zze<A> com_google_android_gms_common_api_zzg_zze_A) {
        this.zzXA.add(com_google_android_gms_common_api_zzg_zze_A);
        com_google_android_gms_common_api_zzg_zze_A.zza(this.zzXB);
    }

    void zze(ConnectionResult connectionResult) {
        this.zzWL.lock();
        try {
            this.zzXy = connectionResult;
            this.zzXx = new zzf(this);
            this.zzXx.begin();
            this.zzXm.signalAll();
        } finally {
            this.zzWL.unlock();
        }
    }

    void zzmK() {
        for (zze com_google_android_gms_common_api_zzg_zze : this.zzXA) {
            com_google_android_gms_common_api_zzg_zze.zza(null);
            com_google_android_gms_common_api_zzg_zze.cancel();
        }
        this.zzXA.clear();
        for (zzi clear : this.zzXz) {
            clear.clear();
        }
        this.zzXz.clear();
        this.zzXw.clear();
    }

    void zzmL() {
        for (Client disconnect : this.zzXu.values()) {
            disconnect.disconnect();
        }
    }

    void zzmM() {
        this.zzWL.lock();
        try {
            this.zzXx = new zze(this, this.zzXa, this.zzXb, this.zzWE, this.zzWL, this.mContext);
            this.zzXx.begin();
            this.zzXm.signalAll();
        } finally {
            this.zzWL.unlock();
        }
    }

    void zzmN() {
        this.zzWL.lock();
        try {
            zzmR();
            this.zzXx = new zzd(this);
            this.zzXx.begin();
            this.zzXm.signalAll();
        } finally {
            this.zzWL.unlock();
        }
    }

    boolean zzmO() {
        return this.zzXp;
    }

    void zzmQ() {
        if (!zzmO()) {
            this.zzXp = true;
            if (this.zzXt == null) {
                this.zzXt = new zzb(this);
                IntentFilter intentFilter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
                intentFilter.addDataScheme("package");
                this.mContext.getApplicationContext().registerReceiver(this.zzXt, intentFilter);
            }
            this.zzXs.sendMessageDelayed(this.zzXs.obtainMessage(1), this.zzXq);
            this.zzXs.sendMessageDelayed(this.zzXs.obtainMessage(2), this.zzXr);
        }
    }

    boolean zzmR() {
        this.zzWL.lock();
        try {
            if (!zzmO()) {
                return false;
            }
            this.zzXp = false;
            this.zzXs.removeMessages(2);
            this.zzXs.removeMessages(1);
            if (this.zzXt != null) {
                this.mContext.getApplicationContext().unregisterReceiver(this.zzXt);
                this.zzXt = null;
            }
            this.zzWL.unlock();
            return true;
        } finally {
            this.zzWL.unlock();
        }
    }

    public <L> zzi<L> zzo(L l) {
        zzu.zzb((Object) l, (Object) "Listener must not be null");
        this.zzWL.lock();
        try {
            zzi<L> com_google_android_gms_common_api_zzi = new zzi(this.zzWt, l);
            this.zzXz.add(com_google_android_gms_common_api_zzi);
            return com_google_android_gms_common_api_zzi;
        } finally {
            this.zzWL.unlock();
        }
    }
}
