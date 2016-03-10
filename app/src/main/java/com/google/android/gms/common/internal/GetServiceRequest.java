package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.internal.IAccountAccessor.zza;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import java.util.Collection;

public class GetServiceRequest implements SafeParcelable {
    public static final Creator<GetServiceRequest> CREATOR = new zzh();
    final int version;
    final int zzaad;
    int zzaae;
    String zzaaf;
    IBinder zzaag;
    Scope[] zzaah;
    Bundle zzaai;
    Account zzaaj;

    public GetServiceRequest(int serviceId) {
        this.version = 2;
        this.zzaae = GooglePlayServicesUtil.GOOGLE_PLAY_SERVICES_VERSION_CODE;
        this.zzaad = serviceId;
    }

    GetServiceRequest(int version, int serviceId, int clientVersion, String callingPackage, IBinder accountAccessorBinder, Scope[] scopes, Bundle extraArgs, Account clientRequestedAccount) {
        this.version = version;
        this.zzaad = serviceId;
        this.zzaae = clientVersion;
        this.zzaaf = callingPackage;
        if (version < 2) {
            this.zzaaj = zzaC(accountAccessorBinder);
        } else {
            this.zzaag = accountAccessorBinder;
            this.zzaaj = clientRequestedAccount;
        }
        this.zzaah = scopes;
        this.zzaai = extraArgs;
    }

    private Account zzaC(IBinder iBinder) {
        return iBinder != null ? zza.zza(zza.zzaD(iBinder)) : null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzh.zza(this, dest, flags);
    }

    public GetServiceRequest zzb(Account account) {
        this.zzaaj = account;
        return this;
    }

    public GetServiceRequest zzb(IAccountAccessor iAccountAccessor) {
        if (iAccountAccessor != null) {
            this.zzaag = iAccountAccessor.asBinder();
        }
        return this;
    }

    public GetServiceRequest zzb(Collection<Scope> collection) {
        this.zzaah = (Scope[]) collection.toArray(new Scope[collection.size()]);
        return this;
    }

    public GetServiceRequest zzcb(String str) {
        this.zzaaf = str;
        return this;
    }

    public GetServiceRequest zzf(Bundle bundle) {
        this.zzaai = bundle;
        return this;
    }
}
