package com.mobcrush.mobcrush;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.android.volley.Response.Listener;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.network.Network;

public class ProfileAboutActivity extends MobcrushActivty implements OnClickListener {
    private boolean mEditMode;
    private EditText mEditText;
    private View mLoading;
    private TextView mTextView;
    private Toolbar mToolbar;
    private User mUser;

    public static Intent getIntent(@NonNull Context context, @NonNull User user, boolean isEditActive) {
        Intent intent = new Intent(context, ProfileAboutActivity.class);
        intent.putExtra(Constants.EXTRA_USER, user.toString());
        intent.putExtra(Constants.EXTRA_DIALOG_MODE, isEditActive);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_profile_about);
        this.mUser = (User) new Gson().fromJson(getIntent().getStringExtra(Constants.EXTRA_USER), User.class);
        this.mEditMode = getIntent().getBooleanExtra(Constants.EXTRA_DIALOG_MODE, false);
        this.mEditText = (EditText) findViewById(R.id.edit);
        this.mTextView = (TextView) findViewById(R.id.text);
        this.mLoading = findViewById(R.id.loading_layout);
        configToolbar();
        try {
            this.mLoading.setVisibility(0);
            Network.getUserChannelDescription(this, this.mUser._id, new Listener<String>() {
                public void onResponse(String response) {
                    ProfileAboutActivity.this.mTextView.setText(response);
                    ProfileAboutActivity.this.mEditText.setText(response);
                    ProfileAboutActivity.this.mLoading.setVisibility(8);
                }
            }, null);
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            this.mLoading.setVisibility(8);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            UIUtils.hideVirtualKeyboard(this);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        if (!this.mEditMode || TextUtils.equals(this.mTextView.getText(), this.mEditText.getText())) {
            super.onBackPressed();
        } else {
            new Builder(this).content((int) R.string.about_quit_confirmation).positiveText((int) R.string.Continue).negativeText(17039360).callback(new ButtonCallback() {
                public void onPositive(MaterialDialog dialog) {
                    super.onPositive(dialog);
                    ProfileAboutActivity.this.finish();
                }

                public void onNegative(MaterialDialog dialog) {
                    super.onNegative(dialog);
                    ProfileAboutActivity.this.mEditText.performClick();
                }
            }).show();
        }
    }

    private void configToolbar() {
        int i = 8;
        this.mToolbar = (Toolbar) findViewById(R.id.toolbar);
        ViewCompat.setElevation(this.mToolbar, (float) getResources().getDimensionPixelSize(R.dimen.headers_elevation));
        this.mToolbar.setBackgroundResource(R.color.dark);
        View btn = this.mToolbar.findViewById(R.id.button);
        int i2 = (this.mUser == null || !this.mUser.equals(PreferenceUtility.getUser())) ? 8 : 0;
        btn.setVisibility(i2);
        btn.setOnClickListener(this);
        View findViewById = this.mToolbar.findViewById(R.id.button_title);
        if (this.mUser != null && this.mUser.equals(PreferenceUtility.getUser())) {
            i = 0;
        }
        findViewById.setVisibility(i);
        try {
            setSupportActionBar(this.mToolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Crashlytics.logException(e);
        }
        updateControls();
        this.mToolbar.setVisibility(0);
    }

    private void updateControls() {
        int i;
        int i2 = 0;
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(this.mEditMode ? R.drawable.ic_chat_close : R.drawable.ic_arrow_back_white_24dp);
        }
        ((TextView) this.mToolbar.findViewById(R.id.title)).setText(R.string.title_activity_profile_about);
        TextView tv = (TextView) this.mToolbar.findViewById(R.id.button_title);
        tv.setAllCaps(true);
        tv.setText(this.mEditMode ? R.string.action_save : R.string.action_edit);
        findViewById(R.id.content).setBackgroundResource(this.mEditMode ? R.color.light : R.color.dark);
        TextView textView = this.mTextView;
        if (this.mEditMode) {
            i = 8;
        } else {
            i = 0;
        }
        textView.setVisibility(i);
        EditText editText = this.mEditText;
        if (!this.mEditMode) {
            i2 = 8;
        }
        editText.setVisibility(i2);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                this.mEditMode = !this.mEditMode;
                updateControls();
                if (this.mEditMode) {
                    this.mEditText.requestFocus();
                    UIUtils.showVirtualKeyboard(this);
                    return;
                }
                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(getString(R.string.updating__));
                dialog.show();
                Network.setUserChannelDescription(this, this.mEditText.getText().toString(), new Listener<Boolean>() {
                    public void onResponse(Boolean response) {
                        if (response.booleanValue()) {
                            UIUtils.hideVirtualKeyboard(ProfileAboutActivity.this);
                            ProfileAboutActivity.this.mTextView.setText(ProfileAboutActivity.this.mEditText.getText());
                        }
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }, null);
                return;
            default:
                return;
        }
    }
}
