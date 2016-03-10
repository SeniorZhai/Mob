package com.wdullaer.materialdatetimepicker.date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.TextView;
import com.wdullaer.materialdatetimepicker.R;

public class TextViewWithCircularIndicator extends TextView {
    private static final int SELECTED_CIRCLE_ALPHA = 255;
    private final int mCircleColor;
    Paint mCirclePaint = new Paint();
    private boolean mDrawCircle;
    private final String mItemIsSelectedText;
    private final int mRadius;

    public TextViewWithCircularIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = context.getResources();
        this.mCircleColor = res.getColor(R.color.mdtp_accent_color);
        this.mRadius = res.getDimensionPixelOffset(R.dimen.mdtp_month_select_circle_radius);
        this.mItemIsSelectedText = context.getResources().getString(R.string.mdtp_item_is_selected);
        init();
    }

    private void init() {
        this.mCirclePaint.setFakeBoldText(true);
        this.mCirclePaint.setAntiAlias(true);
        this.mCirclePaint.setColor(this.mCircleColor);
        this.mCirclePaint.setTextAlign(Align.CENTER);
        this.mCirclePaint.setStyle(Style.FILL);
        this.mCirclePaint.setAlpha(SELECTED_CIRCLE_ALPHA);
    }

    public void drawIndicator(boolean drawCircle) {
        this.mDrawCircle = drawCircle;
    }

    public void onDraw(@NonNull Canvas canvas) {
        if (this.mDrawCircle) {
            int width = getWidth();
            int height = getHeight();
            canvas.drawCircle((float) (width / 2), (float) (height / 2), (float) (Math.min(width, height) / 2), this.mCirclePaint);
        }
        setSelected(this.mDrawCircle);
        super.onDraw(canvas);
    }

    public CharSequence getContentDescription() {
        CharSequence itemText = getText();
        if (!this.mDrawCircle) {
            return itemText;
        }
        return String.format(this.mItemIsSelectedText, new Object[]{itemText});
    }
}
