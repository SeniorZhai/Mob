package com.mobcrush.mobcrush;

import android.app.ProgressDialog;
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
import android.widget.Toast;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.EntityType;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.logic.SocialNetwork;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper.ShareType;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.BuildConfig;

public class ShareToActivity extends MobcrushActivty implements TextWatcher {
    private UsersAdapter mAdapter;
    private Broadcast mBroadcast;
    private View mClearBtn;
    private Runnable mSearchCaller = new Runnable() {
        public void run() {
            ShareToActivity.this.attemptSearch();
        }
    };
    private EditText mSearchEdit;
    private Handler mSearchHanlder = new Handler();
    private TextView mShareItem;

    public static Intent getIntent(Context context, Broadcast broadcast) {
        Intent intent = new Intent(context, ShareToActivity.class);
        intent.putExtra(Constants.EXTRA_BROADCAST, broadcast == null ? null : broadcast.toString());
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_share_to);
        if (getIntent() != null) {
            String broadcast = getIntent().getExtras().getString(Constants.EXTRA_BROADCAST);
            if (broadcast != null) {
                this.mBroadcast = (Broadcast) new Gson().fromJson(broadcast, Broadcast.class);
            }
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle((int) R.string.share_broadcast);
        ((TextView) toolbar.findViewById(R.id.title)).setText(R.string.share_broadcast);
        this.mShareItem = (TextView) toolbar.findViewById(R.id.action);
        this.mShareItem.setText(getString(R.string.action_share).toUpperCase());
        this.mShareItem.setEnabled(false);
        this.mShareItem.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ShareToActivity.this.share();
            }
        });
        toolbar.setNavigationIcon((int) R.drawable.ic_close_x_utility);
        toolbar.setTitleTextColor(getResources().getColor(R.color.dark));
        this.mClearBtn = findViewById(R.id.btn_clear);
        this.mClearBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                ShareToActivity.this.mSearchEdit.setText(BuildConfig.FLAVOR);
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
                ShareToActivity.this.attemptSearch();
                return true;
            }
        });
        this.mSearchEdit.addTextChangedListener(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, 1, false));
        this.mAdapter = new UsersAdapter(this, R.layout.item_user_with_selector);
        this.mAdapter.enableSelectingMode(new Callback() {
            public boolean handleMessage(Message message) {
                if (message == null || ShareToActivity.this.mShareItem == null) {
                    return false;
                }
                ShareToActivity.this.updateShareButton();
                return true;
            }
        });
        recyclerView.setAdapter(this.mAdapter);
        attemptSearch();
    }

    private void updateShareButton() {
        if (this.mAdapter != null && this.mShareItem != null) {
            this.mShareItem.setEnabled(this.mAdapter.getCountOfSelectedItems() > 0);
            this.mShareItem.setText(getString(R.string.action_share).toUpperCase() + MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR + (this.mAdapter.getCountOfSelectedItems() > 0 ? Integer.valueOf(this.mAdapter.getCountOfSelectedItems()) : BuildConfig.FLAVOR));
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    private void share() {
        if (this.mBroadcast == null) {
            Crashlytics.logException(new IllegalStateException("Broadcast is empty"));
            return;
        }
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.sharing__));
        progressDialog.show();
        Network.notifyFollowers(this, EntityType.broadcast, this.mBroadcast._id, this.mAdapter.getListOfSelectedUsers(), false, new Listener<Boolean>() {
            public void onResponse(Boolean response) {
                ShareToActivity.this.closeDialog(progressDialog);
                ShareToActivity shareToActivity = ShareToActivity.this;
                boolean z = response != null && response.booleanValue();
                shareToActivity.showSharingResult(z);
                if (response != null && response.booleanValue()) {
                    GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_SHARES, ShareToActivity.this.mBroadcast._id + ":MC", Long.valueOf(ShareToActivity.this.mAdapter.getCountOfSelectedUsers()));
                    MixpanelHelper.getInstance(MainApplication.getContext()).trackShareEvent(ShareToActivity.this.mBroadcast, SocialNetwork.Mobcrush, ShareType.SPECIFIC_FOLLOWERS, (int) ShareToActivity.this.mAdapter.getCountOfSelectedUsers());
                    ShareToActivity.this.finish();
                }
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                ShareToActivity.this.closeDialog(progressDialog);
                ShareToActivity.this.showSharingResult(false);
            }
        });
    }

    private void closeDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
            }
        }
    }

    private void showSharingResult(boolean isSuccessful) {
        Toast.makeText(this, isSuccessful ? R.string.broadcast_was_shared : R.string.error_sharing_broadcast, 1).show();
    }

    private void attemptSearch() {
        Network.searchFollowers(this, String.valueOf(this.mSearchEdit.getText()), 0, 100, new Listener<User[]>() {
            public void onResponse(User[] response) {
                ShareToActivity.this.mAdapter.clear();
                ShareToActivity.this.mAdapter.addUsers(response);
                ShareToActivity.this.updateShareButton();
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
        this.mSearchHanlder.postDelayed(this.mSearchCaller, 300);
    }
}
