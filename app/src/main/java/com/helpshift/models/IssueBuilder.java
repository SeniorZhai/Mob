package com.helpshift.models;

import java.util.List;

public class IssueBuilder {
    private final String body;
    private final String createdAt;
    private final String issueId;
    private List<Message> messageList = null;
    private int newMessagesCount = 0;
    private final String profileId;
    private Boolean showAgentName = Boolean.valueOf(true);
    private final int status;
    private final String title;
    private final String updatedAt;

    public IssueBuilder(String profileId, String issueId, String body, String title, String createdAt, String updatedAt, int status, boolean showAgentName) {
        this.profileId = profileId;
        this.issueId = issueId;
        this.body = body;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.showAgentName = Boolean.valueOf(showAgentName);
    }

    public IssueBuilder setNewMessagesCount(int newMessagesCount) {
        this.newMessagesCount = newMessagesCount;
        return this;
    }

    public IssueBuilder setMessageList(List<Message> messageList) {
        this.messageList = messageList;
        return this;
    }

    public Issue build() {
        return new Issue(this.profileId, this.issueId, this.body, this.title, this.createdAt, this.updatedAt, this.status, this.messageList, this.newMessagesCount, this.showAgentName.booleanValue());
    }
}
