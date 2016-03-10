package com.mobcrush.mobcrush.broadcast;

import android.annotation.TargetApi;
import android.hardware.display.VirtualDisplay;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.media.projection.MediaProjection;
import com.mobcrush.mobcrush.Mobcrush;

@TargetApi(21)
public class MobcrushProjectionManager {
    private static final String TAG = MobcrushProjectionManager.class.getName();
    private int mDisplayHeight;
    private int mDisplayWidth;
    private int mScreenDensity;
    private ImageReader mScreenImageReader;
    private VirtualDisplay mVirtualDisplay;

    private class ImageAvailableListener implements OnImageAvailableListener {
        private ImageAvailableListener() {
        }

        public void onImageAvailable(ImageReader reader) {
        }
    }

    public void startScreenCapture(int width, int height, int screenDensity, MediaProjection mediaProjection) {
        this.mDisplayWidth = width;
        this.mDisplayHeight = height;
        this.mScreenDensity = screenDensity;
        this.mScreenImageReader = ImageReader.newInstance(this.mDisplayWidth, this.mDisplayHeight, 1, 10);
        this.mScreenImageReader.setOnImageAvailableListener(new ImageAvailableListener(), null);
        Mobcrush.setScreenSurfaceReader(this.mScreenImageReader.getSurface(), this.mScreenImageReader);
        MediaProjection mediaProjection2 = mediaProjection;
        this.mVirtualDisplay = mediaProjection2.createVirtualDisplay("Screen", this.mDisplayWidth, this.mDisplayHeight, this.mScreenDensity, 16, this.mScreenImageReader.getSurface(), null, null);
    }

    public void stopScreenCapture() {
        this.mDisplayWidth = 0;
        this.mDisplayHeight = 0;
        this.mScreenDensity = 0;
        Mobcrush.stopScreenCapture();
        if (this.mScreenImageReader != null) {
            this.mScreenImageReader.close();
            this.mScreenImageReader = null;
        }
        if (this.mVirtualDisplay != null) {
            this.mVirtualDisplay.release();
            this.mVirtualDisplay = null;
        }
    }
}
