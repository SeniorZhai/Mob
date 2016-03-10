package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import com.android.volley.Response.Listener;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.network.Network;

public class ProfileActivity extends MobcrushActivty {
    private ProfileFragment mProfileFragment;
    private User mUser;

    public static Intent getIntent(Context context, User user) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(Constants.EXTRA_USER, user.toString());
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_profile);
        this.mUser = (User) new Gson().fromJson(getIntent().getStringExtra(Constants.EXTRA_USER), User.class);
        if (savedInstanceState == null) {
            this.mProfileFragment = ProfileFragment.newInstance(this.mUser, true);
            getSupportFragmentManager().beginTransaction().add((int) R.id.container, this.mProfileFragment).commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.MCToolbar_Title);
        TextView title = (TextView) toolbar.findViewById(R.id.title);
        if (title != null) {
            title.setTextColor(getResources().getColor(R.color.yellow) & ViewCompat.MEASURED_SIZE_MASK);
        }
        try {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == 16908332) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setIntent(Intent intent) {
        if (intent == null || !Constants.ACTION_UPDATE_USER.equals(intent.getAction())) {
            super.setIntent(intent);
        } else if (this.mProfileFragment != null && this.mUser != null) {
            Network.getUserProfile(this, this.mUser._id, new Listener<User>() {
                public void onResponse(User response) {
                    if (response != null) {
                        ProfileActivity.this.mUser = response;
                        ProfileActivity.this.mProfileFragment.updateUserInfo(ProfileActivity.this.mUser, false, false);
                    }
                }
            }, null);
        }
    }
}
