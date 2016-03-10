package com.facebook.internal;

import com.facebook.FacebookException;
import com.facebook.share.internal.ShareConstants;
import com.helpshift.constants.MessageColumns;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import org.json.JSONArray;
import org.json.JSONObject;

public class GraphUtil {
    private static final String[] dateFormats = new String[]{"yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd"};

    public static JSONObject createOpenGraphActionForPost(String type) {
        JSONObject action = new JSONObject();
        if (type != null) {
            try {
                action.put(MessageColumns.TYPE, type);
            } catch (Throwable e) {
                throw new FacebookException("An error occurred while setting up the open graph action", e);
            }
        }
        return action;
    }

    public static JSONObject createOpenGraphObjectForPost(String type) {
        return createOpenGraphObjectForPost(type, null, null, null, null, null, null);
    }

    public static JSONObject createOpenGraphObjectForPost(String type, String title, String imageUrl, String url, String description, JSONObject objectProperties, String id) {
        JSONObject openGraphObject = new JSONObject();
        if (type != null) {
            try {
                openGraphObject.put(MessageColumns.TYPE, type);
            } catch (Throwable e) {
                throw new FacebookException("An error occurred while setting up the graph object", e);
            }
        }
        openGraphObject.put(SettingsJsonConstants.PROMPT_TITLE_KEY, title);
        if (imageUrl != null) {
            JSONObject imageUrlObject = new JSONObject();
            imageUrlObject.put(SettingsJsonConstants.APP_URL_KEY, imageUrl);
            JSONArray imageUrls = new JSONArray();
            imageUrls.put(imageUrlObject);
            openGraphObject.put("image", imageUrls);
        }
        openGraphObject.put(SettingsJsonConstants.APP_URL_KEY, url);
        openGraphObject.put(ShareConstants.WEB_DIALOG_PARAM_DESCRIPTION, description);
        openGraphObject.put(NativeProtocol.OPEN_GRAPH_CREATE_OBJECT_KEY, true);
        if (objectProperties != null) {
            openGraphObject.put(MPDbAdapter.KEY_DATA, objectProperties);
        }
        if (id != null) {
            openGraphObject.put(DBLikedChannelsHelper.KEY_ID, id);
        }
        return openGraphObject;
    }

    public static boolean isOpenGraphObjectForPost(JSONObject object) {
        return object != null ? object.optBoolean(NativeProtocol.OPEN_GRAPH_CREATE_OBJECT_KEY) : false;
    }
}
