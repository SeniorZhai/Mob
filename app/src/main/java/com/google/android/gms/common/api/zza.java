package com.google.android.gms.common.api;

import android.os.DeadObjectException;
import android.os.RemoteException;
import com.google.android.gms.common.api.Api.Client;
import com.google.android.gms.common.api.Api.ClientKey;
import com.google.android.gms.common.internal.zzu;
import java.util.concurrent.atomic.AtomicReference;

public class zza {

    public interface zzb<R> {
        void zzm(R r);

        void zzr(Status status);
    }

    public static abstract class zza<R extends Result, A extends Client> extends AbstractPendingResult<R> implements zzb<R>, zze<A> {
        private final ClientKey<A> zzVu;
        private AtomicReference<zzc> zzWm = new AtomicReference();

        protected zza(ClientKey<A> clientKey, GoogleApiClient googleApiClient) {
            super(((GoogleApiClient) zzu.zzb((Object) googleApiClient, (Object) "GoogleApiClient must not be null")).getLooper());
            this.zzVu = (ClientKey) zzu.zzu(clientKey);
        }

        private void zza(RemoteException remoteException) {
            zzr(new Status(8, remoteException.getLocalizedMessage(), null));
        }

        protected void onResultConsumed() {
            zzc com_google_android_gms_common_api_zzg_zzc = (zzc) this.zzWm.getAndSet(null);
            if (com_google_android_gms_common_api_zzg_zzc != null) {
                com_google_android_gms_common_api_zzg_zzc.zzc(this);
            }
        }

        protected abstract void zza(A a) throws RemoteException;

        public void zza(zzc com_google_android_gms_common_api_zzg_zzc) {
            this.zzWm.set(com_google_android_gms_common_api_zzg_zzc);
        }

        public final void zzb(A a) throws DeadObjectException {
            try {
                zza((Client) a);
            } catch (RemoteException e) {
                zza(e);
                throw e;
            } catch (RemoteException e2) {
                zza(e2);
            }
        }

        public /* synthetic */ void zzm(Object obj) {
            super.setResult((Result) obj);
        }

        public final ClientKey<A> zzms() {
            return this.zzVu;
        }

        public int zzmv() {
            return 0;
        }

        public final void zzr(Status status) {
            zzu.zzb(!status.isSuccess(), (Object) "Failed result must not be success");
            setResult(createFailedResult(status));
        }
    }
}
