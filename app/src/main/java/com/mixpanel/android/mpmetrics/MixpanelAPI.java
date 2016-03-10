package com.mixpanel.android.mpmetrics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import bolts.MeasurementEvent;
import com.mixpanel.android.R;
import com.mixpanel.android.mpmetrics.BackgroundCapture.OnBackgroundCapturedListener;
import com.mixpanel.android.mpmetrics.DecideMessages.OnNewResultsListener;
import com.mixpanel.android.mpmetrics.InAppNotification.Type;
import com.mixpanel.android.mpmetrics.UpdateDisplayState.DisplayState.InAppNotificationState;
import com.mixpanel.android.mpmetrics.UpdateDisplayState.DisplayState.SurveyState;
import com.mixpanel.android.surveys.SurveyActivity;
import com.mixpanel.android.util.ActivityImageUtils;
import com.mixpanel.android.viewcrawler.TrackingDebug;
import com.mixpanel.android.viewcrawler.UpdatesFromMixpanel;
import com.mixpanel.android.viewcrawler.ViewCrawler;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MixpanelAPI {
    private static final String APP_LINKS_LOGTAG = "MixpanelAPI.AL";
    private static final String ENGAGE_DATE_FORMAT_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String LOGTAG = "MixpanelAPI.API";
    public static final String VERSION = "4.6.2";
    private static final Map<String, Map<Context, MixpanelAPI>> sInstanceMap = new HashMap();
    private static final SharedPreferencesLoader sPrefsLoader = new SharedPreferencesLoader();
    private static Future<SharedPreferences> sReferrerPrefs;
    private static final Tweaks sSharedTweaks = new Tweaks();
    private final MPConfig mConfig = getConfig();
    private final Context mContext;
    private final DecideMessages mDecideMessages;
    private final Map<String, String> mDeviceInfo;
    private final Map<String, Long> mEventTimings = new HashMap();
    private final AnalyticsMessages mMessages;
    private final PeopleImpl mPeople = new PeopleImpl();
    private final PersistentIdentity mPersistentIdentity;
    private final String mToken;
    private final TrackingDebug mTrackingDebug;
    private final UpdatesFromMixpanel mUpdatesFromMixpanel;
    private final UpdatesListener mUpdatesListener;

    interface InstanceProcessor {
        void process(MixpanelAPI mixpanelAPI);
    }

    static /* synthetic */ class AnonymousClass3 {
        static final /* synthetic */ int[] $SwitchMap$com$mixpanel$android$mpmetrics$InAppNotification$Type = new int[Type.values().length];

        static {
            try {
                $SwitchMap$com$mixpanel$android$mpmetrics$InAppNotification$Type[Type.MINI.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mixpanel$android$mpmetrics$InAppNotification$Type[Type.TAKEOVER.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public interface People {
        void addOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener onMixpanelUpdatesReceivedListener);

        void append(String str, Object obj);

        @Deprecated
        void checkForSurvey(SurveyCallbacks surveyCallbacks);

        @Deprecated
        void checkForSurvey(SurveyCallbacks surveyCallbacks, Activity activity);

        void clearCharges();

        void clearPushRegistrationId();

        void deleteUser();

        String getDistinctId();

        InAppNotification getNotificationIfAvailable();

        Survey getSurveyIfAvailable();

        void identify(String str);

        void increment(String str, double d);

        void increment(Map<String, ? extends Number> map);

        void initPushHandling(String str);

        void joinExperimentIfAvailable();

        void merge(String str, JSONObject jSONObject);

        void removeOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener onMixpanelUpdatesReceivedListener);

        void set(String str, Object obj);

        void set(JSONObject jSONObject);

        void setMap(Map<String, Object> map);

        void setOnce(String str, Object obj);

        void setOnce(JSONObject jSONObject);

        void setOnceMap(Map<String, Object> map);

        void setPushRegistrationId(String str);

        void showGivenNotification(InAppNotification inAppNotification, Activity activity);

        void showNotificationById(int i, Activity activity);

        void showNotificationIfAvailable(Activity activity);

        @Deprecated
        void showSurvey(Survey survey, Activity activity);

        void showSurveyById(int i, Activity activity);

        void showSurveyIfAvailable(Activity activity);

        void trackCharge(double d, JSONObject jSONObject);

        void trackNotification(String str, InAppNotification inAppNotification);

        void trackNotificationSeen(InAppNotification inAppNotification);

        void union(String str, JSONArray jSONArray);

        void unset(String str);

        People withIdentity(String str);
    }

    private class PeopleImpl implements People {
        private PeopleImpl() {
        }

        public void identify(String distinctId) {
            synchronized (MixpanelAPI.this.mPersistentIdentity) {
                MixpanelAPI.this.mPersistentIdentity.setPeopleDistinctId(distinctId);
                MixpanelAPI.this.mDecideMessages.setDistinctId(distinctId);
            }
            MixpanelAPI.this.pushWaitingPeopleRecord();
        }

        public void setMap(Map<String, Object> properties) {
            if (properties == null) {
                Log.e(MixpanelAPI.LOGTAG, "setMap does not accept null properties");
                return;
            }
            try {
                set(new JSONObject(properties));
            } catch (NullPointerException e) {
                Log.w(MixpanelAPI.LOGTAG, "Can't have null keys in the properties of setMap!");
            }
        }

        public void set(JSONObject properties) {
            try {
                JSONObject sendProperties = new JSONObject(MixpanelAPI.this.mDeviceInfo);
                Iterator<?> iter = properties.keys();
                while (iter.hasNext()) {
                    String key = (String) iter.next();
                    sendProperties.put(key, properties.get(key));
                }
                MixpanelAPI.this.recordPeopleMessage(stdPeopleMessage("$set", sendProperties));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "Exception setting people properties", e);
            }
        }

        public void set(String property, Object value) {
            try {
                set(new JSONObject().put(property, value));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "set", e);
            }
        }

        public void setOnceMap(Map<String, Object> properties) {
            if (properties == null) {
                Log.e(MixpanelAPI.LOGTAG, "setOnceMap does not accept null properties");
                return;
            }
            try {
                setOnce(new JSONObject(properties));
            } catch (NullPointerException e) {
                Log.w(MixpanelAPI.LOGTAG, "Can't have null keys in the properties setOnceMap!");
            }
        }

        public void setOnce(JSONObject properties) {
            try {
                MixpanelAPI.this.recordPeopleMessage(stdPeopleMessage("$set_once", properties));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "Exception setting people properties");
            }
        }

        public void setOnce(String property, Object value) {
            try {
                setOnce(new JSONObject().put(property, value));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "set", e);
            }
        }

        public void increment(Map<String, ? extends Number> properties) {
            try {
                MixpanelAPI.this.recordPeopleMessage(stdPeopleMessage("$add", new JSONObject(properties)));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "Exception incrementing properties", e);
            }
        }

        public void merge(String property, JSONObject updates) {
            JSONObject mergeMessage = new JSONObject();
            try {
                mergeMessage.put(property, updates);
                MixpanelAPI.this.recordPeopleMessage(stdPeopleMessage("$merge", mergeMessage));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "Exception merging a property", e);
            }
        }

        public void increment(String property, double value) {
            Map<String, Double> map = new HashMap();
            map.put(property, Double.valueOf(value));
            increment(map);
        }

        public void append(String name, Object value) {
            try {
                JSONObject properties = new JSONObject();
                properties.put(name, value);
                MixpanelAPI.this.recordPeopleMessage(stdPeopleMessage("$append", properties));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "Exception appending a property", e);
            }
        }

        public void union(String name, JSONArray value) {
            try {
                JSONObject properties = new JSONObject();
                properties.put(name, value);
                MixpanelAPI.this.recordPeopleMessage(stdPeopleMessage("$union", properties));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "Exception unioning a property");
            }
        }

        public void unset(String name) {
            try {
                JSONArray names = new JSONArray();
                names.put(name);
                MixpanelAPI.this.recordPeopleMessage(stdPeopleMessage("$unset", names));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "Exception unsetting a property", e);
            }
        }

        @Deprecated
        public void checkForSurvey(SurveyCallbacks callbacks) {
            Log.i(MixpanelAPI.LOGTAG, "MixpanelAPI.checkForSurvey is deprecated. Calling is now a no-op.\n    to query surveys, call MixpanelAPI.getPeople().getSurveyIfAvailable()");
        }

        @Deprecated
        public void checkForSurvey(SurveyCallbacks callbacks, Activity parentActivity) {
            Log.i(MixpanelAPI.LOGTAG, "MixpanelAPI.checkForSurvey is deprecated. Calling is now a no-op.\n    to query surveys, call MixpanelAPI.getPeople().getSurveyIfAvailable()");
        }

        public InAppNotification getNotificationIfAvailable() {
            return MixpanelAPI.this.mDecideMessages.getNotification(MixpanelAPI.this.mConfig.getTestMode());
        }

        public void trackNotificationSeen(InAppNotification notif) {
            if (notif != null) {
                trackNotification("$campaign_delivery", notif);
                People people = MixpanelAPI.this.getPeople().withIdentity(getDistinctId());
                DateFormat dateFormat = new SimpleDateFormat(MixpanelAPI.ENGAGE_DATE_FORMAT_STRING, Locale.US);
                JSONObject notifProperties = notif.getCampaignProperties();
                try {
                    notifProperties.put("$time", dateFormat.format(new Date()));
                } catch (JSONException e) {
                    Log.e(MixpanelAPI.LOGTAG, "Exception trying to track an in-app notification seen", e);
                }
                people.append("$campaigns", Integer.valueOf(notif.getId()));
                people.append("$notifications", notifProperties);
            }
        }

        public Survey getSurveyIfAvailable() {
            return MixpanelAPI.this.mDecideMessages.getSurvey(MixpanelAPI.this.mConfig.getTestMode());
        }

        @Deprecated
        public void showSurvey(Survey survey, Activity parent) {
            showGivenOrAvailableSurvey(survey, parent);
        }

        public void showSurveyIfAvailable(Activity parent) {
            if (VERSION.SDK_INT >= 16) {
                showGivenOrAvailableSurvey(null, parent);
            }
        }

        public void showSurveyById(int id, Activity parent) {
            Survey s = MixpanelAPI.this.mDecideMessages.getSurvey(id, MixpanelAPI.this.mConfig.getTestMode());
            if (s != null) {
                showGivenOrAvailableSurvey(s, parent);
            }
        }

        public void showNotificationIfAvailable(Activity parent) {
            if (VERSION.SDK_INT >= 16) {
                showGivenOrAvailableNotification(null, parent);
            }
        }

        public void showNotificationById(int id, Activity parent) {
            showGivenNotification(MixpanelAPI.this.mDecideMessages.getNotification(id, MixpanelAPI.this.mConfig.getTestMode()), parent);
        }

        public void showGivenNotification(InAppNotification notif, Activity parent) {
            if (notif != null) {
                showGivenOrAvailableNotification(notif, parent);
            }
        }

        public void trackNotification(String eventName, InAppNotification notif) {
            MixpanelAPI.this.track(eventName, notif.getCampaignProperties());
        }

        public void joinExperimentIfAvailable() {
            JSONArray variants = MixpanelAPI.this.mDecideMessages.getVariants();
            if (variants != null) {
                MixpanelAPI.this.mUpdatesFromMixpanel.setVariants(variants);
            }
        }

        public void trackCharge(double amount, JSONObject properties) {
            Date now = new Date();
            DateFormat dateFormat = new SimpleDateFormat(MixpanelAPI.ENGAGE_DATE_FORMAT_STRING, Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                JSONObject transactionValue = new JSONObject();
                transactionValue.put("$amount", amount);
                transactionValue.put("$time", dateFormat.format(now));
                if (properties != null) {
                    Iterator<?> iter = properties.keys();
                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        transactionValue.put(key, properties.get(key));
                    }
                }
                append("$transactions", transactionValue);
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "Exception creating new charge", e);
            }
        }

        public void clearCharges() {
            unset("$transactions");
        }

        public void deleteUser() {
            try {
                MixpanelAPI.this.recordPeopleMessage(stdPeopleMessage("$delete", JSONObject.NULL));
            } catch (JSONException e) {
                Log.e(MixpanelAPI.LOGTAG, "Exception deleting a user");
            }
        }

        public void setPushRegistrationId(String registrationId) {
            synchronized (MixpanelAPI.this.mPersistentIdentity) {
                if (MixpanelAPI.this.mPersistentIdentity.getPeopleDistinctId() == null) {
                    return;
                }
                MixpanelAPI.this.mPersistentIdentity.storePushId(registrationId);
                JSONArray ids = new JSONArray();
                ids.put(registrationId);
                union("$android_devices", ids);
            }
        }

        public void clearPushRegistrationId() {
            MixpanelAPI.this.mPersistentIdentity.clearPushId();
            set("$android_devices", new JSONArray());
        }

        public void initPushHandling(String senderID) {
            if (ConfigurationChecker.checkPushConfiguration(MixpanelAPI.this.mContext)) {
                final String pushId = MixpanelAPI.this.mPersistentIdentity.getPushId();
                if (pushId != null) {
                    MixpanelAPI.allInstances(new InstanceProcessor() {
                        public void process(MixpanelAPI api) {
                            if (MPConfig.DEBUG) {
                                Log.v(MixpanelAPI.LOGTAG, "Using existing pushId " + pushId);
                            }
                            api.getPeople().setPushRegistrationId(pushId);
                        }
                    });
                    return;
                } else if (VERSION.SDK_INT >= 21) {
                    registerForPushIdAPI21AndUp(senderID);
                    return;
                } else {
                    registerForPushIdAPI19AndOlder(senderID);
                    return;
                }
            }
            Log.i(MixpanelAPI.LOGTAG, "Can't register for push notification services. Push notifications will not work.");
            Log.i(MixpanelAPI.LOGTAG, "See log tagged " + ConfigurationChecker.LOGTAG + " above for details.");
        }

        public String getDistinctId() {
            return MixpanelAPI.this.mPersistentIdentity.getPeopleDistinctId();
        }

        public People withIdentity(final String distinctId) {
            if (distinctId == null) {
                return null;
            }
            return new PeopleImpl() {
                public String getDistinctId() {
                    return distinctId;
                }

                public void identify(String distinctId) {
                    throw new RuntimeException("This MixpanelPeople object has a fixed, constant distinctId");
                }
            };
        }

        public void addOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener listener) {
            MixpanelAPI.this.mUpdatesListener.addOnMixpanelUpdatesReceivedListener(listener);
        }

        public void removeOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener listener) {
            MixpanelAPI.this.mUpdatesListener.removeOnMixpanelUpdatesReceivedListener(listener);
        }

        private JSONObject stdPeopleMessage(String actionType, Object properties) throws JSONException {
            JSONObject dataObj = new JSONObject();
            String distinctId = getDistinctId();
            dataObj.put(actionType, properties);
            dataObj.put("$token", MixpanelAPI.this.mToken);
            dataObj.put("$time", System.currentTimeMillis());
            if (distinctId != null) {
                dataObj.put("$distinct_id", distinctId);
            }
            return dataObj;
        }

        @TargetApi(21)
        private void registerForPushIdAPI21AndUp(String senderID) {
            MixpanelAPI.this.mMessages.registerForGCM(senderID);
        }

        @TargetApi(19)
        private void registerForPushIdAPI19AndOlder(String senderID) {
            try {
                if (MPConfig.DEBUG) {
                    Log.v(MixpanelAPI.LOGTAG, "Registering a new push id");
                }
                Intent registrationIntent = new Intent("com.google.android.c2dm.intent.REGISTER");
                registrationIntent.putExtra(SettingsJsonConstants.APP_KEY, PendingIntent.getBroadcast(MixpanelAPI.this.mContext, 0, new Intent(), 0));
                registrationIntent.putExtra("sender", senderID);
                MixpanelAPI.this.mContext.startService(registrationIntent);
            } catch (SecurityException e) {
                Log.w(MixpanelAPI.LOGTAG, e);
            }
        }

        private void showGivenOrAvailableSurvey(Survey surveyOrNull, final Activity parent) {
            if (VERSION.SDK_INT < 16) {
                if (MPConfig.DEBUG) {
                    Log.v(MixpanelAPI.LOGTAG, "Will not show survey, os version is too low.");
                }
            } else if (ConfigurationChecker.checkSurveyActivityAvailable(parent.getApplicationContext())) {
                ReentrantLock lock = UpdateDisplayState.getLockObject();
                lock.lock();
                try {
                    if (!UpdateDisplayState.hasCurrentProposal()) {
                        Survey toShow = surveyOrNull;
                        if (toShow == null) {
                            toShow = getSurveyIfAvailable();
                        }
                        if (toShow == null) {
                            lock.unlock();
                            return;
                        }
                        final SurveyState surveyDisplay = new SurveyState(toShow);
                        final int intentId = UpdateDisplayState.proposeDisplay(surveyDisplay, getDistinctId(), MixpanelAPI.this.mToken);
                        if (intentId <= 0) {
                            Log.e(MixpanelAPI.LOGTAG, "DisplayState Lock is in an inconsistent state! Please report this issue to Mixpanel");
                            lock.unlock();
                            return;
                        }
                        OnBackgroundCapturedListener listener = new OnBackgroundCapturedListener() {
                            public void onBackgroundCaptured(Bitmap bitmapCaptured, int highlightColorCaptured) {
                                surveyDisplay.setBackground(bitmapCaptured);
                                surveyDisplay.setHighlightColor(highlightColorCaptured);
                                Intent surveyIntent = new Intent(parent.getApplicationContext(), SurveyActivity.class);
                                surveyIntent.addFlags(268435456);
                                surveyIntent.addFlags(AccessibilityNodeInfoCompat.ACTION_SET_SELECTION);
                                surveyIntent.putExtra(SurveyActivity.INTENT_ID_KEY, intentId);
                                parent.startActivity(surveyIntent);
                            }
                        };
                        lock.unlock();
                        BackgroundCapture.captureBackground(parent, listener);
                    }
                } finally {
                    lock.unlock();
                }
            } else if (MPConfig.DEBUG) {
                Log.v(MixpanelAPI.LOGTAG, "Will not show survey, application isn't configured appropriately.");
            }
        }

        private void showGivenOrAvailableNotification(final InAppNotification notifOrNull, final Activity parent) {
            if (VERSION.SDK_INT >= 16) {
                parent.runOnUiThread(new Runnable() {
                    @TargetApi(16)
                    public void run() {
                        ReentrantLock lock = UpdateDisplayState.getLockObject();
                        lock.lock();
                        try {
                            if (UpdateDisplayState.hasCurrentProposal()) {
                                if (MPConfig.DEBUG) {
                                    Log.v(MixpanelAPI.LOGTAG, "DisplayState is locked, will not show notifications.");
                                }
                                lock.unlock();
                                return;
                            }
                            InAppNotification toShow = notifOrNull;
                            if (toShow == null) {
                                toShow = PeopleImpl.this.getNotificationIfAvailable();
                            }
                            if (toShow == null) {
                                if (MPConfig.DEBUG) {
                                    Log.v(MixpanelAPI.LOGTAG, "No notification available, will not show.");
                                }
                                lock.unlock();
                                return;
                            }
                            Type inAppType = toShow.getType();
                            if (inAppType != Type.TAKEOVER || ConfigurationChecker.checkSurveyActivityAvailable(parent.getApplicationContext())) {
                                int intentId = UpdateDisplayState.proposeDisplay(new InAppNotificationState(toShow, ActivityImageUtils.getHighlightColorFromBackground(parent)), PeopleImpl.this.getDistinctId(), MixpanelAPI.this.mToken);
                                if (intentId <= 0) {
                                    Log.e(MixpanelAPI.LOGTAG, "DisplayState Lock in inconsistent state! Please report this issue to Mixpanel");
                                    lock.unlock();
                                    return;
                                }
                                switch (AnonymousClass3.$SwitchMap$com$mixpanel$android$mpmetrics$InAppNotification$Type[inAppType.ordinal()]) {
                                    case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                                        UpdateDisplayState claimed = UpdateDisplayState.claimDisplayState(intentId);
                                        if (claimed != null) {
                                            InAppFragment inapp = new InAppFragment();
                                            inapp.setDisplayState(MixpanelAPI.this, intentId, (InAppNotificationState) claimed.getDisplayState());
                                            inapp.setRetainInstance(true);
                                            if (MPConfig.DEBUG) {
                                                Log.v(MixpanelAPI.LOGTAG, "Attempting to show mini notification.");
                                            }
                                            FragmentTransaction transaction = parent.getFragmentManager().beginTransaction();
                                            transaction.setCustomAnimations(0, R.anim.com_mixpanel_android_slide_down);
                                            transaction.add(16908290, inapp);
                                            transaction.commit();
                                            break;
                                        }
                                        if (MPConfig.DEBUG) {
                                            Log.v(MixpanelAPI.LOGTAG, "Notification's display proposal was already consumed, no notification will be shown.");
                                        }
                                        lock.unlock();
                                        return;
                                    case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                                        if (MPConfig.DEBUG) {
                                            Log.v(MixpanelAPI.LOGTAG, "Sending intent for takeover notification.");
                                        }
                                        Intent intent = new Intent(parent.getApplicationContext(), SurveyActivity.class);
                                        intent.addFlags(268435456);
                                        intent.addFlags(AccessibilityNodeInfoCompat.ACTION_SET_SELECTION);
                                        intent.putExtra(SurveyActivity.INTENT_ID_KEY, intentId);
                                        parent.startActivity(intent);
                                        break;
                                    default:
                                        Log.e(MixpanelAPI.LOGTAG, "Unrecognized notification type " + inAppType + " can't be shown");
                                        break;
                                }
                                if (!MixpanelAPI.this.mConfig.getTestMode()) {
                                    PeopleImpl.this.trackNotificationSeen(toShow);
                                }
                                lock.unlock();
                                return;
                            }
                            if (MPConfig.DEBUG) {
                                Log.v(MixpanelAPI.LOGTAG, "Application is not configured to show takeover notifications, none will be shown.");
                            }
                            lock.unlock();
                        } catch (Throwable th) {
                            lock.unlock();
                        }
                    }
                });
            } else if (MPConfig.DEBUG) {
                Log.v(MixpanelAPI.LOGTAG, "Will not show notifications, os version is too low.");
            }
        }
    }

    private interface UpdatesListener extends OnNewResultsListener {
        void addOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener onMixpanelUpdatesReceivedListener);

        void removeOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener onMixpanelUpdatesReceivedListener);
    }

    private class SupportedUpdatesListener implements UpdatesListener, Runnable {
        private final Executor mExecutor;
        private final Set<OnMixpanelUpdatesReceivedListener> mListeners;

        private SupportedUpdatesListener() {
            this.mListeners = new HashSet();
            this.mExecutor = Executors.newSingleThreadExecutor();
        }

        public void onNewResults() {
            this.mExecutor.execute(this);
        }

        public synchronized void addOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener listener) {
            if (MixpanelAPI.this.mDecideMessages.hasUpdatesAvailable()) {
                onNewResults();
            }
            this.mListeners.add(listener);
        }

        public synchronized void removeOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener listener) {
            this.mListeners.remove(listener);
        }

        public synchronized void run() {
            for (OnMixpanelUpdatesReceivedListener listener : this.mListeners) {
                listener.onMixpanelUpdatesReceived();
            }
        }
    }

    private class UnsupportedUpdatesFromMixpanel implements UpdatesFromMixpanel {
        private final Tweaks mTweaks;

        public UnsupportedUpdatesFromMixpanel(Tweaks tweaks) {
            this.mTweaks = tweaks;
        }

        public void startUpdates() {
        }

        public void setEventBindings(JSONArray bindings) {
        }

        public void setVariants(JSONArray variants) {
        }

        public Tweaks getTweaks() {
            return this.mTweaks;
        }
    }

    private class UnsupportedUpdatesListener implements UpdatesListener {
        private UnsupportedUpdatesListener() {
        }

        public void onNewResults() {
        }

        public void addOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener listener) {
        }

        public void removeOnMixpanelUpdatesReceivedListener(OnMixpanelUpdatesReceivedListener listener) {
        }
    }

    public static Tweak<String> stringTweak(String tweakName, String defaultValue) {
        return sSharedTweaks.stringTweak(tweakName, defaultValue);
    }

    public static Tweak<Boolean> booleanTweak(String tweakName, boolean defaultValue) {
        return sSharedTweaks.booleanTweak(tweakName, defaultValue);
    }

    public static Tweak<Double> doubleTweak(String tweakName, double defaultValue) {
        return sSharedTweaks.doubleTweak(tweakName, defaultValue);
    }

    public static Tweak<Float> floatTweak(String tweakName, float defaultValue) {
        return sSharedTweaks.floatTweak(tweakName, defaultValue);
    }

    public static Tweak<Long> longTweak(String tweakName, long defaultValue) {
        return sSharedTweaks.longTweak(tweakName, defaultValue);
    }

    public static Tweak<Integer> intTweak(String tweakName, int defaultValue) {
        return sSharedTweaks.intTweak(tweakName, defaultValue);
    }

    public static Tweak<Short> shortTweak(String tweakName, short defaultValue) {
        return sSharedTweaks.shortTweak(tweakName, defaultValue);
    }

    public static Tweak<Byte> byteTweak(String tweakName, byte defaultValue) {
        return sSharedTweaks.byteTweak(tweakName, defaultValue);
    }

    MixpanelAPI(Context context, Future<SharedPreferences> referrerPreferences, String token) {
        this.mContext = context;
        this.mToken = token;
        Map<String, String> deviceInfo = new HashMap();
        deviceInfo.put("$android_lib_version", VERSION);
        deviceInfo.put("$android_os", "Android");
        deviceInfo.put("$android_os_version", VERSION.RELEASE == null ? "UNKNOWN" : VERSION.RELEASE);
        deviceInfo.put("$android_manufacturer", Build.MANUFACTURER == null ? "UNKNOWN" : Build.MANUFACTURER);
        deviceInfo.put("$android_brand", Build.BRAND == null ? "UNKNOWN" : Build.BRAND);
        deviceInfo.put("$android_model", Build.MODEL == null ? "UNKNOWN" : Build.MODEL);
        try {
            PackageInfo info = this.mContext.getPackageManager().getPackageInfo(this.mContext.getPackageName(), 0);
            deviceInfo.put("$android_app_version", info.versionName);
            deviceInfo.put("$android_app_version_code", Integer.toString(info.versionCode));
        } catch (NameNotFoundException e) {
            Log.e(LOGTAG, "Exception getting app version name", e);
        }
        this.mDeviceInfo = Collections.unmodifiableMap(deviceInfo);
        this.mUpdatesFromMixpanel = constructUpdatesFromMixpanel(context, token);
        this.mTrackingDebug = constructTrackingDebug();
        this.mPersistentIdentity = getPersistentIdentity(context, referrerPreferences, token);
        this.mUpdatesListener = constructUpdatesListener();
        this.mDecideMessages = constructDecideUpdates(token, this.mUpdatesListener, this.mUpdatesFromMixpanel);
        String decideId = this.mPersistentIdentity.getPeopleDistinctId();
        if (decideId == null) {
            decideId = this.mPersistentIdentity.getEventsDistinctId();
        }
        this.mDecideMessages.setDistinctId(decideId);
        this.mMessages = getAnalyticsMessages();
        this.mMessages.installDecideCheck(this.mDecideMessages);
        registerMixpanelActivityLifecycleCallbacks();
        if (sendAppOpen()) {
            track("$app_open", null);
        }
        this.mUpdatesFromMixpanel.startUpdates();
    }

    public static MixpanelAPI getInstance(Context context, String token) {
        MixpanelAPI mixpanelAPI = null;
        if (!(token == null || context == null)) {
            synchronized (sInstanceMap) {
                Context appContext = context.getApplicationContext();
                if (sReferrerPrefs == null) {
                    sReferrerPrefs = sPrefsLoader.loadPreferences(context, "com.mixpanel.android.mpmetrics.ReferralInfo", null);
                }
                Map<Context, MixpanelAPI> instances = (Map) sInstanceMap.get(token);
                if (instances == null) {
                    instances = new HashMap();
                    sInstanceMap.put(token, instances);
                }
                mixpanelAPI = (MixpanelAPI) instances.get(appContext);
                if (mixpanelAPI == null && ConfigurationChecker.checkBasicConfiguration(appContext)) {
                    mixpanelAPI = new MixpanelAPI(appContext, sReferrerPrefs, token);
                    registerAppLinksListeners(context, mixpanelAPI);
                    instances.put(appContext, mixpanelAPI);
                }
                checkIntentForInboundAppLink(context);
            }
        }
        return mixpanelAPI;
    }

    @Deprecated
    public static void setFlushInterval(Context context, long milliseconds) {
        Log.i(LOGTAG, "MixpanelAPI.setFlushInterval is deprecated. Calling is now a no-op.\n    To set a custom Mixpanel flush interval for your application, add\n    <meta-data android:name=\"com.mixpanel.android.MPConfig.FlushInterval\" android:value=\"YOUR_INTERVAL\" />\n    to the <application> section of your AndroidManifest.xml.");
    }

    @Deprecated
    public static void enableFallbackServer(Context context, boolean enableIfTrue) {
        Log.i(LOGTAG, "MixpanelAPI.enableFallbackServer is deprecated. This call is a no-op.\n    To enable fallback in your application, add\n    <meta-data android:name=\"com.mixpanel.android.MPConfig.DisableFallback\" android:value=\"false\" />\n    to the <application> section of your AndroidManifest.xml.");
    }

    public void alias(String alias, String original) {
        if (original == null) {
            original = getDistinctId();
        }
        if (alias.equals(original)) {
            Log.w(LOGTAG, "Attempted to alias identical distinct_ids " + alias + ". Alias message will not be sent.");
            return;
        }
        try {
            JSONObject j = new JSONObject();
            j.put("alias", alias);
            j.put("original", original);
            track("$create_alias", j);
        } catch (JSONException e) {
            Log.e(LOGTAG, "Failed to alias", e);
        }
        flush();
    }

    public void identify(String distinctId) {
        synchronized (this.mPersistentIdentity) {
            this.mPersistentIdentity.setEventsDistinctId(distinctId);
            String decideId = this.mPersistentIdentity.getPeopleDistinctId();
            if (decideId == null) {
                decideId = this.mPersistentIdentity.getEventsDistinctId();
            }
            this.mDecideMessages.setDistinctId(decideId);
        }
    }

    public void timeEvent(String eventName) {
        long writeTime = System.currentTimeMillis();
        synchronized (this.mEventTimings) {
            this.mEventTimings.put(eventName, Long.valueOf(writeTime));
        }
    }

    public void trackMap(String eventName, Map<String, Object> properties) {
        if (properties == null) {
            track(eventName, null);
            return;
        }
        try {
            track(eventName, new JSONObject(properties));
        } catch (NullPointerException e) {
            Log.w(LOGTAG, "Can't have null keys in the properties of trackMap!");
        }
    }

    public void track(String eventName, JSONObject properties) {
        synchronized (this.mEventTimings) {
            Long eventBegin = (Long) this.mEventTimings.get(eventName);
            this.mEventTimings.remove(eventName);
        }
        try {
            JSONObject messageProps = new JSONObject();
            for (Entry<String, String> entry : this.mPersistentIdentity.getReferrerProperties().entrySet()) {
                messageProps.put((String) entry.getKey(), (String) entry.getValue());
            }
            this.mPersistentIdentity.addSuperPropertiesToObject(messageProps);
            double timeSecondsDouble = ((double) System.currentTimeMillis()) / 1000.0d;
            messageProps.put("time", (long) timeSecondsDouble);
            messageProps.put("distinct_id", getDistinctId());
            if (eventBegin != null) {
                messageProps.put("$duration", timeSecondsDouble - (((double) eventBegin.longValue()) / 1000.0d));
            }
            if (properties != null) {
                Iterator<?> propIter = properties.keys();
                while (propIter.hasNext()) {
                    String key = (String) propIter.next();
                    messageProps.put(key, properties.get(key));
                }
            }
            this.mMessages.eventsMessage(new EventDescription(eventName, messageProps, this.mToken));
            if (this.mTrackingDebug != null) {
                this.mTrackingDebug.reportTrack(eventName);
            }
        } catch (JSONException e) {
            Log.e(LOGTAG, "Exception tracking event " + eventName, e);
        }
    }

    public void track(String eventName) {
        track(eventName, null);
    }

    public void flush() {
        this.mMessages.postToServer();
    }

    public JSONObject getSuperProperties() {
        JSONObject ret = new JSONObject();
        this.mPersistentIdentity.addSuperPropertiesToObject(ret);
        return ret;
    }

    public String getDistinctId() {
        return this.mPersistentIdentity.getEventsDistinctId();
    }

    public void registerSuperPropertiesMap(Map<String, Object> superProperties) {
        if (superProperties == null) {
            Log.e(LOGTAG, "registerSuperPropertiesMap does not accept null properties");
            return;
        }
        try {
            registerSuperProperties(new JSONObject(superProperties));
        } catch (NullPointerException e) {
            Log.w(LOGTAG, "Can't have null keys in the properties of registerSuperPropertiesMap!");
        }
    }

    public void registerSuperProperties(JSONObject superProperties) {
        this.mPersistentIdentity.registerSuperProperties(superProperties);
    }

    public void unregisterSuperProperty(String superPropertyName) {
        this.mPersistentIdentity.unregisterSuperProperty(superPropertyName);
    }

    public void registerSuperPropertiesOnceMap(Map<String, Object> superProperties) {
        if (superProperties == null) {
            Log.e(LOGTAG, "registerSuperPropertiesOnceMap does not accept null properties");
            return;
        }
        try {
            registerSuperPropertiesOnce(new JSONObject(superProperties));
        } catch (NullPointerException e) {
            Log.w(LOGTAG, "Can't have null keys in the properties of registerSuperPropertiesOnce!");
        }
    }

    public void registerSuperPropertiesOnce(JSONObject superProperties) {
        this.mPersistentIdentity.registerSuperPropertiesOnce(superProperties);
    }

    public void clearSuperProperties() {
        this.mPersistentIdentity.clearSuperProperties();
    }

    public void updateSuperProperties(SuperPropertyUpdate update) {
        this.mPersistentIdentity.updateSuperProperties(update);
    }

    public People getPeople() {
        return this.mPeople;
    }

    public void reset() {
        this.mPersistentIdentity.clearPreferences();
    }

    public Map<String, String> getDeviceInfo() {
        return this.mDeviceInfo;
    }

    @Deprecated
    public void logPosts() {
        Log.i(LOGTAG, "MixpanelAPI.logPosts() is deprecated.\n    To get verbose debug level logging, add\n    <meta-data android:name=\"com.mixpanel.android.MPConfig.EnableDebugLogging\" value=\"true\" />\n    to the <application> section of your AndroidManifest.xml.");
    }

    @TargetApi(16)
    void registerMixpanelActivityLifecycleCallbacks() {
        if (VERSION.SDK_INT >= 16 && this.mConfig.getAutoShowMixpanelUpdates()) {
            if (this.mContext.getApplicationContext() instanceof Application) {
                ((Application) this.mContext.getApplicationContext()).registerActivityLifecycleCallbacks(new MixpanelActivityLifecycleCallbacks(this));
            } else {
                Log.i(LOGTAG, "Context is not an Application, Mixpanel will not automatically show surveys, in-app notifications, or A/B test experiments.");
            }
        }
    }

    static void allInstances(InstanceProcessor processor) {
        synchronized (sInstanceMap) {
            for (Map<Context, MixpanelAPI> contextInstances : sInstanceMap.values()) {
                for (MixpanelAPI instance : contextInstances.values()) {
                    processor.process(instance);
                }
            }
        }
    }

    AnalyticsMessages getAnalyticsMessages() {
        return AnalyticsMessages.getInstance(this.mContext);
    }

    MPConfig getConfig() {
        return MPConfig.getInstance(this.mContext);
    }

    PersistentIdentity getPersistentIdentity(Context context, Future<SharedPreferences> referrerPreferences, String token) {
        OnPrefsLoadedListener listener = new OnPrefsLoadedListener() {
            public void onPrefsLoaded(SharedPreferences preferences) {
                JSONArray records = PersistentIdentity.waitingPeopleRecordsForSending(preferences);
                if (records != null) {
                    MixpanelAPI.this.sendAllPeopleRecords(records);
                }
            }
        };
        return new PersistentIdentity(referrerPreferences, sPrefsLoader.loadPreferences(context, "com.mixpanel.android.mpmetrics.MixpanelAPI_" + token, listener));
    }

    DecideMessages constructDecideUpdates(String token, OnNewResultsListener listener, UpdatesFromMixpanel updatesFromMixpanel) {
        return new DecideMessages(token, listener, updatesFromMixpanel);
    }

    UpdatesListener constructUpdatesListener() {
        if (VERSION.SDK_INT >= 16) {
            return new SupportedUpdatesListener();
        }
        Log.i(LOGTAG, "Surveys and Notifications are not supported on this Android OS Version");
        return new UnsupportedUpdatesListener();
    }

    UpdatesFromMixpanel constructUpdatesFromMixpanel(Context context, String token) {
        if (VERSION.SDK_INT >= 16) {
            return new ViewCrawler(this.mContext, this.mToken, this, sSharedTweaks);
        }
        Log.i(LOGTAG, "Web Configuration, A/B Testing, and Dynamic Tweaks are not supported on this Android OS Version");
        return new UnsupportedUpdatesFromMixpanel(sSharedTweaks);
    }

    TrackingDebug constructTrackingDebug() {
        if (this.mUpdatesFromMixpanel instanceof ViewCrawler) {
            return (TrackingDebug) this.mUpdatesFromMixpanel;
        }
        return null;
    }

    boolean sendAppOpen() {
        return !this.mConfig.getDisableAppOpenEvent();
    }

    private void recordPeopleMessage(JSONObject message) {
        if (message.has("$distinct_id")) {
            this.mMessages.peopleMessage(message);
        } else {
            this.mPersistentIdentity.storeWaitingPeopleRecord(message);
        }
    }

    private void pushWaitingPeopleRecord() {
        JSONArray records = this.mPersistentIdentity.waitingPeopleRecordsForSending();
        if (records != null) {
            sendAllPeopleRecords(records);
        }
    }

    private void sendAllPeopleRecords(JSONArray records) {
        for (int i = 0; i < records.length(); i++) {
            try {
                this.mMessages.peopleMessage(records.getJSONObject(i));
            } catch (JSONException e) {
                Log.e(LOGTAG, "Malformed people record stored pending identity, will not send it.", e);
            }
        }
    }

    private static void registerAppLinksListeners(Context context, final MixpanelAPI mixpanel) {
        try {
            Class<?> clazz = Class.forName("android.support.v4.content.LocalBroadcastManager");
            Method methodGetInstance = clazz.getMethod("getInstance", new Class[]{Context.class});
            clazz.getMethod("registerReceiver", new Class[]{BroadcastReceiver.class, IntentFilter.class}).invoke(methodGetInstance.invoke(null, new Object[]{context}), new Object[]{new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    JSONObject properties = new JSONObject();
                    Bundle args = intent.getBundleExtra(MeasurementEvent.MEASUREMENT_EVENT_ARGS_KEY);
                    if (args != null) {
                        for (String key : args.keySet()) {
                            try {
                                properties.put(key, args.get(key));
                            } catch (JSONException e) {
                                Log.e(MixpanelAPI.APP_LINKS_LOGTAG, "failed to add key \"" + key + "\" to properties for tracking bolts event", e);
                            }
                        }
                    }
                    mixpanel.track("$" + intent.getStringExtra(MeasurementEvent.MEASUREMENT_EVENT_NAME_KEY), properties);
                }
            }, new IntentFilter(MeasurementEvent.MEASUREMENT_EVENT_NOTIFICATION_NAME)});
        } catch (InvocationTargetException e) {
            Log.d(APP_LINKS_LOGTAG, "Failed to invoke LocalBroadcastManager.registerReceiver() -- App Links tracking will not be enabled due to this exception", e);
        } catch (ClassNotFoundException e2) {
            Log.d(APP_LINKS_LOGTAG, "To enable App Links tracking android.support.v4 must be installed: " + e2.getMessage());
        } catch (NoSuchMethodException e3) {
            Log.d(APP_LINKS_LOGTAG, "To enable App Links tracking android.support.v4 must be installed: " + e3.getMessage());
        } catch (IllegalAccessException e4) {
            Log.d(APP_LINKS_LOGTAG, "App Links tracking will not be enabled due to this exception: " + e4.getMessage());
        }
    }

    private static void checkIntentForInboundAppLink(Context context) {
        if (context instanceof Activity) {
            try {
                Class<?> clazz = Class.forName("bolts.AppLinks");
                Intent intent = ((Activity) context).getIntent();
                clazz.getMethod("getTargetUrlFromInboundIntent", new Class[]{Context.class, Intent.class}).invoke(null, new Object[]{context, intent});
                return;
            } catch (InvocationTargetException e) {
                Log.d(APP_LINKS_LOGTAG, "Failed to invoke bolts.AppLinks.getTargetUrlFromInboundIntent() -- Unable to detect inbound App Links", e);
                return;
            } catch (ClassNotFoundException e2) {
                Log.d(APP_LINKS_LOGTAG, "Please install the Bolts library >= 1.1.2 to track App Links: " + e2.getMessage());
                return;
            } catch (NoSuchMethodException e3) {
                Log.d(APP_LINKS_LOGTAG, "Please install the Bolts library >= 1.1.2 to track App Links: " + e3.getMessage());
                return;
            } catch (IllegalAccessException e4) {
                Log.d(APP_LINKS_LOGTAG, "Unable to detect inbound App Links: " + e4.getMessage());
                return;
            }
        }
        Log.d(APP_LINKS_LOGTAG, "Context is not an instance of Activity. To detect inbound App Links, pass an instance of an Activity to getInstance.");
    }
}
