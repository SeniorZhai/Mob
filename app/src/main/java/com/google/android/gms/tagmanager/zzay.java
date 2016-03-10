package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzad;
import com.google.android.gms.internal.zzae;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.http.message.TokenParser;

class zzay extends zzak {
    private static final String ID = zzad.JOINER.toString();
    private static final String zzaLE = zzae.ARG0.toString();
    private static final String zzaLW = zzae.ITEM_SEPARATOR.toString();
    private static final String zzaLX = zzae.KEY_VALUE_SEPARATOR.toString();
    private static final String zzaLY = zzae.ESCAPE.toString();

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] zzaLZ = new int[zza.values().length];

        static {
            try {
                zzaLZ[zza.URL.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                zzaLZ[zza.BACKSLASH.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                zzaLZ[zza.NONE.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private enum zza {
        NONE,
        URL,
        BACKSLASH
    }

    public zzay() {
        super(ID, zzaLE);
    }

    private String zza(String str, zza com_google_android_gms_tagmanager_zzay_zza, Set<Character> set) {
        switch (AnonymousClass1.zzaLZ[com_google_android_gms_tagmanager_zzay_zza.ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                try {
                    return zzdj.zzeQ(str);
                } catch (Throwable e) {
                    zzbg.zzb("Joiner: unsupported encoding", e);
                    return str;
                }
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                String replace = str.replace("\\", "\\\\");
                String str2 = replace;
                for (Character ch : set) {
                    CharSequence ch2 = ch.toString();
                    str2 = str2.replace(ch2, "\\" + ch2);
                }
                return str2;
            default:
                return str;
        }
    }

    private void zza(StringBuilder stringBuilder, String str, zza com_google_android_gms_tagmanager_zzay_zza, Set<Character> set) {
        stringBuilder.append(zza(str, com_google_android_gms_tagmanager_zzay_zza, set));
    }

    private void zza(Set<Character> set, String str) {
        for (int i = 0; i < str.length(); i++) {
            set.add(Character.valueOf(str.charAt(i)));
        }
    }

    public com.google.android.gms.internal.zzag.zza zzE(Map<String, com.google.android.gms.internal.zzag.zza> map) {
        com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza = (com.google.android.gms.internal.zzag.zza) map.get(zzaLE);
        if (com_google_android_gms_internal_zzag_zza == null) {
            return zzdf.zzzQ();
        }
        zza com_google_android_gms_tagmanager_zzay_zza;
        com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza2 = (com.google.android.gms.internal.zzag.zza) map.get(zzaLW);
        String zzg = com_google_android_gms_internal_zzag_zza2 != null ? zzdf.zzg(com_google_android_gms_internal_zzag_zza2) : BuildConfig.FLAVOR;
        com_google_android_gms_internal_zzag_zza2 = (com.google.android.gms.internal.zzag.zza) map.get(zzaLX);
        String zzg2 = com_google_android_gms_internal_zzag_zza2 != null ? zzdf.zzg(com_google_android_gms_internal_zzag_zza2) : "=";
        zza com_google_android_gms_tagmanager_zzay_zza2 = zza.NONE;
        com_google_android_gms_internal_zzag_zza2 = (com.google.android.gms.internal.zzag.zza) map.get(zzaLY);
        Set set;
        if (com_google_android_gms_internal_zzag_zza2 != null) {
            String zzg3 = zzdf.zzg(com_google_android_gms_internal_zzag_zza2);
            if (SettingsJsonConstants.APP_URL_KEY.equals(zzg3)) {
                com_google_android_gms_tagmanager_zzay_zza = zza.URL;
                set = null;
            } else if ("backslash".equals(zzg3)) {
                com_google_android_gms_tagmanager_zzay_zza = zza.BACKSLASH;
                set = new HashSet();
                zza(set, zzg);
                zza(set, zzg2);
                set.remove(Character.valueOf(TokenParser.ESCAPE));
            } else {
                zzbg.zzaz("Joiner: unsupported escape type: " + zzg3);
                return zzdf.zzzQ();
            }
        }
        set = null;
        com_google_android_gms_tagmanager_zzay_zza = com_google_android_gms_tagmanager_zzay_zza2;
        StringBuilder stringBuilder = new StringBuilder();
        switch (com_google_android_gms_internal_zzag_zza.type) {
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                Object obj = 1;
                com.google.android.gms.internal.zzag.zza[] com_google_android_gms_internal_zzag_zzaArr = com_google_android_gms_internal_zzag_zza.zziS;
                int length = com_google_android_gms_internal_zzag_zzaArr.length;
                int i = 0;
                while (i < length) {
                    com.google.android.gms.internal.zzag.zza com_google_android_gms_internal_zzag_zza3 = com_google_android_gms_internal_zzag_zzaArr[i];
                    if (obj == null) {
                        stringBuilder.append(zzg);
                    }
                    zza(stringBuilder, zzdf.zzg(com_google_android_gms_internal_zzag_zza3), com_google_android_gms_tagmanager_zzay_zza, set);
                    i++;
                    obj = null;
                }
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                for (int i2 = 0; i2 < com_google_android_gms_internal_zzag_zza.zziT.length; i2++) {
                    if (i2 > 0) {
                        stringBuilder.append(zzg);
                    }
                    String zzg4 = zzdf.zzg(com_google_android_gms_internal_zzag_zza.zziT[i2]);
                    String zzg5 = zzdf.zzg(com_google_android_gms_internal_zzag_zza.zziU[i2]);
                    zza(stringBuilder, zzg4, com_google_android_gms_tagmanager_zzay_zza, set);
                    stringBuilder.append(zzg2);
                    zza(stringBuilder, zzg5, com_google_android_gms_tagmanager_zzay_zza, set);
                }
                break;
            default:
                zza(stringBuilder, zzdf.zzg(com_google_android_gms_internal_zzag_zza), com_google_android_gms_tagmanager_zzay_zza, set);
                break;
        }
        return zzdf.zzI(stringBuilder.toString());
    }

    public boolean zzyh() {
        return true;
    }
}
