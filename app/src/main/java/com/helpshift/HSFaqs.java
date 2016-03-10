package com.helpshift;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.menu;
import com.helpshift.D.string;
import com.helpshift.res.drawable.HSDraw;
import com.helpshift.res.drawable.HSImages;
import com.helpshift.util.HSActivityUtil;

public final class HSFaqs extends HSActivity {
    public static final String TAG = "HelpShiftDebug";
    private int callFinishRequestCode = 1;
    private HSApiData data;
    private ImageView hsFooter;
    private Boolean showConvOnReportIssue;
    private HSStorage storage;

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
        HSFunnel.pushEvent(HSFunnel.SUPPORT_LAUNCH);
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
        getActionBarHelper().setTitle(getString(string.hs__help_header));
        HSAnalytics.decomp = false;
        this.showConvOnReportIssue = Boolean.valueOf(getIntent().getExtras().getBoolean("showConvOnReportIssue"));
        if (Boolean.valueOf(getIntent().getExtras().getBoolean("showInFullScreen")).booleanValue()) {
            getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
        }
        this.data = new HSApiData(this);
        this.storage = this.data.storage;
        setContentView(layout.hs__faqs);
        getActionBarHelper().setDisplayHomeAsUpEnabled(true);
        this.hsFooter = (ImageView) findViewById(id.hs__helpshiftActivityFooter);
        if (!this.storage.isHelpshiftBrandingDisabled()) {
            this.hsFooter.setImageDrawable(HSDraw.getBitmapDrawable(this, (String) HSImages.imagesMap.get("newHSLogo")));
            this.hsFooter.setBackgroundResource(17170444);
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(menu.hs__faqs_fragment, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }
}
