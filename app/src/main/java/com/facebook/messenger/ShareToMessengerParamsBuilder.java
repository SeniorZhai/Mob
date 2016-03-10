package com.facebook.messenger;

import android.net.Uri;

public class ShareToMessengerParamsBuilder {
    private Uri mExternalUri;
    private String mMetaData;
    private final String mMimeType;
    private final Uri mUri;

    ShareToMessengerParamsBuilder(Uri uri, String mimeType) {
        this.mUri = uri;
        this.mMimeType = mimeType;
    }

    public Uri getUri() {
        return this.mUri;
    }

    public String getMimeType() {
        return this.mMimeType;
    }

    public ShareToMessengerParamsBuilder setMetaData(String metaData) {
        this.mMetaData = metaData;
        return this;
    }

    public String getMetaData() {
        return this.mMetaData;
    }

    public ShareToMessengerParamsBuilder setExternalUri(Uri externalUri) {
        this.mExternalUri = externalUri;
        return this;
    }

    public Uri getExternalUri() {
        return this.mExternalUri;
    }

    public ShareToMessengerParams build() {
        return new ShareToMessengerParams(this);
    }
}
