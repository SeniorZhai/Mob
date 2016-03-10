package com.google.android.gms.internal;

import com.mobcrush.mobcrush.R;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.protocol.HTTP;

public class zzri<M extends zzrh<M>, T> {
    public final int tag;
    protected final int type;
    protected final Class<T> zzaVV;
    protected final boolean zzaVW;

    private zzri(int i, Class<T> cls, int i2, boolean z) {
        this.type = i;
        this.zzaVV = cls;
        this.tag = i2;
        this.zzaVW = z;
    }

    @Deprecated
    public static <M extends zzrh<M>, T extends zzrn> zzri<M, T> zza(int i, Class<T> cls, int i2) {
        return new zzri(i, cls, i2, false);
    }

    private T zzy(List<zzrp> list) {
        int i;
        int i2 = 0;
        List arrayList = new ArrayList();
        for (i = 0; i < list.size(); i++) {
            zzrp com_google_android_gms_internal_zzrp = (zzrp) list.get(i);
            if (com_google_android_gms_internal_zzrp.zzaWg.length != 0) {
                zza(com_google_android_gms_internal_zzrp, arrayList);
            }
        }
        i = arrayList.size();
        if (i == 0) {
            return null;
        }
        T cast = this.zzaVV.cast(Array.newInstance(this.zzaVV.getComponentType(), i));
        while (i2 < i) {
            Array.set(cast, i2, arrayList.get(i2));
            i2++;
        }
        return cast;
    }

    private T zzz(List<zzrp> list) {
        if (list.isEmpty()) {
            return null;
        }
        return this.zzaVV.cast(zzA(zzrf.zzz(((zzrp) list.get(list.size() - 1)).zzaWg)));
    }

    protected Object zzA(zzrf com_google_android_gms_internal_zzrf) {
        Class componentType = this.zzaVW ? this.zzaVV.getComponentType() : this.zzaVV;
        try {
            zzrn com_google_android_gms_internal_zzrn;
            switch (this.type) {
                case HTTP.LF /*10*/:
                    com_google_android_gms_internal_zzrn = (zzrn) componentType.newInstance();
                    com_google_android_gms_internal_zzrf.zza(com_google_android_gms_internal_zzrn, zzrq.zzkV(this.tag));
                    return com_google_android_gms_internal_zzrn;
                case R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                    com_google_android_gms_internal_zzrn = (zzrn) componentType.newInstance();
                    com_google_android_gms_internal_zzrf.zza(com_google_android_gms_internal_zzrn);
                    return com_google_android_gms_internal_zzrn;
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        } catch (Throwable e) {
            throw new IllegalArgumentException("Error creating instance of class " + componentType, e);
        } catch (Throwable e2) {
            throw new IllegalArgumentException("Error creating instance of class " + componentType, e2);
        } catch (Throwable e22) {
            throw new IllegalArgumentException("Error reading extension field", e22);
        }
    }

    int zzQ(Object obj) {
        return this.zzaVW ? zzR(obj) : zzS(obj);
    }

    protected int zzR(Object obj) {
        int i = 0;
        int length = Array.getLength(obj);
        for (int i2 = 0; i2 < length; i2++) {
            if (Array.get(obj, i2) != null) {
                i += zzS(Array.get(obj, i2));
            }
        }
        return i;
    }

    protected int zzS(Object obj) {
        int zzkV = zzrq.zzkV(this.tag);
        switch (this.type) {
            case HTTP.LF /*10*/:
                return zzrg.zzb(zzkV, (zzrn) obj);
            case R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                return zzrg.zzc(zzkV, (zzrn) obj);
            default:
                throw new IllegalArgumentException("Unknown type " + this.type);
        }
    }

    protected void zza(zzrp com_google_android_gms_internal_zzrp, List<Object> list) {
        list.add(zzA(zzrf.zzz(com_google_android_gms_internal_zzrp.zzaWg)));
    }

    void zza(Object obj, zzrg com_google_android_gms_internal_zzrg) throws IOException {
        if (this.zzaVW) {
            zzc(obj, com_google_android_gms_internal_zzrg);
        } else {
            zzb(obj, com_google_android_gms_internal_zzrg);
        }
    }

    protected void zzb(Object obj, zzrg com_google_android_gms_internal_zzrg) {
        try {
            com_google_android_gms_internal_zzrg.zzkN(this.tag);
            switch (this.type) {
                case HTTP.LF /*10*/:
                    zzrn com_google_android_gms_internal_zzrn = (zzrn) obj;
                    int zzkV = zzrq.zzkV(this.tag);
                    com_google_android_gms_internal_zzrg.zzb(com_google_android_gms_internal_zzrn);
                    com_google_android_gms_internal_zzrg.zzC(zzkV, 4);
                    return;
                case R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                    com_google_android_gms_internal_zzrg.zzc((zzrn) obj);
                    return;
                default:
                    throw new IllegalArgumentException("Unknown type " + this.type);
            }
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
        throw new IllegalStateException(e);
    }

    protected void zzc(Object obj, zzrg com_google_android_gms_internal_zzrg) {
        int length = Array.getLength(obj);
        for (int i = 0; i < length; i++) {
            Object obj2 = Array.get(obj, i);
            if (obj2 != null) {
                zzb(obj2, com_google_android_gms_internal_zzrg);
            }
        }
    }

    final T zzx(List<zzrp> list) {
        return list == null ? null : this.zzaVW ? zzy(list) : zzz(list);
    }
}
