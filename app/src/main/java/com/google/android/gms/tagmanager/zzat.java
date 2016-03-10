package com.google.android.gms.tagmanager;

import android.content.Context;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.LinkedBlockingQueue;

class zzat extends Thread implements zzas {
    private static zzat zzaLP;
    private volatile boolean mClosed = false;
    private final Context mContext;
    private volatile boolean zzKU = false;
    private final LinkedBlockingQueue<Runnable> zzaLO = new LinkedBlockingQueue();
    private volatile zzau zzaLQ;

    private zzat(Context context) {
        super("GAThread");
        if (context != null) {
            this.mContext = context.getApplicationContext();
        } else {
            this.mContext = context;
        }
        start();
    }

    static zzat zzaH(Context context) {
        if (zzaLP == null) {
            zzaLP = new zzat(context);
        }
        return zzaLP;
    }

    private String zzd(Throwable th) {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        th.printStackTrace(printStream);
        printStream.flush();
        return new String(byteArrayOutputStream.toByteArray());
    }

    public void run() {
        while (!this.mClosed) {
            try {
                Runnable runnable = (Runnable) this.zzaLO.take();
                if (!this.zzKU) {
                    runnable.run();
                }
            } catch (InterruptedException e) {
                zzbg.zzaA(e.toString());
            } catch (Throwable th) {
                zzbg.zzaz("Error on Google TagManager Thread: " + zzd(th));
                zzbg.zzaz("Google TagManager is shutting down.");
                this.zzKU = true;
            }
        }
    }

    public void zzew(String str) {
        zzh(str, System.currentTimeMillis());
    }

    public void zzf(Runnable runnable) {
        this.zzaLO.add(runnable);
    }

    void zzh(String str, long j) {
        final zzat com_google_android_gms_tagmanager_zzat = this;
        final long j2 = j;
        final String str2 = str;
        zzf(new Runnable(this) {
            final /* synthetic */ zzat zzaLT;

            public void run() {
                if (this.zzaLT.zzaLQ == null) {
                    zzcu zzzz = zzcu.zzzz();
                    zzzz.zza(this.zzaLT.mContext, com_google_android_gms_tagmanager_zzat);
                    this.zzaLT.zzaLQ = zzzz.zzzC();
                }
                this.zzaLT.zzaLQ.zzg(j2, str2);
            }
        });
    }
}
