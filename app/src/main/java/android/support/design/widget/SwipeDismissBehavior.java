package android.support.design.widget;

import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.DefaultRetryPolicy;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class SwipeDismissBehavior<V extends View> extends Behavior<V> {
    private static final float DEFAULT_ALPHA_END_DISTANCE = 0.5f;
    private static final float DEFAULT_ALPHA_START_DISTANCE = 0.0f;
    private static final float DEFAULT_DRAG_DISMISS_THRESHOLD = 0.5f;
    public static final int STATE_DRAGGING = 1;
    public static final int STATE_IDLE = 0;
    public static final int STATE_SETTLING = 2;
    public static final int SWIPE_DIRECTION_ANY = 2;
    public static final int SWIPE_DIRECTION_END_TO_START = 1;
    public static final int SWIPE_DIRECTION_START_TO_END = 0;
    private float mAlphaEndSwipeDistance = DEFAULT_DRAG_DISMISS_THRESHOLD;
    private float mAlphaStartSwipeDistance = DEFAULT_ALPHA_START_DISTANCE;
    private final Callback mDragCallback = new Callback() {
        private int mOriginalCapturedViewLeft;

        public boolean tryCaptureView(View child, int pointerId) {
            this.mOriginalCapturedViewLeft = child.getLeft();
            return true;
        }

        public void onViewDragStateChanged(int state) {
            if (SwipeDismissBehavior.this.mListener != null) {
                SwipeDismissBehavior.this.mListener.onDragStateChanged(state);
            }
        }

        public void onViewReleased(View child, float xvel, float yvel) {
            int targetLeft;
            int childWidth = child.getWidth();
            boolean dismiss = false;
            if (shouldDismiss(child, xvel)) {
                targetLeft = child.getLeft() < this.mOriginalCapturedViewLeft ? this.mOriginalCapturedViewLeft - childWidth : this.mOriginalCapturedViewLeft + childWidth;
                dismiss = true;
            } else {
                targetLeft = this.mOriginalCapturedViewLeft;
            }
            if (SwipeDismissBehavior.this.mViewDragHelper.settleCapturedViewAt(targetLeft, child.getTop())) {
                ViewCompat.postOnAnimation(child, new SettleRunnable(child, dismiss));
            } else if (dismiss && SwipeDismissBehavior.this.mListener != null) {
                SwipeDismissBehavior.this.mListener.onDismiss(child);
            }
        }

        private boolean shouldDismiss(View child, float xvel) {
            if (xvel != SwipeDismissBehavior.DEFAULT_ALPHA_START_DISTANCE) {
                boolean isRtl = ViewCompat.getLayoutDirection(child) == SwipeDismissBehavior.SWIPE_DIRECTION_END_TO_START;
                if (SwipeDismissBehavior.this.mSwipeDirection == SwipeDismissBehavior.SWIPE_DIRECTION_ANY) {
                    return true;
                }
                if (SwipeDismissBehavior.this.mSwipeDirection == 0) {
                    if (isRtl) {
                        if (xvel >= SwipeDismissBehavior.DEFAULT_ALPHA_START_DISTANCE) {
                            return false;
                        }
                        return true;
                    } else if (xvel <= SwipeDismissBehavior.DEFAULT_ALPHA_START_DISTANCE) {
                        return false;
                    } else {
                        return true;
                    }
                } else if (SwipeDismissBehavior.this.mSwipeDirection != SwipeDismissBehavior.SWIPE_DIRECTION_END_TO_START) {
                    return false;
                } else {
                    if (isRtl) {
                        if (xvel <= SwipeDismissBehavior.DEFAULT_ALPHA_START_DISTANCE) {
                            return false;
                        }
                        return true;
                    } else if (xvel >= SwipeDismissBehavior.DEFAULT_ALPHA_START_DISTANCE) {
                        return false;
                    } else {
                        return true;
                    }
                }
            }
            if (Math.abs(child.getLeft() - this.mOriginalCapturedViewLeft) < Math.round(((float) child.getWidth()) * SwipeDismissBehavior.this.mDragDismissThreshold)) {
                return false;
            }
            return true;
        }

        public int getViewHorizontalDragRange(View child) {
            return child.getWidth();
        }

        public int clampViewPositionHorizontal(View child, int left, int dx) {
            int min;
            int max;
            boolean isRtl = ViewCompat.getLayoutDirection(child) == SwipeDismissBehavior.SWIPE_DIRECTION_END_TO_START;
            if (SwipeDismissBehavior.this.mSwipeDirection == 0) {
                if (isRtl) {
                    min = this.mOriginalCapturedViewLeft - child.getWidth();
                    max = this.mOriginalCapturedViewLeft;
                } else {
                    min = this.mOriginalCapturedViewLeft;
                    max = this.mOriginalCapturedViewLeft + child.getWidth();
                }
            } else if (SwipeDismissBehavior.this.mSwipeDirection != SwipeDismissBehavior.SWIPE_DIRECTION_END_TO_START) {
                min = this.mOriginalCapturedViewLeft - child.getWidth();
                max = this.mOriginalCapturedViewLeft + child.getWidth();
            } else if (isRtl) {
                min = this.mOriginalCapturedViewLeft;
                max = this.mOriginalCapturedViewLeft + child.getWidth();
            } else {
                min = this.mOriginalCapturedViewLeft - child.getWidth();
                max = this.mOriginalCapturedViewLeft;
            }
            return SwipeDismissBehavior.clamp(min, left, max);
        }

        public int clampViewPositionVertical(View child, int top, int dy) {
            return child.getTop();
        }

        public void onViewPositionChanged(View child, int left, int top, int dx, int dy) {
            float startAlphaDistance = ((float) this.mOriginalCapturedViewLeft) + (((float) child.getWidth()) * SwipeDismissBehavior.this.mAlphaStartSwipeDistance);
            float endAlphaDistance = ((float) this.mOriginalCapturedViewLeft) + (((float) child.getWidth()) * SwipeDismissBehavior.this.mAlphaEndSwipeDistance);
            if (((float) left) <= startAlphaDistance) {
                ViewCompat.setAlpha(child, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            } else if (((float) left) >= endAlphaDistance) {
                ViewCompat.setAlpha(child, SwipeDismissBehavior.DEFAULT_ALPHA_START_DISTANCE);
            } else {
                ViewCompat.setAlpha(child, SwipeDismissBehavior.clamp((float) SwipeDismissBehavior.DEFAULT_ALPHA_START_DISTANCE, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - SwipeDismissBehavior.fraction(startAlphaDistance, endAlphaDistance, (float) left), (float) DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            }
        }
    };
    private float mDragDismissThreshold = DEFAULT_DRAG_DISMISS_THRESHOLD;
    private boolean mIgnoreEvents;
    private OnDismissListener mListener;
    private float mSensitivity = DEFAULT_ALPHA_START_DISTANCE;
    private boolean mSensitivitySet;
    private int mSwipeDirection = SWIPE_DIRECTION_ANY;
    private ViewDragHelper mViewDragHelper;

    public interface OnDismissListener {
        void onDismiss(View view);

        void onDragStateChanged(int i);
    }

    private class SettleRunnable implements Runnable {
        private final boolean mDismiss;
        private final View mView;

        SettleRunnable(View view, boolean dismiss) {
            this.mView = view;
            this.mDismiss = dismiss;
        }

        public void run() {
            if (SwipeDismissBehavior.this.mViewDragHelper != null && SwipeDismissBehavior.this.mViewDragHelper.continueSettling(true)) {
                ViewCompat.postOnAnimation(this.mView, this);
            } else if (this.mDismiss && SwipeDismissBehavior.this.mListener != null) {
                SwipeDismissBehavior.this.mListener.onDismiss(this.mView);
            }
        }
    }

    public void setListener(OnDismissListener listener) {
        this.mListener = listener;
    }

    public void setSwipeDirection(int direction) {
        this.mSwipeDirection = direction;
    }

    public void setDragDismissDistance(float distance) {
        this.mDragDismissThreshold = clamp((float) DEFAULT_ALPHA_START_DISTANCE, distance, (float) DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public void setStartAlphaSwipeDistance(float fraction) {
        this.mAlphaStartSwipeDistance = clamp((float) DEFAULT_ALPHA_START_DISTANCE, fraction, (float) DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public void setEndAlphaSwipeDistance(float fraction) {
        this.mAlphaEndSwipeDistance = clamp((float) DEFAULT_ALPHA_START_DISTANCE, fraction, (float) DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public void setSensitivity(float sensitivity) {
        this.mSensitivity = sensitivity;
        this.mSensitivitySet = true;
    }

    public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case SWIPE_DIRECTION_END_TO_START /*1*/:
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                if (this.mIgnoreEvents) {
                    this.mIgnoreEvents = false;
                    return false;
                }
                break;
            default:
                boolean z;
                if (parent.isPointInChildBounds(child, (int) event.getX(), (int) event.getY())) {
                    z = false;
                } else {
                    z = true;
                }
                this.mIgnoreEvents = z;
                break;
        }
        if (this.mIgnoreEvents) {
            return false;
        }
        ensureViewDragHelper(parent);
        return this.mViewDragHelper.shouldInterceptTouchEvent(event);
    }

    public boolean onTouchEvent(CoordinatorLayout parent, V v, MotionEvent event) {
        if (this.mViewDragHelper == null) {
            return false;
        }
        this.mViewDragHelper.processTouchEvent(event);
        return true;
    }

    private void ensureViewDragHelper(ViewGroup parent) {
        if (this.mViewDragHelper == null) {
            this.mViewDragHelper = this.mSensitivitySet ? ViewDragHelper.create(parent, this.mSensitivity, this.mDragCallback) : ViewDragHelper.create(parent, this.mDragCallback);
        }
    }

    private static float clamp(float min, float value, float max) {
        return Math.min(Math.max(min, value), max);
    }

    private static int clamp(int min, int value, int max) {
        return Math.min(Math.max(min, value), max);
    }

    public int getDragState() {
        return this.mViewDragHelper != null ? this.mViewDragHelper.getViewDragState() : STATE_IDLE;
    }

    static float fraction(float startValue, float endValue, float value) {
        return (value - startValue) / (endValue - startValue);
    }
}
