package com.mobcrush.mobcrush;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.DataModel;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.mobcrush.mobcrush.logic.BroadcastLogicType;
import com.mobcrush.mobcrush.mixpanel.Source;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.player.Player;
import com.mobcrush.mobcrush.ui.DividerItemDecoration;
import com.mobcrush.mobcrush.ui.ScrollTabHolder;
import com.mobcrush.mobcrush.ui.ScrollTabHolderFragment;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Arrays;

public class BroadcastsFragment extends ScrollTabHolderFragment implements OnClickListener, OnRefreshListener {
    private static final String TAG = "BroadcastsFragment";
    private BroadcastAdapter mAdapter;
    private BroadcastLogicType mBroadcastLogicType;
    private Broadcast[] mBroadcasts;
    private boolean mClearDataOnReceiveNew;
    private boolean mDataIsLoading;
    private boolean mDataWasRequested;
    private Game mGame;
    private int mHeaderHeight;
    private boolean mIsVisibleToUser;
    private int mItemHeight;
    private boolean mItemWasDeleted;
    private int mItemsLoaded = 0;
    private long mLastUpdateTimestamp;
    private LinearLayoutManager mLayoutManager;
    private ArrayList<Integer> mListOfHookedPositions = new ArrayList();
    private View mLoadingView;
    private int mMinimalHeaderHeight;
    private boolean mNoMoreDataToLoad;
    private int mPosition;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private User mUser;
    private UserChannel mUserChannel;
    private ErrorListener onErrorResponse = new ErrorListener() {
        public void onErrorResponse(VolleyError error) {
            BroadcastsFragment.this.mDataIsLoading = false;
            if (BroadcastsFragment.this.isAdded()) {
                BroadcastsFragment.this.mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(BroadcastsFragment.this.getActivity(), R.string.error_network_undeterminated, 1).show();
            }
        }
    };
    private Listener<Broadcast[]> onResponseBroadcasts = new Listener<Broadcast[]>() {
        public void onResponse(Broadcast[] response) {
            Log.d(BroadcastsFragment.TAG, "onResponse. " + (response != null ? response.length + " items. " + Arrays.toString(response) : "null"));
            if (response != null) {
                boolean z;
                BroadcastsFragment broadcastsFragment = BroadcastsFragment.this;
                if (response.length < 10) {
                    z = true;
                } else {
                    z = false;
                }
                broadcastsFragment.mNoMoreDataToLoad = z;
                BroadcastsFragment.access$312(BroadcastsFragment.this, response.length);
            }
            if (BroadcastsFragment.this.isAdded()) {
                if (!(BroadcastsFragment.this.mLoadingView == null || BroadcastsFragment.this.mLoadingView.getVisibility() == 8)) {
                    BroadcastsFragment.this.mLoadingView.setVisibility(8);
                }
                BroadcastsFragment.this.mSwipeRefreshLayout.setRefreshing(false);
            }
            if (response != null) {
                if (BroadcastsFragment.this.mUser != null) {
                    Log.d("!!!", "onResponseBroadcasts: response.length= " + response.length + "; mUser.broadcastCount= " + BroadcastsFragment.this.mUser.getBroadcastCount());
                }
                if (BroadcastsFragment.this.mClearDataOnReceiveNew) {
                    BroadcastsFragment.this.mClearDataOnReceiveNew = false;
                    BroadcastsFragment.this.mListOfHookedPositions.clear();
                    BroadcastsFragment.this.mBroadcasts = null;
                    BroadcastsFragment.this.mAdapter.clearBroadcasts(false);
                    BroadcastsFragment.this.mAdapter.addBroadcasts(response, false);
                    if (!(BroadcastLogicType.New.equals(BroadcastsFragment.this.mBroadcastLogicType) || BroadcastLogicType.Popular.equals(BroadcastsFragment.this.mBroadcastLogicType))) {
                        BroadcastsFragment.this.scrollToTop();
                    }
                } else {
                    BroadcastsFragment.this.mAdapter.addBroadcasts(response, true);
                }
                ArrayList<Broadcast> broadcasts = new ArrayList();
                if (BroadcastsFragment.this.mBroadcasts != null && BroadcastsFragment.this.mBroadcasts.length > 0) {
                    broadcasts.addAll(Arrays.asList(BroadcastsFragment.this.mBroadcasts));
                }
                if (response.length > 0) {
                    broadcasts.addAll(Arrays.asList(response));
                }
                BroadcastsFragment.this.mBroadcasts = (Broadcast[]) broadcasts.toArray(new Broadcast[broadcasts.size()]);
                BroadcastsFragment.this.correctProgressPosition();
            }
            BroadcastsFragment.this.mDataIsLoading = false;
            if (BroadcastsFragment.this.mRecyclerView.getAdapter() == null) {
                Log.e(BroadcastsFragment.TAG, "Adapter is empty!");
            }
            Log.d(BroadcastsFragment.TAG, "total count: " + BroadcastsFragment.this.mAdapter.getDataItemCount());
        }
    };

    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType = new int[BroadcastLogicType.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.New.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.Popular.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.User.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.Channel.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[BroadcastLogicType.Game.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    static /* synthetic */ int access$312(BroadcastsFragment x0, int x1) {
        int i = x0.mItemsLoaded + x1;
        x0.mItemsLoaded = i;
        return i;
    }

    public static BroadcastsFragment newInstance(User user, int position) {
        return newInstance(BroadcastLogicType.User, user, position);
    }

    public static BroadcastsFragment newInstance(UserChannel userChannel, int position) {
        return newInstance(BroadcastLogicType.Channel, userChannel, position);
    }

    public static BroadcastsFragment newInstance(Game game, int position) {
        return newInstance(BroadcastLogicType.Game, game, position);
    }

    public static BroadcastsFragment newInstance(BroadcastLogicType logicType, DataModel dataModel, int position) {
        BroadcastsFragment fragment = new BroadcastsFragment();
        Bundle args = new Bundle();
        if (dataModel != null) {
            switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[logicType.ordinal()]) {
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    args.putString(Constants.EXTRA_USER, dataModel.toString());
                    break;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    args.putString(Constants.EXTRA_USER_CHANNEL, dataModel.toString());
                    break;
                case Player.STATE_ENDED /*5*/:
                    args.putString(Constants.EXTRA_GAME, dataModel.toString());
                    break;
            }
        }
        args.putString(Constants.EXTRA_BROADCASTS_LOGIC, logicType.toString());
        args.putInt(Constants.EXTRA_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onCreate(android.os.Bundle r7) {
        /*
        r6 = this;
        super.onCreate(r7);
        r3 = r6.getArguments();
        if (r3 == 0) goto L_0x0032;
    L_0x0009:
        r3 = r6.getArguments();	 Catch:{ Exception -> 0x004d }
        r4 = "extra_broadcasts_logic";
        r3 = r3.getString(r4);	 Catch:{ Exception -> 0x004d }
        r3 = com.mobcrush.mobcrush.logic.BroadcastLogicType.valueOf(r3);	 Catch:{ Exception -> 0x004d }
        r6.mBroadcastLogicType = r3;	 Catch:{ Exception -> 0x004d }
        r3 = com.mobcrush.mobcrush.BroadcastsFragment.AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType;	 Catch:{ Exception -> 0x004d }
        r4 = r6.mBroadcastLogicType;	 Catch:{ Exception -> 0x004d }
        r4 = r4.ordinal();	 Catch:{ Exception -> 0x004d }
        r3 = r3[r4];	 Catch:{ Exception -> 0x004d }
        switch(r3) {
            case 3: goto L_0x0033;
            case 4: goto L_0x0093;
            case 5: goto L_0x00ae;
            default: goto L_0x0026;
        };
    L_0x0026:
        r3 = r6.getArguments();
        r4 = "extra_position";
        r3 = r3.getInt(r4);
        r6.mPosition = r3;
    L_0x0032:
        return;
    L_0x0033:
        r3 = new com.google.gson.Gson;	 Catch:{ Exception -> 0x004d }
        r3.<init>();	 Catch:{ Exception -> 0x004d }
        r4 = r6.getArguments();	 Catch:{ Exception -> 0x004d }
        r5 = "extra_user";
        r4 = r4.getString(r5);	 Catch:{ Exception -> 0x004d }
        r5 = com.mobcrush.mobcrush.datamodel.User.class;
        r3 = r3.fromJson(r4, r5);	 Catch:{ Exception -> 0x004d }
        r3 = (com.mobcrush.mobcrush.datamodel.User) r3;	 Catch:{ Exception -> 0x004d }
        r6.mUser = r3;	 Catch:{ Exception -> 0x004d }
        goto L_0x0026;
    L_0x004d:
        r0 = move-exception;
        r2 = "UsersFragment initialization error. ";
        r3 = r6.getArguments();
        r4 = "extra_user";
        r3 = r3.containsKey(r4);
        if (r3 == 0) goto L_0x00c9;
    L_0x005c:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r3 = r3.append(r2);
        r4 = "EXTRA_USER:";
        r3 = r3.append(r4);
        r4 = r6.getArguments();
        r5 = "extra_user";
        r4 = r4.getString(r5);
        r3 = r3.append(r4);
        r2 = r3.toString();
    L_0x007d:
        r1 = new java.lang.Exception;
        r1.<init>(r2, r0);
        com.crashlytics.android.Crashlytics.logException(r1);
        r1.printStackTrace();
        r3 = com.mobcrush.mobcrush.logic.BroadcastLogicType.User;
        r6.mBroadcastLogicType = r3;
        r3 = com.mobcrush.mobcrush.common.PreferenceUtility.getUser();
        r6.mUser = r3;
        goto L_0x0026;
    L_0x0093:
        r3 = new com.google.gson.Gson;	 Catch:{ Exception -> 0x004d }
        r3.<init>();	 Catch:{ Exception -> 0x004d }
        r4 = r6.getArguments();	 Catch:{ Exception -> 0x004d }
        r5 = "extra_user_channel";
        r4 = r4.getString(r5);	 Catch:{ Exception -> 0x004d }
        r5 = com.mobcrush.mobcrush.datamodel.UserChannel.class;
        r3 = r3.fromJson(r4, r5);	 Catch:{ Exception -> 0x004d }
        r3 = (com.mobcrush.mobcrush.datamodel.UserChannel) r3;	 Catch:{ Exception -> 0x004d }
        r6.mUserChannel = r3;	 Catch:{ Exception -> 0x004d }
        goto L_0x0026;
    L_0x00ae:
        r3 = new com.google.gson.Gson;	 Catch:{ Exception -> 0x004d }
        r3.<init>();	 Catch:{ Exception -> 0x004d }
        r4 = r6.getArguments();	 Catch:{ Exception -> 0x004d }
        r5 = "extra_game";
        r4 = r4.getString(r5);	 Catch:{ Exception -> 0x004d }
        r5 = com.mobcrush.mobcrush.datamodel.Game.class;
        r3 = r3.fromJson(r4, r5);	 Catch:{ Exception -> 0x004d }
        r3 = (com.mobcrush.mobcrush.datamodel.Game) r3;	 Catch:{ Exception -> 0x004d }
        r6.mGame = r3;	 Catch:{ Exception -> 0x004d }
        goto L_0x0026;
    L_0x00c9:
        r3 = r6.getArguments();
        r4 = "extra_user_channel";
        r3 = r3.containsKey(r4);
        if (r3 == 0) goto L_0x00f7;
    L_0x00d5:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r3 = r3.append(r2);
        r4 = "EXTRA_USER_CHANNEL:";
        r3 = r3.append(r4);
        r4 = r6.getArguments();
        r5 = "extra_user_channel";
        r4 = r4.getString(r5);
        r3 = r3.append(r4);
        r2 = r3.toString();
        goto L_0x007d;
    L_0x00f7:
        r3 = r6.getArguments();
        r4 = "extra_game";
        r3 = r3.containsKey(r4);
        if (r3 == 0) goto L_0x007d;
    L_0x0103:
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r3 = r3.append(r2);
        r4 = "EXTRA_GAME:";
        r3 = r3.append(r4);
        r4 = r6.getArguments();
        r5 = "extra_game";
        r4 = r4.getString(r5);
        r3 = r3.append(r4);
        r2 = r3.toString();
        goto L_0x007d;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mobcrush.mobcrush.BroadcastsFragment.onCreate(android.os.Bundle):void");
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mItemHeight = getResources().getDimensionPixelSize(R.dimen.user_item_height);
        applyHeights();
        if (!this.mDataWasRequested) {
            loadData();
        }
    }

    @SuppressLint({"ResourceAsColor"})
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_broadcasts, container, false);
        this.mLoadingView = view.findViewById(R.id.loading_layout);
        UIUtils.colorizeProgress((ProgressBar) this.mLoadingView.findViewById(R.id.progressBar), getResources().getColor(R.color.yellow));
        this.mLoadingView.setVisibility(0);
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
        this.mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.yellow);
        this.mSwipeRefreshLayout.setColorSchemeResources(R.color.dark);
        this.mSwipeRefreshLayout.setEnabled(true);
        this.mSwipeRefreshLayout.setOnRefreshListener(this);
        this.mAdapter = new BroadcastAdapter(getActivity(), new Broadcast[0], true, Source.fromBroadcastLogicType(this.mBroadcastLogicType));
        this.mAdapter.setOnEventsListener(new Callback() {
            public boolean handleMessage(Message msg) {
                if (msg != null) {
                    switch (msg.what) {
                        case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                            return true;
                        case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                            BroadcastsFragment.this.mItemWasDeleted = true;
                            BroadcastsFragment.this.mListOfHookedPositions.clear();
                            BroadcastsFragment.this.getActivity().setIntent(new Intent(Constants.ACTION_UPDATE_USER_BASE_INFO));
                            return true;
                        case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                            BroadcastsFragment.this.mAdapter.clearBroadcasts(false);
                            BroadcastsFragment.this.mAdapter.addBroadcasts(BroadcastsFragment.this.mBroadcasts, true);
                            return true;
                    }
                }
                return false;
            }
        });
        if (BroadcastLogicType.User.equals(this.mBroadcastLogicType)) {
            Log.d("!!!", "disableOptionMenu SKIPPED");
        } else {
            this.mAdapter.disableOptionMenu();
            Log.d("!!!", "disableOptionMenu");
        }
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        if (!(BroadcastLogicType.New.equals(this.mBroadcastLogicType) || BroadcastLogicType.Popular.equals(this.mBroadcastLogicType))) {
            this.mRecyclerView.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.card_divider_height));
        }
        this.mLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mRecyclerView.setHasFixedSize(false);
        this.mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.card_divider), true));
        this.mRecyclerView.addOnScrollListener(new OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int pos = BroadcastsFragment.this.mLayoutManager.findFirstVisibleItemPosition();
                BroadcastsFragment.this.correctProgressPosition();
                if (BroadcastsFragment.this.mScrollTabHolder != null) {
                    BroadcastsFragment.this.mScrollTabHolder.onScroll(BroadcastsFragment.this.mRecyclerView.getChildAt(0), pos, BroadcastsFragment.this.mLayoutManager.findLastVisibleItemPosition() - pos, BroadcastsFragment.this.mAdapter.getItemCount(), BroadcastsFragment.this.mPosition);
                }
                if (pos >= BroadcastsFragment.this.mRecyclerView.getAdapter().getItemCount() - 5 && !BroadcastsFragment.this.mListOfHookedPositions.contains(Integer.valueOf(pos)) && !BroadcastsFragment.this.mDataIsLoading) {
                    BroadcastsFragment.this.mListOfHookedPositions.add(Integer.valueOf(pos));
                    BroadcastsFragment.this.loadData();
                }
            }
        });
        this.mRecyclerView.setAdapter(this.mAdapter);
        return view;
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[this.mBroadcastLogicType.ordinal()]) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_WATCH_FEED_NEW);
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_WATCH_FEED_POPULAR);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_PROFILE_BROADCASTS);
                    break;
                case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                    GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_TEAM_VIDEOS);
                    break;
                case Player.STATE_ENDED /*5*/:
                    GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_GAME_VIDEOS);
                    break;
            }
        }
        this.mIsVisibleToUser = isVisibleToUser;
        if (isVisibleToUser && isAdded()) {
            loadData(true);
        }
    }

    public void onDetach() {
        super.onDetach();
    }

    public void onClick(View view) {
    }

    public void onRefresh() {
        loadData(true);
    }

    public BroadcastLogicType getLogicType() {
        return this.mBroadcastLogicType;
    }

    public void setUser(User user, boolean forceUpdate) {
        this.mBroadcastLogicType = BroadcastLogicType.User;
        Log.d(TAG, "BroadcastsFragment.setUser: mUser: " + (this.mUser != null ? this.mUser.toString() : "null") + " user: " + (user != null ? user.toString() : BuildConfig.FLAVOR) + "; forceUpdate: " + forceUpdate);
        this.mUser = user;
        if (forceUpdate) {
            this.mClearDataOnReceiveNew = true;
            loadData(user);
        }
    }

    public void scrollToTop() {
        if (this.mRecyclerView != null) {
            this.mRecyclerView.scrollToPosition(0);
        }
    }

    public synchronized void loadData(User user) {
        this.mBroadcastLogicType = BroadcastLogicType.User;
        if (this.mUser == null || this.mUser.equals(user)) {
            this.mDataIsLoading = false;
            if (this.mAdapter != null) {
                this.mAdapter.clearBroadcasts(true);
            }
        }
        this.mUser = user;
        loadData();
    }

    public void setChannel(UserChannel userChannel) {
        this.mBroadcastLogicType = BroadcastLogicType.Channel;
        this.mUserChannel = userChannel;
        this.mDataIsLoading = false;
        ScrollTabHolder scrollTabHolder = this.mScrollTabHolder;
        this.mScrollTabHolder = null;
        adjustScroll(0);
        this.mAdapter.clearBroadcasts(true);
        loadData();
        this.mScrollTabHolder = scrollTabHolder;
    }

    public void setGame(Game game) {
        this.mBroadcastLogicType = BroadcastLogicType.Game;
        this.mGame = game;
        this.mDataIsLoading = false;
        ScrollTabHolder scrollTabHolder = this.mScrollTabHolder;
        this.mScrollTabHolder = null;
        adjustScroll(0);
        this.mAdapter.clearBroadcasts(true);
        loadData();
        this.mScrollTabHolder = scrollTabHolder;
    }

    public synchronized void loadData(boolean force) {
        if (force) {
            this.mClearDataOnReceiveNew = true;
            this.mNoMoreDataToLoad = false;
        }
        loadData();
    }

    public synchronized void loadData() {
        Log.w(TAG, "load data");
        if (this.mAdapter == null) {
            Log.w(TAG, "mAdapter is empty!");
        } else {
            this.mDataWasRequested = true;
            if (!(this.mDataIsLoading || (this.mUser == null && this.mUserChannel == null && this.mGame == null && !BroadcastLogicType.New.equals(this.mBroadcastLogicType) && !BroadcastLogicType.Popular.equals(this.mBroadcastLogicType)))) {
                this.mLastUpdateTimestamp = System.currentTimeMillis();
                if (this.mAdapter.getDataItemCount() == 0) {
                    this.mDataIsLoading = true;
                    Log.d(TAG, "loadData from 0");
                    switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[this.mBroadcastLogicType.ordinal()]) {
                        case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                            Network.cancelBroadcasts();
                            Network.getBroadcasts(getActivity(), 0, 10, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            Network.cancelPopularBroadcasts();
                            Network.getPopularBroadcasts(getActivity(), 0, 10, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                            Network.getUserBroadcasts(getActivity(), this.mUser._id, 0, 10, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                            Network.getChannelBroadcasts(getActivity(), this.mUserChannel.channel._id, 0, 10, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        case Player.STATE_ENDED /*5*/:
                            Network.getGameBroadcasts(getActivity(), this.mGame._id, 0, 10, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        default:
                            this.mDataIsLoading = false;
                            break;
                    }
                }
                int pageSize = 10;
                if (this.mClearDataOnReceiveNew && this.mAdapter.getDataItemCount() > 10) {
                    pageSize = this.mAdapter.getDataItemCount();
                }
                if (this.mClearDataOnReceiveNew) {
                    this.mItemsLoaded = 0;
                }
                int pageNo = (int) ((((double) this.mItemsLoaded) / 10.0d) + 0.5d);
                if (this.mClearDataOnReceiveNew || !this.mNoMoreDataToLoad || this.mItemWasDeleted) {
                    this.mDataIsLoading = true;
                    this.mItemWasDeleted = false;
                    Log.d(TAG, "loadData from " + pageNo + ". Total count: " + this.mAdapter.getDataItemCount());
                    switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$BroadcastLogicType[this.mBroadcastLogicType.ordinal()]) {
                        case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                            Network.getBroadcasts(getActivity(), pageNo, pageSize, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            Network.getPopularBroadcasts(getActivity(), pageNo, pageSize, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                            Network.getUserBroadcasts(getActivity(), this.mUser._id, pageNo, pageSize, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                            Network.getUserBroadcasts(getActivity(), this.mUserChannel.channel._id, pageNo, pageSize, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        case Player.STATE_ENDED /*5*/:
                            Network.getGameBroadcasts(getActivity(), this.mGame._id, pageNo, pageSize, this.onResponseBroadcasts, this.onErrorResponse);
                            break;
                        default:
                            this.mDataIsLoading = false;
                            break;
                    }
                }
                this.mDataIsLoading = false;
                if (this.mSwipeRefreshLayout != null) {
                    Log.d(TAG, "Refresh shown: " + this.mDataIsLoading);
                    this.mSwipeRefreshLayout.setRefreshing(this.mDataIsLoading);
                }
            }
        }
    }

    public void setHeaderHeight(int height, int minimalHeight) {
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
        if (getView() == null) {
            return;
        }
        if (this.mHeaderHeight > 0 || this.mMinimalHeaderHeight > 0) {
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
    }

    public void adjustScroll(int scrollHeight) {
        Log.d(TAG, "adjustScroll: " + scrollHeight);
        this.mRecyclerView.stopScroll();
        if (scrollHeight > this.mMinimalHeaderHeight || this.mLayoutManager.findFirstVisibleItemPosition() < 1) {
            this.mLayoutManager.scrollToPositionWithOffset(1, scrollHeight);
        }
    }

    private void correctProgressPosition() {
        if (isAdded() && this.mRecyclerView != null && this.mRecyclerView.getChildCount() > 0 && !isNewOrPopularBroadcasts()) {
            int offset;
            View v = this.mRecyclerView.getChildAt(0);
            if (v == null) {
                offset = this.mMinimalHeaderHeight;
            } else {
                offset = this.mHeaderHeight + v.getTop();
                if (offset <= 0 || v.getVisibility() == 0) {
                    offset = this.mMinimalHeaderHeight;
                }
            }
            this.mSwipeRefreshLayout.setProgressViewOffset(false, offset, offset);
        }
    }

    private boolean isNewOrPopularBroadcasts() {
        return BroadcastLogicType.New.equals(this.mBroadcastLogicType) || BroadcastLogicType.Popular.equals(this.mBroadcastLogicType);
    }
}
