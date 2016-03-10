package com.firebase.client.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.firebase.client.CredentialStore;

public class AndroidCredentialStore implements CredentialStore {
    private static final String ANDROID_SHARED_PREFERENCE_NAME = "com.firebase.authentication.credentials";
    private final SharedPreferences sharedPreferences;

    public AndroidCredentialStore(Context context) {
        this.sharedPreferences = context.getSharedPreferences(ANDROID_SHARED_PREFERENCE_NAME, 0);
    }

    private String buildKey(String firebaseId, String sessionId) {
        return firebaseId + "/" + sessionId;
    }

    public String loadCredential(String firebaseId, String sessionId) {
        return this.sharedPreferences.getString(buildKey(firebaseId, sessionId), null);
    }

    public boolean storeCredential(String firebaseId, String sessionId, String credential) {
        Editor editor = this.sharedPreferences.edit();
        editor.putString(buildKey(firebaseId, sessionId), credential);
        return editor.commit();
    }

    public boolean clearCredential(String firebaseId, String sessionId) {
        Editor editor = this.sharedPreferences.edit();
        editor.remove(buildKey(firebaseId, sessionId));
        return editor.commit();
    }
}
