package com.google.android.gms.common.api;

import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api.Client;
import com.google.android.gms.common.api.Api.ClientKey;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionProgressReportCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.IAccountAccessor;
import com.google.android.gms.common.internal.ResolveAccountResponse;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzps;
import com.google.android.gms.internal.zzpt;
import com.google.android.gms.signin.internal.AuthAccountResult;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

public class zze implements zzh {
    private final Context mContext;
    private final com.google.android.gms.common.api.Api.zza<? extends zzps, zzpt> zzWE;
    private final zzg zzWK;
    private final Lock zzWL;
    private ConnectionResult zzWM;
    private int zzWN;
    private int zzWO = 0;
    private boolean zzWP = false;
    private int zzWQ;
    private final Bundle zzWR = new Bundle();
    private final Set<ClientKey> zzWS = new HashSet();
    private zzps zzWT;
    private int zzWU;
    private boolean zzWV;
    private boolean zzWW;
    private IAccountAccessor zzWX;
    private boolean zzWY;
    private boolean zzWZ;
    private final com.google.android.gms.common.internal.zze zzXa;
    private final Map<Api<?>, Integer> zzXb;

    private static class zza extends com.google.android.gms.signin.internal.zzb {
        private final WeakReference<zze> zzXe;

        zza(zze com_google_android_gms_common_api_zze) {
            this.zzXe = new WeakReference(com_google_android_gms_common_api_zze);
        }

        public void zza(final ConnectionResult connectionResult, AuthAccountResult authAccountResult) {
            final zze com_google_android_gms_common_api_zze = (zze) this.zzXe.get();
            if (com_google_android_gms_common_api_zze != null) {
                com_google_android_gms_common_api_zze.zzWK.zzXs.post(new Runnable(this) {
                    final /* synthetic */ zza zzXh;

                    public void run() {
                        com_google_android_gms_common_api_zze.zza(connectionResult);
                    }
                });
            }
        }
    }

    private static class zzb extends com.google.android.gms.common.internal.zzq.zza {
        private final WeakReference<zze> zzXe;

        zzb(zze com_google_android_gms_common_api_zze) {
            this.zzXe = new WeakReference(com_google_android_gms_common_api_zze);
        }

        public void zzb(final ResolveAccountResponse resolveAccountResponse) {
            final zze com_google_android_gms_common_api_zze = (zze) this.zzXe.get();
            if (com_google_android_gms_common_api_zze != null) {
                com_google_android_gms_common_api_zze.zzWK.zzXs.post(new Runnable(this) {
                    final /* synthetic */ zzb zzXj;

                    public void run() {
                        com_google_android_gms_common_api_zze.zza(resolveAccountResponse);
                    }
                });
            }
        }
    }

    private static class zzc implements ConnectionProgressReportCallbacks {
        private final WeakReference<zze> zzXe;
        private final Api<?> zzXk;
        private final int zzXl;

        public zzc(zze com_google_android_gms_common_api_zze, Api<?> api, int i) {
            this.zzXe = new WeakReference(com_google_android_gms_common_api_zze);
            this.zzXk = api;
            this.zzXl = i;
        }

        public void onReportAccountValidation(ConnectionResult result) {
            boolean z = true;
            zze com_google_android_gms_common_api_zze = (zze) this.zzXe.get();
            if (com_google_android_gms_common_api_zze != null) {
                if (Looper.myLooper() != com_google_android_gms_common_api_zze.zzWK.getLooper()) {
                    z = false;
                }
                zzu.zza(z, (Object) "onReportAccountValidation must be called on the GoogleApiClient handler thread");
                com_google_android_gms_common_api_zze.zzWL.lock();
                try {
                    if (com_google_android_gms_common_api_zze.zzaW(1)) {
                        if (!result.isSuccess()) {
                            com_google_android_gms_common_api_zze.zzb(result, this.zzXk, this.zzXl);
                        }
                        if (com_google_android_gms_common_api_zze.zzmC()) {
                            com_google_android_gms_common_api_zze.zzmF();
                        }
                        com_google_android_gms_common_api_zze.zzWL.unlock();
                    }
                } finally {
                    com_google_android_gms_common_api_zze.zzWL.unlock();
                }
            }
        }

        public void onReportServiceBinding(ConnectionResult result) {
            boolean z = false;
            zze com_google_android_gms_common_api_zze = (zze) this.zzXe.get();
            if (com_google_android_gms_common_api_zze != null) {
                if (Looper.myLooper() == com_google_android_gms_common_api_zze.zzWK.getLooper()) {
                    z = true;
                }
                zzu.zza(z, (Object) "onReportServiceBinding must be called on the GoogleApiClient handler thread");
                com_google_android_gms_common_api_zze.zzWL.lock();
                try {
                    if (com_google_android_gms_common_api_zze.zzaW(0)) {
                        if (!result.isSuccess()) {
                            com_google_android_gms_common_api_zze.zzb(result, this.zzXk, this.zzXl);
                        }
                        if (com_google_android_gms_common_api_zze.zzmC()) {
                            com_google_android_gms_common_api_zze.zzmD();
                        }
                        com_google_android_gms_common_api_zze.zzWL.unlock();
                    }
                } finally {
                    com_google_android_gms_common_api_zze.zzWL.unlock();
                }
            }
        }
    }

    private class zzd implements ConnectionCallbacks, OnConnectionFailedListener {
        final /* synthetic */ zze zzXd;

        private zzd(zze com_google_android_gms_common_api_zze) {
            this.zzXd = com_google_android_gms_common_api_zze;
        }

        public void onConnected(Bundle connectionHint) {
            this.zzXd.zzWT.zza(new zzb(this.zzXd));
        }

        public void onConnectionFailed(ConnectionResult result) {
            this.zzXd.zzWL.lock();
            try {
                if (this.zzXd.zzc(result)) {
                    this.zzXd.zzmI();
                    this.zzXd.zzmG();
                } else {
                    this.zzXd.zzd(result);
                }
                this.zzXd.zzWL.unlock();
            } catch (Throwable th) {
                this.zzXd.zzWL.unlock();
            }
        }

        public void onConnectionSuspended(int cause) {
        }
    }

    public zze(zzg com_google_android_gms_common_api_zzg, com.google.android.gms.common.internal.zze com_google_android_gms_common_internal_zze, Map<Api<?>, Integer> map, com.google.android.gms.common.api.Api.zza<? extends zzps, zzpt> com_google_android_gms_common_api_Api_zza__extends_com_google_android_gms_internal_zzps__com_google_android_gms_internal_zzpt, Lock lock, Context context) {
        this.zzWK = com_google_android_gms_common_api_zzg;
        this.zzXa = com_google_android_gms_common_internal_zze;
        this.zzXb = map;
        this.zzWE = com_google_android_gms_common_api_Api_zza__extends_com_google_android_gms_internal_zzps__com_google_android_gms_internal_zzpt;
        this.zzWL = lock;
        this.mContext = context;
    }

    private void zzT(boolean z) {
        if (this.zzWT != null) {
            if (this.zzWT.isConnected()) {
                if (z) {
                    this.zzWT.zzxY();
                }
                this.zzWT.disconnect();
            }
            this.zzWX = null;
        }
    }

    private void zza(ConnectionResult connectionResult) {
        this.zzWL.lock();
        try {
            if (zzaW(2)) {
                if (connectionResult.isSuccess()) {
                    zzmG();
                } else if (zzc(connectionResult)) {
                    zzmI();
                    zzmG();
                } else {
                    zzd(connectionResult);
                }
                this.zzWL.unlock();
            }
        } finally {
            this.zzWL.unlock();
        }
    }

    private void zza(ResolveAccountResponse resolveAccountResponse) {
        ConnectionResult zzoa = resolveAccountResponse.zzoa();
        this.zzWL.lock();
        try {
            if (zzaW(0)) {
                if (zzoa.isSuccess()) {
                    this.zzWX = resolveAccountResponse.zznZ();
                    this.zzWW = true;
                    this.zzWY = resolveAccountResponse.zzob();
                    this.zzWZ = resolveAccountResponse.zzoc();
                    zzmE();
                } else if (zzc(zzoa)) {
                    zzmI();
                    if (this.zzWQ == 0) {
                        zzmG();
                    }
                } else {
                    zzd(zzoa);
                }
                this.zzWL.unlock();
            }
        } finally {
            this.zzWL.unlock();
        }
    }

    private boolean zza(int i, int i2, ConnectionResult connectionResult) {
        return (i2 != 1 || zzb(connectionResult)) ? this.zzWM == null || i < this.zzWN : false;
    }

    private boolean zzaW(int i) {
        if (this.zzWO == i) {
            return true;
        }
        Log.wtf("GoogleApiClientConnecting", "GoogleApiClient connecting is in step " + zzaX(this.zzWO) + " but received callback for step " + zzaX(i));
        zzd(new ConnectionResult(8, null));
        return false;
    }

    private String zzaX(int i) {
        switch (i) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                return "STEP_GETTING_SERVICE_BINDINGS";
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return "STEP_VALIDATING_ACCOUNT";
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return "STEP_AUTHENTICATING";
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return "STEP_GETTING_REMOTE_SERVICE";
            default:
                return "UNKNOWN";
        }
    }

    private void zzb(ConnectionResult connectionResult, Api<?> api, int i) {
        if (i != 2) {
            int priority = api.zzmp().getPriority();
            if (zza(priority, i, connectionResult)) {
                this.zzWM = connectionResult;
                this.zzWN = priority;
            }
        }
        this.zzWK.zzXv.put(api.zzms(), connectionResult);
    }

    private static boolean zzb(ConnectionResult connectionResult) {
        return connectionResult.hasResolution() || GooglePlayServicesUtil.zzaT(connectionResult.getErrorCode()) != null;
    }

    private boolean zzc(ConnectionResult connectionResult) {
        return this.zzWU != 2 ? this.zzWU == 1 && !connectionResult.hasResolution() : true;
    }

    private void zzd(ConnectionResult connectionResult) {
        boolean z = false;
        this.zzWP = false;
        this.zzWK.zzXw.clear();
        this.zzWM = connectionResult;
        if (!connectionResult.hasResolution()) {
            z = true;
        }
        zzT(z);
        zzaV(3);
        if (!(this.zzWK.zzmO() && GooglePlayServicesUtil.zze(this.mContext, connectionResult.getErrorCode()))) {
            this.zzWK.zzmR();
            this.zzWK.zzXn.zzh(connectionResult);
        }
        this.zzWK.zzXn.zznT();
    }

    private boolean zzmC() {
        this.zzWQ--;
        if (this.zzWQ > 0) {
            return false;
        }
        if (this.zzWQ < 0) {
            Log.wtf("GoogleApiClientConnecting", "GoogleApiClient received too many callbacks for the given step. Clients may be in an unexpected state; GoogleApiClient will now disconnect.");
            zzd(new ConnectionResult(8, null));
            return false;
        } else if (this.zzWM == null) {
            return true;
        } else {
            zzd(this.zzWM);
            return false;
        }
    }

    private void zzmD() {
        if (this.zzWV) {
            zzmE();
        } else {
            zzmG();
        }
    }

    private void zzmE() {
        if (this.zzWW && this.zzWQ == 0) {
            this.zzWO = 1;
            this.zzWQ = this.zzWK.zzXu.size();
            for (ClientKey clientKey : this.zzWK.zzXu.keySet()) {
                if (!this.zzWK.zzXv.containsKey(clientKey)) {
                    ((Client) this.zzWK.zzXu.get(clientKey)).validateAccount(this.zzWX);
                } else if (zzmC()) {
                    zzmF();
                }
            }
        }
    }

    private void zzmF() {
        this.zzWO = 2;
        this.zzWK.zzXw = zzmJ();
        this.zzWT.zza(this.zzWX, this.zzWK.zzXw, new zza(this));
    }

    private void zzmG() {
        Set set = this.zzWK.zzXw;
        Set zzmJ = set.isEmpty() ? zzmJ() : set;
        this.zzWO = 3;
        this.zzWQ = this.zzWK.zzXu.size();
        for (ClientKey clientKey : this.zzWK.zzXu.keySet()) {
            if (!this.zzWK.zzXv.containsKey(clientKey)) {
                ((Client) this.zzWK.zzXu.get(clientKey)).getRemoteService(this.zzWX, zzmJ);
            } else if (zzmC()) {
                zzmH();
            }
        }
    }

    private void zzmH() {
        this.zzWK.zzmN();
        if (this.zzWT != null) {
            if (this.zzWY) {
                this.zzWT.zza(this.zzWX, this.zzWZ);
            }
            zzT(false);
        }
        for (ClientKey clientKey : this.zzWK.zzXv.keySet()) {
            ((Client) this.zzWK.zzXu.get(clientKey)).disconnect();
        }
        if (this.zzWP) {
            this.zzWP = false;
            zzaV(-1);
            return;
        }
        this.zzWK.zzXn.zzg(this.zzWR.isEmpty() ? null : this.zzWR);
    }

    private void zzmI() {
        this.zzWV = false;
        this.zzWK.zzXw.clear();
        for (ClientKey clientKey : this.zzWS) {
            if (!this.zzWK.zzXv.containsKey(clientKey)) {
                this.zzWK.zzXv.put(clientKey, new ConnectionResult(17, null));
            }
        }
    }

    private Set<Scope> zzmJ() {
        Set<Scope> hashSet = new HashSet(this.zzXa.zznv());
        Map zznx = this.zzXa.zznx();
        for (Api api : zznx.keySet()) {
            if (!this.zzWK.zzXv.containsKey(api.zzms())) {
                hashSet.addAll(((com.google.android.gms.common.internal.zze.zza) zznx.get(api)).zzWJ);
            }
        }
        return hashSet;
    }

    public void begin() {
        this.zzWK.zzXn.zznU();
        this.zzWK.zzXv.clear();
        this.zzWP = false;
        this.zzWV = false;
        this.zzWM = null;
        this.zzWO = 0;
        this.zzWU = 2;
        this.zzWW = false;
        this.zzWY = false;
        int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.mContext);
        if (isGooglePlayServicesAvailable != 0) {
            final ConnectionResult connectionResult = new ConnectionResult(isGooglePlayServicesAvailable, null);
            this.zzWK.zzXs.post(new Runnable(this) {
                final /* synthetic */ zze zzXd;

                public void run() {
                    this.zzXd.zzWL.lock();
                    try {
                        this.zzXd.zzd(connectionResult);
                    } finally {
                        this.zzXd.zzWL.unlock();
                    }
                }
            });
            return;
        }
        Map hashMap = new HashMap();
        int i = 0;
        for (Api api : this.zzXb.keySet()) {
            Client client = (Client) this.zzWK.zzXu.get(api.zzms());
            int intValue = ((Integer) this.zzXb.get(api)).intValue();
            int i2 = (api.zzmp().getPriority() == 1 ? 1 : 0) | i;
            if (client.requiresSignIn()) {
                this.zzWV = true;
                if (intValue < this.zzWU) {
                    this.zzWU = intValue;
                }
                if (intValue != 0) {
                    this.zzWS.add(api.zzms());
                }
            }
            hashMap.put(client, new zzc(this, api, intValue));
            i = i2;
        }
        if (i != 0) {
            this.zzWV = false;
        }
        if (this.zzWV) {
            this.zzXa.zza(Integer.valueOf(this.zzWK.getSessionId()));
            ConnectionCallbacks com_google_android_gms_common_api_zze_zzd = new zzd();
            this.zzWT = (zzps) this.zzWE.zza(this.mContext, this.zzWK.getLooper(), this.zzXa, this.zzXa.zznB(), com_google_android_gms_common_api_zze_zzd, com_google_android_gms_common_api_zze_zzd);
            this.zzWT.connect();
        }
        this.zzWQ = this.zzWK.zzXu.size();
        for (Client client2 : this.zzWK.zzXu.values()) {
            client2.connect((ConnectionProgressReportCallbacks) hashMap.get(client2));
        }
    }

    public void connect() {
        this.zzWP = false;
    }

    public String getName() {
        return "CONNECTING";
    }

    public void onConnected(Bundle connectionHint) {
        if (zzaW(3)) {
            if (connectionHint != null) {
                this.zzWR.putAll(connectionHint);
            }
            if (zzmC()) {
                zzmH();
            }
        }
    }

    public void onConnectionSuspended(int cause) {
        zzd(new ConnectionResult(8, null));
    }

    public <A extends Client, R extends Result, T extends com.google.android.gms.common.api.zza.zza<R, A>> T zza(T t) {
        this.zzWK.zzXo.add(t);
        return t;
    }

    public void zza(ConnectionResult connectionResult, Api<?> api, int i) {
        if (zzaW(3)) {
            zzb(connectionResult, api, i);
            if (zzmC()) {
                zzmH();
            }
        }
    }

    public void zzaV(int i) {
        if (i == -1) {
            Iterator it = this.zzWK.zzXo.iterator();
            while (it.hasNext()) {
                zze com_google_android_gms_common_api_zzg_zze = (zze) it.next();
                if (com_google_android_gms_common_api_zzg_zze.zzmv() != 1) {
                    com_google_android_gms_common_api_zzg_zze.cancel();
                    it.remove();
                }
            }
            this.zzWK.zzmK();
            if (this.zzWM != null || this.zzWK.zzXo.isEmpty()) {
                this.zzWK.zzXv.clear();
                this.zzWM = null;
                zzT(true);
            } else {
                this.zzWP = true;
                return;
            }
        }
        this.zzWK.zze(this.zzWM);
    }

    public <A extends Client, T extends com.google.android.gms.common.api.zza.zza<? extends Result, A>> T zzb(T t) {
        throw new IllegalStateException("GoogleApiClient is not connected yet.");
    }
}
