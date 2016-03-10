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
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.BuildConfig;

public class ForgotPasswordFragment extends Fragment implements OnClickListener, TextWatcher {
    private TextView mEmailError;
    private TextView mEmailText;
    private EditText mEmailView;
    private View mForgotFormView;
    private View mProgressView;
    private View mResultView;
    private View mSubmitButton;
    private Listener<Boolean> onResponseReset = new Listener<Boolean>() {
        public void onResponse(Boolean response) {
            if (ForgotPasswordFragment.this.isAdded()) {
                ForgotPasswordFragment.this.showProgress(false);
                ForgotPasswordFragment.this.mSubmitButton.setOnClickListener(ForgotPasswordFragment.this);
                if (response != null) {
                    if (response.booleanValue()) {
                        ForgotPasswordFragment.this.mResultView.setVisibility(0);
                    } else {
                        ForgotPasswordFragment.this.mEmailError.setText(MainApplication.getRString(R.string.error_invalid_email, new Object[0]));
                        ForgotPasswordFragment.this.mEmailView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                        ForgotPasswordFragment.this.mEmailView.requestFocus();
                    }
                    UIUtils.hideVirtualKeyboard(ForgotPasswordFragment.this.getActivity());
                }
            }
        }
    };

    public static ForgotPasswordFragment newInstance() {
        ForgotPasswordFragment fragment = new ForgotPasswordFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forgot_password, container, false);
        this.mEmailView = (EditText) view.findViewById(R.id.email);
        this.mEmailView.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id != R.integer.submitActionId && id != 0) {
                    return false;
                }
                ForgotPasswordFragment.this.attemptReset();
                return true;
            }
        });
        this.mEmailView.addTextChangedListener(this);
        this.mEmailError = (TextView) view.findViewById(R.id.email_error);
        this.mSubmitButton = view.findViewById(R.id.submit_button);
        this.mSubmitButton.setOnClickListener(this);
        this.mForgotFormView = view.findViewById(R.id.forgot_form);
        this.mProgressView = view.findViewById(R.id.submit_progress);
        this.mResultView = view.findViewById(R.id.result_layout);
        this.mEmailText = (TextView) view.findViewById(R.id.email_text);
        this.mEmailText.setTypeface(UIUtils.getTypeface(getActivity(), Constants.ROBOTO_MEDIUM_FONT_NAME));
        ((TextView) view.findViewById(R.id.thanks_tv)).setTypeface(UIUtils.getTypeface(getActivity(), Constants.ROBOTO_LIGHT_FONT_NAME));
        ((TextView) view.findViewById(R.id.top_description_tv)).setTypeface(UIUtils.getTypeface(getActivity(), Constants.ROBOTO_LIGHT_FONT_NAME));
        ((TextView) view.findViewById(R.id.bottom_description_tv)).setTypeface(UIUtils.getTypeface(getActivity(), Constants.ROBOTO_LIGHT_FONT_NAME));
        ProgressBar pb = (ProgressBar) this.mProgressView.findViewById(R.id.progressBar);
        Drawable d = pb.getIndeterminateDrawable();
        d.setColorFilter(new LightingColorFilter(ViewCompat.MEASURED_STATE_MASK, getResources().getColor(R.color.dark)));
        pb.setIndeterminateDrawable(d);
        return view;
    }

    public void attemptReset() {
        this.mEmailError.setText(BuildConfig.FLAVOR);
        this.mEmailView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        String email = this.mEmailView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(email)) {
            this.mEmailError.setText(MainApplication.getRString(R.string.error_field_required, new Object[0]));
            this.mEmailView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
            focusView = this.mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            this.mEmailError.setText(MainApplication.getRString(R.string.error_invalid_email, new Object[0]));
            this.mEmailView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
            focusView = this.mEmailView;
            cancel = true;
        }
        if (cancel) {
            this.mSubmitButton.setOnClickListener(this);
            focusView.requestFocus();
            return;
        }
        showProgress(true);
        Network.resetPassword(getActivity(), email, this.onResponseReset, null);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    @TargetApi(13)
    public void showProgress(final boolean show) {
        float f = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        int i = 8;
        int i2 = 0;
        int i3;
        if (VERSION.SDK_INT >= 13) {
            int shortAnimTime = getResources().getInteger(17694720);
            View view = this.mForgotFormView;
            if (show) {
                i3 = 8;
            } else {
                i3 = 0;
            }
            view.setVisibility(i3);
            this.mForgotFormView.animate().setDuration((long) shortAnimTime).alpha(show ? 0.0f : DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    ForgotPasswordFragment.this.mForgotFormView.setVisibility(show ? 8 : 0);
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
                    ForgotPasswordFragment.this.mProgressView.setVisibility(show ? 0 : 8);
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
        view2 = this.mForgotFormView;
        if (!show) {
            i = 0;
        }
        view2.setVisibility(i);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit_button:
                view.setOnClickListener(null);
                attemptReset();
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
        if (this.mEmailView.isFocused()) {
            this.mEmailError.setText(null);
            this.mEmailView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
            this.mEmailText.setText(editable);
        }
    }
}
