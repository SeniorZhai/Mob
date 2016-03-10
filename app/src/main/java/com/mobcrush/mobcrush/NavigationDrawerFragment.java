package com.mobcrush.mobcrush;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.Response.Listener;
import com.helpshift.Helpshift;
import com.mobcrush.mobcrush.broadcast.BroadcastActivity;
import com.mobcrush.mobcrush.broadcast.BroadcastService;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.datamodel.Channel;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.datamodel.MenuGroup;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.mobcrush.mobcrush.logic.MenuType;
import com.mobcrush.mobcrush.network.Network;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions.Builder;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.File;

public class NavigationDrawerFragment extends EditProfileFragment implements OnClickListener, OnMenuItemClickListener {
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    private static final int SEND_FEEDBACK = 3;
    public static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String TAG = "NavigationDrawer";
    private TextView mBroadcastItem;
    private NavigationDrawerCallbacks mCallbacks;
    private int mCurrentSelectedPosition = 0;
    private DisplayImageOptions mDio = new Builder().cacheOnDisk(true).cacheInMemory(true).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).showImageForEmptyUri((int) R.drawable.default_profile_pic).showImageOnFail((int) R.drawable.default_profile_pic).build();
    private DisplayImageOptions mDioChannes;
    private DisplayImageOptions mDioGames;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private View mFragmentContainerView;
    private boolean mFromSavedInstanceState;
    private View mGamesItem;
    private LinearLayout mGamesLayout;
    private TextView mLandingItem;
    private LinearLayout mMenuItemsLayout;
    private View mMenuScroll;
    private TextView mProfileName;
    private View mProfileOptions;
    private View mProfileView;
    private TextView mSearchUsersItem;
    private LinearLayout mTeamsLayout;
    private LinearLayout mTournamentsLayout;
    private User mUser;
    private boolean mUserLearnedDrawer;
    private File mUserPhotoFile;
    private int mUserPhotoOrientation = 0;
    private LinearLayout mUsersFeedItem;
    private TextView mWatchFeedItem;

    public interface NavigationDrawerCallbacks {
        void onNavigationDrawerChannelSelected(UserChannel userChannel);

        void onNavigationDrawerGameSelected(Game game);

        void onNavigationDrawerGamesUpdated(Game game);

        void onNavigationDrawerItemSelected(int i);

        void onNavigationDrawerTournamentSelected(Channel channel);
    }

    static /* synthetic */ class AnonymousClass10 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$MenuType = new int[MenuType.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.divider.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.spacer.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.searchUsers.ordinal()] = NavigationDrawerFragment.SEND_FEEDBACK;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.tabbedBroadcastFeed.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.startBroadcasting.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.listUsers.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.user.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.listGames.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.game.ordinal()] = 9;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.listTeams.ordinal()] = 10;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.team.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.listTournaments.ordinal()] = 12;
            } catch (NoSuchFieldError e12) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$MenuType[MenuType.tournament.ordinal()] = 13;
            } catch (NoSuchFieldError e13) {
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        this.mUserLearnedDrawer = true;
        if (savedInstanceState != null) {
            this.mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            Log.i(TAG, "onCreate.Position: " + this.mCurrentSelectedPosition);
            this.mFromSavedInstanceState = true;
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        this.mMenuScroll = root.findViewById(R.id.scroll_view);
        this.mMenuItemsLayout = (LinearLayout) root.findViewById(R.id.layout_menu_items);
        this.mProfileView = root.findViewById(R.id.profile_layout);
        ViewCompat.setElevation(this.mProfileView, (float) getResources().getDimensionPixelSize(R.dimen.headers_elevation));
        this.mProfileView.setOnClickListener(this);
        this.mProfileLogo = (ImageView) root.findViewById(R.id.profile_logo);
        this.mProfileName = (TextView) root.findViewById(R.id.profile_name);
        this.mProfileOptions = root.findViewById(R.id.profile_options);
        this.mProfileOptions.setOnClickListener(this);
        this.mLandingItem = (TextView) root.findViewById(R.id.landing_item);
        this.mLandingItem.setOnClickListener(this);
        this.mWatchFeedItem = (TextView) root.findViewById(R.id.watch_feed_item);
        this.mWatchFeedItem.setActivated(true);
        this.mWatchFeedItem.setOnClickListener(this);
        this.mTournamentsLayout = (LinearLayout) root.findViewById(R.id.tournaments_layout);
        this.mGamesItem = root.findViewById(R.id.games_item);
        this.mGamesItem.setOnClickListener(this);
        this.mGamesLayout = (LinearLayout) root.findViewById(R.id.games_layout);
        this.mTeamsLayout = (LinearLayout) root.findViewById(R.id.teams_layout);
        this.mDioGames = new Builder().cacheOnDisk(true).cacheInMemory(true).displayer(new RoundedBitmapDisplayer(getResources().getDimensionPixelSize(R.dimen.navigation_games_corner))).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
        this.mDioChannes = new Builder().cacheOnDisk(true).cacheInMemory(true).displayer(new RoundedBitmapDisplayer(getResources().getDimensionPixelSize(R.dimen.navigation_teams_corner))).imageScaleType(ImageScaleType.IN_SAMPLE_POWER_OF_2).build();
        ((TextView) root.findViewById(R.id.app_version_tv)).setText(Utils.getAppVersionString());
        return root;
    }

    public void openDrawer() {
        UIUtils.hideVirtualKeyboard(getActivity());
        this.mDrawerLayout.openDrawer(this.mFragmentContainerView);
    }

    public void closeDrawer() {
        this.mDrawerLayout.closeDrawer(this.mFragmentContainerView);
    }

    public void onClick(View v) {
        int position = -1;
        switch (v.getId()) {
            case R.id.games_item:
                position = SEND_FEEDBACK;
                break;
            case R.id.users_item:
                v = null;
                startActivity(FeaturedBroadcastersActivity.getIntent(getActivity()));
                break;
            case R.id.landing_item:
                position = 0;
                break;
            case R.id.broadcast_item:
                startActivity(BroadcastActivity.getIntent(getActivity()));
                break;
            case R.id.search_users_item:
                v = null;
                startActivity(SearchUsersActivity.getIntent(getActivity()));
                break;
            case R.id.watch_feed_item:
                position = 1;
                break;
            case R.id.profile_layout:
                position = 0;
                break;
            case R.id.profile_options:
                PopupMenu popup = new PopupMenu(getActivity(), v);
                popup.getMenuInflater().inflate(PreferenceUtility.isEmailVerified() ? R.menu.menu_profile_more : R.menu.menu_profile_more_for_unverified, popup.getMenu());
                popup.setOnMenuItemClickListener(this);
                popup.show();
                return;
            default:
                v = null;
                break;
        }
        if (v != null) {
            setActivatedNavigationElement((ViewGroup) getView(), v);
            if (this.mCallbacks != null) {
                Log.d(TAG, "onNavigationDrawerItemSelected");
                this.mCallbacks.onNavigationDrawerItemSelected(position);
            }
        }
        closeDrawer();
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        this.mFragmentContainerView = getActivity().findViewById(fragmentId);
        this.mDrawerLayout = drawerLayout;
        this.mDrawerLayout.setDrawerShadow((int) R.drawable.drawer_shadow, (int) GravityCompat.START);
        this.mDrawerToggle = new ActionBarDrawerToggle(getActivity(), this.mDrawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (NavigationDrawerFragment.this.isAdded()) {
                    NavigationDrawerFragment.this.getActivity().supportInvalidateOptionsMenu();
                }
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (NavigationDrawerFragment.this.isAdded()) {
                    if (!NavigationDrawerFragment.this.mUserLearnedDrawer) {
                        NavigationDrawerFragment.this.mUserLearnedDrawer = true;
                        PreferenceManager.getDefaultSharedPreferences(NavigationDrawerFragment.this.getActivity()).edit().putBoolean(NavigationDrawerFragment.PREF_USER_LEARNED_DRAWER, true).apply();
                    }
                    NavigationDrawerFragment.this.getActivity().supportInvalidateOptionsMenu();
                }
            }
        };
        if (!(this.mUserLearnedDrawer || this.mFromSavedInstanceState)) {
            this.mDrawerLayout.openDrawer(this.mFragmentContainerView);
        }
        this.mDrawerLayout.post(new Runnable() {
            public void run() {
                NavigationDrawerFragment.this.mDrawerToggle.syncState();
            }
        });
        this.mDrawerLayout.setDrawerListener(this.mDrawerToggle);
    }

    public void setSelectedItem(int position) {
        this.mCurrentSelectedPosition = position;
        View v = null;
        switch (position) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                v = this.mLandingItem;
                break;
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                v = this.mWatchFeedItem;
                break;
            case SEND_FEEDBACK /*3*/:
                v = this.mGamesItem;
                break;
        }
        if (v != null) {
            setActivatedNavigationElement((ViewGroup) getView(), v);
        }
    }

    public void setSelectedItem(UserChannel userChannel) {
        if (userChannel != null && userChannel.channel != null && userChannel.channel._id != null) {
            View v = this.mTeamsLayout.findViewById(Math.abs(userChannel.channel._id.hashCode()));
            if (v != null) {
                setActivatedNavigationElement((ViewGroup) getView(), v);
            }
        }
    }

    public void setSelectedItem(Game game) {
        if (game != null && game._id != null) {
            View v = this.mGamesLayout.findViewById(Math.abs(game._id.hashCode()));
            if (v != null) {
                setActivatedNavigationElement((ViewGroup) getView(), v);
            }
        }
    }

    public void setSelectedItem(Channel channel) {
        if (channel != null && channel._id != null) {
            View v = this.mTournamentsLayout.findViewById(Math.abs(channel._id.hashCode()));
            if (v != null) {
                setActivatedNavigationElement((ViewGroup) getView(), v);
            }
        }
    }

    public void onResume() {
        super.onResume();
        updateData();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    public void onDetach() {
        super.onDetach();
        this.mCallbacks = null;
    }

    public void onSaveInstanceState(Bundle outState) {
        int i = 1;
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, this.mCurrentSelectedPosition > 1 ? 1 : this.mCurrentSelectedPosition);
        String str = TAG;
        StringBuilder append = new StringBuilder().append("onSaveInstanceState.Position: ");
        if (this.mCurrentSelectedPosition <= 1) {
            i = this.mCurrentSelectedPosition;
        }
        Log.i(str, append.append(i).toString());
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onMenuItemClick(MenuItem item) {
        if (super.onMenuItemClick(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_about:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            case R.id.action_settings:
                startActivity(SettingsActivity.getIntent(getActivity()));
                closeDrawer();
                return true;
            case R.id.action_liked_video:
                if (!Network.isLoggedIn()) {
                    return true;
                }
                startActivity(LikedVideosActivity.getIntent(getActivity(), PreferenceUtility.getUser()));
                closeDrawer();
                return true;
            case R.id.action_send_feedback:
                Object[] objArr = new Object[SEND_FEEDBACK];
                objArr[0] = Constants.FEEDBACK_EMAIL;
                objArr[1] = Uri.encode(MainApplication.getRString(R.string.android_feedback, new Object[0]));
                objArr[2] = Uri.encode(MainApplication.getRString(R.string.feedback_summary_info, PreferenceUtility.getUser().username, Build.MODEL, Integer.valueOf(VERSION.SDK_INT), Constants.APP_VERSION_NAME));
                Intent intent = new Intent("android.intent.action.SENDTO", Uri.parse(String.format("mailto:%s?subject=%s&body=%s", objArr)));
                intent.addFlags(268435456);
                try {
                    startActivityForResult(Intent.createChooser(intent, MainApplication.getRString(R.string.send_feedback_by__, new Object[0])), SEND_FEEDBACK);
                    break;
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getActivity(), MainApplication.getRString(R.string.error_no_email_clients_installed, new Object[0]), 0).show();
                    break;
                }
            case R.id.action_help:
                Helpshift.showFAQs(getActivity());
                break;
            case R.id.action_logout:
                GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_ACCOUNT, Constants.ACTION_LOGOUT);
                Network.logout(true);
                getActivity().stopService(new Intent(getActivity(), BroadcastService.class));
                getActivity().setIntent(new Intent(Constants.ACTION_UPDATE_USER));
                updateData();
                closeDrawer();
                return true;
        }
        return false;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEND_FEEDBACK) {
            UIUtils.hideVirtualKeyboard(getActivity());
        }
    }

    public void loadMenu() {
        if (isAdded() && this.mMenuItemsLayout != null) {
            Network.getMenu(getActivity(), new Listener<MenuGroup[]>() {
                public void onResponse(MenuGroup[] menuGroups) {
                    if (menuGroups != null) {
                        NavigationDrawerFragment.this.buildMenu(menuGroups);
                    }
                }
            }, null);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void buildMenu(com.mobcrush.mobcrush.datamodel.MenuGroup[] r8) {
        /*
        r7 = this;
        r5 = r7.mMenuItemsLayout;
        r5.removeAllViews();
        r0 = r8;
        r3 = r0.length;
        r2 = 0;
    L_0x0008:
        if (r2 >= r3) goto L_0x0049;
    L_0x000a:
        r4 = r0[r2];
        r5 = r4.type;	 Catch:{ Exception -> 0x0025 }
        if (r5 != 0) goto L_0x0013;
    L_0x0010:
        r2 = r2 + 1;
        goto L_0x0008;
    L_0x0013:
        r5 = com.mobcrush.mobcrush.NavigationDrawerFragment.AnonymousClass10.$SwitchMap$com$mobcrush$mobcrush$logic$MenuType;	 Catch:{ Exception -> 0x0025 }
        r6 = r4.type;	 Catch:{ Exception -> 0x0025 }
        r6 = r6.ordinal();	 Catch:{ Exception -> 0x0025 }
        r5 = r5[r6];	 Catch:{ Exception -> 0x0025 }
        switch(r5) {
            case 1: goto L_0x0021;
            case 2: goto L_0x0010;
            case 3: goto L_0x002d;
            case 4: goto L_0x0031;
            case 5: goto L_0x0035;
            case 6: goto L_0x0039;
            case 7: goto L_0x0010;
            case 8: goto L_0x003d;
            case 9: goto L_0x0010;
            case 10: goto L_0x0041;
            case 11: goto L_0x0010;
            case 12: goto L_0x0045;
            default: goto L_0x0020;
        };	 Catch:{ Exception -> 0x0025 }
    L_0x0020:
        goto L_0x0010;
    L_0x0021:
        r7.addDivider();	 Catch:{ Exception -> 0x0025 }
        goto L_0x0010;
    L_0x0025:
        r1 = move-exception;
        r1.printStackTrace();
        com.crashlytics.android.Crashlytics.logException(r1);
        goto L_0x0010;
    L_0x002d:
        r7.addSearchUsers(r4);	 Catch:{ Exception -> 0x0025 }
        goto L_0x0010;
    L_0x0031:
        r7.addBroadcastFeed(r4);	 Catch:{ Exception -> 0x0025 }
        goto L_0x0010;
    L_0x0035:
        r7.addBroadcast(r4);	 Catch:{ Exception -> 0x0025 }
        goto L_0x0010;
    L_0x0039:
        r7.addUsers(r4);	 Catch:{ Exception -> 0x0025 }
        goto L_0x0010;
    L_0x003d:
        r7.addGames(r4);	 Catch:{ Exception -> 0x0025 }
        goto L_0x0010;
    L_0x0041:
        r7.addTeams(r4);	 Catch:{ Exception -> 0x0025 }
        goto L_0x0010;
    L_0x0045:
        r7.addTournaments(r4);	 Catch:{ Exception -> 0x0025 }
        goto L_0x0010;
    L_0x0049:
        r5 = r7.mCurrentSelectedPosition;
        r7.setSelectedItem(r5);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mobcrush.mobcrush.NavigationDrawerFragment.buildMenu(com.mobcrush.mobcrush.datamodel.MenuGroup[]):void");
    }

    public void updateData() {
        if (!isAdded()) {
            return;
        }
        if (!PreferenceUtility.getUser().equals(this.mUser)) {
            this.mUser = PreferenceUtility.getUser();
            if (Network.isLoggedIn()) {
                this.mLandingItem.setVisibility(8);
                this.mProfileView.setVisibility(0);
                this.mProfileName.setText(this.mUser.username);
                if (TextUtils.isEmpty(this.mUser.profileLogo)) {
                    this.mProfileLogo.setImageResource(R.drawable.default_profile_pic);
                } else {
                    ImageLoader.getInstance().displayImage(this.mUser.profileLogo, this.mProfileLogo, this.mDio);
                }
                this.mMenuScroll.setPadding(0, getResources().getDimensionPixelSize(R.dimen.navigation_menu_logo_height), 0, this.mMenuScroll.getPaddingBottom());
            } else {
                this.mMenuScroll.setPadding(0, 0, 0, this.mMenuScroll.getPaddingBottom());
                this.mProfileView.setVisibility(8);
                this.mLandingItem.setVisibility(0);
            }
            loadMenu();
        } else if (this.mUser != null && !TextUtils.equals(this.mUser.profileLogo, PreferenceUtility.getUser().profileLogo)) {
            ImageLoader.getInstance().displayImage(PreferenceUtility.getUser().profileLogo, this.mProfileLogo, this.mDio);
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void addDivider() {
        if (isAdded()) {
            LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation_divider, this.mMenuItemsLayout, true);
        }
    }

    private void addSpacer(MenuGroup m) {
        if (m.height != null) {
            View view = new View(getActivity());
            view.setLayoutParams(new LayoutParams(-1, (int) TypedValue.applyDimension(1, Float.valueOf(m.height).floatValue(), getActivity().getResources().getDisplayMetrics())));
            this.mMenuItemsLayout.addView(view);
        }
    }

    private void addSearchUsers(MenuGroup m) {
        if (isAdded()) {
            this.mSearchUsersItem = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation_item, this.mMenuItemsLayout, false);
            this.mSearchUsersItem.setId(R.id.search_users_item);
            this.mSearchUsersItem.setText(m.description);
            this.mSearchUsersItem.setActivated(false);
            this.mSearchUsersItem.setOnClickListener(this);
            addStateListDrawable(this.mSearchUsersItem, m);
            this.mMenuItemsLayout.addView(this.mSearchUsersItem);
        }
    }

    private void addBroadcast(MenuGroup m) {
        if (isAdded()) {
            this.mBroadcastItem = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation_item, this.mMenuItemsLayout, false);
            this.mBroadcastItem.setId(R.id.broadcast_item);
            this.mBroadcastItem.setText(m.description);
            this.mBroadcastItem.setActivated(false);
            this.mBroadcastItem.setOnClickListener(this);
            if (Network.isLoggedIn() && PreferenceUtility.isEmailVerified()) {
                this.mBroadcastItem.setVisibility(0);
            } else {
                this.mBroadcastItem.setVisibility(8);
            }
            addStateListDrawable(this.mBroadcastItem, m);
            this.mMenuItemsLayout.addView(this.mBroadcastItem);
        }
    }

    private void addBroadcastFeed(MenuGroup m) {
        if (isAdded()) {
            this.mWatchFeedItem = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation_item, this.mMenuItemsLayout, false);
            this.mWatchFeedItem.setId(R.id.watch_feed_item);
            this.mWatchFeedItem.setText(m.description);
            this.mWatchFeedItem.setActivated(false);
            this.mWatchFeedItem.setOnClickListener(this);
            addStateListDrawable(this.mWatchFeedItem, m);
            this.mMenuItemsLayout.addView(this.mWatchFeedItem);
        }
    }

    private void addUsers(MenuGroup m) {
        if (isAdded()) {
            this.mUsersFeedItem = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation_w_all, this.mMenuItemsLayout, false);
            this.mUsersFeedItem.setId(R.id.users_item);
            ((TextView) this.mUsersFeedItem.findViewById(R.id.title)).setText(m.description);
            this.mUsersFeedItem.setOnClickListener(this);
            this.mMenuItemsLayout.addView(this.mUsersFeedItem);
            for (com.mobcrush.mobcrush.datamodel.MenuItem mi : m.contains) {
                addUser(mi);
            }
        }
    }

    private void addUser(com.mobcrush.mobcrush.datamodel.MenuItem mi) {
        if (isAdded() && mi != null && MenuType.user.equals(mi.type)) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation, this.mMenuItemsLayout, false);
            if (mi.parameters != null && mi.parameters.length > 0) {
                v.setId(Math.abs(mi.parameters[0].hashCode()));
            }
            User user = new User();
            user._id = mi.parameters[0];
            user.username = mi.description;
            user.profileLogo = mi.image;
            user.broadcastCount = null;
            user.followerCount = null;
            user.followingCount = null;
            v.setTag(user);
            v.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (NavigationDrawerFragment.this.mCallbacks != null) {
                        Log.d(NavigationDrawerFragment.TAG, "OnClickUser " + v.getTag());
                        Log.d(NavigationDrawerFragment.TAG, "SCREEN_FEATURED_PARTNER: " + v.getTag());
                        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_FEATURED_PARTNER);
                        NavigationDrawerFragment.this.startActivity(ProfileActivity.getIntent(NavigationDrawerFragment.this.getActivity(), (User) v.getTag()));
                    }
                    NavigationDrawerFragment.this.closeDrawer();
                }
            });
            ((TextView) v.findViewById(R.id.item_title)).setText(mi.description);
            if (!TextUtils.isEmpty(mi.image)) {
                ImageLoader.getInstance().displayImage(mi.image, (ImageView) v.findViewById(R.id.item_icon), this.mDioChannes);
            }
            this.mMenuItemsLayout.addView(v);
        }
    }

    private void addGames(MenuGroup m) {
        if (isAdded()) {
            this.mGamesItem = LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation_w_all, this.mMenuItemsLayout, false);
            this.mGamesItem.setId(R.id.games_item);
            ((TextView) this.mGamesItem.findViewById(R.id.title)).setText(m.description);
            this.mGamesItem.setOnClickListener(this);
            this.mMenuItemsLayout.addView(this.mGamesItem);
            for (com.mobcrush.mobcrush.datamodel.MenuItem mi : m.contains) {
                addGame(new Game(mi.parameters[0], mi.description, mi.image, true, Integer.valueOf(0), null));
            }
        }
    }

    private void addGame(Game game) {
        if (isAdded() && this.mMenuItemsLayout != null && game != null) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation, this.mMenuItemsLayout, false);
            if (game._id != null) {
                v.setId(Math.abs(game._id.hashCode()));
            }
            v.setTag(game);
            v.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (NavigationDrawerFragment.this.mCallbacks != null) {
                        Log.d(NavigationDrawerFragment.TAG, "OnClickGame " + v.getTag());
                        NavigationDrawerFragment.this.mCallbacks.onNavigationDrawerGameSelected((Game) v.getTag());
                    }
                    NavigationDrawerFragment.this.closeDrawer();
                    NavigationDrawerFragment.this.setActivatedNavigationElement((ViewGroup) NavigationDrawerFragment.this.getView(), v);
                }
            });
            ((TextView) v.findViewById(R.id.item_title)).setText(game.name);
            if (!TextUtils.isEmpty(game.icon)) {
                ImageLoader.getInstance().displayImage(game.icon, (ImageView) v.findViewById(R.id.item_icon), this.mDioGames);
            }
            this.mMenuItemsLayout.addView(v);
        }
    }

    private void addTournaments(MenuGroup m) {
        if (isAdded()) {
            TextView tv = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation_item, this.mMenuItemsLayout, false);
            tv.setText(m.description);
            tv.setOnClickListener(this);
            this.mMenuItemsLayout.addView(tv);
            for (com.mobcrush.mobcrush.datamodel.MenuItem mi : m.contains) {
                Log.d(TAG, "addTournament: " + m);
                addTournament(new Channel(mi.parameters[0], mi.description, mi.image, null, null, Integer.valueOf(0)));
            }
        }
    }

    private void addTournament(Channel channel) {
        if (isAdded() && this.mMenuItemsLayout != null && channel != null) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation, this.mMenuItemsLayout, false);
            if (channel._id != null) {
                v.setId(Math.abs(channel._id.hashCode()));
            }
            v.setTag(channel);
            v.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (NavigationDrawerFragment.this.mCallbacks != null) {
                        NavigationDrawerFragment.this.mCallbacks.onNavigationDrawerTournamentSelected((Channel) v.getTag());
                    }
                    NavigationDrawerFragment.this.closeDrawer();
                    NavigationDrawerFragment.this.setActivatedNavigationElement((ViewGroup) NavigationDrawerFragment.this.getView(), v);
                }
            });
            ((TextView) v.findViewById(R.id.item_title)).setText(channel.name);
            if (!TextUtils.isEmpty(channel.channelLogo)) {
                ImageLoader.getInstance().displayImage(channel.channelLogo, (ImageView) v.findViewById(R.id.item_icon), this.mDioChannes);
            }
            this.mMenuItemsLayout.addView(v);
        }
    }

    private void addTeams(MenuGroup m) {
        if (isAdded()) {
            TextView tv = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation_item, this.mMenuItemsLayout, false);
            tv.setText(m.description);
            tv.setOnClickListener(this);
            this.mMenuItemsLayout.addView(tv);
            for (com.mobcrush.mobcrush.datamodel.MenuItem mi : m.contains) {
                addTeam(new UserChannel(new Channel(mi.parameters[0], mi.description, mi.image, null, null, Integer.valueOf(0))));
            }
        }
    }

    private void addTeam(UserChannel userChannel) {
        if (isAdded() && this.mMenuItemsLayout != null && userChannel != null) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_navigation, this.mTeamsLayout, false);
            if (!(userChannel.channel == null || userChannel.channel._id == null)) {
                v.setId(Math.abs(userChannel.channel._id.hashCode()));
            }
            v.setTag(userChannel);
            v.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (NavigationDrawerFragment.this.mCallbacks != null) {
                        Log.d(NavigationDrawerFragment.TAG, "onNavigationDrawerChannelSelected");
                        NavigationDrawerFragment.this.mCallbacks.onNavigationDrawerChannelSelected((UserChannel) v.getTag());
                    }
                    NavigationDrawerFragment.this.closeDrawer();
                    NavigationDrawerFragment.this.setActivatedNavigationElement((ViewGroup) NavigationDrawerFragment.this.getView(), v);
                }
            });
            ((TextView) v.findViewById(R.id.item_title)).setText(userChannel.channel.name);
            if (!TextUtils.isEmpty(userChannel.channel.channelLogo)) {
                ImageLoader.getInstance().displayImage(userChannel.channel.channelLogo, (ImageView) v.findViewById(R.id.item_icon), this.mDioChannes);
            }
            this.mMenuItemsLayout.addView(v);
        }
    }

    private void setActivatedNavigationElement(ViewGroup root, View element) {
        if (root != null) {
            for (int i = 0; i < root.getChildCount(); i++) {
                View v = root.getChildAt(i);
                if (v != null) {
                    if (v == element) {
                        try {
                            v.setActivated(true);
                        } catch (Exception e) {
                        }
                    } else {
                        v.setActivated(false);
                        if (v instanceof ViewGroup) {
                            setActivatedNavigationElement((ViewGroup) v, element);
                        }
                    }
                }
            }
        }
    }

    private void addStateListDrawable(final TextView v, MenuGroup m) {
        if (v != null && m != null) {
            final ImageSize imageSize = new ImageSize(getResources().getDimensionPixelSize(R.dimen.navigation_icon_size), getResources().getDimensionPixelSize(R.dimen.navigation_icon_size));
            final StateListDrawable states = new StateListDrawable();
            if (!TextUtils.isEmpty(m.image)) {
                ImageLoader.getInstance().loadImage(m.image, imageSize, this.mDio, new SimpleImageLoadingListener() {
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (NavigationDrawerFragment.this.isAdded()) {
                            states.addState(new int[]{-16843518}, new BitmapDrawable(NavigationDrawerFragment.this.getResources(), Bitmap.createScaledBitmap(loadedImage, imageSize.getWidth(), imageSize.getHeight(), true)));
                            v.setCompoundDrawablesWithIntrinsicBounds(states, null, null, null);
                        }
                    }
                });
            }
            if (!TextUtils.isEmpty(m.image_active)) {
                ImageLoader.getInstance().loadImage(m.image_active, imageSize, this.mDio, new SimpleImageLoadingListener() {
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        if (NavigationDrawerFragment.this.isAdded()) {
                            states.addState(new int[]{16843518}, new BitmapDrawable(NavigationDrawerFragment.this.getResources(), Bitmap.createScaledBitmap(loadedImage, imageSize.getWidth(), imageSize.getHeight(), true)));
                            v.setCompoundDrawablesWithIntrinsicBounds(states, null, null, null);
                        }
                    }
                });
            }
        }
    }
}
