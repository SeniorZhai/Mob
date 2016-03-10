package com.google.android.gms.common.stats;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Debug;
import android.os.Parcelable;
import android.os.Process;
import android.os.SystemClock;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import com.google.android.gms.common.internal.zzd;
import com.google.android.gms.common.stats.zzc.zza;
import com.google.android.gms.internal.zzla;
import com.google.android.gms.internal.zzll;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class zzb {
    private static final Object zzaaJ = new Object();
    private static zzb zzack;
    private static final ComponentName zzacp = new ComponentName(AdvertisingInfoServiceStrategy.GOOGLE_PLAY_SERVICES_INTENT_PACKAGE_NAME, "com.google.android.gms.common.stats.GmsCoreStatsService");
    private final List<String> zzacl;
    private final List<String> zzacm;
    private final List<String> zzacn;
    private final List<String> zzaco;
    private zze zzacq;

    private zzb() {
        if (getLogLevel() == zzd.zzacz) {
            this.zzacl = Collections.EMPTY_LIST;
            this.zzacm = Collections.EMPTY_LIST;
            this.zzacn = Collections.EMPTY_LIST;
            this.zzaco = Collections.EMPTY_LIST;
            return;
        }
        String str = (String) zza.zzacu.get();
        this.zzacl = str == null ? Collections.EMPTY_LIST : Arrays.asList(str.split(","));
        str = (String) zza.zzacv.get();
        this.zzacm = str == null ? Collections.EMPTY_LIST : Arrays.asList(str.split(","));
        str = (String) zza.zzacw.get();
        this.zzacn = str == null ? Collections.EMPTY_LIST : Arrays.asList(str.split(","));
        str = (String) zza.zzacx.get();
        this.zzaco = str == null ? Collections.EMPTY_LIST : Arrays.asList(str.split(","));
        this.zzacq = new zze(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, ((Long) zza.zzacy.get()).longValue());
    }

    private int getLogLevel() {
        try {
            return zzla.zziW() ? ((Integer) zza.zzact.get()).intValue() : zzd.zzacz;
        } catch (SecurityException e) {
            return zzd.zzacz;
        }
    }

    private void zza(Context context, ServiceConnection serviceConnection, String str, Intent intent, int i) {
        if (zzd.zzZR) {
            String zzb = zzb(serviceConnection);
            if (zza(context, str, intent, zzb, i)) {
                Parcelable connectionEvent;
                long currentTimeMillis = System.currentTimeMillis();
                String str2 = null;
                if ((getLogLevel() & zzd.zzacD) != 0) {
                    str2 = zzll.zzl(3, 5);
                }
                long j = 0;
                if ((getLogLevel() & zzd.zzacF) != 0) {
                    j = Debug.getNativeHeapAllocatedSize();
                }
                if (i == 1 || i == 4) {
                    connectionEvent = new ConnectionEvent(currentTimeMillis, i, null, null, null, null, str2, zzb, SystemClock.elapsedRealtime(), j);
                } else {
                    ServiceInfo zzb2 = zzb(context, intent);
                    connectionEvent = new ConnectionEvent(currentTimeMillis, i, zzll.zzaj(context), str, zzb2.processName, zzb2.name, str2, zzb, SystemClock.elapsedRealtime(), j);
                }
                context.startService(new Intent().setComponent(zzacp).putExtra("com.google.android.gms.common.stats.EXTRA_LOG_EVENT", connectionEvent));
            }
        }
    }

    private boolean zza(Context context, Intent intent) {
        ComponentName component = intent.getComponent();
        return (component == null || (zzd.zzZR && AdvertisingInfoServiceStrategy.GOOGLE_PLAY_SERVICES_INTENT_PACKAGE_NAME.equals(component.getPackageName()))) ? false : zzla.zzi(context, component.getPackageName());
    }

    private boolean zza(Context context, String str, Intent intent, String str2, int i) {
        int logLevel = getLogLevel();
        if (logLevel == zzd.zzacz || this.zzacq == null) {
            return false;
        }
        if (i == 4 || i == 1) {
            return this.zzacq.zzcq(str2);
        } else {
            ServiceInfo zzb = zzb(context, intent);
            if (zzb == null) {
                Log.w("ConnectionTracker", String.format("Client %s made an invalid request %s", new Object[]{str, intent.toUri(0)}));
                return false;
            }
            String zzaj = zzll.zzaj(context);
            String str3 = zzb.processName;
            String str4 = zzb.name;
            if (this.zzacl.contains(zzaj) || this.zzacm.contains(str) || this.zzacn.contains(str3) || this.zzaco.contains(str4)) {
                return false;
            }
            if (str3.equals(zzaj) && (logLevel & zzd.zzacE) != 0) {
                return false;
            }
            this.zzacq.zzcp(str2);
            return true;
        }
    }

    private static ServiceInfo zzb(Context context, Intent intent) {
        List queryIntentServices = context.getPackageManager().queryIntentServices(intent, AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        if (queryIntentServices == null || queryIntentServices.size() == 0) {
            Log.w("ConnectionTracker", String.format("There are no handler of this intent: %s\n Stack trace: %s", new Object[]{intent.toUri(0), zzll.zzl(3, 20)}));
            return null;
        }
        if (queryIntentServices.size() > 1) {
            Log.w("ConnectionTracker", String.format("Multiple handlers found for this intent: %s\n Stack trace: %s", new Object[]{intent.toUri(0), zzll.zzl(3, 20)}));
            Iterator it = queryIntentServices.iterator();
            if (it.hasNext()) {
                Log.w("ConnectionTracker", ((ResolveInfo) it.next()).serviceInfo.name);
                return null;
            }
        }
        return ((ResolveInfo) queryIntentServices.get(0)).serviceInfo;
    }

    private String zzb(ServiceConnection serviceConnection) {
        return String.valueOf((Process.myPid() << 32) | System.identityHashCode(serviceConnection));
    }

    public static zzb zzoO() {
        synchronized (zzaaJ) {
            if (zzack == null) {
                zzack = new zzb();
            }
        }
        return zzack;
    }

    public void zza(Context context, ServiceConnection serviceConnection) {
        zza(context, serviceConnection, null, null, 1);
        context.unbindService(serviceConnection);
    }

    public void zza(Context context, ServiceConnection serviceConnection, String str, Intent intent) {
        zza(context, serviceConnection, str, intent, 3);
    }

    public boolean zza(Context context, Intent intent, ServiceConnection serviceConnection, int i) {
        return zza(context, context.getClass().getName(), intent, serviceConnection, i);
    }

    public boolean zza(Context context, String str, Intent intent, ServiceConnection serviceConnection, int i) {
        if (zza(context, intent)) {
            Log.w("ConnectionTracker", "Attempted to bind to a service in a STOPPED package.");
            return false;
        }
        zza(context, serviceConnection, str, intent, 2);
        return context.bindService(intent, serviceConnection, i);
    }

    public void zzb(Context context, ServiceConnection serviceConnection) {
        zza(context, serviceConnection, null, null, 4);
    }
}
