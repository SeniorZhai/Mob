package com.google.android.gms.internal;

import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.LangUtils;

public interface zzag {

    public static final class zza extends zzrh<zza> {
        private static volatile zza[] zziQ;
        public int type;
        public String zziR;
        public zza[] zziS;
        public zza[] zziT;
        public zza[] zziU;
        public String zziV;
        public String zziW;
        public long zziX;
        public boolean zziY;
        public zza[] zziZ;
        public int[] zzja;
        public boolean zzjb;

        public zza() {
            zzR();
        }

        public static zza[] zzQ() {
            if (zziQ == null) {
                synchronized (zzrl.zzaWe) {
                    if (zziQ == null) {
                        zziQ = new zza[0];
                    }
                }
            }
            return zziQ;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof zza)) {
                return false;
            }
            zza com_google_android_gms_internal_zzag_zza = (zza) o;
            if (this.type != com_google_android_gms_internal_zzag_zza.type) {
                return false;
            }
            if (this.zziR == null) {
                if (com_google_android_gms_internal_zzag_zza.zziR != null) {
                    return false;
                }
            } else if (!this.zziR.equals(com_google_android_gms_internal_zzag_zza.zziR)) {
                return false;
            }
            if (!zzrl.equals(this.zziS, com_google_android_gms_internal_zzag_zza.zziS) || !zzrl.equals(this.zziT, com_google_android_gms_internal_zzag_zza.zziT) || !zzrl.equals(this.zziU, com_google_android_gms_internal_zzag_zza.zziU)) {
                return false;
            }
            if (this.zziV == null) {
                if (com_google_android_gms_internal_zzag_zza.zziV != null) {
                    return false;
                }
            } else if (!this.zziV.equals(com_google_android_gms_internal_zzag_zza.zziV)) {
                return false;
            }
            if (this.zziW == null) {
                if (com_google_android_gms_internal_zzag_zza.zziW != null) {
                    return false;
                }
            } else if (!this.zziW.equals(com_google_android_gms_internal_zzag_zza.zziW)) {
                return false;
            }
            return (this.zziX == com_google_android_gms_internal_zzag_zza.zziX && this.zziY == com_google_android_gms_internal_zzag_zza.zziY && zzrl.equals(this.zziZ, com_google_android_gms_internal_zzag_zza.zziZ) && zzrl.equals(this.zzja, com_google_android_gms_internal_zzag_zza.zzja) && this.zzjb == com_google_android_gms_internal_zzag_zza.zzjb) ? zza((zzrh) com_google_android_gms_internal_zzag_zza) : false;
        }

        public int hashCode() {
            int i = 1231;
            int i2 = 0;
            int hashCode = ((this.zziV == null ? 0 : this.zziV.hashCode()) + (((((((((this.zziR == null ? 0 : this.zziR.hashCode()) + ((this.type + 527) * 31)) * 31) + zzrl.hashCode(this.zziS)) * 31) + zzrl.hashCode(this.zziT)) * 31) + zzrl.hashCode(this.zziU)) * 31)) * 31;
            if (this.zziW != null) {
                i2 = this.zziW.hashCode();
            }
            hashCode = ((((((this.zziY ? 1231 : 1237) + ((((hashCode + i2) * 31) + ((int) (this.zziX ^ (this.zziX >>> 32)))) * 31)) * 31) + zzrl.hashCode(this.zziZ)) * 31) + zzrl.hashCode(this.zzja)) * 31;
            if (!this.zzjb) {
                i = 1237;
            }
            return ((hashCode + i) * 31) + zzBI();
        }

        protected int zzB() {
            int i;
            int i2 = 0;
            int zzB = super.zzB() + zzrg.zzA(1, this.type);
            if (!this.zziR.equals(BuildConfig.FLAVOR)) {
                zzB += zzrg.zzk(2, this.zziR);
            }
            if (this.zziS != null && this.zziS.length > 0) {
                i = zzB;
                for (zzrn com_google_android_gms_internal_zzrn : this.zziS) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        i += zzrg.zzc(3, com_google_android_gms_internal_zzrn);
                    }
                }
                zzB = i;
            }
            if (this.zziT != null && this.zziT.length > 0) {
                i = zzB;
                for (zzrn com_google_android_gms_internal_zzrn2 : this.zziT) {
                    if (com_google_android_gms_internal_zzrn2 != null) {
                        i += zzrg.zzc(4, com_google_android_gms_internal_zzrn2);
                    }
                }
                zzB = i;
            }
            if (this.zziU != null && this.zziU.length > 0) {
                i = zzB;
                for (zzrn com_google_android_gms_internal_zzrn22 : this.zziU) {
                    if (com_google_android_gms_internal_zzrn22 != null) {
                        i += zzrg.zzc(5, com_google_android_gms_internal_zzrn22);
                    }
                }
                zzB = i;
            }
            if (!this.zziV.equals(BuildConfig.FLAVOR)) {
                zzB += zzrg.zzk(6, this.zziV);
            }
            if (!this.zziW.equals(BuildConfig.FLAVOR)) {
                zzB += zzrg.zzk(7, this.zziW);
            }
            if (this.zziX != 0) {
                zzB += zzrg.zzd(8, this.zziX);
            }
            if (this.zzjb) {
                zzB += zzrg.zzc(9, this.zzjb);
            }
            if (this.zzja != null && this.zzja.length > 0) {
                int i3 = 0;
                for (int zzkJ : this.zzja) {
                    i3 += zzrg.zzkJ(zzkJ);
                }
                zzB = (zzB + i3) + (this.zzja.length * 1);
            }
            if (this.zziZ != null && this.zziZ.length > 0) {
                while (i2 < this.zziZ.length) {
                    zzrn com_google_android_gms_internal_zzrn3 = this.zziZ[i2];
                    if (com_google_android_gms_internal_zzrn3 != null) {
                        zzB += zzrg.zzc(11, com_google_android_gms_internal_zzrn3);
                    }
                    i2++;
                }
            }
            return this.zziY ? zzB + zzrg.zzc(12, this.zziY) : zzB;
        }

        public zza zzR() {
            this.type = 1;
            this.zziR = BuildConfig.FLAVOR;
            this.zziS = zzQ();
            this.zziT = zzQ();
            this.zziU = zzQ();
            this.zziV = BuildConfig.FLAVOR;
            this.zziW = BuildConfig.FLAVOR;
            this.zziX = 0;
            this.zziY = false;
            this.zziZ = zzQ();
            this.zzja = zzrq.zzaWh;
            this.zzjb = false;
            this.zzaVU = null;
            this.zzaWf = -1;
            return this;
        }

        public void zza(zzrg com_google_android_gms_internal_zzrg) throws IOException {
            int i = 0;
            com_google_android_gms_internal_zzrg.zzy(1, this.type);
            if (!this.zziR.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(2, this.zziR);
            }
            if (this.zziS != null && this.zziS.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn : this.zziS) {
                    if (com_google_android_gms_internal_zzrn != null) {
                        com_google_android_gms_internal_zzrg.zza(3, com_google_android_gms_internal_zzrn);
                    }
                }
            }
            if (this.zziT != null && this.zziT.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn2 : this.zziT) {
                    if (com_google_android_gms_internal_zzrn2 != null) {
                        com_google_android_gms_internal_zzrg.zza(4, com_google_android_gms_internal_zzrn2);
                    }
                }
            }
            if (this.zziU != null && this.zziU.length > 0) {
                for (zzrn com_google_android_gms_internal_zzrn22 : this.zziU) {
                    if (com_google_android_gms_internal_zzrn22 != null) {
                        com_google_android_gms_internal_zzrg.zza(5, com_google_android_gms_internal_zzrn22);
                    }
                }
            }
            if (!this.zziV.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(6, this.zziV);
            }
            if (!this.zziW.equals(BuildConfig.FLAVOR)) {
                com_google_android_gms_internal_zzrg.zzb(7, this.zziW);
            }
            if (this.zziX != 0) {
                com_google_android_gms_internal_zzrg.zzb(8, this.zziX);
            }
            if (this.zzjb) {
                com_google_android_gms_internal_zzrg.zzb(9, this.zzjb);
            }
            if (this.zzja != null && this.zzja.length > 0) {
                for (int zzy : this.zzja) {
                    com_google_android_gms_internal_zzrg.zzy(10, zzy);
                }
            }
            if (this.zziZ != null && this.zziZ.length > 0) {
                while (i < this.zziZ.length) {
                    zzrn com_google_android_gms_internal_zzrn3 = this.zziZ[i];
                    if (com_google_android_gms_internal_zzrn3 != null) {
                        com_google_android_gms_internal_zzrg.zza(11, com_google_android_gms_internal_zzrn3);
                    }
                    i++;
                }
            }
            if (this.zziY) {
                com_google_android_gms_internal_zzrg.zzb(12, this.zziY);
            }
            super.zza(com_google_android_gms_internal_zzrg);
        }

        public /* synthetic */ zzrn zzb(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            return zzl(com_google_android_gms_internal_zzrf);
        }

        public zza zzl(zzrf com_google_android_gms_internal_zzrf) throws IOException {
            while (true) {
                int zzBr = com_google_android_gms_internal_zzrf.zzBr();
                int zzb;
                Object obj;
                int i;
                switch (zzBr) {
                    case ResponseParser.ResponseActionDiscard /*0*/:
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        zzBr = com_google_android_gms_internal_zzrf.zzBu();
                        switch (zzBr) {
                            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                            case Player.STATE_ENDED /*5*/:
                            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                            case DayPickerView.DAYS_PER_WEEK /*7*/:
                            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                                this.type = zzBr;
                                break;
                            default:
                                continue;
                        }
                    case R.styleable.Toolbar_collapseIcon /*18*/:
                        this.zziR = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case R.styleable.Theme_actionMenuTextColor /*26*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 26);
                        zzBr = this.zziS == null ? 0 : this.zziS.length;
                        obj = new zza[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziS, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zza();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zza();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zziS = obj;
                        continue;
                    case R.styleable.Theme_actionModePasteDrawable /*34*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 34);
                        zzBr = this.zziT == null ? 0 : this.zziT.length;
                        obj = new zza[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziT, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zza();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zza();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zziT = obj;
                        continue;
                    case R.styleable.Theme_dialogTheme /*42*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 42);
                        zzBr = this.zziU == null ? 0 : this.zziU.length;
                        obj = new zza[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziU, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zza();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zza();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zziU = obj;
                        continue;
                    case R.styleable.Theme_buttonBarStyle /*50*/:
                        this.zziV = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case R.styleable.Theme_toolbarStyle /*58*/:
                        this.zziW = com_google_android_gms_internal_zzrf.readString();
                        continue;
                    case SettingsJsonConstants.SETTINGS_MAX_CUSTOM_KEY_VALUE_PAIRS_DEFAULT /*64*/:
                        this.zziX = com_google_android_gms_internal_zzrf.zzBt();
                        continue;
                    case R.styleable.Theme_listPreferredItemPaddingRight /*72*/:
                        this.zzjb = com_google_android_gms_internal_zzrf.zzBv();
                        continue;
                    case R.styleable.Theme_listChoiceBackgroundIndicator /*80*/:
                        int zzb2 = zzrq.zzb(com_google_android_gms_internal_zzrf, 80);
                        Object obj2 = new int[zzb2];
                        i = 0;
                        zzb = 0;
                        while (i < zzb2) {
                            if (i != 0) {
                                com_google_android_gms_internal_zzrf.zzBr();
                            }
                            int zzBu = com_google_android_gms_internal_zzrf.zzBu();
                            switch (zzBu) {
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
                                    zzBr = zzb + 1;
                                    obj2[zzb] = zzBu;
                                    break;
                                default:
                                    zzBr = zzb;
                                    break;
                            }
                            i++;
                            zzb = zzBr;
                        }
                        if (zzb != 0) {
                            zzBr = this.zzja == null ? 0 : this.zzja.length;
                            if (zzBr != 0 || zzb != obj2.length) {
                                Object obj3 = new int[(zzBr + zzb)];
                                if (zzBr != 0) {
                                    System.arraycopy(this.zzja, 0, obj3, 0, zzBr);
                                }
                                System.arraycopy(obj2, 0, obj3, zzBr, zzb);
                                this.zzja = obj3;
                                break;
                            }
                            this.zzja = obj2;
                            break;
                        }
                        continue;
                    case R.styleable.Theme_colorPrimaryDark /*82*/:
                        i = com_google_android_gms_internal_zzrf.zzkC(com_google_android_gms_internal_zzrf.zzBy());
                        zzb = com_google_android_gms_internal_zzrf.getPosition();
                        zzBr = 0;
                        while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                            switch (com_google_android_gms_internal_zzrf.zzBu()) {
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
                                    zzBr++;
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (zzBr != 0) {
                            com_google_android_gms_internal_zzrf.zzkE(zzb);
                            zzb = this.zzja == null ? 0 : this.zzja.length;
                            Object obj4 = new int[(zzBr + zzb)];
                            if (zzb != 0) {
                                System.arraycopy(this.zzja, 0, obj4, 0, zzb);
                            }
                            while (com_google_android_gms_internal_zzrf.zzBD() > 0) {
                                int zzBu2 = com_google_android_gms_internal_zzrf.zzBu();
                                switch (zzBu2) {
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
                                        zzBr = zzb + 1;
                                        obj4[zzb] = zzBu2;
                                        zzb = zzBr;
                                        break;
                                    default:
                                        break;
                                }
                            }
                            this.zzja = obj4;
                        }
                        com_google_android_gms_internal_zzrf.zzkD(i);
                        continue;
                    case R.styleable.Theme_alertDialogStyle /*90*/:
                        zzb = zzrq.zzb(com_google_android_gms_internal_zzrf, 90);
                        zzBr = this.zziZ == null ? 0 : this.zziZ.length;
                        obj = new zza[(zzb + zzBr)];
                        if (zzBr != 0) {
                            System.arraycopy(this.zziZ, 0, obj, 0, zzBr);
                        }
                        while (zzBr < obj.length - 1) {
                            obj[zzBr] = new zza();
                            com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                            com_google_android_gms_internal_zzrf.zzBr();
                            zzBr++;
                        }
                        obj[zzBr] = new zza();
                        com_google_android_gms_internal_zzrf.zza(obj[zzBr]);
                        this.zziZ = obj;
                        continue;
                    case R.styleable.Theme_buttonBarNegativeButtonStyle /*96*/:
                        this.zziY = com_google_android_gms_internal_zzrf.zzBv();
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
