package com.firebase.client;

import com.firebase.client.Logger.Level;
import com.firebase.client.core.AuthExpirationBehavior;
import com.firebase.client.core.Context;
import java.util.List;

public class Config extends Context {
    public synchronized void setLogger(Logger logger) {
        assertUnfrozen();
        this.logger = logger;
    }

    public synchronized void setEventTarget(EventTarget eventTarget) {
        assertUnfrozen();
        this.eventTarget = eventTarget;
    }

    public synchronized void setLogLevel(Level logLevel) {
        assertUnfrozen();
        this.logLevel = logLevel;
    }

    public synchronized void setDebugLogComponents(List<String> debugComponents) {
        assertUnfrozen();
        setLogLevel(Level.DEBUG);
        this.loggedComponents = debugComponents;
    }

    void setRunLoop(RunLoop runLoop) {
        this.runLoop = runLoop;
    }

    synchronized void setCredentialStore(CredentialStore store) {
        assertUnfrozen();
        this.credentialStore = store;
    }

    public synchronized void setAuthenticationServer(String host) {
        assertUnfrozen();
        this.authenticationServer = host;
    }

    public synchronized void setSessionPersistenceKey(String sessionKey) {
        assertUnfrozen();
        if (sessionKey == null || sessionKey.isEmpty()) {
            throw new IllegalArgumentException("Session identifier is not allowed to be empty or null!");
        }
        this.persistenceKey = sessionKey;
    }

    public synchronized void enablePersistence() {
        assertUnfrozen();
        setPersistenceEnabled(true);
    }

    public synchronized void setPersistenceEnabled(boolean isEnabled) {
        assertUnfrozen();
        this.persistenceEnabled = isEnabled;
        if (isEnabled) {
            setAuthExpirationBehavior(AuthExpirationBehavior.PAUSE_WRITES_UNTIL_REAUTH);
        } else {
            setAuthExpirationBehavior(AuthExpirationBehavior.DEFAULT);
        }
    }

    private synchronized void setAuthExpirationBehavior(AuthExpirationBehavior behavior) {
        assertUnfrozen();
        this.authExpirationBehavior = behavior;
    }

    public synchronized void setPersistenceCacheSizeBytes(long cacheSizeInBytes) {
        assertUnfrozen();
        if (cacheSizeInBytes < 1048576) {
            throw new FirebaseException("The minimum cache size must be at least 1MB");
        } else if (cacheSizeInBytes > 104857600) {
            throw new FirebaseException("Firebase currently doesn't support a cache size larger than 100MB");
        } else {
            this.cacheSize = cacheSizeInBytes;
        }
    }
}
