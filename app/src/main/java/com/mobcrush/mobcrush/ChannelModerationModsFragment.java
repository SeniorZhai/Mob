package com.mobcrush.mobcrush;

import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.crashlytics.android.Crashlytics;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.logic.ModerationLogicType;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.misc.SimpleChildEventListener;
import com.mobcrush.mobcrush.network.Network;
import java.util.HashMap;
import java.util.Map;

public class ChannelModerationModsFragment extends Fragment {
    private static final boolean DEBUG = true;
    private static final int REQUEST_SELECT_USERS = 1;
    private static final String TAG = "ChMModsFragment";
    private UsersAdapter mAdapter;
    private String mBroadcasterID;
    private String mChannelID;
    private final SimpleChildEventListener mChatModeratorsListener = new SimpleChildEventListener() {
        private long mCurrentUserTimestamp = -1;

        private User getUser(DataSnapshot dataSnapshot) {
            try {
                User user = new ChatMessage(null, 0, (Map) dataSnapshot.getValue(Map.class)).getUser();
                if (user._id == null) {
                    user._id = dataSnapshot.getKey();
                }
                if (user.role != null) {
                    return user;
                }
                if (TextUtils.equals(ChannelModerationModsFragment.this.mBroadcasterID, user._id)) {
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
            if (ChannelModerationModsFragment.this.isAdded()) {
                ChannelModerationModsFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user.username != null) {
                                ChannelModerationModsFragment.this.mAdapter.add(user);
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
            if (ChannelModerationModsFragment.this.isAdded()) {
                ChannelModerationModsFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user.username != null) {
                                ChannelModerationModsFragment.this.mAdapter.change(user);
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
            if (ChannelModerationModsFragment.this.isAdded()) {
                ChannelModerationModsFragment.this.getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            User user = AnonymousClass1.this.getUser(dataSnapshot);
                            if (user.username != null) {
                                ChannelModerationModsFragment.this.mAdapter.remove(user);
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
    private LinearLayoutManager mLayoutManager;
    private int mPosition;
    private RecyclerView mRecyclerView;
    private Firebase mRoomModerators;

    public static ChannelModerationModsFragment newInstance(String channelID, String broadcasterID, int position) {
        ChannelModerationModsFragment fragment = new ChannelModerationModsFragment();
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
        View addMod = view.findViewById(R.id.add_mod);
        addMod.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ChannelModerationModsFragment.this.startActivityForResult(SelectUsersActivity.getIntent(ChannelModerationModsFragment.this.getActivity()), ChannelModerationModsFragment.REQUEST_SELECT_USERS);
            }
        });
        addMod.setVisibility(0);
        this.mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        this.mLayoutManager = new LinearLayoutManager(getActivity(), REQUEST_SELECT_USERS, false);
        this.mRecyclerView.setLayoutManager(this.mLayoutManager);
        this.mAdapter = new UsersAdapter(getActivity(), R.layout.item_user_on_off);
        this.mAdapter.setDivider(R.drawable.user_list_divider_white_20_opaq);
        this.mAdapter.enableModerationMode(ModerationLogicType.Mods, false, new Callback() {
            public boolean handleMessage(Message message) {
                if (message != null) {
                    switch (message.what) {
                        case R.styleable.Toolbar_contentInsetEnd /*6*/:
                            ChannelModerationModsFragment.this.disappointAsModerator(message.arg1);
                            return ChannelModerationModsFragment.DEBUG;
                    }
                }
                return false;
            }
        });
        this.mRecyclerView.setAdapter(this.mAdapter);
        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_USERS && resultCode == -1 && data != null) {
            try {
                User[] arr$ = (User[]) new Gson().fromJson(data.getStringExtra("android.intent.extra.TEXT"), User[].class);
                int len$ = arr$.length;
                for (int i$ = 0; i$ < len$; i$ += REQUEST_SELECT_USERS) {
                    appointAsModerator(arr$[i$]);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (MainApplication.mFirebase == null || getChatChannelId() == null) {
            Log.e(TAG, "getChatChannelId is null");
            return;
        }
        this.mRoomModerators = MainApplication.mFirebase.child(Constants.CHAT_ROOM_MODERATORS).child(getChatChannelId());
        this.mRoomModerators.addChildEventListener(this.mChatModeratorsListener);
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
        if (this.mRoomModerators != null && this.mChatModeratorsListener != null) {
            this.mRoomModerators.removeEventListener(this.mChatModeratorsListener);
        }
    }

    protected void disappointAsModerator(int position) {
        User user = this.mAdapter.getItem(position);
        if (!TextUtils.isEmpty(user._id)) {
            try {
                MainApplication.mFirebase.child(Constants.CHAT_ROOM_MODERATORS).child(getChatChannelId()).child(user._id).removeValue();
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    protected void appointAsModerator(final User user) {
        if (!TextUtils.isEmpty(user._id)) {
            MainApplication.mFirebase.child(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
                public void onDataChange(final DataSnapshot snapshot) {
                    if (ChannelModerationModsFragment.this.isAdded()) {
                        ChannelModerationModsFragment.this.getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                try {
                                    double estimatedServerTimeMs = (double) (((Long) snapshot.getValue(Long.class)).longValue() + System.currentTimeMillis());
                                    Map<String, Object> map = new HashMap();
                                    map.put(Constants.CHAT_EXPIRE_TIMESTAMP, Double.valueOf(estimatedServerTimeMs));
                                    map.put(ChatMessage.USERNAME, user.username);
                                    map.put("userProfileSmall", user.profileLogoSmall);
                                    map.put(ChatMessage.CLIENT, Network.getUserAgent());
                                    MainApplication.mFirebase.child(Constants.CHAT_ROOM_MODERATORS).child(ChannelModerationModsFragment.this.getChatChannelId()).child(user._id).setValue(map);
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
}
