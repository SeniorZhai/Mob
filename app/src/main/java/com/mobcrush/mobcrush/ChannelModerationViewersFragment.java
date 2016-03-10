package com.mobcrush.mobcrush;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.crashlytics.android.Crashlytics;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Firebase.CompletionListener;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.logic.ModerationLogicType;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.misc.SimpleChildEventListener;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.player.Player;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.HashMap;
import java.util.Map;

public class ChannelModerationViewersFragment extends Fragment {
    private static final boolean DEBUG = true;
    private static final String TAG = "ChMViewersFragment";
    private UsersAdapter mAdapter;
    private String mBroadcasterID;
    private String mChannelID;
    private final SimpleChildEventListener mChatUsersListener = new SimpleChildEventListener() {
        private long mCurrentUserTimestamp = -1;

        private User getUser(DataSnapshot dataSnapshot) {
            try {
                User user = new ChatMessage(null, 0, (Map) dataSnapshot.getValue(Map.class)).getUser();
                if (user.role != null) {
                    return user;
                }
                if (TextUtils.equals(ChannelModerationViewersFragment.this.mBroadcasterID, user._id)) {
                    user.role = RoleType.broadcaster;
                    return user;
                } else if (ModerationHelper.isAdmin(user._id)) {
                    user.role = RoleType.admin;
                    return user;
                } else {
                    user.role = RoleType.user;
                    return user;
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
                return null;
            }
        }

        public void onChildAdded(final DataSnapshot dataSnapshot, String s) {
            if (ChannelModerationViewersFragment.this.isAdded()) {
                ChannelModerationViewersFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user.username != null) {
                                ChannelModerationViewersFragment.this.mAdapter.add(user);
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
            if (ChannelModerationViewersFragment.this.isAdded()) {
                ChannelModerationViewersFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user.username != null) {
                                ChannelModerationViewersFragment.this.mAdapter.change(user);
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            Crashlytics.logException(e);
                        }
                    }
                });
            }
        }

        public void onChildRemoved(final DataSnapshot dataSnapshot) {
            if (ChannelModerationViewersFragment.this.isAdded()) {
                ChannelModerationViewersFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user.username != null) {
                                ChannelModerationViewersFragment.this.mAdapter.remove(user);
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
    private boolean mClearDataOnReceiveNew;
    private boolean mFragmentShowing;
    private boolean mIgnoreDialogIsShown;
    private LinearLayoutManager mLayoutManager;
    private boolean mMuteDialogIsShown;
    private int mPosition;
    private RecyclerView mRecyclerView;
    private Firebase mRoomUsers;

    public static ChannelModerationViewersFragment newInstance(String channelID, String broadcasterID, boolean isModerator, int position) {
        ChannelModerationViewersFragment fragment = new ChannelModerationViewersFragment();
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ID, channelID);
        args.putString(Constants.EXTRA_USER, broadcasterID);
        args.putInt(Constants.EXTRA_POSITION, position);
        args.putBoolean(Constants.EXTRA_MODERATOR, isModerator);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mChannelID = getArguments().getString(Constants.EXTRA_ID, null);
        this.mBroadcasterID = getArguments().getString(Constants.EXTRA_USER, null);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_viewers, container, false);
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mAdapter = new UsersAdapter(getActivity(), R.layout.item_user_on_off);
        this.mAdapter.setDivider(R.drawable.user_list_divider_white_20_opaq);
        this.mAdapter.enableModerationMode(ModerationLogicType.Viewers, ModerationHelper.isCurrentUserBroadcaster(this.mBroadcasterID), new Callback() {
            public boolean handleMessage(Message message) {
                if (message != null) {
                    switch (message.what) {
                        case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                            ChannelModerationViewersFragment.this.performIgnoreDialog(message.arg1);
                            return ChannelModerationViewersFragment.DEBUG;
                        case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            ChannelModerationViewersFragment.this.performMuteDialog(message.arg1);
                            return ChannelModerationViewersFragment.DEBUG;
                        case Player.STATE_ENDED /*5*/:
                            ChannelModerationViewersFragment.this.appointAsModerator(message.arg1);
                            return ChannelModerationViewersFragment.DEBUG;
                        case DayPickerView.DAYS_PER_WEEK /*7*/:
                            ChannelModerationViewersFragment.this.banUser(message.arg1);
                            return ChannelModerationViewersFragment.DEBUG;
                    }
                }
                return false;
            }
        });
        if (RoleType.user.equals(getRoleOfCurrentUser())) {
            this.mAdapter.addDisabledActions(R.id.action_mute);
            this.mAdapter.addDisabledActions(R.id.action_ban);
        }
        this.mRecyclerView.setAdapter(this.mAdapter);
        return view;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (MainApplication.mFirebase == null || getChatChannelId() == null) {
            Log.e(TAG, "getChatChannelId is null");
            return;
        }
        this.mRoomUsers = MainApplication.mFirebase.child(Constants.CHAT_ROOM_USERS).child(getChatChannelId());
        this.mRoomUsers.addChildEventListener(this.mChatUsersListener);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        this.mFragmentShowing = isVisibleToUser;
        super.setUserVisibleHint(isVisibleToUser);
    }

    public void onResume() {
        super.onResume();
    }

    public String getChatChannelId() {
        return this.mChannelID;
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mRoomUsers != null && this.mChatUsersListener != null) {
            this.mRoomUsers.removeEventListener(this.mChatUsersListener);
            this.mRoomUsers = null;
        }
    }

    private RoleType getRoleOfCurrentUser() {
        User user = PreferenceUtility.getUser();
        if (ModerationHelper.isBroadcaster(user._id, this.mBroadcasterID)) {
            return RoleType.broadcaster;
        }
        if (ModerationHelper.isAdmin(user._id)) {
            return RoleType.admin;
        }
        if (getArguments().getBoolean(Constants.EXTRA_MODERATOR, false)) {
            return RoleType.moderator;
        }
        return RoleType.user;
    }

    protected void performMuteDialog(final int position) {
        if (isAdded()) {
            this.mMuteDialogIsShown = DEBUG;
            UIUtils.hideVirtualKeyboard(getActivity());
            new Builder(getActivity()).backgroundColorRes(17170443).title((int) R.string.mute_user).titleColorRes(17170444).content(Html.fromHtml(getString(R.string.MuteAllMessagesFrom_S_, this.mAdapter.getItem(position).username))).contentColorRes(17170444).positiveText((int) R.string.MuteFor10Minutes).positiveColorRes(R.color.blue).negativeText((int) R.string.MuteFor1Day).negativeColorRes(R.color.blue).neutralText((int) R.string.MuteIndefinitely).neutralColorRes(R.color.blue).callback(new ButtonCallback() {
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    ChannelModerationViewersFragment.this.muteUser(position, 600000.0d);
                }

                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                    ChannelModerationViewersFragment.this.muteUser(position, 8.64E7d);
                }

                public void onNeutral(MaterialDialog dialog) {
                    super.onNeutral(dialog);
                    ChannelModerationViewersFragment.this.muteUser(position, 6.3072E10d);
                }
            }).dismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    ChannelModerationViewersFragment.this.mMuteDialogIsShown = false;
                }
            }).cancelable(DEBUG).show();
        }
    }

    protected void performIgnoreDialog(final int position) {
        if (isAdded()) {
            this.mIgnoreDialogIsShown = DEBUG;
            UIUtils.hideVirtualKeyboard(getActivity());
            new Builder(getActivity()).backgroundColorRes(17170443).title((int) R.string.ignore_user).titleColorRes(17170444).content(Html.fromHtml(getString(R.string.IgnoreUser_S__, this.mAdapter.getItem(position).username))).contentColorRes(17170444).positiveText((int) R.string.action_ignore).positiveColorRes(R.color.blue).negativeText(17039360).negativeColorRes(R.color.blue).callback(new ButtonCallback() {
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    ChannelModerationViewersFragment.this.ignoreUser(position);
                }
            }).dismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialogInterface) {
                    ChannelModerationViewersFragment.this.mIgnoreDialogIsShown = false;
                }
            }).cancelable(DEBUG).show();
        }
    }

    protected void banUser(int position) {
        ModerationHelper.banUser(getActivity(), MainApplication.mFirebase, this.mAdapter.getItem(position), getRoleOfCurrentUser().toString(), getChatChannelId());
    }

    protected void appointAsModerator(int position) {
        ModerationHelper.appointAsModerator(getActivity(), MainApplication.mFirebase, this.mAdapter.getItem(position), getChatChannelId());
    }

    protected void muteUser(int position, final double time) {
        final User user = this.mAdapter.getItem(position);
        if (!TextUtils.isEmpty(user._id)) {
            MainApplication.mFirebase.child(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
                public void onDataChange(final DataSnapshot snapshot) {
                    if (ChannelModerationViewersFragment.this.isAdded()) {
                        ChannelModerationViewersFragment.this.getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    double estimatedServerTimeMs = (double) (((Long) snapshot.getValue(Long.class)).longValue() + System.currentTimeMillis());
                                    Object map = new HashMap();
                                    map.put(Constants.CHAT_EXPIRE_TIMESTAMP, Double.valueOf(time + estimatedServerTimeMs));
                                    map.put("muter", PreferenceUtility.getUser()._id);
                                    map.put(ChatMessage.USERNAME, user.username);
                                    map.put("userProfileSmall", user.profileLogoSmall);
                                    map.put(ChatMessage.CLIENT, Network.getUserAgent());
                                    MainApplication.mFirebase.child(Constants.CHAT_ROOM_MUTED_USERS).child(ChannelModerationViewersFragment.this.getChatChannelId()).child(user._id).setValue(map, new CompletionListener() {
                                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                            MainApplication.mFirebase.child(Constants.CHAT_ROOM_MESSAGES).child(ChannelModerationViewersFragment.this.getChatChannelId()).child(user._id).child("triggeredMute").setValue(Boolean.valueOf(ChannelModerationViewersFragment.DEBUG), new CompletionListener() {
                                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                                    if (ChannelModerationViewersFragment.this.isAdded()) {
                                                        ChannelModerationViewersFragment.this.getActivity().runOnUiThread(new Runnable() {
                                                            public void run() {
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
        final User user = this.mAdapter.getItem(position);
        if (!TextUtils.isEmpty(user._id)) {
            Object map = new HashMap();
            try {
                map.put(ChatMessage.USER_ID, user._id);
                map.put(ChatMessage.USERNAME, user.username);
                map.put("userProfileSmall", user.profileLogoSmall);
                map.put(ChatMessage.CLIENT, Network.getUserAgent());
                map.put("triggeredIgnoreRoom", getChatChannelId());
                MainApplication.mFirebase.child(Constants.CHAT_ROOM_IGNORED_USERS).child(PreferenceUtility.getUser()._id).child(user._id).setValue(map, new CompletionListener() {
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (ChannelModerationViewersFragment.this.isAdded()) {
                            ChannelModerationViewersFragment.this.getActivity().runOnUiThread(new Runnable() {
                                public void run() {
                                    ChannelModerationViewersFragment.this.confirmIgnoring(user);
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

    protected void confirmIgnoring(User user) {
        View ignoreView = null;
        if (isAdded() && user != null) {
            try {
                UIUtils.hideVirtualKeyboard(getActivity());
                if (getView() != null) {
                    ignoreView = getActivity().findViewById(R.id.popup_notification_layout);
                }
                if (ignoreView != null) {
                    ignoreView.setBackgroundResource(R.color.banner_blue_bg);
                    TextView text = (TextView) ignoreView.findViewById(R.id.description);
                    if (text != null) {
                        text.setText(user.username);
                    }
                    text = (TextView) ignoreView.findViewById(R.id.title);
                    if (text != null) {
                        text.setText(R.string.ignore_banner_title);
                    }
                    ImageView iv = (ImageView) ignoreView.findViewById(R.id.icon);
                    if (iv != null) {
                        iv.setVisibility(0);
                        ImageLoader.getInstance().displayImage(user.profileLogoSmall, iv, new DisplayImageOptions.Builder().displayer(new RoundedBitmapDisplayer(getResources().getDimensionPixelSize(R.dimen.avatar_corner))).showImageForEmptyUri((int) R.drawable.default_profile_pic).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).cacheOnDisk(DEBUG).build());
                    }
                    UIUtils.slideInFromTop(ignoreView);
                    ignoreView.removeCallbacks(null);
                    ignoreView.postDelayed(new Runnable() {
                        public void run() {
                            if (ChannelModerationViewersFragment.this.isAdded()) {
                                UIUtils.slideOutToTop(ignoreView);
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
}
