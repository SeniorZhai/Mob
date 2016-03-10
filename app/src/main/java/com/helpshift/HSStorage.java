package com.helpshift;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.GraphResponse;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.ServerProtocol;
import com.helpshift.constants.MessageColumns;
import com.helpshift.constants.Tables;
import com.helpshift.models.Issue;
import com.helpshift.res.values.HSConfig;
import com.helpshift.res.values.HSConsts;
import com.helpshift.storage.IssuesDataSource;
import com.helpshift.storage.ProfilesDBHelper;
import com.helpshift.util.HSFormat;
import com.helpshift.util.IssuesUtil;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import com.mobcrush.mobcrush.network.Network;
import io.fabric.sdk.android.BuildConfig;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class HSStorage {
    static final String TAG = "HelpShiftDebug";
    private static HashMap cacheIndex;
    private Context context;
    private final String dbFile = "fullIndex.db";
    private SharedPreferences storage;

    public HSStorage(Context c) {
        this.context = c;
        this.storage = c.getSharedPreferences(Helpshift.JSON_PREFS, 0);
    }

    private JSONArray concatJSONArray(JSONArray arr1, JSONArray arr2) throws JSONException {
        int i;
        JSONArray result = new JSONArray();
        for (i = 0; i < arr1.length(); i++) {
            result.put(arr1.get(i));
        }
        for (i = 0; i < arr2.length(); i++) {
            result.put(arr2.get(i));
        }
        return result;
    }

    private JSONObject storageGetObj(String key) throws JSONException {
        return new JSONObject(this.storage.getString(key, "{}"));
    }

    private JSONArray storageGetArr(String key) throws JSONException {
        return new JSONArray(this.storage.getString(key, "[]"));
    }

    private String storageGet(String key) {
        return this.storage.getString(key, BuildConfig.FLAVOR);
    }

    private Integer storageGetInt(String key) {
        return Integer.valueOf(this.storage.getInt(key, 0));
    }

    private Float storageGetFloat(String key) {
        return Float.valueOf(this.storage.getFloat(key, 0.0f));
    }

    private Boolean storageGetBoolean(String key) {
        return Boolean.valueOf(this.storage.getBoolean(key, false));
    }

    private void storageSet(String key, JSONArray data) {
        Editor editor = this.storage.edit();
        editor.putString(key, data.toString());
        editor.commit();
    }

    private void storageSet(String key, JSONObject data) {
        Editor editor = this.storage.edit();
        editor.putString(key, data.toString());
        editor.commit();
    }

    private void storageSet(String key, String data) {
        Editor editor = this.storage.edit();
        editor.putString(key, data);
        editor.commit();
    }

    private void storageSet(String key, Integer data) {
        Editor editor = this.storage.edit();
        editor.putInt(key, data.intValue());
        editor.commit();
    }

    private void storageSet(String key, Float data) {
        Editor editor = this.storage.edit();
        editor.putFloat(key, data.floatValue());
        editor.commit();
    }

    private void storageSet(String key, Boolean data) {
        Editor editor = this.storage.edit();
        editor.putBoolean(key, data.booleanValue());
        editor.commit();
    }

    protected JSONArray mergeMessages(JSONArray messages1, JSONArray messages2) throws JSONException {
        int i;
        HashMap messagesMap = new HashMap();
        JSONArray messagesList = new JSONArray();
        ArrayList<HashMap> messagesMapList = new ArrayList();
        JSONArray messages = concatJSONArray(messages1, messages2);
        for (i = 0; i < messages.length(); i++) {
            JSONObject message = messages.getJSONObject(i);
            if (!messagesMap.containsKey(message.get(DBLikedChannelsHelper.KEY_ID))) {
                messagesMap.put(message.get(DBLikedChannelsHelper.KEY_ID).toString(), message);
                HashMap messageMap = new HashMap();
                messageMap.put(DBLikedChannelsHelper.KEY_ID, message.get(DBLikedChannelsHelper.KEY_ID));
                messageMap.put(MPDbAdapter.KEY_CREATED_AT, message.get(MPDbAdapter.KEY_CREATED_AT));
                messagesMapList.add(messageMap);
            }
        }
        Collections.sort(messagesMapList, new Comparator<HashMap>() {
            public int compare(HashMap a, HashMap b) {
                try {
                    return HSFormat.issueTsFormat.parse(a.get(MPDbAdapter.KEY_CREATED_AT).toString()).compareTo(HSFormat.issueTsFormat.parse(b.get(MPDbAdapter.KEY_CREATED_AT).toString()));
                } catch (Exception e) {
                    return 0;
                }
            }
        });
        for (i = 0; i < messagesMapList.size(); i++) {
            messagesList.put(messagesMap.get(((HashMap) messagesMapList.get(i)).get(DBLikedChannelsHelper.KEY_ID)));
        }
        return messagesList;
    }

    protected JSONObject addMessages(JSONObject issue, JSONArray messages) throws JSONException {
        issue.put(Tables.MESSAGES, mergeMessages(messages, issue.getJSONArray(Tables.MESSAGES)));
        return issue;
    }

    protected void storeIssues(JSONArray issues, String profileId) {
        IssuesDataSource.storeIssues(IssuesUtil.jsonArrayToIssueList(profileId, issues));
        updateOpenConversations(profileId);
        updateActiveConversation(profileId);
    }

    private void updateOpenConversations(String profileId) {
        List<Issue> issueList = IssuesDataSource.getIssues(profileId);
        List<String> openConversations = getOpenConversations(profileId);
        for (Issue issue : issueList) {
            String issueId = issue.getIssueId();
            Integer status = Integer.valueOf(issue.getStatus());
            if (!openConversations.contains(issueId) && (status.equals(Integer.valueOf(0)) || status.equals(Integer.valueOf(1)))) {
                openConversations.add(issueId);
            }
            setOpenConversations(openConversations, profileId);
        }
    }

    protected void updateActiveConversation(String profileId) {
        Boolean dia = (Boolean) HSConfig.configData.get("dia");
        setActiveConversation(BuildConfig.FLAVOR, profileId);
        if (!dia.booleanValue()) {
            for (Issue issue : IssuesDataSource.getIssues(profileId)) {
                String issueId = issue.getIssueId();
                Integer status = Integer.valueOf(issue.getStatus());
                List openConversations = getOpenConversations(profileId);
                if (status.equals(Integer.valueOf(0)) || status.equals(Integer.valueOf(1)) || (status.equals(Integer.valueOf(2)) && openConversations.contains(issueId))) {
                    setActiveConversation(issueId, profileId);
                    return;
                } else if (status.equals(Integer.valueOf(3)) && issueId.equals(getArchivedConversation(profileId))) {
                    setArchivedConversation(BuildConfig.FLAVOR, profileId);
                }
            }
        }
    }

    protected void clearAndUpdateActiveConversation(String issueId, String profileId) {
        List openConvs = getOpenConversations(profileId);
        if (openConvs.contains(issueId)) {
            openConvs.remove(openConvs.indexOf(issueId));
            setOpenConversations(openConvs, profileId);
        }
        setArchivedConversation(issueId, profileId);
        updateActiveConversation(profileId);
    }

    protected HashMap getIssuesTs(String profileId) {
        HashMap result = new HashMap();
        String issuesTs = BuildConfig.FLAVOR;
        try {
            issuesTs = storageGetObj("issuesTs").optString(profileId, BuildConfig.FLAVOR);
        } catch (JSONException e) {
            Log.d(TAG, "getIssuesTs", e);
        }
        if (!TextUtils.isEmpty(issuesTs)) {
            result.put("ts", issuesTs);
            result.put(GraphResponse.SUCCESS_KEY, Boolean.valueOf(true));
        }
        return result;
    }

    protected void setIssuesTs(String dateStr, String profileId) {
        try {
            JSONObject allIssuesTs = storageGetObj("issuesTs");
            allIssuesTs.put(profileId, dateStr);
            storageSet("issuesTs", allIssuesTs);
        } catch (JSONException e) {
            Log.d(TAG, "setIssuesTs", e);
        }
    }

    protected void setIdentity(String identity) {
        storageSet(HTTP.IDENTITY_CODING, identity);
    }

    protected String getIdentity() {
        return storageGet(HTTP.IDENTITY_CODING);
    }

    protected Integer getActiveNotifCnt(String profileId) {
        Integer activeNotifCnt = Integer.valueOf(0);
        String issueId = getActiveConversation(profileId);
        if (TextUtils.isEmpty(issueId)) {
            return activeNotifCnt;
        }
        return Integer.valueOf(IssuesDataSource.getIssue(issueId).getNewMessagesCount());
    }

    protected void resetIssueCount(String issueId) throws JSONException {
        Issue.resetIssueCount(issueId);
        JSONObject pushData = storageGetObj("pushData");
        pushData.put(issueId, 0);
        storageSet("pushData", pushData);
    }

    protected void clearDatabase() {
        Editor editor = this.storage.edit();
        editor.clear();
        editor.commit();
    }

    protected String getApiKey() {
        return storageGet("apiKey");
    }

    protected String getDomain() {
        return storageGet("domain");
    }

    protected String getAppId() {
        return storageGet("appId");
    }

    public String getEmail() {
        return storageGet(ProfilesDBHelper.COLUMN_EMAIL);
    }

    public String getUsername() {
        return storageGet(ChatMessage.USERNAME);
    }

    protected String getDeviceIdentifier() {
        String loginIdentifier = getLoginIdentifier();
        if (TextUtils.isEmpty(loginIdentifier)) {
            return storageGet("deviceId");
        }
        return loginIdentifier;
    }

    public String getDeviceToken() {
        return storageGet("deviceToken");
    }

    public String getForegroundIssue() {
        return storageGet("foregroundIssue");
    }

    protected String getLibraryVersion() {
        return storageGet("libraryVersion");
    }

    protected String getApplicationVersion() {
        return storageGet("applicationVersion");
    }

    protected String getSdkType() {
        return storageGet("sdkType");
    }

    protected String getActiveConversation(String profileId) {
        String activeConversation = null;
        try {
            activeConversation = storageGetObj("activeConversation").optString(profileId, null);
        } catch (JSONException e) {
            Log.d(TAG, "getActiveConversation", e);
        }
        return activeConversation;
    }

    protected List<String> getOpenConversations(String profileId) {
        ArrayList<String> list = new ArrayList();
        try {
            JSONArray openConversations = storageGetObj("openConversations").optJSONArray(profileId);
            if (openConversations != null) {
                int len = openConversations.length();
                for (int i = 0; i < len; i++) {
                    list.add(openConversations.getString(i));
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "getOpenConversations", e);
        }
        return list;
    }

    protected String getArchivedConversation(String profileId) {
        String archivedConversation = null;
        try {
            archivedConversation = storageGetObj("archivedConversations").optString(profileId, null);
        } catch (JSONException e) {
            Log.d(TAG, "getArchivedConversation", e);
        }
        return archivedConversation;
    }

    protected int getIssuePushCount(String issueId) throws JSONException {
        int updatedCount = 1;
        JSONObject pushData = storageGetObj("pushData");
        if (pushData.has(issueId)) {
            updatedCount = pushData.getInt(issueId) + 1;
        }
        pushData.put(issueId, updatedCount);
        storageSet("pushData", pushData);
        return updatedCount;
    }

    protected void setApiKey(String apiKey) {
        storageSet("apiKey", apiKey);
    }

    protected void setDomain(String domain) {
        storageSet("domain", domain);
    }

    protected void setAppId(String appId) {
        storageSet("appId", appId);
    }

    protected void setEmail(String email) {
        storageSet(ProfilesDBHelper.COLUMN_EMAIL, email);
    }

    protected void setUsername(String username) {
        storageSet(ChatMessage.USERNAME, username);
    }

    protected void setDeviceIdentifier(String deviceId) {
        storageSet("deviceId", deviceId);
    }

    protected void setDeviceToken(String deviceToken) {
        storageSet("deviceToken", deviceToken);
    }

    protected void setForeground(Boolean state) {
        storageSet("foreground", state);
    }

    protected void setForegroundIssue(String issueId) {
        storageSet("foregroundIssue", issueId);
    }

    protected void setLibraryVersion(String libraryVersion) {
        storageSet("libraryVersion", libraryVersion);
    }

    protected void setApplicationVersion(String applicationVersion) {
        storageSet("applicationVersion", applicationVersion);
    }

    protected void setSdkType(String sdkType) {
        storageSet("sdkType", sdkType);
    }

    protected void setActiveConversation(String conversationId, String profileId) {
        try {
            JSONObject allActiveConversations = storageGetObj("activeConversation");
            allActiveConversations.put(profileId, conversationId);
            storageSet("activeConversation", allActiveConversations);
        } catch (JSONException e) {
            Log.d(TAG, "setActiveConversation", e);
        }
    }

    protected void setOpenConversations(List openConversationsList, String profileId) {
        JSONArray openConversations = new JSONArray(openConversationsList);
        try {
            JSONObject allOpenConversations = storageGetObj("openConversations");
            allOpenConversations.put(profileId, openConversations);
            storageSet("openConversations", allOpenConversations);
        } catch (JSONException e) {
            Log.d(TAG, "setOpenConversations", e);
        }
    }

    protected void setArchivedConversation(String archivedConversationId, String profileId) {
        try {
            JSONObject allArchivedConversations = storageGetObj("archivedConversations");
            allArchivedConversations.put(profileId, archivedConversationId);
            storageSet("archivedConversations", allArchivedConversations);
        } catch (JSONException e) {
            Log.d(TAG, "setArchivedConversation", e);
        }
    }

    public JSONArray getBreadCrumbs() throws JSONException {
        return storageGetArr("breadCrumbs");
    }

    protected void pushBreadCrumb(String breadCrumb) {
        int limit = getBreadCrumbsLimit().intValue();
        try {
            JSONArray crumbsList = getBreadCrumbs();
            JSONObject crumbObj = new JSONObject();
            crumbObj.put(NativeProtocol.WEB_DIALOG_ACTION, breadCrumb);
            crumbObj.put("datetime", HSFormat.breadCrumbTsFormat.format(new Date()));
            crumbsList.put(crumbObj);
            int length = crumbsList.length();
            if (length > limit) {
                JSONArray newArray = new JSONArray();
                int diff = length - limit;
                for (int i = 0; i < limit; i++) {
                    newArray.put(i, crumbsList.get(i + diff));
                }
                crumbsList = newArray;
            }
            storageSet("breadCrumbs", crumbsList);
        } catch (JSONException e) {
            Log.d(TAG, "Error while leaving breadcrumb", e);
        }
    }

    protected void clearBreadCrumbs() {
        storageSet("breadCrumbs", "[]");
    }

    protected Integer getBreadCrumbsLimit() {
        try {
            return (Integer) getConfig().get("bcl");
        } catch (JSONException e) {
            return Integer.valueOf(10);
        }
    }

    protected void updateBreadCrumbsLimit(Integer newLimit) {
        Integer currentLimit = getBreadCrumbsLimit();
        if (currentLimit.intValue() > newLimit.intValue()) {
            try {
                JSONArray crumbsList = getBreadCrumbs();
                JSONArray newArray = new JSONArray();
                Integer diff = Integer.valueOf(currentLimit.intValue() - newLimit.intValue());
                for (int i = 0; i < newLimit.intValue(); i++) {
                    newArray.put(i, crumbsList.get(diff.intValue() + i));
                }
                storageSet("breadCrumbs", newArray);
            } catch (JSONException e) {
                Log.d(TAG, "Error while updating breadcrumbs limit", e);
            }
        }
    }

    protected void setConfig(JSONObject config) {
        storageSet(Network.CONFIG, config);
    }

    protected JSONObject getConfig() throws JSONException {
        return storageGetObj(Network.CONFIG);
    }

    protected void setAppConfig(JSONObject appConfig) {
        storageSet("appConfig", appConfig);
    }

    public JSONObject getAppConfig() throws JSONException {
        return storageGetObj("appConfig");
    }

    protected void setReviewCounter(int count) {
        storageSet("reviewCounter", Integer.valueOf(count));
    }

    protected int getReviewCounter() {
        return storageGetInt("reviewCounter").intValue();
    }

    protected void setLaunchReviewCounter(int count) {
        storageSet("launchReviewCounter", Integer.valueOf(count));
    }

    protected int getLaunchReviewCounter() {
        return storageGetInt("launchReviewCounter").intValue();
    }

    protected void setReviewed() {
        storageSet("reviewed", Integer.valueOf(1));
    }

    protected void enableReview() {
        storageSet("reviewed", Integer.valueOf(0));
    }

    protected int getReviewed() {
        return storageGetInt("reviewed").intValue();
    }

    protected void setStoredFiles(JSONArray filenames) {
        storageSet("cachedImages", filenames);
    }

    protected JSONArray getStoredFiles() throws JSONException {
        return storageGetArr("cachedImages");
    }

    protected JSONObject getEtags() throws JSONException {
        return storageGetObj("etags");
    }

    public void setEtag(String url, String etag) {
        try {
            JSONObject etags = getEtags();
            etags.put(url, etag);
            storageSet("etags", etags);
        } catch (JSONException e) {
            Log.d(TAG, "set etag", e);
        }
    }

    protected String getEtag(String url) {
        String etag = BuildConfig.FLAVOR;
        try {
            etag = getEtags().optString(url, BuildConfig.FLAVOR);
        } catch (JSONException e) {
            Log.d(TAG, "set etag", e);
        }
        return etag;
    }

    protected void storeIndex(HashMap fullIndex) {
        cacheIndex = fullIndex;
        try {
            ObjectOutputStream oStream = new ObjectOutputStream(this.context.openFileOutput("fullIndex.db", 0));
            oStream.writeObject(fullIndex);
            oStream.flush();
            oStream.close();
            setDBFlag();
        } catch (Exception e) {
            Log.d(TAG, "store index", e);
        }
    }

    protected void loadIndex() {
        if (cacheIndex == null) {
            try {
                ObjectInputStream oStream = new ObjectInputStream(this.context.openFileInput("fullIndex.db"));
                cacheIndex = (HashMap) oStream.readObject();
                oStream.close();
            } catch (Exception e) {
                Log.d(TAG, "read index", e);
            }
        }
    }

    protected HashMap readIndex() {
        return cacheIndex;
    }

    protected Boolean getDBFlag() {
        return storageGetBoolean("dbFlag");
    }

    protected void setDBFlag() {
        storageSet("dbFlag", Boolean.valueOf(true));
    }

    protected void unsetDBFlag() {
        storageSet("dbFlag", Boolean.valueOf(false));
    }

    protected void deleteIndex() {
        cacheIndex = null;
        this.context.deleteFile("fullIndex.db");
        unsetDBFlag();
    }

    private void setFailedMessages(JSONArray failedMessages, String profileId) {
        try {
            JSONObject allFailedMessages = storageGetObj("failedMessages");
            allFailedMessages.put(profileId, failedMessages);
            storageSet("failedMessages", allFailedMessages);
        } catch (JSONException e) {
            Log.d(TAG, "setFailedMessages", e);
        }
    }

    private JSONArray getFailedMessages(String profileId) throws JSONException {
        JSONArray failedMessages = null;
        try {
            failedMessages = storageGetObj("failedMessages").optJSONArray(profileId);
        } catch (JSONException e) {
            Log.d(TAG, "getFailedMessages", e);
        }
        if (failedMessages == null) {
            return new JSONArray();
        }
        return failedMessages;
    }

    protected JSONObject popFailedMessage(String profileId) throws JSONException {
        JSONArray failedMessages = getFailedMessages(profileId);
        JSONArray failedMessagesStore = new JSONArray();
        JSONObject failedMessageRes = null;
        if (failedMessages.length() > 0) {
            for (int i = 0; i < failedMessages.length(); i++) {
                JSONObject failedMessage = failedMessages.getJSONObject(i);
                if (failedMessageRes != null || failedMessage.getInt("state") <= -2) {
                    failedMessagesStore.put(failedMessage);
                } else {
                    failedMessageRes = failedMessage;
                }
            }
            setFailedMessages(failedMessagesStore, profileId);
        }
        return failedMessageRes;
    }

    protected JSONObject popFailedMessage(String id, String profileId) throws JSONException {
        JSONArray failedMessages = getFailedMessages(profileId);
        JSONArray failedMessagesStore = new JSONArray();
        JSONObject failedMessageRes = null;
        if (failedMessages.length() > 0) {
            for (int i = 0; i < failedMessages.length(); i++) {
                JSONObject failedMessage = failedMessages.getJSONObject(i);
                if (failedMessageRes == null && failedMessage.getString(DBLikedChannelsHelper.KEY_ID).equals(id)) {
                    failedMessageRes = failedMessage;
                } else {
                    failedMessagesStore.put(failedMessage);
                }
            }
            setFailedMessages(failedMessagesStore, profileId);
        }
        return failedMessageRes;
    }

    protected JSONArray getFailedMessages(String issueId, String profileId) throws JSONException {
        JSONArray failedMessages = getFailedMessages(profileId);
        JSONArray failedMessagesIssue = new JSONArray();
        for (int i = 0; i < failedMessages.length(); i++) {
            JSONObject failedMessage = failedMessages.getJSONObject(i);
            if (failedMessage.getString(MessageColumns.ISSUE_ID).equals(issueId)) {
                failedMessagesIssue.put(failedMessage);
            }
        }
        return failedMessagesIssue;
    }

    protected void storeFailedMessage(String issueId, String messageText, String type, String refers, int failedState, String profileId) throws JSONException {
        Date now = new Date();
        JSONObject failedMessage = new JSONObject();
        failedMessage.put(MessageColumns.ISSUE_ID, issueId);
        failedMessage.put(MessageColumns.BODY, messageText);
        failedMessage.put(MPDbAdapter.KEY_CREATED_AT, HSFormat.issueTsFormat.format(now));
        failedMessage.put(MessageColumns.TYPE, type);
        failedMessage.put("state", -1);
        failedMessage.put(MessageColumns.ORIGIN, "mobile");
        failedMessage.put("refers", refers);
        failedMessage.put(DBLikedChannelsHelper.KEY_ID, now.getTime() + BuildConfig.FLAVOR);
        failedMessage.put("state", failedState);
        storeFailedMessage(failedMessage, profileId);
    }

    protected void storeFailedMessage(JSONObject failedMessage, String profileId) throws JSONException {
        JSONArray failedMessages = getFailedMessages(profileId);
        failedMessages.put(failedMessage);
        setFailedMessages(failedMessages, profileId);
    }

    protected JSONObject getFailedApiCalls() throws JSONException {
        return storageGetObj("failedApiCalls");
    }

    protected void storeFailedApiCall(String key, JSONObject failedApiCall) throws JSONException {
        JSONObject failedApiCalls = getFailedApiCalls();
        failedApiCalls.put(key, failedApiCall);
        storageSet("failedApiCalls", failedApiCalls);
    }

    protected void storeReply(String replyText, String profileId) {
        try {
            JSONObject allReplyTexts = storageGetObj("allReplyTexts");
            allReplyTexts.put(profileId, replyText);
            storageSet("allReplyTexts", allReplyTexts);
        } catch (JSONException e) {
            Log.d(TAG, "storeReply", e);
        }
    }

    protected String getReply(String profileId) {
        String replyText = BuildConfig.FLAVOR;
        try {
            replyText = storageGetObj("allReplyTexts").optString(profileId, BuildConfig.FLAVOR);
        } catch (JSONException e) {
            Log.d(TAG, "storeReply", e);
        }
        return replyText;
    }

    protected void storeConversationDetail(String conversationText, String identifier) {
        try {
            JSONObject allConversationDetails = storageGetObj("allConversationDetails");
            JSONObject conversationDetail = new JSONObject();
            conversationDetail.put("conversationText", conversationText);
            conversationDetail.put("conversationTextSetTime", System.nanoTime());
            allConversationDetails.put(identifier, conversationDetail);
            storageSet("allConversationDetails", allConversationDetails);
        } catch (JSONException e) {
            Log.d(TAG, "storeConversationDetail", e);
        }
    }

    protected String getConversationDetail(String identifier) {
        String conversationText = BuildConfig.FLAVOR;
        try {
            JSONObject conversationDetail = storageGetObj("allConversationDetails").optJSONObject(identifier);
            if (conversationDetail != null) {
                long timeDiff = System.nanoTime() - conversationDetail.getLong("conversationTextSetTime");
                if (timeDiff < 0 || TimeUnit.NANOSECONDS.toSeconds(timeDiff) > 7200) {
                    storeConversationDetail(identifier, BuildConfig.FLAVOR);
                } else {
                    conversationText = conversationDetail.getString("conversationText");
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "getConversationDetail", e);
        }
        return conversationText;
    }

    protected void setServerTimeDelta(Float delta) {
        storageSet("serverTimeDelta", delta);
    }

    protected Float getServerTimeDelta() {
        return storageGetFloat("serverTimeDelta");
    }

    protected void setUUID(String id) {
        storageSet("uuid", id);
    }

    protected String getUUID() {
        return storageGet("uuid");
    }

    public void setConversationScreenshot(String screenshotPath, String loginIdentifier) {
        try {
            JSONObject allScreenshots = storageGetObj("conversationScreenshots");
            allScreenshots.put(loginIdentifier, screenshotPath);
            storageSet("conversationScreenshots", allScreenshots);
        } catch (JSONException e) {
            Log.d(TAG, "setConversationScreenshot", e);
        }
    }

    public String getConversationScreenshot(String loginIdentifier) {
        String screenshot = BuildConfig.FLAVOR;
        try {
            screenshot = storageGetObj("conversationScreenshots").optString(loginIdentifier, BuildConfig.FLAVOR);
        } catch (JSONException e) {
            Log.d(TAG, "getConversationScreenshot", e);
        }
        return screenshot;
    }

    public void setScreenShotDraft(Boolean status) {
        storageSet("screenShotDraft", status);
    }

    public Boolean getScreenShotDraft() {
        return storageGetBoolean("screenShotDraft");
    }

    public void setIsConversationShowing(Boolean status) {
        storageSet("isConversationShowing", status);
    }

    public Boolean getIsConversationShowing() {
        return storageGetBoolean("isConversationShowing");
    }

    public void setRequireEmail(Boolean status) {
        storageSet("requireEmail", status);
    }

    public Boolean getRequireEmail() {
        return storageGetBoolean("requireEmail");
    }

    public Boolean getEnableFullPrivacy() {
        return storageGetBoolean("fullPrivacy");
    }

    public void setEnableFullPrivacy(Boolean status) {
        storageSet("fullPrivacy", status);
    }

    public JSONObject getCustomMetaData() {
        try {
            return storageGetObj("customMetaData");
        } catch (JSONException e) {
            Log.d(TAG, "Exception getting meta from storage ", e);
            return null;
        }
    }

    public void setCustomMetaData(JSONObject metaData) {
        if (metaData == null) {
            metaData = new JSONObject();
        }
        storageSet("customMetaData", metaData);
    }

    protected void setIssueCSatStates(JSONObject states) {
        storageSet("issueCSatStates", states);
    }

    protected JSONObject getIssueCSatStates() {
        try {
            return storageGetObj("issueCSatStates");
        } catch (JSONException e) {
            Log.d(TAG, "JSONException : ", e);
            return new JSONObject();
        }
    }

    protected void setCSatDraft(JSONObject draft) {
        storageSet("csatDraft", draft);
    }

    protected JSONObject getCSatDraft() {
        try {
            return storageGetObj("csatDraft");
        } catch (JSONException e) {
            Log.d(TAG, "JSONException : ", e);
            return new JSONObject();
        }
    }

    protected void setHideNameAndEmail(Boolean status) {
        storageSet("hideNameAndEmail", status);
    }

    public Boolean getHideNameAndEmail() {
        return storageGetBoolean("hideNameAndEmail");
    }

    public void setConversationPrefillText(String text) {
        storageSet("conversationPrefillText", text);
    }

    public String getConversationPrefillText() {
        return storageGet("conversationPrefillText");
    }

    protected void setShowSearchOnNewConversation(Boolean status) {
        storageSet(HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION, status);
    }

    public Boolean getShowSearchOnNewConversation() {
        return storageGetBoolean(HSConsts.SHOW_SEARCH_ON_NEW_CONVERSATION);
    }

    protected JSONObject getCachedAttachmentFiles() {
        try {
            return storageGetObj("cachedAttachmentFiles");
        } catch (JSONException e) {
            Log.d(TAG, "getCachedAttachmentFiles : ", e);
            return new JSONObject();
        }
    }

    protected void setCachedAttachmentFiles(JSONObject cachedFiles) {
        storageSet("cachedAttachmentFiles", cachedFiles);
    }

    protected void addToCachedAttachmentFiles(String msgId, String path) {
        try {
            JSONObject cachedFiles = getCachedAttachmentFiles();
            cachedFiles.put(msgId, path);
            setCachedAttachmentFiles(cachedFiles);
        } catch (JSONException e) {
            Log.d(TAG, "addToCachedAttachmentFiles : ", e);
        }
    }

    protected void removeFromCachedAttachmentFiles(String msgId) {
        JSONObject cachedFiles = getCachedAttachmentFiles();
        cachedFiles.remove(msgId);
        setCachedAttachmentFiles(cachedFiles);
    }

    protected void addToDownloadedGenericFiles(String msgId, String filePath) {
        try {
            JSONObject downloadedFiles = getDownloadedAttachmentFiles();
            downloadedFiles.put(msgId, filePath);
            setDownloadedAttachmentFiles(downloadedFiles);
        } catch (JSONException e) {
            Log.d(TAG, "addToDownloadedGenericFiles : ", e);
        }
    }

    protected void removeFromDownloadedGenericFiles(String msgId) {
        JSONObject downloadedGenericFiles = getDownloadedAttachmentFiles();
        downloadedGenericFiles.remove(msgId);
        setDownloadedAttachmentFiles(downloadedGenericFiles);
    }

    protected String getFilePathForGenericAttachment(String msgId) {
        JSONObject downloadedFiles = getDownloadedAttachmentFiles();
        if (downloadedFiles.has(msgId)) {
            try {
                return downloadedFiles.getString(msgId);
            } catch (JSONException e) {
                Log.d(TAG, "getFilePathForGenericAttachment : ", e);
            }
        }
        return BuildConfig.FLAVOR;
    }

    protected JSONObject getDownloadedAttachmentFiles() {
        try {
            return storageGetObj("downloadedAttachmentFiles");
        } catch (JSONException e) {
            Log.d(TAG, "JSONException : ", e);
            return new JSONObject();
        }
    }

    protected void setDownloadedAttachmentFiles(JSONObject downloadedAttachmentFiles) {
        storageSet("downloadedAttachmentFiles", downloadedAttachmentFiles);
    }

    protected JSONObject getDownloadedThumbnailFiles() {
        try {
            return storageGetObj("downloadedThumbnailFiles");
        } catch (JSONException e) {
            Log.d(TAG, "getDownloadedThumbnailFiles ", e);
            return new JSONObject();
        }
    }

    protected void setDownloadedThumbnailFiles(JSONObject downloadedThumbnailFiles) {
        storageSet("downloadedThumbnailFiles", downloadedThumbnailFiles);
    }

    protected void addToDownloadedThumbnailFiles(String msgId, String filePath) {
        try {
            JSONObject downloadedThumbnailFiles = getDownloadedThumbnailFiles();
            downloadedThumbnailFiles.put(msgId, filePath);
            setDownloadedThumbnailFiles(downloadedThumbnailFiles);
        } catch (JSONException e) {
            Log.d(TAG, "addToDownloadedThumbnailFiles : ", e);
        }
    }

    protected void removeFromDownloadedThumbnailFiles(String msgId) {
        JSONObject downloadedThumbnailFiles = getDownloadedThumbnailFiles();
        downloadedThumbnailFiles.remove(msgId);
        setDownloadedThumbnailFiles(downloadedThumbnailFiles);
    }

    protected String getFilePathForThumbnail(String msgId) {
        try {
            JSONObject downloadedThumbnailFiles = getDownloadedThumbnailFiles();
            if (downloadedThumbnailFiles.has(msgId)) {
                return downloadedThumbnailFiles.getString(msgId);
            }
        } catch (JSONException e) {
            Log.d(TAG, "getFilePathForThumbnail : ", e);
        }
        return BuildConfig.FLAVOR;
    }

    protected JSONObject getDownloadedImageFiles() {
        try {
            return storageGetObj("downloadedImageFiles");
        } catch (JSONException e) {
            Log.d(TAG, "getDownloadedImageFiles : ", e);
            return new JSONObject();
        }
    }

    protected void setDownloadedImageFiles(JSONObject downloadedImageFiles) {
        storageSet("downloadedImageFiles", downloadedImageFiles);
    }

    protected void addToDownloadedImageFiles(String msgId, String filePath) {
        try {
            JSONObject downloadedImageFiles = getDownloadedImageFiles();
            downloadedImageFiles.put(msgId, filePath);
            setDownloadedImageFiles(downloadedImageFiles);
        } catch (JSONException e) {
            Log.d(TAG, "addToDownloadedImageFiles : ", e);
        }
    }

    protected void removeFromDownloadedImageFiles(String msgId) {
        JSONObject downloadedImageFiles = getDownloadedImageFiles();
        downloadedImageFiles.remove(msgId);
        setDownloadedImageFiles(downloadedImageFiles);
    }

    protected String getFilePathForImage(String msgId) {
        try {
            JSONObject downloadedImageFiles = getDownloadedImageFiles();
            if (downloadedImageFiles.has(msgId)) {
                return downloadedImageFiles.getString(msgId);
            }
        } catch (JSONException e) {
            Log.d(TAG, "getFilePathForImage : ", e);
        }
        return BuildConfig.FLAVOR;
    }

    protected JSONObject getActiveDownloads() {
        try {
            return storageGetObj("activeDownloads");
        } catch (JSONException e) {
            Log.d(TAG, "getActiveDownloads : ", e);
            return new JSONObject();
        }
    }

    protected void setActiveDownloads(JSONObject activeDownloads) {
        storageSet("activeDownloads", activeDownloads);
    }

    protected void addToActiveDownloads(String msgId, String issueId) {
        try {
            JSONObject activeDownloads = getActiveDownloads();
            activeDownloads.put(msgId, issueId);
            setActiveDownloads(activeDownloads);
        } catch (JSONException e) {
            Log.d(TAG, "addToActiveDownloads : ", e);
        }
    }

    protected void removeFromActiveDownloads(String msgId) {
        JSONObject activeDownloads = getActiveDownloads();
        activeDownloads.remove(msgId);
        setActiveDownloads(activeDownloads);
    }

    protected boolean isDownloadActive(String msgId) {
        return getActiveDownloads().has(msgId);
    }

    protected void setLoginIdentifier(String loginIdentifier) {
        storageSet("loginIdentifier", loginIdentifier);
    }

    protected String getLoginIdentifier() {
        return storageGet("loginIdentifier");
    }

    public void saveNotification(String issueId, int issueTs, int newMessageCount, String chatLaunchSource, String contentTitle) {
        String profileId = Issue.getProfileId(issueId);
        if (!TextUtils.isEmpty(profileId)) {
            JSONObject notifications = getNotifications(profileId);
            try {
                JSONObject newNotification = new JSONObject();
                newNotification.put("issueTs", issueTs);
                newNotification.put("newMessageCount", newMessageCount);
                newNotification.put("chatLaunchSource", chatLaunchSource);
                newNotification.put("contentTitle", contentTitle);
                notifications.put(issueId, newNotification);
                setNotifications(profileId, notifications);
            } catch (JSONException e) {
                Log.d(TAG, "saveNotification", e);
            }
        }
    }

    private void setNotifications(String profileId, JSONObject notifications) {
        try {
            JSONObject allNotifications = storageGetObj("notifications");
            allNotifications.put(profileId, notifications);
            storageSet("notifications", allNotifications);
        } catch (JSONException e) {
            Log.d(TAG, "setNotifications", e);
        }
    }

    protected JSONObject getNotifications(String profileId) {
        JSONObject notifications = null;
        try {
            notifications = storageGetObj("notifications").optJSONObject(profileId);
        } catch (JSONException e) {
            Log.d(TAG, "getNotifications", e);
        }
        if (notifications == null) {
            return new JSONObject();
        }
        return notifications;
    }

    protected void clearNotification(String issueId) {
        String profileId = Issue.getProfileId(issueId);
        if (!TextUtils.isEmpty(profileId)) {
            try {
                JSONObject notifications = getNotifications(profileId);
                notifications.put(issueId, null);
                setNotifications(profileId, notifications);
            } catch (JSONException e) {
                Log.d(TAG, "clearNotification", e);
            }
        }
    }

    protected void setSdkLanguage(String language) {
        storageSet("sdkLanguage", language);
    }

    public String getSdkLanguage() {
        return storageGet("sdkLanguage");
    }

    public void updateDisableHelpshiftBranding() {
        boolean z = false;
        Boolean brandingDisabledFromInstall = Boolean.valueOf(false);
        try {
            JSONObject config = getAppConfig();
            if (config.has("disableHelpshiftBranding")) {
                brandingDisabledFromInstall = Boolean.valueOf(config.getBoolean("disableHelpshiftBranding"));
            }
        } catch (JSONException e) {
            Log.d(TAG, "isHelpshiftBrandingDisabled", e);
        }
        Boolean brandingDisabledFromThemeData = Boolean.valueOf(false);
        if (HSConfig.themeData.containsKey("hl")) {
            boolean z2;
            if (((String) HSConfig.themeData.get("hl")).equals(ServerProtocol.DIALOG_RETURN_SCOPES_TRUE)) {
                z2 = false;
            } else {
                z2 = true;
            }
            brandingDisabledFromThemeData = Boolean.valueOf(z2);
        }
        String str = "disableHelpshiftBranding";
        if (brandingDisabledFromInstall.booleanValue() || brandingDisabledFromThemeData.booleanValue()) {
            z = true;
        }
        storageSet(str, Boolean.valueOf(z));
    }

    public boolean isHelpshiftBrandingDisabled() {
        return storageGetBoolean("disableHelpshiftBranding").booleanValue();
    }
}
