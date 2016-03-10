package com.google.android.exoplayer.text;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;
import com.google.android.exoplayer.util.Util;

final class CuePainter {
    private static final float DEFAULT_BOTTOM_PADDING_FRACTION = 0.08f;
    private static final float INNER_PADDING_RATIO = 0.125f;
    private static final float LINE_HEIGHT_FRACTION = 0.0533f;
    private static final String TAG = "CuePainter";
    private int backgroundColor;
    private final float cornerRadius;
    private Alignment cueAlignment;
    private int cuePosition;
    private CharSequence cueText;
    private int edgeColor;
    private int edgeType;
    private int foregroundColor;
    private final RectF lineBounds = new RectF();
    private final float outlineWidth;
    private final Paint paint;
    private int parentBottom;
    private int parentLeft;
    private int parentRight;
    private int parentTop;
    private final float shadowOffset;
    private final float shadowRadius;
    private final float spacingAdd;
    private final float spacingMult;
    private StaticLayout textLayout;
    private int textLeft;
    private int textPaddingX;
    private final TextPaint textPaint;
    private int textTop;
    private int windowColor;

    public CuePainter(Context context) {
        TypedArray styledAttributes = context.obtainStyledAttributes(null, new int[]{16843287, 16843288}, 0, 0);
        this.spacingAdd = (float) styledAttributes.getDimensionPixelSize(0, 0);
        this.spacingMult = styledAttributes.getFloat(1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        styledAttributes.recycle();
        int twoDpInPx = Math.round((2.0f * ((float) context.getResources().getDisplayMetrics().densityDpi)) / 160.0f);
        this.cornerRadius = (float) twoDpInPx;
        this.outlineWidth = (float) twoDpInPx;
        this.shadowRadius = (float) twoDpInPx;
        this.shadowOffset = (float) twoDpInPx;
        this.textPaint = new TextPaint();
        this.textPaint.setAntiAlias(true);
        this.textPaint.setSubpixelText(true);
        this.paint = new Paint();
        this.paint.setAntiAlias(true);
        this.paint.setStyle(Style.FILL);
    }

    public void draw(Cue cue, CaptionStyleCompat style, float fontScale, Canvas canvas, int cueBoxLeft, int cueBoxTop, int cueBoxRight, int cueBoxBottom) {
        if (TextUtils.equals(this.cueText, cue.text) && this.cuePosition == cue.position && Util.areEqual(this.cueAlignment, cue.alignment) && this.foregroundColor == style.foregroundColor && this.backgroundColor == style.backgroundColor && this.windowColor == style.windowColor && this.edgeType == style.edgeType && this.edgeColor == style.edgeColor && Util.areEqual(this.textPaint.getTypeface(), style.typeface) && this.parentLeft == cueBoxLeft && this.parentTop == cueBoxTop && this.parentRight == cueBoxRight && this.parentBottom == cueBoxBottom) {
            drawLayout(canvas);
            return;
        }
        this.cueText = cue.text;
        this.cuePosition = cue.position;
        this.cueAlignment = cue.alignment;
        this.foregroundColor = style.foregroundColor;
        this.backgroundColor = style.backgroundColor;
        this.windowColor = style.windowColor;
        this.edgeType = style.edgeType;
        this.edgeColor = style.edgeColor;
        this.textPaint.setTypeface(style.typeface);
        this.parentLeft = cueBoxLeft;
        this.parentTop = cueBoxTop;
        this.parentRight = cueBoxRight;
        this.parentBottom = cueBoxBottom;
        int parentWidth = this.parentRight - this.parentLeft;
        int parentHeight = this.parentBottom - this.parentTop;
        float textSize = (LINE_HEIGHT_FRACTION * ((float) parentHeight)) * fontScale;
        this.textPaint.setTextSize(textSize);
        int textPaddingX = (int) ((INNER_PADDING_RATIO * textSize) + 0.5f);
        int availableWidth = parentWidth - (textPaddingX * 2);
        if (availableWidth <= 0) {
            Log.w(TAG, "Skipped drawing subtitle cue (insufficient space)");
            return;
        }
        Alignment layoutAlignment = this.cueAlignment == null ? Alignment.ALIGN_CENTER : this.cueAlignment;
        this.textLayout = new StaticLayout(this.cueText, this.textPaint, availableWidth, layoutAlignment, this.spacingMult, this.spacingAdd, true);
        int textHeight = this.textLayout.getHeight();
        int textWidth = 0;
        int lineCount = this.textLayout.getLineCount();
        for (int i = 0; i < lineCount; i++) {
            textWidth = Math.max((int) Math.ceil((double) this.textLayout.getLineWidth(i)), textWidth);
        }
        textWidth += textPaddingX * 2;
        int textLeft = (parentWidth - textWidth) / 2;
        int textRight = textLeft + textWidth;
        int textTop = (this.parentBottom - textHeight) - ((int) (((float) parentHeight) * DEFAULT_BOTTOM_PADDING_FRACTION));
        int textBottom = textTop + textHeight;
        if (cue.position != -1) {
            if (cue.alignment == Alignment.ALIGN_OPPOSITE) {
                textRight = ((cue.position * parentWidth) / 100) + this.parentLeft;
                textLeft = Math.max(textRight - textWidth, this.parentLeft);
            } else {
                textLeft = ((cue.position * parentWidth) / 100) + this.parentLeft;
                textRight = Math.min(textLeft + textWidth, this.parentRight);
            }
        }
        if (cue.line != -1) {
            textTop = ((cue.line * parentHeight) / 100) + this.parentTop;
            if (textTop + textHeight > this.parentBottom) {
                textTop = this.parentBottom - textHeight;
                textBottom = this.parentBottom;
            }
        }
        this.textLayout = new StaticLayout(this.cueText, this.textPaint, textRight - textLeft, layoutAlignment, this.spacingMult, this.spacingAdd, true);
        this.textLeft = textLeft;
        this.textTop = textTop;
        this.textPaddingX = textPaddingX;
        drawLayout(canvas);
    }

    private void drawLayout(Canvas canvas) {
        StaticLayout layout = this.textLayout;
        if (layout != null) {
            int saveCount = canvas.save();
            canvas.translate((float) this.textLeft, (float) this.textTop);
            if (Color.alpha(this.windowColor) > 0) {
                this.paint.setColor(this.windowColor);
                canvas.drawRect((float) (-this.textPaddingX), 0.0f, (float) (layout.getWidth() + this.textPaddingX), (float) layout.getHeight(), this.paint);
            }
            if (Color.alpha(this.backgroundColor) > 0) {
                this.paint.setColor(this.backgroundColor);
                float previousBottom = (float) layout.getLineTop(0);
                int lineCount = layout.getLineCount();
                for (int i = 0; i < lineCount; i++) {
                    this.lineBounds.left = layout.getLineLeft(i) - ((float) this.textPaddingX);
                    this.lineBounds.right = layout.getLineRight(i) + ((float) this.textPaddingX);
                    this.lineBounds.top = previousBottom;
                    this.lineBounds.bottom = (float) layout.getLineBottom(i);
                    previousBottom = this.lineBounds.bottom;
                    canvas.drawRoundRect(this.lineBounds, this.cornerRadius, this.cornerRadius, this.paint);
                }
            }
            if (this.edgeType == 1) {
                this.textPaint.setStrokeJoin(Join.ROUND);
                this.textPaint.setStrokeWidth(this.outlineWidth);
                this.textPaint.setColor(this.edgeColor);
                this.textPaint.setStyle(Style.FILL_AND_STROKE);
                layout.draw(canvas);
            } else if (this.edgeType == 2) {
                this.textPaint.setShadowLayer(this.shadowRadius, this.shadowOffset, this.shadowOffset, this.edgeColor);
            } else if (this.edgeType == 3 || this.edgeType == 4) {
                boolean raised = this.edgeType == 3;
                int colorUp = raised ? -1 : this.edgeColor;
                int colorDown = raised ? this.edgeColor : -1;
                float offset = this.shadowRadius / 2.0f;
                this.textPaint.setColor(this.foregroundColor);
                this.textPaint.setStyle(Style.FILL);
                this.textPaint.setShadowLayer(this.shadowRadius, -offset, -offset, colorUp);
                layout.draw(canvas);
                this.textPaint.setShadowLayer(this.shadowRadius, offset, offset, colorDown);
            }
            this.textPaint.setColor(this.foregroundColor);
            this.textPaint.setStyle(Style.FILL);
            layout.draw(canvas);
            this.textPaint.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
            canvas.restoreToCount(saveCount);
        }
    }
}
