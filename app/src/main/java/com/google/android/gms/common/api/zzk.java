package com.google.android.gms.common.api;

import android.app.PendingIntent;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.mobcrush.mobcrush.Constants;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class zzk implements Creator<Status> {
    static void zza(Status status, Parcel parcel, int i) {
        int zzac = zzb.zzac(parcel);
        zzb.zzc(parcel, 1, status.getStatusCode());
        zzb.zzc(parcel, Constants.UPDATE_COFIG_INTERVAL, status.getVersionCode());
        zzb.zza(parcel, 2, status.getStatusMessage(), false);
        zzb.zza(parcel, 3, status.zzmT(), i, false);
        zzb.zzH(parcel, zzac);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return zzQ(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return zzba(x0);
    }

    public Status zzQ(Parcel parcel) {
        PendingIntent pendingIntent = null;
        int i = 0;
        int zzab = zza.zzab(parcel);
        String str = null;
        int i2 = 0;
        while (parcel.dataPosition() < zzab) {
            int zzaa = zza.zzaa(parcel);
            switch (zza.zzbA(zzaa)) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    i = zza.zzg(parcel, zzaa);
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    str = zza.zzo(parcel, zzaa);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    pendingIntent = (PendingIntent) zza.zza(parcel, zzaa, PendingIntent.CREATOR);
                    break;
                case Constants.UPDATE_COFIG_INTERVAL /*1000*/:
                    i2 = zza.zzg(parcel, zzaa);
                    break;
                default:
                    zza.zzb(parcel, zzaa);
                    break;
            }
        }
        if (parcel.dataPosition() == zzab) {
            return new Status(i2, i, str, pendingIntent);
        }
        throw new zza.zza("Overread allowed size end=" + zzab, parcel);
    }

    public Status[] zzba(int i) {
        return new Status[i];
    }
}
