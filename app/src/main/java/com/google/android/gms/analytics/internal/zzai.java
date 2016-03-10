package com.google.android.gms.analytics.internal;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Pair;
import com.google.android.gms.common.internal.zzu;
import io.fabric.sdk.android.BuildConfig;
import java.util.UUID;

public class zzai extends zzd {
    private SharedPreferences zzMw;
    private long zzMx;
    private long zzMy = -1;
    private final zza zzMz = new zza("monitoring", zzhR().zzjC());

    public final class zza {
        private final String mName;
        private final long zzMA;
        final /* synthetic */ zzai zzMB;

        private zza(zzai com_google_android_gms_analytics_internal_zzai, String str, long j) {
            this.zzMB = com_google_android_gms_analytics_internal_zzai;
            zzu.zzcj(str);
            zzu.zzV(j > 0);
            this.mName = str;
            this.zzMA = j;
        }

        private void zzkq() {
            long currentTimeMillis = this.zzMB.zzhP().currentTimeMillis();
            Editor edit = this.zzMB.zzMw.edit();
            edit.remove(zzkv());
            edit.remove(zzkw());
            edit.putLong(zzku(), currentTimeMillis);
            edit.commit();
        }

        private long zzkr() {
            long zzkt = zzkt();
            return zzkt == 0 ? 0 : Math.abs(zzkt - this.zzMB.zzhP().currentTimeMillis());
        }

        private long zzkt() {
            return this.zzMB.zzMw.getLong(zzku(), 0);
        }

        private String zzku() {
            return this.mName + ":start";
        }

        private String zzkv() {
            return this.mName + ":count";
        }

        public void zzbg(String str) {
            if (zzkt() == 0) {
                zzkq();
            }
            if (str == null) {
                str = BuildConfig.FLAVOR;
            }
            synchronized (this) {
                long j = this.zzMB.zzMw.getLong(zzkv(), 0);
                if (j <= 0) {
                    Editor edit = this.zzMB.zzMw.edit();
                    edit.putString(zzkw(), str);
                    edit.putLong(zzkv(), 1);
                    edit.apply();
                    return;
                }
                Object obj = (UUID.randomUUID().getLeastSignificantBits() & Long.MAX_VALUE) < Long.MAX_VALUE / (j + 1) ? 1 : null;
                Editor edit2 = this.zzMB.zzMw.edit();
                if (obj != null) {
                    edit2.putString(zzkw(), str);
                }
                edit2.putLong(zzkv(), j + 1);
                edit2.apply();
            }
        }

        public Pair<String, Long> zzks() {
            long zzkr = zzkr();
            if (zzkr < this.zzMA) {
                return null;
            }
            if (zzkr > this.zzMA * 2) {
                zzkq();
                return null;
            }
            String string = this.zzMB.zzMw.getString(zzkw(), null);
            zzkr = this.zzMB.zzMw.getLong(zzkv(), 0);
            zzkq();
            return (string == null || zzkr <= 0) ? null : new Pair(string, Long.valueOf(zzkr));
        }

        protected String zzkw() {
            return this.mName + ":value";
        }
    }

    protected zzai(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf);
    }

    public void zzbf(String str) {
        zzhO();
        zzia();
        Editor edit = this.zzMw.edit();
        if (TextUtils.isEmpty(str)) {
            edit.remove("installation_campaign");
        } else {
            edit.putString("installation_campaign", str);
        }
        if (!edit.commit()) {
            zzaW("Failed to commit campaign data");
        }
    }

    protected void zzhn() {
        this.zzMw = getContext().getSharedPreferences("com.google.android.gms.analytics.prefs", 0);
    }

    public long zzkk() {
        zzhO();
        zzia();
        if (this.zzMx == 0) {
            long j = this.zzMw.getLong("first_run", 0);
            if (j != 0) {
                this.zzMx = j;
            } else {
                j = zzhP().currentTimeMillis();
                Editor edit = this.zzMw.edit();
                edit.putLong("first_run", j);
                if (!edit.commit()) {
                    zzaW("Failed to commit first run time");
                }
                this.zzMx = j;
            }
        }
        return this.zzMx;
    }

    public zzaj zzkl() {
        return new zzaj(zzhP(), zzkk());
    }

    public long zzkm() {
        zzhO();
        zzia();
        if (this.zzMy == -1) {
            this.zzMy = this.zzMw.getLong("last_dispatch", 0);
        }
        return this.zzMy;
    }

    public void zzkn() {
        zzhO();
        zzia();
        long currentTimeMillis = zzhP().currentTimeMillis();
        Editor edit = this.zzMw.edit();
        edit.putLong("last_dispatch", currentTimeMillis);
        edit.apply();
        this.zzMy = currentTimeMillis;
    }

    public String zzko() {
        zzhO();
        zzia();
        CharSequence string = this.zzMw.getString("installation_campaign", null);
        return TextUtils.isEmpty(string) ? null : string;
    }

    public zza zzkp() {
        return this.zzMz;
    }
}
