package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.AnimationUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.ChatRateLimit;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.TweakedInsetsFrameLayout;
import io.fabric.sdk.android.BuildConfig;

public class ChatActivity extends MobcrushActivty implements OnClickListener, TextWatcher, OnEditorActionListener {
    private static final int REQUEST_LOGIN = 3;
    private static final int REQUEST_MODERATION = 4;
    private static final String TAG = "ChatActivity";
    private Broadcast mBroadcast;
    private ImageView mChatBtn;
    private ChatFragment mChatFragment;
    private View mChatLayout;
    private ImageView mChatOptionsBtn;
    private View mCloseBtn;
    private View mEditLayout;
    private EditText mEditText;
    private boolean mExpectedQuit;
    private boolean mFirstStart = true;
    private boolean mKeyboardWasShown;
    private boolean mLoginRequested;
    private int mResultCode = -1;
    private TweakedInsetsFrameLayout mRoot;
    private View mSendBtn;
    private ImageView mSlowModeBtn;
    private View mToolbarCloseBtn;
    private boolean mUserIsModerator;

    public static Intent getIntent(Context context, String title, Broadcast broadcast, int chatPosition, boolean chatActive, boolean showJoined, boolean isModerator, ChatRateLimit chatRateLimit) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(Constants.EXTRA_TITLE, title);
        if (broadcast != null) {
            intent.putExtra(Constants.EXTRA_BROADCAST, broadcast.toString());
        }
        intent.putExtra(Constants.EXTRA_CHAT_POSITION, chatPosition);
        intent.putExtra(Constants.EXTRA_CHAT_ACTIVE, chatActive);
        intent.putExtra(Constants.EXTRA_CHAT_SHOW_JOINED, showJoined);
        intent.putExtra(Constants.EXTRA_CHAT_MODERATOR, isModerator);
        if (chatRateLimit != null) {
            intent.putExtra(Constants.EXTRA_CHAT_RATE_LIMIT, chatRateLimit.toString());
        } else {
            intent.putExtra(Constants.EXTRA_CHAT_RATE_LIMIT, new ChatRateLimit().toString());
        }
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        try {
            int i;
            super.onCreate(savedInstanceState);
            overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
            setContentView((int) R.layout.activity_chat);
            int uiOptions = 0;
            if (VERSION.SDK_INT >= 17) {
                uiOptions = 0 | 1284;
            }
            getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setTitleTextColor(getResources().getColor(R.color.yellow));
            toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.MCToolbar_Title);
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
            ((TextView) toolbar.findViewById(R.id.title)).setText(getIntent().getStringExtra(Constants.EXTRA_TITLE));
            toolbar.findViewById(R.id.action_like).setVisibility(8);
            this.mToolbarCloseBtn = toolbar.findViewById(R.id.action_close);
            this.mToolbarCloseBtn.setVisibility(0);
            this.mToolbarCloseBtn.setOnClickListener(this);
            if (getIntent().getBooleanExtra(Constants.EXTRA_CHAT_ACTIVE, false)) {
                i = 8;
            } else {
                i = 0;
            }
            toolbar.setVisibility(i);
            this.mChatFragment = new ChatFragment() {
            };
            this.mChatFragment.mLoadHistory = false;
            this.mBroadcast = (Broadcast) new Gson().fromJson(getIntent().getStringExtra(Constants.EXTRA_BROADCAST), Broadcast.class);
            if (this.mBroadcast != null) {
                this.mChatFragment.mChatRoom = this.mBroadcast.chatRoom;
                this.mChatFragment.mBroadcastId = this.mBroadcast._id;
                this.mChatFragment.mIsLiveBroadcast = this.mBroadcast.isLive;
                this.mChatFragment.mIsCurrentUserOwner = PreferenceUtility.getUser().equals(this.mBroadcast.user);
            }
            this.mChatFragment.mChatLimit = (ChatRateLimit) new Gson().fromJson(getIntent().getStringExtra(Constants.EXTRA_CHAT_RATE_LIMIT), ChatRateLimit.class);
            this.mChatFragment.mOnChatLimitCleared = new Callback() {
                public boolean handleMessage(Message msg) {
                    ChatActivity.this.configChatButtons();
                    return true;
                }
            };
            this.mChatFragment.mFirebaseLessor = true;
            this.mChatFragment.mFirebase = MainApplication.mFirebase;
            this.mChatFragment.mUserIsModerator = getIntent().getBooleanExtra(Constants.EXTRA_CHAT_MODERATOR, true);
            this.mChatFragment.mShowJoined = getIntent().getBooleanExtra(Constants.EXTRA_CHAT_SHOW_JOINED, false);
            getSupportFragmentManager().beginTransaction().add(this.mChatFragment, null).commit();
            this.mRoot = (TweakedInsetsFrameLayout) findViewById(R.id.content);
            this.mChatFragment.mNotificationLayout = this.mRoot.findViewById(R.id.popup_notification_layout);
            this.mChatFragment.mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, 1, false);
            this.mChatFragment.mMessagesAdapter = MainApplication.mChatMessagesAdapter;
            this.mChatFragment.mRecyclerView.setLayoutManager(layoutManager);
            this.mChatFragment.mRecyclerView.setAdapter(this.mChatFragment.mMessagesAdapter);
            if (MainApplication.mChatMessagesAdapter != null) {
                this.mChatFragment.mRecyclerView.scrollToPosition(MainApplication.mChatMessagesAdapter.getItemCount() - 1);
                MainApplication.mChatMessagesAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        ChatActivity.this.mChatFragment.scrollToLatestMessage(false);
                    }
                });
                this.mChatLayout = findViewById(R.id.chat_layout);
                this.mEditLayout = findViewById(R.id.edit_layout);
                this.mChatFragment.mEditLayout = this.mEditLayout;
                this.mEditText = (EditText) findViewById(R.id.edit);
                this.mChatFragment.mEditText = this.mEditText;
                this.mEditText.setOnClickListener(this);
                this.mEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
                    public void onFocusChange(View view, boolean b) {
                        if (b && !Network.isLoggedIn()) {
                            ChatActivity.this.mLoginRequested = true;
                            ChatActivity.this.requireLogin();
                        }
                    }
                });
                this.mEditText.addTextChangedListener(this);
                this.mEditText.setOnEditorActionListener(this);
                this.mChatFragment.mSlowModeTimer = (TextView) this.mRoot.findViewById(R.id.slow_mode_timer);
                this.mSlowModeBtn = (ImageView) this.mRoot.findViewById(R.id.slow_mode_btn);
                this.mSlowModeBtn.setOnClickListener(this);
                this.mSlowModeBtn.setVisibility(isCurrentUserOwner() ? 0 : 8);
                this.mSendBtn = findViewById(R.id.send_btn);
                this.mSendBtn.setOnClickListener(this);
                this.mSendBtn.setVisibility(8);
                this.mCloseBtn = findViewById(R.id.close_btn);
                this.mCloseBtn.setOnClickListener(this);
                this.mCloseBtn.setVisibility(0);
                this.mChatBtn = (ImageView) findViewById(R.id.chat_btn);
                this.mChatBtn.setOnClickListener(this);
                this.mChatOptionsBtn = (ImageView) findViewById(R.id.chat_options_btn);
                this.mChatOptionsBtn.setOnClickListener(this);
                if (getIntent().getBooleanExtra(Constants.EXTRA_CHAT_ACTIVE, false) && PreferenceUtility.isEmailVerified()) {
                    this.mRoot.disableTweaking();
                    this.mEditText.requestFocus();
                }
                this.mChatLayout.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        ChatActivity.this.onKeyboardVisibilityChanged(((double) ChatActivity.this.mChatLayout.getHeight()) / ((double) ChatActivity.this.mChatLayout.getRootView().getHeight()) < 0.7d);
                    }
                });
                this.mChatFragment.subscribeForMetadata();
                this.mChatFragment.applyChatLimitation(this.mChatFragment.mChatLimit);
                this.mChatFragment.subscribeForModeratorsList();
                this.mChatFragment.subscribeForMutedList();
                return;
            }
            Crashlytics.log("MainApplication.mChatMessagesAdapter is empty");
            finish(true);
        } catch (Exception e2) {
            e2.printStackTrace();
            Crashlytics.logException(e2);
            finish(true);
        }
    }

    private boolean isCurrentUserOwner() {
        return PreferenceUtility.getUser().equals(this.mBroadcast.user);
    }

    private void requireLogin() {
        startActivityForResult(LoginActivity.getIntent(this, false, false), REQUEST_LOGIN);
        finish();
    }

    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        this.mExpectedQuit = true;
        super.startActivityForResult(intent, requestCode, options);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        configChatButtons();
        if (getIntent().getBooleanExtra(Constants.EXTRA_CHAT_ACTIVE, false)) {
            this.mEditText.performClick();
        }
    }

    protected void onResume() {
        super.onResume();
        this.mExpectedQuit = false;
        Crashlytics.log("ChatActivity for " + (this.mChatFragment != null ? this.mChatFragment.mBroadcastId : "null"));
        this.mChatFragment.mMessagesAdapter.setChatActivity(this);
    }

    protected void onPause() {
        super.onPause();
        this.mChatFragment.mMessagesAdapter.setChatActivity(null);
        if (!this.mExpectedQuit) {
            sendBroadcast(new Intent(Constants.ACTION_PAUSE_PLAYER));
            finish();
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        this.mExpectedQuit = false;
        setResult(0);
    }

    public void finish(boolean expected) {
        this.mExpectedQuit = expected;
        finish();
    }

    public void finish() {
        if (this.mEditLayout != null) {
            this.mEditLayout.setVisibility(8);
        }
        setResult(this.mResultCode, new Intent());
        super.finish();
        overridePendingTransition(R.anim.slide_in_bottom, R.anim.slide_out_bottom);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        this.mResultCode = 0;
        onBackPressed();
        return true;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                sendMessage();
                return;
            case R.id.close_btn:
            case R.id.chat_btn:
            case R.id.action_close:
                UIUtils.hideVirtualKeyboard((FragmentActivity) this, this.mEditText.getWindowToken());
                finish(true);
                return;
            case R.id.chat_options_btn:
                try {
                    if (this.mBroadcast != null) {
                        startActivity(ChannelModerationActivity.getIntent(this, this.mChatFragment.mChatRoom._id, this.mBroadcast.user != null ? this.mBroadcast.user._id : null, this.mChatFragment.mUserIsModerator));
                        finish(true);
                        return;
                    }
                    return;
                } catch (Throwable e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    return;
                }
            default:
                return;
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        configChatButtons();
    }

    public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
        if (keyEvent == null) {
            sendMessage();
            return true;
        } else if (keyEvent.isShiftPressed()) {
            return false;
        } else {
            sendMessage();
            return true;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_LOGIN) {
            this.mLoginRequested = false;
            if (resultCode == -1) {
                this.mEditText.requestFocus();
            }
        }
    }

    private void onKeyboardVisibilityChanged(boolean isKeyboardShown) {
        int i = 8;
        this.mRoot.disableTweaking();
        this.mEditText.setCursorVisible(isKeyboardShown);
        ImageView imageView = this.mSlowModeBtn;
        if (!isKeyboardShown && isCurrentUserOwner()) {
            i = 0;
        }
        imageView.setVisibility(i);
        if (isKeyboardShown) {
            if (Network.isLoggedIn()) {
                if (!PreferenceUtility.isEmailVerified()) {
                    startActivity(new Intent(this, EmailVerificationRequestActivity.class));
                    return;
                }
            } else if (!this.mLoginRequested) {
                this.mLoginRequested = true;
                requireLogin();
            }
            if (!this.mKeyboardWasShown) {
                this.mFirstStart = false;
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.hide();
                }
                this.mEditLayout.getHandler().removeCallbacksAndMessages(null);
                this.mEditLayout.getHandler().postDelayed(new Runnable() {
                    public void run() {
                        AnimationUtils.changeColor(ChatActivity.this.mEditLayout, Integer.valueOf(ChatActivity.this.getResources().getColor(R.color.chat_edit_inactive_background)).intValue(), Integer.valueOf(ChatActivity.this.getResources().getColor(R.color.chat_edit_active_background)).intValue(), true);
                        AnimationUtils.changeColor(ChatActivity.this.mEditText, Integer.valueOf(ChatActivity.this.getResources().getColor(R.color.chat_edit_inactive_color)).intValue(), Integer.valueOf(ChatActivity.this.getResources().getColor(R.color.chat_edit_active_background)).intValue(), true);
                    }
                }, 150);
                this.mChatFragment.scrollToLatestMessage(true);
                this.mKeyboardWasShown = true;
            }
        } else if (this.mKeyboardWasShown && !this.mChatFragment.mMuteDialogIsShown) {
            finish(true);
        } else if (this.mFirstStart || this.mChatFragment.mMuteDialogIsShown) {
            this.mKeyboardWasShown = false;
        } else {
            UIUtils.showVirtualKeyboard(this);
        }
        configChatButtons();
    }

    private void sendMessage() {
        String s = this.mEditText.getText().toString();
        if (!TextUtils.isEmpty(s) && !this.mChatFragment.isChatLimitReached(true)) {
            this.mEditText.setText(BuildConfig.FLAVOR);
            this.mChatFragment.sendMessage(s, false);
        }
    }

    private void configChatButtons() {
        boolean messageIsEmpty;
        int i;
        int i2 = 8;
        if (this.mEditText.getText().length() == 0) {
            messageIsEmpty = true;
        } else {
            messageIsEmpty = false;
        }
        View view = this.mSendBtn;
        if (messageIsEmpty || this.mChatFragment.mChatLimited) {
            i = 8;
        } else {
            i = 0;
        }
        view.setVisibility(i);
        view = this.mCloseBtn;
        if ((messageIsEmpty || this.mChatFragment.mChatLimited) && this.mKeyboardWasShown) {
            i = 0;
        } else {
            i = 8;
        }
        view.setVisibility(i);
        ImageView imageView = this.mChatBtn;
        if (this.mKeyboardWasShown) {
            i = 8;
        } else {
            i = 0;
        }
        imageView.setVisibility(i);
        ImageView imageView2 = this.mChatOptionsBtn;
        if (!this.mKeyboardWasShown) {
            i2 = 0;
        }
        imageView2.setVisibility(i2);
    }
}
