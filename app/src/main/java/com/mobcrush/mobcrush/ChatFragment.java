package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
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
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.ChatRateLimit;
import com.mobcrush.mobcrush.datamodel.ChatRoom;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.misc.SimpleChildEventListener;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.player.Player;
import com.mobcrush.mobcrush.ui.SwipeableRecyclerViewTouchListener;
import com.mobcrush.mobcrush.ui.SwipeableRecyclerViewTouchListener.SwipeListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public abstract class ChatFragment extends Fragment {
    private static final boolean CHAT_DEBUG = false;
    public static final long HIGH_MUTE_TIME = 63072000000L;
    public static final long LOW_MUTE_TIME = 600000;
    private static final int MAX_RETRIES = 4;
    public static final long MID_MUTE_TIME = 86400000;
    private static final int RETRY_DELAY_MS = 1000;
    private static final int RETRY_FACTOR = 3;
    private static final String TAG = "ChatFragment";
    AuthResultHandler authResultHandler = new AuthResultHandler() {
        public void onAuthenticated(AuthData authData) {
            if (ChatFragment.this.isAddedOrIsComponent() && authData != null && ChatFragment.this.mFirebase != null) {
                ChatFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        Throwable e;
                        try {
                            if (ChatFragment.this.mFirebase != null) {
                                boolean isAlreadyOrdered;
                                int i;
                                ChatFragment.this.subscribeForMetadata();
                                ChatFragment.this.subscribeForModeratorsList();
                                ChatFragment.this.updatePresence();
                                ChatFragment.this.mRoomUsers = ChatFragment.this.mFirebase.child(Constants.CHAT_ROOM_USERS).child(ChatFragment.this.getChatChannelId());
                                ChatFragment.this.mRoomUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        ChatFragment.this.mUsersInRoom = (int) dataSnapshot.getChildrenCount();
                                        if (ChatFragment.this.mRoomUsers != null) {
                                            ChatFragment.this.mRoomUsers.addChildEventListener(ChatFragment.this.mChatUsersListener);
                                        }
                                    }

                                    public void onCancelled(FirebaseError firebaseError) {
                                        if (ChatFragment.this.mRoomUsers != null && ChatFragment.this.mFirebase != null) {
                                            ChatFragment.this.mRoomUsers.addChildEventListener(ChatFragment.this.mChatUsersListener);
                                        }
                                    }
                                });
                                Network.loadAdminListFromFirebase();
                                ChatFragment.this.subscribeForMutedList();
                                if (Network.isLoggedIn()) {
                                    ChatFragment.this.mIgnoredUsers = ChatFragment.this.mFirebase.child(Constants.CHAT_ROOM_IGNORED_USERS).child(PreferenceUtility.getUser()._id);
                                    ChatFragment.this.mIgnoredUsers.addChildEventListener(ChatFragment.this.mIgnoredUsersListener);
                                }
                                Log.d(ChatFragment.TAG, "mQueryMessages: " + ChatFragment.this.mQueryMessages);
                                ChatFragment.this.mQueryMessages = ChatFragment.this.mFirebase.child(Constants.CHAT_ROOM_MESSAGES).child(ChatFragment.this.getChatChannelId());
                                if (ChatFragment.this.mChatRoom == null || ChatFragment.this.mChatRoom.filterBy == null) {
                                    isAlreadyOrdered = ChatFragment.CHAT_DEBUG;
                                } else {
                                    isAlreadyOrdered = true;
                                    ChatFragment.this.mQueryMessages = ChatFragment.this.mQueryMessages.orderByChild(ChatFragment.this.mChatRoom.filterBy).equalTo(ChatFragment.this.mBroadcastId);
                                }
                                Query query = ChatFragment.this.mQueryMessages;
                                if (isAlreadyOrdered) {
                                    i = 50;
                                } else {
                                    i = 10;
                                }
                                query.limitToLast(i).addListenerForSingleValueEvent(new ValueEventListener() {
                                    public void onDataChange(final DataSnapshot dataSnapshot) {
                                        if (ChatFragment.this.isAddedOrIsComponent()) {
                                            ChatFragment.this.mHandler.post(new Runnable() {
                                                public void run() {
                                                    if (dataSnapshot == null || dataSnapshot.getValue() == null || dataSnapshot.getChildrenCount() <= 0) {
                                                        ChatFragment.this.mLastHistoryMessageTimestamp = 0;
                                                    } else {
                                                        ChatFragment.this.addMessage(dataSnapshot, ChatFragment.CHAT_DEBUG, true);
                                                    }
                                                    if (ChatFragment.this.mQueryMessages != null) {
                                                        Log.d(ChatFragment.TAG, "config MessagesListener");
                                                        ChatFragment.this.mQueryMessages = ChatFragment.this.mQueryMessages.limitToLast(isAlreadyOrdered ? 50 : 10);
                                                        ChatFragment.this.mQueryMessages.addChildEventListener(ChatFragment.this.mChatMessagesListener);
                                                    }
                                                }
                                            });
                                        }
                                    }

                                    public void onCancelled(FirebaseError firebaseError) {
                                        ChatFragment.this.mLastHistoryMessageTimestamp = 0;
                                        if (ChatFragment.this.mQueryMessages != null) {
                                            Log.d(ChatFragment.TAG, "config MessagesListened on onCanceled");
                                            ChatFragment.this.mQueryMessages = ChatFragment.this.mQueryMessages.limitToLast(10);
                                            ChatFragment.this.mQueryMessages.addChildEventListener(ChatFragment.this.mChatMessagesListener);
                                        }
                                    }
                                });
                                if (!(ChatFragment.this.mMessagesAdapter == null || isAlreadyOrdered)) {
                                    ChatFragment.this.mMessagesAdapter.setGetDataCallback(new Callback() {
                                        public boolean handleMessage(Message message) {
                                            try {
                                                if (ChatFragment.this.mQueryMessages != null) {
                                                    ChatFragment.this.mQueryMessages.orderByChild(Constants.CHAT_MESSAGE_TIMESTAMP).endAt((double) (ChatFragment.this.mMessagesAdapter.getItem(0).timestamp - 1)).limitToLast(10).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        public void onDataChange(final DataSnapshot dataSnapshot) {
                                                            if (ChatFragment.this.isAddedOrIsComponent()) {
                                                                ChatFragment.this.mHandler.post(new Runnable() {
                                                                    public void run() {
                                                                        Log.d(ChatFragment.TAG, "get additional Messages");
                                                                        if (dataSnapshot != null && dataSnapshot.getValue() != null && dataSnapshot.getChildrenCount() > 0) {
                                                                            ChatFragment.this.addMessage(dataSnapshot, true, ChatFragment.CHAT_DEBUG);
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }

                                                        public void onCancelled(FirebaseError firebaseError) {
                                                            Log.e(ChatFragment.TAG, "GotMoreMessages.onDataChange.ERROR: " + firebaseError);
                                                        }
                                                    });
                                                }
                                            } catch (Throwable e) {
                                                ChatFragment.this.mMessagesAdapter.onGetDataFailed();
                                                e.printStackTrace();
                                                if (!TextUtils.equals(e.getMessage(), "You can't combine multiple orderBy calls!")) {
                                                    Crashlytics.logException(e);
                                                }
                                            }
                                            return true;
                                        }
                                    });
                                }
                                if (ChatFragment.this.mChatOptionsBtn != null) {
                                    ChatFragment.this.mChatOptionsBtn.setVisibility(0);
                                }
                            }
                        } catch (Throwable e2) {
                            e = new Exception("Can't get child Firebase: " + ChatFragment.this.mFirebase + "; channel: " + ChatFragment.this.getChatChannelId(), e2);
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }

        public void onAuthenticationError(final FirebaseError firebaseError) {
            if (ChatFragment.this.isAddedOrIsComponent()) {
                if (ChatFragment.this.mRetryHandler == null) {
                    ChatFragment.this.mRetryHandler = new Handler(Looper.getMainLooper());
                }
                int delay = (ChatFragment.this.mRetryCount * ChatFragment.RETRY_FACTOR) * ChatFragment.RETRY_DELAY_MS;
                ChatFragment.this.mRetryHandler.removeCallbacks(null);
                ChatFragment.this.mRetryHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            Log.e(ChatFragment.TAG, firebaseError.getMessage(), firebaseError.toException());
                            PreferenceUtility.removeFirebaseToken();
                            ChatFragment.this.mFirebase = null;
                            ChatFragment.this.mChatIsConfigured = ChatFragment.CHAT_DEBUG;
                            if (ChatFragment.this.mRetryCount < ChatFragment.MAX_RETRIES) {
                                ChatFragment.access$904(ChatFragment.this);
                                ChatFragment.this.configChat();
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                }, (long) delay);
            }
        }
    };
    private Handler mActiveMuteChecker;
    protected boolean mAutoScrollDisabled;
    protected String mBroadcastId;
    protected boolean mBroadcastWasLive;
    private boolean mChatIsConfigured;
    protected ChatRateLimit mChatLimit;
    protected final Runnable mChatLimitationCountdown = new Runnable() {
        public void run() {
            if (ChatFragment.this.isAdded() && ChatFragment.this.mChatLimit != null) {
                ChatFragment.this.mChatLimitationTimer.removeCallbacks(null);
                long s = ((long) (ChatFragment.this.mChatLimit.secondsPer * ChatFragment.RETRY_DELAY_MS)) - (System.currentTimeMillis() - ChatFragment.this.mLastMessageTimestamp);
                if (ChatFragment.this.mSlowModeTimer == null) {
                    return;
                }
                if (s > 1000) {
                    ChatFragment.this.mSlowModeTimer.setText(String.valueOf(s / 1000));
                    ChatFragment.this.mChatLimitationTimer.postDelayed(ChatFragment.this.mChatLimitationCountdown, 1000);
                    return;
                }
                ChatFragment.this.mSlowModeTimer.setText(BuildConfig.FLAVOR);
                ChatFragment.this.mSlowModeTimer.setVisibility(8);
                ChatFragment.this.mChatLimited = ChatFragment.CHAT_DEBUG;
                if (ChatFragment.this.mOnChatLimitCleared != null) {
                    ChatFragment.this.mOnChatLimitCleared.handleMessage(Message.obtain());
                }
            }
        }
    };
    protected Handler mChatLimitationTimer;
    protected boolean mChatLimited = CHAT_DEBUG;
    protected Callback mChatMembersCountChangingListener;
    protected TextView mChatMessageText;
    private final SimpleChildEventListener mChatMessagesListener = new SimpleChildEventListener() {
        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            if (ChatFragment.this.isAddedOrIsComponent()) {
                ChatFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            ChatMessage message = ChatMessage.from(dataSnapshot);
                            ChatFragment.this.addMessage(message, ChatFragment.CHAT_DEBUG, true);
                            if (ChatFragment.this.mOnNewMessageCallback != null && !message.owner) {
                                ChatFragment.this.mOnNewMessageCallback.handleMessage(Message.obtain(null, 0, message));
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }

        public void onChildChanged(final DataSnapshot dataSnapshot, String s) {
            if (ChatFragment.this.isAddedOrIsComponent()) {
                ChatFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            ChatFragment.this.changeMessage(ChatMessage.from(dataSnapshot));
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }
    };
    private ValueEventListener mChatMetadataListener = new ValueEventListener() {
        public void onDataChange(final DataSnapshot dataSnapshot) {
            if (ChatFragment.this.isAdded()) {
                ChatFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Log.d(ChatFragment.TAG, "mChatMetadataListener onDataChange : " + dataSnapshot);
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            try {
                                ChatFragment.this.applyChatLimitation(ChatRateLimit.from(dataSnapshot));
                            } catch (Throwable e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }
                    }
                });
            }
        }

        public void onCancelled(FirebaseError firebaseError) {
        }
    };
    private ValueEventListener mChatModeratorUsersListener = new ValueEventListener() {
        public void onDataChange(final DataSnapshot dataSnapshot) {
            if (ChatFragment.this.isAddedOrIsComponent()) {
                ChatFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            try {
                                HashMap map = (HashMap) dataSnapshot.getValue(HashMap.class);
                                boolean oldVal = ChatFragment.this.mUserIsModerator;
                                ChatFragment.this.mUserIsModerator = map.containsKey(PreferenceUtility.getUser()._id);
                                if (ChatFragment.this.mUserIsModerator != oldVal) {
                                    LocalBroadcastManager.getInstance(ChatFragment.this.getActivity()).sendBroadcast(new Intent(Constants.EVENT_MOD_CHANGED));
                                    if (ChatFragment.this.mUserIsModerator) {
                                        ChatFragment.this.confirmModerator();
                                    }
                                    ChatFragment.this.updatePresence();
                                    ChatFragment.this.updateModeratorStatus();
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }
                    }
                });
            }
        }

        public void onCancelled(FirebaseError firebaseError) {
        }
    };
    protected final Runnable mChatMuteChecker = new Runnable() {
        public void run() {
            boolean z = ChatFragment.CHAT_DEBUG;
            ChatFragment.this.mChatMuteHandler.removeCallbacks(null);
            ChatFragment.this.mChatMuteHandler.postDelayed(ChatFragment.this.mChatMuteChecker, 1000);
            ChatFragment chatFragment = ChatFragment.this;
            chatFragment.mChatPoints -= Constants.CHAT_POINTS_TICKER;
            if (ChatFragment.this.mChatPoints < 0) {
                ChatFragment.this.mChatPoints = 0;
            }
            chatFragment = ChatFragment.this;
            if (ChatFragment.this.mChatPoints == 0) {
                z = true;
            }
            chatFragment.mUserIsAutoMuted = z;
        }
    };
    protected final Handler mChatMuteHandler = new Handler();
    protected ImageView mChatOptionsBtn;
    protected int mChatPoints;
    protected ChatRoom mChatRoom;
    private final SimpleChildEventListener mChatUsersListener = new SimpleChildEventListener() {
        private long mCurrentUserTimestamp = -1;

        private ChatMessage getChatMessage(DataSnapshot dataSnapshot) {
            ChatMessage chatMessage = null;
            try {
                Map map = (Map) dataSnapshot.getValue(Map.class);
                long timestamp = 0;
                Object o = map.get(Constants.CHAT_MESSAGE_TIMESTAMP);
                if (o != null) {
                    timestamp = Long.parseLong(String.valueOf(o));
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            if (ChatFragment.this.mUsersInRoom < 20 && ChatFragment.this.isShowJoinedRequired()) {
                chatMessage = new ChatMessage(MainApplication.getRString(R.string.joined, new Object[0]), timestamp, map).setSystemMsg();
            }
            return chatMessage;
        }

        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            if (ChatFragment.this.isAddedOrIsComponent()) {
                ChatFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            ChatMessage message = AnonymousClass4.this.getChatMessage(dataSnapshot);
                            if (message == null) {
                                return;
                            }
                            if (TextUtils.equals(message.getUserId(), PreferenceUtility.getUser()._id)) {
                                AnonymousClass4.this.mCurrentUserTimestamp = message.timestamp;
                            } else if (AnonymousClass4.this.mCurrentUserTimestamp > 0 && message.timestamp > AnonymousClass4.this.mCurrentUserTimestamp) {
                                ChatFragment chatFragment = ChatFragment.this;
                                chatFragment.mUsersInRoom++;
                                ChatFragment.this.addMessage(message, true, true);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }

        public void onChildChanged(final DataSnapshot dataSnapshot, final String s) {
            if (ChatFragment.this.isAddedOrIsComponent()) {
                ChatFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            AnonymousClass4.this.onChildAdded(dataSnapshot, s);
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }

        public void onChildRemoved(final DataSnapshot dataSnapshot) {
            if (ChatFragment.this.isAddedOrIsComponent()) {
                ChatFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            ChatFragment chatFragment = ChatFragment.this;
                            chatFragment.mUsersInRoom--;
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }
    };
    protected boolean mCurrentUserIsBanned = CHAT_DEBUG;
    protected boolean mCurrentUserIsMuted = CHAT_DEBUG;
    protected long mCurrentUserTimestamp = -1;
    protected Firebase mCurrentUserValue;
    public View mEditLayout;
    public EditText mEditText;
    protected Firebase mFirebase;
    protected boolean mFirebaseLessor;
    protected final Handler mHandler = new Handler(Looper.getMainLooper());
    protected boolean mIgnoreDialogIsShown = CHAT_DEBUG;
    protected Firebase mIgnoredUsers;
    protected ArrayList<String> mIgnoredUsersList = new ArrayList();
    private SimpleChildEventListener mIgnoredUsersListener = new SimpleChildEventListener() {
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue(HashMap.class);
            if (map != null && map.containsKey(ChatMessage.USER_ID)) {
                try {
                    String id = (String) map.get(ChatMessage.USER_ID);
                    if (!ChatFragment.this.mIgnoredUsersList.contains(id)) {
                        ChatFragment.this.mIgnoredUsersList.add(id);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        }

        public void onChildRemoved(final DataSnapshot dataSnapshot) {
            if (ChatFragment.this.isAddedOrIsComponent()) {
                ChatFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            HashMap map = (HashMap) dataSnapshot.getValue(HashMap.class);
                            if (map != null && map.containsKey(ChatMessage.USER_ID)) {
                                ChatFragment.this.mIgnoredUsersList.remove(String.valueOf(map.get(ChatMessage.USER_ID)));
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }
    };
    protected boolean mIsCurrentUserOwner = CHAT_DEBUG;
    protected boolean mIsLiveBroadcast;
    protected long mLastHistoryMessageTimestamp = -1;
    protected String mLastMessage;
    private long mLastMessageTimestamp;
    protected boolean mLoadHistory = true;
    private int mMessagePerLimitedTime;
    public ChatMessagesAdapter mMessagesAdapter;
    protected Firebase mModeratorUsers;
    protected long mMuteBannerWasShownForTime;
    protected boolean mMuteDialogIsShown = CHAT_DEBUG;
    protected Long mMutedTill;
    protected Firebase mMutedUsers;
    private ValueEventListener mMutedUsersListener = new ValueEventListener() {
        public void onDataChange(final DataSnapshot dataSnapshot) {
            if (ChatFragment.this.isAddedOrIsComponent()) {
                ChatFragment.this.mHandler.post(new Runnable() {
                    public void run() {
                        if (dataSnapshot != null && dataSnapshot.getValue() != null) {
                            try {
                                HashMap map = (HashMap) ((HashMap) dataSnapshot.getValue(HashMap.class)).get(PreferenceUtility.getUser()._id);
                                if (map == null) {
                                    ChatFragment.this.applyBanSettings(ChatFragment.CHAT_DEBUG);
                                } else {
                                    Object banned = map.get(ChatMessage.BANNED);
                                    if (banned != null) {
                                        ChatFragment.this.applyBanSettings(((Boolean) banned).booleanValue());
                                        return;
                                    }
                                }
                                ChatFragment.this.mMutedTill = ModerationHelper.getExpireTimestamp(map);
                                if (ChatFragment.this.mMutedTill == null) {
                                    ChatFragment.this.mMutedTill = Long.valueOf(0);
                                }
                                if (ChatFragment.this.mMutedTill != null) {
                                    ChatFragment.this.applyMuteSettings();
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                                Crashlytics.logException(e);
                            }
                        }
                    }
                });
            }
        }

        public void onCancelled(FirebaseError firebaseError) {
        }
    };
    protected Handler mNotificationHandler;
    protected View mNotificationLayout;
    protected Callback mOnChatLimitCleared;
    private Callback mOnNewMessageCallback;
    protected Query mQueryMessages;
    public RecyclerView mRecyclerView;
    protected boolean mReleaseChatOnPause = true;
    private int mRetryCount = 0;
    private Handler mRetryHandler;
    protected Firebase mRoomMetadata;
    protected Firebase mRoomUsers;
    protected long mSentMessagesCount;
    protected boolean mShowJoined;
    protected ImageView mSlowModeBtn;
    protected TextView mSlowModeTimer;
    protected boolean mUserIsAutoMuted;
    protected boolean mUserIsModerator;
    protected int mUsersInRoom = 0;

    protected enum VisualOperation {
        Hide,
        Show
    }

    static /* synthetic */ int access$904(ChatFragment x0) {
        int i = x0.mRetryCount + 1;
        x0.mRetryCount = i;
        return i;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this.mMessagesAdapter == null) {
            this.mMessagesAdapter = new ChatMessagesAdapter(getActivity(), null);
            this.mMessagesAdapter.setActionCallback(new Callback() {
                public boolean handleMessage(Message message) {
                    if (message != null) {
                        switch (message.what) {
                            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                                ChatFragment.this.performIgnoreDialog(message.arg1);
                                return true;
                            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                                ChatFragment.this.performMuteDialog(message.arg1);
                                return true;
                            case Player.STATE_ENDED /*5*/:
                                ChatFragment.this.appointAsModerator(message.arg1);
                                return true;
                            case DayPickerView.DAYS_PER_WEEK /*7*/:
                                ChatFragment.this.banUser(message.arg1);
                                return true;
                        }
                    }
                    return ChatFragment.CHAT_DEBUG;
                }
            });
        }
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mNotificationLayout = getActivity().findViewById(R.id.popup_notification_layout);
        if (isCurrentUserOwner()) {
            this.mMessagesAdapter.showAppointOption();
        }
        SwipeableRecyclerViewTouchListener swipeTouchListener = new SwipeableRecyclerViewTouchListener(this.mRecyclerView, new SwipeListener() {
            public boolean canSwipe(int position) {
                return (!ChatFragment.this.isCurrentUserOwner() || ChatFragment.this.mMessagesAdapter == null || TextUtils.equals(ChatFragment.this.mMessagesAdapter.getItem(position).getUserId(), PreferenceUtility.getUser()._id)) ? ChatFragment.CHAT_DEBUG : true;
            }

            public void onDismissedBySwipeLeft(RecyclerView recyclerView, int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    ChatFragment.this.performMuteDialog(position);
                }
            }

            public void onDismissedBySwipeRight(RecyclerView recyclerView, int[] reverseSortedPositions) {
                for (int position : reverseSortedPositions) {
                    ChatFragment.this.performMuteDialog(position);
                }
            }
        }, R.id.message_layout, R.id.action_layout);
        if (this.mRecyclerView != null) {
            this.mRecyclerView.addOnItemTouchListener(swipeTouchListener);
        }
    }

    public void onResume() {
        super.onResume();
        MainApplication.mChatMessagesAdapter = this.mMessagesAdapter;
        Long tmp = (Long) MainApplication.mChatTimestamps.get(getChatChannelId());
        this.mLastMessageTimestamp = tmp == null ? 0 : tmp.longValue();
        this.mChatLimited = isChatLimitReached(CHAT_DEBUG);
        if (this.mChatLimited) {
            this.mChatLimitationTimer = new Handler();
            this.mChatLimitationCountdown.run();
        }
        try {
            if (!(this.mChatIsConfigured || !this.mReleaseChatOnPause || this.mFirebaseLessor)) {
                configChat();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mRecyclerView.addOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == 1) {
                    ChatFragment.this.mAutoScrollDisabled = true;
                } else if (newState != 2) {
                    int pos = ((LinearLayoutManager) ChatFragment.this.mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                    ChatFragment.this.mAutoScrollDisabled = pos < ChatFragment.this.mMessagesAdapter.getItemCount() + -2 ? true : ChatFragment.CHAT_DEBUG;
                }
            }
        });
    }

    public void onPause() {
        super.onPause();
        if (this.mNotificationHandler != null) {
            this.mNotificationHandler.removeCallbacks(null);
            this.mNotificationHandler = null;
        }
        if (this.mNotificationLayout != null) {
            this.mNotificationLayout.clearAnimation();
            this.mNotificationLayout.setVisibility(8);
        }
        try {
            if (getChatChannelId() != null && this.mReleaseChatOnPause) {
                releaseChat();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public void releaseChat() {
        this.mChatIsConfigured = CHAT_DEBUG;
        try {
            if (!(this.mQueryMessages == null || this.mChatMessagesListener == null)) {
                this.mQueryMessages.removeEventListener(this.mChatMessagesListener);
                this.mQueryMessages = null;
            }
            if (!(this.mMutedUsers == null || this.mMutedUsersListener == null)) {
                this.mMutedUsers.removeEventListener(this.mMutedUsersListener);
                this.mMutedUsers = null;
            }
            if (!(this.mModeratorUsers == null || this.mChatModeratorUsersListener == null)) {
                this.mModeratorUsers.removeEventListener(this.mChatModeratorUsersListener);
                this.mModeratorUsers = null;
            }
            if (!(this.mIgnoredUsers == null || this.mIgnoredUsersListener == null)) {
                this.mIgnoredUsers.removeEventListener(this.mIgnoredUsersListener);
                this.mIgnoredUsers = null;
            }
            if (!(this.mRoomUsers == null || this.mChatUsersListener == null)) {
                this.mRoomUsers.removeEventListener(this.mChatUsersListener);
                this.mRoomUsers = null;
            }
            if (!(this.mFirebaseLessor || this.mCurrentUserValue == null)) {
                this.mCurrentUserValue.removeValue(new CompletionListener() {
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    }
                });
                this.mCurrentUserValue = null;
            }
            if (!(this.mRoomMetadata == null || this.mChatMetadataListener == null)) {
                this.mRoomMetadata.removeEventListener(this.mChatMetadataListener);
                this.mRoomMetadata = null;
            }
            if (this.mRetryHandler != null) {
                this.mRetryHandler.removeCallbacks(null);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        if (!this.mFirebaseLessor) {
            this.mFirebase = null;
        }
    }

    public boolean isAddedOrIsComponent() {
        return isAdded();
    }

    public void scrollToLatestMessage(boolean force) {
        if (this.mRecyclerView == null || this.mMessagesAdapter.getItemCount() <= 0) {
            Log.e(TAG, "mRecyclerView: " + this.mRecyclerView + "; mMessagesAdapter.getItemCount(): " + this.mMessagesAdapter.getItemCount());
            return;
        }
        int pos = ((LinearLayoutManager) this.mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
        if (!this.mAutoScrollDisabled || force) {
            this.mRecyclerView.scrollToPosition(this.mMessagesAdapter.getItemCount() - 1);
        }
    }

    protected void setChatMembersCountChangingListener(Callback callback) {
        this.mChatMembersCountChangingListener = callback;
    }

    public void configChat() {
        try {
            this.mChatIsConfigured = true;
            if (this.mMessagesAdapter != null) {
                this.mMessagesAdapter.clear();
            }
            if (this.mFirebase == null) {
                this.mFirebase = new Firebase(Constants.CHAT_BASE_ADDRESS);
                MainApplication.mFirebase = this.mFirebase;
            }
            if (PreferenceUtility.getUser().isGuest(MainApplication.getContext()) || !PreferenceUtility.isEmailVerified()) {
                this.mFirebase.authAnonymously(this.authResultHandler);
            } else if (TextUtils.isEmpty(PreferenceUtility.getFirebaseToken())) {
                Network.getFirebaseToken(getActivity(), new Listener<Boolean>() {
                    public void onResponse(Boolean response) {
                        if (response == null || !response.booleanValue() || ChatFragment.this.mFirebase == null) {
                            ChatFragment.this.mChatIsConfigured = ChatFragment.CHAT_DEBUG;
                        } else {
                            ChatFragment.this.mFirebase.authWithCustomToken(PreferenceUtility.getFirebaseToken(), ChatFragment.this.authResultHandler);
                        }
                    }
                }, null);
            } else {
                this.mFirebase.authWithCustomToken(PreferenceUtility.getFirebaseToken(), this.authResultHandler);
            }
            if (this.mMessagesAdapter != null) {
                if (!(isCurrentUserOwner() || this.mUserIsModerator || ModerationHelper.isAdmin(PreferenceUtility.getUser()._id))) {
                    this.mMessagesAdapter.addDisabledActions(R.id.action_mute);
                    this.mMessagesAdapter.addDisabledActions(R.id.action_ban);
                }
                if (PreferenceUtility.getUser().isGuest(MainApplication.getContext())) {
                    this.mMessagesAdapter.addDisabledActions(R.id.action_ignore);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    private RoleType getRoleOfCurrentUser() {
        User user = PreferenceUtility.getUser();
        if (isCurrentUserOwner()) {
            return RoleType.broadcaster;
        }
        if (ModerationHelper.isAdmin(user._id)) {
            return RoleType.admin;
        }
        if (this.mUserIsModerator) {
            return RoleType.moderator;
        }
        return RoleType.user;
    }

    protected void updatePresence() {
        User u = PreferenceUtility.getUser();
        Map<String, Object> post = new HashMap();
        String userId = this.mFirebase.getAuth().getUid();
        if (!u.isGuest(getActivity())) {
            userId = u._id;
            post.put(ChatMessage.USER_ID, u._id);
            post.put(ChatMessage.USERNAME, u.username);
            post.put(ChatMessage.PROFILE_LOGO_SMALL, u.profileLogoSmall != null ? u.profileLogoSmall : u.profileLogo);
            post.put(ChatMessage.ROLE, getRoleOfCurrentUser().toString());
            post.put(ChatMessage.SUBTITLE, u.subtitle);
        }
        post.put(ChatMessage.CLIENT, Network.getUserAgent());
        post.put(Constants.CHAT_MESSAGE_TIMESTAMP, ServerValue.TIMESTAMP);
        this.mCurrentUserValue = null;
        try {
            this.mCurrentUserValue = this.mFirebase.child(u.isGuest(MainApplication.getContext()) ? Constants.CHAT_ROOM_ANONYMOUS_USERS : Constants.CHAT_ROOM_USERS).child(getChatChannelId()).child(userId);
        } catch (Throwable e) {
            Throwable e2 = new Exception("Can't get child for user: " + u + "; Firebase: " + this.mFirebase + "; channel: " + getChatChannelId(), e);
            e2.printStackTrace();
            Crashlytics.logException(e2);
        }
        if (this.mCurrentUserValue != null) {
            this.mCurrentUserValue.setValue(post);
            this.mCurrentUserValue.addListenerForSingleValueEvent(new ValueEventListener() {
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot == null || dataSnapshot.getValue() == null) {
                        ChatFragment.this.mCurrentUserTimestamp = 0;
                        return;
                    }
                    Object o = ((Map) dataSnapshot.getValue(Map.class)).get(Constants.CHAT_MESSAGE_TIMESTAMP);
                    if (o != null) {
                        try {
                            ChatFragment.this.mCurrentUserTimestamp = Long.parseLong(String.valueOf(o));
                        } catch (Throwable e) {
                            e.printStackTrace();
                            ChatFragment.this.mCurrentUserTimestamp = 0;
                        }
                    }
                }

                public void onCancelled(FirebaseError firebaseError) {
                    ChatFragment.this.mCurrentUserTimestamp = 0;
                }
            });
            this.mCurrentUserValue.onDisconnect().removeValue(new CompletionListener() {
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                }
            });
        }
    }

    protected void subscribeForMetadata() {
        this.mRoomMetadata = this.mFirebase.child(Constants.CHAT_ROOM_METADATA).child(getChatChannelId());
        this.mRoomMetadata.addValueEventListener(this.mChatMetadataListener);
    }

    protected void subscribeForModeratorsList() {
        this.mModeratorUsers = this.mFirebase.child(Constants.CHAT_ROOM_MODERATORS).child(getChatChannelId());
        this.mModeratorUsers.addValueEventListener(this.mChatModeratorUsersListener);
    }

    protected void subscribeForMutedList() {
        this.mMutedUsers = this.mFirebase.child(Constants.CHAT_ROOM_MUTED_USERS).child(getChatChannelId());
        this.mMutedUsers.addValueEventListener(this.mMutedUsersListener);
    }

    public void sendMessage(String message, boolean systemMessage) {
        if (message != null && !TextUtils.isEmpty(message.trim())) {
            User u = PreferenceUtility.getUser();
            if (u.isGuest(MainApplication.getContext())) {
                Log.e(TAG, "Anonymous can't send message");
                return;
            }
            try {
                if (TextUtils.equals(MainApplication.getRString(R.string.action_like, new Object[0]).toUpperCase(), message)) {
                    if (DBLikedChannelsHelper.isChannelLiked(MainApplication.getContext(), PreferenceUtility.getUser(), getChatChannelId())) {
                        Log.e(TAG, "channel " + getChatChannelId() + " is already liked");
                        return;
                    }
                    DBLikedChannelsHelper.saveLikedChannel(MainApplication.getContext(), PreferenceUtility.getUser(), getChatChannelId());
                    if (this.mUsersInRoom >= 20) {
                        return;
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            try {
                final ChatMessage m = new ChatMessage();
                m.setMessage(message.replace("\r", BuildConfig.FLAVOR));
                m.setUserInfo(u);
                if (systemMessage) {
                    m.setSystemMsg();
                }
                if (isCurrentUserOwner()) {
                    m.setOwner();
                }
                m.role = getRoleOfCurrentUser();
                if (!(isLiveVideo() || wasLiveVideo())) {
                    m.setFromVOD();
                }
                Log.d(TAG, "getChatChannelId: " + getChatChannelId());
                if (isMessageCanBeSent(message)) {
                    Firebase newPostRef = this.mFirebase.child(Constants.CHAT_ROOM_MESSAGES).child(getChatChannelId()).push();
                    Map<String, Object> map = m.getMapWithMetadata(this.mChatRoom != null ? this.mChatRoom.metadata : null);
                    map.put(Constants.CHAT_MESSAGE_TIMESTAMP, ServerValue.TIMESTAMP);
                    newPostRef.setValue(map);
                    this.mLastMessageTimestamp = System.currentTimeMillis();
                    MainApplication.mChatTimestamps.put(getChatChannelId(), Long.valueOf(this.mLastMessageTimestamp));
                    if (this.mSlowModeTimer != null && this.mChatLimit != null && this.mChatLimit.enabled && doesSlowModeApplyToUser()) {
                        this.mChatLimited = true;
                        this.mSlowModeTimer.setVisibility(0);
                        this.mSlowModeTimer.setText(String.valueOf(this.mChatLimit.secondsPer));
                        this.mChatLimitationTimer = new Handler();
                        this.mChatLimitationTimer.postDelayed(this.mChatLimitationCountdown, 1000);
                    }
                    this.mSentMessagesCount++;
                } else if (isAddedOrIsComponent()) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            ChatFragment.this.addMessage(m, ChatFragment.CHAT_DEBUG, true);
                        }
                    });
                }
            } catch (Throwable e2) {
                e2.printStackTrace();
            }
        }
    }

    private boolean doesSlowModeApplyToUser() {
        return (isCurrentUserOwner() || this.mUserIsModerator || ModerationHelper.isAdmin(PreferenceUtility.getUser()._id)) ? CHAT_DEBUG : true;
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
        this.mLastMessageTimestamp = System.currentTimeMillis();
        if ((this.mUserIsAutoMuted || TextUtils.equals(this.mLastMessage, messsage)) && !isCurrentUserOwner()) {
            return CHAT_DEBUG;
        }
        return true;
    }

    public boolean isChatLimitReached(boolean showNotification) {
        if (this.mChatLimit != null && this.mChatLimit.enabled && doesSlowModeApplyToUser()) {
            if (System.currentTimeMillis() - this.mLastMessageTimestamp < ((long) (this.mChatLimit.secondsPer * RETRY_DELAY_MS))) {
                this.mMessagePerLimitedTime++;
                if (this.mMessagePerLimitedTime >= this.mChatLimit.rate) {
                    if (showNotification) {
                        int i;
                        Context context = MainApplication.getContext();
                        Object[] objArr = new Object[MAX_RETRIES];
                        objArr[0] = Integer.valueOf(this.mChatLimit.rate);
                        if (this.mChatLimit.rate == 1) {
                            i = R.string.message;
                        } else {
                            i = R.string.messages;
                        }
                        objArr[1] = MainApplication.getRString(i, new Object[0]);
                        objArr[2] = Integer.valueOf(this.mChatLimit.secondsPer);
                        if (this.mChatLimit.secondsPer == 1) {
                            i = R.string.second;
                        } else {
                            i = R.string.seconds;
                        }
                        objArr[RETRY_FACTOR] = MainApplication.getRString(i, new Object[0]);
                        Toast.makeText(context, MainApplication.getRString(R.string.chat_slow_mode, objArr), 1).show();
                    }
                    return true;
                }
            }
            this.mMessagePerLimitedTime = 0;
        }
        return CHAT_DEBUG;
    }

    protected void changeMessage(ChatMessage m) {
        try {
            if (isAddedOrIsComponent() && m != null) {
                if (m.banned) {
                    this.mMessagesAdapter.remove(m);
                } else {
                    this.mMessagesAdapter.change(m);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    protected synchronized void addMessage(Object message, boolean checkForUnique, boolean scrollToLatestMessage) {
        try {
            if (isAddedOrIsComponent() && message != null) {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    Exception e = new Exception("addMessage was called not from UI thread");
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    final Object obj = message;
                    final boolean z = checkForUnique;
                    final boolean z2 = scrollToLatestMessage;
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            ChatFragment.this.addMessage(obj, z, z2);
                        }
                    });
                } else {
                    String str;
                    ChatMessage m;
                    if (message instanceof DataSnapshot) {
                        for (DataSnapshot child : ((DataSnapshot) message).getChildren()) {
                            try {
                                m = ChatMessage.from(child);
                                if (m != null) {
                                    this.mLastHistoryMessageTimestamp = Math.max(this.mLastHistoryMessageTimestamp, m.timestamp);
                                    if (!(m.isHidden() || m.isMuteOrigin() || m.banned || this.mIgnoredUsersList.contains(m.getUserId()))) {
                                        if (!((m.isFromVod() && isLiveVideo()) || (TextUtils.isEmpty(m.username) && TextUtils.equals(m.message, MainApplication.getRString(R.string.joined, new Object[0]))))) {
                                            this.mMessagesAdapter.add(m, checkForUnique, !scrollToLatestMessage ? true : CHAT_DEBUG);
                                        }
                                    }
                                }
                            } catch (Throwable e2) {
                                e2.printStackTrace();
                                this.mLastHistoryMessageTimestamp = 0;
                                Throwable e3 = new Exception("Error while adding message: " + child, e2);
                                Crashlytics.logException(e3);
                                Log.e(TAG, BuildConfig.FLAVOR, e3);
                            }
                        }
                        if (scrollToLatestMessage) {
                            this.mMessagesAdapter.notifyDataSetChanged();
                        }
                        scrollToLatestMessage(scrollToLatestMessage);
                    } else if (message instanceof ChatMessage) {
                        m = (ChatMessage) message;
                        if (!(m.timestamp <= this.mLastHistoryMessageTimestamp || m.isHidden() || m.isMuteOrigin() || this.mIgnoredUsersList.contains(m.getUserId()) || ((m.isFromVod() && isLiveVideo()) || (TextUtils.isEmpty(m.username) && TextUtils.equals(m.message, MainApplication.getRString(R.string.joined, new Object[0])))))) {
                            this.mMessagesAdapter.add(m, checkForUnique, true);
                            scrollToLatestMessage(CHAT_DEBUG);
                        }
                    } else if (message instanceof JSONObject) {
                        this.mMessagesAdapter.add(message.toString());
                    } else if (message instanceof JSONArray) {
                        JSONArray a = ((JSONArray) message).optJSONArray(0);
                        if (a != null) {
                            for (int i = 0; i < a.length(); i++) {
                                Object o = a.opt(i);
                                if (o instanceof String) {
                                    this.mMessagesAdapter.add(new ChatMessage().setMessage((String) message), true);
                                } else if (o instanceof JSONObject) {
                                    this.mMessagesAdapter.add(o.toString());
                                }
                            }
                        }
                    } else if (message instanceof String) {
                        this.mMessagesAdapter.add(new ChatMessage().setMessage((String) message), true);
                    }
                    if (this.mMessagesAdapter.getItemCount() > 0) {
                        str = this.mMessagesAdapter.getItem(this.mMessagesAdapter.getItemCount() - 1).message;
                    } else {
                        str = null;
                    }
                    this.mLastMessage = str;
                }
            }
        } catch (Throwable e22) {
            e22.printStackTrace();
            Crashlytics.logException(e22);
        }
    }

    protected void toggleSlowMode() {
        boolean z = true;
        Map<String, Object> limit = new HashMap();
        limit.put(ChatRateLimit.RATE, Integer.valueOf(1));
        limit.put(ChatRateLimit.SECONDS_PER, Integer.valueOf(30));
        String str = ChatRateLimit.ENABLED;
        if (this.mChatLimit != null && this.mChatLimit.enabled) {
            z = CHAT_DEBUG;
        }
        limit.put(str, Boolean.valueOf(z));
        this.mRoomMetadata.child(ChatRateLimit.RATE_LIMIT).setValue(limit);
    }

    protected void applyChatLimitation(@NonNull ChatRateLimit rateLimit) {
        int i = R.string.slow_mode_active;
        if (this.mChatLimit == null || this.mChatLimit.enabled != rateLimit.enabled) {
            if (rateLimit.enabled) {
                String rString;
                String rString2 = MainApplication.getRString(R.string.slow_mode_banner_title, new Object[0]);
                if (doesSlowModeApplyToUser()) {
                    Object[] objArr = new Object[MAX_RETRIES];
                    objArr[0] = Integer.valueOf(rateLimit.rate);
                    objArr[1] = MainApplication.getRString(rateLimit.rate == 1 ? R.string.message : R.string.messages, new Object[0]);
                    objArr[2] = Integer.valueOf(rateLimit.secondsPer);
                    objArr[RETRY_FACTOR] = MainApplication.getRString(rateLimit.secondsPer == 1 ? R.string.second : R.string.seconds, new Object[0]);
                    rString = MainApplication.getRString(R.string.slow_mode_banner_description, objArr);
                } else {
                    rString = null;
                }
                showInfoBanner(null, rString2, rString);
            } else if (this.mChatLimit != null) {
                showInfoBanner(null, MainApplication.getRString(R.string.slow_mode_disabled_banner_title, new Object[0]), MainApplication.getRString(R.string.slow_mode_disabled_banner_description, new Object[0]));
            }
            this.mChatLimit = rateLimit;
        }
        if (this.mSlowModeBtn != null) {
            this.mSlowModeBtn.setImageResource(this.mChatLimit.enabled ? R.drawable.ic_chat_slowmode_active : R.drawable.ic_chat_slowmode);
        }
        if (this.mEditText != null) {
            this.mEditText.setHint(this.mChatLimit.enabled ? R.string.slow_mode_active : R.string.join_the_conversation);
        }
        if (this.mChatMessageText != null) {
            TextView textView = this.mChatMessageText;
            if (!this.mChatLimit.enabled) {
                i = R.string.join_the_conversation;
            }
            textView.setHint(i);
        }
    }

    private void applyBanSettings(boolean isBanned) {
        if (isAddedOrIsComponent() && isBanned != this.mCurrentUserIsBanned) {
            if (this.mCurrentUserIsBanned) {
                ModerationHelper.addToRoom(getActivity(), this.mFirebase, PreferenceUtility.getUser(), getChatChannelId());
            }
            this.mCurrentUserIsBanned = isBanned;
        }
    }

    protected void applyMuteSettings() {
        OnClickListener onClickListener = null;
        if (isAddedOrIsComponent()) {
            try {
                String s;
                boolean z = (this.mMutedTill.longValue() <= 0 || this.mMutedTill.longValue() <= System.currentTimeMillis()) ? CHAT_DEBUG : true;
                this.mCurrentUserIsMuted = z;
                if (this.mCurrentUserIsMuted) {
                    s = Utils.getMuteDateTimeString(getActivity(), this.mMutedTill.longValue(), CHAT_DEBUG);
                } else {
                    s = getString(this.mUserIsModerator ? R.string.you_are_a_moderator : R.string.join_the_conversation);
                }
                if (this.mChatMessageText != null) {
                    TextView textView = this.mChatMessageText;
                    if (!this.mCurrentUserIsMuted && (this instanceof GameDetailsFragment)) {
                        Object obj = (GameDetailsFragment) this;
                    }
                    textView.setOnClickListener(onClickListener);
                    this.mChatMessageText.setHint(s);
                }
                if (this.mEditText != null) {
                    this.mEditText.setEnabled(!this.mCurrentUserIsMuted ? true : CHAT_DEBUG);
                    this.mEditText.setHint(s);
                }
                if (this.mCurrentUserIsMuted) {
                    if (this.mActiveMuteChecker == null) {
                        this.mActiveMuteChecker = new Handler();
                    }
                    this.mActiveMuteChecker.removeCallbacks(null);
                    this.mActiveMuteChecker.postDelayed(new Runnable() {
                        public void run() {
                            ChatFragment.this.applyMuteSettings();
                        }
                    }, Math.max(500, (this.mMutedTill.longValue() - System.currentTimeMillis()) / 2));
                    if (Math.abs(this.mMuteBannerWasShownForTime - this.mMutedTill.longValue()) > HlsChunkSource.DEFAULT_PLAYLIST_BLACKLIST_MS) {
                        this.mMuteBannerWasShownForTime = this.mMutedTill.longValue();
                        showInfoBanner(null, MainApplication.getRString(R.string.mute_banner_title, new Object[0]), MainApplication.getRString(R.string.mute_banner_description, new Object[0]));
                    }
                    if (getActivity() instanceof ChatActivity) {
                        UIUtils.hideVirtualKeyboard(getActivity());
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    protected void confirmIgnoring(ChatMessage message) {
        if (isAddedOrIsComponent() && message != null) {
            showInfoBanner(message.profileLogoSmall, MainApplication.getRString(R.string.ignore_banner_title, new Object[0]), message.username);
        }
    }

    protected void confirmModerator() {
        if (isAddedOrIsComponent()) {
            showInfoBanner(null, MainApplication.getRString(R.string.you_are_a_moderator, new Object[0]), MainApplication.getRString(R.string.use_the_force_wisely, new Object[0]));
        }
    }

    protected void showInfoBanner(String imageUrl, String message, String description) {
        int i = 0;
        if (isAddedOrIsComponent() && message != null) {
            try {
                toggleSystemUI(VisualOperation.Hide);
                UIUtils.hideVirtualKeyboard(getActivity());
                final View view = this.mNotificationLayout;
                if (view != null) {
                    view.setBackgroundResource(R.color.banner_blue_bg);
                    TextView text = (TextView) view.findViewById(R.id.description);
                    if (text != null) {
                        int i2;
                        text.setText(description);
                        if (description != null) {
                            i2 = 0;
                        } else {
                            i2 = 8;
                        }
                        text.setVisibility(i2);
                    }
                    text = (TextView) view.findViewById(R.id.title);
                    if (text != null) {
                        text.setText(message);
                    }
                    ImageView iv = (ImageView) view.findViewById(R.id.icon);
                    if (iv != null) {
                        if (imageUrl == null) {
                            i = 8;
                        }
                        iv.setVisibility(i);
                        if (imageUrl != null) {
                            ImageLoader.getInstance().displayImage(imageUrl, iv, new Builder().displayer(new RoundedBitmapDisplayer(getResources().getDimensionPixelSize(R.dimen.avatar_corner))).showImageForEmptyUri((int) R.drawable.default_profile_pic).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisk(true).build());
                        }
                    }
                    UIUtils.slideInFromTop(view);
                    if (this.mNotificationHandler != null) {
                        this.mNotificationHandler.removeCallbacks(null);
                    }
                    this.mNotificationHandler = new Handler();
                    this.mNotificationHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (ChatFragment.this.isAddedOrIsComponent()) {
                                UIUtils.slideOutToTop(view);
                            }
                        }
                    }, Constants.NOTIFICATION_BANNER_TIMEOUT);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    public String getChatChannelId() {
        if (this.mChatRoom != null) {
            return this.mChatRoom._id;
        }
        Log.e(TAG, "ChatRoom is empty for broadcast " + getBroadcastId());
        Crashlytics.logException(new Exception("ChatRoom is empty for broadcast " + getBroadcastId()));
        return MainApplication.getRString(R.string.app_name, new Object[0]);
    }

    protected String getBroadcastId() {
        return this.mBroadcastId;
    }

    protected boolean isLiveVideo() {
        return this.mIsLiveBroadcast;
    }

    protected boolean wasLiveVideo() {
        return this.mBroadcastWasLive;
    }

    protected boolean isCurrentUserOwner() {
        return this.mIsCurrentUserOwner;
    }

    protected boolean isShowJoinedRequired() {
        return this.mShowJoined;
    }

    protected void banUser(int position) {
        ModerationHelper.banUser(getActivity(), this.mFirebase, this.mMessagesAdapter.getItem(position).getUser(), getRoleOfCurrentUser().toString(), getChatChannelId());
    }

    protected void appointAsModerator(int position) {
        ModerationHelper.appointAsModerator(getActivity(), MainApplication.mFirebase, this.mMessagesAdapter.getItem(position).getUser(), getChatChannelId());
    }

    protected void performMuteDialog(final int position) {
        if (isAddedOrIsComponent()) {
            toggleSystemUI(VisualOperation.Hide);
            this.mMuteDialogIsShown = true;
            UIUtils.hideVirtualKeyboard(getActivity());
            new MaterialDialog.Builder(getActivity()).backgroundColorRes(17170443).title((int) R.string.mute_user).titleColorRes(17170444).content(Html.fromHtml(getString(R.string.MuteAllMessagesFrom_S_, this.mMessagesAdapter.getItem(position).username))).contentColorRes(17170444).positiveText((int) R.string.MuteFor10Minutes).positiveColorRes(R.color.blue).negativeText((int) R.string.MuteFor1Day).negativeColorRes(R.color.blue).neutralText((int) R.string.MuteIndefinitely).neutralColorRes(R.color.blue).callback(new ButtonCallback() {
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    ChatFragment.this.muteUser(position, 600000.0d);
                }

                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                    ChatFragment.this.muteUser(position, 8.64E7d);
                }

                public void onNeutral(MaterialDialog dialog) {
                    super.onNeutral(dialog);
                    ChatFragment.this.muteUser(position, 6.3072E10d);
                }
            }).dismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    ChatFragment.this.mMuteDialogIsShown = ChatFragment.CHAT_DEBUG;
                }
            }).cancelable(true).show();
        }
    }

    protected void updateModeratorStatus() {
        if (isAddedOrIsComponent()) {
            if (this.mMessagesAdapter != null) {
                if (this.mUserIsModerator) {
                    this.mMessagesAdapter.clearDisabledActions();
                } else {
                    this.mMessagesAdapter.addDisabledActions(R.id.action_mute);
                }
            }
            if (!this.mCurrentUserIsMuted) {
                TextView tv = this.mChatMessageText != null ? this.mChatMessageText : this.mEditText;
                if (tv != null) {
                    int i;
                    if (this.mUserIsModerator) {
                        i = R.string.you_are_a_moderator;
                    } else {
                        i = R.string.join_the_conversation;
                    }
                    tv.setHint(i);
                }
            }
        }
    }

    protected void performIgnoreDialog(final int position) {
        if (isAddedOrIsComponent()) {
            this.mIgnoreDialogIsShown = true;
            UIUtils.hideVirtualKeyboard(getActivity());
            new MaterialDialog.Builder(getActivity()).backgroundColorRes(17170443).title((int) R.string.ignore_user).titleColorRes(17170444).content(Html.fromHtml(getString(R.string.IgnoreUser_S__, this.mMessagesAdapter.getItem(position).username))).contentColorRes(17170444).positiveText((int) R.string.action_ignore).positiveColorRes(R.color.blue).negativeText(17039360).negativeColorRes(R.color.blue).callback(new ButtonCallback() {
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    ChatFragment.this.ignoreUser(position);
                }
            }).dismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    ChatFragment.this.mIgnoreDialogIsShown = ChatFragment.CHAT_DEBUG;
                }
            }).cancelable(true).show();
        }
    }

    protected void muteUser(int position, double time) {
        final ChatMessage message = this.mMessagesAdapter.getItem(position);
        if (!TextUtils.isEmpty(message.userId)) {
            final double d = time;
            final int i = position;
            this.mFirebase.child(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
                public void onDataChange(final DataSnapshot snapshot) {
                    if (ChatFragment.this.isAddedOrIsComponent()) {
                        ChatFragment.this.mHandler.post(new Runnable() {
                            public void run() {
                                try {
                                    double estimatedServerTimeMs = (double) (((Long) snapshot.getValue(Long.class)).longValue() + System.currentTimeMillis());
                                    Object map = new HashMap();
                                    map.put(Constants.CHAT_EXPIRE_TIMESTAMP, Double.valueOf(d + estimatedServerTimeMs));
                                    map.put("muter", PreferenceUtility.getUser()._id);
                                    map.put(ChatMessage.USERNAME, message.username);
                                    map.put("userProfileSmall", message.profileLogoSmall);
                                    map.put(ChatMessage.CLIENT, Network.getUserAgent());
                                    ChatFragment.this.mFirebase.child(Constants.CHAT_ROOM_MUTED_USERS).child(ChatFragment.this.getChatChannelId()).child(message.userId).setValue(map, new CompletionListener() {
                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                            ChatFragment.this.mFirebase.child(Constants.CHAT_ROOM_MESSAGES).child(ChatFragment.this.getChatChannelId()).child(message._id).child("triggeredMute").setValue(Boolean.valueOf(true), new CompletionListener() {
                                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                                    if (ChatFragment.this.isAddedOrIsComponent()) {
                                                        ChatFragment.this.mHandler.post(new Runnable() {
                                                            public void run() {
                                                                message.triggeredMute = true;
                                                                ChatFragment.this.mMessagesAdapter.notifyItemChanged(i);
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } catch (Throwable e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }

                public void onCancelled(FirebaseError error) {
                    System.err.println("Listener was cancelled");
                }
            });
        }
    }

    protected void ignoreUser(int position) {
        final ChatMessage message = this.mMessagesAdapter.getItem(position);
        if (!TextUtils.isEmpty(message.userId)) {
            Object map = new HashMap();
            try {
                map.put(ChatMessage.USER_ID, message.userId);
                map.put(ChatMessage.USERNAME, message.username);
                map.put("userProfileSmall", message.profileLogoSmall);
                map.put(ChatMessage.CLIENT, Network.getUserAgent());
                map.put("triggeredIgnoreMessage", message._id);
                map.put("triggeredIgnoreRoom", getChatChannelId());
                this.mIgnoredUsers.child(message.userId).setValue(map, new CompletionListener() {
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (ChatFragment.this.isAddedOrIsComponent()) {
                            ChatFragment.this.mHandler.post(new Runnable() {
                                public void run() {
                                    ChatFragment.this.confirmIgnoring(message);
                                }
                            });
                        }
                    }
                });
            } catch (Throwable e) {
                Throwable e2 = new Exception("can't ignore user: " + map, e);
                e2.printStackTrace();
                Crashlytics.logException(e2);
            }
        }
    }

    public void setOnNewMessageCallback(Callback callback) {
        this.mOnNewMessageCallback = callback;
    }

    protected void toggleSystemUI(VisualOperation operation) {
    }
}
