package com.google.android.gms.auth;

import android.accounts.Account;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class AccountChangeEventsRequest implements SafeParcelable {
    public static final Creator<AccountChangeEventsRequest> CREATOR = new zzb();
    final int mVersion;
    Account zzMY;
    @Deprecated
    String zzOx;
    int zzOz;

    public AccountChangeEventsRequest() {
        this.mVersion = 1;
    }

    AccountChangeEventsRequest(int version, int eventIndex, String accountName, Account account) {
        this.mVersion = version;
        this.zzOz = eventIndex;
        this.zzOx = accountName;
        if (account != null || TextUtils.isEmpty(accountName)) {
            this.zzMY = account;
        } else {
            this.zzMY = new Account(accountName, GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        }
    }

    public int describeContents() {
        return 0;
    }

    public Account getAccount() {
        return this.zzMY;
    }

    public String getAccountName() {
        return this.zzOx;
    }

    public int getEventIndex() {
        return this.zzOz;
    }

    public AccountChangeEventsRequest setAccount(Account account) {
        this.zzMY = account;
        return this;
    }

    public AccountChangeEventsRequest setAccountName(String accountName) {
        this.zzOx = accountName;
        return this;
    }

    public AccountChangeEventsRequest setEventIndex(int eventIndex) {
        this.zzOz = eventIndex;
        return this;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zzb.zza(this, dest, flags);
    }
}
