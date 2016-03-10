package com.mobcrush.mobcrush;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.Listener;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.network.Network;

public class LoginFragment extends Fragment implements OnClickListener, TextWatcher {
    private boolean mDoNotHideKeyboard;
    private View mLoginButton;
    private View mLoginFormView;
    private TextView mPasswordError;
    private EditText mPasswordView;
    private View mProgressView;
    private TextView mUsernameError;
    private EditText mUsernameView;
    private Listener<Boolean> onResponseLogin = new Listener<Boolean>() {
        public void onResponse(Boolean response) {
            if (LoginFragment.this.isAdded()) {
                LoginFragment.this.mLoginButton.setOnClickListener(LoginFragment.this);
                if (response == null || !response.booleanValue() || LoginFragment.this.getActivity() == null) {
                    LoginFragment.this.mUsernameView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                    LoginFragment.this.mPasswordError.setText(MainApplication.getRString(R.string.wrong_email_or_password, new Object[0]));
                    LoginFragment.this.mPasswordView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                    LoginFragment.this.showProgress(false);
                    return;
                }
                LoginFragment.this.getActivity().setResult(-1);
                LoginFragment.this.getActivity().finish();
            }
        }
    };

    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);
        this.mUsernameError = (TextView) view.findViewById(R.id.username_error);
        this.mUsernameView = (EditText) view.findViewById(R.id.username);
        this.mUsernameView.addTextChangedListener(this);
        this.mPasswordError = (TextView) view.findViewById(R.id.password_error);
        this.mPasswordView = (EditText) view.findViewById(R.id.password);
        UIUtils.fixPasswordHintIssue(this.mPasswordView);
        this.mPasswordView.addTextChangedListener(this);
        this.mPasswordView.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id != R.integer.loginActionId && id != 0) {
                    return false;
                }
                LoginFragment.this.attemptLogin();
                return true;
            }
        });
        if (!PreferenceUtility.getUser().isGuest(getActivity())) {
            this.mUsernameView.setText(PreferenceUtility.getUser().username);
            this.mPasswordView.requestFocus();
        }
        this.mLoginButton = view.findViewById(R.id.login_button);
        this.mLoginButton.setOnClickListener(this);
        this.mLoginFormView = view.findViewById(R.id.login_form);
        this.mProgressView = view.findViewById(R.id.login_progress);
        view.findViewById(R.id.forgot_password).setOnClickListener(this);
        ProgressBar pb = (ProgressBar) this.mProgressView.findViewById(R.id.progressBar);
        Drawable d = pb.getIndeterminateDrawable();
        d.setColorFilter(new LightingColorFilter(ViewCompat.MEASURED_STATE_MASK, getResources().getColor(R.color.dark)));
        pb.setIndeterminateDrawable(d);
        return view;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_LOGIN);
    }

    public void onDetach() {
        super.onDetach();
        if (!this.mDoNotHideKeyboard) {
            UIUtils.hideVirtualKeyboard(getActivity());
        }
    }

    public void attemptLogin() {
        this.mUsernameError.setText(null);
        this.mUsernameView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        this.mPasswordError.setText(null);
        this.mPasswordView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        String username = this.mUsernameView.getText().toString();
        String password = this.mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            this.mPasswordError.setText(MainApplication.getRString(R.string.error_short_password, new Object[0]));
            this.mPasswordView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
            focusView = this.mPasswordView;
            cancel = true;
        }
        if (TextUtils.isEmpty(username)) {
            this.mUsernameError.setText(MainApplication.getRString(R.string.error_field_required, new Object[0]));
            this.mUsernameView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
            focusView = this.mUsernameView;
            cancel = true;
        }
        if (cancel) {
            this.mLoginButton.setOnClickListener(this);
            focusView.requestFocus();
            return;
        }
        showProgress(true);
        Network.login(getActivity(), username, password, this.onResponseLogin, null);
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    @TargetApi(13)
    public void showProgress(final boolean show) {
        float f = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        int i = 8;
        int i2 = 0;
        int i3;
        if (VERSION.SDK_INT >= 13) {
            int shortAnimTime = getResources().getInteger(17694720);
            View view = this.mLoginFormView;
            if (show) {
                i3 = 8;
            } else {
                i3 = 0;
            }
            view.setVisibility(i3);
            this.mLoginFormView.animate().setDuration((long) shortAnimTime).alpha(show ? 0.0f : DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    LoginFragment.this.mLoginFormView.setVisibility(show ? 8 : 0);
                }
            });
            View view2 = this.mProgressView;
            if (!show) {
                i2 = 8;
            }
            view2.setVisibility(i2);
            ViewPropertyAnimator duration = this.mProgressView.animate().setDuration((long) shortAnimTime);
            if (!show) {
                f = 0.0f;
            }
            duration.alpha(f).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    LoginFragment.this.mProgressView.setVisibility(show ? 0 : 8);
                }
            });
            return;
        }
        View view3 = this.mProgressView;
        if (show) {
            i3 = 0;
        } else {
            i3 = 8;
        }
        view3.setVisibility(i3);
        view2 = this.mLoginFormView;
        if (!show) {
            i = 0;
        }
        view2.setVisibility(i);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.forgot_password:
                startActivity(ForgotPasswordActivity.getIntent(getActivity()));
                return;
            case R.id.login_button:
                view.setOnClickListener(null);
                attemptLogin();
                return;
            default:
                return;
        }
    }

    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    public void afterTextChanged(Editable editable) {
        if (this.mPasswordView.isFocused()) {
            this.mPasswordError.setText(null);
            this.mPasswordView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        } else if (this.mUsernameView.isFocused()) {
            this.mUsernameError.setText(null);
            this.mUsernameView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        }
    }
}
