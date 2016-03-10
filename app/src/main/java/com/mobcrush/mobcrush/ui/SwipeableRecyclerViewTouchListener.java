package com.mobcrush.mobcrush.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Rect;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnItemTouchListener;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewPropertyAnimator;
import com.android.volley.DefaultRetryPolicy;
import com.mobcrush.mobcrush.Constants;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwipeableRecyclerViewTouchListener implements OnItemTouchListener {
    private View mActionView;
    private int mActionViewID;
    private float mAlpha;
    private int mAnimatingPosition = -1;
    private long mAnimationTime;
    private boolean mApplyAlpha = false;
    private int mDismissAnimationRefCount = 0;
    private int mDownPosition;
    private View mDownView;
    private float mDownX;
    private float mDownY;
    private float mFinalDelta;
    private int mMaxFlingVelocity;
    private int mMinFlingVelocity;
    private boolean mPaused;
    private List<PendingDismissData> mPendingDismisses = new ArrayList();
    private RecyclerView mRecyclerView;
    private View mShiftView;
    private int mSlop;
    private SwipeListener mSwipeListener;
    private boolean mSwiping;
    private int mSwipingSlop;
    private VelocityTracker mVelocityTracker;
    private int mViewID;
    private int mViewWidth = 1;

    public interface SwipeListener {
        boolean canSwipe(int i);

        void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] iArr);

        void onDismissedBySwipeRight(RecyclerView recyclerView, int[] iArr);
    }

    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        public int compareTo(@NonNull PendingDismissData other) {
            return other.position - this.position;
        }
    }

    public SwipeableRecyclerViewTouchListener(RecyclerView recyclerView, SwipeListener listener, int viewID, int actionViewID) {
        ViewConfiguration vc = ViewConfiguration.get(recyclerView.getContext());
        this.mSlop = vc.getScaledTouchSlop();
        this.mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
        this.mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        this.mAnimationTime = (long) recyclerView.getContext().getResources().getInteger(17694720);
        this.mRecyclerView = recyclerView;
        this.mSwipeListener = listener;
        this.mViewID = viewID;
        this.mActionViewID = actionViewID;
        this.mRecyclerView.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                boolean z = true;
                SwipeableRecyclerViewTouchListener swipeableRecyclerViewTouchListener = SwipeableRecyclerViewTouchListener.this;
                if (newState == 1) {
                    z = false;
                }
                swipeableRecyclerViewTouchListener.setEnabled(z);
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
    }

    public void setEnabled(boolean enabled) {
        this.mPaused = !enabled;
    }

    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
        return handleTouchEvent(motionEvent);
    }

    public void onTouchEvent(RecyclerView rv, MotionEvent motionEvent) {
        handleTouchEvent(motionEvent);
    }

    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    private boolean handleTouchEvent(MotionEvent motionEvent) {
        if (this.mViewWidth < 2) {
            this.mViewWidth = this.mRecyclerView.getWidth();
        }
        View v;
        View vA;
        final View view;
        final View view2;
        switch (motionEvent.getActionMasked()) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                if (!this.mPaused) {
                    Rect rect = new Rect();
                    int childCount = this.mRecyclerView.getChildCount();
                    int[] listViewCoords = new int[2];
                    this.mRecyclerView.getLocationOnScreen(listViewCoords);
                    int x = ((int) motionEvent.getRawX()) - listViewCoords[0];
                    int y = ((int) motionEvent.getRawY()) - listViewCoords[1];
                    for (int i = 0; i < childCount; i++) {
                        View child = this.mRecyclerView.getChildAt(i);
                        child.getHitRect(rect);
                        if (rect.contains(x, y)) {
                            this.mDownView = child;
                            this.mShiftView = child.findViewById(this.mViewID);
                            this.mActionView = child.findViewById(this.mActionViewID);
                            if (!(this.mDownView == null || this.mAnimatingPosition == this.mRecyclerView.getChildPosition(this.mDownView))) {
                                this.mAlpha = this.mDownView.getAlpha();
                                this.mDownX = motionEvent.getRawX();
                                this.mDownY = motionEvent.getRawY();
                                this.mDownPosition = this.mRecyclerView.getChildPosition(this.mDownView);
                                if (this.mSwipeListener.canSwipe(this.mDownPosition)) {
                                    this.mVelocityTracker = VelocityTracker.obtain();
                                    this.mVelocityTracker.addMovement(motionEvent);
                                    break;
                                }
                                this.mDownView = null;
                                break;
                            }
                        }
                    }
                    this.mAlpha = this.mDownView.getAlpha();
                    this.mDownX = motionEvent.getRawX();
                    this.mDownY = motionEvent.getRawY();
                    this.mDownPosition = this.mRecyclerView.getChildPosition(this.mDownView);
                    if (this.mSwipeListener.canSwipe(this.mDownPosition)) {
                        this.mVelocityTracker = VelocityTracker.obtain();
                        this.mVelocityTracker.addMovement(motionEvent);
                    } else {
                        this.mDownView = null;
                    }
                    break;
                }
                break;
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                if (this.mVelocityTracker != null) {
                    this.mFinalDelta = motionEvent.getRawX() - this.mDownX;
                    this.mVelocityTracker.addMovement(motionEvent);
                    this.mVelocityTracker.computeCurrentVelocity(Constants.UPDATE_COFIG_INTERVAL);
                    float velocityX = this.mVelocityTracker.getXVelocity();
                    float absVelocityX = Math.abs(velocityX);
                    float absVelocityY = Math.abs(this.mVelocityTracker.getYVelocity());
                    boolean dismiss = false;
                    boolean dismissRight = false;
                    if (Math.abs(this.mFinalDelta) > ((float) (this.mViewWidth / 2)) && this.mSwiping) {
                        dismiss = true;
                        dismissRight = this.mFinalDelta > 0.0f;
                    } else if (((float) this.mMinFlingVelocity) <= absVelocityX && absVelocityX <= ((float) this.mMaxFlingVelocity) && absVelocityY < absVelocityX && this.mSwiping) {
                        dismiss = ((velocityX > 0.0f ? 1 : (velocityX == 0.0f ? 0 : -1)) < 0 ? 1 : null) == ((this.mFinalDelta > 0.0f ? 1 : (this.mFinalDelta == 0.0f ? 0 : -1)) < 0 ? 1 : null);
                        dismissRight = this.mVelocityTracker.getXVelocity() > 0.0f;
                    }
                    v = this.mShiftView != null ? this.mShiftView : this.mDownView;
                    vA = this.mActionView;
                    if (!dismiss || this.mDownPosition == this.mAnimatingPosition || this.mDownPosition == -1) {
                        view = vA;
                        view2 = v;
                        v.animate().translationX(0.0f).alpha(this.mAlpha).setDuration(this.mAnimationTime).setListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                view.setAlpha(0.0f);
                                view2.setBackgroundResource(17170445);
                            }
                        });
                    } else {
                        float f;
                        final int downPosition = this.mDownPosition;
                        this.mDismissAnimationRefCount++;
                        this.mAnimatingPosition = this.mDownPosition;
                        ViewPropertyAnimator animate = v.animate();
                        if (dismissRight) {
                            f = (float) this.mViewWidth;
                        } else {
                            f = (float) (-this.mViewWidth);
                        }
                        view = v;
                        animate.translationX(f).alpha(0.0f).setDuration(this.mAnimationTime).setListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animation) {
                                SwipeableRecyclerViewTouchListener.this.performDismiss(view, downPosition);
                            }
                        });
                        this.mDownView.animate().alpha(0.0f).setDuration(this.mAnimationTime);
                    }
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    this.mDownX = 0.0f;
                    this.mDownY = 0.0f;
                    this.mDownView = null;
                    this.mShiftView = null;
                    this.mDownPosition = -1;
                    this.mSwiping = false;
                    break;
                }
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                if (!(this.mVelocityTracker == null || this.mPaused)) {
                    this.mVelocityTracker.addMovement(motionEvent);
                    float deltaX = motionEvent.getRawX() - this.mDownX;
                    float deltaY = motionEvent.getRawY() - this.mDownY;
                    if (!this.mSwiping && Math.abs(deltaX) > ((float) this.mSlop) && Math.abs(deltaY) < Math.abs(deltaX) / 2.0f) {
                        this.mSwiping = true;
                        this.mSwipingSlop = deltaX > 0.0f ? this.mSlop : -this.mSlop;
                    }
                    if (this.mShiftView != null) {
                        v = this.mShiftView;
                    } else {
                        v = this.mDownView;
                    }
                    if (this.mSwiping) {
                        v.setTranslationX(deltaX - ((float) this.mSwipingSlop));
                        if (this.mApplyAlpha) {
                            v.setAlpha(Math.max(0.0f, Math.min(this.mAlpha, this.mAlpha * (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - (Math.abs(deltaX) / ((float) this.mViewWidth))))));
                        }
                        if (this.mActionView != null) {
                            float p = Math.abs(deltaX) / ((float) this.mViewWidth);
                            if (((double) p) > 0.001d) {
                                this.mShiftView.setBackgroundResource(17170444);
                            }
                            if (((double) p) <= 0.05d || ((double) p) >= 0.7d) {
                                this.mActionView.setAlpha(0.0f);
                            } else {
                                this.mActionView.setAlpha(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                            }
                        }
                        return true;
                    }
                }
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                if (this.mVelocityTracker != null) {
                    if (this.mShiftView != null) {
                        v = this.mShiftView;
                    } else {
                        v = this.mDownView;
                    }
                    vA = this.mActionView;
                    if (v != null && this.mSwiping) {
                        view = vA;
                        view2 = v;
                        v.animate().translationX(0.0f).alpha(this.mAlpha).setDuration(this.mAnimationTime).setListener(new AnimatorListenerAdapter() {
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                view.setAlpha(0.0f);
                                view2.setBackgroundResource(17170445);
                            }
                        });
                    }
                    this.mVelocityTracker.recycle();
                    this.mVelocityTracker = null;
                    this.mDownX = 0.0f;
                    this.mDownY = 0.0f;
                    this.mDownView = null;
                    this.mShiftView = null;
                    this.mDownPosition = -1;
                    this.mSwiping = false;
                    break;
                }
                break;
        }
        return false;
    }

    private void performDismiss(View dismissView, int dismissPosition) {
        this.mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        this.mDismissAnimationRefCount--;
        if (this.mDismissAnimationRefCount == 0) {
            Collections.sort(this.mPendingDismisses);
            int[] dismissPositions = new int[this.mPendingDismisses.size()];
            for (int i = this.mPendingDismisses.size() - 1; i >= 0; i--) {
                dismissPositions[i] = ((PendingDismissData) this.mPendingDismisses.get(i)).position;
            }
            if (this.mFinalDelta > 0.0f) {
                this.mSwipeListener.onDismissedBySwipeRight(this.mRecyclerView, dismissPositions);
            } else {
                this.mSwipeListener.onDismissedBySwipeLeft(this.mRecyclerView, dismissPositions);
            }
            this.mDownPosition = -1;
            for (PendingDismissData pendingDismiss : this.mPendingDismisses) {
                pendingDismiss.view.setAlpha(this.mAlpha);
                pendingDismiss.view.setTranslationX(0.0f);
            }
            long time = SystemClock.uptimeMillis();
            this.mRecyclerView.dispatchTouchEvent(MotionEvent.obtain(time, time, 3, 0.0f, 0.0f, 0));
            this.mPendingDismisses.clear();
            this.mAnimatingPosition = -1;
        }
    }
}
