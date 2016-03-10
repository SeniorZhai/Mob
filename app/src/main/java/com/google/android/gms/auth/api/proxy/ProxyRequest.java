package com.google.android.gms.auth.api.proxy;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class ProxyRequest implements SafeParcelable {
    public static final Creator<ProxyRequest> CREATOR = new zzb();
    public static final int zzPh = 0;
    public static final int zzPi = 1;
    public static final int zzPj = 2;
    public static final int zzPk = 3;
    public static final int zzPl = 4;
    public static final int zzPm = 5;
    public static final int zzPn = 6;
    public static final int zzPo = 7;
    public static final int zzPp = 7;
    final int versionCode;
    public final int zzPq;
    public final long zzPr;
    public final byte[] zzPs;
    Bundle zzPt;
    public final String zzzf;

    ProxyRequest(int version, String googleUrl, int httpMethod, long timeoutMillis, byte[] body, Bundle headers) {
        this.versionCode = version;
        this.zzzf = googleUrl;
        this.zzPq = httpMethod;
        this.zzPr = timeoutMillis;
        this.zzPs = body;
        this.zzPt = headers;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        return "ProxyRequest[ url: " + this.zzzf + ", method: " + this.zzPq + " ]";
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzb.zza(this, parcel, flags);
    }
}
