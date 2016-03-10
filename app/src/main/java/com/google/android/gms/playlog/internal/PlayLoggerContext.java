package com.google.android.gms.playlog.internal;

import android.os.Parcel;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzt;
import com.google.android.gms.common.internal.zzu;

public class PlayLoggerContext implements SafeParcelable {
    public static final zze CREATOR = new zze();
    public final String packageName;
    public final int versionCode;
    public final int zzaGP;
    public final int zzaGQ;
    public final String zzaGR;
    public final String zzaGS;
    public final boolean zzaGT;
    public final String zzaGU;
    public final boolean zzaGV;

    public PlayLoggerContext(int versionCode, String packageName, int packageVersionCode, int logSource, String uploadAccountName, String loggingId, boolean logAndroidId, String logSourceName, boolean isAnonymous) {
        this.versionCode = versionCode;
        this.packageName = packageName;
        this.zzaGP = packageVersionCode;
        this.zzaGQ = logSource;
        this.zzaGR = uploadAccountName;
        this.zzaGS = loggingId;
        this.zzaGT = logAndroidId;
        this.zzaGU = logSourceName;
        this.zzaGV = isAnonymous;
    }

    @Deprecated
    public PlayLoggerContext(String packageName, int packageVersionCode, int logSource, String uploadAccountName, String loggingId, boolean logAndroidId) {
        this.versionCode = 1;
        this.packageName = (String) zzu.zzu(packageName);
        this.zzaGP = packageVersionCode;
        this.zzaGQ = logSource;
        this.zzaGU = null;
        this.zzaGR = uploadAccountName;
        this.zzaGS = loggingId;
        this.zzaGT = logAndroidId;
        this.zzaGV = false;
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof PlayLoggerContext)) {
            return false;
        }
        PlayLoggerContext playLoggerContext = (PlayLoggerContext) object;
        return this.versionCode == playLoggerContext.versionCode && this.packageName.equals(playLoggerContext.packageName) && this.zzaGP == playLoggerContext.zzaGP && this.zzaGQ == playLoggerContext.zzaGQ && zzt.equal(this.zzaGU, playLoggerContext.zzaGU) && zzt.equal(this.zzaGR, playLoggerContext.zzaGR) && zzt.equal(this.zzaGS, playLoggerContext.zzaGS) && this.zzaGT == playLoggerContext.zzaGT && this.zzaGV == playLoggerContext.zzaGV;
    }

    public int hashCode() {
        return zzt.hashCode(Integer.valueOf(this.versionCode), this.packageName, Integer.valueOf(this.zzaGP), Integer.valueOf(this.zzaGQ), this.zzaGU, this.zzaGR, this.zzaGS, Boolean.valueOf(this.zzaGT), Boolean.valueOf(this.zzaGV));
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("PlayLoggerContext[");
        stringBuilder.append("versionCode=").append(this.versionCode).append(',');
        stringBuilder.append("package=").append(this.packageName).append(',');
        stringBuilder.append("packageVersionCode=").append(this.zzaGP).append(',');
        stringBuilder.append("logSource=").append(this.zzaGQ).append(',');
        stringBuilder.append("logSourceName=").append(this.zzaGU).append(',');
        stringBuilder.append("uploadAccount=").append(this.zzaGR).append(',');
        stringBuilder.append("loggingId=").append(this.zzaGS).append(',');
        stringBuilder.append("logAndroidId=").append(this.zzaGT).append(',');
        stringBuilder.append("isAnonymous=").append(this.zzaGV);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zze.zza(this, out, flags);
    }
}
