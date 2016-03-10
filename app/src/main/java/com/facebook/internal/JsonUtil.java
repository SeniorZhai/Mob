package com.facebook.internal;

import android.annotation.SuppressLint;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

class JsonUtil {

    private static final class JSONObjectEntry implements Entry<String, Object> {
        private final String key;
        private final Object value;

        JSONObjectEntry(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        @SuppressLint({"FieldGetter"})
        public String getKey() {
            return this.key;
        }

        public Object getValue() {
            return this.value;
        }

        public Object setValue(Object object) {
            throw new UnsupportedOperationException("JSONObjectEntry is immutable");
        }
    }

    JsonUtil() {
    }

    static void jsonObjectClear(JSONObject jsonObject) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            keys.next();
            keys.remove();
        }
    }

    static boolean jsonObjectContainsValue(JSONObject jsonObject, Object value) {
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            Object thisValue = jsonObject.opt((String) keys.next());
            if (thisValue != null && thisValue.equals(value)) {
                return true;
            }
        }
        return false;
    }

    static Set<Entry<String, Object>> jsonObjectEntrySet(JSONObject jsonObject) {
        HashSet<Entry<String, Object>> result = new HashSet();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            result.add(new JSONObjectEntry(key, jsonObject.opt(key)));
        }
        return result;
    }

    static Set<String> jsonObjectKeySet(JSONObject jsonObject) {
        HashSet<String> result = new HashSet();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            result.add(keys.next());
        }
        return result;
    }

    static void jsonObjectPutAll(JSONObject jsonObject, Map<String, Object> map) {
        for (Entry<String, Object> entry : map.entrySet()) {
            try {
                jsonObject.putOpt((String) entry.getKey(), entry.getValue());
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    static Collection<Object> jsonObjectValues(JSONObject jsonObject) {
        ArrayList<Object> result = new ArrayList();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            result.add(jsonObject.opt((String) keys.next()));
        }
        return result;
    }
}
