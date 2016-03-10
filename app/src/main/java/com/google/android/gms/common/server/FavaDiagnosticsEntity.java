package com.google.android.gms.common.server;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class FavaDiagnosticsEntity implements SafeParcelable {
    public static final zza CREATOR = new zza();
    final int zzCY;
    public final String zzaby;
    public final int zzabz;

    public FavaDiagnosticsEntity(int versionCode, String namespace, int typeNum) {
        this.zzCY = versionCode;
        this.zzaby = namespace;
        this.zzabz = typeNum;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }
}
