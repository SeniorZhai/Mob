package com.helpshift.models;

import com.helpshift.res.values.HSConfig;
import com.helpshift.storage.IssuesDataSource;
import io.fabric.sdk.android.BuildConfig;
import java.util.List;

public class Issue {
    private final String body;
    private final String createdAt;
    private final String issueId;
    private List<Message> messageList;
    private int newMessagesCount;
    private final String profileId;
    private boolean showAgentName;
    private int status;
    private final String title;
    private final String updatedAt;

    Issue(String profileId, String issueId, String body, String title, String createdAt, String updatedAt, int status, List<Message> messageList, int newMessagesCount, boolean showAgentName) {
        this.profileId = profileId;
        this.issueId = issueId;
        this.body = body;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.messageList = messageList;
        this.newMessagesCount = newMessagesCount;
        this.showAgentName = showAgentName;
    }

    public String getProfileId() {
        return this.profileId;
    }

    public String getIssueId() {
        return this.issueId;
    }

    public String getBody() {
        return this.body;
    }

    public String getTitle() {
        return this.title;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    private void setNewMessagesCount(int newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
    }

    public int getNewMessagesCount() {
        return this.newMessagesCount;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }

    public List<Message> getMessageList() {
        if (this.messageList == null) {
            setMessageList(IssuesDataSource.getMessages(getIssueId()));
        }
        return this.messageList;
    }

    public boolean isShowAgentName() {
        return this.showAgentName;
    }

    public static void openIssue(String issueId) {
        Issue issue = IssuesDataSource.getIssue(issueId);
        if (issue.getStatus() != 1) {
            issue.setStatus(1);
            IssuesDataSource.storeIssue(issue);
        }
    }

    public static void resetIssueCount(String issueId) {
        Issue issue = IssuesDataSource.getIssue(issueId);
        issue.setNewMessagesCount(-1);
        IssuesDataSource.storeIssue(issue);
    }

    public static String getProfileId(String issueId) {
        String profileId = BuildConfig.FLAVOR;
        Issue issue = IssuesDataSource.getIssue(issueId);
        if (issue != null) {
            return issue.getProfileId();
        }
        return profileId;
    }

    public static boolean isShowAgentNameEnabled(String issueId) {
        if (((Boolean) HSConfig.configData.get("san")).booleanValue()) {
            return IssuesDataSource.getIssue(issueId).isShowAgentName();
        }
        return false;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Issue)) {
            return false;
        }
        Issue issue = (Issue) object;
        if (getProfileId().equals(issue.getProfileId()) && getIssueId().equals(issue.getIssueId()) && getBody().equals(issue.getBody()) && getTitle().equals(issue.getTitle()) && getCreatedAt().equals(issue.getCreatedAt()) && getUpdatedAt().equals(issue.getUpdatedAt()) && getStatus() == issue.getStatus() && getNewMessagesCount() == issue.getNewMessagesCount() && getMessageList().equals(issue.getMessageList()) && isShowAgentName() == issue.isShowAgentName()) {
            return true;
        }
        return false;
    }
}
