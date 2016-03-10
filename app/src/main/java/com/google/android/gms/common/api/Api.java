package com.google.android.gms.common.api;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionProgressReportCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.IAccountAccessor;
import com.google.android.gms.common.internal.zze;
import com.google.android.gms.common.internal.zzu;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class Api<O extends ApiOptions> {
    private final String mName;
    private final ClientKey<?> zzVu;
    private final zza<?, O> zzWi;
    private final zzc<?, O> zzWj = null;
    private final zzd<?> zzWk;
    private final ArrayList<Scope> zzWl;

    public interface zza<T extends Client, O> {
        int getPriority();

        T zza(Context context, Looper looper, zze com_google_android_gms_common_internal_zze, O o, ConnectionCallbacks connectionCallbacks, OnConnectionFailedListener onConnectionFailedListener);
    }

    public interface ApiOptions {

        public interface HasOptions extends ApiOptions {
        }

        public interface NotRequiredOptions extends ApiOptions {
        }

        public interface Optional extends HasOptions, NotRequiredOptions {
        }

        public static final class NoOptions implements NotRequiredOptions {
            private NoOptions() {
            }
        }
    }

    public interface Client {
        void connect(ConnectionProgressReportCallbacks connectionProgressReportCallbacks);

        void disconnect();

        void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr);

        void getRemoteService(IAccountAccessor iAccountAccessor, Set<Scope> set);

        boolean isConnected();

        boolean isConnecting();

        boolean requiresAccount();

        boolean requiresSignIn();

        void validateAccount(IAccountAccessor iAccountAccessor);
    }

    public static final class ClientKey<C extends Client> {
    }

    public interface zzb<T extends IInterface> {
        String getServiceDescriptor();

        String getStartServiceAction();

        T zzT(IBinder iBinder);
    }

    public interface zzc<T extends zzb, O> {
        T zzl(O o);

        int zzmu();
    }

    public static final class zzd<C extends zzb> {
    }

    public <C extends Client> Api(String name, zza<C, O> clientBuilder, ClientKey<C> clientKey, Scope... impliedScopes) {
        zzu.zzb((Object) clientBuilder, (Object) "Cannot construct an Api with a null ClientBuilder");
        zzu.zzb((Object) clientKey, (Object) "Cannot construct an Api with a null ClientKey");
        this.mName = name;
        this.zzWi = clientBuilder;
        this.zzVu = clientKey;
        this.zzWk = null;
        this.zzWl = new ArrayList(Arrays.asList(impliedScopes));
    }

    public String getName() {
        return this.mName;
    }

    public zza<?, O> zzmp() {
        zzu.zza(this.zzWi != null, (Object) "This API was constructed with a SimpleClientBuilder. Use getSimpleClientBuilder");
        return this.zzWi;
    }

    public zzc<?, O> zzmq() {
        zzu.zza(this.zzWj != null, (Object) "This API was constructed with a ClientBuilder. Use getClientBuilder");
        return this.zzWj;
    }

    public List<Scope> zzmr() {
        return this.zzWl;
    }

    public ClientKey<?> zzms() {
        zzu.zza(this.zzVu != null, (Object) "This API was constructed with a SimpleClientKey. Use getSimpleClientKey");
        return this.zzVu;
    }

    public boolean zzmt() {
        return this.zzWk != null;
    }
}
