package com.mobcrush.mobcrush;

import com.facebook.share.internal.ShareConstants;
import com.mobcrush.mobcrush.logic.SocialNetwork;
import java.util.ArrayList;
import java.util.UUID;

public class Constants {
    public static final String ACTION_CHATS = "Chats";
    public static final String ACTION_DURATION = "Duration";
    public static final String ACTION_ENTER = "Enter";
    public static final String ACTION_FOLLOW = "Follow";
    public static final String ACTION_GAME = "Game";
    public static final String ACTION_LEAVE = "Leave";
    public static final String ACTION_LIKE = "Like";
    public static final String ACTION_LOGIN = "Login";
    public static final String ACTION_LOGOUT = "Logout";
    public static final String ACTION_PAUSE_PLAYER = "action_pause_player";
    public static final String ACTION_SHARES = "Shares";
    public static final String ACTION_SIGNUP = "Signup";
    public static final String ACTION_UPDATE_USER = "action_update_user";
    public static final String ACTION_UPDATE_USER_BASE_INFO = "action_update_user_base_info";
    public static final String ACTION_UPDATE_USER_PHOTO = "action_update_user_photo";
    public static final String ACTION_VIEW = "View";
    public static ArrayList<String> ADMINS_LIST = new ArrayList();
    public static final int ANIMATION_TIME = 300;
    public static final String APP_DOWNLOAD_URL = "http://www.mobcrush.com/beta";
    public static String APP_VERSION_NAME = "1.0";
    public static String BASE_ADDRESS = "stage-api.mobcrush.com";
    public static final String BASE_SCHEME = "https://";
    public static final long BROADCASTS_LIVE_TIME = 30000;
    public static final int BROADCASTS_PAGE_SIZE = 10;
    public static final String BROADCAST_ID_HOLDER = "{broadcastId}";
    public static final String CATEGORY_ACCOUNT = "Account";
    public static final String CATEGORY_REGISTRATION = "Registration";
    public static final String CATEGORY_VIEWER = "Viewer";
    public static final int CHAT_ACTION_APPOINT_AS_MOD = 5;
    public static final int CHAT_ACTION_BAN = 7;
    public static final int CHAT_ACTION_DISAPPOINT_AS_MOD = 6;
    public static final int CHAT_ACTION_IGNORE = 1;
    public static final int CHAT_ACTION_MUTE = 2;
    public static final int CHAT_ACTION_UNBAN = 8;
    public static final int CHAT_ACTION_UNIGNORE = 3;
    public static final int CHAT_ACTION_UNMUTE = 4;
    public static String CHAT_BASE_ADDRESS = "https://mobcrush-stage.firebaseio.com";
    public static final String CHAT_EXPIRE_TIMESTAMP = "expireTimestamp";
    public static final int CHAT_HISTORY_DEPTH = 10;
    public static final int CHAT_MAX_USERS_TO_HIDE_SERVICE_MESSAGES = 20;
    public static final String CHAT_MESSAGE_TIMESTAMP = "timestamp";
    public static final String CHAT_MODERATORS = "global-moderators";
    public static final int CHAT_OFFLINE_HISTORY_DEPTH = 50;
    public static int CHAT_POINTS_LIMIT = 80;
    public static int CHAT_POINTS_TICKER = MESSAGE_TYPE_NEED_NEXT_DATA;
    public static final String CHAT_ROOM_ANONYMOUS_USERS = "room-anonymous-users";
    public static final String CHAT_ROOM_IGNORED_USERS = "ignored-users";
    public static final String CHAT_ROOM_MESSAGES = "room-messages";
    public static final String CHAT_ROOM_METADATA = "room-metadata";
    public static final String CHAT_ROOM_MODERATORS = "room-moderators";
    public static final String CHAT_ROOM_MUTED_USERS = "room-muted-users";
    public static final String CHAT_ROOM_USERS = "room-users";
    public static final String CLIENT_ID = "6ef1f4b5-d19c-49f2-9ef9-edd0193a49c4";
    public static final long CONFIG_LIFETIME = 216000000;
    public static final boolean DEBUG = true;
    public static final int DISCUSSION_HISTORY_DEPTH = 100;
    public static final int EDIT_DEFAULT_BACKGROUND = 2130837567;
    public static final int EDIT_ERROR_BACKGROUND = 2130837588;
    public static final String EVENT_APP_RESUMED = "com.mobcrush.mobcrush.event.app_resumed";
    public static final String EVENT_AUTH_REQUIRED = "com.mobcrush.mobcrush.event.auth_required";
    public static final String EVENT_LOGIN = "com.mobcrush.mobcrush.event.login";
    public static final String EVENT_LOGOUT = "com.mobcrush.mobcrush.event.logout";
    public static final String EVENT_MOD_CHANGED = "com.mobcrush.mobcrush.event.mod_changed";
    public static final String EVENT_SIGNUP = "com.mobcrush.mobcrush.event.signup";
    public static final String EVENT_UPDATED = "com.mobcrush.mobcrush.event.updated";
    public static final String EXTRA_BROADCAST = "extra_broadcast";
    public static final String EXTRA_BROADCASTS = "extra_broadcasts";
    public static final String EXTRA_BROADCASTS_LOGIC = "extra_broadcasts_logic";
    public static final String EXTRA_BROADCAST_ID = "extra_broadcast_id";
    public static final String EXTRA_CHANNEL = "extra_channel";
    public static final String EXTRA_CHAT_ACTIVE = "extra_chat_active";
    public static final String EXTRA_CHAT_MESSAGES = "extra_chat_messages";
    public static final String EXTRA_CHAT_MODERATOR = "extra_chat_show_moderator";
    public static final String EXTRA_CHAT_POSITION = "extra_chat_position";
    public static final String EXTRA_CHAT_RATE_LIMIT = "extra_chat_rate_limit";
    public static final String EXTRA_CHAT_SHOW_JOINED = "extra_chat_show_joined";
    public static final String EXTRA_DIALOG_MODE = "extra_dialog_mode";
    public static final String EXTRA_GAME = "extra_game";
    public static final String EXTRA_GAME_LOGIC = "extra_game_logic";
    public static final String EXTRA_ID = "extra_id";
    public static final String EXTRA_LOGIC = "extra_logic";
    public static final String EXTRA_MODERATOR = "extra_moderator";
    public static final String EXTRA_PATH_TO_FILE = "extra_path_to_file";
    public static final String EXTRA_POSITION = "extra_position";
    public static final String EXTRA_PROJECTION_CODE = "extra_projection_code";
    public static final String EXTRA_PROJECTION_INTENT = "extra_projection_intent";
    public static final String EXTRA_ROW_NO = "extra_row_no";
    public static final String EXTRA_ROW_OFFSET = "extra_row_offset";
    public static final String EXTRA_SCREEN_DENSITY = "extra_screen_density";
    public static final String EXTRA_SHOW_BANNED = "extra_show_banned";
    public static final String EXTRA_SOURCE = "extra_source";
    public static final String EXTRA_STREAM_KEY = "extra_broadcast_id";
    public static final String EXTRA_SWITCH_TO_POPULAR = "extra_switch_to_popular";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_USER = "extra_user";
    public static final String EXTRA_USER_CHANNEL = "extra_user_channel";
    public static final String EXTRA_USER_LOGIC = "extra_user_logic";
    public static final String FACEBOOK = SocialNetwork.Facebook.toString();
    public static final String FEEDBACK_EMAIL = "feedback@mobcrush.com";
    public static final String FOLLOW_TYPEFACE = "Klavika-Regular.ttf";
    public static final String GOOGLE = SocialNetwork.Google.toString();
    public static final String HLS_KEY_HOLDER = "{hls-key}";
    public static final String HLS_MEDIA_CONTENT_ID = "uid:hls:applemaster";
    public static final String INGEST_INDEX_HOLDER = "{ingest-index} ";
    public static final int MAX_PROFILE_IMAGE_SIZE = 640;
    public static final String[] MENU_ADAPTER_KEYS;
    public static final int MESSAGE_TYPE_DISABLE_REFRESH = 8;
    public static final int MESSAGE_TYPE_ENABLE_REFRESH = 7;
    public static final int MESSAGE_TYPE_LOADING_COMPLETE = 6;
    public static final int MESSAGE_TYPE_LOADING_STARTED = 5;
    public static final int MESSAGE_TYPE_NEED_NEXT_DATA = 2;
    public static final int MESSAGE_TYPE_NEED_RESIZE = 1;
    public static final int MESSAGE_TYPE_NEED_RESTORE_DATA = 4;
    public static final int MESSAGE_TYPE_NEED_UPDATE_DATA = 3;
    public static final int MINIMAL_ALLOWED_AGE = 13;
    public static String MIXPANEL_TOKEN = "56a1f5d19268346978438903d69e0c3c";
    public static final String MOBCRUSH_SCHEME = "mobcrush://";
    public static final long NOTIFICATION_BANNER_TIMEOUT = 5000;
    public static final String PRODUCTION_PACKAGE = "com.mobcrush.mobcrush";
    public static final String PUBNUB_PUBLISH_KEY = "pub-c-9d1ae248-225a-4c50-9761-e333b42b31bd";
    public static final String PUBNUB_SUBSCRIBE_KEY = "sub-c-7fc7ae04-480a-11e4-aaa5-02ee2ddab7fe";
    public static final String REGION_NAME_HOLDER = "{region-name}";
    public static final String ROBOTO_LIGHT_FONT_NAME = "Roboto-Light.ttf";
    public static final String ROBOTO_MEDIUM_FONT_NAME = "Roboto-Medium.ttf";
    public static final String SCREEN_CHANNEL = "Channel";
    public static final String SCREEN_FEATURED_ALL = "Featured-All";
    public static final String SCREEN_FEATURED_PARTNER = "Featured-Partner";
    public static final String SCREEN_GAMES = "Games";
    public static final String SCREEN_GAMES_ALL = "Games-All";
    public static final String SCREEN_GAMES_PROMOTED = "Games-Promoted";
    public static final String SCREEN_GAME_BROADCASTERS = "Game-Broadcasters";
    public static final String SCREEN_GAME_DETAILS = "Broadcast";
    public static final String SCREEN_GAME_DISCUSSION = "Game-Discussion";
    public static final String SCREEN_GAME_VIDEOS = "Game-Videos";
    public static final String SCREEN_LIKES = "Settings-VideosLiked";
    public static final String SCREEN_LOGIN = "Settings-Login";
    public static final String SCREEN_PROFILE_BROADCASTS = "Channel-Broadcasts";
    public static final String SCREEN_PROFILE_FOLLOWERS = "Channel-Followers";
    public static final String SCREEN_PROFILE_FOLLOWING = "Channel-Following";
    public static final String SCREEN_SEARCH = "Search";
    public static final String SCREEN_SETTINGS = "Settings";
    public static final String SCREEN_SETTINGS_FACEBOOK = "Settings-Facebook";
    public static final String SCREEN_SETTINGS_TWITTER = "Settings-Twitter";
    public static final String SCREEN_SIGN_UP = "Settings-Signup";
    public static final String SCREEN_TEAMS_PROMOTED = "Teams-Promoted";
    public static final String SCREEN_TEAM_DISCUSSION = "Team-Discussion";
    public static final String SCREEN_TEAM_USERS = "Team-Users";
    public static final String SCREEN_TEAM_VIDEOS = "Team-Videos";
    public static final String SCREEN_TOURNAMENTS_ALL = "Tournaments-All";
    public static final String SCREEN_TOURNAMENTS_PROMOTED = "Tournaments-Promoted";
    public static final String SCREEN_TOURNAMENT_HEARTHSTONE = "Tournament_Hearthstone_AVGL";
    public static final String SCREEN_WATCH_FEED = "Watch feed";
    public static final String SCREEN_WATCH_FEED_NEW = "Watch-New";
    public static final String SCREEN_WATCH_FEED_POPULAR = "Watch-Popular";
    public static final long SEARCH_DELAY = 300;
    public static final String SENDER_ID = "68844038518";
    public static boolean SHOW_WHATS_NEW = false;
    public static final String STREAM_INDEX_HOLDER = "{stream-index}";
    public static String TERMS_OF_SERVICES_ADDRESS = "http://www.mobcrush.com/pages/tos";
    public static final String TWITTER = SocialNetwork.Twitter.toString();
    public static final String TWITTER_FAILURE_URL = "com.mobcrush.mobcrush://twitter/failed";
    public static final String TWITTER_SUCCESS_URL = "com.mobcrush.mobcrush://twitter/success";
    public static final int UPDATE_COFIG_INTERVAL = 1000;
    public static final int UPDATE_VIEWERS_INTERVAL = 5000;
    public static final int USERS_PAGE_SIZE = 20;
    public static final UUID WIDEVINE_UUID = new UUID(-1301668207276963122L, -6645017420763422227L);

    static {
        String[] strArr = new String[MESSAGE_TYPE_NEED_NEXT_DATA];
        strArr[0] = ShareConstants.TITLE;
        strArr[MESSAGE_TYPE_NEED_RESIZE] = "ICON";
        MENU_ADAPTER_KEYS = strArr;
    }
}
