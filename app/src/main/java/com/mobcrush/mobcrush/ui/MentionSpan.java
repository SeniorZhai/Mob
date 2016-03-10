package com.mobcrush.mobcrush.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.RectF;
import android.text.style.ReplacementSpan;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;

public class MentionSpan extends ReplacementSpan {
    private static int PADDING = 0;

    public MentionSpan() {
        PADDING = MainApplication.getContext().getResources().getDimensionPixelSize(R.dimen.mention_padding);
    }

    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        return Math.round(paint.measureText(text, start, end) + ((float) (PADDING * 2)));
    }

    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        RectF rect = new RectF(x, (float) top, ((float) getSize(paint, text, start, end, null)) + x, (float) bottom);
        paint.setColor(MainApplication.getContext().getResources().getColor(R.color.chat_mention_bg));
        canvas.drawRect(rect, paint);
        paint.setColor(MainApplication.getContext().getResources().getColor(R.color.chat_mention_text));
        canvas.drawText(text, start, end, x + ((float) PADDING), (float) y, paint);
    }
}
