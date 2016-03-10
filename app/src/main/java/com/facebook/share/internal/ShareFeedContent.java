package com.facebook.share.internal;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.facebook.share.model.ShareContent;

public class ShareFeedContent extends ShareContent<ShareFeedContent, Builder> {
    public static final Creator<ShareFeedContent> CREATOR = new Creator<ShareFeedContent>() {
        public ShareFeedContent createFromParcel(Parcel in) {
            return new ShareFeedContent(in);
        }

        public ShareFeedContent[] newArray(int size) {
            return new ShareFeedContent[size];
        }
    };
    private final String link;
    private final String linkCaption;
    private final String linkDescription;
    private final String linkName;
    private final String mediaSource;
    private final String picture;
    private final String toId;

    public static final class Builder extends com.facebook.share.model.ShareContent.Builder<ShareFeedContent, Builder> {
        private String link;
        private String linkCaption;
        private String linkDescription;
        private String linkName;
        private String mediaSource;
        private String picture;
        private String toId;

        public Builder setToId(String toId) {
            this.toId = toId;
            return this;
        }

        public Builder setLink(String link) {
            this.link = link;
            return this;
        }

        public Builder setLinkName(String linkName) {
            this.linkName = linkName;
            return this;
        }

        public Builder setLinkCaption(String linkCaption) {
            this.linkCaption = linkCaption;
            return this;
        }

        public Builder setLinkDescription(String linkDescription) {
            this.linkDescription = linkDescription;
            return this;
        }

        public Builder setPicture(String picture) {
            this.picture = picture;
            return this;
        }

        public Builder setMediaSource(String mediaSource) {
            this.mediaSource = mediaSource;
            return this;
        }

        public ShareFeedContent build() {
            return new ShareFeedContent();
        }

        public Builder readFrom(ShareFeedContent model) {
            return model == null ? this : ((Builder) super.readFrom((ShareContent) model)).setToId(model.getToId()).setLink(model.getLink()).setLinkName(model.getLinkName()).setLinkCaption(model.getLinkCaption()).setLinkDescription(model.getLinkDescription()).setPicture(model.getPicture()).setMediaSource(model.getMediaSource());
        }

        public Builder readFrom(Parcel parcel) {
            return readFrom((ShareFeedContent) parcel.readParcelable(ShareFeedContent.class.getClassLoader()));
        }
    }

    private ShareFeedContent(Builder builder) {
        super((com.facebook.share.model.ShareContent.Builder) builder);
        this.toId = builder.toId;
        this.link = builder.link;
        this.linkName = builder.linkName;
        this.linkCaption = builder.linkCaption;
        this.linkDescription = builder.linkDescription;
        this.picture = builder.picture;
        this.mediaSource = builder.mediaSource;
    }

    ShareFeedContent(Parcel in) {
        super(in);
        this.toId = in.readString();
        this.link = in.readString();
        this.linkName = in.readString();
        this.linkCaption = in.readString();
        this.linkDescription = in.readString();
        this.picture = in.readString();
        this.mediaSource = in.readString();
    }

    public String getToId() {
        return this.toId;
    }

    public String getLink() {
        return this.link;
    }

    public String getLinkName() {
        return this.linkName;
    }

    public String getLinkCaption() {
        return this.linkCaption;
    }

    public String getLinkDescription() {
        return this.linkDescription;
    }

    public String getPicture() {
        return this.picture;
    }

    public String getMediaSource() {
        return this.mediaSource;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        out.writeString(this.toId);
        out.writeString(this.link);
        out.writeString(this.linkName);
        out.writeString(this.linkCaption);
        out.writeString(this.linkDescription);
        out.writeString(this.picture);
        out.writeString(this.mediaSource);
    }
}
