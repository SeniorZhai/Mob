package com.mobcrush.mobcrush;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.volley.Response.Listener;
import com.crashlytics.android.Crashlytics;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Firebase.AuthResultHandler;
import com.firebase.client.Firebase.CompletionListener;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.AnimationUtils;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.ChatRoom;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.datamodel.GroupChannel;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.misc.SimpleChildEventListener;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.ScrollTabHolder;
import com.mobcrush.mobcrush.ui.ScrollTabHolderFragment;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class DiscussionFragment extends ScrollTabHolderFragment implements OnScrollListener, TextWatcher, OnClickListener, OnEditorActionListener {
    private static final boolean CHAT_DEBUG = false;
    private static final int MAX_RETRIES = 4;
    private static final int REQUEST_LOGIN = 3;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int RETRY_FACTOR = 3;
    private static final String TAG = "DiscussionFragment";
    private static final String TAG_CHAT = "DiscussionFragment.CHAT";
    AuthResultHandler authResultHandler = new AuthResultHandler() {
        public void onAuthenticated(final AuthData authData) {
            if (DiscussionFragment.this.isAdded() && authData != null) {
                DiscussionFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        User u = PreferenceUtility.getUser();
                        try {
                            Object post = new HashMap();
                            String userId = authData.getUid();
                            if (!u.isGuest(DiscussionFragment.this.getActivity())) {
                                userId = u._id;
                                post.put(ChatMessage.USER_ID, u._id);
                                post.put(ChatMessage.USERNAME, u.username);
                                post.put(ChatMessage.PROFILE_LOGO_SMALL, u.profileLogoSmall);
                                if (ModerationHelper.isAdmin(u._id)) {
                                    post.put(ChatMessage.ROLE, RoleType.admin);
                                }
                            }
                            post.put(ChatMessage.CLIENT, Network.getUserAgent());
                            post.put(Constants.CHAT_MESSAGE_TIMESTAMP, ServerValue.TIMESTAMP.toString());
                            DiscussionFragment.this.mCurrentUserValue = DiscussionFragment.this.mFirebase.child(u.isGuest(MainApplication.getContext()) ? Constants.CHAT_ROOM_ANONYMOUS_USERS : Constants.CHAT_ROOM_USERS).child(DiscussionFragment.this.getChatChannelId()).child(userId);
                            DiscussionFragment.this.mCurrentUserValue.setValue(post, new CompletionListener() {
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    if (firebaseError == null) {
                                    }
                                }
                            });
                            if (DiscussionFragment.this.mCurrentUserValue != null) {
                                DiscussionFragment.this.mCurrentUserValue.onDisconnect().removeValue(new CompletionListener() {
                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    }
                                });
                            }
                            Network.loadAdminListFromFirebase();
                            DiscussionFragment.this.mQueryMessages = DiscussionFragment.this.mFirebase.child(Constants.CHAT_ROOM_MESSAGES).child(DiscussionFragment.this.getChatChannelId());
                            if (!(DiscussionFragment.this.mChatRoom == null || DiscussionFragment.this.mChatRoom.filterBy == null)) {
                                DiscussionFragment.this.mQueryMessages = DiscussionFragment.this.mQueryMessages.orderByChild(DiscussionFragment.this.mChatRoom.filterBy);
                            }
                            DiscussionFragment.this.mQueryMessages.limitToLast(100).addListenerForSingleValueEvent(new ValueEventListener() {
                                public void onDataChange(final DataSnapshot dataSnapshot) {
                                    if (DiscussionFragment.this.isAdded()) {
                                        DiscussionFragment.this.getActivity().runOnUiThread(new Runnable() {
                                            public void run() {
                                                if (dataSnapshot == null || dataSnapshot.getValue() == null || dataSnapshot.getChildrenCount() <= 0) {
                                                    DiscussionFragment.this.mLastHistoryMessageTimestamp = 0;
                                                } else {
                                                    DiscussionFragment.this.addMessage(dataSnapshot);
                                                }
                                                if (DiscussionFragment.this.mQueryMessages != null) {
                                                    DiscussionFragment.this.mQueryMessages.limitToLast(100).addChildEventListener(DiscussionFragment.this.mChatMessagesListener);
                                                }
                                            }
                                        });
                                    }
                                }

                                public void onCancelled(FirebaseError firebaseError) {
                                    DiscussionFragment.this.mLastHistoryMessageTimestamp = 0;
                                    if (DiscussionFragment.this.mQueryMessages != null) {
                                        DiscussionFragment.this.mQueryMessages.limitToLast(100).addChildEventListener(DiscussionFragment.this.mChatMessagesListener);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }

        public void onAuthenticationError(final FirebaseError firebaseError) {
            if (DiscussionFragment.this.isAdded()) {
                if (DiscussionFragment.this.mRetryHandler == null) {
                    DiscussionFragment.this.mRetryHandler = new Handler(Looper.getMainLooper());
                }
                int delay = (DiscussionFragment.this.mRetryCount * DiscussionFragment.RETRY_FACTOR) * DiscussionFragment.RETRY_DELAY_MS;
                DiscussionFragment.this.mRetryHandler.removeCallbacks(null);
                DiscussionFragment.this.mRetryHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            if (firebaseError != null) {
                                PreferenceUtility.removeFirebaseToken();
                                DiscussionFragment.this.mFirebase = null;
                                DiscussionFragment.this.mChatIsConfigured = DiscussionFragment.CHAT_DEBUG;
                            } else {
                                PreferenceUtility.removeFirebaseToken();
                                DiscussionFragment.this.mFirebase = null;
                                DiscussionFragment.this.mChatIsConfigured = DiscussionFragment.CHAT_DEBUG;
                            }
                            if (DiscussionFragment.this.mRetryCount < DiscussionFragment.MAX_RETRIES) {
                                DiscussionFragment.access$604(DiscussionFragment.this);
                                DiscussionFragment.this.configChat();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                }, (long) delay);
            }
        }
    };
    private String mChatChannelId;
    private boolean mChatIsConfigured;
    SimpleChildEventListener mChatMessagesListener = new SimpleChildEventListener() {
        public void onChildAdded(final DataSnapshot dataSnapshot, final String s) {
            if (DiscussionFragment.this.isAdded()) {
                DiscussionFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            DiscussionFragment.this.addMessage(ChatMessage.from(dataSnapshot));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }

        public void onChildRemoved(final DataSnapshot dataSnapshot) {
            if (DiscussionFragment.this.isAdded()) {
                DiscussionFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            DiscussionFragment.this.mMessagesAdapter.remove(ChatMessage.from(dataSnapshot));
                        } catch (Exception e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }
    };
    protected Runnable mChatMuteChecker = new Runnable() {
        public void run() {
            boolean z = DiscussionFragment.CHAT_DEBUG;
            DiscussionFragment.this.mChatMuteHandler.removeCallbacks(null);
            DiscussionFragment.this.mChatMuteHandler.postDelayed(DiscussionFragment.this.mChatMuteChecker, 1000);
            DiscussionFragment discussionFragment = DiscussionFragment.this;
            discussionFragment.mChatPoints -= Constants.CHAT_POINTS_TICKER;
            if (DiscussionFragment.this.mChatPoints < 0) {
                DiscussionFragment.this.mChatPoints = 0;
            }
            discussionFragment = DiscussionFragment.this;
            if (DiscussionFragment.this.mChatPoints == 0) {
                z = true;
            }
            discussionFragment.mUserIsAutoMuted = z;
        }
    };
    protected Handler mChatMuteHandler = new Handler();
    protected int mChatPoints;
    private ChatRoom mChatRoom;
    private View mCloseBtn;
    protected Firebase mCurrentUserValue;
    private View mEditLayout;
    private EditText mEditText;
    protected Firebase mFirebase;
    private View mFooterView;
    private Game mGame;
    private View mHeaderView;
    private boolean mIsKeyboardShown;
    private boolean mIsPaused;
    private boolean mIsSubscribed;
    private int mItemHeight;
    private long mLastHistoryMessageTimestamp;
    private String mLastMessage;
    private ListView mListView;
    private boolean mLoginRequsted;
    private ChatMessagesListAdapter mMessagesAdapter;
    private int mMinimalHeaderHeight;
    private int mPosition;
    protected Query mQueryMessages;
    private int mRetryCount = 0;
    private Handler mRetryHandler;
    private View mRoot;
    private View mSendBtn;
    private UserChannel mUserChannel;
    protected boolean mUserIsAutoMuted;

    static /* synthetic */ int access$604(DiscussionFragment x0) {
        int i = x0.mRetryCount + 1;
        x0.mRetryCount = i;
        return i;
    }

    public static DiscussionFragment newInstance(UserChannel userChannel, int position) {
        DiscussionFragment fragment = new DiscussionFragment();
        Bundle args = new Bundle();
        if (userChannel != null) {
            args.putString(Constants.EXTRA_USER_CHANNEL, userChannel.toString());
        }
        args.putInt(Constants.EXTRA_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public static DiscussionFragment newInstance(Game game, int position) {
        DiscussionFragment fragment = new DiscussionFragment();
        Bundle args = new Bundle();
        if (game != null) {
            args.putString(Constants.EXTRA_GAME, game.toString());
        }
        args.putInt(Constants.EXTRA_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey(Constants.EXTRA_USER_CHANNEL)) {
                this.mUserChannel = (UserChannel) new Gson().fromJson(getArguments().getString(Constants.EXTRA_USER_CHANNEL, null), UserChannel.class);
            }
            if (getArguments().containsKey(Constants.EXTRA_GAME)) {
                this.mGame = (Game) new Gson().fromJson(getArguments().getString(Constants.EXTRA_GAME, null), Game.class);
            }
            this.mPosition = getArguments().getInt(Constants.EXTRA_POSITION);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mRoot = inflater.inflate(R.layout.fragment_discussion, container, CHAT_DEBUG);
        this.mListView = (ListView) this.mRoot.findViewById(16908298);
        if (this.mMessagesAdapter == null) {
            this.mMessagesAdapter = new ChatMessagesListAdapter(getActivity());
        }
        this.mEditLayout = this.mRoot.findViewById(R.id.edit_layout);
        this.mEditText = (EditText) this.mRoot.findViewById(R.id.edit);
        this.mEditText.addTextChangedListener(this);
        this.mEditText.setOnEditorActionListener(this);
        this.mSendBtn = this.mRoot.findViewById(R.id.send_btn);
        this.mSendBtn.setOnClickListener(this);
        this.mSendBtn.setVisibility(8);
        this.mCloseBtn = this.mRoot.findViewById(R.id.close_btn);
        this.mCloseBtn.setOnClickListener(this);
        this.mCloseBtn.setVisibility(8);
        this.mItemHeight = getResources().getDimensionPixelSize(R.dimen.message_item_height);
        this.mListView.setOnScrollListener(this);
        this.mRoot.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (DiscussionFragment.this.getActivity() != null) {
                    DiscussionFragment.this.onKeybordVisibilityChanged(((double) DiscussionFragment.this.mRoot.getHeight()) / ((double) UIUtils.getScreenSize(DiscussionFragment.this.getActivity().getWindowManager()).y) < 0.7d ? true : DiscussionFragment.CHAT_DEBUG);
                }
            }
        });
        return this.mRoot;
    }

    public void onResume() {
        super.onResume();
        this.mIsPaused = CHAT_DEBUG;
        if (!this.mChatIsConfigured) {
            configChat();
        }
    }

    public void onPause() {
        unsubscribeChat();
        this.mIsPaused = true;
        this.mListView.getFirstVisiblePosition();
        if (this.mRetryHandler != null) {
            this.mRetryHandler.removeCallbacks(null);
        }
        super.onPause();
    }

    public void onDetach() {
        super.onDetach();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RETRY_FACTOR) {
            this.mLoginRequsted = CHAT_DEBUG;
            if (resultCode == -1) {
                this.mEditText.requestFocus();
            } else {
                UIUtils.hideVirtualKeyboard(getActivity());
            }
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    public void afterTextChanged(Editable s) {
        configEditButtons();
    }

    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        if (keyEvent == null) {
            sendMessage();
            return true;
        } else if (keyEvent.isShiftPressed()) {
            return CHAT_DEBUG;
        } else {
            sendMessage();
            return true;
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                sendMessage();
                return;
            case R.id.close_btn:
                this.mEditText.setText(BuildConfig.FLAVOR);
                UIUtils.hideVirtualKeyboard(getActivity());
                return;
            default:
                return;
        }
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            UIUtils.hideVirtualKeyboard(getActivity());
        } else if (this.mGame != null) {
            GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_GAME_DISCUSSION);
        } else if (this.mUserChannel != null) {
            GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_TEAM_DISCUSSION);
        }
    }

    public void setHeaderHeight(int height, int minimalHeight) {
        this.mMinimalHeaderHeight = minimalHeight;
        if (this.mHeaderView != null) {
            try {
                this.mListView.removeHeaderView(this.mHeaderView);
            } catch (Exception e) {
            }
        }
        this.mHeaderView = new View(getActivity());
        this.mHeaderView.setMinimumHeight(height);
        this.mListView.addHeaderView(this.mHeaderView, null, CHAT_DEBUG);
        this.mListView.setAdapter(this.mMessagesAdapter);
        correctListFooter();
    }

    private void onKeybordVisibilityChanged(boolean isKeyboardShown) {
        int i = R.color.chat_edit_inactive_background;
        int i2 = R.color.chat_edit_active_background;
        if (this.mIsKeyboardShown != isKeyboardShown) {
            this.mIsKeyboardShown = isKeyboardShown;
            if (this.mIsKeyboardShown) {
                if (!(Network.isLoggedIn() || this.mLoginRequsted)) {
                    this.mLoginRequsted = true;
                    startActivityForResult(LoginActivity.getIntent(getActivity(), CHAT_DEBUG, CHAT_DEBUG), RETRY_FACTOR);
                }
                this.mListView.postDelayed(new Runnable() {
                    public void run() {
                        DiscussionFragment.this.mListView.setSelection(DiscussionFragment.this.mMessagesAdapter.getCount() + 1);
                    }
                }, 100);
            } else {
                this.mEditText.setText(BuildConfig.FLAVOR);
            }
            correctListFooter();
            Integer colorFrom = Integer.valueOf(getResources().getColor(this.mIsKeyboardShown ? R.color.chat_edit_inactive_background : R.color.chat_edit_active_background));
            Resources resources = getResources();
            if (this.mIsKeyboardShown) {
                i = R.color.chat_edit_active_background;
            }
            AnimationUtils.changeColor(this.mEditLayout, colorFrom.intValue(), Integer.valueOf(resources.getColor(i)).intValue(), true);
            colorFrom = Integer.valueOf(getResources().getColor(this.mIsKeyboardShown ? R.color.chat_edit_inactive_color : R.color.chat_edit_active_background));
            resources = getResources();
            if (!this.mIsKeyboardShown) {
                i2 = R.color.chat_edit_inactive_color;
            }
            AnimationUtils.changeColor(this.mEditText, colorFrom.intValue(), Integer.valueOf(resources.getColor(i2)).intValue(), true);
            this.mEditText.setCursorVisible(this.mIsKeyboardShown);
        }
        configEditButtons();
    }

    private void configEditButtons() {
        int i;
        int i2 = 0;
        View view = this.mCloseBtn;
        if (this.mEditText.getText().length() == 0 && this.mIsKeyboardShown) {
            i = 0;
        } else {
            i = 8;
        }
        view.setVisibility(i);
        View view2 = this.mSendBtn;
        if (this.mEditText.getText().length() <= 0 || !this.mIsKeyboardShown) {
            i2 = 8;
        }
        view2.setVisibility(i2);
    }

    private void configChat() {
        if ((this.mUserChannel != null || this.mGame != null) && getChatChannelId() != null) {
            this.mChatIsConfigured = true;
            if (this.mMessagesAdapter != null) {
                this.mMessagesAdapter.clear();
            }
            if (this.mQueryMessages != null) {
                this.mQueryMessages.removeEventListener(this.mChatMessagesListener);
            }
            this.mFirebase = new Firebase(Constants.CHAT_BASE_ADDRESS);
            if (PreferenceUtility.getUser().isGuest(MainApplication.getContext())) {
                this.mFirebase.authAnonymously(this.authResultHandler);
            } else if (TextUtils.isEmpty(PreferenceUtility.getFirebaseToken())) {
                Network.getFirebaseToken(getActivity(), new Listener<Boolean>() {
                    public void onResponse(Boolean response) {
                        if (!response.booleanValue() || DiscussionFragment.this.mFirebase == null) {
                            DiscussionFragment.this.mChatIsConfigured = DiscussionFragment.CHAT_DEBUG;
                        } else {
                            DiscussionFragment.this.mFirebase.authWithCustomToken(PreferenceUtility.getFirebaseToken(), DiscussionFragment.this.authResultHandler);
                        }
                    }
                }, null);
            } else {
                this.mFirebase.authWithCustomToken(PreferenceUtility.getFirebaseToken(), this.authResultHandler);
            }
        }
    }

    private void unsubscribeChat() {
        this.mIsSubscribed = CHAT_DEBUG;
        try {
            if (!(this.mQueryMessages == null || this.mChatMessagesListener == null)) {
                this.mQueryMessages.removeEventListener(this.mChatMessagesListener);
            }
            if (this.mCurrentUserValue != null) {
                this.mCurrentUserValue.removeValue(new CompletionListener() {
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    }
                });
                this.mCurrentUserValue = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mFirebase = null;
    }

    private void sendMessage() {
        String s = this.mEditText.getText().toString();
        if (!TextUtils.isEmpty(s)) {
            this.mEditText.setText(BuildConfig.FLAVOR);
            sendMessage(s);
        }
    }

    private void sendMessage(String message) {
        if (message != null && !TextUtils.isEmpty(message.trim())) {
            if ((this.mUserChannel != null && this.mUserChannel.channel != null && this.mUserChannel.channel._id != null) || this.mGame != null) {
                User u = PreferenceUtility.getUser();
                if (!u.isGuest(getActivity())) {
                    try {
                        ChatMessage m = new ChatMessage();
                        m.setMessage(message.replace("\r", BuildConfig.FLAVOR));
                        m.setUserInfo(u);
                        if (!isMessageCanBeSent(message) || this.mFirebase == null) {
                            addMessage(m);
                            return;
                        }
                        Firebase newPostRef = this.mFirebase.child(Constants.CHAT_ROOM_MESSAGES).child(getChatChannelId()).push();
                        Map<String, Object> map = m.getMapWithMetadata(this.mChatRoom != null ? this.mChatRoom.metadata : null);
                        map.put(Constants.CHAT_MESSAGE_TIMESTAMP, ServerValue.TIMESTAMP);
                        map.put(ChatMessage.CLIENT, Network.getUserAgent());
                        if (ModerationHelper.isAdmin(u._id)) {
                            m.role = RoleType.admin;
                        }
                        newPostRef.setValue(map);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    protected boolean isMessageCanBeSent(String messsage) {
        if (TextUtils.equals(this.mLastMessage, messsage)) {
            this.mChatPoints += 30;
        } else if (TextUtils.getTrimmedLength(messsage) == 1) {
            this.mChatPoints += 20;
        } else {
            this.mChatPoints += 10;
        }
        if (this.mUserIsAutoMuted) {
            this.mChatPoints = Constants.CHAT_POINTS_LIMIT;
        } else if (this.mChatPoints >= Constants.CHAT_POINTS_LIMIT) {
            this.mUserIsAutoMuted = true;
        }
        if (this.mUserIsAutoMuted || TextUtils.equals(this.mLastMessage, messsage)) {
            return CHAT_DEBUG;
        }
        return true;
    }

    private void addMessage(Object message) {
        if (isAdded() && message != null) {
            int i;
            int count = this.mMessagesAdapter.getCount() + (this.mFooterView != null ? 1 : 0);
            if (this.mHeaderView != null) {
                i = 1;
            } else {
                i = 0;
            }
            int count2 = count + i;
            ChatMessage m;
            if (message instanceof DataSnapshot) {
                DataSnapshot dataSnapshot = (DataSnapshot) message;
                ArrayList<ChatMessage> messages = new ArrayList();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    m = (ChatMessage) child.getValue(ChatMessage.class);
                    if (m != null) {
                        try {
                            this.mLastHistoryMessageTimestamp = Math.max(this.mLastHistoryMessageTimestamp, m.timestamp);
                            if (!(m.isHidden() || m.isMuteOrigin())) {
                                messages.add(m);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            this.mLastHistoryMessageTimestamp = 0;
                        }
                    }
                }
                this.mMessagesAdapter.addAll(messages);
            } else if (message instanceof ChatMessage) {
                m = (ChatMessage) message;
                if (!(m.timestamp <= this.mLastHistoryMessageTimestamp || m.isHidden() || m.isMuteOrigin())) {
                    this.mMessagesAdapter.add(m);
                }
            } else if (message instanceof JSONObject) {
                this.mMessagesAdapter.add(message.toString());
            } else if (message instanceof JSONArray) {
                JSONArray a = ((JSONArray) message).optJSONArray(0);
                if (a != null) {
                    ArrayList<ChatMessage> list = new ArrayList();
                    for (int i2 = 0; i2 < a.length(); i2++) {
                        Object o = a.opt(i2);
                        if (o instanceof String) {
                            list.add(new ChatMessage().setMessage((String) o));
                        } else if (o instanceof JSONObject) {
                            list.add(new Gson().fromJson(o.toString(), ChatMessage.class));
                        }
                    }
                    this.mMessagesAdapter.addAll(list);
                }
            } else if (message instanceof String) {
                this.mMessagesAdapter.add(new ChatMessage().setMessage((String) message));
            }
            this.mLastMessage = this.mMessagesAdapter.getCount() > 0 ? ((ChatMessage) this.mMessagesAdapter.getItem(this.mMessagesAdapter.getCount() - 1)).message : null;
            correctListFooter();
            if (this.mListView.getLastVisiblePosition() >= count2 - 2) {
                this.mListView.postDelayed(new Runnable() {
                    public void run() {
                        DiscussionFragment.this.mListView.setSelection(DiscussionFragment.this.mMessagesAdapter.getCount() + 1);
                    }
                }, 100);
            }
        }
    }

    public void adjustScroll(int scrollHeight) {
        if (scrollHeight > this.mMinimalHeaderHeight || this.mListView.getFirstVisiblePosition() < 1) {
            this.mListView.setSelectionFromTop(1, scrollHeight);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.mScrollTabHolder != null) {
            this.mScrollTabHolder.onScroll(view.getChildAt(0), firstVisibleItem, visibleItemCount, totalItemCount, this.mPosition);
        }
    }

    public void setChannel(UserChannel userChannel) {
        if (this.mIsSubscribed) {
            unsubscribeChat();
        }
        ScrollTabHolder scrollTabHolder = this.mScrollTabHolder;
        this.mScrollTabHolder = null;
        this.mUserChannel = userChannel;
        if (!this.mIsPaused) {
            if (userChannel != null && userChannel.channel.chatRoom == null) {
                Network.getChannel(getActivity(), userChannel.channel._id, new Listener<GroupChannel>() {
                    public void onResponse(GroupChannel response) {
                        if (response != null) {
                            try {
                                DiscussionFragment.this.mUserChannel.channel.chatRoom = response.chatRoom;
                                if (response.chatRoom != null) {
                                    DiscussionFragment.this.configChat();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }
                    }
                }, null);
            }
            configChat();
        }
        this.mScrollTabHolder = scrollTabHolder;
    }

    public void setGame(Game game) {
        if (this.mIsSubscribed) {
            unsubscribeChat();
        }
        this.mChatChannelId = null;
        ScrollTabHolder scrollTabHolder = this.mScrollTabHolder;
        this.mScrollTabHolder = null;
        this.mGame = game;
        if (!this.mIsPaused) {
            if (game != null && game.chatRoom == null) {
                Network.getGame(getActivity(), game._id, new Listener<Game>() {
                    public void onResponse(Game response) {
                        Log.d(DiscussionFragment.TAG_CHAT, "GameResponse: " + response);
                        if (response != null) {
                            try {
                                DiscussionFragment.this.mGame = response;
                                if (response.chatRoom != null) {
                                    DiscussionFragment.this.configChat();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }
                    }
                }, null);
            }
            configChat();
        }
        this.mScrollTabHolder = scrollTabHolder;
    }

    private void correctListFooter() {
        if (getActivity() == null) {
            new Exception("correctListFooter.getActivity() == null").printStackTrace();
            return;
        }
        if (this.mMessagesAdapter.getCount() > 0) {
            int height = this.mItemHeight * this.mMessagesAdapter.getCount();
            int listHeight = this.mListView.getHeight();
            if (this.mFooterView != null) {
                this.mListView.removeFooterView(this.mFooterView);
            }
            if (listHeight - this.mItemHeight > this.mMinimalHeaderHeight) {
                this.mFooterView = new View(getActivity());
                this.mFooterView.setLayoutParams(new LayoutParams(-1, (listHeight - this.mMinimalHeaderHeight) - height));
                this.mListView.addFooterView(this.mFooterView, null, CHAT_DEBUG);
            } else {
                this.mFooterView = null;
            }
        } else {
            if (this.mFooterView != null) {
                try {
                    this.mListView.removeFooterView(this.mFooterView);
                } catch (Exception e) {
                }
            }
            if (getView() != null) {
                this.mFooterView = new View(getActivity());
                this.mFooterView.setLayoutParams(new LayoutParams(-1, this.mListView.getHeight() - this.mMinimalHeaderHeight));
                this.mListView.addFooterView(this.mFooterView, null, CHAT_DEBUG);
            }
        }
        this.mMessagesAdapter.notifyDataSetChanged();
    }

    private String getChatChannelId() {
        if (TextUtils.isEmpty(this.mChatChannelId)) {
            if (this.mUserChannel != null && this.mUserChannel.channel.chatRoom != null) {
                this.mChatChannelId = this.mUserChannel.channel.chatRoom._id;
                this.mChatRoom = this.mUserChannel.channel.chatRoom;
            } else if (this.mGame == null || this.mGame.chatRoom == null) {
                this.mChatChannelId = null;
                this.mChatRoom = null;
            } else {
                this.mChatChannelId = this.mGame.chatRoom._id;
                this.mChatRoom = this.mGame.chatRoom;
            }
        }
        return this.mChatChannelId;
    }
}
