package com.mixpanel.android.mpmetrics;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class SystemInformation {
    private static final String LOGTAG = "MixpanelAPI.SysInfo";
    private final Integer mAppVersionCode;
    private final String mAppVersionName;
    private final Context mContext;
    private final DisplayMetrics mDisplayMetrics;
    private final Boolean mHasNFC;
    private final Boolean mHasTelephony;

    public SystemInformation(Context context) {
        this.mContext = context;
        PackageManager packageManager = this.mContext.getPackageManager();
        String foundAppVersionName = null;
        Integer foundAppVersionCode = null;
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(this.mContext.getPackageName(), 0);
            foundAppVersionName = packageInfo.versionName;
            foundAppVersionCode = Integer.valueOf(packageInfo.versionCode);
        } catch (NameNotFoundException e) {
            Log.w(LOGTAG, "System information constructed with a context that apparently doesn't exist.");
        }
        this.mAppVersionName = foundAppVersionName;
        this.mAppVersionCode = foundAppVersionCode;
        Method hasSystemFeatureMethod = null;
        try {
            hasSystemFeatureMethod = packageManager.getClass().getMethod("hasSystemFeature", new Class[]{String.class});
        } catch (NoSuchMethodException e2) {
        }
        Boolean foundNFC = null;
        Boolean foundTelephony = null;
        if (hasSystemFeatureMethod != null) {
            try {
                foundNFC = (Boolean) hasSystemFeatureMethod.invoke(packageManager, new Object[]{"android.hardware.nfc"});
                foundTelephony = (Boolean) hasSystemFeatureMethod.invoke(packageManager, new Object[]{"android.hardware.telephony"});
            } catch (InvocationTargetException e3) {
                Log.w(LOGTAG, "System version appeared to support PackageManager.hasSystemFeature, but we were unable to call it.");
            } catch (IllegalAccessException e4) {
                Log.w(LOGTAG, "System version appeared to support PackageManager.hasSystemFeature, but we were unable to call it.");
            }
        }
        this.mHasNFC = foundNFC;
        this.mHasTelephony = foundTelephony;
        this.mDisplayMetrics = new DisplayMetrics();
        ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getMetrics(this.mDisplayMetrics);
    }

    public String getAppVersionName() {
        return this.mAppVersionName;
    }

    public Integer getAppVersionCode() {
        return this.mAppVersionCode;
    }

    public boolean hasNFC() {
        return this.mHasNFC.booleanValue();
    }

    public boolean hasTelephony() {
        return this.mHasTelephony.booleanValue();
    }

    public DisplayMetrics getDisplayMetrics() {
        return this.mDisplayMetrics;
    }

    public String getPhoneRadioType() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager == null) {
            return null;
        }
        switch (telephonyManager.getPhoneType()) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                return "none";
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return "gsm";
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return "cdma";
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return "sip";
            default:
                return null;
        }
    }

    public String getCurrentNetworkOperator() {
        TelephonyManager telephonyManager = (TelephonyManager) this.mContext.getSystemService("phone");
        if (telephonyManager != null) {
            return telephonyManager.getNetworkOperatorName();
        }
        return null;
    }

    public Boolean isWifiConnected() {
        if (this.mContext.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") == 0) {
            return Boolean.valueOf(((ConnectivityManager) this.mContext.getSystemService("connectivity")).getNetworkInfo(1).isConnected());
        }
        return null;
    }

    public Boolean isBluetoothEnabled() {
        Boolean isBluetoothEnabled = null;
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter != null) {
                isBluetoothEnabled = Boolean.valueOf(bluetoothAdapter.isEnabled());
            }
        } catch (SecurityException e) {
        }
        return isBluetoothEnabled;
    }

    public String getBluetoothVersion() {
        if (VERSION.SDK_INT < 8) {
            return null;
        }
        String bluetoothVersion = "none";
        if (VERSION.SDK_INT >= 18 && this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
            return "ble";
        }
        if (this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth")) {
            return "classic";
        }
        return bluetoothVersion;
    }
}
