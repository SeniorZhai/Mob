package com.google.android.gms.auth.api.credentials;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzu;
import java.util.Collections;
import java.util.List;

public class Credential implements SafeParcelable {
    public static final Creator<Credential> CREATOR = new zza();
    public static final String EXTRA_KEY = "com.google.android.gms.credentials.Credential";
    private final String mName;
    final int zzCY;
    private final String zzKI;
    private final String zzOS;
    private final String zzOT;
    private final Uri zzOU;
    private final List<IdToken> zzOV;
    private final String zzOW;
    private final String zzOX;

    public static class Builder {
        private String mName;
        private final String zzKI;
        private String zzOS;
        private String zzOT;
        private Uri zzOU;
        private List<IdToken> zzOV;
        private String zzOW;
        private String zzOX;

        public Builder(Credential credential) {
            this.zzKI = credential.zzKI;
            this.mName = credential.mName;
            this.zzOU = credential.zzOU;
            this.zzOV = credential.zzOV;
            this.zzOW = credential.zzOW;
            this.zzOX = credential.zzOX;
            this.zzOS = credential.zzOS;
            this.zzOT = credential.zzOT;
        }

        public Builder(String id) {
            this.zzKI = id;
        }

        public Credential build() {
            if (TextUtils.isEmpty(this.zzOW) || TextUtils.isEmpty(this.zzOX)) {
                return new Credential(1, this.zzOS, this.zzOT, this.zzKI, this.mName, this.zzOU, this.zzOV, this.zzOW, this.zzOX);
            }
            throw new IllegalStateException("Only one of password or accountType may be set");
        }

        public Builder setAccountType(String accountType) {
            this.zzOX = accountType;
            return this;
        }

        public Builder setName(String name) {
            this.mName = name;
            return this;
        }

        public Builder setPassword(String password) {
            this.zzOW = password;
            return this;
        }

        public Builder setProfilePictureUri(Uri profilePictureUri) {
            this.zzOU = profilePictureUri;
            return this;
        }
    }

    Credential(int version, String internalCredentialId, String internalServerContext, String id, String name, Uri profilePictureUri, List<IdToken> idTokens, String password, String accountType) {
        this.zzCY = version;
        this.zzOS = internalCredentialId;
        this.zzOT = internalServerContext;
        this.zzKI = (String) zzu.zzu(id);
        this.mName = name;
        this.zzOU = profilePictureUri;
        this.zzOV = idTokens == null ? Collections.emptyList() : Collections.unmodifiableList(idTokens);
        this.zzOW = password;
        this.zzOX = accountType;
    }

    public int describeContents() {
        return 0;
    }

    public String getAccountType() {
        return this.zzOX;
    }

    public String getId() {
        return this.zzKI;
    }

    public String getName() {
        return this.mName;
    }

    public String getPassword() {
        return this.zzOW;
    }

    public Uri getProfilePictureUri() {
        return this.zzOU;
    }

    public void writeToParcel(Parcel out, int flags) {
        zza.zza(this, out, flags);
    }

    public String zzkZ() {
        return this.zzOS;
    }

    public String zzla() {
        return this.zzOT;
    }

    public List<IdToken> zzlb() {
        return this.zzOV;
    }
}
