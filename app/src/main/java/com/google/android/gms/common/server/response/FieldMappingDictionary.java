package com.google.android.gms.common.server.response;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.common.server.response.FastJsonResponse.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FieldMappingDictionary implements SafeParcelable {
    public static final zzc CREATOR = new zzc();
    private final int zzCY;
    private final HashMap<String, Map<String, Field<?, ?>>> zzabQ;
    private final ArrayList<Entry> zzabR;
    private final String zzabS;

    public static class Entry implements SafeParcelable {
        public static final zzd CREATOR = new zzd();
        final String className;
        final int versionCode;
        final ArrayList<FieldMapPair> zzabT;

        Entry(int versionCode, String className, ArrayList<FieldMapPair> fieldMapping) {
            this.versionCode = versionCode;
            this.className = className;
            this.zzabT = fieldMapping;
        }

        Entry(String className, Map<String, Field<?, ?>> fieldMap) {
            this.versionCode = 1;
            this.className = className;
            this.zzabT = zzB(fieldMap);
        }

        private static ArrayList<FieldMapPair> zzB(Map<String, Field<?, ?>> map) {
            if (map == null) {
                return null;
            }
            ArrayList<FieldMapPair> arrayList = new ArrayList();
            for (String str : map.keySet()) {
                arrayList.add(new FieldMapPair(str, (Field) map.get(str)));
            }
            return arrayList;
        }

        public int describeContents() {
            zzd com_google_android_gms_common_server_response_zzd = CREATOR;
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            zzd com_google_android_gms_common_server_response_zzd = CREATOR;
            zzd.zza(this, out, flags);
        }

        HashMap<String, Field<?, ?>> zzoD() {
            HashMap<String, Field<?, ?>> hashMap = new HashMap();
            int size = this.zzabT.size();
            for (int i = 0; i < size; i++) {
                FieldMapPair fieldMapPair = (FieldMapPair) this.zzabT.get(i);
                hashMap.put(fieldMapPair.zzaC, fieldMapPair.zzabU);
            }
            return hashMap;
        }
    }

    public static class FieldMapPair implements SafeParcelable {
        public static final zzb CREATOR = new zzb();
        final int versionCode;
        final String zzaC;
        final Field<?, ?> zzabU;

        FieldMapPair(int versionCode, String key, Field<?, ?> value) {
            this.versionCode = versionCode;
            this.zzaC = key;
            this.zzabU = value;
        }

        FieldMapPair(String key, Field<?, ?> value) {
            this.versionCode = 1;
            this.zzaC = key;
            this.zzabU = value;
        }

        public int describeContents() {
            zzb com_google_android_gms_common_server_response_zzb = CREATOR;
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            zzb com_google_android_gms_common_server_response_zzb = CREATOR;
            zzb.zza(this, out, flags);
        }
    }

    FieldMappingDictionary(int versionCode, ArrayList<Entry> serializedDictionary, String rootClassName) {
        this.zzCY = versionCode;
        this.zzabR = null;
        this.zzabQ = zzc(serializedDictionary);
        this.zzabS = (String) zzu.zzu(rootClassName);
        zzoz();
    }

    public FieldMappingDictionary(Class<? extends FastJsonResponse> rootClazz) {
        this.zzCY = 1;
        this.zzabR = null;
        this.zzabQ = new HashMap();
        this.zzabS = rootClazz.getCanonicalName();
    }

    private static HashMap<String, Map<String, Field<?, ?>>> zzc(ArrayList<Entry> arrayList) {
        HashMap<String, Map<String, Field<?, ?>>> hashMap = new HashMap();
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            Entry entry = (Entry) arrayList.get(i);
            hashMap.put(entry.className, entry.zzoD());
        }
        return hashMap;
    }

    public int describeContents() {
        zzc com_google_android_gms_common_server_response_zzc = CREATOR;
        return 0;
    }

    int getVersionCode() {
        return this.zzCY;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : this.zzabQ.keySet()) {
            stringBuilder.append(str).append(":\n");
            Map map = (Map) this.zzabQ.get(str);
            for (String str2 : map.keySet()) {
                stringBuilder.append("  ").append(str2).append(": ");
                stringBuilder.append(map.get(str2));
            }
        }
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzc com_google_android_gms_common_server_response_zzc = CREATOR;
        zzc.zza(this, out, flags);
    }

    public void zza(Class<? extends FastJsonResponse> cls, Map<String, Field<?, ?>> map) {
        this.zzabQ.put(cls.getCanonicalName(), map);
    }

    public boolean zzb(Class<? extends FastJsonResponse> cls) {
        return this.zzabQ.containsKey(cls.getCanonicalName());
    }

    public Map<String, Field<?, ?>> zzco(String str) {
        return (Map) this.zzabQ.get(str);
    }

    public void zzoA() {
        for (String str : this.zzabQ.keySet()) {
            Map map = (Map) this.zzabQ.get(str);
            HashMap hashMap = new HashMap();
            for (String str2 : map.keySet()) {
                hashMap.put(str2, ((Field) map.get(str2)).zzop());
            }
            this.zzabQ.put(str, hashMap);
        }
    }

    ArrayList<Entry> zzoB() {
        ArrayList<Entry> arrayList = new ArrayList();
        for (String str : this.zzabQ.keySet()) {
            arrayList.add(new Entry(str, (Map) this.zzabQ.get(str)));
        }
        return arrayList;
    }

    public String zzoC() {
        return this.zzabS;
    }

    public void zzoz() {
        for (String str : this.zzabQ.keySet()) {
            Map map = (Map) this.zzabQ.get(str);
            for (String str2 : map.keySet()) {
                ((Field) map.get(str2)).zza(this);
            }
        }
    }
}
