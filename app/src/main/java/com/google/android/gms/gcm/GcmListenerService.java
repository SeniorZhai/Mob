package com.google.android.gms.gcm;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.IBinder;

public abstract class GcmListenerService extends Service {
    private int zzavB;
    private int zzavC = 0;
    private final Object zzqt = new Object();

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private final void zzk(android.content.Intent r5) {
        /*
        r4 = this;
        r0 = "com.google.android.c2dm.intent.RECEIVE";
        r1 = r5.getAction();	 Catch:{ all -> 0x0087 }
        r0 = r0.equals(r1);	 Catch:{ all -> 0x0087 }
        if (r0 != 0) goto L_0x0010;
    L_0x000c:
        android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent(r5);
    L_0x000f:
        return;
    L_0x0010:
        r0 = "message_type";
        r0 = r5.getStringExtra(r0);	 Catch:{ all -> 0x0087 }
        if (r0 != 0) goto L_0x00ad;
    L_0x0018:
        r0 = "gcm";
        r1 = r0;
    L_0x001b:
        r0 = -1;
        r2 = r1.hashCode();	 Catch:{ all -> 0x0087 }
        switch(r2) {
            case -2062414158: goto L_0x005f;
            case 102161: goto L_0x0055;
            case 814694033: goto L_0x0074;
            case 814800675: goto L_0x0069;
            default: goto L_0x0023;
        };	 Catch:{ all -> 0x0087 }
    L_0x0023:
        switch(r0) {
            case 0: goto L_0x007f;
            case 1: goto L_0x008c;
            case 2: goto L_0x0090;
            case 3: goto L_0x009a;
            default: goto L_0x0026;
        };	 Catch:{ all -> 0x0087 }
    L_0x0026:
        r0 = "GcmListenerService";
        r2 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0087 }
        r2.<init>();	 Catch:{ all -> 0x0087 }
        r3 = "Received message with unknown type: ";
        r2 = r2.append(r3);	 Catch:{ all -> 0x0087 }
        r1 = r2.append(r1);	 Catch:{ all -> 0x0087 }
        r1 = r1.toString();	 Catch:{ all -> 0x0087 }
        android.util.Log.w(r0, r1);	 Catch:{ all -> 0x0087 }
    L_0x003e:
        r1 = r4.zzqt;	 Catch:{ all -> 0x0087 }
        monitor-enter(r1);	 Catch:{ all -> 0x0087 }
        r0 = r4.zzavC;	 Catch:{ all -> 0x00aa }
        r0 = r0 + -1;
        r4.zzavC = r0;	 Catch:{ all -> 0x00aa }
        r0 = r4.zzavC;	 Catch:{ all -> 0x00aa }
        if (r0 != 0) goto L_0x0050;
    L_0x004b:
        r0 = r4.zzavB;	 Catch:{ all -> 0x00aa }
        r4.zzgf(r0);	 Catch:{ all -> 0x00aa }
    L_0x0050:
        monitor-exit(r1);	 Catch:{ all -> 0x00aa }
        android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent(r5);
        goto L_0x000f;
    L_0x0055:
        r2 = "gcm";
        r2 = r1.equals(r2);	 Catch:{ all -> 0x0087 }
        if (r2 == 0) goto L_0x0023;
    L_0x005d:
        r0 = 0;
        goto L_0x0023;
    L_0x005f:
        r2 = "deleted_messages";
        r2 = r1.equals(r2);	 Catch:{ all -> 0x0087 }
        if (r2 == 0) goto L_0x0023;
    L_0x0067:
        r0 = 1;
        goto L_0x0023;
    L_0x0069:
        r2 = "send_event";
        r2 = r1.equals(r2);	 Catch:{ all -> 0x0087 }
        if (r2 == 0) goto L_0x0023;
    L_0x0072:
        r0 = 2;
        goto L_0x0023;
    L_0x0074:
        r2 = "send_error";
        r2 = r1.equals(r2);	 Catch:{ all -> 0x0087 }
        if (r2 == 0) goto L_0x0023;
    L_0x007d:
        r0 = 3;
        goto L_0x0023;
    L_0x007f:
        r0 = r5.getExtras();	 Catch:{ all -> 0x0087 }
        r4.zzs(r0);	 Catch:{ all -> 0x0087 }
        goto L_0x003e;
    L_0x0087:
        r0 = move-exception;
        android.support.v4.content.WakefulBroadcastReceiver.completeWakefulIntent(r5);
        throw r0;
    L_0x008c:
        r4.onDeletedMessages();	 Catch:{ all -> 0x0087 }
        goto L_0x003e;
    L_0x0090:
        r0 = "google.message_id";
        r0 = r5.getStringExtra(r0);	 Catch:{ all -> 0x0087 }
        r4.onMessageSent(r0);	 Catch:{ all -> 0x0087 }
        goto L_0x003e;
    L_0x009a:
        r0 = "google.message_id";
        r0 = r5.getStringExtra(r0);	 Catch:{ all -> 0x0087 }
        r1 = "error";
        r1 = r5.getStringExtra(r1);	 Catch:{ all -> 0x0087 }
        r4.onSendError(r0, r1);	 Catch:{ all -> 0x0087 }
        goto L_0x003e;
    L_0x00aa:
        r0 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x00aa }
        throw r0;	 Catch:{ all -> 0x0087 }
    L_0x00ad:
        r1 = r0;
        goto L_0x001b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.gcm.GcmListenerService.zzk(android.content.Intent):void");
    }

    private void zzs(Bundle bundle) {
        bundle.remove("message_type");
        bundle.remove("android.support.content.wakelockid");
        if (zza.zzt(bundle)) {
            zza.zzar(this).zzu(bundle);
            return;
        }
        String string = bundle.getString("from");
        bundle.remove("from");
        onMessageReceived(string, bundle);
    }

    public final IBinder onBind(Intent intent) {
        return null;
    }

    public void onDeletedMessages() {
    }

    public void onMessageReceived(String from, Bundle data) {
    }

    public void onMessageSent(String msgId) {
    }

    public void onSendError(String msgId, String error) {
    }

    public final int onStartCommand(final Intent intent, int flags, int startId) {
        synchronized (this.zzqt) {
            this.zzavB = startId;
            this.zzavC++;
        }
        if (VERSION.SDK_INT >= 11) {
            AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable(this) {
                final /* synthetic */ GcmListenerService zzavD;

                public void run() {
                    this.zzavD.zzk(intent);
                }
            });
        } else {
            new AsyncTask<Void, Void, Void>(this) {
                final /* synthetic */ GcmListenerService zzavD;

                protected /* synthetic */ Object doInBackground(Object[] x0) {
                    return zzb((Void[]) x0);
                }

                protected Void zzb(Void... voidArr) {
                    this.zzavD.zzk(intent);
                    return null;
                }
            }.execute(new Void[0]);
        }
        return 3;
    }

    boolean zzgf(int i) {
        return stopSelfResult(i);
    }
}
