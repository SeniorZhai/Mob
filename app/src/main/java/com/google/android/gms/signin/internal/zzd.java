package com.google.android.gms.signin.internal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.google.android.gms.common.api.Scope;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.util.List;

public interface zzd extends IInterface {

    public static abstract class zza extends Binder implements zzd {
        public zza() {
            attachInterface(this, "com.google.android.gms.signin.internal.IOfflineAccessCallbacks");
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    data.enforceInterface("com.google.android.gms.signin.internal.IOfflineAccessCallbacks");
                    zza(data.readString(), data.createTypedArrayList(Scope.CREATOR), com.google.android.gms.signin.internal.zzf.zza.zzdD(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    data.enforceInterface("com.google.android.gms.signin.internal.IOfflineAccessCallbacks");
                    zza(data.readString(), data.readString(), com.google.android.gms.signin.internal.zzf.zza.zzdD(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString("com.google.android.gms.signin.internal.IOfflineAccessCallbacks");
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void zza(String str, String str2, zzf com_google_android_gms_signin_internal_zzf) throws RemoteException;

    void zza(String str, List<Scope> list, zzf com_google_android_gms_signin_internal_zzf) throws RemoteException;
}
