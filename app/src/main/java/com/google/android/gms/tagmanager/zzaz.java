package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.zzae;
import com.google.android.gms.internal.zzag.zza;
import com.google.android.gms.internal.zzqf;
import com.google.android.gms.internal.zzqf.zzc;
import com.google.android.gms.internal.zzqf.zzd;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class zzaz {
    private static zza zzB(Object obj) throws JSONException {
        return zzdf.zzI(zzC(obj));
    }

    static Object zzC(Object obj) throws JSONException {
        if (obj instanceof JSONArray) {
            throw new RuntimeException("JSONArrays are not supported");
        } else if (JSONObject.NULL.equals(obj)) {
            throw new RuntimeException("JSON nulls are not supported");
        } else if (!(obj instanceof JSONObject)) {
            return obj;
        } else {
            JSONObject jSONObject = (JSONObject) obj;
            Map hashMap = new HashMap();
            Iterator keys = jSONObject.keys();
            while (keys.hasNext()) {
                String str = (String) keys.next();
                hashMap.put(str, zzC(jSONObject.get(str)));
            }
            return hashMap;
        }
    }

    public static zzc zzey(String str) throws JSONException {
        zza zzB = zzB(new JSONObject(str));
        zzd zzAp = zzc.zzAp();
        for (int i = 0; i < zzB.zziT.length; i++) {
            zzAp.zzc(zzqf.zza.zzAm().zzb(zzae.INSTANCE_NAME.toString(), zzB.zziT[i]).zzb(zzae.FUNCTION.toString(), zzdf.zzeJ(zzn.zzyk())).zzb(zzn.zzyl(), zzB.zziU[i]).zzAo());
        }
        return zzAp.zzAs();
    }
}
