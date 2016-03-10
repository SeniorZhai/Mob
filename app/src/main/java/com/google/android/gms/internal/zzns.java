package com.google.android.gms.internal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Process;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import com.google.android.gms.analytics.internal.zzam;
import com.google.android.gms.common.internal.zzu;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public final class zzns {
    private static volatile zzns zzaEh;
    private final Context mContext;
    private volatile zznx zzKm;
    private final List<zznt> zzaEi = new CopyOnWriteArrayList();
    private final zznn zzaEj = new zznn();
    private final zza zzaEk = new zza(this);
    private UncaughtExceptionHandler zzaEl;

    private class zza extends ThreadPoolExecutor {
        final /* synthetic */ zzns zzaEn;

        public zza(zzns com_google_android_gms_internal_zzns) {
            this.zzaEn = com_google_android_gms_internal_zzns;
            super(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
            setThreadFactory(new zzb());
        }

        protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
            return new FutureTask<T>(this, runnable, value) {
                final /* synthetic */ zza zzaEo;

                protected void setException(Throwable error) {
                    UncaughtExceptionHandler zzb = this.zzaEo.zzaEn.zzaEl;
                    if (zzb != null) {
                        zzb.uncaughtException(Thread.currentThread(), error);
                    } else if (Log.isLoggable("GAv4", 6)) {
                        Log.e("GAv4", "MeasurementExecutor: job failed with " + error);
                    }
                    super.setException(error);
                }
            };
        }
    }

    private static class zzb implements ThreadFactory {
        private static final AtomicInteger zzaEp = new AtomicInteger();

        private zzb() {
        }

        public Thread newThread(Runnable target) {
            return new zzc(target, "measurement-" + zzaEp.incrementAndGet());
        }
    }

    private static class zzc extends Thread {
        zzc(Runnable runnable, String str) {
            super(runnable, str);
        }

        public void run() {
            Process.setThreadPriority(10);
            super.run();
        }
    }

    zzns(Context context) {
        Context applicationContext = context.getApplicationContext();
        zzu.zzu(applicationContext);
        this.mContext = applicationContext;
    }

    public static zzns zzaB(Context context) {
        zzu.zzu(context);
        if (zzaEh == null) {
            synchronized (zzns.class) {
                if (zzaEh == null) {
                    zzaEh = new zzns(context);
                }
            }
        }
        return zzaEh;
    }

    private void zzb(zzno com_google_android_gms_internal_zzno) {
        zzu.zzbZ("deliver should be called from worker thread");
        zzu.zzb(com_google_android_gms_internal_zzno.zzvU(), (Object) "Measurement must be submitted");
        List<zznu> zzvR = com_google_android_gms_internal_zzno.zzvR();
        if (!zzvR.isEmpty()) {
            Set hashSet = new HashSet();
            for (zznu com_google_android_gms_internal_zznu : zzvR) {
                Uri zzhe = com_google_android_gms_internal_zznu.zzhe();
                if (!hashSet.contains(zzhe)) {
                    hashSet.add(zzhe);
                    com_google_android_gms_internal_zznu.zzb(com_google_android_gms_internal_zzno);
                }
            }
        }
    }

    public static void zzhO() {
        if (!(Thread.currentThread() instanceof zzc)) {
            throw new IllegalStateException("Call expected from worker thread");
        }
    }

    public Context getContext() {
        return this.mContext;
    }

    public void zza(UncaughtExceptionHandler uncaughtExceptionHandler) {
        this.zzaEl = uncaughtExceptionHandler;
    }

    public <V> Future<V> zzb(Callable<V> callable) {
        zzu.zzu(callable);
        if (!(Thread.currentThread() instanceof zzc)) {
            return this.zzaEk.submit(callable);
        }
        Future futureTask = new FutureTask(callable);
        futureTask.run();
        return futureTask;
    }

    void zze(zzno com_google_android_gms_internal_zzno) {
        if (com_google_android_gms_internal_zzno.zzvY()) {
            throw new IllegalStateException("Measurement prototype can't be submitted");
        } else if (com_google_android_gms_internal_zzno.zzvU()) {
            throw new IllegalStateException("Measurement can only be submitted once");
        } else {
            final zzno zzvP = com_google_android_gms_internal_zzno.zzvP();
            zzvP.zzvV();
            this.zzaEk.execute(new Runnable(this) {
                final /* synthetic */ zzns zzaEn;

                public void run() {
                    zzvP.zzvW().zza(zzvP);
                    for (zznt zza : this.zzaEn.zzaEi) {
                        zza.zza(zzvP);
                    }
                    this.zzaEn.zzb(zzvP);
                }
            });
        }
    }

    public void zze(Runnable runnable) {
        zzu.zzu(runnable);
        this.zzaEk.submit(runnable);
    }

    public zznx zzwc() {
        if (this.zzKm == null) {
            synchronized (this) {
                if (this.zzKm == null) {
                    zznx com_google_android_gms_internal_zznx = new zznx();
                    PackageManager packageManager = this.mContext.getPackageManager();
                    String packageName = this.mContext.getPackageName();
                    com_google_android_gms_internal_zznx.setAppId(packageName);
                    com_google_android_gms_internal_zznx.setAppInstallerId(packageManager.getInstallerPackageName(packageName));
                    String str = null;
                    try {
                        PackageInfo packageInfo = packageManager.getPackageInfo(this.mContext.getPackageName(), 0);
                        if (packageInfo != null) {
                            CharSequence applicationLabel = packageManager.getApplicationLabel(packageInfo.applicationInfo);
                            if (!TextUtils.isEmpty(applicationLabel)) {
                                packageName = applicationLabel.toString();
                            }
                            str = packageInfo.versionName;
                        }
                    } catch (NameNotFoundException e) {
                        Log.e("GAv4", "Error retrieving package info: appName set to " + packageName);
                    }
                    com_google_android_gms_internal_zznx.setAppName(packageName);
                    com_google_android_gms_internal_zznx.setAppVersion(str);
                    this.zzKm = com_google_android_gms_internal_zznx;
                }
            }
        }
        return this.zzKm;
    }

    public zznz zzwd() {
        DisplayMetrics displayMetrics = this.mContext.getResources().getDisplayMetrics();
        zznz com_google_android_gms_internal_zznz = new zznz();
        com_google_android_gms_internal_zznz.setLanguage(zzam.zza(Locale.getDefault()));
        com_google_android_gms_internal_zznz.zzhG(displayMetrics.widthPixels);
        com_google_android_gms_internal_zznz.zzhH(displayMetrics.heightPixels);
        return com_google_android_gms_internal_zznz;
    }
}
