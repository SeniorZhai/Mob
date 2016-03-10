package com.google.android.gms.tagmanager;

import com.facebook.internal.ServerProtocol;
import com.google.android.gms.internal.zzag.zza;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class zzdf {
    private static Map<Object, Object> zzaOA = new HashMap();
    private static zza zzaOB = zzI(zzaOx);
    private static final Object zzaOt = null;
    private static Long zzaOu = new Long(0);
    private static Double zzaOv = new Double(0.0d);
    private static zzde zzaOw = zzde.zzT(0);
    private static String zzaOx = new String(BuildConfig.FLAVOR);
    private static Boolean zzaOy = new Boolean(false);
    private static List<Object> zzaOz = new ArrayList(0);

    private static double getDouble(Object o) {
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        }
        zzbg.zzaz("getDouble received non-Number");
        return 0.0d;
    }

    public static String zzD(Object obj) {
        return obj == null ? zzaOx : obj.toString();
    }

    public static zzde zzE(Object obj) {
        return obj instanceof zzde ? (zzde) obj : zzK(obj) ? zzde.zzT(zzL(obj)) : zzJ(obj) ? zzde.zza(Double.valueOf(getDouble(obj))) : zzeK(zzD(obj));
    }

    public static Long zzF(Object obj) {
        return zzK(obj) ? Long.valueOf(zzL(obj)) : zzeL(zzD(obj));
    }

    public static Double zzG(Object obj) {
        return zzJ(obj) ? Double.valueOf(getDouble(obj)) : zzeM(zzD(obj));
    }

    public static Boolean zzH(Object obj) {
        return obj instanceof Boolean ? (Boolean) obj : zzeN(zzD(obj));
    }

    public static zza zzI(Object obj) {
        boolean z = false;
        zza com_google_android_gms_internal_zzag_zza = new zza();
        if (obj instanceof zza) {
            return (zza) obj;
        }
        if (obj instanceof String) {
            com_google_android_gms_internal_zzag_zza.type = 1;
            com_google_android_gms_internal_zzag_zza.zziR = (String) obj;
        } else if (obj instanceof List) {
            com_google_android_gms_internal_zzag_zza.type = 2;
            List<Object> list = (List) obj;
            r5 = new ArrayList(list.size());
            r1 = false;
            for (Object zzI : list) {
                zza zzI2 = zzI(zzI);
                if (zzI2 == zzaOB) {
                    return zzaOB;
                }
                r0 = r1 || zzI2.zzjb;
                r5.add(zzI2);
                r1 = r0;
            }
            com_google_android_gms_internal_zzag_zza.zziS = (zza[]) r5.toArray(new zza[0]);
            z = r1;
        } else if (obj instanceof Map) {
            com_google_android_gms_internal_zzag_zza.type = 3;
            Set<Entry> entrySet = ((Map) obj).entrySet();
            r5 = new ArrayList(entrySet.size());
            List arrayList = new ArrayList(entrySet.size());
            r1 = false;
            for (Entry entry : entrySet) {
                zza zzI3 = zzI(entry.getKey());
                zza zzI4 = zzI(entry.getValue());
                if (zzI3 == zzaOB || zzI4 == zzaOB) {
                    return zzaOB;
                }
                r0 = r1 || zzI3.zzjb || zzI4.zzjb;
                r5.add(zzI3);
                arrayList.add(zzI4);
                r1 = r0;
            }
            com_google_android_gms_internal_zzag_zza.zziT = (zza[]) r5.toArray(new zza[0]);
            com_google_android_gms_internal_zzag_zza.zziU = (zza[]) arrayList.toArray(new zza[0]);
            z = r1;
        } else if (zzJ(obj)) {
            com_google_android_gms_internal_zzag_zza.type = 1;
            com_google_android_gms_internal_zzag_zza.zziR = obj.toString();
        } else if (zzK(obj)) {
            com_google_android_gms_internal_zzag_zza.type = 6;
            com_google_android_gms_internal_zzag_zza.zziX = zzL(obj);
        } else if (obj instanceof Boolean) {
            com_google_android_gms_internal_zzag_zza.type = 8;
            com_google_android_gms_internal_zzag_zza.zziY = ((Boolean) obj).booleanValue();
        } else {
            zzbg.zzaz("Converting to Value from unknown object type: " + (obj == null ? "null" : obj.getClass().toString()));
            return zzaOB;
        }
        com_google_android_gms_internal_zzag_zza.zzjb = z;
        return com_google_android_gms_internal_zzag_zza;
    }

    private static boolean zzJ(Object obj) {
        return (obj instanceof Double) || (obj instanceof Float) || ((obj instanceof zzde) && ((zzde) obj).zzzF());
    }

    private static boolean zzK(Object obj) {
        return (obj instanceof Byte) || (obj instanceof Short) || (obj instanceof Integer) || (obj instanceof Long) || ((obj instanceof zzde) && ((zzde) obj).zzzG());
    }

    private static long zzL(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        zzbg.zzaz("getInt64 received non-Number");
        return 0;
    }

    public static zza zzeJ(String str) {
        zza com_google_android_gms_internal_zzag_zza = new zza();
        com_google_android_gms_internal_zzag_zza.type = 5;
        com_google_android_gms_internal_zzag_zza.zziW = str;
        return com_google_android_gms_internal_zzag_zza;
    }

    private static zzde zzeK(String str) {
        try {
            return zzde.zzeI(str);
        } catch (NumberFormatException e) {
            zzbg.zzaz("Failed to convert '" + str + "' to a number.");
            return zzaOw;
        }
    }

    private static Long zzeL(String str) {
        zzde zzeK = zzeK(str);
        return zzeK == zzaOw ? zzaOu : Long.valueOf(zzeK.longValue());
    }

    private static Double zzeM(String str) {
        zzde zzeK = zzeK(str);
        return zzeK == zzaOw ? zzaOv : Double.valueOf(zzeK.doubleValue());
    }

    private static Boolean zzeN(String str) {
        return ServerProtocol.DIALOG_RETURN_SCOPES_TRUE.equalsIgnoreCase(str) ? Boolean.TRUE : "false".equalsIgnoreCase(str) ? Boolean.FALSE : zzaOy;
    }

    public static String zzg(zza com_google_android_gms_internal_zzag_zza) {
        return zzD(zzl(com_google_android_gms_internal_zzag_zza));
    }

    public static zzde zzh(zza com_google_android_gms_internal_zzag_zza) {
        return zzE(zzl(com_google_android_gms_internal_zzag_zza));
    }

    public static Long zzi(zza com_google_android_gms_internal_zzag_zza) {
        return zzF(zzl(com_google_android_gms_internal_zzag_zza));
    }

    public static Double zzj(zza com_google_android_gms_internal_zzag_zza) {
        return zzG(zzl(com_google_android_gms_internal_zzag_zza));
    }

    public static Boolean zzk(zza com_google_android_gms_internal_zzag_zza) {
        return zzH(zzl(com_google_android_gms_internal_zzag_zza));
    }

    public static Object zzl(zza com_google_android_gms_internal_zzag_zza) {
        int i = 0;
        if (com_google_android_gms_internal_zzag_zza == null) {
            return zzaOt;
        }
        zza[] com_google_android_gms_internal_zzag_zzaArr;
        int length;
        switch (com_google_android_gms_internal_zzag_zza.type) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return com_google_android_gms_internal_zzag_zza.zziR;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                ArrayList arrayList = new ArrayList(com_google_android_gms_internal_zzag_zza.zziS.length);
                com_google_android_gms_internal_zzag_zzaArr = com_google_android_gms_internal_zzag_zza.zziS;
                length = com_google_android_gms_internal_zzag_zzaArr.length;
                while (i < length) {
                    Object zzl = zzl(com_google_android_gms_internal_zzag_zzaArr[i]);
                    if (zzl == zzaOt) {
                        return zzaOt;
                    }
                    arrayList.add(zzl);
                    i++;
                }
                return arrayList;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                if (com_google_android_gms_internal_zzag_zza.zziT.length != com_google_android_gms_internal_zzag_zza.zziU.length) {
                    zzbg.zzaz("Converting an invalid value to object: " + com_google_android_gms_internal_zzag_zza.toString());
                    return zzaOt;
                }
                Map hashMap = new HashMap(com_google_android_gms_internal_zzag_zza.zziU.length);
                while (i < com_google_android_gms_internal_zzag_zza.zziT.length) {
                    Object zzl2 = zzl(com_google_android_gms_internal_zzag_zza.zziT[i]);
                    Object zzl3 = zzl(com_google_android_gms_internal_zzag_zza.zziU[i]);
                    if (zzl2 == zzaOt || zzl3 == zzaOt) {
                        return zzaOt;
                    }
                    hashMap.put(zzl2, zzl3);
                    i++;
                }
                return hashMap;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                zzbg.zzaz("Trying to convert a macro reference to object");
                return zzaOt;
            case Player.STATE_ENDED /*5*/:
                zzbg.zzaz("Trying to convert a function id to object");
                return zzaOt;
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                return Long.valueOf(com_google_android_gms_internal_zzag_zza.zziX);
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                StringBuffer stringBuffer = new StringBuffer();
                com_google_android_gms_internal_zzag_zzaArr = com_google_android_gms_internal_zzag_zza.zziZ;
                length = com_google_android_gms_internal_zzag_zzaArr.length;
                while (i < length) {
                    String zzg = zzg(com_google_android_gms_internal_zzag_zzaArr[i]);
                    if (zzg == zzaOx) {
                        return zzaOt;
                    }
                    stringBuffer.append(zzg);
                    i++;
                }
                return stringBuffer.toString();
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                return Boolean.valueOf(com_google_android_gms_internal_zzag_zza.zziY);
            default:
                zzbg.zzaz("Failed to convert a value of type: " + com_google_android_gms_internal_zzag_zza.type);
                return zzaOt;
        }
    }

    public static Object zzzK() {
        return zzaOt;
    }

    public static Long zzzL() {
        return zzaOu;
    }

    public static Double zzzM() {
        return zzaOv;
    }

    public static Boolean zzzN() {
        return zzaOy;
    }

    public static zzde zzzO() {
        return zzaOw;
    }

    public static String zzzP() {
        return zzaOx;
    }

    public static zza zzzQ() {
        return zzaOB;
    }
}
