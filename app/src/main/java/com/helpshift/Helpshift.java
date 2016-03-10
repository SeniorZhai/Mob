package com.helpshift;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import com.helpshift.constants.MessageColumns;
import com.helpshift.models.Issue;
import com.helpshift.res.drawable.HSImages;
import com.helpshift.res.values.HSConfig;
import com.helpshift.res.values.HSConsts;
import com.helpshift.storage.IssuesDataSource;
import com.helpshift.storage.ProfilesDBHelper;
import com.helpshift.util.DBUtil;
import com.helpshift.util.HSActivityUtil;
import com.helpshift.util.HSNotification;
import com.helpshift.util.HSPattern;
import com.helpshift.util.Meta;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.AbstractSpiCall;
import java.io.File;
import java.util.HashMap;
import org.json.JSONException;
import org.json.JSONObject;

public final class Helpshift {
    public static final String HSCustomMetadataKey = "hs-custom-metadata";
    public static final String HSIssueTagsKey = "hs-tags";
    public static final String HSTagsKey = "hs-tags";
    public static final String HSUserAcceptedTheSolution = "User accepted the solution";
    public static final String HSUserRejectedTheSolution = "User rejected the solution";
    public static final String HSUserReviewedTheApp = "User reviewed the app";
    public static final String HSUserSentScreenShot = "User sent a screenshot";
    public static final String JSON_PREFS = "HSJsonData";
    public static final String TAG = "HelpShiftDebug";
    private static Context c = null;
    private static HSApiData data = null;
    private static HelpshiftDelegate delegate = null;
    public static final String libraryVersion = "3.10.0";
    private static HSStorage storage = null;

    public enum ENABLE_CONTACT_US {
        ALWAYS,
        NEVER,
        AFTER_VIEWING_FAQS
    }

    public enum HS_RATE_ALERT {
        SUCCESS,
        FEEDBACK,
        CLOSE,
        FAIL
    }

    public interface HelpshiftDelegate {
        void didReceiveNotification(int i);

        void displayAttachmentFile(File file);

        void helpshiftSessionBegan();

        void helpshiftSessionEnded();

        void newConversationStarted(String str);

        void userCompletedCustomerSatisfactionSurvey(int i, String str);

        void userRepliedToConversation(String str);
    }

    private Helpshift() {
    }

    private static void init(Application application) {
        initialize(application.getApplicationContext());
    }

    private static void init(Context context) {
        initialize(context.getApplicationContext());
    }

    private static void initialize(Context context) {
        HelpshiftContext.setApplicationContext(context);
        if (c == null) {
            data = new HSApiData(context);
            storage = data.storage;
            ContactUsFilter.init(context);
            Initializer.init(context);
            c = context;
        }
    }

    private static void cleanStorage() {
        String identity = storage.getIdentity();
        String uuid = storage.getUUID();
        Boolean requireEmail = storage.getRequireEmail();
        Boolean fullPrivacy = storage.getEnableFullPrivacy();
        Boolean hideNameEmail = storage.getHideNameAndEmail();
        Boolean showSearchOnNewConversation = storage.getShowSearchOnNewConversation();
        JSONObject metaData = storage.getCustomMetaData();
        Float timeDelta = storage.getServerTimeDelta();
        String oldVersion = storage.getLibraryVersion();
        if (oldVersion.length() > 0 && !oldVersion.equals(libraryVersion)) {
            storage.clearDatabase();
            storage.setIdentity(identity);
            if (!TextUtils.isEmpty(uuid)) {
                storage.setUUID(uuid);
            }
            storage.setRequireEmail(requireEmail);
            storage.setEnableFullPrivacy(fullPrivacy);
            storage.setHideNameAndEmail(hideNameEmail);
            storage.setShowSearchOnNewConversation(showSearchOnNewConversation);
            storage.setCustomMetaData(metaData);
            storage.setServerTimeDelta(timeDelta);
        }
        storage.setLibraryVersion(libraryVersion);
    }

    public static void install(Application application, String apiKey, String domain, String appId) {
        install(application, apiKey, domain, appId, new HashMap());
    }

    @TargetApi(14)
    public static void install(Application application, String apiKey, String domain, String appId, HashMap config) {
        init(application);
        cleanStorage();
        DBUtil.restoreDatabaseBackup(ProfilesDBHelper.DATABASE_NAME);
        if (config == null) {
            config = new HashMap();
        }
        String profileId = data.getProfileId();
        if (((String) config.get("sdkType")) != null) {
            storage.setSdkType((String) config.get("sdkType"));
        } else {
            storage.setSdkType(AbstractSpiCall.ANDROID_CLIENT_TYPE);
        }
        String notifIcon = config.get("notificationIcon");
        if (notifIcon != null && (notifIcon instanceof String)) {
            HashMap hashMap = config;
            hashMap.put("notificationIcon", Integer.valueOf(application.getResources().getIdentifier(notifIcon, "drawable", application.getPackageName())));
        }
        Object notifSound = config.get("notificationSound");
        if (notifSound != null && (notifSound instanceof String)) {
            hashMap = config;
            hashMap.put("notificationSound", Integer.valueOf(application.getResources().getIdentifier((String) notifSound, "raw", application.getPackageName())));
        }
        Boolean enableDialogUIForTablets = config.get("enableDialogUIForTablets");
        if (enableDialogUIForTablets != null && (enableDialogUIForTablets instanceof Boolean)) {
            hashMap = config;
            hashMap.put("enableDialogUIForTablets", enableDialogUIForTablets);
        }
        storage.updateDisableHelpshiftBranding();
        try {
            String applicationVersion = c.getPackageManager().getPackageInfo(c.getPackageName(), 0).versionName;
            if (!storage.getApplicationVersion().equals(applicationVersion)) {
                data.resetReviewCounter();
                data.enableReview();
                storage.setApplicationVersion(applicationVersion);
            }
        } catch (NameNotFoundException e) {
            Log.d(TAG, "Application Name Not Found", e);
        }
        HSImages.initImages(c);
        storage.setAppConfig(new JSONObject(config));
        storage.setActiveDownloads(new JSONObject());
        data.install(apiKey, domain, appId);
        if (!TextUtils.isEmpty(profileId)) {
            try {
                data.getLatestIssues(new Handler(), new Handler());
            } catch (JSONException e2) {
                Log.d(TAG, "Install - Get Latest Issues", e2);
            }
        }
        if (VERSION.SDK_INT >= 14) {
            HSLifecycleCallbacks hsLifecycleCallbacks = HSLifecycleCallbacks.getInstance();
            application.unregisterActivityLifecycleCallbacks(hsLifecycleCallbacks);
            application.registerActivityLifecycleCallbacks(hsLifecycleCallbacks);
            return;
        }
        data.updateReviewCounter();
        if (data.showReviewP().booleanValue()) {
            Intent i = new Intent(c, HSReview.class);
            i.setFlags(268435456);
            c.startActivity(i);
        }
        try {
            data.getConfig(new Handler() {
                public void handleMessage(Message msg) {
                    HSConfig.updateConfig((JSONObject) msg.obj);
                    Helpshift.storage.updateActiveConversation(Helpshift.data.getProfileId());
                }
            }, new Handler());
        } catch (JSONException e22) {
            Log.d(TAG, e22.toString(), e22);
        }
        if (HelpshiftConnectionUtil.isOnline(c)) {
            c.startService(new Intent(c, HSRetryService.class));
        }
        data.startInAppService();
        data.reportAppStartEvent();
    }

    public static Integer getNotificationCount() {
        if (data != null) {
            return data.storage.getActiveNotifCnt(data.getProfileId());
        }
        return Integer.valueOf(0);
    }

    public static void getNotificationCount(Handler success, final Handler failure) {
        if (success != null) {
            if (data == null || storage == null) {
                if (HelpshiftContext.getApplicationContext() != null) {
                    init(HelpshiftContext.getApplicationContext());
                } else {
                    return;
                }
            }
            Integer activeCnt = storage.getActiveNotifCnt(data.getProfileId());
            Message msgToPost = success.obtainMessage();
            Bundle countData = new Bundle();
            countData.putInt("value", activeCnt.intValue());
            countData.putBoolean("cache", true);
            msgToPost.obj = countData;
            success.sendMessage(msgToPost);
            Handler localFailure = new Handler() {
                public void handleMessage(Message msg) {
                    if (failure != null) {
                        Message msgToPost = failure.obtainMessage();
                        msgToPost.obj = msg.obj;
                        failure.sendMessage(msgToPost);
                    }
                }
            };
            if (TextUtils.isEmpty(data.getProfileId())) {
                Message failureMsg = localFailure.obtainMessage();
                Bundle failureCount = new Bundle();
                failureCount.putInt("value", -1);
                failureMsg.obj = failureCount;
                localFailure.sendMessage(failureMsg);
                return;
            }
            data.getNotificationCount(success, localFailure);
        }
    }

    public static void setNameAndEmail(String name, String email) {
        if (name == null) {
            name = BuildConfig.FLAVOR;
        } else {
            name = name.trim();
        }
        if (email == null) {
            email = BuildConfig.FLAVOR;
        } else {
            email = email.trim();
        }
        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(email)) {
            data.setUsername(BuildConfig.FLAVOR);
            data.setEmail(BuildConfig.FLAVOR);
        }
        if (!(TextUtils.isEmpty(name) || HSPattern.checkSpecialCharacters(name))) {
            data.setUsername(name);
        }
        if (!TextUtils.isEmpty(email) && HSPattern.checkEmail(email)) {
            data.setEmail(email);
        }
    }

    public static void setUserIdentifier(String userIdentifier) {
        if (userIdentifier != null) {
            storage.setDeviceIdentifier(userIdentifier.trim());
        }
    }

    public static void registerDeviceToken(Context context, String deviceToken) {
        init(context);
        if (deviceToken != null) {
            String profileId = data.getProfileId();
            storage.setDeviceToken(deviceToken);
            if (!TextUtils.isEmpty(profileId)) {
                data.updateUAToken();
                return;
            }
            return;
        }
        Log.d(TAG, "Device Token is null");
    }

    public static void leaveBreadCrumb(String breadCrumb) {
        if (breadCrumb != null && !TextUtils.isEmpty(breadCrumb.trim())) {
            storage.pushBreadCrumb(breadCrumb);
        }
    }

    public static void clearBreadCrumbs() {
        storage.clearBreadCrumbs();
    }

    public static void showConversation(Activity a) {
        showConversation(a, new HashMap());
    }

    public static void showConversation(Activity a, HashMap config) {
        Intent i = new Intent(a, HSConversation.class);
        i.putExtra("decomp", true);
        i.putExtras(cleanConfig(config));
        i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(a));
        i.putExtra("chatLaunchSource", HSConsts.SRC_SUPPORT);
        i.putExtra("isRoot", true);
        i.putExtra(HSConsts.SEARCH_PERFORMED, false);
        HSActivityUtil.sessionBeginning();
        a.startActivity(i);
    }

    public static void showFAQSection(Activity a, String sectionPublishId) {
        showFAQSection(a, sectionPublishId, new HashMap());
    }

    public static void showFAQSection(Activity a, String sectionPublishId, HashMap config) {
        Intent i = new Intent(a, HSSection.class);
        i.putExtras(cleanConfig(removeFAQFlowUnsupportedConfigs(config)));
        i.putExtra("sectionPublishId", sectionPublishId);
        i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(a));
        i.putExtra("decomp", true);
        i.putExtra("isRoot", true);
        HSActivityUtil.sessionBeginning();
        a.startActivity(i);
    }

    public static void showSingleFAQ(Activity a, String questionPublishId) {
        showSingleFAQ(a, questionPublishId, new HashMap());
    }

    public static void showSingleFAQ(Activity a, String questionPublishId, HashMap config) {
        Intent i = new Intent(a, HSQuestion.class);
        i.putExtras(cleanConfig(removeFAQFlowUnsupportedConfigs(config)));
        i.putExtra("questionPublishId", questionPublishId);
        i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(a));
        i.putExtra("decomp", true);
        i.putExtra("isRoot", true);
        HSActivityUtil.sessionBeginning();
        a.startActivity(i);
    }

    public static void setMetadataCallback(HSCallable f) {
        Meta.setMetadataCallback(f);
        try {
            storage.setCustomMetaData(Meta.getCustomMeta());
        } catch (JSONException e) {
            Log.d(TAG, "Exception getting custom meta ", e);
        }
    }

    private static void createMetadataCallback(final HashMap config) {
        if (config.containsKey(HSCustomMetadataKey)) {
            setMetadataCallback(new HSCallable() {
                public HashMap call() {
                    if (config.get(Helpshift.HSCustomMetadataKey) instanceof HashMap) {
                        return (HashMap) config.get(Helpshift.HSCustomMetadataKey);
                    }
                    return null;
                }
            });
        }
    }

    public static void showFAQs(Activity a) {
        showFAQs(a, new HashMap());
    }

    public static void showFAQs(Activity a, HashMap config) {
        Intent i = new Intent(a, HSFaqs.class);
        i.putExtras(cleanConfig(removeFAQFlowUnsupportedConfigs(config)));
        i.putExtra("showInFullScreen", HSActivityUtil.isFullScreen(a));
        i.putExtra("decomp", false);
        i.putExtra("isRoot", true);
        HSActivityUtil.sessionBeginning();
        a.startActivity(i);
    }

    private static HashMap removeFAQFlowUnsupportedConfigs(HashMap config) {
        if (config == null) {
            config = new HashMap();
        }
        for (String s : new String[]{"conversationPrefillText"}) {
            config.remove(s);
            if (s.equals(HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION)) {
                storage.setShowSearchOnNewConversation(Boolean.valueOf(false));
            }
        }
        return config;
    }

    private static Bundle cleanConfig(HashMap configMap) {
        boolean z = true;
        ContactUsFilter.setConfig(configMap);
        Bundle cleanConfig = new Bundle();
        if (configMap != null) {
            createMetadataCallback(configMap);
            JSONObject config = new JSONObject(configMap);
            if (!(config.optBoolean("gotoCoversationAfterContactUs", false) || config.optBoolean("gotoConversationAfterContactUs", false))) {
                z = false;
            }
            Boolean showConvOnReportIssue = Boolean.valueOf(z);
            try {
                if (config.has("requireEmail")) {
                    storage.setRequireEmail(Boolean.valueOf(config.getBoolean("requireEmail")));
                }
                if (config.has("hideNameAndEmail")) {
                    storage.setHideNameAndEmail(Boolean.valueOf(config.getBoolean("hideNameAndEmail")));
                }
                if (config.has(HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION)) {
                    storage.setShowSearchOnNewConversation(Boolean.valueOf(config.getBoolean(HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION)));
                }
                if (config.has("enableFullPrivacy")) {
                    storage.setEnableFullPrivacy(Boolean.valueOf(config.getBoolean("enableFullPrivacy")));
                }
            } catch (JSONException e) {
                Log.d(TAG, "Exception parsing config : " + e);
            }
            storage.setConversationPrefillText(null);
            try {
                if (config.has("conversationPrefillText") && !config.getString("conversationPrefillText").equals("null")) {
                    if (config.has(HSCustomMetadataKey)) {
                        cleanConfig.putBoolean("dropMeta", true);
                    }
                    String prefillText = config.getString("conversationPrefillText").trim();
                    if (!TextUtils.isEmpty(prefillText)) {
                        storage.setConversationPrefillText(prefillText);
                    }
                }
            } catch (JSONException e2) {
                Log.d(TAG, "JSON exception while parsing config : ", e2);
            }
            cleanConfig.putBoolean("showConvOnReportIssue", showConvOnReportIssue.booleanValue());
            cleanConfig.putBoolean(HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION, config.optBoolean(HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION, false));
        }
        return cleanConfig;
    }

    public static void handlePush(Context context, Intent i) {
        init(context);
        String issueId = i.getExtras().getString(MessageColumns.ISSUE_ID);
        if (!storage.getForegroundIssue().equals(issueId)) {
            try {
                int messCnt = storage.getIssuePushCount(issueId);
                Issue issue = IssuesDataSource.getIssue(issueId);
                if (issue != null) {
                    HSNotification.showNotif(c, issue, messCnt, HSConsts.SRC_PUSH, i);
                }
            } catch (JSONException e) {
                Log.d(TAG, "handlePush JSONException", e);
            }
        }
    }

    public static void showAlertToRateApp(String url, HSAlertToRateAppListener alertToRateAppListener) {
        Intent intent = new Intent("android.intent.action.VIEW");
        if (!TextUtils.isEmpty(url)) {
            intent.setData(Uri.parse(url.trim()));
        }
        if (!TextUtils.isEmpty(url) && intent.resolveActivity(c.getPackageManager()) != null) {
            HSReviewFragment.setAlertToRateAppListener(alertToRateAppListener);
            Intent i = new Intent(c, HSReview.class);
            i.putExtra("disableReview", false);
            i.putExtra("rurl", url.trim());
            i.setFlags(268435456);
            c.startActivity(i);
        } else if (alertToRateAppListener != null) {
            alertToRateAppListener.onAction(HS_RATE_ALERT.FAIL);
        }
    }

    public static void setDelegate(HelpshiftDelegate delegate) {
        delegate = delegate;
    }

    public static HelpshiftDelegate getDelegate() {
        return delegate;
    }

    public static void login(String identifier, String name, String email) {
        if (data.login(identifier)) {
            setNameAndEmail(name, email);
        }
    }

    public static void logout() {
        data.logout();
    }

    public static void setSDKLanguage(String locale) {
        storage.setSdkLanguage(locale);
    }
}
