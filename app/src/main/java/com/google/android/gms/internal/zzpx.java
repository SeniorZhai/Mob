package com.google.android.gms.internal;

import com.google.android.gms.internal.zzaf.zzf;
import com.google.android.gms.internal.zzaf.zzj;
import com.mobcrush.mobcrush.R;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;

public interface zzpx {

    public static final class zza extends zzrh<zza> {
        public long zzaOZ;
        public zzj zzaPa;
        public zzf zziO;

        public zza() {
            zzzY();
        }

        public static zza zzs(byte[] bArr) throws zzrm {
            return (zza) zzrn.zza(new zza(), bArr);
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zza)) {
                return false;
            }
            zza com_google_android_gms_internal_zzpx_zza = (zza) o;
            if (this.zzaOZ != com_google_android_gms_internal_zzpx_zza.zzaOZ) {
                return false;
            }
            if (this.zziO == null) {
                if (com_google_android_gms_internal_zzpx_zza.zziO != null) {
                    return false;
                }
            } else if (!this.zziO.equals(com_google_android_gms_internal_zzpx_zza.zziO)) {
                return false;
            }
            if (this.zzaPa == null) {
                if (com_google_android_gms_internal_zzpx_zza.zzaPa != null) {
                    return false;
                }
            } else if (!this.zzaPa.equals(com_google_android_gms_internal_zzpx_zza.zzaPa)) {
                return false;
            }
            return zza((zzrh) com_google_android_gms_internal_zzpx_zza);
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.zziO == null ? 0 : this.zziO.hashCode()) + ((((int) (this.zzaOZ ^ (this.zzaOZ >>> 32))) + 527) * 31)) * 31;
            if (this.zzaPa != null) {
                i = this.zzaPa.hashCode();
            }
            return ((hashCode + i) * 31) + zzBI();
        }

        protected int zzB() {
            int zzB = super.zzB() + zzrg.zzd(1, this.zzaOZ);
            if (this.zziO != null) {
                zzB += zzrg.zzc(2, this.zziO);
            }
            return this.zzaPa != null ? zzB + zzrg.zzc(3, this.zzaPa) : zzB;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            com_google_android_gms_internal_zzrg.zzb(1, this.zzaOZ);
            if (this.zziO != null) {
                com_google_android_gms_internal_zzrg.zza(2, this.zziO);
            }
            if (this.zzaPa != null) {
                com_google_android_gms_internal_zzrg.zza(3, this.zzaPa);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzu(com_google_android_gms_internal_zzrf);
        }

        public zza zzu(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        this.zzaOZ = com_google_android_gms_internal_zzrf.zzBt();
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        if (this.zziO == null) {
                            this.zziO = new zzf();
                        }
                        com_google_android_gms_internal_zzrf.zza(this.zziO);
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        if (this.zzaPa == null) {
                            this.zzaPa = new zzj();
                        }
                        com_google_android_gms_internal_zzrf.zza(this.zzaPa);
                        continue;
                    default:
                        if (!zza(com_google_android_gms_internal_zzrf, zzBr)) {
                            break;
                        }
                        continue;
                }
                return this;
            }
        }

        public zza zzzY() {
            this.zzaOZ = 0;
            this.zziO = null;
            this.zzaPa = null;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }
    }
}
