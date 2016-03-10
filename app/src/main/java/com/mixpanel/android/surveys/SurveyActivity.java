package com.mixpanel.android.surveys;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.volley.DefaultRetryPolicy;
import com.google.android.exoplayer.DefaultLoadControl;
import com.mixpanel.android.R;
import com.mixpanel.android.mpmetrics.InAppNotification;
import com.mixpanel.android.mpmetrics.MPConfig;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.mixpanel.android.mpmetrics.MixpanelAPI.People;
import com.mixpanel.android.mpmetrics.Survey;
import com.mixpanel.android.mpmetrics.Survey.Question;
import com.mixpanel.android.mpmetrics.UpdateDisplayState;
import com.mixpanel.android.mpmetrics.UpdateDisplayState.AnswerMap;
import com.mixpanel.android.mpmetrics.UpdateDisplayState.DisplayState;
import com.mixpanel.android.mpmetrics.UpdateDisplayState.DisplayState.InAppNotificationState;
import com.mixpanel.android.mpmetrics.UpdateDisplayState.DisplayState.SurveyState;
import com.mixpanel.android.surveys.CardCarouselLayout.Direction;
import com.mixpanel.android.surveys.CardCarouselLayout.OnQuestionAnsweredListener;
import com.mixpanel.android.surveys.CardCarouselLayout.UnrecognizedAnswerTypeException;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressLint({"ClickableViewAccessibility"})
@TargetApi(16)
public class SurveyActivity extends Activity {
    private static final String CURRENT_QUESTION_BUNDLE_KEY = "com.mixpanel.android.surveys.SurveyActivity.CURRENT_QUESTION_BUNDLE_KEY";
    private static final int GRAY_30PERCENT = Color.argb(SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT, 90, 90, 90);
    public static final String INTENT_ID_KEY = "com.mixpanel.android.surveys.SurveyActivity.INTENT_ID_KEY";
    private static final String LOGTAG = "MixpanelAPI.SrvyActvty";
    private static final int SHADOW_SIZE_THRESHOLD_PX = 100;
    private static final String SURVEY_BEGUN_BUNDLE_KEY = "com.mixpanel.android.surveys.SurveyActivity.SURVEY_BEGIN_BUNDLE_KEY";
    private static final String SURVEY_STATE_BUNDLE_KEY = "com.mixpanel.android.surveys.SurveyActivity.SURVEY_STATE_BUNDLE_KEY";
    private CardCarouselLayout mCardHolder;
    private int mCurrentQuestion = GRAY_30PERCENT;
    private AlertDialog mDialog;
    private int mIntentId = -1;
    private MixpanelAPI mMixpanel;
    private View mNextButton;
    private View mPreviousButton;
    private TextView mProgressTextView;
    private boolean mSurveyBegun = false;
    private UpdateDisplayState mUpdateDisplayState;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mIntentId = getIntent().getIntExtra(INTENT_ID_KEY, ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
        this.mUpdateDisplayState = UpdateDisplayState.claimDisplayState(this.mIntentId);
        if (this.mUpdateDisplayState == null) {
            Log.e(LOGTAG, "SurveyActivity intent received, but nothing was found to show.");
            finish();
            return;
        }
        this.mMixpanel = MixpanelAPI.getInstance(this, this.mUpdateDisplayState.getToken());
        if (isShowingInApp()) {
            onCreateInAppNotification(savedInstanceState);
        } else if (isShowingSurvey()) {
            onCreateSurvey(savedInstanceState);
        } else {
            finish();
        }
    }

    private void onCreateInAppNotification(Bundle savedInstanceState) {
        setContentView(R.layout.com_mixpanel_android_activity_notification_full);
        ImageView backgroundImage = (ImageView) findViewById(R.id.com_mixpanel_android_notification_gradient);
        FadingImageView inAppImageView = (FadingImageView) findViewById(R.id.com_mixpanel_android_notification_image);
        TextView titleView = (TextView) findViewById(R.id.com_mixpanel_android_notification_title);
        TextView subtextView = (TextView) findViewById(R.id.com_mixpanel_android_notification_subtext);
        Button ctaButton = (Button) findViewById(R.id.com_mixpanel_android_notification_button);
        LinearLayout closeButtonWrapper = (LinearLayout) findViewById(R.id.com_mixpanel_android_button_exit_wrapper);
        InAppNotification inApp = ((InAppNotificationState) this.mUpdateDisplayState.getDisplayState()).getInAppNotification();
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        if (getResources().getConfiguration().orientation == 1) {
            LayoutParams params = (RelativeLayout.LayoutParams) closeButtonWrapper.getLayoutParams();
            params.setMargins(GRAY_30PERCENT, GRAY_30PERCENT, GRAY_30PERCENT, (int) (((float) size.y) * 0.06f));
            closeButtonWrapper.setLayoutParams(params);
        }
        Drawable gradientDrawable = new GradientDrawable(Orientation.LEFT_RIGHT, new int[]{-446668676, -448247715, -451405793, -451405793});
        gradientDrawable.setGradientType(1);
        if (getResources().getConfiguration().orientation == 2) {
            gradientDrawable.setGradientCenter(0.25f, 0.5f);
            gradientDrawable.setGradientRadius(((float) Math.min(size.x, size.y)) * DefaultLoadControl.DEFAULT_HIGH_BUFFER_LOAD);
        } else {
            gradientDrawable.setGradientCenter(0.5f, 0.33f);
            gradientDrawable.setGradientRadius(((float) Math.min(size.x, size.y)) * 0.7f);
        }
        setViewBackground(backgroundImage, gradientDrawable);
        titleView.setText(inApp.getTitle());
        subtextView.setText(inApp.getBody());
        Bitmap inAppImage = inApp.getImage();
        inAppImageView.setBackgroundResource(R.drawable.com_mixpanel_android_square_dropshadow);
        if (inAppImage.getWidth() < SHADOW_SIZE_THRESHOLD_PX || inAppImage.getHeight() < SHADOW_SIZE_THRESHOLD_PX) {
            inAppImageView.setBackgroundResource(R.drawable.com_mixpanel_android_square_nodropshadow);
        } else if (Color.alpha(Bitmap.createScaledBitmap(inAppImage, 1, 1, false).getPixel(GRAY_30PERCENT, GRAY_30PERCENT)) < SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
            inAppImageView.setBackgroundResource(R.drawable.com_mixpanel_android_square_nodropshadow);
        }
        inAppImageView.setImageBitmap(inAppImage);
        String ctaUrl = inApp.getCallToActionUrl();
        if (ctaUrl != null && ctaUrl.length() > 0) {
            ctaButton.setText(inApp.getCallToAction());
        }
        final InAppNotification inAppNotification = inApp;
        ctaButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String uriString = inAppNotification.getCallToActionUrl();
                if (uriString != null && uriString.length() > 0) {
                    try {
                        try {
                            SurveyActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(uriString)));
                            SurveyActivity.this.mMixpanel.getPeople().trackNotification("$campaign_open", inAppNotification);
                        } catch (ActivityNotFoundException e) {
                            Log.i(SurveyActivity.LOGTAG, "User doesn't have an activity for notification URI");
                        }
                    } catch (IllegalArgumentException e2) {
                        Log.i(SurveyActivity.LOGTAG, "Can't parse notification URI, will not take any action", e2);
                        return;
                    }
                }
                SurveyActivity.this.finish();
                UpdateDisplayState.releaseDisplayState(SurveyActivity.this.mIntentId);
            }
        });
        ctaButton.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 0) {
                    v.setBackgroundResource(R.drawable.com_mixpanel_android_cta_button_highlight);
                } else {
                    v.setBackgroundResource(R.drawable.com_mixpanel_android_cta_button);
                }
                return false;
            }
        });
        closeButtonWrapper.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SurveyActivity.this.finish();
                UpdateDisplayState.releaseDisplayState(SurveyActivity.this.mIntentId);
            }
        });
        ScaleAnimation scale = new ScaleAnimation(0.95f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.95f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 1, 0.5f, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        scale.setDuration(200);
        inAppImageView.startAnimation(scale);
        TranslateAnimation translate = new TranslateAnimation(1, 0.0f, 1, 0.0f, 1, 0.5f, 1, 0.0f);
        translate.setInterpolator(new DecelerateInterpolator());
        translate.setDuration(200);
        titleView.startAnimation(translate);
        subtextView.startAnimation(translate);
        ctaButton.startAnimation(translate);
        closeButtonWrapper.startAnimation(AnimationUtils.loadAnimation(this, R.anim.com_mixpanel_android_fade_in));
    }

    private void onCreateSurvey(Bundle savedInstanceState) {
        requestOrientationLock();
        if (savedInstanceState != null) {
            this.mCurrentQuestion = savedInstanceState.getInt(CURRENT_QUESTION_BUNDLE_KEY, GRAY_30PERCENT);
            this.mSurveyBegun = savedInstanceState.getBoolean(SURVEY_BEGUN_BUNDLE_KEY);
        }
        if (this.mUpdateDisplayState.getDistinctId() == null) {
            Log.i(LOGTAG, "Can't show a survey to a user with no distinct id set");
            finish();
            return;
        }
        setContentView(R.layout.com_mixpanel_android_activity_survey);
        Bitmap background = getSurveyState().getBackground();
        if (background == null) {
            findViewById(R.id.com_mixpanel_android_activity_survey_id).setBackgroundColor(GRAY_30PERCENT);
        } else {
            getWindow().setBackgroundDrawable(new BitmapDrawable(getResources(), background));
        }
        this.mPreviousButton = findViewById(R.id.com_mixpanel_android_button_previous);
        this.mNextButton = findViewById(R.id.com_mixpanel_android_button_next);
        this.mProgressTextView = (TextView) findViewById(R.id.com_mixpanel_android_progress_text);
        this.mCardHolder = (CardCarouselLayout) findViewById(R.id.com_mixpanel_android_question_card_holder);
        this.mCardHolder.setOnQuestionAnsweredListener(new OnQuestionAnsweredListener() {
            public void onQuestionAnswered(Question question, String answer) {
                SurveyActivity.this.saveAnswer(question, answer);
                SurveyActivity.this.goToNextQuestion();
            }
        });
    }

    protected void onStart() {
        super.onStart();
        DisplayState displayState = this.mUpdateDisplayState.getDisplayState();
        if (displayState != null && displayState.getType() == SurveyState.TYPE) {
            onStartSurvey();
        }
    }

    private void onStartSurvey() {
        if (!this.mSurveyBegun) {
            if (!MPConfig.getInstance(this).getTestMode()) {
                trackSurveyAttempted();
            }
            Builder alertBuilder = new Builder(this);
            alertBuilder.setTitle(R.string.com_mixpanel_android_survey_prompt_dialog_title);
            alertBuilder.setMessage(R.string.com_mixpanel_android_survey_prompt_dialog_message);
            alertBuilder.setPositiveButton(R.string.com_mixpanel_android_sure, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SurveyActivity.this.findViewById(R.id.com_mixpanel_android_activity_survey_id).setVisibility(SurveyActivity.GRAY_30PERCENT);
                    SurveyActivity.this.mSurveyBegun = true;
                    SurveyActivity.this.showQuestion(SurveyActivity.this.mCurrentQuestion);
                }
            });
            alertBuilder.setNegativeButton(R.string.com_mixpanel_android_no_thanks, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SurveyActivity.this.finish();
                }
            });
            alertBuilder.setCancelable(false);
            this.mDialog = alertBuilder.create();
            this.mDialog.show();
        }
    }

    protected void onPause() {
        super.onPause();
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
        }
    }

    protected void onDestroy() {
        if (isShowingSurvey()) {
            onDestroySurvey();
        }
        super.onDestroy();
    }

    @SuppressLint({"SimpleDateFormat"})
    private void onDestroySurvey() {
        if (this.mMixpanel != null) {
            if (this.mUpdateDisplayState != null) {
                SurveyState surveyState = getSurveyState();
                Survey survey = surveyState.getSurvey();
                List<Question> questionList = survey.getQuestions();
                People people = this.mMixpanel.getPeople().withIdentity(this.mUpdateDisplayState.getDistinctId());
                people.append("$responses", Integer.valueOf(survey.getCollectionId()));
                AnswerMap answers = surveyState.getAnswers();
                for (Question question : questionList) {
                    String answerString = answers.get(Integer.valueOf(question.getId()));
                    if (answerString != null) {
                        try {
                            JSONObject answerJson = new JSONObject();
                            answerJson.put("$survey_id", survey.getId());
                            answerJson.put("$collection_id", survey.getCollectionId());
                            answerJson.put("$question_id", question.getId());
                            answerJson.put("$question_type", question.getType().toString());
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                            answerJson.put("$time", dateFormat.format(new Date()));
                            answerJson.put("$value", answerString);
                            people.append("$answers", answerJson);
                        } catch (JSONException e) {
                            Log.e(LOGTAG, "Couldn't record user's answer.", e);
                        }
                    }
                }
            }
            this.mMixpanel.flush();
        }
        UpdateDisplayState.releaseDisplayState(this.mIntentId);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (isShowingSurvey()) {
            onSaveInstanceStateSurvey(outState);
        }
    }

    private void onSaveInstanceStateSurvey(Bundle outState) {
        outState.putBoolean(SURVEY_BEGUN_BUNDLE_KEY, this.mSurveyBegun);
        outState.putInt(CURRENT_QUESTION_BUNDLE_KEY, this.mCurrentQuestion);
        outState.putParcelable(SURVEY_STATE_BUNDLE_KEY, this.mUpdateDisplayState);
    }

    public void onBackPressed() {
        if (!isShowingSurvey() || this.mCurrentQuestion <= 0) {
            if (isShowingInApp()) {
                UpdateDisplayState.releaseDisplayState(this.mIntentId);
            }
            super.onBackPressed();
            return;
        }
        goToPreviousQuestion();
    }

    public void goToPreviousQuestion(View v) {
        goToPreviousQuestion();
    }

    public void goToNextQuestion(View v) {
        goToNextQuestion();
    }

    public void completeSurvey(View v) {
        completeSurvey();
    }

    private SurveyState getSurveyState() {
        return (SurveyState) this.mUpdateDisplayState.getDisplayState();
    }

    private boolean isShowingSurvey() {
        if (this.mUpdateDisplayState == null) {
            return false;
        }
        return SurveyState.TYPE.equals(this.mUpdateDisplayState.getDisplayState().getType());
    }

    private boolean isShowingInApp() {
        if (this.mUpdateDisplayState == null) {
            return false;
        }
        return InAppNotificationState.TYPE.equals(this.mUpdateDisplayState.getDisplayState().getType());
    }

    private void trackSurveyAttempted() {
        Survey survey = getSurveyState().getSurvey();
        People people = this.mMixpanel.getPeople().withIdentity(this.mUpdateDisplayState.getDistinctId());
        people.append("$surveys", Integer.valueOf(survey.getId()));
        people.append("$collections", Integer.valueOf(survey.getCollectionId()));
    }

    private void goToPreviousQuestion() {
        if (this.mCurrentQuestion > 0) {
            showQuestion(this.mCurrentQuestion - 1);
        } else {
            completeSurvey();
        }
    }

    private void goToNextQuestion() {
        if (this.mCurrentQuestion < getSurveyState().getSurvey().getQuestions().size() - 1) {
            showQuestion(this.mCurrentQuestion + 1);
        } else {
            completeSurvey();
        }
    }

    private void showQuestion(int idx) {
        SurveyState surveyState = getSurveyState();
        List<Question> questions = surveyState.getSurvey().getQuestions();
        if (idx == 0 || questions.size() == 0) {
            this.mPreviousButton.setEnabled(false);
        } else {
            this.mPreviousButton.setEnabled(true);
        }
        if (idx >= questions.size() - 1) {
            this.mNextButton.setEnabled(false);
        } else {
            this.mNextButton.setEnabled(true);
        }
        int oldQuestion = this.mCurrentQuestion;
        this.mCurrentQuestion = idx;
        Question question = (Question) questions.get(idx);
        String answerValue = surveyState.getAnswers().get(Integer.valueOf(question.getId()));
        if (oldQuestion < idx) {
            try {
                this.mCardHolder.moveTo(question, answerValue, Direction.FORWARD);
            } catch (UnrecognizedAnswerTypeException e) {
                goToNextQuestion();
                return;
            }
        } else if (oldQuestion > idx) {
            this.mCardHolder.moveTo(question, answerValue, Direction.BACKWARD);
        } else {
            this.mCardHolder.replaceTo(question, answerValue);
        }
        if (questions.size() > 1) {
            this.mProgressTextView.setText(BuildConfig.FLAVOR + (idx + 1) + " of " + questions.size());
        } else {
            this.mProgressTextView.setText(BuildConfig.FLAVOR);
        }
    }

    private void saveAnswer(Question question, String answer) {
        getSurveyState().getAnswers().put(Integer.valueOf(question.getId()), answer.toString());
    }

    @SuppressLint({"NewApi"})
    private void setViewBackground(View v, Drawable d) {
        if (VERSION.SDK_INT < 16) {
            v.setBackgroundDrawable(d);
        } else {
            v.setBackground(d);
        }
    }

    @SuppressLint({"NewApi"})
    private void requestOrientationLock() {
        if (VERSION.SDK_INT >= 18) {
            setRequestedOrientation(14);
            return;
        }
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == 2) {
            setRequestedOrientation(GRAY_30PERCENT);
        } else if (currentOrientation == 1) {
            setRequestedOrientation(1);
        }
    }

    private void completeSurvey() {
        finish();
    }
}
