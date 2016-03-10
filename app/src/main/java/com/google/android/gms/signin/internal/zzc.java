package com.google.android.gms.signin.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.List;

public class zzc implements Creator<CheckServerAuthResult> {
    static void zza(CheckServerAuthResult checkServerAuthResult, Parcel parcel, int i) {
        int zzac = zzb.zzac(parcel);
        zzb.zzc(parcel, 1, checkServerAuthResult.zzCY);
        zzb.zza(parcel, 2, checkServerAuthResult.zzaJY);
        zzb.zzc(parcel, 3, checkServerAuthResult.zzaJZ, false);
        zzb.zzH(parcel, zzac);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return zzfZ(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return zziP(x0);
    }

    public CheckServerAuthResult zzfZ(Parcel parcel) {
        boolean z = false;
        int zzab = zza.zzab(parcel);
        List list = null;
        int i = 0;
        while (parcel.dataPosition() < zzab) {
            int zzaa = zza.zzaa(parcel);
            switch (zza.zzbA(zzaa)) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    i = zza.zzg(parcel, zzaa);
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    z = zza.zzc(parcel, zzaa);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    list = zza.zzc(parcel, zzaa, Scope.CREATOR);
                    break;
                default:
                    zza.zzb(parcel, zzaa);
                    break;
            }
        }
        if (parcel.dataPosition() == zzab) {
            return new CheckServerAuthResult(i, z, list);
        }
        throw new zza.zza("Overread allowed size end=" + zzab, parcel);
    }

    public CheckServerAuthResult[] zziP(int i) {
        return new CheckServerAuthResult[i];
    }
}
