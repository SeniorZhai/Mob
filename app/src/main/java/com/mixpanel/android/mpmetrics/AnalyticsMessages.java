package com.mixpanel.android.mpmetrics;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.tagmanager.DataLayer;
import com.helpshift.res.values.HSConsts;
import com.mixpanel.android.mpmetrics.MPDbAdapter.Table;
import com.mixpanel.android.util.Base64Coder;
import com.mixpanel.android.util.HttpService;
import com.mixpanel.android.util.RemoteService;
import com.mixpanel.android.util.RemoteService.ServiceUnavailableException;
import com.mobcrush.mobcrush.Constants;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

class AnalyticsMessages {
    private static final int ENQUEUE_EVENTS = 1;
    private static final int ENQUEUE_PEOPLE = 0;
    private static final int FLUSH_QUEUE = 2;
    private static final int INSTALL_DECIDE_CHECK = 12;
    private static final int KILL_WORKER = 5;
    private static final String LOGTAG = "MixpanelAPI.Messages";
    private static final int REGISTER_FOR_GCM = 13;
    private static final Map<Context, AnalyticsMessages> sInstances = new HashMap();
    private final MPConfig mConfig;
    private final Context mContext;
    private final Worker mWorker = new Worker();

    static class EventDescription {
        private final String eventName;
        private final JSONObject properties;
        private final String token;

        public EventDescription(String eventName, JSONObject properties, String token) {
            this.eventName = eventName;
            this.properties = properties;
            this.token = token;
        }

        public String getEventName() {
            return this.eventName;
        }

        public JSONObject getProperties() {
            return this.properties;
        }

        public String getToken() {
            return this.token;
        }
    }

    private class Worker {
        private long mAveFlushFrequency = 0;
        private long mFlushCount = 0;
        private Handler mHandler = restartWorkerThread();
        private final Object mHandlerLock = new Object();
        private long mLastFlushTime = -1;
        private SystemInformation mSystemInformation;

        private class AnalyticsMessageHandler extends Handler {
            private MPDbAdapter mDbAdapter = null;
            private final DecideChecker mDecideChecker;
            private final boolean mDisableFallback;
            private final long mFlushInterval;
            private long mRetryAfter;

            public AnalyticsMessageHandler(Looper looper) {
                super(looper);
                this.mDecideChecker = new DecideChecker(AnalyticsMessages.this.mContext, AnalyticsMessages.this.mConfig);
                this.mDisableFallback = AnalyticsMessages.this.mConfig.getDisableFallback();
                this.mFlushInterval = (long) AnalyticsMessages.this.mConfig.getFlushInterval(AnalyticsMessages.this.mContext);
                Worker.this.mSystemInformation = new SystemInformation(AnalyticsMessages.this.mContext);
                this.mRetryAfter = -1;
            }

            public void handleMessage(Message msg) {
                if (this.mDbAdapter == null) {
                    this.mDbAdapter = AnalyticsMessages.this.makeDbAdapter(AnalyticsMessages.this.mContext);
                    this.mDbAdapter.cleanupEvents(System.currentTimeMillis() - ((long) AnalyticsMessages.this.mConfig.getDataExpiration()), Table.EVENTS);
                    this.mDbAdapter.cleanupEvents(System.currentTimeMillis() - ((long) AnalyticsMessages.this.mConfig.getDataExpiration()), Table.PEOPLE);
                }
                int returnCode = -3;
                try {
                    JSONObject message;
                    if (msg.what == 0) {
                        message = msg.obj;
                        AnalyticsMessages.this.logAboutMessageToMixpanel("Queuing people record for sending later");
                        AnalyticsMessages.this.logAboutMessageToMixpanel("    " + message.toString());
                        returnCode = this.mDbAdapter.addJSON(message, Table.PEOPLE);
                    } else if (msg.what == AnalyticsMessages.ENQUEUE_EVENTS) {
                        EventDescription eventDescription = msg.obj;
                        try {
                            message = prepareEventObject(eventDescription);
                            AnalyticsMessages.this.logAboutMessageToMixpanel("Queuing event for sending later");
                            AnalyticsMessages.this.logAboutMessageToMixpanel("    " + message.toString());
                            returnCode = this.mDbAdapter.addJSON(message, Table.EVENTS);
                        } catch (JSONException e) {
                            Log.e(AnalyticsMessages.LOGTAG, "Exception tracking event " + eventDescription.getEventName(), e);
                        }
                    } else if (msg.what == AnalyticsMessages.FLUSH_QUEUE) {
                        AnalyticsMessages.this.logAboutMessageToMixpanel("Flushing queue due to scheduled or forced flush");
                        Worker.this.updateFlushFrequency();
                        if (SystemClock.elapsedRealtime() >= this.mRetryAfter) {
                            try {
                                sendAllData(this.mDbAdapter);
                                this.mDecideChecker.runDecideChecks(AnalyticsMessages.this.getPoster());
                            } catch (ServiceUnavailableException e2) {
                                this.mRetryAfter = SystemClock.elapsedRealtime() + ((long) (e2.getRetryAfter() * Constants.UPDATE_COFIG_INTERVAL));
                            }
                        }
                    } else if (msg.what == AnalyticsMessages.INSTALL_DECIDE_CHECK) {
                        AnalyticsMessages.this.logAboutMessageToMixpanel("Installing a check for surveys and in-app notifications");
                        this.mDecideChecker.addDecideCheck(msg.obj);
                        if (SystemClock.elapsedRealtime() >= this.mRetryAfter) {
                            try {
                                this.mDecideChecker.runDecideChecks(AnalyticsMessages.this.getPoster());
                            } catch (ServiceUnavailableException e22) {
                                this.mRetryAfter = SystemClock.elapsedRealtime() + ((long) (e22.getRetryAfter() * Constants.UPDATE_COFIG_INTERVAL));
                            }
                        }
                    } else if (msg.what == AnalyticsMessages.REGISTER_FOR_GCM) {
                        runGCMRegistration(msg.obj);
                    } else if (msg.what == AnalyticsMessages.KILL_WORKER) {
                        Log.w(AnalyticsMessages.LOGTAG, "Worker received a hard kill. Dumping all events and force-killing. Thread id " + Thread.currentThread().getId());
                        synchronized (Worker.this.mHandlerLock) {
                            this.mDbAdapter.deleteDB();
                            Worker.this.mHandler = null;
                            Looper.myLooper().quit();
                        }
                    } else {
                        Log.e(AnalyticsMessages.LOGTAG, "Unexpected message received by Mixpanel worker: " + msg);
                    }
                    if ((returnCode >= AnalyticsMessages.this.mConfig.getBulkUploadLimit() || returnCode == -2) && SystemClock.elapsedRealtime() >= this.mRetryAfter) {
                        AnalyticsMessages.this.logAboutMessageToMixpanel("Flushing queue due to bulk upload limit");
                        Worker.this.updateFlushFrequency();
                        try {
                            sendAllData(this.mDbAdapter);
                            this.mDecideChecker.runDecideChecks(AnalyticsMessages.this.getPoster());
                        } catch (RuntimeException e3) {
                            this.mRetryAfter = SystemClock.elapsedRealtime() + ((long) (e3.getRetryAfter() * Constants.UPDATE_COFIG_INTERVAL));
                        }
                    } else if (returnCode > 0 && !hasMessages(AnalyticsMessages.FLUSH_QUEUE)) {
                        AnalyticsMessages.this.logAboutMessageToMixpanel("Queue depth " + returnCode + " - Adding flush in " + this.mFlushInterval);
                        if (this.mFlushInterval >= 0) {
                            sendEmptyMessageDelayed(AnalyticsMessages.FLUSH_QUEUE, this.mFlushInterval);
                        }
                    }
                } catch (RuntimeException e32) {
                    Log.e(AnalyticsMessages.LOGTAG, "Worker threw an unhandled exception", e32);
                    synchronized (Worker.this.mHandlerLock) {
                        Worker.this.mHandler = null;
                        try {
                            Looper.myLooper().quit();
                            Log.e(AnalyticsMessages.LOGTAG, "Mixpanel will not process any more analytics messages", e32);
                        } catch (Exception tooLate) {
                            Log.e(AnalyticsMessages.LOGTAG, "Could not halt looper", tooLate);
                        }
                    }
                }
            }

            private void runGCMRegistration(String senderID) {
                try {
                    if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(AnalyticsMessages.this.mContext) != 0) {
                        Log.i(AnalyticsMessages.LOGTAG, "Can't register for push notifications, Google Play Services are not installed.");
                        return;
                    }
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(AnalyticsMessages.this.mContext);
                    String[] strArr = new String[AnalyticsMessages.ENQUEUE_EVENTS];
                    strArr[AnalyticsMessages.ENQUEUE_PEOPLE] = senderID;
                    final String registrationId = gcm.register(strArr);
                    MixpanelAPI.allInstances(new InstanceProcessor() {
                        public void process(MixpanelAPI api) {
                            if (MPConfig.DEBUG) {
                                Log.v(AnalyticsMessages.LOGTAG, "Using existing pushId " + registrationId);
                            }
                            api.getPeople().setPushRegistrationId(registrationId);
                        }
                    });
                } catch (RuntimeException e) {
                    try {
                        Log.i(AnalyticsMessages.LOGTAG, "Can't register for push notifications, Google Play services are not configured.");
                    } catch (IOException e2) {
                        Log.i(AnalyticsMessages.LOGTAG, "Exception when trying to register for GCM", e2);
                    } catch (NoClassDefFoundError e3) {
                        Log.w(AnalyticsMessages.LOGTAG, "Google play services were not part of this build, push notifications cannot be registered or delivered");
                    }
                }
            }

            private void sendAllData(MPDbAdapter dbAdapter) throws ServiceUnavailableException {
                if (AnalyticsMessages.this.getPoster().isOnline(AnalyticsMessages.this.mContext)) {
                    AnalyticsMessages.this.logAboutMessageToMixpanel("Sending records to Mixpanel");
                    Table table;
                    String[] strArr;
                    if (this.mDisableFallback) {
                        table = Table.EVENTS;
                        strArr = new String[AnalyticsMessages.ENQUEUE_EVENTS];
                        strArr[AnalyticsMessages.ENQUEUE_PEOPLE] = AnalyticsMessages.this.mConfig.getEventsEndpoint();
                        sendData(dbAdapter, table, strArr);
                        table = Table.PEOPLE;
                        strArr = new String[AnalyticsMessages.ENQUEUE_EVENTS];
                        strArr[AnalyticsMessages.ENQUEUE_PEOPLE] = AnalyticsMessages.this.mConfig.getPeopleEndpoint();
                        sendData(dbAdapter, table, strArr);
                        return;
                    }
                    table = Table.EVENTS;
                    strArr = new String[AnalyticsMessages.FLUSH_QUEUE];
                    strArr[AnalyticsMessages.ENQUEUE_PEOPLE] = AnalyticsMessages.this.mConfig.getEventsEndpoint();
                    strArr[AnalyticsMessages.ENQUEUE_EVENTS] = AnalyticsMessages.this.mConfig.getEventsFallbackEndpoint();
                    sendData(dbAdapter, table, strArr);
                    table = Table.PEOPLE;
                    strArr = new String[AnalyticsMessages.FLUSH_QUEUE];
                    strArr[AnalyticsMessages.ENQUEUE_PEOPLE] = AnalyticsMessages.this.mConfig.getPeopleEndpoint();
                    strArr[AnalyticsMessages.ENQUEUE_EVENTS] = AnalyticsMessages.this.mConfig.getPeopleFallbackEndpoint();
                    sendData(dbAdapter, table, strArr);
                    return;
                }
                AnalyticsMessages.this.logAboutMessageToMixpanel("Not flushing data to Mixpanel because the device is not connected to the internet.");
            }

            private void sendData(MPDbAdapter dbAdapter, Table table, String[] urls) throws ServiceUnavailableException {
                RemoteService poster = AnalyticsMessages.this.getPoster();
                String[] eventsData = dbAdapter.generateDataString(table);
                if (eventsData != null) {
                    String lastId = eventsData[AnalyticsMessages.ENQUEUE_PEOPLE];
                    String rawMessage = eventsData[AnalyticsMessages.ENQUEUE_EVENTS];
                    String encodedData = Base64Coder.encodeString(rawMessage);
                    List<NameValuePair> params = new ArrayList(AnalyticsMessages.ENQUEUE_EVENTS);
                    params.add(new BasicNameValuePair(MPDbAdapter.KEY_DATA, encodedData));
                    if (MPConfig.DEBUG) {
                        params.add(new BasicNameValuePair("verbose", HSConsts.STATUS_INPROGRESS));
                    }
                    boolean deleteEvents = true;
                    int length = urls.length;
                    int i = AnalyticsMessages.ENQUEUE_PEOPLE;
                    while (i < length) {
                        String url = urls[i];
                        try {
                            byte[] response = poster.performRequest(url, params, AnalyticsMessages.this.mConfig.getSSLSocketFactory());
                            deleteEvents = true;
                            if (response != null) {
                                String parsedResponse = new String(response, HTTP.UTF_8);
                                AnalyticsMessages.this.logAboutMessageToMixpanel("Successfully posted to " + url + ": \n" + rawMessage);
                                AnalyticsMessages.this.logAboutMessageToMixpanel("Response was " + parsedResponse);
                                break;
                            }
                            AnalyticsMessages.this.logAboutMessageToMixpanel("Response was null, unexpected failure posting to " + url + ".");
                            break;
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException("UTF not supported on this platform?", e);
                        } catch (OutOfMemoryError e2) {
                            Log.e(AnalyticsMessages.LOGTAG, "Out of memory when posting to " + url + ".", e2);
                        } catch (MalformedURLException e3) {
                            Log.e(AnalyticsMessages.LOGTAG, "Cannot interpret " + url + " as a URL.", e3);
                        } catch (IOException e4) {
                            AnalyticsMessages.this.logAboutMessageToMixpanel("Cannot post message to " + url + ".", e4);
                            deleteEvents = false;
                            i += AnalyticsMessages.ENQUEUE_EVENTS;
                        }
                    }
                    if (deleteEvents) {
                        AnalyticsMessages.this.logAboutMessageToMixpanel("Not retrying this batch of events, deleting them from DB.");
                        dbAdapter.cleanupEvents(lastId, table);
                        return;
                    }
                    AnalyticsMessages.this.logAboutMessageToMixpanel("Retrying this batch of events.");
                    if (!hasMessages(AnalyticsMessages.FLUSH_QUEUE)) {
                        sendEmptyMessageDelayed(AnalyticsMessages.FLUSH_QUEUE, this.mFlushInterval);
                    }
                }
            }

            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            private org.json.JSONObject getDefaultEventProperties() throws org.json.JSONException {
                /*
                r13 = this;
                r9 = new org.json.JSONObject;
                r9.<init>();
                r11 = "mp_lib";
                r12 = "android";
                r9.put(r11, r12);
                r11 = "$lib_version";
                r12 = "4.6.2";
                r9.put(r11, r12);
                r11 = "$os";
                r12 = "Android";
                r9.put(r11, r12);
                r12 = "$os_version";
                r11 = android.os.Build.VERSION.RELEASE;
                if (r11 != 0) goto L_0x0100;
            L_0x0020:
                r11 = "UNKNOWN";
            L_0x0022:
                r9.put(r12, r11);
                r12 = "$manufacturer";
                r11 = android.os.Build.MANUFACTURER;
                if (r11 != 0) goto L_0x0104;
            L_0x002b:
                r11 = "UNKNOWN";
            L_0x002d:
                r9.put(r12, r11);
                r12 = "$brand";
                r11 = android.os.Build.BRAND;
                if (r11 != 0) goto L_0x0108;
            L_0x0036:
                r11 = "UNKNOWN";
            L_0x0038:
                r9.put(r12, r11);
                r12 = "$model";
                r11 = android.os.Build.MODEL;
                if (r11 != 0) goto L_0x010c;
            L_0x0041:
                r11 = "UNKNOWN";
            L_0x0043:
                r9.put(r12, r11);
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.this;	 Catch:{ RuntimeException -> 0x0119 }
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.this;	 Catch:{ RuntimeException -> 0x0119 }
                r11 = r11.mContext;	 Catch:{ RuntimeException -> 0x0119 }
                r10 = com.google.android.gms.common.GooglePlayServicesUtil.isGooglePlayServicesAvailable(r11);	 Catch:{ RuntimeException -> 0x0119 }
                switch(r10) {
                    case 0: goto L_0x0110;
                    case 1: goto L_0x012d;
                    case 2: goto L_0x0136;
                    case 3: goto L_0x013f;
                    case 4: goto L_0x0055;
                    case 5: goto L_0x0055;
                    case 6: goto L_0x0055;
                    case 7: goto L_0x0055;
                    case 8: goto L_0x0055;
                    case 9: goto L_0x0148;
                    default: goto L_0x0055;
                };
            L_0x0055:
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.this;
                r11 = r11.mSystemInformation;
                r3 = r11.getDisplayMetrics();
                r11 = "$screen_dpi";
                r12 = r3.densityDpi;
                r9.put(r11, r12);
                r11 = "$screen_height";
                r12 = r3.heightPixels;
                r9.put(r11, r12);
                r11 = "$screen_width";
                r12 = r3.widthPixels;
                r9.put(r11, r12);
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.this;
                r11 = r11.mSystemInformation;
                r0 = r11.getAppVersionName();
                if (r0 == 0) goto L_0x0085;
            L_0x0080:
                r11 = "$app_version";
                r9.put(r11, r0);
            L_0x0085:
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.this;
                r11 = r11.mSystemInformation;
                r11 = r11.hasNFC();
                r5 = java.lang.Boolean.valueOf(r11);
                if (r5 == 0) goto L_0x009e;
            L_0x0095:
                r11 = "$has_nfc";
                r12 = r5.booleanValue();
                r9.put(r11, r12);
            L_0x009e:
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.this;
                r11 = r11.mSystemInformation;
                r11 = r11.hasTelephony();
                r6 = java.lang.Boolean.valueOf(r11);
                if (r6 == 0) goto L_0x00b7;
            L_0x00ae:
                r11 = "$has_telephone";
                r12 = r6.booleanValue();
                r9.put(r11, r12);
            L_0x00b7:
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.this;
                r11 = r11.mSystemInformation;
                r2 = r11.getCurrentNetworkOperator();
                if (r2 == 0) goto L_0x00c8;
            L_0x00c3:
                r11 = "$carrier";
                r9.put(r11, r2);
            L_0x00c8:
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.this;
                r11 = r11.mSystemInformation;
                r8 = r11.isWifiConnected();
                if (r8 == 0) goto L_0x00dd;
            L_0x00d4:
                r11 = "$wifi";
                r12 = r8.booleanValue();
                r9.put(r11, r12);
            L_0x00dd:
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.this;
                r11 = r11.mSystemInformation;
                r7 = r11.isBluetoothEnabled();
                if (r7 == 0) goto L_0x00ee;
            L_0x00e9:
                r11 = "$bluetooth_enabled";
                r9.put(r11, r7);
            L_0x00ee:
                r11 = com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.this;
                r11 = r11.mSystemInformation;
                r1 = r11.getBluetoothVersion();
                if (r1 == 0) goto L_0x00ff;
            L_0x00fa:
                r11 = "$bluetooth_version";
                r9.put(r11, r1);
            L_0x00ff:
                return r9;
            L_0x0100:
                r11 = android.os.Build.VERSION.RELEASE;
                goto L_0x0022;
            L_0x0104:
                r11 = android.os.Build.MANUFACTURER;
                goto L_0x002d;
            L_0x0108:
                r11 = android.os.Build.BRAND;
                goto L_0x0038;
            L_0x010c:
                r11 = android.os.Build.MODEL;
                goto L_0x0043;
            L_0x0110:
                r11 = "$google_play_services";
                r12 = "available";
                r9.put(r11, r12);	 Catch:{ RuntimeException -> 0x0119 }
                goto L_0x0055;
            L_0x0119:
                r4 = move-exception;
                r11 = "$google_play_services";
                r12 = "not configured";
                r9.put(r11, r12);	 Catch:{ NoClassDefFoundError -> 0x0123 }
                goto L_0x0055;
            L_0x0123:
                r4 = move-exception;
                r11 = "$google_play_services";
                r12 = "not included";
                r9.put(r11, r12);
                goto L_0x0055;
            L_0x012d:
                r11 = "$google_play_services";
                r12 = "missing";
                r9.put(r11, r12);	 Catch:{ RuntimeException -> 0x0119 }
                goto L_0x0055;
            L_0x0136:
                r11 = "$google_play_services";
                r12 = "out of date";
                r9.put(r11, r12);	 Catch:{ RuntimeException -> 0x0119 }
                goto L_0x0055;
            L_0x013f:
                r11 = "$google_play_services";
                r12 = "disabled";
                r9.put(r11, r12);	 Catch:{ RuntimeException -> 0x0119 }
                goto L_0x0055;
            L_0x0148:
                r11 = "$google_play_services";
                r12 = "invalid";
                r9.put(r11, r12);	 Catch:{ RuntimeException -> 0x0119 }
                goto L_0x0055;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.mixpanel.android.mpmetrics.AnalyticsMessages.Worker.AnalyticsMessageHandler.getDefaultEventProperties():org.json.JSONObject");
            }

            private JSONObject prepareEventObject(EventDescription eventDescription) throws JSONException {
                JSONObject eventObj = new JSONObject();
                JSONObject eventProperties = eventDescription.getProperties();
                JSONObject sendProperties = getDefaultEventProperties();
                sendProperties.put("token", eventDescription.getToken());
                if (eventProperties != null) {
                    Iterator<?> iter = eventProperties.keys();
                    while (iter.hasNext()) {
                        String key = (String) iter.next();
                        sendProperties.put(key, eventProperties.get(key));
                    }
                }
                eventObj.put(DataLayer.EVENT_KEY, eventDescription.getEventName());
                eventObj.put("properties", sendProperties);
                return eventObj;
            }
        }

        public boolean isDead() {
            boolean z;
            synchronized (this.mHandlerLock) {
                z = this.mHandler == null;
            }
            return z;
        }

        public void runMessage(Message msg) {
            synchronized (this.mHandlerLock) {
                if (this.mHandler == null) {
                    AnalyticsMessages.this.logAboutMessageToMixpanel("Dead mixpanel worker dropping a message: " + msg.what);
                } else {
                    this.mHandler.sendMessage(msg);
                }
            }
        }

        private Handler restartWorkerThread() {
            HandlerThread thread = new HandlerThread("com.mixpanel.android.AnalyticsWorker", AnalyticsMessages.ENQUEUE_EVENTS);
            thread.start();
            return new AnalyticsMessageHandler(thread.getLooper());
        }

        private void updateFlushFrequency() {
            long now = System.currentTimeMillis();
            long newFlushCount = this.mFlushCount + 1;
            if (this.mLastFlushTime > 0) {
                this.mAveFlushFrequency = ((now - this.mLastFlushTime) + (this.mAveFlushFrequency * this.mFlushCount)) / newFlushCount;
                AnalyticsMessages.this.logAboutMessageToMixpanel("Average send frequency approximately " + (this.mAveFlushFrequency / 1000) + " seconds.");
            }
            this.mLastFlushTime = now;
            this.mFlushCount = newFlushCount;
        }
    }

    AnalyticsMessages(Context context) {
        this.mContext = context;
        this.mConfig = getConfig(context);
    }

    public static AnalyticsMessages getInstance(Context messageContext) {
        AnalyticsMessages ret;
        synchronized (sInstances) {
            Context appContext = messageContext.getApplicationContext();
            if (sInstances.containsKey(appContext)) {
                ret = (AnalyticsMessages) sInstances.get(appContext);
            } else {
                ret = new AnalyticsMessages(appContext);
                sInstances.put(appContext, ret);
            }
        }
        return ret;
    }

    public void eventsMessage(EventDescription eventDescription) {
        Message m = Message.obtain();
        m.what = ENQUEUE_EVENTS;
        m.obj = eventDescription;
        this.mWorker.runMessage(m);
    }

    public void peopleMessage(JSONObject peopleJson) {
        Message m = Message.obtain();
        m.what = ENQUEUE_PEOPLE;
        m.obj = peopleJson;
        this.mWorker.runMessage(m);
    }

    public void postToServer() {
        Message m = Message.obtain();
        m.what = FLUSH_QUEUE;
        this.mWorker.runMessage(m);
    }

    public void installDecideCheck(DecideMessages check) {
        Message m = Message.obtain();
        m.what = INSTALL_DECIDE_CHECK;
        m.obj = check;
        this.mWorker.runMessage(m);
    }

    public void registerForGCM(String senderID) {
        Message m = Message.obtain();
        m.what = REGISTER_FOR_GCM;
        m.obj = senderID;
        this.mWorker.runMessage(m);
    }

    public void hardKill() {
        Message m = Message.obtain();
        m.what = KILL_WORKER;
        this.mWorker.runMessage(m);
    }

    boolean isDead() {
        return this.mWorker.isDead();
    }

    protected MPDbAdapter makeDbAdapter(Context context) {
        return new MPDbAdapter(context);
    }

    protected MPConfig getConfig(Context context) {
        return MPConfig.getInstance(context);
    }

    protected RemoteService getPoster() {
        return new HttpService();
    }

    private void logAboutMessageToMixpanel(String message) {
        if (MPConfig.DEBUG) {
            Log.v(LOGTAG, message + " (Thread " + Thread.currentThread().getId() + ")");
        }
    }

    private void logAboutMessageToMixpanel(String message, Throwable e) {
        if (MPConfig.DEBUG) {
            Log.v(LOGTAG, message + " (Thread " + Thread.currentThread().getId() + ")", e);
        }
    }
}
