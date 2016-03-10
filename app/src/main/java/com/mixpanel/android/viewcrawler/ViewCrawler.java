package com.mixpanel.android.viewcrawler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Pair;
import com.helpshift.storage.ProfilesDBHelper;
import com.mixpanel.android.mpmetrics.MPConfig;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.mpmetrics.ResourceIds;
import com.mixpanel.android.mpmetrics.ResourceReader.Ids;
import com.mixpanel.android.mpmetrics.SuperPropertyUpdate;
import com.mixpanel.android.mpmetrics.Tweaks;
import com.mixpanel.android.mpmetrics.Tweaks.OnTweakDeclaredListener;
import com.mixpanel.android.util.JSONUtils;
import com.mixpanel.android.viewcrawler.EditProtocol.BadInstructionsException;
import com.mixpanel.android.viewcrawler.EditProtocol.CantGetEditAssetsException;
import com.mixpanel.android.viewcrawler.EditProtocol.Edit;
import com.mixpanel.android.viewcrawler.EditProtocol.InapplicableInstructionsException;
import com.mixpanel.android.viewcrawler.EditorConnection.EditorConnectionException;
import com.mixpanel.android.viewcrawler.FlipGesture.OnFlipGestureListener;
import com.mixpanel.android.viewcrawler.ViewVisitor.LayoutErrorMessage;
import com.mixpanel.android.viewcrawler.ViewVisitor.OnLayoutErrorListener;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@TargetApi(16)
public class ViewCrawler implements UpdatesFromMixpanel, TrackingDebug, OnLayoutErrorListener {
    private static final int EMULATOR_CONNECT_ATTEMPT_INTERVAL_MILLIS = 30000;
    private static final String LOGTAG = "MixpanelAPI.ViewCrawler";
    private static final int MESSAGE_CONNECT_TO_EDITOR = 1;
    private static final int MESSAGE_EVENT_BINDINGS_RECEIVED = 5;
    private static final int MESSAGE_HANDLE_EDITOR_BINDINGS_RECEIVED = 6;
    private static final int MESSAGE_HANDLE_EDITOR_CHANGES_CLEARED = 10;
    private static final int MESSAGE_HANDLE_EDITOR_CHANGES_RECEIVED = 3;
    private static final int MESSAGE_HANDLE_EDITOR_CLOSED = 8;
    private static final int MESSAGE_HANDLE_EDITOR_TWEAKS_RECEIVED = 11;
    private static final int MESSAGE_INITIALIZE_CHANGES = 0;
    private static final int MESSAGE_SEND_DEVICE_INFO = 4;
    private static final int MESSAGE_SEND_EVENT_TRACKED = 7;
    private static final int MESSAGE_SEND_LAYOUT_ERROR = 12;
    private static final int MESSAGE_SEND_STATE_FOR_EDITING = 2;
    private static final int MESSAGE_VARIANTS_RECEIVED = 9;
    private static final String SHARED_PREF_BINDINGS_KEY = "mixpanel.viewcrawler.bindings";
    private static final String SHARED_PREF_CHANGES_KEY = "mixpanel.viewcrawler.changes";
    private static final String SHARED_PREF_EDITS_FILE = "mixpanel.viewcrawler.changes";
    private final MPConfig mConfig;
    private final Map<String, String> mDeviceInfo;
    private final DynamicEventTracker mDynamicEventTracker;
    private final EditState mEditState = new EditState();
    private final ViewCrawlerHandler mMessageThreadHandler;
    private final MixpanelAPI mMixpanel;
    private final float mScaledDensity;
    private final Tweaks mTweaks;

    private class Editor implements com.mixpanel.android.viewcrawler.EditorConnection.Editor {
        private Editor() {
        }

        public void sendSnapshot(JSONObject message) {
            Message msg = ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_SEND_STATE_FOR_EDITING);
            msg.obj = message;
            ViewCrawler.this.mMessageThreadHandler.sendMessage(msg);
        }

        public void performEdit(JSONObject message) {
            Message msg = ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_HANDLE_EDITOR_CHANGES_RECEIVED);
            msg.obj = message;
            ViewCrawler.this.mMessageThreadHandler.sendMessage(msg);
        }

        public void clearEdits(JSONObject message) {
            Message msg = ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_HANDLE_EDITOR_CHANGES_CLEARED);
            msg.obj = message;
            ViewCrawler.this.mMessageThreadHandler.sendMessage(msg);
        }

        public void setTweaks(JSONObject message) {
            Message msg = ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_HANDLE_EDITOR_TWEAKS_RECEIVED);
            msg.obj = message;
            ViewCrawler.this.mMessageThreadHandler.sendMessage(msg);
        }

        public void bindEvents(JSONObject message) {
            Message msg = ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_HANDLE_EDITOR_BINDINGS_RECEIVED);
            msg.obj = message;
            ViewCrawler.this.mMessageThreadHandler.sendMessage(msg);
        }

        public void sendDeviceInfo() {
            ViewCrawler.this.mMessageThreadHandler.sendMessage(ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_SEND_DEVICE_INFO));
        }

        public void cleanup() {
            ViewCrawler.this.mMessageThreadHandler.sendMessage(ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_HANDLE_EDITOR_CLOSED));
        }
    }

    private class EmulatorConnector implements Runnable {
        private volatile boolean mStopped = true;

        public void run() {
            if (!this.mStopped) {
                ViewCrawler.this.mMessageThreadHandler.sendMessage(ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_CONNECT_TO_EDITOR));
            }
            ViewCrawler.this.mMessageThreadHandler.postDelayed(this, Constants.BROADCASTS_LIVE_TIME);
        }

        public void start() {
            this.mStopped = false;
            ViewCrawler.this.mMessageThreadHandler.post(this);
        }

        public void stop() {
            this.mStopped = true;
            ViewCrawler.this.mMessageThreadHandler.removeCallbacks(this);
        }
    }

    private class LifecycleCallbacks implements ActivityLifecycleCallbacks, OnFlipGestureListener {
        private final EmulatorConnector mEmulatorConnector;
        private final FlipGesture mFlipGesture = new FlipGesture(this);

        public LifecycleCallbacks() {
            this.mEmulatorConnector = new EmulatorConnector();
        }

        public void onFlipGesture() {
            ViewCrawler.this.mMessageThreadHandler.sendMessage(ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_CONNECT_TO_EDITOR));
        }

        public void onActivityCreated(Activity activity, Bundle bundle) {
        }

        public void onActivityStarted(Activity activity) {
        }

        public void onActivityResumed(Activity activity) {
            installConnectionSensor(activity);
            ViewCrawler.this.mEditState.add(activity);
        }

        public void onActivityPaused(Activity activity) {
            ViewCrawler.this.mEditState.remove(activity);
            if (ViewCrawler.this.mEditState.isEmpty()) {
                uninstallConnectionSensor(activity);
            }
        }

        public void onActivityStopped(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        public void onActivityDestroyed(Activity activity) {
        }

        private void installConnectionSensor(Activity activity) {
            if (isInEmulator() && !ViewCrawler.this.mConfig.getDisableEmulatorBindingUI()) {
                this.mEmulatorConnector.start();
            } else if (!ViewCrawler.this.mConfig.getDisableGestureBindingUI()) {
                SensorManager sensorManager = (SensorManager) activity.getSystemService("sensor");
                sensorManager.registerListener(this.mFlipGesture, sensorManager.getDefaultSensor(ViewCrawler.MESSAGE_CONNECT_TO_EDITOR), ViewCrawler.MESSAGE_HANDLE_EDITOR_CHANGES_RECEIVED);
            }
        }

        private void uninstallConnectionSensor(Activity activity) {
            if (isInEmulator() && !ViewCrawler.this.mConfig.getDisableEmulatorBindingUI()) {
                this.mEmulatorConnector.stop();
            } else if (!ViewCrawler.this.mConfig.getDisableGestureBindingUI()) {
                ((SensorManager) activity.getSystemService("sensor")).unregisterListener(this.mFlipGesture);
            }
        }

        private boolean isInEmulator() {
            if (Build.HARDWARE.equals("goldfish") && Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic") && Build.PRODUCT.contains(CommonUtils.SDK) && Build.MODEL.toLowerCase(Locale.US).contains(CommonUtils.SDK)) {
                return true;
            }
            return false;
        }
    }

    private static class VariantChange {
        public final String activityName;
        public final JSONObject change;
        public final Pair<Integer, Integer> variantId;

        public VariantChange(String anActivityName, JSONObject someChange, Pair<Integer, Integer> aVariantId) {
            this.activityName = anActivityName;
            this.change = someChange;
            this.variantId = aVariantId;
        }
    }

    private static class VariantTweak {
        public final JSONObject tweak;
        public final Pair<Integer, Integer> variantId;

        public VariantTweak(JSONObject aTweak, Pair<Integer, Integer> aVariantId) {
            this.tweak = aTweak;
            this.variantId = aVariantId;
        }
    }

    private class ViewCrawlerHandler extends Handler {
        private final Context mContext;
        private final List<String> mEditorAssetUrls;
        private final Map<String, Pair<String, JSONObject>> mEditorChanges;
        private EditorConnection mEditorConnection;
        private final List<Pair<String, JSONObject>> mEditorEventBindings;
        private final List<JSONObject> mEditorTweaks;
        private final ImageStore mImageStore;
        private final List<VariantChange> mPersistentChanges;
        private final List<Pair<String, JSONObject>> mPersistentEventBindings;
        private final List<VariantTweak> mPersistentTweaks;
        private final EditProtocol mProtocol;
        private final Set<Pair<Integer, Integer>> mSeenExperiments;
        private ViewSnapshot mSnapshot = null;
        private final Lock mStartLock;
        private final String mToken;

        public ViewCrawlerHandler(Context context, String token, Looper looper, OnLayoutErrorListener layoutErrorListener) {
            super(looper);
            this.mContext = context;
            this.mToken = token;
            String resourcePackage = ViewCrawler.this.mConfig.getResourcePackageName();
            if (resourcePackage == null) {
                resourcePackage = context.getPackageName();
            }
            ResourceIds resourceIds = new Ids(resourcePackage, context);
            this.mImageStore = new ImageStore(context);
            this.mProtocol = new EditProtocol(resourceIds, this.mImageStore, layoutErrorListener);
            this.mEditorChanges = new HashMap();
            this.mEditorTweaks = new ArrayList();
            this.mEditorAssetUrls = new ArrayList();
            this.mEditorEventBindings = new ArrayList();
            this.mPersistentChanges = new ArrayList();
            this.mPersistentTweaks = new ArrayList();
            this.mPersistentEventBindings = new ArrayList();
            this.mSeenExperiments = new HashSet();
            this.mStartLock = new ReentrantLock();
            this.mStartLock.lock();
        }

        public void start() {
            this.mStartLock.unlock();
        }

        public void handleMessage(Message msg) {
            this.mStartLock.lock();
            try {
                switch (msg.what) {
                    case ViewCrawler.MESSAGE_INITIALIZE_CHANGES /*0*/:
                        loadKnownChanges();
                        initializeChanges();
                        break;
                    case ViewCrawler.MESSAGE_CONNECT_TO_EDITOR /*1*/:
                        connectToEditor();
                        break;
                    case ViewCrawler.MESSAGE_SEND_STATE_FOR_EDITING /*2*/:
                        sendSnapshot((JSONObject) msg.obj);
                        break;
                    case ViewCrawler.MESSAGE_HANDLE_EDITOR_CHANGES_RECEIVED /*3*/:
                        handleEditorChangeReceived((JSONObject) msg.obj);
                        break;
                    case ViewCrawler.MESSAGE_SEND_DEVICE_INFO /*4*/:
                        sendDeviceInfo();
                        break;
                    case ViewCrawler.MESSAGE_EVENT_BINDINGS_RECEIVED /*5*/:
                        handleEventBindingsReceived((JSONArray) msg.obj);
                        break;
                    case ViewCrawler.MESSAGE_HANDLE_EDITOR_BINDINGS_RECEIVED /*6*/:
                        handleEditorBindingsReceived((JSONObject) msg.obj);
                        break;
                    case ViewCrawler.MESSAGE_SEND_EVENT_TRACKED /*7*/:
                        sendReportTrackToEditor((String) msg.obj);
                        break;
                    case ViewCrawler.MESSAGE_HANDLE_EDITOR_CLOSED /*8*/:
                        handleEditorClosed();
                        break;
                    case ViewCrawler.MESSAGE_VARIANTS_RECEIVED /*9*/:
                        handleVariantsReceived((JSONArray) msg.obj);
                        break;
                    case ViewCrawler.MESSAGE_HANDLE_EDITOR_CHANGES_CLEARED /*10*/:
                        handleEditorBindingsCleared((JSONObject) msg.obj);
                        break;
                    case ViewCrawler.MESSAGE_HANDLE_EDITOR_TWEAKS_RECEIVED /*11*/:
                        handleEditorTweaksReceived((JSONObject) msg.obj);
                        break;
                    case ViewCrawler.MESSAGE_SEND_LAYOUT_ERROR /*12*/:
                        sendLayoutError((LayoutErrorMessage) msg.obj);
                        break;
                }
                this.mStartLock.unlock();
            } catch (Throwable th) {
                this.mStartLock.unlock();
            }
        }

        private void loadKnownChanges() {
            SharedPreferences preferences = getSharedPreferences();
            String storedChanges = preferences.getString(ViewCrawler.SHARED_PREF_EDITS_FILE, null);
            if (storedChanges != null) {
                try {
                    JSONArray variants = new JSONArray(storedChanges);
                    int variantsLength = variants.length();
                    for (int i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < variantsLength; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                        JSONObject variant = variants.getJSONObject(i);
                        this.mSeenExperiments.add(new Pair(Integer.valueOf(variant.getInt("experiment_id")), Integer.valueOf(variant.getInt(DBLikedChannelsHelper.KEY_ID))));
                    }
                } catch (JSONException e) {
                    Log.e(ViewCrawler.LOGTAG, "Malformed variants found in persistent storage, clearing all variants", e);
                    android.content.SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(ViewCrawler.SHARED_PREF_EDITS_FILE);
                    editor.remove(ViewCrawler.SHARED_PREF_BINDINGS_KEY);
                    editor.apply();
                }
            }
        }

        private void initializeChanges() {
            int i;
            SharedPreferences preferences = getSharedPreferences();
            String storedChanges = preferences.getString(ViewCrawler.SHARED_PREF_EDITS_FILE, null);
            String storedBindings = preferences.getString(ViewCrawler.SHARED_PREF_BINDINGS_KEY, null);
            if (storedChanges != null) {
                try {
                    this.mPersistentChanges.clear();
                    this.mPersistentTweaks.clear();
                    JSONArray jSONArray = new JSONArray(storedChanges);
                    int variantsLength = jSONArray.length();
                    for (int variantIx = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; variantIx < variantsLength; variantIx += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                        JSONObject nextVariant = jSONArray.getJSONObject(variantIx);
                        int variantIdPart = nextVariant.getInt(DBLikedChannelsHelper.KEY_ID);
                        Pair<Integer, Integer> pair = new Pair(Integer.valueOf(nextVariant.getInt("experiment_id")), Integer.valueOf(variantIdPart));
                        JSONArray actions = nextVariant.getJSONArray("actions");
                        for (i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < actions.length(); i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                            JSONObject change = actions.getJSONObject(i);
                            this.mPersistentChanges.add(new VariantChange(JSONUtils.optionalStringKey(change, "target_activity"), change, pair));
                        }
                        JSONArray tweaks = nextVariant.getJSONArray("tweaks");
                        int length = tweaks.length();
                        for (i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < length; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                            this.mPersistentTweaks.add(new VariantTweak(tweaks.getJSONObject(i), pair));
                        }
                    }
                } catch (JSONException e) {
                    Log.i(ViewCrawler.LOGTAG, "JSON error when initializing saved changes, clearing persistent memory", e);
                    android.content.SharedPreferences.Editor editor = preferences.edit();
                    editor.remove(ViewCrawler.SHARED_PREF_EDITS_FILE);
                    editor.remove(ViewCrawler.SHARED_PREF_BINDINGS_KEY);
                    editor.apply();
                }
            }
            if (storedBindings != null) {
                JSONArray bindings = new JSONArray(storedBindings);
                this.mPersistentEventBindings.clear();
                for (i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < bindings.length(); i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                    JSONObject event = bindings.getJSONObject(i);
                    this.mPersistentEventBindings.add(new Pair(JSONUtils.optionalStringKey(event, "target_activity"), event));
                }
            }
            applyVariantsAndEventBindings();
        }

        private void connectToEditor() {
            if (MPConfig.DEBUG) {
                Log.v(ViewCrawler.LOGTAG, "connecting to editor");
            }
            if (this.mEditorConnection == null || !this.mEditorConnection.isValid()) {
                SSLSocketFactory socketFactory = ViewCrawler.this.mConfig.getSSLSocketFactory();
                if (socketFactory != null) {
                    String url = MPConfig.getInstance(this.mContext).getEditorUrl() + this.mToken;
                    try {
                        this.mEditorConnection = new EditorConnection(new URI(url), new Editor(), socketFactory.createSocket());
                    } catch (URISyntaxException e) {
                        Log.e(ViewCrawler.LOGTAG, "Error parsing URI " + url + " for editor websocket", e);
                    } catch (EditorConnectionException e2) {
                        Log.e(ViewCrawler.LOGTAG, "Error connecting to URI " + url, e2);
                    } catch (IOException e3) {
                        Log.i(ViewCrawler.LOGTAG, "Can't create SSL Socket to connect to editor service", e3);
                    }
                } else if (MPConfig.DEBUG) {
                    Log.v(ViewCrawler.LOGTAG, "SSL is not available on this device, no connection will be attempted to the events editor.");
                }
            } else if (MPConfig.DEBUG) {
                Log.v(ViewCrawler.LOGTAG, "There is already a valid connection to an events editor.");
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void sendError(java.lang.String r7) {
            /*
            r6 = this;
            r3 = r6.mEditorConnection;
            if (r3 != 0) goto L_0x0005;
        L_0x0004:
            return;
        L_0x0005:
            r1 = new org.json.JSONObject;
            r1.<init>();
            r3 = "error_message";
            r1.put(r3, r7);	 Catch:{ JSONException -> 0x003f }
        L_0x000f:
            r2 = new java.io.OutputStreamWriter;
            r3 = r6.mEditorConnection;
            r3 = r3.getBufferedOutputStream();
            r2.<init>(r3);
            r3 = "{\"type\": \"error\", ";
            r2.write(r3);	 Catch:{ IOException -> 0x0048 }
            r3 = "\"payload\": ";
            r2.write(r3);	 Catch:{ IOException -> 0x0048 }
            r3 = r1.toString();	 Catch:{ IOException -> 0x0048 }
            r2.write(r3);	 Catch:{ IOException -> 0x0048 }
            r3 = "}";
            r2.write(r3);	 Catch:{ IOException -> 0x0048 }
            r2.close();	 Catch:{ IOException -> 0x0036 }
            goto L_0x0004;
        L_0x0036:
            r0 = move-exception;
            r3 = "MixpanelAPI.ViewCrawler";
            r4 = "Could not close output writer to editor";
            android.util.Log.e(r3, r4, r0);
            goto L_0x0004;
        L_0x003f:
            r0 = move-exception;
            r3 = "MixpanelAPI.ViewCrawler";
            r4 = "Apparently impossible JSONException";
            android.util.Log.e(r3, r4, r0);
            goto L_0x000f;
        L_0x0048:
            r0 = move-exception;
            r3 = "MixpanelAPI.ViewCrawler";
            r4 = "Can't write error message to editor";
            android.util.Log.e(r3, r4, r0);	 Catch:{ all -> 0x005d }
            r2.close();	 Catch:{ IOException -> 0x0054 }
            goto L_0x0004;
        L_0x0054:
            r0 = move-exception;
            r3 = "MixpanelAPI.ViewCrawler";
            r4 = "Could not close output writer to editor";
            android.util.Log.e(r3, r4, r0);
            goto L_0x0004;
        L_0x005d:
            r3 = move-exception;
            r2.close();	 Catch:{ IOException -> 0x0062 }
        L_0x0061:
            throw r3;
        L_0x0062:
            r0 = move-exception;
            r4 = "MixpanelAPI.ViewCrawler";
            r5 = "Could not close output writer to editor";
            android.util.Log.e(r4, r5, r0);
            goto L_0x0061;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mixpanel.android.viewcrawler.ViewCrawler.ViewCrawlerHandler.sendError(java.lang.String):void");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void sendDeviceInfo() {
            /*
            r12 = this;
            r8 = r12.mEditorConnection;
            if (r8 != 0) goto L_0x0005;
        L_0x0004:
            return;
        L_0x0005:
            r8 = r12.mEditorConnection;
            r4 = r8.getBufferedOutputStream();
            r3 = new android.util.JsonWriter;
            r8 = new java.io.OutputStreamWriter;
            r8.<init>(r4);
            r3.<init>(r8);
            r3.beginObject();	 Catch:{ IOException -> 0x009b }
            r8 = "type";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r9 = "device_info_response";
            r8.value(r9);	 Catch:{ IOException -> 0x009b }
            r8 = "payload";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r8.beginObject();	 Catch:{ IOException -> 0x009b }
            r8 = "device_type";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r9 = "Android";
            r8.value(r9);	 Catch:{ IOException -> 0x009b }
            r8 = "device_name";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r9 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x009b }
            r9.<init>();	 Catch:{ IOException -> 0x009b }
            r10 = android.os.Build.BRAND;	 Catch:{ IOException -> 0x009b }
            r9 = r9.append(r10);	 Catch:{ IOException -> 0x009b }
            r10 = "/";
            r9 = r9.append(r10);	 Catch:{ IOException -> 0x009b }
            r10 = android.os.Build.MODEL;	 Catch:{ IOException -> 0x009b }
            r9 = r9.append(r10);	 Catch:{ IOException -> 0x009b }
            r9 = r9.toString();	 Catch:{ IOException -> 0x009b }
            r8.value(r9);	 Catch:{ IOException -> 0x009b }
            r8 = "scaled_density";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r9 = com.mixpanel.android.viewcrawler.ViewCrawler.this;	 Catch:{ IOException -> 0x009b }
            r9 = r9.mScaledDensity;	 Catch:{ IOException -> 0x009b }
            r10 = (double) r9;	 Catch:{ IOException -> 0x009b }
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            r8 = com.mixpanel.android.viewcrawler.ViewCrawler.this;	 Catch:{ IOException -> 0x009b }
            r8 = r8.mDeviceInfo;	 Catch:{ IOException -> 0x009b }
            r8 = r8.entrySet();	 Catch:{ IOException -> 0x009b }
            r9 = r8.iterator();	 Catch:{ IOException -> 0x009b }
        L_0x007b:
            r8 = r9.hasNext();	 Catch:{ IOException -> 0x009b }
            if (r8 == 0) goto L_0x00b2;
        L_0x0081:
            r2 = r9.next();	 Catch:{ IOException -> 0x009b }
            r2 = (java.util.Map.Entry) r2;	 Catch:{ IOException -> 0x009b }
            r8 = r2.getKey();	 Catch:{ IOException -> 0x009b }
            r8 = (java.lang.String) r8;	 Catch:{ IOException -> 0x009b }
            r10 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r8 = r2.getValue();	 Catch:{ IOException -> 0x009b }
            r8 = (java.lang.String) r8;	 Catch:{ IOException -> 0x009b }
            r10.value(r8);	 Catch:{ IOException -> 0x009b }
            goto L_0x007b;
        L_0x009b:
            r1 = move-exception;
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Can't write device_info to server";
            android.util.Log.e(r8, r9, r1);	 Catch:{ all -> 0x0133 }
            r3.close();	 Catch:{ IOException -> 0x00a8 }
            goto L_0x0004;
        L_0x00a8:
            r1 = move-exception;
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Can't close websocket writer";
            android.util.Log.e(r8, r9, r1);
            goto L_0x0004;
        L_0x00b2:
            r8 = com.mixpanel.android.viewcrawler.ViewCrawler.this;	 Catch:{ IOException -> 0x009b }
            r8 = r8.mTweaks;	 Catch:{ IOException -> 0x009b }
            r6 = r8.getAllValues();	 Catch:{ IOException -> 0x009b }
            r8 = "tweaks";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r8.beginArray();	 Catch:{ IOException -> 0x009b }
            r8 = r6.entrySet();	 Catch:{ IOException -> 0x009b }
            r9 = r8.iterator();	 Catch:{ IOException -> 0x009b }
        L_0x00ce:
            r8 = r9.hasNext();	 Catch:{ IOException -> 0x009b }
            if (r8 == 0) goto L_0x01c8;
        L_0x00d4:
            r5 = r9.next();	 Catch:{ IOException -> 0x009b }
            r5 = (java.util.Map.Entry) r5;	 Catch:{ IOException -> 0x009b }
            r0 = r5.getValue();	 Catch:{ IOException -> 0x009b }
            r0 = (com.mixpanel.android.mpmetrics.Tweaks.TweakValue) r0;	 Catch:{ IOException -> 0x009b }
            r7 = r5.getKey();	 Catch:{ IOException -> 0x009b }
            r7 = (java.lang.String) r7;	 Catch:{ IOException -> 0x009b }
            r3.beginObject();	 Catch:{ IOException -> 0x009b }
            r8 = "name";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r8.value(r7);	 Catch:{ IOException -> 0x009b }
            r8 = "minimum";
            r10 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r8 = 0;
            r8 = (java.lang.Number) r8;	 Catch:{ IOException -> 0x009b }
            r10.value(r8);	 Catch:{ IOException -> 0x009b }
            r8 = "maximum";
            r10 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r8 = 0;
            r8 = (java.lang.Number) r8;	 Catch:{ IOException -> 0x009b }
            r10.value(r8);	 Catch:{ IOException -> 0x009b }
            r8 = r0.type;	 Catch:{ IOException -> 0x009b }
            switch(r8) {
                case 1: goto L_0x0138;
                case 2: goto L_0x0157;
                case 3: goto L_0x0181;
                case 4: goto L_0x01ab;
                default: goto L_0x010f;
            };	 Catch:{ IOException -> 0x009b }
        L_0x010f:
            r8 = "MixpanelAPI.ViewCrawler";
            r10 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x009b }
            r10.<init>();	 Catch:{ IOException -> 0x009b }
            r11 = "Unrecognized Tweak Type ";
            r10 = r10.append(r11);	 Catch:{ IOException -> 0x009b }
            r11 = r0.type;	 Catch:{ IOException -> 0x009b }
            r10 = r10.append(r11);	 Catch:{ IOException -> 0x009b }
            r11 = " encountered.";
            r10 = r10.append(r11);	 Catch:{ IOException -> 0x009b }
            r10 = r10.toString();	 Catch:{ IOException -> 0x009b }
            android.util.Log.wtf(r8, r10);	 Catch:{ IOException -> 0x009b }
        L_0x012f:
            r3.endObject();	 Catch:{ IOException -> 0x009b }
            goto L_0x00ce;
        L_0x0133:
            r8 = move-exception;
            r3.close();	 Catch:{ IOException -> 0x01e0 }
        L_0x0137:
            throw r8;
        L_0x0138:
            r8 = "type";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = "boolean";
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            r8 = "value";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = r0.getBooleanValue();	 Catch:{ IOException -> 0x009b }
            r10 = r10.booleanValue();	 Catch:{ IOException -> 0x009b }
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            goto L_0x012f;
        L_0x0157:
            r8 = "type";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = "number";
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            r8 = "encoding";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = "d";
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            r8 = "value";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = r0.getNumberValue();	 Catch:{ IOException -> 0x009b }
            r10 = r10.doubleValue();	 Catch:{ IOException -> 0x009b }
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            goto L_0x012f;
        L_0x0181:
            r8 = "type";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = "number";
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            r8 = "encoding";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = "l";
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            r8 = "value";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = r0.getNumberValue();	 Catch:{ IOException -> 0x009b }
            r10 = r10.longValue();	 Catch:{ IOException -> 0x009b }
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            goto L_0x012f;
        L_0x01ab:
            r8 = "type";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = "string";
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            r8 = "value";
            r8 = r3.name(r8);	 Catch:{ IOException -> 0x009b }
            r10 = r0.getStringValue();	 Catch:{ IOException -> 0x009b }
            r8.value(r10);	 Catch:{ IOException -> 0x009b }
            goto L_0x012f;
        L_0x01c8:
            r3.endArray();	 Catch:{ IOException -> 0x009b }
            r3.endObject();	 Catch:{ IOException -> 0x009b }
            r3.endObject();	 Catch:{ IOException -> 0x009b }
            r3.close();	 Catch:{ IOException -> 0x01d6 }
            goto L_0x0004;
        L_0x01d6:
            r1 = move-exception;
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Can't close websocket writer";
            android.util.Log.e(r8, r9, r1);
            goto L_0x0004;
        L_0x01e0:
            r1 = move-exception;
            r9 = "MixpanelAPI.ViewCrawler";
            r10 = "Can't close websocket writer";
            android.util.Log.e(r9, r10, r1);
            goto L_0x0137;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mixpanel.android.viewcrawler.ViewCrawler.ViewCrawlerHandler.sendDeviceInfo():void");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void sendSnapshot(org.json.JSONObject r12) {
            /*
            r11 = this;
            r6 = java.lang.System.currentTimeMillis();
            r8 = "payload";
            r2 = r12.getJSONObject(r8);	 Catch:{ JSONException -> 0x0036, BadInstructionsException -> 0x0044 }
            r8 = "config";
            r8 = r2.has(r8);	 Catch:{ JSONException -> 0x0036, BadInstructionsException -> 0x0044 }
            if (r8 == 0) goto L_0x0025;
        L_0x0012:
            r8 = r11.mProtocol;	 Catch:{ JSONException -> 0x0036, BadInstructionsException -> 0x0044 }
            r8 = r8.readSnapshotConfig(r2);	 Catch:{ JSONException -> 0x0036, BadInstructionsException -> 0x0044 }
            r11.mSnapshot = r8;	 Catch:{ JSONException -> 0x0036, BadInstructionsException -> 0x0044 }
            r8 = com.mixpanel.android.mpmetrics.MPConfig.DEBUG;	 Catch:{ JSONException -> 0x0036, BadInstructionsException -> 0x0044 }
            if (r8 == 0) goto L_0x0025;
        L_0x001e:
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Initializing snapshot with configuration";
            android.util.Log.v(r8, r9);	 Catch:{ JSONException -> 0x0036, BadInstructionsException -> 0x0044 }
        L_0x0025:
            r8 = r11.mSnapshot;
            if (r8 != 0) goto L_0x0054;
        L_0x0029:
            r8 = "No snapshot configuration (or a malformed snapshot configuration) was sent.";
            r11.sendError(r8);
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Mixpanel editor is misconfigured, sent a snapshot request without a valid configuration.";
            android.util.Log.w(r8, r9);
        L_0x0035:
            return;
        L_0x0036:
            r0 = move-exception;
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Payload with snapshot config required with snapshot request";
            android.util.Log.e(r8, r9, r0);
            r8 = "Payload with snapshot config required with snapshot request";
            r11.sendError(r8);
            goto L_0x0035;
        L_0x0044:
            r0 = move-exception;
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Editor sent malformed message with snapshot request";
            android.util.Log.e(r8, r9, r0);
            r8 = r0.getMessage();
            r11.sendError(r8);
            goto L_0x0035;
        L_0x0054:
            r8 = r11.mEditorConnection;
            r1 = r8.getBufferedOutputStream();
            r3 = new java.io.OutputStreamWriter;
            r3.<init>(r1);
            r8 = "{";
            r3.write(r8);	 Catch:{ IOException -> 0x00ad }
            r8 = "\"type\": \"snapshot_response\",";
            r3.write(r8);	 Catch:{ IOException -> 0x00ad }
            r8 = "\"payload\": {";
            r3.write(r8);	 Catch:{ IOException -> 0x00ad }
            r8 = "\"activities\":";
            r3.write(r8);	 Catch:{ IOException -> 0x00ad }
            r3.flush();	 Catch:{ IOException -> 0x00ad }
            r8 = r11.mSnapshot;	 Catch:{ IOException -> 0x00ad }
            r9 = com.mixpanel.android.viewcrawler.ViewCrawler.this;	 Catch:{ IOException -> 0x00ad }
            r9 = r9.mEditState;	 Catch:{ IOException -> 0x00ad }
            r8.snapshots(r9, r1);	 Catch:{ IOException -> 0x00ad }
            r8 = java.lang.System.currentTimeMillis();	 Catch:{ IOException -> 0x00ad }
            r4 = r8 - r6;
            r8 = ",\"snapshot_time_millis\": ";
            r3.write(r8);	 Catch:{ IOException -> 0x00ad }
            r8 = java.lang.Long.toString(r4);	 Catch:{ IOException -> 0x00ad }
            r3.write(r8);	 Catch:{ IOException -> 0x00ad }
            r8 = "}";
            r3.write(r8);	 Catch:{ IOException -> 0x00ad }
            r8 = "}";
            r3.write(r8);	 Catch:{ IOException -> 0x00ad }
            r3.close();	 Catch:{ IOException -> 0x00a4 }
            goto L_0x0035;
        L_0x00a4:
            r0 = move-exception;
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Can't close writer.";
            android.util.Log.e(r8, r9, r0);
            goto L_0x0035;
        L_0x00ad:
            r0 = move-exception;
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Can't write snapshot request to server";
            android.util.Log.e(r8, r9, r0);	 Catch:{ all -> 0x00c4 }
            r3.close();	 Catch:{ IOException -> 0x00ba }
            goto L_0x0035;
        L_0x00ba:
            r0 = move-exception;
            r8 = "MixpanelAPI.ViewCrawler";
            r9 = "Can't close writer.";
            android.util.Log.e(r8, r9, r0);
            goto L_0x0035;
        L_0x00c4:
            r8 = move-exception;
            r3.close();	 Catch:{ IOException -> 0x00c9 }
        L_0x00c8:
            throw r8;
        L_0x00c9:
            r0 = move-exception;
            r9 = "MixpanelAPI.ViewCrawler";
            r10 = "Can't close writer.";
            android.util.Log.e(r9, r10, r0);
            goto L_0x00c8;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mixpanel.android.viewcrawler.ViewCrawler.ViewCrawlerHandler.sendSnapshot(org.json.JSONObject):void");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void sendReportTrackToEditor(java.lang.String r8) {
            /*
            r7 = this;
            r4 = r7.mEditorConnection;
            if (r4 != 0) goto L_0x0005;
        L_0x0004:
            return;
        L_0x0005:
            r4 = r7.mEditorConnection;
            r2 = r4.getBufferedOutputStream();
            r3 = new java.io.OutputStreamWriter;
            r3.<init>(r2);
            r1 = new android.util.JsonWriter;
            r1.<init>(r3);
            r1.beginObject();	 Catch:{ IOException -> 0x004c }
            r4 = "type";
            r4 = r1.name(r4);	 Catch:{ IOException -> 0x004c }
            r5 = "track_message";
            r4.value(r5);	 Catch:{ IOException -> 0x004c }
            r4 = "payload";
            r1.name(r4);	 Catch:{ IOException -> 0x004c }
            r1.beginObject();	 Catch:{ IOException -> 0x004c }
            r4 = "event_name";
            r4 = r1.name(r4);	 Catch:{ IOException -> 0x004c }
            r4.value(r8);	 Catch:{ IOException -> 0x004c }
            r1.endObject();	 Catch:{ IOException -> 0x004c }
            r1.endObject();	 Catch:{ IOException -> 0x004c }
            r1.flush();	 Catch:{ IOException -> 0x004c }
            r1.close();	 Catch:{ IOException -> 0x0043 }
            goto L_0x0004;
        L_0x0043:
            r0 = move-exception;
            r4 = "MixpanelAPI.ViewCrawler";
            r5 = "Can't close writer.";
            android.util.Log.e(r4, r5, r0);
            goto L_0x0004;
        L_0x004c:
            r0 = move-exception;
            r4 = "MixpanelAPI.ViewCrawler";
            r5 = "Can't write track_message to server";
            android.util.Log.e(r4, r5, r0);	 Catch:{ all -> 0x0061 }
            r1.close();	 Catch:{ IOException -> 0x0058 }
            goto L_0x0004;
        L_0x0058:
            r0 = move-exception;
            r4 = "MixpanelAPI.ViewCrawler";
            r5 = "Can't close writer.";
            android.util.Log.e(r4, r5, r0);
            goto L_0x0004;
        L_0x0061:
            r4 = move-exception;
            r1.close();	 Catch:{ IOException -> 0x0066 }
        L_0x0065:
            throw r4;
        L_0x0066:
            r0 = move-exception;
            r5 = "MixpanelAPI.ViewCrawler";
            r6 = "Can't close writer.";
            android.util.Log.e(r5, r6, r0);
            goto L_0x0065;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mixpanel.android.viewcrawler.ViewCrawler.ViewCrawlerHandler.sendReportTrackToEditor(java.lang.String):void");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void sendLayoutError(com.mixpanel.android.viewcrawler.ViewVisitor.LayoutErrorMessage r8) {
            /*
            r7 = this;
            r4 = r7.mEditorConnection;
            if (r4 != 0) goto L_0x0005;
        L_0x0004:
            return;
        L_0x0005:
            r4 = r7.mEditorConnection;
            r2 = r4.getBufferedOutputStream();
            r3 = new java.io.OutputStreamWriter;
            r3.<init>(r2);
            r1 = new android.util.JsonWriter;
            r1.<init>(r3);
            r1.beginObject();	 Catch:{ IOException -> 0x004e }
            r4 = "type";
            r4 = r1.name(r4);	 Catch:{ IOException -> 0x004e }
            r5 = "layout_error";
            r4.value(r5);	 Catch:{ IOException -> 0x004e }
            r4 = "exception_type";
            r4 = r1.name(r4);	 Catch:{ IOException -> 0x004e }
            r5 = r8.getErrorType();	 Catch:{ IOException -> 0x004e }
            r4.value(r5);	 Catch:{ IOException -> 0x004e }
            r4 = "cid";
            r4 = r1.name(r4);	 Catch:{ IOException -> 0x004e }
            r5 = r8.getName();	 Catch:{ IOException -> 0x004e }
            r4.value(r5);	 Catch:{ IOException -> 0x004e }
            r1.endObject();	 Catch:{ IOException -> 0x004e }
            r1.close();	 Catch:{ IOException -> 0x0045 }
            goto L_0x0004;
        L_0x0045:
            r0 = move-exception;
            r4 = "MixpanelAPI.ViewCrawler";
            r5 = "Can't close writer.";
            android.util.Log.e(r4, r5, r0);
            goto L_0x0004;
        L_0x004e:
            r0 = move-exception;
            r4 = "MixpanelAPI.ViewCrawler";
            r5 = "Can't write track_message to server";
            android.util.Log.e(r4, r5, r0);	 Catch:{ all -> 0x0063 }
            r1.close();	 Catch:{ IOException -> 0x005a }
            goto L_0x0004;
        L_0x005a:
            r0 = move-exception;
            r4 = "MixpanelAPI.ViewCrawler";
            r5 = "Can't close writer.";
            android.util.Log.e(r4, r5, r0);
            goto L_0x0004;
        L_0x0063:
            r4 = move-exception;
            r1.close();	 Catch:{ IOException -> 0x0068 }
        L_0x0067:
            throw r4;
        L_0x0068:
            r0 = move-exception;
            r5 = "MixpanelAPI.ViewCrawler";
            r6 = "Can't close writer.";
            android.util.Log.e(r5, r6, r0);
            goto L_0x0067;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mixpanel.android.viewcrawler.ViewCrawler.ViewCrawlerHandler.sendLayoutError(com.mixpanel.android.viewcrawler.ViewVisitor$LayoutErrorMessage):void");
        }

        private void handleEditorChangeReceived(JSONObject changeMessage) {
            try {
                JSONArray actions = changeMessage.getJSONObject("payload").getJSONArray("actions");
                for (int i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < actions.length(); i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                    JSONObject change = actions.getJSONObject(i);
                    String targetActivity = JSONUtils.optionalStringKey(change, "target_activity");
                    this.mEditorChanges.put(change.getString(ProfilesDBHelper.COLUMN_NAME), new Pair(targetActivity, change));
                }
                applyVariantsAndEventBindings();
            } catch (JSONException e) {
                Log.e(ViewCrawler.LOGTAG, "Bad change request received", e);
            }
        }

        private void handleEditorBindingsCleared(JSONObject clearMessage) {
            try {
                JSONArray actions = clearMessage.getJSONObject("payload").getJSONArray("actions");
                for (int i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < actions.length(); i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                    this.mEditorChanges.remove(actions.getString(i));
                }
            } catch (JSONException e) {
                Log.e(ViewCrawler.LOGTAG, "Bad clear request received", e);
            }
            applyVariantsAndEventBindings();
        }

        private void handleEditorTweaksReceived(JSONObject tweaksMessage) {
            try {
                this.mEditorTweaks.clear();
                JSONArray tweaks = tweaksMessage.getJSONObject("payload").getJSONArray("tweaks");
                int length = tweaks.length();
                for (int i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < length; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                    this.mEditorTweaks.add(tweaks.getJSONObject(i));
                }
            } catch (JSONException e) {
                Log.e(ViewCrawler.LOGTAG, "Bad tweaks received", e);
            }
            applyVariantsAndEventBindings();
        }

        private void handleVariantsReceived(JSONArray variants) {
            android.content.SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(ViewCrawler.SHARED_PREF_EDITS_FILE, variants.toString());
            editor.apply();
            initializeChanges();
        }

        private void handleEventBindingsReceived(JSONArray eventBindings) {
            android.content.SharedPreferences.Editor editor = getSharedPreferences().edit();
            editor.putString(ViewCrawler.SHARED_PREF_BINDINGS_KEY, eventBindings.toString());
            editor.apply();
            initializeChanges();
        }

        private void handleEditorBindingsReceived(JSONObject message) {
            try {
                JSONArray eventBindings = message.getJSONObject("payload").getJSONArray("events");
                int eventCount = eventBindings.length();
                this.mEditorEventBindings.clear();
                for (int i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < eventCount; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                    try {
                        JSONObject event = eventBindings.getJSONObject(i);
                        this.mEditorEventBindings.add(new Pair(JSONUtils.optionalStringKey(event, "target_activity"), event));
                    } catch (JSONException e) {
                        Log.e(ViewCrawler.LOGTAG, "Bad event binding received from editor in " + eventBindings.toString(), e);
                    }
                }
                applyVariantsAndEventBindings();
            } catch (JSONException e2) {
                Log.e(ViewCrawler.LOGTAG, "Bad event bindings received", e2);
            }
        }

        private void handleEditorClosed() {
            this.mEditorChanges.clear();
            this.mEditorEventBindings.clear();
            this.mSnapshot = null;
            if (MPConfig.DEBUG) {
                Log.v(ViewCrawler.LOGTAG, "Editor closed- freeing snapshot");
            }
            applyVariantsAndEventBindings();
            for (String assetUrl : this.mEditorAssetUrls) {
                this.mImageStore.deleteStorage(assetUrl);
            }
        }

        private void applyVariantsAndEventBindings() {
            int i;
            List<Pair<String, ViewVisitor>> newVisitors = new ArrayList();
            Set<Pair<Integer, Integer>> toTrack = new HashSet();
            int size = this.mPersistentChanges.size();
            for (i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < size; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                VariantChange changeInfo = (VariantChange) this.mPersistentChanges.get(i);
                try {
                    newVisitors.add(new Pair(changeInfo.activityName, this.mProtocol.readEdit(changeInfo.change).visitor));
                    if (!this.mSeenExperiments.contains(changeInfo.variantId)) {
                        toTrack.add(changeInfo.variantId);
                    }
                } catch (CantGetEditAssetsException e) {
                    Log.v(ViewCrawler.LOGTAG, "Can't load assets for an edit, won't apply the change now", e);
                } catch (InapplicableInstructionsException e2) {
                    Log.i(ViewCrawler.LOGTAG, e2.getMessage());
                } catch (BadInstructionsException e3) {
                    Log.e(ViewCrawler.LOGTAG, "Bad persistent change request cannot be applied.", e3);
                }
            }
            size = this.mPersistentTweaks.size();
            for (i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < size; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                VariantTweak tweakInfo = (VariantTweak) this.mPersistentTweaks.get(i);
                try {
                    Pair<String, Object> tweakValue = this.mProtocol.readTweak(tweakInfo.tweak);
                    ViewCrawler.this.mTweaks.set((String) tweakValue.first, tweakValue.second);
                    if (!this.mSeenExperiments.contains(tweakInfo.variantId)) {
                        toTrack.add(tweakInfo.variantId);
                    }
                } catch (BadInstructionsException e32) {
                    Log.e(ViewCrawler.LOGTAG, "Bad editor tweak cannot be applied.", e32);
                }
            }
            for (Pair<String, JSONObject> changeInfo2 : this.mEditorChanges.values()) {
                try {
                    Pair<String, JSONObject> changeInfo22;
                    Edit edit = this.mProtocol.readEdit((JSONObject) changeInfo22.second);
                    newVisitors.add(new Pair(changeInfo22.first, edit.visitor));
                    this.mEditorAssetUrls.addAll(edit.imageUrls);
                } catch (CantGetEditAssetsException e4) {
                    Log.v(ViewCrawler.LOGTAG, "Can't load assets for an edit, won't apply the change now", e4);
                } catch (InapplicableInstructionsException e22) {
                    Log.i(ViewCrawler.LOGTAG, e22.getMessage());
                } catch (BadInstructionsException e322) {
                    Log.e(ViewCrawler.LOGTAG, "Bad editor change request cannot be applied.", e322);
                }
            }
            size = this.mEditorTweaks.size();
            for (i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < size; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                try {
                    tweakValue = this.mProtocol.readTweak((JSONObject) this.mEditorTweaks.get(i));
                    ViewCrawler.this.mTweaks.set((String) tweakValue.first, tweakValue.second);
                } catch (BadInstructionsException e3222) {
                    Log.e(ViewCrawler.LOGTAG, "Strange tweaks received", e3222);
                }
            }
            size = this.mPersistentEventBindings.size();
            for (i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < size; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                changeInfo22 = (Pair) this.mPersistentEventBindings.get(i);
                try {
                    newVisitors.add(new Pair(changeInfo22.first, this.mProtocol.readEventBinding((JSONObject) changeInfo22.second, ViewCrawler.this.mDynamicEventTracker)));
                } catch (InapplicableInstructionsException e222) {
                    Log.i(ViewCrawler.LOGTAG, e222.getMessage());
                } catch (BadInstructionsException e32222) {
                    Log.e(ViewCrawler.LOGTAG, "Bad persistent event binding cannot be applied.", e32222);
                }
            }
            size = this.mEditorEventBindings.size();
            for (i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < size; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                changeInfo22 = (Pair) this.mEditorEventBindings.get(i);
                try {
                    newVisitors.add(new Pair(changeInfo22.first, this.mProtocol.readEventBinding((JSONObject) changeInfo22.second, ViewCrawler.this.mDynamicEventTracker)));
                } catch (InapplicableInstructionsException e2222) {
                    Log.i(ViewCrawler.LOGTAG, e2222.getMessage());
                } catch (BadInstructionsException e322222) {
                    Log.e(ViewCrawler.LOGTAG, "Bad editor event binding cannot be applied.", e322222);
                }
            }
            Map<String, List<ViewVisitor>> editMap = new HashMap();
            int totalEdits = newVisitors.size();
            for (i = ViewCrawler.MESSAGE_INITIALIZE_CHANGES; i < totalEdits; i += ViewCrawler.MESSAGE_CONNECT_TO_EDITOR) {
                List<ViewVisitor> mapElement;
                Pair<String, ViewVisitor> next = (Pair) newVisitors.get(i);
                if (editMap.containsKey(next.first)) {
                    mapElement = (List) editMap.get(next.first);
                } else {
                    mapElement = new ArrayList();
                    editMap.put(next.first, mapElement);
                }
                mapElement.add(next.second);
            }
            ViewCrawler.this.mEditState.setEdits(editMap);
            this.mSeenExperiments.addAll(toTrack);
            if (toTrack.size() > 0) {
                JSONObject variantObject = new JSONObject();
                try {
                    for (Pair<Integer, Integer> variant : toTrack) {
                        int experimentId = ((Integer) variant.first).intValue();
                        int variantId = ((Integer) variant.second).intValue();
                        JSONObject trackProps = new JSONObject();
                        trackProps.put("$experiment_id", experimentId);
                        trackProps.put("$variant_id", variantId);
                        ViewCrawler.this.mMixpanel.track("$experiment_started", trackProps);
                        variantObject.put(Integer.toString(experimentId), variantId);
                    }
                } catch (JSONException e5) {
                    Log.wtf(ViewCrawler.LOGTAG, "Could not build JSON for reporting experiment start", e5);
                }
                ViewCrawler.this.mMixpanel.getPeople().merge("$experiments", variantObject);
                final JSONObject jSONObject = variantObject;
                ViewCrawler.this.mMixpanel.updateSuperProperties(new SuperPropertyUpdate() {
                    public JSONObject update(JSONObject in) {
                        try {
                            in.put("$experiments", jSONObject);
                        } catch (JSONException e) {
                            Log.wtf(ViewCrawler.LOGTAG, "Can't write $experiments super property", e);
                        }
                        return in;
                    }
                });
            }
        }

        private SharedPreferences getSharedPreferences() {
            return this.mContext.getSharedPreferences(ViewCrawler.SHARED_PREF_EDITS_FILE + this.mToken, ViewCrawler.MESSAGE_INITIALIZE_CHANGES);
        }
    }

    public ViewCrawler(Context context, String token, MixpanelAPI mixpanel, Tweaks tweaks) {
        this.mConfig = MPConfig.getInstance(context);
        this.mTweaks = tweaks;
        this.mDeviceInfo = mixpanel.getDeviceInfo();
        this.mScaledDensity = Resources.getSystem().getDisplayMetrics().scaledDensity;
        ((Application) context.getApplicationContext()).registerActivityLifecycleCallbacks(new LifecycleCallbacks());
        HandlerThread thread = new HandlerThread(ViewCrawler.class.getCanonicalName());
        thread.setPriority(MESSAGE_HANDLE_EDITOR_CHANGES_CLEARED);
        thread.start();
        this.mMessageThreadHandler = new ViewCrawlerHandler(context, token, thread.getLooper(), this);
        this.mDynamicEventTracker = new DynamicEventTracker(mixpanel, this.mMessageThreadHandler);
        this.mMixpanel = mixpanel;
        this.mTweaks.addOnTweakDeclaredListener(new OnTweakDeclaredListener() {
            public void onTweakDeclared() {
                ViewCrawler.this.mMessageThreadHandler.sendMessage(ViewCrawler.this.mMessageThreadHandler.obtainMessage(ViewCrawler.MESSAGE_SEND_DEVICE_INFO));
            }
        });
    }

    public void startUpdates() {
        this.mMessageThreadHandler.start();
        this.mMessageThreadHandler.sendMessage(this.mMessageThreadHandler.obtainMessage(MESSAGE_INITIALIZE_CHANGES));
    }

    public Tweaks getTweaks() {
        return this.mTweaks;
    }

    public void setEventBindings(JSONArray bindings) {
        Message msg = this.mMessageThreadHandler.obtainMessage(MESSAGE_EVENT_BINDINGS_RECEIVED);
        msg.obj = bindings;
        this.mMessageThreadHandler.sendMessage(msg);
    }

    public void setVariants(JSONArray variants) {
        Message msg = this.mMessageThreadHandler.obtainMessage(MESSAGE_VARIANTS_RECEIVED);
        msg.obj = variants;
        this.mMessageThreadHandler.sendMessage(msg);
    }

    public void reportTrack(String eventName) {
        Message m = this.mMessageThreadHandler.obtainMessage();
        m.what = MESSAGE_SEND_EVENT_TRACKED;
        m.obj = eventName;
        this.mMessageThreadHandler.sendMessage(m);
    }

    public void onLayoutError(LayoutErrorMessage e) {
        Message m = this.mMessageThreadHandler.obtainMessage();
        m.what = MESSAGE_SEND_LAYOUT_ERROR;
        m.obj = e;
        this.mMessageThreadHandler.sendMessage(m);
    }
}
