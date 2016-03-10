package com.mobcrush.mobcrush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.helper.ModerationHelper;
import com.mobcrush.mobcrush.network.Network;
import com.mobcrush.mobcrush.ui.SlidingTabLayout;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.ResponseParser;

public class ChannelModerationActivity extends MobcrushActivty implements OnClickListener {
    private static final int POS_BANNED = 4;
    private static final int POS_IGNORED = 3;
    private static final int POS_MODS = 1;
    private static final int POS_MUTED = 2;
    private static final int POS_VIEWERS = 0;
    private int mCurrentPosition;
    private boolean mExpectedQuit;
    private int mIndexModificator = 0;
    private int mNumPages = 5;
    private BroadcastReceiver mReceiver;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;
    private View mTabBanned;
    private View mTabIgnored;
    private View mTabMods;
    private View mTabMuted;
    private View mTabViewers;
    private ViewPager mViewPager;

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private SparseArrayCompat<Fragment> mScrollTabHolders = new SparseArrayCompat();

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public Fragment getItem(int position) {
            Fragment fragment = null;
            int index = position;
            if (index > 0) {
                index += ChannelModerationActivity.this.mIndexModificator;
            }
            switch (index) {
                case ResponseParser.ResponseActionDiscard /*0*/:
                    fragment = ChannelModerationViewersFragment.newInstance(ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_ID), ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_USER), ChannelModerationActivity.this.getIntent().getBooleanExtra(Constants.EXTRA_MODERATOR, false), position);
                    break;
                case ChannelModerationActivity.POS_MODS /*1*/:
                    fragment = ChannelModerationModsFragment.newInstance(ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_ID), ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_USER), position);
                    break;
                case ChannelModerationActivity.POS_MUTED /*2*/:
                    fragment = ChannelModerationMutedUsersFragment.newInstance(Boolean.valueOf(false), ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_ID), ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_USER), position);
                    break;
                case ChannelModerationActivity.POS_IGNORED /*3*/:
                    fragment = ChannelModerationIgnoredUsersFragment.newInstance(ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_ID), ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_USER), position);
                    break;
                case ChannelModerationActivity.POS_BANNED /*4*/:
                    fragment = ChannelModerationMutedUsersFragment.newInstance(Boolean.valueOf(true), ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_ID), ChannelModerationActivity.this.getIntent().getStringExtra(Constants.EXTRA_USER), position);
                    break;
            }
            this.mScrollTabHolders.put(position, fragment);
            return fragment;
        }

        public int getCount() {
            if (Network.isLoggedIn() && PreferenceUtility.isEmailVerified()) {
                return ChannelModerationActivity.this.mNumPages;
            }
            return ChannelModerationActivity.POS_MODS;
        }

        public int getItemPosition(Object object) {
            return -2;
        }

        public CharSequence getPageTitle(int position) {
            return BuildConfig.FLAVOR;
        }
    }

    public static Intent getIntent(Context context, String channelID, String broadcasterID, boolean isModerator) {
        Intent intent = new Intent(context, ChannelModerationActivity.class);
        intent.putExtra(Constants.EXTRA_ID, channelID);
        intent.putExtra(Constants.EXTRA_USER, broadcasterID);
        intent.putExtra(Constants.EXTRA_MODERATOR, isModerator);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_channel_moderation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.dark);
        toolbar.setTitleTextColor(getResources().getColor(R.color.yellow));
        try {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_close_black_18dp, getTheme());
                UIUtils.colorize(drawable, -1);
                actionBar.setHomeAsUpIndicator(drawable);
                actionBar.setDisplayHomeAsUpEnabled(true);
                if (PreferenceUtility.getUser().isGuest(this) || !PreferenceUtility.isEmailVerified()) {
                    actionBar.setTitle((int) R.string.channel_viewers);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        if (PreferenceUtility.getUser().isGuest(this) || !PreferenceUtility.isEmailVerified()) {
            findViewById(R.id.tabs_layout).setVisibility(8);
        }
        String id = PreferenceUtility.getUser()._id;
        boolean isBroadcaster = ModerationHelper.isBroadcaster(id, getIntent().getStringExtra(Constants.EXTRA_USER));
        this.mTabViewers = findViewById(R.id.text_view_viewers);
        this.mTabViewers.setOnClickListener(this);
        this.mTabMods = findViewById(R.id.text_view_mods);
        this.mTabMods.setOnClickListener(this);
        if (!isBroadcaster) {
            this.mTabMods.setVisibility(8);
            this.mIndexModificator += POS_MODS;
            this.mNumPages--;
        }
        this.mTabMuted = findViewById(R.id.text_view_muted);
        this.mTabMuted.setOnClickListener(this);
        if (!(isBroadcaster || ModerationHelper.isAdmin(id) || getIntent().getBooleanExtra(Constants.EXTRA_MODERATOR, false))) {
            this.mTabMuted.setVisibility(8);
            this.mIndexModificator += POS_MODS;
            this.mNumPages--;
        }
        this.mTabIgnored = findViewById(R.id.text_view_ignored);
        this.mTabIgnored.setOnClickListener(this);
        this.mTabBanned = findViewById(R.id.text_view_banned);
        this.mTabBanned.setOnClickListener(this);
        if (!(isBroadcaster || ModerationHelper.isAdmin(id) || getIntent().getBooleanExtra(Constants.EXTRA_MODERATOR, false))) {
            this.mTabBanned.setVisibility(8);
            this.mNumPages--;
        }
        this.mSlidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tabs);
        SlidingTabLayout slidingTabLayout = this.mSlidingTabLayout;
        int[] iArr = new int[POS_MODS];
        iArr[0] = getResources().getColor(R.color.yellow);
        slidingTabLayout.setSelectedIndicatorColors(iArr);
        this.mViewPager = (ViewPager) findViewById(R.id.viewpager);
        this.mViewPager.setOffscreenPageLimit(POS_BANNED);
        if (this.mSectionsPagerAdapter == null) {
            this.mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        }
        this.mSlidingTabLayout.setOnPageChangeListener(new OnPageChangeListener() {
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                ChannelModerationActivity.this.mCurrentPosition = position;
                ChannelModerationActivity.this.configTabs();
            }

            public void onPageScrollStateChanged(int state) {
            }
        });
        this.mViewPager.setAdapter(this.mSectionsPagerAdapter);
        this.mSlidingTabLayout.setViewPager(this.mViewPager);
        configTabs();
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && TextUtils.equals(intent.getAction(), Constants.EVENT_MOD_CHANGED)) {
                    ChannelModerationActivity.this.finish();
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(this.mReceiver, new IntentFilter(Constants.EVENT_MOD_CHANGED));
    }

    protected void onResume() {
        super.onResume();
        this.mExpectedQuit = false;
    }

    protected void onPause() {
        super.onPause();
        if (!this.mExpectedQuit) {
            sendBroadcast(new Intent(Constants.ACTION_PAUSE_PLAYER));
            finish();
        }
    }

    protected void onDestroy() {
        if (this.mReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
        super.onDestroy();
    }

    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        this.mExpectedQuit = true;
        super.startActivityForResult(intent, requestCode, options);
    }

    private void configTabs() {
        boolean z;
        boolean z2 = true;
        this.mTabViewers.setActivated(this.mCurrentPosition == 0);
        View view = this.mTabMods;
        if (this.mCurrentPosition == 1 - this.mIndexModificator) {
            z = true;
        } else {
            z = false;
        }
        view.setActivated(z);
        view = this.mTabMuted;
        if (this.mCurrentPosition == 2 - this.mIndexModificator) {
            z = true;
        } else {
            z = false;
        }
        view.setActivated(z);
        view = this.mTabIgnored;
        if (this.mCurrentPosition == 3 - this.mIndexModificator) {
            z = true;
        } else {
            z = false;
        }
        view.setActivated(z);
        View view2 = this.mTabBanned;
        if (this.mCurrentPosition != 4 - this.mIndexModificator) {
            z2 = false;
        }
        view2.setActivated(z2);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        this.mExpectedQuit = true;
        super.onBackPressed();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text_view_viewers:
                this.mViewPager.setCurrentItem(0);
                return;
            case R.id.text_view_mods:
                this.mViewPager.setCurrentItem(POS_MODS);
                return;
            case R.id.text_view_muted:
                this.mViewPager.setCurrentItem(2 - this.mIndexModificator);
                return;
            case R.id.text_view_ignored:
                this.mViewPager.setCurrentItem(3 - this.mIndexModificator);
                return;
            case R.id.text_view_banned:
                this.mViewPager.setCurrentItem(4 - this.mIndexModificator);
                return;
            default:
                return;
        }
    }
}
