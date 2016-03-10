package com.google.android.gms.auth.api.credentials;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public final class IdToken implements SafeParcelable {
    public static final Creator<IdToken> CREATOR = new zzc();
    final int zzCY;
    private final String zzOX;
    private final String zzPa;

    IdToken(int version, String accountType, String idToken) {
        this.zzCY = version;
        this.zzOX = accountType;
        this.zzPa = idToken;
    }

    public int describeContents() {
        return 0;
    }

    public String getAccountType() {
        return this.zzOX;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzc.zza(this, out, flags);
    }

    public String zzlc() {
        return this.zzPa;
    }
}
