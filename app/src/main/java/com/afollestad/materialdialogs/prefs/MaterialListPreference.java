package com.afollestad.materialdialogs.prefs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.preference.ListPreference;
import android.preference.Preference.BaseSavedState;
import android.preference.PreferenceManager;
import android.preference.PreferenceManager.OnActivityDestroyListener;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class MaterialListPreference extends ListPreference {
    private Context context;
    private MaterialDialog mDialog;

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

    public MaterialListPreference(Context context) {
        super(context);
        init(context);
    }

    public MaterialListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        if (VERSION.SDK_INT <= 10) {
            setWidgetLayoutResource(0);
        }
    }

    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
        if (this.mDialog != null) {
            this.mDialog.setItems(entries);
        }
    }

    public Dialog getDialog() {
        return this.mDialog;
    }

    protected void showDialog(Bundle state) {
        if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array.");
        }
        Builder builder = new Builder(this.context).title(getDialogTitle()).content(getDialogMessage()).icon(getDialogIcon()).dismissListener(this).callback(new ButtonCallback() {
            public void onNeutral(MaterialDialog dialog) {
                MaterialListPreference.this.onClick(dialog, -3);
            }

            public void onNegative(MaterialDialog dialog) {
                MaterialListPreference.this.onClick(dialog, -2);
            }

            public void onPositive(MaterialDialog dialog) {
                MaterialListPreference.this.onClick(dialog, -1);
            }
        }).negativeText(getNegativeButtonText()).items(getEntries()).autoDismiss(true).itemsCallbackSingleChoice(findIndexOfValue(getValue()), new ListCallbackSingleChoice() {
            public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                MaterialListPreference.this.onClick(null, -1);
                if (which >= 0 && MaterialListPreference.this.getEntryValues() != null) {
                    try {
                        Field clickedIndex = ListPreference.class.getDeclaredField("mClickedDialogEntryIndex");
                        clickedIndex.setAccessible(true);
                        clickedIndex.set(MaterialListPreference.this, Integer.valueOf(which));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }
        });
        View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            builder.customView(contentView, false);
        } else {
            builder.content(getDialogMessage());
        }
        try {
            PreferenceManager pm = getPreferenceManager();
            Method method = pm.getClass().getDeclaredMethod("registerOnActivityDestroyListener", new Class[]{OnActivityDestroyListener.class});
            method.setAccessible(true);
            method.invoke(pm, new Object[]{this});
        } catch (Exception e) {
        }
        this.mDialog = builder.build();
        if (state != null) {
            this.mDialog.onRestoreInstanceState(state);
        }
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
