package com.facebook.login;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginClient.Request;
import io.fabric.sdk.android.BuildConfig;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;

class LoginLogger {
    static final String EVENT_EXTRAS_DEFAULT_AUDIENCE = "default_audience";
    static final String EVENT_EXTRAS_IS_REAUTHORIZE = "isReauthorize";
    static final String EVENT_EXTRAS_LOGIN_BEHAVIOR = "login_behavior";
    static final String EVENT_EXTRAS_MISSING_INTERNET_PERMISSION = "no_internet_permission";
    static final String EVENT_EXTRAS_NEW_PERMISSIONS = "new_permissions";
    static final String EVENT_EXTRAS_NOT_TRIED = "not_tried";
    static final String EVENT_EXTRAS_PERMISSIONS = "permissions";
    static final String EVENT_EXTRAS_REQUEST_CODE = "request_code";
    static final String EVENT_EXTRAS_TRY_LOGIN_ACTIVITY = "try_login_activity";
    static final String EVENT_NAME_LOGIN_COMPLETE = "fb_mobile_login_complete";
    static final String EVENT_NAME_LOGIN_METHOD_COMPLETE = "fb_mobile_login_method_complete";
    static final String EVENT_NAME_LOGIN_METHOD_START = "fb_mobile_login_method_start";
    static final String EVENT_NAME_LOGIN_START = "fb_mobile_login_start";
    static final String EVENT_PARAM_AUTH_LOGGER_ID = "0_auth_logger_id";
    static final String EVENT_PARAM_ERROR_CODE = "4_error_code";
    static final String EVENT_PARAM_ERROR_MESSAGE = "5_error_message";
    static final String EVENT_PARAM_EXTRAS = "6_extras";
    static final String EVENT_PARAM_LOGIN_RESULT = "2_result";
    static final String EVENT_PARAM_METHOD = "3_method";
    static final String EVENT_PARAM_METHOD_RESULT_SKIPPED = "skipped";
    static final String EVENT_PARAM_TIMESTAMP = "1_timestamp_ms";
    private final AppEventsLogger appEventsLogger;
    private String applicationId;

    LoginLogger(Context context, String applicationId) {
        this.applicationId = applicationId;
        this.appEventsLogger = AppEventsLogger.newLogger(context, applicationId);
    }

    public String getApplicationId() {
        return this.applicationId;
    }

    static Bundle newAuthorizationLoggingBundle(String authLoggerId) {
        Bundle bundle = new Bundle();
        bundle.putLong(EVENT_PARAM_TIMESTAMP, System.currentTimeMillis());
        bundle.putString(EVENT_PARAM_AUTH_LOGGER_ID, authLoggerId);
        bundle.putString(EVENT_PARAM_METHOD, BuildConfig.FLAVOR);
        bundle.putString(EVENT_PARAM_LOGIN_RESULT, BuildConfig.FLAVOR);
        bundle.putString(EVENT_PARAM_ERROR_MESSAGE, BuildConfig.FLAVOR);
        bundle.putString(EVENT_PARAM_ERROR_CODE, BuildConfig.FLAVOR);
        bundle.putString(EVENT_PARAM_EXTRAS, BuildConfig.FLAVOR);
        return bundle;
    }

    public void logStartLogin(Request pendingLoginRequest) {
        Bundle bundle = newAuthorizationLoggingBundle(pendingLoginRequest.getAuthId());
        try {
            JSONObject extras = new JSONObject();
            extras.put(EVENT_EXTRAS_LOGIN_BEHAVIOR, pendingLoginRequest.getLoginBehavior().toString());
            extras.put(EVENT_EXTRAS_REQUEST_CODE, LoginClient.getLoginRequestCode());
            extras.put(EVENT_EXTRAS_PERMISSIONS, TextUtils.join(",", pendingLoginRequest.getPermissions()));
            extras.put(EVENT_EXTRAS_DEFAULT_AUDIENCE, pendingLoginRequest.getDefaultAudience().toString());
            extras.put(EVENT_EXTRAS_IS_REAUTHORIZE, pendingLoginRequest.isRerequest());
            bundle.putString(EVENT_PARAM_EXTRAS, extras.toString());
        } catch (JSONException e) {
        }
        this.appEventsLogger.logSdkEvent(EVENT_NAME_LOGIN_START, null, bundle);
    }

    public void logCompleteLogin(String loginRequestId, Map<String, String> loggingExtras, Code result, Map<String, String> resultExtras, Exception exception) {
        Bundle bundle = newAuthorizationLoggingBundle(loginRequestId);
        if (result != null) {
            bundle.putString(EVENT_PARAM_LOGIN_RESULT, result.getLoggingValue());
        }
        if (!(exception == null || exception.getMessage() == null)) {
            bundle.putString(EVENT_PARAM_ERROR_MESSAGE, exception.getMessage());
        }
        JSONObject jSONObject = null;
        if (!loggingExtras.isEmpty()) {
            jSONObject = new JSONObject(loggingExtras);
        }
        if (resultExtras != null) {
            if (jSONObject == null) {
                jSONObject = new JSONObject();
            }
            try {
                for (Entry<String, String> entry : resultExtras.entrySet()) {
                    jSONObject.put((String) entry.getKey(), entry.getValue());
                }
            } catch (JSONException e) {
            }
        }
        if (jSONObject != null) {
            bundle.putString(EVENT_PARAM_EXTRAS, jSONObject.toString());
        }
        this.appEventsLogger.logSdkEvent(EVENT_NAME_LOGIN_COMPLETE, null, bundle);
    }

    public void logAuthorizationMethodStart(String authId, String method) {
        Bundle bundle = newAuthorizationLoggingBundle(authId);
        bundle.putString(EVENT_PARAM_METHOD, method);
        this.appEventsLogger.logSdkEvent(EVENT_NAME_LOGIN_METHOD_START, null, bundle);
    }

    public void logAuthorizationMethodComplete(String authId, String method, String result, String errorMessage, String errorCode, Map<String, String> loggingExtras) {
        Bundle bundle = newAuthorizationLoggingBundle(authId);
        if (result != null) {
            bundle.putString(EVENT_PARAM_LOGIN_RESULT, result);
        }
        if (errorMessage != null) {
            bundle.putString(EVENT_PARAM_ERROR_MESSAGE, errorMessage);
        }
        if (errorCode != null) {
            bundle.putString(EVENT_PARAM_ERROR_CODE, errorCode);
        }
        if (!(loggingExtras == null || loggingExtras.isEmpty())) {
            bundle.putString(EVENT_PARAM_EXTRAS, new JSONObject(loggingExtras).toString());
        }
        bundle.putString(EVENT_PARAM_METHOD, method);
        this.appEventsLogger.logSdkEvent(EVENT_NAME_LOGIN_METHOD_COMPLETE, null, bundle);
    }

    public void logUnexpectedError(String eventName, String errorMessage) {
        logUnexpectedError(eventName, errorMessage, BuildConfig.FLAVOR);
    }

    public void logUnexpectedError(String eventName, String errorMessage, String method) {
        Bundle bundle = newAuthorizationLoggingBundle(BuildConfig.FLAVOR);
        bundle.putString(EVENT_PARAM_LOGIN_RESULT, Code.ERROR.getLoggingValue());
        bundle.putString(EVENT_PARAM_ERROR_MESSAGE, errorMessage);
        bundle.putString(EVENT_PARAM_METHOD, method);
        this.appEventsLogger.logSdkEvent(eventName, null, bundle);
    }
}
