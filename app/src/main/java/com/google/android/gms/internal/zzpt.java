package com.google.android.gms.internal;

import com.google.android.gms.common.api.Api.ApiOptions.Optional;
import com.google.android.gms.common.api.GoogleApiClient.ServerAuthCodeCallbacks;
import com.google.android.gms.common.internal.zzu;

public final class zzpt implements Optional {
    public static final zzpt zzaJQ = new zza().zzyc();
    private final boolean zzaJR;
    private final boolean zzaJS;
    private final String zzaJT;
    private final ServerAuthCodeCallbacks zzaJU;

    public static final class zza {
        private String zzaHb;
        private boolean zzaJV;
        private boolean zzaJW;
        private ServerAuthCodeCallbacks zzaJX;

        private String zzea(String str) {
            zzu.zzu(str);
            boolean z = this.zzaHb == null || this.zzaHb.equals(str);
            zzu.zzb(z, (Object) "two different server client ids provided");
            return str;
        }

        public zza zza(String str, ServerAuthCodeCallbacks serverAuthCodeCallbacks) {
            this.zzaJV = true;
            this.zzaJW = true;
            this.zzaHb = zzea(str);
            this.zzaJX = (ServerAuthCodeCallbacks) zzu.zzu(serverAuthCodeCallbacks);
            return this;
        }

        public zzpt zzyc() {
            return new zzpt(this.zzaJV, this.zzaJW, this.zzaHb, this.zzaJX);
        }
    }

    private zzpt(boolean z, boolean z2, String str, ServerAuthCodeCallbacks serverAuthCodeCallbacks) {
        this.zzaJR = z;
        this.zzaJS = z2;
        this.zzaJT = str;
        this.zzaJU = serverAuthCodeCallbacks;
    }

    public boolean zzxZ() {
        return this.zzaJR;
    }

    public String zzxt() {
        return this.zzaJT;
    }

    public boolean zzya() {
        return this.zzaJS;
    }

    public ServerAuthCodeCallbacks zzyb() {
        return this.zzaJU;
    }
}
