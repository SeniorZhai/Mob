package android.support.design.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.design.R;
import android.support.design.widget.CoordinatorLayout.DefaultBehavior;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.OnApplyWindowInsetsListener;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.BaseSavedState;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.ResponseParser;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@DefaultBehavior(Behavior.class)
public class AppBarLayout extends LinearLayout {
    private static final int INVALID_SCROLL_RANGE = -1;
    private static final int PENDING_ACTION_ANIMATE_ENABLED = 4;
    private static final int PENDING_ACTION_COLLAPSED = 2;
    private static final int PENDING_ACTION_EXPANDED = 1;
    private static final int PENDING_ACTION_NONE = 0;
    private int mDownPreScrollRange;
    private int mDownScrollRange;
    boolean mHaveChildWithInterpolator;
    private WindowInsetsCompat mLastInsets;
    private final List<OnOffsetChangedListener> mListeners;
    private int mPendingAction;
    private float mTargetElevation;
    private int mTotalScrollRange;

    public static class Behavior extends ViewOffsetBehavior<AppBarLayout> {
        private static final int INVALID_POINTER = -1;
        private static final int INVALID_POSITION = -1;
        private int mActivePointerId = INVALID_POSITION;
        private ValueAnimatorCompat mAnimator;
        private Runnable mFlingRunnable;
        private boolean mIsBeingDragged;
        private int mLastMotionY;
        private WeakReference<View> mLastNestedScrollingChildRef;
        private int mOffsetDelta;
        private int mOffsetToChildIndexOnLayout = INVALID_POSITION;
        private boolean mOffsetToChildIndexOnLayoutIsMinHeight;
        private float mOffsetToChildIndexOnLayoutPerc;
        private ScrollerCompat mScroller;
        private boolean mSkipNestedPreScroll;
        private int mTouchSlop = INVALID_POSITION;

        private class FlingRunnable implements Runnable {
            private final AppBarLayout mLayout;
            private final CoordinatorLayout mParent;

            FlingRunnable(CoordinatorLayout parent, AppBarLayout layout) {
                this.mParent = parent;
                this.mLayout = layout;
            }

            public void run() {
                if (this.mLayout != null && Behavior.this.mScroller != null && Behavior.this.mScroller.computeScrollOffset()) {
                    Behavior.this.setAppBarTopBottomOffset(this.mParent, this.mLayout, Behavior.this.mScroller.getCurrY());
                    ViewCompat.postOnAnimation(this.mLayout, this);
                }
            }
        }

        protected static class SavedState extends BaseSavedState {
            public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
                public SavedState createFromParcel(Parcel source) {
                    return new SavedState(source);
                }

                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            };
            boolean firstVisibileChildAtMinimumHeight;
            float firstVisibileChildPercentageShown;
            int firstVisibleChildIndex;

            public SavedState(Parcel source) {
                super(source);
                this.firstVisibleChildIndex = source.readInt();
                this.firstVisibileChildPercentageShown = source.readFloat();
                this.firstVisibileChildAtMinimumHeight = source.readByte() != (byte) 0;
            }

            public SavedState(Parcelable superState) {
                super(superState);
            }

            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(this.firstVisibleChildIndex);
                dest.writeFloat(this.firstVisibileChildPercentageShown);
                dest.writeByte((byte) (this.firstVisibileChildAtMinimumHeight ? AppBarLayout.PENDING_ACTION_EXPANDED : 0));
            }
        }

        public /* bridge */ /* synthetic */ int getLeftAndRightOffset() {
            return super.getLeftAndRightOffset();
        }

        public /* bridge */ /* synthetic */ int getTopAndBottomOffset() {
            return super.getTopAndBottomOffset();
        }

        public /* bridge */ /* synthetic */ boolean setLeftAndRightOffset(int x0) {
            return super.setLeftAndRightOffset(x0);
        }

        public /* bridge */ /* synthetic */ boolean setTopAndBottomOffset(int x0) {
            return super.setTopAndBottomOffset(x0);
        }

        public Behavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public boolean onStartNestedScroll(CoordinatorLayout parent, AppBarLayout child, View directTargetChild, View target, int nestedScrollAxes) {
            boolean started = (nestedScrollAxes & AppBarLayout.PENDING_ACTION_COLLAPSED) != 0 && child.hasScrollableChildren() && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();
            if (started && this.mAnimator != null) {
                this.mAnimator.cancel();
            }
            this.mLastNestedScrollingChildRef = null;
            return started;
        }

        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed) {
            if (dy != 0 && !this.mSkipNestedPreScroll) {
                int min;
                int max;
                if (dy < 0) {
                    min = -child.getTotalScrollRange();
                    max = min + child.getDownNestedPreScrollRange();
                } else {
                    min = -child.getUpNestedPreScrollRange();
                    max = 0;
                }
                consumed[AppBarLayout.PENDING_ACTION_EXPANDED] = scroll(coordinatorLayout, child, dy, min, max);
            }
        }

        public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
            if (dyUnconsumed < 0) {
                scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0);
                this.mSkipNestedPreScroll = true;
                return;
            }
            this.mSkipNestedPreScroll = false;
        }

        public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target) {
            this.mSkipNestedPreScroll = false;
            this.mLastNestedScrollingChildRef = new WeakReference(target);
        }

        public boolean onInterceptTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
            if (this.mTouchSlop < 0) {
                this.mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
            }
            if (ev.getAction() == AppBarLayout.PENDING_ACTION_COLLAPSED && this.mIsBeingDragged) {
                return true;
            }
            int y;
            switch (MotionEventCompat.getActionMasked(ev)) {
                case ResponseParser.ResponseActionDiscard /*0*/:
                    this.mIsBeingDragged = false;
                    y = (int) ev.getY();
                    if (parent.isPointInChildBounds(child, (int) ev.getX(), y) && canDragAppBarLayout()) {
                        this.mLastMotionY = y;
                        this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                        break;
                    }
                case AppBarLayout.PENDING_ACTION_EXPANDED /*1*/:
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    this.mIsBeingDragged = false;
                    this.mActivePointerId = INVALID_POSITION;
                    break;
                case AppBarLayout.PENDING_ACTION_COLLAPSED /*2*/:
                    int activePointerId = this.mActivePointerId;
                    if (activePointerId != INVALID_POSITION) {
                        int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                        if (pointerIndex != INVALID_POSITION) {
                            y = (int) MotionEventCompat.getY(ev, pointerIndex);
                            if (Math.abs(y - this.mLastMotionY) > this.mTouchSlop) {
                                this.mIsBeingDragged = true;
                                this.mLastMotionY = y;
                                break;
                            }
                        }
                    }
                    break;
            }
            return this.mIsBeingDragged;
        }

        public boolean onTouchEvent(CoordinatorLayout parent, AppBarLayout child, MotionEvent ev) {
            if (this.mTouchSlop < 0) {
                this.mTouchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
            }
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            switch (MotionEventCompat.getActionMasked(ev)) {
                case ResponseParser.ResponseActionDiscard /*0*/:
                    if (parent.isPointInChildBounds(child, x, y) && canDragAppBarLayout()) {
                        this.mLastMotionY = y;
                        this.mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                        break;
                    }
                    return false;
                case AppBarLayout.PENDING_ACTION_EXPANDED /*1*/:
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    this.mIsBeingDragged = false;
                    this.mActivePointerId = INVALID_POSITION;
                    break;
                case AppBarLayout.PENDING_ACTION_COLLAPSED /*2*/:
                    int activePointerIndex = MotionEventCompat.findPointerIndex(ev, this.mActivePointerId);
                    if (activePointerIndex != INVALID_POSITION) {
                        y = (int) MotionEventCompat.getY(ev, activePointerIndex);
                        int dy = this.mLastMotionY - y;
                        if (!this.mIsBeingDragged && Math.abs(dy) > this.mTouchSlop) {
                            this.mIsBeingDragged = true;
                            dy = dy > 0 ? dy - this.mTouchSlop : dy + this.mTouchSlop;
                        }
                        if (this.mIsBeingDragged) {
                            this.mLastMotionY = y;
                            scroll(parent, child, dy, -child.getDownNestedScrollRange(), 0);
                            break;
                        }
                    }
                    return false;
                    break;
            }
            return true;
        }

        public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
            if (consumed) {
                int targetScroll;
                if (velocityY < 0.0f) {
                    targetScroll = (-child.getTotalScrollRange()) + child.getDownNestedPreScrollRange();
                    if (getTopBottomOffsetForScrollingSibling() > targetScroll) {
                        return false;
                    }
                }
                targetScroll = -child.getUpNestedPreScrollRange();
                if (getTopBottomOffsetForScrollingSibling() < targetScroll) {
                    return false;
                }
                if (getTopBottomOffsetForScrollingSibling() == targetScroll) {
                    return false;
                }
                animateOffsetTo(coordinatorLayout, child, targetScroll);
                return true;
            }
            return fling(coordinatorLayout, child, -child.getTotalScrollRange(), 0, -velocityY);
        }

        private void animateOffsetTo(final CoordinatorLayout coordinatorLayout, final AppBarLayout child, int offset) {
            if (this.mAnimator == null) {
                this.mAnimator = ViewUtils.createAnimator();
                this.mAnimator.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
                this.mAnimator.setUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimatorCompat animator) {
                        Behavior.this.setAppBarTopBottomOffset(coordinatorLayout, child, animator.getAnimatedIntValue());
                    }
                });
            } else {
                this.mAnimator.cancel();
            }
            this.mAnimator.setIntValues(getTopBottomOffsetForScrollingSibling(), offset);
            this.mAnimator.start();
        }

        private boolean fling(CoordinatorLayout coordinatorLayout, AppBarLayout layout, int minOffset, int maxOffset, float velocityY) {
            if (this.mFlingRunnable != null) {
                layout.removeCallbacks(this.mFlingRunnable);
            }
            if (this.mScroller == null) {
                this.mScroller = ScrollerCompat.create(layout.getContext());
            }
            this.mScroller.fling(0, getTopBottomOffsetForScrollingSibling(), 0, Math.round(velocityY), 0, 0, minOffset, maxOffset);
            if (this.mScroller.computeScrollOffset()) {
                this.mFlingRunnable = new FlingRunnable(coordinatorLayout, layout);
                ViewCompat.postOnAnimation(layout, this.mFlingRunnable);
                return true;
            }
            this.mFlingRunnable = null;
            return false;
        }

        public boolean onLayoutChild(CoordinatorLayout parent, AppBarLayout abl, int layoutDirection) {
            boolean handled = super.onLayoutChild(parent, abl, layoutDirection);
            int pendingAction = abl.getPendingAction();
            int offset;
            if (pendingAction != 0) {
                boolean animate = (pendingAction & AppBarLayout.PENDING_ACTION_ANIMATE_ENABLED) != 0;
                if ((pendingAction & AppBarLayout.PENDING_ACTION_COLLAPSED) != 0) {
                    offset = -abl.getUpNestedPreScrollRange();
                    if (animate) {
                        animateOffsetTo(parent, abl, offset);
                    } else {
                        setAppBarTopBottomOffset(parent, abl, offset);
                    }
                } else if ((pendingAction & AppBarLayout.PENDING_ACTION_EXPANDED) != 0) {
                    if (animate) {
                        animateOffsetTo(parent, abl, 0);
                    } else {
                        setAppBarTopBottomOffset(parent, abl, 0);
                    }
                }
                abl.resetPendingAction();
            } else if (this.mOffsetToChildIndexOnLayout >= 0) {
                View child = abl.getChildAt(this.mOffsetToChildIndexOnLayout);
                offset = -child.getBottom();
                if (this.mOffsetToChildIndexOnLayoutIsMinHeight) {
                    offset += ViewCompat.getMinimumHeight(child);
                } else {
                    offset += Math.round(((float) child.getHeight()) * this.mOffsetToChildIndexOnLayoutPerc);
                }
                setTopAndBottomOffset(offset);
                this.mOffsetToChildIndexOnLayout = INVALID_POSITION;
            }
            dispatchOffsetUpdates(abl);
            return handled;
        }

        private int scroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, int dy, int minOffset, int maxOffset) {
            return setAppBarTopBottomOffset(coordinatorLayout, appBarLayout, getTopBottomOffsetForScrollingSibling() - dy, minOffset, maxOffset);
        }

        private boolean canDragAppBarLayout() {
            if (this.mLastNestedScrollingChildRef == null) {
                return false;
            }
            View view = (View) this.mLastNestedScrollingChildRef.get();
            if (view == null || !view.isShown() || ViewCompat.canScrollVertically(view, INVALID_POSITION)) {
                return false;
            }
            return true;
        }

        final int setAppBarTopBottomOffset(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, int newOffset) {
            return setAppBarTopBottomOffset(coordinatorLayout, appBarLayout, newOffset, Action.UNDEFINED_DURATION, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
        }

        final int setAppBarTopBottomOffset(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, int newOffset, int minOffset, int maxOffset) {
            int curOffset = getTopBottomOffsetForScrollingSibling();
            int consumed = 0;
            if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
                newOffset = MathUtils.constrain(newOffset, minOffset, maxOffset);
                if (curOffset != newOffset) {
                    int interpolatedOffset = appBarLayout.hasChildWithInterpolator() ? interpolateOffset(appBarLayout, newOffset) : newOffset;
                    boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);
                    consumed = curOffset - newOffset;
                    this.mOffsetDelta = newOffset - interpolatedOffset;
                    if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
                        coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
                    }
                    dispatchOffsetUpdates(appBarLayout);
                }
            }
            return consumed;
        }

        private void dispatchOffsetUpdates(AppBarLayout layout) {
            List<OnOffsetChangedListener> listeners = layout.mListeners;
            int z = listeners.size();
            for (int i = 0; i < z; i += AppBarLayout.PENDING_ACTION_EXPANDED) {
                OnOffsetChangedListener listener = (OnOffsetChangedListener) listeners.get(i);
                if (listener != null) {
                    listener.onOffsetChanged(layout, getTopAndBottomOffset());
                }
            }
        }

        private int interpolateOffset(AppBarLayout layout, int offset) {
            int absOffset = Math.abs(offset);
            int i = 0;
            int z = layout.getChildCount();
            while (i < z) {
                View child = layout.getChildAt(i);
                LayoutParams childLp = (LayoutParams) child.getLayoutParams();
                Interpolator interpolator = childLp.getScrollInterpolator();
                if (absOffset < child.getTop() || absOffset > child.getBottom()) {
                    i += AppBarLayout.PENDING_ACTION_EXPANDED;
                } else if (interpolator == null) {
                    return offset;
                } else {
                    int childScrollableHeight = 0;
                    int flags = childLp.getScrollFlags();
                    if ((flags & AppBarLayout.PENDING_ACTION_EXPANDED) != 0) {
                        childScrollableHeight = 0 + ((child.getHeight() + childLp.topMargin) + childLp.bottomMargin);
                        if ((flags & AppBarLayout.PENDING_ACTION_COLLAPSED) != 0) {
                            childScrollableHeight -= ViewCompat.getMinimumHeight(child);
                        }
                    }
                    if (childScrollableHeight <= 0) {
                        return offset;
                    }
                    return Integer.signum(offset) * (child.getTop() + Math.round(((float) childScrollableHeight) * interpolator.getInterpolation(((float) (absOffset - child.getTop())) / ((float) childScrollableHeight))));
                }
            }
            return offset;
        }

        final int getTopBottomOffsetForScrollingSibling() {
            return getTopAndBottomOffset() + this.mOffsetDelta;
        }

        public Parcelable onSaveInstanceState(CoordinatorLayout parent, AppBarLayout appBarLayout) {
            Parcelable superState = super.onSaveInstanceState(parent, appBarLayout);
            int offset = getTopAndBottomOffset();
            int i = 0;
            int count = appBarLayout.getChildCount();
            while (i < count) {
                View child = appBarLayout.getChildAt(i);
                int visBottom = child.getBottom() + offset;
                if (child.getTop() + offset > 0 || visBottom < 0) {
                    i += AppBarLayout.PENDING_ACTION_EXPANDED;
                } else {
                    SavedState ss = new SavedState(superState);
                    ss.firstVisibleChildIndex = i;
                    ss.firstVisibileChildAtMinimumHeight = visBottom == ViewCompat.getMinimumHeight(child);
                    ss.firstVisibileChildPercentageShown = ((float) visBottom) / ((float) child.getHeight());
                    return ss;
                }
            }
            return superState;
        }

        public void onRestoreInstanceState(CoordinatorLayout parent, AppBarLayout appBarLayout, Parcelable state) {
            if (state instanceof SavedState) {
                SavedState ss = (SavedState) state;
                super.onRestoreInstanceState(parent, appBarLayout, ss.getSuperState());
                this.mOffsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
                this.mOffsetToChildIndexOnLayoutPerc = ss.firstVisibileChildPercentageShown;
                this.mOffsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibileChildAtMinimumHeight;
                return;
            }
            super.onRestoreInstanceState(parent, appBarLayout, state);
            this.mOffsetToChildIndexOnLayout = INVALID_POSITION;
        }
    }

    public static class LayoutParams extends android.widget.LinearLayout.LayoutParams {
        static final int FLAG_QUICK_RETURN = 5;
        public static final int SCROLL_FLAG_ENTER_ALWAYS = 4;
        public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 8;
        public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 2;
        public static final int SCROLL_FLAG_SCROLL = 1;
        int mScrollFlags = SCROLL_FLAG_SCROLL;
        Interpolator mScrollInterpolator;

        @Retention(RetentionPolicy.SOURCE)
        public @interface ScrollFlags {
        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.AppBarLayout_LayoutParams);
            this.mScrollFlags = a.getInt(R.styleable.AppBarLayout_LayoutParams_layout_scrollFlags, 0);
            if (a.hasValue(R.styleable.AppBarLayout_LayoutParams_layout_scrollInterpolator)) {
                this.mScrollInterpolator = AnimationUtils.loadInterpolator(c, a.getResourceId(R.styleable.AppBarLayout_LayoutParams_layout_scrollInterpolator, 0));
            }
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, float weight) {
            super(width, height, weight);
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(android.widget.LinearLayout.LayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.mScrollFlags = source.mScrollFlags;
            this.mScrollInterpolator = source.mScrollInterpolator;
        }

        public void setScrollFlags(int flags) {
            this.mScrollFlags = flags;
        }

        public int getScrollFlags() {
            return this.mScrollFlags;
        }

        public void setScrollInterpolator(Interpolator interpolator) {
            this.mScrollInterpolator = interpolator;
        }

        public Interpolator getScrollInterpolator() {
            return this.mScrollInterpolator;
        }
    }

    public interface OnOffsetChangedListener {
        void onOffsetChanged(AppBarLayout appBarLayout, int i);
    }

    public static class ScrollingViewBehavior extends ViewOffsetBehavior<View> {
        private int mOverlayTop;

        public /* bridge */ /* synthetic */ int getLeftAndRightOffset() {
            return super.getLeftAndRightOffset();
        }

        public /* bridge */ /* synthetic */ int getTopAndBottomOffset() {
            return super.getTopAndBottomOffset();
        }

        public /* bridge */ /* synthetic */ boolean onLayoutChild(CoordinatorLayout x0, View x1, int x2) {
            return super.onLayoutChild(x0, x1, x2);
        }

        public /* bridge */ /* synthetic */ boolean setLeftAndRightOffset(int x0) {
            return super.setLeftAndRightOffset(x0);
        }

        public /* bridge */ /* synthetic */ boolean setTopAndBottomOffset(int x0) {
            return super.setTopAndBottomOffset(x0);
        }

        public ScrollingViewBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ScrollingViewBehavior_Params);
            this.mOverlayTop = a.getDimensionPixelSize(R.styleable.ScrollingViewBehavior_Params_behavior_overlapTop, 0);
            a.recycle();
        }

        public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
            return dependency instanceof AppBarLayout;
        }

        public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
            int childLpHeight = child.getLayoutParams().height;
            if (childLpHeight == AppBarLayout.INVALID_SCROLL_RANGE || childLpHeight == -2) {
                List<View> dependencies = parent.getDependencies(child);
                if (dependencies.isEmpty()) {
                    return false;
                }
                AppBarLayout appBar = findFirstAppBarLayout(dependencies);
                if (appBar != null && ViewCompat.isLaidOut(appBar)) {
                    if (ViewCompat.getFitsSystemWindows(appBar)) {
                        ViewCompat.setFitsSystemWindows(child, true);
                    }
                    int availableHeight = MeasureSpec.getSize(parentHeightMeasureSpec);
                    if (availableHeight == 0) {
                        availableHeight = parent.getHeight();
                    }
                    parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, MeasureSpec.makeMeasureSpec((availableHeight - appBar.getMeasuredHeight()) + appBar.getTotalScrollRange(), childLpHeight == AppBarLayout.INVALID_SCROLL_RANGE ? 1073741824 : Action.UNDEFINED_DURATION), heightUsed);
                    return true;
                }
            }
            return false;
        }

        public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
            android.support.design.widget.CoordinatorLayout.Behavior behavior = ((android.support.design.widget.CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
            if (behavior instanceof Behavior) {
                int appBarOffset = ((Behavior) behavior).getTopBottomOffsetForScrollingSibling();
                int expandedMax = dependency.getHeight() - this.mOverlayTop;
                int collapsedMin = parent.getHeight() - child.getHeight();
                if (this.mOverlayTop == 0 || !(dependency instanceof AppBarLayout)) {
                    setTopAndBottomOffset((dependency.getHeight() - this.mOverlayTop) + appBarOffset);
                } else {
                    setTopAndBottomOffset(AnimationUtils.lerp(expandedMax, collapsedMin, ((float) Math.abs(appBarOffset)) / ((float) ((AppBarLayout) dependency).getTotalScrollRange())));
                }
            }
            return false;
        }

        public void setOverlayTop(int overlayTop) {
            this.mOverlayTop = overlayTop;
        }

        public int getOverlayTop() {
            return this.mOverlayTop;
        }

        private static AppBarLayout findFirstAppBarLayout(List<View> views) {
            int z = views.size();
            for (int i = 0; i < z; i += AppBarLayout.PENDING_ACTION_EXPANDED) {
                View view = (View) views.get(i);
                if (view instanceof AppBarLayout) {
                    return (AppBarLayout) view;
                }
            }
            return null;
        }
    }

    public AppBarLayout(Context context) {
        this(context, null);
    }

    public AppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTotalScrollRange = INVALID_SCROLL_RANGE;
        this.mDownPreScrollRange = INVALID_SCROLL_RANGE;
        this.mDownScrollRange = INVALID_SCROLL_RANGE;
        this.mPendingAction = 0;
        setOrientation(PENDING_ACTION_EXPANDED);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AppBarLayout, 0, R.style.Widget_Design_AppBarLayout);
        this.mTargetElevation = (float) a.getDimensionPixelSize(R.styleable.AppBarLayout_elevation, 0);
        setBackgroundDrawable(a.getDrawable(R.styleable.AppBarLayout_android_background));
        if (a.hasValue(R.styleable.AppBarLayout_expanded)) {
            setExpanded(a.getBoolean(R.styleable.AppBarLayout_expanded, false));
        }
        a.recycle();
        ViewUtils.setBoundsViewOutlineProvider(this);
        this.mListeners = new ArrayList();
        ViewCompat.setElevation(this, this.mTargetElevation);
        ViewCompat.setOnApplyWindowInsetsListener(this, new OnApplyWindowInsetsListener() {
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                AppBarLayout.this.setWindowInsets(insets);
                return insets.consumeSystemWindowInsets();
            }
        });
    }

    public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
        if (listener != null && !this.mListeners.contains(listener)) {
            this.mListeners.add(listener);
        }
    }

    public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
        if (listener != null) {
            this.mListeners.remove(listener);
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mTotalScrollRange = INVALID_SCROLL_RANGE;
        this.mDownPreScrollRange = INVALID_SCROLL_RANGE;
        this.mDownPreScrollRange = INVALID_SCROLL_RANGE;
        this.mHaveChildWithInterpolator = false;
        int z = getChildCount();
        for (int i = 0; i < z; i += PENDING_ACTION_EXPANDED) {
            if (((LayoutParams) getChildAt(i).getLayoutParams()).getScrollInterpolator() != null) {
                this.mHaveChildWithInterpolator = true;
                return;
            }
        }
    }

    public void setOrientation(int orientation) {
        if (orientation != PENDING_ACTION_EXPANDED) {
            throw new IllegalArgumentException("AppBarLayout is always vertical and does not support horizontal orientation");
        }
        super.setOrientation(orientation);
    }

    public void setExpanded(boolean expanded) {
        setExpanded(expanded, ViewCompat.isLaidOut(this));
    }

    public void setExpanded(boolean expanded, boolean animate) {
        this.mPendingAction = (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0) | (expanded ? PENDING_ACTION_EXPANDED : PENDING_ACTION_COLLAPSED);
        requestLayout();
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams((int) INVALID_SCROLL_RANGE, -2);
    }

    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    protected LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        if (p instanceof android.widget.LinearLayout.LayoutParams) {
            return new LayoutParams((android.widget.LinearLayout.LayoutParams) p);
        }
        if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    final boolean hasChildWithInterpolator() {
        return this.mHaveChildWithInterpolator;
    }

    public final int getTotalScrollRange() {
        if (this.mTotalScrollRange != INVALID_SCROLL_RANGE) {
            return this.mTotalScrollRange;
        }
        int range = 0;
        int z = getChildCount();
        for (int i = 0; i < z; i += PENDING_ACTION_EXPANDED) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childHeight = ViewCompat.isLaidOut(child) ? child.getHeight() : child.getMeasuredHeight();
            int flags = lp.mScrollFlags;
            if ((flags & PENDING_ACTION_EXPANDED) == 0) {
                break;
            }
            range += (lp.topMargin + childHeight) + lp.bottomMargin;
            if ((flags & PENDING_ACTION_COLLAPSED) != 0) {
                range -= ViewCompat.getMinimumHeight(child);
                break;
            }
        }
        int systemWindowInsetTop = range - (this.mLastInsets != null ? this.mLastInsets.getSystemWindowInsetTop() : 0);
        this.mTotalScrollRange = systemWindowInsetTop;
        return systemWindowInsetTop;
    }

    final boolean hasScrollableChildren() {
        return getTotalScrollRange() != 0;
    }

    final int getUpNestedPreScrollRange() {
        return getTotalScrollRange();
    }

    final int getDownNestedPreScrollRange() {
        if (this.mDownPreScrollRange != INVALID_SCROLL_RANGE) {
            return this.mDownPreScrollRange;
        }
        int range = 0;
        for (int i = getChildCount() + INVALID_SCROLL_RANGE; i >= 0; i += INVALID_SCROLL_RANGE) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childHeight = ViewCompat.isLaidOut(child) ? child.getHeight() : child.getMeasuredHeight();
            int flags = lp.mScrollFlags;
            if ((flags & 5) == 5) {
                range += lp.topMargin + lp.bottomMargin;
                if ((flags & 8) != 0) {
                    range += ViewCompat.getMinimumHeight(child);
                } else {
                    range += childHeight;
                }
            } else if (range > 0) {
                break;
            }
        }
        this.mDownPreScrollRange = range;
        return range;
    }

    final int getDownNestedScrollRange() {
        if (this.mDownScrollRange != INVALID_SCROLL_RANGE) {
            return this.mDownScrollRange;
        }
        int range = 0;
        int z = getChildCount();
        for (int i = 0; i < z; i += PENDING_ACTION_EXPANDED) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            int childHeight = (ViewCompat.isLaidOut(child) ? child.getHeight() : child.getMeasuredHeight()) + (lp.topMargin + lp.bottomMargin);
            int flags = lp.mScrollFlags;
            if ((flags & PENDING_ACTION_EXPANDED) == 0) {
                break;
            }
            range += childHeight;
            if ((flags & PENDING_ACTION_COLLAPSED) != 0) {
                return range - ViewCompat.getMinimumHeight(child);
            }
        }
        this.mDownScrollRange = range;
        return range;
    }

    final int getMinimumHeightForVisibleOverlappingContent() {
        int topInset;
        if (this.mLastInsets != null) {
            topInset = this.mLastInsets.getSystemWindowInsetTop();
        } else {
            topInset = 0;
        }
        int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight != 0) {
            return (minHeight * PENDING_ACTION_COLLAPSED) + topInset;
        }
        int childCount = getChildCount();
        if (childCount >= PENDING_ACTION_EXPANDED) {
            return (ViewCompat.getMinimumHeight(getChildAt(childCount + INVALID_SCROLL_RANGE)) * PENDING_ACTION_COLLAPSED) + topInset;
        }
        return 0;
    }

    public void setTargetElevation(float elevation) {
        this.mTargetElevation = elevation;
    }

    public float getTargetElevation() {
        return this.mTargetElevation;
    }

    int getPendingAction() {
        return this.mPendingAction;
    }

    void resetPendingAction() {
        this.mPendingAction = 0;
    }

    private void setWindowInsets(WindowInsetsCompat insets) {
        this.mTotalScrollRange = INVALID_SCROLL_RANGE;
        this.mLastInsets = insets;
        int i = 0;
        int z = getChildCount();
        while (i < z) {
            insets = ViewCompat.dispatchApplyWindowInsets(getChildAt(i), insets);
            if (!insets.isConsumed()) {
                i += PENDING_ACTION_EXPANDED;
            } else {
                return;
            }
        }
    }
}
