package com.facebook.share.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import com.facebook.R;
import com.facebook.internal.AnalyticsEvents;
import com.facebook.internal.CallbackManagerImpl.RequestCodeOffset;

public final class SendButton extends ShareButtonBase {
    public SendButton(Context context) {
        super(context, null, 0, AnalyticsEvents.EVENT_SEND_BUTTON_CREATE);
    }

    public SendButton(Context context, AttributeSet attrs) {
        super(context, attrs, 0, AnalyticsEvents.EVENT_SEND_BUTTON_CREATE);
    }

    public SendButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, AnalyticsEvents.EVENT_SEND_BUTTON_CREATE);
    }

    protected int getDefaultStyleResource() {
        return R.style.com_facebook_button_send;
    }

    protected OnClickListener getShareOnClickListener() {
        return new OnClickListener() {
            public void onClick(View v) {
                MessageDialog dialog;
                SendButton.this.callExternalOnClickListener(v);
                if (SendButton.this.getFragment() != null) {
                    dialog = new MessageDialog(SendButton.this.getFragment(), SendButton.this.getRequestCode());
                } else {
                    dialog = new MessageDialog(SendButton.this.getActivity(), SendButton.this.getRequestCode());
                }
                dialog.show(SendButton.this.getShareContent());
            }
        };
    }

    protected int getDefaultRequestCode() {
        return RequestCodeOffset.Message.toRequestCode();
    }
}
