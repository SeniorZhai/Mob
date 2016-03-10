package com.google.android.gms.common.internal;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import com.google.android.gms.R;
import com.google.android.gms.internal.zzle;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.LangUtils;

public final class zzf {
    public static String zzb(Context context, int i, String str) {
        Resources resources = context.getResources();
        switch (i) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                if (zzle.zzb(resources)) {
                    return resources.getString(R.string.common_google_play_services_install_text_tablet, new Object[]{str});
                }
                return resources.getString(R.string.common_google_play_services_install_text_phone, new Object[]{str});
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return resources.getString(R.string.common_google_play_services_update_text, new Object[]{str});
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return resources.getString(R.string.common_google_play_services_enable_text, new Object[]{str});
            case Player.STATE_ENDED /*5*/:
                return resources.getString(R.string.common_google_play_services_invalid_account_text);
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                return resources.getString(R.string.common_google_play_services_network_error_text);
            case HTTP.HT /*9*/:
                return resources.getString(R.string.common_google_play_services_unsupported_text, new Object[]{str});
            case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                return resources.getString(R.string.common_google_play_services_api_unavailable_text, new Object[]{str});
            case LangUtils.HASH_SEED /*17*/:
                return resources.getString(R.string.common_google_play_services_sign_in_failed_text);
            case com.mobcrush.mobcrush.R.styleable.Toolbar_collapseIcon /*18*/:
                return resources.getString(R.string.common_google_play_services_updating_text, new Object[]{str});
            case com.mobcrush.mobcrush.R.styleable.Theme_dialogTheme /*42*/:
                return resources.getString(R.string.common_android_wear_update_text, new Object[]{str});
            default:
                return resources.getString(R.string.common_google_play_services_unknown_issue);
        }
    }

    public static String zzc(Context context, int i, String str) {
        Resources resources = context.getResources();
        switch (i) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                if (zzle.zzb(resources)) {
                    return resources.getString(R.string.common_google_play_services_install_text_tablet, new Object[]{str});
                }
                return resources.getString(R.string.common_google_play_services_install_text_phone, new Object[]{str});
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return resources.getString(R.string.common_google_play_services_update_text, new Object[]{str});
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return resources.getString(R.string.common_google_play_services_enable_text, new Object[]{str});
            case Player.STATE_ENDED /*5*/:
                return resources.getString(R.string.common_google_play_services_invalid_account_text);
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                return resources.getString(R.string.common_google_play_services_network_error_text);
            case HTTP.HT /*9*/:
                return resources.getString(R.string.common_google_play_services_unsupported_text, new Object[]{str});
            case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                return resources.getString(R.string.common_google_play_services_api_unavailable_text, new Object[]{str});
            case LangUtils.HASH_SEED /*17*/:
                return resources.getString(R.string.common_google_play_services_sign_in_failed_text);
            case com.mobcrush.mobcrush.R.styleable.Toolbar_collapseIcon /*18*/:
                return resources.getString(R.string.common_google_play_services_updating_text, new Object[]{str});
            case com.mobcrush.mobcrush.R.styleable.Theme_dialogTheme /*42*/:
                return resources.getString(R.string.common_android_wear_notification_needs_update_text, new Object[]{str});
            default:
                return resources.getString(R.string.common_google_play_services_unknown_issue);
        }
    }

    public static final String zzg(Context context, int i) {
        Resources resources = context.getResources();
        switch (i) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return resources.getString(R.string.common_google_play_services_install_title);
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return resources.getString(R.string.common_google_play_services_update_title);
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return resources.getString(R.string.common_google_play_services_enable_title);
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
            case com.mobcrush.mobcrush.R.styleable.Toolbar_contentInsetEnd /*6*/:
                return null;
            case Player.STATE_ENDED /*5*/:
                Log.e("GooglePlayServicesUtil", "An invalid account was specified when connecting. Please provide a valid account.");
                return resources.getString(R.string.common_google_play_services_invalid_account_title);
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                Log.e("GooglePlayServicesUtil", "Network error occurred. Please retry request later.");
                return resources.getString(R.string.common_google_play_services_network_error_title);
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                Log.e("GooglePlayServicesUtil", "Internal error occurred. Please see logs for detailed information");
                return null;
            case HTTP.HT /*9*/:
                Log.e("GooglePlayServicesUtil", "Google Play services is invalid. Cannot recover.");
                return resources.getString(R.string.common_google_play_services_unsupported_title);
            case HTTP.LF /*10*/:
                Log.e("GooglePlayServicesUtil", "Developer error occurred. Please see logs for detailed information");
                return null;
            case com.mobcrush.mobcrush.R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                Log.e("GooglePlayServicesUtil", "The application is not licensed to the user.");
                return null;
            case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                Log.e("GooglePlayServicesUtil", "One of the API components you attempted to connect to is not available.");
                return null;
            case LangUtils.HASH_SEED /*17*/:
                Log.e("GooglePlayServicesUtil", "The specified account could not be signed in.");
                return resources.getString(R.string.common_google_play_services_sign_in_failed_title);
            case com.mobcrush.mobcrush.R.styleable.Toolbar_collapseIcon /*18*/:
                return resources.getString(R.string.common_google_play_services_updating_title);
            case com.mobcrush.mobcrush.R.styleable.Theme_dialogTheme /*42*/:
                return resources.getString(R.string.common_android_wear_update_title);
            default:
                Log.e("GooglePlayServicesUtil", "Unexpected error code " + i);
                return null;
        }
    }

    public static String zzh(Context context, int i) {
        Resources resources = context.getResources();
        switch (i) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return resources.getString(R.string.common_google_play_services_install_button);
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
            case com.mobcrush.mobcrush.R.styleable.Theme_dialogTheme /*42*/:
                return resources.getString(R.string.common_google_play_services_update_button);
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return resources.getString(R.string.common_google_play_services_enable_button);
            default:
                return resources.getString(17039370);
        }
    }

    public static final String zzi(Context context, int i) {
        Resources resources = context.getResources();
        switch (i) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return resources.getString(R.string.common_google_play_services_install_title);
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return resources.getString(R.string.common_google_play_services_update_title);
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return resources.getString(R.string.common_google_play_services_enable_title);
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
            case com.mobcrush.mobcrush.R.styleable.Toolbar_contentInsetEnd /*6*/:
                return null;
            case Player.STATE_ENDED /*5*/:
                Log.e("GooglePlayServicesUtil", "An invalid account was specified when connecting. Please provide a valid account.");
                return resources.getString(R.string.common_google_play_services_invalid_account_title);
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                Log.e("GooglePlayServicesUtil", "Network error occurred. Please retry request later.");
                return resources.getString(R.string.common_google_play_services_network_error_title);
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                Log.e("GooglePlayServicesUtil", "Internal error occurred. Please see logs for detailed information");
                return null;
            case HTTP.HT /*9*/:
                Log.e("GooglePlayServicesUtil", "Google Play services is invalid. Cannot recover.");
                return resources.getString(R.string.common_google_play_services_unsupported_title);
            case HTTP.LF /*10*/:
                Log.e("GooglePlayServicesUtil", "Developer error occurred. Please see logs for detailed information");
                return null;
            case com.mobcrush.mobcrush.R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                Log.e("GooglePlayServicesUtil", "The application is not licensed to the user.");
                return null;
            case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                Log.e("GooglePlayServicesUtil", "One of the API components you attempted to connect to is not available.");
                return null;
            case LangUtils.HASH_SEED /*17*/:
                Log.e("GooglePlayServicesUtil", "The specified account could not be signed in.");
                return resources.getString(R.string.common_google_play_services_sign_in_failed_title);
            case com.mobcrush.mobcrush.R.styleable.Toolbar_collapseIcon /*18*/:
                return resources.getString(R.string.common_google_play_services_updating_title);
            case com.mobcrush.mobcrush.R.styleable.Theme_dialogTheme /*42*/:
                return resources.getString(R.string.common_android_wear_update_title);
            default:
                Log.e("GooglePlayServicesUtil", "Unexpected error code " + i);
                return null;
        }
    }
}
