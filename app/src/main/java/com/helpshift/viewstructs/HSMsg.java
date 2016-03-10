package com.helpshift.viewstructs;

import android.util.Log;
import com.helpshift.util.HSFormat;
import com.helpshift.util.Meta;
import io.fabric.sdk.android.BuildConfig;
import java.text.ParseException;
import java.util.Date;

public final class HSMsg {
    public static String TAG = Meta.TAG;
    public String agentName = BuildConfig.FLAVOR;
    public String body;
    public Boolean clickable = Boolean.valueOf(true);
    public String date;
    public String id;
    public Boolean inProgress = Boolean.valueOf(false);
    public Boolean invisible = Boolean.valueOf(false);
    public String origin;
    public String screenshot;
    public int state;
    public String type;

    public HSMsg(String id, String type, String origin, String body, String date, Boolean invisible, String screenshot, int state, Boolean inProgress, String agentName) {
        this.id = id;
        this.type = type;
        this.origin = origin;
        this.invisible = invisible;
        this.screenshot = screenshot;
        this.state = state;
        this.inProgress = inProgress;
        this.body = body;
        if (origin.equals("admin") && !agentName.equals(BuildConfig.FLAVOR)) {
            this.agentName = agentName + ", ";
        }
        String inputDate = date;
        Date outputDate = new Date();
        try {
            outputDate = HSFormat.inputMsgFormatter.parse(inputDate);
        } catch (ParseException e) {
            Log.d(TAG, e.toString(), e);
        }
        this.date = this.agentName + HSFormat.outputMsgFormatter.format(outputDate);
    }
}
