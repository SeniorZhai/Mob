package com.google.android.gms.internal;

import com.google.android.gms.internal.zzag.zza;
import com.google.android.gms.internal.zzqf.zzb;
import com.google.android.gms.internal.zzqf.zzc;
import com.google.android.gms.internal.zzqf.zzd;
import com.google.android.gms.internal.zzqf.zze;
import com.google.android.gms.internal.zzqf.zzf;
import com.google.android.gms.internal.zzqf.zzg;
import com.google.android.gms.tagmanager.zzbg;
import com.google.android.gms.tagmanager.zzdf;
import com.helpshift.constants.FaqsColumns;
import com.helpshift.res.values.HSConsts;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class zzpz {
    static zza zza(int i, JSONArray jSONArray, zza[] com_google_android_gms_internal_zzag_zzaArr, Set<Integer> set) throws zzg, JSONException {
        int i2 = 0;
        if (set.contains(Integer.valueOf(i))) {
            zzeT("Value cycle detected. Current value reference: " + i + "." + "  Previous value references: " + set + ".");
        }
        Object zza = zza(jSONArray, i, "values");
        if (com_google_android_gms_internal_zzag_zzaArr[i] != null) {
            return com_google_android_gms_internal_zzag_zzaArr[i];
        }
        set.add(Integer.valueOf(i));
        zza com_google_android_gms_internal_zzag_zza = new zza();
        JSONArray jSONArray2;
        if (zza instanceof JSONArray) {
            jSONArray2 = (JSONArray) zza;
            com_google_android_gms_internal_zzag_zza.type = 2;
            com_google_android_gms_internal_zzag_zza.zzjb = true;
            com_google_android_gms_internal_zzag_zza.zziS = new zza[jSONArray2.length()];
            while (i2 < com_google_android_gms_internal_zzag_zza.zziS.length) {
                com_google_android_gms_internal_zzag_zza.zziS[i2] = zza(jSONArray2.getInt(i2), jSONArray, com_google_android_gms_internal_zzag_zzaArr, (Set) set);
                i2++;
            }
        } else if (zza instanceof JSONObject) {
            int i3;
            JSONObject jSONObject = (JSONObject) zza;
            JSONArray optJSONArray = jSONObject.optJSONArray("escaping");
            if (optJSONArray != null) {
                com_google_android_gms_internal_zzag_zza.zzja = new int[optJSONArray.length()];
                for (i3 = 0; i3 < com_google_android_gms_internal_zzag_zza.zzja.length; i3++) {
                    com_google_android_gms_internal_zzag_zza.zzja[i3] = optJSONArray.getInt(i3);
                }
            }
            if (jSONObject.has("function_id")) {
                com_google_android_gms_internal_zzag_zza.type = 5;
                com_google_android_gms_internal_zzag_zza.zziW = jSONObject.getString("function_id");
            } else if (jSONObject.has("macro_reference")) {
                com_google_android_gms_internal_zzag_zza.type = 4;
                com_google_android_gms_internal_zzag_zza.zzjb = true;
                com_google_android_gms_internal_zzag_zza.zziV = zzdf.zzg(zza(jSONObject.getInt("macro_reference"), jSONArray, com_google_android_gms_internal_zzag_zzaArr, (Set) set));
            } else if (jSONObject.has("template_token")) {
                com_google_android_gms_internal_zzag_zza.type = 7;
                com_google_android_gms_internal_zzag_zza.zzjb = true;
                jSONArray2 = jSONObject.getJSONArray("template_token");
                com_google_android_gms_internal_zzag_zza.zziZ = new zza[jSONArray2.length()];
                while (i2 < com_google_android_gms_internal_zzag_zza.zziZ.length) {
                    com_google_android_gms_internal_zzag_zza.zziZ[i2] = zza(jSONArray2.getInt(i2), jSONArray, com_google_android_gms_internal_zzag_zzaArr, (Set) set);
                    i2++;
                }
            } else {
                com_google_android_gms_internal_zzag_zza.type = 3;
                com_google_android_gms_internal_zzag_zza.zzjb = true;
                i3 = jSONObject.length();
                com_google_android_gms_internal_zzag_zza.zziT = new zza[i3];
                com_google_android_gms_internal_zzag_zza.zziU = new zza[i3];
                Iterator keys = jSONObject.keys();
                i3 = 0;
                while (keys.hasNext()) {
                    String str = (String) keys.next();
                    com_google_android_gms_internal_zzag_zza.zziT[i3] = zza(new Integer(str).intValue(), jSONArray, com_google_android_gms_internal_zzag_zzaArr, (Set) set);
                    com_google_android_gms_internal_zzag_zza.zziU[i3] = zza(jSONObject.getInt(str), jSONArray, com_google_android_gms_internal_zzag_zzaArr, (Set) set);
                    i3++;
                }
            }
        } else if (zza instanceof String) {
            com_google_android_gms_internal_zzag_zza.zziR = (String) zza;
            com_google_android_gms_internal_zzag_zza.type = 1;
        } else if (zza instanceof Boolean) {
            com_google_android_gms_internal_zzag_zza.zziY = ((Boolean) zza).booleanValue();
            com_google_android_gms_internal_zzag_zza.type = 8;
        } else if (zza instanceof Integer) {
            com_google_android_gms_internal_zzag_zza.zziX = (long) ((Integer) zza).intValue();
            com_google_android_gms_internal_zzag_zza.type = 6;
        } else {
            zzeT("Invalid value type: " + zza);
        }
        com_google_android_gms_internal_zzag_zzaArr[i] = com_google_android_gms_internal_zzag_zza;
        set.remove(Integer.valueOf(i));
        return com_google_android_gms_internal_zzag_zza;
    }

    static zzqf.zza zza(JSONObject jSONObject, JSONArray jSONArray, JSONArray jSONArray2, zza[] com_google_android_gms_internal_zzag_zzaArr, int i) throws zzg, JSONException {
        zzb zzAm = zzqf.zza.zzAm();
        JSONArray jSONArray3 = jSONObject.getJSONArray("property");
        for (int i2 = 0; i2 < jSONArray3.length(); i2++) {
            JSONObject jSONObject2 = (JSONObject) zza(jSONArray, jSONArray3.getInt(i2), "properties");
            String str = (String) zza(jSONArray2, jSONObject2.getInt("key"), "key");
            zza com_google_android_gms_internal_zzag_zza = (zza) zza((Object[]) com_google_android_gms_internal_zzag_zzaArr, jSONObject2.getInt("value"), "value");
            if (zzae.PUSH_AFTER_EVALUATE.toString().equals(str)) {
                zzAm.zzq(com_google_android_gms_internal_zzag_zza);
            } else {
                zzAm.zzb(str, com_google_android_gms_internal_zzag_zza);
            }
        }
        return zzAm.zzAo();
    }

    static zze zza(JSONObject jSONObject, List<zzqf.zza> list, List<zzqf.zza> list2, List<zzqf.zza> list3, zza[] com_google_android_gms_internal_zzag_zzaArr) throws JSONException {
        int i;
        int i2;
        zzf zzAt = zze.zzAt();
        JSONArray optJSONArray = jSONObject.optJSONArray("positive_predicate");
        JSONArray optJSONArray2 = jSONObject.optJSONArray("negative_predicate");
        JSONArray optJSONArray3 = jSONObject.optJSONArray("add_tag");
        JSONArray optJSONArray4 = jSONObject.optJSONArray("remove_tag");
        JSONArray optJSONArray5 = jSONObject.optJSONArray("add_tag_rule_name");
        JSONArray optJSONArray6 = jSONObject.optJSONArray("remove_tag_rule_name");
        JSONArray optJSONArray7 = jSONObject.optJSONArray("add_macro");
        JSONArray optJSONArray8 = jSONObject.optJSONArray("remove_macro");
        JSONArray optJSONArray9 = jSONObject.optJSONArray("add_macro_rule_name");
        JSONArray optJSONArray10 = jSONObject.optJSONArray("remove_macro_rule_name");
        if (optJSONArray != null) {
            for (i = 0; i < optJSONArray.length(); i++) {
                zzAt.zzd((zzqf.zza) list3.get(optJSONArray.getInt(i)));
            }
        }
        if (optJSONArray2 != null) {
            for (i = 0; i < optJSONArray2.length(); i++) {
                zzAt.zze((zzqf.zza) list3.get(optJSONArray2.getInt(i)));
            }
        }
        if (optJSONArray3 != null) {
            for (i = 0; i < optJSONArray3.length(); i++) {
                zzAt.zzf((zzqf.zza) list.get(optJSONArray3.getInt(i)));
            }
        }
        if (optJSONArray4 != null) {
            for (i = 0; i < optJSONArray4.length(); i++) {
                zzAt.zzg((zzqf.zza) list.get(optJSONArray4.getInt(i)));
            }
        }
        if (optJSONArray5 != null) {
            for (i2 = 0; i2 < optJSONArray5.length(); i2++) {
                zzAt.zzeW(com_google_android_gms_internal_zzag_zzaArr[optJSONArray5.getInt(i2)].zziR);
            }
        }
        if (optJSONArray6 != null) {
            for (i2 = 0; i2 < optJSONArray6.length(); i2++) {
                zzAt.zzeX(com_google_android_gms_internal_zzag_zzaArr[optJSONArray6.getInt(i2)].zziR);
            }
        }
        if (optJSONArray7 != null) {
            for (i = 0; i < optJSONArray7.length(); i++) {
                zzAt.zzh((zzqf.zza) list2.get(optJSONArray7.getInt(i)));
            }
        }
        if (optJSONArray8 != null) {
            for (i = 0; i < optJSONArray8.length(); i++) {
                zzAt.zzi((zzqf.zza) list2.get(optJSONArray8.getInt(i)));
            }
        }
        if (optJSONArray9 != null) {
            for (i2 = 0; i2 < optJSONArray9.length(); i2++) {
                zzAt.zzeY(com_google_android_gms_internal_zzag_zzaArr[optJSONArray9.getInt(i2)].zziR);
            }
        }
        if (optJSONArray10 != null) {
            for (i2 = 0; i2 < optJSONArray10.length(); i2++) {
                zzAt.zzeZ(com_google_android_gms_internal_zzag_zzaArr[optJSONArray10.getInt(i2)].zziR);
            }
        }
        return zzAt.zzAE();
    }

    private static <T> T zza(JSONArray jSONArray, int i, String str) throws zzg {
        if (i >= 0 && i < jSONArray.length()) {
            try {
                return jSONArray.get(i);
            } catch (JSONException e) {
            }
        }
        zzeT("Index out of bounds detected: " + i + " in " + str);
        return null;
    }

    private static <T> T zza(T[] tArr, int i, String str) throws zzg {
        if (i < 0 || i >= tArr.length) {
            zzeT("Index out of bounds detected: " + i + " in " + str);
        }
        return tArr[i];
    }

    static List<zzqf.zza> zza(JSONArray jSONArray, JSONArray jSONArray2, JSONArray jSONArray3, zza[] com_google_android_gms_internal_zzag_zzaArr) throws JSONException, zzg {
        List<zzqf.zza> arrayList = new ArrayList();
        for (int i = 0; i < jSONArray.length(); i++) {
            arrayList.add(zza(jSONArray.getJSONObject(i), jSONArray2, jSONArray3, com_google_android_gms_internal_zzag_zzaArr, i));
        }
        return arrayList;
    }

    private static void zzeT(String str) throws zzg {
        zzbg.zzaz(str);
        throw new zzg(str);
    }

    static zzc zzey(String str) throws JSONException, zzg {
        JSONObject jSONObject = new JSONObject(str);
        Object obj = jSONObject.get("resource");
        if (obj instanceof JSONObject) {
            JSONObject jSONObject2 = (JSONObject) obj;
            zzd zzAp = zzc.zzAp();
            zza[] zzj = zzj(jSONObject2);
            JSONArray jSONArray = jSONObject2.getJSONArray("properties");
            JSONArray jSONArray2 = jSONObject2.getJSONArray("key");
            List zza = zza(jSONObject2.getJSONArray(FaqsColumns.TAGS), jSONArray, jSONArray2, zzj);
            List zza2 = zza(jSONObject2.getJSONArray("predicates"), jSONArray, jSONArray2, zzj);
            List<zzqf.zza> zza3 = zza(jSONObject2.getJSONArray("macros"), jSONArray, jSONArray2, zzj);
            for (zzqf.zza zzc : zza3) {
                zzAp.zzc(zzc);
            }
            jSONArray = jSONObject2.getJSONArray("rules");
            for (int i = 0; i < jSONArray.length(); i++) {
                zzAp.zzb(zza(jSONArray.getJSONObject(i), zza, (List) zza3, zza2, zzj));
            }
            zzAp.zzeV(HSConsts.STATUS_INPROGRESS);
            zzAp.zzjb(1);
            if (jSONObject.optJSONArray("runtime") != null) {
            }
            return zzAp.zzAs();
        }
        throw new zzg("Resource map not found");
    }

    static zza[] zzj(JSONObject jSONObject) throws JSONException, zzg {
        Object opt = jSONObject.opt("values");
        if (opt instanceof JSONArray) {
            JSONArray jSONArray = (JSONArray) opt;
            zza[] com_google_android_gms_internal_zzag_zzaArr = new zza[jSONArray.length()];
            for (int i = 0; i < com_google_android_gms_internal_zzag_zzaArr.length; i++) {
                zza(i, jSONArray, com_google_android_gms_internal_zzag_zzaArr, new HashSet(0));
            }
            return com_google_android_gms_internal_zzag_zzaArr;
        }
        throw new zzg("Missing Values list");
    }
}
