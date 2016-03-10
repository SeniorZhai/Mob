package com.google.android.gms.tagmanager;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.PowerManager;
import android.os.Process;
import android.text.TextUtils;
import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.helpshift.res.values.HSConsts;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class zzdb extends zzak {
    private static final String ID = zzad.TIMER_LISTENER.toString();
    private static final String NAME = zzae.NAME.toString();
    private static final String zzaOe = zzae.INTERVAL.toString();
    private static final String zzaOf = zzae.LIMIT.toString();
    private static final String zzaOg = zzae.UNIQUE_TRIGGER_ID.toString();
    private final Context mContext;
    private Handler mHandler;
    private DataLayer zzaKz;
    private boolean zzaOh;
    private boolean zzaOi;
    private final HandlerThread zzaOj;
    private final Set<String> zzaOk = new HashSet();

    private final class zza implements Runnable {
        private final long zzMC = System.currentTimeMillis();
        private final String zzaOl;
        private final String zzaOm;
        private final long zzaOn;
        private long zzaOo;
        final /* synthetic */ zzdb zzaOp;
        private final long zzaxU;

        zza(zzdb com_google_android_gms_tagmanager_zzdb, String str, String str2, long j, long j2) {
            this.zzaOp = com_google_android_gms_tagmanager_zzdb;
            this.zzaOl = str;
            this.zzaOm = str2;
            this.zzaxU = j;
            this.zzaOn = j2;
        }

        public void run() {
            if (this.zzaOn <= 0 || this.zzaOo < this.zzaOn) {
                this.zzaOo++;
                if (zzcq()) {
                    long currentTimeMillis = System.currentTimeMillis();
                    this.zzaOp.zzaKz.push(DataLayer.mapOf(DataLayer.EVENT_KEY, this.zzaOl, "gtm.timerInterval", String.valueOf(this.zzaxU), "gtm.timerLimit", String.valueOf(this.zzaOn), "gtm.timerStartTime", String.valueOf(this.zzMC), "gtm.timerCurrentTime", String.valueOf(currentTimeMillis), "gtm.timerElapsedTime", String.valueOf(currentTimeMillis - this.zzMC), "gtm.timerEventNumber", String.valueOf(this.zzaOo), "gtm.triggers", this.zzaOm));
                }
                this.zzaOp.mHandler.postDelayed(this, this.zzaxU);
            } else if (!HSConsts.STATUS_NEW.equals(this.zzaOm)) {
                this.zzaOp.zzaOk.remove(this.zzaOm);
            }
        }

        protected boolean zzcq() {
            if (this.zzaOp.zzaOi) {
                return this.zzaOp.zzaOh;
            }
            KeyguardManager keyguardManager = (KeyguardManager) this.zzaOp.mContext.getSystemService("keyguard");
            PowerManager powerManager = (PowerManager) this.zzaOp.mContext.getSystemService("power");
            for (RunningAppProcessInfo runningAppProcessInfo : ((ActivityManager) this.zzaOp.mContext.getSystemService("activity")).getRunningAppProcesses()) {
                if (Process.myPid() == runningAppProcessInfo.pid && runningAppProcessInfo.importance == 100 && !keyguardManager.inKeyguardRestrictedInputMode() && powerManager.isScreenOn()) {
                    return true;
                }
            }
            return false;
        }
    }

    public zzdb(Context context, DataLayer dataLayer) {
        super(ID, zzaOe, NAME);
        this.mContext = context;
        this.zzaKz = dataLayer;
        this.zzaOj = new HandlerThread("Google GTM SDK Timer", 10);
        this.zzaOj.start();
        this.mHandler = new Handler(this.zzaOj.getLooper());
    }

    public com.google.android.gms.internal.zzag.zza zzE(Map<String, com.google.android.gms.internal.zzag.zza> map) {
        long parseLong;
        long parseLong2;
        Object zzg = zzdf.zzg((com.google.android.gms.internal.zzag.zza) map.get(NAME));
        String zzg2 = zzdf.zzg((com.google.android.gms.internal.zzag.zza) map.get(zzaOg));
        String zzg3 = zzdf.zzg((com.google.android.gms.internal.zzag.zza) map.get(zzaOe));
        String zzg4 = zzdf.zzg((com.google.android.gms.internal.zzag.zza) map.get(zzaOf));
        try {
            parseLong = Long.parseLong(zzg3);
        } catch (NumberFormatException e) {
            parseLong = 0;
        }
        try {
            parseLong2 = Long.parseLong(zzg4);
        } catch (NumberFormatException e2) {
            parseLong2 = 0;
        }
        if (parseLong > 0 && !TextUtils.isEmpty(zzg)) {
            if (zzg2 == null || zzg2.isEmpty()) {
                zzg2 = HSConsts.STATUS_NEW;
            }
            if (!this.zzaOk.contains(zzg2)) {
                if (!HSConsts.STATUS_NEW.equals(zzg2)) {
                    this.zzaOk.add(zzg2);
                }
                this.mHandler.postDelayed(new zza(this, zzg, zzg2, parseLong, parseLong2), parseLong);
            }
        }
        return zzdf.zzzQ();
    }

    public boolean zzyh() {
        return false;
    }
}
