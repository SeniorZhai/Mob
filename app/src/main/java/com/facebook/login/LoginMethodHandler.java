package com.facebook.login;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import com.facebook.AccessToken;
import com.facebook.AccessTokenSource;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.AnalyticsEvents;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.Utility;
import com.facebook.login.LoginClient.Request;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

abstract class LoginMethodHandler implements Parcelable {
    protected LoginClient loginClient;
    Map<String, String> methodLoggingExtras;

    abstract String getNameForLogging();

    abstract boolean tryAuthorize(Request request);

    LoginMethodHandler(LoginClient loginClient) {
        this.loginClient = loginClient;
    }

    LoginMethodHandler(Parcel source) {
        this.methodLoggingExtras = Utility.readStringMapFromParcel(source);
    }

    void setLoginClient(LoginClient loginClient) {
        if (this.loginClient != null) {
            throw new FacebookException("Can't set LoginClient if it is already set.");
        }
        this.loginClient = loginClient;
    }

    boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    boolean needsInternetPermission() {
        return false;
    }

    void cancel() {
    }

    protected void addLoggingExtra(String key, Object value) {
        if (this.methodLoggingExtras == null) {
            this.methodLoggingExtras = new HashMap();
        }
        this.methodLoggingExtras.put(key, value == null ? null : value.toString());
    }

    protected void logWebLoginCompleted(String e2e) {
        String applicationId = this.loginClient.getPendingRequest().getApplicationId();
        AppEventsLogger appEventsLogger = AppEventsLogger.newLogger(this.loginClient.getActivity(), applicationId);
        Bundle parameters = new Bundle();
        parameters.putString(AnalyticsEvents.PARAMETER_WEB_LOGIN_E2E, e2e);
        parameters.putLong(AnalyticsEvents.PARAMETER_WEB_LOGIN_SWITCHBACK_TIME, System.currentTimeMillis());
        parameters.putString(ServerProtocol.FALLBACK_DIALOG_PARAM_APP_ID, applicationId);
        appEventsLogger.logSdkEvent(AnalyticsEvents.EVENT_WEB_LOGIN_COMPLETE, null, parameters);
    }

    static AccessToken createAccessTokenFromNativeLogin(Bundle bundle, AccessTokenSource source, String applicationId) {
        Date expires = Utility.getBundleLongAsDate(bundle, NativeProtocol.EXTRA_EXPIRES_SECONDS_SINCE_EPOCH, new Date(0));
        ArrayList<String> permissions = bundle.getStringArrayList(NativeProtocol.EXTRA_PERMISSIONS);
        String token = bundle.getString(NativeProtocol.EXTRA_ACCESS_TOKEN);
        if (Utility.isNullOrEmpty(token)) {
            return null;
        }
        return new AccessToken(token, applicationId, bundle.getString(NativeProtocol.EXTRA_USER_ID), permissions, null, source, expires, new Date());
    }

    public static AccessToken createAccessTokenFromWebBundle(Collection<String> requestedPermissions, Bundle bundle, AccessTokenSource source, String applicationId) throws FacebookException {
        Date expires = Utility.getBundleLongAsDate(bundle, AccessToken.EXPIRES_IN_KEY, new Date());
        String token = bundle.getString(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN);
        String grantedPermissions = bundle.getString("granted_scopes");
        if (!Utility.isNullOrEmpty(grantedPermissions)) {
            requestedPermissions = new ArrayList(Arrays.asList(grantedPermissions.split(",")));
        }
        String deniedPermissions = bundle.getString("denied_scopes");
        List<String> declinedPermissions = null;
        if (!Utility.isNullOrEmpty(deniedPermissions)) {
            declinedPermissions = new ArrayList(Arrays.asList(deniedPermissions.split(",")));
        }
        if (Utility.isNullOrEmpty(token)) {
            return null;
        }
        return new AccessToken(token, applicationId, getUserIDFromSignedRequest(bundle.getString("signed_request")), requestedPermissions, declinedPermissions, source, expires, new Date());
    }

    private static String getUserIDFromSignedRequest(String signedRequest) throws FacebookException {
        if (signedRequest == null || signedRequest.isEmpty()) {
            throw new FacebookException("Authorization response does not contain the signed_request");
        }
        try {
            String[] signatureAndPayload = signedRequest.split("\\.");
            if (signatureAndPayload.length == 2) {
                return new JSONObject(new String(Base64.decode(signatureAndPayload[1], 0), HTTP.UTF_8)).getString(AccessToken.USER_ID_KEY);
            }
        } catch (UnsupportedEncodingException e) {
        } catch (JSONException e2) {
        }
        throw new FacebookException("Failed to retrieve user_id from signed_request");
    }

    public void writeToParcel(Parcel dest, int flags) {
        Utility.writeStringMapToParcel(dest, this.methodLoggingExtras);
    }
}
