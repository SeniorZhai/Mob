package com.helpshift;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.menu;
import com.helpshift.app.ActionBarHelper;
import com.helpshift.res.drawable.HSDraw;
import com.helpshift.res.drawable.HSImages;
import com.helpshift.util.HSActivityUtil;

public final class HSSection extends HSActivity {
    public static final String TAG = "HelpShiftDebug";
    private ActionBarHelper actionBarHelper;
    private HSApiData data;
    private MenuItem mSearchItem;
    private View mSearchView;
    private HSSectionFragment mainListFragment;
    private String sectionPublishId;
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

    private void setTextViewAlpha(TextView tv, float alpha) {
        int color = tv.getCurrentTextColor();
        tv.setTextColor(Color.argb((int) Math.floor((double) (255.0f * alpha)), Color.red(color), Color.green(color), Color.blue(color)));
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
        HSAnalytics.decomp = true;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.sectionPublishId = (String) extras.get("sectionPublishId");
            this.data = new HSApiData(this);
            this.storage = this.data.storage;
            if (Boolean.valueOf(extras.getBoolean("showInFullScreen")).booleanValue()) {
                getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
            }
            setContentView(layout.hs__section);
            this.actionBarHelper = getActionBarHelper();
            this.actionBarHelper.setDisplayHomeAsUpEnabled(true);
            if (!this.storage.isHelpshiftBrandingDisabled()) {
                ImageView iv = (ImageView) findViewById(id.hs__sectionFooter);
                iv.setImageDrawable(HSDraw.getBitmapDrawable(this, (String) HSImages.imagesMap.get("newHSLogo")));
                iv.setBackgroundResource(17170444);
            }
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            this.mainListFragment = new HSSectionFragment();
            Bundle fragmentData = new Bundle();
            fragmentData.putString("sectionPublishId", this.sectionPublishId);
            fragmentData.putBoolean("decomp", true);
            fragmentData.putAll(extras);
            this.mainListFragment.setArguments(fragmentData);
            ft.add(id.hs__sectionContainer, this.mainListFragment);
            ft.commit();
            return;
        }
        finish();
    }

    public void searchIndexesUpdated() {
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
