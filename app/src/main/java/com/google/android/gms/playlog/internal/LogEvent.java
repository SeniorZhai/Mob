package com.google.android.gms.playlog.internal;

import android.os.Bundle;
import android.os.Parcel;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

public class LogEvent implements SafeParcelable {
    public static final zzc CREATOR = new zzc();
    public final String tag;
    public final int versionCode;
    public final long zzaGF;
    public final byte[] zzaGG;
    public final Bundle zzaGH;

    LogEvent(int versionCode, long eventTime, String tag, byte[] sourceExtensionBytes, Bundle keyValuePairs) {
        this.versionCode = versionCode;
        this.zzaGF = eventTime;
        this.tag = tag;
        this.zzaGG = sourceExtensionBytes;
        this.zzaGH = keyValuePairs;
    }

    public LogEvent(long eventTime, String tag, byte[] sourceExtensionBytes, String... extras) {
        this.versionCode = 1;
        this.zzaGF = eventTime;
        this.tag = tag;
        this.zzaGG = sourceExtensionBytes;
        this.zzaGH = zzd(extras);
    }

    private static Bundle zzd(String... strArr) {
        Bundle bundle = null;
        if (strArr != null) {
            if (strArr.length % 2 != 0) {
                throw new IllegalArgumentException("extras must have an even number of elements");
            }
            int length = strArr.length / 2;
            if (length != 0) {
                bundle = new Bundle(length);
                for (int i = 0; i < length; i++) {
                    bundle.putString(strArr[i * 2], strArr[(i * 2) + 1]);
                }
            }
        }
        return bundle;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("tag=").append(this.tag).append(",");
        stringBuilder.append("eventTime=").append(this.zzaGF).append(",");
        if (!(this.zzaGH == null || this.zzaGH.isEmpty())) {
            stringBuilder.append("keyValues=");
            for (String str : this.zzaGH.keySet()) {
                stringBuilder.append("(").append(str).append(",");
                stringBuilder.append(this.zzaGH.getString(str)).append(")");
                stringBuilder.append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
            }
        }
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzc.zza(this, out, flags);
    }
}
