package com.google.android.gms.common.api;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzu;

public final class Scope implements SafeParcelable {
    public static final Creator<Scope> CREATOR = new zzj();
    final int zzCY;
    private final String zzXO;

    Scope(int versionCode, String scopeUri) {
        zzu.zzh(scopeUri, "scopeUri must not be null or empty");
        this.zzCY = versionCode;
        this.zzXO = scopeUri;
    }

    public Scope(String scopeUri) {
        this(1, scopeUri);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return !(o instanceof Scope) ? false : this.zzXO.equals(((Scope) o).zzXO);
    }

    public int hashCode() {
        return this.zzXO.hashCode();
    }

    public String toString() {
        return this.zzXO;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzj.zza(this, dest, flags);
    }

    public String zzmS() {
        return this.zzXO;
    }
}
