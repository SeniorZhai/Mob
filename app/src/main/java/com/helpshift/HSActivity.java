package com.helpshift;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.DefaultRetryPolicy;
import com.helpshift.D.id;
import com.helpshift.D.menu;
import com.helpshift.app.ActionBarActivity;
import com.helpshift.res.values.HSConfig;
import com.helpshift.res.values.HSConsts;
import com.helpshift.util.HSActivityUtil;
import com.helpshift.util.LocaleUtil;
import com.helpshift.util.Styles;
import com.helpshift.view.SimpleMenuItemCompat;
import io.fabric.sdk.android.BuildConfig;
import org.json.JSONException;
import org.json.JSONObject;

class HSActivity extends ActionBarActivity {
    protected static final int CALL_FINISH_REQ_CODE = 1;
    protected static final String TAG = "HelpShiftDebug";
    private final int ISSUE_POLL_DURATION = 3;
    private TextView convIcon;
    private Menu conversationMenu = null;
    private HSApiData data;
    private boolean enableContactUs;
    private Bundle extras;
    private float horizontalScale;
    private TextView notifCount;
    private Thread pollerThread;
    private Handler pollerThreadHandler;
    private MenuItem reportIssueAction;
    private boolean screenInitialized;
    private String screenType;
    private boolean showConvOnReportIssue;
    private HSStorage storage;
    private float verticalScale;

    HSActivity() {
    }

    private void killPoller() {
        if (this.pollerThreadHandler != null) {
            this.pollerThreadHandler.getLooper().quit();
        }
    }

    protected void startPoller() {
        killPoller();
        if (!TextUtils.isEmpty(this.storage.getActiveConversation(this.data.getProfileId()))) {
            this.pollerThread = new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    HSActivity.this.pollerThreadHandler = new Handler() {
                        public void handleMessage(Message msg) {
                            HSActivity.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    HSActivity.this.updateCount(HSActivity.this.data.getProfileId());
                                }
                            });
                        }
                    };
                    new Runnable() {
                        public void run() {
                            try {
                                HSActivity.this.data.getLatestIssues(HSActivity.this.pollerThreadHandler, new Handler());
                            } catch (JSONException e) {
                                Log.d(HSActivity.TAG, "get issues", e);
                            }
                            HSActivity.this.pollerThreadHandler.postDelayed(this, 3000);
                        }
                    }.run();
                    Looper.loop();
                }
            });
            this.pollerThread.start();
        }
    }

    private void updateCount(String profileId) {
        int count = this.storage.getActiveNotifCnt(profileId).intValue();
        if (this.notifCount == null) {
            return;
        }
        if (count > 0) {
            this.convIcon.setVisibility(8);
            this.notifCount.setVisibility(0);
            this.notifCount.setText(BuildConfig.FLAVOR + count);
            return;
        }
        this.convIcon.setVisibility(0);
        this.notifCount.setVisibility(8);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HelpshiftContext.setApplicationContext(getApplicationContext());
        this.data = new HSApiData(this);
        this.storage = this.data.storage;
        LocaleUtil.changeLanguage(this);
        if (VERSION.SDK_INT >= 21) {
            getWindow().addFlags(Action.UNDEFINED_DURATION);
        }
        this.extras = getIntent().getExtras();
        if (this.extras != null) {
            this.showConvOnReportIssue = this.extras.getBoolean("showConvOnReportIssue", false);
        }
        this.data = new HSApiData(this);
        this.storage = this.data.storage;
        setSize();
        if (this instanceof HSQuestion) {
            HSQuestion activity = (HSQuestion) this;
            activity.extras = getIntent().getExtras();
            if (activity.isShowSearchOnNewConversationFlowActive()) {
                this.enableContactUs = false;
            } else {
                this.enableContactUs = ContactUsFilter.showContactUs(LOCATION.QUESTION_ACTION_BAR);
            }
        } else if (this instanceof SearchResultActivity) {
            this.enableContactUs = ContactUsFilter.showContactUs(LOCATION.SEARCH_RESULT_ACTIVITY_HEADER);
        } else {
            this.enableContactUs = ContactUsFilter.showContactUs(LOCATION.ACTION_BAR);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize();
    }

    private boolean isTablet() {
        if (TextUtils.isEmpty(this.screenType)) {
            this.screenType = getResources().getString(R.string.hs__screen_type);
        }
        return !this.screenType.equals("phone");
    }

    private void setSize() {
        if (isTablet() && isDialogUIForTabletsEnabled()) {
            initWindow();
            initScale();
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int windowWidth = (int) (((float) metrics.widthPixels) * this.horizontalScale);
            getWindow().setLayout(windowWidth, (int) (((float) metrics.heightPixels) * this.verticalScale));
        }
    }

    private boolean isDialogUIForTabletsEnabled() {
        Boolean enableDialogUIForTablets = Boolean.valueOf(false);
        try {
            enableDialogUIForTablets = Boolean.valueOf(this.storage.getAppConfig().optBoolean("enableDialogUIForTablets"));
        } catch (JSONException e) {
            Log.d(TAG, "isDialogUIForTabletsEnabled : ", e);
        }
        return enableDialogUIForTablets.booleanValue();
    }

    private void initScale() {
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.hs__tablet_dialog_horizontal_scale, outValue, true);
        this.horizontalScale = outValue.getFloat();
        outValue = new TypedValue();
        getResources().getValue(R.dimen.hs__tablet_dialog_vertical_scale, outValue, true);
        this.verticalScale = outValue.getFloat();
    }

    private void initWindow() {
        if (!this.screenInitialized) {
            requestWindowFeature(8);
            getWindow().setFlags(2, 2);
            LayoutParams params = getWindow().getAttributes();
            params.alpha = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
            params.dimAmount = 0.5f;
            getWindow().setAttributes(params);
            this.screenInitialized = true;
        }
    }

    protected void onResume() {
        super.onResume();
        if (this instanceof HSQuestion) {
            if (((HSQuestion) this).isShowSearchOnNewConversationFlowActive()) {
                this.enableContactUs = false;
            } else {
                this.enableContactUs = ContactUsFilter.showContactUs(LOCATION.QUESTION_ACTION_BAR);
            }
        } else if (this instanceof SearchResultActivity) {
            this.enableContactUs = ContactUsFilter.showContactUs(LOCATION.SEARCH_RESULT_ACTIVITY_HEADER);
        } else {
            this.enableContactUs = ContactUsFilter.showContactUs(LOCATION.ACTION_BAR);
        }
        if (!this.enableContactUs) {
            showConversationMenu(false);
        } else if (this.enableContactUs && !(this instanceof HSConversation)) {
            showConversationMenu(true);
            if (!TextUtils.isEmpty(this.data.getProfileId())) {
                updateCount(this.data.getProfileId());
                startPoller();
            }
        }
        try {
            JSONObject configData = this.storage.getConfig();
            if (configData.length() != 0) {
                HSConfig.updateConfig(configData);
            }
        } catch (JSONException e) {
            Log.d(TAG, e.toString(), e);
        }
    }

    protected void onPause() {
        super.onPause();
        killPoller();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        this.conversationMenu = menu;
        if (this.enableContactUs && !(this instanceof HSConversation)) {
            showConversationMenu(menu);
        }
        return true;
    }

    private void startConversation() {
        Intent i = new Intent(this, HSConversation.class);
        i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(this));
        i.putExtra("chatLaunchSource", HSConsts.SRC_SUPPORT);
        if (this.extras != null) {
            i.putExtras(this.extras);
        }
        i.removeExtra("isRoot");
        if (this instanceof HSQuestion) {
            i.putExtra(HSConsts.SEARCH_PERFORMED, true);
        } else {
            i.putExtra(HSConsts.SEARCH_PERFORMED, getIntent().getBooleanExtra(HSConsts.SEARCH_PERFORMED, false));
        }
        startActivityForResult(i, CALL_FINISH_REQ_CODE);
    }

    public void onStart() {
        super.onStart();
        HSAnalytics.onActivityStarted(this);
    }

    public void onStop() {
        super.onStop();
        HSAnalytics.onActivityStopped(this);
    }

    protected void showConversationMenu(boolean show) {
        if (this.reportIssueAction != null) {
            this.reportIssueAction.setVisible(show);
        } else if (show && this.conversationMenu != null) {
            showConversationMenu(this.conversationMenu);
        }
    }

    private void showConversationMenu(Menu menu) {
        getMenuInflater().inflate(menu.hs__show_conversation, menu);
        this.reportIssueAction = menu.findItem(id.hs__action_report_issue);
        LinearLayout badgeLayout = (LinearLayout) SimpleMenuItemCompat.getActionView(this.reportIssueAction);
        if (badgeLayout != null) {
            this.notifCount = (TextView) badgeLayout.findViewById(id.hs__notification_badge);
            this.convIcon = (TextView) badgeLayout.findViewById(id.hs__conversation_icon);
            Styles.setActionButtonIconColor(this, this.convIcon.getBackground());
            Styles.setActionButtonNotificationIconColor(this, this.notifCount.getBackground());
            badgeLayout.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HSActivity.this.startConversation();
                }
            });
            updateCount(this.data.getProfileId());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Boolean callFinish = Boolean.valueOf(data.getBooleanExtra("callFinish", false));
            if (requestCode != CALL_FINISH_REQ_CODE || resultCode != -1) {
                return;
            }
            if (this instanceof HSConversation) {
                onBackPressed();
            } else if (callFinish.booleanValue()) {
                callFinish();
            }
        }
    }

    private void callFinish() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("callFinish", true);
        setResult(-1, returnIntent);
        finish();
    }
}
