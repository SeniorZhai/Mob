package com.google.android.gms.auth.api.credentials;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public final class CredentialRequest implements SafeParcelable {
    public static final Creator<CredentialRequest> CREATOR = new zzb();
    final int zzCY;
    private final boolean zzOY;
    private final String[] zzOZ;

    public static final class Builder {
        boolean zzOY;
        String[] zzOZ;

        public CredentialRequest build() {
            if (this.zzOZ == null) {
                this.zzOZ = new String[0];
            }
            if (this.zzOY || this.zzOZ.length != 0) {
                return new CredentialRequest();
            }
            throw new IllegalStateException("At least one authentication method must be specified");
        }

        public Builder setAccountTypes(String... accountTypes) {
            this.zzOZ = accountTypes;
            return this;
        }

        public Builder setSupportsPasswordLogin(boolean supportsPasswordLogin) {
            this.zzOY = supportsPasswordLogin;
            return this;
        }
    }

    CredentialRequest(int version, boolean supportsPasswordLogin, String[] accountTypes) {
        this.zzCY = version;
        this.zzOY = supportsPasswordLogin;
        this.zzOZ = accountTypes;
    }

    private CredentialRequest(Builder builder) {
        this.zzCY = 1;
        this.zzOY = builder.zzOY;
        this.zzOZ = builder.zzOZ;
    }

    public int describeContents() {
        return 0;
    }

    public String[] getAccountTypes() {
        return this.zzOZ;
    }

    public boolean getSupportsPasswordLogin() {
        return this.zzOY;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzb.zza(this, out, flags);
    }
}
