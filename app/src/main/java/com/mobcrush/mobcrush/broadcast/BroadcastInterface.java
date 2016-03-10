package com.mobcrush.mobcrush.broadcast;

import android.content.Intent;
import com.mobcrush.mobcrush.MainApplication;

public class BroadcastInterface {
    public void sendMessage(int code) {
        Intent intent = new Intent(BroadcastService.ACTION_SERVICE_STATUS);
        intent.putExtra(BroadcastService.EXTRA_CODE, code);
        MainApplication.getContext().sendBroadcast(intent);
    }
}
