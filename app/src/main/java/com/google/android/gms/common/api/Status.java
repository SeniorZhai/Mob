package com.google.android.gms.common.api;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.IntentSender.SendIntentException;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.google.android.gms.common.internal.zzt;

public final class Status implements Result, SafeParcelable {
    public static final Creator<Status> CREATOR = new zzk();
    public static final Status zzXP = new Status(0);
    public static final Status zzXQ = new Status(14);
    public static final Status zzXR = new Status(8);
    public static final Status zzXS = new Status(15);
    public static final Status zzXT = new Status(16);
    private final PendingIntent mPendingIntent;
    private final int zzCY;
    private final int zzTS;
    private final String zzXU;

    public Status(int statusCode) {
        this(statusCode, null);
    }

    Status(int versionCode, int statusCode, String statusMessage, PendingIntent pendingIntent) {
        this.zzCY = versionCode;
        this.zzTS = statusCode;
        this.zzXU = statusMessage;
        this.mPendingIntent = pendingIntent;
    }

    public Status(int statusCode, String statusMessage) {
        this(1, statusCode, statusMessage, null);
    }

    public Status(int statusCode, String statusMessage, PendingIntent pendingIntent) {
        this(1, statusCode, statusMessage, pendingIntent);
    }

    private String zzmU() {
        return this.zzXU != null ? this.zzXU : CommonStatusCodes.getStatusCodeString(this.zzTS);
    }

    public int describeContents() {
        return 0;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Status)) {
            return false;
        }
        Status status = (Status) obj;
        return this.zzCY == status.zzCY && this.zzTS == status.zzTS && zzt.equal(this.zzXU, status.zzXU) && zzt.equal(this.mPendingIntent, status.mPendingIntent);
    }

    public PendingIntent getResolution() {
        return this.mPendingIntent;
    }

    public Status getStatus() {
        return this;
    }

    public int getStatusCode() {
        return this.zzTS;
    }

    public String getStatusMessage() {
        return this.zzXU;
    }

    int getVersionCode() {
        return this.zzCY;
    }

    public boolean hasResolution() {
        return this.mPendingIntent != null;
    }

    public int hashCode() {
        return zzt.hashCode(Integer.valueOf(this.zzCY), Integer.valueOf(this.zzTS), this.zzXU, this.mPendingIntent);
    }

    public boolean isCanceled() {
        return this.zzTS == 16;
    }

    public boolean isInterrupted() {
        return this.zzTS == 14;
    }

    public boolean isSuccess() {
        return this.zzTS <= 0;
    }

    public void startResolutionForResult(Activity activity, int requestCode) throws SendIntentException {
        if (hasResolution()) {
            activity.startIntentSenderForResult(this.mPendingIntent.getIntentSender(), requestCode, null, 0, 0, 0);
        }
    }

    public String toString() {
        return zzt.zzt(this).zzg("statusCode", zzmU()).zzg("resolution", this.mPendingIntent).toString();
    }

    public void writeToParcel(Parcel out, int flags) {
        zzk.zza(this, out, flags);
    }

    PendingIntent zzmT() {
        return this.mPendingIntent;
    }
}
