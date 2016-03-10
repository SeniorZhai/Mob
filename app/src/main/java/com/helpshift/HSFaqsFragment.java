package com.helpshift;

import android.content.Intent;
import android.database.SQLException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
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
import com.helpshift.util.Meta;
import com.helpshift.util.Styles;
import com.helpshift.util.Xml;
import com.helpshift.view.HSViewPager;
import com.helpshift.view.SimpleMenuItemCompat.MenuItemActions;
import com.helpshift.view.SimpleMenuItemCompat.QueryTextActions;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

public final class HSFaqsFragment extends ListFragment implements QueryTextActions, MenuItemActions {
    private final String TAG = Meta.TAG;
    private ActionBarHelper actionBarHelper;
    private HSActivity activity;
    private ArrayAdapter adapter;
    private String currentLang;
    private HSApiData data;
    private boolean decomp = false;
    private Bundle extras;
    private Handler failureHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HSErrors.showFailToast(((Integer) msg.obj.get(SettingsJsonConstants.APP_STATUS_KEY)).intValue(), null, HSFaqsFragment.this.activity);
        }
    };
    private List<Faq> faqItems = new ArrayList();
    private View listFooter;
    private ListView listView;
    private MenuItem mSearchItem;
    private HSViewPager mViewPager;
    private String prevSearchQuery = BuildConfig.FLAVOR;
    private ArrayAdapter searchAdapter;
    private String searchCache = BuildConfig.FLAVOR;
    private List<Faq> searchItems = new ArrayList();
    private String searchQuery;
    private boolean searchStarted = false;
    private Handler sectionsDbHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            ArrayList arrayList = new ArrayList();
            ArrayList<Section> sections = HSFaqsFragment.this.data.getPopulatedSections(msg.obj);
            HSFaqsFragment.this.faqItems.clear();
            int i;
            if (sections.size() == 1) {
                ArrayList<Faq> faqs = HSFaqsFragment.this.data.getFaqsForSection(((Section) sections.get(0)).getPublishId());
                for (i = 0; i < faqs.size(); i++) {
                    Faq faqItem = (Faq) faqs.get(i);
                    HSFaqsFragment.this.faqItems.add(new Faq(faqItem.getTitle(), faqItem.getPublishId(), "question"));
                }
            } else {
                for (i = 0; i < sections.size(); i++) {
                    Section sectionItem = (Section) sections.get(i);
                    if (!HSFaqsFragment.this.data.isSectionEmpty(sectionItem)) {
                        HSFaqsFragment.this.faqItems.add(new Faq(sectionItem.getTitle(), sectionItem.getPublishId(), "section"));
                    }
                }
            }
            if (HSFaqsFragment.this.faqItems.size() == 0) {
                HSFaqsFragment.this.faqItems.add(new Faq(HSFaqsFragment.this.getResources().getString(string.hs__faqs_search_footer), HSConsts.STATUS_NEW, "empty_status"));
            }
            if (HSFaqsFragment.this.isResumed()) {
                HSFaqsFragment.this.setListShown(true);
            }
            HSFaqsFragment.this.adapter.notifyDataSetChanged();
        }
    };
    private boolean showReportIssue = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.activity = (HSActivity) getActivity();
        this.extras = this.activity.getIntent().getExtras();
        if (this.extras != null) {
            this.decomp = this.extras.getBoolean("decomp");
            this.showReportIssue = ContactUsFilter.showContactUs(LOCATION.SEARCH_FOOTER);
        }
        this.actionBarHelper = this.activity.getActionBarHelper();
        this.data = new HSApiData(this.activity);
        if (this.showReportIssue) {
            this.listFooter = inflater.inflate(layout.hs__search_list_footer, null, false);
        } else {
            this.listFooter = inflater.inflate(layout.hs__no_faqs, null, false);
        }
        int rowResId = layout.hs__simple_list_item_1;
        this.adapter = new ArrayAdapter(this.activity, rowResId, this.faqItems);
        this.searchAdapter = new SearchAdapter(this.activity, rowResId, this.searchItems);
        setListAdapter(this.adapter);
        setHasOptionsMenu(true);
        this.currentLang = Locale.getDefault().getLanguage();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mSearchItem = menu.findItem(id.hs__action_search);
        Styles.setActionButtonIconColor(this.activity, this.mSearchItem.getIcon());
        this.actionBarHelper.setQueryHint(this.mSearchItem, getResources().getString(string.hs__search_hint));
        this.actionBarHelper.setOnQueryTextListener(this.mSearchItem, this);
        this.actionBarHelper.setOnActionExpandListener(this.mSearchItem, this);
        this.data.loadIndex();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.listView = getListView();
        this.listView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent ev) {
                if (HSFaqsFragment.this.mSearchItem != null) {
                    HSFaqsFragment.this.actionBarHelper.clearFocus(HSFaqsFragment.this.mSearchItem);
                }
                return false;
            }
        });
        if (this.showReportIssue) {
            Button reportIssueBtn = (Button) this.listFooter.findViewById(id.report_issue);
            Styles.setButtonCompoundDrawableIconColor(this.activity, reportIssueBtn.getCompoundDrawables()[0]);
            reportIssueBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    HSFaqsFragment.this.performedSearch();
                    Intent i = new Intent(HSFaqsFragment.this.activity, HSConversation.class);
                    i.putExtra(SettingsJsonConstants.PROMPT_MESSAGE_KEY, HSFaqsFragment.this.searchQuery);
                    HSFaqsFragment.this.actionBarHelper.collapseActionView(HSFaqsFragment.this.mSearchItem);
                    i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(HSFaqsFragment.this.activity));
                    i.putExtra("showConvOnReportIssue", HSFaqsFragment.this.extras.getBoolean("showConvOnReportIssue"));
                    i.putExtra("chatLaunchSource", HSConsts.SRC_SUPPORT);
                    i.putExtra("decomp", HSFaqsFragment.this.decomp);
                    i.putExtra(HSConsts.SEARCH_PERFORMED, HSFaqsFragment.this.getActivity().getIntent().getBooleanExtra(HSConsts.SEARCH_PERFORMED, false));
                    HSFaqsFragment.this.getActivity().startActivityForResult(i, 1);
                }
            });
        }
        try {
            setListShown(false);
            this.data.getSections(this.sectionsDbHandler, this.failureHandler);
        } catch (SQLException e) {
            Log.d(Meta.TAG, e.toString(), e);
        }
        this.listView.setDivider(new ColorDrawable(Styles.getColor(this.activity, attr.hs__contentSeparatorColor)));
        this.listView.setDividerHeight(1);
    }

    public void onResume() {
        super.onResume();
        if (ContactUsFilter.showContactUs(LOCATION.ACTION_BAR)) {
            this.activity.showConversationMenu(!this.searchStarted);
        }
    }

    public void onListItemClick(ListView l, View v, int position, long id) {
        Faq clickedItem;
        if (this.searchStarted) {
            performedSearch();
            clickedItem = (Faq) this.searchItems.get(position);
        } else {
            clickedItem = (Faq) this.faqItems.get(position);
        }
        if (!clickedItem.getType().equals("empty_status")) {
            Intent i;
            if (clickedItem.getType().equals("section")) {
                i = new Intent(this.activity, HSQuestionsList.class);
                i.putExtra("sectionPublishId", clickedItem.getPublishId());
            } else {
                i = new Intent(this.activity, HSQuestion.class);
                i.putExtra("questionPublishId", clickedItem.getPublishId());
                i.putExtra("decomp", this.decomp);
                i.putExtra("searchTerms", clickedItem.getSearchTerms());
            }
            i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(this.activity));
            i.putExtras(this.extras);
            i.removeExtra("isRoot");
            getActivity().startActivityForResult(i, 1);
        }
    }

    public void searchIndexesUpdated() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                HSFaqsFragment.this.initSearchList(HSFaqsFragment.this.searchCache);
            }
        });
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
        initSearchList(this.data.getAllFaqs());
    }

    private void initSearchList(ArrayList<Faq> searchArray) {
        if (searchArray.size() == 0 || this.showReportIssue) {
            this.listFooter.setVisibility(0);
        } else {
            this.listFooter.setVisibility(8);
        }
        this.searchItems.clear();
        for (int i = 0; i < searchArray.size(); i++) {
            this.searchItems.add((Faq) searchArray.get(i));
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
                Log.d(Meta.TAG, "JSONException", e);
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
        if (this.mViewPager != null) {
            this.mViewPager.setPagingEnabled(false);
        }
        if (!this.decomp) {
            this.actionBarHelper.setNavigationMode(0);
        }
        searchStarted();
        this.activity.showConversationMenu(false);
        this.actionBarHelper.setIcon(Xml.getLogoResourceValue(getActivity()));
        return true;
    }

    public boolean menuItemCollapsed() {
        performedSearch();
        if (this.mViewPager != null) {
            this.mViewPager.setPagingEnabled(true);
        }
        searchCompleted();
        if (ContactUsFilter.showContactUs(LOCATION.ACTION_BAR)) {
            this.activity.showConversationMenu(true);
        }
        return true;
    }
}
