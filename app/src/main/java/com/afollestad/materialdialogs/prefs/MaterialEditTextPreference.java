package com.afollestad.materialdialogs.prefs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.EditTextPreference;
import android.preference.Preference.BaseSavedState;
import android.preference.PreferenceManager;
import android.preference.PreferenceManager.OnActivityDestroyListener;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.afollestad.materialdialogs.R;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.util.DialogUtils;
import java.lang.reflect.Method;

public class MaterialEditTextPreference extends EditTextPreference {
    private final ButtonCallback callback;
    private int mColor;
    private MaterialDialog mDialog;
    private EditText mEditText;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        Bundle dialogBundle;
        boolean isDialogShowing;

        public SavedState(Parcel source) {
            boolean z = true;
            super(source);
            if (source.readInt() != 1) {
                z = false;
            }
            this.isDialogShowing = z;
            this.dialogBundle = source.readBundle();
        }

        public void writeToParcel(@NonNull Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.isDialogShowing ? 1 : 0);
            dest.writeBundle(this.dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public MaterialEditTextPreference(Context context, AttributeSet attrs) {
        int fallback;
        super(context, attrs);
        this.mColor = 0;
        this.callback = new ButtonCallback() {
            public void onPositive(MaterialDialog dialog) {
                MaterialEditTextPreference.this.onClick(dialog, -1);
                String value = MaterialEditTextPreference.this.mEditText.getText().toString();
                if (MaterialEditTextPreference.this.callChangeListener(value) && MaterialEditTextPreference.this.isPersistent()) {
                    MaterialEditTextPreference.this.setText(value);
                }
            }

            public void onNeutral(MaterialDialog dialog) {
                MaterialEditTextPreference.this.onClick(dialog, -3);
            }

            public void onNegative(MaterialDialog dialog) {
                MaterialEditTextPreference.this.onClick(dialog, -2);
            }
        };
        if (VERSION.SDK_INT >= 21) {
            fallback = DialogUtils.resolveColor(context, 16843829);
        } else {
            fallback = 0;
        }
        this.mColor = DialogUtils.resolveColor(context, R.attr.colorAccent, fallback);
        this.mEditText = new AppCompatEditText(context, attrs);
        this.mEditText.setId(16908291);
        this.mEditText.setEnabled(true);
    }

    public MaterialEditTextPreference(Context context) {
        this(context, null);
    }

    protected void onAddEditTextToDialogView(@NonNull View dialogView, @NonNull EditText editText) {
        ((ViewGroup) dialogView).addView(editText, new LayoutParams(-1, -2));
    }

    protected void onBindDialogView(@NonNull View view) {
        EditText editText = this.mEditText;
        editText.setText(getText());
        if (editText.getText().length() > 0) {
            editText.setSelection(editText.length());
        }
        View oldParent = editText.getParent();
        if (oldParent != view) {
            if (oldParent != null) {
                ((ViewGroup) oldParent).removeView(editText);
            }
            onAddEditTextToDialogView(view, editText);
        }
    }

    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value = this.mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
    }

    public EditText getEditText() {
        return this.mEditText;
    }

    public Dialog getDialog() {
        return this.mDialog;
    }

    protected void showDialog(Bundle state) {
        Builder mBuilder = new Builder(getContext()).title(getDialogTitle()).icon(getDialogIcon()).positiveText(getPositiveButtonText()).negativeText(getNegativeButtonText()).dismissListener(this).callback(this.callback).dismissListener(this);
        View layout = LayoutInflater.from(getContext()).inflate(R.layout.md_stub_inputpref, null);
        onBindDialogView(layout);
        MDTintHelper.setTint(this.mEditText, this.mColor);
        TextView message = (TextView) layout.findViewById(16908299);
        if (getDialogMessage() == null || getDialogMessage().toString().length() <= 0) {
            message.setVisibility(8);
        } else {
            message.setVisibility(0);
            message.setText(getDialogMessage());
        }
        mBuilder.customView(layout, false);
        try {
            PreferenceManager pm = getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod("registerOnActivityDestroyListener", new Class[]{OnActivityDestroyListener.class});
            method.setAccessible(true);
            method.invoke(pm, new Object[]{this});
        } catch (Exception e) {
        }
        this.mDialog = mBuilder.build();
        if (state != null) {
            this.mDialog.onRestoreInstanceState(state);
        }
        requestInputMethod(this.mDialog);
        this.mDialog.show();
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        try {
            PreferenceManager pm = getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod("unregisterOnActivityDestroyListener", new Class[]{OnActivityDestroyListener.class});
            method.setAccessible(true);
            method.invoke(pm, new Object[]{this});
        } catch (Exception e) {
        }
    }

    private void requestInputMethod(Dialog dialog) {
        dialog.getWindow().setSoftInputMode(5);
    }

    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        Dialog dialog = getDialog();
        if (dialog == null || !dialog.isShowing()) {
            return superState;
        }
        Parcelable myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = dialog.onSaveInstanceState();
        return myState;
    }

    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        if (myState.isDialogShowing) {
            showDialog(myState.dialogBundle);
        }
    }
}
