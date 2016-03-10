package com.mobcrush.mobcrush;

import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.crashlytics.android.Crashlytics;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.logic.ModerationLogicType;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.misc.SimpleChildEventListener;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.Map;

public class ChannelModerationMutedUsersFragment extends Fragment {
    private static final boolean DEBUG = true;
    private static final String TAG = "ChMViewersFragment";
    private UsersAdapter mAdapter;
    private String mBroadcasterID;
    private String mChannelID;
    private boolean mClearDataOnReceiveNew;
    private boolean mFragmentShowing;
    private LinearLayoutManager mLayoutManager;
    private Firebase mMutedUsers;
    private final SimpleChildEventListener mMutedUsersListener = new SimpleChildEventListener() {
        private User getUser(DataSnapshot dataSnapshot) {
            try {
                Map map = (Map) dataSnapshot.getValue(Map.class);
                Long timestamp = ModerationHelper.getExpireTimestamp(map);
                if (timestamp != null && timestamp.longValue() <= System.currentTimeMillis()) {
                    return null;
                }
                User user = new ChatMessage(null, 0, map).getUser();
                if (ChannelModerationMutedUsersFragment.this.mShowBanned != user.banned) {
                    return null;
                }
                if (user._id == null) {
                    user._id = dataSnapshot.getKey();
                }
                if (user.role != null) {
                    return user;
                }
                if (TextUtils.equals(ChannelModerationMutedUsersFragment.this.mBroadcasterID, user._id)) {
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
            if (ChannelModerationMutedUsersFragment.this.isAdded()) {
                ChannelModerationMutedUsersFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user != null && user.username != null) {
                                ChannelModerationMutedUsersFragment.this.mAdapter.add(user);
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
            if (ChannelModerationMutedUsersFragment.this.isAdded()) {
                ChannelModerationMutedUsersFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user != null && user.username != null) {
                                ChannelModerationMutedUsersFragment.this.mAdapter.change(user);
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
            if (ChannelModerationMutedUsersFragment.this.isAdded()) {
                ChannelModerationMutedUsersFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user != null && user.username != null) {
                                ChannelModerationMutedUsersFragment.this.mAdapter.remove(user);
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
    private int mPosition;
    private RecyclerView mRecyclerView;
    private boolean mShowBanned;

    public static ChannelModerationMutedUsersFragment newInstance(Boolean showBanned, String channelID, String broadcasterID, int position) {
        ChannelModerationMutedUsersFragment fragment = new ChannelModerationMutedUsersFragment();
        Bundle args = new Bundle();
        args.putBoolean(Constants.EXTRA_SHOW_BANNED, showBanned.booleanValue());
        args.putString(Constants.EXTRA_ID, channelID);
        args.putString(Constants.EXTRA_USER, broadcasterID);
        args.putInt(Constants.EXTRA_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mShowBanned = getArguments().getBoolean(Constants.EXTRA_SHOW_BANNED, false);
        this.mChannelID = getArguments().getString(Constants.EXTRA_ID, null);
        this.mBroadcasterID = getArguments().getString(Constants.EXTRA_USER, null);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ModerationLogicType moderationLogicType;
        View view = inflater.inflate(R.layout.fragment_viewers, container, false);
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mLayoutManager = new LinearLayoutManager(getActivity(), 1, false);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mAdapter = new UsersAdapter(getActivity(), R.layout.item_user_on_off);
        this.mAdapter.setDivider(R.drawable.user_list_divider_white_20_opaq);
        UsersAdapter usersAdapter = this.mAdapter;
        if (this.mShowBanned) {
            moderationLogicType = ModerationLogicType.Banned;
        } else {
            moderationLogicType = ModerationLogicType.Muted;
        }
        usersAdapter.enableModerationMode(moderationLogicType, false, new Callback() {
            public boolean handleMessage(Message message) {
                if (message != null) {
                    switch (message.what) {
                        case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                            ChannelModerationMutedUsersFragment.this.unmuteUser(message.arg1);
                            return ChannelModerationMutedUsersFragment.DEBUG;
                        case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                            ChannelModerationMutedUsersFragment.this.unbanUser(message.arg1);
                            return ChannelModerationMutedUsersFragment.DEBUG;
                    }
                }
                return false;
            }
        });
        this.mRecyclerView.setAdapter(this.mAdapter);
        return view;
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (MainApplication.mFirebase == null || getChatChannelId() == null) {
            Log.e(TAG, "getChatChannelId is null");
            return;
        }
        this.mMutedUsers = MainApplication.mFirebase.child(Constants.CHAT_ROOM_MUTED_USERS).child(getChatChannelId());
        this.mMutedUsers.addChildEventListener(this.mMutedUsersListener);
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
        if (this.mMutedUsers != null && this.mMutedUsersListener != null) {
            this.mMutedUsers.removeEventListener(this.mMutedUsersListener);
        }
    }

    protected void unbanUser(int position) {
        User user = this.mAdapter.getItem(position);
        if (!TextUtils.isEmpty(user._id)) {
            try {
                MainApplication.mFirebase.child(Constants.CHAT_ROOM_MUTED_USERS).child(getChatChannelId()).child(user._id).removeValue();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    protected void unmuteUser(int position) {
        User user = this.mAdapter.getItem(position);
        if (!TextUtils.isEmpty(user._id)) {
            try {
                MainApplication.mFirebase.child(Constants.CHAT_ROOM_MUTED_USERS).child(getChatChannelId()).child(user._id).removeValue();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }
}
