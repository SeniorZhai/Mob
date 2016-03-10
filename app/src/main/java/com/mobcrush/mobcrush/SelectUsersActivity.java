package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
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
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.BuildConfig;

public class SelectUsersActivity extends MobcrushActivty implements TextWatcher {
    private UsersAdapter mAdapter;
    private TextView mAddItem;
    private View mClearBtn;
    private Runnable mSearchCaller = new Runnable() {
        public void run() {
            SelectUsersActivity.this.attemptSearch();
        }
    };
    private EditText mSearchEdit;
    private Handler mSearchHanlder = new Handler();

    public static Intent getIntent(Context context) {
        return new Intent(context, SelectUsersActivity.class);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_select_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundResource(R.color.dark);
        toolbar.setTitle(getString(R.string.add_moderators));
        TextView title = (TextView) toolbar.findViewById(R.id.title);
        title.setText(R.string.add_moderators);
        title.setTextColor(getResources().getColor(R.color.yellow));
        this.mAddItem = (TextView) toolbar.findViewById(R.id.action);
        this.mAddItem.setBackground(null);
        this.mAddItem.setText(getString(R.string.action_add).toUpperCase());
        this.mAddItem.setTextColor(getResources().getColor(R.color.middle_gray));
        this.mAddItem.setEnabled(false);
        this.mAddItem.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("android.intent.extra.TEXT", SelectUsersActivity.this.mAdapter.getSelectedUsers());
                SelectUsersActivity.this.setResult(-1, intent);
                SelectUsersActivity.this.finish();
            }
        });
        toolbar.setNavigationIcon((int) R.drawable.ic_arrow_back_white_24dp);
        this.mClearBtn = findViewById(R.id.btn_clear);
        this.mClearBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                SelectUsersActivity.this.mSearchEdit.setText(BuildConfig.FLAVOR);
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
                SelectUsersActivity.this.attemptSearch();
                return true;
            }
        });
        this.mSearchEdit.addTextChangedListener(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, 1, false));
        this.mAdapter = new UsersAdapter(this, R.layout.item_user_with_selector);
        this.mAdapter.setDivider(R.drawable.user_list_divider_white_20_opaq);
        this.mAdapter.enableSelectingMode(new Callback() {
            public boolean handleMessage(Message message) {
                if (message == null || SelectUsersActivity.this.mAddItem == null) {
                    return false;
                }
                SelectUsersActivity.this.updateActionButton();
                return true;
            }
        });
        recyclerView.setAdapter(this.mAdapter);
    }

    private void updateActionButton() {
        if (this.mAdapter != null && this.mAddItem != null) {
            this.mAddItem.setEnabled(this.mAdapter.getCountOfSelectedItems() > 0);
            this.mAddItem.setText(getString(R.string.action_add).toUpperCase() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + (this.mAdapter.getCountOfSelectedItems() > 0 ? Integer.valueOf(this.mAdapter.getCountOfSelectedItems()) : BuildConfig.FLAVOR));
            this.mAddItem.setTextColor(getResources().getColor(this.mAdapter.getCountOfSelectedItems() > 0 ? R.color.blue : R.color.middle_gray));
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    protected void onPause() {
        UIUtils.hideVirtualKeyboard(this);
        super.onPause();
    }

    private void attemptSearch() {
        Network.searchUsers(this, "^" + String.valueOf(this.mSearchEdit.getText()), new Listener<User[]>() {
            public void onResponse(User[] response) {
                SelectUsersActivity.this.mAdapter.clear();
                SelectUsersActivity.this.mAdapter.addUsers(response);
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
