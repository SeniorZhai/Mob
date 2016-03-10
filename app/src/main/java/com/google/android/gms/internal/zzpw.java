package com.google.android.gms.internal;

import android.content.Context;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.TagManager;
import java.util.HashSet;
import java.util.Set;

public class zzpw {
    private static zzpw zzaOV;
    private Context mContext;
    private boolean mStarted;
    private final Set<zza> zzaOW = new HashSet();
    private TagManager zzaOX = null;
    private zzpv zzoY;

    public interface zza {
        void zzbm();
    }

    zzpw(Context context, TagManager tagManager) {
        this.mContext = context;
        this.zzaOX = tagManager;
    }

    public static zzpw zzaK(Context context) {
        zzu.zzu(context);
        if (zzaOV == null) {
            synchronized (zzpw.class) {
                if (zzaOV == null) {
                    zzaOV = new zzpw(context, TagManager.getInstance(context.getApplicationContext()));
                }
            }
        }
        return zzaOV;
    }

    private void zzgy() {
        synchronized (this) {
            for (zza zzbm : this.zzaOW) {
                zzbm.zzbm();
            }
        }
    }

    public void start() throws IllegalStateException {
        synchronized (this) {
            if (this.mStarted) {
                throw new IllegalStateException("Method start() has already been called");
            } else if (this.zzoY == null) {
                throw new IllegalStateException("No settings configured");
            } else {
                this.mStarted = true;
                this.zzaOX.zzc(this.zzoY.zzzT(), -1, "admob").setResultCallback(new ResultCallback<ContainerHolder>(this) {
                    final /* synthetic */ zzpw zzaOY;

                    {
                        this.zzaOY = r1;
                    }

                    public /* synthetic */ void onResult(Result x0) {
                        zza((ContainerHolder) x0);
                    }

                    public void zza(ContainerHolder containerHolder) {
                        this.zzaOY.zzoY = new zzpu(this.zzaOY.mContext, containerHolder.getStatus().isSuccess() ? containerHolder.getContainer() : null, this.zzaOY.zzzX()).zzzR();
                        this.zzaOY.zzgy();
                    }
                });
            }
        }
    }

    public void zza(zzpv com_google_android_gms_internal_zzpv) {
        synchronized (this) {
            if (this.mStarted) {
                throw new IllegalStateException("Settings can't be changed after TagManager has been started");
            }
            this.zzoY = com_google_android_gms_internal_zzpv;
        }
    }

    public void zza(zza com_google_android_gms_internal_zzpw_zza) {
        synchronized (this) {
            this.zzaOW.add(com_google_android_gms_internal_zzpw_zza);
        }
    }

    public zzpv zzzX() {
        zzpv com_google_android_gms_internal_zzpv;
        synchronized (this) {
            com_google_android_gms_internal_zzpv = this.zzoY;
        }
        return com_google_android_gms_internal_zzpv;
    }
}
