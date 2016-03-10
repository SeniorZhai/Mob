package com.google.android.gms.tagmanager;

import android.content.Context;
import com.google.android.gms.internal.zzaf.zzf;
import com.google.android.gms.internal.zzaf.zzi;
import com.google.android.gms.internal.zzaf.zzj;
import com.google.android.gms.internal.zzqf;
import com.google.android.gms.internal.zzqf.zzc;
import com.google.android.gms.internal.zzqf.zzg;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Container {
    private final Context mContext;
    private zzcp zzaKA;
    private Map<String, FunctionCallMacroCallback> zzaKB = new HashMap();
    private Map<String, FunctionCallTagCallback> zzaKC = new HashMap();
    private volatile long zzaKD;
    private volatile String zzaKE = BuildConfig.FLAVOR;
    private final String zzaKy;
    private final DataLayer zzaKz;

    public interface FunctionCallMacroCallback {
        Object getValue(String str, Map<String, Object> map);
    }

    public interface FunctionCallTagCallback {
        void execute(String str, Map<String, Object> map);
    }

    private class zza implements com.google.android.gms.tagmanager.zzt.zza {
        final /* synthetic */ Container zzaKF;

        private zza(Container container) {
            this.zzaKF = container;
        }

        public Object zzd(String str, Map<String, Object> map) {
            FunctionCallMacroCallback zzef = this.zzaKF.zzef(str);
            return zzef == null ? null : zzef.getValue(str, map);
        }
    }

    private class zzb implements com.google.android.gms.tagmanager.zzt.zza {
        final /* synthetic */ Container zzaKF;

        private zzb(Container container) {
            this.zzaKF = container;
        }

        public Object zzd(String str, Map<String, Object> map) {
            FunctionCallTagCallback zzeg = this.zzaKF.zzeg(str);
            if (zzeg != null) {
                zzeg.execute(str, map);
            }
            return zzdf.zzzP();
        }
    }

    Container(Context context, DataLayer dataLayer, String containerId, long lastRefreshTime, zzj resource) {
        this.mContext = context;
        this.zzaKz = dataLayer;
        this.zzaKy = containerId;
        this.zzaKD = lastRefreshTime;
        zza(resource.zziO);
        if (resource.zziN != null) {
            zza(resource.zziN);
        }
    }

    Container(Context context, DataLayer dataLayer, String containerId, long lastRefreshTime, zzc resource) {
        this.mContext = context;
        this.zzaKz = dataLayer;
        this.zzaKy = containerId;
        this.zzaKD = lastRefreshTime;
        zza(resource);
    }

    private void zza(zzf com_google_android_gms_internal_zzaf_zzf) {
        if (com_google_android_gms_internal_zzaf_zzf == null) {
            throw new NullPointerException();
        }
        try {
            zza(zzqf.zzb(com_google_android_gms_internal_zzaf_zzf));
        } catch (zzg e) {
            zzbg.zzaz("Not loading resource: " + com_google_android_gms_internal_zzaf_zzf + " because it is invalid: " + e.toString());
        }
    }

    private void zza(zzc com_google_android_gms_internal_zzqf_zzc) {
        this.zzaKE = com_google_android_gms_internal_zzqf_zzc.getVersion();
        zzc com_google_android_gms_internal_zzqf_zzc2 = com_google_android_gms_internal_zzqf_zzc;
        zza(new zzcp(this.mContext, com_google_android_gms_internal_zzqf_zzc2, this.zzaKz, new zza(), new zzb(), zzei(this.zzaKE)));
        if (getBoolean("_gtm.loadEventEnabled")) {
            this.zzaKz.pushEvent("gtm.load", DataLayer.mapOf("gtm.id", this.zzaKy));
        }
    }

    private synchronized void zza(zzcp com_google_android_gms_tagmanager_zzcp) {
        this.zzaKA = com_google_android_gms_tagmanager_zzcp;
    }

    private void zza(zzi[] com_google_android_gms_internal_zzaf_zziArr) {
        List arrayList = new ArrayList();
        for (Object add : com_google_android_gms_internal_zzaf_zziArr) {
            arrayList.add(add);
        }
        zzyn().zzs(arrayList);
    }

    private synchronized zzcp zzyn() {
        return this.zzaKA;
    }

    public boolean getBoolean(String key) {
        zzcp zzyn = zzyn();
        if (zzyn == null) {
            zzbg.zzaz("getBoolean called for closed container.");
            return zzdf.zzzN().booleanValue();
        }
        try {
            return zzdf.zzk((com.google.android.gms.internal.zzag.zza) zzyn.zzeD(key).getObject()).booleanValue();
        } catch (Exception e) {
            zzbg.zzaz("Calling getBoolean() threw an exception: " + e.getMessage() + " Returning default value.");
            return zzdf.zzzN().booleanValue();
        }
    }

    public String getContainerId() {
        return this.zzaKy;
    }

    public double getDouble(String key) {
        zzcp zzyn = zzyn();
        if (zzyn == null) {
            zzbg.zzaz("getDouble called for closed container.");
            return zzdf.zzzM().doubleValue();
        }
        try {
            return zzdf.zzj((com.google.android.gms.internal.zzag.zza) zzyn.zzeD(key).getObject()).doubleValue();
        } catch (Exception e) {
            zzbg.zzaz("Calling getDouble() threw an exception: " + e.getMessage() + " Returning default value.");
            return zzdf.zzzM().doubleValue();
        }
    }

    public long getLastRefreshTime() {
        return this.zzaKD;
    }

    public long getLong(String key) {
        zzcp zzyn = zzyn();
        if (zzyn == null) {
            zzbg.zzaz("getLong called for closed container.");
            return zzdf.zzzL().longValue();
        }
        try {
            return zzdf.zzi((com.google.android.gms.internal.zzag.zza) zzyn.zzeD(key).getObject()).longValue();
        } catch (Exception e) {
            zzbg.zzaz("Calling getLong() threw an exception: " + e.getMessage() + " Returning default value.");
            return zzdf.zzzL().longValue();
        }
    }

    public String getString(String key) {
        zzcp zzyn = zzyn();
        if (zzyn == null) {
            zzbg.zzaz("getString called for closed container.");
            return zzdf.zzzP();
        }
        try {
            return zzdf.zzg((com.google.android.gms.internal.zzag.zza) zzyn.zzeD(key).getObject());
        } catch (Exception e) {
            zzbg.zzaz("Calling getString() threw an exception: " + e.getMessage() + " Returning default value.");
            return zzdf.zzzP();
        }
    }

    public boolean isDefault() {
        return getLastRefreshTime() == 0;
    }

    public void registerFunctionCallMacroCallback(String customMacroName, FunctionCallMacroCallback customMacroCallback) {
        if (customMacroCallback == null) {
            throw new NullPointerException("Macro handler must be non-null");
        }
        synchronized (this.zzaKB) {
            this.zzaKB.put(customMacroName, customMacroCallback);
        }
    }

    public void registerFunctionCallTagCallback(String customTagName, FunctionCallTagCallback customTagCallback) {
        if (customTagCallback == null) {
            throw new NullPointerException("Tag callback must be non-null");
        }
        synchronized (this.zzaKC) {
            this.zzaKC.put(customTagName, customTagCallback);
        }
    }

    void release() {
        this.zzaKA = null;
    }

    public void unregisterFunctionCallMacroCallback(String customMacroName) {
        synchronized (this.zzaKB) {
            this.zzaKB.remove(customMacroName);
        }
    }

    public void unregisterFunctionCallTagCallback(String customTagName) {
        synchronized (this.zzaKC) {
            this.zzaKC.remove(customTagName);
        }
    }

    FunctionCallMacroCallback zzef(String str) {
        FunctionCallMacroCallback functionCallMacroCallback;
        synchronized (this.zzaKB) {
            functionCallMacroCallback = (FunctionCallMacroCallback) this.zzaKB.get(str);
        }
        return functionCallMacroCallback;
    }

    FunctionCallTagCallback zzeg(String str) {
        FunctionCallTagCallback functionCallTagCallback;
        synchronized (this.zzaKC) {
            functionCallTagCallback = (FunctionCallTagCallback) this.zzaKC.get(str);
        }
        return functionCallTagCallback;
    }

    void zzeh(String str) {
        zzyn().zzeh(str);
    }

    zzah zzei(String str) {
        if (zzcb.zzzf().zzzg().equals(zza.CONTAINER_DEBUG)) {
        }
        return new zzbo();
    }

    String zzym() {
        return this.zzaKE;
    }
}
