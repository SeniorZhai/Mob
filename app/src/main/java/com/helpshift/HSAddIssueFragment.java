package com.helpshift;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Media;
import android.support.v4.app.Fragment;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.internal.NativeProtocol;
import com.helpshift.D.id;
import com.helpshift.D.layout;
import com.helpshift.D.menu;
import com.helpshift.D.string;
import com.helpshift.HSSearch.HS_SEARCH_OPTIONS;
import com.helpshift.Helpshift.HelpshiftDelegate;
import com.helpshift.app.ActionBarHelper;
import com.helpshift.constants.MessageColumns;
import com.helpshift.exceptions.IdentityException;
import com.helpshift.res.values.HSConfig;
import com.helpshift.res.values.HSConsts;
import com.helpshift.storage.IssuesDataSource;
import com.helpshift.storage.ProfilesDBHelper;
import com.helpshift.util.AttachmentUtil;
import com.helpshift.util.HSActivityUtil;
import com.helpshift.util.HSErrors;
import com.helpshift.util.HSPattern;
import com.helpshift.util.HSTransliterator;
import com.helpshift.util.IdentityFilter;
import com.helpshift.util.Meta;
import com.helpshift.util.Styles;
import com.helpshift.viewstructs.HSMsg;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HSAddIssueFragment extends Fragment {
    public static final String TAG = "HelpShiftDebug";
    ActionBarHelper actionBarHelper;
    private HSActivity activity;
    private MenuItem addIssueMenuItem;
    private MenuItem attachScreenshotMenu;
    private int callFinishRequestCode = 1;
    private ImageButton clearBtn;
    private Boolean decomp;
    private TextView desc;
    private String email;
    private EditText emailField;
    public Handler existsHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                HSAddIssueFragment.this.hsApiData.setProfileId(((JSONObject) msg.obj.get("response")).get(DBLikedChannelsHelper.KEY_ID).toString());
                HSAddIssueFragment.this.hsApiData.getIssues(new Handler() {
                    public void handleMessage(Message msg) {
                        try {
                            HSAddIssueFragment.this.hsApiData.createIssue(HSAddIssueFragment.this.reportHandler, HSAddIssueFragment.this.failureHandler, HSAddIssueFragment.this.getIssueText(), HSAddIssueFragment.this.getUserInfo());
                        } catch (IdentityException e) {
                            Log.d(HSAddIssueFragment.TAG, "Something really foul has happened", e);
                        }
                    }
                }, HSAddIssueFragment.this.failureHandler);
                HSAddIssueFragment.this.hsApiData.updateUAToken();
            } catch (JSONException e) {
                Log.d(HSAddIssueFragment.TAG, e.getMessage(), e);
            }
        }
    };
    private Bundle extras;
    private Handler failureHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HSErrors.showFailToast(((Integer) msg.obj.get(SettingsJsonConstants.APP_STATUS_KEY)).intValue(), null, HSAddIssueFragment.this.activity);
            HSAddIssueFragment.this.setIsReportingIssue(false);
        }
    };
    private Handler getLatestIssuesHandler = new Handler() {
        public void handleMessage(Message msg) {
            HSAddIssueFragment.this.clearScreenshot();
            HSAddIssueFragment.this.handleExit();
        }
    };
    private HelpshiftDelegate helpshiftDelegate;
    private HSApiClient hsApiClient;
    private HSApiData hsApiData;
    private HSStorage hsStorage;
    private String issueId;
    private HSMsg msgData;
    public Handler reportHandler = new Handler() {
        public void handleMessage(Message msg) {
            try {
                JSONObject issue = (JSONObject) msg.obj.get("response");
                HSAddIssueFragment.this.issueId = issue.getString(DBLikedChannelsHelper.KEY_ID);
                JSONArray issues = new JSONArray();
                issues.put(issue);
                HSAddIssueFragment.this.hsStorage.setIssuesTs(issue.getString(MPDbAdapter.KEY_CREATED_AT), HSAddIssueFragment.this.hsApiData.getProfileId());
                HSAddIssueFragment.this.hsStorage.storeIssues(issues, HSAddIssueFragment.this.hsApiData.getProfileId());
                HSAddIssueFragment.this.hsApiData.setUsername(HSAddIssueFragment.this.userName);
                HSAddIssueFragment.this.hsApiData.setEmail(HSAddIssueFragment.this.email);
                HSAddIssueFragment.this.hsStorage.storeReply(BuildConfig.FLAVOR, HSAddIssueFragment.this.hsApiData.getProfileId());
                HSAddIssueFragment.this.hsStorage.storeConversationDetail(BuildConfig.FLAVOR, HSAddIssueFragment.this.hsApiData.getLoginId());
                String newConversationMessage = HSAddIssueFragment.this.desc.getText().toString().trim();
                HSAddIssueFragment.this.desc.setText(BuildConfig.FLAVOR);
                HSFunnel.pushEvent(HSFunnel.CONVERSATION_POSTED);
                if (TextUtils.isEmpty(HSAddIssueFragment.this.screenshotPath)) {
                    HSAddIssueFragment.this.handleExit();
                } else {
                    HSAddIssueFragment.this.hsStorage.setForegroundIssue(HSAddIssueFragment.this.issueId);
                    HSAddIssueFragment.this.msgData = AttachmentUtil.addAndGetLocalRscMsg(HSAddIssueFragment.this.hsStorage, HSAddIssueFragment.this.issueId, HSAddIssueFragment.this.screenshotPath, true);
                    HSAddIssueFragment.this.hsApiClient.addScMessage(HSAddIssueFragment.this.uploadSuccessHandler, HSAddIssueFragment.this.uploadFailHandler, HSAddIssueFragment.this.hsApiData.getProfileId(), HSAddIssueFragment.this.issueId, BuildConfig.FLAVOR, "sc", HSAddIssueFragment.this.msgData.id, HSAddIssueFragment.this.msgData.screenshot);
                }
                HSAddIssueFragment.this.hsApiData.startInAppService();
                if (HSAddIssueFragment.this.helpshiftDelegate != null) {
                    HSAddIssueFragment.this.helpshiftDelegate.newConversationStarted(newConversationMessage);
                }
            } catch (JSONException e) {
                Log.d(HSAddIssueFragment.TAG, e.toString(), e);
            }
        }
    };
    private Boolean requireEmail;
    private ImageView screenshot;
    private String screenshotPath = null;
    private boolean searchActivityShown = false;
    private boolean selectImage;
    private boolean selectingScreenshot = false;
    private boolean sendAnyway = false;
    private Boolean showConvOnReportIssue;
    private Handler uploadFailHandler = new Handler() {
        public void handleMessage(Message msg) {
            com.helpshift.models.Message.setInProgress(HSAddIssueFragment.this.msgData.id, false);
            HSAddIssueFragment.this.clearScreenshot();
            HSAddIssueFragment.this.handleExit();
        }
    };
    private Handler uploadSuccessHandler = new Handler() {
        public void handleMessage(Message msg) {
            JSONObject message = (JSONObject) msg.obj.get("response");
            try {
                JSONObject eventData = new JSONObject();
                eventData.put(MessageColumns.TYPE, SettingsJsonConstants.APP_URL_KEY);
                eventData.put(MessageColumns.BODY, message.getJSONObject(MessageColumns.META).getJSONArray("attachments").getJSONObject(0).getString(SettingsJsonConstants.APP_URL_KEY));
                eventData.put(DBLikedChannelsHelper.KEY_ID, HSAddIssueFragment.this.issueId);
                HSFunnel.pushEvent(HSFunnel.MESSAGE_ADDED, eventData);
                if (HSAddIssueFragment.this.helpshiftDelegate != null) {
                    HSAddIssueFragment.this.helpshiftDelegate.userRepliedToConversation(Helpshift.HSUserSentScreenShot);
                }
                AttachmentUtil.copyAttachment(HSAddIssueFragment.this.getActivity(), HSAddIssueFragment.this.hsApiData, HSAddIssueFragment.this.screenshotPath, message.getJSONObject(MessageColumns.META).getString("refers"), 0);
            } catch (IOException e) {
                Log.d(HSAddIssueFragment.TAG, "Saving uploaded screenshot", e);
            } catch (JSONException e2) {
                Log.d(HSAddIssueFragment.TAG, "uploadSuccessHandler", e2);
            }
            try {
                String refers = message.getJSONObject(MessageColumns.META).getString("refers");
                if (!TextUtils.isEmpty(refers)) {
                    IssuesDataSource.deleteMessage(refers);
                }
                HSAddIssueFragment.this.hsApiData.getLatestIssues(HSAddIssueFragment.this.getLatestIssuesHandler, HSAddIssueFragment.this.getLatestIssuesHandler);
            } catch (JSONException e22) {
                Log.d(HSAddIssueFragment.TAG, "uploadSuccessHandler", e22);
            }
        }
    };
    private String userName;
    private EditText userNameField;

    private void showIssueFiledToast() {
        Toast toast = Toast.makeText(this.activity, getString(string.hs__conversation_started_message), 0);
        toast.setGravity(16, 0, 0);
        toast.show();
    }

    private void handleDecomp() {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("callFinish", true);
        getActivity().setResult(-1, returnIntent);
        getActivity().finish();
        HSActivityUtil.sessionEnding();
    }

    private void handleExit() {
        Boolean dia = (Boolean) HSConfig.configData.get("dia");
        if (this.showConvOnReportIssue.booleanValue() && !dia.booleanValue()) {
            Intent i = new Intent(this.activity, HSConversation.class);
            i.putExtra("newIssue", true);
            i.putExtra("issueId", this.issueId);
            i.putExtra("decomp", this.decomp);
            i.putExtra("showConvOnReportIssue", this.showConvOnReportIssue);
            i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(this.activity));
            i.putExtra("chatLaunchSource", HSConsts.SRC_SUPPORT);
            i.putExtra(HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION, isSearchOnNewConversationEnabled());
            if (isResumed()) {
                getActivity().startActivityForResult(i, 1);
            }
        } else if (isResumed()) {
            showIssueFiledToast();
            handleDecomp();
        }
    }

    private void clearScreenshot() {
        this.screenshot.setVisibility(8);
        this.clearBtn.setVisibility(8);
        this.screenshotPath = BuildConfig.FLAVOR;
        this.hsStorage.setConversationScreenshot(BuildConfig.FLAVOR, this.hsApiData.getLoginId());
        if (!this.hsStorage.getEnableFullPrivacy().booleanValue()) {
            this.attachScreenshotMenu.setVisible(true);
        }
    }

    private String getIssueText() {
        return this.desc.getText().toString().trim();
    }

    private HashMap getUserInfo() {
        HashMap data = null;
        if (IdentityFilter.sendNameEmail(this.hsStorage)) {
            data = new HashMap();
            data.put(ProfilesDBHelper.COLUMN_NAME, this.userName);
            if (this.email.trim().length() > 0) {
                data.put(ProfilesDBHelper.COLUMN_EMAIL, this.email);
            }
        }
        return data;
    }

    public void onPause() {
        super.onPause();
        setIsReportingIssue(false);
        String prefillText = this.hsStorage.getConversationPrefillText();
        if (TextUtils.isEmpty(this.hsStorage.getActiveConversation(this.hsApiData.getProfileId())) && TextUtils.isEmpty(prefillText)) {
            this.hsStorage.storeConversationDetail(getIssueText(), this.hsApiData.getLoginId());
        } else if (!TextUtils.isEmpty(prefillText) && this.extras.getBoolean("dropMeta")) {
            Meta.setMetadataCallback(null);
        }
        saveScreenshot(this.screenshotPath);
        this.hsStorage.setForegroundIssue(BuildConfig.FLAVOR);
    }

    public void onDestroy() {
        super.onDestroy();
        HelpshiftContext.setViewState(null);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.activity = (HSActivity) getActivity();
        this.extras = getArguments();
        this.actionBarHelper = this.activity.getActionBarHelper();
        if (Boolean.valueOf(this.extras.getBoolean("showInFullScreen")).booleanValue()) {
            this.activity.getWindow().setFlags(AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT, AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT);
        }
        this.hsApiData = new HSApiData(this.activity);
        this.hsStorage = this.hsApiData.storage;
        this.hsApiClient = this.hsApiData.client;
        this.helpshiftDelegate = Helpshift.getDelegate();
        if (isSearchOnNewConversationEnabled()) {
            this.hsApiData.getSections(new Handler() {
                public void handleMessage(Message msg) {
                    HSAddIssueFragment.this.hsApiData.loadIndex();
                    HSTransliterator.init();
                }
            }, new Handler());
        }
        this.requireEmail = Boolean.valueOf(IdentityFilter.requireEmailFromUI(this.hsStorage));
        this.decomp = Boolean.valueOf(this.extras.getBoolean("decomp", false));
        this.showConvOnReportIssue = Boolean.valueOf(this.extras.getBoolean("showConvOnReportIssue"));
        if (this.decomp.booleanValue()) {
            HSAnalytics.decomp = true;
        }
        this.searchActivityShown = false;
        setHasOptionsMenu(true);
        return inflater.inflate(layout.hs__new_conversation_fragment, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.desc = (TextView) view.findViewById(id.hs__conversationDetail);
        this.desc.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HSAddIssueFragment.this.desc.setError(null);
            }
        });
        this.userNameField = (EditText) view.findViewById(id.hs__username);
        this.userNameField.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HSAddIssueFragment.this.userNameField.setError(null);
            }
        });
        this.emailField = (EditText) view.findViewById(id.hs__email);
        this.emailField.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                HSAddIssueFragment.this.emailField.setError(null);
            }
        });
        if (this.requireEmail.booleanValue()) {
            this.emailField.setHint(getString(string.hs__email_required_hint));
        }
        if (!IdentityFilter.sendNameEmail(this.hsStorage)) {
            this.userNameField.setText("Anonymous");
        }
        if (IdentityFilter.showNameEmailForm(this.hsApiData)) {
            this.userNameField.setText(this.hsApiData.getUsername());
            this.emailField.setText(this.hsApiData.getEmail());
        } else {
            this.userNameField.setVisibility(8);
            this.emailField.setVisibility(8);
        }
        this.activity.getWindow().setSoftInputMode(4);
        this.actionBarHelper.setDisplayHomeAsUpEnabled(true);
        this.actionBarHelper.setTitle(getString(string.hs__new_conversation_header));
        this.screenshot = (ImageView) view.findViewById(id.hs__screenshot);
        this.screenshot.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                HSAddIssueFragment.this.showScreenshotPreview(HSAddIssueFragment.this.screenshotPath, 2);
            }
        });
        this.clearBtn = (ImageButton) view.findViewById(16908314);
        this.clearBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                HSAddIssueFragment.this.clearScreenshot();
            }
        });
    }

    private void pickImage() {
        this.selectImage = true;
        Intent i = new Intent("android.intent.action.PICK", Media.EXTERNAL_CONTENT_URI);
        HSConversation.setKeepActivityActive(true);
        this.selectingScreenshot = true;
        if (i.resolveActivity(this.activity.getPackageManager()) != null) {
            startActivityForResult(i, 0);
            return;
        }
        i = new Intent("android.intent.action.GET_CONTENT");
        i.setType("image/*");
        i.putExtra("android.intent.extra.LOCAL_ONLY", true);
        if (i.resolveActivity(this.activity.getPackageManager()) != null) {
            startActivityForResult(i, 0);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == id.hs__action_add_conversation) {
            if (!isFormValid()) {
                return true;
            }
            hideKeyboard(this.desc);
            if (isSearchOnNewConversationEnabled() && isSearchResultAvailable(getIssueText())) {
                showSearchOnNewConversation();
                return true;
            }
            startNewConversation();
            return true;
        } else if (id == 16908332) {
            getActivity().onBackPressed();
            return true;
        } else if (id != id.hs__attach_screenshot) {
            return super.onOptionsItemSelected(item);
        } else {
            pickImage();
            return true;
        }
    }

    private boolean isSearchOnNewConversationEnabled() {
        boolean searchPerformed = true;
        Intent intent = getActivity().getIntent();
        if (intent != null) {
            searchPerformed = intent.getBooleanExtra(HSConsts.SEARCH_PERFORMED, true);
        }
        if (searchPerformed || !this.hsStorage.getShowSearchOnNewConversation().booleanValue()) {
            return false;
        }
        return true;
    }

    private boolean isSearchResultAvailable(String issueText) {
        return this.hsApiData.localFaqSearch(issueText, HS_SEARCH_OPTIONS.KEYWORD_SEARCH).size() > 0;
    }

    private void showSearchOnNewConversation() {
        this.searchActivityShown = true;
        this.hsStorage.storeConversationDetail(getIssueText(), this.hsApiData.getLoginId());
        Intent searchResultActivity = new Intent(this.activity, SearchResultActivity.class);
        searchResultActivity.putExtra(HSConsts.SEARCH_QUERY, getIssueText());
        searchResultActivity.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(this.activity));
        HSConversation.setKeepActivityActive(true);
        startActivityForResult(searchResultActivity, HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION_REQUEST_CODE);
    }

    private void startNewConversation() {
        try {
            setIsReportingIssue(true);
            this.hsApiData.createIssue(this.reportHandler, this.failureHandler, getIssueText(), getUserInfo());
        } catch (IdentityException e) {
            this.hsApiData.registerProfile(this.existsHandler, this.failureHandler, this.userName, this.email, this.hsApiData.getLoginId());
        }
    }

    private void showScreenshotPreview(String screenshotPath, int textType) {
        HSConversation.setKeepActivityActive(true);
        this.selectingScreenshot = true;
        Intent screenshotPreviewIntent = new Intent(this.activity, ScreenshotPreviewActivity.class);
        screenshotPreviewIntent.putExtra(ScreenshotPreviewActivity.SCREENSHOT, screenshotPath);
        screenshotPreviewIntent.putExtra(ScreenshotPreviewActivity.SCREENSHOT_TEXT_TYPE, textType);
        screenshotPreviewIntent.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(this.activity));
        startActivityForResult(screenshotPreviewIntent, ScreenshotPreviewActivity.SCREENSHOT_PREVIEW_REQUEST_CODE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != -1) {
            return;
        }
        String screenshotPath;
        if (requestCode == 0) {
            if (AttachmentUtil.isImageUri(getActivity(), data)) {
                screenshotPath = AttachmentUtil.getPath(getActivity(), data);
                if (!TextUtils.isEmpty(screenshotPath)) {
                    showScreenshotPreview(screenshotPath, 1);
                }
            }
        } else if (requestCode == HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION_REQUEST_CODE) {
            HSConversation.setKeepActivityActive(false);
            String action = data.getStringExtra(NativeProtocol.WEB_DIALOG_ACTION);
            if (!TextUtils.isEmpty(action)) {
                if (action.equals("startConversation")) {
                    getActivity().getIntent().putExtra(HSConsts.SEARCH_PERFORMED, true);
                    this.sendAnyway = true;
                    startNewConversation();
                } else if (action.equals("ticketAvoided")) {
                    getActivity().finish();
                }
            }
        } else {
            HSConversation.setKeepActivityActive(false);
            screenshotPath = data.getExtras().getString(ScreenshotPreviewActivity.SCREENSHOT);
            if (TextUtils.isEmpty(screenshotPath)) {
                clearScreenshot();
                return;
            }
            saveScreenshot(screenshotPath);
            setScreenshot(screenshotPath);
        }
    }

    private void saveScreenshot(String screenshotPath) {
        if (!TextUtils.isEmpty(screenshotPath)) {
            this.hsStorage.setConversationScreenshot(screenshotPath, this.hsApiData.getLoginId());
        }
    }

    private void setScreenshot(String screenshotPath) {
        Bitmap screenshotBitmap = AttachmentUtil.getBitmap(screenshotPath, -1);
        if (screenshotBitmap != null) {
            this.screenshot.setImageBitmap(screenshotBitmap);
            this.screenshot.setVisibility(0);
            this.clearBtn.setVisibility(0);
            this.screenshotPath = screenshotPath;
            if (this.attachScreenshotMenu != null) {
                this.attachScreenshotMenu.setVisible(false);
            }
            this.desc.measure(0, 0);
            int height = this.desc.getMeasuredHeight();
            this.screenshot.getLayoutParams().height = height;
            this.screenshot.getLayoutParams().width = (int) Math.round(((double) height) * 0.6666666666666666d);
            this.screenshot.requestLayout();
        }
    }

    private boolean isFormValid() {
        Boolean validForm = Boolean.valueOf(true);
        String issueText = this.desc.getText().toString();
        Boolean isNameEmailFormShown = Boolean.valueOf(IdentityFilter.showNameEmailForm(this.hsApiData));
        if (isNameEmailFormShown.booleanValue()) {
            this.userName = this.userNameField.getText().toString();
            this.email = this.emailField.getText().toString();
        } else {
            this.userName = this.hsApiData.getUsername();
            this.email = this.hsApiData.getEmail();
        }
        if (issueText.trim().length() == 0) {
            this.desc.setError(getString(string.hs__conversation_detail_error));
            validForm = Boolean.valueOf(false);
        } else {
            Resources resources = getResources();
            if (issueText.replaceAll("\\s+", BuildConfig.FLAVOR).length() < resources.getInteger(R.integer.hs__issue_description_min_chars)) {
                this.desc.setError(resources.getString(R.string.hs__description_invalid_length_error));
                validForm = Boolean.valueOf(false);
            } else if (HSPattern.checkSpecialCharacters(issueText)) {
                this.desc.setError(getString(string.hs__invalid_description_error));
                validForm = Boolean.valueOf(false);
            }
        }
        if ((isNameEmailFormShown.booleanValue() && this.userName.trim().length() == 0) || HSPattern.checkSpecialCharacters(this.userName)) {
            this.userNameField.setError(getString(string.hs__username_blank_error));
            validForm = Boolean.valueOf(false);
        }
        if (this.requireEmail.booleanValue() && TextUtils.isEmpty(this.email) && !HSPattern.checkEmail(this.email)) {
            this.emailField.setError(getString(string.hs__invalid_email_error));
            validForm = Boolean.valueOf(false);
        } else if (!(TextUtils.isEmpty(this.email) || HSPattern.checkEmail(this.email))) {
            this.emailField.setError(getString(string.hs__invalid_email_error));
            validForm = Boolean.valueOf(false);
        }
        return validForm.booleanValue();
    }

    public void onResume() {
        super.onResume();
        HelpshiftContext.setViewState(HSConsts.ISSUE_FILING);
        if (!this.sendAnyway) {
            HSFunnel.pushEvent(HSFunnel.REPORTED_ISSUE);
        }
        String initText = BuildConfig.FLAVOR;
        String storedText = this.hsStorage.getConversationDetail(this.hsApiData.getLoginId());
        String prefillText = this.hsStorage.getConversationPrefillText();
        if (this.extras != null) {
            String input = this.extras.getString(SettingsJsonConstants.PROMPT_MESSAGE_KEY);
            if (!(input == null || input.trim().equals(BuildConfig.FLAVOR))) {
                initText = input.substring(0, 1).toUpperCase() + input.substring(1);
            }
        }
        if (!this.selectingScreenshot) {
            if (this.searchActivityShown) {
                this.desc.setText(storedText);
            } else if (!TextUtils.isEmpty(prefillText)) {
                this.desc.setText(prefillText);
            } else if (TextUtils.isEmpty(initText)) {
                this.desc.setText(storedText);
            } else {
                this.desc.setText(initText);
            }
            this.selectingScreenshot = false;
        }
        this.sendAnyway = false;
        this.searchActivityShown = false;
        this.desc.requestFocus();
        setScreenshot(this.hsStorage.getConversationScreenshot(this.hsApiData.getLoginId()));
    }

    private void hideKeyboard(View view) {
        ((InputMethodManager) this.activity.getSystemService("input_method")).hideSoftInputFromWindow(view.getWindowToken(), 2);
    }

    private void setIsReportingIssue(boolean isReportingIssue) {
        boolean z;
        this.activity.setSupportProgressBarIndeterminateVisibility(isReportingIssue);
        if (this.addIssueMenuItem != null) {
            this.addIssueMenuItem.setVisible(!isReportingIssue);
        }
        if (this.clearBtn != null) {
            ImageButton imageButton = this.clearBtn;
            if (isReportingIssue) {
                z = false;
            } else {
                z = true;
            }
            imageButton.setEnabled(z);
        }
        if (this.screenshot != null) {
            ImageView imageView = this.screenshot;
            if (isReportingIssue) {
                z = false;
            } else {
                z = true;
            }
            imageView.setEnabled(z);
        }
        if (this.attachScreenshotMenu == null) {
            return;
        }
        if (isReportingIssue || (this.clearBtn != null && this.clearBtn.getVisibility() == 0)) {
            this.attachScreenshotMenu.setVisible(false);
        } else if (!this.hsStorage.getEnableFullPrivacy().booleanValue()) {
            this.attachScreenshotMenu.setVisible(true);
        }
    }

    public void onStart() {
        super.onStart();
        if (!this.selectImage) {
            HSAnalytics.onActivityStarted(this.activity);
        }
        this.selectImage = false;
    }

    public void onStop() {
        super.onStop();
        if (!this.selectImage) {
            HSAnalytics.onActivityStopped(this.activity);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(menu.hs__add_conversation_menu, menu);
        this.actionBarHelper.setupIndeterminateProgressBar(menu, inflater);
        this.addIssueMenuItem = menu.findItem(id.hs__action_add_conversation);
        Styles.setActionButtonIconColor(this.activity, this.addIssueMenuItem.getIcon());
        this.attachScreenshotMenu = menu.findItem(id.hs__attach_screenshot);
        Styles.setActionButtonIconColor(this.activity, this.attachScreenshotMenu.getIcon());
        if (VERSION.SDK_INT < 11 && this.hsStorage.getEnableFullPrivacy().booleanValue()) {
            menu.removeItem(id.hs__attach_screenshot);
        }
        setIsReportingIssue(false);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        this.attachScreenshotMenu = menu.findItem(id.hs__attach_screenshot);
        if (this.attachScreenshotMenu != null && this.hsStorage.getEnableFullPrivacy().booleanValue()) {
            this.attachScreenshotMenu.setVisible(false);
        }
    }
}
