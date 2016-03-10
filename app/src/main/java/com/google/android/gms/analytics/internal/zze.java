package com.google.android.gms.analytics.internal;

import com.google.android.gms.common.GoogleApiAvailability;
import com.mobcrush.mobcrush.Constants;

public class zze {
    public static final String VERSION = String.valueOf(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_VERSION_CODE / Constants.UPDATE_COFIG_INTERVAL).replaceAll("(\\d+)(\\d)(\\d\\d)", "$1.$2.$3");
    public static final String zzJB = ("ma" + VERSION);
}
