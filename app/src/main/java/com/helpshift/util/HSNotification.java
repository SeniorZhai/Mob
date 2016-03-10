package com.helpshift.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import com.helpshift.D.plurals;
import com.helpshift.D.string;
import com.helpshift.HSApiData;
import com.helpshift.HSConversation;
import com.helpshift.HSStorage;
import com.helpshift.Helpshift;
import com.helpshift.Helpshift.HelpshiftDelegate;
import com.helpshift.constants.MessageColumns;
import com.helpshift.constants.Tables;
import com.helpshift.models.Issue;
import com.helpshift.res.values.HSConsts;
import com.helpshift.storage.IssuesDataSource;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import java.text.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class HSNotification {
    private static final String APP_NAME = "app_name";
    private static String TAG = Meta.TAG;
    private static HSApiData data;

    public static void showNotif(Context c, Issue issue, int messCnt, String chatLaunchSource, Intent intent) {
        String appName;
        Bundle extras = intent.getExtras();
        if (extras == null || !extras.containsKey(APP_NAME)) {
            appName = getApplicationName(c);
        } else {
            appName = extras.getString(APP_NAME);
        }
        try {
            Context context = c;
            showNotif(context, issue.getIssueId(), (int) HSFormat.issueTsFormat.parse(issue.getCreatedAt()).getTime(), messCnt, chatLaunchSource, appName);
        } catch (ParseException e) {
            Log.d(TAG, "showNotif ParseException", e);
        }
    }

    public static String getApplicationName(Context context) {
        String appName = null;
        try {
            appName = context.getPackageManager().getApplicationLabel(context.getApplicationInfo()).toString();
        } catch (NullPointerException e) {
            Log.d(TAG, "getApplicationName", e);
        }
        if (appName == null) {
            return context.getResources().getString(string.hs__default_notification_content_title);
        }
        return appName;
    }

    public static void showNotif(Context c, String issueId, int ts, int newMessCnt, String chatLaunchSource, String contentTitle) {
        if (data == null) {
            data = new HSApiData(c);
        }
        data.storage.saveNotification(issueId, ts, newMessCnt, chatLaunchSource, contentTitle);
        if (Issue.getProfileId(issueId).equals(data.getProfileId())) {
            HelpshiftDelegate delegate = Helpshift.getDelegate();
            if (delegate != null) {
                delegate.didReceiveNotification(newMessCnt);
            }
            NotificationManager notificationManager = (NotificationManager) c.getSystemService("notification");
            CharSequence notifText = c.getResources().getQuantityString(plurals.hs__notification_content_title, newMessCnt, new Object[]{Integer.valueOf(newMessCnt)});
            int notificationIcon = Xml.getLogoResourceValue(c);
            Uri soundUri = null;
            try {
                JSONObject config = new HSStorage(c).getAppConfig();
                if (config.has("notificationSound")) {
                    soundUri = Uri.parse("android.resource://" + c.getPackageName() + "/" + config.getInt("notificationSound"));
                }
                if (config.has("notificationIcon")) {
                    notificationIcon = config.getInt("notificationIcon");
                }
            } catch (JSONException e) {
                Log.d(TAG, "getAppConfig", e);
            }
            Intent intent = new Intent(c, HSConversation.class);
            intent.setFlags(268435456);
            intent.putExtra("issueId", issueId);
            intent.putExtra("chatLaunchSource", chatLaunchSource);
            intent.putExtra("isRoot", true);
            PendingIntent contentIntent = PendingIntent.getActivity(c, ts, intent, 0);
            Builder notificationBuilder = new Builder(c);
            notificationBuilder.setSmallIcon(notificationIcon);
            notificationBuilder.setContentTitle(contentTitle);
            notificationBuilder.setContentText(notifText);
            notificationBuilder.setContentIntent(contentIntent);
            notificationBuilder.setAutoCancel(true);
            if (soundUri != null) {
                notificationBuilder.setSound(soundUri);
                if (hasVibratePermission(c)) {
                    notificationBuilder.setDefaults(6);
                } else {
                    notificationBuilder.setDefaults(4);
                }
            } else if (hasVibratePermission(c)) {
                notificationBuilder.setDefaults(-1);
            } else {
                notificationBuilder.setDefaults(5);
            }
            notificationManager.notify(issueId, 1, notificationBuilder.build());
        }
    }

    private static boolean hasVibratePermission(Context context) {
        if (context.getPackageManager().checkPermission("android.permission.VIBRATE", context.getPackageName()) == 0) {
            return true;
        }
        return false;
    }

    public static Handler getNotifHandler(final Context c, final HSPolling notifCountPoller) {
        final HSApiData data = new HSApiData(c);
        return new Handler() {
            public void handleMessage(Message msg) {
                JSONArray issues = msg.obj;
                try {
                    if (notifCountPoller != null) {
                        notifCountPoller.resetInterval();
                    }
                    for (int i = 0; i < issues.length(); i++) {
                        JSONObject issue = issues.getJSONObject(i);
                        String issueId = issue.getString(DBLikedChannelsHelper.KEY_ID);
                        if (!data.storage.getForegroundIssue().equals(issueId)) {
                            Issue storedIssue = IssuesDataSource.getIssue(issueId);
                            JSONArray messages = issue.getJSONArray(Tables.MESSAGES);
                            if (messages.length() == 1) {
                                JSONObject lastMessage = messages.getJSONObject(messages.length() - 1);
                                if (MessagesUtil.notificationsTurnedOff(lastMessage.getString(MessageColumns.ORIGIN), lastMessage.getString(MessageColumns.TYPE))) {
                                    continue;
                                }
                            }
                            int messCnt = storedIssue.getNewMessagesCount();
                            if (messCnt != 0) {
                                try {
                                    HSNotification.showNotif(c, issue.getString(DBLikedChannelsHelper.KEY_ID), (int) HSFormat.issueTsFormat.parse(issue.getString(MPDbAdapter.KEY_CREATED_AT)).getTime(), messCnt, HSConsts.SRC_INAPP, HSNotification.getApplicationName(c));
                                } catch (ParseException e) {
                                    Log.d(HSNotification.TAG, e.toString());
                                }
                            } else {
                                continue;
                            }
                        }
                    }
                } catch (JSONException e2) {
                    Log.d(HSNotification.TAG, e2.getMessage());
                }
            }
        };
    }
}
