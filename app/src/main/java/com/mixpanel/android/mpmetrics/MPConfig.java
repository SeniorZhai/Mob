package com.mixpanel.android.mpmetrics;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import com.mobcrush.mobcrush.Constants;
import java.security.GeneralSecurityException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

public class MPConfig {
    public static boolean DEBUG = false;
    private static final String LOGTAG = "MixpanelAPI.Conf";
    static final int MAX_NOTIFICATION_CACHE_COUNT = 2;
    static final String REFERRER_PREFS_NAME = "com.mixpanel.android.mpmetrics.ReferralInfo";
    public static final int UI_FEATURES_MIN_API = 16;
    public static final String VERSION = "4.6.2";
    private static MPConfig sInstance;
    private static final Object sInstanceLock = new Object();
    private final boolean mAutoShowMixpanelUpdates;
    private final int mBulkUploadLimit;
    private final int mDataExpiration;
    private final int mDebugFlushInterval;
    private final String mDecideEndpoint;
    private final String mDecideFallbackEndpoint;
    private final boolean mDisableAppOpenEvent;
    private final boolean mDisableEmulatorBindingUI;
    private final boolean mDisableFallback;
    private final boolean mDisableGestureBindingUI;
    private final String mEditorUrl;
    private final String mEventsEndpoint;
    private final String mEventsFallbackEndpoint;
    private final int mFlushInterval;
    private final int mMinimumDatabaseLimit;
    private final String mPeopleEndpoint;
    private final String mPeopleFallbackEndpoint;
    private final String mResourcePackageName;
    private SSLSocketFactory mSSLSocketFactory;
    private final boolean mTestMode;

    public static MPConfig getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = readConfig(context.getApplicationContext());
            }
        }
        return sInstance;
    }

    public synchronized void setSSLSocketFactory(SSLSocketFactory factory) {
        this.mSSLSocketFactory = factory;
    }

    MPConfig(Bundle metaData, Context context) {
        SSLSocketFactory foundSSLFactory;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            foundSSLFactory = sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            Log.i(LOGTAG, "System has no SSL support. Built-in events editor will not be available", e);
            foundSSLFactory = null;
        }
        this.mSSLSocketFactory = foundSSLFactory;
        DEBUG = metaData.getBoolean("com.mixpanel.android.MPConfig.EnableDebugLogging", false);
        if (metaData.containsKey("com.mixpanel.android.MPConfig.AutoCheckForSurveys")) {
            Log.w(LOGTAG, "com.mixpanel.android.MPConfig.AutoCheckForSurveys has been deprecated in favor of com.mixpanel.android.MPConfig.AutoShowMixpanelUpdates. Please update this key as soon as possible.");
        }
        this.mBulkUploadLimit = metaData.getInt("com.mixpanel.android.MPConfig.BulkUploadLimit", 40);
        this.mFlushInterval = metaData.getInt("com.mixpanel.android.MPConfig.FlushInterval", 60000);
        this.mDebugFlushInterval = metaData.getInt("com.mixpanel.android.MPConfig.DebugFlushInterval", Constants.UPDATE_COFIG_INTERVAL);
        this.mDataExpiration = metaData.getInt("com.mixpanel.android.MPConfig.DataExpiration", 432000000);
        this.mMinimumDatabaseLimit = metaData.getInt("com.mixpanel.android.MPConfig.MinimumDatabaseLimit", 20971520);
        this.mDisableFallback = metaData.getBoolean("com.mixpanel.android.MPConfig.DisableFallback", true);
        this.mResourcePackageName = metaData.getString("com.mixpanel.android.MPConfig.ResourcePackageName");
        this.mDisableGestureBindingUI = metaData.getBoolean("com.mixpanel.android.MPConfig.DisableGestureBindingUI", false);
        this.mDisableEmulatorBindingUI = metaData.getBoolean("com.mixpanel.android.MPConfig.DisableEmulatorBindingUI", false);
        this.mDisableAppOpenEvent = metaData.getBoolean("com.mixpanel.android.MPConfig.DisableAppOpenEvent", true);
        boolean z = metaData.getBoolean("com.mixpanel.android.MPConfig.AutoCheckForSurveys", true) && metaData.getBoolean("com.mixpanel.android.MPConfig.AutoShowMixpanelUpdates", true);
        this.mAutoShowMixpanelUpdates = z;
        this.mTestMode = metaData.getBoolean("com.mixpanel.android.MPConfig.TestMode", false);
        String eventsEndpoint = metaData.getString("com.mixpanel.android.MPConfig.EventsEndpoint");
        if (eventsEndpoint == null) {
            eventsEndpoint = "https://api.mixpanel.com/track?ip=1";
        }
        this.mEventsEndpoint = eventsEndpoint;
        String eventsFallbackEndpoint = metaData.getString("com.mixpanel.android.MPConfig.EventsFallbackEndpoint");
        if (eventsFallbackEndpoint == null) {
            eventsFallbackEndpoint = "http://api.mixpanel.com/track?ip=1";
        }
        this.mEventsFallbackEndpoint = eventsFallbackEndpoint;
        String peopleEndpoint = metaData.getString("com.mixpanel.android.MPConfig.PeopleEndpoint");
        if (peopleEndpoint == null) {
            peopleEndpoint = "https://api.mixpanel.com/engage";
        }
        this.mPeopleEndpoint = peopleEndpoint;
        String peopleFallbackEndpoint = metaData.getString("com.mixpanel.android.MPConfig.PeopleFallbackEndpoint");
        if (peopleFallbackEndpoint == null) {
            peopleFallbackEndpoint = "http://api.mixpanel.com/engage";
        }
        this.mPeopleFallbackEndpoint = peopleFallbackEndpoint;
        String decideEndpoint = metaData.getString("com.mixpanel.android.MPConfig.DecideEndpoint");
        if (decideEndpoint == null) {
            decideEndpoint = "https://decide.mixpanel.com/decide";
        }
        this.mDecideEndpoint = decideEndpoint;
        String decideFallbackEndpoint = metaData.getString("com.mixpanel.android.MPConfig.DecideFallbackEndpoint");
        if (decideFallbackEndpoint == null) {
            decideFallbackEndpoint = "http://decide.mixpanel.com/decide";
        }
        this.mDecideFallbackEndpoint = decideFallbackEndpoint;
        String editorUrl = metaData.getString("com.mixpanel.android.MPConfig.EditorUrl");
        if (editorUrl == null) {
            editorUrl = "wss://switchboard.mixpanel.com/connect/";
        }
        this.mEditorUrl = editorUrl;
        if (DEBUG) {
            Log.v(LOGTAG, "Mixpanel configured with:\n    AutoShowMixpanelUpdates " + getAutoShowMixpanelUpdates() + "\n" + "    BulkUploadLimit " + getBulkUploadLimit() + "\n" + "    FlushInterval " + getFlushInterval(context) + "\n" + "    DataExpiration " + getDataExpiration() + "\n" + "    MinimumDatabaseLimit " + getMinimumDatabaseLimit() + "\n" + "    DisableFallback " + getDisableFallback() + "\n" + "    DisableAppOpenEvent " + getDisableAppOpenEvent() + "\n" + "    DisableDeviceUIBinding " + getDisableGestureBindingUI() + "\n" + "    DisableEmulatorUIBinding " + getDisableEmulatorBindingUI() + "\n" + "    EnableDebugLogging " + DEBUG + "\n" + "    TestMode " + getTestMode() + "\n" + "    EventsEndpoint " + getEventsEndpoint() + "\n" + "    PeopleEndpoint " + getPeopleEndpoint() + "\n" + "    DecideEndpoint " + getDecideEndpoint() + "\n" + "    EventsFallbackEndpoint " + getEventsFallbackEndpoint() + "\n" + "    PeopleFallbackEndpoint " + getPeopleFallbackEndpoint() + "\n" + "    DecideFallbackEndpoint " + getDecideFallbackEndpoint() + "\n" + "    EditorUrl " + getEditorUrl() + "\n");
        }
    }

    public int getBulkUploadLimit() {
        return this.mBulkUploadLimit;
    }

    public int getFlushInterval() {
        return getFlushInterval(null);
    }

    public int getFlushInterval(Context context) {
        boolean isDebuggable;
        if (context != null) {
            ApplicationInfo applicationInfo = context.getApplicationInfo();
            int i = applicationInfo.flags & MAX_NOTIFICATION_CACHE_COUNT;
            applicationInfo.flags = i;
            if (i != 0) {
                isDebuggable = true;
                if (isDebuggable) {
                    return this.mFlushInterval;
                }
                return this.mDebugFlushInterval;
            }
        }
        isDebuggable = false;
        if (isDebuggable) {
            return this.mFlushInterval;
        }
        return this.mDebugFlushInterval;
    }

    public int getDataExpiration() {
        return this.mDataExpiration;
    }

    public int getMinimumDatabaseLimit() {
        return this.mMinimumDatabaseLimit;
    }

    public boolean getDisableFallback() {
        return this.mDisableFallback;
    }

    public boolean getDisableGestureBindingUI() {
        return this.mDisableGestureBindingUI;
    }

    public boolean getDisableEmulatorBindingUI() {
        return this.mDisableEmulatorBindingUI;
    }

    public boolean getDisableAppOpenEvent() {
        return this.mDisableAppOpenEvent;
    }

    public boolean getTestMode() {
        return this.mTestMode;
    }

    public String getEventsEndpoint() {
        return this.mEventsEndpoint;
    }

    public String getPeopleEndpoint() {
        return this.mPeopleEndpoint;
    }

    public String getDecideEndpoint() {
        return this.mDecideEndpoint;
    }

    public String getEventsFallbackEndpoint() {
        return this.mEventsFallbackEndpoint;
    }

    public String getPeopleFallbackEndpoint() {
        return this.mPeopleFallbackEndpoint;
    }

    public String getDecideFallbackEndpoint() {
        return this.mDecideFallbackEndpoint;
    }

    public boolean getAutoShowMixpanelUpdates() {
        return this.mAutoShowMixpanelUpdates;
    }

    public String getEditorUrl() {
        return this.mEditorUrl;
    }

    public String getResourcePackageName() {
        return this.mResourcePackageName;
    }

    public synchronized SSLSocketFactory getSSLSocketFactory() {
        return this.mSSLSocketFactory;
    }

    static MPConfig readConfig(Context appContext) {
        String packageName = appContext.getPackageName();
        try {
            Bundle configBundle = appContext.getPackageManager().getApplicationInfo(packageName, AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS).metaData;
            if (configBundle == null) {
                configBundle = new Bundle();
            }
            return new MPConfig(configBundle, appContext);
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Can't configure Mixpanel with package name " + packageName, e);
        }
    }
}
