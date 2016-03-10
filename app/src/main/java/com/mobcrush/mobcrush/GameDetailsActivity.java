package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.mixpanel.Source;
import io.fabric.sdk.android.BuildConfig;

public class GameDetailsActivity extends MobcrushActivty {
    private Broadcast mBroadcast;

    public static Intent getIntent(Context context, Broadcast broadcast, Source source) {
        if (broadcast == null || TextUtils.isEmpty(broadcast._id)) {
            try {
                throw new IllegalArgumentException("Broadcast and broadcast._id can't be empty! Broadcast: " + broadcast);
            } catch (Exception e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
        }
        Intent intent = new Intent(context, GameDetailsActivity.class);
        intent.putExtra(Constants.EXTRA_BROADCAST, broadcast == null ? null : broadcast.toString());
        intent.putExtra(Constants.EXTRA_SOURCE, source.toString());
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        ActionBar actionBar;
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_game_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(getResources().getColor(R.color.yellow));
        toolbar.setTitleTextAppearance(toolbar.getContext(), R.style.MCToolbar_Title);
        try {
            setSupportActionBar(toolbar);
            actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Throwable e) {
            actionBar = null;
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        if (getIntent().getExtras() != null) {
            String broadcast = getIntent().getExtras().getString(Constants.EXTRA_BROADCAST);
            if (broadcast != null) {
                this.mBroadcast = (Broadcast) new Gson().fromJson(broadcast, Broadcast.class);
                if (actionBar != null) {
                    actionBar.setTitle(this.mBroadcast.game != null ? this.mBroadcast.game.name : BuildConfig.FLAVOR);
                }
            }
        }
        if (savedInstanceState == null) {
            Fragment f = new GameDetailsFragment();
            f.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add((int) R.id.container, f).commit();
        }
    }

    protected void onResume() {
        super.onResume();
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

    protected void onPause() {
        super.onPause();
    }

    protected void onDestroy() {
        super.onDestroy();
    }
}
