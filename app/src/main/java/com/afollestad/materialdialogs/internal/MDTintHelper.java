package com.afollestad.materialdialogs.internal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatEditText;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.SeekBar;
import com.afollestad.materialdialogs.R;
import com.afollestad.materialdialogs.util.DialogUtils;

public class MDTintHelper {
    public static void setTint(RadioButton radioButton, int color) {
        r2 = new int[2][];
        r2[0] = new int[]{-16842912};
        r2[1] = new int[]{16842912};
        ColorStateList sl = new ColorStateList(r2, new int[]{DialogUtils.resolveColor(radioButton.getContext(), R.attr.colorControlNormal), color});
        if (VERSION.SDK_INT >= 21) {
            radioButton.setButtonTintList(sl);
            return;
        }
        Drawable d = DrawableCompat.wrap(ContextCompat.getDrawable(radioButton.getContext(), R.drawable.abc_btn_radio_material));
        DrawableCompat.setTintList(d, sl);
        radioButton.setButtonDrawable(d);
    }

    public static void setTint(SeekBar seekBar, int color) {
        ColorStateList s1 = ColorStateList.valueOf(color);
        if (VERSION.SDK_INT >= 21) {
            seekBar.setThumbTintList(s1);
            seekBar.setProgressTintList(s1);
        } else if (VERSION.SDK_INT > 10) {
            Drawable progressDrawable = DrawableCompat.wrap(seekBar.getProgressDrawable());
            seekBar.setProgressDrawable(progressDrawable);
            DrawableCompat.setTintList(progressDrawable, s1);
            if (VERSION.SDK_INT >= 16) {
                Drawable thumbDrawable = DrawableCompat.wrap(seekBar.getThumb());
                DrawableCompat.setTintList(thumbDrawable, s1);
                seekBar.setThumb(thumbDrawable);
            }
        } else {
            Mode mode = Mode.SRC_IN;
            if (VERSION.SDK_INT <= 10) {
                mode = Mode.MULTIPLY;
            }
            if (seekBar.getIndeterminateDrawable() != null) {
                seekBar.getIndeterminateDrawable().setColorFilter(color, mode);
            }
            if (seekBar.getProgressDrawable() != null) {
                seekBar.getProgressDrawable().setColorFilter(color, mode);
            }
        }
    }

    public static void setTint(ProgressBar progressBar, int color) {
        setTint(progressBar, color, false);
    }

    public static void setTint(ProgressBar progressBar, int color, boolean skipIndeterminate) {
        ColorStateList sl = ColorStateList.valueOf(color);
        if (VERSION.SDK_INT >= 21) {
            progressBar.setProgressTintList(sl);
            progressBar.setSecondaryProgressTintList(sl);
            if (!skipIndeterminate) {
                progressBar.setIndeterminateTintList(sl);
                return;
            }
            return;
        }
        Mode mode = Mode.SRC_IN;
        if (VERSION.SDK_INT <= 10) {
            mode = Mode.MULTIPLY;
        }
        if (!(skipIndeterminate || progressBar.getIndeterminateDrawable() == null)) {
            progressBar.getIndeterminateDrawable().setColorFilter(color, mode);
        }
        if (progressBar.getProgressDrawable() != null) {
            progressBar.getProgressDrawable().setColorFilter(color, mode);
        }
    }

    private static ColorStateList createEditTextColorStateList(Context context, int color) {
        states = new int[3][];
        colors = new int[3];
        states[0] = new int[]{-16842910};
        colors[0] = DialogUtils.resolveColor(context, R.attr.colorControlNormal);
        int i = 0 + 1;
        states[i] = new int[]{-16842919, -16842908};
        colors[i] = DialogUtils.resolveColor(context, R.attr.colorControlNormal);
        i++;
        states[i] = new int[0];
        colors[i] = color;
        return new ColorStateList(states, colors);
    }

    public static void setTint(EditText editText, int color) {
        ColorStateList editTextColorStateList = createEditTextColorStateList(editText.getContext(), color);
        if (editText instanceof AppCompatEditText) {
            ((AppCompatEditText) editText).setSupportBackgroundTintList(editTextColorStateList);
        } else if (VERSION.SDK_INT >= 21) {
            editText.setBackgroundTintList(editTextColorStateList);
        }
    }

    public static void setTint(CheckBox box, int color) {
        r2 = new int[2][];
        r2[0] = new int[]{-16842912};
        r2[1] = new int[]{16842912};
        ColorStateList sl = new ColorStateList(r2, new int[]{DialogUtils.resolveColor(box.getContext(), R.attr.colorControlNormal), color});
        if (VERSION.SDK_INT >= 21) {
            box.setButtonTintList(sl);
            return;
        }
        Drawable drawable = DrawableCompat.wrap(ContextCompat.getDrawable(box.getContext(), R.drawable.abc_btn_check_material));
        DrawableCompat.setTintList(drawable, sl);
        box.setButtonDrawable(drawable);
    }
}
