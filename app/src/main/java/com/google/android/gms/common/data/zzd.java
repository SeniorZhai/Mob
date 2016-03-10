package com.google.android.gms.common.data;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class zzd<T extends SafeParcelable> extends AbstractDataBuffer<T> {
    private static final String[] zzYu = new String[]{MPDbAdapter.KEY_DATA};
    private final Creator<T> zzYv;

    public zzd(DataHolder dataHolder, Creator<T> creator) {
        super(dataHolder);
        this.zzYv = creator;
    }

    public /* synthetic */ Object get(int x0) {
        return zzbg(x0);
    }

    public T zzbg(int i) {
        byte[] zzg = this.zzWu.zzg(MPDbAdapter.KEY_DATA, i, this.zzWu.zzbh(i));
        Parcel obtain = Parcel.obtain();
        obtain.unmarshall(zzg, 0, zzg.length);
        obtain.setDataPosition(0);
        SafeParcelable safeParcelable = (SafeParcelable) this.zzYv.createFromParcel(obtain);
        obtain.recycle();
        return safeParcelable;
    }
}
