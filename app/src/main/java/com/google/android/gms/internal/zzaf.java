package com.google.android.gms.internal;

import android.support.v4.media.TransportMediator;
import com.helpshift.res.values.HSConsts;
import com.mobcrush.mobcrush.R;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import org.apache.http.protocol.HTTP;

public interface zzaf {

    public static final class zza extends zzrh<zza> {
        public int level;
        public int zzhK;
        public int zzhL;

        public zza() {
            zzA();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zza)) {
                return false;
            }
            zza com_google_android_gms_internal_zzaf_zza = (zza) o;
            return (this.level == com_google_android_gms_internal_zzaf_zza.level && this.zzhK == com_google_android_gms_internal_zzaf_zza.zzhK && this.zzhL == com_google_android_gms_internal_zzaf_zza.zzhL) ? zza((zzrh) com_google_android_gms_internal_zzaf_zza) : false;
        }

        public int hashCode() {
            return ((((((this.level + 527) * 31) + this.zzhK) * 31) + this.zzhL) * 31) + zzBI();
        }

        public zza zzA() {
            this.level = 1;
            this.zzhK = 0;
            this.zzhL = 0;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        protected int zzB() {
            int zzB = super.zzB();
            if (this.level != 1) {
                zzB += zzrg.zzA(1, this.level);
            }
            if (this.zzhK != 0) {
                zzB += zzrg.zzA(2, this.zzhK);
            }
            return this.zzhL != 0 ? zzB + zzrg.zzA(3, this.zzhL) : zzB;
        }

        public zza zza(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        zzBr = com_google_android_gms_internal_zzrf.zzBu();
                        switch (zzBr) {
                            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                                this.level = zzBr;
                                break;
                            default:
                                continue;
                        }
                    case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                        this.zzhK = com_google_android_gms_internal_zzrf.zzBu();
                        continue;
                    case R.styleable.Toolbar_subtitleTextColor /*24*/:
                        this.zzhL = com_google_android_gms_internal_zzrf.zzBu();
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

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            if (this.level != 1) {
                com_google_android_gms_internal_zzrg.zzy(1, this.level);
            }
            if (this.zzhK != 0) {
                com_google_android_gms_internal_zzrg.zzy(2, this.zzhK);
            }
            if (this.zzhL != 0) {
                com_google_android_gms_internal_zzrg.zzy(3, this.zzhL);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zza(com_google_android_gms_internal_zzrf);
        }
    }

    public static final class zzb extends zzrh<zzb> {
        private static volatile zzb[] zzhM;
        public int name;
        public int[] zzhN;
        public int zzhO;
        public boolean zzhP;
        public boolean zzhQ;

        public zzb() {
            zzD();
        }

        public static zzb[] zzC() {
            if (zzhM == null) {
                synchronized (zzrl.zzaWe) {
                    if (zzhM == null) {
                        zzhM = new zzb[0];
                    }
                }
            }
            return zzhM;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzb)) {
                return false;
            }
            zzb com_google_android_gms_internal_zzaf_zzb = (zzb) o;
            return (zzrl.equals(this.zzhN, com_google_android_gms_internal_zzaf_zzb.zzhN) && this.zzhO == com_google_android_gms_internal_zzaf_zzb.zzhO && this.name == com_google_android_gms_internal_zzaf_zzb.name && this.zzhP == com_google_android_gms_internal_zzaf_zzb.zzhP && this.zzhQ == com_google_android_gms_internal_zzaf_zzb.zzhQ) ? zza((zzrh) com_google_android_gms_internal_zzaf_zzb) : false;
        }

        public int hashCode() {
            int i = 1231;
            int hashCode = ((this.zzhP ? 1231 : 1237) + ((((((zzrl.hashCode(this.zzhN) + 527) * 31) + this.zzhO) * 31) + this.name) * 31)) * 31;
            if (!this.zzhQ) {
                i = 1237;
            }
            return ((hashCode + i) * 31) + zzBI();
        }

        protected int zzB() {
            int i = 0;
            int zzB = super.zzB();
            if (this.zzhQ) {
                zzB += zzrg.zzc(1, this.zzhQ);
            }
            int zzA = zzrg.zzA(2, this.zzhO) + zzB;
            if (this.zzhN == null || this.zzhN.length <= 0) {
                zzB = zzA;
            } else {
                for (int zzkJ : this.zzhN) {
                    i += zzrg.zzkJ(zzkJ);
                }
                zzB = (zzA + i) + (this.zzhN.length * 1);
            }
            if (this.name != 0) {
                zzB += zzrg.zzA(4, this.name);
            }
            return this.zzhP ? zzB + zzrg.zzc(6, this.zzhP) : zzB;
        }

        public zzb zzD() {
            this.zzhN = zzrq.zzaWh;
            this.zzhO = 0;
            this.name = 0;
            this.zzhP = false;
            this.zzhQ = false;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            if (this.zzhQ) {
                com_google_android_gms_internal_zzrg.zzb(1, this.zzhQ);
            }
            com_google_android_gms_internal_zzrg.zzy(2, this.zzhO);
            if (this.zzhN != null && this.zzhN.length > 0) {
                for (int zzy : this.zzhN) {
                    com_google_android_gms_internal_zzrg.zzy(3, zzy);
                }
            }
            if (this.name != 0) {
                com_google_android_gms_internal_zzrg.zzy(4, this.name);
            }
            if (this.zzhP) {
                com_google_android_gms_internal_zzrg.zzb(6, this.zzhP);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzc(com_google_android_gms_internal_zzrf);
        }

        public zzb zzc(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                int zzb;
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        this.zzhQ = com_google_android_gms_internal_zzrf.zzBv();
                        continue;
                    case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                        this.zzhO = com_google_android_gms_internal_zzrf.zzBu();
                        continue;
                    case R.styleable.Toolbar_subtitleTextColor /*24*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 24);
                        zzBr = this.zzhN == null ? 0 : this.zzhN.length;
                        Object obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzhN, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zzhN = obj;
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        int zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zzhN == null ? 0 : this.zzhN.length;
                        Object obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzhN, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zzhN = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case HTTP.SP /*32*/:
                        this.name = com_google_android_gms_internal_zzrf.zzBu();
                        continue;
                    case R.styleable.Theme_homeAsUpIndicator /*48*/:
                        this.zzhP = com_google_android_gms_internal_zzrf.zzBv();
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
    }

    public static final class zzc extends zzrh<zzc> {
        private static volatile zzc[] zzhR;
        public String zzaC;
        public long zzhS;
        public long zzhT;
        public boolean zzhU;
        public long zzhV;

        public zzc() {
            zzF();
        }

        public static zzc[] zzE() {
            if (zzhR == null) {
                synchronized (zzrl.zzaWe) {
                    if (zzhR == null) {
                        zzhR = new zzc[0];
                    }
                }
            }
            return zzhR;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzc)) {
                return false;
            }
            zzc com_google_android_gms_internal_zzaf_zzc = (zzc) o;
            if (this.zzaC == null) {
                if (com_google_android_gms_internal_zzaf_zzc.zzaC != null) {
                    return false;
                }
            } else if (!this.zzaC.equals(com_google_android_gms_internal_zzaf_zzc.zzaC)) {
                return false;
            }
            return (this.zzhS == com_google_android_gms_internal_zzaf_zzc.zzhS && this.zzhT == com_google_android_gms_internal_zzaf_zzc.zzhT && this.zzhU == com_google_android_gms_internal_zzaf_zzc.zzhU && this.zzhV == com_google_android_gms_internal_zzaf_zzc.zzhV) ? zza((zzrh) com_google_android_gms_internal_zzaf_zzc) : false;
        }

        public int hashCode() {
            return (((((this.zzhU ? 1231 : 1237) + (((((((this.zzaC == null ? 0 : this.zzaC.hashCode()) + 527) * 31) + ((int) (this.zzhS ^ (this.zzhS >>> 32)))) * 31) + ((int) (this.zzhT ^ (this.zzhT >>> 32)))) * 31)) * 31) + ((int) (this.zzhV ^ (this.zzhV >>> 32)))) * 31) + zzBI();
        }

        protected int zzB() {
            int zzB = super.zzB();
            if (!this.zzaC.equals(BuildConfig.FLAVOR)) {
                zzB += zzrg.zzk(1, this.zzaC);
            }
            if (this.zzhS != 0) {
                zzB += zzrg.zzd(2, this.zzhS);
            }
            if (this.zzhT != 2147483647L) {
                zzB += zzrg.zzd(3, this.zzhT);
            }
            if (this.zzhU) {
                zzB += zzrg.zzc(4, this.zzhU);
            }
            return this.zzhV != 0 ? zzB + zzrg.zzd(5, this.zzhV) : zzB;
        }

        public zzc zzF() {
            this.zzaC = BuildConfig.FLAVOR;
            this.zzhS = 0;
            this.zzhT = 2147483647L;
            this.zzhU = false;
            this.zzhV = 0;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            if (!this.zzaC.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(1, this.zzaC);
            }
            if (this.zzhS != 0) {
                com_google_android_gms_internal_zzrg.zzb(2, this.zzhS);
            }
            if (this.zzhT != 2147483647L) {
                com_google_android_gms_internal_zzrg.zzb(3, this.zzhT);
            }
            if (this.zzhU) {
                com_google_android_gms_internal_zzrg.zzb(4, this.zzhU);
            }
            if (this.zzhV != 0) {
                com_google_android_gms_internal_zzrg.zzb(5, this.zzhV);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzd(com_google_android_gms_internal_zzrf);
        }

        public zzc zzd(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case HTTP.LF /*10*/:
                        this.zzaC = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                        this.zzhS = com_google_android_gms_internal_zzrf.zzBt();
                        continue;
                    case R.styleable.Toolbar_subtitleTextColor /*24*/:
                        this.zzhT = com_google_android_gms_internal_zzrf.zzBt();
                        continue;
                    case HTTP.SP /*32*/:
                        this.zzhU = com_google_android_gms_internal_zzrf.zzBv();
                        continue;
                    case R.styleable.Theme_textAppearanceLargePopupMenu /*40*/:
                        this.zzhV = com_google_android_gms_internal_zzrf.zzBt();
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
    }

    public static final class zzd extends zzrh<zzd> {
        public com.google.android.gms.internal.zzag.zza[] zzhW;
        public com.google.android.gms.internal.zzag.zza[] zzhX;
        public zzc[] zzhY;

        public zzd() {
            zzG();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzd)) {
                return false;
            }
            zzd com_google_android_gms_internal_zzaf_zzd = (zzd) o;
            return (zzrl.equals(this.zzhW, com_google_android_gms_internal_zzaf_zzd.zzhW) && zzrl.equals(this.zzhX, com_google_android_gms_internal_zzaf_zzd.zzhX) && zzrl.equals(this.zzhY, com_google_android_gms_internal_zzaf_zzd.zzhY)) ? zza((zzrh) com_google_android_gms_internal_zzaf_zzd) : false;
        }

        public int hashCode() {
            return ((((((zzrl.hashCode(this.zzhW) + 527) * 31) + zzrl.hashCode(this.zzhX)) * 31) + zzrl.hashCode(this.zzhY)) * 31) + zzBI();
        }

        protected int zzB() {
            int i;
            int i2 = 0;
            int zzB = super.zzB();
            if (this.zzhW != null && this.zzhW.length > 0) {
                i = zzB;
                for (zzrn com_google_android_gms_internal_zzrn : this.zzhW) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        i += zzrg.zzc(1, com_google_android_gms_internal_zzrn);
                    }
                }
                zzB = i;
            }
            if (this.zzhX != null && this.zzhX.length > 0) {
                i = zzB;
                for (zzrn com_google_android_gms_internal_zzrn2 : this.zzhX) {
                    if (com_google_android_gms_internal_zzrn2 != null) {
                        i += zzrg.zzc(2, com_google_android_gms_internal_zzrn2);
                    }
                }
                zzB = i;
            }
            if (this.zzhY != null && this.zzhY.length > 0) {
                while (i2 < this.zzhY.length) {
                    zzrn com_google_android_gms_internal_zzrn3 = this.zzhY[i2];
                    if (com_google_android_gms_internal_zzrn3 != null) {
                        zzB += zzrg.zzc(3, com_google_android_gms_internal_zzrn3);
                    }
                    i2++;
                }
            }
            return zzB;
        }

        public zzd zzG() {
            this.zzhW = com.google.android.gms.internal.zzag.zza.zzQ();
            this.zzhX = com.google.android.gms.internal.zzag.zza.zzQ();
            this.zzhY = zzc.zzE();
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            int i = 0;
            if (this.zzhW != null && this.zzhW.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn : this.zzhW) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        com_google_android_gms_internal_zzrg.zza(1, com_google_android_gms_internal_zzrn);
                    }
                }
            }
            if (this.zzhX != null && this.zzhX.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn2 : this.zzhX) {
                    if (com_google_android_gms_internal_zzrn2 != null) {
                        com_google_android_gms_internal_zzrg.zza(2, com_google_android_gms_internal_zzrn2);
                    }
                }
            }
            if (this.zzhY != null && this.zzhY.length > 0) {
                while (i < this.zzhY.length) {
                    zzrn com_google_android_gms_internal_zzrn3 = this.zzhY[i];
                    if (com_google_android_gms_internal_zzrn3 != null) {
                        com_google_android_gms_internal_zzrg.zza(3, com_google_android_gms_internal_zzrn3);
                    }
                    i++;
                }
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zze(com_google_android_gms_internal_zzrf);
        }

        public zzd zze(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                int zzb;
                Object obj;
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case HTTP.LF /*10*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 10);
                        zzBr = this.zzhW == null ? 0 : this.zzhW.length;
                        obj = new com.google.android.gms.internal.zzag.zza[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzhW, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new com.google.android.gms.internal.zzag.zza();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new com.google.android.gms.internal.zzag.zza();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzhW = obj;
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 18);
                        zzBr = this.zzhX == null ? 0 : this.zzhX.length;
                        obj = new com.google.android.gms.internal.zzag.zza[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzhX, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new com.google.android.gms.internal.zzag.zza();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new com.google.android.gms.internal.zzag.zza();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzhX = obj;
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 26);
                        zzBr = this.zzhY == null ? 0 : this.zzhY.length;
                        obj = new zzc[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzhY, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zzc();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zzc();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzhY = obj;
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
    }

    public static final class zze extends zzrh<zze> {
        private static volatile zze[] zzhZ;
        public int key;
        public int value;

        public zze() {
            zzI();
        }

        public static zze[] zzH() {
            if (zzhZ == null) {
                synchronized (zzrl.zzaWe) {
                    if (zzhZ == null) {
                        zzhZ = new zze[0];
                    }
                }
            }
            return zzhZ;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zze)) {
                return false;
            }
            zze com_google_android_gms_internal_zzaf_zze = (zze) o;
            return (this.key == com_google_android_gms_internal_zzaf_zze.key && this.value == com_google_android_gms_internal_zzaf_zze.value) ? zza((zzrh) com_google_android_gms_internal_zzaf_zze) : false;
        }

        public int hashCode() {
            return ((((this.key + 527) * 31) + this.value) * 31) + zzBI();
        }

        protected int zzB() {
            return (super.zzB() + zzrg.zzA(1, this.key)) + zzrg.zzA(2, this.value);
        }

        public zze zzI() {
            this.key = 0;
            this.value = 0;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            com_google_android_gms_internal_zzrg.zzy(1, this.key);
            com_google_android_gms_internal_zzrg.zzy(2, this.value);
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzf(com_google_android_gms_internal_zzrf);
        }

        public zze zzf(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        this.key = com_google_android_gms_internal_zzrf.zzBu();
                        continue;
                    case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                        this.value = com_google_android_gms_internal_zzrf.zzBu();
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
    }

    public static final class zzf extends zzrh<zzf> {
        public String version;
        public String[] zzia;
        public String[] zzib;
        public com.google.android.gms.internal.zzag.zza[] zzic;
        public zze[] zzid;
        public zzb[] zzie;
        public zzb[] zzif;
        public zzb[] zzig;
        public zzg[] zzih;
        public String zzii;
        public String zzij;
        public String zzik;
        public zza zzil;
        public float zzim;
        public boolean zzin;
        public String[] zzio;
        public int zzip;

        public zzf() {
            zzJ();
        }

        public static zzf zzc(byte[] bArr) throws zzrm {
            return (zzf) zzrn.zza(new zzf(), bArr);
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzf)) {
                return false;
            }
            zzf com_google_android_gms_internal_zzaf_zzf = (zzf) o;
            if (!zzrl.equals(this.zzia, com_google_android_gms_internal_zzaf_zzf.zzia) || !zzrl.equals(this.zzib, com_google_android_gms_internal_zzaf_zzf.zzib) || !zzrl.equals(this.zzic, com_google_android_gms_internal_zzaf_zzf.zzic) || !zzrl.equals(this.zzid, com_google_android_gms_internal_zzaf_zzf.zzid) || !zzrl.equals(this.zzie, com_google_android_gms_internal_zzaf_zzf.zzie) || !zzrl.equals(this.zzif, com_google_android_gms_internal_zzaf_zzf.zzif) || !zzrl.equals(this.zzig, com_google_android_gms_internal_zzaf_zzf.zzig) || !zzrl.equals(this.zzih, com_google_android_gms_internal_zzaf_zzf.zzih)) {
                return false;
            }
            if (this.zzii == null) {
                if (com_google_android_gms_internal_zzaf_zzf.zzii != null) {
                    return false;
                }
            } else if (!this.zzii.equals(com_google_android_gms_internal_zzaf_zzf.zzii)) {
                return false;
            }
            if (this.zzij == null) {
                if (com_google_android_gms_internal_zzaf_zzf.zzij != null) {
                    return false;
                }
            } else if (!this.zzij.equals(com_google_android_gms_internal_zzaf_zzf.zzij)) {
                return false;
            }
            if (this.zzik == null) {
                if (com_google_android_gms_internal_zzaf_zzf.zzik != null) {
                    return false;
                }
            } else if (!this.zzik.equals(com_google_android_gms_internal_zzaf_zzf.zzik)) {
                return false;
            }
            if (this.version == null) {
                if (com_google_android_gms_internal_zzaf_zzf.version != null) {
                    return false;
                }
            } else if (!this.version.equals(com_google_android_gms_internal_zzaf_zzf.version)) {
                return false;
            }
            if (this.zzil == null) {
                if (com_google_android_gms_internal_zzaf_zzf.zzil != null) {
                    return false;
                }
            } else if (!this.zzil.equals(com_google_android_gms_internal_zzaf_zzf.zzil)) {
                return false;
            }
            return (Float.floatToIntBits(this.zzim) == Float.floatToIntBits(com_google_android_gms_internal_zzaf_zzf.zzim) && this.zzin == com_google_android_gms_internal_zzaf_zzf.zzin && zzrl.equals(this.zzio, com_google_android_gms_internal_zzaf_zzf.zzio) && this.zzip == com_google_android_gms_internal_zzaf_zzf.zzip) ? zza((zzrh) com_google_android_gms_internal_zzaf_zzf) : false;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.version == null ? 0 : this.version.hashCode()) + (((this.zzik == null ? 0 : this.zzik.hashCode()) + (((this.zzij == null ? 0 : this.zzij.hashCode()) + (((this.zzii == null ? 0 : this.zzii.hashCode()) + ((((((((((((((((zzrl.hashCode(this.zzia) + 527) * 31) + zzrl.hashCode(this.zzib)) * 31) + zzrl.hashCode(this.zzic)) * 31) + zzrl.hashCode(this.zzid)) * 31) + zzrl.hashCode(this.zzie)) * 31) + zzrl.hashCode(this.zzif)) * 31) + zzrl.hashCode(this.zzig)) * 31) + zzrl.hashCode(this.zzih)) * 31)) * 31)) * 31)) * 31)) * 31;
            if (this.zzil != null) {
                i = this.zzil.hashCode();
            }
            return (((((((this.zzin ? 1231 : 1237) + ((((hashCode + i) * 31) + Float.floatToIntBits(this.zzim)) * 31)) * 31) + zzrl.hashCode(this.zzio)) * 31) + this.zzip) * 31) + zzBI();
        }

        protected int zzB() {
            int i;
            int i2;
            int i3;
            int i4 = 0;
            int zzB = super.zzB();
            if (this.zzib == null || this.zzib.length <= 0) {
                i = zzB;
            } else {
                i2 = 0;
                i3 = 0;
                for (String str : this.zzib) {
                    if (str != null) {
                        i3++;
                        i2 += zzrg.zzfj(str);
                    }
                }
                i = (zzB + i2) + (i3 * 1);
            }
            if (this.zzic != null && this.zzic.length > 0) {
                i2 = i;
                for (zzrn com_google_android_gms_internal_zzrn : this.zzic) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        i2 += zzrg.zzc(2, com_google_android_gms_internal_zzrn);
                    }
                }
                i = i2;
            }
            if (this.zzid != null && this.zzid.length > 0) {
                i2 = i;
                for (zzrn com_google_android_gms_internal_zzrn2 : this.zzid) {
                    if (com_google_android_gms_internal_zzrn2 != null) {
                        i2 += zzrg.zzc(3, com_google_android_gms_internal_zzrn2);
                    }
                }
                i = i2;
            }
            if (this.zzie != null && this.zzie.length > 0) {
                i2 = i;
                for (zzrn com_google_android_gms_internal_zzrn22 : this.zzie) {
                    if (com_google_android_gms_internal_zzrn22 != null) {
                        i2 += zzrg.zzc(4, com_google_android_gms_internal_zzrn22);
                    }
                }
                i = i2;
            }
            if (this.zzif != null && this.zzif.length > 0) {
                i2 = i;
                for (zzrn com_google_android_gms_internal_zzrn222 : this.zzif) {
                    if (com_google_android_gms_internal_zzrn222 != null) {
                        i2 += zzrg.zzc(5, com_google_android_gms_internal_zzrn222);
                    }
                }
                i = i2;
            }
            if (this.zzig != null && this.zzig.length > 0) {
                i2 = i;
                for (zzrn com_google_android_gms_internal_zzrn2222 : this.zzig) {
                    if (com_google_android_gms_internal_zzrn2222 != null) {
                        i2 += zzrg.zzc(6, com_google_android_gms_internal_zzrn2222);
                    }
                }
                i = i2;
            }
            if (this.zzih != null && this.zzih.length > 0) {
                i2 = i;
                for (zzrn com_google_android_gms_internal_zzrn22222 : this.zzih) {
                    if (com_google_android_gms_internal_zzrn22222 != null) {
                        i2 += zzrg.zzc(7, com_google_android_gms_internal_zzrn22222);
                    }
                }
                i = i2;
            }
            if (!this.zzii.equals(BuildConfig.FLAVOR)) {
                i += zzrg.zzk(9, this.zzii);
            }
            if (!this.zzij.equals(BuildConfig.FLAVOR)) {
                i += zzrg.zzk(10, this.zzij);
            }
            if (!this.zzik.equals(HSConsts.STATUS_NEW)) {
                i += zzrg.zzk(12, this.zzik);
            }
            if (!this.version.equals(BuildConfig.FLAVOR)) {
                i += zzrg.zzk(13, this.version);
            }
            if (this.zzil != null) {
                i += zzrg.zzc(14, this.zzil);
            }
            if (Float.floatToIntBits(this.zzim) != Float.floatToIntBits(0.0f)) {
                i += zzrg.zzc(15, this.zzim);
            }
            if (this.zzio != null && this.zzio.length > 0) {
                i3 = 0;
                zzB = 0;
                for (String str2 : this.zzio) {
                    if (str2 != null) {
                        zzB++;
                        i3 += zzrg.zzfj(str2);
                    }
                }
                i = (i + i3) + (zzB * 2);
            }
            if (this.zzip != 0) {
                i += zzrg.zzA(17, this.zzip);
            }
            if (this.zzin) {
                i += zzrg.zzc(18, this.zzin);
            }
            if (this.zzia == null || this.zzia.length <= 0) {
                return i;
            }
            i2 = 0;
            i3 = 0;
            while (i4 < this.zzia.length) {
                String str3 = this.zzia[i4];
                if (str3 != null) {
                    i3++;
                    i2 += zzrg.zzfj(str3);
                }
                i4++;
            }
            return (i + i2) + (i3 * 2);
        }

        public zzf zzJ() {
            this.zzia = zzrq.zzaWm;
            this.zzib = zzrq.zzaWm;
            this.zzic = com.google.android.gms.internal.zzag.zza.zzQ();
            this.zzid = zze.zzH();
            this.zzie = zzb.zzC();
            this.zzif = zzb.zzC();
            this.zzig = zzb.zzC();
            this.zzih = zzg.zzK();
            this.zzii = BuildConfig.FLAVOR;
            this.zzij = BuildConfig.FLAVOR;
            this.zzik = HSConsts.STATUS_NEW;
            this.version = BuildConfig.FLAVOR;
            this.zzil = null;
            this.zzim = 0.0f;
            this.zzin = false;
            this.zzio = zzrq.zzaWm;
            this.zzip = 0;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            int i = 0;
            if (this.zzib != null && this.zzib.length > 0) {
                for (String str : this.zzib) {
                    if (str != null) {
                        com_google_android_gms_internal_zzrg.zzb(1, str);
                    }
                }
            }
            if (this.zzic != null && this.zzic.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn : this.zzic) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        com_google_android_gms_internal_zzrg.zza(2, com_google_android_gms_internal_zzrn);
                    }
                }
            }
            if (this.zzid != null && this.zzid.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn2 : this.zzid) {
                    if (com_google_android_gms_internal_zzrn2 != null) {
                        com_google_android_gms_internal_zzrg.zza(3, com_google_android_gms_internal_zzrn2);
                    }
                }
            }
            if (this.zzie != null && this.zzie.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn22 : this.zzie) {
                    if (com_google_android_gms_internal_zzrn22 != null) {
                        com_google_android_gms_internal_zzrg.zza(4, com_google_android_gms_internal_zzrn22);
                    }
                }
            }
            if (this.zzif != null && this.zzif.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn222 : this.zzif) {
                    if (com_google_android_gms_internal_zzrn222 != null) {
                        com_google_android_gms_internal_zzrg.zza(5, com_google_android_gms_internal_zzrn222);
                    }
                }
            }
            if (this.zzig != null && this.zzig.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn2222 : this.zzig) {
                    if (com_google_android_gms_internal_zzrn2222 != null) {
                        com_google_android_gms_internal_zzrg.zza(6, com_google_android_gms_internal_zzrn2222);
                    }
                }
            }
            if (this.zzih != null && this.zzih.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn22222 : this.zzih) {
                    if (com_google_android_gms_internal_zzrn22222 != null) {
                        com_google_android_gms_internal_zzrg.zza(7, com_google_android_gms_internal_zzrn22222);
                    }
                }
            }
            if (!this.zzii.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(9, this.zzii);
            }
            if (!this.zzij.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(10, this.zzij);
            }
            if (!this.zzik.equals(HSConsts.STATUS_NEW)) {
                com_google_android_gms_internal_zzrg.zzb(12, this.zzik);
            }
            if (!this.version.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(13, this.version);
            }
            if (this.zzil != null) {
                com_google_android_gms_internal_zzrg.zza(14, this.zzil);
            }
            if (Float.floatToIntBits(this.zzim) != Float.floatToIntBits(0.0f)) {
                com_google_android_gms_internal_zzrg.zzb(15, this.zzim);
            }
            if (this.zzio != null && this.zzio.length > 0) {
                for (String str2 : this.zzio) {
                    if (str2 != null) {
                        com_google_android_gms_internal_zzrg.zzb(16, str2);
                    }
                }
            }
            if (this.zzip != 0) {
                com_google_android_gms_internal_zzrg.zzy(17, this.zzip);
            }
            if (this.zzin) {
                com_google_android_gms_internal_zzrg.zzb(18, this.zzin);
            }
            if (this.zzia != null && this.zzia.length > 0) {
                while (i < this.zzia.length) {
                    String str3 = this.zzia[i];
                    if (str3 != null) {
                        com_google_android_gms_internal_zzrg.zzb(19, str3);
                    }
                    i++;
                }
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzg(com_google_android_gms_internal_zzrf);
        }

        public zzf zzg(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                int zzb;
                Object obj;
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case HTTP.LF /*10*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 10);
                        zzBr = this.zzib == null ? 0 : this.zzib.length;
                        obj = new String[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzib, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                        this.zzib = obj;
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 18);
                        zzBr = this.zzic == null ? 0 : this.zzic.length;
                        obj = new com.google.android.gms.internal.zzag.zza[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzic, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new com.google.android.gms.internal.zzag.zza();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new com.google.android.gms.internal.zzag.zza();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzic = obj;
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 26);
                        zzBr = this.zzid == null ? 0 : this.zzid.length;
                        obj = new zze[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzid, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zze();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zze();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzid = obj;
                        continue;
                    case R.styleable.Theme_actionModePasteDrawable /*34*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 34);
                        zzBr = this.zzie == null ? 0 : this.zzie.length;
                        obj = new zzb[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzie, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zzb();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zzb();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzie = obj;
                        continue;
                    case R.styleable.Theme_dialogTheme /*42*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 42);
                        zzBr = this.zzif == null ? 0 : this.zzif.length;
                        obj = new zzb[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzif, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zzb();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zzb();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzif = obj;
                        continue;
                    case R.styleable.Theme_buttonBarStyle /*50*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 50);
                        zzBr = this.zzig == null ? 0 : this.zzig.length;
                        obj = new zzb[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzig, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zzb();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zzb();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzig = obj;
                        continue;
                    case R.styleable.Theme_toolbarStyle /*58*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 58);
                        zzBr = this.zzih == null ? 0 : this.zzih.length;
                        obj = new zzg[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzih, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zzg();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zzg();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzih = obj;
                        continue;
                    case R.styleable.Theme_listPopupWindowStyle /*74*/:
                        this.zzii = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case R.styleable.Theme_colorPrimaryDark /*82*/:
                        this.zzij = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case R.styleable.Theme_autoCompleteTextViewStyle /*98*/:
                        this.zzik = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case R.styleable.Theme_spinnerStyle /*106*/:
                        this.version = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case 114:
                        if (this.zzil == null) {
                            this.zzil = new zza();
                        }
                        com_google_android_gms_internal_zzrf.zza(this.zzil);
                        continue;
                    case 125:
                        this.zzim = com_google_android_gms_internal_zzrf.readFloat();
                        continue;
                    case TransportMediator.KEYCODE_MEDIA_RECORD /*130*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, TransportMediator.KEYCODE_MEDIA_RECORD);
                        zzBr = this.zzio == null ? 0 : this.zzio.length;
                        obj = new String[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzio, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                        this.zzio = obj;
                        continue;
                    case 136:
                        this.zzip = com_google_android_gms_internal_zzrf.zzBu();
                        continue;
                    case 144:
                        this.zzin = com_google_android_gms_internal_zzrf.zzBv();
                        continue;
                    case 154:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 154);
                        zzBr = this.zzia == null ? 0 : this.zzia.length;
                        obj = new String[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzia, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                        this.zzia = obj;
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
    }

    public static final class zzg extends zzrh<zzg> {
        private static volatile zzg[] zziq;
        public int[] zziA;
        public int[] zzir;
        public int[] zzis;
        public int[] zzit;
        public int[] zziu;
        public int[] zziv;
        public int[] zziw;
        public int[] zzix;
        public int[] zziy;
        public int[] zziz;

        public zzg() {
            zzL();
        }

        public static zzg[] zzK() {
            if (zziq == null) {
                synchronized (zzrl.zzaWe) {
                    if (zziq == null) {
                        zziq = new zzg[0];
                    }
                }
            }
            return zziq;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzg)) {
                return false;
            }
            zzg com_google_android_gms_internal_zzaf_zzg = (zzg) o;
            return (zzrl.equals(this.zzir, com_google_android_gms_internal_zzaf_zzg.zzir) && zzrl.equals(this.zzis, com_google_android_gms_internal_zzaf_zzg.zzis) && zzrl.equals(this.zzit, com_google_android_gms_internal_zzaf_zzg.zzit) && zzrl.equals(this.zziu, com_google_android_gms_internal_zzaf_zzg.zziu) && zzrl.equals(this.zziv, com_google_android_gms_internal_zzaf_zzg.zziv) && zzrl.equals(this.zziw, com_google_android_gms_internal_zzaf_zzg.zziw) && zzrl.equals(this.zzix, com_google_android_gms_internal_zzaf_zzg.zzix) && zzrl.equals(this.zziy, com_google_android_gms_internal_zzaf_zzg.zziy) && zzrl.equals(this.zziz, com_google_android_gms_internal_zzaf_zzg.zziz) && zzrl.equals(this.zziA, com_google_android_gms_internal_zzaf_zzg.zziA)) ? zza((zzrh) com_google_android_gms_internal_zzaf_zzg) : false;
        }

        public int hashCode() {
            return ((((((((((((((((((((zzrl.hashCode(this.zzir) + 527) * 31) + zzrl.hashCode(this.zzis)) * 31) + zzrl.hashCode(this.zzit)) * 31) + zzrl.hashCode(this.zziu)) * 31) + zzrl.hashCode(this.zziv)) * 31) + zzrl.hashCode(this.zziw)) * 31) + zzrl.hashCode(this.zzix)) * 31) + zzrl.hashCode(this.zziy)) * 31) + zzrl.hashCode(this.zziz)) * 31) + zzrl.hashCode(this.zziA)) * 31) + zzBI();
        }

        protected int zzB() {
            int i;
            int i2;
            int i3 = 0;
            int zzB = super.zzB();
            if (this.zzir == null || this.zzir.length <= 0) {
                i = zzB;
            } else {
                i2 = 0;
                for (int zzkJ : this.zzir) {
                    i2 += zzrg.zzkJ(zzkJ);
                }
                i = (zzB + i2) + (this.zzir.length * 1);
            }
            if (this.zzis != null && this.zzis.length > 0) {
                zzB = 0;
                for (int zzkJ2 : this.zzis) {
                    zzB += zzrg.zzkJ(zzkJ2);
                }
                i = (i + zzB) + (this.zzis.length * 1);
            }
            if (this.zzit != null && this.zzit.length > 0) {
                zzB = 0;
                for (int zzkJ22 : this.zzit) {
                    zzB += zzrg.zzkJ(zzkJ22);
                }
                i = (i + zzB) + (this.zzit.length * 1);
            }
            if (this.zziu != null && this.zziu.length > 0) {
                zzB = 0;
                for (int zzkJ222 : this.zziu) {
                    zzB += zzrg.zzkJ(zzkJ222);
                }
                i = (i + zzB) + (this.zziu.length * 1);
            }
            if (this.zziv != null && this.zziv.length > 0) {
                zzB = 0;
                for (int zzkJ2222 : this.zziv) {
                    zzB += zzrg.zzkJ(zzkJ2222);
                }
                i = (i + zzB) + (this.zziv.length * 1);
            }
            if (this.zziw != null && this.zziw.length > 0) {
                zzB = 0;
                for (int zzkJ22222 : this.zziw) {
                    zzB += zzrg.zzkJ(zzkJ22222);
                }
                i = (i + zzB) + (this.zziw.length * 1);
            }
            if (this.zzix != null && this.zzix.length > 0) {
                zzB = 0;
                for (int zzkJ222222 : this.zzix) {
                    zzB += zzrg.zzkJ(zzkJ222222);
                }
                i = (i + zzB) + (this.zzix.length * 1);
            }
            if (this.zziy != null && this.zziy.length > 0) {
                zzB = 0;
                for (int zzkJ2222222 : this.zziy) {
                    zzB += zzrg.zzkJ(zzkJ2222222);
                }
                i = (i + zzB) + (this.zziy.length * 1);
            }
            if (this.zziz != null && this.zziz.length > 0) {
                zzB = 0;
                for (int zzkJ22222222 : this.zziz) {
                    zzB += zzrg.zzkJ(zzkJ22222222);
                }
                i = (i + zzB) + (this.zziz.length * 1);
            }
            if (this.zziA == null || this.zziA.length <= 0) {
                return i;
            }
            i2 = 0;
            while (i3 < this.zziA.length) {
                i2 += zzrg.zzkJ(this.zziA[i3]);
                i3++;
            }
            return (i + i2) + (this.zziA.length * 1);
        }

        public zzg zzL() {
            this.zzir = zzrq.zzaWh;
            this.zzis = zzrq.zzaWh;
            this.zzit = zzrq.zzaWh;
            this.zziu = zzrq.zzaWh;
            this.zziv = zzrq.zzaWh;
            this.zziw = zzrq.zzaWh;
            this.zzix = zzrq.zzaWh;
            this.zziy = zzrq.zzaWh;
            this.zziz = zzrq.zzaWh;
            this.zziA = zzrq.zzaWh;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            int i = 0;
            if (this.zzir != null && this.zzir.length > 0) {
                for (int zzy : this.zzir) {
                    com_google_android_gms_internal_zzrg.zzy(1, zzy);
                }
            }
            if (this.zzis != null && this.zzis.length > 0) {
                for (int zzy2 : this.zzis) {
                    com_google_android_gms_internal_zzrg.zzy(2, zzy2);
                }
            }
            if (this.zzit != null && this.zzit.length > 0) {
                for (int zzy22 : this.zzit) {
                    com_google_android_gms_internal_zzrg.zzy(3, zzy22);
                }
            }
            if (this.zziu != null && this.zziu.length > 0) {
                for (int zzy222 : this.zziu) {
                    com_google_android_gms_internal_zzrg.zzy(4, zzy222);
                }
            }
            if (this.zziv != null && this.zziv.length > 0) {
                for (int zzy2222 : this.zziv) {
                    com_google_android_gms_internal_zzrg.zzy(5, zzy2222);
                }
            }
            if (this.zziw != null && this.zziw.length > 0) {
                for (int zzy22222 : this.zziw) {
                    com_google_android_gms_internal_zzrg.zzy(6, zzy22222);
                }
            }
            if (this.zzix != null && this.zzix.length > 0) {
                for (int zzy222222 : this.zzix) {
                    com_google_android_gms_internal_zzrg.zzy(7, zzy222222);
                }
            }
            if (this.zziy != null && this.zziy.length > 0) {
                for (int zzy2222222 : this.zziy) {
                    com_google_android_gms_internal_zzrg.zzy(8, zzy2222222);
                }
            }
            if (this.zziz != null && this.zziz.length > 0) {
                for (int zzy22222222 : this.zziz) {
                    com_google_android_gms_internal_zzrg.zzy(9, zzy22222222);
                }
            }
            if (this.zziA != null && this.zziA.length > 0) {
                while (i < this.zziA.length) {
                    com_google_android_gms_internal_zzrg.zzy(10, this.zziA[i]);
                    i++;
                }
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzh(com_google_android_gms_internal_zzrf);
        }

        public zzg zzh(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                int zzb;
                Object obj;
                int zzkC;
                Object obj2;
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 8);
                        zzBr = this.zzir == null ? 0 : this.zzir.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzir, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zzir = obj;
                        continue;
                    case HTTP.LF /*10*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zzir == null ? 0 : this.zzir.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzir, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zzir = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 16);
                        zzBr = this.zzis == null ? 0 : this.zzis.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzis, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zzis = obj;
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zzis == null ? 0 : this.zzis.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzis, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zzis = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case R.styleable.Toolbar_subtitleTextColor /*24*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 24);
                        zzBr = this.zzit == null ? 0 : this.zzit.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzit, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zzit = obj;
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zzit == null ? 0 : this.zzit.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzit, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zzit = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case HTTP.SP /*32*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 32);
                        zzBr = this.zziu == null ? 0 : this.zziu.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziu, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziu = obj;
                        continue;
                    case R.styleable.Theme_actionModePasteDrawable /*34*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziu == null ? 0 : this.zziu.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziu, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziu = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case R.styleable.Theme_textAppearanceLargePopupMenu /*40*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 40);
                        zzBr = this.zziv == null ? 0 : this.zziv.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziv, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziv = obj;
                        continue;
                    case R.styleable.Theme_dialogTheme /*42*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziv == null ? 0 : this.zziv.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziv, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziv = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case R.styleable.Theme_homeAsUpIndicator /*48*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 48);
                        zzBr = this.zziw == null ? 0 : this.zziw.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziw, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziw = obj;
                        continue;
                    case R.styleable.Theme_buttonBarStyle /*50*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziw == null ? 0 : this.zziw.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziw, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziw = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case R.styleable.Theme_dividerHorizontal /*56*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 56);
                        zzBr = this.zzix == null ? 0 : this.zzix.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzix, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zzix = obj;
                        continue;
                    case R.styleable.Theme_toolbarStyle /*58*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zzix == null ? 0 : this.zzix.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzix, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zzix = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case SettingsJsonConstants.SETTINGS_MAX_CUSTOM_KEY_VALUE_PAIRS_DEFAULT /*64*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 64);
                        zzBr = this.zziy == null ? 0 : this.zziy.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziy, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziy = obj;
                        continue;
                    case R.styleable.Theme_textColorSearchUrl /*66*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziy == null ? 0 : this.zziy.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziy, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziy = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case R.styleable.Theme_listPreferredItemPaddingRight /*72*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 72);
                        zzBr = this.zziz == null ? 0 : this.zziz.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziz, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziz = obj;
                        continue;
                    case R.styleable.Theme_listPopupWindowStyle /*74*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziz == null ? 0 : this.zziz.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziz, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziz = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case R.styleable.Theme_listChoiceBackgroundIndicator /*80*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 80);
                        zzBr = this.zziA == null ? 0 : this.zziA.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziA, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziA = obj;
                        continue;
                    case R.styleable.Theme_colorPrimaryDark /*82*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziA == null ? 0 : this.zziA.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziA, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziA = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
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
    }

    public static final class zzh extends zzrh<zzh> {
        public static final zzri<com.google.android.gms.internal.zzag.zza, zzh> zziB = zzri.zza(11, zzh.class, 810);
        private static final zzh[] zziC = new zzh[0];
        public int[] zziD;
        public int[] zziE;
        public int[] zziF;
        public int zziG;
        public int[] zziH;
        public int zziI;
        public int zziJ;

        public zzh() {
            zzM();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzh)) {
                return false;
            }
            zzh com_google_android_gms_internal_zzaf_zzh = (zzh) o;
            return (zzrl.equals(this.zziD, com_google_android_gms_internal_zzaf_zzh.zziD) && zzrl.equals(this.zziE, com_google_android_gms_internal_zzaf_zzh.zziE) && zzrl.equals(this.zziF, com_google_android_gms_internal_zzaf_zzh.zziF) && this.zziG == com_google_android_gms_internal_zzaf_zzh.zziG && zzrl.equals(this.zziH, com_google_android_gms_internal_zzaf_zzh.zziH) && this.zziI == com_google_android_gms_internal_zzaf_zzh.zziI && this.zziJ == com_google_android_gms_internal_zzaf_zzh.zziJ) ? zza((zzrh) com_google_android_gms_internal_zzaf_zzh) : false;
        }

        public int hashCode() {
            return ((((((((((((((zzrl.hashCode(this.zziD) + 527) * 31) + zzrl.hashCode(this.zziE)) * 31) + zzrl.hashCode(this.zziF)) * 31) + this.zziG) * 31) + zzrl.hashCode(this.zziH)) * 31) + this.zziI) * 31) + this.zziJ) * 31) + zzBI();
        }

        protected int zzB() {
            int i;
            int i2;
            int i3 = 0;
            int zzB = super.zzB();
            if (this.zziD == null || this.zziD.length <= 0) {
                i = zzB;
            } else {
                i2 = 0;
                for (int zzkJ : this.zziD) {
                    i2 += zzrg.zzkJ(zzkJ);
                }
                i = (zzB + i2) + (this.zziD.length * 1);
            }
            if (this.zziE != null && this.zziE.length > 0) {
                zzB = 0;
                for (int zzkJ2 : this.zziE) {
                    zzB += zzrg.zzkJ(zzkJ2);
                }
                i = (i + zzB) + (this.zziE.length * 1);
            }
            if (this.zziF != null && this.zziF.length > 0) {
                zzB = 0;
                for (int zzkJ22 : this.zziF) {
                    zzB += zzrg.zzkJ(zzkJ22);
                }
                i = (i + zzB) + (this.zziF.length * 1);
            }
            if (this.zziG != 0) {
                i += zzrg.zzA(4, this.zziG);
            }
            if (this.zziH != null && this.zziH.length > 0) {
                i2 = 0;
                while (i3 < this.zziH.length) {
                    i2 += zzrg.zzkJ(this.zziH[i3]);
                    i3++;
                }
                i = (i + i2) + (this.zziH.length * 1);
            }
            if (this.zziI != 0) {
                i += zzrg.zzA(6, this.zziI);
            }
            return this.zziJ != 0 ? i + zzrg.zzA(7, this.zziJ) : i;
        }

        public zzh zzM() {
            this.zziD = zzrq.zzaWh;
            this.zziE = zzrq.zzaWh;
            this.zziF = zzrq.zzaWh;
            this.zziG = 0;
            this.zziH = zzrq.zzaWh;
            this.zziI = 0;
            this.zziJ = 0;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            int i = 0;
            if (this.zziD != null && this.zziD.length > 0) {
                for (int zzy : this.zziD) {
                    com_google_android_gms_internal_zzrg.zzy(1, zzy);
                }
            }
            if (this.zziE != null && this.zziE.length > 0) {
                for (int zzy2 : this.zziE) {
                    com_google_android_gms_internal_zzrg.zzy(2, zzy2);
                }
            }
            if (this.zziF != null && this.zziF.length > 0) {
                for (int zzy22 : this.zziF) {
                    com_google_android_gms_internal_zzrg.zzy(3, zzy22);
                }
            }
            if (this.zziG != 0) {
                com_google_android_gms_internal_zzrg.zzy(4, this.zziG);
            }
            if (this.zziH != null && this.zziH.length > 0) {
                while (i < this.zziH.length) {
                    com_google_android_gms_internal_zzrg.zzy(5, this.zziH[i]);
                    i++;
                }
            }
            if (this.zziI != 0) {
                com_google_android_gms_internal_zzrg.zzy(6, this.zziI);
            }
            if (this.zziJ != 0) {
                com_google_android_gms_internal_zzrg.zzy(7, this.zziJ);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzi(com_google_android_gms_internal_zzrf);
        }

        public zzh zzi(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                int zzb;
                Object obj;
                int zzkC;
                Object obj2;
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 8);
                        zzBr = this.zziD == null ? 0 : this.zziD.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziD, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziD = obj;
                        continue;
                    case HTTP.LF /*10*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziD == null ? 0 : this.zziD.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziD, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziD = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 16);
                        zzBr = this.zziE == null ? 0 : this.zziE.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziE, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziE = obj;
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziE == null ? 0 : this.zziE.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziE, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziE = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case R.styleable.Toolbar_subtitleTextColor /*24*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 24);
                        zzBr = this.zziF == null ? 0 : this.zziF.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziF, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziF = obj;
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziF == null ? 0 : this.zziF.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziF, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziF = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case HTTP.SP /*32*/:
                        this.zziG = com_google_android_gms_internal_zzrf.zzBu();
                        continue;
                    case R.styleable.Theme_textAppearanceLargePopupMenu /*40*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 40);
                        zzBr = this.zziH == null ? 0 : this.zziH.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziH, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zziH = obj;
                        continue;
                    case R.styleable.Theme_dialogTheme /*42*/:
                        zzkC = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            com_google_android_gms_internal_zzrf.zzBu();
                            zzBr++;
                        }
                        com_google_android_gms_internal_zzrf.zzkE(zzb);
                        zzb = this.zziH == null ? 0 : this.zziH.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zziH, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zziH = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case R.styleable.Theme_homeAsUpIndicator /*48*/:
                        this.zziI = com_google_android_gms_internal_zzrf.zzBu();
                        continue;
                    case R.styleable.Theme_dividerHorizontal /*56*/:
                        this.zziJ = com_google_android_gms_internal_zzrf.zzBu();
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
    }

    public static final class zzi extends zzrh<zzi> {
        private static volatile zzi[] zziK;
        public String name;
        public com.google.android.gms.internal.zzag.zza zziL;
        public zzd zziM;

        public zzi() {
            zzO();
        }

        public static zzi[] zzN() {
            if (zziK == null) {
                synchronized (zzrl.zzaWe) {
                    if (zziK == null) {
                        zziK = new zzi[0];
                    }
                }
            }
            return zziK;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzi)) {
                return false;
            }
            zzi com_google_android_gms_internal_zzaf_zzi = (zzi) o;
            if (this.name == null) {
                if (com_google_android_gms_internal_zzaf_zzi.name != null) {
                    return false;
                }
            } else if (!this.name.equals(com_google_android_gms_internal_zzaf_zzi.name)) {
                return false;
            }
            if (this.zziL == null) {
                if (com_google_android_gms_internal_zzaf_zzi.zziL != null) {
                    return false;
                }
            } else if (!this.zziL.equals(com_google_android_gms_internal_zzaf_zzi.zziL)) {
                return false;
            }
            if (this.zziM == null) {
                if (com_google_android_gms_internal_zzaf_zzi.zziM != null) {
                    return false;
                }
            } else if (!this.zziM.equals(com_google_android_gms_internal_zzaf_zzi.zziM)) {
                return false;
            }
            return zza((zzrh) com_google_android_gms_internal_zzaf_zzi);
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.zziL == null ? 0 : this.zziL.hashCode()) + (((this.name == null ? 0 : this.name.hashCode()) + 527) * 31)) * 31;
            if (this.zziM != null) {
                i = this.zziM.hashCode();
            }
            return ((hashCode + i) * 31) + zzBI();
        }

        protected int zzB() {
            int zzB = super.zzB();
            if (!this.name.equals(BuildConfig.FLAVOR)) {
                zzB += zzrg.zzk(1, this.name);
            }
            if (this.zziL != null) {
                zzB += zzrg.zzc(2, this.zziL);
            }
            return this.zziM != null ? zzB + zzrg.zzc(3, this.zziM) : zzB;
        }

        public zzi zzO() {
            this.name = BuildConfig.FLAVOR;
            this.zziL = null;
            this.zziM = null;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            if (!this.name.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(1, this.name);
            }
            if (this.zziL != null) {
                com_google_android_gms_internal_zzrg.zza(2, this.zziL);
            }
            if (this.zziM != null) {
                com_google_android_gms_internal_zzrg.zza(3, this.zziM);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzj(com_google_android_gms_internal_zzrf);
        }

        public zzi zzj(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case HTTP.LF /*10*/:
                        this.name = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        if (this.zziL == null) {
                            this.zziL = new com.google.android.gms.internal.zzag.zza();
                        }
                        com_google_android_gms_internal_zzrf.zza(this.zziL);
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        if (this.zziM == null) {
                            this.zziM = new zzd();
                        }
                        com_google_android_gms_internal_zzrf.zza(this.zziM);
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
    }

    public static final class zzj extends zzrh<zzj> {
        public zzi[] zziN;
        public zzf zziO;
        public String zziP;

        public zzj() {
            zzP();
        }

        public static zzj zzd(byte[] bArr) throws zzrm {
            return (zzj) zzrn.zza(new zzj(), bArr);
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzj)) {
                return false;
            }
            zzj com_google_android_gms_internal_zzaf_zzj = (zzj) o;
            if (!zzrl.equals(this.zziN, com_google_android_gms_internal_zzaf_zzj.zziN)) {
                return false;
            }
            if (this.zziO == null) {
                if (com_google_android_gms_internal_zzaf_zzj.zziO != null) {
                    return false;
                }
            } else if (!this.zziO.equals(com_google_android_gms_internal_zzaf_zzj.zziO)) {
                return false;
            }
            if (this.zziP == null) {
                if (com_google_android_gms_internal_zzaf_zzj.zziP != null) {
                    return false;
                }
            } else if (!this.zziP.equals(com_google_android_gms_internal_zzaf_zzj.zziP)) {
                return false;
            }
            return zza((zzrh) com_google_android_gms_internal_zzaf_zzj);
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.zziO == null ? 0 : this.zziO.hashCode()) + ((zzrl.hashCode(this.zziN) + 527) * 31)) * 31;
            if (this.zziP != null) {
                i = this.zziP.hashCode();
            }
            return ((hashCode + i) * 31) + zzBI();
        }

        protected int zzB() {
            int zzB = super.zzB();
            if (this.zziN != null && this.zziN.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn : this.zziN) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        zzB += zzrg.zzc(1, com_google_android_gms_internal_zzrn);
                    }
                }
            }
            if (this.zziO != null) {
                zzB += zzrg.zzc(2, this.zziO);
            }
            return !this.zziP.equals(BuildConfig.FLAVOR) ? zzB + zzrg.zzk(3, this.zziP) : zzB;
        }

        public zzj zzP() {
            this.zziN = zzi.zzN();
            this.zziO = null;
            this.zziP = BuildConfig.FLAVOR;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            if (this.zziN != null && this.zziN.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn : this.zziN) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        com_google_android_gms_internal_zzrg.zza(1, com_google_android_gms_internal_zzrn);
                    }
                }
            }
            if (this.zziO != null) {
                com_google_android_gms_internal_zzrg.zza(2, this.zziO);
            }
            if (!this.zziP.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(3, this.zziP);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzk(com_google_android_gms_internal_zzrf);
        }

        public zzj zzk(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case HTTP.LF /*10*/:
                        int zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 10);
                        zzBr = this.zziN == null ? 0 : this.zziN.length;
                        Object obj = new zzi[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziN, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zzi();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zzi();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zziN = obj;
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        if (this.zziO == null) {
                            this.zziO = new zzf();
                        }
                        com_google_android_gms_internal_zzrf.zza(this.zziO);
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        this.zziP = com_google_android_gms_internal_zzrf.readString();
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
    }
}
