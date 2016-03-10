package com.mixpanel.android.util;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONUtils {
    public static String optionalStringKey(JSONObject o, String k) throws JSONException {
        if (!o.has(k) || o.isNull(k)) {
            return null;
        }
        return o.getString(k);
    }
}
