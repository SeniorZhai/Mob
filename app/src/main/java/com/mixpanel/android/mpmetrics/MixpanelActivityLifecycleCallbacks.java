package com.mixpanel.android.mpmetrics;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

@TargetApi(16)
class MixpanelActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {
    private final MixpanelAPI mMpInstance;

    public MixpanelActivityLifecycleCallbacks(MixpanelAPI mpInstance) {
        this.mMpInstance = mpInstance;
    }

    public void onActivityStarted(Activity activity) {
        if (activity.isTaskRoot()) {
            this.mMpInstance.getPeople().showNotificationIfAvailable(activity);
            this.mMpInstance.getPeople().showSurveyIfAvailable(activity);
        }
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivityDestroyed(Activity activity) {
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    public void onActivityResumed(Activity activity) {
        this.mMpInstance.getPeople().joinExperimentIfAvailable();
    }

    public void onActivityStopped(Activity activity) {
    }
}
