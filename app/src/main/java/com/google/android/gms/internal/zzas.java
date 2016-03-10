package com.google.android.gms.internal;

import java.io.IOException;

class zzas implements zzaq {
    private zzrg zznC;
    private byte[] zznD;
    private final int zznE;

    public zzas(int i) {
        this.zznE = i;
        reset();
    }

    public void reset() {
        this.zznD = new byte[this.zznE];
        this.zznC = zzrg.zzA(this.zznD);
    }

    public byte[] zzac() throws IOException {
        int zzBG = this.zznC.zzBG();
        if (zzBG < 0) {
            throw new IOException();
        } else if (zzBG == 0) {
            return this.zznD;
        } else {
            Object obj = new byte[(this.zznD.length - zzBG)];
            System.arraycopy(this.zznD, 0, obj, 0, obj.length);
            return obj;
        }
    }

    public void zzb(int i, long j) throws IOException {
        this.zznC.zzb(i, j);
    }

    public void zzb(int i, String str) throws IOException {
        this.zznC.zzb(i, str);
    }
}
