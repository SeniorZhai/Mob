package com.mixpanel.android.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;

public class ActivityImageUtils {
    private static final String LOGTAG = "MixpanelAPI.ActImgUtils";

    public static Bitmap getScaledScreenshot(Activity activity, int scaleWidth, int scaleHeight, boolean relativeScaleIfTrue) {
        View rootView = activity.findViewById(16908290).getRootView();
        boolean originalCacheState = rootView.isDrawingCacheEnabled();
        rootView.setDrawingCacheEnabled(true);
        rootView.buildDrawingCache(true);
        Bitmap original = rootView.getDrawingCache();
        Bitmap scaled = null;
        if (original != null && original.getWidth() > 0 && original.getHeight() > 0) {
            if (relativeScaleIfTrue) {
                scaleWidth = original.getWidth() / scaleWidth;
                scaleHeight = original.getHeight() / scaleHeight;
            }
            if (scaleWidth > 0 && scaleHeight > 0) {
                try {
                    scaled = Bitmap.createScaledBitmap(original, scaleWidth, scaleHeight, false);
                } catch (OutOfMemoryError e) {
                    Log.i(LOGTAG, "Not enough memory to produce scaled image, returning a null screenshot");
                }
            }
        }
        if (!originalCacheState) {
            rootView.setDrawingCacheEnabled(false);
        }
        return scaled;
    }

    public static int getHighlightColorFromBackground(Activity activity) {
        int incolor = ViewCompat.MEASURED_STATE_MASK;
        Bitmap screenshot1px = getScaledScreenshot(activity, 1, 1, false);
        if (screenshot1px != null) {
            incolor = screenshot1px.getPixel(0, 0);
        }
        return getHighlightColor(incolor);
    }

    public static int getHighlightColorFromBitmap(Bitmap bitmap) {
        int incolor = ViewCompat.MEASURED_STATE_MASK;
        if (bitmap != null) {
            incolor = Bitmap.createScaledBitmap(bitmap, 1, 1, false).getPixel(0, 0);
        }
        return getHighlightColor(incolor);
    }

    public static int getHighlightColor(int sampleColor) {
        float[] hsvBackground = new float[3];
        Color.colorToHSV(sampleColor, hsvBackground);
        hsvBackground[2] = 0.3f;
        return Color.HSVToColor(242, hsvBackground);
    }
}
