package com.google.android.gms.common.data;

import android.database.CursorWindow;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.mobcrush.mobcrush.Constants;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class zze implements Creator<DataHolder> {
    static void zza(DataHolder dataHolder, Parcel parcel, int i) {
        int zzac = zzb.zzac(parcel);
        zzb.zza(parcel, 1, dataHolder.zzng(), false);
        zzb.zzc(parcel, Constants.UPDATE_COFIG_INTERVAL, dataHolder.getVersionCode());
        zzb.zza(parcel, 2, dataHolder.zznh(), i, false);
        zzb.zzc(parcel, 3, dataHolder.getStatusCode());
        zzb.zza(parcel, 4, dataHolder.zznb(), false);
        zzb.zzH(parcel, zzac);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return zzS(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return zzbj(x0);
    }

    public DataHolder zzS(Parcel parcel) {
        int i = 0;
        Bundle bundle = null;
        int zzab = zza.zzab(parcel);
        CursorWindow[] cursorWindowArr = null;
        String[] strArr = null;
        int i2 = 0;
        while (parcel.dataPosition() < zzab) {
            int zzaa = zza.zzaa(parcel);
            switch (zza.zzbA(zzaa)) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    strArr = zza.zzA(parcel, zzaa);
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    cursorWindowArr = (CursorWindow[]) zza.zzb(parcel, zzaa, CursorWindow.CREATOR);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    i = zza.zzg(parcel, zzaa);
                    break;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    bundle = zza.zzq(parcel, zzaa);
                    break;
                case Constants.UPDATE_COFIG_INTERVAL /*1000*/:
                    i2 = zza.zzg(parcel, zzaa);
                    break;
                default:
                    zza.zzb(parcel, zzaa);
                    break;
            }
        }
        if (parcel.dataPosition() != zzab) {
            throw new zza.zza("Overread allowed size end=" + zzab, parcel);
        }
        DataHolder dataHolder = new DataHolder(i2, strArr, cursorWindowArr, i, bundle);
        dataHolder.zznf();
        return dataHolder;
    }

    public DataHolder[] zzbj(int i) {
        return new DataHolder[i];
    }
}
