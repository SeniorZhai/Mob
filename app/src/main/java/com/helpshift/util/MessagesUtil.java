package com.helpshift.util;

import android.util.Log;
import android.util.Pair;
import com.helpshift.constants.MessageColumns;
import com.helpshift.res.values.HSConsts;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MessagesUtil {
    private static final String TAG = "HelpShiftDebug";
    private static final List<Pair<String, String>> notificationsOffMessages = Arrays.asList(new Pair[]{new Pair("admin", "rfr"), new Pair("mobile", "ra"), new Pair("mobile", "rj")});
    private static final List<Pair<String, String>> supportedMessages = Arrays.asList(new Pair[]{new Pair("admin", "txt"), new Pair("admin", "rsc"), new Pair("admin", "rar"), new Pair("admin", "rfr"), new Pair("admin", HSConsts.ADMIN_ATTACHMENT_IMAGE_TYPE), new Pair("admin", HSConsts.ADMIN_ATTACHMENT_GENERIC_TYPE), new Pair("mobile", "txt"), new Pair("mobile", "ncr"), new Pair("mobile", "sc"), new Pair("mobile", "ar")});

    public static boolean isMessageSupported(String origin, String type) {
        return supportedMessages.contains(new Pair(origin, type));
    }

    public static boolean notificationsTurnedOff(String origin, String type) {
        return notificationsOffMessages.contains(new Pair(origin, type));
    }

    public static boolean isRfrAccepted(JSONArray messages, int startIndex, String rfrMessageId) {
        for (int i = startIndex; i < messages.length(); i++) {
            try {
                JSONObject message = messages.getJSONObject(i);
                String origin = message.getString(MessageColumns.ORIGIN);
                String type = message.getString(MessageColumns.TYPE);
                if (origin.equals("mobile") && type.equals("ra") && message.getJSONObject(MessageColumns.META).getString("refers").equals(rfrMessageId)) {
                    return true;
                }
            } catch (JSONException e) {
                Log.d(TAG, "isRfrAccepted", e);
            }
        }
        return false;
    }
}
