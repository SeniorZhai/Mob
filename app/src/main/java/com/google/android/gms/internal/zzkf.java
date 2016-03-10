package com.google.android.gms.internal;

import android.os.Binder;

public abstract class zzkf<T> {
    private static zza zzYj = null;
    private static int zzYk = 0;
    private static String zzYl = "com.google.android.providers.gsf.permission.READ_GSERVICES";
    private static final Object zzoW = new Object();
    private T zzLS = null;
    protected final String zztw;
    protected final T zztx;

    private interface zza {
        Long getLong(String str, Long l);

        String getString(String str, String str2);

        Boolean zzb(String str, Boolean bool);

        Float zzb(String str, Float f);

        Integer zzb(String str, Integer num);
    }

    protected zzkf(String str, T t) {
        this.zztw = str;
        this.zztx = t;
    }

    public static boolean isInitialized() {
        return zzYj != null;
    }

    public static zzkf<Float> zza(String str, Float f) {
        return new zzkf<Float>(str, f) {
            protected /* synthetic */ Object zzbP(String str) {
                return zzbT(str);
            }

            protected Float zzbT(String str) {
                return zzkf.zzYj.zzb(this.zztw, (Float) this.zztx);
            }
        };
    }

    public static zzkf<Integer> zza(String str, Integer num) {
        return new zzkf<Integer>(str, num) {
            protected /* synthetic */ Object zzbP(String str) {
                return zzbS(str);
            }

            protected Integer zzbS(String str) {
                return zzkf.zzYj.zzb(this.zztw, (Integer) this.zztx);
            }
        };
    }

    public static zzkf<Long> zza(String str, Long l) {
        return new zzkf<Long>(str, l) {
            protected /* synthetic */ Object zzbP(String str) {
                return zzbR(str);
            }

            protected Long zzbR(String str) {
                return zzkf.zzYj.getLong(this.zztw, (Long) this.zztx);
            }
        };
    }

    public static zzkf<Boolean> zzg(String str, boolean z) {
        return new zzkf<Boolean>(str, Boolean.valueOf(z)) {
            protected /* synthetic */ Object zzbP(String str) {
                return zzbQ(str);
            }

            protected Boolean zzbQ(String str) {
                return zzkf.zzYj.zzb(this.zztw, (Boolean) this.zztx);
            }
        };
    }

    public static int zzmY() {
        return zzYk;
    }

    public static zzkf<String> zzs(String str, String str2) {
        return new zzkf<String>(str, str2) {
            protected /* synthetic */ Object zzbP(String str) {
                return zzbU(str);
            }

            protected String zzbU(String str) {
                return zzkf.zzYj.getString(this.zztw, (String) this.zztx);
            }
        };
    }

    public final T get() {
        return this.zzLS != null ? this.zzLS : zzbP(this.zztw);
    }

    protected abstract T zzbP(String str);

    public final T zzmZ() {
        long clearCallingIdentity = Binder.clearCallingIdentity();
        try {
            T t = get();
            return t;
        } finally {
            Binder.restoreCallingIdentity(clearCallingIdentity);
        }
    }
}
