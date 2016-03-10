package com.google.android.gms.auth.api.credentials.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public final class SaveRequest implements SafeParcelable {
    public static final Creator<SaveRequest> CREATOR = new zzf();
    final int zzCY;
    private final Credential zzPb;

    SaveRequest(int version, Credential credential) {
        this.zzCY = version;
        this.zzPb = credential;
    }

    public SaveRequest(Credential credential) {
        this(1, credential);
    }

    public int describeContents() {
        return 0;
    }

    public Credential getCredential() {
        return this.zzPb;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzf.zza(this, out, flags);
    }
}
