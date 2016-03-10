package com.google.android.gms.common.internal;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

final class zzl extends zzk implements Callback {
    private final Handler mHandler;
    private final HashMap<zza, zzb> zzaaL = new HashMap();
    private final com.google.android.gms.common.stats.zzb zzaaM;
    private final long zzaaN;
    private final Context zzqw;

    private static final class zza {
        private final ComponentName zzaaO;
        private final String zzuO;

        public zza(ComponentName componentName) {
            this.zzuO = null;
            this.zzaaO = (ComponentName) zzu.zzu(componentName);
        }

        public zza(String str) {
            this.zzuO = zzu.zzcj(str);
            this.zzaaO = null;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof zza)) {
                return false;
            }
            zza com_google_android_gms_common_internal_zzl_zza = (zza) o;
            return zzt.equal(this.zzuO, com_google_android_gms_common_internal_zzl_zza.zzuO) && zzt.equal(this.zzaaO, com_google_android_gms_common_internal_zzl_zza.zzaaO);
        }

        public int hashCode() {
            return zzt.hashCode(this.zzuO, this.zzaaO);
        }

        public String toString() {
            return this.zzuO == null ? this.zzaaO.flattenToString() : this.zzuO;
        }

        public Intent zznV() {
            return this.zzuO != null ? new Intent(this.zzuO).setPackage(AdvertisingInfoServiceStrategy.GOOGLE_PLAY_SERVICES_INTENT_PACKAGE_NAME) : new Intent().setComponent(this.zzaaO);
        }
    }

    private final class zzb {
        private int mState = 2;
        private IBinder zzZQ;
        private ComponentName zzaaO;
        private final zza zzaaP = new zza(this);
        private final Set<ServiceConnection> zzaaQ = new HashSet();
        private boolean zzaaR;
        private final zza zzaaS;
        final /* synthetic */ zzl zzaaT;

        public class zza implements ServiceConnection {
            final /* synthetic */ zzb zzaaU;

            public zza(zzb com_google_android_gms_common_internal_zzl_zzb) {
                this.zzaaU = com_google_android_gms_common_internal_zzl_zzb;
            }

            public void onServiceConnected(ComponentName component, IBinder binder) {
                synchronized (this.zzaaU.zzaaT.zzaaL) {
                    this.zzaaU.zzZQ = binder;
                    this.zzaaU.zzaaO = component;
                    for (ServiceConnection onServiceConnected : this.zzaaU.zzaaQ) {
                        onServiceConnected.onServiceConnected(component, binder);
                    }
                    this.zzaaU.mState = 1;
                }
            }

            public void onServiceDisconnected(ComponentName component) {
                synchronized (this.zzaaU.zzaaT.zzaaL) {
                    this.zzaaU.zzZQ = null;
                    this.zzaaU.zzaaO = component;
                    for (ServiceConnection onServiceDisconnected : this.zzaaU.zzaaQ) {
                        onServiceDisconnected.onServiceDisconnected(component);
                    }
                    this.zzaaU.mState = 2;
                }
            }
        }

        public zzb(zzl com_google_android_gms_common_internal_zzl, zza com_google_android_gms_common_internal_zzl_zza) {
            this.zzaaT = com_google_android_gms_common_internal_zzl;
            this.zzaaS = com_google_android_gms_common_internal_zzl_zza;
        }

        public IBinder getBinder() {
            return this.zzZQ;
        }

        public ComponentName getComponentName() {
            return this.zzaaO;
        }

        public int getState() {
            return this.mState;
        }

        public boolean isBound() {
            return this.zzaaR;
        }

        public void zza(ServiceConnection serviceConnection, String str) {
            this.zzaaT.zzaaM.zza(this.zzaaT.zzqw, serviceConnection, str, this.zzaaS.zznV());
            this.zzaaQ.add(serviceConnection);
        }

        public boolean zza(ServiceConnection serviceConnection) {
            return this.zzaaQ.contains(serviceConnection);
        }

        public void zzb(ServiceConnection serviceConnection, String str) {
            this.zzaaT.zzaaM.zzb(this.zzaaT.zzqw, serviceConnection);
            this.zzaaQ.remove(serviceConnection);
        }

        public void zzcc(String str) {
            this.zzaaR = this.zzaaT.zzaaM.zza(this.zzaaT.zzqw, str, this.zzaaS.zznV(), this.zzaaP, 129);
            if (this.zzaaR) {
                this.mState = 3;
                return;
            }
            try {
                this.zzaaT.zzaaM.zza(this.zzaaT.zzqw, this.zzaaP);
            } catch (IllegalArgumentException e) {
            }
        }

        public void zzcd(String str) {
            this.zzaaT.zzaaM.zza(this.zzaaT.zzqw, this.zzaaP);
            this.zzaaR = false;
            this.mState = 2;
        }

        public boolean zznW() {
            return this.zzaaQ.isEmpty();
        }
    }

    zzl(Context context) {
        this.zzqw = context.getApplicationContext();
        this.mHandler = new Handler(context.getMainLooper(), this);
        this.zzaaM = com.google.android.gms.common.stats.zzb.zzoO();
        this.zzaaN = Constants.NOTIFICATION_BANNER_TIMEOUT;
    }

    private boolean zza(zza com_google_android_gms_common_internal_zzl_zza, ServiceConnection serviceConnection, String str) {
        boolean isBound;
        zzu.zzb((Object) serviceConnection, (Object) "ServiceConnection must not be null");
        synchronized (this.zzaaL) {
            zzb com_google_android_gms_common_internal_zzl_zzb = (zzb) this.zzaaL.get(com_google_android_gms_common_internal_zzl_zza);
            if (com_google_android_gms_common_internal_zzl_zzb != null) {
                this.mHandler.removeMessages(0, com_google_android_gms_common_internal_zzl_zzb);
                if (!com_google_android_gms_common_internal_zzl_zzb.zza(serviceConnection)) {
                    com_google_android_gms_common_internal_zzl_zzb.zza(serviceConnection, str);
                    switch (com_google_android_gms_common_internal_zzl_zzb.getState()) {
                        case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                            serviceConnection.onServiceConnected(com_google_android_gms_common_internal_zzl_zzb.getComponentName(), com_google_android_gms_common_internal_zzl_zzb.getBinder());
                            break;
                        case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            com_google_android_gms_common_internal_zzl_zzb.zzcc(str);
                            break;
                        default:
                            break;
                    }
                }
                throw new IllegalStateException("Trying to bind a GmsServiceConnection that was already connected before.  config=" + com_google_android_gms_common_internal_zzl_zza);
            }
            com_google_android_gms_common_internal_zzl_zzb = new zzb(this, com_google_android_gms_common_internal_zzl_zza);
            com_google_android_gms_common_internal_zzl_zzb.zza(serviceConnection, str);
            com_google_android_gms_common_internal_zzl_zzb.zzcc(str);
            this.zzaaL.put(com_google_android_gms_common_internal_zzl_zza, com_google_android_gms_common_internal_zzl_zzb);
            isBound = com_google_android_gms_common_internal_zzl_zzb.isBound();
        }
        return isBound;
    }

    private void zzb(zza com_google_android_gms_common_internal_zzl_zza, ServiceConnection serviceConnection, String str) {
        zzu.zzb((Object) serviceConnection, (Object) "ServiceConnection must not be null");
        synchronized (this.zzaaL) {
            zzb com_google_android_gms_common_internal_zzl_zzb = (zzb) this.zzaaL.get(com_google_android_gms_common_internal_zzl_zza);
            if (com_google_android_gms_common_internal_zzl_zzb == null) {
                throw new IllegalStateException("Nonexistent connection status for service config: " + com_google_android_gms_common_internal_zzl_zza);
            } else if (com_google_android_gms_common_internal_zzl_zzb.zza(serviceConnection)) {
                com_google_android_gms_common_internal_zzl_zzb.zzb(serviceConnection, str);
                if (com_google_android_gms_common_internal_zzl_zzb.zznW()) {
                    this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(0, com_google_android_gms_common_internal_zzl_zzb), this.zzaaN);
                }
            } else {
                throw new IllegalStateException("Trying to unbind a GmsServiceConnection  that was not bound before.  config=" + com_google_android_gms_common_internal_zzl_zza);
            }
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                zzb com_google_android_gms_common_internal_zzl_zzb = (zzb) msg.obj;
                synchronized (this.zzaaL) {
                    if (com_google_android_gms_common_internal_zzl_zzb.zznW()) {
                        if (com_google_android_gms_common_internal_zzl_zzb.isBound()) {
                            com_google_android_gms_common_internal_zzl_zzb.zzcd("GmsClientSupervisor");
                        }
                        this.zzaaL.remove(com_google_android_gms_common_internal_zzl_zzb.zzaaS);
                    }
                }
                return true;
            default:
                return false;
        }
    }

    public boolean zza(ComponentName componentName, ServiceConnection serviceConnection, String str) {
        return zza(new zza(componentName), serviceConnection, str);
    }

    public boolean zza(String str, ServiceConnection serviceConnection, String str2) {
        return zza(new zza(str), serviceConnection, str2);
    }

    public void zzb(ComponentName componentName, ServiceConnection serviceConnection, String str) {
        zzb(new zza(componentName), serviceConnection, str);
    }

    public void zzb(String str, ServiceConnection serviceConnection, String str2) {
        zzb(new zza(str), serviceConnection, str2);
    }
}
