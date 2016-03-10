package com.mixpanel.android.mpmetrics;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build.VERSION;
import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint({"CommitPrefEdits"})
class PersistentIdentity {
    private static final String LOGTAG = "MixpanelAPI.PIdentity";
    private static boolean sReferrerPrefsDirty = true;
    private static final Object sReferrerPrefsLock = new Object();
    private String mEventsDistinctId;
    private boolean mIdentitiesLoaded = false;
    private final Future<SharedPreferences> mLoadReferrerPreferences;
    private final Future<SharedPreferences> mLoadStoredPreferences;
    private String mPeopleDistinctId;
    private final OnSharedPreferenceChangeListener mReferrerChangeListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            synchronized (PersistentIdentity.sReferrerPrefsLock) {
                PersistentIdentity.this.readReferrerProperties();
                PersistentIdentity.sReferrerPrefsDirty = false;
            }
        }
    };
    private Map<String, String> mReferrerPropertiesCache = null;
    private JSONObject mSuperPropertiesCache = null;
    private JSONArray mWaitingPeopleRecords;

    private void readSuperProperties() {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:280)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:167)
*/
        /*
        r6 = this;
        r3 = r6.mLoadStoredPreferences;	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r1 = r3.get();	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r1 = (android.content.SharedPreferences) r1;	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r3 = "super_properties";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r4 = "{}";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r2 = r1.getString(r3, r4);	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r3 = com.mixpanel.android.mpmetrics.MPConfig.DEBUG;	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        if (r3 == 0) goto L_0x002e;	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
    L_0x0016:
        r3 = "MixpanelAPI.PIdentity";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r4 = new java.lang.StringBuilder;	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r4.<init>();	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r5 = "Loading Super Properties ";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r4 = r4.append(r5);	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r4 = r4.append(r2);	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r4 = r4.toString();	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        android.util.Log.v(r3, r4);	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
    L_0x002e:
        r3 = new org.json.JSONObject;	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r3.<init>(r2);	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r6.mSuperPropertiesCache = r3;	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r3 = r6.mSuperPropertiesCache;
        if (r3 != 0) goto L_0x0040;
    L_0x0039:
        r3 = new org.json.JSONObject;
        r3.<init>();
        r6.mSuperPropertiesCache = r3;
    L_0x0040:
        return;
    L_0x0041:
        r0 = move-exception;
        r3 = "MixpanelAPI.PIdentity";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r4 = "Cannot load superProperties from SharedPreferences.";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r5 = r0.getCause();	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        android.util.Log.e(r3, r4, r5);	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r3 = r6.mSuperPropertiesCache;
        if (r3 != 0) goto L_0x0040;
    L_0x0051:
        r3 = new org.json.JSONObject;
        r3.<init>();
        r6.mSuperPropertiesCache = r3;
        goto L_0x0040;
    L_0x0059:
        r0 = move-exception;
        r3 = "MixpanelAPI.PIdentity";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r4 = "Cannot load superProperties from SharedPreferences.";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        android.util.Log.e(r3, r4, r0);	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r3 = r6.mSuperPropertiesCache;
        if (r3 != 0) goto L_0x0040;
    L_0x0065:
        r3 = new org.json.JSONObject;
        r3.<init>();
        r6.mSuperPropertiesCache = r3;
        goto L_0x0040;
    L_0x006d:
        r0 = move-exception;
        r3 = "MixpanelAPI.PIdentity";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r4 = "Cannot parse stored superProperties";	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        android.util.Log.e(r3, r4);	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r6.storeSuperProperties();	 Catch:{ ExecutionException -> 0x0041, InterruptedException -> 0x0059, JSONException -> 0x006d, all -> 0x0084 }
        r3 = r6.mSuperPropertiesCache;
        if (r3 != 0) goto L_0x0040;
    L_0x007c:
        r3 = new org.json.JSONObject;
        r3.<init>();
        r6.mSuperPropertiesCache = r3;
        goto L_0x0040;
    L_0x0084:
        r3 = move-exception;
        r4 = r6.mSuperPropertiesCache;
        if (r4 != 0) goto L_0x0090;
    L_0x0089:
        r4 = new org.json.JSONObject;
        r4.<init>();
        r6.mSuperPropertiesCache = r4;
    L_0x0090:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mixpanel.android.mpmetrics.PersistentIdentity.readSuperProperties():void");
    }

    public static JSONArray waitingPeopleRecordsForSending(SharedPreferences storedPreferences) {
        JSONArray ret = null;
        String peopleDistinctId = storedPreferences.getString("people_distinct_id", null);
        String waitingPeopleRecords = storedPreferences.getString("waiting_array", null);
        if (!(waitingPeopleRecords == null || peopleDistinctId == null)) {
            try {
                JSONArray waitingObjects = new JSONArray(waitingPeopleRecords);
                ret = new JSONArray();
                for (int i = 0; i < waitingObjects.length(); i++) {
                    try {
                        JSONObject ob = waitingObjects.getJSONObject(i);
                        ob.put("$distinct_id", peopleDistinctId);
                        ret.put(ob);
                    } catch (JSONException e) {
                        Log.e(LOGTAG, "Unparsable object found in waiting people records", e);
                    }
                }
                Editor editor = storedPreferences.edit();
                editor.remove("waiting_array");
                writeEdits(editor);
            } catch (JSONException e2) {
                Log.e(LOGTAG, "Waiting people records were unreadable.");
                return null;
            }
        }
        return ret;
    }

    public static void writeReferrerPrefs(Context context, String preferencesName, Map<String, String> properties) {
        synchronized (sReferrerPrefsLock) {
            Editor editor = context.getSharedPreferences(preferencesName, 0).edit();
            editor.clear();
            for (Entry<String, String> entry : properties.entrySet()) {
                editor.putString((String) entry.getKey(), (String) entry.getValue());
            }
            writeEdits(editor);
            sReferrerPrefsDirty = true;
        }
    }

    public PersistentIdentity(Future<SharedPreferences> referrerPreferences, Future<SharedPreferences> storedPreferences) {
        this.mLoadReferrerPreferences = referrerPreferences;
        this.mLoadStoredPreferences = storedPreferences;
    }

    public synchronized void addSuperPropertiesToObject(JSONObject ob) {
        JSONObject superProperties = getSuperPropertiesCache();
        Iterator<?> superIter = superProperties.keys();
        while (superIter.hasNext()) {
            String key = (String) superIter.next();
            try {
                ob.put(key, superProperties.get(key));
            } catch (JSONException e) {
                Log.wtf(LOGTAG, "Object read from one JSON Object cannot be written to another", e);
            }
        }
    }

    public synchronized void updateSuperProperties(SuperPropertyUpdate updates) {
        JSONObject oldPropCache = getSuperPropertiesCache();
        JSONObject copy = new JSONObject();
        try {
            Iterator<String> keys = oldPropCache.keys();
            while (keys.hasNext()) {
                String k = (String) keys.next();
                copy.put(k, oldPropCache.get(k));
            }
            JSONObject replacementCache = updates.update(copy);
            if (replacementCache == null) {
                Log.w(LOGTAG, "An update to Mixpanel's super properties returned null, and will have no effect.");
            } else {
                this.mSuperPropertiesCache = replacementCache;
                storeSuperProperties();
            }
        } catch (JSONException e) {
            Log.wtf(LOGTAG, "Can't copy from one JSONObject to another", e);
        }
        return;
    }

    public Map<String, String> getReferrerProperties() {
        synchronized (sReferrerPrefsLock) {
            if (sReferrerPrefsDirty || this.mReferrerPropertiesCache == null) {
                readReferrerProperties();
                sReferrerPrefsDirty = false;
            }
        }
        return this.mReferrerPropertiesCache;
    }

    public synchronized String getEventsDistinctId() {
        if (!this.mIdentitiesLoaded) {
            readIdentities();
        }
        return this.mEventsDistinctId;
    }

    public synchronized void setEventsDistinctId(String eventsDistinctId) {
        if (!this.mIdentitiesLoaded) {
            readIdentities();
        }
        this.mEventsDistinctId = eventsDistinctId;
        writeIdentities();
    }

    public synchronized String getPeopleDistinctId() {
        if (!this.mIdentitiesLoaded) {
            readIdentities();
        }
        return this.mPeopleDistinctId;
    }

    public synchronized void setPeopleDistinctId(String peopleDistinctId) {
        if (!this.mIdentitiesLoaded) {
            readIdentities();
        }
        this.mPeopleDistinctId = peopleDistinctId;
        writeIdentities();
    }

    public synchronized void storeWaitingPeopleRecord(JSONObject record) {
        if (!this.mIdentitiesLoaded) {
            readIdentities();
        }
        if (this.mWaitingPeopleRecords == null) {
            this.mWaitingPeopleRecords = new JSONArray();
        }
        this.mWaitingPeopleRecords.put(record);
        writeIdentities();
    }

    public synchronized JSONArray waitingPeopleRecordsForSending() {
        JSONArray ret;
        ret = null;
        try {
            ret = waitingPeopleRecordsForSending((SharedPreferences) this.mLoadStoredPreferences.get());
            readIdentities();
        } catch (ExecutionException e) {
            Log.e(LOGTAG, "Couldn't read waiting people records from shared preferences.", e.getCause());
        } catch (InterruptedException e2) {
            Log.e(LOGTAG, "Couldn't read waiting people records from shared preferences.", e2);
        }
        return ret;
    }

    public synchronized void clearPreferences() {
        try {
            Editor prefsEdit = ((SharedPreferences) this.mLoadStoredPreferences.get()).edit();
            prefsEdit.clear();
            writeEdits(prefsEdit);
            readSuperProperties();
            readIdentities();
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e2) {
            throw new RuntimeException(e2.getCause());
        }
    }

    public synchronized void registerSuperProperties(JSONObject superProperties) {
        JSONObject propCache = getSuperPropertiesCache();
        Iterator<?> iter = superProperties.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            try {
                propCache.put(key, superProperties.get(key));
            } catch (JSONException e) {
                Log.e(LOGTAG, "Exception registering super property.", e);
            }
        }
        storeSuperProperties();
    }

    public synchronized void storePushId(String registrationId) {
        try {
            Editor editor = ((SharedPreferences) this.mLoadStoredPreferences.get()).edit();
            editor.putString("push_id", registrationId);
            writeEdits(editor);
        } catch (ExecutionException e) {
            Log.e(LOGTAG, "Can't write push id to shared preferences", e.getCause());
        } catch (InterruptedException e2) {
            Log.e(LOGTAG, "Can't write push id to shared preferences", e2);
        }
    }

    public synchronized void clearPushId() {
        try {
            Editor editor = ((SharedPreferences) this.mLoadStoredPreferences.get()).edit();
            editor.remove("push_id");
            writeEdits(editor);
        } catch (ExecutionException e) {
            Log.e(LOGTAG, "Can't write push id to shared preferences", e.getCause());
        } catch (InterruptedException e2) {
            Log.e(LOGTAG, "Can't write push id to shared preferences", e2);
        }
    }

    public synchronized String getPushId() {
        String ret;
        ret = null;
        try {
            ret = ((SharedPreferences) this.mLoadStoredPreferences.get()).getString("push_id", null);
        } catch (ExecutionException e) {
            Log.e(LOGTAG, "Can't write push id to shared preferences", e.getCause());
        } catch (InterruptedException e2) {
            Log.e(LOGTAG, "Can't write push id to shared preferences", e2);
        }
        return ret;
    }

    public synchronized void unregisterSuperProperty(String superPropertyName) {
        getSuperPropertiesCache().remove(superPropertyName);
        storeSuperProperties();
    }

    public synchronized void registerSuperPropertiesOnce(JSONObject superProperties) {
        JSONObject propCache = getSuperPropertiesCache();
        Iterator<?> iter = superProperties.keys();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            if (!propCache.has(key)) {
                try {
                    propCache.put(key, superProperties.get(key));
                } catch (JSONException e) {
                    Log.e(LOGTAG, "Exception registering super property.", e);
                }
            }
        }
        storeSuperProperties();
    }

    public synchronized void clearSuperProperties() {
        this.mSuperPropertiesCache = new JSONObject();
        storeSuperProperties();
    }

    private JSONObject getSuperPropertiesCache() {
        if (this.mSuperPropertiesCache == null) {
            readSuperProperties();
        }
        return this.mSuperPropertiesCache;
    }

    private void readReferrerProperties() {
        this.mReferrerPropertiesCache = new HashMap();
        try {
            SharedPreferences referrerPrefs = (SharedPreferences) this.mLoadReferrerPreferences.get();
            referrerPrefs.unregisterOnSharedPreferenceChangeListener(this.mReferrerChangeListener);
            referrerPrefs.registerOnSharedPreferenceChangeListener(this.mReferrerChangeListener);
            for (Entry<String, ?> entry : referrerPrefs.getAll().entrySet()) {
                this.mReferrerPropertiesCache.put((String) entry.getKey(), entry.getValue().toString());
            }
        } catch (ExecutionException e) {
            Log.e(LOGTAG, "Cannot load referrer properties from shared preferences.", e.getCause());
        } catch (InterruptedException e2) {
            Log.e(LOGTAG, "Cannot load referrer properties from shared preferences.", e2);
        }
    }

    private void storeSuperProperties() {
        if (this.mSuperPropertiesCache == null) {
            Log.e(LOGTAG, "storeSuperProperties should not be called with uninitialized superPropertiesCache.");
            return;
        }
        String props = this.mSuperPropertiesCache.toString();
        if (MPConfig.DEBUG) {
            Log.v(LOGTAG, "Storing Super Properties " + props);
        }
        try {
            Editor editor = ((SharedPreferences) this.mLoadStoredPreferences.get()).edit();
            editor.putString("super_properties", props);
            writeEdits(editor);
        } catch (ExecutionException e) {
            Log.e(LOGTAG, "Cannot store superProperties in shared preferences.", e.getCause());
        } catch (InterruptedException e2) {
            Log.e(LOGTAG, "Cannot store superProperties in shared preferences.", e2);
        }
    }

    private void readIdentities() {
        SharedPreferences prefs = null;
        try {
            prefs = (SharedPreferences) this.mLoadStoredPreferences.get();
        } catch (ExecutionException e) {
            Log.e(LOGTAG, "Cannot read distinct ids from sharedPreferences.", e.getCause());
        } catch (InterruptedException e2) {
            Log.e(LOGTAG, "Cannot read distinct ids from sharedPreferences.", e2);
        }
        if (prefs != null) {
            this.mEventsDistinctId = prefs.getString("events_distinct_id", null);
            this.mPeopleDistinctId = prefs.getString("people_distinct_id", null);
            this.mWaitingPeopleRecords = null;
            String storedWaitingRecord = prefs.getString("waiting_array", null);
            if (storedWaitingRecord != null) {
                try {
                    this.mWaitingPeopleRecords = new JSONArray(storedWaitingRecord);
                } catch (JSONException e3) {
                    Log.e(LOGTAG, "Could not interpret waiting people JSON record " + storedWaitingRecord);
                }
            }
            if (this.mEventsDistinctId == null) {
                this.mEventsDistinctId = UUID.randomUUID().toString();
                writeIdentities();
            }
            this.mIdentitiesLoaded = true;
        }
    }

    private void writeIdentities() {
        try {
            Editor prefsEditor = ((SharedPreferences) this.mLoadStoredPreferences.get()).edit();
            prefsEditor.putString("events_distinct_id", this.mEventsDistinctId);
            prefsEditor.putString("people_distinct_id", this.mPeopleDistinctId);
            if (this.mWaitingPeopleRecords == null) {
                prefsEditor.remove("waiting_array");
            } else {
                prefsEditor.putString("waiting_array", this.mWaitingPeopleRecords.toString());
            }
            writeEdits(prefsEditor);
        } catch (ExecutionException e) {
            Log.e(LOGTAG, "Can't write distinct ids to shared preferences.", e.getCause());
        } catch (InterruptedException e2) {
            Log.e(LOGTAG, "Can't write distinct ids to shared preferences.", e2);
        }
    }

    @TargetApi(9)
    private static void writeEdits(Editor editor) {
        if (VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }
}
