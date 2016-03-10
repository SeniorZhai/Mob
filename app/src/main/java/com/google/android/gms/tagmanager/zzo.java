package com.google.android.gms.tagmanager;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tagmanager.ContainerHolder.ContainerAvailableListener;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

class zzo implements ContainerHolder {
    private Status zzOt;
    private final Looper zzWt;
    private Container zzaKG;
    private Container zzaKH;
    private zzb zzaKI;
    private zza zzaKJ;
    private TagManager zzaKK;
    private boolean zzaea;

    public interface zza {
        void zzej(String str);

        String zzyo();

        void zzyq();
    }

    private class zzb extends Handler {
        private final ContainerAvailableListener zzaKL;
        final /* synthetic */ zzo zzaKM;

        public zzb(zzo com_google_android_gms_tagmanager_zzo, ContainerAvailableListener containerAvailableListener, Looper looper) {
            this.zzaKM = com_google_android_gms_tagmanager_zzo;
            super(looper);
            this.zzaKL = containerAvailableListener;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    zzel((String) msg.obj);
                    return;
                default:
                    zzbg.zzaz("Don't know how to handle this message.");
                    return;
            }
        }

        public void zzek(String str) {
            sendMessage(obtainMessage(1, str));
        }

        protected void zzel(String str) {
            this.zzaKL.onContainerAvailable(this.zzaKM, str);
        }
    }

    public zzo(Status status) {
        this.zzOt = status;
        this.zzWt = null;
    }

    public zzo(TagManager tagManager, Looper looper, Container container, zza com_google_android_gms_tagmanager_zzo_zza) {
        this.zzaKK = tagManager;
        if (looper == null) {
            looper = Looper.getMainLooper();
        }
        this.zzWt = looper;
        this.zzaKG = container;
        this.zzaKJ = com_google_android_gms_tagmanager_zzo_zza;
        this.zzOt = Status.zzXP;
        tagManager.zza(this);
    }

    private void zzyp() {
        if (this.zzaKI != null) {
            this.zzaKI.zzek(this.zzaKH.zzym());
        }
    }

    public synchronized Container getContainer() {
        Container container = null;
        synchronized (this) {
            if (this.zzaea) {
                zzbg.zzaz("ContainerHolder is released.");
            } else {
                if (this.zzaKH != null) {
                    this.zzaKG = this.zzaKH;
                    this.zzaKH = null;
                }
                container = this.zzaKG;
            }
        }
        return container;
    }

    String getContainerId() {
        if (!this.zzaea) {
            return this.zzaKG.getContainerId();
        }
        zzbg.zzaz("getContainerId called on a released ContainerHolder.");
        return BuildConfig.FLAVOR;
    }

    public Status getStatus() {
        return this.zzOt;
    }

    public synchronized void refresh() {
        if (this.zzaea) {
            zzbg.zzaz("Refreshing a released ContainerHolder.");
        } else {
            this.zzaKJ.zzyq();
        }
    }

    public synchronized void release() {
        if (this.zzaea) {
            zzbg.zzaz("Releasing a released ContainerHolder.");
        } else {
            this.zzaea = true;
            this.zzaKK.zzb(this);
            this.zzaKG.release();
            this.zzaKG = null;
            this.zzaKH = null;
            this.zzaKJ = null;
            this.zzaKI = null;
        }
    }

    public synchronized void setContainerAvailableListener(ContainerAvailableListener listener) {
        if (this.zzaea) {
            zzbg.zzaz("ContainerHolder is released.");
        } else if (listener == null) {
            this.zzaKI = null;
        } else {
            this.zzaKI = new zzb(this, listener, this.zzWt);
            if (this.zzaKH != null) {
                zzyp();
            }
        }
    }

    public synchronized void zza(Container container) {
        if (!this.zzaea) {
            if (container == null) {
                zzbg.zzaz("Unexpected null container.");
            } else {
                this.zzaKH = container;
                zzyp();
            }
        }
    }

    public synchronized void zzeh(String str) {
        if (!this.zzaea) {
            this.zzaKG.zzeh(str);
        }
    }

    void zzej(String str) {
        if (this.zzaea) {
            zzbg.zzaz("setCtfeUrlPathAndQuery called on a released ContainerHolder.");
        } else {
            this.zzaKJ.zzej(str);
        }
    }

    String zzyo() {
        if (!this.zzaea) {
            return this.zzaKJ.zzyo();
        }
        zzbg.zzaz("setCtfeUrlPathAndQuery called on a released ContainerHolder.");
        return BuildConfig.FLAVOR;
    }
}
