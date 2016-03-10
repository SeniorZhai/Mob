package com.helpshift;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.helpshift.D.attr;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.string;
import com.helpshift.HSSearch.HS_SEARCH_OPTIONS;
import com.helpshift.app.ActionBarHelper;
import com.helpshift.customadapters.SearchAdapter;
import com.helpshift.res.values.HSConsts;
import com.helpshift.util.HSActivityUtil;
import com.helpshift.util.HSErrors;
import com.helpshift.util.Styles;
import com.helpshift.util.Xml;
import com.helpshift.view.SimpleMenuItemCompat.MenuItemActions;
import com.helpshift.view.SimpleMenuItemCompat.QueryTextActions;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Locale;
import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

public final class HSSectionFragment extends ListFragment implements QueryTextActions, MenuItemActions, HSFaqSyncStatusEvents {
    public static final String TAG = "HelpShiftDebug";
    private ActionBarHelper actionBarHelper;
    private HSActivity activity;
    private ArrayAdapter adapter;
    private String currentLang;
    private HSApiData data;
    private boolean eventSent = false;
    private ArrayList<Faq> faqItems = new ArrayList();
    private Boolean isDecomp = Boolean.valueOf(false);
    private boolean isVisible;
    private View listFooter;
    private ListView listView;
    private MenuItem mSearchItem;
    private String prevSearchQuery;
    private String publishId;
    private ArrayAdapter searchAdapter;
    private String searchCache = BuildConfig.FLAVOR;
    private ArrayList<Faq> searchItems = new ArrayList();
    private String searchQuery;
    private boolean searchStarted = false;
    private Handler sectionFailHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HSErrors.showFailToast(((Integer) msg.obj.get(SettingsJsonConstants.APP_STATUS_KEY)).intValue(), null, HSSectionFragment.this.activity);
        }
    };
    private String sectionId;
    private String sectionPubId;
    private Handler sectionSuccessHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HSSectionFragment.this.updateSectionData(msg.obj);
        }
    };
    private boolean showReportIssue;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getArguments();
        if (extras != null) {
            this.publishId = extras.getString("sectionPublishId");
            if (this.publishId == null) {
                this.publishId = BuildConfig.FLAVOR;
            }
            this.showReportIssue = ContactUsFilter.showContactUs(LOCATION.SEARCH_FOOTER);
            if (extras.getBoolean("decomp")) {
                HSAnalytics.decomp = true;
                this.isDecomp = Boolean.valueOf(true);
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.activity = (HSActivity) getActivity();
        this.actionBarHelper = this.activity.getActionBarHelper();
        this.data = new HSApiData(this.activity);
        if (this.showReportIssue) {
            this.listFooter = inflater.inflate(layout.hs__search_list_footer, null, false);
        } else {
            this.listFooter = inflater.inflate(layout.hs__no_faqs, null, false);
        }
        this.adapter = new ArrayAdapter(this.activity, layout.hs__simple_list_item_1, this.faqItems);
        this.searchAdapter = new SearchAdapter(this.activity, layout.hs__simple_list_item_1, this.searchItems);
        setListAdapter(this.adapter);
        this.currentLang = Locale.getDefault().getLanguage();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.listView = getListView();
        if (this.isDecomp.booleanValue()) {
            this.listView.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View view, MotionEvent ev) {
                    if (HSSectionFragment.this.mSearchItem != null) {
                        HSSectionFragment.this.actionBarHelper.clearFocus(HSSectionFragment.this.mSearchItem);
                    }
                    return false;
                }
            });
            if (this.showReportIssue) {
                ((Button) this.listFooter.findViewById(id.report_issue)).setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        HSSectionFragment.this.performedSearch();
                        Intent i = new Intent(HSSectionFragment.this.activity, HSConversation.class);
                        i.putExtra(SettingsJsonConstants.PROMPT_MESSAGE_KEY, HSSectionFragment.this.searchQuery);
                        HSSectionFragment.this.actionBarHelper.collapseActionView(HSSectionFragment.this.mSearchItem);
                        i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(HSSectionFragment.this.activity));
                        i.putExtra("showConvOnReportIssue", HSSectionFragment.this.activity.getIntent().getExtras().getBoolean("showConvOnReportIssue"));
                        i.putExtra("chatLaunchSource", HSConsts.SRC_SUPPORT);
                        i.putExtra(HSConsts.SEARCH_PERFORMED, HSSectionFragment.this.getActivity().getIntent().getBooleanExtra(HSConsts.SEARCH_PERFORMED, false));
                        HSSectionFragment.this.getActivity().startActivityForResult(i, 1);
                    }
                });
            }
            setHasOptionsMenu(true);
        }
        HSApiData.addFaqSyncStatusObserver(this);
        if (this.isDecomp.booleanValue()) {
            this.data.getSection(this.publishId, this.sectionSuccessHandler, this.sectionFailHandler);
        } else {
            this.data.getSectionSync(this.publishId, this.sectionSuccessHandler, this.sectionFailHandler);
        }
        this.listView.setDivider(new ColorDrawable(Styles.getColor(this.activity, attr.hs__contentSeparatorColor)));
        this.listView.setDividerHeight(1);
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        this.isVisible = isVisibleToUser;
        if (isVisibleToUser && this.sectionId != null) {
            try {
                JSONObject eventData = new JSONObject();
                eventData.put(DBLikedChannelsHelper.KEY_ID, this.sectionId);
                HSFunnel.pushEvent(HSFunnel.BROWSED_FAQ_LIST, eventData);
                this.eventSent = true;
            } catch (JSONException e) {
                Log.d(TAG, e.toString(), e);
            }
        }
    }

    public void onResume() {
        boolean z = true;
        super.onResume();
        if ((this.isDecomp.booleanValue() || this.isVisible) && this.sectionId != null) {
            try {
                JSONObject eventData = new JSONObject();
                eventData.put(DBLikedChannelsHelper.KEY_ID, this.sectionId);
                HSFunnel.pushEvent(HSFunnel.BROWSED_FAQ_LIST, eventData);
                this.eventSent = true;
            } catch (JSONException e) {
                Log.d(TAG, "event data", e);
            }
        }
        if (ContactUsFilter.showContactUs(LOCATION.ACTION_BAR)) {
            HSActivity hSActivity = this.activity;
            if (this.searchStarted) {
                z = false;
            }
            hSActivity.showConversationMenu(z);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        HSApiData.removeFaqSyncStatusObserver(this);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.isDecomp.booleanValue()) {
            this.mSearchItem = menu.findItem(id.hs__action_search);
            Styles.setActionButtonIconColor(this.activity, this.mSearchItem.getIcon());
            this.actionBarHelper.setQueryHint(this.mSearchItem, getResources().getString(string.hs__search_hint));
            this.actionBarHelper.setOnQueryTextListener(this.mSearchItem, this);
            this.actionBarHelper.setOnActionExpandListener(this.mSearchItem, this);
            this.data.loadIndex();
        }
    }

    public void faqsUpdated() {
        updateSectionData(this.data.getSection(this.publishId));
    }

    public void searchIndexesUpdated() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                HSSectionFragment.this.initSearchList(HSSectionFragment.this.searchCache);
            }
        });
    }

    private void updateSectionData(Section section) {
        ArrayList<Faq> sectionFAQArray = this.data.getFaqsForSection(this.publishId);
        if (this.isDecomp.booleanValue() && section != null) {
            this.activity.getActionBarHelper().setTitle(section.getTitle());
        }
        if (section == null) {
            HSErrors.showFailToast(HttpStatus.SC_NOT_FOUND, null, this.activity);
            return;
        }
        this.faqItems.clear();
        this.sectionId = section.getSectionId();
        this.sectionPubId = section.getPublishId();
        if (!(!getUserVisibleHint() || this.sectionId == null || this.eventSent)) {
            try {
                JSONObject eventData = new JSONObject();
                eventData.put(DBLikedChannelsHelper.KEY_ID, this.sectionId);
                HSFunnel.pushEvent(HSFunnel.BROWSED_FAQ_LIST, eventData);
                this.eventSent = true;
            } catch (JSONException e) {
                Log.d(TAG, e.toString(), e);
            }
        }
        for (int i = 0; i < sectionFAQArray.size(); i++) {
            this.faqItems.add((Faq) sectionFAQArray.get(i));
        }
        if (this.faqItems.size() == 0) {
            this.faqItems.add(new Faq(getResources().getString(string.hs__faqs_search_footer), HSConsts.STATUS_NEW, "empty_status"));
        }
        this.adapter.notifyDataSetChanged();
    }

    public void onListItemClick(ListView list, View view, int questionPosition, long arg3) {
        Faq clickedItem;
        if (this.searchStarted) {
            performedSearch();
            clickedItem = (Faq) this.searchItems.get(questionPosition);
        } else {
            clickedItem = (Faq) this.faqItems.get(questionPosition);
        }
        if (!clickedItem.getType().equals("empty_status")) {
            Intent i = new Intent(this.activity, HSQuestion.class);
            i.putExtra("questionPublishId", clickedItem.getPublishId());
            i.putExtra("decomp", this.isDecomp);
            i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(this.activity));
            i.putExtra("searchTerms", clickedItem.getSearchTerms());
            i.putExtras(getActivity().getIntent().getExtras());
            i.removeExtra("isRoot");
            getActivity().startActivityForResult(i, 1);
        }
    }

    protected void initSearchList(String searchQuery) {
        boolean searchWithAny = false;
        this.searchQuery = searchQuery.trim();
        if (this.currentLang.equals("zh") || this.currentLang.equals("ja") || this.currentLang.equals("ko")) {
            searchWithAny = true;
        }
        if (this.searchQuery.length() == 0 || (this.searchQuery.length() < 3 && !searchWithAny)) {
            initSearchList();
        } else {
            initSearchList(this.data.localFaqSearch(this.searchQuery, HS_SEARCH_OPTIONS.FULL_SEARCH));
        }
    }

    protected void initSearchList() {
        initSearchList(this.data.getFaqsForSection(this.sectionPubId));
    }

    private void initSearchList(ArrayList<Faq> searchArray) {
        if (searchArray.size() == 0 || this.showReportIssue) {
            this.listFooter.setVisibility(0);
        } else {
            this.listFooter.setVisibility(8);
        }
        this.searchItems.clear();
        for (int i = 0; i < searchArray.size(); i++) {
            Faq searchItem = (Faq) searchArray.get(i);
            if (searchItem.getSectionPublishId().equals(this.sectionPubId)) {
                this.searchItems.add(searchItem);
            }
        }
        this.searchAdapter.notifyDataSetChanged();
    }

    protected void searchCompleted() {
        if (this.listView.getFooterViewsCount() != 0) {
            this.listView.removeFooterView(this.listFooter);
        }
        setListAdapter(this.adapter);
        this.adapter.notifyDataSetChanged();
        this.searchStarted = false;
    }

    protected void searchStarted() {
        if (this.listView.getFooterViewsCount() == 0 && this.showReportIssue) {
            this.listView.addFooterView(this.listFooter);
        } else {
            this.listView.addFooterView(this.listFooter, null, false);
            this.listView.setFooterDividersEnabled(false);
        }
        initSearchList();
        setListAdapter(this.searchAdapter);
        this.searchAdapter.notifyDataSetChanged();
        this.searchStarted = true;
    }

    private void performedSearch() {
        getActivity().getIntent().putExtra(HSConsts.SEARCH_PERFORMED, true);
        performedSearch(this.actionBarHelper.getQuery(this.mSearchItem).toString().trim());
    }

    private void performedSearch(String searchString) {
        if (!TextUtils.isEmpty(searchString.trim()) && !searchString.equals(this.prevSearchQuery)) {
            JSONObject eventObj = new JSONObject();
            try {
                eventObj.put(HSFunnel.PERFORMED_SEARCH, searchString);
            } catch (JSONException e) {
                Log.d(TAG, "JSONException", e);
            }
            HSFunnel.pushEvent(HSFunnel.PERFORMED_SEARCH, eventObj);
            this.prevSearchQuery = searchString;
        }
    }

    public boolean queryTextSubmitted(String query) {
        return false;
    }

    public boolean queryTextChanged(String newText) {
        if (newText.length() == 0) {
            performedSearch(this.searchCache);
        } else {
            this.searchCache = newText;
        }
        initSearchList(newText);
        return false;
    }

    public boolean menuItemExpanded() {
        this.prevSearchQuery = BuildConfig.FLAVOR;
        this.searchCache = BuildConfig.FLAVOR;
        searchStarted();
        this.activity.showConversationMenu(false);
        this.actionBarHelper.setIcon(Xml.getLogoResourceValue(getActivity()));
        return true;
    }

    public boolean menuItemCollapsed() {
        performedSearch();
        searchCompleted();
        if (ContactUsFilter.showContactUs(LOCATION.ACTION_BAR)) {
            this.activity.showConversationMenu(true);
        }
        return true;
    }
}
