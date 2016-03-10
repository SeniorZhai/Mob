package com.mobcrush.mobcrush.broadcast;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View.MeasureSpec;

public class AutoFitTextureView extends TextureView {
    private static final int SCALE = 22;
    private int mRatioHeight;
    private int mRatioWidth;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mRatioWidth = 0;
        this.mRatioHeight = 0;
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        this.mRatioWidth = width;
        this.mRatioHeight = height;
        requestLayout();
    }

    public void rotateAspectRatio() {
        int temp = this.mRatioWidth;
        this.mRatioWidth = this.mRatioHeight;
        this.mRatioHeight = temp;
        requestLayout();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = (MeasureSpec.getSize(widthMeasureSpec) * SCALE) / 100;
        int height = (MeasureSpec.getSize(heightMeasureSpec) * SCALE) / 100;
        if (this.mRatioWidth == 0 || this.mRatioHeight == 0) {
            setMeasuredDimension(width, height);
        } else if (width < (this.mRatioWidth * height) / this.mRatioHeight) {
            setMeasuredDimension(width, (this.mRatioHeight * width) / this.mRatioWidth);
        } else {
            setMeasuredDimension((this.mRatioWidth * height) / this.mRatioHeight, height);
        }
    }
}
