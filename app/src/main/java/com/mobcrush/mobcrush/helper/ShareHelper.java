package com.mobcrush.mobcrush.helper;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.widget.ListPopupWindow;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.EmailVerificationRequestActivity;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.ShareToActivity;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.UIUtils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.datamodel.EntityType;
import com.mobcrush.mobcrush.logic.SocialNetwork;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper.ShareType;
import com.mobcrush.mobcrush.network.Network;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShareHelper {
    public static void showSharePopupMenu(View anchor, final OnItemClickListener menuItemClickListener, boolean showYoutube, final Callback onMenuClosed) {
        if (anchor == null || PreferenceUtility.isEmailVerified()) {
            List<HashMap<String, Object>> data = new ArrayList();
            UIUtils.addListMenuItem(data, MainApplication.getRString(R.string.action_share_copy_url, new Object[0]), R.drawable.ic_menu_copy);
            UIUtils.addListMenuItem(data, MainApplication.getRString(R.string.action_share_to_facebook, new Object[0]), R.drawable.ic_facebook);
            UIUtils.addListMenuItem(data, MainApplication.getRString(R.string.action_share_to_twitter, new Object[0]), R.drawable.ic_twitter);
            UIUtils.addListMenuItem(data, MainApplication.getRString(R.string.action_share_to_followers, new Object[0]), R.drawable.ic_share_followers_specific);
            UIUtils.addListMenuItem(data, MainApplication.getRString(R.string.action_share_to_all, new Object[0]), R.drawable.ic_share_followers_all);
            try {
                Crashlytics.log("going to show SharePopupMenu");
                final ListPopupWindow popupWindow = new ListPopupWindow(anchor.getContext());
                popupWindow.setOnDismissListener(new OnDismissListener() {
                    public void onDismiss() {
                        if (onMenuClosed != null) {
                            onMenuClosed.handleMessage(Message.obtain());
                        }
                    }
                });
                UIUtils.showListMenu(popupWindow, anchor, data, Constants.MENU_ADAPTER_KEYS, new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (menuItemClickListener != null) {
                            menuItemClickListener.onItemClick(parent, view, position, id);
                        }
                        if (popupWindow.isShowing()) {
                            popupWindow.dismiss();
                        }
                    }
                });
                return;
            } catch (Throwable e) {
                Throwable e2 = new Exception("Error while showing SharePopupMenu: " + e.getMessage(), e);
                e2.printStackTrace();
                Crashlytics.logException(e2);
                return;
            }
        }
        anchor.getContext().startActivity(new Intent(anchor.getContext(), EmailVerificationRequestActivity.class));
    }

    public static OnItemClickListener getShareMenuItemClickListener(final FragmentActivity activity, final Broadcast broadcast) {
        return new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (id == ((long) activity.getString(R.string.action_share_copy_url).hashCode())) {
                    Broadcast broadcast = broadcast;
                    broadcast.urlsCopied++;
                    ((ClipboardManager) activity.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText(broadcast.title, broadcast.getURL(PreferenceUtility.getConfig())));
                    MixpanelHelper.getInstance(MainApplication.getContext()).trackShareEvent(broadcast, SocialNetwork.URL, ShareType.COPY_URL, 1);
                    return;
                }
                String network;
                String trackedNetwork;
                if (id == ((long) activity.getString(R.string.action_share_to_facebook).hashCode())) {
                    network = Constants.FACEBOOK;
                    trackedNetwork = broadcast._id + ":FB";
                } else if (id == ((long) activity.getString(R.string.action_share_to_twitter).hashCode())) {
                    network = Constants.TWITTER;
                    trackedNetwork = broadcast._id + ":TW";
                } else if (id == ((long) activity.getString(R.string.action_share_to_youtube).hashCode())) {
                    network = Constants.GOOGLE;
                    trackedNetwork = broadcast._id + ":GOOGLE";
                } else if (id == ((long) activity.getString(R.string.action_share_to_followers).hashCode())) {
                    activity.startActivity(ShareToActivity.getIntent(activity, broadcast));
                    return;
                } else if (id == ((long) activity.getString(R.string.action_share_to_all).hashCode())) {
                    new Builder(activity).setMessage(activity.getString(R.string.share_with_all_confirmation, new Object[]{PreferenceUtility.getUser().followerCount})).setPositiveButton((int) R.string.action_share, new OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ShareHelper.shareToAll(activity, broadcast);
                        }
                    }).setNegativeButton(17039360, null).create().show();
                    return;
                } else {
                    return;
                }
                Network.shareBroadcastTo(activity, broadcast._id, network, new Listener<Boolean>() {
                    public void onResponse(Boolean response) {
                        if (response == null || !response.booleanValue()) {
                            activity.startActivityForResult(SocialConnectHelperActivity.getIntent(activity, SocialNetwork.valueOf(network), broadcast), 0);
                            return;
                        }
                        try {
                            GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_SHARES, trackedNetwork, Long.valueOf(1));
                            MixpanelHelper.getInstance(MainApplication.getContext()).trackShareEvent(broadcast, SocialNetwork.valueOf(network), ShareType.valueOf(network.toUpperCase()), 1);
                            Toast.makeText(activity, R.string.broadcast_was_shared, 1).show();
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                            e.printStackTrace();
                        }
                    }
                }, null);
            }
        };
    }

    public static void shareToAll(final FragmentActivity activity, final Broadcast broadcast) {
        final ProgressDialog progressDialog = new ProgressDialog(activity);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(activity.getString(R.string.sharing__));
        progressDialog.show();
        Network.notifyFollowers(activity, EntityType.broadcast, broadcast._id, null, true, new Listener<Boolean>() {
            public void onResponse(Boolean response) {
                ShareHelper.closeDialog(progressDialog);
                if (response != null && response.booleanValue()) {
                    GoogleAnalyticsUtils.trackAction(Constants.CATEGORY_VIEWER, Constants.ACTION_SHARES, broadcast._id + ":MC", Long.valueOf((long) PreferenceUtility.getUser().followerCount.intValue()));
                    MixpanelHelper.getInstance(MainApplication.getContext()).trackShareEvent(broadcast, SocialNetwork.Mobcrush, ShareType.ALL_FOLLOWERS, PreferenceUtility.getUser().followerCount.intValue());
                }
                FragmentActivity fragmentActivity = activity;
                boolean z = response != null && response.booleanValue();
                ShareHelper.showSharingResult(fragmentActivity, z);
            }
        }, new ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                ShareHelper.closeDialog(progressDialog);
                ShareHelper.showSharingResult(activity, false);
            }
        });
    }

    private static void closeDialog(ProgressDialog progressDialog) {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (Exception e) {
            }
        }
    }

    private static void showSharingResult(FragmentActivity activity, boolean isSuccessful) {
        Toast.makeText(activity, isSuccessful ? R.string.broadcast_was_shared : R.string.error_sharing_broadcast, 1).show();
    }
}
