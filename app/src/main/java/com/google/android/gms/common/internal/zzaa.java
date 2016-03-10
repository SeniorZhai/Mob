package com.google.android.gms.common.internal;

import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class zzaa implements Creator<ValidateAccountRequest> {
    static void zza(ValidateAccountRequest validateAccountRequest, Parcel parcel, int i) {
        int zzac = zzb.zzac(parcel);
        zzb.zzc(parcel, 1, validateAccountRequest.zzCY);
        zzb.zzc(parcel, 2, validateAccountRequest.zzod());
        zzb.zza(parcel, 3, validateAccountRequest.zzZO, false);
        zzb.zza(parcel, 4, validateAccountRequest.zzoe(), i, false);
        zzb.zza(parcel, 5, validateAccountRequest.zzof(), false);
        zzb.zza(parcel, 6, validateAccountRequest.getCallingPackage(), false);
        zzb.zzH(parcel, zzac);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return zzZ(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return zzbz(x0);
    }

    public ValidateAccountRequest zzZ(Parcel parcel) {
        int i = 0;
        String str = null;
        int zzab = zza.zzab(parcel);
        Bundle bundle = null;
        Scope[] scopeArr = null;
        IBinder iBinder = null;
        int i2 = 0;
        while (parcel.dataPosition() < zzab) {
            int zzaa = zza.zzaa(parcel);
            switch (zza.zzbA(zzaa)) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    i2 = zza.zzg(parcel, zzaa);
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    i = zza.zzg(parcel, zzaa);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    iBinder = zza.zzp(parcel, zzaa);
                    break;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    scopeArr = (Scope[]) zza.zzb(parcel, zzaa, Scope.CREATOR);
                    break;
                case Player.STATE_ENDED /*5*/:
                    bundle = zza.zzq(parcel, zzaa);
                    break;
                case R.styleable.Toolbar_contentInsetEnd /*6*/:
                    str = zza.zzo(parcel, zzaa);
                    break;
                default:
                    zza.zzb(parcel, zzaa);
                    break;
            }
        }
        if (parcel.dataPosition() == zzab) {
            return new ValidateAccountRequest(i2, i, iBinder, scopeArr, bundle, str);
        }
        throw new zza.zza("Overread allowed size end=" + zzab, parcel);
    }

    public ValidateAccountRequest[] zzbz(int i) {
        return new ValidateAccountRequest[i];
    }
}
