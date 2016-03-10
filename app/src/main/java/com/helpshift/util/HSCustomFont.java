package com.helpshift.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import java.lang.ref.SoftReference;
import java.util.Hashtable;

public final class HSCustomFont {
    public static final String TAG = "HelpShiftDebug";
    private static final Hashtable<String, SoftReference<Typeface>> fontCache = new Hashtable();

    public static void setCustomFont(View textViewOrButton, Context context, AttributeSet attrs, int[] attributeSet, int fontId) {
        TypedArray a = context.obtainStyledAttributes(attrs, attributeSet);
        setCustomFont(textViewOrButton, context, a.getString(fontId));
        a.recycle();
    }

    private static boolean setCustomFont(View textViewOrButton, Context context, String asset) {
        if (TextUtils.isEmpty(asset)) {
            return false;
        }
        try {
            Typeface tf = getFont(context, asset);
            if (textViewOrButton instanceof TextView) {
                ((TextView) textViewOrButton).setTypeface(tf);
            } else {
                ((Button) textViewOrButton).setTypeface(tf);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Could not get typeface: " + asset, e);
            return false;
        }
    }

    public static Typeface getFont(Context c, String name) {
        synchronized (fontCache) {
            if (fontCache.get(name) != null) {
                SoftReference<Typeface> ref = (SoftReference) fontCache.get(name);
                if (ref.get() != null) {
                    Typeface typeface = (Typeface) ref.get();
                    return typeface;
                }
            }
            Typeface typeface2 = Typeface.createFromAsset(c.getAssets(), name);
            fontCache.put(name, new SoftReference(typeface2));
            return typeface2;
        }
    }
}
