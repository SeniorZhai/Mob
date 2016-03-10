package com.google.android.gms.tagmanager;

class zzde extends Number implements Comparable<zzde> {
    private double zzaOq;
    private long zzaOr;
    private boolean zzaOs = false;

    private zzde(double d) {
        this.zzaOq = d;
    }

    private zzde(long j) {
        this.zzaOr = j;
    }

    public static zzde zzT(long j) {
        return new zzde(j);
    }

    public static zzde zza(Double d) {
        return new zzde(d.doubleValue());
    }

    public static zzde zzeI(String str) throws NumberFormatException {
        try {
            return new zzde(Long.parseLong(str));
        } catch (NumberFormatException e) {
            try {
                return new zzde(Double.parseDouble(str));
            } catch (NumberFormatException e2) {
                throw new NumberFormatException(str + " is not a valid TypedNumber");
            }
        }
    }

    public byte byteValue() {
        return (byte) ((int) longValue());
    }

    public /* synthetic */ int compareTo(Object x0) {
        return zza((zzde) x0);
    }

    public double doubleValue() {
        return zzzG() ? (double) this.zzaOr : this.zzaOq;
    }

    public boolean equals(Object other) {
        return (other instanceof zzde) && zza((zzde) other) == 0;
    }

    public float floatValue() {
        return (float) doubleValue();
    }

    public int hashCode() {
        return new Long(longValue()).hashCode();
    }

    public int intValue() {
        return zzzI();
    }

    public long longValue() {
        return zzzH();
    }

    public short shortValue() {
        return zzzJ();
    }

    public String toString() {
        return zzzG() ? Long.toString(this.zzaOr) : Double.toString(this.zzaOq);
    }

    public int zza(zzde com_google_android_gms_tagmanager_zzde) {
        return (zzzG() && com_google_android_gms_tagmanager_zzde.zzzG()) ? new Long(this.zzaOr).compareTo(Long.valueOf(com_google_android_gms_tagmanager_zzde.zzaOr)) : Double.compare(doubleValue(), com_google_android_gms_tagmanager_zzde.doubleValue());
    }

    public boolean zzzF() {
        return !zzzG();
    }

    public boolean zzzG() {
        return this.zzaOs;
    }

    public long zzzH() {
        return zzzG() ? this.zzaOr : (long) this.zzaOq;
    }

    public int zzzI() {
        return (int) longValue();
    }

    public short zzzJ() {
        return (short) ((int) longValue());
    }
}
