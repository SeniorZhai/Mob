package com.soundcloud.android.crop;

import android.app.Activity;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.Iterator;

abstract class MonitoredActivity extends Activity {
    private final ArrayList<LifeCycleListener> listeners = new ArrayList();

    public interface LifeCycleListener {
        void onActivityCreated(MonitoredActivity monitoredActivity);

        void onActivityDestroyed(MonitoredActivity monitoredActivity);

        void onActivityStarted(MonitoredActivity monitoredActivity);

        void onActivityStopped(MonitoredActivity monitoredActivity);
    }

    public static class LifeCycleAdapter implements LifeCycleListener {
        public void onActivityCreated(MonitoredActivity activity) {
        }

        public void onActivityDestroyed(MonitoredActivity activity) {
        }

        public void onActivityStarted(MonitoredActivity activity) {
        }

        public void onActivityStopped(MonitoredActivity activity) {
        }
    }

    MonitoredActivity() {
    }

    public void addLifeCycleListener(LifeCycleListener listener) {
        if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }

    public void removeLifeCycleListener(LifeCycleListener listener) {
        this.listeners.remove(listener);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Iterator i$ = this.listeners.iterator();
        while (i$.hasNext()) {
            ((LifeCycleListener) i$.next()).onActivityCreated(this);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        Iterator i$ = this.listeners.iterator();
        while (i$.hasNext()) {
            ((LifeCycleListener) i$.next()).onActivityDestroyed(this);
        }
    }

    protected void onStart() {
        super.onStart();
        Iterator i$ = this.listeners.iterator();
        while (i$.hasNext()) {
            ((LifeCycleListener) i$.next()).onActivityStarted(this);
        }
    }

    protected void onStop() {
        super.onStop();
        Iterator i$ = this.listeners.iterator();
        while (i$.hasNext()) {
            ((LifeCycleListener) i$.next()).onActivityStopped(this);
        }
    }
}
