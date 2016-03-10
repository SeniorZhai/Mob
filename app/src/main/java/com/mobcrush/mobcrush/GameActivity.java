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
import com.mobcrush.mobcrush.datamodel.Game;
import io.fabric.sdk.android.BuildConfig;

public class GameActivity extends MobcrushActivty {
    public static Intent getIntent(Context context, Game game) {
        Intent intent = new Intent(context, GameActivity.class);
        intent.putExtra(Constants.EXTRA_GAME, new Gson().toJson((Object) game));
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.layout_toolbar_and_container);
        if (savedInstanceState == null) {
            Fragment f = new GameFragment();
            f.setArguments(getIntent().getExtras());
            getSupportFragmentManager().beginTransaction().add((int) R.id.container, f).commit();
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.dark);
        toolbar.setTitleTextColor(getResources().getColor(R.color.yellow));
        Game game = null;
        String string_game = getIntent().getStringExtra(Constants.EXTRA_GAME);
        if (!TextUtils.isEmpty(string_game)) {
            game = (Game) new Gson().fromJson(string_game, Game.class);
        }
        try {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(game != null ? game.name : BuildConfig.FLAVOR);
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }
}
