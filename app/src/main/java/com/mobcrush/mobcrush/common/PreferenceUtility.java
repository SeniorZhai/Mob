package com.mobcrush.mobcrush.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.datamodel.Config;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.BuildConfig;

public class PreferenceUtility {
    private static final String ACCESS_TOKEN = "access_token";
    private static final String APP_VERSION = "appVersion";
    private static final String CONFIG = "config";
    private static final String CONFIG_TIMESTAMP = "config_timestamp";
    private static final String DATE_EXPIRATION = "date_expiration";
    private static final String EMAIL_VERIFIED = "email_verified";
    private static final String FIREBASE_TOKEN = "firebase_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    public static final String REG_ID = "registration_id";
    private static final String STREAM_KEY = "stream_key";
    private static final String TAG = "PreferenceUtility";
    private static final String USER = "user";
    private static final String WHATS_NEW_VERSION = "whats_new_version";
    private static Config mConfig;
    public static final Object mLocker = new Object();
    private static SharedPreferences mPrefs;
    private static User mUser;

    public static SharedPreferences getPref() {
        if (mPrefs == null) {
            mPrefs = PreferenceManager.getDefaultSharedPreferences(MainApplication.getContext());
        }
        return mPrefs;
    }

    public static String getAccessToken() {
        return get(ACCESS_TOKEN);
    }

    public static void setAccessToken(String accessToken) {
        save(ACCESS_TOKEN, accessToken);
    }

    public static void removeAccessToken() {
        remove(ACCESS_TOKEN);
    }

    public static String getRefreshToken() {
        return get(REFRESH_TOKEN);
    }

    public static void setRefreshToken(String refreshToken) {
        save(REFRESH_TOKEN, refreshToken);
    }

    public static void removeRefreshToken() {
        remove(REFRESH_TOKEN);
    }

    public static void setStreamKey(String streamKey) {
        save(STREAM_KEY, streamKey);
    }

    public static String getStreamKey() {
        return get(STREAM_KEY);
    }

    public static void removeStreamKey() {
        remove(STREAM_KEY);
    }

    public static boolean isTokenExpired() {
        String date = get(DATE_EXPIRATION);
        if (TextUtils.isEmpty(date)) {
            return true;
        }
        try {
            if (System.currentTimeMillis() - Long.parseLong(date) <= 1000) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    public static void setFirebaseToken(String token) {
        Log.d("!!!", "token: " + token);
        save(FIREBASE_TOKEN, token);
    }

    public static String getFirebaseToken() {
        Log.d("!!!", "token: " + get(FIREBASE_TOKEN));
        return get(FIREBASE_TOKEN);
    }

    public static void removeFirebaseToken() {
        remove(FIREBASE_TOKEN);
    }

    public static long getExpirationDate() {
        try {
            return Long.parseLong(get(DATE_EXPIRATION));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void setExpirationDate(long expires_in) {
        save(DATE_EXPIRATION, String.valueOf(System.currentTimeMillis() + (1000 * expires_in)));
    }

    public static void removeExprirationDate() {
        remove(DATE_EXPIRATION);
    }

    public static Config getConfig() {
        if (mConfig == null && contains(CONFIG)) {
            String config = get(CONFIG);
            if (TextUtils.isEmpty(config)) {
                Network.updateConfig(null, null, null);
            } else {
                try {
                    mConfig = (Config) new Gson().fromJson(config, Config.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(new Exception("Config will be removed: " + config, e));
                    removeConfig();
                    mConfig = null;
                    Network.updateConfig(null, null, null);
                }
            }
        }
        if (mConfig == null) {
            try {
                return (Config) new Gson().fromJson(Utils.getStringFromRawResource(MainApplication.getContext(), R.raw.config), Config.class);
            } catch (Exception e2) {
            }
        }
        return mConfig;
    }

    public static void setConfig(String config) {
        save(CONFIG_TIMESTAMP, String.valueOf(System.currentTimeMillis()));
        save(CONFIG, config);
        if (mConfig == null) {
            Constants.MIXPANEL_TOKEN = getConfig().mixpanelToken;
            MixpanelHelper.getInstance(MainApplication.getContext()).generateAppInstallEvent();
            return;
        }
        mConfig = null;
    }

    public static long getConfigTimestamp() {
        long j = 0;
        String s = get(CONFIG_TIMESTAMP);
        if (!TextUtils.isEmpty(s)) {
            try {
                j = Long.parseLong(s);
            } catch (NumberFormatException e) {
            }
        }
        return j;
    }

    public static void removeConfig() {
        remove(CONFIG);
    }

    public static String getShownWhatsNewVersion() {
        return get(WHATS_NEW_VERSION);
    }

    public static void updateWhatsNewVersionToCurrent() {
        save(WHATS_NEW_VERSION, Constants.APP_VERSION_NAME);
    }

    public static User getUser() {
        if (mUser == null && contains(USER)) {
            String user = get(USER);
            if (!TextUtils.isEmpty(user)) {
                try {
                    mUser = (User) new Gson().fromJson(user, User.class);
                } catch (Exception e) {
                    removeUser();
                    mUser = null;
                }
            }
        }
        if (mUser == null) {
            mUser = new User();
            mUser.username = MainApplication.getRString(R.string.guest, new Object[0]);
        }
        return mUser;
    }

    public static void setUser(String user) {
        try {
            mUser = (User) new Gson().fromJson(user, User.class);
        } catch (Exception e) {
            mUser = null;
        }
        save(USER, user);
        Utils.setUserToCrashlytics();
    }

    public static void removeUser() {
        mUser = null;
        remove(USER);
    }

    public static void setEmailVerified(String data) {
        save(EMAIL_VERIFIED, data);
    }

    public static boolean isEmailVerified() {
        return get(EMAIL_VERIFIED) != null;
    }

    public static void removeEmailVerified() {
        remove(EMAIL_VERIFIED);
    }

    public static void removeAllPreferencies() {
        getPref().edit().clear().apply();
    }

    public static void storeRegistrationId(Context context, String regId) {
        int appVersion = Utils.getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        save(REG_ID, regId);
        save(APP_VERSION, String.valueOf(appVersion));
    }

    public static String getRegistrationId(Context context) {
        String registrationId = get(REG_ID);
        if (registrationId == null || registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return BuildConfig.FLAVOR;
        }
        if (String.valueOf(Utils.getAppVersion(context)).equals(get(APP_VERSION))) {
            return registrationId;
        }
        Log.i(TAG, "App version changed.");
        return BuildConfig.FLAVOR;
    }

    public static void save(String name, String value) {
        Editor edit = getPref().edit();
        edit.putString(name, value);
        edit.apply();
    }

    public static void remove(String name) {
        Editor edit = getPref().edit();
        edit.remove(name);
        edit.apply();
    }

    public static String get(String name) {
        return getPref().getString(name, null);
    }

    public static boolean contains(String name) {
        return getPref().contains(name);
    }
}
