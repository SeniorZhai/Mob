package com.afollestad.materialdialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import com.afollestad.materialdialogs.internal.MDRootLayout;

class DialogBase extends Dialog implements OnShowListener {
    private OnShowListener mShowListener;
    protected MDRootLayout view;

    protected DialogBase(Context context, int theme) {
        super(context, theme);
    }

    public View findViewById(int id) {
        return this.view.findViewById(id);
    }

    public final void setOnShowListener(OnShowListener listener) {
        this.mShowListener = listener;
    }

    protected final void setOnShowListenerInternal() {
        super.setOnShowListener(this);
    }

    protected final void setViewInternal(View view) {
        super.setContentView(view);
    }

    public void onShow(DialogInterface dialog) {
        if (this.mShowListener != null) {
            this.mShowListener.onShow(dialog);
        }
    }

    @Deprecated
    public void setContentView(int layoutResID) throws IllegalAccessError {
        throw new IllegalAccessError("setContentView() is not supported in MaterialDialog. Specify a custom view in the Builder instead.");
    }

    @Deprecated
    public void setContentView(View view) throws IllegalAccessError {
        throw new IllegalAccessError("setContentView() is not supported in MaterialDialog. Specify a custom view in the Builder instead.");
    }

    @Deprecated
    public void setContentView(View view, LayoutParams params) throws IllegalAccessError {
        throw new IllegalAccessError("setContentView() is not supported in MaterialDialog. Specify a custom view in the Builder instead.");
    }
}
