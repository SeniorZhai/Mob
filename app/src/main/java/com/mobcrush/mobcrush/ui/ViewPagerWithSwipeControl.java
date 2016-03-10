package com.mobcrush.mobcrush.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class ViewPagerWithSwipeControl extends ViewPager {
    private boolean mIsSwipeEnabled = true;

    public ViewPagerWithSwipeControl(Context context) {
        super(context);
    }

    public ViewPagerWithSwipeControl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (this.mIsSwipeEnabled) {
            return super.onTouchEvent(event);
        }
        return false;
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (this.mIsSwipeEnabled) {
            return super.onInterceptTouchEvent(event);
        }
        return false;
    }

    public void enableSwipe(boolean enabled) {
        this.mIsSwipeEnabled = enabled;
    }
}
