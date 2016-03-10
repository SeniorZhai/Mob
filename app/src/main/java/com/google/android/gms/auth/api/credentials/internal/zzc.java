package com.google.android.gms.auth.api.credentials.internal;

import android.content.Context;
import android.os.RemoteException;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.auth.api.credentials.CredentialsApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.zza.zzb;

public final class zzc implements CredentialsApi {

    private static class zza extends zza {
        private zzb<Status> zzPg;

        zza(zzb<Status> com_google_android_gms_common_api_zza_zzb_com_google_android_gms_common_api_Status) {
            this.zzPg = com_google_android_gms_common_api_zza_zzb_com_google_android_gms_common_api_Status;
        }

        public void onStatusResult(Status status) {
            this.zzPg.zzm(status);
        }
    }

    public PendingResult<Status> delete(GoogleApiClient client, final Credential credential) {
        return client.zzb(new zzd<Status>(this, client) {
            final /* synthetic */ zzc zzPd;

            protected /* synthetic */ Result createFailedResult(Status x0) {
                return zzb(x0);
            }

            protected void zza(Context context, ICredentialsService iCredentialsService) throws RemoteException {
                iCredentialsService.performCredentialsDeleteOperation(new zza(this), new DeleteRequest(credential));
            }

            protected Status zzb(Status status) {
                return status;
            }
        });
    }

    public PendingResult<Status> disableAutoSignIn(GoogleApiClient client) {
        return client.zzb(new zzd<Status>(this, client) {
            final /* synthetic */ zzc zzPd;

            protected /* synthetic */ Result createFailedResult(Status x0) {
                return zzb(x0);
            }

            protected void zza(Context context, ICredentialsService iCredentialsService) throws RemoteException {
                iCredentialsService.performDisableAutoSignInOperation(new zza(this));
            }

            protected Status zzb(Status status) {
                return status;
            }
        });
    }

    public PendingResult<CredentialRequestResult> request(GoogleApiClient client, final CredentialRequest request) {
        return client.zza(new zzd<CredentialRequestResult>(this, client) {
            final /* synthetic */ zzc zzPd;

            protected /* synthetic */ Result createFailedResult(Status x0) {
                return zzk(x0);
            }

            protected void zza(Context context, ICredentialsService iCredentialsService) throws RemoteException {
                iCredentialsService.performCredentialsRequestOperation(new zza(this) {
                    final /* synthetic */ AnonymousClass1 zzPe;

                    {
                        this.zzPe = r1;
                    }

                    public void onCredentialResult(Status status, Credential credential) {
                        this.zzPe.setResult(new zzb(status, credential));
                    }
                }, request);
            }

            protected CredentialRequestResult zzk(Status status) {
                return zzb.zzj(status);
            }
        });
    }

    public PendingResult<Status> save(GoogleApiClient client, final Credential credential) {
        return client.zzb(new zzd<Status>(this, client) {
            final /* synthetic */ zzc zzPd;

            protected /* synthetic */ Result createFailedResult(Status x0) {
                return zzb(x0);
            }

            protected void zza(Context context, ICredentialsService iCredentialsService) throws RemoteException {
                iCredentialsService.performCredentialsSaveOperation(new zza(this), new SaveRequest(credential));
            }

            protected Status zzb(Status status) {
                return status;
            }
        });
    }
}
