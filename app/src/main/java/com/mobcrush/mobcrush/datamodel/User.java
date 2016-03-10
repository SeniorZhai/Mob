package com.mobcrush.mobcrush.datamodel;

import android.content.Context;
import android.text.TextUtils;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.logic.RoleType;
import java.util.UUID;
import java.util.regex.Pattern;

public class User extends DataModel {
    public String _id = getGuestId();
    public String accountCreation;
    public int age;
    public boolean banned;
    public String birthdate;
    public Integer broadcastCount;
    public boolean currentFollowed;
    public String email;
    public boolean fb_connect;
    public Integer followerCount;
    public Integer followingCount;
    public String hlsKey;
    public Integer likeCount;
    private Pattern mentionRegexp;
    public boolean notifyEnabled;
    public String primaryChannelUser;
    public String profileLogo;
    public String profileLogoSmall;
    public RoleType role;
    public String streamKey;
    public String subtitle;
    public boolean twitter_connect;
    public String username;
    public Integer viewCount;

    public boolean isGuest(Context context) {
        return context == null || context.getString(R.string.guest).equals(this.username);
    }

    public boolean equals(User user) {
        return equals(user, false);
    }

    public boolean equals(User user, boolean deepComparing) {
        boolean rawRes = user != null && TextUtils.equals(this._id, user._id);
        return (deepComparing && rawRes) ? TextUtils.equals(toString(), user.toString()) : rawRes;
    }

    private String getUsernameForRegexp() {
        if (this.username != null) {
            return Pattern.quote(this.username).replace("/[\\-\\[\\]\\/\\{\\}\\(\\)\\*\\+\\?\\.\\\\\\^\\$\\|]/g", "\\$&");
        }
        return null;
    }

    public Pattern getMentionRegexp() {
        if (this.mentionRegexp == null) {
            this.mentionRegexp = Pattern.compile("(^|[^a-zA-Z0-9])(@?" + getUsernameForRegexp() + ")([^a-zA-Z0-9]|$)", 2);
        }
        return this.mentionRegexp;
    }

    public int getBroadcastCount() {
        return this.broadcastCount == null ? 0 : this.broadcastCount.intValue();
    }

    public static String getGuestId() {
        return "guest-" + UUID.randomUUID();
    }

    public int compare(User user) {
        if (user == null) {
            return 1;
        }
        if (this.role == null) {
            this.role = RoleType.user;
        }
        if (user.role == null) {
            user.role = RoleType.user;
        }
        int res = user.role.compareTo(this.role);
        if (res != 0) {
            return res;
        }
        if (TextUtils.equals(this.username, user.username)) {
            return 0;
        }
        if (this.username == null) {
            return -1;
        }
        return this.username.compareToIgnoreCase(user.username);
    }
}
