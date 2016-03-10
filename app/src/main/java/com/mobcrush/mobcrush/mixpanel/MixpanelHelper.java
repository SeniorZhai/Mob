package com.mobcrush.mobcrush.mixpanel;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import com.crashlytics.android.Crashlytics;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.logic.SocialNetwork;
import com.mobcrush.mobcrush.network.Network;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import org.apache.http.HttpHeaders;
import org.json.JSONException;
import org.json.JSONObject;

public class MixpanelHelper {
    private static final String INSTALL_TRACKED = "INSTALL_TRACKED";
    private static final String OTHER = "Other";
    private static final String PLATFORM = "Android";
    private static final int RETRY_TIMEOUT = 5000;
    private static final String TAG = "MixpanelHelper";
    private static Handler sHandler = new Handler();
    private static MixpanelHelper sInstance;
    private static HashMap<SocialNetwork, Integer> sShares;
    private Runnable generateAppInstallEvent = new Runnable() {
        public void run() {
            MixpanelHelper.this.generateAppInstallEvent();
        }
    };
    private Runnable generateAppOpenEvent = new Runnable() {
        public void run() {
            MixpanelHelper.this.generateAppOpenEvent();
        }
    };
    private GenerateFollowEvent generateFollowEvent = new GenerateFollowEvent();
    private Runnable generateLogInEvent = new Runnable() {
        public void run() {
            MixpanelHelper.this.generateLogInEvent();
        }
    };
    private Runnable generateLogOutEvent = new Runnable() {
        public void run() {
            MixpanelHelper.this.generateLogOutEvent();
        }
    };
    private GenerateSearchEvent generateSearchEvent = new GenerateSearchEvent();
    private GenerateSignUpEvent generateSignUpEvent = new GenerateSignUpEvent();
    private GenerateWatchBroadcastEvent generateWatchBroadcastEvent = new GenerateWatchBroadcastEvent();
    private Runnable initMixpanelAPI = new Runnable() {
        public void run() {
            MixpanelHelper.sHandler.removeCallbacks(MixpanelHelper.this.initMixpanelAPI);
            if (Constants.MIXPANEL_TOKEN == null) {
                Log.d(MixpanelHelper.TAG, "Mixpanel initialization...");
                MixpanelHelper.sHandler.postDelayed(MixpanelHelper.this.initMixpanelAPI, Constants.NOTIFICATION_BANNER_TIMEOUT);
            } else if (MixpanelHelper.this.mMixpanel == null) {
                MixpanelHelper.this.mMixpanel = MixpanelAPI.getInstance(MixpanelHelper.this.mContext, Constants.MIXPANEL_TOKEN);
                Log.d(MixpanelHelper.TAG, "Mixpanel is initialized");
            }
        }
    };
    private Context mContext;
    private MixpanelAPI mMixpanel;
    private SharedPreferences mSharedPreferences;

    static /* synthetic */ class AnonymousClass6 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$mixpanel$MixpanelHelper$Event = new int[Event.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$mixpanel$MixpanelHelper$Event[Event.CONNECT_FACEBOOK.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$mixpanel$MixpanelHelper$Event[Event.CONNECT_TWITTER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$mixpanel$MixpanelHelper$Event[Event.EDIT_PROFILE_PHOTO.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    private enum BroadcastProperty {
        SOURCE("Source"),
        LIVE("Live"),
        CHANNEL(Constants.SCREEN_CHANNEL),
        BROADCAST_ID("Broadcast ID"),
        BROADCASTER_USERNAME("Broadcaster Username"),
        GAME(Constants.ACTION_GAME),
        CHATS("Chat Messages Sent"),
        LIKE(Constants.ACTION_LIKE),
        FOLLOW(Constants.ACTION_FOLLOW),
        SHARE_FACEBOOK("Share Facebook"),
        SHARE_TWITTER("Share Twitter"),
        SHARE_MOBCRUSH("Share Mobcrush"),
        FULL_SCREEN_DURATION("Full Screen Duration"),
        DURATION(Constants.ACTION_DURATION),
        LOCAL_START_TIME_MINUTE("Local Start Time (Minute)"),
        LOCAL_START_TIME_HOUR("Local Start Time (Hour)"),
        LOCAL_START_TIME_WEEKDAY("Local Start Time (Weekday)"),
        LOCAL_END_TIME_MINUTE("Local End Time (Minute)"),
        LOCAL_END_TIME_HOUR("Local End Time (Hour)"),
        LOCAL_END_TIME_WEEKDAY("Local End Time (Weekday)"),
        UTC_START_TIME_MINUTE("UTC Start Time (Minute)"),
        UTC_START_TIME_HOUR("UTC Start Time (Hour)"),
        UTC_START_TIME_WEEKDAY("UTC Start Time (Weekday)"),
        UTC_END_TIME_MINUTE("UTC End Time (Minute)"),
        UTC_END_TIME_HOUR("UTC End Time (Hour)"),
        UTC_END_TIME_WEEKDAY("UTC End Time (Weekday)"),
        TOTAL_VIEWS("Broadcast Current Total Views"),
        TOTAL_LIKES("Broadcast Current Total Likes"),
        SHARE_TYPE("Share Type"),
        SHARE_COPY_URL("Share Copy URL");
        
        private final String mName;

        private BroadcastProperty(String propertyName) {
            this.mName = propertyName;
        }

        public String getName() {
            return this.mName;
        }
    }

    public enum Event {
        APP_INSTALL("App Install"),
        APP_OPEN("App Open"),
        SIGN_UP("Sign Up"),
        LOG_IN("Log In"),
        LOG_OUT("Log Out"),
        WATCH_BROADCAST("Watch Broadcast"),
        SEARCH(Constants.SCREEN_SEARCH),
        FOLLOW_USER("Follow User"),
        SHARE_BROADCAST("Share Broadcast"),
        CONNECT_FACEBOOK("Connect Facebook"),
        CONNECT_TWITTER("Connect Twitter"),
        EDIT_PROFILE_PHOTO("Edit Profile Photo"),
        VIEW_LIKED_VIDEOS("View Liked Videos");
        
        private final String mName;

        private Event(String propertyName) {
            this.mName = propertyName;
        }

        public String getName() {
            return this.mName;
        }
    }

    private class GenerateFollowEvent implements Runnable {
        User user;

        private GenerateFollowEvent() {
        }

        public void config(User user) {
            this.user = user;
        }

        public void run() {
            MixpanelHelper.this.generateFollowEvent(this.user);
        }
    }

    private class GenerateSearchEvent implements Runnable {
        String term;
        String username;

        private GenerateSearchEvent() {
        }

        public void config(String term, String username) {
            this.term = term;
            this.username = username;
        }

        public void run() {
            MixpanelHelper.this.generateSearchEvent(this.term, this.username);
        }
    }

    private class GenerateSignUpEvent implements Runnable {
        Calendar birthDate;
        String email;
        String username;

        private GenerateSignUpEvent() {
        }

        public void config(String email, String username, Calendar birthDate) {
            this.email = email;
            this.username = username;
            this.birthDate = birthDate;
        }

        public void run() {
            MixpanelHelper.this.generateSignUpEvent(this.email, this.username, this.birthDate);
        }
    }

    private class GenerateWatchBroadcastEvent implements Runnable {
        Broadcast broadcast;
        boolean isFollower;
        Source mSource;
        int sentMessages;
        long startTimeMillis;
        int urlsCopied;

        private GenerateWatchBroadcastEvent() {
        }

        public void config(Broadcast broadcast, int sentMessages, int urlsCopied, boolean isFollower, long startTimeMillis, Source mSource) {
            this.broadcast = broadcast;
            this.sentMessages = sentMessages;
            this.urlsCopied = urlsCopied;
            this.isFollower = isFollower;
            this.startTimeMillis = startTimeMillis;
            this.mSource = mSource;
        }

        public void run() {
            MixpanelHelper.this.generateWatchBroadcastEvent(this.broadcast, this.sentMessages, this.urlsCopied, this.isFollower, this.startTimeMillis, this.mSource);
        }
    }

    private enum MiscProperty {
        APP_INSTALLED("App Installed"),
        SEARCH_TERM("Search Term"),
        SELECTED_USER("Selected User"),
        FOLLOWED_USERNAME("Followed Username"),
        TOTAL_FOLLOWERS("Total Followers");
        
        private final String mName;

        private MiscProperty(String propertyName) {
            this.mName = propertyName;
        }

        public String getName() {
            return this.mName;
        }
    }

    private enum PeopleProperty {
        NAME("$name"),
        FIRST_TIME("First Time User"),
        LOGGED_IN("Logged In"),
        EMAIL("$email"),
        USERNAME("$username"),
        USER_ID("User ID"),
        DATE_OF_BIRTH("Date of Birth"),
        AGE(HttpHeaders.AGE),
        ACCOUNT_CREATED("$created"),
        BROADCASTER("Broadcaster"),
        APP_INSTALLED("App Installed"),
        PROFILE_PHOTO("Profile Photo"),
        FACEBOOK_CONNECT("Facebook Connect"),
        TWITTER_CONNECT("Twitter Connect");
        
        private final String mName;

        private PeopleProperty(String propertyName) {
            this.mName = propertyName;
        }

        public String getName() {
            return this.mName;
        }
    }

    public enum ShareType {
        COPY_URL("Copy URL"),
        FACEBOOK("Facebook"),
        TWITTER("Twitter"),
        SPECIFIC_FOLLOWERS("Specific Followers"),
        ALL_FOLLOWERS("All Followers");
        
        private final String mName;

        private ShareType(String propertyName) {
            this.mName = propertyName;
        }

        public String getName() {
            return this.mName;
        }
    }

    private enum SuperProperty {
        FIRST_INSTALL_DATE("First Installed Android"),
        FIRST_TIME("First Time User"),
        LOGGED_IN("Logged In"),
        EMAIL("Email"),
        USERNAME("Username"),
        USER_ID("User ID"),
        DATE_OF_BIRTH("Date of Birth"),
        AGE(HttpHeaders.AGE),
        ACCOUNT_CREATED("Account Created"),
        BROADCASTER("Broadcaster"),
        APP_INSTALLED("App Installed"),
        PROFILE_PHOTO("Profile Photo"),
        FACEBOOK_CONNECT("Facebook Connect"),
        TWITTER_CONNECT("Twitter Connect"),
        PLATFORM("Platform");
        
        private final String mName;

        private SuperProperty(String propertyName) {
            this.mName = propertyName;
        }

        public String getName() {
            return this.mName;
        }
    }

    public static MixpanelHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new MixpanelHelper(context);
        }
        return sInstance;
    }

    private MixpanelHelper(Context context) {
        this.mContext = context;
        this.initMixpanelAPI.run();
        initSharedPreferences(context);
    }

    private void initSharedPreferences(Context context) {
        if (this.mSharedPreferences == null) {
            this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public void generateAppInstallEvent() {
        if (this.mSharedPreferences.contains(INSTALL_TRACKED)) {
            Log.d(TAG, "AppInstallEvent is already tracked");
            return;
        }
        sHandler.removeCallbacks(this.generateAppInstallEvent);
        if (this.mMixpanel == null) {
            sHandler.postDelayed(this.generateAppInstallEvent, Constants.NOTIFICATION_BANNER_TIMEOUT);
            return;
        }
        Log.d(TAG, "AppInstallEvent is tracked");
        JSONObject eventProps = new JSONObject();
        JSONObject superProps = new JSONObject();
        try {
            superProps.put(SuperProperty.APP_INSTALLED.getName(), Utils.getCurrentDateUTC());
            superProps.put(SuperProperty.PLATFORM.getName(), PLATFORM);
            eventProps.put(MiscProperty.APP_INSTALLED.getName(), Utils.getCurrentDateUTC());
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mMixpanel.registerSuperProperties(superProps);
        this.mMixpanel.track(Event.APP_INSTALL.getName(), eventProps);
        this.mSharedPreferences.edit().putBoolean(INSTALL_TRACKED, true).commit();
    }

    public void generateAppOpenEvent() {
        boolean z = true;
        sHandler.removeCallbacks(this.generateAppOpenEvent);
        if (this.mMixpanel == null) {
            sHandler.postDelayed(this.generateAppOpenEvent, Constants.NOTIFICATION_BANNER_TIMEOUT);
            return;
        }
        JSONObject props = new JSONObject();
        try {
            props.put(SuperProperty.FIRST_TIME.getName(), !this.mMixpanel.getSuperProperties().has(SuperProperty.FIRST_TIME.getName()));
            boolean loggedIn = Network.isLoggedIn();
            props.put(SuperProperty.LOGGED_IN.getName(), loggedIn);
            if (loggedIn) {
                User user = PreferenceUtility.getUser();
                this.mMixpanel.identify(PreferenceUtility.getUser()._id);
                props.put(SuperProperty.BROADCASTER.getName(), isBroadcaster(user));
                String name = SuperProperty.PROFILE_PHOTO.getName();
                if (user.profileLogo == null) {
                    z = false;
                }
                props.put(name, z);
                props.put(SuperProperty.USER_ID.getName(), user._id);
                props.put(SuperProperty.USERNAME.getName(), user.username);
                props.put(SuperProperty.AGE.getName(), user.age);
                props.put(SuperProperty.EMAIL.getName(), user.email);
                props.put(SuperProperty.APP_INSTALLED.getName(), Utils.getInstallationDateUTC(MainApplication.getContext()));
                props.put(SuperProperty.PLATFORM.getName(), PLATFORM);
                props.put(SuperProperty.FACEBOOK_CONNECT.getName(), user.fb_connect);
                props.put(SuperProperty.TWITTER_CONNECT.getName(), user.twitter_connect);
                setUserProperties(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mMixpanel.registerSuperProperties(props);
        this.mMixpanel.track(Event.APP_OPEN.getName());
    }

    public void generateSignUpEvent(String email, String username, Calendar birthDate) {
        sHandler.removeCallbacks(this.generateAppOpenEvent);
        if (this.mMixpanel == null) {
            this.generateSignUpEvent.config(email, username, birthDate);
            sHandler.postDelayed(this.generateSignUpEvent, Constants.NOTIFICATION_BANNER_TIMEOUT);
            return;
        }
        User user = PreferenceUtility.getUser();
        if (user == null) {
            Exception exception = new Exception("generateSignUpEvent was called for empty user");
            exception.printStackTrace();
            Crashlytics.logException(exception);
            return;
        }
        String userId = user._id;
        JSONObject props = new JSONObject();
        try {
            props.put(SuperProperty.EMAIL.getName(), email);
            props.put(SuperProperty.USERNAME.getName(), username);
            props.put(SuperProperty.AGE.getName(), Utils.getAgeFromDOB(birthDate));
            props.put(SuperProperty.BROADCASTER.getName(), false);
            props.put(SuperProperty.LOGGED_IN.getName(), Network.isLoggedIn());
            props.put(SuperProperty.APP_INSTALLED.getName(), Utils.getInstallationDateUTC(MainApplication.getContext()));
            props.put(SuperProperty.FACEBOOK_CONNECT.getName(), false);
            props.put(SuperProperty.TWITTER_CONNECT.getName(), false);
            props.put(SuperProperty.USER_ID.getName(), userId);
            props.put(SuperProperty.DATE_OF_BIRTH.getName(), new SimpleDateFormat("yyyy-MM-dd").format(birthDate.getTime()));
            props.put(SuperProperty.ACCOUNT_CREATED.getName(), Utils.getCurrentDateUTC());
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mMixpanel.registerSuperProperties(props);
        this.mMixpanel.track(Event.SIGN_UP.getName());
        this.mMixpanel.alias(userId, null);
    }

    public void generateLogInEvent() {
        sHandler.removeCallbacks(this.generateLogInEvent);
        if (this.mMixpanel == null) {
            sHandler.postDelayed(this.generateLogInEvent, Constants.NOTIFICATION_BANNER_TIMEOUT);
            return;
        }
        User user = PreferenceUtility.getUser();
        if (user == null) {
            Exception exception = new Exception("generateLogInEvent was called for empty user");
            exception.printStackTrace();
            Crashlytics.logException(exception);
            return;
        }
        JSONObject props = new JSONObject();
        try {
            props.put(SuperProperty.USER_ID.getName(), user._id);
            props.put(SuperProperty.USERNAME.getName(), user.username);
            props.put(SuperProperty.AGE.getName(), user.age);
            props.put(SuperProperty.EMAIL.getName(), user.email);
            props.put(SuperProperty.APP_INSTALLED.getName(), Utils.getInstallationDateUTC(MainApplication.getContext()));
            props.put(SuperProperty.LOGGED_IN.getName(), Network.isLoggedIn());
            props.put(SuperProperty.BROADCASTER.getName(), isBroadcaster(user));
            props.put(SuperProperty.PROFILE_PHOTO.getName(), user.profileLogo != null);
            props.put(SuperProperty.FACEBOOK_CONNECT.getName(), user.fb_connect);
            props.put(SuperProperty.TWITTER_CONNECT.getName(), user.twitter_connect);
            props.put(SuperProperty.DATE_OF_BIRTH.getName(), user.birthdate);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mMixpanel.registerSuperProperties(props);
        this.mMixpanel.identify(user._id);
        setUserProperties(user);
        this.mMixpanel.track(Event.LOG_IN.getName());
    }

    private void setUserProperties(User user) {
        this.mMixpanel.getPeople().identify(user._id);
        if (isBroadcaster(user)) {
            JSONObject props = new JSONObject();
            try {
                props.put(PeopleProperty.USERNAME.getName(), user.username);
                props.put(PeopleProperty.NAME.getName(), user.username);
                props.put(PeopleProperty.ACCOUNT_CREATED.getName(), user.accountCreation);
                props.put(PeopleProperty.EMAIL.getName(), user.email);
                props.put(PeopleProperty.AGE.getName(), user.age);
                props.put(PeopleProperty.USER_ID.getName(), user._id);
                props.put(PeopleProperty.APP_INSTALLED.getName(), Utils.getInstallationDateUTC(MainApplication.getContext()));
                props.put(PeopleProperty.LOGGED_IN.getName(), Network.isLoggedIn());
                props.put(PeopleProperty.BROADCASTER.getName(), isBroadcaster(user));
                props.put(PeopleProperty.PROFILE_PHOTO.getName(), user.profileLogo != null);
                props.put(PeopleProperty.FACEBOOK_CONNECT.getName(), user.fb_connect);
                props.put(PeopleProperty.TWITTER_CONNECT.getName(), user.twitter_connect);
                props.put(PeopleProperty.DATE_OF_BIRTH.getName(), user.birthdate);
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            this.mMixpanel.getPeople().set(props);
        }
    }

    private void setUserProperty(PeopleProperty property, boolean value) {
        User user = PreferenceUtility.getUser();
        if (isBroadcaster(user)) {
            this.mMixpanel.getPeople().identify(user._id);
            JSONObject props = new JSONObject();
            try {
                props.put(property.getName(), value);
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            this.mMixpanel.getPeople().set(props);
        }
    }

    private boolean isBroadcaster(User user) {
        if (user.broadcastCount == null || user.followerCount == null || user.broadcastCount.intValue() <= 0 || user.followerCount.intValue() <= 10) {
            return false;
        }
        return true;
    }

    public void generateLogOutEvent() {
        sHandler.removeCallbacks(this.generateLogOutEvent);
        if (this.mMixpanel == null) {
            sHandler.postDelayed(this.generateLogOutEvent, Constants.NOTIFICATION_BANNER_TIMEOUT);
        } else if (PreferenceUtility.getUser() == null) {
            Exception exception = new Exception("generateLogOutEvent was called for empty user");
            exception.printStackTrace();
            Crashlytics.logException(exception);
        } else {
            JSONObject props = new JSONObject();
            try {
                props.put(SuperProperty.LOGGED_IN.getName(), false);
            } catch (JSONException e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            this.mMixpanel.registerSuperProperties(props);
            this.mMixpanel.track(Event.LOG_OUT.getName());
        }
    }

    public void trackShareEvent(Broadcast broadcast, SocialNetwork shareNetwork, ShareType shareType, int count) {
        if (sShares == null) {
            sShares = new HashMap();
            for (SocialNetwork network : SocialNetwork.values()) {
                sShares.put(network, Integer.valueOf(0));
            }
        }
        sShares.put(shareNetwork, Integer.valueOf(((Integer) sShares.get(shareNetwork)).intValue() + count));
        JSONObject superProps = new JSONObject();
        JSONObject eventProps = new JSONObject();
        try {
            eventProps.put(BroadcastProperty.BROADCASTER_USERNAME.getName(), broadcast.user.username);
            eventProps.put(BroadcastProperty.LIVE.getName(), broadcast.isLive);
            eventProps.put(BroadcastProperty.GAME.getName(), broadcast.game == null ? OTHER : broadcast.game.name);
            eventProps.put(BroadcastProperty.SHARE_TYPE.getName(), shareType.getName());
            superProps.put(SuperProperty.PLATFORM.getName(), PLATFORM);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mMixpanel.registerSuperProperties(superProps);
        this.mMixpanel.track(Event.SHARE_BROADCAST.getName(), eventProps);
    }

    public void generateWatchBroadcastEvent(Broadcast broadcast, int sentMessages, int urlsCopied, boolean isFollower, long startTimeMillis, Source mSource) {
        sHandler.removeCallbacks(this.generateWatchBroadcastEvent);
        if (this.mMixpanel == null) {
            this.generateWatchBroadcastEvent.config(broadcast, sentMessages, urlsCopied, isFollower, startTimeMillis, mSource);
            sHandler.postDelayed(this.generateWatchBroadcastEvent, Constants.NOTIFICATION_BANNER_TIMEOUT);
            return;
        }
        JSONObject eventProps = new JSONObject();
        JSONObject superProps = new JSONObject();
        try {
            eventProps.put(BroadcastProperty.SOURCE.getName(), mSource.getName());
            eventProps.put(BroadcastProperty.LIVE.getName(), broadcast.isLive);
            eventProps.put(BroadcastProperty.CHANNEL.getName(), broadcast.channel.name);
            eventProps.put(BroadcastProperty.BROADCAST_ID.getName(), broadcast._id);
            eventProps.put(BroadcastProperty.BROADCASTER_USERNAME.getName(), broadcast.user.username);
            eventProps.put(BroadcastProperty.GAME.getName(), broadcast.game == null ? OTHER : broadcast.game.name);
            eventProps.put(BroadcastProperty.CHATS.getName(), sentMessages);
            eventProps.put(BroadcastProperty.SHARE_COPY_URL.getName(), urlsCopied > 0 ? 1 : 0);
            eventProps.put(BroadcastProperty.LIKE.getName(), broadcast.currentLiked ? 1 : 0);
            eventProps.put(BroadcastProperty.FOLLOW.getName(), isFollower ? 1 : 0);
            if (sShares != null) {
                eventProps.put(BroadcastProperty.SHARE_FACEBOOK.getName(), sShares.get(SocialNetwork.Facebook.name()));
                eventProps.put(BroadcastProperty.SHARE_TWITTER.getName(), sShares.get(SocialNetwork.Twitter.name()));
                eventProps.put(BroadcastProperty.SHARE_MOBCRUSH.getName(), sShares.get(SocialNetwork.Mobcrush.name()));
                sShares = null;
            } else {
                eventProps.put(BroadcastProperty.SHARE_FACEBOOK.getName(), 0);
                eventProps.put(BroadcastProperty.SHARE_TWITTER.getName(), 0);
                eventProps.put(BroadcastProperty.SHARE_MOBCRUSH.getName(), 0);
            }
            long endTimeMillis = System.currentTimeMillis();
            eventProps.put(BroadcastProperty.DURATION.getName(), (int) ((endTimeMillis - startTimeMillis) / 1000));
            Calendar startTime = Calendar.getInstance();
            startTime.setTimeInMillis(startTimeMillis);
            eventProps.put(BroadcastProperty.LOCAL_START_TIME_MINUTE.getName(), startTime.get(12));
            eventProps.put(BroadcastProperty.LOCAL_START_TIME_HOUR.getName(), startTime.get(10));
            eventProps.put(BroadcastProperty.LOCAL_START_TIME_WEEKDAY.getName(), startTime.get(7) - 1);
            Calendar endTime = Calendar.getInstance();
            endTime.setTimeInMillis(endTimeMillis);
            eventProps.put(BroadcastProperty.LOCAL_END_TIME_MINUTE.getName(), endTime.get(12));
            eventProps.put(BroadcastProperty.LOCAL_END_TIME_HOUR.getName(), endTime.get(10));
            eventProps.put(BroadcastProperty.LOCAL_END_TIME_WEEKDAY.getName(), endTime.get(7) - 1);
            Calendar startTimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            startTimeUTC.setTimeInMillis(startTimeMillis);
            eventProps.put(BroadcastProperty.UTC_START_TIME_MINUTE.getName(), startTimeUTC.get(12));
            eventProps.put(BroadcastProperty.UTC_START_TIME_HOUR.getName(), startTimeUTC.get(10));
            eventProps.put(BroadcastProperty.UTC_START_TIME_WEEKDAY.getName(), startTimeUTC.get(7) - 1);
            Calendar endTimeUTC = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            endTimeUTC.setTimeInMillis(endTimeMillis);
            eventProps.put(BroadcastProperty.UTC_END_TIME_MINUTE.getName(), endTimeUTC.get(12));
            eventProps.put(BroadcastProperty.UTC_END_TIME_HOUR.getName(), endTimeUTC.get(10));
            eventProps.put(BroadcastProperty.UTC_END_TIME_WEEKDAY.getName(), endTimeUTC.get(7) - 1);
            eventProps.put(BroadcastProperty.TOTAL_VIEWS.getName(), broadcast.totalViews);
            eventProps.put(BroadcastProperty.TOTAL_LIKES.getName(), broadcast.likes);
            superProps.put(SuperProperty.PLATFORM.getName(), PLATFORM);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mMixpanel.registerSuperProperties(superProps);
        this.mMixpanel.track(Event.WATCH_BROADCAST.getName(), eventProps);
    }

    public void generateSearchEvent(String term, String username) {
        sHandler.removeCallbacks(this.generateSearchEvent);
        if (this.mMixpanel == null) {
            this.generateSearchEvent.config(term, username);
            sHandler.postDelayed(this.generateSearchEvent, Constants.NOTIFICATION_BANNER_TIMEOUT);
            return;
        }
        JSONObject eventProps = new JSONObject();
        JSONObject superProps = new JSONObject();
        try {
            eventProps.put(MiscProperty.SEARCH_TERM.getName(), term);
            eventProps.put(MiscProperty.SELECTED_USER.getName(), username);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mMixpanel.track(Event.SEARCH.getName(), eventProps);
    }

    public void generateFollowEvent(User user) {
        sHandler.removeCallbacks(this.generateFollowEvent);
        if (this.mMixpanel == null) {
            this.generateFollowEvent.config(user);
            sHandler.postDelayed(this.generateFollowEvent, Constants.NOTIFICATION_BANNER_TIMEOUT);
            return;
        }
        JSONObject eventProps = new JSONObject();
        JSONObject superProps = new JSONObject();
        try {
            eventProps.put(MiscProperty.FOLLOWED_USERNAME.getName(), user.username);
            eventProps.put(MiscProperty.TOTAL_FOLLOWERS.getName(), user.followerCount);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mMixpanel.track(Event.FOLLOW_USER.getName(), eventProps);
    }

    public void generateEvent(Event event) {
        if (this.mMixpanel == null) {
            Crashlytics.logException(new Exception("generateEvent is called with empty Mixpanel instance"));
            return;
        }
        switch (AnonymousClass6.$SwitchMap$com$mobcrush$mobcrush$mixpanel$MixpanelHelper$Event[event.ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                setSuperProperty(SuperProperty.FACEBOOK_CONNECT, true);
                setUserProperty(PeopleProperty.FACEBOOK_CONNECT, true);
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                setSuperProperty(SuperProperty.TWITTER_CONNECT, true);
                setUserProperty(PeopleProperty.TWITTER_CONNECT, true);
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                setSuperProperty(SuperProperty.PROFILE_PHOTO, true);
                setUserProperty(PeopleProperty.PROFILE_PHOTO, true);
                break;
        }
        this.mMixpanel.track(event.getName());
    }

    private void setSuperProperty(SuperProperty property, boolean value) {
        JSONObject superProps = new JSONObject();
        try {
            superProps.put(property.getName(), value);
        } catch (JSONException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mMixpanel.registerSuperProperties(superProps);
    }

    private void putIncrementedSuperProperty(SuperProperty property, int increment, JSONObject props) throws JSONException {
        putIncrementedSuperProperty(property.getName(), increment, props);
    }

    private void putIncrementedSuperProperty(String propertyName, int increment, JSONObject props) throws JSONException {
        if (this.mMixpanel != null && this.mMixpanel.getSuperProperties().has(propertyName)) {
            increment += this.mMixpanel.getSuperProperties().getInt(propertyName);
        }
        props.put(propertyName, increment);
    }

    public void showSurveyIfAvailable(Activity activity) {
        if (this.mMixpanel != null) {
            this.mMixpanel.getPeople().showSurveyIfAvailable(activity);
        }
    }

    public void flush() {
        if (this.mMixpanel != null) {
            this.mMixpanel.flush();
        }
    }
}
