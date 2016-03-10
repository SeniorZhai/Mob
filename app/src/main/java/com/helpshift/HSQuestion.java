package com.helpshift;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.ImageView;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.menu;
import com.helpshift.D.string;
import com.helpshift.app.ActionBarHelper;
import com.helpshift.res.drawable.HSDraw;
import com.helpshift.res.drawable.HSImages;
import com.helpshift.res.values.HSConsts;
import com.helpshift.util.HSActivityUtil;
import com.helpshift.util.Styles;

public final class HSQuestion extends HSActivity {
    public static final String TAG = "HelpShiftDebug";
    ActionBarHelper actionBarHelper;
    private HSApiData data;
    Bundle extras;
    private ImageView hsFooter;
    private HSQuestionFragment questionFragment = null;

    public /* bridge */ /* synthetic */ void onConfigurationChanged(Configuration x0) {
        super.onConfigurationChanged(x0);
    }

    public /* bridge */ /* synthetic */ void onStart() {
        super.onStart();
    }

    public /* bridge */ /* synthetic */ void onStop() {
        super.onStop();
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        Bundle extras = getIntent().getExtras();
        if (extras != null && Boolean.valueOf(extras.getBoolean("isRoot")).booleanValue() && isFinishing()) {
            HSActivityUtil.sessionEnding();
        }
        super.onPause();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(5);
        getActionBarHelper().setTitle(getString(string.hs__question_header));
        this.extras = getIntent().getExtras();
        if (this.extras != null) {
            if (Boolean.valueOf(this.extras.getBoolean("showInFullScreen")).booleanValue()) {
                getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
            }
            this.data = new HSApiData(this);
            setContentView(layout.hs__question);
            this.actionBarHelper = getActionBarHelper();
            this.actionBarHelper.setDisplayHomeAsUpEnabled(true);
            if (!this.data.storage.isHelpshiftBrandingDisabled()) {
                this.hsFooter = (ImageView) findViewById(id.hs__helpshiftActivityFooter);
                this.hsFooter.setImageDrawable(HSDraw.getBitmapDrawable(this, (String) HSImages.imagesMap.get("newHSLogo")));
                this.hsFooter.setBackgroundResource(17170444);
            }
            setSupportProgressBarIndeterminateVisibility(true);
            return;
        }
        finish();
    }

    protected boolean isShowSearchOnNewConversationFlowActive() {
        if (this.extras != null) {
            String questionFlow = this.extras.getString(HSConsts.QUESTION_FLOW);
            if (!TextUtils.isEmpty(questionFlow) && questionFlow.equals(HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION_FLOW)) {
                return true;
            }
        }
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (isShowSearchOnNewConversationFlowActive()) {
            getMenuInflater().inflate(menu.hs__search_on_conversation, menu);
            Styles.setActionButtonIconColor(this, menu.findItem(id.hs__action_done).getIcon());
        }
        if (this.actionBarHelper == null) {
            this.actionBarHelper = getActionBarHelper();
        }
        this.actionBarHelper.setupIndeterminateProgressBar(menu, getMenuInflater());
        return super.onCreateOptionsMenu(menu);
    }

    public void onAttachFragment(Fragment f) {
        super.onAttachFragment(f);
        if (f instanceof HSQuestionFragment) {
            this.questionFragment = (HSQuestionFragment) f;
        }
    }
}
