package com.google.android.gms.gcm;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.util.HashSet;
import java.util.Set;

public abstract class GcmTaskService extends Service {
    public static final String SERVICE_ACTION_EXECUTE_TASK = "com.google.android.gms.gcm.ACTION_TASK_READY";
    public static final String SERVICE_ACTION_INITIALIZE = "com.google.android.gms.gcm.SERVICE_ACTION_INITIALIZE";
    public static final String SERVICE_PERMISSION = "com.google.android.gms.permission.BIND_NETWORK_TASK_SERVICE";
    private final Set<String> zzavL = new HashSet();
    private int zzavM;

    private class zza extends Thread {
        private final String mTag;
        private final zzb zzavN;
        final /* synthetic */ GcmTaskService zzavO;

        zza(GcmTaskService gcmTaskService, String str, IBinder iBinder) {
            this.zzavO = gcmTaskService;
            this.mTag = str;
            this.zzavN = com.google.android.gms.gcm.zzb.zza.zzbN(iBinder);
        }

        public void run() {
            try {
                this.zzavN.zzgg(this.zzavO.onRunTask(new TaskParams(this.mTag)));
            } catch (RemoteException e) {
                Log.e("GcmTaskService", "Error reporting result of operation to scheduler for " + this.mTag);
            } finally {
                this.zzavO.zzdc(this.mTag);
            }
        }
    }

    private void zzdc(String str) {
        synchronized (this.zzavL) {
            this.zzavL.remove(str);
            if (this.zzavL.size() == 0) {
                stopSelf(this.zzavM);
            }
        }
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onInitializeTasks() {
    }

    public abstract int onRunTask(TaskParams taskParams);

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (SERVICE_ACTION_EXECUTE_TASK.equals(intent.getAction())) {
            String stringExtra = intent.getStringExtra("tag");
            Parcelable parcelableExtra = intent.getParcelableExtra("callback");
            if (parcelableExtra == null || !(parcelableExtra instanceof PendingCallback)) {
                Log.e("GcmTaskService", getPackageName() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + stringExtra + ": Could not process request, invalid callback.");
            } else {
                synchronized (this.zzavL) {
                    this.zzavL.add(stringExtra);
                    stopSelf(this.zzavM);
                    this.zzavM = startId;
                }
                new zza(this, stringExtra, ((PendingCallback) parcelableExtra).getIBinder()).start();
            }
        } else if (SERVICE_ACTION_INITIALIZE.equals(intent.getAction())) {
            onInitializeTasks();
            synchronized (this.zzavL) {
                this.zzavM = startId;
                if (this.zzavL.size() == 0) {
                    stopSelf(this.zzavM);
                }
            }
        }
        return 2;
    }
}
