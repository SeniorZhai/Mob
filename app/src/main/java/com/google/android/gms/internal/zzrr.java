package com.google.android.gms.internal;

import android.support.v4.media.TransportMediator;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.util.Arrays;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.LangUtils;

public interface zzrr {

    public static final class zza extends zzrh<zza> {
        public String[] zzaWp;
        public String[] zzaWq;
        public int[] zzaWr;
        public int[] zzaWs;

        public zza() {
            zzBW();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zza)) {
                return false;
            }
            zza com_google_android_gms_internal_zzrr_zza = (zza) o;
            return (zzrl.equals(this.zzaWp, com_google_android_gms_internal_zzrr_zza.zzaWp) && zzrl.equals(this.zzaWq, com_google_android_gms_internal_zzrr_zza.zzaWq) && zzrl.equals(this.zzaWr, com_google_android_gms_internal_zzrr_zza.zzaWr) && zzrl.equals(this.zzaWs, com_google_android_gms_internal_zzrr_zza.zzaWs)) ? zza((zzrh) com_google_android_gms_internal_zzrr_zza) : false;
        }

        public int hashCode() {
            return ((((((((zzrl.hashCode(this.zzaWp) + 527) * 31) + zzrl.hashCode(this.zzaWq)) * 31) + zzrl.hashCode(this.zzaWr)) * 31) + zzrl.hashCode(this.zzaWs)) * 31) + zzBI();
        }

        protected int zzB() {
            int i;
            int i2;
            int i3;
            int i4 = 0;
            int zzB = super.zzB();
            if (this.zzaWp == null || this.zzaWp.length <= 0) {
                i = zzB;
            } else {
                i2 = 0;
                i3 = 0;
                for (String str : this.zzaWp) {
                    if (str != null) {
                        i3++;
                        i2 += zzrg.zzfj(str);
                    }
                }
                i = (zzB + i2) + (i3 * 1);
            }
            if (this.zzaWq != null && this.zzaWq.length > 0) {
                i3 = 0;
                zzB = 0;
                for (String str2 : this.zzaWq) {
                    if (str2 != null) {
                        zzB++;
                        i3 += zzrg.zzfj(str2);
                    }
                }
                i = (i + i3) + (zzB * 1);
            }
            if (this.zzaWr != null && this.zzaWr.length > 0) {
                i3 = 0;
                for (int zzB2 : this.zzaWr) {
                    i3 += zzrg.zzkJ(zzB2);
                }
                i = (i + i3) + (this.zzaWr.length * 1);
            }
            if (this.zzaWs == null || this.zzaWs.length <= 0) {
                return i;
            }
            i2 = 0;
            while (i4 < this.zzaWs.length) {
                i2 += zzrg.zzkJ(this.zzaWs[i4]);
                i4++;
            }
            return (i + i2) + (this.zzaWs.length * 1);
        }

        public zza zzB(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                int zzb;
                Object obj;
                int zzkC;
                Object obj2;
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case HTTP.LF /*10*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 10);
                        zzBr = this.zzaWp == null ? 0 : this.zzaWp.length;
                        obj = new String[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzaWp, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                        this.zzaWp = obj;
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 18);
                        zzBr = this.zzaWq == null ? 0 : this.zzaWq.length;
                        obj = new String[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzaWq, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.readString();
                        this.zzaWq = obj;
                        continue;
                    case R.styleable.Toolbar_subtitleTextColor /*24*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 24);
                        zzBr = this.zzaWr == null ? 0 : this.zzaWr.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzaWr, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zzaWr = obj;
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
                        zzb = this.zzaWr == null ? 0 : this.zzaWr.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzaWr, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zzaWr = obj2;
                        com_google_android_gms_internal_zzrf.zzkD(zzkC);
                        continue;
                    case HTTP.SP /*32*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 32);
                        zzBr = this.zzaWs == null ? 0 : this.zzaWs.length;
                        obj = new int[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzaWs, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.zzBu();
                        this.zzaWs = obj;
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
                        zzb = this.zzaWs == null ? 0 : this.zzaWs.length;
                        obj2 = new int[(zzBr + zzb)];
                        if (zzb != 0) {
                            System.arraycopy(this.zzaWs, 0, obj2, 0, zzb);
                        }
                        while (zzb < obj2.length) {
                            obj2[zzb] = com_google_android_gms_internal_zzrf.zzBu();
                            zzb++;
                        }
                        this.zzaWs = obj2;
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

        public zza zzBW() {
            this.zzaWp = zzrq.zzaWm;
            this.zzaWq = zzrq.zzaWm;
            this.zzaWr = zzrq.zzaWh;
            this.zzaWs = zzrq.zzaWh;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            int i = 0;
            if (this.zzaWp != null && this.zzaWp.length > 0) {
                for (String str : this.zzaWp) {
                    if (str != null) {
                        com_google_android_gms_internal_zzrg.zzb(1, str);
                    }
                }
            }
            if (this.zzaWq != null && this.zzaWq.length > 0) {
                for (String str2 : this.zzaWq) {
                    if (str2 != null) {
                        com_google_android_gms_internal_zzrg.zzb(2, str2);
                    }
                }
            }
            if (this.zzaWr != null && this.zzaWr.length > 0) {
                for (int zzy : this.zzaWr) {
                    com_google_android_gms_internal_zzrg.zzy(3, zzy);
                }
            }
            if (this.zzaWs != null && this.zzaWs.length > 0) {
                while (i < this.zzaWs.length) {
                    com_google_android_gms_internal_zzrg.zzy(4, this.zzaWs[i]);
                    i++;
                }
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzB(com_google_android_gms_internal_zzrf);
        }
    }

    public static final class zzb extends zzrh<zzb> {
        public String version;
        public int zzaWt;
        public String zzaWu;

        public zzb() {
            zzBX();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzb)) {
                return false;
            }
            zzb com_google_android_gms_internal_zzrr_zzb = (zzb) o;
            if (this.zzaWt != com_google_android_gms_internal_zzrr_zzb.zzaWt) {
                return false;
            }
            if (this.zzaWu == null) {
                if (com_google_android_gms_internal_zzrr_zzb.zzaWu != null) {
                    return false;
                }
            } else if (!this.zzaWu.equals(com_google_android_gms_internal_zzrr_zzb.zzaWu)) {
                return false;
            }
            if (this.version == null) {
                if (com_google_android_gms_internal_zzrr_zzb.version != null) {
                    return false;
                }
            } else if (!this.version.equals(com_google_android_gms_internal_zzrr_zzb.version)) {
                return false;
            }
            return zza((zzrh) com_google_android_gms_internal_zzrr_zzb);
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.zzaWu == null ? 0 : this.zzaWu.hashCode()) + ((this.zzaWt + 527) * 31)) * 31;
            if (this.version != null) {
                i = this.version.hashCode();
            }
            return ((hashCode + i) * 31) + zzBI();
        }

        protected int zzB() {
            int zzB = super.zzB();
            if (this.zzaWt != 0) {
                zzB += zzrg.zzA(1, this.zzaWt);
            }
            if (!this.zzaWu.equals(BuildConfig.FLAVOR)) {
                zzB += zzrg.zzk(2, this.zzaWu);
            }
            return !this.version.equals(BuildConfig.FLAVOR) ? zzB + zzrg.zzk(3, this.version) : zzB;
        }

        public zzb zzBX() {
            this.zzaWt = 0;
            this.zzaWu = BuildConfig.FLAVOR;
            this.version = BuildConfig.FLAVOR;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public zzb zzC(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        zzBr = com_google_android_gms_internal_zzrf.zzBu();
                        switch (zzBr) {
                            case ResponseParser.ResponseActionDiscard /*0*/:
                            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                            case Player.STATE_ENDED /*5*/:
                            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                            case DayPickerView.DAYS_PER_WEEK /*7*/:
                            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                            case HTTP.HT /*9*/:
                            case HTTP.LF /*10*/:
                            case R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                            case R.styleable.Toolbar_titleMargins /*12*/:
                            case HTTP.CR /*13*/:
                            case R.styleable.Toolbar_titleMarginEnd /*14*/:
                            case R.styleable.Toolbar_titleMarginTop /*15*/:
                            case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                            case LangUtils.HASH_SEED /*17*/:
                            case R.styleable.Toolbar_collapseIcon /*18*/:
                            case R.styleable.Toolbar_collapseContentDescription /*19*/:
                            case R.styleable.Toolbar_navigationIcon /*20*/:
                            case R.styleable.Toolbar_navigationContentDescription /*21*/:
                            case R.styleable.Toolbar_logoDescription /*22*/:
                            case R.styleable.Toolbar_titleTextColor /*23*/:
                            case R.styleable.Toolbar_subtitleTextColor /*24*/:
                            case R.styleable.Theme_actionMenuTextAppearance /*25*/:
                            case R.styleable.Theme_actionMenuTextColor /*26*/:
                                this.zzaWt = zzBr;
                                break;
                            default:
                                continue;
                        }
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        this.zzaWu = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        this.version = com_google_android_gms_internal_zzrf.readString();
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
            if (this.zzaWt != 0) {
                com_google_android_gms_internal_zzrg.zzy(1, this.zzaWt);
            }
            if (!this.zzaWu.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(2, this.zzaWu);
            }
            if (!this.version.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(3, this.version);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzC(com_google_android_gms_internal_zzrf);
        }
    }

    public static final class zzc extends zzrh<zzc> {
        public byte[] zzaWv;
        public byte[][] zzaWw;
        public boolean zzaWx;

        public zzc() {
            zzBY();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzc)) {
                return false;
            }
            zzc com_google_android_gms_internal_zzrr_zzc = (zzc) o;
            return (Arrays.equals(this.zzaWv, com_google_android_gms_internal_zzrr_zzc.zzaWv) && zzrl.zza(this.zzaWw, com_google_android_gms_internal_zzrr_zzc.zzaWw) && this.zzaWx == com_google_android_gms_internal_zzrr_zzc.zzaWx) ? zza((zzrh) com_google_android_gms_internal_zzrr_zzc) : false;
        }

        public int hashCode() {
            return (((this.zzaWx ? 1231 : 1237) + ((((Arrays.hashCode(this.zzaWv) + 527) * 31) + zzrl.zza(this.zzaWw)) * 31)) * 31) + zzBI();
        }

        protected int zzB() {
            int i = 0;
            int zzB = super.zzB();
            if (!Arrays.equals(this.zzaWv, zzrq.zzaWo)) {
                zzB += zzrg.zzb(1, this.zzaWv);
            }
            if (this.zzaWw != null && this.zzaWw.length > 0) {
                int i2 = 0;
                int i3 = 0;
                while (i < this.zzaWw.length) {
                    byte[] bArr = this.zzaWw[i];
                    if (bArr != null) {
                        i3++;
                        i2 += zzrg.zzC(bArr);
                    }
                    i++;
                }
                zzB = (zzB + i2) + (i3 * 1);
            }
            return this.zzaWx ? zzB + zzrg.zzc(3, this.zzaWx) : zzB;
        }

        public zzc zzBY() {
            this.zzaWv = zzrq.zzaWo;
            this.zzaWw = zzrq.zzaWn;
            this.zzaWx = false;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public zzc zzD(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case HTTP.LF /*10*/:
                        this.zzaWv = com_google_android_gms_internal_zzrf.readBytes();
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        int zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 18);
                        zzBr = this.zzaWw == null ? 0 : this.zzaWw.length;
                        Object obj = new byte[(zzb + zzBr)][];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzaWw, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = com_google_android_gms_internal_zzrf.readBytes();
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = com_google_android_gms_internal_zzrf.readBytes();
                        this.zzaWw = obj;
                        continue;
                    case R.styleable.Toolbar_subtitleTextColor /*24*/:
                        this.zzaWx = com_google_android_gms_internal_zzrf.zzBv();
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
            if (!Arrays.equals(this.zzaWv, zzrq.zzaWo)) {
                com_google_android_gms_internal_zzrg.zza(1, this.zzaWv);
            }
            if (this.zzaWw != null && this.zzaWw.length > 0) {
                for (byte[] bArr : this.zzaWw) {
                    if (bArr != null) {
                        com_google_android_gms_internal_zzrg.zza(2, bArr);
                    }
                }
            }
            if (this.zzaWx) {
                com_google_android_gms_internal_zzrg.zzb(3, this.zzaWx);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzD(com_google_android_gms_internal_zzrf);
        }
    }

    public static final class zzd extends zzrh<zzd> {
        public String tag;
        public int zzaWA;
        public int zzaWB;
        public boolean zzaWC;
        public zze[] zzaWD;
        public zzb zzaWE;
        public byte[] zzaWF;
        public byte[] zzaWG;
        public byte[] zzaWH;
        public zza zzaWI;
        public String zzaWJ;
        public long zzaWK;
        public zzc zzaWL;
        public byte[] zzaWM;
        public long zzaWy;
        public long zzaWz;

        public zzd() {
            zzBZ();
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zzd)) {
                return false;
            }
            zzd com_google_android_gms_internal_zzrr_zzd = (zzd) o;
            if (this.zzaWy != com_google_android_gms_internal_zzrr_zzd.zzaWy || this.zzaWz != com_google_android_gms_internal_zzrr_zzd.zzaWz) {
                return false;
            }
            if (this.tag == null) {
                if (com_google_android_gms_internal_zzrr_zzd.tag != null) {
                    return false;
                }
            } else if (!this.tag.equals(com_google_android_gms_internal_zzrr_zzd.tag)) {
                return false;
            }
            if (this.zzaWA != com_google_android_gms_internal_zzrr_zzd.zzaWA || this.zzaWB != com_google_android_gms_internal_zzrr_zzd.zzaWB || this.zzaWC != com_google_android_gms_internal_zzrr_zzd.zzaWC || !zzrl.equals(this.zzaWD, com_google_android_gms_internal_zzrr_zzd.zzaWD)) {
                return false;
            }
            if (this.zzaWE == null) {
                if (com_google_android_gms_internal_zzrr_zzd.zzaWE != null) {
                    return false;
                }
            } else if (!this.zzaWE.equals(com_google_android_gms_internal_zzrr_zzd.zzaWE)) {
                return false;
            }
            if (!Arrays.equals(this.zzaWF, com_google_android_gms_internal_zzrr_zzd.zzaWF) || !Arrays.equals(this.zzaWG, com_google_android_gms_internal_zzrr_zzd.zzaWG) || !Arrays.equals(this.zzaWH, com_google_android_gms_internal_zzrr_zzd.zzaWH)) {
                return false;
            }
            if (this.zzaWI == null) {
                if (com_google_android_gms_internal_zzrr_zzd.zzaWI != null) {
                    return false;
                }
            } else if (!this.zzaWI.equals(com_google_android_gms_internal_zzrr_zzd.zzaWI)) {
                return false;
            }
            if (this.zzaWJ == null) {
                if (com_google_android_gms_internal_zzrr_zzd.zzaWJ != null) {
                    return false;
                }
            } else if (!this.zzaWJ.equals(com_google_android_gms_internal_zzrr_zzd.zzaWJ)) {
                return false;
            }
            if (this.zzaWK != com_google_android_gms_internal_zzrr_zzd.zzaWK) {
                return false;
            }
            if (this.zzaWL == null) {
                if (com_google_android_gms_internal_zzrr_zzd.zzaWL != null) {
                    return false;
                }
            } else if (!this.zzaWL.equals(com_google_android_gms_internal_zzrr_zzd.zzaWL)) {
                return false;
            }
            return Arrays.equals(this.zzaWM, com_google_android_gms_internal_zzrr_zzd.zzaWM) ? zza((zzrh) com_google_android_gms_internal_zzrr_zzd) : false;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((((this.zzaWJ == null ? 0 : this.zzaWJ.hashCode()) + (((this.zzaWI == null ? 0 : this.zzaWI.hashCode()) + (((((((((this.zzaWE == null ? 0 : this.zzaWE.hashCode()) + (((((this.zzaWC ? 1231 : 1237) + (((((((this.tag == null ? 0 : this.tag.hashCode()) + ((((((int) (this.zzaWy ^ (this.zzaWy >>> 32))) + 527) * 31) + ((int) (this.zzaWz ^ (this.zzaWz >>> 32)))) * 31)) * 31) + this.zzaWA) * 31) + this.zzaWB) * 31)) * 31) + zzrl.hashCode(this.zzaWD)) * 31)) * 31) + Arrays.hashCode(this.zzaWF)) * 31) + Arrays.hashCode(this.zzaWG)) * 31) + Arrays.hashCode(this.zzaWH)) * 31)) * 31)) * 31) + ((int) (this.zzaWK ^ (this.zzaWK >>> 32)))) * 31;
            if (this.zzaWL != null) {
                i = this.zzaWL.hashCode();
            }
            return ((((hashCode + i) * 31) + Arrays.hashCode(this.zzaWM)) * 31) + zzBI();
        }

        protected int zzB() {
            int zzB = super.zzB();
            if (this.zzaWy != 0) {
                zzB += zzrg.zzd(1, this.zzaWy);
            }
            if (!this.tag.equals(BuildConfig.FLAVOR)) {
                zzB += zzrg.zzk(2, this.tag);
            }
            if (this.zzaWD != null && this.zzaWD.length > 0) {
                int i = zzB;
                for (zzrn com_google_android_gms_internal_zzrn : this.zzaWD) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        i += zzrg.zzc(3, com_google_android_gms_internal_zzrn);
                    }
                }
                zzB = i;
            }
            if (!Arrays.equals(this.zzaWF, zzrq.zzaWo)) {
                zzB += zzrg.zzb(6, this.zzaWF);
            }
            if (this.zzaWI != null) {
                zzB += zzrg.zzc(7, this.zzaWI);
            }
            if (!Arrays.equals(this.zzaWG, zzrq.zzaWo)) {
                zzB += zzrg.zzb(8, this.zzaWG);
            }
            if (this.zzaWE != null) {
                zzB += zzrg.zzc(9, this.zzaWE);
            }
            if (this.zzaWC) {
                zzB += zzrg.zzc(10, this.zzaWC);
            }
            if (this.zzaWA != 0) {
                zzB += zzrg.zzA(11, this.zzaWA);
            }
            if (this.zzaWB != 0) {
                zzB += zzrg.zzA(12, this.zzaWB);
            }
            if (!Arrays.equals(this.zzaWH, zzrq.zzaWo)) {
                zzB += zzrg.zzb(13, this.zzaWH);
            }
            if (!this.zzaWJ.equals(BuildConfig.FLAVOR)) {
                zzB += zzrg.zzk(14, this.zzaWJ);
            }
            if (this.zzaWK != 180000) {
                zzB += zzrg.zze(15, this.zzaWK);
            }
            if (this.zzaWL != null) {
                zzB += zzrg.zzc(16, this.zzaWL);
            }
            if (this.zzaWz != 0) {
                zzB += zzrg.zzd(17, this.zzaWz);
            }
            return !Arrays.equals(this.zzaWM, zzrq.zzaWo) ? zzB + zzrg.zzb(18, this.zzaWM) : zzB;
        }

        public zzd zzBZ() {
            this.zzaWy = 0;
            this.zzaWz = 0;
            this.tag = BuildConfig.FLAVOR;
            this.zzaWA = 0;
            this.zzaWB = 0;
            this.zzaWC = false;
            this.zzaWD = zze.zzCa();
            this.zzaWE = null;
            this.zzaWF = zzrq.zzaWo;
            this.zzaWG = zzrq.zzaWo;
            this.zzaWH = zzrq.zzaWo;
            this.zzaWI = null;
            this.zzaWJ = BuildConfig.FLAVOR;
            this.zzaWK = 180000;
            this.zzaWL = null;
            this.zzaWM = zzrq.zzaWo;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public zzd zzE(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        this.zzaWy = com_google_android_gms_internal_zzrf.zzBt();
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        this.tag = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        int zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 26);
                        zzBr = this.zzaWD == null ? 0 : this.zzaWD.length;
                        Object obj = new zze[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zzaWD, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zze();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zze();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zzaWD = obj;
                        continue;
                    case R.styleable.Theme_buttonBarStyle /*50*/:
                        this.zzaWF = com_google_android_gms_internal_zzrf.readBytes();
                        continue;
                    case R.styleable.Theme_toolbarStyle /*58*/:
                        if (this.zzaWI == null) {
                            this.zzaWI = new zza();
                        }
                        com_google_android_gms_internal_zzrf.zza(this.zzaWI);
                        continue;
                    case R.styleable.Theme_textColorSearchUrl /*66*/:
                        this.zzaWG = com_google_android_gms_internal_zzrf.readBytes();
                        continue;
                    case R.styleable.Theme_listPopupWindowStyle /*74*/:
                        if (this.zzaWE == null) {
                            this.zzaWE = new zzb();
                        }
                        com_google_android_gms_internal_zzrf.zza(this.zzaWE);
                        continue;
                    case R.styleable.Theme_listChoiceBackgroundIndicator /*80*/:
                        this.zzaWC = com_google_android_gms_internal_zzrf.zzBv();
                        continue;
                    case R.styleable.Theme_colorSwitchThumbNormal /*88*/:
                        this.zzaWA = com_google_android_gms_internal_zzrf.zzBu();
                        continue;
                    case R.styleable.Theme_buttonBarNegativeButtonStyle /*96*/:
                        this.zzaWB = com_google_android_gms_internal_zzrf.zzBu();
                        continue;
                    case R.styleable.Theme_spinnerStyle /*106*/:
                        this.zzaWH = com_google_android_gms_internal_zzrf.readBytes();
                        continue;
                    case 114:
                        this.zzaWJ = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case 120:
                        this.zzaWK = com_google_android_gms_internal_zzrf.zzBx();
                        continue;
                    case TransportMediator.KEYCODE_MEDIA_RECORD /*130*/:
                        if (this.zzaWL == null) {
                            this.zzaWL = new zzc();
                        }
                        com_google_android_gms_internal_zzrf.zza(this.zzaWL);
                        continue;
                    case 136:
                        this.zzaWz = com_google_android_gms_internal_zzrf.zzBt();
                        continue;
                    case 146:
                        this.zzaWM = com_google_android_gms_internal_zzrf.readBytes();
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
            if (this.zzaWy != 0) {
                com_google_android_gms_internal_zzrg.zzb(1, this.zzaWy);
            }
            if (!this.tag.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(2, this.tag);
            }
            if (this.zzaWD != null && this.zzaWD.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn : this.zzaWD) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        com_google_android_gms_internal_zzrg.zza(3, com_google_android_gms_internal_zzrn);
                    }
                }
            }
            if (!Arrays.equals(this.zzaWF, zzrq.zzaWo)) {
                com_google_android_gms_internal_zzrg.zza(6, this.zzaWF);
            }
            if (this.zzaWI != null) {
                com_google_android_gms_internal_zzrg.zza(7, this.zzaWI);
            }
            if (!Arrays.equals(this.zzaWG, zzrq.zzaWo)) {
                com_google_android_gms_internal_zzrg.zza(8, this.zzaWG);
            }
            if (this.zzaWE != null) {
                com_google_android_gms_internal_zzrg.zza(9, this.zzaWE);
            }
            if (this.zzaWC) {
                com_google_android_gms_internal_zzrg.zzb(10, this.zzaWC);
            }
            if (this.zzaWA != 0) {
                com_google_android_gms_internal_zzrg.zzy(11, this.zzaWA);
            }
            if (this.zzaWB != 0) {
                com_google_android_gms_internal_zzrg.zzy(12, this.zzaWB);
            }
            if (!Arrays.equals(this.zzaWH, zzrq.zzaWo)) {
                com_google_android_gms_internal_zzrg.zza(13, this.zzaWH);
            }
            if (!this.zzaWJ.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(14, this.zzaWJ);
            }
            if (this.zzaWK != 180000) {
                com_google_android_gms_internal_zzrg.zzc(15, this.zzaWK);
            }
            if (this.zzaWL != null) {
                com_google_android_gms_internal_zzrg.zza(16, this.zzaWL);
            }
            if (this.zzaWz != 0) {
                com_google_android_gms_internal_zzrg.zzb(17, this.zzaWz);
            }
            if (!Arrays.equals(this.zzaWM, zzrq.zzaWo)) {
                com_google_android_gms_internal_zzrg.zza(18, this.zzaWM);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzE(com_google_android_gms_internal_zzrf);
        }
    }

    public static final class zze extends zzrh<zze> {
        private static volatile zze[] zzaWN;
        public String value;
        public String zzaC;

        public zze() {
            zzCb();
        }

        public static zze[] zzCa() {
            if (zzaWN == null) {
                synchronized (zzrl.zzaWe) {
                    if (zzaWN == null) {
                        zzaWN = new zze[0];
                    }
                }
            }
            return zzaWN;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zze)) {
                return false;
            }
            zze com_google_android_gms_internal_zzrr_zze = (zze) o;
            if (this.zzaC == null) {
                if (com_google_android_gms_internal_zzrr_zze.zzaC != null) {
                    return false;
                }
            } else if (!this.zzaC.equals(com_google_android_gms_internal_zzrr_zze.zzaC)) {
                return false;
            }
            if (this.value == null) {
                if (com_google_android_gms_internal_zzrr_zze.value != null) {
                    return false;
                }
            } else if (!this.value.equals(com_google_android_gms_internal_zzrr_zze.value)) {
                return false;
            }
            return zza((zzrh) com_google_android_gms_internal_zzrr_zze);
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.zzaC == null ? 0 : this.zzaC.hashCode()) + 527) * 31;
            if (this.value != null) {
                i = this.value.hashCode();
            }
            return ((hashCode + i) * 31) + zzBI();
        }

        protected int zzB() {
            int zzB = super.zzB();
            if (!this.zzaC.equals(BuildConfig.FLAVOR)) {
                zzB += zzrg.zzk(1, this.zzaC);
            }
            return !this.value.equals(BuildConfig.FLAVOR) ? zzB + zzrg.zzk(2, this.value) : zzB;
        }

        public zze zzCb() {
            this.zzaC = BuildConfig.FLAVOR;
            this.value = BuildConfig.FLAVOR;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public zze zzF(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case HTTP.LF /*10*/:
                        this.zzaC = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        this.value = com_google_android_gms_internal_zzrf.readString();
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
            if (!this.zzaC.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(1, this.zzaC);
            }
            if (!this.value.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(2, this.value);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzF(com_google_android_gms_internal_zzrf);
        }
    }
}
