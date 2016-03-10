package com.facebook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.facebook.GraphRequest.Callback;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import com.mobcrush.mobcrush.ChatFragment;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.json.JSONObject;

final class AccessTokenManager {
    static final String ACTION_CURRENT_ACCESS_TOKEN_CHANGED = "com.facebook.sdk.ACTION_CURRENT_ACCESS_TOKEN_CHANGED";
    static final String EXTRA_NEW_ACCESS_TOKEN = "com.facebook.sdk.EXTRA_NEW_ACCESS_TOKEN";
    static final String EXTRA_OLD_ACCESS_TOKEN = "com.facebook.sdk.EXTRA_OLD_ACCESS_TOKEN";
    private static final String ME_PERMISSIONS_GRAPH_PATH = "me/permissions";
    static final String SHARED_PREFERENCES_NAME = "com.facebook.AccessTokenManager.SharedPreferences";
    static final String TAG = "AccessTokenManager";
    private static final String TOKEN_EXTEND_GRAPH_PATH = "oauth/access_token";
    private static final int TOKEN_EXTEND_RETRY_SECONDS = 3600;
    private static final int TOKEN_EXTEND_THRESHOLD_SECONDS = 86400;
    private static volatile AccessTokenManager instance;
    private final AccessTokenCache accessTokenCache;
    private AccessToken currentAccessToken;
    private Date lastAttemptedTokenExtendDate = new Date(0);
    private final LocalBroadcastManager localBroadcastManager;
    private AtomicBoolean tokenRefreshInProgress = new AtomicBoolean(false);

    private static class RefreshResult {
        public String accessToken;
        public int expiresAt;

        private RefreshResult() {
        }
    }

    AccessTokenManager(LocalBroadcastManager localBroadcastManager, AccessTokenCache accessTokenCache) {
        Validate.notNull(localBroadcastManager, "localBroadcastManager");
        Validate.notNull(accessTokenCache, "accessTokenCache");
        this.localBroadcastManager = localBroadcastManager;
        this.accessTokenCache = accessTokenCache;
    }

    static AccessTokenManager getInstance() {
        if (instance == null) {
            synchronized (AccessTokenManager.class) {
                if (instance == null) {
                    instance = new AccessTokenManager(LocalBroadcastManager.getInstance(FacebookSdk.getApplicationContext()), new AccessTokenCache());
                }
            }
        }
        return instance;
    }

    AccessToken getCurrentAccessToken() {
        return this.currentAccessToken;
    }

    boolean loadCurrentAccessToken() {
        AccessToken accessToken = this.accessTokenCache.load();
        if (accessToken == null) {
            return false;
        }
        setCurrentAccessToken(accessToken, false);
        return true;
    }

    void setCurrentAccessToken(AccessToken currentAccessToken) {
        setCurrentAccessToken(currentAccessToken, true);
    }

    private void setCurrentAccessToken(AccessToken currentAccessToken, boolean saveToCache) {
        AccessToken oldAccessToken = this.currentAccessToken;
        this.currentAccessToken = currentAccessToken;
        this.tokenRefreshInProgress.set(false);
        this.lastAttemptedTokenExtendDate = new Date(0);
        if (saveToCache) {
            if (currentAccessToken != null) {
                this.accessTokenCache.save(currentAccessToken);
            } else {
                this.accessTokenCache.clear();
                Utility.clearFacebookCookies(FacebookSdk.getApplicationContext());
            }
        }
        if (!Utility.areObjectsEqual(oldAccessToken, currentAccessToken)) {
            sendCurrentAccessTokenChangedBroadcast(oldAccessToken, currentAccessToken);
        }
    }

    private void sendCurrentAccessTokenChangedBroadcast(AccessToken oldAccessToken, AccessToken currentAccessToken) {
        Intent intent = new Intent(ACTION_CURRENT_ACCESS_TOKEN_CHANGED);
        intent.putExtra(EXTRA_OLD_ACCESS_TOKEN, oldAccessToken);
        intent.putExtra(EXTRA_NEW_ACCESS_TOKEN, currentAccessToken);
        this.localBroadcastManager.sendBroadcast(intent);
    }

    void extendAccessTokenIfNeeded() {
        if (shouldExtendAccessToken()) {
            refreshCurrentAccessToken();
        }
    }

    private boolean shouldExtendAccessToken() {
        if (this.currentAccessToken == null) {
            return false;
        }
        Long now = Long.valueOf(new Date().getTime());
        if (!this.currentAccessToken.getSource().canExtendToken() || now.longValue() - this.lastAttemptedTokenExtendDate.getTime() <= 3600000 || now.longValue() - this.currentAccessToken.getLastRefresh().getTime() <= ChatFragment.MID_MUTE_TIME) {
            return false;
        }
        return true;
    }

    private static GraphRequest createGrantedPermissionsRequest(AccessToken accessToken, Callback callback) {
        return new GraphRequest(accessToken, ME_PERMISSIONS_GRAPH_PATH, new Bundle(), HttpMethod.GET, callback);
    }

    private static GraphRequest createExtendAccessTokenRequest(AccessToken accessToken, Callback callback) {
        Bundle parameters = new Bundle();
        parameters.putString("grant_type", "fb_extend_sso_token");
        return new GraphRequest(accessToken, TOKEN_EXTEND_GRAPH_PATH, parameters, HttpMethod.GET, callback);
    }

    void refreshCurrentAccessToken() {
        if (Looper.getMainLooper().equals(Looper.myLooper())) {
            refreshCurrentAccessTokenImpl();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                public void run() {
                    AccessTokenManager.this.refreshCurrentAccessTokenImpl();
                }
            });
        }
    }

    private void refreshCurrentAccessTokenImpl() {
        final AccessToken accessToken = this.currentAccessToken;
        if (accessToken != null && this.tokenRefreshInProgress.compareAndSet(false, true)) {
            Validate.runningOnUiThread();
            this.lastAttemptedTokenExtendDate = new Date();
            final Set<String> permissions = new HashSet();
            final Set<String> declinedPermissions = new HashSet();
            final AtomicBoolean permissionsCallSucceeded = new AtomicBoolean(false);
            final RefreshResult refreshResult = new RefreshResult();
            GraphRequestBatch batch = new GraphRequestBatch(createGrantedPermissionsRequest(accessToken, new Callback() {
                public void onCompleted(GraphResponse response) {
                    JSONObject result = response.getJSONObject();
                    if (result != null) {
                        JSONArray permissionsArray = result.optJSONArray(MPDbAdapter.KEY_DATA);
                        if (permissionsArray != null) {
                            permissionsCallSucceeded.set(true);
                            for (int i = 0; i < permissionsArray.length(); i++) {
                                JSONObject permissionEntry = permissionsArray.optJSONObject(i);
                                if (permissionEntry != null) {
                                    String permission = permissionEntry.optString("permission");
                                    String status = permissionEntry.optString(SettingsJsonConstants.APP_STATUS_KEY);
                                    if (!(Utility.isNullOrEmpty(permission) || Utility.isNullOrEmpty(status))) {
                                        status = status.toLowerCase(Locale.US);
                                        if (status.equals("granted")) {
                                            permissions.add(permission);
                                        } else if (status.equals("declined")) {
                                            declinedPermissions.add(permission);
                                        } else {
                                            Log.w(AccessTokenManager.TAG, "Unexpected status: " + status);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }), createExtendAccessTokenRequest(accessToken, new Callback() {
                public void onCompleted(GraphResponse response) {
                    JSONObject data = response.getJSONObject();
                    if (data != null) {
                        refreshResult.accessToken = data.optString(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN);
                        refreshResult.expiresAt = data.optInt(SettingsJsonConstants.EXPIRES_AT_KEY);
                    }
                }
            }));
            batch.addCallback(new GraphRequestBatch.Callback() {
                public void onBatchCompleted(GraphRequestBatch batch) {
                    if (AccessTokenManager.getInstance().getCurrentAccessToken() != null && AccessTokenManager.getInstance().getCurrentAccessToken().getUserId() == accessToken.getUserId()) {
                        try {
                            if (permissionsCallSucceeded.get() || refreshResult.accessToken != null || refreshResult.expiresAt != 0) {
                                String str;
                                Collection collection;
                                Collection collection2;
                                Date date;
                                if (refreshResult.accessToken != null) {
                                    str = refreshResult.accessToken;
                                } else {
                                    str = accessToken.getToken();
                                }
                                String applicationId = accessToken.getApplicationId();
                                String userId = accessToken.getUserId();
                                if (permissionsCallSucceeded.get()) {
                                    collection = permissions;
                                } else {
                                    collection = accessToken.getPermissions();
                                }
                                if (permissionsCallSucceeded.get()) {
                                    collection2 = declinedPermissions;
                                } else {
                                    collection2 = accessToken.getDeclinedPermissions();
                                }
                                AccessTokenSource source = accessToken.getSource();
                                if (refreshResult.expiresAt != 0) {
                                    date = new Date(((long) refreshResult.expiresAt) * 1000);
                                } else {
                                    date = accessToken.getExpires();
                                }
                                AccessTokenManager.getInstance().setCurrentAccessToken(new AccessToken(str, applicationId, userId, collection, collection2, source, date, new Date()));
                                AccessTokenManager.this.tokenRefreshInProgress.set(false);
                            }
                        } finally {
                            AccessTokenManager.this.tokenRefreshInProgress.set(false);
                        }
                    }
                }
            });
            batch.executeAsync();
        }
    }
}
