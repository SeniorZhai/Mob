package com.facebook.share.model;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ShareContent<P extends ShareContent, E extends Builder> implements ShareModel {
    private final Uri contentUrl;
    private final List<String> peopleIds;
    private final String placeId;
    private final String ref;

    public static abstract class Builder<P extends ShareContent, E extends Builder> implements ShareModelBuilder<P, E> {
        private Uri contentUrl;
        private List<String> peopleIds;
        private String placeId;
        private String ref;

        public E setContentUrl(@Nullable Uri contentUrl) {
            this.contentUrl = contentUrl;
            return this;
        }

        public E setPeopleIds(@Nullable List<String> peopleIds) {
            this.peopleIds = peopleIds == null ? null : Collections.unmodifiableList(peopleIds);
            return this;
        }

        public E setPlaceId(@Nullable String placeId) {
            this.placeId = placeId;
            return this;
        }

        public E setRef(@Nullable String ref) {
            this.ref = ref;
            return this;
        }

        public E readFrom(P content) {
            return content == null ? this : setContentUrl(content.getContentUrl()).setPeopleIds(content.getPeopleIds()).setPlaceId(content.getPlaceId()).setRef(content.getRef());
        }
    }

    protected ShareContent(Builder builder) {
        this.contentUrl = builder.contentUrl;
        this.peopleIds = builder.peopleIds;
        this.placeId = builder.placeId;
        this.ref = builder.ref;
    }

    protected ShareContent(Parcel in) {
        this.contentUrl = (Uri) in.readParcelable(Uri.class.getClassLoader());
        this.peopleIds = readUnmodifiableStringList(in);
        this.placeId = in.readString();
        this.ref = in.readString();
    }

    @Nullable
    public Uri getContentUrl() {
        return this.contentUrl;
    }

    @Nullable
    public List<String> getPeopleIds() {
        return this.peopleIds;
    }

    @Nullable
    public String getPlaceId() {
        return this.placeId;
    }

    @Nullable
    public String getRef() {
        return this.ref;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.contentUrl, 0);
        out.writeStringList(this.peopleIds);
        out.writeString(this.placeId);
        out.writeString(this.ref);
    }

    private List<String> readUnmodifiableStringList(Parcel in) {
        List<String> list = new ArrayList();
        in.readStringList(list);
        return list.size() == 0 ? null : Collections.unmodifiableList(list);
    }
}
