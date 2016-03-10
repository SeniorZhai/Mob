package com.helpshift.models;

import android.text.TextUtils;
import com.helpshift.storage.IssuesDataSource;
import io.fabric.sdk.android.BuildConfig;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private final String author;
    private final String body;
    private final String createdAt;
    private boolean inProgress;
    private boolean invisible;
    private final String issueId;
    private final String messageId;
    private boolean messageSeen;
    private final String meta;
    private final String origin;
    private String screenshot;
    private final String type;

    Message(String issueId, String messageId, String body, String origin, String type, String createdAt, String author, String meta, String screenshot, boolean messageSeen, boolean invisible, boolean inProgress) {
        this.issueId = issueId;
        this.messageId = messageId;
        this.body = body;
        this.origin = origin;
        this.type = type;
        this.createdAt = createdAt;
        this.author = author;
        this.meta = meta;
        this.screenshot = screenshot;
        this.messageSeen = messageSeen;
        this.invisible = invisible;
        this.inProgress = inProgress;
    }

    public String getIssueId() {
        return this.issueId;
    }

    public String getMessageId() {
        return this.messageId;
    }

    public String getBody() {
        return this.body;
    }

    public String getOrigin() {
        return this.origin;
    }

    public String getType() {
        return this.type;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getMeta() {
        return this.meta;
    }

    public void setScreenshot(String screenshot) {
        if (TextUtils.isEmpty(screenshot)) {
            this.screenshot = BuildConfig.FLAVOR;
        } else {
            this.screenshot = screenshot;
        }
    }

    public String getScreenshot() {
        return this.screenshot;
    }

    private void setMessageSeen(boolean messageSeen) {
        this.messageSeen = messageSeen;
    }

    public boolean isMessageSeen() {
        return this.messageSeen;
    }

    public boolean isInvisible() {
        return this.invisible;
    }

    private void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isInProgress() {
        return this.inProgress;
    }

    private void setInvisible(boolean invisible) {
        this.invisible = invisible;
    }

    public static void setInvisible(String messageId, boolean invisible) {
        Message message = IssuesDataSource.getMessage(messageId);
        message.setInvisible(invisible);
        IssuesDataSource.storeMessage(message);
    }

    public static void setInProgress(String messageId, boolean inProgress) {
        Message message = IssuesDataSource.getMessage(messageId);
        message.setInProgress(inProgress);
        IssuesDataSource.storeMessage(message);
    }

    public static void setScreenshot(String messageId, String screenshot) {
        Message message = IssuesDataSource.getMessage(messageId);
        message.setScreenshot(screenshot);
        IssuesDataSource.storeMessage(message);
    }

    public static List<String> updateMessagesSeenState(String issueId) {
        List<String> messageIds = new ArrayList();
        List<Message> messageList = IssuesDataSource.getUnseenMessages(issueId);
        for (Message message : messageList) {
            message.setMessageSeen(true);
            messageIds.add(message.getMessageId());
        }
        IssuesDataSource.storeMessages(messageList);
        return messageIds;
    }

    public boolean equals(Object object) {
        if (!(object instanceof Message)) {
            return false;
        }
        Message message = (Message) object;
        if (getIssueId().equals(message.getIssueId()) && getMessageId().equals(message.getMessageId()) && getBody().equals(message.getBody()) && getOrigin().equals(message.getOrigin()) && getType().equals(message.getType()) && getCreatedAt().equals(message.getCreatedAt()) && getAuthor().equals(message.getAuthor()) && getMeta().equals(message.getMeta()) && getScreenshot().equals(message.getScreenshot()) && isMessageSeen() == message.isMessageSeen() && isInvisible() == message.isInvisible() && isInProgress() == message.isInProgress()) {
            return true;
        }
        return false;
    }
}
