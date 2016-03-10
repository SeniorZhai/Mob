package com.helpshift.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import com.helpshift.D.attr;

public class Styles {
    public static int getColor(Context context, int attribute) {
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(new int[]{attribute});
        int color = typedArray.getColor(0, -1);
        typedArray.recycle();
        return color;
    }

    public static void setActionButtonIconColor(Context context, Drawable actionButtonIcon) {
        setColorFilter(context, actionButtonIcon, attr.hs__actionButtonIconColor);
    }

    public static void setActionButtonNotificationIconColor(Context context, Drawable actionButtonNotificationIcon) {
        setColorFilter(context, actionButtonNotificationIcon, attr.hs__actionButtonNotificationIconColor);
    }

    public static void setButtonCompoundDrawableIconColor(Context context, Drawable buttonCompoundDrawableIcon) {
        setColorFilter(context, buttonCompoundDrawableIcon, attr.hs__buttonCompoundDrawableIconColor);
    }

    public static void setSendMessageButtonIconColor(Context context, Drawable sendMessageButtonIcon) {
        setColorFilter(context, sendMessageButtonIcon, attr.hs__sendMessageButtonIconColor);
    }

    public static void setSendMessageButtonActiveIconColor(Context context, Drawable sendMessageButtonActiveIcon) {
        setColorFilter(context, sendMessageButtonActiveIcon, attr.hs__sendMessageButtonActiveIconColor);
    }

    public static void setAcceptButtonIconColor(Context context, Drawable acceptButtonIcon) {
        setColorFilter(context, acceptButtonIcon, attr.hs__acceptButtonIconColor);
    }

    public static void setRejectButtonIconColor(Context context, Drawable rejectButtonIcon) {
        setColorFilter(context, rejectButtonIcon, attr.hs__rejectButtonIconColor);
    }

    public static void setAttachScreenshotButtonIconColor(Context context, Drawable attachScreenshotButtonIcon) {
        setColorFilter(context, attachScreenshotButtonIcon, attr.hs__attachScreenshotButtonIconColor);
    }

    public static void setReviewButtonIconColor(Context context, Drawable reviewButtonIcon) {
        setColorFilter(context, reviewButtonIcon, attr.hs__reviewButtonIconColor);
    }

    public static void setAdminChatBubbleColor(Context context, Drawable adminChatBubbleNinePatch) {
        setColorFilter(context, adminChatBubbleNinePatch, attr.hs__adminChatBubbleColor);
    }

    public static void setUserChatBubbleColor(Context context, Drawable userChatBubbleNinePatch) {
        setColorFilter(context, userChatBubbleNinePatch, attr.hs__userChatBubbleColor);
    }

    public static void setDownloadAttachmentButtonIconColor(Context context, Drawable downloadAttachmentButtonIcon) {
        setColorFilter(context, downloadAttachmentButtonIcon, attr.hs__downloadAttachmentButtonIconColor);
    }

    public static void setLaunchAttachmentButtonIconColor(Context context, Drawable launchAttachmentButtonIcon) {
        setColorFilter(context, launchAttachmentButtonIcon, attr.hs__launchAttachmentButtonIconColor);
    }

    private static void setColorFilter(Context context, Drawable drawable, int attr) {
        if (drawable != null) {
            drawable.setColorFilter(getColor(context, attr), Mode.SRC_ATOP);
        }
    }
}
