package com.mobcrush.mobcrush.helper;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import com.android.volley.Response.Listener;
import com.crashlytics.android.Crashlytics;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Firebase.CompletionListener;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.network.Network;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.HashMap;
import java.util.Map;

public class ModerationHelper {

    public interface MutedCallback {
        void userMuted(ChatMessage chatMessage);
    }

    static /* synthetic */ class AnonymousClass7 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$RoleType = new int[RoleType.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.user.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.moderator.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.broadcaster.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$RoleType[RoleType.admin.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    public static boolean isAdmin(String id) {
        return Constants.ADMINS_LIST.contains(id);
    }

    public static boolean isCurrentUserBroadcaster(String broadcasterID) {
        return isBroadcaster(PreferenceUtility.getUser()._id, broadcasterID);
    }

    public static boolean isBroadcaster(String userID, String broadcasterID) {
        return TextUtils.equals(userID, broadcasterID);
    }

    public static Long getExpireTimestamp(Map map) {
        if (map == null || !map.containsKey(Constants.CHAT_EXPIRE_TIMESTAMP)) {
            return null;
        }
        try {
            return Long.valueOf(Utils.convertTimeForCurrentTimeZone(((Double) map.get(Constants.CHAT_EXPIRE_TIMESTAMP)).doubleValue()));
        } catch (ClassCastException e) {
            return Long.valueOf(Utils.convertTimeForCurrentTimeZone((double) ((Long) map.get(Constants.CHAT_EXPIRE_TIMESTAMP)).longValue()));
        }
    }

    public static void appointAsModerator(final FragmentActivity activity, Firebase firebase, final User user, final String channelId) {
        if (activity != null && firebase != null && user != null && !TextUtils.isEmpty(user._id)) {
            firebase.child(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            try {
                                Map<String, Object> map = new HashMap();
                                map.put(ChatMessage.USER_ID, user._id);
                                map.put(ChatMessage.USERNAME, user.username);
                                map.put("userProfileSmall", user.profileLogoSmall);
                                map.put(ChatMessage.CLIENT, Network.getUserAgent());
                                MainApplication.mFirebase.child(Constants.CHAT_ROOM_MODERATORS).child(channelId).child(user._id).setValue(map);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                public void onCancelled(FirebaseError error) {
                    new Exception(error.toException()).printStackTrace();
                }
            });
        }
    }

    public static void removeModerator(User user, String channelId) {
        if (user != null && !TextUtils.isEmpty(user._id) && channelId != null) {
            MainApplication.mFirebase.child(Constants.CHAT_ROOM_MODERATORS).child(channelId).child(user._id).removeValue();
        }
    }

    public static void appointAsModerator(Firebase firebase, final User user, final String channelId) {
        if (firebase != null && user != null && !TextUtils.isEmpty(user._id)) {
            firebase.child(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        Map<String, Object> map = new HashMap();
                        map.put(ChatMessage.USER_ID, user._id);
                        map.put(ChatMessage.USERNAME, user.username);
                        map.put("userProfileSmall", user.profileLogoSmall);
                        map.put(ChatMessage.CLIENT, Network.getUserAgent());
                        MainApplication.mFirebase.child(Constants.CHAT_ROOM_MODERATORS).child(channelId).child(user._id).setValue(map);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                public void onCancelled(FirebaseError error) {
                    new Exception(error.toException()).printStackTrace();
                }
            });
        }
    }

    public static void banUser(FragmentActivity activity, final Firebase firebase, final User user, String role, final String channelId) {
        Network.banUser(activity, channelId, user._id, role, new Listener<Boolean>() {
            public void onResponse(Boolean response) {
                if (response != null && response.booleanValue()) {
                    try {
                        Firebase tmp = firebase.child(Constants.CHAT_ROOM_USERS).child(channelId).child(user._id);
                        if (tmp != null) {
                            tmp.removeValue();
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                    }
                }
            }
        }, null);
    }

    public static void muteUser(ChatMessage message, User user, double time, String channelId, MutedCallback callback) {
        if (!TextUtils.isEmpty(user._id)) {
            final double d = time;
            final User user2 = user;
            final String str = channelId;
            final MutedCallback mutedCallback = callback;
            final ChatMessage chatMessage = message;
            MainApplication.mFirebase.child(".info/serverTimeOffset").addValueEventListener(new ValueEventListener() {
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        double estimatedServerTimeMs = (double) (((Long) snapshot.getValue(Long.class)).longValue() + System.currentTimeMillis());
                        Object map = new HashMap();
                        map.put(Constants.CHAT_EXPIRE_TIMESTAMP, Double.valueOf(d + estimatedServerTimeMs));
                        map.put("muter", PreferenceUtility.getUser()._id);
                        map.put(ChatMessage.USERNAME, user2.username);
                        map.put("userProfileSmall", user2.profileLogoSmall);
                        map.put(ChatMessage.CLIENT, Network.getUserAgent());
                        MainApplication.mFirebase.child(Constants.CHAT_ROOM_MUTED_USERS).child(str).child(user2._id).setValue(map, new CompletionListener() {
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                MainApplication.mFirebase.child(Constants.CHAT_ROOM_MESSAGES).child(str).child(user2._id).child("triggeredMute").setValue(Boolean.valueOf(true), new CompletionListener() {
                                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                        mutedCallback.userMuted(chatMessage);
                                    }
                                });
                            }
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }

                public void onCancelled(FirebaseError error) {
                    System.err.println("Listener was cancelled");
                }
            });
        }
    }

    public static void addToRoom(FragmentActivity activity, Firebase firebase, User user, String channelId) {
        try {
            Object post = new HashMap();
            post.put(ChatMessage.USER_ID, user._id);
            post.put(ChatMessage.USERNAME, user.username);
            post.put(ChatMessage.PROFILE_LOGO_SMALL, user.profileLogoSmall);
            if (isAdmin(user._id)) {
                post.put(ChatMessage.ROLE, RoleType.admin);
            }
            post.put(ChatMessage.CLIENT, Network.getUserAgent());
            post.put(Constants.CHAT_MESSAGE_TIMESTAMP, ServerValue.TIMESTAMP.toString());
            firebase.child(Constants.CHAT_ROOM_USERS).child(channelId).child(user._id).setValue(post, new CompletionListener() {
                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public static void ignoreUser(User user, String channelId) {
        if (!TextUtils.isEmpty(user._id)) {
            try {
                Object post = new HashMap();
                post.put(ChatMessage.USER_ID, user._id);
                post.put(ChatMessage.USERNAME, user.username);
                post.put("userProfileSmall", user.profileLogoSmall);
                post.put(ChatMessage.CLIENT, Network.getUserAgent());
                post.put("triggeredIgnoreRoom", channelId);
                MainApplication.mFirebase.child(Constants.CHAT_ROOM_IGNORED_USERS).child(PreferenceUtility.getUser()._id).child(user._id).setValue(post, new CompletionListener() {
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    public static boolean canBanUser(RoleType currentUser, RoleType banUser) {
        if (currentUser == null || RoleType.user.equals(currentUser)) {
            return false;
        }
        if (banUser == null) {
            banUser = RoleType.user;
        }
        switch (AnonymousClass7.$SwitchMap$com$mobcrush$mobcrush$logic$RoleType[banUser.ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return true;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                if (RoleType.moderator.equals(currentUser)) {
                    return false;
                }
                return true;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return false;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                return false;
            default:
                return true;
        }
    }
}
