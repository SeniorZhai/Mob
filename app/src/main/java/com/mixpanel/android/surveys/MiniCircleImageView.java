package com.mixpanel.android.surveys;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

public class MiniCircleImageView extends ImageView {
    private int mCanvasHeight;
    private int mCanvasWidth;
    private Paint mWhitePaint;

    public MiniCircleImageView(Context context) {
        super(context);
        init();
    }

    public MiniCircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MiniCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.mWhitePaint = new Paint(1);
        this.mWhitePaint.setColor(getResources().getColor(17170443));
        this.mWhitePaint.setStyle(Style.STROKE);
        this.mWhitePaint.setStrokeWidth(TypedValue.applyDimension(1, 2.0f, getResources().getDisplayMetrics()));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float centerX = (float) (this.mCanvasWidth / 2);
        float centerY = (float) (this.mCanvasHeight / 2);
        canvas.drawCircle(centerX, centerY, 0.7f * Math.min(centerX, centerY), this.mWhitePaint);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mCanvasWidth = w;
        this.mCanvasHeight = h;
    }
}
