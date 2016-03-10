package com.google.android.gms.analytics.internal;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Process;
import android.text.TextUtils;
import com.google.android.gms.common.internal.zzd;
import com.google.android.gms.common.internal.zzu;
import com.mobcrush.mobcrush.ChatFragment;
import java.util.HashSet;
import java.util.Set;

public class zzr {
    private final zzf zzIa;
    private Boolean zzKO;
    private String zzKP;
    private Set<Integer> zzKQ;

    protected zzr(zzf com_google_android_gms_analytics_internal_zzf) {
        zzu.zzu(com_google_android_gms_analytics_internal_zzf);
        this.zzIa = com_google_android_gms_analytics_internal_zzf;
    }

    public boolean zziW() {
        return zzd.zzZR;
    }

    public boolean zziX() {
        if (this.zzKO == null) {
            synchronized (this) {
                if (this.zzKO == null) {
                    Context context = this.zzIa.getContext();
                    ApplicationInfo applicationInfo = context.getApplicationInfo();
                    if (applicationInfo != null) {
                        String str = applicationInfo.processName;
                        ActivityManager activityManager = (ActivityManager) context.getSystemService("activity");
                        if (activityManager != null) {
                            int myPid = Process.myPid();
                            for (RunningAppProcessInfo runningAppProcessInfo : activityManager.getRunningAppProcesses()) {
                                if (myPid == runningAppProcessInfo.pid) {
                                    boolean z = str != null && str.equals(runningAppProcessInfo.processName);
                                    this.zzKO = Boolean.valueOf(z);
                                }
                            }
                        }
                    }
                    if (this.zzKO == null) {
                        this.zzKO = Boolean.TRUE;
                        this.zzIa.zzhQ().zzaX("My process not in the list of running processes");
                    }
                }
            }
        }
        return this.zzKO.booleanValue();
    }

    public boolean zziY() {
        return ((Boolean) zzy.zzLa.get()).booleanValue();
    }

    public int zziZ() {
        return ((Integer) zzy.zzLt.get()).intValue();
    }

    public int zzjA() {
        return ((Integer) zzy.zzLE.get()).intValue();
    }

    public long zzjB() {
        return ((Long) zzy.zzLF.get()).longValue();
    }

    public long zzjC() {
        return ((Long) zzy.zzLO.get()).longValue();
    }

    public int zzja() {
        return ((Integer) zzy.zzLx.get()).intValue();
    }

    public int zzjb() {
        return ((Integer) zzy.zzLy.get()).intValue();
    }

    public int zzjc() {
        return ((Integer) zzy.zzLz.get()).intValue();
    }

    public long zzjd() {
        return ((Long) zzy.zzLi.get()).longValue();
    }

    public long zzje() {
        return ((Long) zzy.zzLh.get()).longValue();
    }

    public long zzjf() {
        return ((Long) zzy.zzLl.get()).longValue();
    }

    public long zzjg() {
        return ((Long) zzy.zzLm.get()).longValue();
    }

    public int zzjh() {
        return ((Integer) zzy.zzLn.get()).intValue();
    }

    public int zzji() {
        return ((Integer) zzy.zzLo.get()).intValue();
    }

    public long zzjj() {
        return (long) ((Integer) zzy.zzLB.get()).intValue();
    }

    public String zzjk() {
        return (String) zzy.zzLq.get();
    }

    public String zzjl() {
        return (String) zzy.zzLp.get();
    }

    public String zzjm() {
        return (String) zzy.zzLr.get();
    }

    public String zzjn() {
        return (String) zzy.zzLs.get();
    }

    public zzm zzjo() {
        return zzm.zzbc((String) zzy.zzLu.get());
    }

    public zzo zzjp() {
        return zzo.zzbd((String) zzy.zzLv.get());
    }

    public Set<Integer> zzjq() {
        String str = (String) zzy.zzLA.get();
        if (this.zzKQ == null || this.zzKP == null || !this.zzKP.equals(str)) {
            String[] split = TextUtils.split(str, ",");
            Set hashSet = new HashSet();
            for (String parseInt : split) {
                try {
                    hashSet.add(Integer.valueOf(Integer.parseInt(parseInt)));
                } catch (NumberFormatException e) {
                }
            }
            this.zzKP = str;
            this.zzKQ = hashSet;
        }
        return this.zzKQ;
    }

    public long zzjr() {
        return ((Long) zzy.zzLJ.get()).longValue();
    }

    public long zzjs() {
        return ((Long) zzy.zzLK.get()).longValue();
    }

    public long zzjt() {
        return ((Long) zzy.zzLN.get()).longValue();
    }

    public int zzju() {
        return ((Integer) zzy.zzLe.get()).intValue();
    }

    public int zzjv() {
        return ((Integer) zzy.zzLg.get()).intValue();
    }

    public String zzjw() {
        return "google_analytics_v4.db";
    }

    public String zzjx() {
        return "google_analytics2_v4.db";
    }

    public long zzjy() {
        return ChatFragment.MID_MUTE_TIME;
    }

    public int zzjz() {
        return ((Integer) zzy.zzLD.get()).intValue();
    }
}
