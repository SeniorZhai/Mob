package com.google.android.gms.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.Notification;
import android.app.Notification.BigTextStyle;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller.SessionInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri.Builder;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompatExtras;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import com.facebook.internal.Utility;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.android.gms.R;
import com.google.android.gms.common.internal.zzd;
import com.google.android.gms.common.internal.zzf;
import com.google.android.gms.common.internal.zzg;
import com.google.android.gms.common.internal.zzm;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.internal.zzkz;
import com.google.android.gms.internal.zzlk;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.http.protocol.HTTP;

public final class GooglePlayServicesUtil {
    public static final String GMS_ERROR_DIALOG = "GooglePlayServicesErrorDialog";
    @Deprecated
    public static final String GOOGLE_PLAY_SERVICES_PACKAGE = "com.google.android.gms";
    @Deprecated
    public static final int GOOGLE_PLAY_SERVICES_VERSION_CODE = zzml();
    public static final String GOOGLE_PLAY_STORE_PACKAGE = "com.android.vending";
    public static boolean zzVS = false;
    public static boolean zzVT = false;
    private static int zzVU = -1;
    private static String zzVV = null;
    private static Integer zzVW = null;
    static final AtomicBoolean zzVX = new AtomicBoolean();
    private static final Object zzoW = new Object();

    private static class zza extends Handler {
        private final Context zzqw;

        zza(Context context) {
            super(Looper.myLooper() == null ? Looper.getMainLooper() : Looper.myLooper());
            this.zzqw = context.getApplicationContext();
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    int isGooglePlayServicesAvailable = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.zzqw);
                    if (GooglePlayServicesUtil.isUserRecoverableError(isGooglePlayServicesAvailable)) {
                        GooglePlayServicesUtil.zza(isGooglePlayServicesAvailable, this.zzqw);
                        return;
                    }
                    return;
                default:
                    Log.w("GooglePlayServicesUtil", "Don't know how to handle this message: " + msg.what);
                    return;
            }
        }
    }

    private GooglePlayServicesUtil() {
    }

    @Deprecated
    public static Dialog getErrorDialog(int errorCode, Activity activity, int requestCode) {
        return getErrorDialog(errorCode, activity, requestCode, null);
    }

    @Deprecated
    public static Dialog getErrorDialog(int errorCode, Activity activity, int requestCode, OnCancelListener cancelListener) {
        return zza(errorCode, activity, null, requestCode, cancelListener);
    }

    @Deprecated
    public static PendingIntent getErrorPendingIntent(int errorCode, Context context, int requestCode) {
        Intent zzaT = zzaT(errorCode);
        return zzaT == null ? null : PendingIntent.getActivity(context, requestCode, zzaT, 268435456);
    }

    @Deprecated
    public static String getErrorString(int errorCode) {
        return ConnectionResult.getStatusString(errorCode);
    }

    @Deprecated
    public static String getOpenSourceSoftwareLicenseInfo(Context context) {
        InputStream openInputStream;
        try {
            openInputStream = context.getContentResolver().openInputStream(new Builder().scheme("android.resource").authority(GOOGLE_PLAY_SERVICES_PACKAGE).appendPath("raw").appendPath("oss_notice").build());
            String next = new Scanner(openInputStream).useDelimiter("\\A").next();
            if (openInputStream == null) {
                return next;
            }
            openInputStream.close();
            return next;
        } catch (NoSuchElementException e) {
            if (openInputStream != null) {
                openInputStream.close();
            }
            return null;
        } catch (Exception e2) {
            return null;
        } catch (Throwable th) {
            if (openInputStream != null) {
                openInputStream.close();
            }
        }
    }

    public static Context getRemoteContext(Context context) {
        try {
            return context.createPackageContext(GOOGLE_PLAY_SERVICES_PACKAGE, 3);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static Resources getRemoteResource(Context context) {
        try {
            return context.getPackageManager().getResourcesForApplication(GOOGLE_PLAY_SERVICES_PACKAGE);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    @Deprecated
    public static int isGooglePlayServicesAvailable(Context context) {
        if (zzd.zzZR) {
            return GOOGLE_PLAY_SERVICES_VERSION_CODE;
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            context.getResources().getString(R.string.common_google_play_services_unknown_issue);
        } catch (Throwable th) {
            Log.e("GooglePlayServicesUtil", "The Google Play services resources were not found. Check your project configuration to ensure that the resources are included.");
        }
        if (!GOOGLE_PLAY_SERVICES_PACKAGE.equals(context.getPackageName())) {
            zzaa(context);
        }
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(GOOGLE_PLAY_SERVICES_PACKAGE, 64);
            zzd zzmn = zzd.zzmn();
            if (!zzkz.zzbP(packageInfo.versionCode) && !zzkz.zzai(context)) {
                try {
                    if (zzmn.zza(packageManager.getPackageInfo(GOOGLE_PLAY_STORE_PACKAGE, 64), zzbk.zzVR) == null) {
                        Log.w("GooglePlayServicesUtil", "Google Play Store signature invalid.");
                        return 9;
                    }
                    if (zzmn.zza(packageInfo, zzmn.zza(packageManager.getPackageInfo(GOOGLE_PLAY_STORE_PACKAGE, 64), zzbk.zzVR)) == null) {
                        Log.w("GooglePlayServicesUtil", "Google Play services signature invalid.");
                        return 9;
                    }
                } catch (NameNotFoundException e) {
                    if (zzh(context, GOOGLE_PLAY_STORE_PACKAGE)) {
                        Log.w("GooglePlayServicesUtil", "Google Play Store is updating.");
                        if (zzmn.zza(packageInfo, zzbk.zzVR) == null) {
                            Log.w("GooglePlayServicesUtil", "Google Play services signature invalid.");
                            return 9;
                        }
                    }
                    Log.w("GooglePlayServicesUtil", "Google Play Store is neither installed nor updating.");
                    return 9;
                }
            } else if (zzmn.zza(packageInfo, zzbk.zzVR) == null) {
                Log.w("GooglePlayServicesUtil", "Google Play services signature invalid.");
                return 9;
            }
            if (zzkz.zzbN(packageInfo.versionCode) < zzkz.zzbN(GOOGLE_PLAY_SERVICES_VERSION_CODE)) {
                Log.w("GooglePlayServicesUtil", "Google Play services out of date.  Requires " + GOOGLE_PLAY_SERVICES_VERSION_CODE + " but found " + packageInfo.versionCode);
                return 2;
            }
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if (applicationInfo == null) {
                try {
                    applicationInfo = packageManager.getApplicationInfo(GOOGLE_PLAY_SERVICES_PACKAGE, GOOGLE_PLAY_SERVICES_VERSION_CODE);
                } catch (NameNotFoundException e2) {
                    Log.wtf("GooglePlayServicesUtil", "Google Play services missing when getting application info.");
                    e2.printStackTrace();
                    return 1;
                }
            }
            return !applicationInfo.enabled ? 3 : GOOGLE_PLAY_SERVICES_VERSION_CODE;
        } catch (NameNotFoundException e3) {
            Log.w("GooglePlayServicesUtil", "Google Play services is missing.");
            return 1;
        }
    }

    @Deprecated
    public static boolean isUserRecoverableError(int errorCode) {
        switch (errorCode) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
            case HTTP.HT /*9*/:
                return true;
            default:
                return false;
        }
    }

    @Deprecated
    public static boolean showErrorDialogFragment(int errorCode, Activity activity, int requestCode) {
        return showErrorDialogFragment(errorCode, activity, requestCode, null);
    }

    @Deprecated
    public static boolean showErrorDialogFragment(int errorCode, Activity activity, int requestCode, OnCancelListener cancelListener) {
        return showErrorDialogFragment(errorCode, activity, null, requestCode, cancelListener);
    }

    public static boolean showErrorDialogFragment(int errorCode, Activity activity, Fragment fragment, int requestCode, OnCancelListener cancelListener) {
        boolean z = false;
        Dialog zza = zza(errorCode, activity, fragment, requestCode, cancelListener);
        if (zza == null) {
            return z;
        }
        try {
            z = activity instanceof FragmentActivity;
        } catch (NoClassDefFoundError e) {
        }
        if (z) {
            SupportErrorDialogFragment.newInstance(zza, cancelListener).show(((FragmentActivity) activity).getSupportFragmentManager(), GMS_ERROR_DIALOG);
        } else if (zzlk.zzoR()) {
            ErrorDialogFragment.newInstance(zza, cancelListener).show(activity.getFragmentManager(), GMS_ERROR_DIALOG);
        } else {
            throw new RuntimeException("This Activity does not support Fragments.");
        }
        return true;
    }

    @Deprecated
    public static void showErrorNotification(int errorCode, Context context) {
        if (zzkz.zzai(context) && errorCode == 2) {
            errorCode = 42;
        }
        if (zze(context, errorCode) || zzf(context, errorCode)) {
            zzab(context);
        } else {
            zza(errorCode, context);
        }
    }

    @Deprecated
    public static void zzY(Context context) throws GooglePlayServicesRepairableException, GooglePlayServicesNotAvailableException {
        int isGooglePlayServicesAvailable = isGooglePlayServicesAvailable(context);
        if (isGooglePlayServicesAvailable != 0) {
            Intent zzaT = zzaT(isGooglePlayServicesAvailable);
            Log.e("GooglePlayServicesUtil", "GooglePlayServices not available due to error " + isGooglePlayServicesAvailable);
            if (zzaT == null) {
                throw new GooglePlayServicesNotAvailableException(isGooglePlayServicesAvailable);
            }
            throw new GooglePlayServicesRepairableException(isGooglePlayServicesAvailable, "Google Play Services not available", zzaT);
        }
    }

    private static Dialog zza(int i, Activity activity, Fragment fragment, int i2, OnCancelListener onCancelListener) {
        AlertDialog.Builder builder = null;
        if (i == 0) {
            return null;
        }
        if (zzkz.zzai(activity) && i == 2) {
            i = 42;
        }
        if (zzlk.zzoU()) {
            TypedValue typedValue = new TypedValue();
            activity.getTheme().resolveAttribute(16843529, typedValue, true);
            if ("Theme.Dialog.Alert".equals(activity.getResources().getResourceEntryName(typedValue.resourceId))) {
                builder = new AlertDialog.Builder(activity, 5);
            }
        }
        if (builder == null) {
            builder = new AlertDialog.Builder(activity);
        }
        builder.setMessage(zzf.zzb(activity, i, zzad(activity)));
        if (onCancelListener != null) {
            builder.setOnCancelListener(onCancelListener);
        }
        Intent zzaT = zzaT(i);
        OnClickListener com_google_android_gms_common_internal_zzg = fragment == null ? new zzg(activity, zzaT, i2) : new zzg(fragment, zzaT, i2);
        CharSequence zzh = zzf.zzh(activity, i);
        if (zzh != null) {
            builder.setPositiveButton(zzh, com_google_android_gms_common_internal_zzg);
        }
        CharSequence zzg = zzf.zzg(activity, i);
        if (zzg != null) {
            builder.setTitle(zzg);
        }
        return builder.create();
    }

    private static void zza(int i, Context context) {
        zza(i, context, null);
    }

    private static void zza(int i, Context context, String str) {
        Notification build;
        int i2;
        Resources resources = context.getResources();
        String zzad = zzad(context);
        CharSequence zzi = zzf.zzi(context, i);
        if (zzi == null) {
            zzi = resources.getString(R.string.common_google_play_services_notification_ticker);
        }
        CharSequence zzc = zzf.zzc(context, i, zzad);
        PendingIntent errorPendingIntent = getErrorPendingIntent(i, context, GOOGLE_PLAY_SERVICES_VERSION_CODE);
        if (zzkz.zzai(context)) {
            zzu.zzU(zzlk.zzoV());
            build = new Notification.Builder(context).setSmallIcon(R.drawable.common_ic_googleplayservices).setPriority(2).setAutoCancel(true).setStyle(new BigTextStyle().bigText(zzi + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + zzc)).addAction(R.drawable.common_full_open_on_phone, resources.getString(R.string.common_open_on_phone), errorPendingIntent).build();
        } else {
            CharSequence string = resources.getString(R.string.common_google_play_services_notification_ticker);
            if (zzlk.zzoR()) {
                Notification build2;
                Notification.Builder autoCancel = new Notification.Builder(context).setSmallIcon(17301642).setContentTitle(zzi).setContentText(zzc).setContentIntent(errorPendingIntent).setTicker(string).setAutoCancel(true);
                if (zzlk.zzoY()) {
                    autoCancel.setLocalOnly(true);
                }
                if (zzlk.zzoV()) {
                    autoCancel.setStyle(new BigTextStyle().bigText(zzc));
                    build2 = autoCancel.build();
                } else {
                    build2 = autoCancel.getNotification();
                }
                if (VERSION.SDK_INT == 19) {
                    build2.extras.putBoolean(NotificationCompatExtras.EXTRA_LOCAL_ONLY, true);
                }
                build = build2;
            } else {
                build = new Notification(17301642, string, System.currentTimeMillis());
                build.flags |= 16;
                build.setLatestEventInfo(context, zzi, zzc, errorPendingIntent);
            }
        }
        if (zzaU(i)) {
            zzVX.set(false);
            i2 = 10436;
        } else {
            i2 = 39789;
        }
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (str != null) {
            notificationManager.notify(str, i2, build);
        } else {
            notificationManager.notify(i2, build);
        }
    }

    public static boolean zza(Context context, int i, String str) {
        if (zzlk.zzoX()) {
            try {
                ((AppOpsManager) context.getSystemService("appops")).checkPackage(i, str);
                return true;
            } catch (SecurityException e) {
                return false;
            }
        }
        String[] packagesForUid = context.getPackageManager().getPackagesForUid(i);
        if (str == null || packagesForUid == null) {
            return false;
        }
        for (int i2 = GOOGLE_PLAY_SERVICES_VERSION_CODE; i2 < packagesForUid.length; i2++) {
            if (str.equals(packagesForUid[i2])) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    public static Intent zzaT(int i) {
        switch (i) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return zzm.zzcg(GOOGLE_PLAY_SERVICES_PACKAGE);
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return zzm.zzce(GOOGLE_PLAY_SERVICES_PACKAGE);
            case com.mobcrush.mobcrush.R.styleable.Theme_dialogTheme /*42*/:
                return zzm.zznX();
            default:
                return null;
        }
    }

    private static boolean zzaU(int i) {
        switch (i) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
            case com.mobcrush.mobcrush.R.styleable.Toolbar_collapseIcon /*18*/:
            case com.mobcrush.mobcrush.R.styleable.Theme_dialogTheme /*42*/:
                return true;
            default:
                return false;
        }
    }

    private static void zzaa(Context context) {
        synchronized (zzoW) {
            if (zzVV == null) {
                zzVV = context.getPackageName();
                try {
                    Bundle bundle = context.getPackageManager().getApplicationInfo(context.getPackageName(), AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS).metaData;
                    if (bundle != null) {
                        zzVW = Integer.valueOf(bundle.getInt("com.google.android.gms.version"));
                    } else {
                        zzVW = null;
                    }
                } catch (Throwable e) {
                    Log.wtf("GooglePlayServicesUtil", "This should never happen.", e);
                }
            } else if (!zzVV.equals(context.getPackageName())) {
                throw new IllegalArgumentException("isGooglePlayServicesAvailable should only be called with Context from your application's package. A previous call used package '" + zzVV + "' and this call used package '" + context.getPackageName() + "'.");
            }
            Integer num = zzVW;
        }
        if (num == null) {
            throw new IllegalStateException("A required meta-data tag in your app's AndroidManifest.xml does not exist.  You must have the following declaration within the <application> element:     <meta-data android:name=\"com.google.android.gms.version\" android:value=\"@integer/google_play_services_version\" />");
        } else if (num.intValue() != GOOGLE_PLAY_SERVICES_VERSION_CODE) {
            throw new IllegalStateException("The meta-data tag in your app's AndroidManifest.xml does not have the right value.  Expected " + GOOGLE_PLAY_SERVICES_VERSION_CODE + " but" + " found " + num + ".  You must have the" + " following declaration within the <application> element: " + "    <meta-data android:name=\"" + "com.google.android.gms.version" + "\" android:value=\"@integer/google_play_services_version\" />");
        }
    }

    private static void zzab(Context context) {
        Handler com_google_android_gms_common_GooglePlayServicesUtil_zza = new zza(context);
        com_google_android_gms_common_GooglePlayServicesUtil_zza.sendMessageDelayed(com_google_android_gms_common_GooglePlayServicesUtil_zza.obtainMessage(1), 120000);
    }

    @Deprecated
    public static void zzac(Context context) {
        if (!zzVX.getAndSet(true)) {
            try {
                ((NotificationManager) context.getSystemService("notification")).cancel(10436);
            } catch (SecurityException e) {
            }
        }
    }

    public static String zzad(Context context) {
        Object obj = context.getApplicationInfo().name;
        if (!TextUtils.isEmpty(obj)) {
            return obj;
        }
        ApplicationInfo applicationInfo;
        String packageName = context.getPackageName();
        PackageManager packageManager = context.getApplicationContext().getPackageManager();
        try {
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), GOOGLE_PLAY_SERVICES_VERSION_CODE);
        } catch (NameNotFoundException e) {
            applicationInfo = null;
        }
        return applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo).toString() : packageName;
    }

    public static boolean zzae(Context context) {
        return zzlk.zzoZ() && context.getPackageManager().hasSystemFeature("com.google.sidewinder");
    }

    public static boolean zzb(PackageManager packageManager) {
        synchronized (zzoW) {
            if (zzVU == -1) {
                try {
                    if (zzd.zzmn().zza(packageManager.getPackageInfo(GOOGLE_PLAY_SERVICES_PACKAGE, 64), zzc.zzVK[1]) != null) {
                        zzVU = 1;
                    } else {
                        zzVU = GOOGLE_PLAY_SERVICES_VERSION_CODE;
                    }
                } catch (NameNotFoundException e) {
                    zzVU = GOOGLE_PLAY_SERVICES_VERSION_CODE;
                }
            }
        }
        return zzVU != 0;
    }

    @Deprecated
    public static boolean zzb(PackageManager packageManager, String str) {
        return zzd.zzmn().zzb(packageManager, str);
    }

    public static boolean zzc(PackageManager packageManager) {
        return zzb(packageManager) || !zzmm();
    }

    public static boolean zzd(Context context, int i) {
        return zza(context, i, GOOGLE_PLAY_SERVICES_PACKAGE) && zzb(context.getPackageManager(), GOOGLE_PLAY_SERVICES_PACKAGE);
    }

    @Deprecated
    public static boolean zze(Context context, int i) {
        return i == 18 ? true : i == 1 ? zzh(context, GOOGLE_PLAY_SERVICES_PACKAGE) : false;
    }

    @Deprecated
    public static boolean zzf(Context context, int i) {
        return i == 9 ? zzh(context, GOOGLE_PLAY_STORE_PACKAGE) : false;
    }

    public static boolean zzh(Context context, String str) {
        if (zzlk.zzoZ()) {
            for (SessionInfo appPackageName : context.getPackageManager().getPackageInstaller().getAllSessions()) {
                if (str.equals(appPackageName.getAppPackageName())) {
                    return true;
                }
            }
        }
        try {
            if (context.getPackageManager().getApplicationInfo(str, Utility.DEFAULT_STREAM_BUFFER_SIZE).enabled) {
                return true;
            }
        } catch (NameNotFoundException e) {
        }
        return false;
    }

    private static int zzml() {
        return 7571000;
    }

    public static boolean zzmm() {
        return zzVS ? zzVT : DBLikedChannelsHelper.USER.equals(Build.TYPE);
    }
}
