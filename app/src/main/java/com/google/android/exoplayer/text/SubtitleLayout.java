package com.google.android.exoplayer.text;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.android.volley.DefaultRetryPolicy;
import java.util.ArrayList;
import java.util.List;

public final class SubtitleLayout extends View {
    private List<Cue> cues;
    private float fontScale;
    private final List<CuePainter> painters;
    private CaptionStyleCompat style;

    public SubtitleLayout(Context context) {
        this(context, null);
    }

    public SubtitleLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.painters = new ArrayList();
        this.fontScale = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        this.style = CaptionStyleCompat.DEFAULT;
    }

    public void setCues(List<Cue> cues) {
        if (this.cues != cues) {
            this.cues = cues;
            int cueCount = cues == null ? 0 : cues.size();
            while (this.painters.size() < cueCount) {
                this.painters.add(new CuePainter(getContext()));
            }
            invalidate();
        }
    }

    public void setFontScale(float fontScale) {
        if (this.fontScale != fontScale) {
            this.fontScale = fontScale;
            invalidate();
        }
    }

    public void setStyle(CaptionStyleCompat style) {
        if (this.style != style) {
            this.style = style;
            invalidate();
        }
    }

    public void dispatchDraw(Canvas canvas) {
        int cueCount = this.cues == null ? 0 : this.cues.size();
        for (int i = 0; i < cueCount; i++) {
            ((CuePainter) this.painters.get(i)).draw((Cue) this.cues.get(i), this.style, this.fontScale, canvas, getLeft(), getTop(), getRight(), getBottom());
        }
    }
}
