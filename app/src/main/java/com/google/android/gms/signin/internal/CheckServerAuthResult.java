package com.google.android.gms.signin.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class CheckServerAuthResult implements SafeParcelable {
    public static final Creator<CheckServerAuthResult> CREATOR = new zzc();
    final int zzCY;
    final boolean zzaJY;
    final List<Scope> zzaJZ;

    CheckServerAuthResult(int versionCode, boolean newAuthCodeRequired, List<Scope> additionalScopes) {
        this.zzCY = versionCode;
        this.zzaJY = newAuthCodeRequired;
        this.zzaJZ = additionalScopes;
    }

    public CheckServerAuthResult(boolean newAuthCodeRequired, Set<Scope> additionalScopes) {
        this(1, newAuthCodeRequired, zzf(additionalScopes));
    }

    private static List<Scope> zzf(Set<Scope> set) {
        return set == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList(set));
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzc.zza(this, dest, flags);
    }
}
