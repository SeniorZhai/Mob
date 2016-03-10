package com.mobcrush.mobcrush.broadcast;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.Notification.MediaStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.Mobcrush;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.broadcast.BroadcastHelper.BroadcastStatusCallback;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.datamodel.User;
import com.nostra13.universalimageloader.core.ImageLoader;

@TargetApi(21)
public class BroadcastService extends Service {
    public static final String ACTION_EXPAND_MENU = "action_expand_menu";
    public static final String ACTION_QUIT_SERVICE = "action_quit_service";
    public static final String ACTION_SERVICE_STATUS = "action_service_status";
    public static final String ACTION_START_BROADCAST = "action_start_broadcast";
    public static final String ACTION_STOP_BROADCAST = "action_stop_broadcast";
    public static final String ACTION_VIEW_SETTINGS = "action_view_settings";
    public static final String EXTRA_CODE = "code";
    private static final IntentFilter INTENT_FILTER = new IntentFilter();
    public static final int NOTIFICATION_ID = 1;
    private static final String TAG = BroadcastService.class.getName();
    private static boolean isRunning = false;
    private BroadcastHelper broadcastHelper;
    private BroadcastMenu broadcastMenu;
    private Intent mediaProjectionIntent;
    private BroadcastEventReceiver receiver;
    private int resultCode;
    private int screenDensity;
    private String streamKey;

    public class BroadcastEventReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            Log.d(BroadcastService.TAG, "Received intent: " + intent.getAction());
            if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                if (BroadcastService.this.broadcastMenu != null) {
                    BroadcastService.this.broadcastMenu.showBroadcastEnd();
                }
            } else if (BroadcastService.ACTION_SERVICE_STATUS.equals(intent.getAction()) && intent.hasExtra(BroadcastService.EXTRA_CODE) && intent.getIntExtra(BroadcastService.EXTRA_CODE, 0) == BroadcastService.NOTIFICATION_ID) {
                String errorMessage = "An error response was returned from the broadcast";
                Log.e(BroadcastService.TAG, errorMessage);
                Crashlytics.logException(new Exception(errorMessage));
                Toast.makeText(BroadcastService.this.getApplicationContext(), R.string.broadcast_error, BroadcastService.NOTIFICATION_ID).show();
                BroadcastService.this.broadcastMenu.showBroadcastEnd();
            }
        }
    }

    static {
        INTENT_FILTER.addAction("android.intent.action.SCREEN_OFF");
        INTENT_FILTER.addAction(ACTION_SERVICE_STATUS);
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Nullable
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        Mobcrush.load();
        this.receiver = new BroadcastEventReceiver();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            registerReceiver(this.receiver, INTENT_FILTER);
            Intent dismissDialog = new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            if (ACTION_QUIT_SERVICE.equals(intent.getAction())) {
                if (this.broadcastHelper == null || !this.broadcastHelper.isBroadcasting()) {
                    stopSelf();
                } else {
                    getApplicationContext().sendBroadcast(dismissDialog);
                    Toast.makeText(getApplicationContext(), R.string.broadcasting_warning, NOTIFICATION_ID).show();
                }
            } else if (ACTION_STOP_BROADCAST.equals(intent.getAction())) {
                if (this.broadcastMenu != null) {
                    this.broadcastMenu.showBroadcastEnd();
                }
            } else if (ACTION_EXPAND_MENU.equals(intent.getAction())) {
                if (this.broadcastMenu != null) {
                    getApplicationContext().sendBroadcast(dismissDialog);
                    this.broadcastMenu.expandMenu(true);
                }
            } else if (ACTION_START_BROADCAST.equals(intent.getAction())) {
                if (this.broadcastMenu != null) {
                    getApplicationContext().sendBroadcast(dismissDialog);
                    this.broadcastMenu.showBroadcastStart();
                }
            } else if (ACTION_VIEW_SETTINGS.equals(intent.getAction())) {
                if (this.broadcastMenu != null) {
                    getApplicationContext().sendBroadcast(dismissDialog);
                    this.broadcastMenu.expandMenu(true);
                    this.broadcastMenu.showSettings();
                }
            } else if (!isRunning && intent.hasExtra(Constants.EXTRA_SCREEN_DENSITY) && intent.hasExtra(Constants.EXTRA_STREAM_KEY) && intent.hasExtra(Constants.EXTRA_PROJECTION_INTENT) && intent.hasExtra(Constants.EXTRA_PROJECTION_CODE)) {
                this.screenDensity = intent.getIntExtra(Constants.EXTRA_SCREEN_DENSITY, 0);
                this.streamKey = intent.getStringExtra(Constants.EXTRA_STREAM_KEY);
                this.mediaProjectionIntent = (Intent) intent.getParcelableExtra(Constants.EXTRA_PROJECTION_INTENT);
                this.resultCode = intent.getIntExtra(Constants.EXTRA_PROJECTION_CODE, 0);
                startForegroundService();
            }
        }
        return 2;
    }

    private void startForegroundService() {
        if (!isRunning) {
            isRunning = true;
            this.broadcastHelper = new BroadcastHelper(this, this.screenDensity, this.streamKey, this.mediaProjectionIntent, this.resultCode, new BroadcastStatusCallback() {
                public void onBroadcastStarted() {
                    BroadcastService.this.updateNotification(true);
                }

                public void onBroadcastEnded() {
                    BroadcastService.this.updateNotification(false);
                }
            });
            this.broadcastMenu = new BroadcastMenu(this, this.broadcastHelper);
            updateNotification(false);
            this.broadcastMenu.init();
        }
    }

    private void updateNotification(boolean isBroadcasting) {
        Notification notification = getServiceNotification(isBroadcasting);
        if (isBroadcasting) {
            startForeground(NOTIFICATION_ID, notification);
            return;
        }
        stopForeground(false);
        ((NotificationManager) getSystemService("notification")).notify(NOTIFICATION_ID, notification);
    }

    private Notification getServiceNotification(boolean broadcasting) {
        User user = PreferenceUtility.getUser();
        if (user == null) {
            return null;
        }
        Bitmap userIcon;
        Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.default_profile_pic);
        if (user.profileLogo == null) {
            userIcon = defaultIcon;
        } else {
            userIcon = ImageLoader.getInstance().loadImageSync(user.profileLogo);
            if (userIcon == null) {
                userIcon = defaultIcon;
            }
        }
        Context context = getApplicationContext();
        Builder builder = new Builder(context);
        builder.setStyle(new MediaStyle().setShowActionsInCompactView(new int[]{0, NOTIFICATION_ID, 2}));
        Intent expandIntent = new Intent(context, BroadcastService.class);
        expandIntent.setAction(ACTION_EXPAND_MENU);
        builder.addAction(new Action.Builder(R.drawable.ic_broadcast_notification_logo, null, PendingIntent.getService(context, 0, expandIntent, 0)).build());
        if (broadcasting) {
            builder.setSubText(getString(R.string.broadcasting));
            builder.setUsesChronometer(true);
            Intent stopIntent = new Intent(context, BroadcastService.class);
            stopIntent.setAction(ACTION_STOP_BROADCAST);
            builder.addAction(new Action.Builder(R.drawable.ic_braodcast_notification_stop, null, PendingIntent.getService(context, 0, stopIntent, 0)).build());
        } else {
            builder.setSubText(getString(R.string.ready_to_broadcast));
            builder.setShowWhen(false);
            Intent startIntent = new Intent(context, BroadcastService.class);
            startIntent.setAction(ACTION_START_BROADCAST);
            builder.addAction(new Action.Builder(R.drawable.ic_braodcast_notification_start, null, PendingIntent.getService(context, 0, startIntent, 0)).build());
        }
        Intent settingsIntent = new Intent(context, BroadcastService.class);
        settingsIntent.setAction(ACTION_VIEW_SETTINGS);
        builder.addAction(new Action.Builder(R.drawable.ic_braodcast_notification_settings, null, PendingIntent.getService(context, 0, settingsIntent, 0)).build());
        Intent quitIntent = new Intent(context, BroadcastService.class);
        quitIntent.setAction(ACTION_QUIT_SERVICE);
        return builder.setContentTitle(getString(R.string.broadcast_notification_title)).setContentText(user.username).setLargeIcon(userIcon).setSmallIcon(R.drawable.ic_notification).setDeleteIntent(PendingIntent.getService(context, 0, quitIntent, 0)).build();
    }

    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        unregisterReceiver(this.receiver);
        if (this.broadcastHelper != null) {
            this.broadcastHelper.endBroadcast();
        }
        if (this.broadcastMenu != null) {
            this.broadcastMenu.destroy();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.broadcastMenu == null) {
            return;
        }
        if (newConfig.orientation == 2 || newConfig.orientation == NOTIFICATION_ID) {
            this.broadcastMenu.updateOrientation();
        }
    }
}
