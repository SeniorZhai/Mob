package com.mobcrush.mobcrush.common;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders.AppViewBuilder;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.google.android.gms.analytics.Tracker;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.datamodel.User;
import java.util.Map;

public class GoogleAnalyticsUtils {
    private static final String NULL_TRACKER_ERROR = "Google Analytics tracker was not initialized";
    private static Tracker mAppTracker;

    public static void setGoogleAnalyticsAppTracker(Tracker tracker) {
        mAppTracker = tracker;
    }

    public static void trackScreenNamed(String screenName) {
        if (mAppTracker != null) {
            mAppTracker.setScreenName(screenName);
            AppViewBuilder builder = new AppViewBuilder();
            User user = PreferenceUtility.getUser();
            builder.setCustomDimension(1, user._id);
            setSpecificVariables(user);
            mAppTracker.send(builder.build());
            Crashlytics.log(screenName);
            return;
        }
        throw new IllegalStateException(NULL_TRACKER_ERROR);
    }

    public static void trackAction(String category, String action) {
        trackAction(category, action, null, null);
    }

    public static void trackAction(String category, String action, String label, Long value) {
        if (mAppTracker != null) {
            EventBuilder action2 = new EventBuilder().setCategory(category).setAction(action);
            if (label == null) {
                label = "Android";
            }
            EventBuilder builder = action2.setLabel(label);
            if (value != null) {
                builder.setValue(value.longValue());
            }
            User user = PreferenceUtility.getUser();
            builder.setCustomDimension(1, user._id);
            setSpecificVariables(user);
            mAppTracker.send(builder.build());
            Crashlytics.log(category + ":" + action);
            return;
        }
        throw new IllegalStateException(NULL_TRACKER_ERROR);
    }

    private static void setSpecificVariables(User user) {
        if (user != null && user._id != null) {
            mAppTracker.set("&uid", user._id);
            mAppTracker.set("&appName", MainApplication.getRString(R.string.app_name, new Object[0]));
            mAppTracker.set("&appId", MainApplication.getContext().getPackageName());
            mAppTracker.set("&appVersion", Constants.APP_VERSION_NAME);
            mAppTracker.set("&appInstallerId", MainApplication.getContext().getPackageManager().getInstallerPackageName(MainApplication.getContext().getPackageName()));
        }
    }

    private static Map<String, String> createEvent(String category, String action, String label) {
        return new EventBuilder().setCategory(category).setAction(action).setLabel(label).build();
    }
}
