package com.google.android.gms.common.data;

import android.os.Bundle;
import java.util.Iterator;

public abstract class AbstractDataBuffer<T> implements DataBuffer<T> {
    protected final DataHolder zzWu;

    protected AbstractDataBuffer(DataHolder dataHolder) {
        this.zzWu = dataHolder;
        if (this.zzWu != null) {
            this.zzWu.zzp(this);
        }
    }

    @Deprecated
    public final void close() {
        release();
    }

    public abstract T get(int i);

    public int getCount() {
        return this.zzWu == null ? 0 : this.zzWu.getCount();
    }

    @Deprecated
    public boolean isClosed() {
        return this.zzWu == null || this.zzWu.isClosed();
    }

    public Iterator<T> iterator() {
        return new zzb(this);
    }

    public void release() {
        if (this.zzWu != null) {
            this.zzWu.close();
        }
    }

    public Iterator<T> singleRefIterator() {
        return new zzg(this);
    }

    public Bundle zznb() {
        return this.zzWu.zznb();
    }
}
