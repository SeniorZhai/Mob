package com.firebase.client.core;

import com.facebook.internal.AnalyticsEvents;
import com.firebase.client.CredentialStore;
import com.firebase.client.EventTarget;
import com.firebase.client.Firebase;
import com.firebase.client.Logger;
import com.firebase.client.Logger.Level;
import com.firebase.client.RunLoop;
import com.firebase.client.authentication.NoopCredentialStore;
import com.firebase.client.core.persistence.PersistenceManager;
import com.firebase.client.utilities.DefaultLogger;
import com.firebase.client.utilities.DefaultRunLoop;
import com.firebase.client.utilities.LogWrapper;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

enum JvmPlatform implements Platform {
    INSTANCE;

    public Logger newLogger(Context ctx, Level level, List<String> components) {
        return new DefaultLogger(level, components);
    }

    public EventTarget newEventTarget(Context ctx) {
        int i = 1;
        final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, i, 3, TimeUnit.SECONDS, new LinkedBlockingQueue(), new ThreadFactory() {
            ThreadFactory wrappedFactory;

            {
                this.wrappedFactory = Executors.defaultThreadFactory();
            }

            public Thread newThread(Runnable r) {
                Thread thread = this.wrappedFactory.newThread(r);
                thread.setName("FirebaseEventTarget");
                thread.setDaemon(true);
                return thread;
            }
        });
        return new EventTarget() {
            public void postEvent(Runnable r) {
                executor.execute(r);
            }

            public void shutdown() {
                executor.setCorePoolSize(0);
            }

            public void restart() {
                executor.setCorePoolSize(1);
            }
        };
    }

    public RunLoop newRunLoop(Context context) {
        final LogWrapper logger = context.getLogger("RunLoop");
        return new DefaultRunLoop() {
            public void handleException(Throwable e) {
                logger.error("Uncaught exception in Firebase runloop (" + Firebase.getSdkVersion() + "). Please report to support@firebase.com", e);
            }
        };
    }

    public String getUserAgent(Context ctx) {
        return System.getProperty("java.specification.version", AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_UNKNOWN) + "/" + System.getProperty("java.vm.name", "Unknown JVM");
    }

    public String getPlatformVersion() {
        return "jvm-" + Firebase.getSdkVersion();
    }

    public PersistenceManager createPersistenceManager(Context ctx, String namespace) {
        return null;
    }

    public CredentialStore newCredentialStore(Context ctx) {
        return new NoopCredentialStore(ctx);
    }

    public void runBackgroundTask(Context ctx, final Runnable r) {
        new Thread() {
            public void run() {
                try {
                    r.run();
                } catch (Throwable e) {
                    System.err.println("An unexpected error occurred. Please contact support@firebase.com. Details: " + e.getMessage());
                    RuntimeException runtimeException = new RuntimeException(e);
                }
            }
        }.start();
    }
}
