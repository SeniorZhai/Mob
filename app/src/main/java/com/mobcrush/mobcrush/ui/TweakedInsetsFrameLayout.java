package com.mobcrush.mobcrush.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.mobcrush.mobcrush.common.UIUtils;

public class TweakedInsetsFrameLayout extends FrameLayout {
    private boolean mTweakingDisabled = false;

    public TweakedInsetsFrameLayout(Context context) {
        super(context);
    }

    public TweakedInsetsFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TweakedInsetsFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @TargetApi(16)
    protected boolean fitSystemWindows(Rect insets) {
        insets.left = 0;
        if (!this.mTweakingDisabled) {
            insets.top = 0;
        }
        insets.right = 0;
        if (UIUtils.isLandscape(getContext())) {
            insets.bottom = 0;
        }
        return super.fitSystemWindows(insets);
    }

    public void disableTweaking() {
        this.mTweakingDisabled = true;
    }
}
