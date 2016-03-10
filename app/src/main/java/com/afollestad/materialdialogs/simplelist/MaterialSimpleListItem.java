package com.afollestad.materialdialogs.simplelist;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;

public class MaterialSimpleListItem {
    private Builder mBuilder;

    public static class Builder {
        protected CharSequence mContent;
        private Context mContext;
        protected Drawable mIcon;

        public Builder(Context context) {
            this.mContext = context;
        }

        public Builder icon(Drawable icon) {
            this.mIcon = icon;
            return this;
        }

        public Builder icon(@DrawableRes int iconRes) {
            return icon(ContextCompat.getDrawable(this.mContext, iconRes));
        }

        public Builder content(CharSequence content) {
            this.mContent = content;
            return this;
        }

        public Builder content(@StringRes int contentRes) {
            return content(this.mContext.getString(contentRes));
        }

        public MaterialSimpleListItem build() {
            return new MaterialSimpleListItem();
        }
    }

    private MaterialSimpleListItem(Builder builder) {
        this.mBuilder = builder;
    }

    public Drawable getIcon() {
        return this.mBuilder.mIcon;
    }

    public CharSequence getContent() {
        return this.mBuilder.mContent;
    }

    public String toString() {
        if (getContent() != null) {
            return getContent().toString();
        }
        return "(no content)";
    }
}
