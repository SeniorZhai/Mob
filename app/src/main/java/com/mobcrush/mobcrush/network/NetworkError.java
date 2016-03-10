package com.mobcrush.mobcrush.network;

import android.text.TextUtils;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetworkError {
    public static String getFirstErrorString(JSONObject error, String model) {
        JSONArray errorMessages = error.optJSONArray(model);
        if (errorMessages == null || errorMessages.length() <= 0) {
            return null;
        }
        return errorMessages.optString(0);
    }

    public static String getErrorMessage(String error, String model) {
        String message = MainApplication.getRString(R.string.error_network_undeterminated, new Object[0]);
        if (!TextUtils.isEmpty(error)) {
            try {
                message = new JSONObject(error).getString(model);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return message;
    }
}
