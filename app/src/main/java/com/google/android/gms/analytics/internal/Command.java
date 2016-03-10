package com.google.android.gms.analytics.internal;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Command implements Parcelable {
    @Deprecated
    public static final Creator<Command> CREATOR = new Creator<Command>() {
        public /* synthetic */ Object createFromParcel(Parcel x0) {
            return zzq(x0);
        }

        public /* synthetic */ Object[] newArray(int x0) {
            return zzaa(x0);
        }

        @Deprecated
        public Command[] zzaa(int i) {
            return new Command[i];
        }

        @Deprecated
        public Command zzq(Parcel parcel) {
            return new Command(parcel);
        }
    };
    private String mValue;
    private String zzKI;
    private String zzKJ;

    @Deprecated
    Command(Parcel in) {
        readFromParcel(in);
    }

    @Deprecated
    private void readFromParcel(Parcel in) {
        this.zzKI = in.readString();
        this.zzKJ = in.readString();
        this.mValue = in.readString();
    }

    @Deprecated
    public int describeContents() {
        return 0;
    }

    public String getId() {
        return this.zzKI;
    }

    public String getValue() {
        return this.mValue;
    }

    @Deprecated
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.zzKI);
        out.writeString(this.zzKJ);
        out.writeString(this.mValue);
    }
}
