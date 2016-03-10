package com.google.android.gms.analytics.internal;

public class zzz extends zzq<zzaa> {

    private static class zza implements com.google.android.gms.analytics.internal.zzq.zza<zzaa> {
        private final zzf zzJy;
        private final zzaa zzLT = new zzaa();

        public zza(zzf com_google_android_gms_analytics_internal_zzf) {
            this.zzJy = com_google_android_gms_analytics_internal_zzf;
        }

        public void zzc(String str, boolean z) {
            if ("ga_dryRun".equals(str)) {
                this.zzLT.zzLY = z ? 1 : 0;
                return;
            }
            this.zzJy.zzhQ().zzd("Bool xml configuration name not recognized", str);
        }

        public void zzd(String str, int i) {
            if ("ga_dispatchPeriod".equals(str)) {
                this.zzLT.zzLX = i;
            } else {
                this.zzJy.zzhQ().zzd("Int xml configuration name not recognized", str);
            }
        }

        public /* synthetic */ zzp zziV() {
            return zzjJ();
        }

        public zzaa zzjJ() {
            return this.zzLT;
        }

        public void zzk(String str, String str2) {
        }

        public void zzl(String str, String str2) {
            if ("ga_appName".equals(str)) {
                this.zzLT.zzLU = str2;
            } else if ("ga_appVersion".equals(str)) {
                this.zzLT.zzLV = str2;
            } else if ("ga_logLevel".equals(str)) {
                this.zzLT.zzLW = str2;
            } else {
                this.zzJy.zzhQ().zzd("String xml configuration name not recognized", str);
            }
        }
    }

    public zzz(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf, new zza(com_google_android_gms_analytics_internal_zzf));
    }
}
