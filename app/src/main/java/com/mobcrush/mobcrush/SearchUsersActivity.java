package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.BuildConfig;

public class SearchUsersActivity extends MobcrushActivty implements TextWatcher {
    private UsersAdapter mAdapter;
    private View mClearBtn;
    private Runnable mSearchCaller = new Runnable() {
        public void run() {
            SearchUsersActivity.this.attemptSearch();
        }
    };
    private EditText mSearchEdit;
    private Handler mSearchHanlder = new Handler();
    private TextView mShareItem;

    public static Intent getIntent(Context context) {
        return new Intent(context, SearchUsersActivity.class);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_search_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle((int) R.string.search);
        ((TextView) toolbar.findViewById(R.id.title)).setText(R.string.search);
        this.mShareItem = (TextView) toolbar.findViewById(R.id.action);
        this.mShareItem.setVisibility(8);
        toolbar.setNavigationIcon((int) R.drawable.ic_close_x_utility);
        toolbar.setTitleTextColor(getResources().getColor(R.color.dark));
        this.mClearBtn = findViewById(R.id.btn_clear);
        this.mClearBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SearchUsersActivity.this.mSearchEdit.setText(BuildConfig.FLAVOR);
            }
        });
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
        this.mSearchEdit = (EditText) findViewById(R.id.search_edit);
        this.mSearchEdit.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id != R.integer.searchActionId && id != 3 && id != 0) {
                    return false;
                }
                SearchUsersActivity.this.attemptSearch();
                return true;
            }
        });
        this.mSearchEdit.addTextChangedListener(this);
        this.mSearchEdit.requestFocus();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, 1, false));
        this.mAdapter = new UsersAdapter(this, R.layout.item_user);
        this.mAdapter.enableTrackFollow();
        recyclerView.setAdapter(this.mAdapter);
        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_SEARCH);
    }

    protected void onPause() {
        UIUtils.hideVirtualKeyboard(this);
        super.onPause();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    private void attemptSearch() {
        final String term = "^" + String.valueOf(this.mSearchEdit.getText());
        Network.searchUsers(this, term, new Listener<User[]>() {
            public void onResponse(User[] response) {
                SearchUsersActivity.this.mAdapter.clear();
                SearchUsersActivity.this.mAdapter.addUsers(response);
                SearchUsersActivity.this.mAdapter.setSearchTermToTrack(term);
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    public void afterTextChanged(Editable editable) {
        View view = this.mClearBtn;
        int i = (editable == null || editable.length() == 0) ? 8 : 0;
        view.setVisibility(i);
        this.mSearchHanlder.removeCallbacks(this.mSearchCaller);
        if (editable == null) {
            return;
        }
        if (editable.length() >= 2) {
            this.mSearchHanlder.postDelayed(this.mSearchCaller, 300);
            return;
        }
        this.mAdapter.clear();
        this.mAdapter.notifyDataSetChanged();
    }
}
