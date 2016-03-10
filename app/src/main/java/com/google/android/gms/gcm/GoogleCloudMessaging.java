package com.google.android.gms.gcm;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.zzc;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class GoogleCloudMessaging {
    public static final String ERROR_MAIN_THREAD = "MAIN_THREAD";
    public static final String ERROR_SERVICE_NOT_AVAILABLE = "SERVICE_NOT_AVAILABLE";
    public static final String INSTANCE_ID_SCOPE = "GCM";
    @Deprecated
    public static final String MESSAGE_TYPE_DELETED = "deleted_messages";
    @Deprecated
    public static final String MESSAGE_TYPE_MESSAGE = "gcm";
    @Deprecated
    public static final String MESSAGE_TYPE_SEND_ERROR = "send_error";
    @Deprecated
    public static final String MESSAGE_TYPE_SEND_EVENT = "send_event";
    public static int zzavP = 5000000;
    public static int zzavQ = 6500000;
    public static int zzavR = 7000000;
    static GoogleCloudMessaging zzavS;
    private static final AtomicInteger zzavV = new AtomicInteger(1);
    private PendingIntent zzavT;
    private Map<String, Handler> zzavU = Collections.synchronizedMap(new HashMap());
    private final BlockingQueue<Intent> zzavW = new LinkedBlockingQueue();
    final Messenger zzavX = new Messenger(new Handler(this, Looper.getMainLooper()) {
        final /* synthetic */ GoogleCloudMessaging zzavY;

        public void handleMessage(Message msg) {
            if (msg == null || !(msg.obj instanceof Intent)) {
                Log.w(GoogleCloudMessaging.INSTANCE_ID_SCOPE, "Dropping invalid message");
            }
            Intent intent = (Intent) msg.obj;
            if ("com.google.android.c2dm.intent.REGISTRATION".equals(intent.getAction())) {
                this.zzavY.zzavW.add(intent);
            } else if (!this.zzavY.zzl(intent)) {
                intent.setPackage(this.zzavY.zzpH.getPackageName());
                this.zzavY.zzpH.sendBroadcast(intent);
            }
        }
    });
    private Context zzpH;

    public static synchronized GoogleCloudMessaging getInstance(Context context) {
        GoogleCloudMessaging googleCloudMessaging;
        synchronized (GoogleCloudMessaging.class) {
            if (zzavS == null) {
                zzavS = new GoogleCloudMessaging();
                zzavS.zzpH = context.getApplicationContext();
            }
            googleCloudMessaging = zzavS;
        }
        return googleCloudMessaging;
    }

    static String zza(Intent intent, String str) throws IOException {
        if (intent == null) {
            throw new IOException(ERROR_SERVICE_NOT_AVAILABLE);
        }
        String stringExtra = intent.getStringExtra(str);
        if (stringExtra != null) {
            return stringExtra;
        }
        stringExtra = intent.getStringExtra(Extra.ERROR);
        if (stringExtra != null) {
            throw new IOException(stringExtra);
        }
        throw new IOException(ERROR_SERVICE_NOT_AVAILABLE);
    }

    private void zza(String str, String str2, long j, int i, Bundle bundle) throws IOException {
        if (str == null) {
            throw new IllegalArgumentException("Missing 'to'");
        }
        Intent intent = new Intent("com.google.android.gcm.intent.SEND");
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        zzm(intent);
        intent.setPackage(zzas(this.zzpH));
        intent.putExtra("google.to", str);
        intent.putExtra("google.message_id", str2);
        intent.putExtra("google.ttl", Long.toString(j));
        intent.putExtra("google.delay", Integer.toString(i));
        if (zzas(this.zzpH).contains(".gsf")) {
            Bundle bundle2 = new Bundle();
            for (String str3 : bundle.keySet()) {
                Object obj = bundle.get(str3);
                if (obj instanceof String) {
                    bundle2.putString("gcm." + str3, (String) obj);
                }
            }
            bundle2.putString("google.to", str);
            bundle2.putString("google.message_id", str2);
            InstanceID.getInstance(this.zzpH).zzc(INSTANCE_ID_SCOPE, "upstream", bundle2);
            return;
        }
        this.zzpH.sendOrderedBroadcast(intent, "com.google.android.gtalkservice.permission.GTALK_SERVICE");
    }

    public static String zzas(Context context) {
        return zzc.zzaw(context);
    }

    public static int zzat(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(zzas(context), 0).versionCode;
        } catch (NameNotFoundException e) {
            return -1;
        }
    }

    private boolean zzl(Intent intent) {
        Object stringExtra = intent.getStringExtra("In-Reply-To");
        if (stringExtra == null && intent.hasExtra(Extra.ERROR)) {
            stringExtra = intent.getStringExtra("google.message_id");
        }
        if (stringExtra != null) {
            Handler handler = (Handler) this.zzavU.remove(stringExtra);
            if (handler != null) {
                Message obtain = Message.obtain();
                obtain.obj = intent;
                return handler.sendMessage(obtain);
            }
        }
        return false;
    }

    private String zztS() {
        return "google.rpc" + String.valueOf(zzavV.getAndIncrement());
    }

    public void close() {
        zzavS = null;
        zza.zzavF = null;
        zztT();
    }

    public String getMessageType(Intent intent) {
        if (!"com.google.android.c2dm.intent.RECEIVE".equals(intent.getAction())) {
            return null;
        }
        String stringExtra = intent.getStringExtra("message_type");
        return stringExtra == null ? MESSAGE_TYPE_MESSAGE : stringExtra;
    }

    @Deprecated
    public synchronized String register(String... senderIds) throws IOException {
        String zzc;
        zzc = zzc(senderIds);
        Bundle bundle = new Bundle();
        if (zzas(this.zzpH).contains(".gsf")) {
            bundle.putString("legacy.sender", zzc);
            zzc = InstanceID.getInstance(this.zzpH).getToken(zzc, INSTANCE_ID_SCOPE, bundle);
        } else {
            bundle.putString("sender", zzc);
            zzc = zza(zzx(bundle), PreferenceUtility.REG_ID);
        }
        return zzc;
    }

    public void send(String to, String msgId, long timeToLive, Bundle data) throws IOException {
        zza(to, msgId, timeToLive, -1, data);
    }

    public void send(String to, String msgId, Bundle data) throws IOException {
        send(to, msgId, -1, data);
    }

    @Deprecated
    public synchronized void unregister() throws IOException {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new IOException(ERROR_MAIN_THREAD);
        }
        InstanceID.getInstance(this.zzpH).deleteInstanceID();
    }

    String zzc(String... strArr) {
        if (strArr == null || strArr.length == 0) {
            throw new IllegalArgumentException("No senderIds");
        }
        StringBuilder stringBuilder = new StringBuilder(strArr[0]);
        for (int i = 1; i < strArr.length; i++) {
            stringBuilder.append(',').append(strArr[i]);
        }
        return stringBuilder.toString();
    }

    synchronized void zzm(Intent intent) {
        if (this.zzavT == null) {
            Intent intent2 = new Intent();
            intent2.setPackage("com.google.example.invalidpackage");
            this.zzavT = PendingIntent.getBroadcast(this.zzpH, 0, intent2, 0);
        }
        intent.putExtra(SettingsJsonConstants.APP_KEY, this.zzavT);
    }

    synchronized void zztT() {
        if (this.zzavT != null) {
            this.zzavT.cancel();
            this.zzavT = null;
        }
    }

    @Deprecated
    Intent zzx(Bundle bundle) throws IOException {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            throw new IOException(ERROR_MAIN_THREAD);
        } else if (zzat(this.zzpH) < 0) {
            throw new IOException("Google Play Services missing");
        } else {
            if (bundle == null) {
                bundle = new Bundle();
            }
            Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
            intent.setPackage(zzas(this.zzpH));
            zzm(intent);
            intent.putExtra("google.message_id", zztS());
            intent.putExtras(bundle);
            intent.putExtra("google.messenger", this.zzavX);
            this.zzpH.startService(intent);
            try {
                return (Intent) this.zzavW.poll(Constants.BROADCASTS_LIVE_TIME, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new IOException(e.getMessage());
            }
        }
    }
}
