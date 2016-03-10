package com.mobcrush.mobcrush;

import android.media.ImageReader;
import android.view.Surface;
import java.nio.ByteBuffer;

public class Mobcrush {
    public static native void setCameraPixelData(int i, int i2, ByteBuffer byteBuffer, int i3, int i4, ByteBuffer byteBuffer2, int i5, int i6, ByteBuffer byteBuffer3, int i7, int i8);

    public static native void setCameraSurfaceReader(Surface surface, ImageReader imageReader);

    public static native void setMuteMic(boolean z);

    public static native void setPrivacyFilter(boolean z);

    public static native void setScreenPixelData(ByteBuffer byteBuffer, int i, int i2);

    public static native void setScreenSurfaceReader(Surface surface, ImageReader imageReader);

    public static native void setSwap(boolean z);

    public static native void start(int i, int i2, String str);

    public static native void startCamera();

    public static native void stop();

    public static native void stopCamera();

    public static native void stopScreenCapture();

    public static void load() {
        System.loadLibrary("mobcrush");
    }
}
