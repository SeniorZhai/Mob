package com.mobcrush.mobcrush;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.logic.BroadcastLogicType;
import com.mobcrush.mobcrush.ui.ScrollTabHolder;
import com.mobcrush.mobcrush.ui.ScrollTabHolderFragment;
import com.mobcrush.mobcrush.ui.SlidingTabLayout;
import io.fabric.sdk.android.BuildConfig;
import java.util.List;

public class WatchesFragment extends Fragment implements OnClickListener, OnRefreshListener {
    private static final boolean DEBUG = false;
    private static final int POSITION_NEW = 0;
    private static final int POSITION_POPULAR = 1;
    private static final String TAG = "WatchesFragment";
    private BroadcastsFragment mNewBroadcastFragment;
    private BroadcastsFragment mPopularBroadcastFragment;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private boolean mShowNewBroadcast = true;
    private SlidingTabLayout mSlidingTabLayout;
    private TextView mTextViewNew;
    private TextView mTextViewPopular;
    private ViewPager mViewPager;

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private ScrollTabHolder mListener;
        private SparseArrayCompat<ScrollTabHolder> mScrollTabHolders = new SparseArrayCompat();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setTabHolderScrollingContent(ScrollTabHolder listener) {
            this.mListener = listener;
        }

        public Fragment getItem(int position) {
            ScrollTabHolderFragment fragment = null;
            switch (position) {
                case WatchesFragment.POSITION_NEW /*0*/:
                    fragment = WatchesFragment.this.mNewBroadcastFragment = BroadcastsFragment.newInstance(BroadcastLogicType.New, null, position);
                    break;
                case WatchesFragment.POSITION_POPULAR /*1*/:
                    fragment = WatchesFragment.this.mPopularBroadcastFragment = BroadcastsFragment.newInstance(BroadcastLogicType.Popular, null, position);
                    break;
            }
            this.mScrollTabHolders.put(position, fragment);
            if (!(this.mListener == null || fragment == null)) {
                fragment.setScrollTabHolder(this.mListener);
            }
            return fragment;
        }

        public int getItemPosition(Object object) {
            if (WatchesFragment.this.mNewBroadcastFragment == null || WatchesFragment.this.mPopularBroadcastFragment == null) {
                return -2;
            }
            return super.getItemPosition(object);
        }

        public int getCount() {
            return 2;
        }

        public CharSequence getPageTitle(int position) {
            int resId = WatchesFragment.POSITION_NEW;
            switch (position) {
                case WatchesFragment.POSITION_NEW /*0*/:
                    resId = R.string.type_new;
                    break;
                case WatchesFragment.POSITION_POPULAR /*1*/:
                    resId = R.string.type_popular;
                    break;
            }
            if (resId > 0) {
                return WatchesFragment.this.getString(resId);
            }
            return BuildConfig.FLAVOR;
        }

        public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
            return this.mScrollTabHolders;
        }
    }

    public static WatchesFragment newInstance(boolean switchToPopular) {
        WatchesFragment fragment = new WatchesFragment();
        Bundle args = new Bundle();
        if (switchToPopular) {
            args.putBoolean(Constants.EXTRA_SWITCH_TO_POPULAR, true);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
        }
    }

    @SuppressLint({"ResourceAsColor"})
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watches, container, DEBUG);
        ViewCompat.setElevation(view.findViewById(R.id.tabs_layout), (float) getResources().getDimensionPixelSize(R.dimen.headers_elevation));
        this.mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        SlidingTabLayout slidingTabLayout = this.mSlidingTabLayout;
        int[] iArr = new int[POSITION_POPULAR];
        iArr[POSITION_NEW] = getResources().getColor(R.color.yellow);
        slidingTabLayout.setSelectedIndicatorColors(iArr);
        this.mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        this.mViewPager.setOffscreenPageLimit(POSITION_POPULAR);
        if (this.mSectionsPagerAdapter == null) {
            this.mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        }
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        this.mSlidingTabLayout.setViewPager(this.mViewPager);
        this.mSlidingTabLayout.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                WatchesFragment.this.mShowNewBroadcast = position == 0 ? true : WatchesFragment.DEBUG;
                WatchesFragment.this.configTabs();
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        this.mTextViewNew = (TextView) view.findViewById(R.id.text_view_new);
        this.mTextViewNew.setOnClickListener(this);
        this.mTextViewPopular = (TextView) view.findViewById(R.id.text_view_popular);
        this.mTextViewPopular.setOnClickListener(this);
        this.mViewPager.setCurrentItem(getArguments().getBoolean(Constants.EXTRA_SWITCH_TO_POPULAR, DEBUG) ? POSITION_POPULAR : POSITION_NEW);
        configTabs();
        return view;
    }

    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            try {
                List<Fragment> list = getChildFragmentManager().getFragments();
                for (int i = POSITION_NEW; i < list.size(); i += POSITION_POPULAR) {
                    BroadcastsFragment f = (BroadcastsFragment) list.get(i);
                    if (BroadcastLogicType.New.equals(f.getLogicType())) {
                        this.mNewBroadcastFragment = f;
                    } else if (BroadcastLogicType.Popular.equals(f.getLogicType())) {
                        this.mPopularBroadcastFragment = f;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                this.mSectionsPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    public void onResume() {
        super.onResume();
        Crashlytics.log("WatchesFragment.onResume");
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void onDetach() {
        super.onDetach();
    }

    public void onClick(View view) {
        this.mShowNewBroadcast = view == this.mTextViewNew ? true : DEBUG;
        int currentItem = this.mViewPager.getCurrentItem();
        int newItem = this.mShowNewBroadcast ? POSITION_NEW : POSITION_POPULAR;
        if (currentItem != newItem) {
            this.mViewPager.setCurrentItem(newItem);
            configTabs();
            return;
        }
        BroadcastsFragment f;
        if (currentItem == 0) {
            f = this.mNewBroadcastFragment;
        } else {
            f = this.mPopularBroadcastFragment;
        }
        if (f != null) {
            f.scrollToTop();
            f.onRefresh();
        }
    }

    public void onRefresh() {
        refreshWatches();
    }

    public void switchToTab(boolean popularBroadcasts) {
        if (popularBroadcasts != this.mShowNewBroadcast) {
            onClick(popularBroadcasts ? this.mTextViewPopular : this.mTextViewNew);
        }
    }

    public void refreshWatches() {
        if (this.mShowNewBroadcast && this.mNewBroadcastFragment != null) {
            this.mNewBroadcastFragment.loadData(true);
        } else if (this.mPopularBroadcastFragment != null) {
            this.mPopularBroadcastFragment.loadData(true);
        }
    }

    private void configTabs() {
        this.mTextViewNew.setActivated(this.mShowNewBroadcast);
        this.mTextViewPopular.setActivated(!this.mShowNewBroadcast ? true : DEBUG);
    }
}
