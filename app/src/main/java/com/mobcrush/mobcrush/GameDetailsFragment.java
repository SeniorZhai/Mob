package com.mobcrush.mobcrush;

import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.LightingColorFilter;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnSystemUiVisibilityChangeListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.google.android.exoplayer.util.MpegAudioHeader;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.AnimationUtils;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.Config;
import com.mobcrush.mobcrush.datamodel.EntityType;
import com.mobcrush.mobcrush.helper.ShareHelper;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.mixpanel.Source;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.BuildConfig;

public class GameDetailsFragment extends PlayerFragment implements OnClickListener, OnTouchListener {
    private static final int HIDE_CONTROLS_TIMEOUT = 2000;
    private static final int REQUEST_CHAT = 4;
    private static final int REQUEST_LOGIN = 3;
    private static final int REQUEST_MODERATION = 5;
    private static final String TAG = "GameDetailsFrgmt";
    private static final String TAG_VIDEO = "GameDetailsFrgmt.VIDEO";
    private static final int VIDEO_RENEW_TIMOUT = 10000;
    private Broadcast mBroadcast;
    private View mBroadcastEndedView;
    private Handler mBroadcastHandler = new Handler();
    private boolean mBroadcastLiveEnded;
    private ImageView mChatBtn;
    private View mChatLayout;
    private int mDefaultVideoHeight;
    private boolean mDoNotHideToolbar = false;
    private long mEnterTimestamp;
    private boolean mFirstStartOfVideo = true;
    private TextView mFollow;
    private ImageView mFullscreenChatBtn;
    private Handler mHandler = new Handler();
    private boolean mHideChatWithMediaController;
    private boolean mIsFollower = false;
    private int mLastVideoHeight = -1;
    private TextView mLikesText;
    private int mPrevPlayerState;
    private ProgressBar mProgressView;
    private View mRoot;
    private OnItemClickListener mShareMenuClickListener;
    private boolean mShowChat;
    private View mShutterView;
    private Source mSource;
    private boolean mSystemUIShown = true;
    private View mToolbar;
    private boolean mToolbarShowingIsDisabled = false;
    private View mToolbarUnderlayer;
    private View mUserLayout;
    private TextView mUserNameText;
    private Handler mVideoHandler = new Handler();
    private boolean mVideoOverlayWasShown = false;
    private View mVideoProgress;
    private boolean mWasMoveGesture = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String broadcast = null;
        if (getArguments() != null) {
            broadcast = getArguments().getString(Constants.EXTRA_BROADCAST);
            if (broadcast != null) {
                this.mBroadcast = (Broadcast) new Gson().fromJson(broadcast, Broadcast.class);
            }
            String source = getArguments().getString(Constants.EXTRA_SOURCE);
            if (source != null) {
                this.mSource = Source.valueOf(source);
            }
        }
        Exception e;
        try {
            if (this.mBroadcast == null || this.mBroadcast._id == null) {
                e = new IllegalArgumentException("Broadcast and broadcast._id can't be empty! Broadcast: " + broadcast);
                e.printStackTrace();
                Crashlytics.logException(e);
                Toast.makeText(getActivity(), R.string.error_network_undeterminated, 1).show();
                getActivity().finish();
            }
            if (this.mBroadcast.isLive) {
                this.mBroadcastWasLive = true;
            }
            if (this.mBroadcast.chatRoom == null) {
                e = new IllegalArgumentException("Broadcast and mBroadcast.chatRoom can't be empty! Broadcast: " + broadcast);
                e.printStackTrace();
                Crashlytics.logException(e);
                Toast.makeText(getActivity(), R.string.error_network_undeterminated, 1).show();
                getActivity().finish();
            }
        } catch (Exception e2) {
            e2.printStackTrace();
            Crashlytics.logException(e2);
        }
        this.mBroadcastId = this.mBroadcast._id;
        this.mChatRoom = this.mBroadcast.chatRoom;
        try {
            GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_GAME_DETAILS);
            this.mEnterTimestamp = System.currentTimeMillis();
            GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_ENTER, this.mBroadcastId, Long.valueOf(this.mEnterTimestamp));
            GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_VIEW, this.mBroadcastId, Long.valueOf(this.mEnterTimestamp));
            GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_GAME, this.mBroadcastId + ":" + (this.mBroadcast.game != null ? this.mBroadcast.game.name : BuildConfig.FLAVOR), null);
        } catch (Throwable e3) {
            e3.printStackTrace();
            Crashlytics.logException(e3);
        }
        if (this.mBroadcast != null && this.mBroadcast._id != null) {
            Network.updateViewerCount(getActivity(), this.mBroadcast._id, true, null, null);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int i;
        int i2 = 8;
        this.mToolbar = getActivity().findViewById(R.id.toolbar_layout);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((TextView) toolbar.findViewById(R.id.title)).setText(this.mBroadcast.game != null ? this.mBroadcast.game.name : BuildConfig.FLAVOR);
            this.mLikesText = (TextView) toolbar.findViewById(R.id.action_like);
            this.mLikesText.setOnClickListener(this);
            configLikesView();
        }
        this.mRoot = inflater.inflate(R.layout.fragment_game_details, container, false);
        this.mToolbarUnderlayer = this.mRoot.findViewById(R.id.toolbar_underlayer);
        Drawable d = ((ProgressBar) this.mRoot.findViewById(R.id.progress_video)).getIndeterminateDrawable();
        d.setColorFilter(new LightingColorFilter(ViewCompat.MEASURED_STATE_MASK, getResources().getColor(R.color.progress_color)));
        ((ProgressBar) this.mRoot.findViewById(R.id.progress_video)).setIndeterminateDrawable(d);
        this.mShutterView = this.mRoot.findViewById(R.id.shutter_view);
        this.mBroadcastEndedView = this.mRoot.findViewById(R.id.broadcast_ended_text);
        this.mRecyclerView = (RecyclerView) this.mRoot.findViewById(R.id.recycler_view);
        this.mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), 1, false));
        if (this.mMessagesAdapter == null) {
            this.mMessagesAdapter = new ChatMessagesAdapter(getActivity(), null);
        }
        this.mMessagesAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                applyChanges();
            }

            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                applyChanges();
            }

            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                applyChanges();
            }

            public void onChanged() {
                super.onChanged();
                applyChanges();
            }

            private void applyChanges() {
                if (GameDetailsFragment.this.isAdded()) {
                    GameDetailsFragment.this.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            if (GameDetailsFragment.this.mChatBtn != null) {
                                GameDetailsFragment.this.mChatBtn.setVisibility(GameDetailsFragment.this.mMessagesAdapter.getItemCount() > 0 ? 0 : 8);
                            }
                            if (GameDetailsFragment.this.mVideoController != null) {
                                GameDetailsFragment.this.mVideoController.updateChat();
                            }
                        }
                    });
                }
            }
        });
        this.mRecyclerView.setAdapter(this.mMessagesAdapter);
        this.mRecyclerView.setOnTouchListener(this);
        this.mChatLayout = this.mRoot.findViewById(R.id.chat_layout);
        this.mEditLayout = this.mRoot.findViewById(R.id.edit_layout);
        this.mChatMessageText = (TextView) this.mRoot.findViewById(R.id.chat_message);
        this.mChatMessageText.setOnClickListener(this);
        this.mUserLayout = this.mRoot.findViewById(R.id.user_info_layout);
        this.mUserNameText = (TextView) this.mRoot.findViewById(R.id.user_name_text);
        this.mUserNameText.setText(this.mBroadcast.user != null ? this.mBroadcast.user.username : BuildConfig.FLAVOR);
        this.mUserNameText.setOnClickListener(this);
        this.mFullscreenChatBtn = (ImageView) this.mRoot.findViewById(R.id.fullscreen_chat_btn);
        this.mFullscreenChatBtn.setOnClickListener(this);
        this.mSlowModeBtn = (ImageView) this.mRoot.findViewById(R.id.slow_mode_btn);
        this.mSlowModeBtn.setOnClickListener(this);
        ImageView imageView = this.mSlowModeBtn;
        if (isCurrentUserOwner()) {
            i = 0;
        } else {
            i = 8;
        }
        imageView.setVisibility(i);
        this.mChatBtn = (ImageView) this.mRoot.findViewById(R.id.chat_btn);
        this.mChatBtn.setOnClickListener(this);
        imageView = this.mChatBtn;
        if (this.mMessagesAdapter.getItemCount() > 0) {
            i = 0;
        } else {
            i = 8;
        }
        imageView.setVisibility(i);
        this.mChatOptionsBtn = (ImageView) this.mRoot.findViewById(R.id.chat_options_btn);
        this.mChatOptionsBtn.setOnClickListener(this);
        this.mProgressView = (ProgressBar) this.mRoot.findViewById(R.id.progress);
        UIUtils.colorizeProgress(this.mProgressView, getResources().getColor(R.color.dark));
        this.mFollow = (TextView) this.mRoot.findViewById(R.id.action_follow);
        this.mFollow.setTypeface(UIUtils.getTypeface(getActivity(), Constants.FOLLOW_TYPEFACE));
        TextView textView = this.mFollow;
        if (!PreferenceUtility.getUser()._id.equals(this.mBroadcast.user._id)) {
            i2 = 0;
        }
        textView.setVisibility(i2);
        this.mFollow.setOnClickListener(this);
        this.mVideoLayout = (RelativeLayout) this.mRoot.findViewById(R.id.video_layout);
        this.mVideoProgress = this.mRoot.findViewById(R.id.progress_video_layout);
        this.mSurfaceView = (SurfaceView) this.mRoot.findViewById(R.id.surface_view);
        this.mSurfaceView.setOnTouchListener(this);
        if (!(this.mBroadcast == null || this.mBroadcast.height == 0 || this.mBroadcast.width == 0)) {
            this.mVideoWidth = this.mBroadcast.width;
            this.mVideoHeight = this.mBroadcast.height;
            this.mPixelWidthAspectRatio = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
            correctVideoAspect(this.mVideoHeight);
        }
        getActivity().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                Log.d(GameDetailsFragment.TAG, "onSystemUiVisibilityChange: " + visibility);
                if (visibility == 0 && !UIUtils.isLandscape(GameDetailsFragment.this.getActivity())) {
                }
            }
        });
        return this.mRoot;
    }

    public void onResume() {
        super.onResume();
        StringBuilder append = new StringBuilder().append("Broadcast onResume ");
        String str = (this.mBroadcast == null || this.mBroadcast._id == null) ? " null" : this.mBroadcast._id;
        Crashlytics.log(append.append(str).toString());
        if (this.mBroadcast == null || this.mBroadcast._id == null) {
            try {
                getActivity().finish();
                return;
            } catch (Throwable th) {
            }
        }
        actualizeBroadcastToPlay();
        checkIfFollower();
        hideSystemUI();
        updateViewersAndLikesCount();
    }

    public void onPause() {
        try {
            if (this.mHandler != null) {
                this.mHandler.removeCallbacksAndMessages(null);
            }
            if (this.mVideoHandler != null) {
                this.mVideoHandler.removeCallbacksAndMessages(null);
            }
            StringBuilder append = new StringBuilder().append("Broadcast onPause ");
            String str = (this.mBroadcast == null || this.mBroadcast._id == null) ? " null" : this.mBroadcast._id;
            Crashlytics.log(append.append(str).toString());
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        super.onPause();
    }

    public void onDestroy() {
        try {
            if (this.mBroadcast != null) {
                Network.updateViewerCount(getActivity(), this.mBroadcast._id, false, null, null);
                long timestamp = System.currentTimeMillis();
                GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_DURATION, this.mBroadcastId, Long.valueOf(timestamp - this.mEnterTimestamp));
                GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_LEAVE, this.mBroadcastId, Long.valueOf(timestamp));
                GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_CHATS, this.mBroadcastId, Long.valueOf(this.mSentMessagesCount));
                generateWatchBroadcastEvent();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHAT) {
            this.mReleaseChatOnPause = true;
            this.mShouldReleasePlayer = true;
            this.mToolbarShowingIsDisabled = false;
            if (resultCode == 0) {
                getActivity().onBackPressed();
                getActivity().overridePendingTransition(0, 0);
                return;
            }
            this.mSentMessagesCount++;
            UIUtils.fadeOut(this.mShutterView);
            UIUtils.fadeIn(this.mUserLayout);
            UIUtils.fadeIn(this.mChatLayout);
            scrollToLatestMessage(true);
        } else if (requestCode == REQUEST_MODERATION) {
            this.mReleaseChatOnPause = true;
            this.mShouldReleasePlayer = true;
            this.mToolbarShowingIsDisabled = false;
            scrollToLatestMessage(true);
        } else if (requestCode == REQUEST_LOGIN && resultCode == -1) {
            this.mMessagesAdapter.clearDisabledActions();
        }
    }

    public void onClick(View view) {
        boolean z = true;
        Object obj = null;
        switch (view.getId()) {
            case R.id.user_name_text:
                try {
                    startActivity(ProfileActivity.getIntent(getActivity(), this.mBroadcast.user));
                    return;
                } catch (Throwable e) {
                    StringBuilder append = new StringBuilder().append("Error while starting ProfileActivity for user: ");
                    if (this.mBroadcast != null) {
                        obj = this.mBroadcast.user;
                    }
                    Throwable e2 = new Exception(append.append(obj).toString(), e);
                    e2.printStackTrace();
                    Crashlytics.logException(e2);
                    return;
                }
            case R.id.action_follow:
                this.mVideoHandler.removeCallbacksAndMessages(null);
                if (Network.isLoggedIn()) {
                    this.mProgressView.setVisibility(0);
                    Network.follow(getActivity(), !this.mIsFollower, EntityType.user, this.mBroadcast.user._id, new Listener<Boolean>() {
                        public void onResponse(Boolean response) {
                            GameDetailsFragment.this.mProgressView.setVisibility(8);
                            if (response != null) {
                                GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_FOLLOW, GameDetailsFragment.this.mBroadcastId, Long.valueOf(1));
                                GameDetailsFragment.this.mIsFollower = response.booleanValue();
                            }
                            GameDetailsFragment.this.configFollowView();
                        }
                    }, null);
                    return;
                }
                startActivityForResult(LoginActivity.getIntent(getActivity()), REQUEST_LOGIN);
                return;
            case R.id.fullscreen_chat_btn:
                this.mShowChat = false;
                configUIVisibility();
                return;
            case R.id.chat_message:
                if (!Network.isLoggedIn()) {
                    startActivityForResult(LoginActivity.getIntent(getActivity()), REQUEST_LOGIN);
                    return;
                } else if (!PreferenceUtility.isEmailVerified()) {
                    startActivity(new Intent(getActivity(), EmailVerificationRequestActivity.class));
                    return;
                }
                break;
            case R.id.slow_mode_btn:
                toggleSlowMode();
                return;
            case R.id.chat_btn:
                break;
            case R.id.chat_options_btn:
                String broadcasterID;
                this.mShouldReleasePlayer = false;
                this.mReleaseChatOnPause = false;
                if (this.mBroadcast == null || this.mBroadcast.user == null) {
                    broadcasterID = null;
                } else {
                    broadcasterID = this.mBroadcast.user._id;
                }
                startActivityForResult(ChannelModerationActivity.getIntent(getActivity(), getChatChannelId(), broadcasterID, this.mUserIsModerator), REQUEST_MODERATION);
                return;
            case R.id.action_like:
                if (!Network.isLoggedIn()) {
                    startActivityForResult(LoginActivity.getIntent(getActivity()), REQUEST_LOGIN);
                    return;
                } else if (PreferenceUtility.isEmailVerified()) {
                    Network.like(getActivity(), !this.mBroadcast.currentLiked, this.mBroadcast, new Listener<Boolean>() {
                        public void onResponse(Boolean response) {
                            if (response != null) {
                                GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_LIKE, GameDetailsFragment.this.mBroadcastId, Long.valueOf(1));
                                GameDetailsFragment.this.mBroadcast.currentLiked = response.booleanValue();
                                Broadcast access$100 = GameDetailsFragment.this.mBroadcast;
                                access$100.likes = (GameDetailsFragment.this.mBroadcast.currentLiked ? 1 : -1) + access$100.likes;
                                GameDetailsFragment.this.configLikesView();
                            }
                            if (GameDetailsFragment.this.mBroadcast.currentLiked) {
                                GameDetailsFragment.this.sendMessage(MainApplication.getRString(R.string.action_like, new Object[0]).toUpperCase(), true);
                            }
                        }
                    }, new ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            String str = GameDetailsFragment.TAG;
                            String message = (error == null || error.getMessage() == null) ? BuildConfig.FLAVOR : error.getMessage();
                            Log.e(str, message);
                        }
                    });
                    return;
                } else {
                    startActivity(new Intent(getActivity(), EmailVerificationRequestActivity.class));
                    return;
                }
            default:
                return;
        }
        hideSystemUI();
        this.mShouldReleasePlayer = false;
        this.mReleaseChatOnPause = false;
        UIUtils.fadeIn(this.mShutterView);
        UIUtils.fadeOut(this.mUserLayout);
        this.mChatLayout.setVisibility(REQUEST_CHAT);
        this.mToolbarShowingIsDisabled = true;
        try {
            String str;
            Context activity = getActivity();
            if (this.mBroadcast == null || this.mBroadcast.game == null) {
                str = null;
            } else {
                str = this.mBroadcast.game.name;
            }
            Broadcast broadcast = this.mBroadcast;
            int top = this.mRecyclerView.getTop();
            if (view.getId() != R.id.chat_message || this.mCurrentUserIsMuted) {
                z = false;
            }
            Intent intent = ChatActivity.getIntent(activity, str, broadcast, top, z, isLiveVideo(), this.mUserIsModerator, this.mChatLimit);
            Crashlytics.log("Starting ChatActivity " + intent);
            startActivityForResult(intent, REQUEST_CHAT);
        } catch (Exception e3) {
            e3.printStackTrace();
            Crashlytics.logException(new Exception("Start ChatActivity failed", e3));
        }
    }

    public boolean onTouch(View view, MotionEvent motionEvent) {
        try {
            if (this.mVideoController == null) {
                return false;
            }
            int id = view.getId();
            if (this.mVideoController.isShowing() || !(id == R.id.pause || id == R.id.mediacontroller_progress || id == R.id.fullscreen)) {
                if (motionEvent.getAction() == 0 && view != this.mRecyclerView && view != this.mUserLayout && view != this.mEditLayout) {
                    this.mHandler.removeCallbacksAndMessages(null);
                    this.mWasMoveGesture = false;
                } else if (motionEvent.getAction() == 2) {
                    this.mWasMoveGesture = true;
                    if (!(!this.mVideoController.isShowing() || view == this.mRecyclerView || view == this.mUserLayout || view == this.mEditLayout)) {
                        hideSystemUIDelayed();
                    }
                } else if (motionEvent.getAction() == 1) {
                    if (id == R.id.surface_view || id == R.id.pause || id == R.id.mediacontroller_progress || id == R.id.fullscreen) {
                        showSystemUI(true);
                    } else if (id == R.id.share) {
                        showSystemUI(false);
                    } else if (view == this.mVideoController) {
                        if (this.mSystemUIShown) {
                            hideSystemUI();
                        } else {
                            showSystemUI(true);
                        }
                    } else if (view == this.mRecyclerView && !this.mWasMoveGesture && this.mMessagesAdapter.getItemCount() > 0) {
                        this.mChatBtn.performClick();
                    }
                }
                if (view.getId() == R.id.surface_view || view == this.mVideoController) {
                    return true;
                }
                return false;
            }
            showSystemUI(true);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            return false;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mHandler.removeCallbacksAndMessages(null);
        if (isAdded() && !isRemoving()) {
            hideSystemUI();
        }
    }

    private void actualizeBroadcastToPlay() {
        if (isLiveVideo()) {
            Network.getBroadcast(getActivity(), this.mBroadcast._id, new Listener<Broadcast>() {
                public void onResponse(Broadcast response) {
                    if (response != null) {
                        GameDetailsFragment.this.mBroadcast = response;
                        if (GameDetailsFragment.this.mBroadcast.isLive) {
                            GameDetailsFragment.this.mBroadcastWasLive = true;
                        }
                        GameDetailsFragment.this.mChatRoom = response.chatRoom;
                        if (GameDetailsFragment.this.mChatRoom == null) {
                            try {
                                throw new IllegalArgumentException("Broadcast and mBroadcast.chatRoom can't be empty! Broadcast: " + response);
                            } catch (Exception e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }
                        GameDetailsFragment.this.prepareVideoToPlay();
                        return;
                    }
                    GameDetailsFragment.this.mBroadcast.isLive = false;
                    GameDetailsFragment.this.onDeletedBroadcast();
                }
            }, new ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    GameDetailsFragment.this.mBroadcast.isLive = false;
                    GameDetailsFragment.this.prepareVideoToPlay();
                }
            });
        } else {
            prepareVideoToPlay();
        }
    }

    public void toggleFullScreen() {
        super.toggleFullScreen();
        if (this.mVideoHeight > this.mVideoWidth) {
            final View v;
            if (this.mFullScreenMode) {
                this.mVideoLayout.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
                this.mRoot.setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
                if (!this.mHideChatWithMediaController) {
                    if (this.mShouldReleasePlayer) {
                        AnimationUtils.shift(this.mChatLayout, 0, this.mRoot.getHeight() - this.mVideoLayout.getHeight(), null);
                        AnimationUtils.shift(this.mUserLayout, 0, this.mRoot.getHeight() - this.mVideoLayout.getHeight(), null);
                    }
                    v = this.mRoot.findViewById(R.id.space_holder);
                    final LayoutParams lp = (LayoutParams) v.getLayoutParams();
                    AnimationUtils.shift(this.mVideoLayout, 0, (this.mRoot.getHeight() - this.mVideoLayout.getHeight()) / 2, new Runnable() {
                        public void run() {
                            lp.height = (GameDetailsFragment.this.mRoot.getHeight() - GameDetailsFragment.this.mVideoLayout.getHeight()) / 2;
                            v.setLayoutParams(lp);
                        }
                    });
                } else if (this.mShouldReleasePlayer) {
                    UIUtils.fadeOut(this.mChatLayout);
                    UIUtils.fadeOut(this.mUserLayout);
                }
            } else {
                this.mRoot.setBackgroundResource(R.color.dark);
                this.mVideoLayout.setBackgroundResource(R.color.dark);
                if (this.mShouldReleasePlayer) {
                    AnimationUtils.shift(this.mChatLayout, this.mRoot.getHeight() - this.mVideoLayout.getHeight(), 0, new Runnable() {
                        public void run() {
                        }
                    });
                    AnimationUtils.shift(this.mUserLayout, this.mRoot.getHeight() - this.mVideoLayout.getHeight(), 0, new Runnable() {
                        public void run() {
                        }
                    });
                }
                if (!this.mHideChatWithMediaController) {
                    v = this.mRoot.findViewById(R.id.space_holder);
                    AnimationUtils.shift(this.mVideoLayout, 0, (-(this.mRoot.getHeight() - this.mVideoLayout.getHeight())) / 2, new Runnable() {
                        public void run() {
                            LayoutParams lp = (LayoutParams) v.getLayoutParams();
                            lp.height = 0;
                            v.setLayoutParams(lp);
                        }
                    });
                } else if (this.mShouldReleasePlayer) {
                    UIUtils.fadeIn(this.mChatLayout);
                    UIUtils.fadeIn(this.mUserLayout);
                }
            }
        }
        configUIVisibility();
    }

    public boolean isChatShowing() {
        return this.mShowChat;
    }

    public boolean isChatAvailable() {
        return this.mMessagesAdapter.getItemCount() != 0 && UIUtils.isLandscape(getActivity());
    }

    public void toggleChat() {
        this.mShowChat = !this.mShowChat;
        if (this.mShowChat) {
            hideSystemUI();
        }
        configUIVisibility();
    }

    private void prepareVideoToPlay() {
        if (isAdded()) {
            Config config = PreferenceUtility.getConfig();
            if (config != null) {
                String url;
                try {
                    if (this.mBroadcast == null || this.mBroadcast._id == null) {
                        throw new IllegalArgumentException("Broadcast or broadcast._id can't be empty");
                    }
                    url = this.mBroadcast.getURL(config);
                    if (url != null) {
                        Log.d(TAG_VIDEO, "setVideoPath: " + url);
                        preparePlayer(this.mSurfaceView, Uri.parse(url));
                        if (this.mVideoController != null) {
                            this.mVideoController.setTouchListener(this);
                            if (isLiveVideo()) {
                                this.mVideoController.setLiveMode(this.mBroadcast.startDate);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(new Exception("Can't prepareVideoToPlay for broadcast " + this.mBroadcast, e));
                    url = null;
                    onDeletedBroadcast();
                }
            } else if (this.mVideoController != null) {
                this.mVideoController.getHandler().removeCallbacks(null);
                this.mVideoController.postDelayed(new Runnable() {
                    public void run() {
                        GameDetailsFragment.this.prepareVideoToPlay();
                    }
                }, 1000);
            }
        }
    }

    public void correctVideoAspect(int defaultHeight) {
        this.mLastVideoHeight = defaultHeight;
        super.correctVideoAspect(this.mDefaultVideoHeight);
        Log.d(TAG_VIDEO, "correctViewAspect: " + defaultHeight);
        if (this.mVideoLayout == null || this.mRoot == null) {
            if (this.mVideoController != null) {
                this.mVideoController.updateControls();
            }
            Log.i(TAG_VIDEO, "correctViewAspect: " + defaultHeight + " canceled");
            return;
        }
        int videoHeight = getActualVideoHeight();
        if (Math.max(this.mRoot.getHeight(), this.mRoot.getWidth()) - videoHeight < this.mEditLayout.getHeight() + this.mUserLayout.getHeight()) {
            if (this.mVideoController != null) {
                this.mVideoController.disableFullScreen(true);
            }
            this.mHideChatWithMediaController = true;
            this.mRecyclerView.setVisibility(8);
            LayoutParams layoutParams = (LayoutParams) this.mChatLayout.getLayoutParams();
            layoutParams.height = this.mEditLayout.getHeight() + 1;
            layoutParams.addRule(REQUEST_LOGIN, 0);
            layoutParams.addRule(12);
            this.mChatLayout.setLayoutParams(layoutParams);
            layoutParams = (LayoutParams) this.mUserLayout.getLayoutParams();
            layoutParams.addRule(REQUEST_LOGIN, 0);
            layoutParams.addRule(12);
            layoutParams.bottomMargin = this.mEditLayout.getHeight();
            this.mUserLayout.setLayoutParams(layoutParams);
            if (this.mVideoController != null) {
                this.mVideoController.setPadding(0, 0, 0, Math.max(((this.mEditLayout.getHeight() + videoHeight) + this.mUserLayout.getHeight()) - this.mRoot.getHeight(), 0));
            }
        } else {
            if (this.mVideoController != null) {
                this.mVideoController.disableFullScreen(false);
            }
            this.mHideChatWithMediaController = false;
        }
        if (this.mVideoController != null) {
            this.mVideoController.updateControls();
        }
    }

    private void onDeletedBroadcast() {
        if (getActivity() != null) {
            try {
                new Builder(getActivity()).setMessage(R.string.error_broadcast_has_been_deleted).setPositiveButton(17039370, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        if (GameDetailsFragment.this.getActivity() != null) {
                            GameDetailsFragment.this.getActivity().finish();
                        }
                    }
                }).create().show();
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    private int getActualVideoHeight() {
        if (this.mVideoWidth == 0) {
            return 0;
        }
        return Math.min((int) (((double) this.mRoot.getWidth()) * (((double) this.mVideoHeight) / ((double) this.mVideoWidth))), Math.max(this.mRoot.getWidth(), this.mRoot.getHeight()));
    }

    protected boolean isLiveVideo() {
        return this.mBroadcast.isLive;
    }

    protected boolean wasLiveVideo() {
        return this.mBroadcastWasLive;
    }

    protected boolean isCurrentUserOwner() {
        return PreferenceUtility.getUser().equals(this.mBroadcast.user);
    }

    protected boolean isShowJoinedRequired() {
        return isLiveVideo();
    }

    private void configLikesView() {
        if (isAdded() && this.mLikesText != null && this.mBroadcast != null) {
            this.mLikesText.setCompoundDrawablesWithIntrinsicBounds(this.mBroadcast.currentLiked ? R.drawable.ic_like_appbar_active : R.drawable.ic_like_appbar, 0, 0, 0);
            this.mLikesText.setText(String.valueOf(this.mBroadcast.likes));
        }
    }

    private void configFollowView() {
        if (isAdded()) {
            this.mFollow.setActivated(!this.mIsFollower);
            this.mFollow.setText(this.mIsFollower ? R.string.UNFOLLOW : R.string.FOLLOW);
            this.mFollow.setTextColor(getResources().getColor(this.mIsFollower ? R.color.dark : R.color.yellow));
        }
    }

    private void checkIfFollower() {
        Network.checkIfFollower(getActivity(), EntityType.user, this.mBroadcast.user._id, new Listener<Boolean>() {
            public void onResponse(Boolean response) {
                GameDetailsFragment.this.mIsFollower = response != null ? response.booleanValue() : false;
                if (GameDetailsFragment.this.isAdded()) {
                    GameDetailsFragment.this.mProgressView.setVisibility(8);
                    GameDetailsFragment.this.configFollowView();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (GameDetailsFragment.this.isAdded()) {
                    GameDetailsFragment.this.mProgressView.setVisibility(8);
                    GameDetailsFragment.this.configFollowView();
                }
            }
        });
    }

    public void onStateChanged(boolean playWhenReady, int playbackState) {
        int i = 0;
        super.onStateChanged(playWhenReady, playbackState);
        Log.d("Player", "onStateChanged: " + playbackState);
        if (isAdded() && getView() != null) {
            if (this.mVideoController != null) {
                this.mVideoController.updatePausePlay();
            }
            if (playbackState == REQUEST_LOGIN && isLiveVideo() && this.mPrevPlayerState == REQUEST_CHAT) {
                this.mVideoHandler.removeCallbacksAndMessages(null);
                this.mVideoHandler.postDelayed(new Runnable() {
                    public void run() {
                        Log.e(GameDetailsFragment.TAG_VIDEO, "force preparing video to play");
                        if (GameDetailsFragment.this.mBroadcast != null) {
                            Network.getBroadcast(GameDetailsFragment.this.getActivity(), GameDetailsFragment.this.mBroadcast._id, new Listener<Broadcast>() {
                                public void onResponse(Broadcast response) {
                                    Log.d(GameDetailsFragment.TAG, "broadcast: " + response);
                                    if (response == null || response.isLive) {
                                        GameDetailsFragment.this.releasePlayer();
                                        GameDetailsFragment.this.prepareVideoToPlay();
                                        return;
                                    }
                                    GameDetailsFragment.this.releasePlayer();
                                    GameDetailsFragment.this.mBroadcastLiveEnded = true;
                                    GameDetailsFragment.this.mVideoController.clearLiveMode();
                                    GameDetailsFragment.this.hideSystemUI();
                                    GameDetailsFragment.this.toggleToolbar(VisualOperation.Show);
                                    GameDetailsFragment.this.generateWatchBroadcastEvent();
                                }
                            }, null);
                        }
                    }
                }, 10000);
            } else {
                this.mVideoHandler.removeCallbacksAndMessages(null);
            }
            if ((playbackState == REQUEST_LOGIN || playbackState == 1) && this.mFirstStartOfVideo) {
                if (this.mVideoController != null) {
                    this.mFirstStartOfVideo = false;
                    showSystemUI(false);
                    View view = this.mChatLayout;
                    if (this.mHideChatWithMediaController) {
                        i = 8;
                    }
                    view.setVisibility(i);
                    this.mShareMenuClickListener = ShareHelper.getShareMenuItemClickListener(getActivity(), this.mBroadcast);
                    this.mVideoController.setOnShareMenuItemClickListener(new OnItemClickListener() {
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            GameDetailsFragment.this.cancelDelayedHidingSystemUI();
                            GameDetailsFragment.this.mShareMenuClickListener.onItemClick(adapterView, view, i, l);
                        }
                    });
                    this.mVideoController.setCallbackOnShareMenuClosed(new Callback() {
                        public boolean handleMessage(Message message) {
                            GameDetailsFragment.this.hideSystemUIDelayed();
                            return true;
                        }
                    });
                }
            } else if (playbackState == REQUEST_CHAT && !this.mVideoOverlayWasShown) {
                this.mVideoOverlayWasShown = true;
                showSystemUI(true);
            } else if (playbackState == REQUEST_MODERATION) {
                if (isLiveVideo()) {
                    View v = getView().findViewById(R.id.broadcast_ended_text);
                    if (v != null) {
                        v.setVisibility(0);
                    }
                    this.mVideoController.clearLiveMode();
                } else {
                    this.mPlayer.getPlayerControl().pause();
                    this.mPlayer.seekTo(0);
                }
                showSystemUI(false);
            } else {
                configUIVisibility();
            }
            this.mPrevPlayerState = playbackState;
        }
    }

    private void updateViewersAndLikesCount() {
        if (this.mBroadcast == null) {
            Crashlytics.logException(new IllegalStateException("Broadcast is empty"));
            return;
        }
        Network.getBroadcastStats(getActivity(), this.mBroadcast._id, new Listener<Broadcast>() {
            public void onResponse(Broadcast response) {
                if (response != null) {
                    GameDetailsFragment.this.mBroadcast.totalViews = response.totalViews;
                    GameDetailsFragment.this.mBroadcast.currentViewers = response.currentViewers;
                    GameDetailsFragment.this.mBroadcast.likes = response.likes;
                    if (GameDetailsFragment.this.isAdded()) {
                        if (GameDetailsFragment.this.mVideoController != null) {
                            GameDetailsFragment.this.mVideoController.setViewersCount(response.getViewsNumber());
                        }
                        GameDetailsFragment.this.mLikesText.setText(String.valueOf(GameDetailsFragment.this.mBroadcast.likes));
                    }
                }
            }
        }, null);
        if (this.mVideoController != null && this.mVideoController.isShowing()) {
            this.mBroadcastHandler.postDelayed(new Runnable() {
                public void run() {
                    GameDetailsFragment.this.mBroadcastHandler.removeCallbacks(null);
                    if (GameDetailsFragment.this.isLiveVideo()) {
                        GameDetailsFragment.this.updateViewersAndLikesCount();
                    }
                }
            }, Constants.NOTIFICATION_BANNER_TIMEOUT);
        }
    }

    private void showSystemUI(boolean withAutoHide) {
        toggleSystemUI(VisualOperation.Show);
        if (withAutoHide) {
            hideSystemUIDelayed();
        }
    }

    private void hideSystemUI() {
        cancelDelayedHidingSystemUI();
        toggleSystemUI(VisualOperation.Hide);
    }

    private void hideSystemUIDelayed() {
        cancelDelayedHidingSystemUI();
        if (this.mHandler != null) {
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    GameDetailsFragment.this.toggleSystemUI(VisualOperation.Hide);
                }
            }, 2000);
        }
    }

    private void cancelDelayedHidingSystemUI() {
        this.mHandler.removeCallbacksAndMessages(null);
    }

    protected void toggleSystemUI(VisualOperation operation) {
        Log.i(TAG, "toggleSystemUI " + operation);
        if (!isAdded() || isRemoving()) {
            Log.e(TAG, "toggleSystemUI canceled");
            return;
        }
        if (this.mSystemUIShown && VisualOperation.Show.equals(operation)) {
            Log.i(TAG, "systemUI already shown");
        }
        if (!this.mSystemUIShown && VisualOperation.Hide.equals(operation)) {
            Log.i(TAG, "systemUI already hidden");
        }
        this.mSystemUIShown = VisualOperation.Show.equals(operation);
        if (VisualOperation.Hide.equals(operation)) {
            Log.d(TAG, "hiding mVideoController");
            UIUtils.fadeOut(this.mVideoController);
            if (this.mHideChatWithMediaController) {
                UIUtils.fadeOut(this.mChatLayout);
                UIUtils.fadeOut(this.mUserLayout);
            }
        } else {
            if (isLiveVideo()) {
                updateViewersAndLikesCount();
            }
            if (((this.mPlayer != null && this.mPlayer.getDuration() > 0) || isLiveVideo()) && !this.mBroadcastLiveEnded) {
                Log.d(TAG, "showing mVideoController");
                if (!this.mShowChat) {
                    UIUtils.fadeIn(this.mVideoController);
                    if (this.mHideChatWithMediaController) {
                        UIUtils.fadeIn(this.mChatLayout);
                        UIUtils.fadeIn(this.mUserLayout);
                    }
                }
            }
        }
        toggleToolbar(operation);
        toggleStatusBar(operation);
        configUIVisibility();
    }

    @TargetApi(16)
    private void toggleStatusBar(VisualOperation operation) {
        int uiOptions = 0;
        if (VERSION.SDK_INT >= 17) {
            uiOptions = 0 | 1284;
        }
        if (UIUtils.isLandscape(getActivity()) && VisualOperation.Hide.equals(operation)) {
            if (VERSION.SDK_INT == 16) {
                uiOptions |= REQUEST_CHAT;
            }
            uiOptions |= 2;
            if (VERSION.SDK_INT >= 19) {
                uiOptions |= MpegAudioHeader.MAX_FRAME_SIZE_BYTES;
            }
        }
        Log.i(TAG, "hideSystemUI: " + uiOptions);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

    private void toggleToolbar(VisualOperation operation) {
        if (VisualOperation.Hide.equals(operation)) {
            if (this.mDoNotHideToolbar || this.mBroadcastLiveEnded) {
                this.mDoNotHideToolbar = false;
            } else {
                UIUtils.slideOutToTop(this.mToolbar);
            }
        } else if (!this.mToolbarShowingIsDisabled) {
            UIUtils.slideInFromTop(this.mToolbar);
        }
    }

    private synchronized void configUIVisibility() {
        int i = 0;
        synchronized (this) {
            if (!isAdded() || isRemoving()) {
                Log.i(TAG, "Fragment is not added or is removing");
            } else {
                ImageView imageView;
                int i2;
                View view;
                boolean isLandscape = UIUtils.isLandscape(getActivity());
                ViewGroup.LayoutParams layoutParams = this.mSurfaceView.getLayoutParams();
                layoutParams.width = -1;
                Point size = UIUtils.getScreenSize(getActivity().getWindowManager());
                if (isLandscape) {
                    this.mChatOptionsBtn.setVisibility(8);
                    layoutParams.height = -1;
                    this.mSurfaceView.setLayoutParams(layoutParams);
                    correctVideoAspect(Math.min(size.x, size.y));
                } else {
                    imageView = this.mChatOptionsBtn;
                    if (this.mFirebase != null) {
                        i2 = 0;
                    } else {
                        i2 = 8;
                    }
                    imageView.setVisibility(i2);
                    correctVideoAspect(this.mDefaultVideoHeight);
                }
                int visibility = 8;
                if (!isLandscape && (this.mVideoHeight <= this.mVideoWidth || !this.mFullScreenMode)) {
                    visibility = 0;
                }
                if ((!this.mHideChatWithMediaController || this.mSystemUIShown) && this.mShouldReleasePlayer) {
                    imageView = this.mFullscreenChatBtn;
                    i2 = (isLandscape && this.mShowChat) ? 0 : 8;
                    imageView.setVisibility(i2);
                    if (isLandscape && this.mShowChat) {
                        scrollToLatestMessage(true);
                    }
                    if (this.mChatLayout.getAnimation() == null) {
                        LayoutParams lp = (LayoutParams) this.mChatLayout.getLayoutParams();
                        if (UIUtils.isLandscape(getActivity())) {
                            lp.height = getResources().getDimensionPixelSize(R.dimen.chat_landscape_height);
                            lp.addRule(REQUEST_LOGIN, 0);
                        } else {
                            lp.height = -1;
                            lp.addRule(REQUEST_LOGIN, this.mUserLayout.getId());
                        }
                        this.mChatLayout.setLayoutParams(lp);
                        this.mChatLayout.setBackgroundResource(isLandscape ? R.drawable.bg_chat_landscape : R.color.list_background);
                        view = this.mChatLayout;
                        if (!isLandscape || this.mShowChat) {
                            i2 = 0;
                        } else {
                            i2 = 8;
                        }
                        view.setVisibility(i2);
                        view = this.mToolbarUnderlayer;
                        if (isLandscape && this.mShowChat) {
                            i2 = 8;
                        } else {
                            i2 = 8;
                        }
                        view.setVisibility(i2);
                        int padding = getResources().getDimensionPixelSize(R.dimen.chat_landscape_bottom_padding);
                        this.mRecyclerView.setVerticalFadingEdgeEnabled(isLandscape);
                        this.mRecyclerView.setPadding(padding, 0, padding, 0);
                    }
                    if (this.mUserLayout.getAnimation() == null) {
                        this.mUserLayout.setVisibility(visibility);
                    }
                    if (this.mEditLayout.getAnimation() == null) {
                        this.mEditLayout.setVisibility(visibility);
                    }
                } else {
                    this.mUserLayout.setVisibility(8);
                    this.mChatLayout.setVisibility(8);
                }
                view = this.mVideoProgress;
                if ((this.mVideoController == null || !this.mVideoController.isShowing()) && ((this.mPlayer == null || this.mPlayer.getPlaybackState() == REQUEST_LOGIN || this.mPlayer.getPlaybackState() == 2) && !this.mBroadcastLiveEnded)) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                view.setVisibility(i2);
                view = this.mBroadcastEndedView;
                if (this.mBroadcastLiveEnded) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                view.setVisibility(i2);
                SurfaceView surfaceView = this.mSurfaceView;
                if (this.mBroadcastLiveEnded) {
                    i = 8;
                }
                surfaceView.setVisibility(i);
            }
        }
    }

    public void onError(Exception e) {
        super.onError(e);
        Log.e(TAG_VIDEO, "onError: " + (e != null ? e.getMessage() : " exception is empty"), e);
        this.mVideoProgress.setVisibility(8);
    }

    private void generateWatchBroadcastEvent() {
        MixpanelHelper.getInstance(MainApplication.getContext()).generateWatchBroadcastEvent(this.mBroadcast, (int) this.mSentMessagesCount, this.mBroadcast.urlsCopied, this.mIsFollower, this.mEnterTimestamp, this.mSource);
    }
}
