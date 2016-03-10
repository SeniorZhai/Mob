package com.mixpanel.android.mpmetrics;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.os.Build.VERSION;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import com.google.android.exoplayer.util.MpegAudioHeader;
import com.mixpanel.android.surveys.SurveyActivity;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ConfigurationChecker {
    public static String LOGTAG = "MixpanelAPI.ConfigurationChecker";

    ConfigurationChecker() {
    }

    public static boolean checkBasicConfiguration(Context context) {
        if (context.getPackageManager().checkPermission("android.permission.INTERNET", context.getPackageName()) == 0) {
            return true;
        }
        Log.w(LOGTAG, "Package does not have permission android.permission.INTERNET - Mixpanel will not work at all!");
        Log.i(LOGTAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n<uses-permission android:name=\"android.permission.INTERNET\" />");
        return false;
    }

    public static boolean checkPushConfiguration(Context context) {
        if (VERSION.SDK_INT < 8) {
            Log.i(LOGTAG, "Mixpanel push notifications not supported in SDK " + VERSION.SDK_INT);
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        String permissionName = packageName + ".permission.C2D_MESSAGE";
        try {
            packageManager.getPermissionInfo(permissionName, MpegAudioHeader.MAX_FRAME_SIZE_BYTES);
            if (packageManager.checkPermission("com.google.android.c2dm.permission.RECEIVE", packageName) != 0) {
                Log.w(LOGTAG, "Package does not have permission com.google.android.c2dm.permission.RECEIVE");
                Log.i(LOGTAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n<uses-permission android:name=\"com.google.android.c2dm.permission.RECEIVE\" />");
                return false;
            } else if (packageManager.checkPermission("android.permission.INTERNET", packageName) != 0) {
                Log.w(LOGTAG, "Package does not have permission android.permission.INTERNET");
                Log.i(LOGTAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n<uses-permission android:name=\"android.permission.INTERNET\" />");
                return false;
            } else if (packageManager.checkPermission("android.permission.WAKE_LOCK", packageName) != 0) {
                Log.w(LOGTAG, "Package does not have permission android.permission.WAKE_LOCK");
                Log.i(LOGTAG, "You can fix this by adding the following to your AndroidManifest.xml file:\n<uses-permission android:name=\"android.permission.WAKE_LOCK\" />");
                return false;
            } else {
                if (packageManager.checkPermission("android.permission.GET_ACCOUNTS", packageName) != 0) {
                    Log.i(LOGTAG, "Package does not have permission android.permission.GET_ACCOUNTS");
                    Log.i(LOGTAG, "Android versions below 4.1 require GET_ACCOUNTS to receive Mixpanel push notifications.\nDevices with later OS versions will still be able to receive messages, but if you'd like to support older devices, you'll need to add the following to your AndroidManifest.xml file:\n<uses-permission android:name=\"android.permission.GET_ACCOUNTS\" />");
                    if (VERSION.SDK_INT < 16) {
                        return false;
                    }
                }
                try {
                    ActivityInfo[] receivers = packageManager.getPackageInfo(packageName, 2).receivers;
                    if (receivers == null || receivers.length == 0) {
                        Log.w(LOGTAG, "No receiver for package " + packageName);
                        Log.i(LOGTAG, "You can fix this with the following into your <application> tag:\n" + samplePushConfigurationMessage(packageName));
                        return false;
                    }
                    Set<String> allowedReceivers = new HashSet();
                    for (ActivityInfo receiver : receivers) {
                        if ("com.google.android.c2dm.permission.SEND".equals(receiver.permission)) {
                            allowedReceivers.add(receiver.name);
                        }
                    }
                    if (allowedReceivers.isEmpty()) {
                        Log.w(LOGTAG, "No receiver allowed to receive com.google.android.c2dm.permission.SEND");
                        Log.i(LOGTAG, "You can fix by pasting the following into the <application> tag in your AndroidManifest.xml:\n" + samplePushConfigurationMessage(packageName));
                        return false;
                    } else if (!checkReceiver(context, allowedReceivers, "com.google.android.c2dm.intent.RECEIVE")) {
                        return false;
                    } else {
                        boolean canRegisterWithPlayServices = false;
                        try {
                            Class.forName("com.google.android.gms.common.GooglePlayServicesUtil");
                            canRegisterWithPlayServices = true;
                        } catch (ClassNotFoundException e) {
                            Log.w(LOGTAG, "Google Play Services aren't included in your build- push notifications won't work on Lollipop/API 21 or greater");
                            Log.i(LOGTAG, "You can fix this by adding com.google.android.gms:play-services as a dependency of your gradle or maven project");
                        }
                        boolean canRegisterWithRegistrationIntent = true;
                        if (!checkReceiver(context, allowedReceivers, "com.google.android.c2dm.intent.REGISTRATION")) {
                            Log.i(LOGTAG, "(You can still receive push notifications on Lollipop/API 21 or greater with this configuration)");
                            canRegisterWithRegistrationIntent = false;
                        }
                        if (canRegisterWithPlayServices || canRegisterWithRegistrationIntent) {
                            return true;
                        }
                        return false;
                    }
                } catch (NameNotFoundException e2) {
                    Log.w(LOGTAG, "Could not get receivers for package " + packageName);
                    return false;
                }
            }
        } catch (NameNotFoundException e3) {
            Log.w(LOGTAG, "Application does not define permission " + permissionName);
            Log.i(LOGTAG, "You will need to add the following lines to your application manifest:\n<permission android:name=\"" + packageName + ".permission.C2D_MESSAGE\" android:protectionLevel=\"signature\" />\n" + "<uses-permission android:name=\"" + packageName + ".permission.C2D_MESSAGE\" />");
            return false;
        }
    }

    public static boolean checkSurveyActivityAvailable(Context context) {
        if (VERSION.SDK_INT < 16) {
            return false;
        }
        Intent surveyIntent = new Intent(context, SurveyActivity.class);
        surveyIntent.addFlags(268435456);
        surveyIntent.addFlags(AccessibilityNodeInfoCompat.ACTION_SET_SELECTION);
        if (context.getPackageManager().queryIntentActivities(surveyIntent, 0).size() != 0) {
            return true;
        }
        Log.w(LOGTAG, SurveyActivity.class.getName() + " is not registered as an activity in your application, so surveys can't be shown.");
        Log.i(LOGTAG, "Please add the child tag <activity android:name=\"com.mixpanel.android.surveys.SurveyActivity\" /> to your <application> tag.");
        return false;
    }

    private static String samplePushConfigurationMessage(String packageName) {
        return "<receiver android:name=\"com.mixpanel.android.mpmetrics.GCMReceiver\"\n          android:permission=\"com.google.android.c2dm.permission.SEND\" >\n    <intent-filter>\n       <action android:name=\"com.google.android.c2dm.intent.RECEIVE\" />\n       <action android:name=\"com.google.android.c2dm.intent.REGISTRATION\" />\n       <category android:name=\"" + packageName + "\" />\n" + "    </intent-filter>\n" + "</receiver>";
    }

    private static boolean checkReceiver(Context context, Set<String> allowedReceivers, String action) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();
        Intent intent = new Intent(action);
        intent.setPackage(packageName);
        List<ResolveInfo> receivers = pm.queryBroadcastReceivers(intent, 32);
        if (receivers.isEmpty()) {
            Log.w(LOGTAG, "No receivers for action " + action);
            Log.i(LOGTAG, "You can fix by pasting the following into the <application> tag in your AndroidManifest.xml:\n" + samplePushConfigurationMessage(packageName));
            return false;
        }
        for (ResolveInfo receiver : receivers) {
            String name = receiver.activityInfo.name;
            if (!allowedReceivers.contains(name)) {
                Log.w(LOGTAG, "Receiver " + name + " is not set with permission com.google.android.c2dm.permission.SEND");
                Log.i(LOGTAG, "Please add the attribute 'android:permission=\"com.google.android.c2dm.permission.SEND\"' to your <receiver> tag");
                return false;
            }
        }
        return true;
    }
}
