package com.google.android.gms.common.data;

import java.util.NoSuchElementException;

public class zzg<T> extends zzb<T> {
    private T zzYM;

    public zzg(DataBuffer<T> dataBuffer) {
        super(dataBuffer);
    }

    public T next() {
        if (hasNext()) {
            this.zzYq++;
            if (this.zzYq == 0) {
                this.zzYM = this.zzYp.get(0);
                if (!(this.zzYM instanceof zzc)) {
                    throw new IllegalStateException("DataBuffer reference of type " + this.zzYM.getClass() + " is not movable");
                }
            }
            ((zzc) this.zzYM).zzbf(this.zzYq);
            return this.zzYM;
        }
        throw new NoSuchElementException("Cannot advance the iterator beyond " + this.zzYq);
    }
}
