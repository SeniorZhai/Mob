package com.mobcrush.mobcrush.common;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

public class AndroidBug5497Workaround {
    private LayoutParams frameLayoutParams = ((LayoutParams) this.mChildOfContent.getLayoutParams());
    private View mChildOfContent;
    private int usableHeightPrevious;

    public static void assistActivity(Activity activity) {
        AndroidBug5497Workaround androidBug5497Workaround = new AndroidBug5497Workaround(activity);
    }

    private AndroidBug5497Workaround(Activity activity) {
        this.mChildOfContent = ((FrameLayout) activity.findViewById(16908290)).getChildAt(0);
        this.mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                AndroidBug5497Workaround.this.possiblyResizeChildOfContent();
            }
        });
    }

    private void possiblyResizeChildOfContent() {
        int usableHeightNow = computeUsableHeight();
        if (usableHeightNow != this.usableHeightPrevious) {
            int usableHeightSansKeyboard = this.mChildOfContent.getRootView().getHeight();
            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
            if (heightDifference > usableHeightSansKeyboard / 4) {
                this.frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
            } else {
                this.frameLayoutParams.height = usableHeightSansKeyboard;
            }
            this.mChildOfContent.requestLayout();
            this.usableHeightPrevious = usableHeightNow;
        }
    }

    private int computeUsableHeight() {
        Rect r = new Rect();
        this.mChildOfContent.getWindowVisibleDisplayFrame(r);
        return r.bottom - r.top;
    }
}
