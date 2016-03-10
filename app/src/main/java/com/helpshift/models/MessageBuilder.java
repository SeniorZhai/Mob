package com.helpshift.models;

import io.fabric.sdk.android.BuildConfig;

public class MessageBuilder {
    private final String author;
    private final String body;
    private final String createdAt;
    private boolean inProgress = false;
    private boolean invisible = false;
    private final String issueId;
    private final String messageId;
    private boolean messageSeen = false;
    private final String meta;
    private final String origin;
    private String screenshot = BuildConfig.FLAVOR;
    private final String type;

    public MessageBuilder(String issueId, String messageId, String body, String origin, String type, String createdAt, String author, String meta) {
        this.issueId = issueId;
        this.messageId = messageId;
        this.body = body;
        this.origin = origin;
        this.type = type;
        this.createdAt = createdAt;
        this.author = author;
        this.meta = meta;
    }

    public MessageBuilder setScreenshot(String screenshot) {
        this.screenshot = screenshot;
        return this;
    }

    public MessageBuilder setMessageSeen(boolean messageSeen) {
        this.messageSeen = messageSeen;
        return this;
    }

    public MessageBuilder setInvisible(boolean invisible) {
        this.invisible = invisible;
        return this;
    }

    public MessageBuilder setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
        return this;
    }

    public Message build() {
        return new Message(this.issueId, this.messageId, this.body, this.origin, this.type, this.createdAt, this.author, this.meta, this.screenshot, this.messageSeen, this.invisible, this.inProgress);
    }
}
