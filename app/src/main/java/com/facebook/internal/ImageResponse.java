package com.facebook.internal;

import android.graphics.Bitmap;

public class ImageResponse {
    private Bitmap bitmap;
    private Exception error;
    private boolean isCachedRedirect;
    private ImageRequest request;

    ImageResponse(ImageRequest request, Exception error, boolean isCachedRedirect, Bitmap bitmap) {
        this.request = request;
        this.error = error;
        this.bitmap = bitmap;
        this.isCachedRedirect = isCachedRedirect;
    }

    public ImageRequest getRequest() {
        return this.request;
    }

    public Exception getError() {
        return this.error;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public boolean isCachedRedirect() {
        return this.isCachedRedirect;
    }
}
