package com.google.android.gms.common.server.response;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.common.server.converter.ConverterWrapper;
import com.google.android.gms.internal.zzky;
import com.google.android.gms.internal.zzlh;
import com.google.android.gms.internal.zzli;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.protocol.HTTP;

public abstract class FastJsonResponse {

    public interface zza<I, O> {
        I convertBack(O o);

        int zzok();

        int zzol();
    }

    public static class Field<I, O> implements SafeParcelable {
        public static final zza CREATOR = new zza();
        private final int zzCY;
        protected final int zzabG;
        protected final boolean zzabH;
        protected final int zzabI;
        protected final boolean zzabJ;
        protected final String zzabK;
        protected final int zzabL;
        protected final Class<? extends FastJsonResponse> zzabM;
        protected final String zzabN;
        private FieldMappingDictionary zzabO;
        private zza<I, O> zzabP;

        Field(int versionCode, int typeIn, boolean typeInArray, int typeOut, boolean typeOutArray, String outputFieldName, int safeParcelableFieldId, String concreteTypeName, ConverterWrapper wrappedConverter) {
            this.zzCY = versionCode;
            this.zzabG = typeIn;
            this.zzabH = typeInArray;
            this.zzabI = typeOut;
            this.zzabJ = typeOutArray;
            this.zzabK = outputFieldName;
            this.zzabL = safeParcelableFieldId;
            if (concreteTypeName == null) {
                this.zzabM = null;
                this.zzabN = null;
            } else {
                this.zzabM = SafeParcelResponse.class;
                this.zzabN = concreteTypeName;
            }
            if (wrappedConverter == null) {
                this.zzabP = null;
            } else {
                this.zzabP = wrappedConverter.zzoi();
            }
        }

        protected Field(int typeIn, boolean typeInArray, int typeOut, boolean typeOutArray, String outputFieldName, int safeParcelableFieldId, Class<? extends FastJsonResponse> concreteType, zza<I, O> converter) {
            this.zzCY = 1;
            this.zzabG = typeIn;
            this.zzabH = typeInArray;
            this.zzabI = typeOut;
            this.zzabJ = typeOutArray;
            this.zzabK = outputFieldName;
            this.zzabL = safeParcelableFieldId;
            this.zzabM = concreteType;
            if (concreteType == null) {
                this.zzabN = null;
            } else {
                this.zzabN = concreteType.getCanonicalName();
            }
            this.zzabP = converter;
        }

        public static Field zza(String str, int i, zza<?, ?> com_google_android_gms_common_server_response_FastJsonResponse_zza___, boolean z) {
            return new Field(com_google_android_gms_common_server_response_FastJsonResponse_zza___.zzok(), z, com_google_android_gms_common_server_response_FastJsonResponse_zza___.zzol(), false, str, i, null, com_google_android_gms_common_server_response_FastJsonResponse_zza___);
        }

        public static <T extends FastJsonResponse> Field<T, T> zza(String str, int i, Class<T> cls) {
            return new Field(11, false, 11, false, str, i, cls, null);
        }

        public static <T extends FastJsonResponse> Field<ArrayList<T>, ArrayList<T>> zzb(String str, int i, Class<T> cls) {
            return new Field(11, true, 11, true, str, i, cls, null);
        }

        public static Field<Integer, Integer> zzi(String str, int i) {
            return new Field(0, false, 0, false, str, i, null, null);
        }

        public static Field<Double, Double> zzj(String str, int i) {
            return new Field(4, false, 4, false, str, i, null, null);
        }

        public static Field<Boolean, Boolean> zzk(String str, int i) {
            return new Field(6, false, 6, false, str, i, null, null);
        }

        public static Field<String, String> zzl(String str, int i) {
            return new Field(7, false, 7, false, str, i, null, null);
        }

        public static Field<ArrayList<String>, ArrayList<String>> zzm(String str, int i) {
            return new Field(7, true, 7, true, str, i, null, null);
        }

        public I convertBack(O output) {
            return this.zzabP.convertBack(output);
        }

        public int describeContents() {
            zza com_google_android_gms_common_server_response_zza = CREATOR;
            return 0;
        }

        public int getVersionCode() {
            return this.zzCY;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Field\n");
            stringBuilder.append("            versionCode=").append(this.zzCY).append('\n');
            stringBuilder.append("                 typeIn=").append(this.zzabG).append('\n');
            stringBuilder.append("            typeInArray=").append(this.zzabH).append('\n');
            stringBuilder.append("                typeOut=").append(this.zzabI).append('\n');
            stringBuilder.append("           typeOutArray=").append(this.zzabJ).append('\n');
            stringBuilder.append("        outputFieldName=").append(this.zzabK).append('\n');
            stringBuilder.append("      safeParcelFieldId=").append(this.zzabL).append('\n');
            stringBuilder.append("       concreteTypeName=").append(zzov()).append('\n');
            if (zzou() != null) {
                stringBuilder.append("     concreteType.class=").append(zzou().getCanonicalName()).append('\n');
            }
            stringBuilder.append("          converterName=").append(this.zzabP == null ? "null" : this.zzabP.getClass().getCanonicalName()).append('\n');
            return stringBuilder.toString();
        }

        public void writeToParcel(Parcel out, int flags) {
            zza com_google_android_gms_common_server_response_zza = CREATOR;
            zza.zza(this, out, flags);
        }

        public void zza(FieldMappingDictionary fieldMappingDictionary) {
            this.zzabO = fieldMappingDictionary;
        }

        public int zzok() {
            return this.zzabG;
        }

        public int zzol() {
            return this.zzabI;
        }

        public Field<I, O> zzop() {
            return new Field(this.zzCY, this.zzabG, this.zzabH, this.zzabI, this.zzabJ, this.zzabK, this.zzabL, this.zzabN, zzox());
        }

        public boolean zzoq() {
            return this.zzabH;
        }

        public boolean zzor() {
            return this.zzabJ;
        }

        public String zzos() {
            return this.zzabK;
        }

        public int zzot() {
            return this.zzabL;
        }

        public Class<? extends FastJsonResponse> zzou() {
            return this.zzabM;
        }

        String zzov() {
            return this.zzabN == null ? null : this.zzabN;
        }

        public boolean zzow() {
            return this.zzabP != null;
        }

        ConverterWrapper zzox() {
            return this.zzabP == null ? null : ConverterWrapper.zza(this.zzabP);
        }

        public Map<String, Field<?, ?>> zzoy() {
            zzu.zzu(this.zzabN);
            zzu.zzu(this.zzabO);
            return this.zzabO.zzco(this.zzabN);
        }
    }

    private void zza(StringBuilder stringBuilder, Field field, Object obj) {
        if (field.zzok() == 11) {
            stringBuilder.append(((FastJsonResponse) field.zzou().cast(obj)).toString());
        } else if (field.zzok() == 7) {
            stringBuilder.append("\"");
            stringBuilder.append(zzlh.zzcr((String) obj));
            stringBuilder.append("\"");
        } else {
            stringBuilder.append(obj);
        }
    }

    private void zza(StringBuilder stringBuilder, Field field, ArrayList<Object> arrayList) {
        stringBuilder.append("[");
        int size = arrayList.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            Object obj = arrayList.get(i);
            if (obj != null) {
                zza(stringBuilder, field, obj);
            }
        }
        stringBuilder.append("]");
    }

    public String toString() {
        Map zzom = zzom();
        StringBuilder stringBuilder = new StringBuilder(100);
        for (String str : zzom.keySet()) {
            Field field = (Field) zzom.get(str);
            if (zza(field)) {
                Object zza = zza(field, zzb(field));
                if (stringBuilder.length() == 0) {
                    stringBuilder.append("{");
                } else {
                    stringBuilder.append(",");
                }
                stringBuilder.append("\"").append(str).append("\":");
                if (zza != null) {
                    switch (field.zzol()) {
                        case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                            stringBuilder.append("\"").append(zzky.zzi((byte[]) zza)).append("\"");
                            break;
                        case HTTP.HT /*9*/:
                            stringBuilder.append("\"").append(zzky.zzj((byte[]) zza)).append("\"");
                            break;
                        case HTTP.LF /*10*/:
                            zzli.zza(stringBuilder, (HashMap) zza);
                            break;
                        default:
                            if (!field.zzoq()) {
                                zza(stringBuilder, field, zza);
                                break;
                            }
                            zza(stringBuilder, field, (ArrayList) zza);
                            break;
                    }
                }
                stringBuilder.append("null");
            }
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.append("}");
        } else {
            stringBuilder.append("{}");
        }
        return stringBuilder.toString();
    }

    protected <O, I> I zza(Field<I, O> field, Object obj) {
        return field.zzabP != null ? field.convertBack(obj) : obj;
    }

    protected boolean zza(Field field) {
        return field.zzol() == 11 ? field.zzor() ? zzcn(field.zzos()) : zzcm(field.zzos()) : zzcl(field.zzos());
    }

    protected Object zzb(Field field) {
        String zzos = field.zzos();
        if (field.zzou() == null) {
            return zzck(field.zzos());
        }
        zzu.zza(zzck(field.zzos()) == null, "Concrete field shouldn't be value object: %s", field.zzos());
        Map zzoo = field.zzor() ? zzoo() : zzon();
        if (zzoo != null) {
            return zzoo.get(zzos);
        }
        try {
            return getClass().getMethod("get" + Character.toUpperCase(zzos.charAt(0)) + zzos.substring(1), new Class[0]).invoke(this, new Object[0]);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract Object zzck(String str);

    protected abstract boolean zzcl(String str);

    protected boolean zzcm(String str) {
        throw new UnsupportedOperationException("Concrete types not supported");
    }

    protected boolean zzcn(String str) {
        throw new UnsupportedOperationException("Concrete type arrays not supported");
    }

    public abstract Map<String, Field<?, ?>> zzom();

    public HashMap<String, Object> zzon() {
        return null;
    }

    public HashMap<String, Object> zzoo() {
        return null;
    }
}
