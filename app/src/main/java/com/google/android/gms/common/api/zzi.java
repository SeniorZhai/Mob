package com.google.android.gms.common.api;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.android.gms.common.internal.zzu;

public final class zzi<L> {
    private volatile L mListener;
    private final zza zzXL;

    public interface zzb<L> {
        void zzmw();

        void zzn(L l);
    }

    private final class zza extends Handler {
        final /* synthetic */ zzi zzXM;

        public zza(zzi com_google_android_gms_common_api_zzi, Looper looper) {
            this.zzXM = com_google_android_gms_common_api_zzi;
            super(looper);
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            if (msg.what != 1) {
                z = false;
            }
            zzu.zzV(z);
            this.zzXM.zzb((zzb) msg.obj);
        }
    }

    zzi(Looper looper, L l) {
        this.zzXL = new zza(this, looper);
        this.mListener = zzu.zzb((Object) l, (Object) "Listener must not be null");
    }

    public void clear() {
        this.mListener = null;
    }

    public void zza(zzb<? super L> com_google_android_gms_common_api_zzi_zzb__super_L) {
        zzu.zzb((Object) com_google_android_gms_common_api_zzi_zzb__super_L, (Object) "Notifier must not be null");
        this.zzXL.sendMessage(this.zzXL.obtainMessage(1, com_google_android_gms_common_api_zzi_zzb__super_L));
    }

    void zzb(zzb<? super L> com_google_android_gms_common_api_zzi_zzb__super_L) {
        Object obj = this.mListener;
        if (obj == null) {
            com_google_android_gms_common_api_zzi_zzb__super_L.zzmw();
            return;
        }
        try {
            com_google_android_gms_common_api_zzi_zzb__super_L.zzn(obj);
        } catch (RuntimeException e) {
            com_google_android_gms_common_api_zzi_zzb__super_L.zzmw();
            throw e;
        }
    }
}
