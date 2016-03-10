package com.soundcloud.android.crop;

import android.graphics.Bitmap;
import android.graphics.Matrix;

class RotateBitmap {
    private Bitmap bitmap;
    private int rotation;

    public RotateBitmap(Bitmap bitmap, int rotation) {
        this.bitmap = bitmap;
        this.rotation = rotation % 360;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getRotation() {
        return this.rotation;
    }

    public Bitmap getBitmap() {
        return this.bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Matrix getRotateMatrix() {
        Matrix matrix = new Matrix();
        if (!(this.bitmap == null || this.rotation == 0)) {
            matrix.preTranslate((float) (-(this.bitmap.getWidth() / 2)), (float) (-(this.bitmap.getHeight() / 2)));
            matrix.postRotate((float) this.rotation);
            matrix.postTranslate((float) (getWidth() / 2), (float) (getHeight() / 2));
        }
        return matrix;
    }

    public boolean isOrientationChanged() {
        return (this.rotation / 90) % 2 != 0;
    }

    public int getHeight() {
        if (this.bitmap == null) {
            return 0;
        }
        if (isOrientationChanged()) {
            return this.bitmap.getWidth();
        }
        return this.bitmap.getHeight();
    }

    public int getWidth() {
        if (this.bitmap == null) {
            return 0;
        }
        if (isOrientationChanged()) {
            return this.bitmap.getHeight();
        }
        return this.bitmap.getWidth();
    }

    public void recycle() {
        if (this.bitmap != null) {
            this.bitmap.recycle();
            this.bitmap = null;
        }
    }
}
