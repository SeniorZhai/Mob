package com.google.android.gms.common.server.converter;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.server.response.FastJsonResponse.zza;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public final class StringToIntConverter implements SafeParcelable, zza<String, Integer> {
    public static final zzb CREATOR = new zzb();
    private final int zzCY;
    private final HashMap<String, Integer> zzabB;
    private final HashMap<Integer, String> zzabC;
    private final ArrayList<Entry> zzabD;

    public static final class Entry implements SafeParcelable {
        public static final zzc CREATOR = new zzc();
        final int versionCode;
        final String zzabE;
        final int zzabF;

        Entry(int versionCode, String stringValue, int intValue) {
            this.versionCode = versionCode;
            this.zzabE = stringValue;
            this.zzabF = intValue;
        }

        Entry(String stringValue, int intValue) {
            this.versionCode = 1;
            this.zzabE = stringValue;
            this.zzabF = intValue;
        }

        public int describeContents() {
            zzc com_google_android_gms_common_server_converter_zzc = CREATOR;
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            zzc com_google_android_gms_common_server_converter_zzc = CREATOR;
            zzc.zza(this, out, flags);
        }
    }

    public StringToIntConverter() {
        this.zzCY = 1;
        this.zzabB = new HashMap();
        this.zzabC = new HashMap();
        this.zzabD = null;
    }

    StringToIntConverter(int versionCode, ArrayList<Entry> serializedMap) {
        this.zzCY = versionCode;
        this.zzabB = new HashMap();
        this.zzabC = new HashMap();
        this.zzabD = null;
        zzb((ArrayList) serializedMap);
    }

    private void zzb(ArrayList<Entry> arrayList) {
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            zzh(entry.zzabE, entry.zzabF);
        }
    }

    public /* synthetic */ Object convertBack(Object x0) {
        return zzb((Integer) x0);
    }

    public int describeContents() {
        zzb com_google_android_gms_common_server_converter_zzb = CREATOR;
        return 0;
    }

    int getVersionCode() {
        return this.zzCY;
    }

    public void writeToParcel(Parcel out, int flags) {
        zzb com_google_android_gms_common_server_converter_zzb = CREATOR;
        zzb.zza(this, out, flags);
    }

    public String zzb(Integer num) {
        String str = (String) this.zzabC.get(num);
        return (str == null && this.zzabB.containsKey("gms_unknown")) ? "gms_unknown" : str;
    }

    public StringToIntConverter zzh(String str, int i) {
        this.zzabB.put(str, Integer.valueOf(i));
        this.zzabC.put(Integer.valueOf(i), str);
        return this;
    }

    ArrayList<Entry> zzoj() {
        ArrayList<Entry> arrayList = new ArrayList();
        for (String str : this.zzabB.keySet()) {
            arrayList.add(new Entry(str, ((Integer) this.zzabB.get(str)).intValue()));
        }
        return arrayList;
    }

    public int zzok() {
        return 7;
    }

    public int zzol() {
        return 0;
    }
}
