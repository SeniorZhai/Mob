package com.mixpanel.android.mpmetrics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Build.VERSION;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.mixpanel.android.mpmetrics.InAppNotification.Type;
import com.mixpanel.android.util.RemoteService;
import com.mixpanel.android.util.RemoteService.ServiceUnavailableException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class DecideChecker {
    private static final JSONArray EMPTY_JSON_ARRAY = new JSONArray();
    private static final String LOGTAG = "MixpanelAPI.DChecker";
    private final List<DecideMessages> mChecks = new LinkedList();
    private final MPConfig mConfig;
    private final Context mContext;

    static class Result {
        public JSONArray eventBindings = DecideChecker.EMPTY_JSON_ARRAY;
        public final List<InAppNotification> notifications = new ArrayList();
        public final List<Survey> surveys = new ArrayList();
        public JSONArray variants = DecideChecker.EMPTY_JSON_ARRAY;
    }

    static class UnintelligibleMessageException extends Exception {
        private static final long serialVersionUID = -6501269367559104957L;

        public UnintelligibleMessageException(String message, JSONException cause) {
            super(message, cause);
        }
    }

    public DecideChecker(Context context, MPConfig config) {
        this.mContext = context;
        this.mConfig = config;
    }

    public void addDecideCheck(DecideMessages check) {
        this.mChecks.add(check);
    }

    public void runDecideChecks(RemoteService poster) throws ServiceUnavailableException {
        for (DecideMessages updates : this.mChecks) {
            try {
                Result result = runDecideCheck(updates.getToken(), updates.getDistinctId(), poster);
                updates.reportResults(result.surveys, result.notifications, result.eventBindings, result.variants);
            } catch (UnintelligibleMessageException e) {
                Log.e(LOGTAG, e.getMessage(), e);
            }
        }
    }

    private Result runDecideCheck(String token, String distinctId, RemoteService poster) throws ServiceUnavailableException, UnintelligibleMessageException {
        String responseString = getDecideResponseFromServer(token, distinctId, poster);
        if (MPConfig.DEBUG) {
            Log.v(LOGTAG, "Mixpanel decide server response was:\n" + responseString);
        }
        Result parsed = new Result();
        if (responseString != null) {
            parsed = parseDecideResponse(responseString);
        }
        Iterator<InAppNotification> notificationIterator = parsed.notifications.iterator();
        while (notificationIterator.hasNext()) {
            InAppNotification notification = (InAppNotification) notificationIterator.next();
            Bitmap image = getNotificationImage(notification, this.mContext, poster);
            if (image == null) {
                Log.i(LOGTAG, "Could not retrieve image for notification " + notification.getId() + ", will not show the notification.");
                notificationIterator.remove();
            } else {
                notification.setImage(image);
            }
        }
        return parsed;
    }

    static Result parseDecideResponse(String responseString) throws UnintelligibleMessageException {
        Result ret = new Result();
        try {
            int i;
            JSONObject response = new JSONObject(responseString);
            JSONArray surveys = null;
            if (response.has("surveys")) {
                try {
                    surveys = response.getJSONArray("surveys");
                } catch (JSONException e) {
                    Log.e(LOGTAG, "Mixpanel endpoint returned non-array JSON for surveys: " + response);
                }
            }
            if (surveys != null) {
                for (i = 0; i < surveys.length(); i++) {
                    try {
                        ret.surveys.add(new Survey(surveys.getJSONObject(i)));
                    } catch (JSONException e2) {
                        Log.e(LOGTAG, "Received a strange response from surveys service: " + surveys.toString());
                    } catch (BadDecideObjectException e3) {
                        Log.e(LOGTAG, "Received a strange response from surveys service: " + surveys.toString());
                    }
                }
            }
            JSONArray notifications = null;
            if (response.has("notifications")) {
                try {
                    notifications = response.getJSONArray("notifications");
                } catch (JSONException e4) {
                    Log.e(LOGTAG, "Mixpanel endpoint returned non-array JSON for notifications: " + response);
                }
            }
            if (notifications != null) {
                int notificationsToRead = Math.min(notifications.length(), 2);
                for (i = 0; i < notificationsToRead; i++) {
                    try {
                        ret.notifications.add(new InAppNotification(notifications.getJSONObject(i)));
                    } catch (JSONException e5) {
                        Log.e(LOGTAG, "Received a strange response from notifications service: " + notifications.toString(), e5);
                    } catch (BadDecideObjectException e6) {
                        Log.e(LOGTAG, "Received a strange response from notifications service: " + notifications.toString(), e6);
                    } catch (OutOfMemoryError e7) {
                        Log.e(LOGTAG, "Not enough memory to show load notification from package: " + notifications.toString(), e7);
                    }
                }
            }
            if (response.has("event_bindings")) {
                try {
                    ret.eventBindings = response.getJSONArray("event_bindings");
                } catch (JSONException e8) {
                    Log.e(LOGTAG, "Mixpanel endpoint returned non-array JSON for event bindings: " + response);
                }
            }
            if (response.has("variants")) {
                try {
                    ret.variants = response.getJSONArray("variants");
                } catch (JSONException e9) {
                    Log.e(LOGTAG, "Mixpanel endpoint returned non-array JSON for variants: " + response);
                }
            }
            return ret;
        } catch (JSONException e52) {
            throw new UnintelligibleMessageException("Mixpanel endpoint returned unparsable result:\n" + responseString, e52);
        }
    }

    private String getDecideResponseFromServer(String unescapedToken, String unescapedDistinctId, RemoteService poster) throws ServiceUnavailableException {
        try {
            String escapedId;
            String escapedToken = URLEncoder.encode(unescapedToken, "utf-8");
            if (unescapedDistinctId != null) {
                escapedId = URLEncoder.encode(unescapedDistinctId, "utf-8");
            } else {
                escapedId = null;
            }
            StringBuilder queryBuilder = new StringBuilder().append("?version=1&lib=android&token=").append(escapedToken);
            if (escapedId != null) {
                queryBuilder.append("&distinct_id=").append(escapedId);
            }
            String checkQuery = queryBuilder.toString();
            String[] urls = this.mConfig.getDisableFallback() ? new String[]{this.mConfig.getDecideEndpoint() + checkQuery} : new String[]{this.mConfig.getDecideEndpoint() + checkQuery, this.mConfig.getDecideFallbackEndpoint() + checkQuery};
            if (MPConfig.DEBUG) {
                Log.v(LOGTAG, "Querying decide server, urls:");
                for (String str : urls) {
                    Log.v(LOGTAG, "    >> " + str);
                }
            }
            byte[] response = getUrls(poster, this.mContext, urls);
            if (response == null) {
                return null;
            }
            try {
                return new String(response, HTTP.UTF_8);
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF not supported on this platform?", e);
            }
        } catch (UnsupportedEncodingException e2) {
            throw new RuntimeException("Mixpanel library requires utf-8 string encoding to be available", e2);
        }
    }

    private static Bitmap getNotificationImage(InAppNotification notification, Context context, RemoteService poster) throws ServiceUnavailableException {
        String[] urls = new String[]{notification.getImage2xUrl(), notification.getImageUrl()};
        int displayWidth = getDisplayWidth(((WindowManager) context.getSystemService("window")).getDefaultDisplay());
        if (notification.getType() == Type.TAKEOVER && displayWidth >= 720) {
            urls = new String[]{notification.getImage4xUrl(), notification.getImage2xUrl(), notification.getImageUrl()};
        }
        byte[] response = getUrls(poster, context, urls);
        if (response != null) {
            return BitmapFactory.decodeByteArray(response, 0, response.length);
        }
        Log.i(LOGTAG, "Failed to download images from " + Arrays.toString(urls));
        return null;
    }

    @SuppressLint({"NewApi"})
    private static int getDisplayWidth(Display display) {
        if (VERSION.SDK_INT < 13) {
            return display.getWidth();
        }
        Point displaySize = new Point();
        display.getSize(displaySize);
        return displaySize.x;
    }

    private static byte[] getUrls(RemoteService poster, Context context, String[] urls) throws ServiceUnavailableException {
        int i;
        byte[] bArr = null;
        if (poster.isOnline(context)) {
            bArr = null;
            int length = urls.length;
            i = 0;
            while (i < length) {
                String url = urls[i];
                try {
                    bArr = poster.performRequest(url, null, MPConfig.getInstance(context).getSSLSocketFactory());
                    break;
                } catch (MalformedURLException e) {
                    Log.e(LOGTAG, "Cannot interpret " + url + " as a URL.", e);
                } catch (FileNotFoundException e2) {
                    if (MPConfig.DEBUG) {
                        Log.v(LOGTAG, "Cannot get " + url + ", file not found.", e2);
                    }
                } catch (IOException e3) {
                    if (MPConfig.DEBUG) {
                        Log.v(LOGTAG, "Cannot get " + url + ".", e3);
                    }
                } catch (OutOfMemoryError e4) {
                    Log.e(LOGTAG, "Out of memory when getting to " + url + ".", e4);
                }
            }
        }
        return bArr;
        i++;
    }
}
