package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.common.UIUtils;

public class LoginActivity extends MobcrushActivty implements OnClickListener {
    private static final String EXTRA_CLOSE_KEYBOARD = "extra_close_keyboard";
    private static final String EXTRA_SIGNUP = "extra_signup";
    public static boolean mIsAlreadyShowing = false;
    private boolean mCloseKeyboard = true;
    private boolean mDialogMode;
    private Fragment mFLogin;
    private Fragment mFSignUp;
    private boolean mSignup;

    public static Intent getIntent(Context context) {
        return getIntent(context, false);
    }

    public static Intent getIntent(Context context, boolean signup) {
        return getIntent(context, signup, true);
    }

    public static Intent getIntent(Context context, boolean signup, boolean closeKeyboard) {
        return getIntent(context, true, signup, true);
    }

    public static Intent getIntent(Context context, boolean dialogMode, boolean signup, boolean closeKeyboard) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(AccessibilityNodeInfoCompat.ACTION_SET_SELECTION);
        intent.putExtra(Constants.EXTRA_DIALOG_MODE, dialogMode);
        intent.putExtra(EXTRA_SIGNUP, signup);
        intent.putExtra(EXTRA_CLOSE_KEYBOARD, closeKeyboard);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        this.mSignup = getIntent().getBooleanExtra(EXTRA_SIGNUP, false);
        super.onCreate(savedInstanceState);
        mIsAlreadyShowing = true;
        setContentView((int) R.layout.activity_login);
        configToolbar();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            this.mCloseKeyboard = extras.getBoolean(EXTRA_CLOSE_KEYBOARD, true);
            instantiateFragments(extras);
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add((int) R.id.container, this.mFSignUp);
            transaction.add((int) R.id.container, this.mFLogin);
            transaction.hide(this.mSignup ? this.mFLogin : this.mFSignUp);
            transaction.commit();
        }
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

    public void onBackPressed() {
        super.onBackPressed();
        if (this.mCloseKeyboard) {
            UIUtils.hideVirtualKeyboard(this);
        }
    }

    protected void onDestroy() {
        mIsAlreadyShowing = false;
        super.onDestroy();
    }

    public void onClick(View view) {
        if (view != null && view.getId() == R.id.button) {
            this.mSignup = !this.mSignup;
            try {
                if (this.mFLogin == null || this.mFSignUp == null) {
                    instantiateFragments(getIntent().getExtras());
                }
                getSupportFragmentManager().beginTransaction().hide(this.mSignup ? this.mFLogin : this.mFSignUp).show(this.mSignup ? this.mFSignUp : this.mFLogin).commitAllowingStateLoss();
            } catch (Throwable e) {
                e.printStackTrace();
                Crashlytics.logException(e);
            }
            configToolbar();
        }
    }

    private void instantiateFragments(Bundle extras) {
        this.mFSignUp = SignUpFragment.newInstance();
        extras.putBoolean(EXTRA_SIGNUP, true);
        this.mFSignUp.setArguments(extras);
        this.mFLogin = LoginFragment.newInstance();
        extras.putBoolean(EXTRA_SIGNUP, false);
        this.mFLogin.setArguments(extras);
    }

    private void configToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (!this.mDialogMode) {
            ((TextView) toolbar.findViewById(R.id.title)).setText(this.mSignup ? R.string.sign_up : R.string.log_in);
            toolbar.setBackgroundResource(R.color.dark);
            ((TextView) toolbar.findViewById(R.id.button_title)).setText(this.mSignup ? R.string.or_log_in : R.string.or_sign_up);
            toolbar.findViewById(R.id.button).setOnClickListener(this);
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
            toolbar.setVisibility(0);
        }
    }
}
