package com.mobcrush.mobcrush.broadcast;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.app.AlertDialog.Builder;
import android.util.DisplayMetrics;
import android.util.Log;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.MobcrushActivty;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.common.PreferenceUtility;
import com.mobcrush.mobcrush.common.Utils;
import com.mobcrush.mobcrush.datamodel.User;
import com.mobcrush.mobcrush.datamodel.UserChannel;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.services.common.CommonUtils;

public class BroadcastActivity extends MobcrushActivty {
    private static final int PERMISSION_CODE = 1;
    private static final String TAG = BroadcastActivity.class.getName();
    private int mScreenDensity;
    public User mUser;

    static /* synthetic */ class AnonymousClass14 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$broadcast$BroadcastActivity$Approval = new int[Approval.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$broadcast$BroadcastActivity$Approval[Approval.WHITELISTED.ordinal()] = BroadcastActivity.PERMISSION_CODE;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$broadcast$BroadcastActivity$Approval[Approval.BLACKLISTED.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    private enum Approval {
        WHITELISTED,
        BLACKLISTED,
        NOT_FOUND
    }

    public static Intent getIntent(Context context) {
        Intent intent = new Intent(context, BroadcastActivity.class);
        intent.setFlags(AccessibilityNodeInfoCompat.ACTION_SET_SELECTION);
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (VERSION.SDK_INT >= 21) {
            Network.checkStreamDeviceApproval(this, new Listener<String>() {
                public void onResponse(String response) {
                    switch (AnonymousClass14.$SwitchMap$com$mobcrush$mobcrush$broadcast$BroadcastActivity$Approval[Approval.valueOf(response.toUpperCase()).ordinal()]) {
                        case BroadcastActivity.PERMISSION_CODE /*1*/:
                            BroadcastActivity.this.checkWifiStatusAndStart();
                            return;
                        case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            BroadcastActivity.this.showBlacklistedDialog();
                            return;
                        default:
                            BroadcastActivity.this.showWarningDialog();
                            return;
                    }
                }
            }, new ErrorListener() {
                public void onErrorResponse(VolleyError error) {
                    BroadcastActivity.this.finish();
                }
            });
        } else {
            new Builder(this).setTitle((int) R.string.oh_no).setMessage((int) R.string.os_not_supported).setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    BroadcastActivity.this.finish();
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    BroadcastActivity.this.finish();
                }
            }).create().show();
        }
    }

    private void showWarningDialog() {
        new Builder(this).setTitle(17039380).setMessage((int) R.string.device_support_unknown).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BroadcastActivity.this.checkWifiStatusAndStart();
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BroadcastActivity.this.finish();
            }
        }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                BroadcastActivity.this.finish();
            }
        }).create().show();
    }

    private void showBlacklistedDialog() {
        new Builder(this).setTitle((int) R.string.oh_no).setMessage((int) R.string.device_not_supported).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BroadcastActivity.this.finish();
            }
        }).setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                BroadcastActivity.this.finish();
            }
        }).create().show();
    }

    private void checkWifiStatusAndStart() {
        if (Utils.isWifiAvailable(this) || !PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_key_broadcast_over_wifi), true)) {
            initStreamingService();
        } else {
            new Builder(this).setTitle(17039380).setMessage((int) R.string.wifi_disabled_warning).setPositiveButton((int) R.string.just_once, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    BroadcastActivity.this.initStreamingService();
                }
            }).setNegativeButton((int) R.string.cancel, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    BroadcastActivity.this.finish();
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    BroadcastActivity.this.finish();
                }
            }).create().show();
        }
    }

    @TargetApi(21)
    private void initStreamingService() {
        this.mUser = PreferenceUtility.getUser();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        this.mScreenDensity = metrics.densityDpi;
        startActivityForResult(((MediaProjectionManager) getSystemService("media_projection")).createScreenCaptureIntent(), PERMISSION_CODE);
    }

    @TargetApi(21)
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PERMISSION_CODE /*1*/:
                if (resultCode != -1) {
                    finish();
                    return;
                }
                Intent broadcastServiceIntent = new Intent(this, BroadcastService.class);
                broadcastServiceIntent.putExtra(Constants.EXTRA_PROJECTION_INTENT, data);
                broadcastServiceIntent.putExtra(Constants.EXTRA_PROJECTION_CODE, resultCode);
                startStreamingService(broadcastServiceIntent);
                return;
            default:
                Log.e(TAG, "Unknown request code: " + requestCode);
                return;
        }
    }

    @TargetApi(21)
    private void startStreamingService(final Intent broadcastServiceIntent) {
        if (PreferenceUtility.getStreamKey() == null) {
            Network.getUserChannels(null, false, new Listener<UserChannel[]>() {
                public void onResponse(UserChannel[] response) {
                    BroadcastActivity.this.startStreamingService(broadcastServiceIntent);
                }
            }, null);
            return;
        }
        broadcastServiceIntent.putExtra(Constants.EXTRA_SCREEN_DENSITY, this.mScreenDensity);
        broadcastServiceIntent.putExtra(Constants.EXTRA_STREAM_KEY, PreferenceUtility.getStreamKey());
        startService(broadcastServiceIntent);
        Intent homeIntent = new Intent("android.intent.action.MAIN");
        homeIntent.addCategory("android.intent.category.HOME");
        homeIntent.setFlags(268435456);
        startActivity(homeIntent);
        finish();
    }
}
