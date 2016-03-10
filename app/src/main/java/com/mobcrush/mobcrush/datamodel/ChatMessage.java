package com.mobcrush.mobcrush.datamodel;

import android.support.annotation.NonNull;
import com.crashlytics.android.Crashlytics;
import com.firebase.client.DataSnapshot;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.logic.RoleType;
import com.mobcrush.mobcrush.network.Network;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ChatMessage extends DataModel {
    public static final String BANNED = "banned";
    public static final String CLIENT = "client";
    private static final String FROM_VOD = "fromVod";
    private static final String HIDDEN = "hidden";
    public static final String MESSAGE = "message";
    private static final String MODERATOR = "moderator";
    private static final String MUTE_ORIGIN = "muteOrigin";
    private static final String OWNER = "owner";
    public static final String PROFILE_LOGO_SMALL = "profileLogoSmall";
    public static final String ROLE = "role";
    public static final String SUBTITLE = "subtitle";
    private static final String SYSTEM_MSG = "systemMsg";
    private static final String TRIGGERED_MUTE = "triggeredMute";
    public static final String USERNAME = "username";
    public static final String USER_ID = "userId";
    public String _id;
    public boolean banned;
    public String broadcastId;
    public String client;
    public Object flags;
    public boolean fromVod;
    public boolean hidden;
    public String message;
    public boolean moderator;
    public boolean muteOrigin;
    public boolean owner;
    public String profileLogoSmall;
    public RoleType role;
    public String subtitle;
    public boolean systemMsg;
    public long timestamp;
    public boolean triggeredMute;
    public String ua;
    public String userId;
    public String username;

    public ChatMessage(String message, long timestamp, @NonNull Map map) {
        this.message = message;
        this.timestamp = timestamp;
        if (map.containsKey(USERNAME)) {
            this.username = (String) map.get(USERNAME);
        }
        if (map.containsKey(USER_ID)) {
            this.userId = (String) map.get(USER_ID);
        }
        if (map.containsKey(PROFILE_LOGO_SMALL)) {
            this.profileLogoSmall = (String) map.get(PROFILE_LOGO_SMALL);
        }
        if (map.containsKey(ROLE)) {
            this.role = RoleType.valueOf((String) map.get(ROLE));
        }
        if (map.containsKey(BANNED)) {
            this.banned = ((Boolean) map.get(BANNED)).booleanValue();
        }
        if (map.containsKey(SUBTITLE)) {
            this.subtitle = (String) map.get(SUBTITLE);
        }
    }

    public static ChatMessage from(DataSnapshot snapshot) {
        ChatMessage message = new ChatMessage();
        if (snapshot != null) {
            try {
                message._id = snapshot.getKey();
                Map map = (Map) snapshot.getValue(Map.class);
                Object tmp = map.get(MESSAGE);
                if (tmp != null) {
                    message.message = (String) tmp;
                }
                tmp = map.get(USERNAME);
                if (tmp != null) {
                    message.username = (String) tmp;
                }
                tmp = map.get(USER_ID);
                if (tmp != null) {
                    message.userId = (String) tmp;
                }
                tmp = map.get(PROFILE_LOGO_SMALL);
                if (tmp != null) {
                    message.profileLogoSmall = (String) tmp;
                }
                tmp = map.get(Constants.CHAT_MESSAGE_TIMESTAMP);
                if (tmp != null) {
                    message.timestamp = ((Long) tmp).longValue();
                }
                tmp = map.get("broadcastId");
                if (tmp != null) {
                    message.broadcastId = (String) tmp;
                }
                tmp = map.get(SYSTEM_MSG);
                if (tmp != null) {
                    message.systemMsg = ((Boolean) tmp).booleanValue();
                }
                tmp = map.get(OWNER);
                if (tmp != null) {
                    message.owner = ((Boolean) tmp).booleanValue();
                }
                tmp = map.get(MODERATOR);
                if (tmp != null) {
                    message.moderator = ((Boolean) tmp).booleanValue();
                }
                tmp = map.get(FROM_VOD);
                if (tmp != null) {
                    message.fromVod = ((Boolean) tmp).booleanValue();
                }
                tmp = map.get(HIDDEN);
                if (tmp != null) {
                    message.hidden = ((Boolean) tmp).booleanValue();
                }
                tmp = map.get(MUTE_ORIGIN);
                if (tmp != null) {
                    message.muteOrigin = ((Boolean) tmp).booleanValue();
                }
                tmp = map.get(TRIGGERED_MUTE);
                if (tmp != null) {
                    message.triggeredMute = ((Boolean) tmp).booleanValue();
                }
                tmp = map.get(ROLE);
                if (tmp != null) {
                    message.role = RoleType.valueOf((String) tmp);
                }
                tmp = map.get(BANNED);
                if (tmp != null) {
                    message.banned = ((Boolean) tmp).booleanValue();
                }
                tmp = map.get(CLIENT);
                if (tmp != null) {
                    message.client = (String) tmp;
                }
                tmp = map.get(SUBTITLE);
                if (tmp != null) {
                    message.subtitle = (String) tmp;
                }
            } catch (Throwable e) {
                Throwable e2 = new Exception("Can't parse snapshot " + snapshot.toString(), e);
                e2.printStackTrace();
                Crashlytics.logException(e2);
            }
        }
        return message;
    }

    public User getUser() {
        User user = new User();
        user._id = this.userId;
        user.username = this.username;
        user.profileLogoSmall = this.profileLogoSmall;
        user.role = this.role;
        user.banned = this.banned;
        user.subtitle = this.subtitle;
        return user;
    }

    public String getUserId() {
        return this.userId;
    }

    public ChatMessage setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public ChatMessage setUserInfo(User u) {
        if (u != null) {
            this.userId = u._id;
            this.username = u.username;
            this.subtitle = u.subtitle;
            this.profileLogoSmall = u.profileLogoSmall != null ? u.profileLogoSmall : u.profileLogo;
        } else {
            this.userId = null;
            this.username = null;
            this.subtitle = null;
            this.profileLogoSmall = null;
        }
        return this;
    }

    public String getUsername() {
        return this.username;
    }

    public ChatMessage setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getProfileLogoSmall() {
        return this.profileLogoSmall;
    }

    public ChatMessage setProfileLogoSmall(String profileLogoSmall) {
        this.profileLogoSmall = profileLogoSmall;
        return this;
    }

    public String getMessage() {
        return this.message;
    }

    public ChatMessage setMessage(String message) {
        this.message = message;
        return this;
    }

    public ChatMessage setOwner() {
        this.owner = true;
        return this;
    }

    public ChatMessage setSystemMsg() {
        this.systemMsg = true;
        return this;
    }

    public ChatMessage setFromVOD() {
        this.fromVod = true;
        return this;
    }

    public ChatMessage setSubtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public boolean isSystemMsg() {
        return this.systemMsg;
    }

    public ChatMessage setModerator() {
        this.moderator = true;
        return this;
    }

    public String getBroadcastId() {
        return this.broadcastId;
    }

    public ChatMessage setBroadcastId(String broadcastId) {
        this.broadcastId = broadcastId;
        return this;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public boolean isFromVod() {
        return this.fromVod;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isMuteOrigin() {
        return this.muteOrigin;
    }

    public boolean isTriggeredMute() {
        return this.triggeredMute;
    }

    public Map<String, Object> getMapWithMetadata(Map<String, Object> metadata) {
        Map<String, Object> msg = new HashMap();
        msg.put(MESSAGE, this.message);
        msg.put(USERNAME, this.username);
        msg.put(SUBTITLE, this.subtitle);
        msg.put(USER_ID, this.userId);
        msg.put(PROFILE_LOGO_SMALL, this.profileLogoSmall);
        msg.put(Constants.CHAT_MESSAGE_TIMESTAMP, Long.valueOf(this.timestamp));
        msg.put(CLIENT, Network.getUserAgent());
        if (this.role != null) {
            msg.put(ROLE, this.role);
        }
        if (this.fromVod) {
            msg.put(FROM_VOD, Boolean.valueOf(true));
        }
        if (this.systemMsg) {
            msg.put(SYSTEM_MSG, Boolean.valueOf(true));
        }
        if (this.owner) {
            msg.put(OWNER, Boolean.valueOf(true));
        }
        if (this.moderator) {
            msg.put(MODERATOR, Boolean.valueOf(true));
        }
        if (this.hidden) {
            msg.put(HIDDEN, Boolean.valueOf(true));
        }
        if (this.muteOrigin) {
            msg.put(MUTE_ORIGIN, Boolean.valueOf(true));
        }
        if (this.triggeredMute) {
            msg.put(TRIGGERED_MUTE, Boolean.valueOf(true));
        }
        if (metadata != null) {
            for (Entry<String, Object> entry : metadata.entrySet()) {
                msg.put(entry.getKey(), entry.getValue());
            }
        }
        return msg;
    }
}
