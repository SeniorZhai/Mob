package com.mixpanel.android.mpmetrics;

import android.util.Log;
import com.mixpanel.android.viewcrawler.UpdatesFromMixpanel;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;

class DecideMessages {
    private static final String LOGTAG = "MixpanelAPI.DecideUpdts";
    private String mDistinctId = null;
    private final OnNewResultsListener mListener;
    private final Set<Integer> mNotificationIds = new HashSet();
    private final Set<Integer> mSurveyIds = new HashSet();
    private final String mToken;
    private final List<InAppNotification> mUnseenNotifications = new LinkedList();
    private final List<Survey> mUnseenSurveys = new LinkedList();
    private final UpdatesFromMixpanel mUpdatesFromMixpanel;
    private JSONArray mVariants;

    public interface OnNewResultsListener {
        void onNewResults();
    }

    public DecideMessages(String token, OnNewResultsListener listener, UpdatesFromMixpanel updatesFromMixpanel) {
        this.mToken = token;
        this.mListener = listener;
        this.mUpdatesFromMixpanel = updatesFromMixpanel;
    }

    public String getToken() {
        return this.mToken;
    }

    public synchronized void setDistinctId(String distinctId) {
        this.mUnseenSurveys.clear();
        this.mUnseenNotifications.clear();
        this.mDistinctId = distinctId;
    }

    public synchronized String getDistinctId() {
        return this.mDistinctId;
    }

    public synchronized void reportResults(List<Survey> newSurveys, List<InAppNotification> newNotifications, JSONArray eventBindings, JSONArray variants) {
        boolean newContent = false;
        this.mUpdatesFromMixpanel.setEventBindings(eventBindings);
        for (Survey s : newSurveys) {
            int id = s.getId();
            if (!this.mSurveyIds.contains(Integer.valueOf(id))) {
                this.mSurveyIds.add(Integer.valueOf(id));
                this.mUnseenSurveys.add(s);
                newContent = true;
            }
        }
        for (InAppNotification n : newNotifications) {
            id = n.getId();
            if (!this.mNotificationIds.contains(Integer.valueOf(id))) {
                this.mNotificationIds.add(Integer.valueOf(id));
                this.mUnseenNotifications.add(n);
                newContent = true;
            }
        }
        this.mVariants = variants;
        if (MPConfig.DEBUG) {
            Log.v(LOGTAG, "New Decide content has become available. " + newSurveys.size() + " surveys and " + newNotifications.size() + " notifications have been added.");
        }
        if (newContent && hasUpdatesAvailable() && this.mListener != null) {
            this.mListener.onNewResults();
        }
    }

    public synchronized Survey getSurvey(boolean replace) {
        Survey survey;
        if (this.mUnseenSurveys.isEmpty()) {
            survey = null;
        } else {
            survey = (Survey) this.mUnseenSurveys.remove(0);
            if (replace) {
                this.mUnseenSurveys.add(this.mUnseenSurveys.size(), survey);
            }
        }
        return survey;
    }

    public synchronized Survey getSurvey(int id, boolean replace) {
        Survey survey;
        survey = null;
        int i = 0;
        while (i < this.mUnseenSurveys.size()) {
            if (((Survey) this.mUnseenSurveys.get(i)).getId() == id) {
                survey = (Survey) this.mUnseenSurveys.get(i);
                if (!replace) {
                    this.mUnseenSurveys.remove(i);
                }
            } else {
                i++;
            }
        }
        return survey;
    }

    public synchronized JSONArray getVariants() {
        return this.mVariants;
    }

    public synchronized InAppNotification getNotification(boolean replace) {
        InAppNotification inAppNotification;
        if (this.mUnseenNotifications.isEmpty()) {
            if (MPConfig.DEBUG) {
                Log.v(LOGTAG, "No unseen notifications exist, none will be returned.");
            }
            inAppNotification = null;
        } else {
            inAppNotification = (InAppNotification) this.mUnseenNotifications.remove(0);
            if (replace) {
                this.mUnseenNotifications.add(this.mUnseenNotifications.size(), inAppNotification);
            } else if (MPConfig.DEBUG) {
                Log.v(LOGTAG, "Recording notification " + inAppNotification + " as seen.");
            }
        }
        return inAppNotification;
    }

    public synchronized InAppNotification getNotification(int id, boolean replace) {
        InAppNotification notif;
        notif = null;
        int i = 0;
        while (i < this.mUnseenNotifications.size()) {
            if (((InAppNotification) this.mUnseenNotifications.get(i)).getId() == id) {
                notif = (InAppNotification) this.mUnseenNotifications.get(i);
                if (!replace) {
                    this.mUnseenNotifications.remove(i);
                }
            } else {
                i++;
            }
        }
        return notif;
    }

    public synchronized boolean hasUpdatesAvailable() {
        boolean z;
        z = (this.mUnseenNotifications.isEmpty() && this.mUnseenSurveys.isEmpty() && this.mVariants == null) ? false : true;
        return z;
    }
}
