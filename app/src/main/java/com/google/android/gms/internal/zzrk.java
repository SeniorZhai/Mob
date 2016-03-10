package com.google.android.gms.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class zzrk implements Cloneable {
    private zzri<?, ?> zzaWb;
    private Object zzaWc;
    private List<zzrp> zzaWd = new ArrayList();

    zzrk() {
    }

    private byte[] toByteArray() throws IOException {
        byte[] bArr = new byte[zzB()];
        zza(zzrg.zzA(bArr));
        return bArr;
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return zzBM();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzrk)) {
            return false;
        }
        zzrk com_google_android_gms_internal_zzrk = (zzrk) o;
        if (this.zzaWc == null || com_google_android_gms_internal_zzrk.zzaWc == null) {
            if (this.zzaWd != null && com_google_android_gms_internal_zzrk.zzaWd != null) {
                return this.zzaWd.equals(com_google_android_gms_internal_zzrk.zzaWd);
            }
            try {
                return Arrays.equals(toByteArray(), com_google_android_gms_internal_zzrk.toByteArray());
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        } else if (this.zzaWb != com_google_android_gms_internal_zzrk.zzaWb) {
            return false;
        } else {
            if (!this.zzaWb.zzaVV.isArray()) {
                return this.zzaWc.equals(com_google_android_gms_internal_zzrk.zzaWc);
            }
            if (this.zzaWc instanceof byte[]) {
                return Arrays.equals((byte[]) this.zzaWc, (byte[]) com_google_android_gms_internal_zzrk.zzaWc);
            }
            if (this.zzaWc instanceof int[]) {
                return Arrays.equals((int[]) this.zzaWc, (int[]) com_google_android_gms_internal_zzrk.zzaWc);
            }
            if (this.zzaWc instanceof long[]) {
                return Arrays.equals((long[]) this.zzaWc, (long[]) com_google_android_gms_internal_zzrk.zzaWc);
            }
            if (this.zzaWc instanceof float[]) {
                return Arrays.equals((float[]) this.zzaWc, (float[]) com_google_android_gms_internal_zzrk.zzaWc);
            }
            if (this.zzaWc instanceof double[]) {
                return Arrays.equals((double[]) this.zzaWc, (double[]) com_google_android_gms_internal_zzrk.zzaWc);
            }
            return this.zzaWc instanceof boolean[] ? Arrays.equals((boolean[]) this.zzaWc, (boolean[]) com_google_android_gms_internal_zzrk.zzaWc) : Arrays.deepEquals((Object[]) this.zzaWc, (Object[]) com_google_android_gms_internal_zzrk.zzaWc);
        }
    }

    public int hashCode() {
        try {
            return Arrays.hashCode(toByteArray()) + 527;
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    int zzB() {
        if (this.zzaWc != null) {
            return this.zzaWb.zzQ(this.zzaWc);
        }
        int i = 0;
        for (zzrp zzB : this.zzaWd) {
            i = zzB.zzB() + i;
        }
        return i;
    }

    public final zzrk zzBM() {
        int i = 0;
        zzrk com_google_android_gms_internal_zzrk = new zzrk();
        try {
            com_google_android_gms_internal_zzrk.zzaWb = this.zzaWb;
            if (this.zzaWd == null) {
                com_google_android_gms_internal_zzrk.zzaWd = null;
            } else {
                com_google_android_gms_internal_zzrk.zzaWd.addAll(this.zzaWd);
            }
            if (this.zzaWc != null) {
                if (this.zzaWc instanceof zzrn) {
                    com_google_android_gms_internal_zzrk.zzaWc = ((zzrn) this.zzaWc).zzBK();
                } else if (this.zzaWc instanceof byte[]) {
                    com_google_android_gms_internal_zzrk.zzaWc = ((byte[]) this.zzaWc).clone();
                } else if (this.zzaWc instanceof byte[][]) {
                    byte[][] bArr = (byte[][]) this.zzaWc;
                    Object obj = new byte[bArr.length][];
                    com_google_android_gms_internal_zzrk.zzaWc = obj;
                    for (int i2 = 0; i2 < bArr.length; i2++) {
                        obj[i2] = (byte[]) bArr[i2].clone();
                    }
                } else if (this.zzaWc instanceof boolean[]) {
                    com_google_android_gms_internal_zzrk.zzaWc = ((boolean[]) this.zzaWc).clone();
                } else if (this.zzaWc instanceof int[]) {
                    com_google_android_gms_internal_zzrk.zzaWc = ((int[]) this.zzaWc).clone();
                } else if (this.zzaWc instanceof long[]) {
                    com_google_android_gms_internal_zzrk.zzaWc = ((long[]) this.zzaWc).clone();
                } else if (this.zzaWc instanceof float[]) {
                    com_google_android_gms_internal_zzrk.zzaWc = ((float[]) this.zzaWc).clone();
                } else if (this.zzaWc instanceof double[]) {
                    com_google_android_gms_internal_zzrk.zzaWc = ((double[]) this.zzaWc).clone();
                } else if (this.zzaWc instanceof zzrn[]) {
                    zzrn[] com_google_android_gms_internal_zzrnArr = (zzrn[]) this.zzaWc;
                    Object obj2 = new zzrn[com_google_android_gms_internal_zzrnArr.length];
                    com_google_android_gms_internal_zzrk.zzaWc = obj2;
                    while (i < com_google_android_gms_internal_zzrnArr.length) {
                        obj2[i] = com_google_android_gms_internal_zzrnArr[i].zzBK();
                        i++;
                    }
                }
            }
            return com_google_android_gms_internal_zzrk;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
        if (this.zzaWc != null) {
            this.zzaWb.zza(this.zzaWc, com_google_android_gms_internal_zzrg);
            return;
        }
        for (zzrp zza : this.zzaWd) {
            zza.zza(com_google_android_gms_internal_zzrg);
        }
    }

    void zza(zzrp com_google_android_gms_internal_zzrp) {
        this.zzaWd.add(com_google_android_gms_internal_zzrp);
    }

    <T> T zzb(zzri<?, T> com_google_android_gms_internal_zzri___T) {
        if (this.zzaWc == null) {
            this.zzaWb = com_google_android_gms_internal_zzri___T;
            this.zzaWc = com_google_android_gms_internal_zzri___T.zzx(this.zzaWd);
            this.zzaWd = null;
        } else if (this.zzaWb != com_google_android_gms_internal_zzri___T) {
            throw new IllegalStateException("Tried to getExtension with a differernt Extension.");
        }
        return this.zzaWc;
    }
}
