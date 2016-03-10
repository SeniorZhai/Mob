package com.cocosw.bottomsheet;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import com.google.android.exoplayer.DefaultLoadControl;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;

class ClosableSlidingLayout extends FrameLayout {
    private static final int INVALID_POINTER = -1;
    private final float MINVEL;
    private boolean collapsible;
    private int height;
    private int mActivePointerId;
    private ViewDragHelper mDragHelper;
    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private SlideListener mListener;
    View mTarget;
    boolean swipeable;
    private int top;
    private float yDiff;

    interface SlideListener {
        void onClosed();

        void onOpened();
    }

    private class ViewDragCallback extends Callback {
        private ViewDragCallback() {
        }

        public boolean tryCaptureView(View child, int pointerId) {
            return true;
        }

        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            if (yvel > ClosableSlidingLayout.this.MINVEL) {
                ClosableSlidingLayout.this.dismiss(releasedChild, yvel);
            } else if (releasedChild.getTop() >= ClosableSlidingLayout.this.top + (ClosableSlidingLayout.this.height / 2)) {
                ClosableSlidingLayout.this.dismiss(releasedChild, yvel);
            } else {
                ClosableSlidingLayout.this.mDragHelper.smoothSlideViewTo(releasedChild, 0, ClosableSlidingLayout.this.top);
            }
            ViewCompat.postInvalidateOnAnimation(ClosableSlidingLayout.this);
        }

        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            if (VERSION.SDK_INT < 11) {
                ClosableSlidingLayout.this.invalidate();
            }
            if (ClosableSlidingLayout.this.height - top < 1 && ClosableSlidingLayout.this.mListener != null) {
                ClosableSlidingLayout.this.mListener.onClosed();
            }
        }

        public int clampViewPositionVertical(View child, int top, int dy) {
            return Math.max(top, ClosableSlidingLayout.this.top);
        }
    }

    public ClosableSlidingLayout(Context context) {
        this(context, null);
    }

    public ClosableSlidingLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(11)
    public ClosableSlidingLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.collapsible = false;
        this.swipeable = true;
        this.mDragHelper = ViewDragHelper.create(this, DefaultLoadControl.DEFAULT_HIGH_BUFFER_LOAD, new ViewDragCallback());
        this.MINVEL = getResources().getDisplayMetrics().density * 400.0f;
    }

    public boolean onInterceptTouchEvent(@NonNull MotionEvent event) {
        int action = MotionEventCompat.getActionMasked(event);
        if (!isEnabled() || canChildScrollUp()) {
            return false;
        }
        if (action == 3 || action == 1) {
            this.mActivePointerId = INVALID_POINTER;
            this.mIsBeingDragged = false;
            if (this.collapsible && (-this.yDiff) > ((float) this.mDragHelper.getTouchSlop())) {
                expand(this.mDragHelper.getCapturedView(), 0.0f);
            }
            this.mDragHelper.cancel();
            return false;
        }
        switch (action) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                this.height = getChildAt(0).getHeight();
                this.top = getChildAt(0).getTop();
                this.mActivePointerId = MotionEventCompat.getPointerId(event, 0);
                this.mIsBeingDragged = false;
                float initialMotionY = getMotionEventY(event, this.mActivePointerId);
                if (initialMotionY != -1.0f) {
                    this.mInitialMotionY = initialMotionY;
                    this.yDiff = 0.0f;
                    break;
                }
                return false;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                if (this.mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                float y = getMotionEventY(event, this.mActivePointerId);
                if (y != -1.0f) {
                    this.yDiff = y - this.mInitialMotionY;
                    if (this.swipeable && this.yDiff > ((float) this.mDragHelper.getTouchSlop()) && !this.mIsBeingDragged) {
                        this.mIsBeingDragged = true;
                        this.mDragHelper.captureChildView(getChildAt(0), 0);
                        break;
                    }
                }
                return false;
        }
        this.mDragHelper.shouldInterceptTouchEvent(event);
        return this.mIsBeingDragged;
    }

    public void requestDisallowInterceptTouchEvent(boolean b) {
    }

    private boolean canChildScrollUp() {
        if (VERSION.SDK_INT >= 14) {
            return ViewCompat.canScrollVertically(this.mTarget, INVALID_POINTER);
        }
        if (this.mTarget instanceof AbsListView) {
            AbsListView absListView = this.mTarget;
            if (absListView.getChildCount() <= 0 || (absListView.getFirstVisiblePosition() <= 0 && absListView.getChildAt(0).getTop() >= absListView.getPaddingTop())) {
                return false;
            }
            return true;
        } else if (this.mTarget.getScrollY() <= 0) {
            return false;
        } else {
            return true;
        }
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1.0f;
        }
        return MotionEventCompat.getY(ev, index);
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if (!isEnabled() || canChildScrollUp()) {
            return super.onTouchEvent(ev);
        }
        try {
            if (this.swipeable) {
                this.mDragHelper.processTouchEvent(ev);
            }
        } catch (Exception e) {
        }
        return true;
    }

    public void computeScroll() {
        if (this.mDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void setSlideListener(SlideListener listener) {
        this.mListener = listener;
    }

    void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
    }

    private void expand(View releasedChild, float yvel) {
        if (this.mListener != null) {
            this.mListener.onOpened();
        }
    }

    private void dismiss(View view, float yvel) {
        this.mDragHelper.smoothSlideViewTo(view, 0, this.top + this.height);
        this.mDragHelper.cancel();
        ViewCompat.postInvalidateOnAnimation(this);
    }
}
