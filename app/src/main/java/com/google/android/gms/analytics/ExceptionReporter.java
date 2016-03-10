package com.google.android.gms.analytics;

import android.content.Context;
import com.google.android.gms.analytics.HitBuilders.ExceptionBuilder;
import com.google.android.gms.analytics.internal.zzae;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;

public class ExceptionReporter implements UncaughtExceptionHandler {
    private final Context mContext;
    private final UncaughtExceptionHandler zzIp;
    private final Tracker zzIq;
    private ExceptionParser zzIr;
    private GoogleAnalytics zzIs;

    public ExceptionReporter(Tracker tracker, UncaughtExceptionHandler originalHandler, Context context) {
        if (tracker == null) {
            throw new NullPointerException("tracker cannot be null");
        } else if (context == null) {
            throw new NullPointerException("context cannot be null");
        } else {
            this.zzIp = originalHandler;
            this.zzIq = tracker;
            this.zzIr = new StandardExceptionParser(context, new ArrayList());
            this.mContext = context.getApplicationContext();
            zzae.zzaB("ExceptionReporter created, original handler is " + (originalHandler == null ? "null" : originalHandler.getClass().getName()));
        }
    }

    public ExceptionParser getExceptionParser() {
        return this.zzIr;
    }

    public void setExceptionParser(ExceptionParser exceptionParser) {
        this.zzIr = exceptionParser;
    }

    public void uncaughtException(Thread t, Throwable e) {
        String str = "UncaughtException";
        if (this.zzIr != null) {
            str = this.zzIr.getDescription(t != null ? t.getName() : null, e);
        }
        zzae.zzaB("Reporting uncaught exception: " + str);
        this.zzIq.send(new ExceptionBuilder().setDescription(str).setFatal(true).build());
        GoogleAnalytics zzhg = zzhg();
        zzhg.dispatchLocalHits();
        zzhg.zzhk();
        if (this.zzIp != null) {
            zzae.zzaB("Passing exception to the original handler");
            this.zzIp.uncaughtException(t, e);
        }
    }

    GoogleAnalytics zzhg() {
        if (this.zzIs == null) {
            this.zzIs = GoogleAnalytics.getInstance(this.mContext);
        }
        return this.zzIs;
    }

    UncaughtExceptionHandler zzhh() {
        return this.zzIp;
    }
}
