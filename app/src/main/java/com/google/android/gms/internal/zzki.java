package com.google.android.gms.internal;

import android.graphics.Canvas;
import android.graphics.Path;
import android.net.Uri;
import android.widget.ImageView;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public final class zzki extends ImageView {
    private Uri zzZF;
    private int zzZG;
    private int zzZH;
    private zza zzZI;
    private int zzZJ;
    private float zzZK;

    public interface zza {
        Path zzk(int i, int i2);
    }

    protected void onDraw(Canvas canvas) {
        if (this.zzZI != null) {
            canvas.clipPath(this.zzZI.zzk(getWidth(), getHeight()));
        }
        super.onDraw(canvas);
        if (this.zzZH != 0) {
            canvas.drawColor(this.zzZH);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight;
        int i;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        switch (this.zzZJ) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                measuredHeight = getMeasuredHeight();
                i = (int) (((float) measuredHeight) * this.zzZK);
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                i = getMeasuredWidth();
                measuredHeight = (int) (((float) i) / this.zzZK);
                break;
            default:
                return;
        }
        setMeasuredDimension(i, measuredHeight);
    }

    public void zzbo(int i) {
        this.zzZG = i;
    }

    public void zzi(Uri uri) {
        this.zzZF = uri;
    }

    public int zznr() {
        return this.zzZG;
    }
}
