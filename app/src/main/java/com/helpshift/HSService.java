package com.helpshift;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.helpshift.util.HSNotification;
import com.helpshift.util.HSPolling;
import java.util.List;

public final class HSService extends Service {
    public static final String TAG = "HelpShiftDebug";
    private static HSPolling notifCountPoller = null;
    private HSApiData data = null;
    private Boolean foreground;
    private Handler getCountHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (HSService.this.isAppOnForeground()) {
                if (!(HSService.this.foreground.booleanValue() || HSService.notifCountPoller == null)) {
                    HSService.notifCountPoller.resetInterval();
                }
                HSService.this.foreground = Boolean.valueOf(true);
                HSService.this.data.getNotificationData(HSNotification.getNotifHandler(HSService.this, HSService.notifCountPoller), new Handler());
                return;
            }
            HSService.this.foreground = Boolean.valueOf(false);
            HSService.this.stopSelf();
        }
    };
    private final IBinder hsBinder = new HSBinder();
    private HSLifecycleCallbacks hsLifecycleCallbacks = null;

    public class HSBinder extends Binder {
        HSService getService() {
            return HSService.this;
        }
    }

    private String getAppName() {
        ApplicationInfo ai;
        PackageManager pm = getPackageManager();
        try {
            ai = pm.getApplicationInfo(getPackageName(), 0);
        } catch (NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    private boolean isAppOnForeground() {
        if (VERSION.SDK_INT >= 14) {
            if (this.hsLifecycleCallbacks == null) {
                this.hsLifecycleCallbacks = HSLifecycleCallbacks.getInstance();
            }
            HSLifecycleCallbacks hSLifecycleCallbacks = this.hsLifecycleCallbacks;
            return HSLifecycleCallbacks.isForeground();
        }
        List<RunningAppProcessInfo> appProcesses = ((ActivityManager) getSystemService("activity")).getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        String packageName = getPackageName();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == 100 && appProcess.processName.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (this.data == null) {
            this.data = new HSApiData(this);
        }
        if (this.foreground == null) {
            this.foreground = Boolean.valueOf(true);
        }
        if (notifCountPoller == null) {
            notifCountPoller = new HSPolling(this.getCountHandler, 5, Boolean.valueOf(true), this);
            notifCountPoller.startRepeatingTask();
        }
        return 2;
    }

    public IBinder onBind(Intent intent) {
        return this.hsBinder;
    }

    public void onDestroy() {
        if (notifCountPoller != null) {
            notifCountPoller.stopRepeatingTask();
            notifCountPoller = null;
        }
        super.onDestroy();
    }

    public static void resetInterval() {
        if (notifCountPoller != null) {
            notifCountPoller.resetInterval();
        }
    }

    public static void stopPolling() {
        if (notifCountPoller != null) {
            notifCountPoller.stopRepeatingTask();
        }
    }
}
