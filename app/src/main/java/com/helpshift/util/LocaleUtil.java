package com.helpshift.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import com.helpshift.HSStorage;
import com.helpshift.HelpshiftContext;
import io.fabric.sdk.android.services.events.EventsFilesManager;
import java.util.Locale;

public class LocaleUtil {
    private static final HSStorage storage = new HSStorage(HelpshiftContext.getApplicationContext());

    public static void changeLanguage(Context context) {
        String language = storage.getSdkLanguage();
        if (!TextUtils.isEmpty(language)) {
            Resources resources = context.getResources();
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            Configuration configuration = resources.getConfiguration();
            configuration.locale = getLocale(language);
            resources.updateConfiguration(configuration, displayMetrics);
        }
    }

    private static Locale getLocale(String language) {
        if (!language.contains(EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR)) {
            return new Locale(language);
        }
        String[] languageArray = language.split(EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR);
        return new Locale(languageArray[0], languageArray[1]);
    }

    public static String getAcceptLanguageHeader() {
        String sdkLanguage = storage.getSdkLanguage();
        if (TextUtils.isEmpty(sdkLanguage)) {
            return Locale.getDefault().toString();
        }
        return sdkLanguage;
    }
}
