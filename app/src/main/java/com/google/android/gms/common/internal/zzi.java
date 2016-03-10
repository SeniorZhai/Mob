package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api.Client;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionProgressReportCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.http.protocol.HTTP;

public abstract class zzi<T extends IInterface> implements Client, com.google.android.gms.common.internal.zzj.zza {
    public static final String[] zzaav = new String[]{"service_esmobile", "service_googleme"};
    private final Context mContext;
    final Handler mHandler;
    private final Account zzMY;
    private final Set<Scope> zzWJ;
    private final Looper zzWt;
    private final zze zzXa;
    private final zzk zzaak;
    private zzp zzaal;
    private ConnectionProgressReportCallbacks zzaam;
    private T zzaan;
    private final ArrayList<zzc<?>> zzaao;
    private zze zzaap;
    private int zzaaq;
    private ConnectionCallbacks zzaar;
    private OnConnectionFailedListener zzaas;
    private final int zzaat;
    protected AtomicInteger zzaau;
    private final Object zzqt;

    protected abstract class zzc<TListener> {
        private TListener mListener;
        final /* synthetic */ zzi zzaax;
        private boolean zzaay = false;

        public zzc(zzi com_google_android_gms_common_internal_zzi, TListener tListener) {
            this.zzaax = com_google_android_gms_common_internal_zzi;
            this.mListener = tListener;
        }

        public void unregister() {
            zznR();
            synchronized (this.zzaax.zzaao) {
                this.zzaax.zzaao.remove(this);
            }
        }

        protected abstract void zznP();

        public void zznQ() {
            synchronized (this) {
                Object obj = this.mListener;
                if (this.zzaay) {
                    Log.w("GmsClient", "Callback proxy " + this + " being reused. This is not safe.");
                }
            }
            if (obj != null) {
                try {
                    zzr(obj);
                } catch (RuntimeException e) {
                    zznP();
                    throw e;
                }
            }
            zznP();
            synchronized (this) {
                this.zzaay = true;
            }
            unregister();
        }

        public void zznR() {
            synchronized (this) {
                this.mListener = null;
            }
        }

        protected abstract void zzr(TListener tListener);
    }

    private abstract class zza extends zzc<Boolean> {
        public final int statusCode;
        public final Bundle zzaaw;
        final /* synthetic */ zzi zzaax;

        protected zza(zzi com_google_android_gms_common_internal_zzi, int i, Bundle bundle) {
            this.zzaax = com_google_android_gms_common_internal_zzi;
            super(com_google_android_gms_common_internal_zzi, Boolean.valueOf(true));
            this.statusCode = i;
            this.zzaaw = bundle;
        }

        protected void zzc(Boolean bool) {
            PendingIntent pendingIntent = null;
            if (bool == null) {
                this.zzaax.zza(1, null);
                return;
            }
            switch (this.statusCode) {
                case ResponseParser.ResponseActionDiscard /*0*/:
                    if (!zznO()) {
                        this.zzaax.zza(1, null);
                        zzg(new ConnectionResult(8, null));
                        return;
                    }
                    return;
                case HTTP.LF /*10*/:
                    this.zzaax.zza(1, null);
                    throw new IllegalStateException("A fatal developer error has occurred. Check the logs for further information.");
                default:
                    this.zzaax.zza(1, null);
                    if (this.zzaaw != null) {
                        pendingIntent = (PendingIntent) this.zzaaw.getParcelable("pendingIntent");
                    }
                    zzg(new ConnectionResult(this.statusCode, pendingIntent));
                    return;
            }
        }

        protected abstract void zzg(ConnectionResult connectionResult);

        protected abstract boolean zznO();

        protected void zznP() {
        }

        protected /* synthetic */ void zzr(Object obj) {
            zzc((Boolean) obj);
        }
    }

    final class zzb extends Handler {
        final /* synthetic */ zzi zzaax;

        public zzb(zzi com_google_android_gms_common_internal_zzi, Looper looper) {
            this.zzaax = com_google_android_gms_common_internal_zzi;
            super(looper);
        }

        private void zza(Message message) {
            zzc com_google_android_gms_common_internal_zzi_zzc = (zzc) message.obj;
            com_google_android_gms_common_internal_zzi_zzc.zznP();
            com_google_android_gms_common_internal_zzi_zzc.unregister();
        }

        private boolean zzb(Message message) {
            return message.what == 2 || message.what == 1 || message.what == 5 || message.what == 6;
        }

        public void handleMessage(Message msg) {
            if (this.zzaax.zzaau.get() != msg.arg1) {
                if (zzb(msg)) {
                    zza(msg);
                }
            } else if ((msg.what == 1 || msg.what == 5 || msg.what == 6) && !this.zzaax.isConnecting()) {
                zza(msg);
            } else if (msg.what == 3) {
                ConnectionResult connectionResult = new ConnectionResult(msg.arg2, null);
                this.zzaax.zzaam.onReportServiceBinding(connectionResult);
                this.zzaax.onConnectionFailed(connectionResult);
            } else if (msg.what == 4) {
                this.zzaax.zza(4, null);
                if (this.zzaax.zzaar != null) {
                    this.zzaax.zzaar.onConnectionSuspended(msg.arg2);
                }
                this.zzaax.onConnectionSuspended(msg.arg2);
                this.zzaax.zza(4, 1, null);
            } else if (msg.what == 2 && !this.zzaax.isConnected()) {
                zza(msg);
            } else if (zzb(msg)) {
                ((zzc) msg.obj).zznQ();
            } else {
                Log.wtf("GmsClient", "Don't know how to handle this message.");
            }
        }
    }

    public static final class zzd extends com.google.android.gms.common.internal.zzo.zza {
        private final int zzaaA;
        private zzi zzaaz;

        public zzd(zzi com_google_android_gms_common_internal_zzi, int i) {
            this.zzaaz = com_google_android_gms_common_internal_zzi;
            this.zzaaA = i;
        }

        private void zznS() {
            this.zzaaz = null;
        }

        public void zza(int i, IBinder iBinder, Bundle bundle) {
            zzu.zzb(this.zzaaz, (Object) "onPostInitComplete can be called only once per call to getRemoteService");
            this.zzaaz.zza(i, iBinder, bundle, this.zzaaA);
            zznS();
        }

        public void zzb(int i, Bundle bundle) {
            zzu.zzb(this.zzaaz, (Object) "onAccountValidationComplete can be called only once per call to validateAccount");
            this.zzaaz.zza(i, bundle, this.zzaaA);
            zznS();
        }
    }

    public final class zze implements ServiceConnection {
        private final int zzaaA;
        final /* synthetic */ zzi zzaax;

        public zze(zzi com_google_android_gms_common_internal_zzi, int i) {
            this.zzaax = com_google_android_gms_common_internal_zzi;
            this.zzaaA = i;
        }

        public void onServiceConnected(ComponentName component, IBinder binder) {
            zzu.zzb((Object) binder, (Object) "Expecting a valid IBinder");
            this.zzaax.zzaal = com.google.android.gms.common.internal.zzp.zza.zzaG(binder);
            this.zzaax.zzbt(this.zzaaA);
        }

        public void onServiceDisconnected(ComponentName component) {
            this.zzaax.mHandler.sendMessage(this.zzaax.mHandler.obtainMessage(4, this.zzaaA, 1));
        }
    }

    protected class zzf implements ConnectionProgressReportCallbacks {
        final /* synthetic */ zzi zzaax;

        public zzf(zzi com_google_android_gms_common_internal_zzi) {
            this.zzaax = com_google_android_gms_common_internal_zzi;
        }

        public void onReportAccountValidation(ConnectionResult result) {
            throw new IllegalStateException("Legacy GmsClient received onReportAccountValidation callback.");
        }

        public void onReportServiceBinding(ConnectionResult result) {
            if (result.isSuccess()) {
                this.zzaax.getRemoteService(null, this.zzaax.zzWJ);
            } else if (this.zzaax.zzaas != null) {
                this.zzaax.zzaas.onConnectionFailed(result);
            }
        }
    }

    protected final class zzg extends zza {
        public final IBinder zzaaB;
        final /* synthetic */ zzi zzaax;

        public zzg(zzi com_google_android_gms_common_internal_zzi, int i, IBinder iBinder, Bundle bundle) {
            this.zzaax = com_google_android_gms_common_internal_zzi;
            super(com_google_android_gms_common_internal_zzi, i, bundle);
            this.zzaaB = iBinder;
        }

        protected void zzg(ConnectionResult connectionResult) {
            if (this.zzaax.zzaas != null) {
                this.zzaax.zzaas.onConnectionFailed(connectionResult);
            }
            this.zzaax.onConnectionFailed(connectionResult);
        }

        protected boolean zznO() {
            try {
                String interfaceDescriptor = this.zzaaB.getInterfaceDescriptor();
                if (this.zzaax.getServiceDescriptor().equals(interfaceDescriptor)) {
                    IInterface zzT = this.zzaax.zzT(this.zzaaB);
                    if (zzT == null || !this.zzaax.zza(2, 3, zzT)) {
                        return false;
                    }
                    Bundle zzlM = this.zzaax.zzlM();
                    if (this.zzaax.zzaar != null) {
                        this.zzaax.zzaar.onConnected(zzlM);
                    }
                    GooglePlayServicesUtil.zzac(this.zzaax.mContext);
                    return true;
                }
                Log.e("GmsClient", "service descriptor mismatch: " + this.zzaax.getServiceDescriptor() + " vs. " + interfaceDescriptor);
                return false;
            } catch (RemoteException e) {
                Log.w("GmsClient", "service probably died");
                return false;
            }
        }
    }

    protected final class zzh extends zza {
        final /* synthetic */ zzi zzaax;

        public zzh(zzi com_google_android_gms_common_internal_zzi) {
            this.zzaax = com_google_android_gms_common_internal_zzi;
            super(com_google_android_gms_common_internal_zzi, 0, null);
        }

        protected void zzg(ConnectionResult connectionResult) {
            this.zzaax.zzaam.onReportServiceBinding(connectionResult);
            this.zzaax.onConnectionFailed(connectionResult);
        }

        protected boolean zznO() {
            this.zzaax.zzaam.onReportServiceBinding(ConnectionResult.zzVG);
            return true;
        }
    }

    protected final class zzi extends zza {
        final /* synthetic */ zzi zzaax;

        public zzi(zzi com_google_android_gms_common_internal_zzi, int i, Bundle bundle) {
            this.zzaax = com_google_android_gms_common_internal_zzi;
            super(com_google_android_gms_common_internal_zzi, i, bundle);
        }

        protected void zzg(ConnectionResult connectionResult) {
            this.zzaax.zzaam.onReportAccountValidation(connectionResult);
            this.zzaax.onConnectionFailed(connectionResult);
        }

        protected boolean zznO() {
            this.zzaax.zzaam.onReportAccountValidation(ConnectionResult.zzVG);
            return true;
        }
    }

    @Deprecated
    protected zzi(Context context, Looper looper, int i, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        this.zzqt = new Object();
        this.zzaao = new ArrayList();
        this.zzaaq = 1;
        this.zzaau = new AtomicInteger(0);
        this.mContext = (Context) zzu.zzu(context);
        this.zzWt = (Looper) zzu.zzb((Object) looper, (Object) "Looper must not be null");
        this.zzaak = zzk.zzah(context);
        this.mHandler = new zzb(this, looper);
        this.zzaat = i;
        this.zzMY = null;
        this.zzWJ = Collections.emptySet();
        this.zzXa = new Builder(context).zzmx();
        this.zzaar = (ConnectionCallbacks) zzu.zzu(connectionCallbacks);
        this.zzaas = (OnConnectionFailedListener) zzu.zzu(onConnectionFailedListener);
    }

    protected zzi(Context context, Looper looper, int i, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener, zze com_google_android_gms_common_internal_zze) {
        this(context, looper, zzk.zzah(context), i, com_google_android_gms_common_internal_zze, connectionCallbacks, onConnectionFailedListener);
    }

    protected zzi(Context context, Looper looper, zzk com_google_android_gms_common_internal_zzk, int i, zze com_google_android_gms_common_internal_zze) {
        this.zzqt = new Object();
        this.zzaao = new ArrayList();
        this.zzaaq = 1;
        this.zzaau = new AtomicInteger(0);
        this.mContext = (Context) zzu.zzb((Object) context, (Object) "Context must not be null");
        this.zzWt = (Looper) zzu.zzb((Object) looper, (Object) "Looper must not be null");
        this.zzaak = (zzk) zzu.zzb((Object) com_google_android_gms_common_internal_zzk, (Object) "Supervisor must not be null");
        this.mHandler = new zzb(this, looper);
        this.zzaat = i;
        this.zzXa = (zze) zzu.zzu(com_google_android_gms_common_internal_zze);
        this.zzMY = com_google_android_gms_common_internal_zze.getAccount();
        this.zzWJ = zzb(com_google_android_gms_common_internal_zze.zznw());
    }

    protected zzi(Context context, Looper looper, zzk com_google_android_gms_common_internal_zzk, int i, zze com_google_android_gms_common_internal_zze, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener) {
        this(context, looper, com_google_android_gms_common_internal_zzk, i, com_google_android_gms_common_internal_zze);
        this.zzaar = (ConnectionCallbacks) zzu.zzu(connectionCallbacks);
        this.zzaas = (OnConnectionFailedListener) zzu.zzu(onConnectionFailedListener);
    }

    private void zza(int i, T t) {
        boolean z = true;
        if ((i == 3) != (t != null)) {
            z = false;
        }
        zzu.zzV(z);
        synchronized (this.zzqt) {
            this.zzaaq = i;
            this.zzaan = t;
            switch (i) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    zznI();
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    zznH();
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    zznG();
                    break;
            }
        }
    }

    private void zza(ConnectionProgressReportCallbacks connectionProgressReportCallbacks) {
        this.zzaam = (ConnectionProgressReportCallbacks) zzu.zzb((Object) connectionProgressReportCallbacks, (Object) "Connection progress callbacks cannot be null.");
    }

    private boolean zza(int i, int i2, T t) {
        boolean z;
        synchronized (this.zzqt) {
            if (this.zzaaq != i) {
                z = false;
            } else {
                zza(i2, (IInterface) t);
                z = true;
            }
        }
        return z;
    }

    private Set<Scope> zzb(Set<Scope> set) {
        Set<Scope> zza = zza((Set) set);
        if (zza == null) {
            return zza;
        }
        for (Scope contains : zza) {
            if (!set.contains(contains)) {
                throw new IllegalStateException("Expanding scopes is not permitted, use implied scopes instead");
            }
        }
        return zza;
    }

    private void zznH() {
        if (this.zzaap != null) {
            Log.e("GmsClient", "Calling connect() while still connected, missing disconnect() for " + getStartServiceAction());
            this.zzaak.zzb(getStartServiceAction(), this.zzaap, zzkQ());
            this.zzaau.incrementAndGet();
        }
        this.zzaap = new zze(this, this.zzaau.get());
        if (!this.zzaak.zza(getStartServiceAction(), this.zzaap, zzkQ())) {
            Log.e("GmsClient", "unable to connect to service: " + getStartServiceAction());
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3, this.zzaau.get(), 9));
        }
    }

    private void zznI() {
        if (this.zzaap != null) {
            this.zzaak.zzb(getStartServiceAction(), this.zzaap, zzkQ());
            this.zzaap = null;
        }
    }

    public void connect(ConnectionProgressReportCallbacks callbacks) {
        zza(callbacks);
        zza(2, null);
    }

    public void disconnect() {
        this.zzaau.incrementAndGet();
        synchronized (this.zzaao) {
            int size = this.zzaao.size();
            for (int i = 0; i < size; i++) {
                ((zzc) this.zzaao.get(i)).zznR();
            }
            this.zzaao.clear();
        }
        zza(1, null);
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        synchronized (this.zzqt) {
            int i = this.zzaaq;
            IInterface iInterface = this.zzaan;
        }
        writer.append(prefix).append("mConnectState=");
        switch (i) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                writer.print("DISCONNECTED");
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                writer.print("CONNECTING");
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                writer.print("CONNECTED");
                break;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                writer.print("DISCONNECTING");
                break;
            default:
                writer.print("UNKNOWN");
                break;
        }
        writer.append(" mService=");
        if (iInterface == null) {
            writer.println("null");
        } else {
            writer.append(getServiceDescriptor()).append("@").println(Integer.toHexString(System.identityHashCode(iInterface.asBinder())));
        }
    }

    public final Context getContext() {
        return this.mContext;
    }

    public final Looper getLooper() {
        return this.zzWt;
    }

    public void getRemoteService(IAccountAccessor authedAccountAccessor, Set<Scope> scopes) {
        try {
            GetServiceRequest zzf = new GetServiceRequest(this.zzaat).zzcb(this.mContext.getPackageName()).zzf(zzkR());
            if (scopes != null) {
                zzf.zzb((Collection) scopes);
            }
            if (requiresSignIn()) {
                zzf.zzb(zznt()).zzb(authedAccountAccessor);
            } else if (requiresAccount()) {
                zzf.zzb(this.zzMY);
            }
            this.zzaal.zza(new zzd(this, this.zzaau.get()), zzf);
        } catch (DeadObjectException e) {
            Log.w("GmsClient", "service died");
            zzbs(1);
        } catch (Throwable e2) {
            Log.w("GmsClient", "Remote exception occurred", e2);
        }
    }

    protected abstract String getServiceDescriptor();

    protected abstract String getStartServiceAction();

    public boolean isConnected() {
        boolean z;
        synchronized (this.zzqt) {
            z = this.zzaaq == 3;
        }
        return z;
    }

    public boolean isConnecting() {
        boolean z;
        synchronized (this.zzqt) {
            z = this.zzaaq == 2;
        }
        return z;
    }

    protected void onConnectionFailed(ConnectionResult result) {
    }

    protected void onConnectionSuspended(int cause) {
    }

    public boolean requiresAccount() {
        return false;
    }

    public boolean requiresSignIn() {
        return false;
    }

    public void validateAccount(IAccountAccessor resolvedAccountAccessor) {
        try {
            this.zzaal.zza(new zzd(this, this.zzaau.get()), new ValidateAccountRequest(resolvedAccountAccessor, (Scope[]) this.zzWJ.toArray(new Scope[this.zzWJ.size()]), this.mContext.getPackageName(), zznN()));
        } catch (DeadObjectException e) {
            Log.w("GmsClient", "service died");
            zzbs(1);
        } catch (Throwable e2) {
            Log.w("GmsClient", "Remote exception occurred", e2);
        }
    }

    protected abstract T zzT(IBinder iBinder);

    protected Set<Scope> zza(Set<Scope> set) {
        return set;
    }

    protected void zza(int i, Bundle bundle, int i2) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(5, i2, -1, new zzi(this, i, bundle)));
    }

    protected void zza(int i, IBinder iBinder, Bundle bundle, int i2) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(1, i2, -1, new zzg(this, i, iBinder, bundle)));
    }

    @Deprecated
    public final void zza(zzc<?> com_google_android_gms_common_internal_zzi_zzc_) {
        synchronized (this.zzaao) {
            this.zzaao.add(com_google_android_gms_common_internal_zzi_zzc_);
        }
        this.mHandler.sendMessage(this.mHandler.obtainMessage(2, this.zzaau.get(), -1, com_google_android_gms_common_internal_zzi_zzc_));
    }

    public void zzbs(int i) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(4, this.zzaau.get(), i));
    }

    protected void zzbt(int i) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(6, i, -1, new zzh(this)));
    }

    protected String zzkQ() {
        return this.zzXa.zznz();
    }

    protected Bundle zzkR() {
        return new Bundle();
    }

    public Bundle zzlM() {
        return null;
    }

    protected void zznG() {
    }

    public void zznJ() {
        int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.mContext);
        if (isGooglePlayServicesAvailable != 0) {
            zza(1, null);
            this.zzaam = new zzf(this);
            this.mHandler.sendMessage(this.mHandler.obtainMessage(3, this.zzaau.get(), isGooglePlayServicesAvailable));
            return;
        }
        connect(new zzf(this));
    }

    protected final zze zznK() {
        return this.zzXa;
    }

    protected final void zznL() {
        if (!isConnected()) {
            throw new IllegalStateException("Not connected. Call connect() and wait for onConnected() to be called.");
        }
    }

    public final T zznM() throws DeadObjectException {
        T t;
        synchronized (this.zzqt) {
            if (this.zzaaq == 4) {
                throw new DeadObjectException();
            }
            zznL();
            zzu.zza(this.zzaan != null, (Object) "Client is connected but service is null");
            t = this.zzaan;
        }
        return t;
    }

    protected Bundle zznN() {
        return null;
    }

    public final Account zznt() {
        return this.zzMY != null ? this.zzMY : new Account("<<default account>>", GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
    }
}
