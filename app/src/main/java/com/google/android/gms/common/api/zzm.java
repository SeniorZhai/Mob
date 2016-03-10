package com.google.android.gms.common.api;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzu;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class zzm extends Fragment implements OnCancelListener {
    private boolean mStarted;
    private boolean zzXV;
    private int zzXW = -1;
    private ConnectionResult zzXX;
    private final Handler zzXY = new Handler(Looper.getMainLooper());
    private final SparseArray<zza> zzXZ = new SparseArray();

    private class zza implements OnConnectionFailedListener {
        public final int zzYa;
        public final GoogleApiClient zzYb;
        public final OnConnectionFailedListener zzYc;
        final /* synthetic */ zzm zzYd;

        public zza(zzm com_google_android_gms_common_api_zzm, int i, GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
            this.zzYd = com_google_android_gms_common_api_zzm;
            this.zzYa = i;
            this.zzYb = googleApiClient;
            this.zzYc = onConnectionFailedListener;
            googleApiClient.registerConnectionFailedListener(this);
        }

        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            writer.append(prefix).append("GoogleApiClient #").print(this.zzYa);
            writer.println(":");
            this.zzYb.dump(prefix + "  ", fd, writer, args);
        }

        public void onConnectionFailed(ConnectionResult result) {
            this.zzYd.zzXY.post(new zzb(this.zzYd, this.zzYa, result));
        }

        public void zzmW() {
            this.zzYb.unregisterConnectionFailedListener(this);
            this.zzYb.disconnect();
        }
    }

    private class zzb implements Runnable {
        final /* synthetic */ zzm zzYd;
        private final int zzYe;
        private final ConnectionResult zzYf;

        public zzb(zzm com_google_android_gms_common_api_zzm, int i, ConnectionResult connectionResult) {
            this.zzYd = com_google_android_gms_common_api_zzm;
            this.zzYe = i;
            this.zzYf = connectionResult;
        }

        public void run() {
            if (this.zzYd.mStarted && !this.zzYd.zzXV) {
                this.zzYd.zzXV = true;
                this.zzYd.zzXW = this.zzYe;
                this.zzYd.zzXX = this.zzYf;
                if (this.zzYf.hasResolution()) {
                    try {
                        this.zzYf.startResolutionForResult(this.zzYd.getActivity(), ((this.zzYd.getActivity().getSupportFragmentManager().getFragments().indexOf(this.zzYd) + 1) << 16) + 1);
                    } catch (SendIntentException e) {
                        this.zzYd.zzmV();
                    }
                } else if (GooglePlayServicesUtil.isUserRecoverableError(this.zzYf.getErrorCode())) {
                    GooglePlayServicesUtil.showErrorDialogFragment(this.zzYf.getErrorCode(), this.zzYd.getActivity(), this.zzYd, 2, this.zzYd);
                } else {
                    this.zzYd.zza(this.zzYe, this.zzYf);
                }
            }
        }
    }

    public static zzm zza(FragmentActivity fragmentActivity) {
        zzu.zzbY("Must be called from main thread of process");
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        try {
            zzm com_google_android_gms_common_api_zzm = (zzm) supportFragmentManager.findFragmentByTag("GmsSupportLifecycleFragment");
            if (com_google_android_gms_common_api_zzm != null && !com_google_android_gms_common_api_zzm.isRemoving()) {
                return com_google_android_gms_common_api_zzm;
            }
            Fragment com_google_android_gms_common_api_zzm2 = new zzm();
            supportFragmentManager.beginTransaction().add(com_google_android_gms_common_api_zzm2, "GmsSupportLifecycleFragment").commit();
            supportFragmentManager.executePendingTransactions();
            return com_google_android_gms_common_api_zzm2;
        } catch (Throwable e) {
            throw new IllegalStateException("Fragment with tag GmsSupportLifecycleFragment is not a SupportLifecycleFragment", e);
        }
    }

    private void zza(int i, ConnectionResult connectionResult) {
        Log.w("GmsSupportLifecycleFragment", "Unresolved error while connecting client. Stopping auto-manage.");
        zza com_google_android_gms_common_api_zzm_zza = (zza) this.zzXZ.get(i);
        if (com_google_android_gms_common_api_zzm_zza != null) {
            zzbb(i);
            OnConnectionFailedListener onConnectionFailedListener = com_google_android_gms_common_api_zzm_zza.zzYc;
            if (onConnectionFailedListener != null) {
                onConnectionFailedListener.onConnectionFailed(connectionResult);
            }
        }
        zzmV();
    }

    private void zzmV() {
        this.zzXV = false;
        this.zzXW = -1;
        this.zzXX = null;
        for (int i = 0; i < this.zzXZ.size(); i++) {
            ((zza) this.zzXZ.valueAt(i)).zzYb.connect();
        }
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        for (int i = 0; i < this.zzXZ.size(); i++) {
            ((zza) this.zzXZ.valueAt(i)).dump(prefix, fd, writer, args);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onActivityResult(int r4, int r5, android.content.Intent r6) {
        /*
        r3 = this;
        r0 = 1;
        r1 = 0;
        switch(r4) {
            case 1: goto L_0x0017;
            case 2: goto L_0x000c;
            default: goto L_0x0005;
        };
    L_0x0005:
        r0 = r1;
    L_0x0006:
        if (r0 == 0) goto L_0x001b;
    L_0x0008:
        r3.zzmV();
    L_0x000b:
        return;
    L_0x000c:
        r2 = r3.getActivity();
        r2 = com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable(r2);
        if (r2 != 0) goto L_0x0005;
    L_0x0016:
        goto L_0x0006;
    L_0x0017:
        r2 = -1;
        if (r5 != r2) goto L_0x0005;
    L_0x001a:
        goto L_0x0006;
    L_0x001b:
        r0 = r3.zzXW;
        r1 = r3.zzXX;
        r3.zza(r0, r1);
        goto L_0x000b;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.api.zzm.onActivityResult(int, int, android.content.Intent):void");
    }

    public void onCancel(DialogInterface dialogInterface) {
        zza(this.zzXW, new ConnectionResult(13, null));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.zzXV = savedInstanceState.getBoolean("resolving_error", false);
            this.zzXW = savedInstanceState.getInt("failed_client_id", -1);
            if (this.zzXW >= 0) {
                this.zzXX = new ConnectionResult(savedInstanceState.getInt("failed_status"), (PendingIntent) savedInstanceState.getParcelable("failed_resolution"));
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("resolving_error", this.zzXV);
        if (this.zzXW >= 0) {
            outState.putInt("failed_client_id", this.zzXW);
            outState.putInt("failed_status", this.zzXX.getErrorCode());
            outState.putParcelable("failed_resolution", this.zzXX.getResolution());
        }
    }

    public void onStart() {
        super.onStart();
        this.mStarted = true;
        if (!this.zzXV) {
            for (int i = 0; i < this.zzXZ.size(); i++) {
                ((zza) this.zzXZ.valueAt(i)).zzYb.connect();
            }
        }
    }

    public void onStop() {
        super.onStop();
        this.mStarted = false;
        for (int i = 0; i < this.zzXZ.size(); i++) {
            ((zza) this.zzXZ.valueAt(i)).zzYb.disconnect();
        }
    }

    public void zza(int i, GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
        zzu.zzb((Object) googleApiClient, (Object) "GoogleApiClient instance cannot be null");
        zzu.zza(this.zzXZ.indexOfKey(i) < 0, "Already managing a GoogleApiClient with id " + i);
        this.zzXZ.put(i, new zza(this, i, googleApiClient, onConnectionFailedListener));
        if (this.mStarted && !this.zzXV) {
            googleApiClient.connect();
        }
    }

    public void zzbb(int i) {
        zza com_google_android_gms_common_api_zzm_zza = (zza) this.zzXZ.get(i);
        this.zzXZ.remove(i);
        if (com_google_android_gms_common_api_zzm_zza != null) {
            com_google_android_gms_common_api_zzm_zza.zzmW();
        }
    }
}
