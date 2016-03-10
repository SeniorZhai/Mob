package com.helpshift.util;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.internal.AnalyticsEvents;
import com.helpshift.HSCallable;
import com.helpshift.HSStorage;
import com.helpshift.Helpshift;
import com.helpshift.res.values.HSConfig;
import com.helpshift.res.values.HSConsts;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.AbstractSpiCall;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.MissingResourceException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class Meta {
    public static final String TAG = "HelpShiftDebug";
    private static HSCallable metaFn;

    public static JSONObject getMetaInfo(Context c, Boolean attachDeviceInfo, String customIdentifier) {
        JSONObject meta = new JSONObject();
        try {
            meta.put("breadcrumbs", getBreadCrumbs(c));
        } catch (JSONException e) {
            Log.d(TAG, "Error while getting device info", e);
        }
        try {
            if (attachDeviceInfo.booleanValue()) {
                meta.put("device_info", getDeviceInfo(c));
            } else {
                meta.put("device_info", new JSONObject());
            }
        } catch (JSONException e2) {
            Log.d(TAG, "Error while getting device info", e2);
        }
        try {
            meta.put("extra", getExtra(customIdentifier));
        } catch (JSONException e22) {
            Log.d(TAG, "Error while getting extra info", e22);
        }
        try {
            meta.put("logs", formatLogList(com.helpshift.Log.getLogs(((Integer) HSConfig.configData.get("dbgl")).intValue())));
        } catch (JSONException e222) {
            Log.d(TAG, "Error while getting debug logs", e222);
        }
        try {
            meta.put("device_token", new HSStorage(c).getDeviceToken());
        } catch (JSONException e2222) {
            Log.d(TAG, "Error while getting device token", e2222);
        }
        HSStorage storage = new HSStorage(c);
        if (metaFn != null) {
            try {
                JSONObject customMeta = getCustomMeta();
                if (customMeta != null) {
                    meta.put("custom_meta", customMeta);
                }
                storage.setCustomMetaData(customMeta);
            } catch (JSONException e22222) {
                Log.d(TAG, "Error while getting extra meta", e22222);
            }
        } else {
            try {
                JSONObject storageMeta = storage.getCustomMetaData();
                if (storageMeta != null) {
                    meta.put("custom_meta", storageMeta);
                }
            } catch (JSONException e222222) {
                Log.d(TAG, "Exception in getting meta from storage ", e222222);
            }
        }
        return meta;
    }

    private static JSONArray getBreadCrumbs(Context c) throws JSONException {
        return new HSStorage(c).getBreadCrumbs();
    }

    private static JSONObject getDeviceInfo(Context c) throws JSONException {
        JSONObject deviceInfo = new JSONObject();
        deviceInfo.put("platform", AbstractSpiCall.ANDROID_CLIENT_TYPE);
        deviceInfo.put("library-version", Helpshift.libraryVersion);
        deviceInfo.put("device-model", Build.MODEL);
        deviceInfo.put("os-version", VERSION.RELEASE);
        try {
            deviceInfo.put("language-code", LocaleUtil.getAcceptLanguageHeader());
        } catch (MissingResourceException e) {
            Log.d(TAG, "Device Info - MissingResourceException", e);
        }
        deviceInfo.put(Constants.CHAT_MESSAGE_TIMESTAMP, HSFormat.deviceInfoTsFormat.format(new Date()));
        deviceInfo.put("application-identifier", c.getPackageName());
        deviceInfo.put("application-name", getAppName(c));
        deviceInfo.put("application-version", getApplicationVersion(c));
        deviceInfo.put("disk-space", getDiskSpace(c));
        TelephonyManager tm = (TelephonyManager) c.getSystemService("phone");
        deviceInfo.put("country-code", tm.getSimCountryIso());
        deviceInfo.put("carrier-name", tm.getNetworkOperatorName());
        try {
            deviceInfo.put("network-type", getNetworkType(c));
        } catch (SecurityException e2) {
            Log.d(TAG, "No permission for Network Access", e2);
        }
        Intent batteryStatus = c.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        deviceInfo.put("battery-level", getBatteryLevel(batteryStatus));
        deviceInfo.put("battery-status", getBatteryStatus(batteryStatus));
        return deviceInfo;
    }

    private static JSONObject getExtra(String customIdentifier) throws JSONException {
        JSONObject extra = new JSONObject();
        extra.put("api-version", HSConsts.STATUS_RESOLVED);
        extra.put("library-version", Helpshift.libraryVersion);
        if (customIdentifier != null) {
            extra.put("user-id", customIdentifier);
        }
        return extra;
    }

    private static String getAppName(Context c) {
        ApplicationInfo ai;
        PackageManager pm = c.getPackageManager();
        try {
            ai = pm.getApplicationInfo(c.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            ai = null;
        }
        return (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }

    private static JSONObject getDiskSpace(Context c) {
        JSONObject diskSpace = new JSONObject();
        StatFs phoneStat = new StatFs(Environment.getDataDirectory().getPath());
        StatFs sdStat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        double total_phone_memory = ((double) Math.round(100.0d * ((((double) phoneStat.getBlockCount()) * ((double) phoneStat.getBlockSize())) / 1.073741824E9d))) / 100.0d;
        double free_sd_memory = ((double) Math.round(100.0d * ((((double) sdStat.getAvailableBlocks()) * ((double) sdStat.getBlockSize())) / 1.073741824E9d))) / 100.0d;
        double total_sd_memory = ((double) Math.round(100.0d * ((((double) sdStat.getBlockCount()) * ((double) sdStat.getBlockSize())) / 1.073741824E9d))) / 100.0d;
        try {
            diskSpace.put("free-space-phone", (((double) Math.round(100.0d * ((((double) phoneStat.getAvailableBlocks()) * ((double) phoneStat.getBlockSize())) / 1.073741824E9d))) / 100.0d) + " GB");
            diskSpace.put("total-space-phone", total_phone_memory + " GB");
            diskSpace.put("free-space-sd", free_sd_memory + " GB");
            diskSpace.put("total-space-sd", total_sd_memory + " GB");
        } catch (JSONException e) {
            Log.d(TAG, e.toString(), e);
        }
        return diskSpace;
    }

    private static String getNetworkType(Context c) {
        NetworkInfo ani = ((ConnectivityManager) c.getSystemService("connectivity")).getActiveNetworkInfo();
        String type = AnalyticsEvents.PARAMETER_DIALOG_OUTCOME_VALUE_UNKNOWN;
        if (ani != null) {
            return ani.getTypeName();
        }
        return type;
    }

    private static String getBatteryLevel(Intent batteryStatus) {
        return ((int) ((((float) batteryStatus.getIntExtra("level", -1)) / ((float) batteryStatus.getIntExtra("scale", -1))) * 100.0f)) + "%";
    }

    private static String getBatteryStatus(Intent batteryStatus) {
        int status = batteryStatus.getIntExtra(SettingsJsonConstants.APP_STATUS_KEY, -1);
        boolean isCharging = status == 2 || status == 5;
        return isCharging ? "Charging" : "Not charging";
    }

    public static String getApplicationVersion(Context c) {
        String appVersion = null;
        try {
            return c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            Log.d(TAG, "Package not found exception", e);
            return appVersion;
        }
    }

    private static JSONObject formatLog(HashMap log) {
        JSONObject outputLog = new JSONObject();
        try {
            outputLog.put(SettingsJsonConstants.PROMPT_MESSAGE_KEY, log.get(SettingsJsonConstants.PROMPT_MESSAGE_KEY));
            outputLog.put("level", log.get("level"));
            outputLog.put("tag", log.get("tag"));
            outputLog.put("exception", log.get("exception"));
        } catch (JSONException e) {
            Log.d(TAG, "Format debug lgos", e);
        }
        return outputLog;
    }

    private static JSONArray formatLogList(ArrayList<HashMap> logs) {
        JSONArray outputList = new JSONArray();
        for (int i = 0; i < logs.size(); i++) {
            outputList.put(formatLog((HashMap) logs.get(i)));
        }
        return outputList;
    }

    public static void setMetadataCallback(HSCallable f) {
        metaFn = f;
    }

    public static JSONObject getCustomMeta() throws JSONException {
        if (metaFn != null) {
            HashMap meta = metaFn.call();
            if (meta != null) {
                return new JSONObject(cleanMetaForTags(removeEmptyKeyOrValue(meta)));
            }
        }
        return null;
    }

    private static HashMap removeEmptyKeyOrValue(HashMap metadata) {
        HashMap newMetaData = (HashMap) metadata.clone();
        for (Object key : metadata.keySet()) {
            Object value = metadata.get(key);
            if ((key instanceof String) && ((String) key).trim().equalsIgnoreCase(BuildConfig.FLAVOR)) {
                newMetaData.remove(key);
            }
            if ((value instanceof String) && ((String) value).trim().equalsIgnoreCase(BuildConfig.FLAVOR)) {
                newMetaData.remove(key);
            }
        }
        return newMetaData;
    }

    private static String[] cleanTags(String[] input) {
        String[] v = input;
        int w = v.length;
        int r = w;
        int n = w;
        while (r > 0) {
            r--;
            String s = v[r];
            if (!(s == null || TextUtils.isEmpty(s.trim()))) {
                w--;
                v[w] = s.trim();
            }
        }
        return (String[]) new HashSet(Arrays.asList(ArraysCompat.copyOfRange(v, w, n))).toArray(new String[0]);
    }

    private static HashMap cleanMetaForTags(HashMap meta) {
        Object tags = meta.get(Helpshift.HSTagsKey);
        meta.remove(Helpshift.HSTagsKey);
        if (tags instanceof String[]) {
            meta.put(Helpshift.HSTagsKey, new JSONArray(Arrays.asList(cleanTags((String[]) tags))));
        }
        return meta;
    }
}
