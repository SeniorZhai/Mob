package com.facebook.share.internal;

import android.support.annotation.Nullable;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphObject;
import com.facebook.share.model.SharePhoto;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class OpenGraphJSONUtility {

    public interface PhotoJSONProcessor {
        JSONObject toJSONObject(SharePhoto sharePhoto);
    }

    public static JSONObject toJSONObject(ShareOpenGraphAction action, PhotoJSONProcessor photoJSONProcessor) throws JSONException {
        JSONObject result = new JSONObject();
        for (String key : action.keySet()) {
            result.put(key, toJSONValue(action.get(key), photoJSONProcessor));
        }
        return result;
    }

    private static JSONObject toJSONObject(ShareOpenGraphObject object, PhotoJSONProcessor photoJSONProcessor) throws JSONException {
        JSONObject result = new JSONObject();
        for (String key : object.keySet()) {
            result.put(key, toJSONValue(object.get(key), photoJSONProcessor));
        }
        return result;
    }

    private static JSONArray toJSONArray(List list, PhotoJSONProcessor photoJSONProcessor) throws JSONException {
        JSONArray result = new JSONArray();
        for (Object item : list) {
            result.put(toJSONValue(item, photoJSONProcessor));
        }
        return result;
    }

    public static Object toJSONValue(@Nullable Object object, PhotoJSONProcessor photoJSONProcessor) throws JSONException {
        if (object == null) {
            return JSONObject.NULL;
        }
        if ((object instanceof String) || (object instanceof Boolean) || (object instanceof Double) || (object instanceof Float) || (object instanceof Integer) || (object instanceof Long)) {
            return object;
        }
        if (object instanceof SharePhoto) {
            if (photoJSONProcessor != null) {
                return photoJSONProcessor.toJSONObject((SharePhoto) object);
            }
            return null;
        } else if (object instanceof ShareOpenGraphObject) {
            return toJSONObject((ShareOpenGraphObject) object, photoJSONProcessor);
        } else {
            if (object instanceof List) {
                return toJSONArray((List) object, photoJSONProcessor);
            }
            throw new IllegalArgumentException("Invalid object found for JSON serialization: " + object.toString());
        }
    }

    private OpenGraphJSONUtility() {
    }
}
