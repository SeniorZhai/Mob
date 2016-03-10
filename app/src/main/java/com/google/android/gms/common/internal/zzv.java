package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class zzv implements Creator<ResolveAccountRequest> {
    static void zza(ResolveAccountRequest resolveAccountRequest, Parcel parcel, int i) {
        int zzac = zzb.zzac(parcel);
        zzb.zzc(parcel, 1, resolveAccountRequest.zzCY);
        zzb.zza(parcel, 2, resolveAccountRequest.getAccount(), i, false);
        zzb.zzc(parcel, 3, resolveAccountRequest.getSessionId());
        zzb.zzH(parcel, zzac);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return zzX(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return zzbx(x0);
    }

    public ResolveAccountRequest zzX(Parcel parcel) {
        int i = 0;
        int zzab = zza.zzab(parcel);
        Account account = null;
        int i2 = 0;
        while (parcel.dataPosition() < zzab) {
            Account account2;
            int zzg;
            int zzaa = zza.zzaa(parcel);
            switch (zza.zzbA(zzaa)) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    int i3 = i;
                    account2 = account;
                    zzg = zza.zzg(parcel, zzaa);
                    zzaa = i3;
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    zzg = i2;
                    Account account3 = (Account) zza.zza(parcel, zzaa, Account.CREATOR);
                    zzaa = i;
                    account2 = account3;
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    zzaa = zza.zzg(parcel, zzaa);
                    account2 = account;
                    zzg = i2;
                    break;
                default:
                    zza.zzb(parcel, zzaa);
                    zzaa = i;
                    account2 = account;
                    zzg = i2;
                    break;
            }
            i2 = zzg;
            account = account2;
            i = zzaa;
        }
        if (parcel.dataPosition() == zzab) {
            return new ResolveAccountRequest(i2, account, i);
        }
        throw new zza.zza("Overread allowed size end=" + zzab, parcel);
    }

    public ResolveAccountRequest[] zzbx(int i) {
        return new ResolveAccountRequest[i];
    }
}
