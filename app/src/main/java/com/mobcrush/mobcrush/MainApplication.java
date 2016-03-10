package com.mobcrush.mobcrush;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.multidex.MultiDex;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.session.PlaybackStateCompat;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.firebase.client.Config;
import com.firebase.client.EventTarget;
import com.firebase.client.Firebase;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.network.Network;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration.Builder;
import com.nostra13.universalimageloader.utils.StorageUtils;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.events.EventsFilesManager;
import java.util.HashMap;

public class MainApplication extends Application {
    private static final int TIMEOUT_FOR_BACKGROUND_STATE = 5000;
    private static int mActivitiesCount;
    private static boolean mAppInBackground;
    public static ChatMessagesAdapter mChatMessagesAdapter;
    public static final HashMap<String, Long> mChatTimestamps = new HashMap();
    private static Handler mCheckHandler = new Handler();
    private static Context mContext;
    public static Firebase mFirebase;

    public static Context getContext() {
        return mContext;
    }

    public static String getRString(@StringRes int resId, Object... formatArgs) {
        if (mContext != null) {
            return mContext.getString(resId, formatArgs);
        }
        return null;
    }

    public static void onActivityResumed(String classname) {
        if (mAppInBackground) {
            mAppInBackground = false;
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(new Intent(Constants.EVENT_APP_RESUMED));
        }
        mActivitiesCount++;
    }

    public static void onActivityPaused(String classname) {
        mActivitiesCount--;
        if (mActivitiesCount <= 0) {
            mActivitiesCount = 0;
        }
        if (mCheckHandler == null) {
            mCheckHandler = new Handler();
        }
        mCheckHandler.postDelayed(new Runnable() {
            public void run() {
                MainApplication.mCheckHandler.removeCallbacks(null);
                MainApplication.mAppInBackground = MainApplication.mActivitiesCount <= 0;
            }
        }, Constants.NOTIFICATION_BANNER_TIMEOUT);
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void onCreate() {
        super.onCreate();
        mContext = this;
        Constants.APP_VERSION_NAME = Utils.getAppVersionName(this);
        Constants.BASE_ADDRESS = getRString(R.string.base_address, new Object[0]);
        Constants.CHAT_BASE_ADDRESS = getRString(R.string.chat_base_address, new Object[0]);
        Constants.MIXPANEL_TOKEN = PreferenceUtility.getConfig().mixpanelToken;
        Constants.TERMS_OF_SERVICES_ADDRESS = getRString(R.string.tos_address, new Object[0]);
        initGoogleAnalyticsTracker();
        Fabric.with(getApplicationContext(), new Crashlytics());
        Utils.setUserToCrashlytics();
        Network.init(this);
        if (Constants.MIXPANEL_TOKEN == null) {
            updateConfig();
        }
        if (PreferenceUtility.getConfigTimestamp() <= 0 || Utils.compareVersions(Constants.APP_VERSION_NAME, PreferenceUtility.getConfig().android.currentVersion).intValue() < 0) {
            PreferenceUtility.updateWhatsNewVersionToCurrent();
        } else {
            Constants.SHOW_WHATS_NEW = true;
        }
        if (PreferenceUtility.getUser() != null && Network.isLoggedIn()) {
            Network.getMyProfile(null, null, null);
            if (PreferenceUtility.getStreamKey() == null) {
                Network.getUserChannels(null, false, null, null);
            }
        }
        Firebase.setAndroidContext(this);
        Config defaultConfig = Firebase.getDefaultConfig();
        defaultConfig.setEventTarget(new EventTarget() {
            public void postEvent(Runnable runnable) {
                new Thread(runnable).start();
            }

            public void shutdown() {
            }

            public void restart() {
            }
        });
        Firebase.setDefaultConfig(defaultConfig);
        int cacheSize = ((int) (Runtime.getRuntime().maxMemory() / PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID)) / 8;
        Context context = getApplicationContext();
        ImageLoader.getInstance().init(new Builder(context).threadPoolSize(5).diskCache(new UnlimitedDiskCache(StorageUtils.getCacheDirectory(context, true), null, new FileNameGenerator() {
            public String generate(String s) {
                return BuildConfig.FLAVOR + s.hashCode() + EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR + Uri.parse(s).getLastPathSegment();
            }
        })).threadPriority(1).memoryCache(new LruMemoryCache(cacheSize)).denyCacheImageMultipleSizesInMemory().build());
        if (mFirebase == null) {
            mFirebase = new Firebase(Constants.CHAT_BASE_ADDRESS);
        }
    }

    private void updateConfig() {
        if (Utils.isInternetAvailable(this)) {
            Network.updateConfig(null, new Listener<Boolean>() {
                public void onResponse(Boolean response) {
                    if (!Boolean.TRUE.equals(response) || PreferenceUtility.getConfig() == null) {
                        MainApplication.this.updateConfig();
                    }
                }
            }, new ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    MainApplication.this.updateConfig();
                }
            });
        }
    }

    synchronized void initGoogleAnalyticsTracker() {
        Tracker t = GoogleAnalytics.getInstance(this).newTracker((int) R.xml.app_tracker);
        t.enableAdvertisingIdCollection(true);
        GoogleAnalyticsUtils.setGoogleAnalyticsAppTracker(t);
    }
}
