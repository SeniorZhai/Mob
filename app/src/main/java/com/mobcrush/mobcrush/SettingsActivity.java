package com.mobcrush.mobcrush;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceActivity.Header;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.RingtonePreference;
import android.preference.SwitchPreference;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import com.android.volley.Response.Listener;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.helper.SocialConnectHelperActivity;
import com.mobcrush.mobcrush.logic.SocialNetwork;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final boolean ALWAYS_SIMPLE_PREFS = true;
    private static final int REQUEST_CODE_CONNECT_FACEBOOK = 1002;
    private static final int REQUEST_CODE_CONNECT_TWITTER = 1003;
    private static final int REQUEST_CODE_CONNECT_YOUTUBE = 1004;
    private static boolean mFacebookIsConnected;
    private static boolean mTwitterIsConnected;
    private static boolean mYoutubeIsConnected;
    private static OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object value) {
            CharSequence charSequence = null;
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                if (index >= 0) {
                    charSequence = listPreference.getEntries()[index];
                }
                preference.setSummary(charSequence);
            } else if (!(preference instanceof RingtonePreference)) {
                preference.setSummary(stringValue);
            } else if (TextUtils.isEmpty(stringValue)) {
                preference.setSummary(R.string.pref_ringtone_silent);
            } else {
                Ringtone ringtone = RingtoneManager.getRingtone(preference.getContext(), Uri.parse(stringValue));
                if (ringtone == null) {
                    preference.setSummary(null);
                } else {
                    preference.setSummary(ringtone.getTitle(preference.getContext()));
                }
            }
            return SettingsActivity.ALWAYS_SIMPLE_PREFS;
        }
    };
    private AppCompatDelegate mDelegate;
    private boolean mIsAlreadyShownToUser;
    private boolean mWaitingForNetworkResponse;

    @TargetApi(11)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    public static void setSocialState(String provider, boolean state) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainApplication.getContext());
        int key = 0;
        if (TextUtils.equals(Constants.FACEBOOK, provider)) {
            key = R.string.pref_key_facebook;
            mFacebookIsConnected = state;
        } else if (TextUtils.equals(Constants.TWITTER, provider)) {
            key = R.string.pref_key_twitter;
            mTwitterIsConnected = state;
        } else if (TextUtils.equals(Constants.GOOGLE, provider)) {
            key = R.string.pref_key_youtube;
            mYoutubeIsConnected = ALWAYS_SIMPLE_PREFS;
        }
        if (key == 0) {
        }
    }

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & 15) >= 4 ? ALWAYS_SIMPLE_PREFS : false;
    }

    private static boolean isSimplePreferences(Context context) {
        return ALWAYS_SIMPLE_PREFS;
    }

    private static void bindPreferenceSummaryToValue(Preference preference) {
        if (preference != null) {
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), BuildConfig.FLAVOR));
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return GeneralPreferenceFragment.class.getName().equals(fragmentName);
    }

    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        this.mWaitingForNetworkResponse = ALWAYS_SIMPLE_PREFS;
        Network.getUserConnections(null, PreferenceUtility.getUser()._id, new Listener<ArrayList<String>>() {
            public void onResponse(ArrayList<String> connections) {
                if (connections == null) {
                    connections = new ArrayList();
                }
                boolean exists = connections.contains(Constants.FACEBOOK.toLowerCase());
                SettingsActivity.setSocialState(Constants.FACEBOOK, exists);
                SettingsActivity.this.silentSetSwitchPreference((int) R.string.pref_key_facebook, exists);
                exists = connections.contains(Constants.TWITTER.toLowerCase());
                SettingsActivity.setSocialState(Constants.TWITTER, exists);
                SettingsActivity.this.silentSetSwitchPreference((int) R.string.pref_key_twitter, exists);
                exists = connections.contains(Constants.GOOGLE.toLowerCase());
                SettingsActivity.setSocialState(Constants.GOOGLE, exists);
                SettingsActivity.this.silentSetSwitchPreference((int) R.string.pref_key_youtube, exists);
                SettingsActivity.this.mWaitingForNetworkResponse = false;
            }
        }, null);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
        ViewGroup rootView = (ViewGroup) getListView().getParent();
        rootView.setPadding(0, 0, 0, 0);
        Toolbar toolbar = (Toolbar) getLayoutInflater().inflate(R.layout.toolbar_settings, rootView, false);
        toolbar.setBackgroundResource(R.color.dark);
        toolbar.setTitleTextColor(getResources().getColor(R.color.yellow));
        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.MCToolbar_Title);
        toolbar.setTitle((int) R.string.title_activity_settings);
        toolbar.setNavigationOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SettingsActivity.this.finish();
            }
        });
        try {
            setSupportActionBar(toolbar);
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        rootView.addView(toolbar, 0);
        setupSimplePreferencesScreen();
        View v = new View(this);
        v.setBackgroundColor(getResources().getColor(R.color.user_list_divider));
        v.setLayoutParams(new LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.list_divider_height)));
        v.setPadding(0, getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin) / 2, 0, 0);
        rootView.addView(v);
        TextView tv = new TextView(this);
        tv.setLayoutParams(new LayoutParams(-1, getResources().getDimensionPixelSize(R.dimen.button_height)));
        tv.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin));
        tv.setGravity(17);
        tv.setText(Utils.getAppVersionString());
        rootView.addView(tv);
    }

    public ActionBar getSupportActionBar() {
        return getDelegate().getSupportActionBar();
    }

    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    public MenuInflater getMenuInflater() {
        return getDelegate().getMenuInflater();
    }

    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    public void setContentView(View view) {
        getDelegate().setContentView(view);
    }

    public void setContentView(View view, LayoutParams params) {
        getDelegate().setContentView(view, params);
    }

    public void addContentView(View view, LayoutParams params) {
        getDelegate().addContentView(view, params);
    }

    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    protected void onTitleChanged(CharSequence title, int color) {
        super.onTitleChanged(title, color);
        getDelegate().setTitle(title);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getDelegate().onConfigurationChanged(newConfig);
    }

    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    public void invalidateOptionsMenu() {
        getDelegate().invalidateOptionsMenu();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onResume() {
        super.onResume();
        MainApplication.onActivityResumed(getClass().getSimpleName());
        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_SETTINGS);
        this.mIsAlreadyShownToUser = ALWAYS_SIMPLE_PREFS;
    }

    protected void onPause() {
        MainApplication.onActivityPaused(getClass().getSimpleName());
        super.onPause();
        this.mIsAlreadyShownToUser = false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CONNECT_FACEBOOK) {
            if (resultCode == -1) {
                silentSetSwitchPreference((int) R.string.pref_key_facebook, (boolean) ALWAYS_SIMPLE_PREFS);
            } else {
                silentSetSwitchPreference((int) R.string.pref_key_facebook, false);
            }
        } else if (requestCode == REQUEST_CODE_CONNECT_TWITTER) {
            if (resultCode == -1) {
                silentSetSwitchPreference((int) R.string.pref_key_twitter, (boolean) ALWAYS_SIMPLE_PREFS);
            } else {
                silentSetSwitchPreference((int) R.string.pref_key_twitter, false);
            }
        } else if (requestCode != REQUEST_CODE_CONNECT_YOUTUBE) {
        } else {
            if (resultCode == -1) {
                silentSetSwitchPreference((int) R.string.pref_key_youtube, (boolean) ALWAYS_SIMPLE_PREFS);
            } else {
                silentSetSwitchPreference((int) R.string.pref_key_youtube, false);
            }
        }
    }

    private void silentSetSwitchPreference(int keyResId, boolean value) {
        silentSetSwitchPreference((SwitchPreference) findPreference(getString(keyResId)), value);
    }

    private void silentSetSwitchPreference(SwitchPreference sp, boolean value) {
        if (sp != null) {
            OnPreferenceChangeListener onPreferenceChangeListener = sp.getOnPreferenceChangeListener();
            sp.setOnPreferenceChangeListener(null);
            sp.setChecked(value);
            if (onPreferenceChangeListener != null) {
                sp.setOnPreferenceChangeListener(onPreferenceChangeListener);
            }
        }
    }

    private void setupSimplePreferencesScreen() {
        if (isSimplePreferences(this)) {
            addPreferencesFromResource(R.xml.pref_general);
            PreferenceCategory fakeHeader = new PreferenceCategory(this);
            fakeHeader.setTitle(R.string.pref_header_social_networks);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_social_networks);
            findPreference(getString(R.string.pref_key_facebook)).setOnPreferenceChangeListener(this);
            findPreference(getString(R.string.pref_key_twitter)).setOnPreferenceChangeListener(this);
            Preference preference = findPreference(getString(R.string.pref_key_youtube));
            if (preference != null) {
                preference.setOnPreferenceChangeListener(this);
            }
            fakeHeader = new PreferenceCategory(this);
            fakeHeader.setTitle(R.string.pref_header_notifications);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_notification);
            findPreference(getString(R.string.pref_key_general_notification)).setOnPreferenceClickListener(this);
            findPreference(getString(R.string.pref_key_follower_notification)).setOnPreferenceClickListener(this);
            fakeHeader = new PreferenceCategory(this);
            fakeHeader.setTitle(R.string.pref_header_notification_sounds);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_notification_sound);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_notifications_ringtone)));
            fakeHeader = new PreferenceCategory(this);
            fakeHeader.setTitle(R.string.pref_header_broadcast);
            getPreferenceScreen().addPreference(fakeHeader);
            addPreferencesFromResource(R.xml.pref_broadcast_over_wifi);
            bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_key_notifications_ringtone)));
        }
    }

    public boolean onIsMultiPane() {
        return (!isXLargeTablet(this) || isSimplePreferences(this)) ? false : ALWAYS_SIMPLE_PREFS;
    }

    @TargetApi(11)
    public void onBuildHeaders(List<Header> target) {
        if (!isSimplePreferences(this)) {
            loadHeadersFromResource(R.xml.pref_headers, target);
        }
    }

    public boolean onPreferenceChange(final Preference preference, Object newValue) {
        if (preference == null) {
            return false;
        }
        if (this.mWaitingForNetworkResponse || !this.mIsAlreadyShownToUser) {
            return ALWAYS_SIMPLE_PREFS;
        }
        if (!(newValue instanceof Boolean) || !((Boolean) newValue).booleanValue()) {
            String provider;
            final ProgressDialog dlg = new ProgressDialog(this);
            dlg.setMessage(getString(R.string.updating__));
            dlg.show();
            if (TextUtils.equals(preference.getKey(), getString(R.string.pref_key_facebook))) {
                provider = Constants.FACEBOOK;
            } else if (TextUtils.equals(preference.getKey(), getString(R.string.pref_key_twitter))) {
                provider = Constants.TWITTER;
            } else if (!TextUtils.equals(preference.getKey(), getString(R.string.pref_key_youtube))) {
                return false;
            } else {
                provider = Constants.GOOGLE;
            }
            Network.disconnectUserSocialNetwork(null, provider, new Listener<Boolean>() {
                public void onResponse(Boolean response) {
                    if (response == null || !response.booleanValue()) {
                        SettingsActivity.this.silentSetSwitchPreference((SwitchPreference) preference, (boolean) SettingsActivity.ALWAYS_SIMPLE_PREFS);
                    } else {
                        SettingsActivity.setSocialState(provider, false);
                    }
                    if (dlg.isShowing()) {
                        try {
                            dlg.dismiss();
                        } catch (Exception e) {
                        }
                    }
                }
            }, null);
        } else if (TextUtils.equals(preference.getKey(), getString(R.string.pref_key_facebook))) {
            try {
                if (mFacebookIsConnected) {
                    return ALWAYS_SIMPLE_PREFS;
                }
                startActivityForResult(SocialConnectHelperActivity.getIntent(this, SocialNetwork.Facebook), REQUEST_CODE_CONNECT_FACEBOOK);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (TextUtils.equals(preference.getKey(), getString(R.string.pref_key_twitter))) {
            try {
                if (mTwitterIsConnected) {
                    return ALWAYS_SIMPLE_PREFS;
                }
                startActivityForResult(SocialConnectHelperActivity.getIntent(this, SocialNetwork.Twitter), REQUEST_CODE_CONNECT_TWITTER);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } else if (!TextUtils.equals(preference.getKey(), getString(R.string.pref_key_youtube))) {
            return false;
        } else {
            if (mYoutubeIsConnected) {
                return ALWAYS_SIMPLE_PREFS;
            }
            startActivityForResult(SocialConnectHelperActivity.getIntent(this, SocialNetwork.Google), REQUEST_CODE_CONNECT_YOUTUBE);
            return false;
        }
        return ALWAYS_SIMPLE_PREFS;
    }

    private AppCompatDelegate getDelegate() {
        if (this.mDelegate == null) {
            this.mDelegate = AppCompatDelegate.create((Activity) this, null);
        }
        return this.mDelegate;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference != null) {
            if (TextUtils.equals(getString(R.string.pref_key_general_notification), preference.getKey())) {
                startActivity(SettingsNotificationsActivity.getIntent(this));
                return ALWAYS_SIMPLE_PREFS;
            } else if (TextUtils.equals(getString(R.string.pref_key_follower_notification), preference.getKey())) {
                startActivity(new Intent(this, SettingsFollowingNotificationsActivity.class));
                return ALWAYS_SIMPLE_PREFS;
            }
        }
        return false;
    }
}
