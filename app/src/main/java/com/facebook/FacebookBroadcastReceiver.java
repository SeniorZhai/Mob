package com.facebook;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.facebook.internal.NativeProtocol;

public class FacebookBroadcastReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String appCallId = intent.getStringExtra(NativeProtocol.EXTRA_PROTOCOL_CALL_ID);
        String action = intent.getStringExtra(NativeProtocol.EXTRA_PROTOCOL_ACTION);
        if (appCallId != null && action != null) {
            Bundle extras = intent.getExtras();
            if (NativeProtocol.isErrorResult(intent)) {
                onFailedAppCall(appCallId, action, extras);
            } else {
                onSuccessfulAppCall(appCallId, action, extras);
            }
        }
    }

    protected void onSuccessfulAppCall(String appCallId, String action, Bundle extras) {
    }

    protected void onFailedAppCall(String appCallId, String action, Bundle extras) {
    }
}
