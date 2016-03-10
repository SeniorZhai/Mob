package com.google.android.gms.analytics.internal;

public class zzak extends zzq<zzal> {

    private static class zza extends zzc implements com.google.android.gms.analytics.internal.zzq.zza<zzal> {
        private final zzal zzMD = new zzal();

        public zza(zzf com_google_android_gms_analytics_internal_zzf) {
            super(com_google_android_gms_analytics_internal_zzf);
        }

        public void zzc(String str, boolean z) {
            int i = 1;
            zzal com_google_android_gms_analytics_internal_zzal;
            if ("ga_autoActivityTracking".equals(str)) {
                com_google_android_gms_analytics_internal_zzal = this.zzMD;
                if (!z) {
                    i = 0;
                }
                com_google_android_gms_analytics_internal_zzal.zzMG = i;
            } else if ("ga_anonymizeIp".equals(str)) {
                com_google_android_gms_analytics_internal_zzal = this.zzMD;
                if (!z) {
                    i = 0;
                }
                com_google_android_gms_analytics_internal_zzal.zzMH = i;
            } else if ("ga_reportUncaughtExceptions".equals(str)) {
                com_google_android_gms_analytics_internal_zzal = this.zzMD;
                if (!z) {
                    i = 0;
                }
                com_google_android_gms_analytics_internal_zzal.zzMI = i;
            } else {
                zzd("bool configuration name not recognized", str);
            }
        }

        public void zzd(String str, int i) {
            if ("ga_sessionTimeout".equals(str)) {
                this.zzMD.zzMF = i;
            } else {
                zzd("int configuration name not recognized", str);
            }
        }

        public /* synthetic */ zzp zziV() {
            return zzkx();
        }

        public void zzk(String str, String str2) {
            this.zzMD.zzMJ.put(str, str2);
        }

        public zzal zzkx() {
            return this.zzMD;
        }

        public void zzl(String str, String str2) {
            if ("ga_trackingId".equals(str)) {
                this.zzMD.zztd = str2;
            } else if ("ga_sampleFrequency".equals(str)) {
                try {
                    this.zzMD.zzME = Double.parseDouble(str2);
                } catch (NumberFormatException e) {
                    zzc("Error parsing ga_sampleFrequency value", str2, e);
                }
            } else {
                zzd("string configuration name not recognized", str);
            }
        }
    }

    public zzak(zzf com_google_android_gms_analytics_internal_zzf) {
        super(com_google_android_gms_analytics_internal_zzf, new zza(com_google_android_gms_analytics_internal_zzf));
    }
}
