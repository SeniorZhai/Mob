package android.support.design.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.R;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.internal.widget.TintManager;
import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

public class TabLayout extends HorizontalScrollView {
    private static final int ANIMATION_DURATION = 300;
    private static final int DEFAULT_HEIGHT = 48;
    private static final int FIXED_WRAP_GUTTER_MIN = 16;
    public static final int GRAVITY_CENTER = 1;
    public static final int GRAVITY_FILL = 0;
    public static final int MODE_FIXED = 1;
    public static final int MODE_SCROLLABLE = 0;
    private static final int MOTION_NON_ADJACENT_OFFSET = 24;
    private static final int TAB_MIN_WIDTH_MARGIN = 56;
    private int mContentInsetStart;
    private ValueAnimatorCompat mIndicatorAnimator;
    private int mMode;
    private OnTabSelectedListener mOnTabSelectedListener;
    private final int mRequestedTabMaxWidth;
    private ValueAnimatorCompat mScrollAnimator;
    private Tab mSelectedTab;
    private final int mTabBackgroundResId;
    private OnClickListener mTabClickListener;
    private int mTabGravity;
    private int mTabMaxWidth;
    private final int mTabMinWidth;
    private int mTabPaddingBottom;
    private int mTabPaddingEnd;
    private int mTabPaddingStart;
    private int mTabPaddingTop;
    private final SlidingTabStrip mTabStrip;
    private int mTabTextAppearance;
    private ColorStateList mTabTextColors;
    private final ArrayList<Tab> mTabs;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Mode {
    }

    public interface OnTabSelectedListener {
        void onTabReselected(Tab tab);

        void onTabSelected(Tab tab);

        void onTabUnselected(Tab tab);
    }

    private class SlidingTabStrip extends LinearLayout {
        private int mIndicatorLeft = -1;
        private int mIndicatorRight = -1;
        private int mSelectedIndicatorHeight;
        private final Paint mSelectedIndicatorPaint;
        private int mSelectedPosition = -1;
        private float mSelectionOffset;

        SlidingTabStrip(Context context) {
            super(context);
            setWillNotDraw(false);
            this.mSelectedIndicatorPaint = new Paint();
        }

        void setSelectedIndicatorColor(int color) {
            if (this.mSelectedIndicatorPaint.getColor() != color) {
                this.mSelectedIndicatorPaint.setColor(color);
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        void setSelectedIndicatorHeight(int height) {
            if (this.mSelectedIndicatorHeight != height) {
                this.mSelectedIndicatorHeight = height;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        boolean childrenNeedLayout() {
            int z = getChildCount();
            for (int i = TabLayout.MODE_SCROLLABLE; i < z; i += TabLayout.MODE_FIXED) {
                if (getChildAt(i).getWidth() <= 0) {
                    return true;
                }
            }
            return false;
        }

        void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
            this.mSelectedPosition = position;
            this.mSelectionOffset = positionOffset;
            updateIndicatorPosition();
        }

        float getIndicatorPosition() {
            return ((float) this.mSelectedPosition) + this.mSelectionOffset;
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (MeasureSpec.getMode(widthMeasureSpec) == 1073741824 && TabLayout.this.mMode == TabLayout.MODE_FIXED && TabLayout.this.mTabGravity == TabLayout.MODE_FIXED) {
                int i;
                int count = getChildCount();
                int unspecifiedSpec = MeasureSpec.makeMeasureSpec(TabLayout.MODE_SCROLLABLE, TabLayout.MODE_SCROLLABLE);
                int largestTabWidth = TabLayout.MODE_SCROLLABLE;
                int z = count;
                for (i = TabLayout.MODE_SCROLLABLE; i < z; i += TabLayout.MODE_FIXED) {
                    View child = getChildAt(i);
                    child.measure(unspecifiedSpec, heightMeasureSpec);
                    largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
                }
                if (largestTabWidth > 0) {
                    if (largestTabWidth * count <= getMeasuredWidth() - (TabLayout.this.dpToPx(TabLayout.FIXED_WRAP_GUTTER_MIN) * 2)) {
                        for (i = TabLayout.MODE_SCROLLABLE; i < count; i += TabLayout.MODE_FIXED) {
                            LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
                            lp.width = largestTabWidth;
                            lp.weight = 0.0f;
                        }
                    } else {
                        TabLayout.this.mTabGravity = TabLayout.MODE_SCROLLABLE;
                        TabLayout.this.updateTabViewsLayoutParams();
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }
            }
        }

        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            updateIndicatorPosition();
        }

        private void updateIndicatorPosition() {
            int right;
            int left;
            View selectedTitle = getChildAt(this.mSelectedPosition);
            if (selectedTitle == null || selectedTitle.getWidth() <= 0) {
                right = -1;
                left = -1;
            } else {
                left = selectedTitle.getLeft();
                right = selectedTitle.getRight();
                if (this.mSelectionOffset > 0.0f && this.mSelectedPosition < getChildCount() - 1) {
                    View nextTitle = getChildAt(this.mSelectedPosition + TabLayout.MODE_FIXED);
                    left = (int) ((this.mSelectionOffset * ((float) nextTitle.getLeft())) + ((DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - this.mSelectionOffset) * ((float) left)));
                    right = (int) ((this.mSelectionOffset * ((float) nextTitle.getRight())) + ((DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - this.mSelectionOffset) * ((float) right)));
                }
            }
            setIndicatorPosition(left, right);
        }

        private void setIndicatorPosition(int left, int right) {
            if (left != this.mIndicatorLeft || right != this.mIndicatorRight) {
                this.mIndicatorLeft = left;
                this.mIndicatorRight = right;
                ViewCompat.postInvalidateOnAnimation(this);
            }
        }

        void animateIndicatorToPosition(final int position, int duration) {
            int startLeft;
            int startRight;
            boolean isRtl = ViewCompat.getLayoutDirection(this) == TabLayout.MODE_FIXED;
            View targetView = getChildAt(position);
            final int targetLeft = targetView.getLeft();
            final int targetRight = targetView.getRight();
            if (Math.abs(position - this.mSelectedPosition) <= TabLayout.MODE_FIXED) {
                startLeft = this.mIndicatorLeft;
                startRight = this.mIndicatorRight;
            } else {
                int offset = TabLayout.this.dpToPx(TabLayout.MOTION_NON_ADJACENT_OFFSET);
                if (position < this.mSelectedPosition) {
                    if (isRtl) {
                        startRight = targetLeft - offset;
                        startLeft = startRight;
                    } else {
                        startRight = targetRight + offset;
                        startLeft = startRight;
                    }
                } else if (isRtl) {
                    startRight = targetRight + offset;
                    startLeft = startRight;
                } else {
                    startRight = targetLeft - offset;
                    startLeft = startRight;
                }
            }
            if (startLeft != targetLeft || startRight != targetRight) {
                ValueAnimatorCompat animator = TabLayout.this.mIndicatorAnimator = ViewUtils.createAnimator();
                animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                animator.setDuration(duration);
                animator.setFloatValues(0.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                animator.setUpdateListener(new AnimatorUpdateListener() {
                    public void onAnimationUpdate(ValueAnimatorCompat animator) {
                        float fraction = animator.getAnimatedFraction();
                        SlidingTabStrip.this.setIndicatorPosition(AnimationUtils.lerp(startLeft, targetLeft, fraction), AnimationUtils.lerp(startRight, targetRight, fraction));
                    }
                });
                animator.setListener(new AnimatorListenerAdapter() {
                    public void onAnimationEnd(ValueAnimatorCompat animator) {
                        SlidingTabStrip.this.mSelectedPosition = position;
                        SlidingTabStrip.this.mSelectionOffset = 0.0f;
                    }

                    public void onAnimationCancel(ValueAnimatorCompat animator) {
                        SlidingTabStrip.this.mSelectedPosition = position;
                        SlidingTabStrip.this.mSelectionOffset = 0.0f;
                    }
                });
                animator.start();
            }
        }

        public void draw(Canvas canvas) {
            super.draw(canvas);
            if (this.mIndicatorLeft >= 0 && this.mIndicatorRight > this.mIndicatorLeft) {
                canvas.drawRect((float) this.mIndicatorLeft, (float) (getHeight() - this.mSelectedIndicatorHeight), (float) this.mIndicatorRight, (float) getHeight(), this.mSelectedIndicatorPaint);
            }
        }
    }

    public static final class Tab {
        public static final int INVALID_POSITION = -1;
        private CharSequence mContentDesc;
        private View mCustomView;
        private Drawable mIcon;
        private final TabLayout mParent;
        private int mPosition = INVALID_POSITION;
        private Object mTag;
        private CharSequence mText;

        Tab(TabLayout parent) {
            this.mParent = parent;
        }

        @Nullable
        public Object getTag() {
            return this.mTag;
        }

        @NonNull
        public Tab setTag(@Nullable Object tag) {
            this.mTag = tag;
            return this;
        }

        @Nullable
        public View getCustomView() {
            return this.mCustomView;
        }

        @NonNull
        public Tab setCustomView(@Nullable View view) {
            this.mCustomView = view;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }
            return this;
        }

        @NonNull
        public Tab setCustomView(@LayoutRes int layoutResId) {
            return setCustomView(LayoutInflater.from(this.mParent.getContext()).inflate(layoutResId, null));
        }

        @Nullable
        public Drawable getIcon() {
            return this.mIcon;
        }

        public int getPosition() {
            return this.mPosition;
        }

        void setPosition(int position) {
            this.mPosition = position;
        }

        @Nullable
        public CharSequence getText() {
            return this.mText;
        }

        @NonNull
        public Tab setIcon(@Nullable Drawable icon) {
            this.mIcon = icon;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }
            return this;
        }

        @NonNull
        public Tab setIcon(@DrawableRes int resId) {
            return setIcon(TintManager.getDrawable(this.mParent.getContext(), resId));
        }

        @NonNull
        public Tab setText(@Nullable CharSequence text) {
            this.mText = text;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }
            return this;
        }

        @NonNull
        public Tab setText(@StringRes int resId) {
            return setText(this.mParent.getResources().getText(resId));
        }

        public void select() {
            this.mParent.selectTab(this);
        }

        public boolean isSelected() {
            return this.mParent.getSelectedTabPosition() == this.mPosition;
        }

        @NonNull
        public Tab setContentDescription(@StringRes int resId) {
            return setContentDescription(this.mParent.getResources().getText(resId));
        }

        @NonNull
        public Tab setContentDescription(@Nullable CharSequence contentDesc) {
            this.mContentDesc = contentDesc;
            if (this.mPosition >= 0) {
                this.mParent.updateTab(this.mPosition);
            }
            return this;
        }

        @Nullable
        public CharSequence getContentDescription() {
            return this.mContentDesc;
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TabGravity {
    }

    public static class TabLayoutOnPageChangeListener implements OnPageChangeListener {
        private int mPreviousScrollState;
        private int mScrollState;
        private final WeakReference<TabLayout> mTabLayoutRef;

        public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
            this.mTabLayoutRef = new WeakReference(tabLayout);
        }

        public void onPageScrollStateChanged(int state) {
            this.mPreviousScrollState = this.mScrollState;
            this.mScrollState = state;
        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            boolean updateText = true;
            TabLayout tabLayout = (TabLayout) this.mTabLayoutRef.get();
            if (tabLayout != null) {
                if (!(this.mScrollState == TabLayout.MODE_FIXED || (this.mScrollState == 2 && this.mPreviousScrollState == TabLayout.MODE_FIXED))) {
                    updateText = false;
                }
                tabLayout.setScrollPosition(position, positionOffset, updateText);
            }
        }

        public void onPageSelected(int position) {
            TabLayout tabLayout = (TabLayout) this.mTabLayoutRef.get();
            if (tabLayout != null) {
                tabLayout.selectTab(tabLayout.getTabAt(position), this.mScrollState == 0);
            }
        }
    }

    class TabView extends LinearLayout implements OnLongClickListener {
        private ImageView mCustomIconView;
        private TextView mCustomTextView;
        private View mCustomView;
        private ImageView mIconView;
        private final Tab mTab;
        private TextView mTextView;

        public TabView(Context context, Tab tab) {
            super(context);
            this.mTab = tab;
            if (TabLayout.this.mTabBackgroundResId != 0) {
                setBackgroundDrawable(TintManager.getDrawable(context, TabLayout.this.mTabBackgroundResId));
            }
            ViewCompat.setPaddingRelative(this, TabLayout.this.mTabPaddingStart, TabLayout.this.mTabPaddingTop, TabLayout.this.mTabPaddingEnd, TabLayout.this.mTabPaddingBottom);
            setGravity(17);
            update();
        }

        public void setSelected(boolean selected) {
            boolean changed = isSelected() != selected;
            super.setSelected(selected);
            if (changed && selected) {
                sendAccessibilityEvent(4);
                if (this.mTextView != null) {
                    this.mTextView.setSelected(selected);
                }
                if (this.mIconView != null) {
                    this.mIconView.setSelected(selected);
                }
            }
        }

        @TargetApi(14)
        public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
            super.onInitializeAccessibilityEvent(event);
            event.setClassName(android.support.v7.app.ActionBar.Tab.class.getName());
        }

        @TargetApi(14)
        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
            super.onInitializeAccessibilityNodeInfo(info);
            info.setClassName(android.support.v7.app.ActionBar.Tab.class.getName());
        }

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int measuredWidth = getMeasuredWidth();
            if (measuredWidth < TabLayout.this.mTabMinWidth || measuredWidth > TabLayout.this.mTabMaxWidth) {
                super.onMeasure(MeasureSpec.makeMeasureSpec(MathUtils.constrain(measuredWidth, TabLayout.this.mTabMinWidth, TabLayout.this.mTabMaxWidth), 1073741824), heightMeasureSpec);
            }
        }

        final void update() {
            Tab tab = this.mTab;
            View custom = tab.getCustomView();
            if (custom != null) {
                TabView customParent = custom.getParent();
                if (customParent != this) {
                    if (customParent != null) {
                        customParent.removeView(custom);
                    }
                    addView(custom);
                }
                this.mCustomView = custom;
                if (this.mTextView != null) {
                    this.mTextView.setVisibility(8);
                }
                if (this.mIconView != null) {
                    this.mIconView.setVisibility(8);
                    this.mIconView.setImageDrawable(null);
                }
                this.mCustomTextView = (TextView) custom.findViewById(16908308);
                this.mCustomIconView = (ImageView) custom.findViewById(16908294);
            } else {
                if (this.mCustomView != null) {
                    removeView(this.mCustomView);
                    this.mCustomView = null;
                }
                this.mCustomTextView = null;
                this.mCustomIconView = null;
            }
            if (this.mCustomView == null) {
                if (this.mIconView == null) {
                    ImageView iconView = (ImageView) LayoutInflater.from(getContext()).inflate(R.layout.design_layout_tab_icon, this, false);
                    addView(iconView, TabLayout.MODE_SCROLLABLE);
                    this.mIconView = iconView;
                }
                if (this.mTextView == null) {
                    TextView textView = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.design_layout_tab_text, this, false);
                    addView(textView);
                    this.mTextView = textView;
                }
                this.mTextView.setTextAppearance(getContext(), TabLayout.this.mTabTextAppearance);
                if (TabLayout.this.mTabTextColors != null) {
                    this.mTextView.setTextColor(TabLayout.this.mTabTextColors);
                }
                updateTextAndIcon(tab, this.mTextView, this.mIconView);
            } else if (this.mCustomTextView != null || this.mCustomIconView != null) {
                updateTextAndIcon(tab, this.mCustomTextView, this.mCustomIconView);
            }
        }

        private void updateTextAndIcon(Tab tab, TextView textView, ImageView iconView) {
            boolean hasText;
            Drawable icon = tab.getIcon();
            CharSequence text = tab.getText();
            if (iconView != null) {
                if (icon != null) {
                    iconView.setImageDrawable(icon);
                    iconView.setVisibility(TabLayout.MODE_SCROLLABLE);
                    setVisibility(TabLayout.MODE_SCROLLABLE);
                } else {
                    iconView.setVisibility(8);
                    iconView.setImageDrawable(null);
                }
                iconView.setContentDescription(tab.getContentDescription());
            }
            if (TextUtils.isEmpty(text)) {
                hasText = false;
            } else {
                hasText = true;
            }
            if (textView != null) {
                if (hasText) {
                    textView.setText(text);
                    textView.setContentDescription(tab.getContentDescription());
                    textView.setVisibility(TabLayout.MODE_SCROLLABLE);
                    setVisibility(TabLayout.MODE_SCROLLABLE);
                } else {
                    textView.setVisibility(8);
                    textView.setText(null);
                }
            }
            if (hasText || TextUtils.isEmpty(tab.getContentDescription())) {
                setOnLongClickListener(null);
                setLongClickable(false);
                return;
            }
            setOnLongClickListener(this);
        }

        public boolean onLongClick(View v) {
            int[] screenPos = new int[2];
            getLocationOnScreen(screenPos);
            Context context = getContext();
            int width = getWidth();
            int height = getHeight();
            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            Toast cheatSheet = Toast.makeText(context, this.mTab.getContentDescription(), TabLayout.MODE_SCROLLABLE);
            cheatSheet.setGravity(49, (screenPos[TabLayout.MODE_SCROLLABLE] + (width / 2)) - (screenWidth / 2), height);
            cheatSheet.show();
            return true;
        }

        public Tab getTab() {
            return this.mTab;
        }
    }

    public static class ViewPagerOnTabSelectedListener implements OnTabSelectedListener {
        private final ViewPager mViewPager;

        public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
            this.mViewPager = viewPager;
        }

        public void onTabSelected(Tab tab) {
            this.mViewPager.setCurrentItem(tab.getPosition());
        }

        public void onTabUnselected(Tab tab) {
        }

        public void onTabReselected(Tab tab) {
        }
    }

    public TabLayout(Context context) {
        this(context, null);
    }

    public TabLayout(Context context, AttributeSet attrs) {
        this(context, attrs, MODE_SCROLLABLE);
    }

    public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mTabs = new ArrayList();
        this.mTabMaxWidth = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        setHorizontalScrollBarEnabled(false);
        setFillViewport(true);
        this.mTabStrip = new SlidingTabStrip(context);
        addView(this.mTabStrip, -2, -1);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabLayout, defStyleAttr, R.style.Widget_Design_TabLayout);
        this.mTabStrip.setSelectedIndicatorHeight(a.getDimensionPixelSize(R.styleable.TabLayout_tabIndicatorHeight, MODE_SCROLLABLE));
        this.mTabStrip.setSelectedIndicatorColor(a.getColor(R.styleable.TabLayout_tabIndicatorColor, MODE_SCROLLABLE));
        this.mTabTextAppearance = a.getResourceId(R.styleable.TabLayout_tabTextAppearance, R.style.TextAppearance_Design_Tab);
        int dimensionPixelSize = a.getDimensionPixelSize(R.styleable.TabLayout_tabPadding, MODE_SCROLLABLE);
        this.mTabPaddingBottom = dimensionPixelSize;
        this.mTabPaddingEnd = dimensionPixelSize;
        this.mTabPaddingTop = dimensionPixelSize;
        this.mTabPaddingStart = dimensionPixelSize;
        this.mTabPaddingStart = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingStart, this.mTabPaddingStart);
        this.mTabPaddingTop = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingTop, this.mTabPaddingTop);
        this.mTabPaddingEnd = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingEnd, this.mTabPaddingEnd);
        this.mTabPaddingBottom = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingBottom, this.mTabPaddingBottom);
        this.mTabTextColors = loadTextColorFromTextAppearance(this.mTabTextAppearance);
        if (a.hasValue(R.styleable.TabLayout_tabTextColor)) {
            this.mTabTextColors = a.getColorStateList(R.styleable.TabLayout_tabTextColor);
        }
        if (a.hasValue(R.styleable.TabLayout_tabSelectedTextColor)) {
            this.mTabTextColors = createColorStateList(this.mTabTextColors.getDefaultColor(), a.getColor(R.styleable.TabLayout_tabSelectedTextColor, MODE_SCROLLABLE));
        }
        this.mTabMinWidth = a.getDimensionPixelSize(R.styleable.TabLayout_tabMinWidth, MODE_SCROLLABLE);
        this.mRequestedTabMaxWidth = a.getDimensionPixelSize(R.styleable.TabLayout_tabMaxWidth, MODE_SCROLLABLE);
        this.mTabBackgroundResId = a.getResourceId(R.styleable.TabLayout_tabBackground, MODE_SCROLLABLE);
        this.mContentInsetStart = a.getDimensionPixelSize(R.styleable.TabLayout_tabContentStart, MODE_SCROLLABLE);
        this.mMode = a.getInt(R.styleable.TabLayout_tabMode, MODE_FIXED);
        this.mTabGravity = a.getInt(R.styleable.TabLayout_tabGravity, MODE_SCROLLABLE);
        a.recycle();
        applyModeAndGravity();
    }

    public void setSelectedTabIndicatorColor(@ColorInt int color) {
        this.mTabStrip.setSelectedIndicatorColor(color);
    }

    public void setSelectedTabIndicatorHeight(int height) {
        this.mTabStrip.setSelectedIndicatorHeight(height);
    }

    public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
        if ((this.mIndicatorAnimator == null || !this.mIndicatorAnimator.isRunning()) && position >= 0 && position < this.mTabStrip.getChildCount()) {
            this.mTabStrip.setIndicatorPositionFromTabPosition(position, positionOffset);
            scrollTo(calculateScrollXForTab(position, positionOffset), MODE_SCROLLABLE);
            if (updateSelectedText) {
                setSelectedTabView(Math.round(((float) position) + positionOffset));
            }
        }
    }

    private float getScrollPosition() {
        return this.mTabStrip.getIndicatorPosition();
    }

    public void addTab(@NonNull Tab tab) {
        addTab(tab, this.mTabs.isEmpty());
    }

    public void addTab(@NonNull Tab tab, int position) {
        addTab(tab, position, this.mTabs.isEmpty());
    }

    public void addTab(@NonNull Tab tab, boolean setSelected) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
        }
        addTabView(tab, setSelected);
        configureTab(tab, this.mTabs.size());
        if (setSelected) {
            tab.select();
        }
    }

    public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
        }
        addTabView(tab, position, setSelected);
        configureTab(tab, position);
        if (setSelected) {
            tab.select();
        }
    }

    public void setOnTabSelectedListener(OnTabSelectedListener onTabSelectedListener) {
        this.mOnTabSelectedListener = onTabSelectedListener;
    }

    @NonNull
    public Tab newTab() {
        return new Tab(this);
    }

    public int getTabCount() {
        return this.mTabs.size();
    }

    @Nullable
    public Tab getTabAt(int index) {
        return (Tab) this.mTabs.get(index);
    }

    public int getSelectedTabPosition() {
        return this.mSelectedTab != null ? this.mSelectedTab.getPosition() : -1;
    }

    public void removeTab(Tab tab) {
        if (tab.mParent != this) {
            throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
        }
        removeTabAt(tab.getPosition());
    }

    public void removeTabAt(int position) {
        int selectedTabPosition = this.mSelectedTab != null ? this.mSelectedTab.getPosition() : MODE_SCROLLABLE;
        removeTabViewAt(position);
        Tab removedTab = (Tab) this.mTabs.remove(position);
        if (removedTab != null) {
            removedTab.setPosition(-1);
        }
        int newTabCount = this.mTabs.size();
        for (int i = position; i < newTabCount; i += MODE_FIXED) {
            ((Tab) this.mTabs.get(i)).setPosition(i);
        }
        if (selectedTabPosition == position) {
            selectTab(this.mTabs.isEmpty() ? null : (Tab) this.mTabs.get(Math.max(MODE_SCROLLABLE, position - 1)));
        }
    }

    public void removeAllTabs() {
        this.mTabStrip.removeAllViews();
        Iterator<Tab> i = this.mTabs.iterator();
        while (i.hasNext()) {
            ((Tab) i.next()).setPosition(-1);
            i.remove();
        }
        this.mSelectedTab = null;
    }

    public void setTabMode(int mode) {
        if (mode != this.mMode) {
            this.mMode = mode;
            applyModeAndGravity();
        }
    }

    public int getTabMode() {
        return this.mMode;
    }

    public void setTabGravity(int gravity) {
        if (this.mTabGravity != gravity) {
            this.mTabGravity = gravity;
            applyModeAndGravity();
        }
    }

    public int getTabGravity() {
        return this.mTabGravity;
    }

    public void setTabTextColors(@Nullable ColorStateList textColor) {
        if (this.mTabTextColors != textColor) {
            this.mTabTextColors = textColor;
            updateAllTabs();
        }
    }

    @Nullable
    public ColorStateList getTabTextColors() {
        return this.mTabTextColors;
    }

    public void setTabTextColors(int normalColor, int selectedColor) {
        setTabTextColors(createColorStateList(normalColor, selectedColor));
    }

    public void setupWithViewPager(@NonNull ViewPager viewPager) {
        PagerAdapter adapter = viewPager.getAdapter();
        if (adapter == null) {
            throw new IllegalArgumentException("ViewPager does not have a PagerAdapter set");
        }
        setTabsFromPagerAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(this));
        setOnTabSelectedListener(new ViewPagerOnTabSelectedListener(viewPager));
        if (adapter.getCount() > 0) {
            int curItem = viewPager.getCurrentItem();
            if (getSelectedTabPosition() != curItem) {
                selectTab(getTabAt(curItem));
            }
        }
    }

    public void setTabsFromPagerAdapter(@NonNull PagerAdapter adapter) {
        removeAllTabs();
        int count = adapter.getCount();
        for (int i = MODE_SCROLLABLE; i < count; i += MODE_FIXED) {
            addTab(newTab().setText(adapter.getPageTitle(i)));
        }
    }

    private void updateAllTabs() {
        int z = this.mTabStrip.getChildCount();
        for (int i = MODE_SCROLLABLE; i < z; i += MODE_FIXED) {
            updateTab(i);
        }
    }

    private TabView createTabView(Tab tab) {
        TabView tabView = new TabView(getContext(), tab);
        tabView.setFocusable(true);
        if (this.mTabClickListener == null) {
            this.mTabClickListener = new OnClickListener() {
                public void onClick(View view) {
                    ((TabView) view).getTab().select();
                }
            };
        }
        tabView.setOnClickListener(this.mTabClickListener);
        return tabView;
    }

    private void configureTab(Tab tab, int position) {
        tab.setPosition(position);
        this.mTabs.add(position, tab);
        int count = this.mTabs.size();
        for (int i = position + MODE_FIXED; i < count; i += MODE_FIXED) {
            ((Tab) this.mTabs.get(i)).setPosition(i);
        }
    }

    private void updateTab(int position) {
        TabView view = (TabView) this.mTabStrip.getChildAt(position);
        if (view != null) {
            view.update();
        }
    }

    private void addTabView(Tab tab, boolean setSelected) {
        TabView tabView = createTabView(tab);
        this.mTabStrip.addView(tabView, createLayoutParamsForTabs());
        if (setSelected) {
            tabView.setSelected(true);
        }
    }

    private void addTabView(Tab tab, int position, boolean setSelected) {
        TabView tabView = createTabView(tab);
        this.mTabStrip.addView(tabView, position, createLayoutParamsForTabs());
        if (setSelected) {
            tabView.setSelected(true);
        }
    }

    private LayoutParams createLayoutParamsForTabs() {
        LayoutParams lp = new LayoutParams(-2, -1);
        updateTabViewLayoutParams(lp);
        return lp;
    }

    private void updateTabViewLayoutParams(LayoutParams lp) {
        if (this.mMode == MODE_FIXED && this.mTabGravity == 0) {
            lp.width = MODE_SCROLLABLE;
            lp.weight = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
            return;
        }
        lp.width = -2;
        lp.weight = 0.0f;
    }

    private int dpToPx(int dps) {
        return Math.round(getResources().getDisplayMetrics().density * ((float) dps));
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int idealHeight = (dpToPx(DEFAULT_HEIGHT) + getPaddingTop()) + getPaddingBottom();
        switch (MeasureSpec.getMode(heightMeasureSpec)) {
            case Action.UNDEFINED_DURATION /*-2147483648*/:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(idealHeight, MeasureSpec.getSize(heightMeasureSpec)), 1073741824);
                break;
            case MODE_SCROLLABLE /*0*/:
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(idealHeight, 1073741824);
                break;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.mMode == MODE_FIXED && getChildCount() == MODE_FIXED) {
            View child = getChildAt(MODE_SCROLLABLE);
            int width = getMeasuredWidth();
            if (child.getMeasuredWidth() > width) {
                child.measure(MeasureSpec.makeMeasureSpec(width, 1073741824), getChildMeasureSpec(heightMeasureSpec, getPaddingTop() + getPaddingBottom(), child.getLayoutParams().height));
            }
        }
        int maxTabWidth = this.mRequestedTabMaxWidth;
        int defaultTabMaxWidth = getMeasuredWidth() - dpToPx(TAB_MIN_WIDTH_MARGIN);
        if (maxTabWidth == 0 || maxTabWidth > defaultTabMaxWidth) {
            maxTabWidth = defaultTabMaxWidth;
        }
        if (this.mTabMaxWidth != maxTabWidth) {
            this.mTabMaxWidth = maxTabWidth;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void removeTabViewAt(int position) {
        this.mTabStrip.removeViewAt(position);
        requestLayout();
    }

    private void animateToTab(int newPosition) {
        if (newPosition != -1) {
            if (getWindowToken() == null || !ViewCompat.isLaidOut(this) || this.mTabStrip.childrenNeedLayout()) {
                setScrollPosition(newPosition, 0.0f, true);
                return;
            }
            int startScrollX = getScrollX();
            int targetScrollX = calculateScrollXForTab(newPosition, 0.0f);
            if (startScrollX != targetScrollX) {
                if (this.mScrollAnimator == null) {
                    this.mScrollAnimator = ViewUtils.createAnimator();
                    this.mScrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
                    this.mScrollAnimator.setDuration(ANIMATION_DURATION);
                    this.mScrollAnimator.setUpdateListener(new AnimatorUpdateListener() {
                        public void onAnimationUpdate(ValueAnimatorCompat animator) {
                            TabLayout.this.scrollTo(animator.getAnimatedIntValue(), TabLayout.MODE_SCROLLABLE);
                        }
                    });
                }
                this.mScrollAnimator.setIntValues(startScrollX, targetScrollX);
                this.mScrollAnimator.start();
            }
            this.mTabStrip.animateIndicatorToPosition(newPosition, ANIMATION_DURATION);
        }
    }

    private void setSelectedTabView(int position) {
        int tabCount = this.mTabStrip.getChildCount();
        if (position < tabCount && !this.mTabStrip.getChildAt(position).isSelected()) {
            int i = MODE_SCROLLABLE;
            while (i < tabCount) {
                this.mTabStrip.getChildAt(i).setSelected(i == position);
                i += MODE_FIXED;
            }
        }
    }

    void selectTab(Tab tab) {
        selectTab(tab, true);
    }

    void selectTab(Tab tab, boolean updateIndicator) {
        if (this.mSelectedTab != tab) {
            int newPosition;
            if (tab != null) {
                newPosition = tab.getPosition();
            } else {
                newPosition = -1;
            }
            setSelectedTabView(newPosition);
            if (updateIndicator) {
                if ((this.mSelectedTab == null || this.mSelectedTab.getPosition() == -1) && newPosition != -1) {
                    setScrollPosition(newPosition, 0.0f, true);
                } else {
                    animateToTab(newPosition);
                }
            }
            if (!(this.mSelectedTab == null || this.mOnTabSelectedListener == null)) {
                this.mOnTabSelectedListener.onTabUnselected(this.mSelectedTab);
            }
            this.mSelectedTab = tab;
            if (this.mSelectedTab != null && this.mOnTabSelectedListener != null) {
                this.mOnTabSelectedListener.onTabSelected(this.mSelectedTab);
            }
        } else if (this.mSelectedTab != null) {
            if (this.mOnTabSelectedListener != null) {
                this.mOnTabSelectedListener.onTabReselected(this.mSelectedTab);
            }
            animateToTab(tab.getPosition());
        }
    }

    private int calculateScrollXForTab(int position, float positionOffset) {
        int nextWidth = MODE_SCROLLABLE;
        if (this.mMode != 0) {
            return MODE_SCROLLABLE;
        }
        View selectedChild = this.mTabStrip.getChildAt(position);
        View nextChild = position + MODE_FIXED < this.mTabStrip.getChildCount() ? this.mTabStrip.getChildAt(position + MODE_FIXED) : null;
        int selectedWidth = selectedChild != null ? selectedChild.getWidth() : MODE_SCROLLABLE;
        if (nextChild != null) {
            nextWidth = nextChild.getWidth();
        }
        return ((selectedChild.getLeft() + ((int) ((((float) (selectedWidth + nextWidth)) * positionOffset) * 0.5f))) + (selectedChild.getWidth() / 2)) - (getWidth() / 2);
    }

    private void applyModeAndGravity() {
        int paddingStart = MODE_SCROLLABLE;
        if (this.mMode == 0) {
            paddingStart = Math.max(MODE_SCROLLABLE, this.mContentInsetStart - this.mTabPaddingStart);
        }
        ViewCompat.setPaddingRelative(this.mTabStrip, paddingStart, MODE_SCROLLABLE, MODE_SCROLLABLE, MODE_SCROLLABLE);
        switch (this.mMode) {
            case MODE_SCROLLABLE /*0*/:
                this.mTabStrip.setGravity(GravityCompat.START);
                break;
            case MODE_FIXED /*1*/:
                this.mTabStrip.setGravity(MODE_FIXED);
                break;
        }
        updateTabViewsLayoutParams();
    }

    private void updateTabViewsLayoutParams() {
        for (int i = MODE_SCROLLABLE; i < this.mTabStrip.getChildCount(); i += MODE_FIXED) {
            View child = this.mTabStrip.getChildAt(i);
            updateTabViewLayoutParams((LayoutParams) child.getLayoutParams());
            child.requestLayout();
        }
    }

    private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
        states = new int[2][];
        int[] colors = new int[]{SELECTED_STATE_SET, selectedColor};
        int i = MODE_SCROLLABLE + MODE_FIXED;
        states[i] = EMPTY_STATE_SET;
        colors[i] = defaultColor;
        i += MODE_FIXED;
        return new ColorStateList(states, colors);
    }

    private ColorStateList loadTextColorFromTextAppearance(int textAppearanceResId) {
        TypedArray a = getContext().obtainStyledAttributes(textAppearanceResId, R.styleable.TextAppearance);
        try {
            ColorStateList colorStateList = a.getColorStateList(R.styleable.TextAppearance_android_textColor);
            return colorStateList;
        } finally {
            a.recycle();
        }
    }
}
