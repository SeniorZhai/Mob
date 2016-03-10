package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class zzh implements Creator<GetServiceRequest> {
    static void zza(GetServiceRequest getServiceRequest, Parcel parcel, int i) {
        int zzac = zzb.zzac(parcel);
        zzb.zzc(parcel, 1, getServiceRequest.version);
        zzb.zzc(parcel, 2, getServiceRequest.zzaad);
        zzb.zzc(parcel, 3, getServiceRequest.zzaae);
        zzb.zza(parcel, 4, getServiceRequest.zzaaf, false);
        zzb.zza(parcel, 5, getServiceRequest.zzaag, false);
        zzb.zza(parcel, 6, getServiceRequest.zzaah, i, false);
        zzb.zza(parcel, 7, getServiceRequest.zzaai, false);
        zzb.zza(parcel, 8, getServiceRequest.zzaaj, i, false);
        zzb.zzH(parcel, zzac);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return zzW(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return zzbr(x0);
    }

    public GetServiceRequest zzW(Parcel parcel) {
        int i = 0;
        Account account = null;
        int zzab = zza.zzab(parcel);
        Bundle bundle = null;
        Scope[] scopeArr = null;
        IBinder iBinder = null;
        String str = null;
        int i2 = 0;
        int i3 = 0;
        while (parcel.dataPosition() < zzab) {
            int zzaa = zza.zzaa(parcel);
            switch (zza.zzbA(zzaa)) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    i3 = zza.zzg(parcel, zzaa);
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    i2 = zza.zzg(parcel, zzaa);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    i = zza.zzg(parcel, zzaa);
                    break;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    str = zza.zzo(parcel, zzaa);
                    break;
                case Player.STATE_ENDED /*5*/:
                    iBinder = zza.zzp(parcel, zzaa);
                    break;
                case R.styleable.Toolbar_contentInsetEnd /*6*/:
                    scopeArr = (Scope[]) zza.zzb(parcel, zzaa, Scope.CREATOR);
                    break;
                case DayPickerView.DAYS_PER_WEEK /*7*/:
                    bundle = zza.zzq(parcel, zzaa);
                    break;
                case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                    account = (Account) zza.zza(parcel, zzaa, Account.CREATOR);
                    break;
                default:
                    zza.zzb(parcel, zzaa);
                    break;
            }
        }
        if (parcel.dataPosition() == zzab) {
            return new GetServiceRequest(i3, i2, i, str, iBinder, scopeArr, bundle, account);
        }
        throw new zza.zza("Overread allowed size end=" + zzab, parcel);
    }

    public GetServiceRequest[] zzbr(int i) {
        return new GetServiceRequest[i];
    }
}
