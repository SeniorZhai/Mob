package com.google.android.gms.gcm;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.zzu;

public abstract class Task implements Parcelable {
    public static final int NETWORK_STATE_ANY = 2;
    public static final int NETWORK_STATE_CONNECTED = 0;
    public static final int NETWORK_STATE_UNMETERED = 1;
    protected static final long UNINITIALIZED = -1;
    private final String mTag;
    private final String zzawf;
    private final boolean zzawg;
    private final boolean zzawh;
    private final int zzawi;
    private final boolean zzawj;

    public static abstract class Builder {
        protected String gcmTaskService;
        protected boolean isPersisted;
        protected int requiredNetworkState;
        protected boolean requiresCharging;
        protected String tag;
        protected boolean updateCurrent;

        public abstract Task build();

        protected void checkConditions() {
            zzu.zzb(this.gcmTaskService != null, (Object) "Must provide an endpoint for this task by calling setService(ComponentName).");
            GcmNetworkManager.zzcY(this.tag);
        }

        public abstract Builder setPersisted(boolean z);

        public abstract Builder setRequiredNetwork(int i);

        public abstract Builder setRequiresCharging(boolean z);

        public abstract Builder setService(Class<? extends GcmTaskService> cls);

        public abstract Builder setTag(String str);

        public abstract Builder setUpdateCurrent(boolean z);
    }

    @Deprecated
    Task(Parcel in) {
        boolean z = true;
        this.zzawf = in.readString();
        this.mTag = in.readString();
        this.zzawg = in.readInt() == NETWORK_STATE_UNMETERED;
        if (in.readInt() != NETWORK_STATE_UNMETERED) {
            z = false;
        }
        this.zzawh = z;
        this.zzawi = NETWORK_STATE_ANY;
        this.zzawj = false;
    }

    Task(Builder builder) {
        this.zzawf = builder.gcmTaskService;
        this.mTag = builder.tag;
        this.zzawg = builder.updateCurrent;
        this.zzawh = builder.isPersisted;
        this.zzawi = builder.requiredNetworkState;
        this.zzawj = builder.requiresCharging;
    }

    public int describeContents() {
        return NETWORK_STATE_CONNECTED;
    }

    public int getRequiredNetwork() {
        return this.zzawi;
    }

    public boolean getRequiresCharging() {
        return this.zzawj;
    }

    public String getServiceName() {
        return this.zzawf;
    }

    public String getTag() {
        return this.mTag;
    }

    public boolean isPersisted() {
        return this.zzawh;
    }

    public boolean isUpdateCurrent() {
        return this.zzawg;
    }

    public void toBundle(Bundle bundle) {
        bundle.putString("tag", this.mTag);
        bundle.putBoolean("update_current", this.zzawg);
        bundle.putBoolean("persisted", this.zzawh);
        bundle.putString(NotificationCompatApi21.CATEGORY_SERVICE, this.zzawf);
        bundle.putInt("requiredNetwork", this.zzawi);
        bundle.putBoolean("requiresCharging", this.zzawj);
    }

    public void writeToParcel(Parcel parcel, int i) {
        int i2 = NETWORK_STATE_UNMETERED;
        parcel.writeString(this.zzawf);
        parcel.writeString(this.mTag);
        parcel.writeInt(this.zzawg ? NETWORK_STATE_UNMETERED : NETWORK_STATE_CONNECTED);
        if (!this.zzawh) {
            i2 = NETWORK_STATE_CONNECTED;
        }
        parcel.writeInt(i2);
    }
}
