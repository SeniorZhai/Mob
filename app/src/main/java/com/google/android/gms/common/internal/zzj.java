package com.google.android.gms.common.internal;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public final class zzj implements Callback {
    private final Handler mHandler;
    private final zza zzaaC;
    private final ArrayList<ConnectionCallbacks> zzaaD = new ArrayList();
    final ArrayList<ConnectionCallbacks> zzaaE = new ArrayList();
    private final ArrayList<OnConnectionFailedListener> zzaaF = new ArrayList();
    private volatile boolean zzaaG = false;
    private final AtomicInteger zzaaH = new AtomicInteger(0);
    private boolean zzaaI = false;
    private final Object zzqt = new Object();

    public interface zza {
        boolean isConnected();

        Bundle zzlM();
    }

    public zzj(Looper looper, zza com_google_android_gms_common_internal_zzj_zza) {
        this.zzaaC = com_google_android_gms_common_internal_zzj_zza;
        this.mHandler = new Handler(looper, this);
    }

    public boolean handleMessage(Message msg) {
        if (msg.what == 1) {
            ConnectionCallbacks connectionCallbacks = (ConnectionCallbacks) msg.obj;
            synchronized (this.zzqt) {
                if (this.zzaaG && this.zzaaC.isConnected() && this.zzaaD.contains(connectionCallbacks)) {
                    connectionCallbacks.onConnected(this.zzaaC.zzlM());
                }
            }
            return true;
        }
        Log.wtf("GmsClientEvents", "Don't know how to handle this message.");
        return false;
    }

    public boolean isConnectionCallbacksRegistered(ConnectionCallbacks listener) {
        boolean contains;
        zzu.zzu(listener);
        synchronized (this.zzqt) {
            contains = this.zzaaD.contains(listener);
        }
        return contains;
    }

    public boolean isConnectionFailedListenerRegistered(OnConnectionFailedListener listener) {
        boolean contains;
        zzu.zzu(listener);
        synchronized (this.zzqt) {
            contains = this.zzaaF.contains(listener);
        }
        return contains;
    }

    public void registerConnectionCallbacks(ConnectionCallbacks listener) {
        zzu.zzu(listener);
        synchronized (this.zzqt) {
            if (this.zzaaD.contains(listener)) {
                Log.w("GmsClientEvents", "registerConnectionCallbacks(): listener " + listener + " is already registered");
            } else {
                this.zzaaD.add(listener);
            }
        }
        if (this.zzaaC.isConnected()) {
            this.mHandler.sendMessage(this.mHandler.obtainMessage(1, listener));
        }
    }

    public void registerConnectionFailedListener(OnConnectionFailedListener listener) {
        zzu.zzu(listener);
        synchronized (this.zzqt) {
            if (this.zzaaF.contains(listener)) {
                Log.w("GmsClientEvents", "registerConnectionFailedListener(): listener " + listener + " is already registered");
            } else {
                this.zzaaF.add(listener);
            }
        }
    }

    public void unregisterConnectionCallbacks(ConnectionCallbacks listener) {
        zzu.zzu(listener);
        synchronized (this.zzqt) {
            if (!this.zzaaD.remove(listener)) {
                Log.w("GmsClientEvents", "unregisterConnectionCallbacks(): listener " + listener + " not found");
            } else if (this.zzaaI) {
                this.zzaaE.add(listener);
            }
        }
    }

    public void unregisterConnectionFailedListener(OnConnectionFailedListener listener) {
        zzu.zzu(listener);
        synchronized (this.zzqt) {
            if (!this.zzaaF.remove(listener)) {
                Log.w("GmsClientEvents", "unregisterConnectionFailedListener(): listener " + listener + " not found");
            }
        }
    }

    public void zzbu(int i) {
        this.mHandler.removeMessages(1);
        synchronized (this.zzqt) {
            this.zzaaI = true;
            ArrayList arrayList = new ArrayList(this.zzaaD);
            int i2 = this.zzaaH.get();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ConnectionCallbacks connectionCallbacks = (ConnectionCallbacks) it.next();
                if (!this.zzaaG || this.zzaaH.get() != i2) {
                    break;
                } else if (this.zzaaD.contains(connectionCallbacks)) {
                    connectionCallbacks.onConnectionSuspended(i);
                }
            }
            this.zzaaE.clear();
            this.zzaaI = false;
        }
    }

    public void zzg(Bundle bundle) {
        boolean z = true;
        synchronized (this.zzqt) {
            zzu.zzU(!this.zzaaI);
            this.mHandler.removeMessages(1);
            this.zzaaI = true;
            if (this.zzaaE.size() != 0) {
                z = false;
            }
            zzu.zzU(z);
            ArrayList arrayList = new ArrayList(this.zzaaD);
            int i = this.zzaaH.get();
            Iterator it = arrayList.iterator();
            while (it.hasNext()) {
                ConnectionCallbacks connectionCallbacks = (ConnectionCallbacks) it.next();
                if (!this.zzaaG || !this.zzaaC.isConnected() || this.zzaaH.get() != i) {
                    break;
                } else if (!this.zzaaE.contains(connectionCallbacks)) {
                    connectionCallbacks.onConnected(bundle);
                }
            }
            this.zzaaE.clear();
            this.zzaaI = false;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void zzh(com.google.android.gms.common.ConnectionResult r6) {
        /*
        r5 = this;
        r0 = r5.mHandler;
        r1 = 1;
        r0.removeMessages(r1);
        r1 = r5.zzqt;
        monitor-enter(r1);
        r0 = new java.util.ArrayList;	 Catch:{ all -> 0x0040 }
        r2 = r5.zzaaF;	 Catch:{ all -> 0x0040 }
        r0.<init>(r2);	 Catch:{ all -> 0x0040 }
        r2 = r5.zzaaH;	 Catch:{ all -> 0x0040 }
        r2 = r2.get();	 Catch:{ all -> 0x0040 }
        r3 = r0.iterator();	 Catch:{ all -> 0x0040 }
    L_0x001a:
        r0 = r3.hasNext();	 Catch:{ all -> 0x0040 }
        if (r0 == 0) goto L_0x0043;
    L_0x0020:
        r0 = r3.next();	 Catch:{ all -> 0x0040 }
        r0 = (com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener) r0;	 Catch:{ all -> 0x0040 }
        r4 = r5.zzaaG;	 Catch:{ all -> 0x0040 }
        if (r4 == 0) goto L_0x0032;
    L_0x002a:
        r4 = r5.zzaaH;	 Catch:{ all -> 0x0040 }
        r4 = r4.get();	 Catch:{ all -> 0x0040 }
        if (r4 == r2) goto L_0x0034;
    L_0x0032:
        monitor-exit(r1);	 Catch:{ all -> 0x0040 }
    L_0x0033:
        return;
    L_0x0034:
        r4 = r5.zzaaF;	 Catch:{ all -> 0x0040 }
        r4 = r4.contains(r0);	 Catch:{ all -> 0x0040 }
        if (r4 == 0) goto L_0x001a;
    L_0x003c:
        r0.onConnectionFailed(r6);	 Catch:{ all -> 0x0040 }
        goto L_0x001a;
    L_0x0040:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0040 }
        throw r0;
    L_0x0043:
        monitor-exit(r1);	 Catch:{ all -> 0x0040 }
        goto L_0x0033;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.internal.zzj.zzh(com.google.android.gms.common.ConnectionResult):void");
    }

    public void zznT() {
        this.zzaaG = false;
        this.zzaaH.incrementAndGet();
    }

    public void zznU() {
        this.zzaaG = true;
    }
}
