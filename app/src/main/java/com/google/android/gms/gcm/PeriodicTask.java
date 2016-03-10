package com.google.android.gms.gcm;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;

public class PeriodicTask extends Task {
    public static final Creator<PeriodicTask> CREATOR = new Creator<PeriodicTask>() {
        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return zzdX(x0);
        }

        public /* synthetic */ Object[] newArray(int x0) {
            return zzgj(x0);
        }

        public PeriodicTask zzdX(Parcel parcel) {
            return new PeriodicTask(parcel);
        }

        public PeriodicTask[] zzgj(int i) {
            return new PeriodicTask[i];
        }
    };
    protected long mFlexInSeconds;
    protected long mIntervalInSeconds;

    public static class Builder extends com.google.android.gms.gcm.Task.Builder {
        private long zzawd;
        private long zzawe;

        public Builder() {
            this.zzawd = -1;
            this.zzawe = -1;
            this.isPersisted = true;
        }

        public PeriodicTask build() {
            checkConditions();
            return new PeriodicTask();
        }

        protected void checkConditions() {
            super.checkConditions();
            if (this.zzawd == -1) {
                throw new IllegalArgumentException("Must call setPeriod(long) to establish an execution interval for this periodic task.");
            } else if (this.zzawe == -1) {
                this.zzawe = (long) (((float) this.zzawd) * 0.1f);
            }
        }

        public Builder setFlex(long flexInSeconds) {
            this.zzawe = flexInSeconds;
            return this;
        }

        public Builder setPeriod(long intervalInSeconds) {
            this.zzawd = intervalInSeconds;
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
    private PeriodicTask(Parcel in) {
        super(in);
        this.mIntervalInSeconds = -1;
        this.mFlexInSeconds = -1;
        this.mIntervalInSeconds = in.readLong();
        this.mFlexInSeconds = in.readLong();
    }

    private PeriodicTask(Builder builder) {
        super((com.google.android.gms.gcm.Task.Builder) builder);
        this.mIntervalInSeconds = -1;
        this.mFlexInSeconds = -1;
        this.mIntervalInSeconds = builder.zzawd;
        this.mFlexInSeconds = builder.zzawe;
    }

    public long getFlex() {
        return this.mFlexInSeconds;
    }

    public long getPeriod() {
        return this.mIntervalInSeconds;
    }

    public void toBundle(Bundle bundle) {
        super.toBundle(bundle);
        bundle.putLong("period", this.mIntervalInSeconds);
        bundle.putLong("period_flex", this.mFlexInSeconds);
    }

    public String toString() {
        return super.toString() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + "period=" + getPeriod() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + "flex=" + getFlex();
    }

    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeLong(this.mIntervalInSeconds);
        parcel.writeLong(this.mFlexInSeconds);
    }
}
