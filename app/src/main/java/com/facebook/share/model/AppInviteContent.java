package com.facebook.share.model;

import android.os.Parcel;

public final class AppInviteContent implements ShareModel {
    private final String applinkUrl;
    private final String previewImageUrl;

    public static class Builder implements ShareModelBuilder<AppInviteContent, Builder> {
        private String applinkUrl;
        private String previewImageUrl;

        public Builder setApplinkUrl(String applinkUrl) {
            this.applinkUrl = applinkUrl;
            return this;
        }

        public Builder setPreviewImageUrl(String previewImageUrl) {
            this.previewImageUrl = previewImageUrl;
            return this;
        }

        public AppInviteContent build() {
            return new AppInviteContent();
        }

        public Builder readFrom(AppInviteContent content) {
            return content == null ? this : setApplinkUrl(content.getApplinkUrl()).setPreviewImageUrl(content.getPreviewImageUrl());
        }

        public Builder readFrom(Parcel parcel) {
            return readFrom((AppInviteContent) parcel.readParcelable(AppInviteContent.class.getClassLoader()));
        }
    }

    private AppInviteContent(Builder builder) {
        this.applinkUrl = builder.applinkUrl;
        this.previewImageUrl = builder.previewImageUrl;
    }

    AppInviteContent(Parcel in) {
        this.applinkUrl = in.readString();
        this.previewImageUrl = in.readString();
    }

    public String getApplinkUrl() {
        return this.applinkUrl;
    }

    public String getPreviewImageUrl() {
        return this.previewImageUrl;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.applinkUrl);
        out.writeString(this.previewImageUrl);
    }
}
