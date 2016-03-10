package com.afollestad.materialdialogs.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ScrollView;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog.NotImplementedException;
import com.afollestad.materialdialogs.R;
import com.afollestad.materialdialogs.util.DialogUtils;

public class MDRootLayout extends ViewGroup {
    private static final int INDEX_NEGATIVE = 1;
    private static final int INDEX_NEUTRAL = 0;
    private static final int INDEX_POSITIVE = 2;
    private OnScrollChangedListener mBottomOnScrollChangedListener;
    private int mButtonBarHeight;
    private GravityEnum mButtonGravity;
    private int mButtonHorizontalEdgeMargin;
    private int mButtonPaddingFull;
    private MDButton[] mButtons;
    private View mContent;
    private Paint mDividerPaint;
    private int mDividerWidth;
    private boolean mDrawBottomDivider;
    private boolean mDrawTopDivider;
    private boolean mForceStack;
    private boolean mIsStacked;
    private boolean mNoTitleNoPadding;
    private int mNoTitlePaddingFull;
    private boolean mReducePaddingNoTitleNoButtons;
    private View mTitleBar;
    private OnScrollChangedListener mTopOnScrollChangedListener;
    private boolean mUseFullPadding;

    public MDRootLayout(Context context) {
        super(context);
        this.mDrawTopDivider = false;
        this.mDrawBottomDivider = false;
        this.mButtons = new MDButton[3];
        this.mForceStack = false;
        this.mIsStacked = false;
        this.mUseFullPadding = true;
        this.mButtonGravity = GravityEnum.START;
        init(context, null, INDEX_NEUTRAL);
    }

    public MDRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mDrawTopDivider = false;
        this.mDrawBottomDivider = false;
        this.mButtons = new MDButton[3];
        this.mForceStack = false;
        this.mIsStacked = false;
        this.mUseFullPadding = true;
        this.mButtonGravity = GravityEnum.START;
        init(context, attrs, INDEX_NEUTRAL);
    }

    @TargetApi(11)
    public MDRootLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mDrawTopDivider = false;
        this.mDrawBottomDivider = false;
        this.mButtons = new MDButton[3];
        this.mForceStack = false;
        this.mIsStacked = false;
        this.mUseFullPadding = true;
        this.mButtonGravity = GravityEnum.START;
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public MDRootLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDrawTopDivider = false;
        this.mDrawBottomDivider = false;
        this.mButtons = new MDButton[3];
        this.mForceStack = false;
        this.mIsStacked = false;
        this.mUseFullPadding = true;
        this.mButtonGravity = GravityEnum.START;
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        Resources r = context.getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MDRootLayout, defStyleAttr, INDEX_NEUTRAL);
        this.mReducePaddingNoTitleNoButtons = a.getBoolean(R.styleable.MDRootLayout_md_reduce_padding_no_title_no_buttons, true);
        a.recycle();
        this.mNoTitlePaddingFull = r.getDimensionPixelSize(R.dimen.md_notitle_vertical_padding);
        this.mButtonPaddingFull = r.getDimensionPixelSize(R.dimen.md_button_frame_vertical_padding);
        this.mButtonHorizontalEdgeMargin = r.getDimensionPixelSize(R.dimen.md_button_padding_frame_side);
        this.mButtonBarHeight = r.getDimensionPixelSize(R.dimen.md_button_height);
        this.mDividerPaint = new Paint();
        this.mDividerWidth = r.getDimensionPixelSize(R.dimen.md_divider_height);
        this.mDividerPaint.setColor(DialogUtils.resolveColor(context, R.attr.md_divider_color));
        setWillNotDraw(false);
    }

    public void noTitleNoPadding() {
        this.mNoTitleNoPadding = true;
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        for (int i = INDEX_NEUTRAL; i < getChildCount(); i += INDEX_NEGATIVE) {
            View v = getChildAt(i);
            if (v.getId() == R.id.titleFrame) {
                this.mTitleBar = v;
            } else if (v.getId() == R.id.buttonDefaultNeutral) {
                this.mButtons[INDEX_NEUTRAL] = (MDButton) v;
            } else if (v.getId() == R.id.buttonDefaultNegative) {
                this.mButtons[INDEX_NEGATIVE] = (MDButton) v;
            } else if (v.getId() == R.id.buttonDefaultPositive) {
                this.mButtons[INDEX_POSITIVE] = (MDButton) v;
            } else {
                this.mContent = v;
            }
        }
    }

    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        boolean stacked;
        MDButton[] arr$;
        int len$;
        int i$;
        MDButton button;
        int fullPadding;
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        this.mUseFullPadding = true;
        boolean hasButtons = false;
        if (this.mForceStack) {
            stacked = true;
        } else {
            int buttonsWidth = INDEX_NEUTRAL;
            arr$ = this.mButtons;
            len$ = arr$.length;
            for (i$ = INDEX_NEUTRAL; i$ < len$; i$ += INDEX_NEGATIVE) {
                button = arr$[i$];
                if (button != null && isVisible(button)) {
                    button.setStacked(false, false);
                    measureChild(button, widthMeasureSpec, heightMeasureSpec);
                    buttonsWidth += button.getMeasuredWidth();
                    hasButtons = true;
                }
            }
            stacked = buttonsWidth > width - (getContext().getResources().getDimensionPixelSize(R.dimen.md_neutral_button_margin) * INDEX_POSITIVE);
        }
        int stackedHeight = INDEX_NEUTRAL;
        this.mIsStacked = stacked;
        if (stacked) {
            arr$ = this.mButtons;
            len$ = arr$.length;
            for (i$ = INDEX_NEUTRAL; i$ < len$; i$ += INDEX_NEGATIVE) {
                button = arr$[i$];
                if (button != null && isVisible(button)) {
                    button.setStacked(true, false);
                    measureChild(button, widthMeasureSpec, heightMeasureSpec);
                    stackedHeight += button.getMeasuredHeight();
                    hasButtons = true;
                }
            }
        }
        int availableHeight = height;
        int minPadding = INDEX_NEUTRAL;
        if (!hasButtons) {
            fullPadding = INDEX_NEUTRAL + (this.mButtonPaddingFull * INDEX_POSITIVE);
        } else if (this.mIsStacked) {
            availableHeight -= stackedHeight;
            fullPadding = INDEX_NEUTRAL + (this.mButtonPaddingFull * INDEX_POSITIVE);
            minPadding = INDEX_NEUTRAL + (this.mButtonPaddingFull * INDEX_POSITIVE);
        } else {
            availableHeight -= this.mButtonBarHeight;
            fullPadding = INDEX_NEUTRAL + (this.mButtonPaddingFull * INDEX_POSITIVE);
        }
        if (isVisible(this.mTitleBar)) {
            this.mTitleBar.measure(MeasureSpec.makeMeasureSpec(width, 1073741824), INDEX_NEUTRAL);
            availableHeight -= this.mTitleBar.getMeasuredHeight();
        } else if (!this.mNoTitleNoPadding) {
            fullPadding += this.mNoTitlePaddingFull;
        }
        if (isVisible(this.mContent)) {
            this.mContent.measure(MeasureSpec.makeMeasureSpec(width, 1073741824), MeasureSpec.makeMeasureSpec(availableHeight - minPadding, Action.UNDEFINED_DURATION));
            if (this.mContent.getMeasuredHeight() > availableHeight - fullPadding) {
                this.mUseFullPadding = false;
                availableHeight = INDEX_NEUTRAL;
            } else if (!this.mReducePaddingNoTitleNoButtons || isVisible(this.mTitleBar) || hasButtons) {
                this.mUseFullPadding = true;
                availableHeight -= this.mContent.getMeasuredHeight() + fullPadding;
            } else {
                this.mUseFullPadding = false;
                availableHeight -= this.mContent.getMeasuredHeight() + minPadding;
            }
        }
        setMeasuredDimension(width, height - availableHeight);
    }

    private static boolean isVisible(View v) {
        boolean visible;
        if (v == null || v.getVisibility() == 8) {
            visible = false;
        } else {
            visible = true;
        }
        if (!visible || !(v instanceof MDButton)) {
            return visible;
        }
        if (((MDButton) v).getText().toString().trim().length() > 0) {
            return true;
        }
        return false;
    }

    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.mContent != null) {
            int y;
            if (this.mDrawTopDivider) {
                y = this.mContent.getTop();
                canvas.drawRect(0.0f, (float) (y - this.mDividerWidth), (float) getMeasuredWidth(), (float) y, this.mDividerPaint);
            }
            if (this.mDrawBottomDivider) {
                y = this.mContent.getBottom();
                canvas.drawRect(0.0f, (float) y, (float) getMeasuredWidth(), (float) (this.mDividerWidth + y), this.mDividerPaint);
            }
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (isVisible(this.mTitleBar)) {
            int height = this.mTitleBar.getMeasuredHeight();
            this.mTitleBar.layout(l, t, r, t + height);
            t += height;
        } else if (!this.mNoTitleNoPadding && this.mUseFullPadding) {
            t += this.mNoTitlePaddingFull;
        }
        if (isVisible(this.mContent)) {
            this.mContent.layout(l, t, r, this.mContent.getMeasuredHeight() + t);
        }
        if (this.mIsStacked) {
            b -= this.mButtonPaddingFull;
            MDButton[] arr$ = this.mButtons;
            int len$ = arr$.length;
            for (int i$ = INDEX_NEUTRAL; i$ < len$; i$ += INDEX_NEGATIVE) {
                MDButton mButton = arr$[i$];
                if (isVisible(mButton)) {
                    mButton.layout(l, b - mButton.getMeasuredHeight(), r, b);
                    b -= mButton.getMeasuredHeight();
                }
            }
        } else {
            int bl;
            int br;
            int barBottom = b;
            if (this.mUseFullPadding) {
                barBottom -= this.mButtonPaddingFull;
            }
            int barTop = barBottom - this.mButtonBarHeight;
            int offset = this.mButtonHorizontalEdgeMargin;
            int neutralLeft = -1;
            int neutralRight = -1;
            if (isVisible(this.mButtons[INDEX_POSITIVE])) {
                if (this.mButtonGravity == GravityEnum.END) {
                    bl = l + offset;
                    br = bl + this.mButtons[INDEX_POSITIVE].getMeasuredWidth();
                } else {
                    br = r - offset;
                    bl = br - this.mButtons[INDEX_POSITIVE].getMeasuredWidth();
                    neutralRight = bl;
                }
                this.mButtons[INDEX_POSITIVE].layout(bl, barTop, br, barBottom);
                offset += this.mButtons[INDEX_POSITIVE].getMeasuredWidth();
            }
            if (isVisible(this.mButtons[INDEX_NEGATIVE])) {
                if (this.mButtonGravity == GravityEnum.END) {
                    bl = l + offset;
                    br = bl + this.mButtons[INDEX_NEGATIVE].getMeasuredWidth();
                } else {
                    if (this.mButtonGravity == GravityEnum.START) {
                        br = r - offset;
                        bl = br - this.mButtons[INDEX_NEGATIVE].getMeasuredWidth();
                    } else {
                        bl = l + this.mButtonHorizontalEdgeMargin;
                        br = bl + this.mButtons[INDEX_NEGATIVE].getMeasuredWidth();
                        neutralLeft = br;
                    }
                }
                this.mButtons[INDEX_NEGATIVE].layout(bl, barTop, br, barBottom);
            }
            if (isVisible(this.mButtons[INDEX_NEUTRAL])) {
                if (this.mButtonGravity == GravityEnum.END) {
                    br = r - this.mButtonHorizontalEdgeMargin;
                    bl = br - this.mButtons[INDEX_NEUTRAL].getMeasuredWidth();
                } else {
                    if (this.mButtonGravity == GravityEnum.START) {
                        bl = l + this.mButtonHorizontalEdgeMargin;
                        br = bl + this.mButtons[INDEX_NEUTRAL].getMeasuredWidth();
                    } else {
                        if (neutralLeft == -1 && neutralRight != -1) {
                            neutralLeft = neutralRight - this.mButtons[INDEX_NEUTRAL].getMeasuredWidth();
                        } else if (neutralRight == -1 && neutralLeft != -1) {
                            neutralRight = neutralLeft + this.mButtons[INDEX_NEUTRAL].getMeasuredWidth();
                        } else if (neutralRight == -1) {
                            neutralLeft = ((r - l) / INDEX_POSITIVE) - (this.mButtons[INDEX_NEUTRAL].getMeasuredWidth() / INDEX_POSITIVE);
                            neutralRight = neutralLeft + this.mButtons[INDEX_NEUTRAL].getMeasuredWidth();
                        }
                        bl = neutralLeft;
                        br = neutralRight;
                    }
                }
                this.mButtons[INDEX_NEUTRAL].layout(bl, barTop, br, barBottom);
            }
        }
        setUpDividersVisibility(this.mContent, true, true);
    }

    public void setForceStack(boolean forceStack) {
        this.mForceStack = forceStack;
        invalidate();
    }

    public void setDividerColor(int color) {
        this.mDividerPaint.setColor(color);
        invalidate();
    }

    public void setButtonGravity(GravityEnum gravity) {
        this.mButtonGravity = gravity;
    }

    public void setButtonStackedGravity(GravityEnum gravity) {
        MDButton[] arr$ = this.mButtons;
        int len$ = arr$.length;
        for (int i$ = INDEX_NEUTRAL; i$ < len$; i$ += INDEX_NEGATIVE) {
            MDButton mButton = arr$[i$];
            if (mButton != null) {
                mButton.setStackedGravity(gravity);
            }
        }
    }

    private void setUpDividersVisibility(final View view, final boolean setForTop, final boolean setForBottom) {
        if (view != null) {
            if (view instanceof ScrollView) {
                ScrollView sv = (ScrollView) view;
                if (canScrollViewScroll(sv)) {
                    addScrollListener(sv, setForTop, setForBottom);
                    return;
                }
                if (setForTop) {
                    this.mDrawTopDivider = false;
                }
                if (setForBottom) {
                    this.mDrawBottomDivider = false;
                }
            } else if (view instanceof AdapterView) {
                AdapterView sv2 = (AdapterView) view;
                if (canAdapterViewScroll(sv2)) {
                    addScrollListener(sv2, setForTop, setForBottom);
                    return;
                }
                if (setForTop) {
                    this.mDrawTopDivider = false;
                }
                if (setForBottom) {
                    this.mDrawBottomDivider = false;
                }
            } else if (view instanceof WebView) {
                view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                    public boolean onPreDraw() {
                        if (view.getMeasuredHeight() != 0) {
                            if (MDRootLayout.canWebViewScroll((WebView) view)) {
                                MDRootLayout.this.addScrollListener((ViewGroup) view, setForTop, setForBottom);
                            } else {
                                if (setForTop) {
                                    MDRootLayout.this.mDrawTopDivider = false;
                                }
                                if (setForBottom) {
                                    MDRootLayout.this.mDrawBottomDivider = false;
                                }
                            }
                            view.getViewTreeObserver().removeOnPreDrawListener(this);
                        }
                        return true;
                    }
                });
            } else if (view instanceof RecyclerView) {
                boolean canScroll = canRecyclerViewScroll((RecyclerView) view);
                if (setForTop) {
                    this.mDrawTopDivider = canScroll;
                }
                if (setForBottom) {
                    this.mDrawBottomDivider = canScroll;
                }
            } else if (view instanceof ViewGroup) {
                View topView = getTopView((ViewGroup) view);
                setUpDividersVisibility(topView, setForTop, setForBottom);
                View bottomView = getBottomView((ViewGroup) view);
                if (bottomView != topView) {
                    setUpDividersVisibility(bottomView, false, true);
                }
            }
        }
    }

    private void addScrollListener(final ViewGroup vg, final boolean setForTop, final boolean setForBottom) {
        if ((!setForBottom && this.mTopOnScrollChangedListener == null) || (setForBottom && this.mBottomOnScrollChangedListener == null)) {
            OnScrollChangedListener onScrollChangedListener = new OnScrollChangedListener() {
                public void onScrollChanged() {
                    boolean hasButtons = false;
                    MDButton[] arr$ = MDRootLayout.this.mButtons;
                    int len$ = arr$.length;
                    for (int i$ = MDRootLayout.INDEX_NEUTRAL; i$ < len$; i$ += MDRootLayout.INDEX_NEGATIVE) {
                        MDButton button = arr$[i$];
                        if (button != null && button.getVisibility() != 8) {
                            hasButtons = true;
                            break;
                        }
                    }
                    if (vg instanceof WebView) {
                        MDRootLayout.this.invalidateDividersForWebView((WebView) vg, setForTop, setForBottom, hasButtons);
                    } else {
                        MDRootLayout.this.invalidateDividersForScrollingView(vg, setForTop, setForBottom, hasButtons);
                    }
                    MDRootLayout.this.invalidate();
                }
            };
            if (setForBottom) {
                this.mBottomOnScrollChangedListener = onScrollChangedListener;
                vg.getViewTreeObserver().addOnScrollChangedListener(this.mBottomOnScrollChangedListener);
            } else {
                this.mTopOnScrollChangedListener = onScrollChangedListener;
                vg.getViewTreeObserver().addOnScrollChangedListener(this.mTopOnScrollChangedListener);
            }
            onScrollChangedListener.onScrollChanged();
        }
    }

    private void invalidateDividersForScrollingView(ViewGroup view, boolean setForTop, boolean setForBottom, boolean hasButtons) {
        boolean z = true;
        if (setForTop && view.getChildCount() > 0) {
            boolean z2 = (this.mTitleBar == null || this.mTitleBar.getVisibility() == 8 || view.getScrollY() + view.getPaddingTop() <= view.getChildAt(INDEX_NEUTRAL).getTop()) ? false : true;
            this.mDrawTopDivider = z2;
        }
        if (setForBottom && view.getChildCount() > 0) {
            if (!hasButtons || (view.getScrollY() + view.getHeight()) - view.getPaddingBottom() >= view.getChildAt(view.getChildCount() - 1).getBottom()) {
                z = false;
            }
            this.mDrawBottomDivider = z;
        }
    }

    private void invalidateDividersForWebView(WebView view, boolean setForTop, boolean setForBottom, boolean hasButtons) {
        boolean z = true;
        if (setForTop) {
            boolean z2 = (this.mTitleBar == null || this.mTitleBar.getVisibility() == 8 || view.getScrollY() + view.getPaddingTop() <= 0) ? false : true;
            this.mDrawTopDivider = z2;
        }
        if (setForBottom) {
            if (!hasButtons || ((float) ((view.getScrollY() + view.getMeasuredHeight()) - view.getPaddingBottom())) >= ((float) view.getContentHeight()) * view.getScale()) {
                z = false;
            }
            this.mDrawBottomDivider = z;
        }
    }

    public static boolean canRecyclerViewScroll(RecyclerView view) {
        LayoutManager lm = view.getLayoutManager();
        int count = view.getAdapter().getItemCount();
        if (lm instanceof LinearLayoutManager) {
            int lastVisible = ((LinearLayoutManager) lm).findLastVisibleItemPosition();
            if (lastVisible == -1) {
                return false;
            }
            boolean lastItemVisible;
            if (lastVisible == count - 1) {
                lastItemVisible = true;
            } else {
                lastItemVisible = false;
            }
            if (!lastItemVisible || (view.getChildCount() > 0 && view.getChildAt(view.getChildCount() - 1).getBottom() > view.getHeight() - view.getPaddingBottom())) {
                return true;
            }
            return false;
        }
        throw new NotImplementedException("Material Dialogs currently only supports LinearLayoutManager. Please report any new layout managers.");
    }

    private static boolean canScrollViewScroll(ScrollView sv) {
        if (sv.getChildCount() != 0 && (sv.getMeasuredHeight() - sv.getPaddingTop()) - sv.getPaddingBottom() < sv.getChildAt(INDEX_NEUTRAL).getMeasuredHeight()) {
            return true;
        }
        return false;
    }

    private static boolean canWebViewScroll(WebView view) {
        return ((float) view.getMeasuredHeight()) < ((float) view.getContentHeight()) * view.getScale();
    }

    private static boolean canAdapterViewScroll(AdapterView lv) {
        if (lv.getLastVisiblePosition() == -1) {
            return false;
        }
        boolean firstItemVisible;
        if (lv.getFirstVisiblePosition() == 0) {
            firstItemVisible = true;
        } else {
            firstItemVisible = false;
        }
        boolean lastItemVisible;
        if (lv.getLastVisiblePosition() == lv.getCount() - 1) {
            lastItemVisible = true;
        } else {
            lastItemVisible = false;
        }
        if (!firstItemVisible || !lastItemVisible || lv.getChildCount() <= 0 || lv.getChildAt(INDEX_NEUTRAL).getTop() < lv.getPaddingTop() || lv.getChildAt(lv.getChildCount() - 1).getBottom() > lv.getHeight() - lv.getPaddingBottom()) {
            return true;
        }
        return false;
    }

    @Nullable
    private static View getBottomView(ViewGroup viewGroup) {
        if (viewGroup == null || viewGroup.getChildCount() == 0) {
            return null;
        }
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            View child = viewGroup.getChildAt(i);
            if (child.getVisibility() == 0 && child.getBottom() == viewGroup.getMeasuredHeight()) {
                return child;
            }
        }
        return null;
    }

    @Nullable
    private static View getTopView(ViewGroup viewGroup) {
        if (viewGroup == null || viewGroup.getChildCount() == 0) {
            return null;
        }
        for (int i = viewGroup.getChildCount() - 1; i >= 0; i--) {
            View child = viewGroup.getChildAt(i);
            if (child.getVisibility() == 0 && child.getTop() == 0) {
                return child;
            }
        }
        return null;
    }
}
