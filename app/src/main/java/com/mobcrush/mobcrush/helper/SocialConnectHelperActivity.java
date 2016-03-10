package com.mobcrush.mobcrush.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.android.volley.Response.Listener;
import com.crashlytics.android.Crashlytics;
import com.facebook.CallbackManager;
import com.facebook.CallbackManager.Factory;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.gson.Gson;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.InAppBrowser;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.common.GoogleAnalyticsUtils;
import com.mobcrush.mobcrush.datamodel.Broadcast;
import com.mobcrush.mobcrush.logic.SocialNetwork;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper.Event;
import com.mobcrush.mobcrush.mixpanel.MixpanelHelper.ShareType;
import com.mobcrush.mobcrush.network.Network;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.util.Collections;

public class SocialConnectHelperActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_CONNECT_TWITTER = 1002;
    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
    private Broadcast mBroadcast;
    private String mEmail;
    private CallbackManager mFBCallbackManager;
    private String mNetwork;

    static /* synthetic */ class AnonymousClass5 {
        static final /* synthetic */ int[] $SwitchMap$com$mobcrush$mobcrush$logic$SocialNetwork = new int[SocialNetwork.values().length];

        static {
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$SocialNetwork[SocialNetwork.Facebook.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$SocialNetwork[SocialNetwork.Twitter.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$mobcrush$mobcrush$logic$SocialNetwork[SocialNetwork.Google.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
    }

    public static Intent getIntent(Context context, SocialNetwork network) {
        return getIntent(context, network, null);
    }

    public static Intent getIntent(Context context, SocialNetwork network, Broadcast broadcast) {
        Intent intent = new Intent(context, SocialConnectHelperActivity.class);
        intent.putExtra("android.intent.extra.TEXT", String.valueOf(network));
        if (broadcast != null) {
            intent.putExtra(Constants.EXTRA_BROADCAST, broadcast.toString());
        }
        return intent;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_social_connect_helper);
        String broadcast = getIntent().getExtras().getString(Constants.EXTRA_BROADCAST);
        if (broadcast != null) {
            this.mBroadcast = (Broadcast) new Gson().fromJson(broadcast, Broadcast.class);
        }
        this.mNetwork = getIntent().getStringExtra("android.intent.extra.TEXT");
        this.mFBCallbackManager = Factory.create();
        try {
            switch (AnonymousClass5.$SwitchMap$com$mobcrush$mobcrush$logic$SocialNetwork[SocialNetwork.valueOf(this.mNetwork).ordinal()]) {
                case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                    connectFacebook();
                    return;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    connectTwitter();
                    return;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    pickUserAccount();
                    return;
                default:
                    return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        e.printStackTrace();
        finish();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            this.mFBCallbackManager.onActivityResult(requestCode, resultCode, data);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.error_network_undeterminated, 0).show();
        }
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == -1) {
                this.mEmail = data.getStringExtra("authAccount");
                if (this.mEmail != null) {
                    try {
                        fetchToken(this.mEmail);
                        return;
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        Toast.makeText(this, R.string.error_network_undeterminated, 0).show();
                    }
                }
            }
            setResult(0);
            finish();
        } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR) {
            if (resultCode == -1 && this.mEmail != null) {
                try {
                    fetchToken(this.mEmail);
                    return;
                } catch (IOException e22) {
                    e22.printStackTrace();
                }
            }
            setResult(0);
            finish();
        } else if (requestCode == REQUEST_CODE_CONNECT_TWITTER) {
            if (resultCode == -1) {
                MixpanelHelper.getInstance(MainApplication.getContext()).generateEvent(Event.CONNECT_TWITTER);
                shareItem();
            }
            setResult(resultCode);
            finish();
        }
    }

    private void pickUserAccount() {
        startActivityForResult(AccountPicker.newChooseAccountIntent(null, null, new String[]{GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE}, false, null, null, null, null), REQUEST_CODE_PICK_ACCOUNT);
    }

    protected void fetchToken(final String email) throws IOException {
        new AsyncTask<Void, String, String>() {
            protected String doInBackground(Void... voids) {
                try {
                    String token = GoogleAuthUtil.getToken(SocialConnectHelperActivity.this, email, "oauth2:profile https://www.googleapis.com/auth/plus.login https://www.googleapis.com/auth/youtube.upload");
                    if (token == null) {
                        return token;
                    }
                    Network.connectYoutube(null, token, new Listener<Boolean>() {
                        public void onResponse(Boolean response) {
                            SocialConnectHelperActivity.this.shareItem();
                            SocialConnectHelperActivity.this.setResult(-1);
                            SocialConnectHelperActivity.this.finish();
                        }
                    }, null);
                    return token;
                } catch (Exception e) {
                    if (e instanceof UserRecoverableAuthException) {
                        SocialConnectHelperActivity.this.startActivityForResult(((UserRecoverableAuthException) e).getIntent(), SocialConnectHelperActivity.REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    }
                    return null;
                }
            }
        }.execute(new Void[0]);
    }

    private void connectFacebook() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        LoginManager.getInstance().registerCallback(this.mFBCallbackManager, new FacebookCallback<LoginResult>() {
            public void onSuccess(LoginResult loginResult) {
                if (loginResult != null) {
                    Network.connectFacebook(null, loginResult.getAccessToken(), new Listener<Boolean>() {
                        public void onResponse(Boolean response) {
                            if (response != null && response.booleanValue()) {
                                MixpanelHelper.getInstance(MainApplication.getContext()).generateEvent(Event.CONNECT_FACEBOOK);
                                SocialConnectHelperActivity.this.shareItem();
                                SocialConnectHelperActivity.this.setResult(-1);
                                SocialConnectHelperActivity.this.finish();
                            }
                        }
                    }, null);
                }
            }

            public void onCancel() {
                SocialConnectHelperActivity.this.setResult(0);
                SocialConnectHelperActivity.this.finish();
            }

            public void onError(FacebookException exception) {
                Crashlytics.logException(exception);
                exception.printStackTrace();
                SocialConnectHelperActivity.this.setResult(0);
                SocialConnectHelperActivity.this.finish();
            }
        });
        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_SETTINGS_FACEBOOK);
        LoginManager.getInstance().setLoginBehavior(LoginBehavior.NATIVE_WITH_FALLBACK);
        LoginManager.getInstance().logInWithPublishPermissions((Activity) this, Collections.singletonList("publish_actions"));
    }

    private void connectTwitter() {
        Network.getTwitterOAuthUrl(null, new Listener<String>() {
            public void onResponse(String response) {
                if (response != null) {
                    try {
                        GoogleAnalyticsUtils.trackScreenNamed(Constants.SCREEN_SETTINGS_TWITTER);
                        SocialConnectHelperActivity.this.startActivityForResult(InAppBrowser.getIntent(SocialConnectHelperActivity.this, response), SocialConnectHelperActivity.REQUEST_CODE_CONNECT_TWITTER);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Crashlytics.logException(e);
                        SocialConnectHelperActivity.this.setResult(0);
                        SocialConnectHelperActivity.this.finish();
                    }
                }
            }
        }, null);
    }

    private void shareItem() {
        if (this.mBroadcast != null) {
            Network.shareBroadcastTo(this, this.mBroadcast._id, this.mNetwork, new Listener<Boolean>() {
                public void onResponse(Boolean response) {
                    try {
                        MixpanelHelper.getInstance(MainApplication.getContext()).trackShareEvent(SocialConnectHelperActivity.this.mBroadcast, SocialNetwork.valueOf(SocialConnectHelperActivity.this.mNetwork), ShareType.valueOf(SocialConnectHelperActivity.this.mNetwork.toUpperCase()), 1);
                        Context context = SocialConnectHelperActivity.this;
                        int i = (response == null || !response.booleanValue()) ? R.string.error_sharing_broadcast : R.string.broadcast_was_shared;
                        Toast.makeText(context, i, 1).show();
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        e.printStackTrace();
                        SocialConnectHelperActivity.this.setResult(0);
                        SocialConnectHelperActivity.this.finish();
                    }
                }
            }, null);
        }
    }
}
