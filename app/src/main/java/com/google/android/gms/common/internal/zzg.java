package com.google.android.gms.common.internal;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

public class zzg implements OnClickListener {
    private final Activity mActivity;
    private final Intent mIntent;
    private final Fragment zzZX;
    private final int zzZY;

    public zzg(Activity activity, Intent intent, int i) {
        this.mActivity = activity;
        this.zzZX = null;
        this.mIntent = intent;
        this.zzZY = i;
    }

    public zzg(Fragment fragment, Intent intent, int i) {
        this.mActivity = null;
        this.zzZX = fragment;
        this.mIntent = intent;
        this.zzZY = i;
    }

    public void onClick(DialogInterface dialog, int which) {
        try {
            if (this.mIntent != null && this.zzZX != null) {
                this.zzZX.startActivityForResult(this.mIntent, this.zzZY);
            } else if (this.mIntent != null) {
                this.mActivity.startActivityForResult(this.mIntent, this.zzZY);
            }
            dialog.dismiss();
        } catch (ActivityNotFoundException e) {
            Log.e("SettingsRedirect", "Can't redirect to app settings for Google Play services");
        }
    }
}
