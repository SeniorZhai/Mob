package com.facebook.appevents;

import android.os.Bundle;
import com.facebook.LoggingBehavior;
import com.facebook.internal.Logger;
import com.mobcrush.mobcrush.ChatFragment;
import java.io.Serializable;
import java.util.Locale;

class FacebookTimeSpentData implements Serializable {
    private static final long APP_ACTIVATE_SUPPRESSION_PERIOD_IN_MILLISECONDS = 300000;
    private static final long FIRST_TIME_LOAD_RESUME_TIME = -1;
    private static final long[] INACTIVE_SECONDS_QUANTA = new long[]{APP_ACTIVATE_SUPPRESSION_PERIOD_IN_MILLISECONDS, 900000, 1800000, 3600000, 21600000, 43200000, ChatFragment.MID_MUTE_TIME, 172800000, 259200000, 604800000, 1209600000, 1814400000, 2419200000L, 5184000000L, 7776000000L, 10368000000L, 12960000000L, 15552000000L, 31536000000L};
    private static final long INTERRUPTION_THRESHOLD_MILLISECONDS = 1000;
    private static final long NUM_MILLISECONDS_IDLE_TO_BE_NEW_SESSION = 60000;
    private static final String TAG = AppEventsLogger.class.getCanonicalName();
    private static final long serialVersionUID = 1;
    private String firstOpenSourceApplication;
    private int interruptionCount;
    private boolean isAppActive;
    private boolean isWarmLaunch;
    private long lastActivateEventLoggedTime;
    private long lastResumeTime;
    private long lastSuspendTime;
    private long millisecondsSpentInSession;

    private static class SerializationProxyV1 implements Serializable {
        private static final long serialVersionUID = 6;
        private final int interruptionCount;
        private final long lastResumeTime;
        private final long lastSuspendTime;
        private final long millisecondsSpentInSession;

        SerializationProxyV1(long lastResumeTime, long lastSuspendTime, long millisecondsSpentInSession, int interruptionCount) {
            this.lastResumeTime = lastResumeTime;
            this.lastSuspendTime = lastSuspendTime;
            this.millisecondsSpentInSession = millisecondsSpentInSession;
            this.interruptionCount = interruptionCount;
        }

        private Object readResolve() {
            return new FacebookTimeSpentData(this.lastResumeTime, this.lastSuspendTime, this.millisecondsSpentInSession, this.interruptionCount);
        }
    }

    private static class SerializationProxyV2 implements Serializable {
        private static final long serialVersionUID = 6;
        private final String firstOpenSourceApplication;
        private final int interruptionCount;
        private final long lastResumeTime;
        private final long lastSuspendTime;
        private final long millisecondsSpentInSession;

        SerializationProxyV2(long lastResumeTime, long lastSuspendTime, long millisecondsSpentInSession, int interruptionCount, String firstOpenSourceApplication) {
            this.lastResumeTime = lastResumeTime;
            this.lastSuspendTime = lastSuspendTime;
            this.millisecondsSpentInSession = millisecondsSpentInSession;
            this.interruptionCount = interruptionCount;
            this.firstOpenSourceApplication = firstOpenSourceApplication;
        }

        private Object readResolve() {
            return new FacebookTimeSpentData(this.lastResumeTime, this.lastSuspendTime, this.millisecondsSpentInSession, this.interruptionCount, this.firstOpenSourceApplication);
        }
    }

    private FacebookTimeSpentData(long lastResumeTime, long lastSuspendTime, long millisecondsSpentInSession, int interruptionCount) {
        resetSession();
        this.lastResumeTime = lastResumeTime;
        this.lastSuspendTime = lastSuspendTime;
        this.millisecondsSpentInSession = millisecondsSpentInSession;
        this.interruptionCount = interruptionCount;
    }

    FacebookTimeSpentData() {
        resetSession();
    }

    private FacebookTimeSpentData(long lastResumeTime, long lastSuspendTime, long millisecondsSpentInSession, int interruptionCount, String firstOpenSourceApplication) {
        resetSession();
        this.lastResumeTime = lastResumeTime;
        this.lastSuspendTime = lastSuspendTime;
        this.millisecondsSpentInSession = millisecondsSpentInSession;
        this.interruptionCount = interruptionCount;
        this.firstOpenSourceApplication = firstOpenSourceApplication;
    }

    private Object writeReplace() {
        return new SerializationProxyV2(this.lastResumeTime, this.lastSuspendTime, this.millisecondsSpentInSession, this.interruptionCount, this.firstOpenSourceApplication);
    }

    void onSuspend(AppEventsLogger logger, long eventTime) {
        if (this.isAppActive) {
            long now = eventTime;
            long delta = now - this.lastResumeTime;
            if (delta < 0) {
                Logger.log(LoggingBehavior.APP_EVENTS, TAG, "Clock skew detected");
                delta = 0;
            }
            this.millisecondsSpentInSession += delta;
            this.lastSuspendTime = now;
            this.isAppActive = false;
            return;
        }
        Logger.log(LoggingBehavior.APP_EVENTS, TAG, "Suspend for inactive app");
    }

    void onResume(AppEventsLogger logger, long eventTime, String sourceApplicationInfo) {
        long now = eventTime;
        if (isColdLaunch() || now - this.lastActivateEventLoggedTime > APP_ACTIVATE_SUPPRESSION_PERIOD_IN_MILLISECONDS) {
            Bundle eventParams = new Bundle();
            eventParams.putString(AppEventsConstants.EVENT_PARAM_SOURCE_APPLICATION, sourceApplicationInfo);
            logger.logEvent(AppEventsConstants.EVENT_NAME_ACTIVATED_APP, eventParams);
            this.lastActivateEventLoggedTime = now;
        }
        if (this.isAppActive) {
            Logger.log(LoggingBehavior.APP_EVENTS, TAG, "Resume for active app");
            return;
        }
        long interruptionDurationMillis = wasSuspendedEver() ? now - this.lastSuspendTime : 0;
        if (interruptionDurationMillis < 0) {
            Logger.log(LoggingBehavior.APP_EVENTS, TAG, "Clock skew detected");
            interruptionDurationMillis = 0;
        }
        if (interruptionDurationMillis > NUM_MILLISECONDS_IDLE_TO_BE_NEW_SESSION) {
            logAppDeactivatedEvent(logger, interruptionDurationMillis);
        } else if (interruptionDurationMillis > INTERRUPTION_THRESHOLD_MILLISECONDS) {
            this.interruptionCount++;
        }
        if (this.interruptionCount == 0) {
            this.firstOpenSourceApplication = sourceApplicationInfo;
        }
        this.lastResumeTime = now;
        this.isAppActive = true;
    }

    private void logAppDeactivatedEvent(AppEventsLogger logger, long interruptionDurationMillis) {
        Bundle eventParams = new Bundle();
        eventParams.putInt(AppEventsConstants.EVENT_NAME_SESSION_INTERRUPTIONS, this.interruptionCount);
        eventParams.putString(AppEventsConstants.EVENT_NAME_TIME_BETWEEN_SESSIONS, String.format(Locale.ROOT, "session_quanta_%d", new Object[]{Integer.valueOf(getQuantaIndex(interruptionDurationMillis))}));
        eventParams.putString(AppEventsConstants.EVENT_PARAM_SOURCE_APPLICATION, this.firstOpenSourceApplication);
        logger.logEvent(AppEventsConstants.EVENT_NAME_DEACTIVATED_APP, (double) (this.millisecondsSpentInSession / INTERRUPTION_THRESHOLD_MILLISECONDS), eventParams);
        resetSession();
    }

    private static int getQuantaIndex(long timeBetweenSessions) {
        int quantaIndex = 0;
        while (quantaIndex < INACTIVE_SECONDS_QUANTA.length && INACTIVE_SECONDS_QUANTA[quantaIndex] < timeBetweenSessions) {
            quantaIndex++;
        }
        return quantaIndex;
    }

    private void resetSession() {
        this.isAppActive = false;
        this.lastResumeTime = FIRST_TIME_LOAD_RESUME_TIME;
        this.lastSuspendTime = FIRST_TIME_LOAD_RESUME_TIME;
        this.interruptionCount = 0;
        this.millisecondsSpentInSession = 0;
    }

    private boolean wasSuspendedEver() {
        return this.lastSuspendTime != FIRST_TIME_LOAD_RESUME_TIME;
    }

    private boolean isColdLaunch() {
        boolean result = !this.isWarmLaunch;
        this.isWarmLaunch = true;
        return result;
    }
}
