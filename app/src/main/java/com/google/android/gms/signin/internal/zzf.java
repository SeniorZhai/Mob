package com.google.android.gms.signin.internal;

import android.accounts.Account;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.common.internal.AuthAccountRequest;
import com.google.android.gms.common.internal.IAccountAccessor;
import com.google.android.gms.common.internal.ResolveAccountRequest;
import com.google.android.gms.common.internal.zzq;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import org.apache.http.protocol.HTTP;

public interface zzf extends IInterface {

    public static abstract class zza extends Binder implements zzf {

        private static class zza implements zzf {
            private IBinder zznF;

            zza(IBinder iBinder) {
                this.zznF = iBinder;
            }

            public IBinder asBinder() {
                return this.zznF;
            }

            public void zza(int i, Account account, zze com_google_android_gms_signin_internal_zze) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInService");
                    obtain.writeInt(i);
                    if (account != null) {
                        obtain.writeInt(1);
                        account.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(com_google_android_gms_signin_internal_zze != null ? com_google_android_gms_signin_internal_zze.asBinder() : null);
                    this.zznF.transact(8, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(AuthAccountRequest authAccountRequest, zze com_google_android_gms_signin_internal_zze) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInService");
                    if (authAccountRequest != null) {
                        obtain.writeInt(1);
                        authAccountRequest.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(com_google_android_gms_signin_internal_zze != null ? com_google_android_gms_signin_internal_zze.asBinder() : null);
                    this.zznF.transact(2, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(IAccountAccessor iAccountAccessor, int i, boolean z) throws RemoteException {
                int i2 = 0;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInService");
                    obtain.writeStrongBinder(iAccountAccessor != null ? iAccountAccessor.asBinder() : null);
                    obtain.writeInt(i);
                    if (z) {
                        i2 = 1;
                    }
                    obtain.writeInt(i2);
                    this.zznF.transact(9, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(ResolveAccountRequest resolveAccountRequest, zzq com_google_android_gms_common_internal_zzq) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInService");
                    if (resolveAccountRequest != null) {
                        obtain.writeInt(1);
                        resolveAccountRequest.writeToParcel(obtain, 0);
                    } else {
                        obtain.writeInt(0);
                    }
                    obtain.writeStrongBinder(com_google_android_gms_common_internal_zzq != null ? com_google_android_gms_common_internal_zzq.asBinder() : null);
                    this.zznF.transact(5, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zza(CheckServerAuthResult checkServerAuthResult) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInService");
                    if (checkServerAuthResult != null) {
                        obtain.writeInt(1);
                        checkServerAuthResult.writeToParcel(obtain, 0);
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

            public void zzal(boolean z) throws RemoteException {
                int i = 0;
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInService");
                    if (z) {
                        i = 1;
                    }
                    obtain.writeInt(i);
                    this.zznF.transact(4, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }

            public void zziQ(int i) throws RemoteException {
                Parcel obtain = Parcel.obtain();
                Parcel obtain2 = Parcel.obtain();
                try {
                    obtain.writeInterfaceToken("com.google.android.gms.signin.internal.ISignInService");
                    obtain.writeInt(i);
                    this.zznF.transact(7, obtain, obtain2, 0);
                    obtain2.readException();
                } finally {
                    obtain2.recycle();
                    obtain.recycle();
                }
            }
        }

        public static zzf zzdD(IBinder iBinder) {
            if (iBinder == null) {
                return null;
            }
            IInterface queryLocalInterface = iBinder.queryLocalInterface("com.google.android.gms.signin.internal.ISignInService");
            return (queryLocalInterface == null || !(queryLocalInterface instanceof zzf)) ? new zza(iBinder) : (zzf) queryLocalInterface;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean z = false;
            Account account = null;
            switch (code) {
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    AuthAccountRequest authAccountRequest;
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInService");
                    if (data.readInt() != 0) {
                        authAccountRequest = (AuthAccountRequest) AuthAccountRequest.CREATOR.createFromParcel(data);
                    }
                    zza(authAccountRequest, com.google.android.gms.signin.internal.zze.zza.zzdC(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    CheckServerAuthResult checkServerAuthResult;
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInService");
                    if (data.readInt() != 0) {
                        checkServerAuthResult = (CheckServerAuthResult) CheckServerAuthResult.CREATOR.createFromParcel(data);
                    }
                    zza(checkServerAuthResult);
                    reply.writeNoException();
                    return true;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInService");
                    zzal(data.readInt() != 0);
                    reply.writeNoException();
                    return true;
                case Player.STATE_ENDED /*5*/:
                    ResolveAccountRequest resolveAccountRequest;
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInService");
                    if (data.readInt() != 0) {
                        resolveAccountRequest = (ResolveAccountRequest) ResolveAccountRequest.CREATOR.createFromParcel(data);
                    }
                    zza(resolveAccountRequest, com.google.android.gms.common.internal.zzq.zza.zzaH(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case DayPickerView.DAYS_PER_WEEK /*7*/:
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInService");
                    zziQ(data.readInt());
                    reply.writeNoException();
                    return true;
                case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInService");
                    int readInt = data.readInt();
                    if (data.readInt() != 0) {
                        account = (Account) Account.CREATOR.createFromParcel(data);
                    }
                    zza(readInt, account, com.google.android.gms.signin.internal.zze.zza.zzdC(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case HTTP.HT /*9*/:
                    data.enforceInterface("com.google.android.gms.signin.internal.ISignInService");
                    IAccountAccessor zzaD = com.google.android.gms.common.internal.IAccountAccessor.zza.zzaD(data.readStrongBinder());
                    int readInt2 = data.readInt();
                    if (data.readInt() != 0) {
                        z = true;
                    }
                    zza(zzaD, readInt2, z);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.signin.internal.ISignInService");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(int i, Account account, zze com_google_android_gms_signin_internal_zze) throws RemoteException;

    void zza(AuthAccountRequest authAccountRequest, zze com_google_android_gms_signin_internal_zze) throws RemoteException;

    void zza(IAccountAccessor iAccountAccessor, int i, boolean z) throws RemoteException;

    void zza(ResolveAccountRequest resolveAccountRequest, zzq com_google_android_gms_common_internal_zzq) throws RemoteException;

    void zza(CheckServerAuthResult checkServerAuthResult) throws RemoteException;

    void zzal(boolean z) throws RemoteException;

    void zziQ(int i) throws RemoteException;
}
