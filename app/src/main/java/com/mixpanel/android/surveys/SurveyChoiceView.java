package com.mixpanel.android.surveys;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CheckedTextView;
import com.android.volley.DefaultRetryPolicy;
import com.mobcrush.mobcrush.R;
import io.fabric.sdk.android.services.common.CommonUtils;

public class SurveyChoiceView extends CheckedTextView {
    private static final int ANIMATION_DURATION = 300;
    private static final int BOX_PADDING_LEFT_DP = 22;
    private static final int BOX_PADDING_TOP_DP = 12;
    private static final int CHECKMARK_HEIGHT_DP = 14;
    private float mCheckmarkLeftOffset;
    private Drawable mSurveyChoiceCheckMark;
    private float mTextLeftOffset;

    private class SetCheckAnimation extends Animation {
        private SetCheckAnimation() {
        }

        public boolean willChangeTransformationMatrix() {
            return false;
        }

        public boolean willChangeBounds() {
            return false;
        }

        protected void applyTransformation(float interpolatedTime, Transformation t) {
            float checkmarkOffset = 0.0f;
            float textOffset = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
            if (interpolatedTime <= 0.5f) {
                checkmarkOffset = interpolatedTime - 0.5f;
            } else {
                textOffset = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT + ((interpolatedTime - 0.5f) * 2.0f);
            }
            SurveyChoiceView.this.mCheckmarkLeftOffset = checkmarkOffset;
            SurveyChoiceView.this.mTextLeftOffset = textOffset;
            SurveyChoiceView.this.requestLayout();
        }
    }

    public SurveyChoiceView(Context context) {
        super(context);
        initSurveyChoiceView();
    }

    public SurveyChoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSurveyChoiceView();
    }

    public SurveyChoiceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initSurveyChoiceView();
    }

    public void setCheckMarkDrawable(Drawable d) {
        super.setCheckMarkDrawable(d);
        this.mSurveyChoiceCheckMark = d;
    }

    public void setChecked(boolean checked) {
        boolean wasChecked = isChecked();
        super.setChecked(checked);
        if (isChecked() && !wasChecked) {
            Animation transition = new SetCheckAnimation();
            transition.setDuration(300);
            startAnimation(transition);
        }
    }

    protected void onDraw(Canvas canvas) {
        Drawable checkMarkDrawable = this.mSurveyChoiceCheckMark;
        float density = getResources().getDisplayMetrics().density;
        int checkmarkWidth = 0;
        if (checkMarkDrawable != null && isChecked()) {
            checkmarkWidth = (int) (14.0f * density);
        }
        int checkmarkHeight = checkmarkWidth;
        int boxPaddingTop = (int) (12.0f * density);
        int boxPaddingLeft = (int) (22.0f * density);
        setCheckMarkDrawable(null);
        setPadding((int) (((float) boxPaddingLeft) + (this.mTextLeftOffset * ((float) checkmarkWidth))), boxPaddingTop, boxPaddingLeft, boxPaddingTop);
        super.onDraw(canvas);
        int checkPaddingLeft = (int) (((float) boxPaddingLeft) + (this.mCheckmarkLeftOffset * ((float) checkmarkWidth)));
        setPadding(checkPaddingLeft, boxPaddingTop, boxPaddingLeft, boxPaddingTop);
        setCheckMarkDrawable(checkMarkDrawable);
        if (checkMarkDrawable != null) {
            int y = 0;
            switch (getGravity() & 112) {
                case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                    y = (getHeight() - checkmarkHeight) / 2;
                    break;
                case R.styleable.Theme_listChoiceBackgroundIndicator /*80*/:
                    y = getHeight() - checkmarkHeight;
                    break;
            }
            int top = y;
            int left = checkPaddingLeft;
            int scrollX = getScrollX() + left;
            checkMarkDrawable.setBounds(scrollX, top, getScrollX() + (left + checkmarkWidth), top + checkmarkHeight);
            checkMarkDrawable.draw(canvas);
        }
        setPadding(boxPaddingLeft, boxPaddingTop, boxPaddingLeft, boxPaddingTop);
    }

    private void initSurveyChoiceView() {
        this.mCheckmarkLeftOffset = 0.0f;
        this.mTextLeftOffset = 1.5f;
    }
}
