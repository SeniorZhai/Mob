package com.google.android.gms.internal;

import android.content.res.Configuration;
import android.content.res.Resources;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public final class zzle {
    public static boolean zzb(Resources resources) {
        if (resources == null) {
            return false;
        }
        return (zzlk.zzoR() && ((resources.getConfiguration().screenLayout & 15) > 3)) || zzc(resources);
    }

    private static boolean zzc(Resources resources) {
        Configuration configuration = resources.getConfiguration();
        return zzlk.zzoT() && (configuration.screenLayout & 15) <= 3 && configuration.smallestScreenWidthDp >= SettingsJsonConstants.ANALYTICS_FLUSH_INTERVAL_SECS_DEFAULT;
    }
}
