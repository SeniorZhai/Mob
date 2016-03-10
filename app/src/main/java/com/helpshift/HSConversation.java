package com.helpshift;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.string;
import com.helpshift.app.ActionBarHelper;
import com.helpshift.res.drawable.HSDraw;
import com.helpshift.res.drawable.HSImages;
import com.helpshift.res.values.HSConsts;
import com.helpshift.util.HSActivityUtil;
import java.util.Locale;

public final class HSConversation extends HSActivity {
    public static final String TAG = "HelpShiftDebug";
    public static boolean keepActivityActive = false;
    private Bundle bundle;
    private HSApiData data;
    private FragmentTransaction ft;
    private Locale locale;
    private HSStorage storage;

    public /* bridge */ /* synthetic */ boolean onCreateOptionsMenu(Menu x0) {
        return super.onCreateOptionsMenu(x0);
    }

    public /* bridge */ /* synthetic */ void onStart() {
        super.onStart();
    }

    public /* bridge */ /* synthetic */ void onStop() {
        super.onStop();
    }

    public static void setKeepActivityActive(boolean selectingAttachment) {
        keepActivityActive = selectingAttachment;
    }

    public static boolean isActivityActive() {
        return keepActivityActive;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.locale = getResources().getConfiguration().locale;
        this.data = new HSApiData(this);
        this.storage = this.data.storage;
        Bundle extras = getIntent().getExtras();
        ActionBarHelper actionBarHelper = getActionBarHelper();
        actionBarHelper.supportRequestWindowFeature(5);
        actionBarHelper.setDisplayHomeAsUpEnabled(true);
        actionBarHelper.setTitle(getString(string.hs__conversation_header));
        setContentView(layout.hs__conversation);
        if (!this.storage.isHelpshiftBrandingDisabled()) {
            LinearLayout addIssueFooter = (LinearLayout) findViewById(id.hs__newConversationFooter);
            ImageView iv = new ImageView(this);
            iv.setImageDrawable(HSDraw.getBitmapDrawable(this, (String) HSImages.imagesMap.get("newHSLogo")));
            iv.setBackgroundResource(17170444);
            addIssueFooter.addView(iv);
        }
        this.bundle = new Bundle(extras);
        String chatLaunchSource = extras.getString("chatLaunchSource");
        HSAnalytics.decomp = extras.getBoolean("decomp", false);
        this.ft = getSupportFragmentManager().beginTransaction();
        if (savedInstanceState != null) {
            return;
        }
        if (extras.getBoolean("newConversation")) {
            showNewConversationFragment();
        } else if (HSConsts.SRC_PUSH.equals(chatLaunchSource) || HSConsts.SRC_INAPP.equals(chatLaunchSource)) {
            showMessagesFragment();
        } else {
            showFragment();
        }
    }

    private void showFragment() {
        String activeConversation = this.storage.getActiveConversation(this.data.getProfileId());
        String archivedConversation = this.storage.getArchivedConversation(this.data.getProfileId());
        if (!TextUtils.isEmpty(archivedConversation)) {
            this.bundle.putString("issueId", archivedConversation);
            showMessagesFragment();
        } else if (TextUtils.isEmpty(activeConversation)) {
            showNewConversationFragment();
        } else {
            this.bundle.putString("issueId", activeConversation);
            showMessagesFragment();
        }
    }

    private void showNewConversationFragment() {
        this.ft.add(id.hs__fragment_holder, Fragment.instantiate(this, HSAddIssueFragment.class.getName(), this.bundle));
        this.ft.commit();
        super.startPoller();
    }

    private void showMessagesFragment() {
        this.ft.add(id.hs__fragment_holder, Fragment.instantiate(this, HSMessagesFragment.class.getName(), this.bundle));
        this.ft.commit();
    }

    protected void onResume() {
        Bundle extras = getIntent().getExtras();
        String activeConversation = this.storage.getActiveConversation(this.data.getProfileId());
        String archivedConversation = this.storage.getArchivedConversation(this.data.getProfileId());
        if (extras.getBoolean("newConversation") || (TextUtils.isEmpty(activeConversation) && TextUtils.isEmpty(archivedConversation))) {
            HSActivityUtil.restoreFullscreen(this);
        } else {
            HSActivityUtil.forceNotFullscreen(this);
        }
        this.storage.setIsConversationShowing(Boolean.valueOf(true));
        super.onResume();
    }

    protected void onPause() {
        Bundle extras = getIntent().getExtras();
        String activeConversation = this.storage.getActiveConversation(this.data.getProfileId());
        String archivedConversation = this.storage.getArchivedConversation(this.data.getProfileId());
        if ((extras.getBoolean("newConversation") || (TextUtils.isEmpty(activeConversation) && TextUtils.isEmpty(archivedConversation))) && !isActivityActive()) {
            setResult(-1, new Intent());
            finish();
        }
        HSActivityUtil.restoreFullscreen(this);
        if (extras != null && Boolean.valueOf(extras.getBoolean("isRoot")).booleanValue() && isFinishing()) {
            HSActivityUtil.sessionEnding();
        }
        super.onPause();
    }

    public void onBackPressed() {
        setResult(-1, new Intent());
        super.onBackPressed();
    }

    public void onDestroy() {
        this.storage.setIsConversationShowing(Boolean.valueOf(false));
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!this.locale.equals(newConfig.locale)) {
            restartActivity();
        }
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}
