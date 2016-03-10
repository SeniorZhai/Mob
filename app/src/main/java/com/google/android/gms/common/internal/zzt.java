package com.google.android.gms.common.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class zzt {

    public static final class zza {
        private final Object zzGE;
        private final List<String> zzabb;

        private zza(Object obj) {
            this.zzGE = zzu.zzu(obj);
            this.zzabb = new ArrayList();
        }

        public String toString() {
            StringBuilder append = new StringBuilder(100).append(this.zzGE.getClass().getSimpleName()).append('{');
            int size = this.zzabb.size();
            for (int i = 0; i < size; i++) {
                append.append((String) this.zzabb.get(i));
                if (i < size - 1) {
                    append.append(", ");
                }
            }
            return append.append('}').toString();
        }

        public zza zzg(String str, Object obj) {
            this.zzabb.add(((String) zzu.zzu(str)) + "=" + String.valueOf(obj));
            return this;
        }
    }

    public static boolean equal(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    public static int hashCode(Object... objects) {
        return Arrays.hashCode(objects);
    }

    public static zza zzt(Object obj) {
        return new zza(obj);
    }
}
