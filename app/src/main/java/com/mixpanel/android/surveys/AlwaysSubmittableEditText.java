package com.mixpanel.android.surveys;

import android.content.Context;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

public class AlwaysSubmittableEditText extends EditText {
    public AlwaysSubmittableEditText(Context context) {
        super(context);
    }

    public AlwaysSubmittableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlwaysSubmittableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection ret = super.onCreateInputConnection(outAttrs);
        outAttrs.imeOptions &= -1073741825;
        return ret;
    }
}
