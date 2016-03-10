package com.helpshift.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import com.facebook.internal.AnalyticsEvents;
import com.mobcrush.mobcrush.Constants;

public final class HSPolling {
    public static final String TAG = "HelpShiftDebug";
    private Context c = null;
    private float interval;
    private float maxInterval = 60000.0f;
    private float minInterval;
    Runnable pollerRun = new Runnable() {
        public void run() {
            HSPolling.this.task.sendMessage(HSPolling.this.task.obtainMessage());
            HSPolling.this.task.postDelayed(HSPolling.this.pollerRun, (long) HSPolling.this.interval);
            if (HSPolling.this.smartPolling.booleanValue()) {
                HSPolling.this.updateInterval(HSPolling.this.interval);
            }
        }
    };
    private Boolean smartPolling = Boolean.valueOf(false);
    private Handler task;

    private static Float getBatteryLevel(Intent batteryStatus) {
        return Float.valueOf(((float) batteryStatus.getIntExtra("level", -1)) / ((float) batteryStatus.getIntExtra("scale", -1)));
    }

    private static String getNetworkType(Context c) {
        NetworkInfo ani = ((ConnectivityManager) c.getSystemService("connectivity")).getActiveNetworkInfo();
        String type = AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_UNKNOWN;
        if (ani != null) {
            return ani.getTypeName();
        }
        return type;
    }

    public HSPolling(Handler task, int interval) {
        this.task = task;
        this.minInterval = (float) (interval * Constants.UPDATE_COFIG_INTERVAL);
        this.interval = (float) (interval * Constants.UPDATE_COFIG_INTERVAL);
    }

    public HSPolling(Handler task, int interval, Boolean smartPolling, Context c) {
        this.task = task;
        this.interval = (float) (interval * Constants.UPDATE_COFIG_INTERVAL);
        this.minInterval = (float) (interval * Constants.UPDATE_COFIG_INTERVAL);
        this.smartPolling = smartPolling;
        this.c = c;
    }

    public void resetInterval() {
        this.interval = this.minInterval;
        this.task.removeCallbacks(this.pollerRun);
        this.task.postDelayed(this.pollerRun, (long) this.interval);
    }

    public void startRepeatingTask() {
        this.pollerRun.run();
    }

    public void stopRepeatingTask() {
        this.task.removeCallbacks(this.pollerRun);
    }

    public void updateInterval(float interval) {
        if (interval < this.maxInterval) {
            interval = ((this.minInterval + interval) * 1.618f) * (2.0f - getBatteryLevel(this.c.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))).floatValue());
            try {
                if (!getNetworkType(this.c).equals("WIFI")) {
                    interval *= 1.618f;
                }
            } catch (SecurityException e) {
                Log.d(TAG, "No permission for Network Access", e);
            }
            if (interval > this.maxInterval) {
                this.interval = this.maxInterval;
            } else {
                this.interval = interval;
            }
        }
    }
}
