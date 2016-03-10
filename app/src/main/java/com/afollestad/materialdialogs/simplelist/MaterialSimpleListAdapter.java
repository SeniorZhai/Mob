package com.afollestad.materialdialogs.simplelist;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.R;

public class MaterialSimpleListAdapter extends ArrayAdapter<MaterialSimpleListItem> {
    private MaterialDialog dialog;

    public void setDialog(MaterialDialog dialog) {
        setDialog(dialog, true);
    }

    public void setDialog(MaterialDialog dialog, boolean notifyDataSetChanged) {
        this.dialog = dialog;
        if (notifyDataSetChanged) {
            notifyDataSetChanged();
        }
    }

    public MaterialSimpleListAdapter(Context context) {
        super(context, R.layout.md_simplelist_item, 16908310);
    }

    public boolean hasStableIds() {
        return true;
    }

    public long getItemId(int position) {
        return (long) position;
    }

    public View getView(int index, View convertView, ViewGroup parent) {
        View view = super.getView(index, convertView, parent);
        if (this.dialog != null) {
            MaterialSimpleListItem item = (MaterialSimpleListItem) getItem(index);
            ImageView ic = (ImageView) view.findViewById(16908294);
            if (item.getIcon() != null) {
                ic.setImageDrawable(item.getIcon());
            } else {
                ic.setVisibility(8);
            }
            TextView tv = (TextView) view.findViewById(16908310);
            tv.setTextColor(this.dialog.getBuilder().getItemColor());
            tv.setText(item.getContent());
            this.dialog.setTypeface(tv, this.dialog.getBuilder().getRegularFont());
            setupGravity((ViewGroup) view);
        }
        return view;
    }

    @TargetApi(17)
    private void setupGravity(ViewGroup view) {
        LinearLayout itemRoot = (LinearLayout) view;
        GravityEnum gravity = this.dialog.getBuilder().getItemsGravity();
        itemRoot.setGravity(gravity.getGravityInt() | 16);
        if (view.getChildCount() != 2) {
            return;
        }
        CompoundButton first;
        TextView second;
        if (this.dialog.getBuilder().getItemsGravity() == GravityEnum.END && !isRTL() && (view.getChildAt(0) instanceof ImageView)) {
            first = (CompoundButton) view.getChildAt(0);
            view.removeView(first);
            second = (TextView) view.getChildAt(0);
            view.removeView(second);
            second.setPadding(second.getPaddingRight(), second.getPaddingTop(), second.getPaddingLeft(), second.getPaddingBottom());
            view.addView(second);
            view.addView(first);
        } else if (gravity == GravityEnum.START && isRTL() && (view.getChildAt(1) instanceof ImageView)) {
            first = (CompoundButton) view.getChildAt(1);
            view.removeView(first);
            second = (TextView) view.getChildAt(0);
            view.removeView(second);
            second.setPadding(second.getPaddingRight(), second.getPaddingTop(), second.getPaddingRight(), second.getPaddingBottom());
            view.addView(first);
            view.addView(second);
        }
    }

    @TargetApi(17)
    private boolean isRTL() {
        boolean z = true;
        if (VERSION.SDK_INT < 17) {
            return false;
        }
        if (getContext().getResources().getConfiguration().getLayoutDirection() != 1) {
            z = false;
        }
        return z;
    }
}
