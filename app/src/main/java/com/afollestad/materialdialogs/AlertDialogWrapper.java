package com.afollestad.materialdialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.DialogInterface.OnShowListener;
import android.graphics.drawable.Drawable;
import android.support.annotation.ArrayRes;
import android.support.annotation.AttrRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ListAdapter;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;
import com.afollestad.materialdialogs.MaterialDialog.ListCallback;
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackMultiChoice;
import com.afollestad.materialdialogs.MaterialDialog.ListCallbackSingleChoice;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AlertDialogWrapper {

    public static class Builder {
        private final com.afollestad.materialdialogs.MaterialDialog.Builder builder;
        private OnClickListener negativeDialogListener;
        private OnClickListener neutralDialogListener;
        private OnClickListener onClickListener;
        private OnClickListener positiveDialogListener;

        public Builder(@NonNull Context context) {
            this.builder = new com.afollestad.materialdialogs.MaterialDialog.Builder(context);
        }

        public Builder autoDismiss(boolean dismiss) {
            this.builder.autoDismiss(dismiss);
            return this;
        }

        public Builder setMessage(@StringRes int messageId) {
            this.builder.content(messageId);
            return this;
        }

        public Builder setMessage(@NonNull CharSequence message) {
            this.builder.content(message);
            return this;
        }

        public Builder setTitle(@StringRes int titleId) {
            this.builder.title(titleId);
            return this;
        }

        public Builder setTitle(@NonNull CharSequence title) {
            this.builder.title(title);
            return this;
        }

        public Builder setIcon(@DrawableRes int iconId) {
            this.builder.iconRes(iconId);
            return this;
        }

        public Builder setIcon(Drawable icon) {
            this.builder.icon(icon);
            return this;
        }

        public Builder setIconAttribute(@AttrRes int attrId) {
            this.builder.iconAttr(attrId);
            return this;
        }

        public Builder setNegativeButton(@StringRes int textId, OnClickListener listener) {
            this.builder.negativeText(textId);
            this.negativeDialogListener = listener;
            return this;
        }

        public Builder setNegativeButton(@NonNull CharSequence text, OnClickListener listener) {
            this.builder.negativeText(text);
            this.negativeDialogListener = listener;
            return this;
        }

        public Builder setPositiveButton(@StringRes int textId, OnClickListener listener) {
            this.builder.positiveText(textId);
            this.positiveDialogListener = listener;
            return this;
        }

        public Builder setPositiveButton(@NonNull CharSequence text, OnClickListener listener) {
            this.builder.positiveText(text);
            this.positiveDialogListener = listener;
            return this;
        }

        public Builder setNeutralButton(@StringRes int textId, OnClickListener listener) {
            this.builder.neutralText(textId);
            this.neutralDialogListener = listener;
            return this;
        }

        public Builder setNeutralButton(@NonNull CharSequence text, OnClickListener listener) {
            this.builder.neutralText(text);
            this.neutralDialogListener = listener;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            this.builder.cancelable(cancelable);
            return this;
        }

        public Builder setItems(@ArrayRes int itemsId, OnClickListener listener) {
            this.builder.items(itemsId);
            this.onClickListener = listener;
            return this;
        }

        public Builder setItems(CharSequence[] items, OnClickListener listener) {
            this.builder.items(items);
            this.onClickListener = listener;
            return this;
        }

        @Deprecated
        public Builder setAdapter(ListAdapter adapter) {
            return setAdapter(adapter, null);
        }

        public Builder setAdapter(ListAdapter adapter, final OnClickListener listener) {
            this.builder.adapter = adapter;
            this.builder.listCallbackCustom = new ListCallback() {
                public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    listener.onClick(dialog, which);
                }
            };
            return this;
        }

        @UiThread
        public Dialog create() {
            addButtonsCallback();
            addListCallbacks();
            return this.builder.build();
        }

        @UiThread
        public Dialog show() {
            Dialog dialog = create();
            dialog.show();
            return dialog;
        }

        private void addListCallbacks() {
            if (this.onClickListener != null) {
                this.builder.itemsCallback(new ListCallback() {
                    public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        Builder.this.onClickListener.onClick(dialog, which);
                    }
                });
            }
        }

        private void addButtonsCallback() {
            if (this.positiveDialogListener != null || this.negativeDialogListener != null) {
                this.builder.callback(new ButtonCallback() {
                    public void onNeutral(MaterialDialog dialog) {
                        if (Builder.this.neutralDialogListener != null) {
                            Builder.this.neutralDialogListener.onClick(dialog, -3);
                        }
                    }

                    public void onPositive(MaterialDialog dialog) {
                        if (Builder.this.positiveDialogListener != null) {
                            Builder.this.positiveDialogListener.onClick(dialog, -1);
                        }
                    }

                    public void onNegative(MaterialDialog dialog) {
                        if (Builder.this.negativeDialogListener != null) {
                            Builder.this.negativeDialogListener.onClick(dialog, -2);
                        }
                    }
                });
            }
        }

        public Builder setView(@NonNull View view) {
            this.builder.customView(view, false);
            return this;
        }

        public Builder setMultiChoiceItems(@ArrayRes int itemsId, @Nullable boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            this.builder.items(itemsId);
            setUpMultiChoiceCallback(checkedItems, listener);
            return this;
        }

        public Builder setMultiChoiceItems(@NonNull String[] items, @Nullable boolean[] checkedItems, OnMultiChoiceClickListener listener) {
            this.builder.items((CharSequence[]) items);
            setUpMultiChoiceCallback(checkedItems, listener);
            return this;
        }

        public Builder alwaysCallSingleChoiceCallback() {
            this.builder.alwaysCallSingleChoiceCallback();
            return this;
        }

        public Builder alwaysCallMultiChoiceCallback() {
            this.builder.alwaysCallMultiChoiceCallback();
            return this;
        }

        private void setUpMultiChoiceCallback(@Nullable final boolean[] checkedItems, final OnMultiChoiceClickListener listener) {
            Integer[] selectedIndicesArr = null;
            if (checkedItems != null) {
                ArrayList<Integer> selectedIndices = new ArrayList();
                for (int i = 0; i < checkedItems.length; i++) {
                    if (checkedItems[i]) {
                        selectedIndices.add(Integer.valueOf(i));
                    }
                }
                selectedIndicesArr = (Integer[]) selectedIndices.toArray(new Integer[selectedIndices.size()]);
            }
            this.builder.itemsCallbackMultiChoice(selectedIndicesArr, new ListCallbackMultiChoice() {
                public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                    List<Integer> whichList = Arrays.asList(which);
                    if (checkedItems != null) {
                        for (int i = 0; i < checkedItems.length; i++) {
                            boolean oldChecked = checkedItems[i];
                            checkedItems[i] = whichList.contains(Integer.valueOf(i));
                            if (oldChecked != checkedItems[i]) {
                                listener.onClick(dialog, i, checkedItems[i]);
                            }
                        }
                    }
                    return true;
                }
            });
        }

        public Builder setSingleChoiceItems(@NonNull String[] items, int checkedItem, final OnClickListener listener) {
            this.builder.items((CharSequence[]) items);
            this.builder.itemsCallbackSingleChoice(checkedItem, new ListCallbackSingleChoice() {
                public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    listener.onClick(dialog, which);
                    return true;
                }
            });
            return this;
        }

        public Builder setSingleChoiceItems(@ArrayRes int itemsId, int checkedItem, final OnClickListener listener) {
            this.builder.items(itemsId);
            this.builder.itemsCallbackSingleChoice(checkedItem, new ListCallbackSingleChoice() {
                public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                    listener.onClick(dialog, which);
                    return true;
                }
            });
            return this;
        }

        public Builder setOnCancelListener(@NonNull OnCancelListener listener) {
            this.builder.cancelListener(listener);
            return this;
        }

        public Builder setOnDismissListener(@NonNull OnDismissListener listener) {
            this.builder.dismissListener(listener);
            return this;
        }

        public Builder setOnShowListener(@NonNull OnShowListener listener) {
            this.builder.showListener(listener);
            return this;
        }

        public Builder setOnKeyListener(@NonNull OnKeyListener listener) {
            this.builder.keyListener(listener);
            return this;
        }
    }
}
