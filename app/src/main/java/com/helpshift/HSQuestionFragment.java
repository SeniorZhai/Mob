package com.helpshift;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebSettings.TextSize;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.internal.NativeProtocol;
import com.helpshift.D.attr;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.string;
import com.helpshift.res.values.HSConsts;
import com.helpshift.util.HSActivityUtil;
import com.helpshift.util.HSErrors;
import com.helpshift.util.HSHTML5WebView;
import com.helpshift.util.HSTransliterator;
import com.helpshift.util.Styles;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.HttpHost;
import org.json.JSONException;
import org.json.JSONObject;

public final class HSQuestionFragment extends Fragment {
    public static final String TAG = "HelpShiftDebug";
    private HSActivity activity;
    private String bodyText;
    private Button contactUsBtn;
    OnClickListener contactUsClickListener = new OnClickListener() {
        public void onClick(View view) {
            Intent i = new Intent(HSQuestionFragment.this.activity, HSConversation.class);
            i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(HSQuestionFragment.this.activity));
            i.putExtra("chatLaunchSource", HSConsts.SRC_SUPPORT);
            i.putExtras(HSQuestionFragment.this.extras);
            i.removeExtra("isRoot");
            i.putExtra(HSConsts.SEARCH_PERFORMED, true);
            HSQuestionFragment.this.getActivity().startActivityForResult(i, 1);
        }
    };
    private LinearLayout contactUsContainer;
    private HSApiData data;
    private Button dislikeButton;
    private Boolean dislikeClicked = Boolean.valueOf(false);
    private Boolean enableContactUs;
    private JSONObject eventData;
    private Boolean eventSent = Boolean.valueOf(false);
    private Bundle extras;
    private String faqId = BuildConfig.FLAVOR;
    private TextView helpfulText;
    private int isHelpful = 0;
    private Boolean isHighlighted = Boolean.valueOf(false);
    private Boolean isRtl = Boolean.valueOf(false);
    private Button likeButton;
    private Boolean likeClicked = Boolean.valueOf(false);
    private HSHTML5WebView mWebView;
    public Handler markFailHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HSErrors.showFailToast(((Integer) msg.obj.get(SettingsJsonConstants.APP_STATUS_KEY)).intValue(), null, HSQuestionFragment.this.activity);
        }
    };
    private Handler questionFailHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HSErrors.showFailToast(((Integer) msg.obj.get(SettingsJsonConstants.APP_STATUS_KEY)).intValue(), null, HSQuestionFragment.this.activity);
        }
    };
    public Handler questionHandler = new Handler() {
        public void handleMessage(Message msg) {
            Faq question = msg.obj;
            if (HSQuestionFragment.this.isResumed()) {
                HSQuestionFragment.this.updateQuestionUI(question);
            }
            if (!HSQuestionFragment.this.eventSent.booleanValue()) {
                try {
                    HSQuestionFragment.this.faqId = question.getId();
                    JSONObject eventData = new JSONObject();
                    eventData.put(DBLikedChannelsHelper.KEY_ID, HSQuestionFragment.this.faqId);
                    HSFunnel.pushEvent(HSFunnel.READ_FAQ, eventData);
                    HSQuestionFragment.this.eventSent = Boolean.valueOf(true);
                } catch (JSONException e) {
                    Log.d(HSQuestionFragment.TAG, "JSONException", e);
                }
            }
        }
    };
    private TextView questionText;
    OnClickListener sendAnywayClickListner = new OnClickListener() {
        public void onClick(View view) {
            HSQuestionFragment.this.sendAction("startConversation");
        }
    };
    private HSStorage storage;
    private String titleText;
    private TextView unhelpfulText;

    private void showMarkedToast(Boolean isHelpful) {
        CharSequence toastString = BuildConfig.FLAVOR;
        if (isHelpful.booleanValue()) {
            toastString = getResources().getString(string.hs__mark_helpful_toast);
        } else {
            toastString = getResources().getString(string.hs__mark_unhelpful_toast);
        }
        Toast toast = Toast.makeText(this.activity, toastString, 0);
        toast.setGravity(17, 0, 0);
        toast.show();
    }

    public void showMenuOptions() {
        if (this.isHelpful == 1) {
            hideDislikeItem();
        } else if (this.isHelpful == -1) {
            hideLikeItem();
        } else if (this.isHelpful == 0) {
            showQuestionItem();
        }
    }

    public void highlightSearchTerms() {
        ArrayList<String> matchedWords = (ArrayList) this.extras.get("searchTerms");
        if (!this.isHighlighted.booleanValue() && matchedWords != null && matchedWords.size() > 0) {
            Iterator i$;
            Collections.sort(matchedWords);
            Collections.reverse(matchedWords);
            LinkedHashSet<String> reverseTransKeywords = new LinkedHashSet();
            int highlightColor = Styles.getColor(getActivity(), attr.hs__searchHighlightColor);
            String hexColor = String.format("#%06X", new Object[]{Integer.valueOf(ViewCompat.MEASURED_SIZE_MASK & highlightColor)});
            boolean isEnglish = HSTransliterator.unidecode(this.titleText).equals(this.titleText) && HSTransliterator.unidecode(this.bodyText).equals(this.bodyText);
            String word;
            if (isEnglish) {
                i$ = matchedWords.iterator();
                while (i$.hasNext()) {
                    word = (String) i$.next();
                    if (word.length() >= 3) {
                        reverseTransKeywords.add(word);
                    }
                }
            } else {
                int i;
                String charTransliteration;
                int j;
                int titleLength = this.titleText.length();
                String titleTrans = BuildConfig.FLAVOR;
                ArrayList<Integer> titleIndex = new ArrayList();
                for (i = 0; i < titleLength; i++) {
                    charTransliteration = HSTransliterator.unidecode(this.titleText.charAt(i) + BuildConfig.FLAVOR);
                    for (j = 0; j < charTransliteration.length(); j++) {
                        titleTrans = titleTrans + charTransliteration.charAt(j);
                        titleIndex.add(Integer.valueOf(i));
                    }
                }
                titleTrans = titleTrans.toLowerCase();
                int bodyLength = this.bodyText.length();
                HSTransliterator.unidecode(this.bodyText);
                String bodyTrans = BuildConfig.FLAVOR;
                ArrayList<Integer> bodyIndex = new ArrayList();
                for (i = 0; i < bodyLength; i++) {
                    charTransliteration = HSTransliterator.unidecode(this.bodyText.charAt(i) + BuildConfig.FLAVOR);
                    for (j = 0; j < charTransliteration.length(); j++) {
                        bodyTrans = bodyTrans + charTransliteration.charAt(j);
                        bodyIndex.add(Integer.valueOf(i));
                    }
                }
                bodyTrans = bodyTrans.toLowerCase();
                i$ = matchedWords.iterator();
                while (i$.hasNext()) {
                    word = (String) i$.next();
                    if (word.length() >= 3) {
                        int index;
                        word = word.toLowerCase();
                        for (index = TextUtils.indexOf(titleTrans, word, 0); index >= 0; index = TextUtils.indexOf(titleTrans, word, word.length() + index)) {
                            reverseTransKeywords.add(this.titleText.substring(((Integer) titleIndex.get(index)).intValue(), ((Integer) titleIndex.get((word.length() + index) - 1)).intValue() + 1));
                        }
                        for (index = TextUtils.indexOf(bodyTrans, word, 0); index >= 0; index = TextUtils.indexOf(bodyTrans, word, word.length() + index)) {
                            reverseTransKeywords.add(this.bodyText.substring(((Integer) bodyIndex.get(index)).intValue(), ((Integer) bodyIndex.get((word.length() + index) - 1)).intValue() + 1));
                        }
                    }
                }
            }
            this.bodyText = ">" + this.bodyText + "<";
            this.titleText = ">" + this.titleText + "<";
            Pattern pattern = Pattern.compile(">[^<]+<");
            i$ = reverseTransKeywords.iterator();
            while (i$.hasNext()) {
                String content;
                String reverseTransWord = (String) i$.next();
                String titleTextCopy = this.titleText;
                Matcher matcher = pattern.matcher(titleTextCopy);
                while (matcher.find()) {
                    content = titleTextCopy.substring(matcher.start(), matcher.end());
                    this.titleText = this.titleText.replace(content, content.replaceAll("(?i)(" + reverseTransWord + ")", "<span style=\"background-color: " + hexColor + "\">$1</span>"));
                }
                String bodyTextCopy = this.bodyText;
                matcher = pattern.matcher(bodyTextCopy);
                while (matcher.find()) {
                    content = bodyTextCopy.substring(matcher.start(), matcher.end());
                    this.bodyText = this.bodyText.replace(content, content.replaceAll("(?i)(" + reverseTransWord + ")", "<span style=\"background-color: " + hexColor + "\">$1</span>"));
                }
            }
            this.titleText = this.titleText.substring(1, this.titleText.length() - 1);
            this.bodyText = this.bodyText.substring(1, this.bodyText.length() - 1);
            this.isHighlighted = Boolean.valueOf(true);
            initWebView();
        }
    }

    private void showQuestionItem() {
        if (!this.likeClicked.booleanValue() && !this.dislikeClicked.booleanValue()) {
            this.questionText.setVisibility(0);
            this.likeButton.setVisibility(0);
            this.dislikeButton.setVisibility(0);
            this.unhelpfulText.setVisibility(8);
            this.contactUsBtn.setVisibility(8);
            this.helpfulText.setVisibility(8);
        }
    }

    private void hideLikeItem() {
        if (this.enableContactUs.booleanValue()) {
            this.contactUsBtn.setVisibility(0);
        }
        if (isShowSearchOnNewConversationFlowActive()) {
            this.contactUsBtn.setVisibility(0);
            this.contactUsBtn.setText(string.hs__send_anyway);
            this.contactUsBtn.setOnClickListener(this.sendAnywayClickListner);
            HelpshiftContext.setViewState(HSConsts.ISSUE_FILING);
        }
        this.unhelpfulText.setVisibility(0);
        this.questionText.setVisibility(8);
        this.likeButton.setVisibility(8);
        this.dislikeButton.setVisibility(8);
    }

    private void hideDislikeItem() {
        this.questionText.setVisibility(8);
        this.likeButton.setVisibility(8);
        this.dislikeButton.setVisibility(8);
        this.helpfulText.setVisibility(0);
    }

    @TargetApi(11)
    public void onResume() {
        if (this.mWebView != null) {
            this.mWebView.onResume();
        }
        if (!(TextUtils.isEmpty(this.faqId) || this.eventSent.booleanValue())) {
            try {
                JSONObject eventData = new JSONObject();
                eventData.put(DBLikedChannelsHelper.KEY_ID, this.faqId);
                HSFunnel.pushEvent(HSFunnel.READ_FAQ, eventData);
                this.eventSent = Boolean.valueOf(true);
            } catch (JSONException e) {
                Log.d(TAG, "JSONException", e);
            }
        }
        super.onResume();
    }

    public void onPause() {
        try {
            if (this.mWebView != null) {
                Class.forName("android.webkit.WebView").getMethod("onPause", (Class[]) null).invoke(this.mWebView, (Object[]) null);
            }
        } catch (ClassNotFoundException cnfe) {
            Log.d(TAG, "ClassNotFoundException : ", cnfe);
        } catch (NoSuchMethodException nsme) {
            Log.d(TAG, "NoSuchMethodException : ", nsme);
        } catch (InvocationTargetException ite) {
            Log.d(TAG, "InvocationTargetException : ", ite);
        } catch (IllegalAccessException iae) {
            Log.d(TAG, "IllegalAccessException : ", iae);
        }
        super.onPause();
    }

    private void initWebView() {
        String webBodyText;
        LinearLayout webViewParent = (LinearLayout) this.activity.findViewById(id.hs__webViewParent);
        TypedArray array = this.activity.getTheme().obtainStyledAttributes(new int[]{16842801, 16842806});
        if (this.mWebView == null) {
            this.mWebView = new HSHTML5WebView(getActivity(), this);
            webViewParent.addView(this.mWebView.getLayout(), new LayoutParams(-1, -1));
            this.mWebView.setBackgroundColor(array.getColor(0, ViewCompat.MEASURED_SIZE_MASK));
            WebSettings s = this.mWebView.getSettings();
            s.setJavaScriptEnabled(true);
            if (VERSION.SDK_INT <= 11) {
                s.setPluginState(PluginState.ON);
            }
            s.setTextSize(TextSize.NORMAL);
        }
        if (this.bodyText.contains("<iframe")) {
            try {
                this.bodyText = this.bodyText.replace("https", HttpHost.DEFAULT_SCHEME_NAME);
            } catch (NullPointerException e) {
                Log.d(TAG, e.toString(), e);
            }
        }
        String textColor = String.format("#%06X", new Object[]{Integer.valueOf(array.getColor(1, ViewCompat.MEASURED_SIZE_MASK) & ViewCompat.MEASURED_SIZE_MASK)});
        array.recycle();
        if (this.isRtl.booleanValue()) {
            webBodyText = "<html dir=\"rtl\">";
        } else {
            webBodyText = "<html>";
        }
        this.mWebView.loadDataWithBaseURL(null, webBodyText + "<head>" + "<style type=\"text/css\">img, object, embed { max-width: 100%; }" + "body { margin: 0px 10px 10px 0px; padding: 0; line-height: 1.5; white-space: normal; word-wrap: break-word; color: " + textColor + "; }" + ".title { display:block; margin: -12px 0 6px 0; padding: 0; font-size: 1.3125em; line-height: 1.25 }" + "</style>" + "<script language=\"javascript\">var iframe = document.getElementsByTagName (\"iframe\") [0]; if (iframe) { iframe.width = \"100%\"; iframe.style.width = \"100%\"; }" + "document.addEventListener('click',function(event) {" + "if (event.target instanceof HTMLImageElement) { event.preventDefault(); event.stopPropagation(); }" + "}, false);" + "</script>\u200b" + "</head>" + "<body>" + "<strong class='title'>" + this.titleText + "</strong>" + this.bodyText + "</body>" + "</html>", "text/html", "utf-8", null);
    }

    private void updateQuestionUI(Faq question) {
        this.titleText = question.getTitle();
        this.bodyText = question.getBody();
        this.faqId = question.getId();
        this.isRtl = question.getIsRtl();
        this.isHelpful = question.getIsHelpful();
        this.isHighlighted = Boolean.valueOf(false);
        initWebView();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.activity = (HSActivity) getActivity();
        this.data = new HSApiData(this.activity);
        this.storage = this.data.storage;
        return inflater.inflate(layout.hs__question_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.extras = this.activity.getIntent().getExtras();
        if (this.extras != null) {
            if (this.extras.get("questionPublishId") != null) {
                if (this.extras.getBoolean("decomp")) {
                    HSAnalytics.decomp = true;
                }
                this.data.getQuestion((String) this.extras.get("questionPublishId"), this.questionHandler, this.questionFailHandler);
            }
            this.enableContactUs = Boolean.valueOf(ContactUsFilter.showContactUs(LOCATION.QUESTION_FOOTER));
        }
        TypedArray array = this.activity.getTheme().obtainStyledAttributes(new int[]{16842801});
        int activityBackgroundColor = array.getColor(0, ViewCompat.MEASURED_SIZE_MASK);
        array.recycle();
        getView().setBackgroundColor(activityBackgroundColor);
        this.contactUsContainer = (LinearLayout) view.findViewById(id.hs__contactUsContainer);
        this.questionText = (TextView) view.findViewById(id.hs__question);
        this.helpfulText = (TextView) view.findViewById(id.hs__helpful_text);
        this.unhelpfulText = (TextView) view.findViewById(id.hs__unhelpful_text);
        this.contactUsBtn = (Button) view.findViewById(id.hs__contact_us_btn);
        Styles.setButtonCompoundDrawableIconColor(this.activity, this.contactUsBtn.getCompoundDrawables()[0]);
        this.likeButton = (Button) view.findViewById(id.hs__action_faq_helpful);
        this.likeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                HSQuestionFragment.this.likeClicked = Boolean.valueOf(true);
                HSQuestionFragment.this.markQuestion(new Handler(), HSQuestionFragment.this.markFailHandler, HSQuestionFragment.this.faqId, Boolean.valueOf(true));
                HSFunnel.pushEvent(HSFunnel.MARKED_HELPFUL, HSQuestionFragment.this.eventData);
                HSQuestionFragment.this.hideDislikeItem();
                HSQuestionFragment.this.showMarkedToast(Boolean.valueOf(true));
                if (HSQuestionFragment.this.isShowSearchOnNewConversationFlowActive()) {
                    HSQuestionFragment.this.sendAction("ticketAvoided");
                }
            }
        });
        this.dislikeButton = (Button) view.findViewById(id.hs__action_faq_unhelpful);
        this.dislikeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                HSQuestionFragment.this.dislikeClicked = Boolean.valueOf(true);
                HSQuestionFragment.this.markQuestion(new Handler(), HSQuestionFragment.this.markFailHandler, HSQuestionFragment.this.faqId, Boolean.valueOf(false));
                HSFunnel.pushEvent(HSFunnel.MARKED_UNHELPFUL, HSQuestionFragment.this.eventData);
                HSQuestionFragment.this.hideLikeItem();
                HSQuestionFragment.this.showMarkedToast(Boolean.valueOf(false));
            }
        });
        this.contactUsBtn.setOnClickListener(this.contactUsClickListener);
        setHasOptionsMenu(true);
    }

    public boolean isShowSearchOnNewConversationFlowActive() {
        return ((HSQuestion) getActivity()).isShowSearchOnNewConversationFlowActive();
    }

    public void onDestroy() {
        if (this.mWebView != null) {
            this.mWebView.freeMemory();
            this.mWebView.removeAllViews();
            ((ViewGroup) this.mWebView.getParent()).removeView(this.mWebView);
            this.mWebView.destroy();
        }
        HelpshiftContext.setViewState(null);
        super.onDestroy();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void markQuestion(Handler success, Handler failure, String faqId, Boolean helpful) {
        JSONObject params = new JSONObject();
        this.eventData = new JSONObject();
        try {
            this.eventData.put(DBLikedChannelsHelper.KEY_ID, faqId);
            params.put(HSFunnel.READ_FAQ, faqId);
            params.put(HSFunnel.MARKED_HELPFUL, helpful);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException", e);
        }
        this.data.markQuestion(success, this.data.getApiFailHandler(failure, faqId, 0, params), faqId, helpful);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            if (this.mWebView == null || !this.mWebView.inCustomView()) {
                getActivity().finish();
                return true;
            }
            this.mWebView.hideCustomView();
            return true;
        } else if (id != id.hs__action_done) {
            return super.onOptionsItemSelected(item);
        } else {
            sendAction("ticketAvoided");
            return true;
        }
    }

    private void sendTicketAvoidedEvent() {
        JSONObject eventData = new JSONObject();
        try {
            eventData.put(DBLikedChannelsHelper.KEY_ID, this.faqId);
            eventData.put("str", this.storage.getConversationDetail(this.data.getLoginId()));
            HSFunnel.pushEvent(HSFunnel.TICKET_AVOIDED, eventData);
        } catch (JSONException e) {
            Log.d(TAG, "sendTicketAvoidedEvent", e);
        }
    }

    private void sendAction(String action) {
        if (action.equals("ticketAvoided")) {
            sendTicketAvoidedEvent();
            this.storage.storeConversationDetail(BuildConfig.FLAVOR, this.data.getLoginId());
            this.storage.setConversationScreenshot(BuildConfig.FLAVOR, this.data.getLoginId());
        } else if (action.equals("startConversation")) {
            HSFunnel.pushEvent(HSFunnel.TICKET_AVOIDANCE_FAILED);
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra(NativeProtocol.WEB_DIALOG_ACTION, action);
        getActivity().setResult(-1, returnIntent);
        getActivity().finish();
    }

    public void hideQuestionFooter() {
        this.contactUsContainer.setVisibility(8);
    }
}
