package com.helpshift;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.facebook.internal.NativeProtocol;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.string;
import com.helpshift.HSSearch.HS_SEARCH_OPTIONS;
import com.helpshift.app.ActionBarHelper;
import com.helpshift.customadapters.SearchAdapter;
import com.helpshift.res.drawable.HSDraw;
import com.helpshift.res.drawable.HSImages;
import com.helpshift.res.values.HSConsts;
import com.helpshift.util.HSActivityUtil;
import com.helpshift.util.HSColor;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;

public final class SearchResultActivity extends HSActivity {
    private HSApiData data;

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

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (Boolean.valueOf(extras.getBoolean("showInFullScreen")).booleanValue()) {
                getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
            }
            this.data = new HSApiData(this);
            setContentView(layout.hs__search_result_activity);
            if (!this.data.storage.isHelpshiftBrandingDisabled()) {
                ImageView hsFooter = (ImageView) findViewById(id.hs__helpshiftActivityFooter);
                hsFooter.setImageDrawable(HSDraw.getBitmapDrawable(this, (String) HSImages.imagesMap.get("newHSLogo")));
                hsFooter.setBackgroundResource(17170444);
            }
            TextView headerView = (TextView) getLayoutInflater().inflate(layout.hs__search_result_header, null, false);
            HSColor.setTextViewAlpha(headerView, 0.5f);
            getListView().addHeaderView(headerView, BuildConfig.FLAVOR, false);
            getListView().addFooterView(getLayoutInflater().inflate(layout.hs__search_result_footer, null, false));
            ActionBarHelper actionBarHelper = getActionBarHelper();
            actionBarHelper.setDisplayHomeAsUpEnabled(true);
            actionBarHelper.setTitle(getResources().getString(string.hs__search_result_title));
            final ArrayList<Faq> searchItems = new HSApiData(this).localFaqSearch(extras.getString(HSConsts.SEARCH_QUERY), HS_SEARCH_OPTIONS.KEYWORD_SEARCH);
            int end = 3;
            if (searchItems.size() < 3) {
                end = searchItems.size();
            }
            setListAdapter(new SearchAdapter(this, layout.hs__simple_list_item_3, searchItems.subList(0, end)));
            getListView().setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    Faq clickedItem = (Faq) searchItems.get(position - 1);
                    Intent questionIntent = new Intent(SearchResultActivity.this, HSQuestion.class);
                    questionIntent.putExtra("questionPublishId", clickedItem.getPublishId());
                    questionIntent.putExtra("searchTerms", clickedItem.getSearchTerms());
                    questionIntent.putExtra(HSConsts.QUESTION_FLOW, HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION_FLOW);
                    questionIntent.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(SearchResultActivity.this));
                    SearchResultActivity.this.startActivityForResult(questionIntent, HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION_REQUEST_CODE);
                }
            });
            ((Button) findViewById(id.send_anyway_button)).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    HSFunnel.pushEvent(HSFunnel.TICKET_AVOIDANCE_FAILED);
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(NativeProtocol.WEB_DIALOG_ACTION, "startConversation");
                    SearchResultActivity.this.setResult(-1, returnIntent);
                    SearchResultActivity.this.finish();
                }
            });
            return;
        }
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    private void setListAdapter(SearchAdapter adapter) {
        getListView().setAdapter(adapter);
    }

    private ListView getListView() {
        return (ListView) findViewById(16908298);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION_REQUEST_CODE) {
            Intent returnIntent = new Intent();
            returnIntent.putExtras(data);
            setResult(-1, returnIntent);
            finish();
        }
    }

    protected void onResume() {
        super.onResume();
        HelpshiftContext.setViewState(HSConsts.ISSUE_FILING);
    }

    protected void onDestroy() {
        super.onDestroy();
        HelpshiftContext.setViewState(null);
    }
}
