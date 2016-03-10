package com.mixpanel.android.surveys;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.ImageView;
import com.android.volley.DefaultRetryPolicy;
import com.google.android.exoplayer.DefaultLoadControl;
import com.mixpanel.android.R;

public class FadingImageView extends ImageView {
    private Paint mAlphaGradientPaint;
    private Shader mAlphaGradientShader;
    private Paint mDarkenGradientPaint;
    private Shader mDarkenGradientShader;
    private Matrix mGradientMatrix;
    private int mHeight;
    private int mWidth;

    public FadingImageView(Context context) {
        super(context);
        initFadingImageView();
    }

    public FadingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initFadingImageView();
    }

    public FadingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initFadingImageView();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mHeight = getHeight();
        this.mWidth = getWidth();
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (getResources().getConfiguration().orientation == 1) {
            View bottomWrapperView = getRootView().findViewById(R.id.com_mixpanel_android_notification_bottom_wrapper);
            int bottomWrapperHeight = 0;
            if (!(bottomWrapperView == null || bottomWrapperView.getHeight() == 0)) {
                bottomWrapperHeight = bottomWrapperView.getHeight();
            }
            this.mGradientMatrix.setScale(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, ((float) (parentHeight - bottomWrapperHeight)) + TypedValue.applyDimension(1, 15.0f, getResources().getDisplayMetrics()));
        } else {
            this.mGradientMatrix.setScale(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, (float) parentHeight);
        }
        this.mAlphaGradientShader.setLocalMatrix(this.mGradientMatrix);
        this.mDarkenGradientShader.setLocalMatrix(this.mGradientMatrix);
    }

    public void draw(Canvas canvas) {
        Rect clip = canvas.getClipBounds();
        int restoreTo = canvas.saveLayer(0.0f, 0.0f, (float) clip.width(), (float) clip.height(), null, 31);
        super.draw(canvas);
        if (getResources().getConfiguration().orientation == 1) {
            canvas.drawRect(0.0f, 0.0f, (float) this.mWidth, (float) this.mHeight, this.mAlphaGradientPaint);
        } else {
            canvas.drawRect((float) getPaddingLeft(), (float) getPaddingTop(), (float) (this.mWidth - getPaddingRight()), (float) (this.mHeight - getPaddingBottom()), this.mDarkenGradientPaint);
        }
        canvas.restoreToCount(restoreTo);
    }

    private void initFadingImageView() {
        this.mGradientMatrix = new Matrix();
        this.mAlphaGradientPaint = new Paint();
        this.mAlphaGradientShader = new LinearGradient(0.0f, 0.0f, 0.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, new int[]{ViewCompat.MEASURED_STATE_MASK, ViewCompat.MEASURED_STATE_MASK, -452984832, 0}, new float[]{0.0f, 0.7f, DefaultLoadControl.DEFAULT_HIGH_BUFFER_LOAD, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT}, TileMode.CLAMP);
        this.mAlphaGradientPaint.setShader(this.mAlphaGradientShader);
        this.mAlphaGradientPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        this.mDarkenGradientPaint = new Paint();
        this.mDarkenGradientShader = new LinearGradient(0.0f, 0.0f, 0.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, new int[]{0, 0, ViewCompat.MEASURED_STATE_MASK, ViewCompat.MEASURED_STATE_MASK}, new float[]{0.0f, 0.85f, 0.98f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT}, TileMode.CLAMP);
        this.mDarkenGradientPaint.setShader(this.mDarkenGradientShader);
        this.mAlphaGradientPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
    }
}
