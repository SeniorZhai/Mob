package com.helpshift.util;

import android.graphics.Color;
import android.widget.TextView;

public class HSColor {
    public static void setTextViewAlpha(TextView tv, float alpha) {
        int color = tv.getCurrentTextColor();
        tv.setTextColor(Color.argb((int) Math.floor((double) (255.0f * alpha)), Color.red(color), Color.green(color), Color.blue(color)));
    }
}
