package com.mobcrush.mobcrush.common;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import com.facebook.internal.Utility;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.datamodel.User;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.http.protocol.HTTP;

public class Utils {
    public static int getAppVersion(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static String getAppVersionName(Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    public static boolean isOrientationLocked(Context context) {
        return System.getInt(context.getContentResolver(), "accelerometer_rotation", 1) == 0;
    }

    public static void setUserToCrashlytics() {
        User user = PreferenceUtility.getUser();
        if (user != null) {
            Crashlytics.setUserName(user.username);
            Crashlytics.setUserIdentifier(user._id);
        }
    }

    public static boolean isInternetAvailable(Context context) {
        if (context == null) {
            return false;
        }
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService("connectivity");
        if (conMgr == null) {
            return false;
        }
        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

    public static boolean isWifiAvailable(Context context) {
        boolean z = true;
        if (context == null) {
            return false;
        }
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService("connectivity");
        if (conMgr == null) {
            return false;
        }
        NetworkInfo networkInfo = conMgr.getActiveNetworkInfo();
        if (networkInfo == null || networkInfo.getType() != 1) {
            z = false;
        }
        return z;
    }

    public static Integer compareVersions(String v1, String v2) {
        int i = 0;
        if (v1 == null || v2 == null) {
            int i2;
            if (v1 == null) {
                i2 = 0;
            } else {
                i2 = 1;
            }
            if (v2 != null) {
                i = 1;
            }
            return Integer.valueOf(i2 - i);
        }
        String[] vals1 = v1.split("\\.");
        String[] vals2 = v2.split("\\.");
        int i3 = 0;
        while (i3 < vals1.length && i3 < vals2.length && TextUtils.equals(vals1[i3], vals2[i3])) {
            i3++;
        }
        if (i3 >= vals1.length || i3 >= vals2.length) {
            return Integer.valueOf(vals1.length - vals2.length);
        }
        return Integer.valueOf(Integer.valueOf(vals1[i3]).compareTo(Integer.valueOf(vals2[i3])));
    }

    public static String getMuteDateTimeString(Context context, long time, boolean pureTime) {
        if (pureTime) {
            try {
                String prefix = BuildConfig.FLAVOR;
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                return BuildConfig.FLAVOR;
            }
        }
        prefix = MainApplication.getRString(R.string.MutedUntil_, new Object[0]);
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        StringBuilder append;
        String str;
        switch (c.get(6) - Calendar.getInstance().get(6)) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                append = new StringBuilder().append(prefix).append(MainApplication.getRString(R.string.Today, new Object[0])).append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
                str = (context == null || !DateFormat.is24HourFormat(context)) ? "h:mm a" : "H:mm";
                return append.append(new SimpleDateFormat(str, Locale.getDefault()).format(new Date(time))).toString();
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                append = new StringBuilder().append(prefix).append(MainApplication.getRString(R.string.Tomorrow, new Object[0])).append(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
                str = (context == null || !DateFormat.is24HourFormat(context)) ? "h:mm a" : "H:mm";
                return append.append(new SimpleDateFormat(str, Locale.getDefault()).format(new Date(time))).toString();
            default:
                if (time - System.currentTimeMillis() < 31536000000L) {
                    append = new StringBuilder().append(MainApplication.getRString(R.string.MutedUntil_, new Object[0]));
                    str = (context == null || !DateFormat.is24HourFormat(context)) ? "MM/dd/yyyy h:mm a" : "MM/dd/yyyy H:mm";
                    return append.append(new SimpleDateFormat(str, Locale.getDefault()).format(new Date(time))).toString();
                }
                return MainApplication.getRString(pureTime ? R.string.Indefinitely : R.string.MutedIndefinitely, new Object[0]);
        }
    }

    public static long convertTimeForCurrentTimeZone(double time) {
        if (time <= 0.0d) {
            return 0;
        }
        Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("UTC"));
        c.setTimeInMillis((long) time);
        c.setTimeZone(TimeZone.getDefault());
        return c.getTimeInMillis();
    }

    public static String getStringFromRawResource(Context context, int resId) throws IOException {
        if (context == null) {
            return null;
        }
        InputStream is = context.getResources().openRawResource(resId);
        Writer writer = new StringWriter();
        char[] buffer = new char[Utility.DEFAULT_STREAM_BUFFER_SIZE];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, HTTP.UTF_8));
            while (true) {
                int n = reader.read(buffer);
                if (n == -1) {
                    break;
                }
                writer.write(buffer, 0, n);
            }
            return writer.toString();
        } finally {
            is.close();
        }
    }

    public static String getAppVersionString() {
        return MainApplication.getRString(R.string.app_name, new Object[0]) + " v." + Constants.APP_VERSION_NAME;
    }

    public static String getCurrentDateUTC() {
        SimpleDateFormat dateUTC = getDateFormat();
        dateUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateUTC.format(new Date());
    }

    public static int getAgeFromDOB(Calendar dob) {
        Calendar today = Calendar.getInstance();
        int age = today.get(1) - dob.get(1);
        if (today.get(2) < dob.get(2)) {
            return age - 1;
        }
        if (today.get(2) != dob.get(2) || today.get(5) >= dob.get(5)) {
            return age;
        }
        return age - 1;
    }

    public static String getInstallationDateUTC(Context context) {
        SimpleDateFormat date = getDateFormat();
        date.setTimeZone(TimeZone.getTimeZone("UTC"));
        long installTime = System.currentTimeMillis();
        try {
            installTime = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).firstInstallTime;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return date.format(Long.valueOf(installTime));
    }

    private static SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
    }

    public static MenuItem findItem(Menu menu, int id) {
        if (menu == null || id <= 0) {
            return null;
        }
        for (int i = 0; i < menu.size(); i++) {
            MenuItem mi = menu.getItem(i);
            if (mi != null && mi.getItemId() == id) {
                return mi;
            }
        }
        return null;
    }

    public static int dpToPx(int dp) {
        return Math.round(((float) dp) * MainApplication.getContext().getResources().getDisplayMetrics().density);
    }
}
