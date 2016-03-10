package com.google.android.gms.common;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import com.google.android.gms.common.internal.zzu;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.internal.zzy;
import com.google.android.gms.dynamic.zzg.zza;

public final class SignInButton extends FrameLayout implements OnClickListener {
    public static final int COLOR_DARK = 0;
    public static final int COLOR_LIGHT = 1;
    public static final int SIZE_ICON_ONLY = 2;
    public static final int SIZE_STANDARD = 0;
    public static final int SIZE_WIDE = 1;
    private int mColor;
    private int mSize;
    private View zzVZ;
    private OnClickListener zzWa;

    public SignInButton(Context context) {
        this(context, null);
    }

    public SignInButton(Context context, AttributeSet attrs) {
        this(context, attrs, SIZE_STANDARD);
    }

    public SignInButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.zzWa = null;
        setStyle(SIZE_STANDARD, SIZE_STANDARD);
    }

    private static Button zza(Context context, int i, int i2) {
        Button com_google_android_gms_common_internal_zzy = new zzy(context);
        com_google_android_gms_common_internal_zzy.zza(context.getResources(), i, i2);
        return com_google_android_gms_common_internal_zzy;
    }

    private void zzaf(Context context) {
        if (this.zzVZ != null) {
            removeView(this.zzVZ);
        }
        try {
            this.zzVZ = zzx.zzb(context, this.mSize, this.mColor);
        } catch (zza e) {
            Log.w("SignInButton", "Sign in button not found, using placeholder instead");
            this.zzVZ = zza(context, this.mSize, this.mColor);
        }
        addView(this.zzVZ);
        this.zzVZ.setEnabled(isEnabled());
        this.zzVZ.setOnClickListener(this);
    }

    public void onClick(View view) {
        if (this.zzWa != null && view == this.zzVZ) {
            this.zzWa.onClick(this);
        }
    }

    public void setColorScheme(int colorScheme) {
        setStyle(this.mSize, colorScheme);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.zzVZ.setEnabled(enabled);
    }

    public void setOnClickListener(OnClickListener listener) {
        this.zzWa = listener;
        if (this.zzVZ != null) {
            this.zzVZ.setOnClickListener(this);
        }
    }

    public void setSize(int buttonSize) {
        setStyle(buttonSize, this.mColor);
    }

    public void setStyle(int buttonSize, int colorScheme) {
        boolean z = buttonSize >= 0 && buttonSize < 3;
        Object[] objArr = new Object[SIZE_WIDE];
        objArr[SIZE_STANDARD] = Integer.valueOf(buttonSize);
        zzu.zza(z, "Unknown button size %d", objArr);
        z = colorScheme >= 0 && colorScheme < SIZE_ICON_ONLY;
        Object[] objArr2 = new Object[SIZE_WIDE];
        objArr2[SIZE_STANDARD] = Integer.valueOf(colorScheme);
        zzu.zza(z, "Unknown color scheme %s", objArr2);
        this.mSize = buttonSize;
        this.mColor = colorScheme;
        zzaf(getContext());
    }
}
