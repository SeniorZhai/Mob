package com.mobcrush.mobcrush.broadcast;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;

public class BroadcastLayout extends LinearLayout {
    private boolean isKeyboardShown;
    private SoftKeyboardVisibilityChangeListener listener;

    public interface SoftKeyboardVisibilityChangeListener {
        void onSoftKeyboardHide();

        void onSoftKeyboardShow();
    }

    public BroadcastLayout(Context context) {
        super(context);
    }

    public BroadcastLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BroadcastLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == 4 && this.isKeyboardShown) {
            this.isKeyboardShown = false;
            this.listener.onSoftKeyboardHide();
        }
        return super.dispatchKeyEventPreIme(event);
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (getHeight() > MeasureSpec.getSize(heightMeasureSpec) && !this.isKeyboardShown) {
            this.isKeyboardShown = true;
            this.listener.onSoftKeyboardShow();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setOnSoftKeyboardVisibilityChangeListener(SoftKeyboardVisibilityChangeListener listener) {
        this.listener = listener;
    }
}
