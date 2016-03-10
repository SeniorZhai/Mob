package com.google.android.exoplayer.chunk;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;
import com.google.android.exoplayer.MediaCodecUtil;
import com.google.android.exoplayer.MediaCodecUtil.DecoderQueryException;
import com.google.android.exoplayer.util.Util;
import java.util.ArrayList;
import java.util.List;

public final class VideoFormatSelectorUtil {
    private static final float FRACTION_TO_CONSIDER_FULLSCREEN = 0.98f;

    public static int[] selectVideoFormatsForDefaultDisplay(Context context, List<? extends FormatWrapper> formatWrappers, String[] allowedContainerMimeTypes, boolean filterHdFormats) throws DecoderQueryException {
        Point displaySize = getDisplaySize(((WindowManager) context.getSystemService("window")).getDefaultDisplay());
        return selectVideoFormats(formatWrappers, allowedContainerMimeTypes, filterHdFormats, true, displaySize.x, displaySize.y);
    }

    public static int[] selectVideoFormats(List<? extends FormatWrapper> formatWrappers, String[] allowedContainerMimeTypes, boolean filterHdFormats, boolean orientationMayChange, int viewportWidth, int viewportHeight) throws DecoderQueryException {
        int i;
        int maxVideoPixelsToRetain = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
        ArrayList<Integer> selectedIndexList = new ArrayList();
        int maxDecodableFrameSize = MediaCodecUtil.maxH264DecodableFrameSize();
        int formatWrapperCount = formatWrappers.size();
        for (i = 0; i < formatWrapperCount; i++) {
            Format format = ((FormatWrapper) formatWrappers.get(i)).getFormat();
            if (isFormatPlayable(format, allowedContainerMimeTypes, filterHdFormats, maxDecodableFrameSize)) {
                selectedIndexList.add(Integer.valueOf(i));
                if (format.width > 0 && format.height > 0) {
                    Point maxVideoSizeInViewport = getMaxVideoSizeInViewport(orientationMayChange, viewportWidth, viewportHeight, format.width, format.height);
                    int videoPixels = format.width * format.height;
                    if (format.width >= ((int) (((float) maxVideoSizeInViewport.x) * FRACTION_TO_CONSIDER_FULLSCREEN)) && format.height >= ((int) (((float) maxVideoSizeInViewport.y) * FRACTION_TO_CONSIDER_FULLSCREEN)) && videoPixels < maxVideoPixelsToRetain) {
                        maxVideoPixelsToRetain = videoPixels;
                    }
                }
            }
        }
        for (i = selectedIndexList.size() - 1; i >= 0; i--) {
            format = ((FormatWrapper) formatWrappers.get(i)).getFormat();
            if (format.width > 0 && format.height > 0 && format.width * format.height > maxVideoPixelsToRetain) {
                selectedIndexList.remove(i);
            }
        }
        return Util.toArray(selectedIndexList);
    }

    private static boolean isFormatPlayable(Format format, String[] allowedContainerMimeTypes, boolean filterHdFormats, int maxDecodableFrameSize) {
        if (allowedContainerMimeTypes != null && !Util.contains(allowedContainerMimeTypes, format.mimeType)) {
            return false;
        }
        if (filterHdFormats && (format.width >= 1280 || format.height >= 720)) {
            return false;
        }
        if (format.width <= 0 || format.height <= 0 || format.width * format.height <= maxDecodableFrameSize) {
            return true;
        }
        return false;
    }

    private static Point getMaxVideoSizeInViewport(boolean orientationMayChange, int viewportWidth, int viewportHeight, int videoWidth, int videoHeight) {
        Object obj = 1;
        if (orientationMayChange) {
            Object obj2 = videoWidth > videoHeight ? 1 : null;
            if (viewportWidth <= viewportHeight) {
                obj = null;
            }
            if (obj2 != obj) {
                int tempViewportWidth = viewportWidth;
                viewportWidth = viewportHeight;
                viewportHeight = tempViewportWidth;
            }
        }
        if (videoWidth * viewportHeight >= videoHeight * viewportWidth) {
            return new Point(viewportWidth, Util.ceilDivide(viewportWidth * videoHeight, videoWidth));
        }
        return new Point(Util.ceilDivide(viewportHeight * videoWidth, videoHeight), viewportHeight);
    }

    private static Point getDisplaySize(Display display) {
        Point displaySize = new Point();
        if (Util.SDK_INT >= 17) {
            getDisplaySizeV17(display, displaySize);
        } else if (Util.SDK_INT >= 16) {
            getDisplaySizeV16(display, displaySize);
        } else {
            getDisplaySizeV9(display, displaySize);
        }
        return displaySize;
    }

    @TargetApi(17)
    private static void getDisplaySizeV17(Display display, Point outSize) {
        display.getRealSize(outSize);
    }

    @TargetApi(16)
    private static void getDisplaySizeV16(Display display, Point outSize) {
        display.getSize(outSize);
    }

    private static void getDisplaySizeV9(Display display, Point outSize) {
        outSize.x = display.getWidth();
        outSize.y = display.getHeight();
    }

    private VideoFormatSelectorUtil() {
    }
}
