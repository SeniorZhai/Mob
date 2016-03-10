package com.google.android.gms.common.internal;

import java.util.Iterator;

public class zzs {
    private final String separator;

    private zzs(String str) {
        this.separator = str;
    }

    public static zzs zzci(String str) {
        return new zzs(str);
    }

    public final String zza(Iterable<?> iterable) {
        return zza(new StringBuilder(), iterable).toString();
    }

    public final StringBuilder zza(StringBuilder stringBuilder, Iterable<?> iterable) {
        Iterator it = iterable.iterator();
        if (it.hasNext()) {
            stringBuilder.append(zzs(it.next()));
            while (it.hasNext()) {
                stringBuilder.append(this.separator);
                stringBuilder.append(zzs(it.next()));
            }
        }
        return stringBuilder;
    }

    CharSequence zzs(Object obj) {
        return obj instanceof CharSequence ? (CharSequence) obj : obj.toString();
    }
}
