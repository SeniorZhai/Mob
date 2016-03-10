package com.facebook.internal;

import android.content.Context;
import android.net.Uri;
import com.mobcrush.mobcrush.datamodel.ChatMessage;
import java.util.Locale;

public class ImageRequest {
    private static final String AUTHORITY = "graph.facebook.com";
    private static final String HEIGHT_PARAM = "height";
    private static final String MIGRATION_PARAM = "migration_overrides";
    private static final String MIGRATION_VALUE = "{october_2012:true}";
    private static final String PATH = "%s/picture";
    private static final String SCHEME = "https";
    public static final int UNSPECIFIED_DIMENSION = 0;
    private static final String WIDTH_PARAM = "width";
    private boolean allowCachedRedirects;
    private Callback callback;
    private Object callerTag;
    private Context context;
    private Uri imageUri;

    public static class Builder {
        private boolean allowCachedRedirects;
        private Callback callback;
        private Object callerTag;
        private Context context;
        private Uri imageUrl;

        public Builder(Context context, Uri imageUri) {
            Validate.notNull(imageUri, "imageUri");
            this.context = context;
            this.imageUrl = imageUri;
        }

        public Builder setCallback(Callback callback) {
            this.callback = callback;
            return this;
        }

        public Builder setCallerTag(Object callerTag) {
            this.callerTag = callerTag;
            return this;
        }

        public Builder setAllowCachedRedirects(boolean allowCachedRedirects) {
            this.allowCachedRedirects = allowCachedRedirects;
            return this;
        }

        public ImageRequest build() {
            return new ImageRequest();
        }
    }

    public interface Callback {
        void onCompleted(ImageResponse imageResponse);
    }

    public static Uri getProfilePictureUri(String userId, int width, int height) {
        Validate.notNullOrEmpty(userId, ChatMessage.USER_ID);
        width = Math.max(width, UNSPECIFIED_DIMENSION);
        height = Math.max(height, UNSPECIFIED_DIMENSION);
        if (width == 0 && height == 0) {
            throw new IllegalArgumentException("Either width or height must be greater than 0");
        }
        android.net.Uri.Builder builder = new android.net.Uri.Builder().scheme(SCHEME).authority(AUTHORITY).path(String.format(Locale.US, PATH, new Object[]{userId}));
        if (height != 0) {
            builder.appendQueryParameter(HEIGHT_PARAM, String.valueOf(height));
        }
        if (width != 0) {
            builder.appendQueryParameter(WIDTH_PARAM, String.valueOf(width));
        }
        builder.appendQueryParameter(MIGRATION_PARAM, MIGRATION_VALUE);
        return builder.build();
    }

    private ImageRequest(Builder builder) {
        this.context = builder.context;
        this.imageUri = builder.imageUrl;
        this.callback = builder.callback;
        this.allowCachedRedirects = builder.allowCachedRedirects;
        this.callerTag = builder.callerTag == null ? new Object() : builder.callerTag;
    }

    public Context getContext() {
        return this.context;
    }

    public Uri getImageUri() {
        return this.imageUri;
    }

    public Callback getCallback() {
        return this.callback;
    }

    public boolean isCachedRedirectAllowed() {
        return this.allowCachedRedirects;
    }

    public Object getCallerTag() {
        return this.callerTag;
    }
}
