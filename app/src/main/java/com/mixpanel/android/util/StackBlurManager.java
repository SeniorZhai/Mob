package com.mixpanel.android.util;

import android.graphics.Bitmap;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.reflect.Array;

public class StackBlurManager {
    public static void process(Bitmap source, int radius) {
        if (radius >= 1) {
            int i;
            int y;
            int bsum;
            int gsum;
            int rsum;
            int boutsum;
            int goutsum;
            int routsum;
            int binsum;
            int ginsum;
            int rinsum;
            int p;
            int[] sir;
            int rbs;
            int stackpointer;
            int x;
            int width = source.getWidth();
            int height = source.getHeight();
            int[] currentPixels = new int[(width * height)];
            source.getPixels(currentPixels, 0, width, 0, 0, width, height);
            int wm = width - 1;
            int hm = height - 1;
            int wh = width * height;
            int div = (radius + radius) + 1;
            int[] r = new int[wh];
            int[] g = new int[wh];
            int[] b = new int[wh];
            int[] vmin = new int[Math.max(width, height)];
            int divsum = (div + 1) >> 1;
            divsum *= divsum;
            int[] dv = new int[(divsum * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY)];
            for (i = 0; i < divsum * AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY; i++) {
                dv[i] = i / divsum;
            }
            int yw = 0;
            int yi = 0;
            int[][] stack = (int[][]) Array.newInstance(Integer.TYPE, new int[]{div, 3});
            int r1 = radius + 1;
            for (y = 0; y < height; y++) {
                bsum = 0;
                gsum = 0;
                rsum = 0;
                boutsum = 0;
                goutsum = 0;
                routsum = 0;
                binsum = 0;
                ginsum = 0;
                rinsum = 0;
                for (i = -radius; i <= radius; i++) {
                    p = currentPixels[Math.min(wm, Math.max(i, 0)) + yi];
                    sir = stack[i + radius];
                    sir[0] = (16711680 & p) >> 16;
                    sir[1] = (MotionEventCompat.ACTION_POINTER_INDEX_MASK & p) >> 8;
                    sir[2] = p & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
                    rbs = r1 - Math.abs(i);
                    rsum += sir[0] * rbs;
                    gsum += sir[1] * rbs;
                    bsum += sir[2] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                }
                stackpointer = radius;
                for (x = 0; x < width; x++) {
                    r[yi] = dv[rsum];
                    g[yi] = dv[gsum];
                    b[yi] = dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    sir = stack[((stackpointer - radius) + div) % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if (y == 0) {
                        vmin[x] = Math.min((x + radius) + 1, wm);
                    }
                    p = currentPixels[vmin[x] + yw];
                    sir[0] = (16711680 & p) >> 16;
                    sir[1] = (MotionEventCompat.ACTION_POINTER_INDEX_MASK & p) >> 8;
                    sir[2] = p & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer % div];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    yi++;
                }
                yw += width;
            }
            for (x = 0; x < width; x++) {
                bsum = 0;
                gsum = 0;
                rsum = 0;
                boutsum = 0;
                goutsum = 0;
                routsum = 0;
                binsum = 0;
                ginsum = 0;
                rinsum = 0;
                int yp = (-radius) * width;
                for (i = -radius; i <= radius; i++) {
                    yi = Math.max(0, yp) + x;
                    sir = stack[i + radius];
                    sir[0] = r[yi];
                    sir[1] = g[yi];
                    sir[2] = b[yi];
                    rbs = r1 - Math.abs(i);
                    rsum += r[yi] * rbs;
                    gsum += g[yi] * rbs;
                    bsum += b[yi] * rbs;
                    if (i > 0) {
                        rinsum += sir[0];
                        ginsum += sir[1];
                        binsum += sir[2];
                    } else {
                        routsum += sir[0];
                        goutsum += sir[1];
                        boutsum += sir[2];
                    }
                    if (i < hm) {
                        yp += width;
                    }
                }
                yi = x;
                stackpointer = radius;
                for (y = 0; y < height; y++) {
                    currentPixels[yi] = ((ViewCompat.MEASURED_STATE_MASK | (dv[rsum] << 16)) | (dv[gsum] << 8)) | dv[bsum];
                    rsum -= routsum;
                    gsum -= goutsum;
                    bsum -= boutsum;
                    sir = stack[((stackpointer - radius) + div) % div];
                    routsum -= sir[0];
                    goutsum -= sir[1];
                    boutsum -= sir[2];
                    if (x == 0) {
                        vmin[y] = Math.min(y + r1, hm) * width;
                    }
                    p = x + vmin[y];
                    sir[0] = r[p];
                    sir[1] = g[p];
                    sir[2] = b[p];
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                    rsum += rinsum;
                    gsum += ginsum;
                    bsum += binsum;
                    stackpointer = (stackpointer + 1) % div;
                    sir = stack[stackpointer];
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                    rinsum -= sir[0];
                    ginsum -= sir[1];
                    binsum -= sir[2];
                    yi += width;
                }
            }
            source.setPixels(currentPixels, 0, width, 0, 0, width, height);
        }
    }
}
