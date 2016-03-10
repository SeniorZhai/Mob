package com.mobcrush.mobcrush.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View.MeasureSpec;
import android.widget.TextView;

public class TightTextView extends TextView {
    public TightTextView(Context context) {
        this(context, null, 0);
    }

    public TightTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TightTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) != 1073741824) {
            Layout layout = getLayout();
            int linesCount = layout.getLineCount();
            if (linesCount > 1) {
                float textRealMaxWidth = 0.0f;
                for (int n = 0; n < linesCount; n++) {
                    textRealMaxWidth = Math.max(textRealMaxWidth, layout.getLineWidth(n));
                }
                int w = Math.round(textRealMaxWidth);
                if (w < getMeasuredWidth()) {
                    super.onMeasure(MeasureSpec.makeMeasureSpec(w, Action.UNDEFINED_DURATION), heightMeasureSpec);
                }
            }
        }
    }
}
