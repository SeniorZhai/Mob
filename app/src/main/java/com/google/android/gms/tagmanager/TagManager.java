package com.google.android.gms.tagmanager;

import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import com.google.android.gms.common.api.PendingResult;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class TagManager {
    private static TagManager zzaOb;
    private final Context mContext;
    private final DataLayer zzaKz;
    private final zzs zzaMV;
    private final zza zzaNY;
    private final zzct zzaNZ;
    private final ConcurrentMap<zzo, Boolean> zzaOa;

    public interface zza {
        zzp zza(Context context, TagManager tagManager, Looper looper, String str, int i, zzs com_google_android_gms_tagmanager_zzs);
    }

    static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] zzaOd = new int[zza.values().length];

        static {
            try {
                zzaOd[zza.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                zzaOd[zza.CONTAINER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                zzaOd[zza.CONTAINER_DEBUG.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    TagManager(Context context, zza containerHolderLoaderProvider, DataLayer dataLayer, zzct serviceManager) {
        if (context == null) {
            throw new NullPointerException("context cannot be null");
        }
        this.mContext = context.getApplicationContext();
        this.zzaNZ = serviceManager;
        this.zzaNY = containerHolderLoaderProvider;
        this.zzaOa = new ConcurrentHashMap();
        this.zzaKz = dataLayer;
        this.zzaKz.zza(new zzb(this) {
            final /* synthetic */ TagManager zzaOc;

            {
                this.zzaOc = r1;
            }

            public void zzF(Map<String, Object> map) {
                Object obj = map.get(DataLayer.EVENT_KEY);
                if (obj != null) {
                    this.zzaOc.zzeF(obj.toString());
                }
            }
        });
        this.zzaKz.zza(new zzd(this.mContext));
        this.zzaMV = new zzs();
        zzzE();
    }

    public static TagManager getInstance(Context context) {
        TagManager tagManager;
        synchronized (TagManager.class) {
            if (zzaOb == null) {
                if (context == null) {
                    zzbg.zzaz("TagManager.getInstance requires non-null context.");
                    throw new NullPointerException();
                }
                zzaOb = new TagManager(context, new zza() {
                    public zzp zza(Context context, TagManager tagManager, Looper looper, String str, int i, zzs com_google_android_gms_tagmanager_zzs) {
                        return new zzp(context, tagManager, looper, str, i, com_google_android_gms_tagmanager_zzs);
                    }
                }, new DataLayer(new zzw(context)), zzcu.zzzz());
            }
            tagManager = zzaOb;
        }
        return tagManager;
    }

    private void zzeF(String str) {
        for (zzo zzeh : this.zzaOa.keySet()) {
            zzeh.zzeh(str);
        }
    }

    private void zzzE() {
        if (VERSION.SDK_INT >= 14) {
            this.mContext.registerComponentCallbacks(new ComponentCallbacks2(this) {
                final /* synthetic */ TagManager zzaOc;

                {
                    this.zzaOc = r1;
                }

                public void onConfigurationChanged(Configuration configuration) {
                }

                public void onLowMemory() {
                }

                public void onTrimMemory(int i) {
                    if (i == 20) {
                        this.zzaOc.dispatch();
                    }
                }
            });
        }
    }

    public void dispatch() {
        this.zzaNZ.dispatch();
    }

    public DataLayer getDataLayer() {
        return this.zzaKz;
    }

    public PendingResult<ContainerHolder> loadContainerDefaultOnly(String containerId, int defaultContainerResourceId) {
        PendingResult zza = this.zzaNY.zza(this.mContext, this, null, containerId, defaultContainerResourceId, this.zzaMV);
        zza.zzyr();
        return zza;
    }

    public PendingResult<ContainerHolder> loadContainerDefaultOnly(String containerId, int defaultContainerResourceId, Handler handler) {
        PendingResult zza = this.zzaNY.zza(this.mContext, this, handler.getLooper(), containerId, defaultContainerResourceId, this.zzaMV);
        zza.zzyr();
        return zza;
    }

    public PendingResult<ContainerHolder> loadContainerPreferFresh(String containerId, int defaultContainerResourceId) {
        PendingResult zza = this.zzaNY.zza(this.mContext, this, null, containerId, defaultContainerResourceId, this.zzaMV);
        zza.zzyt();
        return zza;
    }

    public PendingResult<ContainerHolder> loadContainerPreferFresh(String containerId, int defaultContainerResourceId, Handler handler) {
        PendingResult zza = this.zzaNY.zza(this.mContext, this, handler.getLooper(), containerId, defaultContainerResourceId, this.zzaMV);
        zza.zzyt();
        return zza;
    }

    public PendingResult<ContainerHolder> loadContainerPreferNonDefault(String containerId, int defaultContainerResourceId) {
        PendingResult zza = this.zzaNY.zza(this.mContext, this, null, containerId, defaultContainerResourceId, this.zzaMV);
        zza.zzys();
        return zza;
    }

    public PendingResult<ContainerHolder> loadContainerPreferNonDefault(String containerId, int defaultContainerResourceId, Handler handler) {
        PendingResult zza = this.zzaNY.zza(this.mContext, this, handler.getLooper(), containerId, defaultContainerResourceId, this.zzaMV);
        zza.zzys();
        return zza;
    }

    public void setVerboseLoggingEnabled(boolean enableVerboseLogging) {
        zzbg.setLogLevel(enableVerboseLogging ? 2 : 5);
    }

    void zza(zzo com_google_android_gms_tagmanager_zzo) {
        this.zzaOa.put(com_google_android_gms_tagmanager_zzo, Boolean.valueOf(true));
    }

    boolean zzb(zzo com_google_android_gms_tagmanager_zzo) {
        return this.zzaOa.remove(com_google_android_gms_tagmanager_zzo) != null;
    }

    public PendingResult<ContainerHolder> zzc(String str, int i, String str2) {
        PendingResult zza = this.zzaNY.zza(this.mContext, this, null, str, i, this.zzaMV);
        zza.load(str2);
        return zza;
    }

    synchronized boolean zzl(Uri uri) {
        boolean z;
        zzcb zzzf = zzcb.zzzf();
        if (zzzf.zzl(uri)) {
            String containerId = zzzf.getContainerId();
            switch (AnonymousClass4.zzaOd[zzzf.zzzg().ordinal()]) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    for (zzo com_google_android_gms_tagmanager_zzo : this.zzaOa.keySet()) {
                        if (com_google_android_gms_tagmanager_zzo.getContainerId().equals(containerId)) {
                            com_google_android_gms_tagmanager_zzo.zzej(null);
                            com_google_android_gms_tagmanager_zzo.refresh();
                        }
                    }
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    for (zzo com_google_android_gms_tagmanager_zzo2 : this.zzaOa.keySet()) {
                        if (com_google_android_gms_tagmanager_zzo2.getContainerId().equals(containerId)) {
                            com_google_android_gms_tagmanager_zzo2.zzej(zzzf.zzzh());
                            com_google_android_gms_tagmanager_zzo2.refresh();
                        } else if (com_google_android_gms_tagmanager_zzo2.zzyo() != null) {
                            com_google_android_gms_tagmanager_zzo2.zzej(null);
                            com_google_android_gms_tagmanager_zzo2.refresh();
                        }
                    }
                    break;
            }
            z = true;
        } else {
            z = false;
        }
        return z;
    }
}
