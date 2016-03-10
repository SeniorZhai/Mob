package com.facebook.share.model;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SharePhotoContent extends ShareContent<SharePhotoContent, Builder> {
    public static final Creator<SharePhotoContent> CREATOR = new Creator<SharePhotoContent>() {
        public SharePhotoContent createFromParcel(Parcel in) {
            return new SharePhotoContent(in);
        }

        public SharePhotoContent[] newArray(int size) {
            return new SharePhotoContent[size];
        }
    };
    private final List<SharePhoto> photos;

    public static class Builder extends com.facebook.share.model.ShareContent.Builder<SharePhotoContent, Builder> {
        private final List<SharePhoto> photos = new ArrayList();

        public Builder addPhoto(@Nullable SharePhoto photo) {
            if (photo != null) {
                this.photos.add(new com.facebook.share.model.SharePhoto.Builder().readFrom(photo).build());
            }
            return this;
        }

        public Builder addPhotos(@Nullable List<SharePhoto> photos) {
            if (photos != null) {
                for (SharePhoto photo : photos) {
                    addPhoto(photo);
                }
            }
            return this;
        }

        public SharePhotoContent build() {
            return new SharePhotoContent();
        }

        public Builder readFrom(SharePhotoContent model) {
            return model == null ? this : ((Builder) super.readFrom((ShareContent) model)).addPhotos(model.getPhotos());
        }

        public Builder readFrom(Parcel parcel) {
            return readFrom((SharePhotoContent) parcel.readParcelable(SharePhotoContent.class.getClassLoader()));
        }

        public Builder setPhotos(@Nullable List<SharePhoto> photos) {
            this.photos.clear();
            addPhotos(photos);
            return this;
        }
    }

    private SharePhotoContent(Builder builder) {
        super((com.facebook.share.model.ShareContent.Builder) builder);
        this.photos = Collections.unmodifiableList(builder.photos);
    }

    SharePhotoContent(Parcel in) {
        super(in);
        this.photos = Collections.unmodifiableList(com.facebook.share.model.SharePhoto.Builder.readListFrom(in));
    }

    @Nullable
    public List<SharePhoto> getPhotos() {
        return this.photos;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        super.writeToParcel(out, flags);
        com.facebook.share.model.SharePhoto.Builder.writeListTo(out, this.photos);
    }
}
