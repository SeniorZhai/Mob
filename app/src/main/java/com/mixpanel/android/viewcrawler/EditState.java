package com.mixpanel.android.viewcrawler;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class EditState extends UIThreadSet<Activity> {
    private static final String LOGTAG = "MixpanelAPI.EditState";
    private final Set<EditBinding> mCurrentEdits = new HashSet();
    private final Map<String, List<ViewVisitor>> mIntendedEdits = new HashMap();
    private final Handler mUiThreadHandler = new Handler(Looper.getMainLooper());

    private static class EditBinding implements OnGlobalLayoutListener, Runnable {
        private boolean mAlive = true;
        private volatile boolean mDying = false;
        private final ViewVisitor mEdit;
        private final Handler mHandler;
        private final WeakReference<View> mViewRoot;

        public EditBinding(View viewRoot, ViewVisitor edit, Handler uiThreadHandler) {
            this.mEdit = edit;
            this.mViewRoot = new WeakReference(viewRoot);
            this.mHandler = uiThreadHandler;
            ViewTreeObserver observer = viewRoot.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.addOnGlobalLayoutListener(this);
            }
            run();
        }

        public void onGlobalLayout() {
            run();
        }

        public void run() {
            if (this.mAlive) {
                View viewRoot = (View) this.mViewRoot.get();
                if (viewRoot == null || this.mDying) {
                    cleanUp();
                    return;
                }
                this.mEdit.visit(viewRoot);
                this.mHandler.removeCallbacks(this);
                this.mHandler.postDelayed(this, 1000);
            }
        }

        public void kill() {
            this.mDying = true;
            this.mHandler.post(this);
        }

        private void cleanUp() {
            if (this.mAlive) {
                View viewRoot = (View) this.mViewRoot.get();
                if (viewRoot != null) {
                    ViewTreeObserver observer = viewRoot.getViewTreeObserver();
                    if (observer.isAlive()) {
                        observer.removeGlobalOnLayoutListener(this);
                    }
                }
                this.mEdit.cleanup();
            }
            this.mAlive = false;
        }
    }

    public void add(Activity newOne) {
        super.add(newOne);
        applyEditsOnUiThread();
    }

    public void remove(Activity oldOne) {
        super.remove(oldOne);
    }

    public void setEdits(Map<String, List<ViewVisitor>> newEdits) {
        synchronized (this.mCurrentEdits) {
            for (EditBinding stale : this.mCurrentEdits) {
                stale.kill();
            }
            this.mCurrentEdits.clear();
        }
        synchronized (this.mIntendedEdits) {
            this.mIntendedEdits.clear();
            this.mIntendedEdits.putAll(newEdits);
        }
        applyEditsOnUiThread();
    }

    private void applyEditsOnUiThread() {
        if (Thread.currentThread() == this.mUiThreadHandler.getLooper().getThread()) {
            applyIntendedEdits();
        } else {
            this.mUiThreadHandler.post(new Runnable() {
                public void run() {
                    EditState.this.applyIntendedEdits();
                }
            });
        }
    }

    private void applyIntendedEdits() {
        for (Activity activity : getAll()) {
            String activityName = activity.getClass().getCanonicalName();
            View rootView = activity.getWindow().getDecorView().getRootView();
            synchronized (this.mIntendedEdits) {
                List<ViewVisitor> specificChanges = (List) this.mIntendedEdits.get(activityName);
                List<ViewVisitor> wildcardChanges = (List) this.mIntendedEdits.get(null);
            }
            if (specificChanges != null) {
                applyChangesFromList(rootView, specificChanges);
            }
            if (wildcardChanges != null) {
                applyChangesFromList(rootView, wildcardChanges);
            }
        }
    }

    private void applyChangesFromList(View rootView, List<ViewVisitor> changes) {
        synchronized (this.mCurrentEdits) {
            int size = changes.size();
            for (int i = 0; i < size; i++) {
                this.mCurrentEdits.add(new EditBinding(rootView, (ViewVisitor) changes.get(i), this.mUiThreadHandler));
            }
        }
    }
}
