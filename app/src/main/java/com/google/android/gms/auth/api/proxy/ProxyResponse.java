package com.google.android.gms.auth.api.proxy;

import android.app.PendingIntent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class ProxyResponse implements SafeParcelable {
    public static final Creator<ProxyResponse> CREATOR = new zzc();
    final int versionCode;
    public final byte[] zzPs;
    final Bundle zzPt;
    public final int zzPu;
    public final PendingIntent zzPv;
    public final int zzPw;

    ProxyResponse(int version, int googlePlayServicesStatusCode, PendingIntent recoveryAction, int httpCode, Bundle headers, byte[] body) {
        this.versionCode = version;
        this.zzPu = googlePlayServicesStatusCode;
        this.zzPw = httpCode;
        this.zzPt = headers;
        this.zzPs = body;
        this.zzPv = recoveryAction;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flags) {
        zzc.zza(this, parcel, flags);
    }
}
