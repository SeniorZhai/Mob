package com.mixpanel.android.surveys;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.android.volley.DefaultRetryPolicy;
import com.google.android.exoplayer.DefaultLoadControl;
import com.mixpanel.android.R;
import com.mixpanel.android.mpmetrics.Survey.Question;
import com.mixpanel.android.mpmetrics.Survey.QuestionType;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.List;

public class CardCarouselLayout extends ViewGroup {
    private static final long ANIMATION_DURATION_MILLIS = 330;
    private static final long ANIMATION_ROTATION_MILLIS = 198;
    private static int EXIT_ANGLE = 45;
    private static float EXIT_ROTATION_CENTER_X = 0.5f;
    private static float EXIT_ROTATION_CENTER_Y = 0.5f;
    private static float EXIT_SIZE = DefaultLoadControl.DEFAULT_HIGH_BUFFER_LOAD;
    private QuestionCard mBackupCard;
    private OnQuestionAnsweredListener mListener = null;
    private final List<View> mMatchParentChildren = new ArrayList(1);
    private QuestionCard mVisibleCard;

    static /* synthetic */ class AnonymousClass2 {
        static final /* synthetic */ int[] $SwitchMap$com$mixpanel$android$surveys$CardCarouselLayout$Direction = new int[Direction.values().length];

        static {
            try {
                $SwitchMap$com$mixpanel$android$surveys$CardCarouselLayout$Direction[Direction.FORWARD.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mixpanel$android$surveys$CardCarouselLayout$Direction[Direction.BACKWARD.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private static class ChoiceAdapter implements ListAdapter {
        private static final int VIEW_TYPE_FIRST = 0;
        private static final int VIEW_TYPE_LAST = 1;
        private static final int VIEW_TYPE_MAX = 3;
        private static final int VIEW_TYPE_MIDDLE = 2;
        private final List<String> mChoices;
        private final LayoutInflater mInflater;

        public ChoiceAdapter(List<String> choices, LayoutInflater inflater) {
            this.mChoices = choices;
            this.mInflater = inflater;
        }

        public int getCount() {
            return this.mChoices.size();
        }

        public String getItem(int position) {
            return (String) this.mChoices.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public int getItemViewType(int position) {
            if (position == 0) {
                return VIEW_TYPE_FIRST;
            }
            if (position == this.mChoices.size() - 1) {
                return VIEW_TYPE_LAST;
            }
            return VIEW_TYPE_MIDDLE;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            int viewId = -1;
            if (convertView == null) {
                switch (getItemViewType(position)) {
                    case VIEW_TYPE_FIRST /*0*/:
                        viewId = R.layout.com_mixpanel_android_first_choice_answer;
                        break;
                    case VIEW_TYPE_LAST /*1*/:
                        viewId = R.layout.com_mixpanel_android_last_choice_answer;
                        break;
                    case VIEW_TYPE_MIDDLE /*2*/:
                        viewId = R.layout.com_mixpanel_android_middle_choice_answer;
                        break;
                }
                convertView = this.mInflater.inflate(viewId, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.com_mixpanel_android_multiple_choice_answer_text)).setText((String) this.mChoices.get(position));
            return convertView;
        }

        public int getViewTypeCount() {
            return VIEW_TYPE_MAX;
        }

        public boolean hasStableIds() {
            return true;
        }

        public boolean isEmpty() {
            return this.mChoices.isEmpty();
        }

        public void registerDataSetObserver(DataSetObserver observer) {
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
        }

        public boolean areAllItemsEnabled() {
            return true;
        }

        public boolean isEnabled(int arg0) {
            return true;
        }
    }

    public enum Direction {
        FORWARD,
        BACKWARD
    }

    public interface OnQuestionAnsweredListener {
        void onQuestionAnswered(Question question, String str);
    }

    private class QuestionCard {
        private final View mCardView;
        private final ListView mChoiceView;
        private final TextView mEditAnswerView;
        private final TextView mPromptView;
        private Question mQuestion;

        public QuestionCard(View cardView) {
            this.mCardView = cardView;
            this.mPromptView = (TextView) cardView.findViewWithTag("com_mixpanel_android_TAG_prompt_text");
            this.mEditAnswerView = (EditText) cardView.findViewWithTag("com_mixpanel_android_TAG_text_answer");
            this.mChoiceView = (ListView) cardView.findViewWithTag("com_mixpanel_android_TAG_choice_list");
            this.mEditAnswerView.setText(BuildConfig.FLAVOR);
            this.mEditAnswerView.setOnEditorActionListener(new OnEditorActionListener(CardCarouselLayout.this) {
                public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                    boolean enterKeyDown;
                    if (event != null && event.getKeyCode() == 66 && event.getAction() == 0 && (event.getFlags() & 32) == 0) {
                        enterKeyDown = true;
                    } else {
                        enterKeyDown = false;
                    }
                    if (!enterKeyDown && actionId != 6) {
                        return false;
                    }
                    view.clearComposingText();
                    if (CardCarouselLayout.this.mListener != null) {
                        CardCarouselLayout.this.mListener.onQuestionAnswered(QuestionCard.this.mQuestion, view.getText().toString());
                    }
                    return true;
                }
            });
            this.mChoiceView.setOnItemClickListener(new OnItemClickListener(CardCarouselLayout.this) {
                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    if (CardCarouselLayout.this.mListener != null) {
                        final String answer = parent.getItemAtPosition(position).toString();
                        CardCarouselLayout.this.postDelayed(new Runnable() {
                            public void run() {
                                CardCarouselLayout.this.mListener.onQuestionAnswered(QuestionCard.this.mQuestion, answer);
                            }
                        }, 165);
                    }
                }
            });
        }

        public View getView() {
            return this.mCardView;
        }

        public void showQuestionOnCard(Question question, String answerOrNull) throws UnrecognizedAnswerTypeException {
            this.mQuestion = question;
            this.mPromptView.setText(this.mQuestion.getPrompt());
            InputMethodManager inputMethodManager = (InputMethodManager) this.mCardView.getContext().getSystemService("input_method");
            QuestionType questionType = question.getType();
            if (QuestionType.TEXT == questionType) {
                this.mChoiceView.setVisibility(8);
                this.mEditAnswerView.setVisibility(0);
                if (answerOrNull != null) {
                    this.mEditAnswerView.setText(answerOrNull);
                }
                if (CardCarouselLayout.this.getResources().getConfiguration().orientation == 1) {
                    this.mEditAnswerView.requestFocus();
                    inputMethodManager.showSoftInput(this.mEditAnswerView, 0);
                } else {
                    inputMethodManager.hideSoftInputFromWindow(this.mCardView.getWindowToken(), 0);
                }
            } else if (QuestionType.MULTIPLE_CHOICE == questionType) {
                inputMethodManager.hideSoftInputFromWindow(this.mCardView.getWindowToken(), 0);
                this.mChoiceView.setVisibility(0);
                this.mEditAnswerView.setVisibility(8);
                ChoiceAdapter answerAdapter = new ChoiceAdapter(question.getChoices(), LayoutInflater.from(CardCarouselLayout.this.getContext()));
                this.mChoiceView.setAdapter(answerAdapter);
                this.mChoiceView.clearChoices();
                if (answerOrNull != null) {
                    for (int i = 0; i < answerAdapter.getCount(); i++) {
                        if (answerAdapter.getItem(i).equals(answerOrNull)) {
                            this.mChoiceView.setItemChecked(i, true);
                        }
                    }
                }
            } else {
                throw new UnrecognizedAnswerTypeException("No way to display question type " + questionType);
            }
            this.mCardView.invalidate();
        }
    }

    public static class UnrecognizedAnswerTypeException extends Exception {
        private static final long serialVersionUID = -6040399928243560328L;

        private UnrecognizedAnswerTypeException(String string) {
            super(string);
        }
    }

    public CardCarouselLayout(Context context) {
        super(context);
        initCards(context);
    }

    public CardCarouselLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initCards(context);
    }

    public CardCarouselLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initCards(context);
    }

    public boolean shouldDelayChildPressedState() {
        return false;
    }

    public void setOnQuestionAnsweredListener(OnQuestionAnsweredListener listener) {
        this.mListener = listener;
    }

    public void moveTo(Question question, String answerOrNull, Direction direction) throws UnrecognizedAnswerTypeException {
        QuestionCard tmp = this.mBackupCard;
        this.mBackupCard = this.mVisibleCard;
        this.mVisibleCard = tmp;
        this.mVisibleCard.showQuestionOnCard(question, answerOrNull);
        final View viewShowing = this.mBackupCard.getView();
        View viewToShow = this.mVisibleCard.getView();
        viewShowing.setVisibility(0);
        viewToShow.setVisibility(0);
        Animation exit = null;
        Animation entrance = null;
        switch (AnonymousClass2.$SwitchMap$com$mixpanel$android$surveys$CardCarouselLayout$Direction[direction.ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                exit = exitLeft();
                entrance = enterRight();
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                exit = exitRight();
                entrance = enterLeft();
                break;
        }
        exit.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(Animation animation) {
                viewShowing.setVisibility(8);
            }

            public void onAnimationRepeat(Animation animation) {
            }

            public void onAnimationStart(Animation animation) {
            }
        });
        viewShowing.startAnimation(exit);
        viewToShow.startAnimation(entrance);
        invalidate();
    }

    public void replaceTo(Question question, String answerOrNull) throws UnrecognizedAnswerTypeException {
        this.mVisibleCard.showQuestionOnCard(question, answerOrNull);
        removeAllViews();
        addView(this.mVisibleCard.getView());
        addView(this.mBackupCard.getView());
        invalidate();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        boolean measureMatchParentChildren = (MeasureSpec.getMode(widthMeasureSpec) == 1073741824 && MeasureSpec.getMode(heightMeasureSpec) == 1073741824) ? false : true;
        this.mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != 8) {
                LayoutParams lp = child.getLayoutParams();
                child.measure(getChildMeasureSpec(widthMeasureSpec, 0, lp.width), getChildMeasureSpec(heightMeasureSpec, 0, lp.height));
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight());
                if (measureMatchParentChildren && (lp.width == -1 || lp.height == -1)) {
                    this.mMatchParentChildren.add(child);
                }
            }
        }
        setMeasuredDimension(resolveSize(Math.max(maxWidth, getSuggestedMinimumWidth()), widthMeasureSpec), resolveSize(Math.max(maxHeight, getSuggestedMinimumHeight()), heightMeasureSpec));
        for (View child2 : this.mMatchParentChildren) {
            int childWidthMeasureSpec;
            int childHeightMeasureSpec;
            lp = child2.getLayoutParams();
            if (lp.width == -1) {
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(), 1073741824);
            } else {
                childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
            }
            if (lp.height == -1) {
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredHeight(), 1073741824);
            } else {
                childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0, lp.height);
            }
            child2.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        View visible = this.mVisibleCard.getView();
        int visibleWidth = 0;
        if (visible.getVisibility() != 8) {
            visibleWidth = visible.getMeasuredWidth();
            visible.layout(0, 0, visibleWidth, visible.getMeasuredHeight());
        }
        View backup = this.mBackupCard.getView();
        if (backup.getVisibility() != 8) {
            backup.layout(visibleWidth, 0, visibleWidth + backup.getMeasuredWidth(), backup.getMeasuredHeight());
        }
    }

    private void initCards(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v1 = inflater.inflate(R.layout.com_mixpanel_android_question_card, this, false);
        this.mVisibleCard = new QuestionCard(v1);
        View v2 = inflater.inflate(R.layout.com_mixpanel_android_question_card, this, false);
        this.mBackupCard = new QuestionCard(v2);
        addView(v1);
        addView(v2);
    }

    private Animation enterRight() {
        AnimationSet set = new AnimationSet(false);
        RotateAnimation rotateIn = new RotateAnimation((float) EXIT_ANGLE, 0.0f, 1, EXIT_ROTATION_CENTER_X, 1, EXIT_ROTATION_CENTER_Y);
        rotateIn.setDuration(ANIMATION_ROTATION_MILLIS);
        set.addAnimation(rotateIn);
        ScaleAnimation scaleUp = new ScaleAnimation(EXIT_SIZE, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, EXIT_SIZE, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 1, EXIT_ROTATION_CENTER_X, 1, EXIT_ROTATION_CENTER_Y);
        scaleUp.setDuration(ANIMATION_ROTATION_MILLIS);
        set.addAnimation(scaleUp);
        TranslateAnimation slideX = new TranslateAnimation(2, 1.3f, 2, 0.0f, 2, 0.0f, 2, 0.0f);
        slideX.setDuration(ANIMATION_DURATION_MILLIS);
        set.addAnimation(slideX);
        return set;
    }

    private Animation exitRight() {
        AnimationSet set = new AnimationSet(false);
        RotateAnimation rotateOut = new RotateAnimation(0.0f, (float) EXIT_ANGLE, 1, EXIT_ROTATION_CENTER_X, 1, EXIT_ROTATION_CENTER_Y);
        rotateOut.setDuration(ANIMATION_ROTATION_MILLIS);
        rotateOut.setStartOffset(132);
        set.addAnimation(rotateOut);
        ScaleAnimation scaleDown = new ScaleAnimation(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, EXIT_SIZE, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, EXIT_SIZE, 1, EXIT_ROTATION_CENTER_X, 1, EXIT_ROTATION_CENTER_Y);
        scaleDown.setDuration(ANIMATION_ROTATION_MILLIS);
        scaleDown.setStartOffset(132);
        set.addAnimation(scaleDown);
        TranslateAnimation slideX = new TranslateAnimation(2, -1.0f, 2, 0.3f, 2, 0.0f, 2, 0.0f);
        slideX.setInterpolator(new AccelerateInterpolator());
        slideX.setDuration(ANIMATION_DURATION_MILLIS);
        set.addAnimation(slideX);
        return set;
    }

    private Animation enterLeft() {
        AnimationSet set = new AnimationSet(false);
        RotateAnimation rotateIn = new RotateAnimation((float) (-EXIT_ANGLE), 0.0f, 1, EXIT_ROTATION_CENTER_X, 1, EXIT_ROTATION_CENTER_Y);
        rotateIn.setDuration(ANIMATION_ROTATION_MILLIS);
        set.addAnimation(rotateIn);
        ScaleAnimation scaleUp = new ScaleAnimation(EXIT_SIZE, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, EXIT_SIZE, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 1, EXIT_ROTATION_CENTER_X, 1, EXIT_ROTATION_CENTER_Y);
        scaleUp.setDuration(ANIMATION_ROTATION_MILLIS);
        set.addAnimation(scaleUp);
        TranslateAnimation slideX = new TranslateAnimation(2, -1.3f, 2, 0.0f, 2, 0.0f, 2, 0.0f);
        slideX.setDuration(ANIMATION_DURATION_MILLIS);
        set.addAnimation(slideX);
        return set;
    }

    private Animation exitLeft() {
        AnimationSet set = new AnimationSet(false);
        RotateAnimation rotateOut = new RotateAnimation(0.0f, (float) (-EXIT_ANGLE), 1, EXIT_ROTATION_CENTER_X, 1, EXIT_ROTATION_CENTER_Y);
        rotateOut.setDuration(ANIMATION_DURATION_MILLIS);
        rotateOut.setStartOffset(132);
        set.addAnimation(rotateOut);
        ScaleAnimation scaleDown = new ScaleAnimation(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, EXIT_SIZE, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, EXIT_SIZE, 1, EXIT_ROTATION_CENTER_X, 1, EXIT_ROTATION_CENTER_Y);
        scaleDown.setDuration(ANIMATION_DURATION_MILLIS);
        scaleDown.setStartOffset(132);
        set.addAnimation(scaleDown);
        TranslateAnimation slideX = new TranslateAnimation(2, -1.0f, 2, -2.3f, 2, 0.0f, 2, 0.0f);
        slideX.setInterpolator(new AccelerateInterpolator());
        slideX.setDuration(ANIMATION_DURATION_MILLIS);
        set.addAnimation(slideX);
        return set;
    }
}
