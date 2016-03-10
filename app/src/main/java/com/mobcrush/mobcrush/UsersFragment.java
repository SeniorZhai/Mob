package com.mobcrush.mobcrush;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.datamodel.DataModel;
import com.mobcrush.mobcrush.datamodel.EntityType;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.mobcrush.mobcrush.logic.UserLogicType;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.player.Player;
import com.mobcrush.mobcrush.ui.ScrollTabHolder;
import com.mobcrush.mobcrush.ui.ScrollTabHolderFragment;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class UsersFragment extends ScrollTabHolderFragment implements OnRefreshListener {
    private static final String TAG = "UsersFragment";
    private UsersAdapter mAdapter;
    private boolean mClearDataOnReceiveNew;
    private boolean mDataIsLoading;
    private boolean mFragmentShowing;
    private Game mGame;
    private int mHeaderHeight;
    private LinearLayoutManager mLayoutManager;
    private int mMinimalHeaderHeight;
    private int mPosition;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private User mUser;
    private UserChannel mUserChannel;
    private UserLogicType mUserLogicType;
    private Listener<User[]> onUsersResponse = new Listener<User[]>() {
        public void onResponse(User[] users) {
            UsersFragment.this.mDataIsLoading = false;
            if (UsersFragment.this.isAdded()) {
                UsersFragment.this.mSwipeRefreshLayout.setRefreshing(false);
            }
            if (users != null) {
                if (UsersFragment.this.mClearDataOnReceiveNew) {
                    UsersFragment.this.mClearDataOnReceiveNew = false;
                    UsersFragment.this.mAdapter.clear();
                }
                UsersFragment.this.mAdapter.addUsers(users);
                if (UsersFragment.this.mUser != null && UserLogicType.Following.equals(UsersFragment.this.mUserLogicType)) {
                    Log.d("!!!", "FollowingFragment: users.length= " + users.length + "; mUser.followingCount= " + UsersFragment.this.mUser.followingCount);
                }
            }
            if (UsersFragment.this.isAdded()) {
                if (UsersFragment.this.mRecyclerView.getAdapter() == null) {
                    UsersFragment.this.mRecyclerView.setAdapter(UsersFragment.this.mAdapter);
                }
                UsersFragment.this.correctProgressPosition();
            }
        }
    };

    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$UserLogicType = new int[UserLogicType.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$UserLogicType[UserLogicType.Followers.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$UserLogicType[UserLogicType.Following.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$UserLogicType[UserLogicType.ChannelUsers.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$UserLogicType[UserLogicType.GameBroadcasters.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$UserLogicType[UserLogicType.FollowingSettings.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    public static UsersFragment newInstance(UserChannel userChannel, int position) {
        return newInstance(userChannel, UserLogicType.ChannelUsers, position);
    }

    public static UsersFragment newInstance(Game game, int position) {
        return newInstance(game, UserLogicType.GameBroadcasters, position);
    }

    public static UsersFragment newInstance(DataModel dataModel, UserLogicType logicType, int position) {
        UsersFragment fragment = new UsersFragment();
        Bundle args = new Bundle();
        if (dataModel != null) {
            switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$UserLogicType[logicType.ordinal()]) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    args.putString(Constants.EXTRA_USER, dataModel.toString());
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    args.putString(Constants.EXTRA_USER_CHANNEL, dataModel.toString());
                    break;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    args.putString(Constants.EXTRA_GAME, dataModel.toString());
                    break;
            }
        }
        args.putString(Constants.EXTRA_USER_LOGIC, logicType.toString());
        args.putInt(Constants.EXTRA_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            try {
                if (getArguments().containsKey(Constants.EXTRA_USER)) {
                    this.mUser = (User) new Gson().fromJson(getArguments().getString(Constants.EXTRA_USER), User.class);
                } else if (getArguments().containsKey(Constants.EXTRA_USER_CHANNEL)) {
                    this.mUserChannel = (UserChannel) new Gson().fromJson(getArguments().getString(Constants.EXTRA_USER_CHANNEL, null), UserChannel.class);
                } else if (getArguments().containsKey(Constants.EXTRA_GAME)) {
                    this.mGame = (Game) new Gson().fromJson(getArguments().getString(Constants.EXTRA_GAME, null), Game.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
                String error = "UsersFragment initialization error. ";
                if (getArguments().containsKey(Constants.EXTRA_USER)) {
                    error = error + "EXTRA_USER:" + getArguments().getString(Constants.EXTRA_USER);
                } else if (getArguments().containsKey(Constants.EXTRA_USER_CHANNEL)) {
                    error = error + "EXTRA_USER_CHANNEL:" + getArguments().getString(Constants.EXTRA_USER_CHANNEL);
                } else if (getArguments().containsKey(Constants.EXTRA_GAME)) {
                    error = error + "EXTRA_GAME:" + getArguments().getString(Constants.EXTRA_GAME);
                }
                Crashlytics.logException(new Exception(error, e));
            }
            this.mUserLogicType = UserLogicType.valueOf(getArguments().getString(Constants.EXTRA_USER_LOGIC, null));
            this.mPosition = getArguments().getInt(Constants.EXTRA_POSITION);
        }
    }

    @SuppressLint({"ResourceAsColor"})
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_followxxx, container, false);
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        this.mSwipeRefreshLayout.setEnabled(false);
        this.mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.yellow);
        this.mSwipeRefreshLayout.setColorSchemeResources(R.color.dark);
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        int resID = R.layout.item_user;
        if (UserLogicType.GameBroadcasters.equals(this.mUserLogicType) || UserLogicType.ChannelUsers.equals(this.mUserLogicType)) {
            resID = R.layout.item_user_broadcasters;
        } else if (UserLogicType.FollowingSettings.equals(this.mUserLogicType)) {
            resID = R.layout.item_user_on_off;
        }
        this.mAdapter = new UsersAdapter(getActivity(), resID);
        this.mAdapter.setOnNeedNextDataCallback(new Callback() {
            public boolean handleMessage(Message msg) {
                if (msg != null && msg.what == 2 && (UserLogicType.Following.equals(UsersFragment.this.mUserLogicType) || UserLogicType.Followers.equals(UsersFragment.this.mUserLogicType) || UserLogicType.FollowingSettings.equals(UsersFragment.this.mUserLogicType))) {
                    UsersFragment.this.loadData();
                }
                return false;
            }
        });
        if (UserLogicType.FollowingSettings.equals(this.mUserLogicType)) {
            this.mAdapter.enableSwitchMode();
        }
        return view;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        applyHeights();
        this.mRecyclerView.addOnScrollListener(new OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                UsersFragment.this.correctProgressPosition();
                if (UsersFragment.this.mScrollTabHolder != null) {
                    UsersFragment.this.mScrollTabHolder.onScroll(UsersFragment.this.mLayoutManager.getChildAt(0), UsersFragment.this.mLayoutManager.findFirstVisibleItemPosition(), 0, UsersFragment.this.mAdapter.getDataItemCount(), UsersFragment.this.mPosition);
                }
            }
        });
        if (this.mAdapter != null && this.mAdapter.getDataItemCount() == 0) {
            if (this.mFragmentShowing || UserLogicType.FollowingSettings.equals(this.mUserLogicType)) {
                this.mClearDataOnReceiveNew = true;
                loadData();
            }
        }
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.mFragmentShowing = isVisibleToUser;
        if (isAdded()) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser) {
                switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$UserLogicType[this.mUserLogicType.ordinal()]) {
                    case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_PROFILE_FOLLOWERS);
                        break;
                    case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_PROFILE_FOLLOWING);
                        break;
                    case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_TEAM_USERS);
                        break;
                    case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_GAME_BROADCASTERS);
                        break;
                }
                loadData();
            }
        }
    }

    public void onResume() {
        super.onResume();
    }

    public void onRefresh() {
        this.mClearDataOnReceiveNew = true;
        loadData();
    }

    public void setHeaderHeight(int height, int minimalHeight) {
        Log.d(TAG, String.format("setHeaderHeight: %d, %d. isAdded: %b", new Object[]{Integer.valueOf(height), Integer.valueOf(minimalHeight), Boolean.valueOf(isAdded())}));
        if (height == this.mHeaderHeight && this.mMinimalHeaderHeight == minimalHeight) {
            Log.i(TAG, String.format("setHeaderHeight: %d, %d. skipped", new Object[]{Integer.valueOf(height), Integer.valueOf(minimalHeight)}));
            return;
        }
        Log.d(TAG, String.format("setHeaderHeight: %d, %d", new Object[]{Integer.valueOf(height), Integer.valueOf(minimalHeight)}));
        this.mHeaderHeight = height;
        this.mMinimalHeaderHeight = minimalHeight;
        applyHeights();
    }

    private void applyHeights() {
        if (getView() != null && (this.mHeaderHeight > 0 || this.mMinimalHeaderHeight > 0)) {
            View header = new View(getActivity());
            header.setMinimumHeight(this.mHeaderHeight);
            this.mAdapter.addHeaderView(header);
            this.mAdapter.setMinimalHeight(Math.max(getView().getHeight(), ((ViewGroup) getView().getParent()).getHeight()) - this.mMinimalHeaderHeight);
            if (this.mRecyclerView != null) {
                this.mRecyclerView.setAdapter(this.mAdapter);
            } else {
                Log.w(TAG, "mRecyclerView is Empty");
            }
        }
        correctProgressPosition();
    }

    private void correctProgressPosition() {
        if (isAdded() && this.mRecyclerView != null && this.mRecyclerView.getChildCount() > 0) {
            View v = this.mRecyclerView.getChildAt(0);
            int offset = this.mHeaderHeight + v.getTop();
            if (offset <= 0 || (v instanceof CardView)) {
                offset = this.mMinimalHeaderHeight;
            }
            this.mSwipeRefreshLayout.setProgressViewOffset(false, offset, offset);
        }
    }

    public void setUser(User user, boolean forceUpdate) {
        Log.d(TAG, this.mUserLogicType + ".setUser: mUser: " + this.mUser + " user: " + user + "; forceUpdate: " + forceUpdate);
        this.mUser = user;
        if (forceUpdate) {
            this.mClearDataOnReceiveNew = true;
            loadData(user);
        }
    }

    public void loadData(User user) {
        if (this.mUser == null || (this.mUser.equals(user) && this.mAdapter != null)) {
            this.mDataIsLoading = false;
            this.mAdapter.clear();
        }
        this.mUser = user;
        loadData();
    }

    public void setChannel(UserChannel channel) {
        this.mUserChannel = channel;
        this.mUserLogicType = UserLogicType.ChannelUsers;
        this.mDataIsLoading = false;
        ScrollTabHolder scrollTabHolder = this.mScrollTabHolder;
        this.mScrollTabHolder = null;
        adjustScroll(0);
        this.mAdapter.clear();
        loadData();
        this.mScrollTabHolder = scrollTabHolder;
    }

    public void setGame(Game game) {
        this.mGame = game;
        this.mUserLogicType = UserLogicType.GameBroadcasters;
        this.mDataIsLoading = false;
        ScrollTabHolder scrollTabHolder = this.mScrollTabHolder;
        this.mScrollTabHolder = null;
        adjustScroll(0);
        this.mAdapter.clear();
        loadData();
        this.mScrollTabHolder = scrollTabHolder;
    }

    public synchronized void loadData() {
        int i = 0;
        synchronized (this) {
            if (this.mAdapter != null) {
                ErrorListener errorListener = new ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        if (UsersFragment.this.isAdded()) {
                            UsersFragment.this.mSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                };
                if (!(this.mDataIsLoading || (this.mUser == null && this.mUserChannel == null && this.mGame == null)) || UserLogicType.FollowingSettings.equals(this.mUserLogicType)) {
                    int itemCount;
                    int pageSize;
                    switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$UserLogicType[this.mUserLogicType.ordinal()]) {
                        case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                        case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            if (this.mUser != null) {
                                itemCount = this.mAdapter.getDataItemCount();
                                pageSize = 20;
                                if (this.mClearDataOnReceiveNew && itemCount > 20) {
                                    pageSize = itemCount;
                                }
                                if (this.mClearDataOnReceiveNew || (itemCount / 20) * pageSize >= itemCount) {
                                    this.mDataIsLoading = true;
                                    if (isAdded() && this.mSwipeRefreshLayout != null) {
                                        this.mSwipeRefreshLayout.setRefreshing(true);
                                    }
                                    FragmentActivity activity;
                                    EntityType entityType;
                                    String str;
                                    if (!UserLogicType.Following.equals(this.mUserLogicType)) {
                                        activity = getActivity();
                                        entityType = EntityType.user;
                                        str = this.mUser._id;
                                        if (!this.mClearDataOnReceiveNew) {
                                            i = itemCount / 20;
                                        }
                                        Network.getFollowers(activity, entityType, str, i, pageSize, this.onUsersResponse, errorListener);
                                        break;
                                    }
                                    activity = getActivity();
                                    entityType = EntityType.user;
                                    str = this.mUser._id;
                                    if (!this.mClearDataOnReceiveNew) {
                                        i = itemCount / 20;
                                    }
                                    Network.getFollowing(activity, entityType, str, i, pageSize, this.onUsersResponse, errorListener);
                                    break;
                                }
                            }
                            break;
                        case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                            this.mClearDataOnReceiveNew = true;
                            if (this.mUserChannel != null) {
                                this.mDataIsLoading = true;
                                if (isAdded() && this.mSwipeRefreshLayout != null) {
                                    this.mSwipeRefreshLayout.setRefreshing(true);
                                }
                                Network.getChannelUsers(getActivity(), this.mUserChannel.channel._id, this.onUsersResponse, errorListener);
                                break;
                            }
                            break;
                        case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                            this.mClearDataOnReceiveNew = true;
                            if (this.mGame != null) {
                                this.mDataIsLoading = true;
                                if (isAdded() && this.mSwipeRefreshLayout != null) {
                                    this.mSwipeRefreshLayout.setRefreshing(true);
                                }
                                Network.getGameUsers(getActivity(), this.mGame._id, this.onUsersResponse, errorListener);
                                break;
                            }
                            break;
                        case Player.STATE_ENDED /*5*/:
                            itemCount = this.mAdapter.getDataItemCount();
                            pageSize = 20;
                            if (this.mClearDataOnReceiveNew && itemCount > 20) {
                                pageSize = itemCount;
                            }
                            if (this.mClearDataOnReceiveNew || (itemCount / 20) * pageSize >= itemCount) {
                                this.mDataIsLoading = true;
                                if (isAdded() && this.mSwipeRefreshLayout != null) {
                                    this.mSwipeRefreshLayout.setRefreshing(true);
                                }
                                Network.getFollowingSettings(getActivity(), this.mClearDataOnReceiveNew ? 0 : itemCount / 20, pageSize, this.onUsersResponse, errorListener);
                                break;
                            }
                        default:
                            break;
                    }
                }
            }
            Log.i(TAG, "Adapter has not been initialized yet");
        }
    }

    public void adjustScroll(int scrollHeight) {
        Log.d(TAG, "adjustScroll: " + scrollHeight);
        this.mRecyclerView.stopScroll();
        if (scrollHeight > this.mMinimalHeaderHeight || this.mLayoutManager.findFirstVisibleItemPosition() < 1) {
            this.mLayoutManager.scrollToPositionWithOffset(1, scrollHeight);
        }
    }
}
