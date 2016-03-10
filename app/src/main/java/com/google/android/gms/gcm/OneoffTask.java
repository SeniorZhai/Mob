package com.google.android.gms.gcm;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

public class OneoffTask extends Task {
    public static final Creator<OneoffTask> CREATOR = new Creator<OneoffTask>() {
        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return zzdV(x0);
        }

        public /* synthetic */ Object[] newArray(int x0) {
            return zzgh(x0);
        }

        public OneoffTask zzdV(Parcel parcel) {
            return new OneoffTask(parcel);
        }

        public OneoffTask[] zzgh(int i) {
            return new OneoffTask[i];
        }
    };
    private final long zzavZ;
    private final long zzawa;

    public static class Builder extends com.google.android.gms.gcm.Task.Builder {
        private long zzawb;
        private long zzawc;

        public Builder() {
            this.zzawb = -1;
            this.zzawc = -1;
            this.isPersisted = false;
        }

        public OneoffTask build() {
            checkConditions();
            return new OneoffTask();
        }

        protected void checkConditions() {
            super.checkConditions();
            if (this.zzawb == -1 || this.zzawc == -1) {
                throw new IllegalArgumentException("Must specify an execution window using setExecutionWindow.");
            } else if (this.zzawb >= this.zzawc) {
                throw new IllegalArgumentException("Window start must be shorter than window end.");
            }
        }

        public Builder setExecutionWindow(long windowStartDelaySeconds, long windowEndDelaySeconds) {
            this.zzawb = windowStartDelaySeconds;
            this.zzawc = windowEndDelaySeconds;
            return this;
        }

        public Builder setPersisted(boolean isPersisted) {
            this.isPersisted = isPersisted;
            return this;
        }

        public Builder setRequiredNetwork(int requiredNetworkState) {
            this.requiredNetworkState = requiredNetworkState;
            return this;
        }

        public Builder setRequiresCharging(boolean requiresCharging) {
            this.requiresCharging = requiresCharging;
            return this;
        }

        public Builder setService(Class<? extends GcmTaskService> gcmTaskService) {
            this.gcmTaskService = gcmTaskService.getName();
            return this;
        }

        public Builder setTag(String tag) {
            this.tag = tag;
            return this;
        }

        public Builder setUpdateCurrent(boolean updateCurrent) {
            this.updateCurrent = updateCurrent;
            return this;
        }
    }

    @Deprecated
    private OneoffTask(Parcel in) {
        super(in);
        this.zzavZ = in.readLong();
        this.zzawa = in.readLong();
    }

    private OneoffTask(Builder builder) {
        super((com.google.android.gms.gcm.Task.Builder) builder);
        this.zzavZ = builder.zzawb;
        this.zzawa = builder.zzawc;
    }

    public long getWindowEnd() {
        return this.zzawa;
    }

    public long getWindowStart() {
        return this.zzavZ;
    }

    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);
        bundle.putLong("window_start", this.zzavZ);
        bundle.putLong("window_end", this.zzawa);
    }

    public String toString() {
        return super.toString() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + "windowStart=" + getWindowStart() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + "windowEnd=" + getWindowEnd();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeLong(this.zzavZ);
        parcel.writeLong(this.zzawa);
    }
}
