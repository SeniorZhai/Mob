package com.google.android.gms.common.data;

import android.database.CharArrayBuffer;
import android.database.CursorIndexOutOfBoundsException;
import android.database.CursorWindow;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzu;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataHolder implements SafeParcelable {
    public static final zze CREATOR = new zze();
    private static final zza zzYE = new zza(new String[0], null) {
    };
    boolean mClosed;
    private final int zzCY;
    private final int zzTS;
    int[] zzYA;
    int zzYB;
    private Object zzYC;
    private boolean zzYD;
    private final String[] zzYw;
    Bundle zzYx;
    private final CursorWindow[] zzYy;
    private final Bundle zzYz;

    public static class zza {
        private final ArrayList<HashMap<String, Object>> zzYF;
        private final String zzYG;
        private final HashMap<Object, Integer> zzYH;
        private boolean zzYI;
        private String zzYJ;
        private final String[] zzYw;

        private zza(String[] strArr, String str) {
            this.zzYw = (String[]) zzu.zzu(strArr);
            this.zzYF = new ArrayList();
            this.zzYG = str;
            this.zzYH = new HashMap();
            this.zzYI = false;
            this.zzYJ = null;
        }
    }

    public static class zzb extends RuntimeException {
        public zzb(String str) {
            super(str);
        }
    }

    DataHolder(int versionCode, String[] columns, CursorWindow[] windows, int statusCode, Bundle metadata) {
        this.mClosed = false;
        this.zzYD = true;
        this.zzCY = versionCode;
        this.zzYw = columns;
        this.zzYy = windows;
        this.zzTS = statusCode;
        this.zzYz = metadata;
    }

    private DataHolder(zza builder, int statusCode, Bundle metadata) {
        this(builder.zzYw, zza(builder, -1), statusCode, metadata);
    }

    public DataHolder(String[] columns, CursorWindow[] windows, int statusCode, Bundle metadata) {
        this.mClosed = false;
        this.zzYD = true;
        this.zzCY = 1;
        this.zzYw = (String[]) zzu.zzu(columns);
        this.zzYy = (CursorWindow[]) zzu.zzu(windows);
        this.zzTS = statusCode;
        this.zzYz = metadata;
        zznf();
    }

    public static DataHolder zza(int i, Bundle bundle) {
        return new DataHolder(zzYE, i, bundle);
    }

    private static CursorWindow[] zza(zza com_google_android_gms_common_data_DataHolder_zza, int i) {
        int size;
        int i2 = 0;
        if (com_google_android_gms_common_data_DataHolder_zza.zzYw.length == 0) {
            return new CursorWindow[0];
        }
        List zzb = (i < 0 || i >= com_google_android_gms_common_data_DataHolder_zza.zzYF.size()) ? com_google_android_gms_common_data_DataHolder_zza.zzYF : com_google_android_gms_common_data_DataHolder_zza.zzYF.subList(0, i);
        int size2 = zzb.size();
        CursorWindow cursorWindow = new CursorWindow(false);
        ArrayList arrayList = new ArrayList();
        arrayList.add(cursorWindow);
        cursorWindow.setNumColumns(com_google_android_gms_common_data_DataHolder_zza.zzYw.length);
        int i3 = 0;
        int i4 = 0;
        while (i3 < size2) {
            if (!cursorWindow.allocRow()) {
                Log.d("DataHolder", "Allocating additional cursor window for large data set (row " + i3 + ")");
                cursorWindow = new CursorWindow(false);
                cursorWindow.setStartPosition(i3);
                cursorWindow.setNumColumns(com_google_android_gms_common_data_DataHolder_zza.zzYw.length);
                arrayList.add(cursorWindow);
                if (!cursorWindow.allocRow()) {
                    Log.e("DataHolder", "Unable to allocate row to hold data.");
                    arrayList.remove(cursorWindow);
                    return (CursorWindow[]) arrayList.toArray(new CursorWindow[arrayList.size()]);
                }
            }
            try {
                int i5;
                int i6;
                CursorWindow cursorWindow2;
                Map map = (Map) zzb.get(i3);
                boolean z = true;
                for (int i7 = 0; i7 < com_google_android_gms_common_data_DataHolder_zza.zzYw.length && z; i7++) {
                    String str = com_google_android_gms_common_data_DataHolder_zza.zzYw[i7];
                    Object obj = map.get(str);
                    if (obj == null) {
                        z = cursorWindow.putNull(i3, i7);
                    } else if (obj instanceof String) {
                        z = cursorWindow.putString((String) obj, i3, i7);
                    } else if (obj instanceof Long) {
                        z = cursorWindow.putLong(((Long) obj).longValue(), i3, i7);
                    } else if (obj instanceof Integer) {
                        z = cursorWindow.putLong((long) ((Integer) obj).intValue(), i3, i7);
                    } else if (obj instanceof Boolean) {
                        z = cursorWindow.putLong(((Boolean) obj).booleanValue() ? 1 : 0, i3, i7);
                    } else if (obj instanceof byte[]) {
                        z = cursorWindow.putBlob((byte[]) obj, i3, i7);
                    } else if (obj instanceof Double) {
                        z = cursorWindow.putDouble(((Double) obj).doubleValue(), i3, i7);
                    } else if (obj instanceof Float) {
                        z = cursorWindow.putDouble((double) ((Float) obj).floatValue(), i3, i7);
                    } else {
                        throw new IllegalArgumentException("Unsupported object for column " + str + ": " + obj);
                    }
                }
                if (z) {
                    i5 = i3;
                    i6 = 0;
                    cursorWindow2 = cursorWindow;
                } else if (i4 != 0) {
                    throw new zzb("Could not add the value to a new CursorWindow. The size of value may be larger than what a CursorWindow can handle.");
                } else {
                    Log.d("DataHolder", "Couldn't populate window data for row " + i3 + " - allocating new window.");
                    cursorWindow.freeLastRow();
                    CursorWindow cursorWindow3 = new CursorWindow(false);
                    cursorWindow3.setStartPosition(i3);
                    cursorWindow3.setNumColumns(com_google_android_gms_common_data_DataHolder_zza.zzYw.length);
                    arrayList.add(cursorWindow3);
                    i5 = i3 - 1;
                    cursorWindow2 = cursorWindow3;
                    i6 = 1;
                }
                i4 = i6;
                cursorWindow = cursorWindow2;
                i3 = i5 + 1;
            } catch (RuntimeException e) {
                RuntimeException runtimeException = e;
                size = arrayList.size();
                while (i2 < size) {
                    ((CursorWindow) arrayList.get(i2)).close();
                    i2++;
                }
                throw runtimeException;
            }
        }
        return (CursorWindow[]) arrayList.toArray(new CursorWindow[arrayList.size()]);
    }

    public static DataHolder zzbi(int i) {
        return zza(i, null);
    }

    private void zzg(String str, int i) {
        if (this.zzYx == null || !this.zzYx.containsKey(str)) {
            throw new IllegalArgumentException("No such column: " + str);
        } else if (isClosed()) {
            throw new IllegalArgumentException("Buffer is closed.");
        } else if (i < 0 || i >= this.zzYB) {
            throw new CursorIndexOutOfBoundsException(i, this.zzYB);
        }
    }

    public void close() {
        synchronized (this) {
            if (!this.mClosed) {
                this.mClosed = true;
                for (CursorWindow close : this.zzYy) {
                    close.close();
                }
            }
        }
    }

    public int describeContents() {
        return 0;
    }

    protected void finalize() throws Throwable {
        try {
            if (this.zzYD && this.zzYy.length > 0 && !isClosed()) {
                Log.e("DataBuffer", "Internal data leak within a DataBuffer object detected!  Be sure to explicitly call release() on all DataBuffer extending objects when you are done with them. (" + (this.zzYC == null ? "internal object: " + toString() : this.zzYC.toString()) + ")");
                close();
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int getCount() {
        return this.zzYB;
    }

    public int getStatusCode() {
        return this.zzTS;
    }

    int getVersionCode() {
        return this.zzCY;
    }

    public boolean isClosed() {
        boolean z;
        synchronized (this) {
            z = this.mClosed;
        }
        return z;
    }

    public void writeToParcel(Parcel dest, int flags) {
        zze.zza(this, dest, flags);
    }

    public void zza(String str, int i, int i2, CharArrayBuffer charArrayBuffer) {
        zzg(str, i);
        this.zzYy[i2].copyStringToBuffer(i, this.zzYx.getInt(str), charArrayBuffer);
    }

    public long zzb(String str, int i, int i2) {
        zzg(str, i);
        return this.zzYy[i2].getLong(i, this.zzYx.getInt(str));
    }

    public boolean zzbV(String str) {
        return this.zzYx.containsKey(str);
    }

    public int zzbh(int i) {
        int i2 = 0;
        boolean z = i >= 0 && i < this.zzYB;
        zzu.zzU(z);
        while (i2 < this.zzYA.length) {
            if (i < this.zzYA[i2]) {
                i2--;
                break;
            }
            i2++;
        }
        return i2 == this.zzYA.length ? i2 - 1 : i2;
    }

    public int zzc(String str, int i, int i2) {
        zzg(str, i);
        return this.zzYy[i2].getInt(i, this.zzYx.getInt(str));
    }

    public String zzd(String str, int i, int i2) {
        zzg(str, i);
        return this.zzYy[i2].getString(i, this.zzYx.getInt(str));
    }

    public boolean zze(String str, int i, int i2) {
        zzg(str, i);
        return Long.valueOf(this.zzYy[i2].getLong(i, this.zzYx.getInt(str))).longValue() == 1;
    }

    public float zzf(String str, int i, int i2) {
        zzg(str, i);
        return this.zzYy[i2].getFloat(i, this.zzYx.getInt(str));
    }

    public byte[] zzg(String str, int i, int i2) {
        zzg(str, i);
        return this.zzYy[i2].getBlob(i, this.zzYx.getInt(str));
    }

    public Uri zzh(String str, int i, int i2) {
        String zzd = zzd(str, i, i2);
        return zzd == null ? null : Uri.parse(zzd);
    }

    public boolean zzi(String str, int i, int i2) {
        zzg(str, i);
        return this.zzYy[i2].isNull(i, this.zzYx.getInt(str));
    }

    public Bundle zznb() {
        return this.zzYz;
    }

    public void zznf() {
        int i;
        int i2 = 0;
        this.zzYx = new Bundle();
        for (i = 0; i < this.zzYw.length; i++) {
            this.zzYx.putInt(this.zzYw[i], i);
        }
        this.zzYA = new int[this.zzYy.length];
        i = 0;
        while (i2 < this.zzYy.length) {
            this.zzYA[i2] = i;
            i += this.zzYy[i2].getNumRows() - (i - this.zzYy[i2].getStartPosition());
            i2++;
        }
        this.zzYB = i;
    }

    String[] zzng() {
        return this.zzYw;
    }

    CursorWindow[] zznh() {
        return this.zzYy;
    }

    public void zzp(Object obj) {
        this.zzYC = obj;
    }
}
