package com.facebook.appevents;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import bolts.AppLinks;
import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.Callback;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.internal.AppEventsLoggerUtility;
import com.facebook.internal.AppEventsLoggerUtility.GraphAPIActivityType;
import com.facebook.internal.AttributionIdentifiers;
import com.facebook.internal.Logger;
import com.facebook.internal.ServerProtocol;
import com.facebook.internal.Utility;
import com.facebook.internal.Utility.FetchedAppSettings;
import com.facebook.internal.Validate;
import com.helpshift.res.values.HSConsts;
import com.mobcrush.mobcrush.Constants;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppEventsLogger {
    public static final String ACTION_APP_EVENTS_FLUSHED = "com.facebook.sdk.APP_EVENTS_FLUSHED";
    public static final String APP_EVENTS_EXTRA_FLUSH_RESULT = "com.facebook.sdk.APP_EVENTS_FLUSH_RESULT";
    public static final String APP_EVENTS_EXTRA_NUM_EVENTS_FLUSHED = "com.facebook.sdk.APP_EVENTS_NUM_EVENTS_FLUSHED";
    public static final String APP_EVENT_PREFERENCES = "com.facebook.sdk.appEventPreferences";
    private static final int APP_SUPPORTS_ATTRIBUTION_ID_RECHECK_PERIOD_IN_SECONDS = 86400;
    private static final int FLUSH_APP_SESSION_INFO_IN_SECONDS = 30;
    private static final int FLUSH_PERIOD_IN_SECONDS = 15;
    private static final int NUM_LOG_EVENTS_TO_TRY_TO_FLUSH_AFTER = 100;
    private static final String SOURCE_APPLICATION_HAS_BEEN_SET_BY_THIS_INTENT = "_fbSourceApplicationHasBeenSet";
    private static final String TAG = AppEventsLogger.class.getCanonicalName();
    private static String anonymousAppDeviceGUID;
    private static Context applicationContext;
    private static ScheduledThreadPoolExecutor backgroundExecutor;
    private static FlushBehavior flushBehavior = FlushBehavior.AUTO;
    private static boolean isOpenedByApplink;
    private static boolean requestInFlight;
    private static String sourceApplication;
    private static Map<AccessTokenAppIdPair, SessionEventsState> stateMap = new ConcurrentHashMap();
    private static Object staticLock = new Object();
    private final AccessTokenAppIdPair accessTokenAppId;
    private final String contextName;

    private static class AccessTokenAppIdPair implements Serializable {
        private static final long serialVersionUID = 1;
        private final String accessTokenString;
        private final String applicationId;

        private static class SerializationProxyV1 implements Serializable {
            private static final long serialVersionUID = -2488473066578201069L;
            private final String accessTokenString;
            private final String appId;

            private SerializationProxyV1(String accessTokenString, String appId) {
                this.accessTokenString = accessTokenString;
                this.appId = appId;
            }

            private Object readResolve() {
                return new AccessTokenAppIdPair(this.accessTokenString, this.appId);
            }
        }

        AccessTokenAppIdPair(AccessToken accessToken) {
            this(accessToken.getToken(), FacebookSdk.getApplicationId());
        }

        AccessTokenAppIdPair(String accessTokenString, String applicationId) {
            if (Utility.isNullOrEmpty(accessTokenString)) {
                accessTokenString = null;
            }
            this.accessTokenString = accessTokenString;
            this.applicationId = applicationId;
        }

        String getAccessTokenString() {
            return this.accessTokenString;
        }

        String getApplicationId() {
            return this.applicationId;
        }

        public int hashCode() {
            int i = 0;
            int hashCode = this.accessTokenString == null ? 0 : this.accessTokenString.hashCode();
            if (this.applicationId != null) {
                i = this.applicationId.hashCode();
            }
            return hashCode ^ i;
        }

        public boolean equals(Object o) {
            if (!(o instanceof AccessTokenAppIdPair)) {
                return false;
            }
            AccessTokenAppIdPair p = (AccessTokenAppIdPair) o;
            if (Utility.areObjectsEqual(p.accessTokenString, this.accessTokenString) && Utility.areObjectsEqual(p.applicationId, this.applicationId)) {
                return true;
            }
            return false;
        }

        private Object writeReplace() {
            return new SerializationProxyV1(this.accessTokenString, this.applicationId);
        }
    }

    static class AppEvent implements Serializable {
        private static final long serialVersionUID = 1;
        private static final HashSet<String> validatedIdentifiers = new HashSet();
        private boolean isImplicit;
        private JSONObject jsonObject;
        private String name;

        private static class SerializationProxyV1 implements Serializable {
            private static final long serialVersionUID = -2488473066578201069L;
            private final boolean isImplicit;
            private final String jsonString;

            private SerializationProxyV1(String jsonString, boolean isImplicit) {
                this.jsonString = jsonString;
                this.isImplicit = isImplicit;
            }

            private Object readResolve() throws JSONException {
                return new AppEvent(this.jsonString, this.isImplicit);
            }
        }

        public AppEvent(String contextName, String eventName, Double valueToSum, Bundle parameters, boolean isImplicitlyLogged) {
            try {
                validateIdentifier(eventName);
                this.name = eventName;
                this.isImplicit = isImplicitlyLogged;
                this.jsonObject = new JSONObject();
                this.jsonObject.put("_eventName", eventName);
                this.jsonObject.put("_logTime", System.currentTimeMillis() / 1000);
                this.jsonObject.put("_ui", contextName);
                if (valueToSum != null) {
                    this.jsonObject.put("_valueToSum", valueToSum.doubleValue());
                }
                if (this.isImplicit) {
                    this.jsonObject.put("_implicitlyLogged", HSConsts.STATUS_INPROGRESS);
                }
                if (parameters != null) {
                    for (String key : parameters.keySet()) {
                        validateIdentifier(key);
                        Object value = parameters.get(key);
                        if ((value instanceof String) || (value instanceof Number)) {
                            this.jsonObject.put(key, value.toString());
                        } else {
                            throw new FacebookException(String.format("Parameter value '%s' for key '%s' should be a string or a numeric type.", new Object[]{value, key}));
                        }
                    }
                }
                if (!this.isImplicit) {
                    Logger.log(LoggingBehavior.APP_EVENTS, "AppEvents", "Created app event '%s'", this.jsonObject.toString());
                }
            } catch (JSONException jsonException) {
                Logger.log(LoggingBehavior.APP_EVENTS, "AppEvents", "JSON encoding for app event failed: '%s'", jsonException.toString());
                this.jsonObject = null;
            } catch (FacebookException e) {
                Logger.log(LoggingBehavior.APP_EVENTS, "AppEvents", "Invalid app event name or parameter:", e.toString());
                this.jsonObject = null;
            }
        }

        public String getName() {
            return this.name;
        }

        private AppEvent(String jsonString, boolean isImplicit) throws JSONException {
            this.jsonObject = new JSONObject(jsonString);
            this.isImplicit = isImplicit;
        }

        public boolean getIsImplicit() {
            return this.isImplicit;
        }

        public JSONObject getJSONObject() {
            return this.jsonObject;
        }

        private void validateIdentifier(String identifier) throws FacebookException {
            String regex = "^[0-9a-zA-Z_]+[0-9a-zA-Z _-]*$";
            if (identifier == null || identifier.length() == 0 || identifier.length() > 40) {
                if (identifier == null) {
                    identifier = "<None Provided>";
                }
                throw new FacebookException(String.format(Locale.ROOT, "Identifier '%s' must be less than %d characters", new Object[]{identifier, Integer.valueOf(40)}));
            }
            synchronized (validatedIdentifiers) {
                boolean alreadyValidated = validatedIdentifiers.contains(identifier);
            }
            if (!alreadyValidated) {
                if (identifier.matches("^[0-9a-zA-Z_]+[0-9a-zA-Z _-]*$")) {
                    synchronized (validatedIdentifiers) {
                        validatedIdentifiers.add(identifier);
                    }
                    return;
                }
                throw new FacebookException(String.format("Skipping event named '%s' due to illegal name - must be under 40 chars and alphanumeric, _, - or space, and not start with a space or hyphen.", new Object[]{identifier}));
            }
        }

        private Object writeReplace() {
            return new SerializationProxyV1(this.jsonObject.toString(), this.isImplicit);
        }

        public String toString() {
            return String.format("\"%s\", implicit: %b, json: %s", new Object[]{this.jsonObject.optString("_eventName"), Boolean.valueOf(this.isImplicit), this.jsonObject.toString()});
        }
    }

    public enum FlushBehavior {
        AUTO,
        EXPLICIT_ONLY
    }

    private enum FlushReason {
        EXPLICIT,
        TIMER,
        SESSION_CHANGE,
        PERSISTED_EVENTS,
        EVENT_THRESHOLD,
        EAGER_FLUSHING_EVENT
    }

    private enum FlushResult {
        SUCCESS,
        SERVER_ERROR,
        NO_CONNECTIVITY,
        UNKNOWN_ERROR
    }

    private static class FlushStatistics {
        public int numEvents;
        public FlushResult result;

        private FlushStatistics() {
            this.numEvents = 0;
            this.result = FlushResult.SUCCESS;
        }
    }

    static class PersistedAppSessionInfo {
        private static final String PERSISTED_SESSION_INFO_FILENAME = "AppEventsLogger.persistedsessioninfo";
        private static final Runnable appSessionInfoFlushRunnable = new Runnable() {
            public void run() {
                PersistedAppSessionInfo.saveAppSessionInformation(AppEventsLogger.applicationContext);
            }
        };
        private static Map<AccessTokenAppIdPair, FacebookTimeSpentData> appSessionInfoMap;
        private static boolean hasChanges = false;
        private static boolean isLoaded = false;
        private static final Object staticLock = new Object();

        PersistedAppSessionInfo() {
        }

        private static void restoreAppSessionInformation(Context context) {
            Throwable th;
            Exception e;
            ObjectInputStream ois = null;
            synchronized (staticLock) {
                if (!isLoaded) {
                    try {
                        ObjectInputStream ois2 = new ObjectInputStream(context.openFileInput(PERSISTED_SESSION_INFO_FILENAME));
                        try {
                            appSessionInfoMap = (HashMap) ois2.readObject();
                            Logger.log(LoggingBehavior.APP_EVENTS, "AppEvents", "App session info loaded");
                            try {
                                Utility.closeQuietly(ois2);
                                context.deleteFile(PERSISTED_SESSION_INFO_FILENAME);
                                if (appSessionInfoMap == null) {
                                    appSessionInfoMap = new HashMap();
                                }
                                isLoaded = true;
                                hasChanges = false;
                                ois = ois2;
                            } catch (Throwable th2) {
                                th = th2;
                                ois = ois2;
                                throw th;
                            }
                        } catch (FileNotFoundException e2) {
                            ois = ois2;
                            Utility.closeQuietly(ois);
                            context.deleteFile(PERSISTED_SESSION_INFO_FILENAME);
                            if (appSessionInfoMap == null) {
                                appSessionInfoMap = new HashMap();
                            }
                            isLoaded = true;
                            hasChanges = false;
                        } catch (Exception e3) {
                            e = e3;
                            ois = ois2;
                            try {
                                Log.d(AppEventsLogger.TAG, "Got unexpected exception: " + e.toString());
                                Utility.closeQuietly(ois);
                                context.deleteFile(PERSISTED_SESSION_INFO_FILENAME);
                                if (appSessionInfoMap == null) {
                                    appSessionInfoMap = new HashMap();
                                }
                                isLoaded = true;
                                hasChanges = false;
                            } catch (Throwable th3) {
                                th = th3;
                                Utility.closeQuietly(ois);
                                context.deleteFile(PERSISTED_SESSION_INFO_FILENAME);
                                if (appSessionInfoMap == null) {
                                    appSessionInfoMap = new HashMap();
                                }
                                isLoaded = true;
                                hasChanges = false;
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            ois = ois2;
                            Utility.closeQuietly(ois);
                            context.deleteFile(PERSISTED_SESSION_INFO_FILENAME);
                            if (appSessionInfoMap == null) {
                                appSessionInfoMap = new HashMap();
                            }
                            isLoaded = true;
                            hasChanges = false;
                            throw th;
                        }
                    } catch (FileNotFoundException e4) {
                        Utility.closeQuietly(ois);
                        context.deleteFile(PERSISTED_SESSION_INFO_FILENAME);
                        if (appSessionInfoMap == null) {
                            appSessionInfoMap = new HashMap();
                        }
                        isLoaded = true;
                        hasChanges = false;
                    } catch (Exception e5) {
                        e = e5;
                        Log.d(AppEventsLogger.TAG, "Got unexpected exception: " + e.toString());
                        Utility.closeQuietly(ois);
                        context.deleteFile(PERSISTED_SESSION_INFO_FILENAME);
                        if (appSessionInfoMap == null) {
                            appSessionInfoMap = new HashMap();
                        }
                        isLoaded = true;
                        hasChanges = false;
                    } catch (Throwable th5) {
                        th = th5;
                        throw th;
                    }
                }
            }
        }

        static void saveAppSessionInformation(Context context) {
            Throwable th;
            Exception e;
            ObjectOutputStream oos = null;
            synchronized (staticLock) {
                try {
                    if (hasChanges) {
                        try {
                            ObjectOutputStream oos2 = new ObjectOutputStream(new BufferedOutputStream(context.openFileOutput(PERSISTED_SESSION_INFO_FILENAME, 0)));
                            try {
                                oos2.writeObject(appSessionInfoMap);
                                hasChanges = false;
                                Logger.log(LoggingBehavior.APP_EVENTS, "AppEvents", "App session info saved");
                                try {
                                    Utility.closeQuietly(oos2);
                                    oos = oos2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    oos = oos2;
                                    throw th;
                                }
                            } catch (Exception e2) {
                                e = e2;
                                oos = oos2;
                                try {
                                    Log.d(AppEventsLogger.TAG, "Got unexpected exception: " + e.toString());
                                    Utility.closeQuietly(oos);
                                } catch (Throwable th3) {
                                    th = th3;
                                    Utility.closeQuietly(oos);
                                    throw th;
                                }
                            } catch (Throwable th4) {
                                th = th4;
                                oos = oos2;
                                Utility.closeQuietly(oos);
                                throw th;
                            }
                        } catch (Exception e3) {
                            e = e3;
                            Log.d(AppEventsLogger.TAG, "Got unexpected exception: " + e.toString());
                            Utility.closeQuietly(oos);
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    throw th;
                }
            }
        }

        static void onResume(Context context, AccessTokenAppIdPair accessTokenAppId, AppEventsLogger logger, long eventTime, String sourceApplicationInfo) {
            synchronized (staticLock) {
                getTimeSpentData(context, accessTokenAppId).onResume(logger, eventTime, sourceApplicationInfo);
                onTimeSpentDataUpdate();
            }
        }

        static void onSuspend(Context context, AccessTokenAppIdPair accessTokenAppId, AppEventsLogger logger, long eventTime) {
            synchronized (staticLock) {
                getTimeSpentData(context, accessTokenAppId).onSuspend(logger, eventTime);
                onTimeSpentDataUpdate();
            }
        }

        private static FacebookTimeSpentData getTimeSpentData(Context context, AccessTokenAppIdPair accessTokenAppId) {
            restoreAppSessionInformation(context);
            FacebookTimeSpentData result = (FacebookTimeSpentData) appSessionInfoMap.get(accessTokenAppId);
            if (result != null) {
                return result;
            }
            result = new FacebookTimeSpentData();
            appSessionInfoMap.put(accessTokenAppId, result);
            return result;
        }

        private static void onTimeSpentDataUpdate() {
            if (!hasChanges) {
                hasChanges = true;
                AppEventsLogger.backgroundExecutor.schedule(appSessionInfoFlushRunnable, 30, TimeUnit.SECONDS);
            }
        }
    }

    static class PersistedEvents {
        static final String PERSISTED_EVENTS_FILENAME = "AppEventsLogger.persistedevents";
        private static Object staticLock = new Object();
        private Context context;
        private HashMap<AccessTokenAppIdPair, List<AppEvent>> persistedEvents = new HashMap();

        private PersistedEvents(Context context) {
            this.context = context;
        }

        public static PersistedEvents readAndClearStore(Context context) {
            PersistedEvents persistedEvents;
            synchronized (staticLock) {
                persistedEvents = new PersistedEvents(context);
                persistedEvents.readAndClearStore();
            }
            return persistedEvents;
        }

        public static void persistEvents(Context context, AccessTokenAppIdPair accessTokenAppId, SessionEventsState eventsToPersist) {
            Map<AccessTokenAppIdPair, SessionEventsState> map = new HashMap();
            map.put(accessTokenAppId, eventsToPersist);
            persistEvents(context, map);
        }

        public static void persistEvents(Context context, Map<AccessTokenAppIdPair, SessionEventsState> eventsToPersist) {
            synchronized (staticLock) {
                PersistedEvents persistedEvents = readAndClearStore(context);
                for (Entry<AccessTokenAppIdPair, SessionEventsState> entry : eventsToPersist.entrySet()) {
                    List<AppEvent> events = ((SessionEventsState) entry.getValue()).getEventsToPersist();
                    if (events.size() != 0) {
                        persistedEvents.addEvents((AccessTokenAppIdPair) entry.getKey(), events);
                    }
                }
                persistedEvents.write();
            }
        }

        public Set<AccessTokenAppIdPair> keySet() {
            return this.persistedEvents.keySet();
        }

        public List<AppEvent> getEvents(AccessTokenAppIdPair accessTokenAppId) {
            return (List) this.persistedEvents.get(accessTokenAppId);
        }

        private void write() {
            Exception e;
            Throwable th;
            ObjectOutputStream oos = null;
            try {
                ObjectOutputStream oos2 = new ObjectOutputStream(new BufferedOutputStream(this.context.openFileOutput(PERSISTED_EVENTS_FILENAME, 0)));
                try {
                    oos2.writeObject(this.persistedEvents);
                    Utility.closeQuietly(oos2);
                    oos = oos2;
                } catch (Exception e2) {
                    e = e2;
                    oos = oos2;
                    try {
                        Log.d(AppEventsLogger.TAG, "Got unexpected exception: " + e.toString());
                        Utility.closeQuietly(oos);
                    } catch (Throwable th2) {
                        th = th2;
                        Utility.closeQuietly(oos);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    oos = oos2;
                    Utility.closeQuietly(oos);
                    throw th;
                }
            } catch (Exception e3) {
                e = e3;
                Log.d(AppEventsLogger.TAG, "Got unexpected exception: " + e.toString());
                Utility.closeQuietly(oos);
            }
        }

        private void readAndClearStore() {
            Exception e;
            Throwable th;
            ObjectInputStream ois = null;
            try {
                ObjectInputStream ois2 = new ObjectInputStream(new BufferedInputStream(this.context.openFileInput(PERSISTED_EVENTS_FILENAME)));
                try {
                    HashMap<AccessTokenAppIdPair, List<AppEvent>> obj = (HashMap) ois2.readObject();
                    this.context.getFileStreamPath(PERSISTED_EVENTS_FILENAME).delete();
                    this.persistedEvents = obj;
                    Utility.closeQuietly(ois2);
                    ois = ois2;
                } catch (FileNotFoundException e2) {
                    ois = ois2;
                    Utility.closeQuietly(ois);
                } catch (Exception e3) {
                    e = e3;
                    ois = ois2;
                    try {
                        Log.d(AppEventsLogger.TAG, "Got unexpected exception: " + e.toString());
                        Utility.closeQuietly(ois);
                    } catch (Throwable th2) {
                        th = th2;
                        Utility.closeQuietly(ois);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    ois = ois2;
                    Utility.closeQuietly(ois);
                    throw th;
                }
            } catch (FileNotFoundException e4) {
                Utility.closeQuietly(ois);
            } catch (Exception e5) {
                e = e5;
                Log.d(AppEventsLogger.TAG, "Got unexpected exception: " + e.toString());
                Utility.closeQuietly(ois);
            }
        }

        public void addEvents(AccessTokenAppIdPair accessTokenAppId, List<AppEvent> eventsToPersist) {
            if (!this.persistedEvents.containsKey(accessTokenAppId)) {
                this.persistedEvents.put(accessTokenAppId, new ArrayList());
            }
            ((List) this.persistedEvents.get(accessTokenAppId)).addAll(eventsToPersist);
        }
    }

    static class SessionEventsState {
        public static final String ENCODED_EVENTS_KEY = "encoded_events";
        public static final String EVENT_COUNT_KEY = "event_count";
        public static final String NUM_SKIPPED_KEY = "num_skipped";
        private final int MAX_ACCUMULATED_LOG_EVENTS = Constants.UPDATE_COFIG_INTERVAL;
        private List<AppEvent> accumulatedEvents = new ArrayList();
        private String anonymousAppDeviceGUID;
        private AttributionIdentifiers attributionIdentifiers;
        private List<AppEvent> inFlightEvents = new ArrayList();
        private int numSkippedEventsDueToFullBuffer;
        private String packageName;

        public SessionEventsState(AttributionIdentifiers identifiers, String packageName, String anonymousGUID) {
            this.attributionIdentifiers = identifiers;
            this.packageName = packageName;
            this.anonymousAppDeviceGUID = anonymousGUID;
        }

        public synchronized void addEvent(AppEvent event) {
            if (this.accumulatedEvents.size() + this.inFlightEvents.size() >= Constants.UPDATE_COFIG_INTERVAL) {
                this.numSkippedEventsDueToFullBuffer++;
            } else {
                this.accumulatedEvents.add(event);
            }
        }

        public synchronized int getAccumulatedEventCount() {
            return this.accumulatedEvents.size();
        }

        public synchronized void clearInFlightAndStats(boolean moveToAccumulated) {
            if (moveToAccumulated) {
                this.accumulatedEvents.addAll(this.inFlightEvents);
            }
            this.inFlightEvents.clear();
            this.numSkippedEventsDueToFullBuffer = 0;
        }

        public int populateRequest(GraphRequest request, boolean includeImplicitEvents, boolean limitEventUsage) {
            synchronized (this) {
                int numSkipped = this.numSkippedEventsDueToFullBuffer;
                this.inFlightEvents.addAll(this.accumulatedEvents);
                this.accumulatedEvents.clear();
                JSONArray jsonArray = new JSONArray();
                for (AppEvent event : this.inFlightEvents) {
                    if (includeImplicitEvents || !event.getIsImplicit()) {
                        jsonArray.put(event.getJSONObject());
                    }
                }
                if (jsonArray.length() == 0) {
                    return 0;
                }
                populateRequest(request, numSkipped, jsonArray, limitEventUsage);
                return jsonArray.length();
            }
        }

        public synchronized List<AppEvent> getEventsToPersist() {
            List<AppEvent> result;
            result = this.accumulatedEvents;
            this.accumulatedEvents = new ArrayList();
            return result;
        }

        public synchronized void accumulatePersistedEvents(List<AppEvent> events) {
            this.accumulatedEvents.addAll(events);
        }

        private void populateRequest(GraphRequest request, int numSkipped, JSONArray events, boolean limitEventUsage) {
            JSONObject publishParams;
            try {
                publishParams = AppEventsLoggerUtility.getJSONObjectForGraphAPICall(GraphAPIActivityType.CUSTOM_APP_EVENTS, this.attributionIdentifiers, this.anonymousAppDeviceGUID, limitEventUsage, AppEventsLogger.applicationContext);
                if (this.numSkippedEventsDueToFullBuffer > 0) {
                    publishParams.put("num_skipped_events", numSkipped);
                }
            } catch (JSONException e) {
                publishParams = new JSONObject();
            }
            request.setGraphObject(publishParams);
            Bundle requestParameters = request.getParameters();
            if (requestParameters == null) {
                requestParameters = new Bundle();
            }
            String jsonString = events.toString();
            if (jsonString != null) {
                requestParameters.putByteArray("custom_events_file", getStringAsByteArray(jsonString));
                request.setTag(jsonString);
            }
            request.setParameters(requestParameters);
        }

        private byte[] getStringAsByteArray(String jsonString) {
            byte[] jsonUtf8 = null;
            try {
                jsonUtf8 = jsonString.getBytes(HTTP.UTF_8);
            } catch (Exception e) {
                Utility.logd("Encoding exception: ", e);
            }
            return jsonUtf8;
        }
    }

    public static void activateApp(Context context) {
        FacebookSdk.sdkInitialize(context);
        activateApp(context, Utility.getMetadataApplicationId(context));
    }

    public static void activateApp(Context context, String applicationId) {
        if (context == null || applicationId == null) {
            throw new IllegalArgumentException("Both context and applicationId must be non-null");
        }
        if (context instanceof Activity) {
            setSourceApplication((Activity) context);
        } else {
            resetSourceApplication();
            Log.d(AppEventsLogger.class.getName(), "To set source application the context of activateApp must be an instance of Activity");
        }
        FacebookSdk.publishInstallAsync(context, applicationId);
        final AppEventsLogger logger = new AppEventsLogger(context, applicationId, null);
        final long eventTime = System.currentTimeMillis();
        final String sourceApplicationInfo = getSourceApplication();
        backgroundExecutor.execute(new Runnable() {
            public void run() {
                logger.logAppSessionResumeEvent(eventTime, sourceApplicationInfo);
            }
        });
    }

    public static void deactivateApp(Context context) {
        deactivateApp(context, Utility.getMetadataApplicationId(context));
    }

    public static void deactivateApp(Context context, String applicationId) {
        if (context == null || applicationId == null) {
            throw new IllegalArgumentException("Both context and applicationId must be non-null");
        }
        resetSourceApplication();
        final AppEventsLogger logger = new AppEventsLogger(context, applicationId, null);
        final long eventTime = System.currentTimeMillis();
        backgroundExecutor.execute(new Runnable() {
            public void run() {
                logger.logAppSessionSuspendEvent(eventTime);
            }
        });
    }

    private void logAppSessionResumeEvent(long eventTime, String sourceApplicationInfo) {
        PersistedAppSessionInfo.onResume(applicationContext, this.accessTokenAppId, this, eventTime, sourceApplicationInfo);
    }

    private void logAppSessionSuspendEvent(long eventTime) {
        PersistedAppSessionInfo.onSuspend(applicationContext, this.accessTokenAppId, this, eventTime);
    }

    public static AppEventsLogger newLogger(Context context) {
        return new AppEventsLogger(context, null, null);
    }

    public static AppEventsLogger newLogger(Context context, AccessToken accessToken) {
        return new AppEventsLogger(context, null, accessToken);
    }

    public static AppEventsLogger newLogger(Context context, String applicationId, AccessToken accessToken) {
        return new AppEventsLogger(context, applicationId, accessToken);
    }

    public static AppEventsLogger newLogger(Context context, String applicationId) {
        return new AppEventsLogger(context, applicationId, null);
    }

    public static FlushBehavior getFlushBehavior() {
        FlushBehavior flushBehavior;
        synchronized (staticLock) {
            flushBehavior = flushBehavior;
        }
        return flushBehavior;
    }

    public static void setFlushBehavior(FlushBehavior flushBehavior) {
        synchronized (staticLock) {
            flushBehavior = flushBehavior;
        }
    }

    public void logEvent(String eventName) {
        logEvent(eventName, null);
    }

    public void logEvent(String eventName, double valueToSum) {
        logEvent(eventName, valueToSum, null);
    }

    public void logEvent(String eventName, Bundle parameters) {
        logEvent(eventName, null, parameters, false);
    }

    public void logEvent(String eventName, double valueToSum, Bundle parameters) {
        logEvent(eventName, Double.valueOf(valueToSum), parameters, false);
    }

    public void logPurchase(BigDecimal purchaseAmount, Currency currency) {
        logPurchase(purchaseAmount, currency, null);
    }

    public void logPurchase(BigDecimal purchaseAmount, Currency currency, Bundle parameters) {
        if (purchaseAmount == null) {
            notifyDeveloperError("purchaseAmount cannot be null");
        } else if (currency == null) {
            notifyDeveloperError("currency cannot be null");
        } else {
            if (parameters == null) {
                parameters = new Bundle();
            }
            parameters.putString(AppEventsConstants.EVENT_PARAM_CURRENCY, currency.getCurrencyCode());
            logEvent(AppEventsConstants.EVENT_NAME_PURCHASED, purchaseAmount.doubleValue(), parameters);
            eagerFlush();
        }
    }

    public void flush() {
        flush(FlushReason.EXPLICIT);
    }

    public static void onContextStop() {
        PersistedEvents.persistEvents(applicationContext, stateMap);
    }

    public boolean isValidForAccessToken(AccessToken accessToken) {
        return this.accessTokenAppId.equals(new AccessTokenAppIdPair(accessToken));
    }

    public void logSdkEvent(String eventName, Double valueToSum, Bundle parameters) {
        logEvent(eventName, valueToSum, parameters, true);
    }

    public String getApplicationId() {
        return this.accessTokenAppId.getApplicationId();
    }

    private AppEventsLogger(Context context, String applicationId, AccessToken accessToken) {
        Validate.notNull(context, "context");
        this.contextName = Utility.getActivityName(context);
        if (accessToken == null) {
            accessToken = AccessToken.getCurrentAccessToken();
        }
        if (accessToken == null || !(applicationId == null || applicationId.equals(accessToken.getApplicationId()))) {
            if (applicationId == null) {
                applicationId = Utility.getMetadataApplicationId(context);
            }
            this.accessTokenAppId = new AccessTokenAppIdPair(null, applicationId);
        } else {
            this.accessTokenAppId = new AccessTokenAppIdPair(accessToken);
        }
        synchronized (staticLock) {
            if (applicationContext == null) {
                applicationContext = context.getApplicationContext();
            }
        }
        initializeTimersIfNeeded();
    }

    private static void initializeTimersIfNeeded() {
        synchronized (staticLock) {
            if (backgroundExecutor != null) {
                return;
            }
            backgroundExecutor = new ScheduledThreadPoolExecutor(1);
            backgroundExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    if (AppEventsLogger.getFlushBehavior() != FlushBehavior.EXPLICIT_ONLY) {
                        AppEventsLogger.flushAndWait(FlushReason.TIMER);
                    }
                }
            }, 0, 15, TimeUnit.SECONDS);
            backgroundExecutor.scheduleAtFixedRate(new Runnable() {
                public void run() {
                    Set<String> applicationIds = new HashSet();
                    synchronized (AppEventsLogger.staticLock) {
                        for (AccessTokenAppIdPair accessTokenAppId : AppEventsLogger.stateMap.keySet()) {
                            applicationIds.add(accessTokenAppId.getApplicationId());
                        }
                    }
                    for (String applicationId : applicationIds) {
                        Utility.queryAppSettings(applicationId, true);
                    }
                }
            }, 0, 86400, TimeUnit.SECONDS);
        }
    }

    private void logEvent(String eventName, Double valueToSum, Bundle parameters, boolean isImplicitlyLogged) {
        logEvent(applicationContext, new AppEvent(this.contextName, eventName, valueToSum, parameters, isImplicitlyLogged), this.accessTokenAppId);
    }

    private static void logEvent(final Context context, final AppEvent event, final AccessTokenAppIdPair accessTokenAppId) {
        FacebookSdk.getExecutor().execute(new Runnable() {
            public void run() {
                AppEventsLogger.getSessionEventsState(context, accessTokenAppId).addEvent(event);
                AppEventsLogger.flushIfNecessary();
            }
        });
    }

    static void eagerFlush() {
        if (getFlushBehavior() != FlushBehavior.EXPLICIT_ONLY) {
            flush(FlushReason.EAGER_FLUSHING_EVENT);
        }
    }

    private static void flushIfNecessary() {
        synchronized (staticLock) {
            if (getFlushBehavior() != FlushBehavior.EXPLICIT_ONLY && getAccumulatedEventCount() > NUM_LOG_EVENTS_TO_TRY_TO_FLUSH_AFTER) {
                flush(FlushReason.EVENT_THRESHOLD);
            }
        }
    }

    private static int getAccumulatedEventCount() {
        int result;
        synchronized (staticLock) {
            result = 0;
            for (SessionEventsState state : stateMap.values()) {
                result += state.getAccumulatedEventCount();
            }
        }
        return result;
    }

    private static SessionEventsState getSessionEventsState(Context context, AccessTokenAppIdPair accessTokenAppId) {
        Throwable th;
        AttributionIdentifiers attributionIdentifiers = null;
        if (((SessionEventsState) stateMap.get(accessTokenAppId)) == null) {
            attributionIdentifiers = AttributionIdentifiers.getAttributionIdentifiers(context);
        }
        synchronized (staticLock) {
            try {
                SessionEventsState state = (SessionEventsState) stateMap.get(accessTokenAppId);
                if (state == null) {
                    SessionEventsState state2 = new SessionEventsState(attributionIdentifiers, context.getPackageName(), getAnonymousAppDeviceGUID(context));
                    try {
                        stateMap.put(accessTokenAppId, state2);
                        state = state2;
                    } catch (Throwable th2) {
                        th = th2;
                        state = state2;
                        throw th;
                    }
                }
                return state;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    private static SessionEventsState getSessionEventsState(AccessTokenAppIdPair accessTokenAppId) {
        SessionEventsState sessionEventsState;
        synchronized (staticLock) {
            sessionEventsState = (SessionEventsState) stateMap.get(accessTokenAppId);
        }
        return sessionEventsState;
    }

    private static void flush(final FlushReason reason) {
        FacebookSdk.getExecutor().execute(new Runnable() {
            public void run() {
                AppEventsLogger.flushAndWait(reason);
            }
        });
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void flushAndWait(com.facebook.appevents.AppEventsLogger.FlushReason r6) {
        /*
        r5 = staticLock;
        monitor-enter(r5);
        r4 = requestInFlight;	 Catch:{ all -> 0x0048 }
        if (r4 == 0) goto L_0x0009;
    L_0x0007:
        monitor-exit(r5);	 Catch:{ all -> 0x0048 }
    L_0x0008:
        return;
    L_0x0009:
        r4 = 1;
        requestInFlight = r4;	 Catch:{ all -> 0x0048 }
        r3 = new java.util.HashSet;	 Catch:{ all -> 0x0048 }
        r4 = stateMap;	 Catch:{ all -> 0x0048 }
        r4 = r4.keySet();	 Catch:{ all -> 0x0048 }
        r3.<init>(r4);	 Catch:{ all -> 0x0048 }
        monitor-exit(r5);	 Catch:{ all -> 0x0048 }
        accumulatePersistedEvents();
        r1 = 0;
        r1 = buildAndExecuteRequests(r6, r3);	 Catch:{ Exception -> 0x004b }
    L_0x0020:
        r5 = staticLock;
        monitor-enter(r5);
        r4 = 0;
        requestInFlight = r4;	 Catch:{ all -> 0x0054 }
        monitor-exit(r5);	 Catch:{ all -> 0x0054 }
        if (r1 == 0) goto L_0x0008;
    L_0x0029:
        r2 = new android.content.Intent;
        r4 = "com.facebook.sdk.APP_EVENTS_FLUSHED";
        r2.<init>(r4);
        r4 = "com.facebook.sdk.APP_EVENTS_NUM_EVENTS_FLUSHED";
        r5 = r1.numEvents;
        r2.putExtra(r4, r5);
        r4 = "com.facebook.sdk.APP_EVENTS_FLUSH_RESULT";
        r5 = r1.result;
        r2.putExtra(r4, r5);
        r4 = applicationContext;
        r4 = android.support.v4.content.LocalBroadcastManager.getInstance(r4);
        r4.sendBroadcast(r2);
        goto L_0x0008;
    L_0x0048:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x0048 }
        throw r4;
    L_0x004b:
        r0 = move-exception;
        r4 = TAG;
        r5 = "Caught unexpected exception while flushing: ";
        com.facebook.internal.Utility.logd(r4, r5, r0);
        goto L_0x0020;
    L_0x0054:
        r4 = move-exception;
        monitor-exit(r5);	 Catch:{ all -> 0x0054 }
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.appevents.AppEventsLogger.flushAndWait(com.facebook.appevents.AppEventsLogger$FlushReason):void");
    }

    private static FlushStatistics buildAndExecuteRequests(FlushReason reason, Set<AccessTokenAppIdPair> keysToFlush) {
        GraphRequest request;
        FlushStatistics flushResults = new FlushStatistics();
        boolean limitEventUsage = FacebookSdk.getLimitEventAndDataUsage(applicationContext);
        List<GraphRequest> requestsToExecute = new ArrayList();
        for (AccessTokenAppIdPair accessTokenAppId : keysToFlush) {
            SessionEventsState sessionEventsState = getSessionEventsState(accessTokenAppId);
            if (sessionEventsState != null) {
                request = buildRequestForSession(accessTokenAppId, sessionEventsState, limitEventUsage, flushResults);
                if (request != null) {
                    requestsToExecute.add(request);
                }
            }
        }
        if (requestsToExecute.size() <= 0) {
            return null;
        }
        Logger.log(LoggingBehavior.APP_EVENTS, TAG, "Flushing %d events due to %s.", Integer.valueOf(flushResults.numEvents), reason.toString());
        for (GraphRequest request2 : requestsToExecute) {
            request2.executeAndWait();
        }
        return flushResults;
    }

    private static GraphRequest buildRequestForSession(final AccessTokenAppIdPair accessTokenAppId, final SessionEventsState sessionEventsState, boolean limitEventUsage, final FlushStatistics flushState) {
        FetchedAppSettings fetchedAppSettings = Utility.queryAppSettings(accessTokenAppId.getApplicationId(), false);
        final GraphRequest postRequest = GraphRequest.newPostRequest(null, String.format("%s/activities", new Object[]{applicationId}), null, null);
        Bundle requestParameters = postRequest.getParameters();
        if (requestParameters == null) {
            requestParameters = new Bundle();
        }
        requestParameters.putString(ServerProtocol.DIALOG_PARAM_ACCESS_TOKEN, accessTokenAppId.getAccessTokenString());
        postRequest.setParameters(requestParameters);
        if (fetchedAppSettings == null) {
            return null;
        }
        int numEvents = sessionEventsState.populateRequest(postRequest, fetchedAppSettings.supportsImplicitLogging(), limitEventUsage);
        if (numEvents == 0) {
            return null;
        }
        flushState.numEvents += numEvents;
        postRequest.setCallback(new Callback() {
            public void onCompleted(GraphResponse response) {
                AppEventsLogger.handleResponse(accessTokenAppId, postRequest, response, sessionEventsState, flushState);
            }
        });
        return postRequest;
    }

    private static void handleResponse(AccessTokenAppIdPair accessTokenAppId, GraphRequest request, GraphResponse response, SessionEventsState sessionEventsState, FlushStatistics flushState) {
        FacebookRequestError error = response.getError();
        String resultDescription = "Success";
        FlushResult flushResult = FlushResult.SUCCESS;
        if (error != null) {
            if (error.getErrorCode() == -1) {
                resultDescription = "Failed: No Connectivity";
                flushResult = FlushResult.NO_CONNECTIVITY;
            } else {
                resultDescription = String.format("Failed:\n  Response: %s\n  Error %s", new Object[]{response.toString(), error.toString()});
                flushResult = FlushResult.SERVER_ERROR;
            }
        }
        if (FacebookSdk.isLoggingBehaviorEnabled(LoggingBehavior.APP_EVENTS)) {
            String prettyPrintedEvents;
            try {
                prettyPrintedEvents = new JSONArray((String) request.getTag()).toString(2);
            } catch (JSONException e) {
                prettyPrintedEvents = "<Can't encode events for debug logging>";
            }
            Logger.log(LoggingBehavior.APP_EVENTS, TAG, "Flush completed\nParams: %s\n  Result: %s\n  Events JSON: %s", request.getGraphObject().toString(), resultDescription, prettyPrintedEvents);
        }
        sessionEventsState.clearInFlightAndStats(error != null);
        if (flushResult == FlushResult.NO_CONNECTIVITY) {
            PersistedEvents.persistEvents(applicationContext, accessTokenAppId, sessionEventsState);
        }
        if (flushResult != FlushResult.SUCCESS && flushState.result != FlushResult.NO_CONNECTIVITY) {
            flushState.result = flushResult;
        }
    }

    private static int accumulatePersistedEvents() {
        PersistedEvents persistedEvents = PersistedEvents.readAndClearStore(applicationContext);
        int result = 0;
        for (AccessTokenAppIdPair accessTokenAppId : persistedEvents.keySet()) {
            SessionEventsState sessionEventsState = getSessionEventsState(applicationContext, accessTokenAppId);
            List<AppEvent> events = persistedEvents.getEvents(accessTokenAppId);
            sessionEventsState.accumulatePersistedEvents(events);
            result += events.size();
        }
        return result;
    }

    private static void notifyDeveloperError(String message) {
        Logger.log(LoggingBehavior.DEVELOPER_ERRORS, "AppEvents", message);
    }

    private static void setSourceApplication(Activity activity) {
        ComponentName callingApplication = activity.getCallingActivity();
        if (callingApplication != null) {
            String callingApplicationPackage = callingApplication.getPackageName();
            if (callingApplicationPackage.equals(activity.getPackageName())) {
                resetSourceApplication();
                return;
            }
            sourceApplication = callingApplicationPackage;
        }
        Intent openIntent = activity.getIntent();
        if (openIntent == null || openIntent.getBooleanExtra(SOURCE_APPLICATION_HAS_BEEN_SET_BY_THIS_INTENT, false)) {
            resetSourceApplication();
            return;
        }
        Bundle applinkData = AppLinks.getAppLinkData(openIntent);
        if (applinkData == null) {
            resetSourceApplication();
            return;
        }
        isOpenedByApplink = true;
        Bundle applinkReferrerData = applinkData.getBundle("referer_app_link");
        if (applinkReferrerData == null) {
            sourceApplication = null;
            return;
        }
        sourceApplication = applinkReferrerData.getString("package");
        openIntent.putExtra(SOURCE_APPLICATION_HAS_BEEN_SET_BY_THIS_INTENT, true);
    }

    static void setSourceApplication(String applicationPackage, boolean openByAppLink) {
        sourceApplication = applicationPackage;
        isOpenedByApplink = openByAppLink;
    }

    static String getSourceApplication() {
        String openType = "Unclassified";
        if (isOpenedByApplink) {
            openType = "Applink";
        }
        if (sourceApplication != null) {
            return openType + "(" + sourceApplication + ")";
        }
        return openType;
    }

    static void resetSourceApplication() {
        sourceApplication = null;
        isOpenedByApplink = false;
    }

    public static String getAnonymousAppDeviceGUID(Context context) {
        if (anonymousAppDeviceGUID == null) {
            synchronized (staticLock) {
                if (anonymousAppDeviceGUID == null) {
                    anonymousAppDeviceGUID = context.getSharedPreferences(APP_EVENT_PREFERENCES, 0).getString("anonymousAppDeviceGUID", null);
                    if (anonymousAppDeviceGUID == null) {
                        anonymousAppDeviceGUID = "XZ" + UUID.randomUUID().toString();
                        context.getSharedPreferences(APP_EVENT_PREFERENCES, 0).edit().putString("anonymousAppDeviceGUID", anonymousAppDeviceGUID).apply();
                    }
                }
            }
        }
        return anonymousAppDeviceGUID;
    }
}
