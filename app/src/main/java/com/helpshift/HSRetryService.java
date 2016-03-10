package com.helpshift;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public final class HSRetryService extends Service {
    public static final String TAG = "HelpShiftDebug";
    private HSApiData data = null;

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (this.data == null) {
            this.data = new HSApiData(this);
        }
        this.data.sendFailedMessages();
        this.data.sendFailedApiCalls();
        stopSelf();
        return 2;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
