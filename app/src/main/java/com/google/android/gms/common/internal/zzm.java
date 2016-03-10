package com.google.android.gms.common.internal;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;

public class zzm {
    private static final Uri zzaaV = Uri.parse("http://plus.google.com/");
    private static final Uri zzaaW = zzaaV.buildUpon().appendPath("circles").appendPath("find").build();

    public static Intent zzce(String str) {
        Uri fromParts = Uri.fromParts("package", str, null);
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS");
        intent.setData(fromParts);
        return intent;
    }

    private static Uri zzcf(String str) {
        return Uri.parse("market://details").buildUpon().appendQueryParameter(DBLikedChannelsHelper.KEY_ID, str).build();
    }

    public static Intent zzcg(String str) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(zzcf(str));
        intent.setPackage(GooglePlayServicesUtil.GOOGLE_PLAY_STORE_PACKAGE);
        intent.addFlags(AccessibilityNodeInfoCompat.ACTION_COLLAPSE);
        return intent;
    }

    public static Intent zznX() {
        Intent intent = new Intent("com.google.android.clockwork.home.UPDATE_ANDROID_WEAR_ACTION");
        intent.setPackage("com.google.android.wearable.app");
        return intent;
    }
}
