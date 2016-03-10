package com.google.android.gms.iid;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.helpshift.res.values.HSConsts;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.http.HttpHeaders;
import org.apache.http.protocol.HTTP;

public class zzc {
    static String zzaxd = null;
    static int zzaxe = 0;
    static int zzaxf = 0;
    static int zzaxg = 0;
    PendingIntent zzavT;
    Messenger zzavX;
    Map<String, Object> zzaxh = new HashMap();
    Messenger zzaxi;
    MessengerCompat zzaxj;
    long zzaxk;
    long zzaxl;
    int zzaxm;
    int zzaxn;
    long zzaxo;
    Context zzpH;

    public zzc(Context context) {
        this.zzpH = context;
    }

    static String zza(KeyPair keyPair, String... strArr) {
        String str = null;
        try {
            byte[] bytes = TextUtils.join("\n", strArr).getBytes(HTTP.UTF_8);
            try {
                PrivateKey privateKey = keyPair.getPrivate();
                Signature instance = Signature.getInstance(privateKey instanceof RSAPrivateKey ? "SHA256withRSA" : "SHA256withECDSA");
                instance.initSign(privateKey);
                instance.update(bytes);
                str = InstanceID.zzm(instance.sign());
            } catch (Throwable e) {
                Log.e("InstanceID/Rpc", "Unable to sign registration request", e);
            }
        } catch (Throwable e2) {
            Log.e("InstanceID/Rpc", "Unable to encode string", e2);
        }
        return str;
    }

    public static String zzaw(Context context) {
        ApplicationInfo applicationInfo;
        if (zzaxd != null) {
            return zzaxd;
        }
        zzaxe = Process.myUid();
        PackageManager packageManager = context.getPackageManager();
        for (ResolveInfo resolveInfo : packageManager.queryIntentServices(new Intent("com.google.android.c2dm.intent.REGISTER"), 0)) {
            if (packageManager.checkPermission("com.google.android.c2dm.permission.RECEIVE", resolveInfo.serviceInfo.packageName) == 0) {
                try {
                    ApplicationInfo applicationInfo2 = packageManager.getApplicationInfo(resolveInfo.serviceInfo.packageName, 0);
                    Log.w("InstanceID/Rpc", "Found " + applicationInfo2.uid);
                    zzaxf = applicationInfo2.uid;
                    zzaxd = resolveInfo.serviceInfo.packageName;
                    return zzaxd;
                } catch (NameNotFoundException e) {
                }
            } else {
                Log.w("InstanceID/Rpc", "Possible malicious package " + resolveInfo.serviceInfo.packageName + " declares " + "com.google.android.c2dm.intent.REGISTER" + " without permission");
            }
        }
        Log.w("InstanceID/Rpc", "Failed to resolve REGISTER intent, falling back");
        try {
            applicationInfo = packageManager.getApplicationInfo(AdvertisingInfoServiceStrategy.GOOGLE_PLAY_SERVICES_INTENT_PACKAGE_NAME, 0);
            zzaxd = applicationInfo.packageName;
            zzaxf = applicationInfo.uid;
            return zzaxd;
        } catch (NameNotFoundException e2) {
            try {
                applicationInfo = packageManager.getApplicationInfo("com.google.android.gsf", 0);
                zzaxd = applicationInfo.packageName;
                zzaxf = applicationInfo.uid;
                return zzaxd;
            } catch (NameNotFoundException e3) {
                Log.w("InstanceID/Rpc", "Both Google Play Services and legacy GSF package are missing");
                return null;
            }
        }
    }

    private Intent zzb(Bundle bundle, KeyPair keyPair) throws IOException {
        Intent intent;
        ConditionVariable conditionVariable = new ConditionVariable();
        String zzuk = zzuk();
        synchronized (getClass()) {
            this.zzaxh.put(zzuk, conditionVariable);
        }
        zza(bundle, keyPair, zzuk);
        conditionVariable.block(Constants.BROADCASTS_LIVE_TIME);
        synchronized (getClass()) {
            Object remove = this.zzaxh.remove(zzuk);
            if (remove instanceof Intent) {
                intent = (Intent) remove;
            } else if (remove instanceof String) {
                throw new IOException((String) remove);
            } else {
                Log.w("InstanceID/Rpc", "No response " + remove);
                throw new IOException(InstanceID.ERROR_TIMEOUT);
            }
        }
        return intent;
    }

    private void zzdd(String str) {
        if ("com.google.android.gsf".equals(zzaxd)) {
            this.zzaxm++;
            if (this.zzaxm >= 3) {
                if (this.zzaxm == 3) {
                    this.zzaxn = new Random().nextInt(Constants.UPDATE_COFIG_INTERVAL) + Constants.UPDATE_COFIG_INTERVAL;
                }
                this.zzaxn *= 2;
                this.zzaxo = SystemClock.elapsedRealtime() + ((long) this.zzaxn);
                Log.w("InstanceID/Rpc", "Backoff due to " + str + " for " + this.zzaxn);
            }
        }
    }

    private void zze(Object obj, Object obj2) {
        if (obj instanceof ConditionVariable) {
            ((ConditionVariable) obj).open();
        }
        if (obj instanceof Messenger) {
            Messenger messenger = (Messenger) obj;
            Message obtain = Message.obtain();
            obtain.obj = obj2;
            try {
                messenger.send(obtain);
            } catch (RemoteException e) {
                Log.w("InstanceID/Rpc", "Failed to send response " + e);
            }
        }
    }

    private void zzi(String str, Object obj) {
        synchronized (getClass()) {
            Object obj2 = this.zzaxh.get(str);
            this.zzaxh.put(str, obj);
            zze(obj2, obj);
        }
    }

    public static synchronized String zzuk() {
        String num;
        synchronized (zzc.class) {
            int i = zzaxg;
            zzaxg = i + 1;
            num = Integer.toString(i);
        }
        return num;
    }

    private void zzx(Object obj) {
        synchronized (getClass()) {
            for (String str : this.zzaxh.keySet()) {
                Object obj2 = this.zzaxh.get(str);
                this.zzaxh.put(str, obj);
                zze(obj2, obj);
            }
        }
    }

    Intent zza(Bundle bundle, KeyPair keyPair) throws IOException {
        Intent zzb = zzb(bundle, keyPair);
        return (zzb == null || !zzb.hasExtra("google.messenger")) ? zzb : zzb(bundle, keyPair);
    }

    void zza(Bundle bundle, KeyPair keyPair, String str) throws IOException {
        long elapsedRealtime = SystemClock.elapsedRealtime();
        if (this.zzaxo == 0 || elapsedRealtime > this.zzaxo) {
            zzuj();
            if (zzaxd == null) {
                throw new IOException(InstanceID.ERROR_MISSING_INSTANCEID_SERVICE);
            }
            this.zzaxk = SystemClock.elapsedRealtime();
            Intent intent = new Intent("com.google.android.c2dm.intent.REGISTER");
            intent.setPackage(zzaxd);
            bundle.putString("gmsv", Integer.toString(GoogleCloudMessaging.zzat(this.zzpH)));
            bundle.putString("osv", Integer.toString(VERSION.SDK_INT));
            bundle.putString("app_ver", Integer.toString(InstanceID.zzau(this.zzpH)));
            bundle.putString("cliv", HSConsts.STATUS_INPROGRESS);
            bundle.putString("appid", InstanceID.zza(keyPair));
            bundle.putString("pub2", InstanceID.zzm(keyPair.getPublic().getEncoded()));
            bundle.putString("sig", zza(keyPair, this.zzpH.getPackageName(), r1));
            intent.putExtras(bundle);
            zzo(intent);
            zzb(intent, str);
            return;
        }
        Log.w("InstanceID/Rpc", "Backoff mode, next request attempt: " + (this.zzaxo - elapsedRealtime) + " interval: " + this.zzaxn);
        throw new IOException(InstanceID.ERROR_BACKOFF);
    }

    protected void zzb(Intent intent, String str) {
        this.zzaxk = SystemClock.elapsedRealtime();
        intent.putExtra("kid", "|ID|" + str + "|");
        intent.putExtra("X-kid", "|ID|" + str + "|");
        boolean equals = "com.google.android.gsf".equals(zzaxd);
        String stringExtra = intent.getStringExtra("useGsf");
        if (stringExtra != null) {
            equals = HSConsts.STATUS_INPROGRESS.equals(stringExtra);
        }
        if (Log.isLoggable("InstanceID/Rpc", 3)) {
            Log.d("InstanceID/Rpc", "Sending " + intent.getExtras());
        }
        if (this.zzaxi != null) {
            intent.putExtra("google.messenger", this.zzavX);
            Message obtain = Message.obtain();
            obtain.obj = intent;
            try {
                this.zzaxi.send(obtain);
                return;
            } catch (RemoteException e) {
                if (Log.isLoggable("InstanceID/Rpc", 3)) {
                    Log.d("InstanceID/Rpc", "Messenger failed, fallback to startService");
                }
            }
        }
        if (equals) {
            Intent intent2 = new Intent("com.google.android.gms.iid.InstanceID");
            intent2.setPackage(this.zzpH.getPackageName());
            intent2.putExtra("GSF", intent);
            this.zzpH.startService(intent2);
            return;
        }
        intent.putExtra("google.messenger", this.zzavX);
        intent.putExtra("messenger2", HSConsts.STATUS_INPROGRESS);
        if (this.zzaxj != null) {
            Message obtain2 = Message.obtain();
            obtain2.obj = intent;
            try {
                this.zzaxj.send(obtain2);
                return;
            } catch (RemoteException e2) {
                if (Log.isLoggable("InstanceID/Rpc", 3)) {
                    Log.d("InstanceID/Rpc", "Messenger failed, fallback to startService");
                }
            }
        }
        this.zzpH.startService(intent);
    }

    public void zze(Message message) {
        if (message != null) {
            if (message.obj instanceof Intent) {
                Intent intent = (Intent) message.obj;
                intent.setExtrasClassLoader(MessengerCompat.class.getClassLoader());
                if (intent.hasExtra("google.messenger")) {
                    Parcelable parcelableExtra = intent.getParcelableExtra("google.messenger");
                    if (parcelableExtra instanceof MessengerCompat) {
                        this.zzaxj = (MessengerCompat) parcelableExtra;
                    }
                    if (parcelableExtra instanceof Messenger) {
                        this.zzaxi = (Messenger) parcelableExtra;
                    }
                }
                zzr((Intent) message.obj);
                return;
            }
            Log.w("InstanceID/Rpc", "Dropping invalid message");
        }
    }

    synchronized void zzo(Intent intent) {
        if (this.zzavT == null) {
            Intent intent2 = new Intent();
            intent2.setPackage("com.google.example.invalidpackage");
            this.zzavT = PendingIntent.getBroadcast(this.zzpH, 0, intent2, 0);
        }
        intent.putExtra(SettingsJsonConstants.APP_KEY, this.zzavT);
    }

    String zzp(Intent intent) throws IOException {
        if (intent == null) {
            throw new IOException(InstanceID.ERROR_SERVICE_NOT_AVAILABLE);
        }
        String stringExtra = intent.getStringExtra(PreferenceUtility.REG_ID);
        if (stringExtra == null) {
            stringExtra = intent.getStringExtra("unregistered");
        }
        intent.getLongExtra(HttpHeaders.RETRY_AFTER, 0);
        if (stringExtra != null) {
            if (stringExtra == null) {
                return stringExtra;
            }
            stringExtra = intent.getStringExtra(Extra.ERROR);
            if (stringExtra == null) {
                throw new IOException(stringExtra);
            }
            Log.w("InstanceID/Rpc", "Unexpected response from GCM " + intent.getExtras(), new Throwable());
            throw new IOException(InstanceID.ERROR_SERVICE_NOT_AVAILABLE);
        } else if (stringExtra == null) {
            return stringExtra;
        } else {
            stringExtra = intent.getStringExtra(Extra.ERROR);
            if (stringExtra == null) {
                Log.w("InstanceID/Rpc", "Unexpected response from GCM " + intent.getExtras(), new Throwable());
                throw new IOException(InstanceID.ERROR_SERVICE_NOT_AVAILABLE);
            }
            throw new IOException(stringExtra);
        }
    }

    void zzq(Intent intent) {
        String stringExtra = intent.getStringExtra(Extra.ERROR);
        if (stringExtra == null) {
            Log.w("InstanceID/Rpc", "Unexpected response, no error or registration id " + intent.getExtras());
            return;
        }
        if (Log.isLoggable("InstanceID/Rpc", 3)) {
            Log.d("InstanceID/Rpc", "Received InstanceID error " + stringExtra);
        }
        String str = null;
        if (stringExtra.startsWith("|")) {
            String[] split = stringExtra.split("\\|");
            if (!"ID".equals(split[1])) {
                Log.w("InstanceID/Rpc", "Unexpected structured response " + stringExtra);
            }
            if (split.length > 2) {
                str = split[2];
                stringExtra = split[3];
                if (stringExtra.startsWith(":")) {
                    stringExtra = stringExtra.substring(1);
                }
            } else {
                stringExtra = "UNKNOWN";
            }
            intent.putExtra(Extra.ERROR, stringExtra);
        }
        if (str == null) {
            zzx(stringExtra);
        } else {
            zzi(str, stringExtra);
        }
        long longExtra = intent.getLongExtra(HttpHeaders.RETRY_AFTER, 0);
        if (longExtra > 0) {
            this.zzaxl = SystemClock.elapsedRealtime();
            this.zzaxn = ((int) longExtra) * Constants.UPDATE_COFIG_INTERVAL;
            this.zzaxo = SystemClock.elapsedRealtime() + ((long) this.zzaxn);
            Log.w("InstanceID/Rpc", "Explicit request from server to backoff: " + this.zzaxn);
        } else if (InstanceID.ERROR_SERVICE_NOT_AVAILABLE.equals(stringExtra) || "AUTHENTICATION_FAILED".equals(stringExtra)) {
            zzdd(stringExtra);
        }
    }

    void zzr(Intent intent) {
        if (intent != null) {
            String action = intent.getAction();
            if ("com.google.android.c2dm.intent.REGISTRATION".equals(action) || "com.google.android.gms.iid.InstanceID".equals(action)) {
                action = intent.getStringExtra(PreferenceUtility.REG_ID);
                String stringExtra = action == null ? intent.getStringExtra("unregistered") : action;
                if (stringExtra == null) {
                    zzq(intent);
                    return;
                }
                this.zzaxk = SystemClock.elapsedRealtime();
                this.zzaxo = 0;
                this.zzaxm = 0;
                this.zzaxn = 0;
                if (Log.isLoggable("InstanceID/Rpc", 3)) {
                    Log.d("InstanceID/Rpc", "AppIDResponse: " + stringExtra + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + intent.getExtras());
                }
                action = null;
                if (stringExtra.startsWith("|")) {
                    String[] split = stringExtra.split("\\|");
                    if (!"ID".equals(split[1])) {
                        Log.w("InstanceID/Rpc", "Unexpected structured response " + stringExtra);
                    }
                    stringExtra = split[2];
                    if (split.length > 4) {
                        if ("SYNC".equals(split[3])) {
                            InstanceIDListenerService.zzav(this.zzpH);
                        } else if ("RST".equals(split[3])) {
                            InstanceIDListenerService.zza(this.zzpH, InstanceID.getInstance(this.zzpH).zzug());
                            intent.removeExtra(PreferenceUtility.REG_ID);
                            zzi(stringExtra, intent);
                            return;
                        }
                    }
                    action = split[split.length - 1];
                    if (action.startsWith(":")) {
                        action = action.substring(1);
                    }
                    intent.putExtra(PreferenceUtility.REG_ID, action);
                    action = stringExtra;
                }
                if (action == null) {
                    zzx(intent);
                } else {
                    zzi(action, intent);
                }
            } else if (Log.isLoggable("InstanceID/Rpc", 3)) {
                Log.d("InstanceID/Rpc", "Unexpected response " + intent.getAction());
            }
        } else if (Log.isLoggable("InstanceID/Rpc", 3)) {
            Log.d("InstanceID/Rpc", "Unexpected response: null");
        }
    }

    void zzuj() {
        if (this.zzavX == null) {
            zzaw(this.zzpH);
            this.zzavX = new Messenger(new Handler(this, Looper.getMainLooper()) {
                final /* synthetic */ zzc zzaxp;

                public void handleMessage(Message msg) {
                    this.zzaxp.zze(msg);
                }
            });
        }
    }
}
