package com.facebook.share.model;

import android.os.Parcel;

public final class AppGroupCreationContent implements ShareModel {
    private final String description;
    private final String name;
    private AppGroupPrivacy privacy;

    public enum AppGroupPrivacy {
        Open,
        Closed
    }

    public static class Builder implements ShareModelBuilder<AppGroupCreationContent, Builder> {
        private String description;
        private String name;
        private AppGroupPrivacy privacy;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setAppGroupPrivacy(AppGroupPrivacy privacy) {
            this.privacy = privacy;
            return this;
        }

        public AppGroupCreationContent build() {
            return new AppGroupCreationContent();
        }

        public Builder readFrom(AppGroupCreationContent content) {
            return content == null ? this : setName(content.getName()).setDescription(content.getDescription()).setAppGroupPrivacy(content.getAppGroupPrivacy());
        }

        public Builder readFrom(Parcel parcel) {
            return readFrom((AppGroupCreationContent) parcel.readParcelable(AppGroupCreationContent.class.getClassLoader()));
        }
    }

    private AppGroupCreationContent(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.privacy = builder.privacy;
    }

    AppGroupCreationContent(Parcel in) {
        this.name = in.readString();
        this.description = in.readString();
        this.privacy = AppGroupPrivacy.valueOf(in.readString());
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public AppGroupPrivacy getAppGroupPrivacy() {
        return this.privacy;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.name);
        out.writeString(this.description);
        out.writeString(this.privacy.toString());
    }
}
