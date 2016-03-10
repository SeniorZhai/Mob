package com.google.android.gms.internal;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.Drawable.ConstantState;
import android.os.SystemClock;
import com.android.volley.DefaultRetryPolicy;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public final class zzkg extends Drawable implements Callback {
    private int mFrom;
    private long zzKT;
    private int zzZA;
    private boolean zzZh;
    private int zzZo;
    private int zzZp;
    private int zzZq;
    private int zzZr;
    private int zzZs;
    private boolean zzZt;
    private zzb zzZu;
    private Drawable zzZv;
    private Drawable zzZw;
    private boolean zzZx;
    private boolean zzZy;
    private boolean zzZz;

    private static final class zza extends Drawable {
        private static final zza zzZB = new zza();
        private static final zza zzZC = new zza();

        private static final class zza extends ConstantState {
            private zza() {
            }

            public int getChangingConfigurations() {
                return 0;
            }

            public Drawable newDrawable() {
                return zza.zzZB;
            }
        }

        private zza() {
        }

        public void draw(Canvas canvas) {
        }

        public ConstantState getConstantState() {
            return zzZC;
        }

        public int getOpacity() {
            return -2;
        }

        public void setAlpha(int alpha) {
        }

        public void setColorFilter(ColorFilter cf) {
        }
    }

    static final class zzb extends ConstantState {
        int zzZD;
        int zzZE;

        zzb(zzb com_google_android_gms_internal_zzkg_zzb) {
            if (com_google_android_gms_internal_zzkg_zzb != null) {
                this.zzZD = com_google_android_gms_internal_zzkg_zzb.zzZD;
                this.zzZE = com_google_android_gms_internal_zzkg_zzb.zzZE;
            }
        }

        public int getChangingConfigurations() {
            return this.zzZD;
        }

        public Drawable newDrawable() {
            return new zzkg(this);
        }
    }

    public zzkg(Drawable drawable, Drawable drawable2) {
        this(null);
        if (drawable == null) {
            drawable = zza.zzZB;
        }
        this.zzZv = drawable;
        drawable.setCallback(this);
        zzb com_google_android_gms_internal_zzkg_zzb = this.zzZu;
        com_google_android_gms_internal_zzkg_zzb.zzZE |= drawable.getChangingConfigurations();
        if (drawable2 == null) {
            drawable2 = zza.zzZB;
        }
        this.zzZw = drawable2;
        drawable2.setCallback(this);
        com_google_android_gms_internal_zzkg_zzb = this.zzZu;
        com_google_android_gms_internal_zzkg_zzb.zzZE |= drawable2.getChangingConfigurations();
    }

    zzkg(zzb com_google_android_gms_internal_zzkg_zzb) {
        this.zzZo = 0;
        this.zzZq = SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
        this.zzZs = 0;
        this.zzZh = true;
        this.zzZu = new zzb(com_google_android_gms_internal_zzkg_zzb);
    }

    public boolean canConstantState() {
        if (!this.zzZx) {
            boolean z = (this.zzZv.getConstantState() == null || this.zzZw.getConstantState() == null) ? false : true;
            this.zzZy = z;
            this.zzZx = true;
        }
        return this.zzZy;
    }

    public void draw(Canvas canvas) {
        int i = 1;
        int i2 = 0;
        switch (this.zzZo) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                this.zzKT = SystemClock.uptimeMillis();
                this.zzZo = 2;
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                if (this.zzKT >= 0) {
                    float uptimeMillis = ((float) (SystemClock.uptimeMillis() - this.zzKT)) / ((float) this.zzZr);
                    if (uptimeMillis < DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
                        i = 0;
                    }
                    if (i != 0) {
                        this.zzZo = 0;
                    }
                    float min = Math.min(uptimeMillis, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    this.zzZs = (int) ((min * ((float) (this.zzZp - this.mFrom))) + ((float) this.mFrom));
                    break;
                }
                break;
        }
        i2 = i;
        i = this.zzZs;
        boolean z = this.zzZh;
        Drawable drawable = this.zzZv;
        Drawable drawable2 = this.zzZw;
        if (i2 != 0) {
            if (!z || i == 0) {
                drawable.draw(canvas);
            }
            if (i == this.zzZq) {
                drawable2.setAlpha(this.zzZq);
                drawable2.draw(canvas);
                return;
            }
            return;
        }
        if (z) {
            drawable.setAlpha(this.zzZq - i);
        }
        drawable.draw(canvas);
        if (z) {
            drawable.setAlpha(this.zzZq);
        }
        if (i > 0) {
            drawable2.setAlpha(i);
            drawable2.draw(canvas);
            drawable2.setAlpha(this.zzZq);
        }
        invalidateSelf();
    }

    public int getChangingConfigurations() {
        return (super.getChangingConfigurations() | this.zzZu.zzZD) | this.zzZu.zzZE;
    }

    public ConstantState getConstantState() {
        if (!canConstantState()) {
            return null;
        }
        this.zzZu.zzZD = getChangingConfigurations();
        return this.zzZu;
    }

    public int getIntrinsicHeight() {
        return Math.max(this.zzZv.getIntrinsicHeight(), this.zzZw.getIntrinsicHeight());
    }

    public int getIntrinsicWidth() {
        return Math.max(this.zzZv.getIntrinsicWidth(), this.zzZw.getIntrinsicWidth());
    }

    public int getOpacity() {
        if (!this.zzZz) {
            this.zzZA = Drawable.resolveOpacity(this.zzZv.getOpacity(), this.zzZw.getOpacity());
            this.zzZz = true;
        }
        return this.zzZA;
    }

    public void invalidateDrawable(Drawable who) {
        if (zzlk.zzoR()) {
            Callback callback = getCallback();
            if (callback != null) {
                callback.invalidateDrawable(this);
            }
        }
    }

    public Drawable mutate() {
        if (!this.zzZt && super.mutate() == this) {
            if (canConstantState()) {
                this.zzZv.mutate();
                this.zzZw.mutate();
                this.zzZt = true;
            } else {
                throw new IllegalStateException("One or more children of this LayerDrawable does not have constant state; this drawable cannot be mutated.");
            }
        }
        return this;
    }

    protected void onBoundsChange(Rect bounds) {
        this.zzZv.setBounds(bounds);
        this.zzZw.setBounds(bounds);
    }

    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (zzlk.zzoR()) {
            Callback callback = getCallback();
            if (callback != null) {
                callback.scheduleDrawable(this, what, when);
            }
        }
    }

    public void setAlpha(int alpha) {
        if (this.zzZs == this.zzZq) {
            this.zzZs = alpha;
        }
        this.zzZq = alpha;
        invalidateSelf();
    }

    public void setColorFilter(ColorFilter cf) {
        this.zzZv.setColorFilter(cf);
        this.zzZw.setColorFilter(cf);
    }

    public void startTransition(int durationMillis) {
        this.mFrom = 0;
        this.zzZp = this.zzZq;
        this.zzZs = 0;
        this.zzZr = durationMillis;
        this.zzZo = 1;
        invalidateSelf();
    }

    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (zzlk.zzoR()) {
            Callback callback = getCallback();
            if (callback != null) {
                callback.unscheduleDrawable(this, what);
            }
        }
    }

    public Drawable zznp() {
        return this.zzZw;
    }
}
