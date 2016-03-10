package com.google.android.gms.common.internal;

import android.accounts.Account;
import android.content.Context;
import android.os.Binder;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class zza extends com.google.android.gms.common.internal.IAccountAccessor.zza {
    private Context mContext;
    private Account zzMY;
    int zzZN;

    public static Account zza(IAccountAccessor iAccountAccessor) {
        Account account = null;
        if (iAccountAccessor != null) {
            long clearCallingIdentity = Binder.clearCallingIdentity();
            try {
                account = iAccountAccessor.getAccount();
            } catch (RemoteException e) {
                Log.w("AccountAccessor", "Remote account accessor probably died");
            } finally {
                Binder.restoreCallingIdentity(clearCallingIdentity);
            }
        }
        return account;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return !(o instanceof zza) ? false : this.zzMY.equals(((zza) o).zzMY);
    }

    public Account getAccount() {
        int callingUid = Binder.getCallingUid();
        if (callingUid == this.zzZN) {
            return this.zzMY;
        }
        if (GooglePlayServicesUtil.zzd(this.mContext, callingUid)) {
            this.zzZN = callingUid;
            return this.zzMY;
        }
        throw new SecurityException("Caller is not GooglePlayServices");
    }
}
