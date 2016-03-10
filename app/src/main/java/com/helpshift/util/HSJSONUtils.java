package com.helpshift.util;

import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HSJSONUtils {
    private static final String TAG = "HelpShiftDebug";

    public static String[] getJSONObjectKeys(JSONObject inputObject) {
        Iterator keys = inputObject.keys();
        ArrayList<String> objectKeys = new ArrayList();
        while (keys != null && keys.hasNext()) {
            objectKeys.add((String) keys.next());
        }
        return (String[]) objectKeys.toArray(new String[objectKeys.size()]);
    }

    public static HashMap<String, String> toStringHashMap(JSONObject object) {
        HashMap<String, String> map = new HashMap();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            try {
                if (object.get(key) instanceof String) {
                    map.put(key, object.getString(key));
                }
            } catch (JSONException e) {
                Log.d(TAG, "JsonException ", e);
            }
        }
        return map;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

    public static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        }
        if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        }
        if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        }
        return json;
    }

    public static ArrayList<String> jsonToStringArrayList(String input) {
        ArrayList<String> list = new ArrayList();
        try {
            JSONArray jsonArray = new JSONArray(input);
            int length = jsonArray.length();
            for (int i = 0; i < length; i++) {
                list.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            Log.d(TAG, "jsonToStringArrayList", e);
        }
        return list;
    }
}
