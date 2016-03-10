package com.facebook.messenger;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import bolts.AppLinks;
import com.facebook.FacebookSdk;
import com.facebook.internal.ServerProtocol;
import com.facebook.messenger.MessengerThreadParams.Origin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MessengerUtils {
    public static final String EXTRA_APP_ID = "com.facebook.orca.extra.APPLICATION_ID";
    public static final String EXTRA_EXTERNAL_URI = "com.facebook.orca.extra.EXTERNAL_URI";
    public static final String EXTRA_IS_COMPOSE = "com.facebook.orca.extra.IS_COMPOSE";
    public static final String EXTRA_IS_REPLY = "com.facebook.orca.extra.IS_REPLY";
    public static final String EXTRA_METADATA = "com.facebook.orca.extra.METADATA";
    public static final String EXTRA_PARTICIPANTS = "com.facebook.orca.extra.PARTICIPANTS";
    public static final String EXTRA_PROTOCOL_VERSION = "com.facebook.orca.extra.PROTOCOL_VERSION";
    public static final String EXTRA_REPLY_TOKEN_KEY = "com.facebook.orca.extra.REPLY_TOKEN";
    public static final String EXTRA_THREAD_TOKEN_KEY = "com.facebook.orca.extra.THREAD_TOKEN";
    public static final String ORCA_THREAD_CATEGORY_20150314 = "com.facebook.orca.category.PLATFORM_THREAD_20150314";
    public static final String PACKAGE_NAME = "com.facebook.orca";
    public static final int PROTOCOL_VERSION_20150314 = 20150314;
    private static final String TAG = "MessengerUtils";

    public static void shareToMessenger(Activity activity, int requestCode, ShareToMessengerParams shareToMessengerParams) {
        if (!hasMessengerInstalled(activity)) {
            openMessengerInPlayStore(activity);
        } else if (getAllAvailableProtocolVersions(activity).contains(Integer.valueOf(PROTOCOL_VERSION_20150314))) {
            shareToMessenger20150314(activity, requestCode, shareToMessengerParams);
        } else {
            openMessengerInPlayStore(activity);
        }
    }

    private static void shareToMessenger20150314(Activity activity, int requestCode, ShareToMessengerParams shareToMessengerParams) {
        try {
            Intent shareIntent = new Intent("android.intent.action.SEND");
            shareIntent.setFlags(1);
            shareIntent.setPackage(PACKAGE_NAME);
            shareIntent.putExtra("android.intent.extra.STREAM", shareToMessengerParams.uri);
            shareIntent.setType(shareToMessengerParams.mimeType);
            String appId = FacebookSdk.getApplicationId();
            if (appId != null) {
                shareIntent.putExtra(EXTRA_PROTOCOL_VERSION, PROTOCOL_VERSION_20150314);
                shareIntent.putExtra(EXTRA_APP_ID, appId);
                shareIntent.putExtra(EXTRA_METADATA, shareToMessengerParams.metaData);
                shareIntent.putExtra(EXTRA_EXTERNAL_URI, shareToMessengerParams.externalUri);
            }
            activity.startActivityForResult(shareIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            activity.startActivity(activity.getPackageManager().getLaunchIntentForPackage(PACKAGE_NAME));
        }
    }

    public static MessengerThreadParams getMessengerThreadParamsForIntent(Intent intent) {
        Set<String> categories = intent.getCategories();
        if (categories == null || !categories.contains(ORCA_THREAD_CATEGORY_20150314)) {
            return null;
        }
        Bundle appLinkExtras = AppLinks.getAppLinkExtras(intent);
        String threadToken = appLinkExtras.getString(EXTRA_THREAD_TOKEN_KEY);
        String metadata = appLinkExtras.getString(EXTRA_METADATA);
        String participants = appLinkExtras.getString(EXTRA_PARTICIPANTS);
        boolean isReply = appLinkExtras.getBoolean(EXTRA_IS_REPLY);
        boolean isCompose = appLinkExtras.getBoolean(EXTRA_IS_COMPOSE);
        Origin origin = Origin.UNKNOWN;
        if (isReply) {
            origin = Origin.REPLY_FLOW;
        } else if (isCompose) {
            origin = Origin.COMPOSE_FLOW;
        }
        return new MessengerThreadParams(origin, threadToken, metadata, parseParticipants(participants));
    }

    public static void finishShareToMessenger(Activity activity, ShareToMessengerParams shareToMessengerParams) {
        Intent originalIntent = activity.getIntent();
        Set<String> categories = originalIntent.getCategories();
        if (categories == null) {
            activity.setResult(0, null);
            activity.finish();
        } else if (categories.contains(ORCA_THREAD_CATEGORY_20150314)) {
            Bundle appLinkExtras = AppLinks.getAppLinkExtras(originalIntent);
            Intent resultIntent = new Intent();
            if (categories.contains(ORCA_THREAD_CATEGORY_20150314)) {
                resultIntent.putExtra(EXTRA_PROTOCOL_VERSION, PROTOCOL_VERSION_20150314);
                resultIntent.putExtra(EXTRA_THREAD_TOKEN_KEY, appLinkExtras.getString(EXTRA_THREAD_TOKEN_KEY));
                resultIntent.setDataAndType(shareToMessengerParams.uri, shareToMessengerParams.mimeType);
                resultIntent.setFlags(1);
                resultIntent.putExtra(EXTRA_APP_ID, FacebookSdk.getApplicationId());
                resultIntent.putExtra(EXTRA_METADATA, shareToMessengerParams.metaData);
                resultIntent.putExtra(EXTRA_EXTERNAL_URI, shareToMessengerParams.externalUri);
                activity.setResult(-1, resultIntent);
                activity.finish();
                return;
            }
            throw new RuntimeException();
        } else {
            activity.setResult(0, null);
            activity.finish();
        }
    }

    public static boolean hasMessengerInstalled(Context context) {
        try {
            context.getPackageManager().getPackageInfo(PACKAGE_NAME, 0);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static void openMessengerInPlayStore(Context context) {
        try {
            startViewUri(context, "market://details?id=com.facebook.orca");
        } catch (ActivityNotFoundException e) {
            startViewUri(context, "http://play.google.com/store/apps/details?id=com.facebook.orca");
        }
    }

    private static Set<Integer> getAllAvailableProtocolVersions(Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        Set<Integer> allAvailableVersions = new HashSet();
        Cursor c = contentResolver.query(Uri.parse("content://com.facebook.orca.provider.MessengerPlatformProvider/versions"), new String[]{ServerProtocol.FALLBACK_DIALOG_PARAM_VERSION}, null, null, null);
        if (c != null) {
            try {
                int versionColumnIndex = c.getColumnIndex(ServerProtocol.FALLBACK_DIALOG_PARAM_VERSION);
                while (c.moveToNext()) {
                    allAvailableVersions.add(Integer.valueOf(c.getInt(versionColumnIndex)));
                }
            } finally {
                c.close();
            }
        }
        return allAvailableVersions;
    }

    private static void startViewUri(Context context, String uri) {
        context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(uri)));
    }

    private static List<String> parseParticipants(String s) {
        if (s == null || s.length() == 0) {
            return Collections.emptyList();
        }
        String[] parts = s.split(",");
        List<String> ret = new ArrayList();
        for (String part : parts) {
            ret.add(part.trim());
        }
        return ret;
    }
}
