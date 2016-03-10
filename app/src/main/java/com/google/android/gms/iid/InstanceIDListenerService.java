package com.google.android.gms.iid;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import com.facebook.internal.NativeProtocol;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import io.fabric.sdk.android.BuildConfig;
import java.io.IOException;

public class InstanceIDListenerService extends Service {
    static String ACTION = NativeProtocol.WEB_DIALOG_ACTION;
    private static String zzavK = "google.com/iid";
    private static String zzawW = "CMD";
    MessengerCompat zzawU = new MessengerCompat(new Handler(this, Looper.getMainLooper()) {
        final /* synthetic */ InstanceIDListenerService zzawZ;

        public void handleMessage(Message msg) {
            this.zzawZ.zza(msg, MessengerCompat.zzc(msg));
        }
    });
    BroadcastReceiver zzawV = new BroadcastReceiver(this) {
        final /* synthetic */ InstanceIDListenerService zzawZ;

        {
            this.zzawZ = r1;
        }

        public void onReceive(Context context, Intent intent) {
            if (Log.isLoggable("InstanceID", 3)) {
                intent.getStringExtra(PreferenceUtility.REG_ID);
                Log.d("InstanceID", "Received GSF callback using dynamic receiver: " + intent.getExtras());
            }
            this.zzawZ.zzn(intent);
            this.zzawZ.stop();
        }
    };
    int zzawX;
    int zzawY;

    static void zza(Context context, zzd com_google_android_gms_iid_zzd) {
        com_google_android_gms_iid_zzd.zzul();
        Intent intent = new Intent("com.google.android.gms.iid.InstanceID");
        intent.putExtra(zzawW, "RST");
        intent.setPackage(context.getPackageName());
        context.startService(intent);
    }

    private void zza(Message message, int i) {
        zzc.zzaw(this);
        getPackageManager();
        if (i == zzc.zzaxf || i == zzc.zzaxe) {
            zzn((Intent) message.obj);
        } else {
            Log.w("InstanceID", "Message from unexpected caller " + i + " mine=" + zzc.zzaxe + " appid=" + zzc.zzaxf);
        }
    }

    static void zzav(Context context) {
        Intent intent = new Intent("com.google.android.gms.iid.InstanceID");
        intent.setPackage(context.getPackageName());
        intent.putExtra(zzawW, "SYNC");
        context.startService(intent);
    }

    public IBinder onBind(Intent intent) {
        return (intent == null || !"com.google.android.gms.iid.InstanceID".equals(intent.getAction())) ? null : this.zzawU.getBinder();
    }

    public void onCreate() {
        IntentFilter intentFilter = new IntentFilter("com.google.android.c2dm.intent.REGISTRATION");
        intentFilter.addCategory(getPackageName());
        registerReceiver(this.zzawV, intentFilter, "com.google.android.c2dm.permission.RECEIVE", null);
    }

    public void onDestroy() {
        unregisterReceiver(this.zzawV);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        zzgn(startId);
        if (intent == null) {
            stop();
            return 2;
        }
        try {
            if ("com.google.android.gms.iid.InstanceID".equals(intent.getAction())) {
                if (VERSION.SDK_INT <= 18) {
                    Intent intent2 = (Intent) intent.getParcelableExtra("GSF");
                    if (intent2 != null) {
                        startService(intent2);
                        return 1;
                    }
                }
                zzn(intent);
            }
            stop();
            if (intent.getStringExtra("from") != null) {
                WakefulBroadcastReceiver.completeWakefulIntent(intent);
            }
            return 2;
        } finally {
            stop();
        }
    }

    public void onTokenRefresh() {
    }

    void stop() {
        synchronized (this) {
            this.zzawX--;
            if (this.zzawX == 0) {
                stopSelf(this.zzawY);
            }
            if (Log.isLoggable("InstanceID", 3)) {
                Log.d("InstanceID", "Stop " + this.zzawX + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + this.zzawY);
            }
        }
    }

    public void zzab(boolean z) {
        onTokenRefresh();
    }

    void zzgn(int i) {
        synchronized (this) {
            this.zzawX++;
            if (i > this.zzawY) {
                this.zzawY = i;
            }
        }
    }

    public void zzn(Intent intent) {
        InstanceID instance;
        String stringExtra = intent.getStringExtra("subtype");
        if (stringExtra == null) {
            instance = InstanceID.getInstance(this);
        } else {
            Bundle bundle = new Bundle();
            bundle.putString("subtype", stringExtra);
            instance = InstanceID.zza(this, bundle);
        }
        String stringExtra2 = intent.getStringExtra(zzawW);
        if (intent.getStringExtra(Extra.ERROR) == null && intent.getStringExtra(PreferenceUtility.REG_ID) == null) {
            if (Log.isLoggable("InstanceID", 3)) {
                Log.d("InstanceID", "Service command " + stringExtra + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + stringExtra2 + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + intent.getExtras());
            }
            if (intent.getStringExtra("unregistered") != null) {
                zzd zzug = instance.zzug();
                if (stringExtra == null) {
                    stringExtra = BuildConfig.FLAVOR;
                }
                zzug.zzdi(stringExtra);
                instance.zzuh().zzr(intent);
                return;
            } else if ("RST".equals(stringExtra2)) {
                instance.zzuf();
                zzab(true);
                return;
            } else if ("RST_FULL".equals(stringExtra2)) {
                if (!instance.zzug().isEmpty()) {
                    instance.zzug().zzul();
                    zzab(true);
                    return;
                }
                return;
            } else if ("SYNC".equals(stringExtra2)) {
                instance.zzug().zzdi(stringExtra);
                zzab(false);
                return;
            } else if ("PING".equals(stringExtra2)) {
                try {
                    GoogleCloudMessaging.getInstance(this).send(zzavK, zzc.zzuk(), 0, intent.getExtras());
                    return;
                } catch (IOException e) {
                    Log.w("InstanceID", "Failed to send ping response");
                    return;
                }
            } else {
                return;
            }
        }
        if (Log.isLoggable("InstanceID", 3)) {
            Log.d("InstanceID", "Register result in service " + stringExtra);
        }
        instance.zzuh().zzr(intent);
    }
}
