package com.mobcrush.mobcrush.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class ResizableVideoView extends VideoView {
    private int mOverrideHeight = 0;
    private int mOverrideWidth = 0;

    public ResizableVideoView(Context context) {
        super(context);
    }

    public ResizableVideoView(Context context, AttributeSet set) {
        super(context, set);
    }

    public ResizableVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void resizeVideo(int width, int height) {
        this.mOverrideWidth = width;
        this.mOverrideHeight = height;
        getHolder().setFixedSize(width, height);
        requestLayout();
        invalidate();
    }

    public int getOverrideWidth() {
        return this.mOverrideWidth;
    }

    public int getmOverrideHeight() {
        return this.mOverrideHeight;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mOverrideWidth == 0 || this.mOverrideHeight == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(this.mOverrideWidth, this.mOverrideHeight);
        }
    }
}
