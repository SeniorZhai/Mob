package com.google.android.gms.auth.api.credentials.internal;

import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialRequestResult;
import com.google.android.gms.common.api.Status;

public final class zzb implements CredentialRequestResult {
    private final Status zzOt;
    private final Credential zzPb;

    public zzb(Status status, Credential credential) {
        this.zzOt = status;
        this.zzPb = credential;
    }

    public static zzb zzj(Status status) {
        return new zzb(status, null);
    }

    public Credential getCredential() {
        return this.zzPb;
    }

    public Status getStatus() {
        return this.zzOt;
    }
}
