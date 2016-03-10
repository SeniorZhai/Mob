package com.facebook.internal;

import android.os.Bundle;
import com.facebook.FacebookSdk;
import com.facebook.LoggingBehavior;
import java.util.Collection;
import org.json.JSONException;
import org.json.JSONObject;

public final class ServerProtocol {
    private static final String DIALOG_AUTHORITY_FORMAT = "m.%s";
    public static final String DIALOG_CANCEL_URI = "fbconnect://cancel";
    public static final String DIALOG_PARAM_ACCESS_TOKEN = "access_token";
    public static final String DIALOG_PARAM_APP_ID = "app_id";
    public static final String DIALOG_PARAM_AUTH_TYPE = "auth_type";
    public static final String DIALOG_PARAM_CLIENT_ID = "client_id";
    public static final String DIALOG_PARAM_DEFAULT_AUDIENCE = "default_audience";
    public static final String DIALOG_PARAM_DISPLAY = "display";
    public static final String DIALOG_PARAM_DISPLAY_TOUCH = "touch";
    public static final String DIALOG_PARAM_E2E = "e2e";
    public static final String DIALOG_PARAM_LEGACY_OVERRIDE = "legacy_override";
    public static final String DIALOG_PARAM_REDIRECT_URI = "redirect_uri";
    public static final String DIALOG_PARAM_RESPONSE_TYPE = "response_type";
    public static final String DIALOG_PARAM_RETURN_SCOPES = "return_scopes";
    public static final String DIALOG_PARAM_SCOPE = "scope";
    public static final String DIALOG_PATH = "dialog/";
    public static final String DIALOG_REDIRECT_URI = "fbconnect://success";
    public static final String DIALOG_REREQUEST_AUTH_TYPE = "rerequest";
    public static final String DIALOG_RESPONSE_TYPE_TOKEN_AND_SIGNED_REQUEST = "token,signed_request";
    public static final String DIALOG_RETURN_SCOPES_TRUE = "true";
    public static final String FALLBACK_DIALOG_DISPLAY_VALUE_TOUCH = "touch";
    public static final String FALLBACK_DIALOG_PARAM_APP_ID = "app_id";
    public static final String FALLBACK_DIALOG_PARAM_BRIDGE_ARGS = "bridge_args";
    public static final String FALLBACK_DIALOG_PARAM_KEY_HASH = "android_key_hash";
    public static final String FALLBACK_DIALOG_PARAM_METHOD_ARGS = "method_args";
    public static final String FALLBACK_DIALOG_PARAM_METHOD_RESULTS = "method_results";
    public static final String FALLBACK_DIALOG_PARAM_VERSION = "version";
    public static final String GRAPH_API_VERSION = "v2.4";
    private static final String GRAPH_URL_FORMAT = "https://graph.%s";
    private static final String GRAPH_VIDEO_URL_FORMAT = "https://graph-video.%s";
    private static final String TAG = ServerProtocol.class.getName();
    public static final String errorConnectionFailure = "CONNECTION_FAILURE";
    public static final Collection<String> errorsProxyAuthDisabled = Utility.unmodifiableCollection("service_disabled", "AndroidAuthKillSwitchException");
    public static final Collection<String> errorsUserCanceled = Utility.unmodifiableCollection("access_denied", "OAuthAccessDeniedException");

    public static final String getDialogAuthority() {
        return String.format(DIALOG_AUTHORITY_FORMAT, new Object[]{FacebookSdk.getFacebookDomain()});
    }

    public static final String getGraphUrlBase() {
        return String.format(GRAPH_URL_FORMAT, new Object[]{FacebookSdk.getFacebookDomain()});
    }

    public static final String getGraphVideoUrlBase() {
        return String.format(GRAPH_VIDEO_URL_FORMAT, new Object[]{FacebookSdk.getFacebookDomain()});
    }

    public static final String getAPIVersion() {
        return GRAPH_API_VERSION;
    }

    public static Bundle getQueryParamsForPlatformActivityIntentWebFallback(String callId, int version, Bundle methodArgs) {
        String keyHash = FacebookSdk.getApplicationSignature(FacebookSdk.getApplicationContext());
        if (Utility.isNullOrEmpty(keyHash)) {
            return null;
        }
        Bundle webParams = new Bundle();
        webParams.putString(FALLBACK_DIALOG_PARAM_KEY_HASH, keyHash);
        webParams.putString(FALLBACK_DIALOG_PARAM_APP_ID, FacebookSdk.getApplicationId());
        webParams.putInt(FALLBACK_DIALOG_PARAM_VERSION, version);
        webParams.putString(DIALOG_PARAM_DISPLAY, FALLBACK_DIALOG_DISPLAY_VALUE_TOUCH);
        Bundle bridgeArguments = new Bundle();
        bridgeArguments.putString(NativeProtocol.BRIDGE_ARG_ACTION_ID_STRING, callId);
        if (methodArgs == null) {
            methodArgs = new Bundle();
        }
        try {
            JSONObject bridgeArgsJSON = BundleJSONConverter.convertToJSON(bridgeArguments);
            JSONObject methodArgsJSON = BundleJSONConverter.convertToJSON(methodArgs);
            if (bridgeArgsJSON == null || methodArgsJSON == null) {
                return null;
            }
            webParams.putString(FALLBACK_DIALOG_PARAM_BRIDGE_ARGS, bridgeArgsJSON.toString());
            webParams.putString(FALLBACK_DIALOG_PARAM_METHOD_ARGS, methodArgsJSON.toString());
            return webParams;
        } catch (JSONException je) {
            Logger.log(LoggingBehavior.DEVELOPER_ERRORS, 6, TAG, "Error creating Url -- " + je);
            return null;
        }
    }
}
