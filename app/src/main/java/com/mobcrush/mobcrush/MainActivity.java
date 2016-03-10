package com.mobcrush.mobcrush;

import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.Toolbar.LayoutParams;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import com.android.volley.Response.Listener;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;
import com.helpshift.Helpshift;
import com.mobcrush.mobcrush.NavigationDrawerFragment.NavigationDrawerCallbacks;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.Channel;
import com.mobcrush.mobcrush.datamodel.Config;
import com.mobcrush.mobcrush.datamodel.Game;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import com.mobcrush.mobcrush.logic.GameLogicType;
import com.mobcrush.mobcrush.logic.NetworkLogic;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.mixpanel.Source;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.ViewPagerWithSwipeControl;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class MainActivity extends MobcrushActivty implements NavigationDrawerCallbacks, OnClickListener, OnPageChangeListener {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final int POSITION_ALL_GAMES = 3;
    public static final int POSITION_CHANNELS = 5;
    public static final int POSITION_GAME = 4;
    public static final int POSITION_PROFILE = 0;
    public static final int POSITION_TOURNAMENTS = 2;
    public static final int POSITION_WATCHES = 1;
    private static final String STATE_SELECTED_POSITION = "state_selected_position";
    private static final String TAG = "Mobcrush.MainActivity";
    private AllGamesFragment mAllGamesFragment;
    private BroadcastReceiver mBroadcastReceiver;
    private ChannelFragment mChannelFragment;
    private GoogleCloudMessaging mGCM;
    private GameFragment mGameFragment;
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private Integer mPagePositionToRestore;
    private PlayFragment mPlayFragment;
    private ProfileFragment mProfileFragment;
    private String mRegid;
    SectionsPagerAdapter mSectionsPagerAdapter;
    private boolean mShowChannels;
    private View mShutterView;
    private Boolean mSwitchWatchFeedToPopular = null;
    private View mTabView;
    private Toolbar mToolbar;
    private GameFragment mTournamentFragment;
    private ViewPagerWithSwipeControl mViewPager;
    private WatchesFragment mWatchesFragment;

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            switch (position) {
                case MainActivity.POSITION_PROFILE /*0*/:
                    MainActivity.this.mProfileFragment = ProfileFragment.newInstance(PreferenceUtility.getUser());
                    Log.i(MainActivity.TAG, "ProfileFragment created");
                    return MainActivity.this.mProfileFragment;
                case MainActivity.POSITION_WATCHES /*1*/:
                    MainActivity.this.mWatchesFragment = WatchesFragment.newInstance(MainActivity.this.mSwitchWatchFeedToPopular != null ? MainActivity.this.mSwitchWatchFeedToPopular.booleanValue() : false);
                    MainActivity.this.mSwitchWatchFeedToPopular = null;
                    Log.i(MainActivity.TAG, "WatchesFragment created");
                    return MainActivity.this.mWatchesFragment;
                case MainActivity.POSITION_TOURNAMENTS /*2*/:
                    if (MainActivity.this.mTournamentFragment == null) {
                        MainActivity.this.mTournamentFragment = GameFragment.newInstance(null, GameLogicType.Channel);
                    }
                    Log.i(MainActivity.TAG, "TournamentFragment created");
                    return MainActivity.this.mTournamentFragment;
                case MainActivity.POSITION_ALL_GAMES /*3*/:
                    MainActivity.this.mAllGamesFragment = AllGamesFragment.newInstance();
                    Log.i(MainActivity.TAG, "AllGamesFragment created");
                    return MainActivity.this.mAllGamesFragment;
                case MainActivity.POSITION_GAME /*4*/:
                    if (MainActivity.this.mGameFragment == null) {
                        MainActivity.this.mGameFragment = GameFragment.newInstance(null, GameLogicType.Game);
                    }
                    Log.i(MainActivity.TAG, "GameFragment created");
                    return MainActivity.this.mGameFragment;
                default:
                    return MainActivity.this.mChannelFragment;
            }
        }

        public int getCount() {
            int count = (!MainActivity.this.mShowChannels || MainActivity.this.mChannelFragment == null) ? MainActivity.POSITION_GAME : MainActivity.POSITION_CHANNELS;
            if (MainActivity.this.mGameFragment != null) {
                return count + MainActivity.POSITION_WATCHES;
            }
            return count;
        }

        public CharSequence getPageTitle(int position) {
            return BuildConfig.FLAVOR;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        MixpanelHelper.getInstance(this).generateAppOpenEvent();
        Helpshift.install(getApplication(), "2fa126c4b1ab85d53b68446dfc23e068", "mobcrush.helpshift.com", "mobcrush_platform_20150721074149497-0fd9656bf3bb07e");
        try {
            if (getSupportFragmentManager().getFragments() != null) {
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                for (Fragment f : getSupportFragmentManager().getFragments()) {
                    if (f != null && ((f instanceof GameFragment) || (f instanceof ChannelFragment))) {
                        fragmentTransaction.remove(f);
                    }
                }
                fragmentTransaction.commitAllowingStateLoss();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        this.mToolbar = (Toolbar) findViewById(R.id.toolbar);
        this.mToolbar.setLogo((int) R.drawable.ic_mc_logo);
        this.mToolbar.setBackgroundResource(R.color.dark);
        this.mToolbar.setNavigationIcon((int) R.drawable.ic_hamburger);
        try {
            setSupportActionBar(this.mToolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
            }
        } catch (Throwable e2) {
            e2.printStackTrace();
            Crashlytics.logException(e2);
        }
        configTabView(this.mToolbar);
        this.mShutterView = findViewById(R.id.shutter_view);
        this.mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        this.mViewPager = (ViewPagerWithSwipeControl) findViewById(R.id.pager);
        this.mViewPager.enableSwipe(false);
        this.mViewPager.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        this.mViewPager.setOffscreenPageLimit(7);
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        this.mViewPager.setCurrentItem(POSITION_WATCHES);
        this.mViewPager.setOnPageChangeListener(this);
        this.mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        this.mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
        if (checkPlayServices()) {
            this.mGCM = GoogleCloudMessaging.getInstance(this);
            this.mRegid = PreferenceUtility.getRegistrationId(getApplicationContext());
            if (this.mRegid == null || this.mRegid.isEmpty()) {
                Network.registerInBackground(this);
            } else {
                Log.i(TAG, "Device registered, registration ID=" + this.mRegid);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
        if (Constants.SHOW_WHATS_NEW) {
            showWhatsNew();
        }
    }

    private void checkForUpdate() {
        Config config = PreferenceUtility.getConfig();
        if (config != null && config.android != null) {
            boolean updateIsRequired;
            if (Utils.compareVersions(Constants.APP_VERSION_NAME, config.android.minVersion).intValue() < 0) {
                updateIsRequired = true;
            } else {
                updateIsRequired = false;
            }
            boolean updateIsDesired = false;
            if (!updateIsRequired) {
                if (Utils.compareVersions(Constants.APP_VERSION_NAME, config.android.currentVersion).intValue() < 0) {
                    updateIsDesired = true;
                } else {
                    updateIsDesired = false;
                }
            }
            if (updateIsDesired || updateIsRequired) {
                showUpdateDialog(updateIsRequired);
            }
        }
    }

    private void showUpdateDialog(boolean force) {
        int i;
        boolean z;
        Builder title = new Builder(this).setTitle(R.string.new_version_available_);
        if (force) {
            i = R.string.update_is_required;
        } else {
            i = R.string.update_is_desired;
        }
        title = title.setMessage(i).setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (Constants.PRODUCTION_PACKAGE.equals(MainActivity.this.getPackageName())) {
                    MainActivity.this.startPlayStoreToUpdate();
                } else {
                    Uri uri;
                    try {
                        uri = Uri.parse(PreferenceUtility.getConfig().android.downloadUrl);
                    } catch (Exception e) {
                        uri = Uri.parse(Constants.APP_DOWNLOAD_URL);
                    }
                    Intent intent = new Intent("android.intent.action.VIEW", uri);
                    intent.setFlags(268435456);
                    MainActivity.this.startActivity(intent);
                }
                MainActivity.this.finish();
            }
        });
        if (force) {
            z = false;
        } else {
            z = true;
        }
        Builder builder = title.setCancelable(z);
        if (!force) {
            builder.setNegativeButton(17039370, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        dialogInterface.dismiss();
                    } catch (Exception e) {
                    }
                }
            });
        }
        builder.create().show();
    }

    private void showMigrateDialog() {
        new Builder(this).setTitle(R.string.new_version_available_).setMessage(R.string.update_is_required).setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.startPlayStoreToUpdate();
                MainActivity.this.finish();
            }
        }).setCancelable(false).create().show();
    }

    private void startPlayStoreToUpdate() {
        Uri uri = Uri.parse(PreferenceUtility.getConfig().android.playLinkToUpdateAndroid);
        if (uri == null) {
            uri = Uri.parse("market://details?id=com.mobcrush.mobcrush");
        }
        try {
            startActivity(new Intent("android.intent.action.VIEW", uri));
        } catch (ActivityNotFoundException e) {
            Uri.parse(PreferenceUtility.getConfig().android.playLinktoUpdateWeb);
            if (uri == null) {
                uri = Uri.parse("https://play.google.com/store/apps/details?id=com.mobcrush.mobcrush");
            }
            startActivity(new Intent("android.intent.action.VIEW", uri));
        }
    }

    private void showWhatsNew() {
        if (Utils.compareVersions(Constants.APP_VERSION_NAME, PreferenceUtility.getShownWhatsNewVersion()).intValue() > 0) {
            PreferenceUtility.updateWhatsNewVersionToCurrent();
            Log.d(TAG, "showWhatsNew");
        }
    }

    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        this.mPagePositionToRestore = Integer.valueOf(savedInstanceState.getInt(STATE_SELECTED_POSITION, -1));
        if (this.mPagePositionToRestore.intValue() > -1) {
            this.mViewPager.setCurrentItem(this.mPagePositionToRestore.intValue());
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        int i = POSITION_WATCHES;
        super.onSaveInstanceState(outState);
        String str = STATE_SELECTED_POSITION;
        if (this.mViewPager.getCurrentItem() <= POSITION_WATCHES) {
            i = this.mViewPager.getCurrentItem();
        }
        outState.putInt(str, i);
    }

    public void onNavigationDrawerItemSelected(int position) {
        if (this.mViewPager != null && position >= 0) {
            switch (position) {
                case POSITION_PROFILE /*0*/:
                    if (!(this.mProfileFragment == null || !Network.isLoggedIn() || PreferenceUtility.getUser().equals(this.mProfileFragment.getUser()))) {
                        this.mProfileFragment.updateUserInfo(PreferenceUtility.getUser(), true, true);
                    }
                    if (this.mViewPager.getCurrentItem() != 0) {
                        this.mViewPager.setCurrentItem(POSITION_PROFILE);
                        return;
                    }
                    return;
                case POSITION_WATCHES /*1*/:
                    if (this.mViewPager.getCurrentItem() != POSITION_WATCHES) {
                        this.mViewPager.setCurrentItem(POSITION_WATCHES);
                        return;
                    } else if (this.mWatchesFragment != null) {
                        this.mWatchesFragment.refreshWatches();
                        return;
                    } else {
                        return;
                    }
                case POSITION_ALL_GAMES /*3*/:
                    if (this.mViewPager.getCurrentItem() != POSITION_ALL_GAMES) {
                        this.mViewPager.setCurrentItem(POSITION_ALL_GAMES);
                        return;
                    }
                    return;
                default:
                    Log.i(TAG, "onNavigationDrawerItemSelected: " + position);
                    if (this.mViewPager.getCurrentItem() != POSITION_WATCHES) {
                        this.mViewPager.setCurrentItem(POSITION_WATCHES);
                        return;
                    }
                    return;
            }
        }
    }

    public void onNavigationDrawerGamesUpdated(Game game) {
        if (this.mGameFragment == null) {
            this.mGameFragment = GameFragment.newInstance(game);
            this.mSectionsPagerAdapter.notifyDataSetChanged();
        }
    }

    public void onNavigationDrawerGameSelected(Game game) {
        if (this.mViewPager != null && game != null) {
            if (this.mGameFragment == null) {
                this.mGameFragment = GameFragment.newInstance(game);
                this.mSectionsPagerAdapter.notifyDataSetChanged();
            } else {
                this.mGameFragment.setGame(game);
            }
            if (this.mViewPager.getCurrentItem() != POSITION_GAME) {
                Log.d(TAG, "SCREEN_GAMES_PROMOTED: " + game);
                GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_GAMES_PROMOTED);
                this.mViewPager.setCurrentItem(POSITION_GAME);
            }
        }
    }

    public void onNavigationDrawerChannelSelected(UserChannel channel) {
        if (this.mViewPager != null && channel != null) {
            this.mShowChannels = true;
            boolean needUpdate = false;
            if (this.mGameFragment == null) {
                this.mGameFragment = GameFragment.newInstance(null, GameLogicType.Game);
                needUpdate = true;
            }
            if (this.mChannelFragment == null) {
                this.mChannelFragment = ChannelFragment.newInstance(channel);
                needUpdate = true;
            } else {
                this.mChannelFragment.setChannel(channel);
            }
            if (this.mViewPager.getCurrentItem() != POSITION_CHANNELS) {
                if (needUpdate) {
                    this.mSectionsPagerAdapter.notifyDataSetChanged();
                }
                Log.d(TAG, "SCREEN_TEAMS_PROMOTED: " + channel);
                GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_TEAMS_PROMOTED);
                this.mViewPager.setCurrentItem(POSITION_CHANNELS);
            }
        }
    }

    public void onNavigationDrawerTournamentSelected(Channel channel) {
        if (this.mViewPager != null && channel != null) {
            this.mShowChannels = true;
            this.mSectionsPagerAdapter.notifyDataSetChanged();
            if (this.mTournamentFragment == null) {
                this.mTournamentFragment = GameFragment.newInstance(channel);
                this.mSectionsPagerAdapter.notifyDataSetChanged();
            } else {
                this.mTournamentFragment.setChannel(channel);
            }
            if (this.mViewPager.getCurrentItem() != POSITION_TOURNAMENTS) {
                Log.d(TAG, "SCREEN_TOURNAMENTS_PROMOTED: " + channel);
                GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_TOURNAMENTS_PROMOTED);
                this.mViewPager.setCurrentItem(POSITION_TOURNAMENTS);
            }
        }
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        NetworkLogic.renewTokenIfRequired(this);
        loadIntentData(getIntent());
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    if (TextUtils.equals(intent.getAction(), Constants.EVENT_SIGNUP) || TextUtils.equals(intent.getAction(), Constants.EVENT_LOGIN)) {
                        MainActivity.this.mViewPager.setCurrentItem(MainActivity.POSITION_WATCHES);
                    } else if (TextUtils.equals(intent.getAction(), Constants.EVENT_APP_RESUMED)) {
                        MainActivity.this.mNavigationDrawerFragment.loadMenu();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(Constants.EVENT_SIGNUP);
        filter.addAction(Constants.EVENT_LOGIN);
        filter.addAction(Constants.EVENT_APP_RESUMED);
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mBroadcastReceiver, filter);
    }

    protected void onResume() {
        super.onResume();
        MixpanelHelper.getInstance(this).showSurveyIfAvailable(this);
        if (!(PreferenceUtility.getConfig().android.playForceUpdate && "com.mobcrush.mobcrush.dev".equals(getApplication().getPackageName())) && "com.mobcrush.mobcrush.stage".equals(getApplication().getPackageName())) {
        }
        if (this.mToolbar != null) {
            this.mToolbar.setBackgroundResource(R.color.dark);
            this.mToolbar.getBackground().setAlpha(SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        }
        checkPlayServices();
        if (System.currentTimeMillis() - PreferenceUtility.getConfigTimestamp() < Constants.CONFIG_LIFETIME) {
            Network.updateConfig(this, new Listener<Boolean>() {
                public void onResponse(Boolean response) {
                    if (response != null && response.booleanValue()) {
                        MainActivity.this.checkForUpdate();
                    }
                }
            }, null);
        }
        if (this.mSwitchWatchFeedToPopular == null || this.mWatchesFragment == null) {
            Log.d(TAG, "Intent: mSwitchWatchFeedToPopular:" + this.mSwitchWatchFeedToPopular + " ; mWatchesFragment:" + this.mWatchesFragment);
        } else {
            this.mWatchesFragment.switchToTab(this.mSwitchWatchFeedToPopular.booleanValue());
            this.mSwitchWatchFeedToPopular = null;
        }
        if (this.mNavigationDrawerFragment != null && this.mViewPager != null) {
            this.mNavigationDrawerFragment.setSelectedItem(this.mViewPager.getCurrentItem());
        }
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        ((NotificationManager) getSystemService("notification")).cancel(POSITION_WATCHES);
        if (this.mBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mBroadcastReceiver);
            this.mBroadcastReceiver = null;
        }
        MixpanelHelper.getInstance(this).flush();
        super.onDestroy();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.mShutterView.setVisibility(8);
    }

    public void setIntent(Intent intent) {
        if (intent == null || !Constants.ACTION_UPDATE_USER.equals(intent.getAction())) {
            if (intent == null || !Constants.ACTION_UPDATE_USER_BASE_INFO.equals(intent.getAction())) {
                if (intent == null || !Constants.ACTION_UPDATE_USER_PHOTO.equals(intent.getAction())) {
                    super.setIntent(intent);
                    return;
                }
                try {
                    Bitmap bitmap = BitmapFactory.decodeFile(intent.getStringExtra(Constants.EXTRA_PATH_TO_FILE));
                    if (!(this.mNavigationDrawerFragment == null || this.mNavigationDrawerFragment.mProfileLogo == null)) {
                        this.mNavigationDrawerFragment.mProfileLogo.setImageBitmap(bitmap);
                    }
                    if (this.mProfileFragment != null && this.mProfileFragment.mProfileLogo != null) {
                        this.mProfileFragment.mProfileLogo.setImageBitmap(bitmap);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            } else if (this.mProfileFragment != null) {
                this.mProfileFragment.updateUserBaseInfo(PreferenceUtility.getUser(), false);
            }
        } else if (Network.isLoggedIn()) {
            User user = PreferenceUtility.getUser();
            if (this.mProfileFragment != null) {
                Network.getUserProfile(this, user._id, new Listener<User>() {
                    public void onResponse(User response) {
                        if (response != null) {
                            PreferenceUtility.setUser(response.toString());
                            MainActivity.this.mProfileFragment.updateUserInfo(response, false, false);
                        }
                    }
                }, null);
            }
        } else {
            this.mShowChannels = false;
            try {
                this.mSectionsPagerAdapter.notifyDataSetChanged();
                this.mViewPager.setCurrentItem(POSITION_WATCHES);
            } catch (Exception e2) {
                e2.printStackTrace();
                Crashlytics.logException(e2);
            }
            if (this.mProfileFragment != null) {
                this.mProfileFragment.updateUserInfo(PreferenceUtility.getUser(), false, false);
            }
            if (this.mChannelFragment != null) {
                this.mChannelFragment.setChannel(null);
            }
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        loadIntentData(intent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            this.mNavigationDrawerFragment.openDrawer();
        } else if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPageScrolled(int i, float v, int i2) {
    }

    public void onPageSelected(int i) {
        boolean z;
        boolean z2 = true;
        this.mTabView.findViewById(R.id.iv_watch).setActivated(i == POSITION_WATCHES);
        View findViewById = this.mTabView.findViewById(R.id.iv_profile);
        if (i == 0) {
            z = true;
        } else {
            z = false;
        }
        findViewById.setActivated(z);
        switch (i) {
            case POSITION_PROFILE /*0*/:
            case POSITION_WATCHES /*1*/:
            case POSITION_ALL_GAMES /*3*/:
                String str = TAG;
                StringBuilder append = new StringBuilder().append("PROFILE: ");
                if (i != 0) {
                    z2 = false;
                }
                Log.i(str, append.append(z2).toString());
                try {
                    if (this.mNavigationDrawerFragment != null) {
                        this.mNavigationDrawerFragment.setSelectedItem(i);
                        return;
                    }
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            case POSITION_TOURNAMENTS /*2*/:
                if (this.mNavigationDrawerFragment != null && this.mGameFragment != null) {
                    this.mNavigationDrawerFragment.setSelectedItem(this.mTournamentFragment.getChannel());
                    return;
                }
                return;
            case POSITION_GAME /*4*/:
                if (this.mNavigationDrawerFragment != null && this.mGameFragment != null) {
                    this.mNavigationDrawerFragment.setSelectedItem(this.mGameFragment.getGame());
                    return;
                }
                return;
            case POSITION_CHANNELS /*5*/:
                if (this.mNavigationDrawerFragment != null && this.mChannelFragment != null) {
                    this.mNavigationDrawerFragment.setSelectedItem(this.mChannelFragment.getChannel());
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onPageScrollStateChanged(int i) {
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_watch:
                onNavigationDrawerItemSelected(POSITION_WATCHES);
                return;
            case R.id.iv_games:
                this.mViewPager.setCurrentItem(POSITION_WATCHES);
                return;
            case R.id.iv_profile:
                onNavigationDrawerItemSelected(POSITION_PROFILE);
                return;
            default:
                return;
        }
    }

    private void configTabView(Toolbar toolbar) {
        this.mTabView = LayoutInflater.from(toolbar.getContext()).inflate(R.layout.layout_tabs, toolbar, false);
        this.mTabView.setVisibility(8);
        LayoutParams layoutParams = new LayoutParams(-1, -2);
        layoutParams.gravity = GravityCompat.END;
        toolbar.addView(this.mTabView, layoutParams);
        this.mTabView.findViewById(R.id.iv_watch).setOnClickListener(this);
        this.mTabView.findViewById(R.id.iv_games).setOnClickListener(this);
        this.mTabView.findViewById(R.id.iv_profile).setOnClickListener(this);
        this.mTabView.findViewById(R.id.iv_watch).setActivated(true);
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == 0) {
            return true;
        }
        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
        } else {
            Log.i(TAG, "This device is not supported.");
            finish();
        }
        return false;
    }

    private void loadIntentData(Intent intent) {
        if (intent != null) {
            if ("android.intent.action.VIEW".equals(intent.getAction())) {
                String data = intent.getDataString();
                if (data != null) {
                    String[] params = data.replace(Constants.MOBCRUSH_SCHEME, BuildConfig.FLAVOR).split("/");
                    if (params.length > 0) {
                        this.mShutterView.setVisibility(POSITION_PROFILE);
                        if (params[POSITION_PROFILE].equalsIgnoreCase("broadcast")) {
                            Network.getBroadcast(this, params[POSITION_WATCHES], new Listener<Broadcast>() {
                                public void onResponse(Broadcast response) {
                                    if (response != null) {
                                        MainActivity.this.startActivityForResult(GameDetailsActivity.getIntent(MainActivity.this, response, Source.DEEPLINK), MainActivity.POSITION_PROFILE);
                                    }
                                }
                            }, null);
                            return;
                        } else if (params[POSITION_PROFILE].equalsIgnoreCase("feed")) {
                            this.mSwitchWatchFeedToPopular = Boolean.valueOf(TextUtils.equals("popularBroadcasts", params[POSITION_WATCHES]));
                            return;
                        } else if (params[POSITION_PROFILE].equalsIgnoreCase(DBLikedChannelsHelper.USER)) {
                            Network.getUserProfile(this, params[POSITION_WATCHES], new Listener<User>() {
                                public void onResponse(User response) {
                                    if (response != null) {
                                        MainActivity.this.startActivityForResult(ProfileActivity.getIntent(MainActivity.this, response), MainActivity.POSITION_PROFILE);
                                    }
                                }
                            }, null);
                            return;
                        } else {
                            return;
                        }
                    }
                    return;
                }
            }
            GcmIntentService.clearShownNotifications();
            Broadcast broadcast = null;
            try {
                String b = intent.getStringExtra(Constants.EXTRA_BROADCAST);
                if (b != null) {
                    broadcast = (Broadcast) new Gson().fromJson(b, Broadcast.class);
                }
            } catch (Exception e) {
                e.printStackTrace();
                broadcast = null;
            }
            if (broadcast == null) {
                User user = null;
                try {
                    String u = intent.getStringExtra(Constants.EXTRA_USER);
                    if (u != null) {
                        user = (User) new Gson().fromJson(u, User.class);
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                    user = null;
                }
                if (user != null) {
                    startActivity(ProfileActivity.getIntent(this, user));
                }
            } else if (TextUtils.isEmpty(broadcast._id)) {
                Crashlytics.logException(new IllegalArgumentException("Broadcast and broadcast._id can't be empty! Broadcast: " + broadcast));
                new Builder(this).setTitle(MainApplication.getRString(R.string.error_message_broadcast_is_no_longer_available, new Object[POSITION_PROFILE])).setPositiveButton(17039370, null).create().show();
            } else {
                startActivity(GameDetailsActivity.getIntent(this, broadcast, Source.DEEPLINK));
            }
        }
    }
}
