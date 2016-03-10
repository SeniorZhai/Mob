package com.soundcloud.android.crop;

import android.content.Context;
import android.net.Uri;
import android.support.v4.app.Fragment;

public class CropCompat extends Crop {
    public CropCompat(Uri source) {
        super(source);
    }

    public CropCompat output(Uri output) {
        super.output(output);
        return this;
    }

    public CropCompat withAspect(int x, int y) {
        super.withAspect(x, y);
        return this;
    }

    public CropCompat asSquare() {
        super.asSquare();
        return this;
    }

    public CropCompat withMaxSize(int width, int height) {
        super.withMaxSize(width, height);
        return this;
    }

    public void start(Context context, Fragment fragment) {
        fragment.startActivityForResult(getIntent(context), Crop.REQUEST_CROP);
    }
}
