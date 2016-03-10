package com.mobcrush.mobcrush;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v4.util.Pair;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.network.Network;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import java.util.ArrayList;
import java.util.Iterator;

public class GcmIntentService extends IntentService {
    private static ArrayList<Pair<String, String>> SHOWN_NOTIFICATIONS = new ArrayList();
    public static final String TAG = "mobcrush.GcmIntentSrv";
    Builder builder;
    private NotificationManager mNotificationManager;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static void clearShownNotifications() {
        SHOWN_NOTIFICATIONS = new ArrayList();
    }

    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        String messageType = GoogleCloudMessaging.getInstance(this).getMessageType(intent);
        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                Log.d(TAG, "Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED.equals(messageType)) {
                Log.d(TAG, "Deleted messages on server: " + extras.toString());
            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i(TAG, "Received: " + extras.toString());
                sendNotification(extras);
            }
        }
        WakefulBroadcastReceiver.completeWakefulIntent(intent);
    }

    private void sendNotification(Bundle bundle) {
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        if (bundle != null) {
            String id;
            final String message = bundle.getString("alertText");
            String broadcastId = bundle.getString("broadcastId");
            String userId = bundle.getString(ChatMessage.USER_ID);
            if (broadcastId != null) {
                id = broadcastId;
            } else {
                id = userId;
            }
            if (isNotificationAlreadyShown(id, message)) {
                Log.d(TAG, "notification for " + id + " was already shown");
                return;
            }
            SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            if (!pm.getBoolean(getString(R.string.pref_key_notifications), true)) {
                Log.i(TAG, "Push notifications are disabled");
            } else if (broadcastId != null) {
                boolean isNotificationEnabled;
                if (message != null && message.toLowerCase().contains("liked your video")) {
                    isNotificationEnabled = pm.getBoolean(getString(R.string.pref_key_notifications_like), true);
                } else if (message == null || !message.toLowerCase().contains("wants you to check out")) {
                    isNotificationEnabled = pm.getBoolean(getString(R.string.pref_key_notifications_broadcasts), true);
                } else {
                    isNotificationEnabled = pm.getBoolean(getString(R.string.pref_key_notifications_shares), true);
                }
                if (isNotificationEnabled) {
                    Network.getBroadcast(null, broadcastId, new Listener<Broadcast>() {
                        public void onResponse(final Broadcast response) {
                            if (response != null && message != null) {
                                Network.getUserProfile(null, response.user._id, new Listener<User>() {
                                    public void onResponse(User user) {
                                        if (user != null) {
                                            Log.d(GcmIntentService.TAG, "User: " + user.toString());
                                            response.user.profileLogo = user.profileLogo;
                                            response.user.profileLogoSmall = user.profileLogoSmall;
                                            Log.d(GcmIntentService.TAG, "Broadcast: " + response.toString());
                                        }
                                        GcmIntentService.this.showNotification(id, Constants.EXTRA_BROADCAST, response.toString(), message);
                                    }
                                }, new ErrorListener() {
                                    public void onErrorResponse(VolleyError error) {
                                        GcmIntentService.this.showNotification(id, Constants.EXTRA_BROADCAST, response.toString(), message);
                                    }
                                });
                            }
                        }
                    }, null);
                }
            } else if (pm.getBoolean(getString(R.string.pref_key_notifications_follower), true)) {
                Network.getUserProfile(null, userId, new Listener<User>() {
                    public void onResponse(User response) {
                        if (response != null && message != null) {
                            GcmIntentService.this.showNotification(id, Constants.EXTRA_USER, response.toString(), message);
                        }
                    }
                }, null);
            } else {
                Log.i(TAG, "Push notifications for new followers are disabled");
            }
        }
    }

    private void showNotification(final String id, String type, String extra, String message) {
        int i;
        Log.d(TAG, "showNotification: " + extra);
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(type, extra);
        intent.setFlags(603979776);
        PendingIntent contentIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String ringtone = pm.getString(getString(R.string.pref_key_notifications_ringtone), null);
        Builder builder = new Builder(this);
        if (VERSION.SDK_INT >= 21) {
            i = R.drawable.ic_notification;
        } else {
            i = R.drawable.ic_launcher;
        }
        final Builder builder2 = builder.setSmallIcon(i).setColor(getResources().getColor(R.color.dark)).setContentTitle(getString(R.string.app_name)).setAutoCancel(true).setWhen(System.currentTimeMillis()).setStyle(new BigTextStyle().bigText(message)).setContentText(message);
        builder2.setContentIntent(contentIntent);
        if (TextUtils.isEmpty(ringtone)) {
            Log.i(TAG, "ringtone is empty");
        } else {
            try {
                builder2.setSound(Uri.parse(ringtone));
                Log.i(TAG, "ringtone:" + ringtone);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
        if (pm.getBoolean(getString(R.string.pref_key_notifications_vibrate), false)) {
            builder2.setVibrate(new long[]{150, 300});
        }
        String imageUrl = null;
        try {
            User user;
            if (Constants.EXTRA_BROADCAST.equals(type)) {
                user = ((Broadcast) new Gson().fromJson(extra, Broadcast.class)).user;
            } else {
                user = (User) new Gson().fromJson(extra, User.class);
            }
            if (user != null) {
                imageUrl = user.profileLogoSmall != null ? user.profileLogoSmall : user.profileLogo;
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            Crashlytics.logException(e2);
        }
        final String str = message;
        ImageLoader.getInstance().loadImage(imageUrl, new ImageLoadingListener() {
            public void onLoadingStarted(String imageUri, View view) {
            }

            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                GcmIntentService.this.showNotification(id, str, builder2, null);
            }

            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                GcmIntentService.this.showNotification(id, str, builder2, loadedImage);
            }

            public void onLoadingCancelled(String imageUri, View view) {
                GcmIntentService.this.showNotification(id, str, builder2, null);
            }
        });
    }

    private void showNotification(String id, String message, Builder builder, Bitmap bitmap) {
        try {
            boolean consolidate = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.pref_key_group_notifications), false);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile_pic);
            }
            builder.setLargeIcon(bitmap);
            if (SHOWN_NOTIFICATIONS.size() > 0 && consolidate) {
                builder.setContentText(message + " and " + SHOWN_NOTIFICATIONS.size() + " other event happened");
                this.mNotificationManager.cancelAll();
            }
            Notification notification = builder.build();
            if (notification.contentView == null || notification.bigContentView == null) {
                String error = "notification.contentView (" + notification.contentView + ") or notification.bigContentView (" + notification.bigContentView + ") is empty!";
                Log.e(TAG, error);
                Crashlytics.logException(new Exception(error));
                return;
            }
            this.mNotificationManager.notify(TAG, (int) System.currentTimeMillis(), notification);
            SHOWN_NOTIFICATIONS.add(new Pair(id, message));
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private boolean isNotificationAlreadyShown(String id, String message) {
        if (SHOWN_NOTIFICATIONS == null || SHOWN_NOTIFICATIONS.size() == 0) {
            return false;
        }
        Iterator i$ = SHOWN_NOTIFICATIONS.iterator();
        while (i$.hasNext()) {
            Pair<String, String> p = (Pair) i$.next();
            if (p.first != null && ((String) p.first).equals(id) && p.second != null && ((String) p.second).equals(message)) {
                return true;
            }
        }
        return false;
    }
}
