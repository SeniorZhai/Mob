package com.mobcrush.mobcrush;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response.Listener;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.GroupChannel;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.ScrollTabHolder;
import com.mobcrush.mobcrush.ui.ScrollTabHolderFragment;
import com.mobcrush.mobcrush.ui.SlidingTabLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import io.fabric.sdk.android.BuildConfig;

public class ChannelFragment extends Fragment implements ScrollTabHolder, OnClickListener, OnMenuItemClickListener {
    private static final String CHANNEL_DESCRIPTION_TYPEFACE = "Klavika-Light.ttf";
    private static final String CHANNEL_TITLE_TYPEFACE = "Roboto-Light.ttf";
    private static final int POSITION_DISCUSSION = 1;
    private static final int POSITION_MEMBERS = 2;
    private static final int POSITION_VIDEOS = 0;
    private static final String TABS_TYPEFACE = "Roboto-Medium.ttf";
    private static final String TAG = "ChannelFragment";
    private BroadcastsFragment mBroadcastFragment;
    private DiscussionFragment mDiscussionFragment;
    private UsersFragment mFollowingFragment;
    private View mHeaderBox;
    private int mHeaderHeightPixels;
    private int mHeaderMaxOffset;
    private int mHeaderMinimalHeight;
    private float mMaxHeaderElevation;
    private boolean mRequiredUpdating;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;
    private TextView mTabDiscussion;
    private TextView mTabMembers;
    private TextView mTabVideos;
    private View mTabsLayout;
    private UserChannel mUserChannel;
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
            ScrollTabHolderFragment fragment;
            Log.d(ChannelFragment.TAG, "getItem " + position);
            switch (position) {
                case ChannelFragment.POSITION_VIDEOS /*0*/:
                    fragment = ChannelFragment.this.mBroadcastFragment = BroadcastsFragment.newInstance(ChannelFragment.this.mUserChannel, position);
                    break;
                case ChannelFragment.POSITION_DISCUSSION /*1*/:
                    fragment = ChannelFragment.this.mDiscussionFragment = DiscussionFragment.newInstance(ChannelFragment.this.mUserChannel, position);
                    break;
                default:
                    fragment = ChannelFragment.this.mFollowingFragment = UsersFragment.newInstance(ChannelFragment.this.mUserChannel, position);
                    break;
            }
            this.mScrollTabHolders.put(position, fragment);
            if (this.mListener != null) {
                fragment.setScrollTabHolder(this.mListener);
            }
            return fragment;
        }

        public int getCount() {
            return 3;
        }

        public CharSequence getPageTitle(int position) {
            int resId = ChannelFragment.POSITION_VIDEOS;
            switch (position) {
                case ChannelFragment.POSITION_VIDEOS /*0*/:
                    resId = R.string.videos;
                    break;
                case ChannelFragment.POSITION_DISCUSSION /*1*/:
                    resId = R.string.discussion;
                    break;
                case ChannelFragment.POSITION_MEMBERS /*2*/:
                    resId = R.string.members;
                    break;
            }
            if (resId > 0) {
                return MainApplication.getRString(resId, new Object[ChannelFragment.POSITION_VIDEOS]);
            }
            return BuildConfig.FLAVOR;
        }

        public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
            return this.mScrollTabHolders;
        }
    }

    public static ChannelFragment newInstance(UserChannel userChannel) {
        ChannelFragment fragment = new ChannelFragment();
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_USER_CHANNEL, userChannel.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            String userChannel = getArguments().getString(Constants.EXTRA_USER_CHANNEL, null);
            if (!TextUtils.isEmpty(userChannel)) {
                this.mUserChannel = (UserChannel) new Gson().fromJson(userChannel, UserChannel.class);
                if (this.mUserChannel != null && this.mUserChannel.channel.chatRoom == null) {
                    Network.getChannel(getActivity(), this.mUserChannel.channel._id, new Listener<GroupChannel>() {
                        public void onResponse(GroupChannel response) {
                            if (response != null) {
                                ChannelFragment.this.mUserChannel.channel.chatRoom = response.chatRoom;
                                ChannelFragment.this.mUserChannel.channel.posterImage = response.posterImage;
                                ChannelFragment.this.mUserChannel.channel.memberCount = response.memberCount;
                                ChannelFragment.this.update();
                            }
                        }
                    }, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_channels, container, false);
        this.mHeaderBox = root.findViewById(R.id.layout_header);
        ViewCompat.setElevation(this.mHeaderBox, (float) getResources().getDimensionPixelSize(R.dimen.headers_elevation));
        this.mMaxHeaderElevation = 20.0f;
        this.mTabsLayout = root.findViewById(R.id.tabs_layout);
        this.mTabVideos = (TextView) root.findViewById(R.id.tab_videos);
        this.mTabVideos.setTypeface(UIUtils.getTypeface(getActivity(), TABS_TYPEFACE));
        this.mTabVideos.setOnClickListener(this);
        this.mTabDiscussion = (TextView) root.findViewById(R.id.tab_discussion);
        this.mTabDiscussion.setTypeface(UIUtils.getTypeface(getActivity(), TABS_TYPEFACE));
        this.mTabDiscussion.setOnClickListener(this);
        this.mTabMembers = (TextView) root.findViewById(R.id.tab_members);
        this.mTabMembers.setTypeface(UIUtils.getTypeface(getActivity(), TABS_TYPEFACE));
        this.mTabMembers.setOnClickListener(this);
        this.mSlidingTabLayout = (SlidingTabLayout) root.findViewById(R.id.sliding_tabs);
        SlidingTabLayout slidingTabLayout = this.mSlidingTabLayout;
        int[] iArr = new int[POSITION_DISCUSSION];
        iArr[POSITION_VIDEOS] = getResources().getColor(R.color.yellow);
        slidingTabLayout.setSelectedIndicatorColors(iArr);
        this.mViewPager = (ViewPager) root.findViewById(R.id.viewpager);
        this.mViewPager.setOffscreenPageLimit(POSITION_MEMBERS);
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
                ChannelFragment.this.refreshTabs();
                ScrollTabHolder currentHolder = (ScrollTabHolder) ChannelFragment.this.mSectionsPagerAdapter.getScrollTabHolders().valueAt(position);
                if (currentHolder == null || ChannelFragment.this.mHeaderBox == null) {
                    Log.e(ChannelFragment.TAG, "adjustScroll skipped: " + currentHolder + "; mHeaderBox: " + ChannelFragment.this.mHeaderBox);
                    return;
                }
                Log.i(ChannelFragment.TAG, "adjustScroll " + ((int) (((float) ChannelFragment.this.mHeaderBox.getHeight()) + ChannelFragment.this.mHeaderBox.getTranslationY())));
                currentHolder.adjustScroll((int) (((float) ChannelFragment.this.mHeaderBox.getHeight()) + ChannelFragment.this.mHeaderBox.getTranslationY()));
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        this.mViewPager.setCurrentItem(POSITION_VIDEOS);
        configHeader(root);
        ViewTreeObserver vto = root.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (ChannelFragment.this.mHeaderHeightPixels == 0 && ChannelFragment.this.mSectionsPagerAdapter.getCount() > 0) {
                        ChannelFragment.this.computeScrollingMetrics();
                    }
                }
            });
        }
        return root;
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            refreshTabs();
            if (this.mRequiredUpdating) {
                this.mRequiredUpdating = false;
                update();
            }
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.channel_options:
                PopupMenu popup = new PopupMenu(getActivity(), view);
                popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
                return;
            case R.id.tab_videos:
                this.mViewPager.setCurrentItem(POSITION_VIDEOS);
                return;
            case R.id.tab_discussion:
                this.mViewPager.setCurrentItem(POSITION_DISCUSSION);
                return;
            case R.id.tab_members:
                this.mViewPager.setCurrentItem(POSITION_MEMBERS);
                return;
            default:
                return;
        }
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                return true;
            default:
                return false;
        }
    }

    public void adjustScroll(int scrollHeight) {
    }

    public void onScroll(View view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
        if (this.mViewPager.getCurrentItem() == pagePosition) {
            this.mHeaderBox.setTranslationY((float) Math.max(-getScrollY(view, firstVisibleItem), this.mHeaderMaxOffset));
        }
    }

    public int getScrollY(View view, int firstVisiblePosition) {
        if (view == null) {
            return POSITION_VIDEOS;
        }
        int top = view.getTop();
        int headerHeight = POSITION_VIDEOS;
        if (firstVisiblePosition >= POSITION_DISCUSSION) {
            headerHeight = this.mHeaderHeightPixels;
        }
        if (Math.abs(this.mHeaderMaxOffset - top) <= POSITION_MEMBERS) {
            top = this.mHeaderMaxOffset;
        }
        return ((-top) + (view.getHeight() * firstVisiblePosition)) + headerHeight;
    }

    public UserChannel getChannel() {
        return this.mUserChannel;
    }

    public void setChannel(UserChannel channel) {
        this.mUserChannel = channel;
        this.mHeaderHeightPixels = POSITION_VIDEOS;
        if (isAdded() || channel == null) {
            update();
        } else {
            this.mRequiredUpdating = true;
        }
    }

    private void update() {
        configHeader(getView());
        computeScrollingMetrics();
        if (this.mBroadcastFragment != null) {
            this.mBroadcastFragment.setChannel(this.mUserChannel);
        }
        if (this.mDiscussionFragment != null) {
            this.mDiscussionFragment.setChannel(this.mUserChannel);
        }
        if (this.mFollowingFragment != null) {
            this.mFollowingFragment.setChannel(this.mUserChannel);
        }
        ScrollTabHolder currentHolder = (ScrollTabHolder) this.mSectionsPagerAdapter.getScrollTabHolders().valueAt(this.mViewPager.getCurrentItem());
        if (currentHolder == null || this.mHeaderBox == null) {
            Log.e(TAG, "update.adjustScroll skipped: " + currentHolder + "; mHeaderBox: " + this.mHeaderBox);
            return;
        }
        Log.i(TAG, "update.adjustScroll " + ((int) (((float) this.mHeaderBox.getHeight()) + this.mHeaderBox.getTranslationY())));
        currentHolder.adjustScroll((int) (((float) this.mHeaderBox.getHeight()) + this.mHeaderBox.getTranslationY()));
    }

    private void configHeader(View v) {
        if (this.mUserChannel != null && v != null && isAdded()) {
            TextView title = (TextView) v.findViewById(R.id.channel_title);
            title.setText(this.mUserChannel.channel.name);
            title.setTypeface(UIUtils.getTypeface(getActivity(), CHANNEL_TITLE_TYPEFACE));
            final TextView description = (TextView) v.findViewById(R.id.channel_description);
            Object[] objArr = new Object[POSITION_DISCUSSION];
            objArr[POSITION_VIDEOS] = Integer.valueOf(POSITION_VIDEOS);
            description.setText(MainApplication.getRString(R.string._N_members, objArr));
            description.setTypeface(UIUtils.getTypeface(getActivity(), CHANNEL_DESCRIPTION_TYPEFACE));
            ImageLoader.getInstance().displayImage(this.mUserChannel.channel.channelLogo, (ImageView) v.findViewById(R.id.channel_icon), new Builder().cacheOnDisk(true).cacheInMemory(true).build());
            if (this.mUserChannel != null && this.mUserChannel.channel != null) {
                Network.getChannel(getActivity(), this.mUserChannel.channel._id, new Listener<GroupChannel>() {
                    public void onResponse(GroupChannel response) {
                        if (response != null && ChannelFragment.this.isAdded()) {
                            TextView textView = description;
                            Object[] objArr = new Object[ChannelFragment.POSITION_DISCUSSION];
                            objArr[ChannelFragment.POSITION_VIDEOS] = response.memberCount;
                            textView.setText(MainApplication.getRString(R.string._N_members, objArr));
                        }
                    }
                }, null);
            }
        }
    }

    private void refreshTabs() {
        int i = R.color.channel_tab_selected;
        if (this.mViewPager != null && this.mTabVideos != null && this.mTabDiscussion != null && this.mTabMembers != null) {
            int i2 = this.mViewPager.getCurrentItem();
            this.mTabVideos.setTextColor(getResources().getColor(i2 == 0 ? R.color.channel_tab_selected : R.color.channel_tab_normal));
            this.mTabDiscussion.setTextColor(getResources().getColor(i2 == POSITION_DISCUSSION ? R.color.channel_tab_selected : R.color.channel_tab_normal));
            TextView textView = this.mTabMembers;
            Resources resources = getResources();
            if (i2 != POSITION_MEMBERS) {
                i = R.color.channel_tab_normal;
            }
            textView.setTextColor(resources.getColor(i));
        }
    }

    private void computeScrollingMetrics() {
        boolean needToResetFragments;
        if (this.mHeaderHeightPixels == 0) {
            needToResetFragments = true;
        } else {
            needToResetFragments = false;
        }
        this.mHeaderHeightPixels = this.mHeaderBox != null ? this.mHeaderBox.getHeight() : POSITION_VIDEOS;
        this.mHeaderMinimalHeight = this.mTabsLayout != null ? this.mTabsLayout.getHeight() : POSITION_VIDEOS;
        this.mHeaderMaxOffset = this.mHeaderMinimalHeight - this.mHeaderHeightPixels;
        if (needToResetFragments) {
            if (this.mBroadcastFragment != null) {
                this.mBroadcastFragment.setHeaderHeight(this.mHeaderHeightPixels, this.mHeaderMinimalHeight);
            }
            if (this.mFollowingFragment != null) {
                this.mFollowingFragment.setHeaderHeight(this.mHeaderHeightPixels, this.mHeaderMinimalHeight);
            }
            if (this.mDiscussionFragment != null) {
                this.mDiscussionFragment.setHeaderHeight(this.mHeaderHeightPixels, this.mHeaderMinimalHeight);
            }
            onScroll(null, POSITION_VIDEOS, POSITION_VIDEOS, POSITION_VIDEOS, POSITION_VIDEOS);
        }
    }
}
