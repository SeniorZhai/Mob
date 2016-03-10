package com.mobcrush.mobcrush.datamodel;

import android.text.TextUtils;
import com.google.gson.Gson;

public class DataModel {
    public String toString() {
        return new Gson().toJson((Object) this);
    }

    public boolean equals(DataModel dataModel) {
        return TextUtils.equals(toString(), dataModel.toString());
    }
}
