package com.google.android.gms.common.api;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import com.google.android.gms.common.api.PendingResult.BatchCallback;
import com.google.android.gms.common.internal.ICancelToken;
import com.google.android.gms.common.internal.zzu;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public abstract class AbstractPendingResult<R extends Result> implements PendingResult<R> {
    protected final CallbackHandler<R> mHandler;
    private boolean zzL;
    private final Object zzWb = new Object();
    private final ArrayList<BatchCallback> zzWc = new ArrayList();
    private ResultCallback<R> zzWd;
    private volatile R zzWe;
    private volatile boolean zzWf;
    private boolean zzWg;
    private ICancelToken zzWh;
    private final CountDownLatch zzoD = new CountDownLatch(1);

    public static class CallbackHandler<R extends Result> extends Handler {
        public static final int CALLBACK_ON_COMPLETE = 1;
        public static final int CALLBACK_ON_TIMEOUT = 2;

        public CallbackHandler() {
            this(Looper.getMainLooper());
        }

        public CallbackHandler(Looper looper) {
            super(looper);
        }

        protected void deliverResultCallback(ResultCallback<R> callback, R result) {
            try {
                callback.onResult(result);
            } catch (RuntimeException e) {
                AbstractPendingResult.zzb(result);
                throw e;
            }
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CALLBACK_ON_COMPLETE /*1*/:
                    Pair pair = (Pair) msg.obj;
                    deliverResultCallback((ResultCallback) pair.first, (Result) pair.second);
                    return;
                case CALLBACK_ON_TIMEOUT /*2*/:
                    ((AbstractPendingResult) msg.obj).forceFailureUnlessReady(Status.zzXS);
                    return;
                default:
                    Log.wtf("AbstractPendingResult", "Don't know how to handle this message.");
                    return;
            }
        }

        public void removeTimeoutMessages() {
            removeMessages(CALLBACK_ON_TIMEOUT);
        }

        public void sendResultCallback(ResultCallback<R> callback, R result) {
            sendMessage(obtainMessage(CALLBACK_ON_COMPLETE, new Pair(callback, result)));
        }

        public void sendTimeoutResultCallback(AbstractPendingResult<R> pendingResult, long millis) {
            sendMessageDelayed(obtainMessage(CALLBACK_ON_TIMEOUT, pendingResult), millis);
        }
    }

    protected AbstractPendingResult(Looper looper) {
        this.mHandler = new CallbackHandler(looper);
    }

    protected AbstractPendingResult(CallbackHandler<R> callbackHandler) {
        this.mHandler = (CallbackHandler) zzu.zzb((Object) callbackHandler, (Object) "CallbackHandler must not be null");
    }

    private void zza(R r) {
        this.zzWe = r;
        this.zzWh = null;
        this.zzoD.countDown();
        Status status = this.zzWe.getStatus();
        if (this.zzWd != null) {
            this.mHandler.removeTimeoutMessages();
            if (!this.zzL) {
                this.mHandler.sendResultCallback(this.zzWd, zzmo());
            }
        }
        Iterator it = this.zzWc.iterator();
        while (it.hasNext()) {
            ((BatchCallback) it.next()).zzs(status);
        }
        this.zzWc.clear();
    }

    static void zzb(Result result) {
        if (result instanceof Releasable) {
            try {
                ((Releasable) result).release();
            } catch (Throwable e) {
                Log.w("AbstractPendingResult", "Unable to release " + result, e);
            }
        }
    }

    private R zzmo() {
        R r;
        boolean z = true;
        synchronized (this.zzWb) {
            if (this.zzWf) {
                z = false;
            }
            zzu.zza(z, (Object) "Result has already been consumed.");
            zzu.zza(isReady(), (Object) "Result is not ready.");
            r = this.zzWe;
            this.zzWe = null;
            this.zzWd = null;
            this.zzWf = true;
        }
        onResultConsumed();
        return r;
    }

    public final void addBatchCallback(BatchCallback callback) {
        zzu.zza(!this.zzWf, (Object) "Result has already been consumed.");
        synchronized (this.zzWb) {
            if (isReady()) {
                callback.zzs(this.zzWe.getStatus());
            } else {
                this.zzWc.add(callback);
            }
        }
    }

    public final R await() {
        boolean z = true;
        zzu.zza(Looper.myLooper() != Looper.getMainLooper(), (Object) "await must not be called on the UI thread");
        if (this.zzWf) {
            z = false;
        }
        zzu.zza(z, (Object) "Result has already been consumed");
        try {
            this.zzoD.await();
        } catch (InterruptedException e) {
            forceFailureUnlessReady(Status.zzXQ);
        }
        zzu.zza(isReady(), (Object) "Result is not ready.");
        return zzmo();
    }

    public final R await(long time, TimeUnit units) {
        boolean z = true;
        boolean z2 = time <= 0 || Looper.myLooper() != Looper.getMainLooper();
        zzu.zza(z2, (Object) "await must not be called on the UI thread when time is greater than zero.");
        if (this.zzWf) {
            z = false;
        }
        zzu.zza(z, (Object) "Result has already been consumed.");
        try {
            if (!this.zzoD.await(time, units)) {
                forceFailureUnlessReady(Status.zzXS);
            }
        } catch (InterruptedException e) {
            forceFailureUnlessReady(Status.zzXQ);
        }
        zzu.zza(isReady(), (Object) "Result is not ready.");
        return zzmo();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void cancel() {
        /*
        r2 = this;
        r1 = r2.zzWb;
        monitor-enter(r1);
        r0 = r2.zzL;	 Catch:{ all -> 0x002c }
        if (r0 != 0) goto L_0x000b;
    L_0x0007:
        r0 = r2.zzWf;	 Catch:{ all -> 0x002c }
        if (r0 == 0) goto L_0x000d;
    L_0x000b:
        monitor-exit(r1);	 Catch:{ all -> 0x002c }
    L_0x000c:
        return;
    L_0x000d:
        r0 = r2.zzWh;	 Catch:{ all -> 0x002c }
        if (r0 == 0) goto L_0x0016;
    L_0x0011:
        r0 = r2.zzWh;	 Catch:{ RemoteException -> 0x002f }
        r0.cancel();	 Catch:{ RemoteException -> 0x002f }
    L_0x0016:
        r0 = r2.zzWe;	 Catch:{ all -> 0x002c }
        zzb(r0);	 Catch:{ all -> 0x002c }
        r0 = 0;
        r2.zzWd = r0;	 Catch:{ all -> 0x002c }
        r0 = 1;
        r2.zzL = r0;	 Catch:{ all -> 0x002c }
        r0 = com.google.android.gms.common.api.Status.zzXT;	 Catch:{ all -> 0x002c }
        r0 = r2.createFailedResult(r0);	 Catch:{ all -> 0x002c }
        r2.zza(r0);	 Catch:{ all -> 0x002c }
        monitor-exit(r1);	 Catch:{ all -> 0x002c }
        goto L_0x000c;
    L_0x002c:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x002c }
        throw r0;
    L_0x002f:
        r0 = move-exception;
        goto L_0x0016;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.api.AbstractPendingResult.cancel():void");
    }

    protected abstract R createFailedResult(Status status);

    public final void forceFailureUnlessReady(Status status) {
        synchronized (this.zzWb) {
            if (!isReady()) {
                setResult(createFailedResult(status));
                this.zzWg = true;
            }
        }
    }

    public boolean isCanceled() {
        boolean z;
        synchronized (this.zzWb) {
            z = this.zzL;
        }
        return z;
    }

    public final boolean isReady() {
        return this.zzoD.getCount() == 0;
    }

    protected void onResultConsumed() {
    }

    protected final void setCancelToken(ICancelToken cancelToken) {
        synchronized (this.zzWb) {
            this.zzWh = cancelToken;
        }
    }

    public final void setResult(R result) {
        boolean z = true;
        synchronized (this.zzWb) {
            if (this.zzWg || this.zzL) {
                zzb(result);
                return;
            }
            zzu.zza(!isReady(), (Object) "Results have already been set");
            if (this.zzWf) {
                z = false;
            }
            zzu.zza(z, (Object) "Result has already been consumed");
            zza(result);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void setResultCallback(com.google.android.gms.common.api.ResultCallback<R> r4) {
        /*
        r3 = this;
        r0 = r3.zzWf;
        if (r0 != 0) goto L_0x0015;
    L_0x0004:
        r0 = 1;
    L_0x0005:
        r1 = "Result has already been consumed.";
        com.google.android.gms.common.internal.zzu.zza(r0, r1);
        r1 = r3.zzWb;
        monitor-enter(r1);
        r0 = r3.isCanceled();	 Catch:{ all -> 0x0028 }
        if (r0 == 0) goto L_0x0017;
    L_0x0013:
        monitor-exit(r1);	 Catch:{ all -> 0x0028 }
    L_0x0014:
        return;
    L_0x0015:
        r0 = 0;
        goto L_0x0005;
    L_0x0017:
        r0 = r3.isReady();	 Catch:{ all -> 0x0028 }
        if (r0 == 0) goto L_0x002b;
    L_0x001d:
        r0 = r3.mHandler;	 Catch:{ all -> 0x0028 }
        r2 = r3.zzmo();	 Catch:{ all -> 0x0028 }
        r0.sendResultCallback(r4, r2);	 Catch:{ all -> 0x0028 }
    L_0x0026:
        monitor-exit(r1);	 Catch:{ all -> 0x0028 }
        goto L_0x0014;
    L_0x0028:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0028 }
        throw r0;
    L_0x002b:
        r3.zzWd = r4;	 Catch:{ all -> 0x0028 }
        goto L_0x0026;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.api.AbstractPendingResult.setResultCallback(com.google.android.gms.common.api.ResultCallback):void");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final void setResultCallback(com.google.android.gms.common.api.ResultCallback<R> r5, long r6, java.util.concurrent.TimeUnit r8) {
        /*
        r4 = this;
        r0 = r4.zzWf;
        if (r0 != 0) goto L_0x0015;
    L_0x0004:
        r0 = 1;
    L_0x0005:
        r1 = "Result has already been consumed.";
        com.google.android.gms.common.internal.zzu.zza(r0, r1);
        r1 = r4.zzWb;
        monitor-enter(r1);
        r0 = r4.isCanceled();	 Catch:{ all -> 0x0028 }
        if (r0 == 0) goto L_0x0017;
    L_0x0013:
        monitor-exit(r1);	 Catch:{ all -> 0x0028 }
    L_0x0014:
        return;
    L_0x0015:
        r0 = 0;
        goto L_0x0005;
    L_0x0017:
        r0 = r4.isReady();	 Catch:{ all -> 0x0028 }
        if (r0 == 0) goto L_0x002b;
    L_0x001d:
        r0 = r4.mHandler;	 Catch:{ all -> 0x0028 }
        r2 = r4.zzmo();	 Catch:{ all -> 0x0028 }
        r0.sendResultCallback(r5, r2);	 Catch:{ all -> 0x0028 }
    L_0x0026:
        monitor-exit(r1);	 Catch:{ all -> 0x0028 }
        goto L_0x0014;
    L_0x0028:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0028 }
        throw r0;
    L_0x002b:
        r4.zzWd = r5;	 Catch:{ all -> 0x0028 }
        r0 = r4.mHandler;	 Catch:{ all -> 0x0028 }
        r2 = r8.toMillis(r6);	 Catch:{ all -> 0x0028 }
        r0.sendTimeoutResultCallback(r4, r2);	 Catch:{ all -> 0x0028 }
        goto L_0x0026;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.api.AbstractPendingResult.setResultCallback(com.google.android.gms.common.api.ResultCallback, long, java.util.concurrent.TimeUnit):void");
    }
}
