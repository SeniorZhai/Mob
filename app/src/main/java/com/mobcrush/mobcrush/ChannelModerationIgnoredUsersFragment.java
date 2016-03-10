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
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.logic.ModerationLogicType;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.misc.SimpleChildEventListener;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import java.util.Map;

public class ChannelModerationIgnoredUsersFragment extends Fragment {
    private static final boolean DEBUG = true;
    private static final String TAG = "ChMIgnoredUsersFragment";
    private UsersAdapter mAdapter;
    private String mBroadcasterID;
    private String mChannelID;
    private boolean mClearDataOnReceiveNew;
    private boolean mFragmentShowing;
    private boolean mIgnoreDialogIsShown;
    private Firebase mIgnoredUsers;
    private final SimpleChildEventListener mIgnoredUsersListener = new SimpleChildEventListener() {
        private long mCurrentUserTimestamp = -1;

        private User getUser(DataSnapshot dataSnapshot) {
            try {
                User user = new ChatMessage(null, 0, (Map) dataSnapshot.getValue(Map.class)).getUser();
                if (user.role != null) {
                    return user;
                }
                if (TextUtils.equals(ChannelModerationIgnoredUsersFragment.this.mBroadcasterID, user._id)) {
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
            if (ChannelModerationIgnoredUsersFragment.this.isAdded()) {
                ChannelModerationIgnoredUsersFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user.username != null) {
                                ChannelModerationIgnoredUsersFragment.this.mAdapter.add(user);
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
            if (ChannelModerationIgnoredUsersFragment.this.isAdded()) {
                ChannelModerationIgnoredUsersFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user.username != null) {
                                ChannelModerationIgnoredUsersFragment.this.mAdapter.change(user);
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
            if (ChannelModerationIgnoredUsersFragment.this.isAdded()) {
                ChannelModerationIgnoredUsersFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user.username != null) {
                                ChannelModerationIgnoredUsersFragment.this.mAdapter.remove(user);
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
    private LinearLayoutManager mLayoutManager;
    private boolean mMuteDialogIsShown;
    private int mPosition;
    private RecyclerView mRecyclerView;

    public static ChannelModerationIgnoredUsersFragment newInstance(String channelID, String broadcasterID, int position) {
        ChannelModerationIgnoredUsersFragment fragment = new ChannelModerationIgnoredUsersFragment();
        Bundle args = new Bundle();
        args.putString(Constants.EXTRA_ID, channelID);
        args.putString(Constants.EXTRA_USER, broadcasterID);
        args.putInt(Constants.EXTRA_POSITION, position);
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
        this.mAdapter.enableModerationMode(ModerationLogicType.Ignored, false, new Callback() {
            public boolean handleMessage(Message message) {
                if (message != null) {
                    switch (message.what) {
                        case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                            ChannelModerationIgnoredUsersFragment.this.unignoreUser(message.arg1);
                            return ChannelModerationIgnoredUsersFragment.DEBUG;
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
        } else if (MainApplication.mFirebase != null && PreferenceUtility.isEmailVerified()) {
            this.mIgnoredUsers = MainApplication.mFirebase.child(Constants.CHAT_ROOM_IGNORED_USERS).child(PreferenceUtility.getUser()._id);
            this.mIgnoredUsers.addChildEventListener(this.mIgnoredUsersListener);
        }
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
        if (this.mIgnoredUsers != null && this.mIgnoredUsersListener != null) {
            this.mIgnoredUsers.removeEventListener(this.mIgnoredUsersListener);
        }
    }

    protected void unignoreUser(int position) {
        User user = this.mAdapter.getItem(position);
        if (!TextUtils.isEmpty(user._id)) {
            try {
                MainApplication.mFirebase.child(Constants.CHAT_ROOM_IGNORED_USERS).child(PreferenceUtility.getUser()._id).child(user._id).removeValue();
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }
}
