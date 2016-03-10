package com.mobcrush.mobcrush;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.DataModel;
import com.mobcrush.mobcrush.datamodel.EntityType;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.logic.UserLogicType;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.ScrollTabHolder;
import com.mobcrush.mobcrush.ui.ScrollTabHolderFragment;
import com.mobcrush.mobcrush.ui.SlidingTabLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.text.NumberFormat;
import java.util.Locale;

public class ProfileFragment extends EditProfileFragment implements ScrollTabHolder, OnClickListener, OnRefreshListener {
    private static final String EXTRA_SHOW_TOOLBAR = "extra_show_toolbar";
    private static final String LANDING_TEXT_TYPEFACE = "KlavikaWebDisplayExtraLight.ttf";
    private static final float PHOTO_ASPECT_RATIO = 1.0f;
    private static final int REQUEST_LOGIN = 3;
    private static final int REQUEST_SIGNUP = 2;
    private static final String TAG = "ProfileFragment";
    private static final String USERNAME_TYPEFACE = "Klavika-Light.ttf";
    private TextView mAbout;
    private TextView mBroadcasatsCount;
    private BroadcastsFragment mBroadcastFragment;
    private View mBroadcastsCountProgress;
    private ViewGroup mDetailsContainer;
    private TextView mFollow;
    private TextView mFollowersCount;
    private View mFollowersCountProgress;
    private UsersFragment mFollowersFragment;
    private TextView mFollowingCount;
    private View mFollowingCountProgress;
    private UsersFragment mFollowingFragment;
    private OnGlobalLayoutListener mGlobalLayoutListener = new OnGlobalLayoutListener() {
        public void onGlobalLayout() {
            ProfileFragment.this.recomputePhotoAndScrollingMetrics();
        }
    };
    private View mHeaderBox;
    private Drawable mHeaderBoxDrawable;
    private boolean mIsFollower;
    private boolean mIsUserDataUpdating = false;
    private View mLandingLayout;
    private float mMaxHeaderElevation;
    private int mMinHeight = -1;
    private int mMinimalHeaderHeight;
    private View mMoreBtn;
    private int mPhotoHeightPixels;
    private View mPhotoViewContainer;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private View mShutterView;
    private boolean mSkipUpdate;
    private SlidingTabLayout mSlidingTabLayout;
    private boolean mStandaloneVariant;
    private TextView mSubtitle;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mTitle;
    private int mTitleTextColor;
    private Toolbar mToolbar;
    private int mToolbarHeight;
    private User mUser;
    private boolean mUserHasPhoto = false;
    private TextView mUsername;
    private ViewPager mViewPager;
    private TextView mViewsCount;
    private ErrorListener onErrorFollowing = new ErrorListener() {
        public void onErrorResponse(VolleyError error) {
            ProfileFragment.this.configFollowView();
        }
    };
    private Listener<Boolean> onResponseFollowing = new Listener<Boolean>() {
        public void onResponse(Boolean response) {
            ProfileFragment.this.mIsFollower = response != null ? response.booleanValue() : ProfileFragment.this.mIsFollower;
            if (ProfileFragment.this.isAdded()) {
                ProfileFragment.this.configFollowView();
            }
        }
    };
    private Listener<User> onResponseUser = new Listener<User>() {
        public void onResponse(User response) {
            ProfileFragment.this.mIsUserDataUpdating = false;
            ProfileFragment.this.mUser = response;
            if (ProfileFragment.this.isAdded()) {
                ProfileFragment.this.updateUserInfo(ProfileFragment.this.mUser, true, true);
            }
        }
    };

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ScrollTabHolder mListener;
        private int mPagesCount;
        private SparseArrayCompat<ScrollTabHolder> mScrollTabHolders = new SparseArrayCompat();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setTabHolderScrollingContent(ScrollTabHolder listener) {
            this.mListener = listener;
        }

        public Fragment getItem(int position) {
            int i;
            ScrollTabHolderFragment fragment;
            int i2 = 1;
            boolean shouldBeShown = ProfileFragment.this.isBroadcastsShouldBeShown(ProfileFragment.this.mUser);
            if (shouldBeShown) {
                i = 0;
            } else {
                i = 1;
            }
            DataModel access$300;
            switch (i + position) {
                case ResponseParser.ResponseActionDiscard /*0*/:
                    Log.w(ProfileFragment.TAG, "getItem " + position + ". mBroadcastFragment: " + ProfileFragment.this.mBroadcastFragment + "; mUser: " + ProfileFragment.this.mUser);
                    if (ProfileFragment.this.mBroadcastFragment == null) {
                        ProfileFragment.this.mBroadcastFragment = BroadcastsFragment.newInstance(ProfileFragment.this.mUser != null ? ProfileFragment.this.mUser : null, 0);
                    }
                    fragment = ProfileFragment.this.mBroadcastFragment;
                    if (ProfileFragment.this.mBroadcastFragment != null) {
                        ProfileFragment.this.mBroadcastFragment.setHeaderHeight(ProfileFragment.this.mPhotoHeightPixels, ProfileFragment.this.mMinHeight);
                        break;
                    }
                    break;
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    Log.w(ProfileFragment.TAG, "getItem " + position + ". mFollowersFragment: " + ProfileFragment.this.mFollowersFragment + "; mUser: " + ProfileFragment.this.mUser);
                    if (ProfileFragment.this.mFollowersFragment == null) {
                        ProfileFragment profileFragment = ProfileFragment.this;
                        access$300 = ProfileFragment.this.mUser != null ? ProfileFragment.this.mUser : PreferenceUtility.getUser();
                        UserLogicType userLogicType = UserLogicType.Followers;
                        if (!shouldBeShown) {
                            i2 = 0;
                        }
                        profileFragment.mFollowersFragment = UsersFragment.newInstance(access$300, userLogicType, i2);
                    }
                    fragment = ProfileFragment.this.mFollowersFragment;
                    if (ProfileFragment.this.mFollowersFragment != null) {
                        ProfileFragment.this.mFollowersFragment.setHeaderHeight(ProfileFragment.this.mPhotoHeightPixels, ProfileFragment.this.mMinHeight);
                        break;
                    }
                    break;
                default:
                    Log.w(ProfileFragment.TAG, "getItem " + position + ". mFollowingFragment: " + ProfileFragment.this.mFollowingFragment + "; mUser: " + ProfileFragment.this.mUser);
                    if (ProfileFragment.this.mFollowingFragment == null) {
                        ProfileFragment profileFragment2 = ProfileFragment.this;
                        access$300 = ProfileFragment.this.mUser != null ? ProfileFragment.this.mUser : PreferenceUtility.getUser();
                        UserLogicType userLogicType2 = UserLogicType.Following;
                        if (shouldBeShown) {
                            i2 = ProfileFragment.REQUEST_SIGNUP;
                        }
                        profileFragment2.mFollowingFragment = UsersFragment.newInstance(access$300, userLogicType2, i2);
                    }
                    fragment = ProfileFragment.this.mFollowingFragment;
                    if (ProfileFragment.this.mFollowingFragment != null) {
                        ProfileFragment.this.mFollowingFragment.setHeaderHeight(ProfileFragment.this.mPhotoHeightPixels, ProfileFragment.this.mMinHeight);
                        break;
                    }
                    break;
            }
            if (fragment == null) {
                throw new IllegalStateException("Fragment cannot be null");
            }
            this.mScrollTabHolders.put(position, fragment);
            if (this.mListener != null) {
                fragment.setScrollTabHolder(this.mListener);
            }
            return fragment;
        }

        public int getCount() {
            int i = 1;
            if (this.mPagesCount == 0) {
                int i2;
                int i3 = this.mPagesCount;
                if (ProfileFragment.this.isBroadcastsShouldBeShown(ProfileFragment.this.mUser)) {
                    i2 = 1;
                } else {
                    i2 = 0;
                }
                this.mPagesCount = i2 + i3;
                i3 = this.mPagesCount;
                if (ProfileFragment.this.mUser == null || ProfileFragment.this.mUser.followerCount == null) {
                    i2 = 0;
                } else {
                    i2 = 1;
                }
                this.mPagesCount = i2 + i3;
                i2 = this.mPagesCount;
                if (ProfileFragment.this.mUser == null || ProfileFragment.this.mUser.followingCount == null) {
                    i = 0;
                }
                this.mPagesCount = i2 + i;
            }
            return this.mPagesCount;
        }

        public int getItemPosition(Object object) {
            return -2;
        }

        public long getItemId(int position) {
            return ProfileFragment.this.mUser != null ? (long) (ProfileFragment.this.mUser._id.hashCode() + position) : (long) position;
        }

        public CharSequence getPageTitle(int position) {
            switch (position) {
                case ResponseParser.ResponseActionDiscard /*0*/:
                    return BuildConfig.FLAVOR;
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    return BuildConfig.FLAVOR;
                case ProfileFragment.REQUEST_SIGNUP /*2*/:
                    return BuildConfig.FLAVOR;
                default:
                    return null;
            }
        }

        public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
            return this.mScrollTabHolders;
        }

        public void notifyDataSetChanged() {
            this.mPagesCount = 0;
            super.notifyDataSetChanged();
        }
    }

    public static ProfileFragment newInstance(User user) {
        return newInstance(user, false);
    }

    public static ProfileFragment newInstance(User user, boolean showToolbar) {
        ProfileFragment fragment = new ProfileFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_SHOW_TOOLBAR, showToolbar);
        args.putString(Constants.EXTRA_USER, user.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        boolean z;
        super.onCreate(savedInstanceState);
        try {
            String user = getArguments().getString(Constants.EXTRA_USER, null);
            if (!TextUtils.isEmpty(user)) {
                this.mUser = (User) new Gson().fromJson(user, User.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mStandaloneVariant = getArguments().getBoolean(EXTRA_SHOW_TOOLBAR, false);
        this.mTitleTextColor = getResources().getColor(R.color.yellow);
        if (this.mUser != null) {
            z = true;
        } else {
            z = false;
        }
        setHasOptionsMenu(z);
    }

    @SuppressLint({"ResourceAsColor"})
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;
        Log.d(TAG, "onCreateView");
        try {
            root = inflater.inflate(R.layout.fragment_profile, container, false);
        } catch (Throwable ex) {
            ex.printStackTrace();
            Crashlytics.logException(ex);
            return new View(container.getContext());
        }
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.refreshLayout);
        this.mSwipeRefreshLayout.setOnRefreshListener(this);
        this.mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.yellow);
        this.mSwipeRefreshLayout.setColorSchemeResources(R.color.dark);
        this.mToolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        this.mTitle = (TextView) this.mToolbar.findViewById(R.id.title);
        this.mDetailsContainer = (ViewGroup) root.findViewById(R.id.details_container);
        this.mPhotoViewContainer = root.findViewById(R.id.profile_photo_container);
        ViewCompat.setElevation(this.mPhotoViewContainer, (float) getResources().getDimensionPixelSize(R.dimen.headers_elevation));
        this.mProfileLogo = (ImageView) root.findViewById(R.id.profile_photo);
        this.mShutterView = root.findViewById(R.id.shutter_view);
        this.mHeaderBox = root.findViewById(R.id.headers);
        this.mHeaderBoxDrawable = this.mHeaderBox.getBackground();
        ViewTreeObserver vto = root.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(this.mGlobalLayoutListener);
        }
        this.mMaxHeaderElevation = 20.0f;
        this.mSlidingTabLayout = (SlidingTabLayout) root.findViewById(R.id.sliding_tabs);
        this.mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.yellow));
        this.mViewPager = (ViewPager) root.findViewById(R.id.viewpager);
        this.mViewPager.setOffscreenPageLimit(REQUEST_SIGNUP);
        if (this.mSectionsPagerAdapter == null) {
            this.mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        }
        this.mSectionsPagerAdapter.setTabHolderScrollingContent(this);
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        this.mSlidingTabLayout.setViewPager(this.mViewPager);
        this.mSlidingTabLayout.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                ScrollTabHolder currentHolder = (ScrollTabHolder) ProfileFragment.this.mSectionsPagerAdapter.getScrollTabHolders().valueAt(position);
                if (ProfileFragment.this.mPhotoViewContainer != null) {
                    if (currentHolder != null) {
                        Log.d(ProfileFragment.TAG, "adjustScroll: " + (((float) ProfileFragment.this.mHeaderBox.getHeight()) + ProfileFragment.this.mHeaderBox.getTranslationY()));
                        currentHolder.adjustScroll((int) (((float) ProfileFragment.this.mPhotoViewContainer.getHeight()) + ProfileFragment.this.mPhotoViewContainer.getTranslationY()));
                    }
                    ProfileFragment.this.mSwipeRefreshLayout.setEnabled(ProfileFragment.this.mPhotoViewContainer.getTranslationY() == 0.0f);
                }
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        this.mViewPager.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                ProfileFragment.this.mSwipeRefreshLayout.setEnabled(false);
                switch (event.getAction()) {
                    case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    case ProfileFragment.REQUEST_LOGIN /*3*/:
                        ProfileFragment.this.mSwipeRefreshLayout.setEnabled(true);
                        break;
                }
                return false;
            }
        });
        this.mUsername = (TextView) root.findViewById(R.id.profile_name);
        this.mUsername.setTypeface(UIUtils.getTypeface(getActivity(), USERNAME_TYPEFACE));
        this.mSubtitle = (TextView) root.findViewById(R.id.profile_subtitle);
        this.mSubtitle.setTypeface(UIUtils.getTypeface(getActivity(), USERNAME_TYPEFACE));
        this.mViewsCount = (TextView) root.findViewById(R.id.profile_views_count);
        this.mViewsCount.setTypeface(UIUtils.getTypeface(getActivity(), USERNAME_TYPEFACE));
        this.mBroadcastsCountProgress = root.findViewById(R.id.broadcast_progress);
        UIUtils.colorizeProgress((ProgressBar) this.mBroadcastsCountProgress, getResources().getColor(R.color.yellow));
        this.mBroadcasatsCount = (TextView) root.findViewById(R.id.broadcasts_count);
        this.mFollowingCount = (TextView) root.findViewById(R.id.following_count);
        this.mFollowingCountProgress = root.findViewById(R.id.following_progress);
        UIUtils.colorizeProgress((ProgressBar) this.mFollowingCountProgress, getResources().getColor(R.color.yellow));
        this.mFollowersCount = (TextView) root.findViewById(R.id.followers_count);
        this.mFollowersCountProgress = root.findViewById(R.id.followers_progress);
        UIUtils.colorizeProgress((ProgressBar) this.mFollowersCountProgress, getResources().getColor(R.color.yellow));
        this.mMoreBtn = root.findViewById(R.id.more_button);
        this.mMoreBtn.setOnClickListener(this);
        View view = this.mMoreBtn;
        int i = ((getActivity() instanceof MainActivity) && PreferenceUtility.getUser().equals(this.mUser)) ? 0 : 8;
        view.setVisibility(i);
        this.mLandingLayout = root.findViewById(R.id.landing_layout);
        ((TextView) root.findViewById(R.id.live_text)).setTypeface(UIUtils.getTypeface(getActivity(), USERNAME_TYPEFACE));
        ((TextView) root.findViewById(R.id.landing_text)).setTypeface(UIUtils.getTypeface(getActivity(), LANDING_TEXT_TYPEFACE));
        root.findViewById(R.id.broadcasts_layout).setOnClickListener(this);
        root.findViewById(R.id.followers_layout).setOnClickListener(this);
        root.findViewById(R.id.following_layout).setOnClickListener(this);
        root.findViewById(R.id.signup_button).setOnClickListener(this);
        root.findViewById(R.id.login_button).setOnClickListener(this);
        configControls();
        updateData();
        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_CHANNEL);
        return root;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getChildFragmentManager().getFragments() != null) {
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            for (Fragment f : getChildFragmentManager().getFragments()) {
                if (f != null) {
                    fragmentTransaction.remove(f);
                }
            }
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: " + isVisibleToUser);
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: " + isAdded());
        if (this.mSkipUpdate) {
            this.mSkipUpdate = false;
        }
    }

    public void onClick(View view) {
        int i = 0;
        ViewPager viewPager;
        switch (view.getId()) {
            case R.id.login_button:
                startActivityForResult(LoginActivity.getIntent(getActivity(), false, false, true), REQUEST_LOGIN);
                return;
            case R.id.more_button:
                if (PreferenceUtility.isEmailVerified()) {
                    PopupMenu popup = new PopupMenu(getActivity(), view);
                    popup.getMenuInflater().inflate(R.menu.menu_profile_owner, popup.getMenu());
                    popup.setOnMenuItemClickListener(this);
                    popup.show();
                    return;
                }
                startActivity(EmailVerificationRequestActivity.getIntent(getActivity()));
                return;
            case R.id.broadcasts_layout:
                this.mViewPager.setCurrentItem(0);
                return;
            case R.id.followers_layout:
                viewPager = this.mViewPager;
                if (!isBroadcastsShouldBeShown(this.mUser)) {
                    i = 1;
                }
                viewPager.setCurrentItem(1 - i);
                return;
            case R.id.following_layout:
                viewPager = this.mViewPager;
                if (!isBroadcastsShouldBeShown(this.mUser)) {
                    i = 1;
                }
                viewPager.setCurrentItem(2 - i);
                return;
            case R.id.signup_button:
                startActivityForResult(LoginActivity.getIntent(getActivity(), false, true, true), REQUEST_LOGIN);
                return;
            default:
                return;
        }
    }

    public void adjustScroll(int scrollHeight) {
    }

    public void onScroll(View view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
        if (this.mViewPager.getCurrentItem() == pagePosition) {
            int scrollY = getScrollY(view, firstVisibleItem);
            this.mSwipeRefreshLayout.setEnabled(scrollY == 0);
            this.mPhotoViewContainer.setTranslationY((float) Math.max(-scrollY, this.mMinimalHeaderHeight));
            float ratio = Math.max(Math.min(this.mPhotoViewContainer.getTranslationY() / ((float) this.mMinimalHeaderHeight), PHOTO_ASPECT_RATIO), 0.0f);
            int newTextAlpha = (int) (255.0f * ratio);
            int newAlpha = ((int) (170.0f * ratio)) + 85;
            int color = this.mUsername.getCurrentTextColor();
            int usernameAlpha = newTextAlpha >= 170 ? 0 : 255 - ((newTextAlpha * REQUEST_LOGIN) / REQUEST_SIGNUP);
            this.mUsername.setTextColor(Color.argb(usernameAlpha, Color.red(color), Color.green(color), Color.blue(color)));
            this.mViewsCount.setTextColor(Color.argb(usernameAlpha, Color.red(color), Color.green(color), Color.blue(color)));
            this.mShutterView.getBackground().setAlpha(newAlpha);
            newAlpha = newAlpha == SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT ? SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT : 0;
            Drawable drawable = this.mHeaderBoxDrawable;
            int i = (this.mToolbar == null || this.mUserHasPhoto) ? newAlpha : SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            drawable.setAlpha(i);
            if (this.mToolbar != null && this.mUserHasPhoto) {
                this.mToolbar.getBackground().setAlpha(newAlpha);
                if (newTextAlpha < 170) {
                    newTextAlpha = 0;
                }
                this.mTitle.setTextColor(Color.argb(newTextAlpha, Color.red(this.mTitleTextColor), Color.green(this.mTitleTextColor), Color.blue(this.mTitleTextColor)));
            }
        }
    }

    public int getScrollY(View view, int firstVisiblePosition) {
        if (view == null) {
            return 0;
        }
        int top = view.getTop();
        int headerHeight = 0;
        if (firstVisiblePosition >= 1) {
            headerHeight = this.mPhotoHeightPixels;
        }
        return ((-top) + (view.getHeight() * firstVisiblePosition)) + headerHeight;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_profile, menu);
        if (this.mToolbar != null) {
            this.mFollow = (TextView) this.mToolbar.findViewById(R.id.action_follow);
            this.mFollow.setTypeface(UIUtils.getTypeface(getActivity(), Constants.FOLLOW_TYPEFACE));
            this.mFollow.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    Network.follow(ProfileFragment.this.getActivity(), !ProfileFragment.this.mIsFollower, EntityType.user, ProfileFragment.this.mUser._id, ProfileFragment.this.onResponseFollowing, ProfileFragment.this.onErrorFollowing);
                }
            });
            this.mAbout = (TextView) this.mToolbar.findViewById(R.id.action_about);
            this.mAbout.setVisibility(this.mUser != null ? 0 : 8);
            this.mAbout.setTypeface(UIUtils.getTypeface(getActivity(), Constants.FOLLOW_TYPEFACE));
            this.mAbout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ProfileFragment.this.startActivity(ProfileAboutActivity.getIntent(ProfileFragment.this.getContext(), ProfileFragment.this.mUser, false));
                }
            });
            Network.checkIfFollower(getActivity(), EntityType.user, this.mUser._id, this.onResponseFollowing, this.onErrorFollowing);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1) {
            switch (requestCode) {
                case REQUEST_LOGIN /*3*/:
                    this.mUser = PreferenceUtility.getUser();
                    updateData();
                    return;
                default:
                    return;
            }
        }
    }

    private void configFollowView() {
        int i = 0;
        if (isAdded() && this.mFollow != null) {
            this.mFollow.setActivated(!this.mIsFollower);
            this.mFollow.setText(!this.mIsFollower ? R.string.FOLLOW : R.string.UNFOLLOW);
            this.mFollow.setTextColor(getResources().getColor(this.mIsFollower ? R.color.follow_toolbar_inactive : R.color.follow_toolbar_active));
            TextView textView = this.mFollow;
            if (this.mUser == null || this.mUser._id.equals(PreferenceUtility.getUser()._id)) {
                i = 8;
            }
            textView.setVisibility(i);
        }
    }

    private void recomputePhotoAndScrollingMetrics() {
        int height;
        boolean needToResetFragments;
        if (this.mToolbar != null) {
            height = this.mToolbar.getHeight();
        } else {
            height = 0;
        }
        this.mToolbarHeight = height;
        if (this.mPhotoHeightPixels == 0) {
            needToResetFragments = true;
        } else {
            needToResetFragments = false;
        }
        this.mPhotoHeightPixels = (int) (((float) this.mProfileLogo.getWidth()) / PHOTO_ASPECT_RATIO);
        this.mMinimalHeaderHeight = ((-this.mPhotoHeightPixels) + this.mHeaderBox.getHeight()) + this.mToolbarHeight;
        LayoutParams lp = this.mPhotoViewContainer.getLayoutParams();
        if (lp.height != this.mPhotoHeightPixels) {
            lp.height = this.mPhotoHeightPixels;
            this.mPhotoViewContainer.setLayoutParams(lp);
        }
        lp = this.mShutterView.getLayoutParams();
        if (lp.height != this.mPhotoHeightPixels) {
            lp.height = this.mPhotoHeightPixels;
            this.mShutterView.setLayoutParams(lp);
        }
        if (needToResetFragments && this.mPhotoHeightPixels != 0) {
            this.mMinHeight = this.mHeaderBox.getHeight() + this.mToolbarHeight;
            if (this.mBroadcastFragment != null) {
                this.mBroadcastFragment.setHeaderHeight(this.mPhotoHeightPixels, this.mMinHeight);
            }
            if (this.mFollowingFragment != null) {
                this.mFollowingFragment.setHeaderHeight(this.mPhotoHeightPixels, this.mMinHeight);
            }
            if (this.mFollowersFragment != null) {
                this.mFollowersFragment.setHeaderHeight(this.mPhotoHeightPixels, this.mMinHeight);
            }
            onScroll(null, 0, 0, 0, 0);
        }
    }

    private void configControls() {
        if (!this.mStandaloneVariant) {
            this.mToolbar = null;
        }
    }

    public void onDestroy() {
        if (this.mToolbar != null) {
            this.mToolbar.getBackground().setAlpha(SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
            this.mTitle.setTextColor(Color.argb(SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT, Color.red(this.mTitleTextColor), Color.green(this.mTitleTextColor), Color.blue(this.mTitleTextColor)));
        }
        super.onDestroy();
    }

    public User getUser() {
        return this.mUser;
    }

    public void refreshProfile() {
        Log.d(TAG, "refreshProfile");
        ScrollTabHolder currentHolder = (ScrollTabHolder) this.mSectionsPagerAdapter.getScrollTabHolders().valueAt(this.mViewPager.getCurrentItem());
        try {
            updateData();
            onScroll(null, 0, 0, 0, this.mViewPager.getCurrentItem());
            currentHolder.adjustScroll(this.mPhotoViewContainer.getHeight());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateData() {
        Log.d(TAG, "updateData. isResumed: " + isResumed());
        if (this.mUser == null || (this.mUser.isGuest(getActivity()) && PreferenceUtility.getUser().equals(this.mUser))) {
            if (this.mToolbar == null) {
                this.mUser = PreferenceUtility.getUser();
            }
            updateUserInfo(this.mUser, true, true);
        } else if (!this.mIsUserDataUpdating) {
            updateUserInfo(this.mUser, true, false);
            this.mIsUserDataUpdating = true;
            Network.getUserProfile(getActivity(), this.mUser._id, this.onResponseUser, new ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    ProfileFragment.this.mIsUserDataUpdating = false;
                }
            });
        }
    }

    public void updateUserInfo(User user, boolean updateUserPhoto, boolean forceLoadData) {
        int i = 0;
        Log.d(TAG, "updateUserInfo: " + user);
        if (this.mSwipeRefreshLayout != null && isAdded()) {
            this.mSwipeRefreshLayout.setRefreshing(false);
        }
        if (!(user == null || this.mUser == null || TextUtils.equals(user.profileLogo, this.mUser.profileLogo)) || (user != null && this.mUser == null)) {
            i = 1;
        }
        updateUserPhoto |= i;
        this.mUser = user;
        if (user == null || user.isGuest(getActivity())) {
            this.mBroadcastFragment = null;
            this.mFollowersFragment = null;
            this.mFollowingFragment = null;
        } else if (forceLoadData || ((this.mBroadcastFragment == null && this.mFollowersFragment == null && this.mFollowingFragment == null) || (isBroadcastsShouldBeShown(user) && this.mBroadcastFragment == null))) {
            initFragments();
            this.mSectionsPagerAdapter.notifyDataSetChanged();
            this.mSlidingTabLayout.setViewPager(this.mViewPager);
        }
        try {
            updateUserBaseInfo(this.mUser, updateUserPhoto);
            if (!this.mStandaloneVariant) {
                if (this.mBroadcastFragment != null) {
                    this.mBroadcastFragment.setUser(this.mUser, forceLoadData);
                }
                if (this.mFollowingFragment != null) {
                    this.mFollowingFragment.setUser(this.mUser, forceLoadData);
                }
                if (this.mFollowersFragment != null) {
                    this.mFollowersFragment.setUser(this.mUser, forceLoadData);
                }
            }
            configControls();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void updateUserBaseInfo(User user, boolean updatePhoto) {
        boolean z = true;
        int i = 8;
        synchronized (this) {
            Log.d(TAG, "updateUserBaseInfo");
            this.mUser = user;
            if (isAdded()) {
                View findViewById;
                int i2;
                if (this.mToolbar != null) {
                    this.mTitle.setText(this.mUser.username);
                }
                boolean isGuest = (this.mUser == null || this.mUser.isGuest(getActivity())) && !this.mStandaloneVariant;
                if (isGuest) {
                    this.mPhotoHeightPixels = 0;
                }
                if (getView() != null) {
                    findViewById = getView().findViewById(R.id.broadcasts_layout);
                    if (isBroadcastsShouldBeShown(user)) {
                        i2 = 0;
                    } else {
                        i2 = 8;
                    }
                    findViewById.setVisibility(i2);
                }
                findViewById = this.mLandingLayout;
                if (isGuest) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                findViewById.setVisibility(i2);
                ViewPager viewPager = this.mViewPager;
                if (isGuest) {
                    i2 = 8;
                } else {
                    i2 = 0;
                }
                viewPager.setVisibility(i2);
                findViewById = this.mHeaderBox;
                if (isGuest) {
                    i2 = 8;
                } else {
                    i2 = 0;
                }
                findViewById.setVisibility(i2);
                findViewById = this.mPhotoViewContainer;
                if (isGuest) {
                    i2 = 8;
                } else {
                    i2 = 0;
                }
                findViewById.setVisibility(i2);
                SlidingTabLayout slidingTabLayout = this.mSlidingTabLayout;
                if (!isGuest) {
                    i = 0;
                }
                slidingTabLayout.setVisibility(i);
                this.mUsername.setText(!isGuest ? this.mUser.username : MainApplication.getRString(R.string.guest, new Object[0]));
                this.mSubtitle.setText(this.mUser.subtitle);
                try {
                    TextView textView = this.mViewsCount;
                    CharSequence charSequence = (this.mUser.viewCount == null || this.mUser.viewCount.intValue() <= 0) ? BuildConfig.FLAVOR : NumberFormat.getInstance(Locale.getDefault()).format(this.mUser.viewCount) + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + MainApplication.getRString(R.string.Views, new Object[0]).toUpperCase();
                    textView.setText(charSequence);
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
                if (this.mUser.broadcastCount == null || this.mUser.getBroadcastCount() == 0) {
                    this.mBroadcasatsCount.setVisibility(8);
                    this.mBroadcastsCountProgress.setVisibility(0);
                } else {
                    this.mBroadcasatsCount.setText(String.valueOf(this.mUser.getBroadcastCount()));
                    this.mBroadcasatsCount.setVisibility(0);
                    this.mBroadcastsCountProgress.setVisibility(8);
                }
                if (this.mUser.followingCount != null) {
                    this.mFollowingCount.setText(String.valueOf(this.mUser.followingCount));
                    this.mFollowingCount.setVisibility(0);
                    this.mFollowingCountProgress.setVisibility(8);
                } else {
                    this.mFollowingCount.setVisibility(8);
                    this.mFollowingCountProgress.setVisibility(0);
                }
                if (this.mUser.followerCount != null) {
                    this.mFollowersCount.setText(String.valueOf(this.mUser.followerCount));
                    this.mFollowersCount.setVisibility(0);
                    this.mFollowersCountProgress.setVisibility(8);
                } else {
                    this.mFollowersCount.setVisibility(8);
                    this.mFollowersCountProgress.setVisibility(0);
                }
                if (this.mUser == null || TextUtils.isEmpty(this.mUser.profileLogo)) {
                    z = false;
                }
                this.mUserHasPhoto = z;
                if (updatePhoto) {
                    this.mProfileLogo.setImageResource(R.drawable.default_profile_pic);
                    if (this.mUserHasPhoto) {
                        ImageLoader.getInstance().displayImage(this.mUser.profileLogo, this.mProfileLogo, new Builder().cacheOnDisk(true).cacheInMemory(true).build());
                    }
                }
            }
        }
    }

    public void updateUserPhoto(String photoPath) {
        Log.i(TAG, "updateUserPhoto to: " + photoPath);
        this.mSkipUpdate = true;
        if (!(photoPath == null || this.mUser == null)) {
            this.mUser.profileLogo = photoPath;
        }
        if (!isAdded() || photoPath == null) {
            Log.i(TAG, "updateUserPhoto skipped: " + isAdded() + "; " + photoPath);
            return;
        }
        try {
            this.mProfileLogo.setImageBitmap(BitmapFactory.decodeFile(photoPath));
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public void onRefresh() {
        updateData();
    }

    private boolean isBroadcastsShouldBeShown(User user) {
        if (user == null) {
            user = this.mUser;
        }
        return user != null && user.getBroadcastCount() > 0;
    }

    private void initFragments() {
        BroadcastsFragment broadcastsFragment = null;
        int i = 1;
        int i2 = 0;
        try {
            if (!(this.mBroadcastFragment == null && this.mFollowersFragment == null && this.mFollowingFragment == null)) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                if (this.mBroadcastFragment != null) {
                    transaction.remove(this.mBroadcastFragment);
                }
                if (this.mFollowersFragment != null) {
                    transaction.remove(this.mFollowersFragment);
                }
                if (this.mFollowingFragment != null) {
                    transaction.remove(this.mFollowingFragment);
                }
                transaction.commitAllowingStateLoss();
                getChildFragmentManager().executePendingTransactions();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        boolean shouldBeShown = isBroadcastsShouldBeShown(this.mUser);
        if (shouldBeShown) {
            User user;
            if (this.mUser != null) {
                user = this.mUser;
            }
            broadcastsFragment = BroadcastsFragment.newInstance(user, 0);
        }
        this.mBroadcastFragment = broadcastsFragment;
        DataModel user2 = this.mUser != null ? this.mUser : PreferenceUtility.getUser();
        UserLogicType userLogicType = UserLogicType.Followers;
        if (shouldBeShown) {
            i2 = 1;
        }
        this.mFollowersFragment = UsersFragment.newInstance(user2, userLogicType, i2);
        user2 = this.mUser != null ? this.mUser : PreferenceUtility.getUser();
        UserLogicType userLogicType2 = UserLogicType.Following;
        if (shouldBeShown) {
            i = REQUEST_SIGNUP;
        }
        this.mFollowingFragment = UsersFragment.newInstance(user2, userLogicType2, i);
    }
}
