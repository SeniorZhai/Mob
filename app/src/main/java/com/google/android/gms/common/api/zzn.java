package com.google.android.gms.common.api;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzu;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class zzn extends Fragment implements OnCancelListener, LoaderCallbacks<ConnectionResult> {
    private boolean zzXV;
    private int zzXW = -1;
    private ConnectionResult zzXX;
    private final Handler zzXY = new Handler(Looper.getMainLooper());
    private final SparseArray<zzb> zzXZ = new SparseArray();

    static class zza extends Loader<ConnectionResult> implements ConnectionCallbacks, OnConnectionFailedListener {
        public final GoogleApiClient zzYb;
        private boolean zzYg;
        private ConnectionResult zzYh;

        public zza(Context context, GoogleApiClient googleApiClient) {
            super(context);
            this.zzYb = googleApiClient;
        }

        private void zzf(ConnectionResult connectionResult) {
            this.zzYh = connectionResult;
            if (isStarted() && !isAbandoned()) {
                deliverResult(connectionResult);
            }
        }

        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            this.zzYb.dump(prefix, fd, writer, args);
        }

        public void onConnected(Bundle connectionHint) {
            this.zzYg = false;
            zzf(ConnectionResult.zzVG);
        }

        public void onConnectionFailed(ConnectionResult result) {
            this.zzYg = true;
            zzf(result);
        }

        public void onConnectionSuspended(int cause) {
        }

        protected void onReset() {
            this.zzYh = null;
            this.zzYg = false;
            this.zzYb.unregisterConnectionCallbacks(this);
            this.zzYb.unregisterConnectionFailedListener(this);
            this.zzYb.disconnect();
        }

        protected void onStartLoading() {
            super.onStartLoading();
            this.zzYb.registerConnectionCallbacks(this);
            this.zzYb.registerConnectionFailedListener(this);
            if (this.zzYh != null) {
                deliverResult(this.zzYh);
            }
            if (!this.zzYb.isConnected() && !this.zzYb.isConnecting() && !this.zzYg) {
                this.zzYb.connect();
            }
        }

        protected void onStopLoading() {
            this.zzYb.disconnect();
        }

        public boolean zzmX() {
            return this.zzYg;
        }
    }

    private static class zzb {
        public final GoogleApiClient zzYb;
        public final OnConnectionFailedListener zzYc;

        private zzb(GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
            this.zzYb = googleApiClient;
            this.zzYc = onConnectionFailedListener;
        }
    }

    private class zzc implements Runnable {
        private final int zzYe;
        private final ConnectionResult zzYf;
        final /* synthetic */ zzn zzYi;

        public zzc(zzn com_google_android_gms_common_api_zzn, int i, ConnectionResult connectionResult) {
            this.zzYi = com_google_android_gms_common_api_zzn;
            this.zzYe = i;
            this.zzYf = connectionResult;
        }

        public void run() {
            if (this.zzYf.hasResolution()) {
                try {
                    this.zzYf.startResolutionForResult(this.zzYi.getActivity(), ((this.zzYi.getActivity().getSupportFragmentManager().getFragments().indexOf(this.zzYi) + 1) << 16) + 1);
                } catch (SendIntentException e) {
                    this.zzYi.zzmV();
                }
            } else if (GooglePlayServicesUtil.isUserRecoverableError(this.zzYf.getErrorCode())) {
                GooglePlayServicesUtil.showErrorDialogFragment(this.zzYf.getErrorCode(), this.zzYi.getActivity(), this.zzYi, 2, this.zzYi);
            } else {
                this.zzYi.zza(this.zzYe, this.zzYf);
            }
        }
    }

    private void zza(int i, ConnectionResult connectionResult) {
        Log.w("GmsSupportLoaderLifecycleFragment", "Unresolved error while connecting client. Stopping auto-manage.");
        zzb com_google_android_gms_common_api_zzn_zzb = (zzb) this.zzXZ.get(i);
        if (com_google_android_gms_common_api_zzn_zzb != null) {
            zzbb(i);
            OnConnectionFailedListener onConnectionFailedListener = com_google_android_gms_common_api_zzn_zzb.zzYc;
            if (onConnectionFailedListener != null) {
                onConnectionFailedListener.onConnectionFailed(connectionResult);
            }
        }
        zzmV();
    }

    public static zzn zzb(FragmentActivity fragmentActivity) {
        zzu.zzbY("Must be called from main thread of process");
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        try {
            zzn com_google_android_gms_common_api_zzn = (zzn) supportFragmentManager.findFragmentByTag("GmsSupportLoaderLifecycleFragment");
            if (com_google_android_gms_common_api_zzn != null && !com_google_android_gms_common_api_zzn.isRemoving()) {
                return com_google_android_gms_common_api_zzn;
            }
            Fragment com_google_android_gms_common_api_zzn2 = new zzn();
            supportFragmentManager.beginTransaction().add(com_google_android_gms_common_api_zzn2, "GmsSupportLoaderLifecycleFragment").commit();
            supportFragmentManager.executePendingTransactions();
            return com_google_android_gms_common_api_zzn2;
        } catch (Throwable e) {
            throw new IllegalStateException("Fragment with tag GmsSupportLoaderLifecycleFragment is not a SupportLoaderLifecycleFragment", e);
        }
    }

    private void zzb(int i, ConnectionResult connectionResult) {
        if (!this.zzXV) {
            this.zzXV = true;
            this.zzXW = i;
            this.zzXX = connectionResult;
            this.zzXY.post(new zzc(this, i, connectionResult));
        }
    }

    private void zzmV() {
        int i = 0;
        this.zzXV = false;
        this.zzXW = -1;
        this.zzXX = null;
        LoaderManager loaderManager = getLoaderManager();
        while (i < this.zzXZ.size()) {
            int keyAt = this.zzXZ.keyAt(i);
            zza zzbd = zzbd(keyAt);
            if (zzbd != null && zzbd.zzmX()) {
                loaderManager.destroyLoader(keyAt);
                loaderManager.initLoader(keyAt, null, this);
            }
            i++;
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
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.api.zzn.onActivityResult(int, int, android.content.Intent):void");
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        int i = 0;
        while (i < this.zzXZ.size()) {
            int keyAt = this.zzXZ.keyAt(i);
            zza zzbd = zzbd(keyAt);
            if (zzbd == null || ((zzb) this.zzXZ.valueAt(i)).zzYb == zzbd.zzYb) {
                getLoaderManager().initLoader(keyAt, null, this);
            } else {
                getLoaderManager().restartLoader(keyAt, null, this);
            }
            i++;
        }
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

    public Loader<ConnectionResult> onCreateLoader(int id, Bundle args) {
        return new zza(getActivity(), ((zzb) this.zzXZ.get(id)).zzYb);
    }

    public /* synthetic */ void onLoadFinished(Loader x0, Object x1) {
        zza(x0, (ConnectionResult) x1);
    }

    public void onLoaderReset(Loader<ConnectionResult> loader) {
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
        if (!this.zzXV) {
            for (int i = 0; i < this.zzXZ.size(); i++) {
                getLoaderManager().initLoader(this.zzXZ.keyAt(i), null, this);
            }
        }
    }

    public void zza(int i, GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
        zzu.zzb((Object) googleApiClient, (Object) "GoogleApiClient instance cannot be null");
        zzu.zza(this.zzXZ.indexOfKey(i) < 0, "Already managing a GoogleApiClient with id " + i);
        this.zzXZ.put(i, new zzb(googleApiClient, onConnectionFailedListener));
        if (getActivity() != null) {
            LoaderManager.enableDebugLogging(false);
            getLoaderManager().initLoader(i, null, this);
        }
    }

    public void zza(Loader<ConnectionResult> loader, ConnectionResult connectionResult) {
        if (!connectionResult.isSuccess()) {
            zzb(loader.getId(), connectionResult);
        }
    }

    public void zzbb(int i) {
        this.zzXZ.remove(i);
        getLoaderManager().destroyLoader(i);
    }

    public GoogleApiClient zzbc(int i) {
        if (getActivity() != null) {
            zza zzbd = zzbd(i);
            if (zzbd != null) {
                return zzbd.zzYb;
            }
        }
        return null;
    }

    zza zzbd(int i) {
        try {
            return (zza) getLoaderManager().getLoader(i);
        } catch (Throwable e) {
            throw new IllegalStateException("Unknown loader in SupportLoaderLifecycleFragment", e);
        }
    }
}
