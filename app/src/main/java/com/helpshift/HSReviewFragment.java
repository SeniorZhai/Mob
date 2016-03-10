package com.helpshift;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import com.helpshift.D.string;
import com.helpshift.Helpshift.HS_RATE_ALERT;
import com.helpshift.res.values.HSConsts;
import com.helpshift.util.HSActivityUtil;
import com.helpshift.util.Meta;
import io.fabric.sdk.android.BuildConfig;
import org.json.JSONException;

public final class HSReviewFragment extends DialogFragment {
    private static HSAlertToRateAppListener alertToRateAppListener;
    private final String TAG = Meta.TAG;
    private HSApiData data;
    private boolean disableReview = true;
    private String rurl = BuildConfig.FLAVOR;
    private HSStorage storage;

    protected static void setAlertToRateAppListener(HSAlertToRateAppListener listener) {
        alertToRateAppListener = listener;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        Bundle extras = activity.getIntent().getExtras();
        if (extras != null) {
            this.disableReview = extras.getBoolean("disableReview", true);
            this.rurl = extras.getString("rurl");
        }
        this.data = new HSApiData(activity);
        this.storage = this.data.storage;
        return initAlertDialog(activity);
    }

    private void gotoApp(String url) {
        if (!TextUtils.isEmpty(url)) {
            Intent intent = new Intent("android.intent.action.VIEW");
            intent.setData(Uri.parse(url.trim()));
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                getActivity().startActivity(intent);
            }
        }
    }

    public void onCancel(DialogInterface dialog) {
        HSFunnel.pushAppReviewedEvent("later");
        sendAlertToRateAppAction(HS_RATE_ALERT.CLOSE);
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.disableReview) {
            this.data.disableReview();
        }
        getActivity().finish();
    }

    private void sendAlertToRateAppAction(HS_RATE_ALERT action) {
        if (alertToRateAppListener != null) {
            alertToRateAppListener.onAction(action);
        }
        alertToRateAppListener = null;
    }

    private Dialog initAlertDialog(FragmentActivity activity) {
        Builder builder = new Builder(activity);
        builder.setMessage(string.hs__review_message);
        AlertDialog dialog = builder.create();
        dialog.setTitle(string.hs__review_title);
        dialog.setIcon(activity.getApplicationInfo().icon);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setButton(-1, getResources().getString(string.hs__rate_button), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (TextUtils.isEmpty(HSReviewFragment.this.rurl)) {
                        HSReviewFragment.this.rurl = HSReviewFragment.this.storage.getConfig().optString("rurl", BuildConfig.FLAVOR);
                    }
                    HSReviewFragment.this.rurl = HSReviewFragment.this.rurl.trim();
                    if (!TextUtils.isEmpty(HSReviewFragment.this.rurl)) {
                        HSReviewFragment.this.gotoApp(HSReviewFragment.this.rurl);
                    }
                } catch (JSONException e) {
                    Log.d(Meta.TAG, e.getMessage());
                }
                HSFunnel.pushAppReviewedEvent("reviewed");
                HSReviewFragment.this.sendAlertToRateAppAction(HS_RATE_ALERT.SUCCESS);
            }
        });
        dialog.setButton(-3, getResources().getString(string.hs__feedback_button), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HSFunnel.pushAppReviewedEvent("feedback");
                HSReviewFragment.this.sendAlertToRateAppAction(HS_RATE_ALERT.FEEDBACK);
                if (!HSReviewFragment.this.storage.getIsConversationShowing().booleanValue()) {
                    Intent i = new Intent(HSReviewFragment.this.getActivity(), HSConversation.class);
                    i.putExtra("decomp", true);
                    i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(HSReviewFragment.this.getActivity()));
                    i.putExtra("chatLaunchSource", HSConsts.SRC_SUPPORT);
                    i.putExtra("isRoot", true);
                    i.putExtra(HSConsts.SEARCH_PERFORMED, true);
                    HSReviewFragment.this.getActivity().startActivity(i);
                }
            }
        });
        dialog.setButton(-2, getResources().getString(string.hs__review_close_button), new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                HSFunnel.pushAppReviewedEvent("later");
                HSReviewFragment.this.sendAlertToRateAppAction(HS_RATE_ALERT.CLOSE);
            }
        });
        return dialog;
    }
}
