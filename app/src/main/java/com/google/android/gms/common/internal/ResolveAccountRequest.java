package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class ResolveAccountRequest implements SafeParcelable {
    public static final Creator<ResolveAccountRequest> CREATOR = new zzv();
    final int zzCY;
    private final Account zzMY;
    private final int zzabc;

    ResolveAccountRequest(int versionCode, Account account, int sessionId) {
        this.zzCY = versionCode;
        this.zzMY = account;
        this.zzabc = sessionId;
    }

    public ResolveAccountRequest(Account account, int sessionId) {
        this(1, account, sessionId);
    }

    public int describeContents() {
        return 0;
    }

    public Account getAccount() {
        return this.zzMY;
    }

    public int getSessionId() {
        return this.zzabc;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzv.zza(this, dest, flags);
    }
}
