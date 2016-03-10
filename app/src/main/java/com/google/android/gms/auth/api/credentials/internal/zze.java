package com.google.android.gms.auth.api.credentials.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.common.internal.safeparcel.zza;
import com.google.android.gms.common.internal.safeparcel.zzb;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class zze implements Creator<DeleteRequest> {
    static void zza(DeleteRequest deleteRequest, Parcel parcel, int i) {
        int zzac = zzb.zzac(parcel);
        zzb.zza(parcel, 1, deleteRequest.getCredential(), i, false);
        zzb.zzc(parcel, Constants.UPDATE_COFIG_INTERVAL, deleteRequest.zzCY);
        zzb.zzH(parcel, zzac);
    }

    public /* synthetic */ Object createFromParcel(Parcel x0) {
        return zzF(x0);
    }

    public /* synthetic */ Object[] newArray(int x0) {
        return zzav(x0);
    }

    public DeleteRequest zzF(Parcel parcel) {
        int zzab = zza.zzab(parcel);
        int i = 0;
        Credential credential = null;
        while (parcel.dataPosition() < zzab) {
            int zzaa = zza.zzaa(parcel);
            switch (zza.zzbA(zzaa)) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    credential = (Credential) zza.zza(parcel, zzaa, Credential.CREATOR);
                    break;
                case Constants.UPDATE_COFIG_INTERVAL /*1000*/:
                    i = zza.zzg(parcel, zzaa);
                    break;
                default:
                    zza.zzb(parcel, zzaa);
                    break;
            }
        }
        if (parcel.dataPosition() == zzab) {
            return new DeleteRequest(i, credential);
        }
        throw new zza.zza("Overread allowed size end=" + zzab, parcel);
    }

    public DeleteRequest[] zzav(int i) {
        return new DeleteRequest[i];
    }
}
