package com.helpshift;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.SimpleOnPageChangeListener;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils.TruncateAt;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.helpshift.D.attr;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.string;
import com.helpshift.app.ActionBarHelper;
import com.helpshift.constants.MessageColumns;
import com.helpshift.res.drawable.HSDraw;
import com.helpshift.res.drawable.HSImages;
import com.helpshift.util.Styles;
import java.util.ArrayList;
import java.util.HashMap;

public final class HSQuestionsList extends HSActivity {
    public static final String TAG = "HelpShiftDebug";
    private HSApiData data;
    private HSSectionPagerAdapter hsAdapter;
    private ImageView hsFooter;
    private ViewPager viewPager;

    public /* bridge */ /* synthetic */ void onConfigurationChanged(Configuration x0) {
        super.onConfigurationChanged(x0);
    }

    public /* bridge */ /* synthetic */ boolean onCreateOptionsMenu(Menu x0) {
        return super.onCreateOptionsMenu(x0);
    }

    public /* bridge */ /* synthetic */ void onStart() {
        super.onStart();
    }

    public /* bridge */ /* synthetic */ void onStop() {
        super.onStop();
    }

    private void appendHashMap(ArrayList list, Object type, Object obj) {
        HashMap<String, Object> map = new HashMap();
        map.put(MessageColumns.TYPE, type);
        map.put("obj", obj);
        list.add(map);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Boolean.valueOf(getIntent().getExtras().getBoolean("showInFullScreen")).booleanValue()) {
            getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
        }
        this.data = new HSApiData(this);
        setContentView(layout.hs__questions_list);
        this.hsAdapter = new HSSectionPagerAdapter(getSupportFragmentManager(), this, getIntent().getExtras().getString("sectionPublishId"));
        this.viewPager = (ViewPager) findViewById(id.hs__sections_pager);
        this.viewPager.setAdapter(this.hsAdapter);
        this.viewPager.setOnPageChangeListener(new SimpleOnPageChangeListener() {
            public void onPageSelected(int position) {
                HSQuestionsList.this.hsAdapter.onPageSelected(position);
            }
        });
        this.viewPager.setCurrentItem(this.hsAdapter.getCurrentPosition());
        PagerTabStrip tabStrip = (PagerTabStrip) findViewById(id.hs__pager_tab_strip);
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            View nextChild = tabStrip.getChildAt(i);
            if (nextChild instanceof TextView) {
                TextView textViewToConvert = (TextView) nextChild;
                textViewToConvert.setSingleLine();
                textViewToConvert.setEllipsize(TruncateAt.END);
            }
        }
        tabStrip.setTabIndicatorColor(Styles.getColor(this, attr.hs__faqsPagerTabStripIndicatorColor));
        ActionBarHelper actionBarHelper = getActionBarHelper();
        actionBarHelper.setDisplayHomeAsUpEnabled(true);
        actionBarHelper.setTitle(getResources().getString(string.hs__faq_header));
        if (!this.data.storage.isHelpshiftBrandingDisabled()) {
            this.hsFooter = (ImageView) findViewById(id.hs__helpshiftActivityFooter);
            this.hsFooter.setImageDrawable(HSDraw.getBitmapDrawable(this, (String) HSImages.imagesMap.get("newHSLogo")));
            this.hsFooter.setBackgroundResource(17170444);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    public void onResume() {
        super.onResume();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }
}
