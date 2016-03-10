package com.facebook.share.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.facebook.R;
import com.facebook.internal.AnalyticsEvents;
import com.facebook.internal.CallbackManagerImpl.RequestCodeOffset;

public final class ShareButton extends ShareButtonBase {
    public ShareButton(Context context) {
        super(context, null, 0, AnalyticsEvents.EVENT_SHARE_BUTTON_CREATE);
    }

    public ShareButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0, AnalyticsEvents.EVENT_SHARE_BUTTON_CREATE);
    }

    public ShareButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, AnalyticsEvents.EVENT_SHARE_BUTTON_CREATE);
    }

    protected int getDefaultStyleResource() {
        return R.style.com_facebook_button_share;
    }

    protected OnClickListener getShareOnClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                ShareDialog dialog;
                ShareButton.this.callExternalOnClickListener(v);
                if (ShareButton.this.getFragment() != null) {
                    dialog = new ShareDialog(ShareButton.this.getFragment(), ShareButton.this.getRequestCode());
                } else {
                    dialog = new ShareDialog(ShareButton.this.getActivity(), ShareButton.this.getRequestCode());
                }
                dialog.show(ShareButton.this.getShareContent());
            }
        };
    }

    protected int getDefaultRequestCode() {
        return RequestCodeOffset.Share.toRequestCode();
    }
}
