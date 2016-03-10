package com.google.android.gms.internal;

class zzrj implements Cloneable {
    private static final zzrk zzaVX = new zzrk();
    private int mSize;
    private boolean zzaVY;
    private int[] zzaVZ;
    private zzrk[] zzaWa;

    public zzrj() {
        this(10);
    }

    public zzrj(int i) {
        this.zzaVY = false;
        int idealIntArraySize = idealIntArraySize(i);
        this.zzaVZ = new int[idealIntArraySize];
        this.zzaWa = new zzrk[idealIntArraySize];
        this.mSize = 0;
    }

    private void gc() {
        int i = this.mSize;
        int[] iArr = this.zzaVZ;
        zzrk[] com_google_android_gms_internal_zzrkArr = this.zzaWa;
        int i2 = 0;
        for (int i3 = 0; i3 < i; i3++) {
            zzrk com_google_android_gms_internal_zzrk = com_google_android_gms_internal_zzrkArr[i3];
            if (com_google_android_gms_internal_zzrk != zzaVX) {
                if (i3 != i2) {
                    iArr[i2] = iArr[i3];
                    com_google_android_gms_internal_zzrkArr[i2] = com_google_android_gms_internal_zzrk;
                    com_google_android_gms_internal_zzrkArr[i3] = null;
                }
                i2++;
            }
        }
        this.zzaVY = false;
        this.mSize = i2;
    }

    private int idealByteArraySize(int need) {
        for (int i = 4; i < 32; i++) {
            if (need <= (1 << i) - 12) {
                return (1 << i) - 12;
            }
        }
        return need;
    }

    private int idealIntArraySize(int need) {
        return idealByteArraySize(need * 4) / 4;
    }

    private boolean zza(int[] iArr, int[] iArr2, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            if (iArr[i2] != iArr2[i2]) {
                return false;
            }
        }
        return true;
    }

    private boolean zza(zzrk[] com_google_android_gms_internal_zzrkArr, zzrk[] com_google_android_gms_internal_zzrkArr2, int i) {
        for (int i2 = 0; i2 < i; i2++) {
            if (!com_google_android_gms_internal_zzrkArr[i2].equals(com_google_android_gms_internal_zzrkArr2[i2])) {
                return false;
            }
        }
        return true;
    }

    private int zzkT(int i) {
        int i2 = 0;
        int i3 = this.mSize - 1;
        while (i2 <= i3) {
            int i4 = (i2 + i3) >>> 1;
            int i5 = this.zzaVZ[i4];
            if (i5 < i) {
                i2 = i4 + 1;
            } else if (i5 <= i) {
                return i4;
            } else {
                i3 = i4 - 1;
            }
        }
        return i2 ^ -1;
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return zzBL();
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzrj)) {
            return false;
        }
        zzrj com_google_android_gms_internal_zzrj = (zzrj) o;
        if (size() != com_google_android_gms_internal_zzrj.size()) {
            return false;
        }
        return zza(this.zzaVZ, com_google_android_gms_internal_zzrj.zzaVZ, this.mSize) && zza(this.zzaWa, com_google_android_gms_internal_zzrj.zzaWa, this.mSize);
    }

    public int hashCode() {
        if (this.zzaVY) {
            gc();
        }
        int i = 17;
        for (int i2 = 0; i2 < this.mSize; i2++) {
            i = (((i * 31) + this.zzaVZ[i2]) * 31) + this.zzaWa[i2].hashCode();
        }
        return i;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        if (this.zzaVY) {
            gc();
        }
        return this.mSize;
    }

    public final zzrj zzBL() {
        int i = 0;
        int size = size();
        zzrj com_google_android_gms_internal_zzrj = new zzrj(size);
        System.arraycopy(this.zzaVZ, 0, com_google_android_gms_internal_zzrj.zzaVZ, 0, size);
        while (i < size) {
            if (this.zzaWa[i] != null) {
                com_google_android_gms_internal_zzrj.zzaWa[i] = this.zzaWa[i].zzBM();
            }
            i++;
        }
        com_google_android_gms_internal_zzrj.mSize = size;
        return com_google_android_gms_internal_zzrj;
    }

    public void zza(int i, zzrk com_google_android_gms_internal_zzrk) {
        int zzkT = zzkT(i);
        if (zzkT >= 0) {
            this.zzaWa[zzkT] = com_google_android_gms_internal_zzrk;
            return;
        }
        zzkT ^= -1;
        if (zzkT >= this.mSize || this.zzaWa[zzkT] != zzaVX) {
            if (this.zzaVY && this.mSize >= this.zzaVZ.length) {
                gc();
                zzkT = zzkT(i) ^ -1;
            }
            if (this.mSize >= this.zzaVZ.length) {
                int idealIntArraySize = idealIntArraySize(this.mSize + 1);
                Object obj = new int[idealIntArraySize];
                Object obj2 = new zzrk[idealIntArraySize];
                System.arraycopy(this.zzaVZ, 0, obj, 0, this.zzaVZ.length);
                System.arraycopy(this.zzaWa, 0, obj2, 0, this.zzaWa.length);
                this.zzaVZ = obj;
                this.zzaWa = obj2;
            }
            if (this.mSize - zzkT != 0) {
                System.arraycopy(this.zzaVZ, zzkT, this.zzaVZ, zzkT + 1, this.mSize - zzkT);
                System.arraycopy(this.zzaWa, zzkT, this.zzaWa, zzkT + 1, this.mSize - zzkT);
            }
            this.zzaVZ[zzkT] = i;
            this.zzaWa[zzkT] = com_google_android_gms_internal_zzrk;
            this.mSize++;
            return;
        }
        this.zzaVZ[zzkT] = i;
        this.zzaWa[zzkT] = com_google_android_gms_internal_zzrk;
    }

    public zzrk zzkR(int i) {
        int zzkT = zzkT(i);
        return (zzkT < 0 || this.zzaWa[zzkT] == zzaVX) ? null : this.zzaWa[zzkT];
    }

    public zzrk zzkS(int i) {
        if (this.zzaVY) {
            gc();
        }
        return this.zzaWa[i];
    }
}
