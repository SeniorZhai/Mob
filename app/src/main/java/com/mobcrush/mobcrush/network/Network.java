package com.mobcrush.mobcrush.network;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestQueue.RequestFilter;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.internal.ServerProtocol;
import com.facebook.share.internal.ShareConstants;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.gson.Gson;
import com.helpshift.res.values.HSConsts;
import com.helpshift.storage.ProfilesDBHelper;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.EmailVerificationRequestActivity;
import com.mobcrush.mobcrush.LoginActivity;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.BroadcastData;
import com.mobcrush.mobcrush.datamodel.Channel;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.datamodel.EntityType;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.datamodel.GroupChannel;
import com.mobcrush.mobcrush.datamodel.MenuGroup;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.mobcrush.mobcrush.datamodel.Watch;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import com.mobcrush.mobcrush.logic.NetworkLogic;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper.Event;
import com.nostra13.universalimageloader.utils.StorageUtils;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.AbstractSpiCall;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Network {
    private static final String ACCESS_TOKEN = "access_token";
    public static final String AUTH_TOKEN_NAME = "Authorization";
    public static final String BASIC_AUTH_STRING = "Basic NmVmMWY0YjUtZDE5Yy00OWYyLTllZjktZWRkMDE5M2E0OWM0OmE=";
    private static final String BROADCAST = "broadcast/%s";
    private static final String BROADCASTS = "broadcasts/%s/%s";
    private static final String BROADCAST_STATS = "broadcastStats/%s";
    private static HashSet<String> CACHE_FILTER = new HashSet();
    private static final String CHANNEL = "channel/%s";
    private static final String CHANNEL_BROADCASTS = "channelBroadcasts/%s/%s/%s";
    private static final String CHANNEL_USERS = "channelUsers/%s";
    private static final String CHECK_IF_FOLLOWER = "checkIfFollower/%s/%s";
    public static final String CONFIG = "config";
    private static final String END_BROADCAST = "endBroadcast";
    private static final String FEATURED_SPOTLIGHT_USERS = "featuredSpotlightUsers";
    private static final String FEATURED_TOP_USERS = "featuredTopUsers";
    private static final String FIREBASE_TOKEN = "chat/firebase/token";
    private static final String FOLLOW = "follow";
    private static final String FOLLOWERS = "followers/%s/%s/%s/%s";
    private static final String FOLLOWING = "following/%s/%s/%s/%s";
    private static final String FOLLOWING_NOTIFY_TOGGLE = "followingNotifyToggle";
    private static final String FOLLOWING_SETTINGS = "followingSettings/%s/%s";
    private static final String FOLLOW_DELETE = "follow/%s/%s";
    private static final String GAME = "game/%s";
    private static final String GAMES = "games";
    private static final String GAME_BROADCASTS = "gameBroadcasts/%s/%s/%s";
    private static final String GAME_USERS = "gameUsers/%s";
    private static final String LAST_STREAM = "lastStream/";
    private static final String LATEST_BROADCAST = "latestBroadcastFromStreamKey/%s";
    private static final String LIKE = "like";
    private static final String LIKED_BROADCAST = "userLikeBroadcasts/%s/%s/%s";
    private static final String LIKE_DELETE = "like/%s";
    public static final String LOGIN_OR_REFRESH = "oauth2/token";
    public static final int MAX_GET_RETRY_COUNT = 3;
    public static final String MC_USER = "Mobcrush-user";
    private static final String ME = "me";
    private static final String MENU = "menu";
    private static final String MENU_GAMES = "menuGames";
    private static final String MENU_TOURNAMENTS = "menuTournaments";
    private static final String NOTIFY_FOLLOWERS = "notify/followers/%s/%s";
    private static final String POPULAR_BROADCASTS = "popularBroadcasts/%s/%s";
    private static final String PROFILE = "user/%s";
    private static final String PROFILE_PHOTO = "userPhoto";
    public static final String REGISTER = "users";
    private static final String REGISTER_DEVICE = "devices";
    private static final String RESEND_VERIFICATION_EMAIL = "resendEmailVerification";
    private static final String RESET_PASSWORD = "passwordResetRequest";
    private static final String SEARCH_FOLLOWERS = "search/followers?regex=%s&pageIndex=%s&pageSize=%s";
    private static final String SEARCH_USERS = "search/byUserList";
    private static final String SOCIAL_REGISTER_FB_TKN = "sc/social/connect/facebook";
    private static final String SOCIAL_REGISTER_TWITTER_TKN = "sc/social/connect/twitter";
    private static final String SOCIAL_REGISTER_YOUTUBE_TKN = "sc/social/youtube/%s";
    private static final String SOCIAL_SHARE_BROADCAST = "sc/social/broadcast/%s";
    private static final String SOCIAL_TWITTER_OAUTH_URL = "oauth/authorize/twitter";
    private static final String SOCIAL_USER_CONNECT = "sc/social/user/%s";
    private static final String SOCIAL_USER_DISCONNECT = "sc/social/userdisconnect/%s";
    private static final String SOCIAL_USER_GET_CONNECTIONS = "sc/social/user/connections/%s";
    private static final String STATUS = "status";
    private static final String STATUS_OK = "OK";
    private static final String STREAM_DEVICE_APPROVAL = "checkStreamDeviceApproval?os=android&model=%s";
    private static final String TAG = "Network";
    public static final int TIMEOUT_MS = 50000;
    private static final String UPDATE_VIEWER_COUNT = "updateViewerCount";
    private static final String USER_AGENT = "MobCrush-Android";
    private static final String USER_BAN = "userBan";
    private static final String USER_BROADCASTS = "userBroadcasts/%s/%s/%s";
    private static final String USER_CHANNELS = "userChannels";
    private static final String USER_CHANNEL_DESCRIPTION = "userChannelDescription";
    private static final String WATCHES = "watchFeed";
    private static ImageLoader mImageLoader;
    private static RequestQueue mRQ;

    static {
        CACHE_FILTER.add(MENU);
    }

    public static boolean isLoggedIn() {
        return PreferenceUtility.getRefreshToken() != null;
    }

    public static boolean isInternetAvailable(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void init(Context context) {
        mRQ = getRequestQueue(context);
    }

    public static void cancelAll() {
        if (mRQ != null) {
            try {
                mRQ.cancelAll(new RequestFilter() {
                    public boolean apply(Request<?> request) {
                        return true;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
    }

    private static RequestQueue getRequestQueue(Context context) {
        return Volley.newRequestQueue(context, new HurlStack());
    }

    public static ImageLoader getImageLoader() {
        if (mImageLoader == null) {
            mImageLoader = new ImageLoader(mRQ, new BitmapLruCache(StorageUtils.getCacheDirectory(MainApplication.getContext())));
        }
        return mImageLoader;
    }

    public static void registerAccount(final FragmentActivity activity, String username, String email, String password, Calendar birthDate, Listener<Boolean> listener, ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        params.put(ChatMessage.USERNAME, username);
        params.put(ProfilesDBHelper.COLUMN_EMAIL, email);
        params.put("password", password);
        params.put("clientId", Constants.CLIENT_ID);
        params.put("birthdate", String.valueOf(birthDate.getTime().getTime()));
        final ErrorListener errorListener2 = errorListener;
        final Listener<Boolean> listener2 = listener;
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 1, false, REGISTER, params, 0, new Listener<String>() {
            public void onResponse(String response) {
                Log.i(Network.TAG, "registerAccount: " + response);
                if (response != null) {
                    if (response.contains("USR_")) {
                        try {
                            if (new JSONObject(response).has(Network.STATUS)) {
                                if (errorListener2 != null) {
                                    errorListener2.onErrorResponse(new VolleyError(response));
                                    return;
                                } else if (listener2 != null) {
                                    listener2.onResponse(null);
                                    return;
                                } else {
                                    return;
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    Network.updateAuthData(response);
                    try {
                        PreferenceUtility.setUser(new JSONObject(response).getString(DBLikedChannelsHelper.USER));
                        Network.registerInBackground(activity);
                        NetworkLogic.onSignedUp();
                        GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_REGISTRATION, Constants.ACTION_SIGNUP);
                    } catch (JSONException e2) {
                        response = null;
                    }
                }
                if (listener2 != null) {
                    boolean z;
                    Listener listener = listener2;
                    if (response == null || !response.contains(Network.ACCESS_TOKEN)) {
                        z = false;
                    } else {
                        z = true;
                    }
                    listener.onResponse(Boolean.valueOf(z));
                }
            }
        }, listener, errorListener));
    }

    public static void login(final FragmentActivity activity, String username, String password, final Listener<Boolean> listener, final ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        params.put(ChatMessage.USERNAME, username);
        params.put("password", password);
        params.put("grant_type", "password");
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                Network.updateAuthData(response);
                if (response != null) {
                    try {
                        PreferenceUtility.setUser(new JSONObject(response).getString(DBLikedChannelsHelper.USER));
                        Network.registerInBackground(activity);
                        Network.getMyProfile(activity, new Listener<User>() {
                            public void onResponse(User response) {
                                if (response != null) {
                                    Network.getUserChannels(activity, false, new Listener<UserChannel[]>() {
                                        public void onResponse(UserChannel[] response) {
                                            MixpanelHelper.getInstance(MainApplication.getContext()).generateLogInEvent();
                                            listener.onResponse(Boolean.valueOf(response != null));
                                            NetworkLogic.onLogin();
                                        }
                                    }, errorListener);
                                } else {
                                    listener.onResponse(Boolean.valueOf(false));
                                }
                            }
                        }, errorListener);
                        GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_ACCOUNT, Constants.ACTION_LOGIN);
                    } catch (JSONException e) {
                        listener.onResponse(null);
                    }
                    Network.getFirebaseToken(activity, null, null);
                    return;
                }
                listener.onResponse(Boolean.valueOf(false));
            }
        };
        executeAuthRequest(new NetworkTask(activity, 1, false, String.format(LOGIN_OR_REFRESH, new Object[]{Constants.BASE_SCHEME, Constants.CLIENT_ID, "a", Constants.BASE_ADDRESS}), params, 0, respListener, listener, errorListener));
    }

    public static void resetPassword(FragmentActivity activity, String email, final Listener<Boolean> listener, ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        params.put(ProfilesDBHelper.COLUMN_EMAIL, email);
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 1, false, RESET_PASSWORD, params, 0, new Listener<String>() {
            public void onResponse(String response) {
                if (response != null) {
                    Log.i(Network.TAG, "Reset password: " + response);
                    if (listener != null) {
                        listener.onResponse(Boolean.valueOf(response.contains("app_success")));
                    }
                } else if (listener != null) {
                    listener.onResponse(null);
                }
            }
        }, listener, errorListener));
    }

    public static void resendVerificationEmail(FragmentActivity activity, final Listener<Boolean> listener, ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        boolean z = true;
        executeAuthRequest(new NetworkTask(fragmentActivity, 1, z, RESEND_VERIFICATION_EMAIL, null, 0, new Listener<String>() {
            public void onResponse(String response) {
                if (response != null) {
                    if (listener != null) {
                        listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                    }
                } else if (listener != null) {
                    listener.onResponse(null);
                }
            }
        }, listener, errorListener));
    }

    public static void refreshToken(FragmentActivity activity, final Listener<Boolean> listener, ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        params.put("grant_type", "refresh_token");
        params.put("refresh_token", PreferenceUtility.getRefreshToken());
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                Network.updateAuthData(response);
                if (listener != null) {
                    listener.onResponse(Boolean.valueOf(response != null));
                }
            }
        };
        executeAuthRequest(new NetworkTask(activity, 1, false, String.format(LOGIN_OR_REFRESH, new Object[]{Constants.BASE_SCHEME, Constants.CLIENT_ID, "a", Constants.BASE_ADDRESS}), params, 0, respListener, listener, errorListener));
    }

    public static void getFirebaseToken(FragmentActivity activity, final Listener<Boolean> listener, final ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, isLoggedIn(), FIREBASE_TOKEN, null, 0, new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "getFirebaseToken: " + response);
                try {
                    JSONObject o = new JSONObject(response);
                    if (o.has("token")) {
                        PreferenceUtility.setFirebaseToken(o.optString("token"));
                        if (listener != null) {
                            listener.onResponse(Boolean.valueOf(true));
                            return;
                        }
                        return;
                    }
                    PreferenceUtility.removeFirebaseToken();
                    if (listener != null) {
                        listener.onResponse(Boolean.valueOf(false));
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                    if (errorListener != null) {
                        errorListener.onErrorResponse(new VolleyError(e));
                    } else if (listener != null) {
                        listener.onResponse(null);
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void updateConfig(FragmentActivity activity, final Listener<Boolean> listener, final ErrorListener errorListener) {
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "config: " + response);
                if (response != null) {
                    PreferenceUtility.setConfig(response);
                    if (listener != null) {
                        listener.onResponse(Boolean.valueOf(true));
                    }
                } else if (errorListener != null) {
                    errorListener.onErrorResponse(new VolleyError("can't get config"));
                } else if (listener != null) {
                    listener.onResponse(Boolean.valueOf(false));
                }
            }
        };
        Crashlytics.log("Config is requested");
        executeAuthRequest(new NetworkTask(activity, 0, false, CONFIG, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
    }

    public static void getMyProfile(FragmentActivity activity, final Listener<User> listener, final ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, isLoggedIn(), ME, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "getMe: " + response);
                if (response != null) {
                    try {
                        User user = (User) new Gson().fromJson(response, User.class);
                        if (PreferenceUtility.getUser().equals(user)) {
                            PreferenceUtility.setUser(response);
                        }
                        if (listener != null) {
                            listener.onResponse(user);
                        }
                    } catch (Throwable e) {
                        Log.e(Network.TAG, "response: " + response, e);
                        Crashlytics.logException(new Exception("Error in getUserProfile: " + response, e));
                        if (errorListener != null) {
                            errorListener.onErrorResponse(new VolleyError(e));
                        } else if (listener != null) {
                            listener.onResponse(null);
                        }
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void getUserProfile(FragmentActivity activity, String uid, final Listener<User> listener, final ErrorListener errorListener) {
        if (TextUtils.isEmpty(uid)) {
            if (isLoggedIn()) {
                uid = PreferenceUtility.getUser()._id;
            } else if (listener != null) {
                listener.onResponse(null);
                return;
            } else {
                return;
            }
        }
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null && response != null) {
                    try {
                        Log.d(Network.TAG, "getUserResponse: " + response);
                        listener.onResponse((User) new Gson().fromJson(response, User.class));
                    } catch (Throwable e) {
                        Log.e(Network.TAG, "response: " + response, e);
                        Crashlytics.logException(new Exception("Error in getUserProfile: " + response, e));
                        if (errorListener != null) {
                            errorListener.onErrorResponse(new VolleyError(e));
                        } else {
                            listener.onResponse(null);
                        }
                    }
                }
            }
        };
        executeAuthRequest(new NetworkTask(activity, 0, false, String.format(PROFILE, new Object[]{uid}), null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
    }

    public static void setUserChannelDescription(FragmentActivity activity, String description, final Listener<Boolean> listener, ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        params.put(ShareConstants.WEB_DIALOG_PARAM_DESCRIPTION, description);
        FragmentActivity fragmentActivity = activity;
        boolean z = true;
        executeAuthRequest(new NetworkTask(fragmentActivity, 1, z, USER_CHANNEL_DESCRIPTION, params, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "setUserChannelDescription: " + response);
                if (listener != null) {
                    listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                }
            }
        }, listener, errorListener));
    }

    public static void getUserChannelDescription(FragmentActivity activity, String uid, final Listener<String> listener, final ErrorListener errorListener) {
        if (TextUtils.isEmpty(uid)) {
            if (isLoggedIn()) {
                uid = PreferenceUtility.getUser()._id;
            } else if (listener != null) {
                listener.onResponse(null);
                return;
            } else {
                return;
            }
        }
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null && response != null) {
                    try {
                        Log.d(Network.TAG, "getUserChannelDescription: " + response);
                        listener.onResponse(new JSONObject(response).optString(ShareConstants.WEB_DIALOG_PARAM_DESCRIPTION));
                    } catch (Throwable e) {
                        Log.e(Network.TAG, "response: " + response, e);
                        Crashlytics.logException(new Exception("Error in getUserChannelDescription: " + response, e));
                        if (errorListener != null) {
                            errorListener.onErrorResponse(new VolleyError(e));
                        } else {
                            listener.onResponse(null);
                        }
                    }
                }
            }
        };
        executeAuthRequest(new NetworkTask(activity, 0, false, "userChannelDescription/" + uid, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
    }

    public static void getGames(FragmentActivity activity, final Listener<Game[]> listener, ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        boolean z = false;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, z, GAMES, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Game[] games = (Game[]) new Gson().fromJson(response, Game[].class);
                if (listener != null) {
                    if (games == null) {
                        games = new Game[0];
                    }
                    listener.onResponse(games);
                }
            }
        }, listener, errorListener));
    }

    public static void getMenu(FragmentActivity activity, final Listener<MenuGroup[]> listener, final ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, isLoggedIn(), MENU, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                try {
                    Log.d(Network.TAG, "getMenu: " + response);
                    MenuGroup[] menuGroups = (MenuGroup[]) new Gson().fromJson(new JSONObject(response).optString("menuItems"), MenuGroup[].class);
                    if (listener != null) {
                        listener.onResponse(menuGroups);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (errorListener != null) {
                        errorListener.onErrorResponse(new VolleyError(e));
                    } else if (listener != null) {
                        listener.onResponse(null);
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void getMenuTournaments(FragmentActivity activity, final Listener<Channel[]> listener, ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        boolean z = false;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, z, MENU_TOURNAMENTS, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Channel[] channels = (Channel[]) new Gson().fromJson(response, Channel[].class);
                if (listener != null) {
                    if (channels == null) {
                        channels = new Channel[0];
                    }
                    listener.onResponse(channels);
                }
            }
        }, listener, errorListener));
    }

    public static void getMenuGames(FragmentActivity activity, final Listener<Game[]> listener, ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        boolean z = false;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, z, MENU_GAMES, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Game[] games = (Game[]) new Gson().fromJson(response, Game[].class);
                if (listener != null) {
                    if (games == null) {
                        games = new Game[0];
                    }
                    listener.onResponse(games);
                }
            }
        }, listener, errorListener));
    }

    public static void getWatchesOld(FragmentActivity activity, final Listener<Watch[]> listener, ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        boolean z = false;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, z, WATCHES, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Watch[] watches = (Watch[]) new Gson().fromJson(response, Watch[].class);
                if (listener != null) {
                    if (watches == null) {
                        watches = new Watch[0];
                    }
                    listener.onResponse(watches);
                }
            }
        }, listener, errorListener));
    }

    public static void getWatches(FragmentActivity activity, final Listener<Broadcast[]> listener, ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, isLoggedIn(), WATCHES, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Broadcast[] broadcasts = (Broadcast[]) new Gson().fromJson(response, Broadcast[].class);
                if (listener != null) {
                    if (broadcasts == null) {
                        broadcasts = new Broadcast[0];
                    }
                    listener.onResponse(broadcasts);
                }
            }
        }, listener, errorListener));
    }

    public static void getBroadcasts(FragmentActivity activity, int pageIndex, int pageSize, final Listener<Broadcast[]> listener, final ErrorListener errorListener) {
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                try {
                    Broadcast[] broadcasts = (Broadcast[]) new Gson().fromJson(response, Broadcast[].class);
                    if (listener != null) {
                        if (broadcasts == null) {
                            broadcasts = new Broadcast[0];
                        }
                        listener.onResponse(broadcasts);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    Crashlytics.logException(new Exception("getBroadcast: " + response, e));
                    if (errorListener != null) {
                        errorListener.onErrorResponse(new VolleyError(e));
                    } else if (listener != null) {
                        listener.onResponse(null);
                    }
                }
            }
        };
        String address = String.format(BROADCASTS, new Object[]{Integer.valueOf(pageIndex), Integer.valueOf(pageSize)});
        executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener).setTag(BROADCASTS));
    }

    public static void cancelBroadcasts() {
        mRQ.cancelAll(BROADCASTS);
    }

    public static void getPopularBroadcasts(FragmentActivity activity, int pageIndex, int pageSize, final Listener<Broadcast[]> listener, ErrorListener errorListener) {
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                Broadcast[] broadcasts = (Broadcast[]) new Gson().fromJson(response, Broadcast[].class);
                if (listener != null) {
                    if (broadcasts == null) {
                        broadcasts = new Broadcast[0];
                    }
                    listener.onResponse(broadcasts);
                }
            }
        };
        String address = String.format(POPULAR_BROADCASTS, new Object[]{Integer.valueOf(pageIndex), Integer.valueOf(pageSize)});
        executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener).setTag(POPULAR_BROADCASTS));
    }

    public static void cancelPopularBroadcasts() {
        mRQ.cancelAll(POPULAR_BROADCASTS);
    }

    public static void getChannel(FragmentActivity activity, String channelId, final Listener<GroupChannel> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(channelId)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    GroupChannel broadcasts = (GroupChannel) new Gson().fromJson(response, GroupChannel.class);
                    if (listener != null) {
                        listener.onResponse(broadcasts);
                    }
                }
            };
            String address = String.format(CHANNEL, new Object[]{channelId});
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void getGame(FragmentActivity activity, String gameId, final Listener<Game> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(gameId)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    Game game = (Game) new Gson().fromJson(response, Game.class);
                    if (listener != null) {
                        listener.onResponse(game);
                    }
                }
            };
            String address = String.format(GAME, new Object[]{gameId});
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void getChannelBroadcasts(FragmentActivity activity, String channelId, int pageIndex, int pageSize, final Listener<Broadcast[]> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(channelId)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    Broadcast[] broadcasts = (Broadcast[]) new Gson().fromJson(response, Broadcast[].class);
                    if (listener != null) {
                        if (broadcasts == null) {
                            broadcasts = new Broadcast[0];
                        }
                        listener.onResponse(broadcasts);
                    }
                }
            };
            String str = CHANNEL_BROADCASTS;
            Object[] objArr = new Object[MAX_GET_RETRY_COUNT];
            objArr[0] = channelId;
            objArr[1] = Integer.valueOf(pageIndex);
            objArr[2] = Integer.valueOf(pageSize);
            Object address = String.format(str, objArr);
            mRQ.cancelAll(address);
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener).setTag(address));
        } else if (listener != null) {
            listener.onResponse(new Broadcast[0]);
        }
    }

    public static void getGameBroadcasts(FragmentActivity activity, String gameId, int pageIndex, int pageSize, final Listener<Broadcast[]> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(gameId)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    Broadcast[] broadcasts = (Broadcast[]) new Gson().fromJson(response, Broadcast[].class);
                    if (listener != null) {
                        if (broadcasts == null) {
                            broadcasts = new Broadcast[0];
                        }
                        listener.onResponse(broadcasts);
                    }
                }
            };
            String str = GAME_BROADCASTS;
            Object[] objArr = new Object[MAX_GET_RETRY_COUNT];
            objArr[0] = gameId;
            objArr[1] = Integer.valueOf(pageIndex);
            objArr[2] = Integer.valueOf(pageSize);
            Object address = String.format(str, objArr);
            mRQ.cancelAll(address);
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener).setTag(address));
        } else if (listener != null) {
            listener.onResponse(new Broadcast[0]);
        }
    }

    public static void getBroadcast(FragmentActivity activity, final String broadcastId, final Listener<Broadcast> listener, ErrorListener errorListener) {
        if (broadcastId != null) {
            Log.i(TAG, "getBroadcast is performed");
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    Broadcast broadcast = null;
                    if (!(response == null || response.contains("ERROR"))) {
                        try {
                            broadcast = (Broadcast) new Gson().fromJson(response, Broadcast.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                            broadcast = null;
                        }
                        if (broadcast == null) {
                            Crashlytics.log("Empty broadcast for id " + broadcastId + ": " + response);
                        } else if (TextUtils.isEmpty(broadcast._id)) {
                            broadcast = null;
                        }
                    }
                    if (listener != null) {
                        listener.onResponse(broadcast);
                    }
                }
            };
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), String.format(BROADCAST, new Object[]{broadcastId}), null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void getBroadcastStats(FragmentActivity activity, String broadcastId, final Listener<Broadcast> listener, ErrorListener errorListener) {
        if (broadcastId != null) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    Broadcast broadcast = null;
                    if (!(response == null || response.contains("ERROR"))) {
                        try {
                            broadcast = (Broadcast) new Gson().fromJson(response, Broadcast.class);
                        } catch (Exception e) {
                            e.printStackTrace();
                            broadcast = null;
                        }
                    }
                    if (listener != null) {
                        listener.onResponse(broadcast);
                    }
                }
            };
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), String.format(BROADCAST_STATS, new Object[]{broadcastId}), null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void setBroadcastInfo(FragmentActivity activity, String broadcastId, String title, String game, final Listener<Boolean> listener, ErrorListener errorListener) {
        if (broadcastId != null) {
            HashMap<String, String> params = new HashMap();
            params.put(SettingsJsonConstants.PROMPT_TITLE_KEY, title);
            if (game != null) {
                params.put("game", game);
            }
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    try {
                        if (listener != null) {
                            listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            };
            executeAuthRequest(new NetworkTask(activity, 2, isLoggedIn(), String.format(BROADCAST, new Object[]{broadcastId}), params, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void deleteBroadcast(FragmentActivity activity, String broadcastId, final Listener<Boolean> listener, ErrorListener errorListener) {
        if (broadcastId != null) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    try {
                        if (listener != null) {
                            boolean ret = Network.isResponseOK(response);
                            if (ret && PreferenceUtility.getUser().getBroadcastCount() != 0) {
                                User user = PreferenceUtility.getUser();
                                Integer num = user.broadcastCount;
                                user.broadcastCount = Integer.valueOf(user.broadcastCount.intValue() - 1);
                            }
                            listener.onResponse(Boolean.valueOf(ret));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            };
            executeAuthRequest(new NetworkTask(activity, MAX_GET_RETRY_COUNT, isLoggedIn(), String.format(BROADCAST, new Object[]{broadcastId}), null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void getLikedBroadcasts(FragmentActivity activity, User user, int pageIndex, int pageSize, final Listener<Broadcast[]> listener, ErrorListener errorListener) {
        if (user != null) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    Broadcast[] broadcasts = (Broadcast[]) new Gson().fromJson(response, Broadcast[].class);
                    if (listener != null) {
                        if (broadcasts == null) {
                            broadcasts = new Broadcast[0];
                        }
                        listener.onResponse(broadcasts);
                    }
                }
            };
            String str = LIKED_BROADCAST;
            Object[] objArr = new Object[MAX_GET_RETRY_COUNT];
            objArr[0] = user._id;
            objArr[1] = Integer.valueOf(pageIndex);
            objArr[2] = Integer.valueOf(pageSize);
            executeAuthRequest(new NetworkTask(activity, 0, true, String.format(str, objArr), null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void getUserChannels(FragmentActivity activity, boolean excludePersonalChannel, final Listener<UserChannel[]> listener, ErrorListener errorListener) {
        executeAuthRequest(new NetworkTask(activity, 0, true, USER_CHANNELS + (excludePersonalChannel ? "/true" : BuildConfig.FLAVOR), null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, response);
                UserChannel[] channels = (UserChannel[]) new Gson().fromJson(response, UserChannel[].class);
                if (listener != null) {
                    if (channels == null) {
                        channels = new UserChannel[0];
                    }
                    listener.onResponse(channels);
                }
                if (channels != null && channels.length > 0) {
                    boolean streamKeySet = false;
                    for (UserChannel userChannel : channels) {
                        if (userChannel.channel.type.equals(DBLikedChannelsHelper.USER)) {
                            PreferenceUtility.setStreamKey(userChannel.streamKey);
                            streamKeySet = true;
                        }
                    }
                    if (!streamKeySet) {
                        PreferenceUtility.setStreamKey(channels[0].streamKey);
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void updateViewerCount(FragmentActivity activity, String broadcastId, boolean inc, final Listener<Boolean> listener, ErrorListener errorListener) {
        Log.d(TAG, "updateViewerCount for: " + broadcastId + (inc ? ".INC" : ".DEC"));
        HashMap<String, String> params = new HashMap();
        params.put("broadcastId", broadcastId);
        params.put("incDec", inc ? "INC" : "DEC");
        executeAuthRequest(new NetworkTask(activity, 1, false, UPDATE_VIEWER_COUNT, params, 0, new Listener<String>() {
            public void onResponse(String response) {
                try {
                    if (listener != null) {
                        listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        }, listener, errorListener));
    }

    public static void notifyFollowers(FragmentActivity activity, EntityType type, String uid, String users, boolean sendToAll, final Listener<Boolean> listener, ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        if (sendToAll) {
            params.put("sendToAll", ServerProtocol.DIALOG_RETURN_SCOPES_TRUE);
        } else {
            params.put(REGISTER, users);
        }
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                try {
                    if (listener != null) {
                        listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onResponse(null);
                }
            }
        };
        String address = String.format(NOTIFY_FOLLOWERS, new Object[]{type.toString(), uid});
        executeAuthRequest(new NetworkTask(activity, 1, isLoggedIn(), address, params, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
    }

    public static void getLastStream(FragmentActivity activity, String hlsKey, final Listener<String> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(hlsKey)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    if (listener != null) {
                        try {
                            listener.onResponse(new JSONObject(response).optString("lastStreamIndex"));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(HSConsts.STATUS_NEW);
                        }
                    }
                }
            };
            executeAuthRequest(new NetworkTask(activity, 0, false, LAST_STREAM + hlsKey, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(BuildConfig.FLAVOR);
        }
    }

    public static void follow(FragmentActivity activity, final boolean follow, EntityType type, String oid, Listener<Boolean> listener, ErrorListener errorListener) {
        boolean forceClose = false;
        if (!PreferenceUtility.isEmailVerified() && isLoggedIn()) {
            Context context = activity;
            if (context == null) {
                context = MainApplication.getContext();
            }
            Intent intent = EmailVerificationRequestActivity.getIntent(context);
            intent.setFlags(268435456);
            context.startActivity(intent);
            forceClose = true;
        }
        if (!forceClose && !TextUtils.isEmpty(oid)) {
            HashMap<String, String> params = new HashMap();
            params.put("followedType", type.toString());
            params.put("oid", oid);
            final Listener<Boolean> listener2 = listener;
            executeAuthRequest(new NetworkTask(activity, follow ? 2 : MAX_GET_RETRY_COUNT, true, follow ? FOLLOW : String.format(FOLLOW_DELETE, new Object[]{type.toString(), oid}), params, MAX_GET_RETRY_COUNT, new Listener<String>() {
                public void onResponse(String response) {
                    if (listener2 != null) {
                        try {
                            listener2.onResponse(Network.isResponseOK(response) ? Boolean.valueOf(follow) : null);
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener2.onResponse(null);
                        }
                    }
                }
            }, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void like(FragmentActivity activity, final boolean like, Broadcast broadcast, final Listener<Boolean> listener, ErrorListener errorListener) {
        if (broadcast != null) {
            HashMap<String, String> params = new HashMap();
            params.put("broadcastId", broadcast._id);
            executeAuthRequest(new NetworkTask(activity, like ? 2 : MAX_GET_RETRY_COUNT, true, like ? LIKE : String.format(LIKE_DELETE, new Object[]{broadcast._id}), params, MAX_GET_RETRY_COUNT, new Listener<String>() {
                public void onResponse(String response) {
                    if (listener != null) {
                        try {
                            Object valueOf;
                            boolean status = Network.isResponseOK(new JSONObject(response));
                            if (Network.isLoggedIn() && status) {
                                User user = PreferenceUtility.getUser();
                                if (user.likeCount == null) {
                                    user.likeCount = Integer.valueOf(0);
                                }
                                user.likeCount = Integer.valueOf((like ? 1 : -1) + user.likeCount.intValue());
                            }
                            Listener listener = listener;
                            if (status) {
                                valueOf = Boolean.valueOf(like);
                            } else {
                                valueOf = null;
                            }
                            listener.onResponse(valueOf);
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(null);
                        }
                    }
                }
            }, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void getUserBroadcasts(FragmentActivity activity, String uid, int pageIndex, int pageSize, final Listener<Broadcast[]> listener, ErrorListener errorListener) {
        String str = TAG;
        Object[] objArr = new Object[MAX_GET_RETRY_COUNT];
        objArr[0] = uid;
        objArr[1] = Integer.valueOf(pageIndex);
        objArr[2] = Integer.valueOf(pageSize);
        Log.i(str, String.format("getUserBroadcasts(%s, %d, %d)", objArr));
        if (!TextUtils.isEmpty(uid)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    Log.d(Network.TAG, "getUserBroadcasts:" + response);
                    if (listener != null) {
                        try {
                            listener.onResponse((Broadcast[]) new Gson().fromJson(response, Broadcast[].class));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(null);
                        }
                    }
                }
            };
            str = USER_BROADCASTS;
            Object[] objArr2 = new Object[MAX_GET_RETRY_COUNT];
            objArr2[0] = uid;
            objArr2[1] = Integer.valueOf(pageIndex);
            objArr2[2] = Integer.valueOf(pageSize);
            Object address = String.format(str, objArr2);
            mRQ.cancelAll(address);
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener).setTag(address));
        } else if (listener != null) {
            listener.onResponse(new Broadcast[0]);
        }
    }

    public static void getUserConnections(FragmentActivity activity, String userId, final Listener<ArrayList<String>> listener, ErrorListener errorListener) {
        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("The userId parameter should be provided");
        }
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "getUserConnections:" + response);
                if (listener != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (Network.isResponseOK(jsonObject)) {
                            JSONArray array = jsonObject.optJSONArray("connections");
                            if (array != null && array.length() > 0) {
                                ArrayList<String> connections = new ArrayList();
                                for (int i = 0; i < array.length(); i++) {
                                    connections.add(array.getString(i));
                                }
                                listener.onResponse(connections);
                                return;
                            }
                        }
                        listener.onResponse(null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            }
        };
        try {
            String address = String.format(SOCIAL_USER_GET_CONNECTIONS, new Object[]{URLEncoder.encode(userId, "utf-8")});
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            if (errorListener != null) {
                errorListener.onErrorResponse(new VolleyError(e.getMessage()));
            } else if (listener != null) {
                listener.onResponse(null);
            }
        }
    }

    public static void establishUserConnection(FragmentActivity activity, String guid, final Listener<Boolean> listener, ErrorListener errorListener) {
        if (TextUtils.isEmpty(guid)) {
            throw new IllegalArgumentException("The guid parameter should be provided");
        }
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "establishUserConnection:" + response);
                if (listener == null) {
                    return;
                }
                if (Network.isResponseOK(response)) {
                    listener.onResponse(Boolean.valueOf(true));
                } else {
                    listener.onResponse(Boolean.valueOf(false));
                }
            }
        };
        try {
            String address = String.format(SOCIAL_USER_CONNECT, new Object[]{URLEncoder.encode(guid, "utf-8")});
            executeAuthRequest(new NetworkTask(activity, 2, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            if (errorListener != null) {
                errorListener.onErrorResponse(new VolleyError(e.getMessage()));
            } else if (listener != null) {
                listener.onResponse(null);
            }
        }
    }

    public static void disconnectUserSocialNetwork(FragmentActivity activity, String socialNetwork, final Listener<Boolean> listener, ErrorListener errorListener) {
        if (TextUtils.isEmpty(socialNetwork)) {
            throw new IllegalArgumentException("The socialNetwork parameter should be provided");
        }
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "disconnectUserSocialNetwork:" + response);
                if (listener == null) {
                    return;
                }
                if (Network.isResponseOK(response) || response.contains("NOTLOGGEDIN")) {
                    listener.onResponse(Boolean.valueOf(true));
                } else {
                    listener.onResponse(Boolean.valueOf(false));
                }
            }
        };
        String address = String.format(SOCIAL_USER_DISCONNECT, new Object[]{socialNetwork.toLowerCase()});
        executeAuthRequest(new NetworkTask(activity, 2, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
    }

    public static void connectFacebook(FragmentActivity activity, AccessToken accessToken, final Listener<Boolean> listener, ErrorListener errorListener) {
        if (accessToken != null) {
            HashMap<String, String> params = new HashMap();
            params.put("accessToken", accessToken.getToken());
            params.put("extendToken", "false");
            params.put(DBLikedChannelsHelper.KEY_ID, accessToken.getUserId());
            params.put("expires", String.valueOf(accessToken.getExpires().getTime()));
            FragmentActivity fragmentActivity = activity;
            executeAuthRequest(new NetworkTask(fragmentActivity, 1, isLoggedIn(), SOCIAL_REGISTER_FB_TKN, params, MAX_GET_RETRY_COUNT, new Listener<String>() {
                public void onResponse(String response) {
                    if (listener != null) {
                        listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                    }
                }
            }, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void getTwitterOAuthUrl(FragmentActivity activity, final Listener<String> listener, final ErrorListener errorListener) {
        Log.i(TAG, "getTwitterOAuthUrl", new Exception());
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, true, SOCIAL_TWITTER_OAUTH_URL, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null) {
                    try {
                        listener.onResponse(new JSONObject(response).getString("redirect"));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        if (errorListener != null) {
                            errorListener.onErrorResponse(new VolleyError(e));
                        } else {
                            listener.onResponse(null);
                        }
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void connectYoutube(FragmentActivity activity, String token, final Listener<Boolean> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(token)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    if (listener != null) {
                        listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                    }
                }
            };
            String address = String.format(SOCIAL_REGISTER_YOUTUBE_TKN, new Object[]{token});
            executeAuthRequest(new NetworkTask(activity, 2, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void shareBroadcastTo(FragmentActivity activity, String broadcastId, String shareTo, final Listener<Boolean> listener, ErrorListener errorListener) {
        Log.d(TAG, "shareBroadcastTo: " + shareTo);
        if (TextUtils.isEmpty(broadcastId)) {
            throw new IllegalArgumentException("The broadcastId parameter should be provided");
        } else if (TextUtils.isEmpty(shareTo)) {
            throw new IllegalArgumentException("The shareTo parameter should be provided");
        } else {
            HashMap<String, String> params = new HashMap();
            params.put("shareTo", shareTo.toLowerCase());
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    Log.d(Network.TAG, "shareBroadcastTo:" + response);
                    if (listener != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (Network.isResponseOK(jsonObject)) {
                                listener.onResponse(Boolean.valueOf(true));
                            } else if (TextUtils.equals(jsonObject.optString(Network.STATUS), "LOGIN")) {
                                listener.onResponse(null);
                            } else {
                                listener.onResponse(Boolean.valueOf(false));
                            }
                        } catch (Exception e) {
                            listener.onResponse(Boolean.valueOf(false));
                        }
                    }
                }
            };
            String address = String.format(SOCIAL_SHARE_BROADCAST, new Object[]{broadcastId});
            executeAuthRequest(new NetworkTask(activity, 2, isLoggedIn(), address, params, 0, respListener, listener, errorListener));
        }
    }

    public static void getFollowing(FragmentActivity activity, EntityType type, String oid, int pageIndex, int pageSize, final Listener<User[]> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(oid)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    if (listener != null) {
                        try {
                            listener.onResponse((User[]) new Gson().fromJson(response, User[].class));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(null);
                        }
                    }
                }
            };
            String address = String.format(FOLLOWING, new Object[]{type.toString(), oid, Integer.valueOf(pageIndex), Integer.valueOf(pageSize)});
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(new User[0]);
        }
    }

    public static void getFollowingSettings(FragmentActivity activity, int pageIndex, int pageSize, final Listener<User[]> listener, ErrorListener errorListener) {
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null) {
                    try {
                        listener.onResponse((User[]) new Gson().fromJson(response, User[].class));
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            }
        };
        String address = String.format(FOLLOWING_SETTINGS, new Object[]{Integer.valueOf(pageIndex), Integer.valueOf(pageSize)});
        executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
    }

    public static void setFollowingNotifyToggle(FragmentActivity activity, EntityType type, String oid, boolean notify, final Listener<Boolean> listener, ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        params.put("followedType", type.toString());
        params.put("oid", oid);
        params.put("notify", String.valueOf(notify));
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 2, isLoggedIn(), FOLLOWING_NOTIFY_TOGGLE, params, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null) {
                    try {
                        listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void getFeaturedSpotlightUsers(FragmentActivity activity, final Listener<User[]> listener, ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, isLoggedIn(), FEATURED_SPOTLIGHT_USERS, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "getFeaturedSpotlightUsers: " + response);
                if (listener != null) {
                    try {
                        listener.onResponse((User[]) new Gson().fromJson(response, User[].class));
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void getFeaturedTopUsers(FragmentActivity activity, final Listener<User[]> listener, ErrorListener errorListener) {
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 0, isLoggedIn(), FEATURED_TOP_USERS, null, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "getFeaturedTopUsers: " + response);
                if (listener != null) {
                    try {
                        listener.onResponse((User[]) new Gson().fromJson(response, User[].class));
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void getFollowers(FragmentActivity activity, EntityType type, String oid, int pageIndex, int pageSize, final Listener<User[]> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(oid)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    if (listener != null) {
                        try {
                            listener.onResponse((User[]) new Gson().fromJson(response, User[].class));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(null);
                        }
                    }
                }
            };
            String address = String.format(FOLLOWERS, new Object[]{type.toString(), oid, Integer.valueOf(pageIndex), Integer.valueOf(pageSize)});
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(new User[0]);
        }
    }

    public static void searchUsers(FragmentActivity activity, String filter, final Listener<User[]> listener, ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        String str = "regex";
        if (TextUtils.isEmpty(filter)) {
            filter = ".*";
        }
        params.put(str, filter);
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 1, isLoggedIn(), SEARCH_USERS, params, MAX_GET_RETRY_COUNT, new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null) {
                    try {
                        listener.onResponse((User[]) new Gson().fromJson(new JSONArray(response).toString(), User[].class));
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void searchFollowers(FragmentActivity activity, String filter, int pageIndex, int pageSize, final Listener<User[]> listener, ErrorListener errorListener) {
        Object obj;
        String str;
        HashMap<String, String> params = new HashMap();
        String str2 = "regex";
        if (TextUtils.isEmpty(filter)) {
            obj = ".*";
        } else {
            str = filter;
        }
        params.put(str2, obj);
        params.put("pageIndex", String.valueOf(pageIndex));
        params.put("pageSize", String.valueOf(pageSize));
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null) {
                    try {
                        listener.onResponse((User[]) new Gson().fromJson(new JSONObject(response).getJSONArray(Network.REGISTER).toString(), User[].class));
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            }
        };
        str = SEARCH_FOLLOWERS;
        Object[] objArr = new Object[MAX_GET_RETRY_COUNT];
        if (filter == null) {
            filter = ".*";
        }
        objArr[0] = Uri.parse(filter).toString();
        objArr[1] = Integer.valueOf(pageIndex);
        objArr[2] = Integer.valueOf(pageSize);
        String address = String.format(str, objArr);
        executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
    }

    public static void getChannelUsers(FragmentActivity activity, String channelId, final Listener<User[]> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(channelId)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    if (listener != null) {
                        try {
                            listener.onResponse((User[]) new Gson().fromJson(response, User[].class));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(null);
                        }
                    }
                }
            };
            String address = String.format(CHANNEL_USERS, new Object[]{channelId});
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(new User[0]);
        }
    }

    public static void getGameUsers(FragmentActivity activity, String gameId, final Listener<User[]> listener, ErrorListener errorListener) {
        if (!TextUtils.isEmpty(gameId)) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    if (listener != null) {
                        try {
                            listener.onResponse((User[]) new Gson().fromJson(response, User[].class));
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(null);
                        }
                    }
                }
            };
            String address = String.format(GAME_USERS, new Object[]{gameId});
            executeAuthRequest(new NetworkTask(activity, 0, isLoggedIn(), address, null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(new User[0]);
        }
    }

    public static void registerInBackground(final FragmentActivity activity) {
        AnonymousClass54 anonymousClass54 = new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                String errorMsg = "Error while registering device: ";
                try {
                    final String regId = InstanceID.getInstance(activity).getToken(Constants.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);
                    if (regId == null) {
                        Log.e(Network.TAG, "Error while registering device: regId is null");
                    } else if (Network.isLoggedIn()) {
                        Network.registerDevice(activity, regId, new Listener<Boolean>() {
                            public void onResponse(Boolean response) {
                                if (response == null || !response.booleanValue()) {
                                    Log.e(Network.TAG, "Error while registering device: no details");
                                    return;
                                }
                                Log.i(Network.TAG, "Device registered, registration ID=" + regId);
                                PreferenceUtility.storeRegistrationId(activity, regId);
                            }
                        }, new ErrorListener() {
                            public void onErrorResponse(VolleyError error) {
                                Log.e(Network.TAG, "Error while registering device: " + error.getMessage());
                            }
                        });
                    }
                } catch (Throwable ex) {
                    Log.e(Network.TAG, "Error while registering device: " + ex.getMessage());
                    if (!(TextUtils.equals(InstanceID.ERROR_SERVICE_NOT_AVAILABLE, ex.getMessage()) || TextUtils.equals("PHONE_REGISTRATION_ERROR", ex.getMessage()) || TextUtils.equals("AUTHENTICATION_FAILED", ex.getMessage()))) {
                        Crashlytics.logException(new Exception("Error while registering device: ", ex));
                    }
                }
                return null;
            }
        };
        Void[] voidArr = new Void[MAX_GET_RETRY_COUNT];
        voidArr[0] = null;
        voidArr[1] = null;
        voidArr[2] = null;
        anonymousClass54.execute(voidArr);
    }

    private static void registerDevice(FragmentActivity activity, String registrationId, final Listener<Boolean> listener, ErrorListener errorListener) {
        if (isLoggedIn() && !TextUtils.isEmpty(registrationId)) {
            HashMap<String, String> params = new HashMap();
            params.put("deviceToken", registrationId);
            params.put("deviceType", AbstractSpiCall.ANDROID_CLIENT_TYPE);
            FragmentActivity fragmentActivity = activity;
            executeAuthRequest(new NetworkTask(fragmentActivity, 2, true, REGISTER_DEVICE, params, MAX_GET_RETRY_COUNT, new Listener<String>() {
                public void onResponse(String response) {
                    try {
                        if (listener != null) {
                            listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            }, listener, errorListener));
        } else if (errorListener != null) {
            String str;
            if (TextUtils.isEmpty(registrationId)) {
                str = "registrationId is empty";
            } else {
                str = "No logged in user";
            }
            errorListener.onErrorResponse(new VolleyError(str));
        } else if (listener != null) {
            listener.onResponse(null);
        }
    }

    public static void checkIfFollower(FragmentActivity activity, EntityType type, String oid, final Listener<Boolean> listener, ErrorListener errorListener) {
        if (isLoggedIn()) {
            Listener<String> respListener = new Listener<String>() {
                public void onResponse(String response) {
                    if (listener != null) {
                        try {
                            JSONObject object = new JSONObject(response);
                            if (Network.isResponseOK(object)) {
                                listener.onResponse(Boolean.valueOf(object.optBoolean("exists", false)));
                            } else {
                                listener.onResponse(Boolean.valueOf(false));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            listener.onResponse(Boolean.valueOf(false));
                        }
                    }
                }
            };
            executeAuthRequest(new NetworkTask(activity, 0, true, String.format(CHECK_IF_FOLLOWER, new Object[]{type, oid}), null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
        } else if (listener != null) {
            listener.onResponse(Boolean.valueOf(false));
        }
    }

    public static void uploadPhoto(File image, final Listener<Boolean> listener, final ErrorListener errorListener) {
        PhotoMultipartRequest request = new PhotoMultipartRequest(buildAddress(PROFILE_PHOTO), image, new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null) {
                    try {
                        if (Network.isResponseOK(new JSONObject(response))) {
                            Network.getMyProfile(null, new Listener<User>() {
                                public void onResponse(User response) {
                                    listener.onResponse(Boolean.valueOf(true));
                                }
                            }, errorListener);
                            MixpanelHelper.getInstance(MainApplication.getContext()).generateEvent(Event.EDIT_PROFILE_PHOTO);
                            return;
                        }
                        listener.onResponse(Boolean.valueOf(false));
                    } catch (Exception e) {
                        e.printStackTrace();
                        listener.onResponse(null);
                    }
                }
            }
        }, errorListener) {
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap();
                headers.put(Network.AUTH_TOKEN_NAME, Network.buildAuthorizationString());
                headers.put(HTTP.USER_AGENT, "MobCrush-Android/" + Constants.APP_VERSION_NAME);
                return headers;
            }
        };
        request.setRetryPolicy(getRetryPolicy(0));
        mRQ.add(request);
    }

    public static void logout(boolean cancelCurrentTasks) {
        MixpanelHelper.getInstance(MainApplication.getContext()).generateLogOutEvent();
        if (cancelCurrentTasks) {
            mRQ.cancelAll(new RequestFilter() {
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
        PreferenceUtility.removeStreamKey();
        PreferenceUtility.removeAccessToken();
        PreferenceUtility.removeRefreshToken();
        PreferenceUtility.removeFirebaseToken();
        PreferenceUtility.removeExprirationDate();
        PreferenceUtility.removeEmailVerified();
        PreferenceUtility.removeUser();
        PreferenceUtility.removeAllPreferencies();
    }

    public static void executeAuthRequest(final NetworkTask task) {
        if (validateSession(task)) {
            final NetworkTask networkTask = task;
            NetworkRequest request = new NetworkRequest(task.getMethod(), buildAddress(task.getAddress()), task.getListener(), task) {
                protected Map<String, String> getParams() throws AuthFailureError {
                    return networkTask.getParams();
                }

                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap();
                    if (networkTask.getAddress().contains(Network.LOGIN_OR_REFRESH)) {
                        headers.put(Network.AUTH_TOKEN_NAME, Network.BASIC_AUTH_STRING);
                    } else if (Network.isLoggedIn() && networkTask.isNeedAuthorization()) {
                        headers.put(Network.AUTH_TOKEN_NAME, Network.buildAuthorizationString());
                    }
                    headers.put(HTTP.USER_AGENT, Network.getUserAgent());
                    return headers;
                }
            };
            if (task.getTag() != null) {
                request.setTag(task.getTag());
            }
            request.setRetryPolicy(getRetryPolicy(task.getMaxRetryCount()));
            request.setShouldCache(!CACHE_FILTER.contains(task.getAddress()));
            mRQ.add(request);
            return;
        }
        refreshToken(null, new Listener<Boolean>() {
            public void onResponse(Boolean response) {
                if (response != null && response.booleanValue()) {
                    Network.executeAuthRequest(task);
                } else if (task.getResultListener() != null) {
                    task.getResultListener().onResponse(null);
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                if (task.getErrorListener() != null) {
                    task.getErrorListener().onErrorResponse(error);
                } else if (task.getResultListener() != null) {
                    task.getResultListener().onResponse(null);
                }
            }
        });
    }

    private static RetryPolicy getRetryPolicy(int retryCount) {
        return new DefaultRetryPolicy(TIMEOUT_MS, retryCount, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT) {
            public void retry(VolleyError error) throws VolleyError {
                throw error;
            }
        };
    }

    private static boolean validateSession(NetworkTask task) {
        if (task.isNeedAuthorization()) {
            if (!isLoggedIn()) {
                FragmentActivity activity = task.getActivity();
                if (activity != null) {
                    activity.startActivity(LoginActivity.getIntent(activity));
                }
            } else if (NetworkLogic.isSessionExpired()) {
                NetworkLogic.onAuthRequired();
                return false;
            }
        }
        return true;
    }

    private static void updateAuthData(String response) {
        if (response != null) {
            try {
                JSONObject o = new JSONObject(response);
                PreferenceUtility.setAccessToken(o.optString(ACCESS_TOKEN));
                PreferenceUtility.setRefreshToken(o.optString("refresh_token"));
                PreferenceUtility.setExpirationDate((long) o.optInt(AccessToken.EXPIRES_IN_KEY, 0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getUserAgent() {
        return "MobCrush-Android/" + Constants.APP_VERSION_NAME;
    }

    public static String buildAddress(String path) {
        if (path != null && path.contains(HttpHost.DEFAULT_SCHEME_NAME)) {
            return path;
        }
        Object[] objArr = new Object[MAX_GET_RETRY_COUNT];
        objArr[0] = Constants.BASE_SCHEME;
        objArr[1] = Constants.BASE_ADDRESS;
        objArr[2] = path;
        return String.format("%s%s/%s", objArr);
    }

    private static String buildAuthorizationString() {
        return "Bearer " + PreferenceUtility.getAccessToken();
    }

    private static boolean isResponseOK(String response) {
        boolean z = false;
        if (response != null) {
            try {
                z = isResponseOK(new JSONObject(response));
            } catch (Exception e) {
                Exception e2 = new Exception("Error parsing response: " + response, e);
                e2.printStackTrace();
                Crashlytics.logException(e2);
            }
        }
        return z;
    }

    private static boolean isResponseOK(JSONObject object) {
        return STATUS_OK.equals(object.optString(STATUS, BuildConfig.FLAVOR));
    }

    public static void banUser(FragmentActivity activity, String channelId, String userId, String role, final Listener<Boolean> listener, final ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        params.put("roomId", channelId);
        params.put(ChatMessage.USER_ID, userId);
        params.put("bannerRole", role);
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 1, isLoggedIn(), USER_BAN, params, 0, new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null) {
                    try {
                        listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        if (errorListener != null) {
                            errorListener.onErrorResponse(new VolleyError(e));
                        } else {
                            listener.onResponse(null);
                        }
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void endBroadcast(FragmentActivity activity, String broadcastId, final Listener<Boolean> listener, final ErrorListener errorListener) {
        HashMap<String, String> params = new HashMap();
        params.put("broadcastId", broadcastId);
        FragmentActivity fragmentActivity = activity;
        executeAuthRequest(new NetworkTask(fragmentActivity, 1, false, END_BROADCAST, params, 0, new Listener<String>() {
            public void onResponse(String response) {
                if (listener != null) {
                    try {
                        listener.onResponse(Boolean.valueOf(Network.isResponseOK(response)));
                    } catch (Throwable e) {
                        e.printStackTrace();
                        if (errorListener != null) {
                            errorListener.onErrorResponse(new VolleyError(e));
                        } else {
                            listener.onResponse(Boolean.valueOf(false));
                        }
                    }
                }
            }
        }, listener, errorListener));
    }

    public static void latestBroadcast(FragmentActivity activity, String streamKey, final Listener<BroadcastData> listener, final ErrorListener errorListener) {
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "latestBroadcast: " + response);
                if (listener != null) {
                    try {
                        listener.onResponse((BroadcastData) new Gson().fromJson(response, BroadcastData.class));
                    } catch (Throwable e) {
                        Log.e(Network.TAG, "response: " + response, e);
                        Crashlytics.logException(new Exception("Error in latestBroadcast: " + response, e));
                        if (errorListener != null) {
                            errorListener.onErrorResponse(new VolleyError(e));
                        } else {
                            listener.onResponse(null);
                        }
                    }
                }
            }
        };
        executeAuthRequest(new NetworkTask(activity, 0, false, String.format(LATEST_BROADCAST, new Object[]{streamKey}), null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
    }

    public static void loadAdminListFromFirebase() {
        MainApplication.mFirebase.child(Constants.CHAT_MODERATORS).addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null) {
                    Constants.ADMINS_LIST.clear();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        Constants.ADMINS_LIST.add(child.getKey());
                    }
                }
            }

            public void onCancelled(FirebaseError firebaseError) {
            }
        });
    }

    public static void checkStreamDeviceApproval(FragmentActivity activity, final Listener<String> listener, final ErrorListener errorListener) {
        Listener<String> respListener = new Listener<String>() {
            public void onResponse(String response) {
                Log.d(Network.TAG, "latestBroadcast: " + response);
                if (listener != null) {
                    try {
                        listener.onResponse(new JSONObject(response).optString("approval"));
                    } catch (Throwable e) {
                        Log.e(Network.TAG, "response: " + response, e);
                        Crashlytics.logException(new Exception("Error in latestBroadcast: " + response, e));
                        if (errorListener != null) {
                            errorListener.onErrorResponse(new VolleyError(e));
                        } else {
                            listener.onResponse(null);
                        }
                    }
                }
            }
        };
        executeAuthRequest(new NetworkTask(activity, 0, false, String.format(STREAM_DEVICE_APPROVAL, new Object[]{Build.MODEL.replaceAll(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, "%20")}), null, MAX_GET_RETRY_COUNT, respListener, listener, errorListener));
    }
}
