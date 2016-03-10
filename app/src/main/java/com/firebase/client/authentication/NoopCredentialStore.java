package com.firebase.client.authentication;

import com.firebase.client.CredentialStore;
import com.firebase.client.core.Context;
import com.firebase.client.utilities.LogWrapper;

public class NoopCredentialStore implements CredentialStore {
    private final LogWrapper logger;

    public NoopCredentialStore(Context context) {
        this.logger = context.getLogger("CredentialStore");
    }

    public String loadCredential(String firebaseId, String sessionId) {
        return null;
    }

    public boolean storeCredential(String firebaseId, String sessionId, String credentialData) {
        this.logger.warn("Using no-op credential store. Not persisting credentials! If you want to persist authentication, please use a custom implementation of CredentialStore.");
        return true;
    }

    public boolean clearCredential(String firebaseId, String sessionId) {
        return true;
    }
}
