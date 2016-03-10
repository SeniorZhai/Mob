package com.google.android.gms.analytics.internal;

import android.util.Log;
import com.google.android.gms.analytics.Logger;

class zzs implements Logger {
    private boolean zzIz;
    private int zzKR = 2;

    zzs() {
    }

    public void error(Exception exception) {
    }

    public void error(String msg) {
    }

    public int getLogLevel() {
        return this.zzKR;
    }

    public void info(String msg) {
    }

    public void setLogLevel(int level) {
        this.zzKR = level;
        if (!this.zzIz) {
            Log.i((String) zzy.zzLb.get(), "Logger is deprecated. To enable debug logging, please run:\nadb shell setprop log.tag." + ((String) zzy.zzLb.get()) + " DEBUG");
            this.zzIz = true;
        }
    }

    public void verbose(String msg) {
    }

    public void warn(String msg) {
    }
}
