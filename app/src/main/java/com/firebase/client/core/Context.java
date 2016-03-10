package com.firebase.client.core;

import com.firebase.client.CredentialStore;
import com.firebase.client.EventTarget;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseException;
import com.firebase.client.Logger;
import com.firebase.client.Logger.Level;
import com.firebase.client.RunLoop;
import com.firebase.client.authentication.Constants;
import com.firebase.client.core.persistence.NoopPersistenceManager;
import com.firebase.client.core.persistence.PersistenceManager;
import com.firebase.client.utilities.LogWrapper;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class Context {
    private static final long DEFAULT_CACHE_SIZE = 10485760;
    private static android.content.Context androidContext;
    private static Platform platform;
    protected AuthExpirationBehavior authExpirationBehavior = AuthExpirationBehavior.DEFAULT;
    protected String authenticationServer;
    protected long cacheSize = DEFAULT_CACHE_SIZE;
    protected CredentialStore credentialStore;
    protected EventTarget eventTarget;
    private PersistenceManager forcedPersistenceManager;
    private boolean frozen = false;
    protected Level logLevel = Level.INFO;
    protected List<String> loggedComponents;
    protected Logger logger;
    protected boolean persistenceEnabled;
    protected String persistenceKey;
    protected RunLoop runLoop;
    private boolean stopped = false;
    protected String userAgent;

    private Platform getPlatform() {
        if (platform == null) {
            if (AndroidSupport.isAndroid()) {
                throw new RuntimeException("You need to set the Android context using Firebase.setAndroidContext() before using Firebase.");
            }
            platform = JvmPlatform.INSTANCE;
        }
        return platform;
    }

    public static synchronized void setAndroidContext(android.content.Context context) {
        synchronized (Context.class) {
            if (androidContext == null) {
                androidContext = context.getApplicationContext();
                try {
                    platform = (Platform) Class.forName("com.firebase.client.android.AndroidPlatform").getConstructor(new Class[]{android.content.Context.class}).newInstance(new Object[]{context});
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Android classes not found. Are you using the firebase-client-android artifact?");
                } catch (InvocationTargetException e2) {
                    throw new RuntimeException("Something went wrong, please report to support@firebase.com", e2);
                } catch (NoSuchMethodException e3) {
                    throw new RuntimeException("Something went wrong, please report to support@firebase.com", e3);
                } catch (InstantiationException e4) {
                    throw new RuntimeException("Something went wrong, please report to support@firebase.com", e4);
                } catch (IllegalAccessException e5) {
                    throw new RuntimeException("Something went wrong, please report to support@firebase.com", e5);
                }
            }
        }
    }

    public boolean isFrozen() {
        return this.frozen;
    }

    public boolean isStopped() {
        return this.stopped;
    }

    synchronized void freeze() {
        if (!this.frozen) {
            this.frozen = true;
            initServices();
        }
    }

    public void requireStarted() {
        if (this.stopped) {
            restartServices();
            this.stopped = false;
        }
    }

    private void initServices() {
        ensureLogger();
        getPlatform();
        ensureUserAgent();
        ensureEventTarget();
        ensureRunLoop();
        ensureSessionIdentifier();
        ensureCredentialStore();
    }

    private void restartServices() {
        this.eventTarget.restart();
        this.runLoop.restart();
    }

    void stop() {
        this.stopped = true;
        this.eventTarget.shutdown();
        this.runLoop.shutdown();
    }

    protected void assertUnfrozen() {
        if (isFrozen()) {
            throw new FirebaseException("Modifications to Config objects must occur before they are in use");
        }
    }

    public LogWrapper getLogger(String component) {
        return new LogWrapper(this.logger, component);
    }

    public LogWrapper getLogger(String component, String prefix) {
        return new LogWrapper(this.logger, component, prefix);
    }

    PersistenceManager getPersistenceManager(String firebaseId) {
        if (this.forcedPersistenceManager != null) {
            return this.forcedPersistenceManager;
        }
        if (!this.persistenceEnabled) {
            return new NoopPersistenceManager();
        }
        PersistenceManager cache = platform.createPersistenceManager(this, firebaseId);
        if (cache != null) {
            return cache;
        }
        throw new IllegalArgumentException("You have enabled persistence, but persistence is not supported on this platform. If you have any questions around persistence please contact support@firebase.com.");
    }

    public boolean isPersistenceEnabled() {
        return this.persistenceEnabled;
    }

    public AuthExpirationBehavior getAuthExpirationBehavior() {
        return this.authExpirationBehavior;
    }

    public long getPersistenceCacheSizeBytes() {
        return this.cacheSize;
    }

    void forcePersistenceManager(PersistenceManager persistenceManager) {
        this.forcedPersistenceManager = persistenceManager;
    }

    public EventTarget getEventTarget() {
        return this.eventTarget;
    }

    public RunLoop getRunLoop() {
        return this.runLoop;
    }

    public void runBackgroundTask(Runnable r) {
        getPlatform().runBackgroundTask(this, r);
    }

    public String getUserAgent() {
        return this.userAgent;
    }

    public String getPlatformVersion() {
        return getPlatform().getPlatformVersion();
    }

    public String getSessionPersistenceKey() {
        return this.persistenceKey;
    }

    public CredentialStore getCredentialStore() {
        return this.credentialStore;
    }

    public String getAuthenticationServer() {
        if (this.authenticationServer == null) {
            return Constants.FIREBASE_AUTH_DEFAULT_API_HOST;
        }
        return this.authenticationServer;
    }

    public boolean isCustomAuthenticationServerSet() {
        return this.authenticationServer != null;
    }

    private void ensureLogger() {
        if (this.logger == null) {
            this.logger = getPlatform().newLogger(this, this.logLevel, this.loggedComponents);
        }
    }

    private void ensureRunLoop() {
        if (this.runLoop == null) {
            this.runLoop = platform.newRunLoop(this);
        }
    }

    private void ensureEventTarget() {
        if (this.eventTarget == null) {
            this.eventTarget = getPlatform().newEventTarget(this);
        }
    }

    private void ensureUserAgent() {
        if (this.userAgent == null) {
            this.userAgent = buildUserAgent(getPlatform().getUserAgent(this));
        }
    }

    private void ensureCredentialStore() {
        if (this.credentialStore == null) {
            this.credentialStore = getPlatform().newCredentialStore(this);
        }
    }

    private void ensureSessionIdentifier() {
        if (this.persistenceKey == null) {
            this.persistenceKey = "default";
        }
    }

    private String buildUserAgent(String platformAgent) {
        return "Firebase/" + Constants.WIRE_PROTOCOL_VERSION + "/" + Firebase.getSdkVersion() + "/" + platformAgent;
    }
}
