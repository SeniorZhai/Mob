package com.google.android.gms.common.internal;

import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.internal.IAccountAccessor.zza;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class ResolveAccountResponse implements SafeParcelable {
    public static final Creator<ResolveAccountResponse> CREATOR = new zzw();
    final int zzCY;
    private boolean zzWY;
    private ConnectionResult zzYh;
    IBinder zzZO;
    private boolean zzabd;

    public ResolveAccountResponse(int connectionResultStatusCode) {
        this(new ConnectionResult(connectionResultStatusCode, null));
    }

    ResolveAccountResponse(int versionCode, IBinder accountAccessorBinder, ConnectionResult connectionResult, boolean saveDefaultAccount, boolean isFromCrossClientAuth) {
        this.zzCY = versionCode;
        this.zzZO = accountAccessorBinder;
        this.zzYh = connectionResult;
        this.zzWY = saveDefaultAccount;
        this.zzabd = isFromCrossClientAuth;
    }

    public ResolveAccountResponse(ConnectionResult result) {
        this(1, null, result, false, false);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ResolveAccountResponse)) {
            return false;
        }
        ResolveAccountResponse resolveAccountResponse = (ResolveAccountResponse) o;
        return this.zzYh.equals(resolveAccountResponse.zzYh) && zznZ().equals(resolveAccountResponse.zznZ());
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzw.zza(this, dest, flags);
    }

    public IAccountAccessor zznZ() {
        return zza.zzaD(this.zzZO);
    }

    public ConnectionResult zzoa() {
        return this.zzYh;
    }

    public boolean zzob() {
        return this.zzWY;
    }

    public boolean zzoc() {
        return this.zzabd;
    }
}
