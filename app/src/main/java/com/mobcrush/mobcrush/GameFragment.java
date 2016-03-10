package com.mobcrush.mobcrush;

import android.content.res.Resources;
import android.graphics.Bitmap;
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
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Channel;
import com.mobcrush.mobcrush.datamodel.DataModel;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.datamodel.GroupChannel;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.mobcrush.mobcrush.logic.GameLogicType;
import com.mobcrush.mobcrush.logic.UserLogicType;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.ScrollTabHolder;
import com.mobcrush.mobcrush.ui.ScrollTabHolderFragment;
import com.mobcrush.mobcrush.ui.SlidingTabLayout;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import io.fabric.sdk.android.BuildConfig;

public class GameFragment extends Fragment implements ScrollTabHolder, OnClickListener, OnMenuItemClickListener {
    private static final String GAME_DESCRIPTION_TYPEFACE = "Klavika-Light.ttf";
    private static final String GAME_TITLE_TYPEFACE = "Roboto-Light.ttf";
    private static final int POSITION_BROADCASTERS = 2;
    private static final int POSITION_DISCUSSION = 1;
    private static final int POSITION_VIDEOS = 0;
    private static final String TABS_TYPEFACE = "Roboto-Medium.ttf";
    private static final String TAG = "GameFragment";
    private ErrorListener errorListener = new ErrorListener() {
        public void onErrorResponse(VolleyError error) {
            GameFragment.this.mIsNetworkProcessing = false;
        }
    };
    private BroadcastsFragment mBroadcastFragment;
    private boolean mChildHeightConfigured;
    private UsersFragment mFollowingFragment;
    private Game mGame;
    private GameLogicType mGameLogicType = GameLogicType.Game;
    private View mHeaderBox;
    private int mHeaderHeightPixels;
    private int mHeaderMaxOffset;
    private boolean mIsNetworkProcessing;
    private ImageView mPosterImage;
    private boolean mRequiredUpdating;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TextView mTabBroadcasters;
    private TextView mTabVideos;
    private View mTabsLayout;
    private UserChannel mUserChannel;
    private ViewPager mViewPager;

    static /* synthetic */ class AnonymousClass9 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$GameLogicType = new int[GameLogicType.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$GameLogicType[GameLogicType.Game.ordinal()] = GameFragment.POSITION_DISCUSSION;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$GameLogicType[GameLogicType.Channel.ordinal()] = GameFragment.POSITION_BROADCASTERS;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

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
            switch (position) {
                case GameFragment.POSITION_VIDEOS /*0*/:
                    if (!GameLogicType.Game.equals(GameFragment.this.mGameLogicType)) {
                        fragment = GameFragment.this.mBroadcastFragment = BroadcastsFragment.newInstance(GameFragment.this.mUserChannel, position);
                        break;
                    }
                    fragment = GameFragment.this.mBroadcastFragment = BroadcastsFragment.newInstance(GameFragment.this.mGame, position);
                    break;
                default:
                    DataModel access$100;
                    UserLogicType userLogicType;
                    GameFragment gameFragment = GameFragment.this;
                    if (GameLogicType.Game.equals(GameFragment.this.mGameLogicType)) {
                        access$100 = GameFragment.this.mGame;
                    } else {
                        access$100 = GameFragment.this.mUserChannel;
                    }
                    if (GameLogicType.Game.equals(GameFragment.this.mGameLogicType)) {
                        userLogicType = UserLogicType.GameBroadcasters;
                    } else {
                        userLogicType = UserLogicType.ChannelUsers;
                    }
                    fragment = gameFragment.mFollowingFragment = UsersFragment.newInstance(access$100, userLogicType, position);
                    break;
            }
            this.mScrollTabHolders.put(position, fragment);
            if (this.mListener != null) {
                fragment.setScrollTabHolder(this.mListener);
            }
            return fragment;
        }

        public int getCount() {
            return GameFragment.POSITION_BROADCASTERS;
        }

        public CharSequence getPageTitle(int position) {
            int resId = GameFragment.POSITION_VIDEOS;
            switch (position) {
                case GameFragment.POSITION_VIDEOS /*0*/:
                    resId = R.string.videos;
                    break;
                case GameFragment.POSITION_BROADCASTERS /*2*/:
                    resId = R.string.members;
                    break;
            }
            if (resId > 0) {
                return MainApplication.getRString(resId, new Object[GameFragment.POSITION_VIDEOS]);
            }
            return BuildConfig.FLAVOR;
        }

        public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
            return this.mScrollTabHolders;
        }
    }

    public static GameFragment newInstance(Game game) {
        return newInstance(game, GameLogicType.Game);
    }

    public static GameFragment newInstance(Channel channel) {
        return newInstance(channel, GameLogicType.Channel);
    }

    public static GameFragment newInstance(DataModel model, GameLogicType logicType) {
        if (logicType == null) {
            throw new IllegalArgumentException("logicType should be specified");
        }
        GameFragment fragment = new GameFragment();
        Bundle args = new Bundle();
        switch (AnonymousClass9.$SwitchMap$com$mobcrush$mobcrush$logic$GameLogicType[logicType.ordinal()]) {
            case POSITION_DISCUSSION /*1*/:
                if (model != null) {
                    args.putString(Constants.EXTRA_GAME, model.toString());
                    break;
                }
                break;
            case POSITION_BROADCASTERS /*2*/:
                if (model != null) {
                    args.putString(Constants.EXTRA_CHANNEL, model.toString());
                    break;
                }
                break;
        }
        args.putString(Constants.EXTRA_GAME_LOGIC, logicType.toString());
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            String game = getArguments().getString(Constants.EXTRA_GAME, null);
            if (!TextUtils.isEmpty(game)) {
                this.mGame = (Game) new Gson().fromJson(game, Game.class);
                if (this.mGame != null && this.mGame.chatRoom == null) {
                    Network.getGame(getActivity(), this.mGame._id, new Listener<Game>() {
                        public void onResponse(Game response) {
                            if (response != null) {
                                GameFragment.this.mGame = response;
                                GameFragment.this.update();
                            }
                        }
                    }, null);
                }
            }
            String channel = getArguments().getString(Constants.EXTRA_CHANNEL, null);
            if (!TextUtils.isEmpty(game)) {
                this.mUserChannel = new UserChannel((Channel) new Gson().fromJson(channel, Channel.class));
                if (this.mUserChannel.channel.chatRoom == null) {
                    Network.getChannel(getActivity(), this.mUserChannel.channel._id, new Listener<GroupChannel>() {
                        public void onResponse(GroupChannel response) {
                            if (response != null) {
                                GameFragment.this.mUserChannel.channel.chatRoom = response.chatRoom;
                                GameFragment.this.mUserChannel.channel.posterImage = response.posterImage;
                                GameFragment.this.mUserChannel.channel.memberCount = response.memberCount;
                                GameFragment.this.update();
                            }
                        }
                    }, null);
                }
            }
            String logic = getArguments().getString(Constants.EXTRA_GAME_LOGIC, null);
            if (!TextUtils.isEmpty(game)) {
                this.mGameLogicType = GameLogicType.valueOf(logic);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_game, container, false);
        this.mHeaderBox = root.findViewById(R.id.layout_header);
        ViewCompat.setElevation(this.mHeaderBox, (float) getResources().getDimensionPixelSize(R.dimen.headers_elevation));
        this.mTabsLayout = root.findViewById(R.id.tabs_layout);
        this.mTabVideos = (TextView) root.findViewById(R.id.tab_videos);
        this.mTabVideos.setTypeface(UIUtils.getTypeface(getActivity(), TABS_TYPEFACE));
        this.mTabVideos.setOnClickListener(this);
        this.mTabBroadcasters = (TextView) root.findViewById(R.id.tab_broadcasters);
        this.mTabBroadcasters.setTypeface(UIUtils.getTypeface(getActivity(), TABS_TYPEFACE));
        this.mTabBroadcasters.setOnClickListener(this);
        SlidingTabLayout mSlidingTabLayout = (SlidingTabLayout) root.findViewById(R.id.sliding_tabs);
        int[] iArr = new int[POSITION_DISCUSSION];
        iArr[POSITION_VIDEOS] = getResources().getColor(R.color.yellow);
        mSlidingTabLayout.setSelectedIndicatorColors(iArr);
        this.mViewPager = (ViewPager) root.findViewById(R.id.viewpager);
        this.mViewPager.setOffscreenPageLimit(POSITION_BROADCASTERS);
        this.mPosterImage = (ImageView) root.findViewById(R.id.game_poster_image);
        if (this.mSectionsPagerAdapter == null) {
            this.mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager());
        }
        this.mSectionsPagerAdapter.setTabHolderScrollingContent(this);
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        mSlidingTabLayout.setViewPager(this.mViewPager);
        mSlidingTabLayout.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                GameFragment.this.refreshTabs();
                ScrollTabHolder currentHolder = (ScrollTabHolder) GameFragment.this.mSectionsPagerAdapter.getScrollTabHolders().valueAt(position);
                if (currentHolder == null || GameFragment.this.mHeaderBox == null) {
                    Log.e(GameFragment.TAG, "adjustScroll skipped: " + currentHolder + "; mHeaderBox: " + GameFragment.this.mHeaderBox);
                    return;
                }
                Log.i(GameFragment.TAG, "adjustScroll " + ((int) (((float) GameFragment.this.mHeaderBox.getHeight()) + GameFragment.this.mHeaderBox.getTranslationY())) + "; headerBox: " + GameFragment.this.mHeaderBox.getHeight() + "; translation: " + GameFragment.this.mHeaderBox.getTranslationY() + "; mHeaderHeightPixels: " + GameFragment.this.mHeaderHeightPixels);
                currentHolder.adjustScroll((int) (((float) GameFragment.this.mHeaderHeightPixels) + GameFragment.this.mHeaderBox.getTranslationY()));
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
                    if ((GameFragment.this.mHeaderHeightPixels == 0 && GameFragment.this.mSectionsPagerAdapter.getCount() > 0) || !GameFragment.this.mChildHeightConfigured) {
                        GameFragment.this.mHeaderHeightPixels = GameFragment.POSITION_VIDEOS;
                        GameFragment.this.computeScrollingMetrics();
                    }
                }
            });
        }
        refreshTabs();
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
        if (isVisibleToUser) {
            refreshTabs();
            if (this.mRequiredUpdating) {
                this.mRequiredUpdating = false;
                update();
            }
        }
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
                if (this.mBroadcastFragment != null) {
                    this.mBroadcastFragment.onRefresh();
                    return;
                }
                return;
            case R.id.tab_discussion:
                this.mViewPager.setCurrentItem(POSITION_DISCUSSION);
                return;
            case R.id.tab_broadcasters:
                this.mViewPager.setCurrentItem(POSITION_BROADCASTERS);
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
        if (Math.abs(this.mHeaderMaxOffset - top) <= POSITION_BROADCASTERS) {
            top = this.mHeaderMaxOffset;
        }
        return ((-top) + (view.getHeight() * firstVisiblePosition)) + headerHeight;
    }

    public Game getGame() {
        return this.mGame;
    }

    public void setGame(Game game) {
        this.mGameLogicType = GameLogicType.Game;
        if (this.mGame == null || game == null || !TextUtils.equals(this.mGame._id, game._id)) {
            this.mGame = game;
            this.mIsNetworkProcessing = false;
            this.mHeaderHeightPixels = POSITION_VIDEOS;
            if (isAdded() || game == null) {
                update();
            } else {
                this.mRequiredUpdating = true;
            }
            try {
                if (this.mHeaderBox != null) {
                    this.mHeaderBox.setTranslationY(0.0f);
                    return;
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        this.mGame.broadcastCount = game.broadcastCount;
        this.mGame.enabled = game.enabled;
    }

    public Channel getChannel() {
        return this.mUserChannel != null ? this.mUserChannel.channel : null;
    }

    public void setChannel(Channel channel) {
        this.mGameLogicType = GameLogicType.Channel;
        if (this.mUserChannel == null || channel == null || !TextUtils.equals(this.mUserChannel.channel._id, channel._id)) {
            this.mUserChannel = new UserChannel(channel);
            this.mHeaderHeightPixels = POSITION_VIDEOS;
            if (isAdded() || channel == null) {
                update();
            } else {
                this.mRequiredUpdating = true;
            }
            try {
                if (this.mHeaderBox != null) {
                    this.mHeaderBox.setTranslationY(0.0f);
                    return;
                }
                return;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
        this.mUserChannel.channel.memberCount = channel.memberCount;
        this.mUserChannel.channel.posterImage = channel.posterImage;
    }

    private void update() {
        this.mHeaderHeightPixels = POSITION_VIDEOS;
        configHeader(getView());
        computeScrollingMetrics();
        if (this.mBroadcastFragment != null) {
            if (GameLogicType.Game.equals(this.mGameLogicType)) {
                this.mBroadcastFragment.setGame(this.mGame);
            } else {
                this.mBroadcastFragment.setChannel(this.mUserChannel);
            }
        }
        if (this.mFollowingFragment != null) {
            if (GameLogicType.Game.equals(this.mGameLogicType)) {
                this.mFollowingFragment.setGame(this.mGame);
            } else {
                this.mFollowingFragment.setChannel(this.mUserChannel);
            }
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
        if (!GameLogicType.Game.equals(this.mGameLogicType) || this.mGame != null) {
            if ((!GameLogicType.Channel.equals(this.mGameLogicType) || this.mUserChannel != null) && v != null) {
                TextView title = (TextView) v.findViewById(R.id.game_title);
                title.setText(GameLogicType.Game.equals(this.mGameLogicType) ? this.mGame.name : this.mUserChannel.channel.name);
                title.setTypeface(UIUtils.getTypeface(getActivity(), GAME_TITLE_TYPEFACE));
                final TextView description = (TextView) v.findViewById(R.id.game_description);
                CharSequence charSequence;
                Object[] objArr;
                if (GameLogicType.Game.equals(this.mGameLogicType)) {
                    if (this.mGame.broadcastCount == null) {
                        charSequence = BuildConfig.FLAVOR;
                    } else {
                        objArr = new Object[POSITION_DISCUSSION];
                        objArr[POSITION_VIDEOS] = this.mGame.broadcastCount;
                        charSequence = MainApplication.getRString(R.string._N_broadcasts, objArr);
                    }
                    description.setText(charSequence);
                } else {
                    if (this.mUserChannel.channel.memberCount == null) {
                        charSequence = BuildConfig.FLAVOR;
                    } else {
                        objArr = new Object[POSITION_DISCUSSION];
                        objArr[POSITION_VIDEOS] = this.mUserChannel.channel.memberCount;
                        charSequence = MainApplication.getRString(R.string._N_Broadcasters, objArr);
                    }
                    description.setText(charSequence);
                }
                description.setTypeface(UIUtils.getTypeface(getActivity(), GAME_DESCRIPTION_TYPEFACE));
                ImageLoader.getInstance().displayImage(GameLogicType.Game.equals(this.mGameLogicType) ? this.mGame.icon : this.mUserChannel.channel.channelLogo, (ImageView) v.findViewById(R.id.game_icon), new Builder().displayer(new RoundedBitmapDisplayer(getResources().getDimensionPixelSize(R.dimen.games_logo_corner))).cacheOnDisk(true).cacheInMemory(true).build());
                if (isAdded()) {
                    final String posterImage = this.mGame != null ? this.mGame.posterImage : this.mUserChannel.channel.posterImage;
                    if (TextUtils.isEmpty(posterImage)) {
                        this.mPosterImage.setVisibility(8);
                    } else {
                        setPosterImage(posterImage, false);
                    }
                    this.mHeaderHeightPixels = POSITION_VIDEOS;
                    computeScrollingMetrics();
                    if (!this.mIsNetworkProcessing) {
                        this.mIsNetworkProcessing = true;
                        if (GameLogicType.Game.equals(this.mGameLogicType)) {
                            Network.getGame(getActivity(), this.mGame._id, new Listener<Game>() {
                                public void onResponse(Game response) {
                                    if (response != null) {
                                        if (!TextUtils.equals(posterImage, response.posterImage)) {
                                            GameFragment.this.setPosterImage(response.posterImage, true);
                                        }
                                        GameFragment.this.mGame = response;
                                        if (response.broadcastCount != null && GameFragment.this.isAdded()) {
                                            TextView textView = description;
                                            Object[] objArr = new Object[GameFragment.POSITION_DISCUSSION];
                                            objArr[GameFragment.POSITION_VIDEOS] = response.broadcastCount;
                                            textView.setText(MainApplication.getRString(R.string._N_broadcasts, objArr));
                                        }
                                    }
                                    GameFragment.this.mIsNetworkProcessing = false;
                                }
                            }, this.errorListener);
                        } else {
                            Network.getChannel(getActivity(), this.mUserChannel.channel._id, new Listener<GroupChannel>() {
                                public void onResponse(GroupChannel response) {
                                    if (response != null) {
                                        if (!TextUtils.equals(posterImage, response.posterImage)) {
                                            GameFragment.this.setPosterImage(response.posterImage, true);
                                        }
                                        GameFragment.this.mUserChannel.channel.posterImage = response.posterImage;
                                        GameFragment.this.mUserChannel.channel.memberCount = response.memberCount;
                                        if (response.memberCount != null && GameFragment.this.isAdded()) {
                                            TextView textView = description;
                                            Object[] objArr = new Object[GameFragment.POSITION_DISCUSSION];
                                            objArr[GameFragment.POSITION_VIDEOS] = response.memberCount;
                                            textView.setText(MainApplication.getRString(R.string._N_Broadcasters, objArr));
                                        }
                                    }
                                    GameFragment.this.mIsNetworkProcessing = false;
                                }
                            }, this.errorListener);
                        }
                    }
                }
            }
        }
    }

    private void setPosterImage(String posterImage, final boolean needAnimation) {
        DisplayImageOptions dio = new Builder().cacheOnDisk(true).cacheInMemory(true).build();
        if (TextUtils.isEmpty(posterImage) || getActivity() == null) {
            this.mHeaderHeightPixels = POSITION_VIDEOS;
            computeScrollingMetrics();
            return;
        }
        final int posterHeight = (int) ((((double) UIUtils.getScreenSize(getActivity().getWindowManager()).x) * 9.0d) / 16.0d);
        ImageLoader.getInstance().displayImage(posterImage, this.mPosterImage, dio, new ImageLoadingListener() {
            public void onLoadingStarted(String imageUri, View view) {
            }

            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            }

            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (GameFragment.this.isAdded()) {
                    LayoutParams layoutParams = GameFragment.this.mHeaderBox.getLayoutParams();
                    layoutParams.height = posterHeight + GameFragment.this.getResources().getDimensionPixelSize(R.dimen.game_header_height);
                    GameFragment.this.mHeaderBox.setLayoutParams(layoutParams);
                    layoutParams = GameFragment.this.mPosterImage.getLayoutParams();
                    layoutParams.height = posterHeight;
                    GameFragment.this.mPosterImage.setLayoutParams(layoutParams);
                    GameFragment.this.mPosterImage.setImageBitmap(loadedImage);
                    GameFragment.this.mPosterImage.setVisibility(GameFragment.POSITION_VIDEOS);
                    if (needAnimation) {
                        TranslateAnimation animation = new TranslateAnimation(0.0f, 0.0f, (float) (-posterHeight), 0.0f);
                        animation.setAnimationListener(new AnimationListener() {
                            public void onAnimationStart(Animation animation) {
                            }

                            public void onAnimationRepeat(Animation animation) {
                            }

                            public void onAnimationEnd(Animation animation) {
                                GameFragment.this.mHeaderHeightPixels = GameFragment.POSITION_VIDEOS;
                                GameFragment.this.computeScrollingMetrics();
                            }
                        });
                        animation.setDuration(300);
                        GameFragment.this.mHeaderBox.startAnimation(animation);
                        GameFragment.this.mViewPager.startAnimation(animation);
                        return;
                    }
                    GameFragment.this.mHeaderHeightPixels = GameFragment.POSITION_VIDEOS;
                    GameFragment.this.computeScrollingMetrics();
                }
            }

            public void onLoadingCancelled(String imageUri, View view) {
            }
        });
    }

    private void refreshTabs() {
        int i = R.color.channel_tab_selected;
        if (this.mViewPager != null && this.mTabVideos != null && this.mTabBroadcasters != null) {
            int i2 = this.mViewPager.getCurrentItem();
            this.mTabVideos.setTextColor(getResources().getColor(i2 == 0 ? R.color.channel_tab_selected : R.color.channel_tab_normal));
            TextView textView = this.mTabBroadcasters;
            Resources resources = getResources();
            if (i2 != POSITION_BROADCASTERS) {
                i = R.color.channel_tab_normal;
            }
            textView.setTextColor(resources.getColor(i));
        }
    }

    private void computeScrollingMetrics() {
        if (getActivity() != null) {
            boolean needToResetFragments;
            if (this.mHeaderHeightPixels == 0) {
                needToResetFragments = true;
            } else {
                needToResetFragments = false;
            }
            this.mHeaderHeightPixels = getActivity().getResources().getDimensionPixelSize(R.dimen.game_header_height);
            if (this.mPosterImage.getVisibility() == 0) {
                this.mHeaderHeightPixels += (int) ((((double) UIUtils.getScreenSize(getActivity().getWindowManager()).x) * 9.0d) / 16.0d);
            }
            int mHeaderMinimalHeight = this.mTabsLayout.getHeight();
            this.mHeaderMaxOffset = mHeaderMinimalHeight - this.mHeaderHeightPixels;
            this.mTabsLayout.setVisibility(POSITION_VIDEOS);
            if (needToResetFragments) {
                if (this.mBroadcastFragment != null) {
                    this.mBroadcastFragment.setHeaderHeight(this.mHeaderHeightPixels, mHeaderMinimalHeight);
                    this.mChildHeightConfigured = true;
                } else {
                    this.mChildHeightConfigured = false;
                }
                if (this.mFollowingFragment != null) {
                    this.mFollowingFragment.setHeaderHeight(this.mHeaderHeightPixels, mHeaderMinimalHeight);
                    this.mChildHeightConfigured = true;
                } else {
                    this.mChildHeightConfigured = false;
                }
                onScroll(null, POSITION_VIDEOS, POSITION_VIDEOS, POSITION_VIDEOS, POSITION_VIDEOS);
            }
        }
    }
}
