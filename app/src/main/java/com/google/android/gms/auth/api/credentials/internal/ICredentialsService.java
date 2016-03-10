package com.google.android.gms.auth.api.credentials.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.auth.api.credentials.CredentialRequest;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public interface ICredentialsService extends IInterface {

    public static abstract class zza extends Binder implements ICredentialsService {

        private static class zza implements ICredentialsService {
            private IBinder zznF;

            zza(IBinder iBinder) {
                this.zznF = iBinder;
            }

            public IBinder asBinder() {
                return this.zznF;
            }

            public void performCredentialsDeleteOperation(ICredentialsCallbacks callbacks, DeleteRequest request) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    obtain.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (request != null) {
                        obtain.writeInt(1);
                        request.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.zznF.transact(3, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void performCredentialsRequestOperation(ICredentialsCallbacks callbacks, CredentialRequest request) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    obtain.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (request != null) {
                        obtain.writeInt(1);
                        request.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.zznF.transact(1, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void performCredentialsSaveOperation(ICredentialsCallbacks callbacks, SaveRequest request) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    obtain.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    if (request != null) {
                        obtain.writeInt(1);
                        request.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    this.zznF.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void performDisableAutoSignInOperation(ICredentialsCallbacks callbacks) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    obtain.writeStrongBinder(callbacks != null ? callbacks.asBinder() : null);
                    this.zznF.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static ICredentialsService zzaq(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof ICredentialsService)) ? new zza(iBinder) : (ICredentialsService) queryLocalInterface;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            DeleteRequest deleteRequest = null;
            ICredentialsCallbacks zzap;
            switch (code) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    CredentialRequest credentialRequest;
                    data.enforceInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    zzap = com.google.android.gms.auth.api.credentials.internal.ICredentialsCallbacks.zza.zzap(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        credentialRequest = (CredentialRequest) CredentialRequest.CREATOR.createFromParcel(data);
                    }
                    performCredentialsRequestOperation(zzap, credentialRequest);
                    reply.writeNoException();
                    return true;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    SaveRequest saveRequest;
                    data.enforceInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    zzap = com.google.android.gms.auth.api.credentials.internal.ICredentialsCallbacks.zza.zzap(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        saveRequest = (SaveRequest) SaveRequest.CREATOR.createFromParcel(data);
                    }
                    performCredentialsSaveOperation(zzap, saveRequest);
                    reply.writeNoException();
                    return true;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    data.enforceInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    zzap = com.google.android.gms.auth.api.credentials.internal.ICredentialsCallbacks.zza.zzap(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        deleteRequest = (DeleteRequest) DeleteRequest.CREATOR.createFromParcel(data);
                    }
                    performCredentialsDeleteOperation(zzap, deleteRequest);
                    reply.writeNoException();
                    return true;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    data.enforceInterface("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    performDisableAutoSignInOperation(com.google.android.gms.auth.api.credentials.internal.ICredentialsCallbacks.zza.zzap(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.auth.api.credentials.internal.ICredentialsService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void performCredentialsDeleteOperation(ICredentialsCallbacks iCredentialsCallbacks, DeleteRequest deleteRequest) throws RemoteException;

    void performCredentialsRequestOperation(ICredentialsCallbacks iCredentialsCallbacks, CredentialRequest credentialRequest) throws RemoteException;

    void performCredentialsSaveOperation(ICredentialsCallbacks iCredentialsCallbacks, SaveRequest saveRequest) throws RemoteException;

    void performDisableAutoSignInOperation(ICredentialsCallbacks iCredentialsCallbacks) throws RemoteException;
}
