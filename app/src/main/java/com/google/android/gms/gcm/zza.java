package com.google.android.gms.gcm;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

class zza {
    static zza zzavF;
    private Context mContext;

    private class zza extends IllegalArgumentException {
        final /* synthetic */ zza zzavG;

        private zza(zza com_google_android_gms_gcm_zza, String str) {
            this.zzavG = com_google_android_gms_gcm_zza;
            super(str);
        }
    }

    private zza(Context context) {
        this.mContext = context.getApplicationContext();
    }

    private void zza(String str, Notification notification) {
        if (Log.isLoggable("GcmNotification", 3)) {
            Log.d("GcmNotification", "Showing notification");
        }
        NotificationManager notificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        if (TextUtils.isEmpty(str)) {
            str = "GCM-Notification:" + SystemClock.uptimeMillis();
        }
        notificationManager.notify(str, 0, notification);
    }

    static synchronized zza zzar(Context context) {
        zza com_google_android_gms_gcm_zza;
        synchronized (zza.class) {
            if (zzavF == null) {
                zzavF = new zza(context);
            }
            com_google_android_gms_gcm_zza = zzavF;
        }
        return com_google_android_gms_gcm_zza;
    }

    static String zzb(Bundle bundle, String str) {
        String string = bundle.getString(str);
        return string == null ? bundle.getString(str.replace("gcm.n.", "gcm.notification.")) : string;
    }

    private int zzda(String str) {
        if (TextUtils.isEmpty(str)) {
            throw new zza("Missing icon");
        }
        Resources resources = this.mContext.getResources();
        int identifier = resources.getIdentifier(str, "drawable", this.mContext.getPackageName());
        if (identifier == 0) {
            identifier = resources.getIdentifier(str, "mipmap", this.mContext.getPackageName());
            if (identifier == 0) {
                throw new zza("Icon resource not found: " + str);
            }
        }
        return identifier;
    }

    private Uri zzdb(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        if ("default".equals(str)) {
            return RingtoneManager.getDefaultUri(2);
        }
        throw new zza("Invalid sound: " + str);
    }

    static boolean zzt(Bundle bundle) {
        return zzb(bundle, "gcm.n.title") != null;
    }

    private Notification zzv(Bundle bundle) {
        CharSequence zzb = zzb(bundle, "gcm.n.title");
        if (TextUtils.isEmpty(zzb)) {
            throw new zza("Missing title");
        }
        CharSequence zzb2 = zzb(bundle, "gcm.n.body");
        int zzda = zzda(zzb(bundle, "gcm.n.icon"));
        Uri zzdb = zzdb(zzb(bundle, "gcm.n.sound"));
        PendingIntent zzw = zzw(bundle);
        if (VERSION.SDK_INT >= 11) {
            Builder contentText = new Builder(this.mContext).setAutoCancel(true).setSmallIcon(zzda).setContentTitle(zzb).setContentText(zzb2);
            if (VERSION.SDK_INT >= 21) {
                Object zzb3 = zzb(bundle, "gcm.n.color");
                if (!TextUtils.isEmpty(zzb3)) {
                    contentText.setColor(Color.parseColor(zzb3));
                }
            }
            if (zzdb != null) {
                contentText.setSound(zzdb);
            }
            if (zzw != null) {
                contentText.setContentIntent(zzw);
            }
            return VERSION.SDK_INT >= 16 ? contentText.build() : contentText.getNotification();
        } else {
            Notification notification = new Notification();
            notification.flags |= 16;
            notification.icon = zzda;
            if (zzdb != null) {
                notification.sound = zzdb;
            }
            if (zzw == null) {
                Intent intent = new Intent();
                intent.setPackage("com.google.example.invalidpackage");
                zzw = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
            }
            notification.setLatestEventInfo(this.mContext, zzb, zzb2, zzw);
            return notification;
        }
    }

    private PendingIntent zzw(Bundle bundle) {
        Object zzb = zzb(bundle, "gcm.n.click_action");
        if (TextUtils.isEmpty(zzb)) {
            return null;
        }
        Intent intent = new Intent(zzb);
        intent.setPackage(this.mContext.getPackageName());
        intent.setFlags(268435456);
        intent.putExtras(bundle);
        for (String str : bundle.keySet()) {
            if (str.startsWith("gcm.n.") || str.startsWith("gcm.notification.")) {
                intent.removeExtra(str);
            }
        }
        return PendingIntent.getActivity(this.mContext, 0, intent, 1073741824);
    }

    boolean zzu(Bundle bundle) {
        try {
            zza(zzb(bundle, "gcm.n.tag"), zzv(bundle));
            return true;
        } catch (zza e) {
            Log.w("GcmNotification", "Failed to show notification: " + e.getMessage());
            return false;
        }
    }
}
