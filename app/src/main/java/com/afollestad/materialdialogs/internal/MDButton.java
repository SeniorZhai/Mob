package com.afollestad.materialdialogs.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v7.internal.text.AllCapsTransformationMethod;
import android.util.AttributeSet;
import android.widget.TextView;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.R;
import com.afollestad.materialdialogs.util.DialogUtils;

public class MDButton extends TextView {
    private Drawable mDefaultBackground;
    private boolean mStacked = false;
    private Drawable mStackedBackground;
    private int mStackedEndPadding;
    private GravityEnum mStackedGravity;

    public MDButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public MDButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public MDButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mStackedEndPadding = context.getResources().getDimensionPixelSize(R.dimen.md_dialog_frame_margin);
        this.mStackedGravity = GravityEnum.END;
    }

    void setStacked(boolean stacked, boolean force) {
        if (this.mStacked != stacked || force) {
            setGravity(stacked ? this.mStackedGravity.getGravityInt() | 16 : 17);
            if (VERSION.SDK_INT >= 17) {
                setTextAlignment(stacked ? this.mStackedGravity.getTextAlignment() : 4);
            }
            DialogUtils.setBackgroundCompat(this, stacked ? this.mStackedBackground : this.mDefaultBackground);
            if (stacked) {
                setPadding(this.mStackedEndPadding, getPaddingTop(), this.mStackedEndPadding, getPaddingBottom());
            }
            this.mStacked = stacked;
        }
    }

    public void setStackedGravity(GravityEnum gravity) {
        this.mStackedGravity = gravity;
    }

    public void setStackedSelector(Drawable d) {
        this.mStackedBackground = d;
        if (this.mStacked) {
            setStacked(true, true);
        }
    }

    public void setDefaultSelector(Drawable d) {
        this.mDefaultBackground = d;
        if (!this.mStacked) {
            setStacked(false, true);
        }
    }

    public void setAllCapsCompat(boolean allCaps) {
        if (VERSION.SDK_INT >= 14) {
            setAllCaps(allCaps);
        } else if (allCaps) {
            setTransformationMethod(new AllCapsTransformationMethod(getContext()));
        } else {
            setTransformationMethod(null);
        }
    }
}
