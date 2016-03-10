package com.mobcrush.mobcrush;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import com.android.volley.Response.Listener;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.network.Network;

public class EmailVerificationRequestActivity extends MobcrushActivty implements OnClickListener {
    public static Intent getIntent(Context context) {
        return new Intent(context, EmailVerificationRequestActivity.class);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= 17) {
            getWindow().getDecorView().setSystemUiVisibility(4);
        } else {
            getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
        }
        setContentView((int) R.layout.activity_email_verification_request);
        findViewById(R.id.close_iv).setOnClickListener(this);
        findViewById(R.id.resend_button).setOnClickListener(this);
        ((TextView) findViewById(R.id.title)).setTypeface(UIUtils.getTypeface(this, Constants.ROBOTO_LIGHT_FONT_NAME));
        ((TextView) findViewById(R.id.top_description_tv)).setTypeface(UIUtils.getTypeface(this, Constants.ROBOTO_LIGHT_FONT_NAME));
        ((TextView) findViewById(R.id.bottom_description_tv)).setTypeface(UIUtils.getTypeface(this, Constants.ROBOTO_LIGHT_FONT_NAME));
    }

    public void onClick(final View view) {
        if (view != null) {
            switch (view.getId()) {
                case R.id.close_iv:
                    finish();
                    return;
                case R.id.resend_button:
                    view.setOnClickListener(null);
                    view.postDelayed(new Runnable() {
                        public void run() {
                            Network.resendVerificationEmail(EmailVerificationRequestActivity.this, new Listener<Boolean>() {
                                public void onResponse(Boolean response) {
                                    if (response != null && response.booleanValue()) {
                                        EmailVerificationRequestActivity.this.finish();
                                    }
                                    view.setOnClickListener(null);
                                }
                            }, null);
                            EmailVerificationRequestActivity.this.finish();
                        }
                    }, (long) getResources().getInteger(17694720));
                    return;
                default:
                    return;
            }
        }
    }
}
