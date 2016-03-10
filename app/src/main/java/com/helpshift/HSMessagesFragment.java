package com.helpshift;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.Fragment;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import com.facebook.GraphResponse;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.menu;
import com.helpshift.D.string;
import com.helpshift.HSApiData.HS_ISSUE_CSAT_STATE;
import com.helpshift.Helpshift.HelpshiftDelegate;
import com.helpshift.constants.MessageColumns;
import com.helpshift.customadapters.MessagesAdapter;
import com.helpshift.models.Issue;
import com.helpshift.res.values.HSConsts;
import com.helpshift.storage.IssuesDataSource;
import com.helpshift.storage.ProfilesDBHelper;
import com.helpshift.util.AttachmentUtil;
import com.helpshift.util.HSActivityUtil;
import com.helpshift.util.HSColor;
import com.helpshift.util.HSErrors;
import com.helpshift.util.HSJSONUtils;
import com.helpshift.util.IssuesUtil;
import com.helpshift.util.MessagesUtil;
import com.helpshift.util.Styles;
import com.helpshift.viewstructs.HSMsg;
import com.helpshift.widget.CSATView;
import com.helpshift.widget.CSATView.CSATListener;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HSMessagesFragment extends Fragment implements CSATListener, DownloadTaskCallBacks {
    public static final String TAG = "HelpShiftDebug";
    private final int MESSAGE_POLL_DURATION = 3;
    private HSActivity activity;
    private MessagesAdapter adapter;
    private MenuItem attachScreenshotMenu;
    private String chatLaunchSource;
    private LinearLayout confirmationBox;
    private final BroadcastReceiver connChecker = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HSMessagesFragment.this.startPoller();
        }
    };
    private CSATView csatView = null;
    private ViewStub csatViewStub;
    private Boolean decomp;
    private DownloadTaskCallBacks downloadTaskCallBacks;
    private boolean enableNCRMessage = true;
    private Bundle extras;
    private final BroadcastReceiver failedMessageRequestChecker = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            HSMessagesFragment.this.refreshMessages();
        }
    };
    private Handler fetchMessagesFailure = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Integer status = (Integer) msg.obj.get(SettingsJsonConstants.APP_STATUS_KEY);
            if (!(status.intValue() == -1 || HSMessagesFragment.this.pollerThreadHandler == null)) {
                HSMessagesFragment.this.pollerThreadHandler.getLooper().quit();
            }
            HSErrors.showFailToast(status.intValue(), null, HSMessagesFragment.this.activity);
        }
    };
    private Handler fetchMessagesSuccess = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj.length() > 0) {
                HSMessagesFragment.this.showAgentName = Issue.isShowAgentNameEnabled(HSMessagesFragment.this.issueId);
                HSMessagesFragment.this.refreshMessages();
                HSMessagesFragment.this.refreshStatus();
                HSMessagesFragment.this.messagesListView.setSelection(HSMessagesFragment.this.adapter.getCount() - 1);
            }
        }
    };
    private HelpshiftDelegate helpshiftDelegate;
    private HSApiData hsApiData;
    private HSApiClient hsClient;
    private HSStorage hsStorage;
    private String issueId;
    private RelativeLayout messageBox;
    private HashSet<String> messageIdsSet = new HashSet();
    private ArrayList<HSMsg> messagesList = new ArrayList();
    private TextView messagesListFooterView;
    private ListView messagesListView;
    private boolean newActivity = true;
    private LinearLayout newConversationBox;
    private Button newConversationBtn;
    private Boolean newIssue;
    private boolean persistMessageBox = false;
    private Thread pollerThread;
    private Handler pollerThreadHandler;
    private Handler replyFailHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (HSMessagesFragment.this.pollerThreadHandler != null) {
                HSMessagesFragment.this.pollerThreadHandler.getLooper().quit();
            }
            HSMessagesFragment.this.refreshMessages();
        }
    };
    private EditText replyField;
    private Handler replyHandler = new Handler() {
        public void handleMessage(Message msg) {
            HSMessagesFragment.this.renderReplyMsg(msg);
        }
    };
    private HashSet<String> scReferIdsSet = new HashSet();
    private boolean selectImage = false;
    private boolean showAgentName;
    private Boolean showConvOnReportIssue;
    private boolean showingConfirmationBox = false;
    private boolean showingNewConversationBox = false;
    private ImageButton solvedBtn;
    private String ssMsgPos;
    private ImageButton unsolvedBtn;

    private class DownloadImagesTask extends AsyncTask<HashMap, Void, HashMap> {
        private DownloadImagesTask() {
        }

        protected HashMap doInBackground(HashMap... imagesData) {
            HashMap imageData = imagesData[0];
            String url = (String) imageData.get(SettingsJsonConstants.APP_URL_KEY);
            String messageId = (String) imageData.get("messageId");
            int attachId = ((Integer) imageData.get("attachId")).intValue();
            int position = ((Integer) imageData.get("position")).intValue();
            HashMap result = new HashMap();
            try {
                String filePath = HSMessagesFragment.this.downloadAttachment(url, messageId, attachId);
                result.put(GraphResponse.SUCCESS_KEY, Boolean.valueOf(true));
                result.put("filepath", filePath);
                result.put("position", Integer.valueOf(position));
            } catch (IOException e) {
                Log.d(HSMessagesFragment.TAG, "Downloading image", e);
                result.put(GraphResponse.SUCCESS_KEY, Boolean.valueOf(false));
            }
            return result;
        }

        protected void onPostExecute(HashMap result) {
            if (((Boolean) result.get(GraphResponse.SUCCESS_KEY)).booleanValue()) {
                ((HSMsg) HSMessagesFragment.this.messagesList.get(((Integer) result.get("position")).intValue())).screenshot = (String) result.get("filepath");
                HSMessagesFragment.this.adapter.notifyDataSetChanged();
            }
        }
    }

    private void gotoApp(String marketUrl) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setData(Uri.parse(marketUrl));
        if (!isResumed() || intent.resolveActivity(getActivity().getPackageManager()) == null) {
            HSErrors.showFailToast(4, null, this.activity);
        } else {
            startActivity(intent);
        }
    }

    private void renderReplyMsg(Message msg) {
        JSONObject message = (JSONObject) msg.obj.get("response");
        JSONArray messages = new JSONArray();
        messages.put(message);
        IssuesDataSource.storeMessages(IssuesUtil.jsonArrayToMessageList(messages));
        refreshMessages();
    }

    private void refreshStatus() {
        List openConversations = this.hsStorage.getOpenConversations(this.hsApiData.getProfileId());
        Integer status = Integer.valueOf(IssuesDataSource.getIssue(this.issueId).getStatus());
        int replyLength = this.hsStorage.getReply(this.hsApiData.getProfileId()).trim().length();
        if (status.equals(Integer.valueOf(0)) || status.equals(Integer.valueOf(1))) {
            showMessageBox();
            this.persistMessageBox = false;
        } else if (!status.equals(Integer.valueOf(2))) {
        } else {
            if (openConversations.contains(this.issueId)) {
                showConfirmationBox();
            } else if (this.persistMessageBox || replyLength != 0 || this.hsStorage.getScreenShotDraft().booleanValue()) {
                showMessageBox();
            } else {
                showNewConversationBox();
            }
        }
    }

    private void showConfirmationBox() {
        this.adapter.enableButtons(false);
        LayoutParams params = (LayoutParams) this.confirmationBox.getLayoutParams();
        this.messageBox.setVisibility(8);
        hideKeyboard(this.replyField);
        params.addRule(12);
        this.confirmationBox.setLayoutParams(params);
        this.confirmationBox.setVisibility(0);
        this.newConversationBox.setVisibility(8);
        if (this.attachScreenshotMenu != null) {
            this.attachScreenshotMenu.setVisible(false);
        }
        this.showingConfirmationBox = true;
        setMessagesListViewFooter(string.hs__confirmation_footer_msg);
    }

    private void showNewConversationBox() {
        this.enableNCRMessage = false;
        this.adapter.enableButtons(false);
        hideKeyboard(this.replyField);
        this.confirmationBox.setVisibility(8);
        this.newConversationBox.setVisibility(0);
        this.messageBox.setVisibility(8);
        if (this.attachScreenshotMenu != null) {
            this.attachScreenshotMenu.setVisible(false);
        }
        HS_ISSUE_CSAT_STATE state = this.hsApiData.getCSatState(this.issueId);
        if (state == HS_ISSUE_CSAT_STATE.CSAT_APPLICABLE || state == HS_ISSUE_CSAT_STATE.CSAT_REQUESTED) {
            this.csatView = inflateCSATView();
            changeNewConversationButtonMargin(getResources().getConfiguration());
            setMessagesListViewFooter(string.hs__confirmation_footer_msg);
        } else {
            setMessagesListViewFooter(string.hs__conversation_end_msg);
        }
        this.showingNewConversationBox = true;
    }

    private CSATView inflateCSATView() {
        if (this.csatViewStub == null) {
            return null;
        }
        CSATView rView = (CSATView) this.csatViewStub.inflate();
        rView.setCSATListener(this);
        this.csatViewStub = null;
        this.hsApiData.setCSatState(this.issueId, HS_ISSUE_CSAT_STATE.CSAT_REQUESTED);
        return rView;
    }

    public void csatViewDissmissed() {
        setMessagesListViewFooter(string.hs__conversation_end_msg);
    }

    public void sendCSATSurvey(int rating, String feedback) {
        feedback = feedback.trim();
        this.hsApiData.sendCustomerSatisfactionSurvey(Integer.valueOf(rating), feedback, this.issueId, new Handler(), new Handler());
        if (this.helpshiftDelegate != null) {
            this.helpshiftDelegate.userCompletedCustomerSatisfactionSurvey(rating, feedback);
        }
    }

    private void setMessagesListViewFooter(int resId) {
        this.messagesListView.removeFooterView(this.messagesListFooterView);
        if (resId != -1) {
            this.messagesListFooterView.setText(resId);
            this.messagesListView.addFooterView(this.messagesListFooterView);
        }
    }

    private void showMessageBox() {
        this.adapter.enableButtons(true);
        this.confirmationBox.setVisibility(8);
        this.newConversationBox.setVisibility(8);
        this.messageBox.setVisibility(0);
        if (!(this.attachScreenshotMenu == null || this.hsStorage.getEnableFullPrivacy().booleanValue())) {
            this.attachScreenshotMenu.setVisible(true);
        }
        setMessagesListViewFooter(-1);
    }

    private void showKeyboard(View v) {
        v.requestFocus();
        ((InputMethodManager) this.activity.getSystemService("input_method")).showSoftInput(v, 0);
    }

    private void hideKeyboard(View v) {
        ((InputMethodManager) this.activity.getSystemService("input_method")).hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void refreshMessages() {
        refreshMessages(this.hsApiData.getMessagesWithFails(this.issueId));
    }

    private void refreshMessages(JSONArray messages) {
        try {
            this.messagesList.clear();
            this.messageIdsSet.clear();
            this.scReferIdsSet.clear();
            int messagesLength = messages.length();
            int i = 0;
            while (i < messagesLength) {
                JSONObject message = messages.getJSONObject(i);
                String id = message.getString(DBLikedChannelsHelper.KEY_ID);
                String type = message.getString(MessageColumns.TYPE);
                if (type.equals(HSConsts.ADMIN_ATTACHMENT_GENERIC_TYPE)) {
                    String filePath = this.hsStorage.getFilePathForGenericAttachment(id);
                    if (new File(filePath).exists()) {
                        message.put("state", 3);
                    } else {
                        if (!filePath.equals(BuildConfig.FLAVOR)) {
                            this.hsStorage.removeFromDownloadedGenericFiles(id);
                        }
                        if (this.hsStorage.isDownloadActive(id)) {
                            message.put("state", 1);
                        }
                    }
                } else if (type.equals(HSConsts.ADMIN_ATTACHMENT_IMAGE_TYPE)) {
                    String imagePath = this.hsStorage.getFilePathForImage(id);
                    if (new File(imagePath).exists()) {
                        message.put(MessageColumns.SCREENSHOT, imagePath);
                        message.put("state", 3);
                    } else {
                        if (!imagePath.equals(BuildConfig.FLAVOR)) {
                            this.hsStorage.removeFromDownloadedImageFiles(id);
                        }
                        String thumbnailPath = this.hsStorage.getFilePathForThumbnail(id);
                        if (new File(thumbnailPath).exists()) {
                            message.put(MessageColumns.SCREENSHOT, thumbnailPath);
                            if (this.hsStorage.isDownloadActive(id)) {
                                message.put("state", 2);
                            } else {
                                message.put("state", 1);
                            }
                        } else {
                            if (!thumbnailPath.equals(BuildConfig.FLAVOR)) {
                                this.hsStorage.removeFromDownloadedThumbnailFiles(id);
                            }
                        }
                    }
                }
                String origin = message.getString(MessageColumns.ORIGIN);
                String body = message.getString(MessageColumns.BODY);
                String date = message.getString(MPDbAdapter.KEY_CREATED_AT);
                int state = message.optInt("state", 0);
                Boolean inProgress = Boolean.valueOf(message.optBoolean("inProgress", false));
                if (!origin.equals("mobile") || !type.equals("ncr") || this.enableNCRMessage || i != messagesLength - 1) {
                    String screenshot = message.optString(MessageColumns.SCREENSHOT, BuildConfig.FLAVOR);
                    if (type.equals("rsc")) {
                        screenshot = message.optString(MessageColumns.SCREENSHOT, BuildConfig.FLAVOR);
                        if (id.startsWith(AttachmentUtil.LOCAL_RSC_MSG_ID_PREFIX) && this.scReferIdsSet.contains(id)) {
                            IssuesDataSource.deleteMessage(id);
                        }
                    }
                    if (!origin.equals("admin") || !type.equals("rfr") || MessagesUtil.isRfrAccepted(messages, i, id)) {
                        Boolean metaResponse = Boolean.valueOf(false);
                        JSONObject meta = message.optJSONObject(MessageColumns.META);
                        if (meta != null) {
                            JSONObject messageMeta = meta.optJSONObject("response");
                            if (messageMeta != null) {
                                metaResponse = Boolean.valueOf(messageMeta.optBoolean("state"));
                            }
                        }
                        String agentName = BuildConfig.FLAVOR;
                        if (this.showAgentName) {
                            JSONObject author = message.optJSONObject(MessageColumns.AUTHOR);
                            if (author != null) {
                                agentName = author.optString(ProfilesDBHelper.COLUMN_NAME);
                            }
                        }
                        boolean z = message.optBoolean(MessageColumns.INVISIBLE) || metaResponse.booleanValue();
                        Boolean invisible = Boolean.valueOf(z);
                        if (MessagesUtil.isMessageSupported(origin, type) && !this.messageIdsSet.contains(id)) {
                            this.messageIdsSet.add(id);
                            this.messagesList.add(new HSMsg(id, type, origin, body, date, invisible, screenshot, state, inProgress, agentName));
                            if (type.equals("sc")) {
                                meta = message.optJSONObject(MessageColumns.META);
                                if (meta != null) {
                                    JSONArray attachments = meta.optJSONArray("attachments");
                                    String refers = meta.optString("refers", id);
                                    if (refers.startsWith(AttachmentUtil.LOCAL_RSC_MSG_ID_PREFIX)) {
                                        if (this.messageIdsSet.contains(refers)) {
                                            removeMessage(refers);
                                            IssuesDataSource.deleteMessage(refers);
                                        } else {
                                            this.scReferIdsSet.add(refers);
                                        }
                                    }
                                    if (attachments != null && attachments.length() > 0 && refers != null) {
                                        JSONObject attachment = attachments.optJSONObject(0);
                                        if (attachment != null) {
                                            String url = attachment.optString(SettingsJsonConstants.APP_URL_KEY, BuildConfig.FLAVOR);
                                            HashMap imgData = new HashMap();
                                            imgData.put(SettingsJsonConstants.APP_URL_KEY, url);
                                            imgData.put("messageId", refers);
                                            imgData.put("attachId", Integer.valueOf(0));
                                            imgData.put("position", Integer.valueOf(this.messagesList.size() - 1));
                                            new DownloadImagesTask().execute(new HashMap[]{imgData});
                                        }
                                    } else if (attachments == null) {
                                        removeMessage(id);
                                    }
                                }
                            }
                        }
                    }
                }
                i++;
            }
        } catch (Throwable e) {
            Log.d(TAG, "Slug in get(\"slug\") no found", e);
        }
        if (messages.length() > 0) {
            this.adapter.notifyDataSetChanged();
        }
    }

    private void removeMessage(String messageId) {
        Iterator it = this.messagesList.iterator();
        while (it.hasNext()) {
            if (((HSMsg) it.next()).id.equals(messageId)) {
                it.remove();
            }
        }
        this.messageIdsSet.remove(messageId);
    }

    public void onPause() {
        super.onPause();
        if (this.pollerThreadHandler != null) {
            this.pollerThreadHandler.getLooper().quit();
        }
        try {
            this.hsStorage.resetIssueCount(this.issueId);
            if (this.helpshiftDelegate != null) {
                this.helpshiftDelegate.didReceiveNotification(0);
            }
        } catch (JSONException e) {
            Log.d(TAG, e.getMessage(), e);
        }
        this.hsStorage.setForeground(Boolean.valueOf(false));
        this.hsStorage.setForegroundIssue(BuildConfig.FLAVOR);
        this.hsApiData.resetServiceInterval();
        this.activity.unregisterReceiver(this.connChecker);
        this.activity.unregisterReceiver(this.failedMessageRequestChecker);
        DownloadManager.deregisterDownloadTaskCallBacks();
        this.hsStorage.storeReply(this.replyField.getText().toString().trim(), this.hsApiData.getProfileId());
    }

    public void onDestroy() {
        super.onDestroy();
        HelpshiftContext.setViewState(null);
    }

    public void startPoller() {
        if (this.pollerThreadHandler != null) {
            this.pollerThreadHandler.getLooper().quit();
            this.pollerThread = null;
        }
        String conversation = this.hsStorage.getActiveConversation(this.hsApiData.getProfileId());
        String archivedConversation = this.hsStorage.getArchivedConversation(this.hsApiData.getProfileId());
        if (!TextUtils.isEmpty(conversation) || !TextUtils.isEmpty(archivedConversation)) {
            this.pollerThread = new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    HSMessagesFragment.this.pollerThreadHandler = new Handler() {
                        public void handleMessage(Message msg) {
                            Message newMsg = HSMessagesFragment.this.fetchMessagesSuccess.obtainMessage();
                            newMsg.obj = msg.obj;
                            HSMessagesFragment.this.fetchMessagesSuccess.sendMessage(newMsg);
                        }
                    };
                    new Runnable() {
                        public void run() {
                            try {
                                HSMessagesFragment.this.hsApiData.getLatestIssues(HSMessagesFragment.this.pollerThreadHandler, HSMessagesFragment.this.fetchMessagesFailure, HSMessagesFragment.this.chatLaunchSource);
                            } catch (JSONException e) {
                                Log.d(HSMessagesFragment.TAG, "get issues", e);
                            }
                            HSMessagesFragment.this.pollerThreadHandler.postDelayed(this, 3000);
                        }
                    }.run();
                    Looper.loop();
                }
            });
            this.pollerThread.start();
        }
    }

    public void onResume() {
        super.onResume();
        this.selectImage = false;
        ((NotificationManager) getActivity().getSystemService("notification")).cancel(this.issueId, 1);
        this.hsStorage.clearNotification(this.issueId);
        IntentFilter connFilter = new IntentFilter();
        connFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.activity.registerReceiver(this.connChecker, connFilter);
        IntentFilter failFilter = new IntentFilter();
        failFilter.addAction("com.helpshift.failedMessageRequest");
        this.activity.registerReceiver(this.failedMessageRequestChecker, failFilter);
        startPoller();
        try {
            this.hsStorage.resetIssueCount(this.issueId);
            if (this.helpshiftDelegate != null) {
                this.helpshiftDelegate.didReceiveNotification(0);
            }
        } catch (JSONException e) {
            Log.d(TAG, e.toString(), e);
        }
        this.hsStorage.setForeground(Boolean.valueOf(true));
        this.hsStorage.setForegroundIssue(this.issueId);
        this.hsApiData.updateMessageSeenState(this.issueId, this.chatLaunchSource);
        String replyText = this.hsStorage.getReply(this.hsApiData.getProfileId());
        if (!this.showingNewConversationBox) {
            this.replyField.setText(replyText);
        }
        if (this.newActivity && TextUtils.isEmpty(replyText)) {
            this.persistMessageBox = false;
        }
        this.newActivity = false;
        if (VERSION.SDK_INT < 11) {
            if (this.messageBox == null || this.messageBox.getVisibility() != 0) {
                this.attachScreenshotMenu.setVisible(false);
            } else {
                this.attachScreenshotMenu.setVisible(true);
            }
        }
        DownloadManager.registerDownloadTaskCallbacks(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.activity = (HSActivity) getActivity();
        this.activity.setSupportProgressBarIndeterminateVisibility(false);
        this.extras = getArguments();
        this.extras.remove(SettingsJsonConstants.PROMPT_MESSAGE_KEY);
        this.hsApiData = new HSApiData(this.activity);
        this.hsStorage = this.hsApiData.storage;
        this.hsClient = this.hsApiData.client;
        this.newIssue = Boolean.valueOf(this.extras.getBoolean("newIssue", false));
        this.decomp = Boolean.valueOf(this.extras.getBoolean("decomp", false));
        this.chatLaunchSource = this.extras.getString("chatLaunchSource");
        this.showConvOnReportIssue = Boolean.valueOf(this.extras.getBoolean("showConvOnReportIssue"));
        this.downloadTaskCallBacks = this;
        this.helpshiftDelegate = Helpshift.getDelegate();
        setHasOptionsMenu(true);
        this.messagesListFooterView = (TextView) inflater.inflate(layout.hs__messages_list_footer, null);
        HSColor.setTextViewAlpha(this.messagesListFooterView, 0.7f);
        return inflater.inflate(layout.hs__messages_fragment, container, false);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        changeNewConversationButtonMargin(newConfig);
    }

    private void changeNewConversationButtonMargin(Configuration config) {
        if (this.newConversationBtn != null) {
            int topMargin;
            int bottomMargin;
            int dividerTopMargin;
            if (config.orientation == 1) {
                topMargin = dpToPixel(28);
                bottomMargin = dpToPixel(32);
                dividerTopMargin = dpToPixel(28);
            } else {
                topMargin = dpToPixel(6);
                bottomMargin = dpToPixel(6);
                dividerTopMargin = 0;
            }
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
            layoutParams.setMargins(0, topMargin, 0, bottomMargin);
            this.newConversationBtn.setLayoutParams(layoutParams);
            if (this.csatView != null) {
                this.csatView.setDividerMargin(0, dividerTopMargin, 0, 0);
            }
        }
    }

    private int dpToPixel(int dpValue) {
        return (int) (((float) dpValue) * getResources().getDisplayMetrics().density);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.messagesListView = (ListView) view.findViewById(id.hs__messagesList);
        this.replyField = (EditText) view.findViewById(id.hs__messageText);
        final ImageButton addReply = (ImageButton) view.findViewById(id.hs__sendMessageBtn);
        this.confirmationBox = (LinearLayout) view.findViewById(id.hs__confirmation);
        this.newConversationBox = (LinearLayout) view.findViewById(id.hs__new_conversation);
        this.messageBox = (RelativeLayout) view.findViewById(id.relativeLayout1);
        this.solvedBtn = (ImageButton) view.findViewById(16908313);
        this.unsolvedBtn = (ImageButton) view.findViewById(16908314);
        this.newConversationBtn = (Button) view.findViewById(id.hs__new_conversation_btn);
        changeNewConversationButtonMargin(getResources().getConfiguration());
        this.csatViewStub = (ViewStub) view.findViewById(id.csat_view_stub);
        Styles.setButtonCompoundDrawableIconColor(this.activity, this.newConversationBtn.getCompoundDrawables()[0]);
        Styles.setAcceptButtonIconColor(this.activity, this.solvedBtn.getDrawable());
        Styles.setRejectButtonIconColor(this.activity, this.unsolvedBtn.getDrawable());
        this.solvedBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                HSMessagesFragment.this.replyField.setText(BuildConfig.FLAVOR);
                HSMessagesFragment.this.hsStorage.storeReply(BuildConfig.FLAVOR, HSMessagesFragment.this.hsApiData.getProfileId());
                HSMessagesFragment.this.sendResolutionEvent(Boolean.valueOf(true));
                HSMessagesFragment.this.persistMessageBox = false;
                HSMessagesFragment.this.refreshStatus();
                if (HSMessagesFragment.this.helpshiftDelegate != null) {
                    HSMessagesFragment.this.helpshiftDelegate.userRepliedToConversation(Helpshift.HSUserAcceptedTheSolution);
                }
            }
        });
        this.unsolvedBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                HelpshiftContext.setViewState(HSConsts.MESSAGE_FILING);
                HSMessagesFragment.this.refreshMessages();
                HSMessagesFragment.this.persistMessageBox = true;
                HSMessagesFragment.this.showMessageBox();
                if (HSMessagesFragment.this.replyField.getText().toString().trim().length() == 0) {
                    HSMessagesFragment.this.showKeyboard(HSMessagesFragment.this.replyField);
                }
                HSMessagesFragment.this.sendResolutionEvent(Boolean.valueOf(false));
                if (HSMessagesFragment.this.helpshiftDelegate != null) {
                    HSMessagesFragment.this.helpshiftDelegate.userRepliedToConversation(Helpshift.HSUserRejectedTheSolution);
                }
            }
        });
        this.newConversationBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent i = new Intent(HSMessagesFragment.this.activity, HSConversation.class);
                i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(HSMessagesFragment.this.activity));
                i.putExtra("newConversation", true);
                i.putExtras(HSMessagesFragment.this.extras);
                i.removeExtra("isRoot");
                HSMessagesFragment.this.getActivity().startActivityForResult(i, 1);
                HSMessagesFragment.this.hsStorage.setArchivedConversation(BuildConfig.FLAVOR, HSMessagesFragment.this.hsApiData.getProfileId());
            }
        });
        this.messagesListView.setDivider(null);
        this.adapter = new MessagesAdapter(this, 17367043, this.messagesList);
        TextView dummyTextView = new TextView(this.activity);
        this.messagesListView.addFooterView(dummyTextView);
        this.messagesListView.setAdapter(this.adapter);
        this.messagesListView.removeFooterView(dummyTextView);
        this.issueId = this.extras.getString("issueId");
        this.showAgentName = Issue.isShowAgentNameEnabled(this.issueId);
        refreshStatus();
        refreshMessages();
        this.messagesListView.setSelection(this.adapter.getCount() - 1);
        this.activity.getActionBarHelper().setDisplayHomeAsUpEnabled(true);
        if (this.replyField.getText().length() == 0) {
            addReply.setEnabled(false);
            addReply.setAlpha(64);
            Styles.setSendMessageButtonIconColor(this.activity, addReply.getDrawable());
        } else {
            addReply.setEnabled(true);
            addReply.setAlpha(SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
            Styles.setSendMessageButtonActiveIconColor(this.activity, addReply.getDrawable());
        }
        addReply.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                String replyText = HSMessagesFragment.this.replyField.getText().toString().trim();
                if (!TextUtils.isEmpty(replyText)) {
                    HSMessagesFragment.this.replyField.setText(BuildConfig.FLAVOR);
                    HSMessagesFragment.this.addMessage(HSMessagesFragment.this.replyHandler, HSMessagesFragment.this.replyFailHandler, HSMessagesFragment.this.issueId, replyText, "txt", BuildConfig.FLAVOR);
                    if (HSMessagesFragment.this.helpshiftDelegate != null) {
                        HSMessagesFragment.this.helpshiftDelegate.userRepliedToConversation(replyText);
                    }
                    try {
                        JSONObject eventData = new JSONObject();
                        eventData.put(MessageColumns.TYPE, "txt");
                        eventData.put(MessageColumns.BODY, replyText);
                        eventData.put(DBLikedChannelsHelper.KEY_ID, HSMessagesFragment.this.issueId);
                        HSFunnel.pushEvent(HSFunnel.MESSAGE_ADDED, eventData);
                    } catch (JSONException e) {
                        Log.d(HSMessagesFragment.TAG, "JSONException", e);
                    }
                }
            }
        });
        this.replyField.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 4) {
                    addReply.performClick();
                }
                return false;
            }
        });
        this.replyField.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HSMessagesFragment.this.persistMessageBox = true;
                if (s.length() == 0) {
                    addReply.setEnabled(false);
                    addReply.setAlpha(64);
                    Styles.setSendMessageButtonIconColor(HSMessagesFragment.this.activity, addReply.getDrawable());
                    return;
                }
                addReply.setEnabled(true);
                addReply.setAlpha(SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
                Styles.setSendMessageButtonActiveIconColor(HSMessagesFragment.this.activity, addReply.getDrawable());
            }
        });
    }

    private void sendResolutionEvent(Boolean accepted) {
        this.hsStorage.clearAndUpdateActiveConversation(this.issueId, this.hsApiData.getProfileId());
        try {
            JSONObject eventData = new JSONObject();
            eventData.put(DBLikedChannelsHelper.KEY_ID, this.issueId);
            String messageType = BuildConfig.FLAVOR;
            if (accepted.booleanValue()) {
                HSFunnel.pushEvent(HSFunnel.RESOLUTION_ACCEPTED, eventData);
                messageType = "ca";
            } else {
                HSFunnel.pushEvent(HSFunnel.RESOLUTION_REJECTED, eventData);
                messageType = "ncr";
            }
            addMessage(this.replyHandler, this.replyFailHandler, this.issueId, BuildConfig.FLAVOR, messageType, BuildConfig.FLAVOR);
            this.hsApiData.setCSatState(this.issueId, HS_ISSUE_CSAT_STATE.CSAT_APPLICABLE);
        } catch (JSONException e) {
            Log.d(TAG, "JSONException", e);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == 16908332) {
            getActivity().onBackPressed();
            return true;
        } else if (id != id.hs__attach_screenshot) {
            return super.onOptionsItemSelected(item);
        } else {
            selectImagePopup(0);
            return true;
        }
    }

    public void onStart() {
        super.onStart();
        if (!this.selectImage) {
            HSAnalytics.onActivityStarted(this.activity);
            try {
                JSONObject eventData = new JSONObject();
                eventData.put(DBLikedChannelsHelper.KEY_ID, this.issueId);
                HSFunnel.pushEvent(HSFunnel.OPEN_ISSUE, eventData);
            } catch (JSONException e) {
                Log.d(TAG, "JSONException", e);
            }
        }
    }

    public void onStop() {
        super.onStop();
        if (!this.selectImage) {
            HSAnalytics.onActivityStopped(this.activity);
        }
    }

    public void replyConfirmation(String messageId, Boolean accepted, final int position) {
        HSMsg msgData = (HSMsg) this.messagesList.get(position);
        msgData.clickable = Boolean.valueOf(false);
        msgData.inProgress = Boolean.valueOf(true);
        com.helpshift.models.Message.setInProgress(msgData.id, true);
        this.adapter.notifyDataSetChanged();
        Handler replySysHandler = new Handler() {
            public void handleMessage(Message msg) {
                HSMsg msgData = (HSMsg) HSMessagesFragment.this.messagesList.get(position);
                msgData.clickable = Boolean.valueOf(false);
                msgData.invisible = Boolean.valueOf(true);
                msgData.inProgress = Boolean.valueOf(false);
                HSMessagesFragment.this.adapter.notifyDataSetChanged();
                com.helpshift.models.Message.setInvisible(msgData.id, true);
                com.helpshift.models.Message.setInProgress(msgData.id, false);
                HSMessagesFragment.this.renderReplyMsg(msg);
            }
        };
        Handler replySysFailHandler = new Handler() {
            public void handleMessage(Message msg) {
                HSMsg msgData = (HSMsg) HSMessagesFragment.this.messagesList.get(position);
                msgData.clickable = Boolean.valueOf(true);
                msgData.inProgress = Boolean.valueOf(false);
                com.helpshift.models.Message.setInProgress(msgData.id, false);
                HSMessagesFragment.this.adapter.notifyDataSetChanged();
                HSErrors.showFailToast(((Integer) msg.obj.get(SettingsJsonConstants.APP_STATUS_KEY)).intValue(), null, HSMessagesFragment.this.activity);
            }
        };
        if (accepted.booleanValue()) {
            addMessage(replySysHandler, replySysFailHandler, this.issueId, BuildConfig.FLAVOR, "ca", messageId);
        } else {
            addMessage(replySysHandler, replySysFailHandler, this.issueId, BuildConfig.FLAVOR, "ncr", messageId);
        }
        try {
            JSONObject eventData = new JSONObject();
            eventData.put(DBLikedChannelsHelper.KEY_ID, this.issueId);
            if (accepted.booleanValue()) {
                HSFunnel.pushEvent(HSFunnel.RESOLUTION_ACCEPTED, eventData);
            } else {
                HSFunnel.pushEvent(HSFunnel.RESOLUTION_REJECTED, eventData);
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException", e);
        }
    }

    public void replyReview(String messageId, final int position) {
        HSMsg msgData = (HSMsg) this.messagesList.get(position);
        msgData.clickable = Boolean.valueOf(false);
        msgData.inProgress = Boolean.valueOf(true);
        com.helpshift.models.Message.setInProgress(msgData.id, true);
        this.adapter.notifyDataSetChanged();
        addMessage(new Handler() {
            public void handleMessage(Message msg) {
                HSMsg msgData = (HSMsg) HSMessagesFragment.this.messagesList.get(position);
                msgData.clickable = Boolean.valueOf(false);
                msgData.invisible = Boolean.valueOf(true);
                msgData.inProgress = Boolean.valueOf(false);
                HSMessagesFragment.this.adapter.notifyDataSetChanged();
                com.helpshift.models.Message.setInProgress(msgData.id, false);
                com.helpshift.models.Message.setInvisible(msgData.id, true);
                HSMessagesFragment.this.renderReplyMsg(msg);
                try {
                    JSONObject eventData = new JSONObject();
                    eventData.put(MessageColumns.TYPE, "conversation");
                    HSFunnel.pushEvent(HSFunnel.REVIEWED_APP, eventData);
                    if (HSMessagesFragment.this.helpshiftDelegate != null) {
                        HSMessagesFragment.this.helpshiftDelegate.userRepliedToConversation(Helpshift.HSUserReviewedTheApp);
                    }
                    String rurl = HSMessagesFragment.this.hsStorage.getConfig().optString("rurl", BuildConfig.FLAVOR).trim();
                    if (!TextUtils.isEmpty(rurl)) {
                        HSMessagesFragment.this.hsApiData.disableReview();
                        HSMessagesFragment.this.gotoApp(rurl);
                    }
                } catch (JSONException e) {
                    Log.d(HSMessagesFragment.TAG, e.getMessage(), e);
                }
            }
        }, new Handler() {
            public void handleMessage(Message msg) {
                HSMsg msgData = (HSMsg) HSMessagesFragment.this.messagesList.get(position);
                msgData.clickable = Boolean.valueOf(true);
                msgData.inProgress = Boolean.valueOf(false);
                com.helpshift.models.Message.setInProgress(msgData.id, false);
                HSMessagesFragment.this.adapter.notifyDataSetChanged();
                HSErrors.showFailToast(((Integer) msg.obj.get(SettingsJsonConstants.APP_STATUS_KEY)).intValue(), null, HSMessagesFragment.this.activity);
            }
        }, this.issueId, BuildConfig.FLAVOR, "ar", messageId);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        if (-1 != resultCode) {
            return;
        }
        String screenshotPath;
        if (requestCode == ScreenshotPreviewActivity.SCREENSHOT_PREVIEW_REQUEST_CODE) {
            HSMsg msgData;
            screenshotPath = imageReturnedIntent.getExtras().getString(ScreenshotPreviewActivity.SCREENSHOT);
            int position = imageReturnedIntent.getExtras().getInt(ScreenshotPreviewActivity.SCREENSHOT_POSITION);
            if (position == 0) {
                msgData = AttachmentUtil.addAndGetLocalRscMsg(this.hsStorage, this.issueId, screenshotPath);
                this.messagesList.add(msgData);
            } else {
                msgData = (HSMsg) this.messagesList.get(position);
                msgData.screenshot = screenshotPath;
            }
            com.helpshift.models.Message.setScreenshot(msgData.id, screenshotPath);
            this.adapter.notifyDataSetChanged();
            attachImage(this.messagesList.indexOf(msgData));
        } else if (AttachmentUtil.isImageUri(getActivity(), imageReturnedIntent)) {
            screenshotPath = AttachmentUtil.getPath(getActivity(), imageReturnedIntent);
            if (!TextUtils.isEmpty(screenshotPath)) {
                Intent screenshotPreviewIntent = new Intent(this.activity, ScreenshotPreviewActivity.class);
                screenshotPreviewIntent.putExtra(ScreenshotPreviewActivity.SCREENSHOT, screenshotPath);
                screenshotPreviewIntent.putExtra(ScreenshotPreviewActivity.SCREENSHOT_POSITION, requestCode);
                screenshotPreviewIntent.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(this.activity));
                startActivityForResult(screenshotPreviewIntent, ScreenshotPreviewActivity.SCREENSHOT_PREVIEW_REQUEST_CODE);
            }
        }
    }

    public void removeScreenshot(int position) {
        HSMsg msgData = (HSMsg) this.messagesList.get(position);
        if (msgData.id.startsWith(AttachmentUtil.LOCAL_RSC_MSG_ID_PREFIX)) {
            IssuesDataSource.deleteMessage(msgData.id);
            this.messagesList.remove(position);
        } else {
            com.helpshift.models.Message.setScreenshot(msgData.id, BuildConfig.FLAVOR);
            msgData.screenshot = BuildConfig.FLAVOR;
        }
        this.adapter.notifyDataSetChanged();
    }

    public void selectImagePopup(int position) {
        this.hsStorage.setScreenShotDraft(Boolean.valueOf(true));
        this.selectImage = true;
        Intent i = new Intent("android.intent.action.PICK", Media.EXTERNAL_CONTENT_URI);
        if (i.resolveActivity(this.activity.getPackageManager()) != null) {
            startActivityForResult(i, position);
            return;
        }
        i = new Intent("android.intent.action.GET_CONTENT");
        i.setType("image/*");
        i.putExtra("android.intent.extra.LOCAL_ONLY", true);
        if (i.resolveActivity(this.activity.getPackageManager()) != null) {
            startActivityForResult(i, position);
        }
    }

    public void attachImage(final int position) {
        String profileId = this.hsApiData.getProfileId();
        HSMsg msgData = (HSMsg) this.messagesList.get(position);
        msgData.clickable = Boolean.valueOf(false);
        msgData.inProgress = Boolean.valueOf(true);
        com.helpshift.models.Message.setInProgress(msgData.id, true);
        this.adapter.notifyDataSetChanged();
        this.hsClient.addScMessage(new Handler() {
            public void handleMessage(Message msg) {
                HSMsg msgData = (HSMsg) HSMessagesFragment.this.messagesList.get(position);
                JSONObject message = (JSONObject) msg.obj.get("response");
                try {
                    JSONObject eventData = new JSONObject();
                    eventData.put(MessageColumns.TYPE, SettingsJsonConstants.APP_URL_KEY);
                    eventData.put(MessageColumns.BODY, message.getJSONObject(MessageColumns.META).getJSONArray("attachments").getJSONObject(0).getString(SettingsJsonConstants.APP_URL_KEY));
                    eventData.put(DBLikedChannelsHelper.KEY_ID, HSMessagesFragment.this.issueId);
                    HSFunnel.pushEvent(HSFunnel.MESSAGE_ADDED, eventData);
                    if (HSMessagesFragment.this.helpshiftDelegate != null) {
                        HSMessagesFragment.this.helpshiftDelegate.userRepliedToConversation(Helpshift.HSUserSentScreenShot);
                    }
                } catch (JSONException e) {
                    Log.d(HSMessagesFragment.TAG, "Error while getting screenshot url", e);
                }
                try {
                    AttachmentUtil.copyAttachment(HSMessagesFragment.this.getActivity(), HSMessagesFragment.this.hsApiData, msgData.screenshot, msgData.id, 0);
                } catch (IOException e2) {
                    Log.d(HSMessagesFragment.TAG, "Saving uploaded screenshot", e2);
                }
                if (msgData.id.startsWith(AttachmentUtil.LOCAL_RSC_MSG_ID_PREFIX)) {
                    IssuesDataSource.deleteMessage(msgData.id);
                    HSMessagesFragment.this.messagesList.remove(position);
                } else {
                    msgData.inProgress = Boolean.valueOf(false);
                    msgData.invisible = Boolean.valueOf(true);
                    msgData.screenshot = BuildConfig.FLAVOR;
                    msgData.clickable = Boolean.valueOf(false);
                    com.helpshift.models.Message.setInProgress(msgData.id, false);
                    com.helpshift.models.Message.setInvisible(msgData.id, true);
                    com.helpshift.models.Message.setScreenshot(msgData.id, BuildConfig.FLAVOR);
                }
                HSMessagesFragment.this.adapter.notifyDataSetChanged();
                HSMessagesFragment.this.renderReplyMsg(msg);
            }
        }, new Handler() {
            public void handleMessage(Message msg) {
                HSMsg msgData = (HSMsg) HSMessagesFragment.this.messagesList.get(position);
                msgData.clickable = Boolean.valueOf(true);
                msgData.inProgress = Boolean.valueOf(false);
                com.helpshift.models.Message.setInProgress(msgData.id, false);
                HSMessagesFragment.this.adapter.notifyDataSetChanged();
                HSMessagesFragment.this.messagesList.set(position, msgData);
                HSErrors.showFailToast(((Integer) msg.obj.get(SettingsJsonConstants.APP_STATUS_KEY)).intValue(), null, HSMessagesFragment.this.activity);
            }
        }, profileId, this.issueId, BuildConfig.FLAVOR, "sc", msgData.id, msgData.screenshot);
    }

    public String downloadAttachment(String urlStr, String messageId, int attachId) throws IOException {
        URL url = new URL(urlStr);
        InputStream input = null;
        FileOutputStream output = null;
        try {
            String outputName = messageId + attachId + "-thumbnail";
            File outputFile = new File(this.activity.getFilesDir(), outputName);
            String fname = outputFile.getAbsolutePath();
            if (!outputFile.exists()) {
                this.hsApiData.storeFile(outputName);
                input = url.openConnection().getInputStream();
                output = this.activity.openFileOutput(outputName, 0);
                byte[] data = new byte[AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT];
                while (true) {
                    int read = input.read(data);
                    if (read == -1) {
                        break;
                    }
                    output.write(data, 0, read);
                }
            }
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
            return fname;
        } catch (Throwable th) {
            if (output != null) {
                output.close();
            }
            if (input != null) {
                input.close();
            }
        }
    }

    public void downloadAdminAttachment(JSONObject attachment, int position, int downloadType) {
        HSMsg msgData = (HSMsg) this.messagesList.get(position);
        switch (downloadType) {
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                msgData.state = 1;
                updateView(msgData);
                break;
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                msgData.state = 2;
                updateView(msgData);
                break;
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                if (!this.hsStorage.isDownloadActive(msgData.id)) {
                    if (new File(this.hsStorage.getFilePathForThumbnail(msgData.id)).exists()) {
                        return;
                    }
                }
                return;
                break;
        }
        DownloadManager.startDownload(attachment, position, msgData.id, this.issueId, downloadType);
    }

    public void launchAttachment(HSMsg msg) {
        try {
            String filePath;
            String contentType = new JSONObject(msg.body).optString("content-type", BuildConfig.FLAVOR);
            if (msg.type.equals(HSConsts.ADMIN_ATTACHMENT_GENERIC_TYPE)) {
                filePath = this.hsStorage.getFilePathForGenericAttachment(msg.id);
            } else {
                filePath = this.hsStorage.getFilePathForImage(msg.id);
            }
            File file = new File(filePath);
            if (file.exists()) {
                Uri uri = Uri.fromFile(file);
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setDataAndType(uri, contentType);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                    return;
                } else if (this.helpshiftDelegate != null) {
                    this.helpshiftDelegate.displayAttachmentFile(file);
                    return;
                } else {
                    HSErrors.showFailToast(4, null, this.activity);
                    return;
                }
            }
            HSErrors.showFailToast(5, null, this.activity);
        } catch (JSONException e) {
            Log.d(TAG, "launchAttachment : ", e);
        }
    }

    public void updateView(HSMsg msgData) {
        int start = this.messagesListView.getFirstVisiblePosition();
        int end = this.messagesListView.getLastVisiblePosition();
        for (int i = start; i <= end; i++) {
            if (msgData.equals(this.messagesListView.getItemAtPosition(i))) {
                this.adapter.getView(i, this.messagesListView.getChildAt(i - start), this.messagesListView);
                return;
            }
        }
    }

    @TargetApi(13)
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        menu.add(0, v.getId(), 0, "Copy");
        String copyText = ((TextView) v).getText().toString();
        if (VERSION.SDK_INT >= 13) {
            ((ClipboardManager) this.activity.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Copy Text", copyText));
        } else {
            ((android.text.ClipboardManager) this.activity.getSystemService("clipboard")).setText(copyText);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(menu.hs__messages_menu, menu);
        this.attachScreenshotMenu = menu.findItem(id.hs__attach_screenshot);
        Styles.setActionButtonIconColor(this.activity, this.attachScreenshotMenu.getIcon());
        if (!this.hsStorage.getEnableFullPrivacy().booleanValue() && this.messageBox != null && this.messageBox.getVisibility() == 0) {
            this.attachScreenshotMenu.setVisible(true);
        } else if (VERSION.SDK_INT < 11) {
            menu.removeItem(id.hs__attach_screenshot);
        } else {
            this.attachScreenshotMenu.setVisible(true);
        }
        if (this.showingNewConversationBox || this.showingConfirmationBox) {
            this.attachScreenshotMenu.setVisible(false);
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        this.attachScreenshotMenu = menu.findItem(id.hs__attach_screenshot);
        if (this.attachScreenshotMenu != null && this.hsStorage.getEnableFullPrivacy().booleanValue()) {
            this.attachScreenshotMenu.setVisible(false);
        }
    }

    public void onDownloadTaskPaused(int position, String msgId, String issueId, int downloadType) {
    }

    public void onDownloadTaskResumed(int position, String msgId, String issueId, int downloadType) {
    }

    public void onDownloadTaskComplete(String filePath, int position, String msgId, String issueId, int downloadType) {
        if (issueId.equals(this.issueId)) {
            HSMsg msgData = (HSMsg) this.messagesList.get(position);
            if (msgId.equals(msgData.id)) {
                switch (downloadType) {
                    case R.styleable.Toolbar_contentInsetEnd /*6*/:
                        msgData.state = 3;
                        break;
                    case DayPickerView.DAYS_PER_WEEK /*7*/:
                        msgData.state = 3;
                        msgData.screenshot = filePath;
                        break;
                    case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                        msgData.state = 1;
                        msgData.screenshot = filePath;
                        break;
                }
                updateView(msgData);
                if (isResumed() && downloadType != 8) {
                    launchAttachment(msgData);
                }
            }
        }
    }

    public void onDownloadTaskFailed(int position, String msgId, String issueId, int downloadType) {
        if (issueId.equals(this.issueId)) {
            HSMsg msgData = (HSMsg) this.messagesList.get(position);
            if (msgId.equals(msgData.id)) {
                switch (downloadType) {
                    case R.styleable.Toolbar_contentInsetEnd /*6*/:
                        msgData.state = 0;
                        updateView(msgData);
                        return;
                    case DayPickerView.DAYS_PER_WEEK /*7*/:
                        msgData.state = 1;
                        updateView(msgData);
                        return;
                    default:
                        return;
                }
            }
        }
    }

    public void onProgressChanged(double progress, int position, String msgId, String issueId, int downloadType) {
        if (issueId.equals(this.issueId)) {
            HSMsg msgData = (HSMsg) this.messagesList.get(position);
            if (!msgId.equals(msgData.id)) {
                return;
            }
            if (downloadType == 6) {
                msgData.state = 2;
                int start = this.messagesListView.getFirstVisiblePosition();
                int end = this.messagesListView.getLastVisiblePosition();
                for (int i = start; i <= end; i++) {
                    if (msgData.equals(this.messagesListView.getItemAtPosition(i))) {
                        View view = this.messagesListView.getChildAt(i - start);
                        ((ProgressBar) view.findViewById(16908301)).setProgress((int) progress);
                        this.adapter.getView(i, view, this.messagesListView);
                        return;
                    }
                }
            } else if (downloadType == 7 && msgData.state != 2) {
                msgData.state = 2;
                updateView(msgData);
            }
        }
    }

    public void retryMessage(final String id) {
        try {
            JSONObject failedMessage = this.hsApiData.storage.popFailedMessage(id, this.hsApiData.getProfileId());
            if (failedMessage != null) {
                JSONObject tempMess = new JSONObject(failedMessage, HSJSONUtils.getJSONObjectKeys(failedMessage));
                tempMess.put("state", 1);
                JSONArray messages = this.hsApiData.getMessagesWithFails(this.issueId);
                messages.put(tempMess);
                refreshMessages(messages);
                this.hsApiData.storage.storeFailedMessage(tempMess, this.hsApiData.getProfileId());
                Handler addMessageHandler = new Handler() {
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        try {
                            HSMessagesFragment.this.hsApiData.storage.popFailedMessage(id, HSMessagesFragment.this.hsApiData.getProfileId());
                        } catch (JSONException e) {
                            Log.d(HSMessagesFragment.TAG, "addMessageHandler", e);
                        }
                        HSMessagesFragment.this.refreshMessages();
                    }
                };
                addMessage(addMessageHandler, addMessageHandler, failedMessage.getString(MessageColumns.ISSUE_ID), failedMessage.getString(MessageColumns.BODY), failedMessage.getString(MessageColumns.TYPE), failedMessage.getString("refers"), failedMessage.optInt("state", 0) - 1);
            }
        } catch (JSONException e) {
            Log.d(TAG, "retryMessage", e);
        }
    }

    private void addMessage(Handler success, Handler failure, String issueId, String messageText, String type, String refers) {
        this.hsStorage.setScreenShotDraft(Boolean.valueOf(false));
        if (!type.equals("ar")) {
            this.hsApiData.setCSatState(issueId, HS_ISSUE_CSAT_STATE.CSAT_NOT_APPLICABLE);
        }
        this.hsApiData.addMessage(success, failure, issueId, messageText, type, refers);
    }

    private void addMessage(Handler success, Handler failure, String issueId, String messageText, String type, String refers, int failedState) {
        this.hsStorage.setScreenShotDraft(Boolean.valueOf(false));
        if (!type.equals("ar")) {
            this.hsApiData.setCSatState(issueId, HS_ISSUE_CSAT_STATE.CSAT_NOT_APPLICABLE);
        }
        this.hsApiData.addMessage(success, failure, issueId, messageText, type, refers, failedState);
    }
}
