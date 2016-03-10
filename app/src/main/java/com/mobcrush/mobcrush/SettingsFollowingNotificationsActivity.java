package com.mobcrush.mobcrush;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.logic.UserLogicType;

public class SettingsFollowingNotificationsActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.layout_toolbar_and_container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.dark);
        toolbar.setTitleTextColor(getResources().getColor(R.color.yellow));
        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.MCToolbar_Title);
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
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add((int) R.id.container, UsersFragment.newInstance(PreferenceUtility.getUser(), UserLogicType.FollowingSettings, 0));
            transaction.commit();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
