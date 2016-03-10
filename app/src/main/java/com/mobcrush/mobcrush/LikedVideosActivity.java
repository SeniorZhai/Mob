package com.mobcrush.mobcrush;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper.Event;
import com.mobcrush.mobcrush.mixpanel.Source;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.DividerItemDecoration;
import io.fabric.sdk.android.BuildConfig;

public class LikedVideosActivity extends MobcrushActivty {
    private User mUser;

    public static class LikedVideoFragment extends Fragment implements OnRefreshListener {
        private BroadcastAdapter mAdapter;
        private boolean mClearDataOnReceiveNew;
        private int mCurrentPageIndex;
        private boolean mDataIsLoading;
        private LayoutManager mLayoutManager;
        private TextView mLikesTextView;
        private View mProgress;
        private RecyclerView mRecyclerView;
        private SwipeRefreshLayout mSwipeRefreshLayout;
        private User mUser;
        private ErrorListener onErrorResponse = new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                LikedVideoFragment.this.mDataIsLoading = false;
                if (LikedVideoFragment.this.isAdded()) {
                    LikedVideoFragment.this.mProgress.setVisibility(8);
                    LikedVideoFragment.this.mSwipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(LikedVideoFragment.this.getActivity(), R.string.error_network_undeterminated, 1).show();
                }
            }
        };
        private Listener<Broadcast[]> onResponseBroadcasts = new Listener<Broadcast[]>() {
            public void onResponse(Broadcast[] response) {
                LikedVideoFragment.this.mDataIsLoading = false;
                if (response != null) {
                    if (LikedVideoFragment.this.mClearDataOnReceiveNew) {
                        LikedVideoFragment.this.mClearDataOnReceiveNew = false;
                        LikedVideoFragment.this.mAdapter.updateBroadcasts(response);
                    } else {
                        LikedVideoFragment.this.mAdapter.addBroadcasts(response, true);
                    }
                    LikedVideoFragment.this.mSwipeRefreshLayout.setRefreshing(false);
                }
                if (LikedVideoFragment.this.isAdded()) {
                    LikedVideoFragment.this.mProgress.setVisibility(8);
                }
            }
        };

        @SuppressLint({"ResourceAsColor"})
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            String user = getArguments().getString(Constants.EXTRA_USER);
            if (user != null) {
                this.mUser = (User) new Gson().fromJson(user, User.class);
            }
            this.mLikesTextView = (TextView) getActivity().findViewById(R.id.toolbar).findViewById(R.id.action_like);
            if (this.mLikesTextView != null) {
                this.mLikesTextView.setText(String.valueOf(this.mUser.likeCount));
            }
            View view = inflater.inflate(R.layout.fragment_liked_videos, container, false);
            this.mProgress = view.findViewById(R.id.loading_layout);
            UIUtils.colorizeProgress((ProgressBar) this.mProgress.findViewById(R.id.progressBar), getResources().getColor(R.color.yellow));
            this.mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refreshLayout);
            this.mSwipeRefreshLayout.setOnRefreshListener(this);
            this.mSwipeRefreshLayout.setProgressBackgroundColor(R.color.yellow);
            this.mSwipeRefreshLayout.setColorSchemeResources(R.color.dark);
            this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
            this.mRecyclerView.setHasFixedSize(false);
            this.mRecyclerView.addItemDecoration(new DividerItemDecoration(getResources().getDrawable(R.drawable.card_divider), false));
            this.mLayoutManager = new LinearLayoutManager(getActivity());
            this.mRecyclerView.setLayoutManager(this.mLayoutManager);
            this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            this.mAdapter = new BroadcastAdapter(getActivity(), new Broadcast[0], true, Source.LIKED);
            this.mAdapter.setOnNeedNextDataCallback(new Callback() {
                public boolean handleMessage(Message msg) {
                    if (msg != null && msg.what == 2) {
                        LikedVideoFragment.this.loadData();
                    }
                    return false;
                }
            });
            this.mRecyclerView.setAdapter(this.mAdapter);
            return view;
        }

        public void onResume() {
            super.onResume();
            loadData();
        }

        public void onRefresh() {
            this.mCurrentPageIndex = 0;
            this.mClearDataOnReceiveNew = true;
            loadData();
        }

        public void loadData() {
            int i = 0;
            if (!this.mDataIsLoading) {
                this.mDataIsLoading = true;
                if (this.mAdapter.getItemCount() == 0) {
                    this.mProgress.setVisibility(0);
                }
                int pageSize = 10;
                if (this.mClearDataOnReceiveNew && this.mAdapter.getItemCount() > 10) {
                    pageSize = this.mAdapter.getItemCount();
                }
                if (this.mClearDataOnReceiveNew || this.mAdapter.getItemCount() % 10 == 0) {
                    this.mSwipeRefreshLayout.setRefreshing(this.mClearDataOnReceiveNew);
                    FragmentActivity activity = getActivity();
                    User user = PreferenceUtility.getUser();
                    if (!this.mClearDataOnReceiveNew) {
                        i = this.mAdapter.getItemCount() / 10;
                    }
                    Network.getLikedBroadcasts(activity, user, i, pageSize, this.onResponseBroadcasts, this.onErrorResponse);
                    return;
                }
                this.mDataIsLoading = false;
            }
        }
    }

    public static Intent getIntent(Context context, User user) {
        Intent intent = new Intent(context, LikedVideosActivity.class);
        if (user != null) {
            intent.putExtra(Constants.EXTRA_USER, user.toString());
        }
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_liked_videos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.dark);
        ((TextView) toolbar.findViewById(R.id.title)).setText(R.string.title_activity_liked_videos);
        TextView tv = (TextView) toolbar.findViewById(R.id.action_like);
        tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_like_appbar_active, 0, 0, 0);
        tv.setText(BuildConfig.FLAVOR);
        try {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        String user = getIntent().getStringExtra(Constants.EXTRA_USER);
        if (user != null) {
            this.mUser = (User) new Gson().fromJson(user, User.class);
        }
        if (savedInstanceState == null) {
            Fragment f = new LikedVideoFragment();
            f.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add((int) R.id.container, f).commit();
        }
    }

    protected void onResume() {
        super.onResume();
        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_LIKES);
        MixpanelHelper.getInstance(MainApplication.getContext()).generateEvent(Event.VIEW_LIKED_VIDEOS);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
