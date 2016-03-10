package com.mobcrush.mobcrush;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.network.Network;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateSetListener;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

public class SignUpFragment extends Fragment implements OnClickListener, TextWatcher {
    private int mBirthdayDay = -1;
    private TextView mBirthdayError;
    private int mBirthdayMonth = -1;
    private TextView mBirthdayView;
    private int mBirthdayYear = -1;
    private View mConfirmationLayout;
    private boolean mDoNotHideKeyboard;
    private TextView mEmailError;
    private TextView mEmailTextView;
    private EditText mEmailView;
    private View mLoginFormView;
    private TextView mPasswordError;
    private EditText mPasswordView;
    private View mProgressView;
    private View mSignUpButton;
    private View mTOSView;
    private TextView mUsernameError;
    private EditText mUsernameView;
    private Listener<Boolean> onResponseLogin = new Listener<Boolean>() {
        public void onResponse(Boolean response) {
            if (SignUpFragment.this.isAdded()) {
                SignUpFragment.this.mSignUpButton.setOnClickListener(SignUpFragment.this);
                if (response != null && response.booleanValue()) {
                    MixpanelHelper.getInstance(SignUpFragment.this.getActivity()).generateSignUpEvent(SignUpFragment.this.mEmailView.getText().toString(), SignUpFragment.this.mUsernameView.getText().toString(), SignUpFragment.this.getDOBCalendar());
                }
                SignUpFragment.this.showProgress(false);
                if (response != null && response.booleanValue()) {
                    UIUtils.hideVirtualKeyboard(SignUpFragment.this.getActivity());
                    SignUpFragment.this.showEmailConfirmationNotification();
                }
            }
        }
    };

    public static SignUpFragment newInstance() {
        SignUpFragment fragment = new SignUpFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        this.mUsernameError = (TextView) view.findViewById(R.id.username_error);
        this.mUsernameView = (EditText) view.findViewById(R.id.username);
        this.mUsernameView.addTextChangedListener(this);
        this.mEmailError = (TextView) view.findViewById(R.id.email_error);
        this.mEmailView = (EditText) view.findViewById(R.id.email);
        this.mEmailView.addTextChangedListener(this);
        this.mPasswordError = (TextView) view.findViewById(R.id.password_error);
        this.mPasswordView = (EditText) view.findViewById(R.id.password);
        UIUtils.fixPasswordHintIssue(this.mPasswordView);
        this.mPasswordView.addTextChangedListener(this);
        this.mPasswordView.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id != R.integer.signupActionId && id != 0) {
                    return false;
                }
                SignUpFragment.this.attemptSignup();
                return true;
            }
        });
        this.mBirthdayError = (TextView) view.findViewById(R.id.birthday_error);
        this.mBirthdayError.setVisibility(0);
        this.mBirthdayView = (TextView) view.findViewById(R.id.birthday);
        this.mBirthdayView.setVisibility(0);
        this.mBirthdayView.setOnClickListener(this);
        this.mBirthdayView.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    view.performClick();
                }
            }
        });
        this.mSignUpButton = view.findViewById(R.id.signup_button);
        this.mSignUpButton.setOnClickListener(this);
        this.mTOSView = view.findViewById(R.id.tos_tv);
        this.mTOSView.setVisibility(0);
        this.mTOSView.setOnClickListener(this);
        this.mConfirmationLayout = view.findViewById(R.id.confirmation_layout);
        ((TextView) view.findViewById(R.id.thanks_tv)).setTypeface(UIUtils.getTypeface(getActivity(), Constants.ROBOTO_LIGHT_FONT_NAME));
        ((TextView) view.findViewById(R.id.top_description_tv)).setTypeface(UIUtils.getTypeface(getActivity(), Constants.ROBOTO_LIGHT_FONT_NAME));
        ((TextView) view.findViewById(R.id.bottom_description_tv)).setTypeface(UIUtils.getTypeface(getActivity(), Constants.ROBOTO_LIGHT_FONT_NAME));
        this.mEmailTextView = (TextView) view.findViewById(R.id.email_text);
        this.mEmailTextView.setTypeface(UIUtils.getTypeface(getActivity(), Constants.ROBOTO_MEDIUM_FONT_NAME));
        view.findViewById(R.id.done_button).setOnClickListener(this);
        this.mLoginFormView = view.findViewById(R.id.login_form);
        this.mProgressView = view.findViewById(R.id.login_progress);
        ProgressBar pb = (ProgressBar) this.mProgressView.findViewById(R.id.progressBar);
        Drawable d = pb.getIndeterminateDrawable();
        d.setColorFilter(new LightingColorFilter(ViewCompat.MEASURED_STATE_MASK, getResources().getColor(R.color.dark)));
        pb.setIndeterminateDrawable(d);
        Calendar calendar = Calendar.getInstance();
        this.mBirthdayYear = calendar.get(1);
        this.mBirthdayMonth = calendar.get(2);
        this.mBirthdayDay = calendar.get(5);
        return view;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_SIGN_UP);
    }

    public void onDetach() {
        super.onDetach();
        if (!this.mDoNotHideKeyboard) {
            UIUtils.hideVirtualKeyboard(getActivity());
        }
    }

    public void attemptSignup() {
        this.mUsernameError.setText(null);
        this.mUsernameView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        this.mEmailError.setText(null);
        this.mEmailView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        this.mPasswordError.setText(null);
        this.mPasswordView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
        String username = this.mUsernameView.getText().toString();
        String email = this.mEmailView.getText().toString();
        String password = this.mPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(password)) {
            this.mPasswordError.setText(MainApplication.getRString(R.string.error_field_required, new Object[0]));
            this.mPasswordView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
            focusView = this.mPasswordView;
            cancel = true;
        } else if (!(TextUtils.isEmpty(password) || isPasswordValid(password))) {
            this.mPasswordError.setText(MainApplication.getRString(R.string.error_short_password, new Object[0]));
            this.mPasswordView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
            focusView = this.mPasswordView;
            cancel = true;
        }
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
        if (TextUtils.isEmpty(username)) {
            this.mUsernameError.setText(MainApplication.getRString(R.string.error_field_required, new Object[0]));
            this.mUsernameView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
            focusView = this.mUsernameView;
            cancel = true;
        } else {
            if (username.length() < 4) {
                this.mUsernameError.setText(MainApplication.getRString(R.string.error_short_username, new Object[0]));
                this.mUsernameView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                focusView = this.mUsernameView;
                cancel = true;
            }
            if (username.length() > 16) {
                this.mUsernameError.setText(MainApplication.getRString(R.string.error_long_username, new Object[0]));
                this.mUsernameView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                focusView = this.mUsernameView;
                cancel = true;
            }
        }
        if (TextUtils.isEmpty(this.mBirthdayView.getText()) || this.mBirthdayDay < 0 || this.mBirthdayMonth < 0 || this.mBirthdayYear < 0) {
            this.mBirthdayError.setText(MainApplication.getRString(R.string.error_field_required, new Object[0]));
            this.mBirthdayView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
            cancel = true;
        } else {
            Calendar calendar = getDOBCalendar();
            calendar.roll(1, 13);
            if (calendar.compareTo(Calendar.getInstance()) > 0) {
                new Builder(getActivity()).title((int) R.string.sorry).content((int) R.string.error_minimal_age).positiveText(17039370).show();
                cancel = true;
                focusView = null;
            }
        }
        if (cancel) {
            this.mSignUpButton.setOnClickListener(this);
            if (focusView != null) {
                focusView.requestFocus();
                return;
            }
            return;
        }
        showProgress(true);
        Network.registerAccount(getActivity(), username, email, password, getDOBCalendar(), this.onResponseLogin, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                SignUpFragment.this.mSignUpButton.setOnClickListener(SignUpFragment.this);
                SignUpFragment.this.showProgress(false);
                if (error == null) {
                    return;
                }
                if ((error.getMessage() != null && error.getMessage().contains("USR_")) || (error.networkResponse != null && error.networkResponse.statusCode == HttpStatus.SC_OK)) {
                    try {
                        String s = new JSONObject(error.getMessage()).optString(SettingsJsonConstants.APP_STATUS_KEY);
                        if (SignUpFragment.this.isAdded() && !TextUtils.isEmpty(s)) {
                            if (s.equalsIgnoreCase("USR_INVALID_USERNAME")) {
                                SignUpFragment.this.mUsernameError.setText(SignUpFragment.this.getString(R.string.error_invalid_username));
                                SignUpFragment.this.mUsernameView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                            } else if (s.equalsIgnoreCase("USR_LENGTH_MIN")) {
                                SignUpFragment.this.mUsernameError.setText(SignUpFragment.this.getString(R.string.error_short_username));
                                SignUpFragment.this.mUsernameView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                            } else if (s.equalsIgnoreCase("USR_LENGTH_MAX")) {
                                SignUpFragment.this.mUsernameError.setText(SignUpFragment.this.getString(R.string.error_long_username));
                                SignUpFragment.this.mUsernameView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                            } else if (s.equalsIgnoreCase("USR_DUP_USERNAME") || s.equalsIgnoreCase("USR_USERNAME")) {
                                SignUpFragment.this.mUsernameError.setText(SignUpFragment.this.getString(R.string.error_taken_username));
                                SignUpFragment.this.mUsernameView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                            } else if (s.equalsIgnoreCase("USR_INVALID_EMAIL")) {
                                SignUpFragment.this.mEmailError.setText(SignUpFragment.this.getString(R.string.error_invalid_email));
                                SignUpFragment.this.mEmailView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                            } else if (s.equalsIgnoreCase("USR_DUP_EMAIL") || s.equalsIgnoreCase("USR_EMAIL")) {
                                SignUpFragment.this.mEmailError.setText(SignUpFragment.this.getString(R.string.error_taken_email));
                                SignUpFragment.this.mEmailView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                            } else if (s.equalsIgnoreCase("USR_PASSWORD_MIN") || s.equalsIgnoreCase("USR_PASSWORD")) {
                                SignUpFragment.this.mPasswordError.setText(SignUpFragment.this.getString(R.string.error_short_password));
                                SignUpFragment.this.mPasswordView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                            } else if (s.equalsIgnoreCase("USR_PASSWORD_MAX")) {
                                SignUpFragment.this.mPasswordError.setText(SignUpFragment.this.getString(R.string.error_long_password));
                                SignUpFragment.this.mPasswordView.setBackgroundResource(R.drawable.apptheme_textfield_error_holo_light);
                            } else {
                                SignUpFragment.this.showNetworkErrorMessage();
                            }
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        SignUpFragment.this.showNetworkErrorMessage();
                    }
                }
            }
        });
    }

    private void showNetworkErrorMessage() {
        Toast.makeText(getActivity(), R.string.error_network_undeterminated, 1).show();
    }

    private void showEmailConfirmationNotification() {
        try {
            getActivity().findViewById(R.id.toolbar).findViewById(R.id.button_title).setVisibility(8);
        } catch (Throwable th) {
        }
        this.mConfirmationLayout.setVisibility(0);
    }

    private boolean isEmailValid(String email) {
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    @TargetApi(13)
    public void showProgress(final boolean show) {
        float f = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        int i = 8;
        int i2 = 0;
        if (!isAdded()) {
            return;
        }
        if (VERSION.SDK_INT >= 13) {
            int shortAnimTime = getResources().getInteger(17694720);
            this.mLoginFormView.setVisibility(show ? 8 : 0);
            this.mLoginFormView.animate().setDuration((long) shortAnimTime).alpha(show ? 0.0f : DefaultRetryPolicy.DEFAULT_BACKOFF_MULT).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    SignUpFragment.this.mLoginFormView.setVisibility(show ? 8 : 0);
                }
            });
            View view = this.mProgressView;
            if (!show) {
                i2 = 8;
            }
            view.setVisibility(i2);
            ViewPropertyAnimator duration = this.mProgressView.animate().setDuration((long) shortAnimTime);
            if (!show) {
                f = 0.0f;
            }
            duration.alpha(f).setListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    SignUpFragment.this.mProgressView.setVisibility(show ? 0 : 8);
                }
            });
            return;
        }
        int i3;
        View view2 = this.mProgressView;
        if (show) {
            i3 = 0;
        } else {
            i3 = 8;
        }
        view2.setVisibility(i3);
        view = this.mLoginFormView;
        if (!show) {
            i = 0;
        }
        view.setVisibility(i);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.done_button:
                getActivity().setResult(-1);
                getActivity().onBackPressed();
                return;
            case R.id.birthday:
                this.mBirthdayView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
                this.mBirthdayError.setText(BuildConfig.FLAVOR);
                UIUtils.hideVirtualKeyboard(getActivity());
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(new OnDateSetListener() {
                    public void onDateSet(DatePickerDialog datePicker, int year, int monthOfYear, int dayOfMonth) {
                        SignUpFragment.this.mBirthdayDay = dayOfMonth;
                        SignUpFragment.this.mBirthdayMonth = monthOfYear;
                        SignUpFragment.this.mBirthdayYear = year;
                        try {
                            SignUpFragment.this.mBirthdayView.setText(new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(SignUpFragment.this.getDOBCalendar().getTime()));
                        } catch (Throwable th) {
                            SignUpFragment.this.mBirthdayView.setText((SignUpFragment.this.mBirthdayMonth + 1) + "/" + SignUpFragment.this.mBirthdayDay + "/" + SignUpFragment.this.mBirthdayYear);
                        }
                    }
                }, this.mBirthdayYear, this.mBirthdayMonth, this.mBirthdayDay);
                datePickerDialog.setThemeDark(true);
                datePickerDialog.show(getActivity().getFragmentManager(), "Datepickerdialog");
                return;
            case R.id.tos_tv:
                UIUtils.hideVirtualKeyboard(getActivity());
                startActivity(AboutActivity.getIntent(getActivity(), Constants.TERMS_OF_SERVICES_ADDRESS, getString(R.string.title_activity_tos)));
                return;
            case R.id.signup_button:
                view.setOnClickListener(null);
                attemptSignup();
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
        } else if (this.mEmailView.isFocused()) {
            this.mEmailError.setText(null);
            this.mEmailView.setBackgroundResource(R.drawable.apptheme_edit_text_holo_light);
            this.mEmailTextView.setText(editable);
        }
    }

    private Calendar getDOBCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1, this.mBirthdayYear);
        calendar.set(2, this.mBirthdayMonth);
        calendar.set(5, this.mBirthdayDay);
        return calendar;
    }
}
