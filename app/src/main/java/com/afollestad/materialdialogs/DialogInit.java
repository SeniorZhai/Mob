package com.afollestad.materialdialogs;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build.VERSION;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.annotation.UiThread;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.internal.MDButton;
import com.afollestad.materialdialogs.internal.MDRootLayout;
import com.afollestad.materialdialogs.internal.MDTintHelper;
import com.afollestad.materialdialogs.progress.CircularProgressDrawable;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.util.DialogUtils;
import java.util.ArrayList;
import java.util.Arrays;

class DialogInit {
    DialogInit() {
    }

    @StyleRes
    public static int getTheme(@NonNull Builder builder) {
        boolean darkTheme = DialogUtils.resolveBoolean(builder.context, R.attr.md_dark_theme, builder.theme == Theme.DARK);
        builder.theme = darkTheme ? Theme.DARK : Theme.LIGHT;
        return darkTheme ? R.style.MD_Dark : R.style.MD_Light;
    }

    @LayoutRes
    public static int getInflateLayout(Builder builder) {
        if (builder.customView != null) {
            return R.layout.md_dialog_custom;
        }
        if ((builder.items != null && builder.items.length > 0) || builder.adapter != null) {
            return R.layout.md_dialog_list;
        }
        if (builder.progress > -2) {
            return R.layout.md_dialog_progress;
        }
        if (builder.indeterminateProgress) {
            if (builder.indeterminateIsHorizontalProgress) {
                return R.layout.md_dialog_progress_indeterminate_horizontal;
            }
            return R.layout.md_dialog_progress_indeterminate;
        } else if (builder.inputCallback != null) {
            return R.layout.md_dialog_input;
        } else {
            return R.layout.md_dialog_basic;
        }
    }

    @UiThread
    public static void init(MaterialDialog dialog) {
        boolean textAllCaps;
        Builder builder = dialog.mBuilder;
        dialog.setCancelable(builder.cancelable);
        dialog.setCanceledOnTouchOutside(builder.cancelable);
        if (builder.backgroundColor == 0) {
            builder.backgroundColor = DialogUtils.resolveColor(builder.context, R.attr.md_background_color);
        }
        if (builder.backgroundColor != 0) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setCornerRadius(builder.context.getResources().getDimension(R.dimen.md_bg_corner_radius));
            drawable.setColor(builder.backgroundColor);
            DialogUtils.setBackgroundCompat(dialog.view, drawable);
        }
        if (!builder.positiveColorSet) {
            builder.positiveColor = DialogUtils.resolveActionTextColorStateList(builder.context, R.attr.md_positive_color, builder.positiveColor);
        }
        if (!builder.neutralColorSet) {
            builder.neutralColor = DialogUtils.resolveActionTextColorStateList(builder.context, R.attr.md_neutral_color, builder.neutralColor);
        }
        if (!builder.negativeColorSet) {
            builder.negativeColor = DialogUtils.resolveActionTextColorStateList(builder.context, R.attr.md_negative_color, builder.negativeColor);
        }
        if (!builder.widgetColorSet) {
            builder.widgetColor = DialogUtils.resolveColor(builder.context, R.attr.md_widget_color, builder.widgetColor);
        }
        if (!builder.titleColorSet) {
            builder.titleColor = DialogUtils.resolveColor(builder.context, R.attr.md_title_color, DialogUtils.resolveColor(dialog.getContext(), 16842806));
        }
        if (!builder.contentColorSet) {
            builder.contentColor = DialogUtils.resolveColor(builder.context, R.attr.md_content_color, DialogUtils.resolveColor(dialog.getContext(), 16842808));
        }
        if (!builder.itemColorSet) {
            builder.itemColor = DialogUtils.resolveColor(builder.context, R.attr.md_item_color, builder.contentColor);
        }
        dialog.title = (TextView) dialog.view.findViewById(R.id.title);
        dialog.icon = (ImageView) dialog.view.findViewById(R.id.icon);
        dialog.titleFrame = dialog.view.findViewById(R.id.titleFrame);
        dialog.content = (TextView) dialog.view.findViewById(R.id.content);
        dialog.listView = (ListView) dialog.view.findViewById(R.id.contentListView);
        dialog.positiveButton = (MDButton) dialog.view.findViewById(R.id.buttonDefaultPositive);
        dialog.neutralButton = (MDButton) dialog.view.findViewById(R.id.buttonDefaultNeutral);
        dialog.negativeButton = (MDButton) dialog.view.findViewById(R.id.buttonDefaultNegative);
        if (builder.inputCallback != null && builder.positiveText == null) {
            builder.positiveText = builder.context.getText(17039370);
        }
        dialog.positiveButton.setVisibility(builder.positiveText != null ? 0 : 8);
        dialog.neutralButton.setVisibility(builder.neutralText != null ? 0 : 8);
        dialog.negativeButton.setVisibility(builder.negativeText != null ? 0 : 8);
        if (builder.icon != null) {
            dialog.icon.setVisibility(0);
            dialog.icon.setImageDrawable(builder.icon);
        } else {
            Drawable d = DialogUtils.resolveDrawable(builder.context, R.attr.md_icon);
            if (d != null) {
                dialog.icon.setVisibility(0);
                dialog.icon.setImageDrawable(d);
            } else {
                dialog.icon.setVisibility(8);
            }
        }
        int maxIconSize = builder.maxIconSize;
        if (maxIconSize == -1) {
            maxIconSize = DialogUtils.resolveDimension(builder.context, R.attr.md_icon_max_size);
        }
        if (builder.limitIconToDefaultSize || DialogUtils.resolveBoolean(builder.context, R.attr.md_icon_limit_icon_to_default_size)) {
            maxIconSize = builder.context.getResources().getDimensionPixelSize(R.dimen.md_icon_max_size);
        }
        if (maxIconSize > -1) {
            dialog.icon.setAdjustViewBounds(true);
            dialog.icon.setMaxHeight(maxIconSize);
            dialog.icon.setMaxWidth(maxIconSize);
            dialog.icon.requestLayout();
        }
        if (!builder.dividerColorSet) {
            builder.dividerColor = DialogUtils.resolveColor(builder.context, R.attr.md_divider_color, DialogUtils.resolveColor(dialog.getContext(), R.attr.md_divider));
        }
        dialog.view.setDividerColor(builder.dividerColor);
        if (dialog.title != null) {
            dialog.setTypeface(dialog.title, builder.mediumFont);
            dialog.title.setTextColor(builder.titleColor);
            dialog.title.setGravity(builder.titleGravity.getGravityInt());
            if (VERSION.SDK_INT >= 17) {
                dialog.title.setTextAlignment(builder.titleGravity.getTextAlignment());
            }
            if (builder.title == null) {
                dialog.titleFrame.setVisibility(8);
            } else {
                dialog.title.setText(builder.title);
                dialog.titleFrame.setVisibility(0);
            }
        }
        if (dialog.content != null) {
            dialog.content.setMovementMethod(new LinkMovementMethod());
            dialog.setTypeface(dialog.content, builder.regularFont);
            dialog.content.setLineSpacing(0.0f, builder.contentLineSpacingMultiplier);
            if (builder.positiveColor == null) {
                dialog.content.setLinkTextColor(DialogUtils.resolveColor(dialog.getContext(), 16842806));
            } else {
                dialog.content.setLinkTextColor(builder.positiveColor);
            }
            dialog.content.setTextColor(builder.contentColor);
            dialog.content.setGravity(builder.contentGravity.getGravityInt());
            if (VERSION.SDK_INT >= 17) {
                dialog.content.setTextAlignment(builder.contentGravity.getTextAlignment());
            }
            if (builder.content != null) {
                dialog.content.setText(builder.content);
                dialog.content.setVisibility(0);
            } else {
                dialog.content.setVisibility(8);
            }
        }
        dialog.view.setButtonGravity(builder.buttonsGravity);
        dialog.view.setButtonStackedGravity(builder.btnStackedGravity);
        dialog.view.setForceStack(builder.forceStacking);
        if (VERSION.SDK_INT >= 14) {
            textAllCaps = DialogUtils.resolveBoolean(builder.context, 16843660, true);
            if (textAllCaps) {
                textAllCaps = DialogUtils.resolveBoolean(builder.context, R.attr.textAllCaps, true);
            }
        } else {
            textAllCaps = DialogUtils.resolveBoolean(builder.context, R.attr.textAllCaps, true);
        }
        TextView positiveTextView = dialog.positiveButton;
        dialog.setTypeface(positiveTextView, builder.mediumFont);
        positiveTextView.setAllCapsCompat(textAllCaps);
        positiveTextView.setText(builder.positiveText);
        positiveTextView.setTextColor(builder.positiveColor);
        dialog.positiveButton.setStackedSelector(dialog.getButtonSelector(DialogAction.POSITIVE, true));
        dialog.positiveButton.setDefaultSelector(dialog.getButtonSelector(DialogAction.POSITIVE, false));
        dialog.positiveButton.setTag(DialogAction.POSITIVE);
        dialog.positiveButton.setOnClickListener(dialog);
        dialog.positiveButton.setVisibility(0);
        MDButton negativeTextView = dialog.negativeButton;
        dialog.setTypeface(negativeTextView, builder.mediumFont);
        negativeTextView.setAllCapsCompat(textAllCaps);
        negativeTextView.setText(builder.negativeText);
        negativeTextView.setTextColor(builder.negativeColor);
        dialog.negativeButton.setStackedSelector(dialog.getButtonSelector(DialogAction.NEGATIVE, true));
        dialog.negativeButton.setDefaultSelector(dialog.getButtonSelector(DialogAction.NEGATIVE, false));
        dialog.negativeButton.setTag(DialogAction.NEGATIVE);
        dialog.negativeButton.setOnClickListener(dialog);
        dialog.negativeButton.setVisibility(0);
        MDButton neutralTextView = dialog.neutralButton;
        dialog.setTypeface(neutralTextView, builder.mediumFont);
        neutralTextView.setAllCapsCompat(textAllCaps);
        neutralTextView.setText(builder.neutralText);
        neutralTextView.setTextColor(builder.neutralColor);
        dialog.neutralButton.setStackedSelector(dialog.getButtonSelector(DialogAction.NEUTRAL, true));
        dialog.neutralButton.setDefaultSelector(dialog.getButtonSelector(DialogAction.NEUTRAL, false));
        dialog.neutralButton.setTag(DialogAction.NEUTRAL);
        dialog.neutralButton.setOnClickListener(dialog);
        dialog.neutralButton.setVisibility(0);
        if (builder.listCallbackMultiChoice != null) {
            dialog.selectedIndicesList = new ArrayList();
        }
        if (dialog.listView != null && ((builder.items != null && builder.items.length > 0) || builder.adapter != null)) {
            dialog.listView.setSelector(dialog.getListSelector());
            if (builder.adapter == null) {
                if (builder.listCallbackSingleChoice != null) {
                    dialog.listType = ListType.SINGLE;
                } else if (builder.listCallbackMultiChoice != null) {
                    dialog.listType = ListType.MULTI;
                    if (builder.selectedIndices != null) {
                        dialog.selectedIndicesList = new ArrayList(Arrays.asList(builder.selectedIndices));
                    }
                } else {
                    dialog.listType = ListType.REGULAR;
                }
                builder.adapter = new MaterialDialogAdapter(dialog, ListType.getLayoutForType(dialog.listType));
            } else if (builder.adapter instanceof MaterialSimpleListAdapter) {
                ((MaterialSimpleListAdapter) builder.adapter).setDialog(dialog, false);
            }
        }
        setupProgressDialog(dialog);
        setupInputDialog(dialog);
        if (builder.customView != null) {
            ((MDRootLayout) dialog.view.findViewById(R.id.root)).noTitleNoPadding();
            FrameLayout frame = (FrameLayout) dialog.view.findViewById(R.id.customViewFrame);
            dialog.customViewFrame = frame;
            View innerView = builder.customView;
            if (builder.wrapCustomViewInScroll) {
                Resources r = dialog.getContext().getResources();
                int framePadding = r.getDimensionPixelSize(R.dimen.md_dialog_frame_margin);
                View scrollView = new ScrollView(dialog.getContext());
                int paddingTop = r.getDimensionPixelSize(R.dimen.md_content_padding_top);
                int paddingBottom = r.getDimensionPixelSize(R.dimen.md_content_padding_bottom);
                scrollView.setClipToPadding(false);
                if (innerView instanceof EditText) {
                    scrollView.setPadding(framePadding, paddingTop, framePadding, paddingBottom);
                } else {
                    scrollView.setPadding(0, paddingTop, 0, paddingBottom);
                    innerView.setPadding(framePadding, 0, framePadding, 0);
                }
                scrollView.addView(innerView, new LayoutParams(-1, -2));
                innerView = scrollView;
            }
            frame.addView(innerView, new ViewGroup.LayoutParams(-1, -2));
        }
        if (builder.showListener != null) {
            dialog.setOnShowListener(builder.showListener);
        }
        if (builder.cancelListener != null) {
            dialog.setOnCancelListener(builder.cancelListener);
        }
        if (builder.dismissListener != null) {
            dialog.setOnDismissListener(builder.dismissListener);
        }
        if (builder.keyListener != null) {
            dialog.setOnKeyListener(builder.keyListener);
        }
        dialog.setOnShowListenerInternal();
        dialog.invalidateList();
        dialog.setViewInternal(dialog.view);
        dialog.checkIfListInitScroll();
    }

    private static void setupProgressDialog(MaterialDialog dialog) {
        Builder builder = dialog.mBuilder;
        if (builder.indeterminateProgress || builder.progress > -2) {
            dialog.mProgress = (ProgressBar) dialog.view.findViewById(16908301);
            if (dialog.mProgress != null) {
                if (!builder.indeterminateProgress || builder.indeterminateIsHorizontalProgress || VERSION.SDK_INT < 14 || VERSION.SDK_INT >= 21) {
                    MDTintHelper.setTint(dialog.mProgress, builder.widgetColor);
                } else {
                    dialog.mProgress.setIndeterminateDrawable(new CircularProgressDrawable(builder.widgetColor, builder.context.getResources().getDimension(R.dimen.circular_progress_border)));
                    MDTintHelper.setTint(dialog.mProgress, builder.widgetColor, true);
                }
                if (!builder.indeterminateProgress || builder.indeterminateIsHorizontalProgress) {
                    dialog.mProgress.setIndeterminate(builder.indeterminateIsHorizontalProgress);
                    dialog.mProgress.setProgress(0);
                    dialog.mProgress.setMax(builder.progressMax);
                    dialog.mProgressLabel = (TextView) dialog.view.findViewById(R.id.label);
                    if (dialog.mProgressLabel != null) {
                        dialog.mProgressLabel.setTextColor(builder.contentColor);
                        dialog.setTypeface(dialog.mProgressLabel, builder.mediumFont);
                        dialog.mProgressLabel.setText(builder.progressPercentFormat.format(0));
                    }
                    dialog.mProgressMinMax = (TextView) dialog.view.findViewById(R.id.minMax);
                    if (dialog.mProgressMinMax != null) {
                        dialog.mProgressMinMax.setTextColor(builder.contentColor);
                        dialog.setTypeface(dialog.mProgressMinMax, builder.regularFont);
                        if (builder.showMinMax) {
                            dialog.mProgressMinMax.setVisibility(0);
                            dialog.mProgressMinMax.setText(String.format(builder.progressNumberFormat, new Object[]{Integer.valueOf(0), Integer.valueOf(builder.progressMax)}));
                            MarginLayoutParams lp = (MarginLayoutParams) dialog.mProgress.getLayoutParams();
                            lp.leftMargin = 0;
                            lp.rightMargin = 0;
                            return;
                        }
                        dialog.mProgressMinMax.setVisibility(8);
                        return;
                    }
                    builder.showMinMax = false;
                }
            }
        }
    }

    private static void setupInputDialog(MaterialDialog dialog) {
        Builder builder = dialog.mBuilder;
        dialog.input = (EditText) dialog.view.findViewById(16908297);
        if (dialog.input != null) {
            dialog.setTypeface(dialog.input, builder.regularFont);
            if (builder.inputPrefill != null) {
                dialog.input.setText(builder.inputPrefill);
            }
            dialog.setInternalInputCallback();
            dialog.input.setHint(builder.inputHint);
            dialog.input.setSingleLine();
            dialog.input.setTextColor(builder.contentColor);
            dialog.input.setHintTextColor(DialogUtils.adjustAlpha(builder.contentColor, 0.3f));
            MDTintHelper.setTint(dialog.input, dialog.mBuilder.widgetColor);
            if (builder.inputType != -1) {
                dialog.input.setInputType(builder.inputType);
                if ((builder.inputType & AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) == AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) {
                    dialog.input.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
            dialog.inputMinMax = (TextView) dialog.view.findViewById(R.id.minMax);
            if (builder.inputMaxLength > -1) {
                dialog.invalidateInputMinMaxIndicator(dialog.input.getText().toString().length(), !builder.inputAllowEmpty);
                return;
            }
            dialog.inputMinMax.setVisibility(8);
            dialog.inputMinMax = null;
        }
    }
}
