package android.support.v4.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import io.fabric.sdk.android.services.common.ResponseParser;

public class Space extends View {
    public Space(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (getVisibility() == 0) {
            setVisibility(4);
        }
    }

    public Space(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Space(Context context) {
        this(context, null);
    }

    public void draw(Canvas canvas) {
    }

    private static int getDefaultSize2(int size, int measureSpec) {
        int result = size;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case Action.UNDEFINED_DURATION /*-2147483648*/:
                return Math.min(size, specSize);
            case ResponseParser.ResponseActionDiscard /*0*/:
                return size;
            case 1073741824:
                return specSize;
            default:
                return result;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize2(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize2(getSuggestedMinimumHeight(), heightMeasureSpec));
    }
}