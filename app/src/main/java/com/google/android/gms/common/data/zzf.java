package com.google.android.gms.common.data;

import java.util.ArrayList;

public abstract class zzf<T> extends AbstractDataBuffer<T> {
    private boolean zzYK = false;
    private ArrayList<Integer> zzYL;

    protected zzf(DataHolder dataHolder) {
        super(dataHolder);
    }

    private void zznj() {
        synchronized (this) {
            if (!this.zzYK) {
                int count = this.zzWu.getCount();
                this.zzYL = new ArrayList();
                if (count > 0) {
                    this.zzYL.add(Integer.valueOf(0));
                    String zzni = zzni();
                    String zzd = this.zzWu.zzd(zzni, 0, this.zzWu.zzbh(0));
                    int i = 1;
                    while (i < count) {
                        int zzbh = this.zzWu.zzbh(i);
                        String zzd2 = this.zzWu.zzd(zzni, i, zzbh);
                        if (zzd2 == null) {
                            throw new NullPointerException("Missing value for markerColumn: " + zzni + ", at row: " + i + ", for window: " + zzbh);
                        }
                        if (zzd2.equals(zzd)) {
                            zzd2 = zzd;
                        } else {
                            this.zzYL.add(Integer.valueOf(i));
                        }
                        i++;
                        zzd = zzd2;
                    }
                }
                this.zzYK = true;
            }
        }
    }

    public final T get(int position) {
        zznj();
        return zzj(zzbk(position), zzbl(position));
    }

    public int getCount() {
        zznj();
        return this.zzYL.size();
    }

    int zzbk(int i) {
        if (i >= 0 && i < this.zzYL.size()) {
            return ((Integer) this.zzYL.get(i)).intValue();
        }
        throw new IllegalArgumentException("Position " + i + " is out of bounds for this buffer");
    }

    protected int zzbl(int i) {
        if (i < 0 || i == this.zzYL.size()) {
            return 0;
        }
        int count = i == this.zzYL.size() + -1 ? this.zzWu.getCount() - ((Integer) this.zzYL.get(i)).intValue() : ((Integer) this.zzYL.get(i + 1)).intValue() - ((Integer) this.zzYL.get(i)).intValue();
        if (count != 1) {
            return count;
        }
        int zzbk = zzbk(i);
        int zzbh = this.zzWu.zzbh(zzbk);
        String zznk = zznk();
        return (zznk == null || this.zzWu.zzd(zznk, zzbk, zzbh) != null) ? count : 0;
    }

    protected abstract T zzj(int i, int i2);

    protected abstract String zzni();

    protected String zznk() {
        return null;
    }
}
