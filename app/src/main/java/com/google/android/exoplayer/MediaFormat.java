package com.google.android.exoplayer;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import com.android.volley.DefaultRetryPolicy;
import com.google.android.exoplayer.util.Util;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MediaFormat {
    private static final String KEY_PIXEL_WIDTH_HEIGHT_RATIO = "com.google.android.videos.pixelWidthHeightRatio";
    public static final int NO_VALUE = -1;
    public final int channelCount;
    public final long durationUs;
    private android.media.MediaFormat frameworkMediaFormat;
    private int hashCode;
    public final int height;
    public final List<byte[]> initializationData;
    private int maxHeight;
    public final int maxInputSize;
    private int maxWidth;
    public final String mimeType;
    public final float pixelWidthHeightRatio;
    public final int sampleRate;
    public final int width;

    @TargetApi(16)
    public static MediaFormat createFromFrameworkMediaFormatV16(android.media.MediaFormat format) {
        return new MediaFormat(format);
    }

    public static MediaFormat createVideoFormat(String mimeType, int maxInputSize, int width, int height, List<byte[]> initializationData) {
        return createVideoFormat(mimeType, maxInputSize, -1, width, height, initializationData);
    }

    public static MediaFormat createVideoFormat(String mimeType, int maxInputSize, long durationUs, int width, int height, List<byte[]> initializationData) {
        return createVideoFormat(mimeType, maxInputSize, durationUs, width, height, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, initializationData);
    }

    public static MediaFormat createVideoFormat(String mimeType, int maxInputSize, long durationUs, int width, int height, float pixelWidthHeightRatio, List<byte[]> initializationData) {
        return new MediaFormat(mimeType, maxInputSize, durationUs, width, height, pixelWidthHeightRatio, NO_VALUE, NO_VALUE, initializationData);
    }

    public static MediaFormat createAudioFormat(String mimeType, int maxInputSize, int channelCount, int sampleRate, List<byte[]> initializationData) {
        return createAudioFormat(mimeType, maxInputSize, -1, channelCount, sampleRate, initializationData);
    }

    public static MediaFormat createAudioFormat(String mimeType, int maxInputSize, long durationUs, int channelCount, int sampleRate, List<byte[]> initializationData) {
        return new MediaFormat(mimeType, maxInputSize, durationUs, NO_VALUE, NO_VALUE, -1.0f, channelCount, sampleRate, initializationData);
    }

    public static MediaFormat createTextFormat(String mimeType) {
        return createFormatForMimeType(mimeType);
    }

    public static MediaFormat createFormatForMimeType(String mimeType) {
        return new MediaFormat(mimeType, NO_VALUE, -1, NO_VALUE, NO_VALUE, -1.0f, NO_VALUE, NO_VALUE, null);
    }

    @TargetApi(16)
    private MediaFormat(android.media.MediaFormat format) {
        this.frameworkMediaFormat = format;
        this.mimeType = format.getString("mime");
        this.maxInputSize = getOptionalIntegerV16(format, "max-input-size");
        this.width = getOptionalIntegerV16(format, SettingsJsonConstants.ICON_WIDTH_KEY);
        this.height = getOptionalIntegerV16(format, SettingsJsonConstants.ICON_HEIGHT_KEY);
        this.channelCount = getOptionalIntegerV16(format, "channel-count");
        this.sampleRate = getOptionalIntegerV16(format, "sample-rate");
        this.pixelWidthHeightRatio = getOptionalFloatV16(format, KEY_PIXEL_WIDTH_HEIGHT_RATIO);
        this.initializationData = new ArrayList();
        for (int i = 0; format.containsKey("csd-" + i); i++) {
            ByteBuffer buffer = format.getByteBuffer("csd-" + i);
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            this.initializationData.add(data);
            buffer.flip();
        }
        this.durationUs = format.containsKey("durationUs") ? format.getLong("durationUs") : -1;
        this.maxWidth = NO_VALUE;
        this.maxHeight = NO_VALUE;
    }

    private MediaFormat(String mimeType, int maxInputSize, long durationUs, int width, int height, float pixelWidthHeightRatio, int channelCount, int sampleRate, List<byte[]> initializationData) {
        this.mimeType = mimeType;
        this.maxInputSize = maxInputSize;
        this.durationUs = durationUs;
        this.width = width;
        this.height = height;
        this.pixelWidthHeightRatio = pixelWidthHeightRatio;
        this.channelCount = channelCount;
        this.sampleRate = sampleRate;
        if (initializationData == null) {
            initializationData = Collections.emptyList();
        }
        this.initializationData = initializationData;
        this.maxWidth = NO_VALUE;
        this.maxHeight = NO_VALUE;
    }

    public void setMaxVideoDimensions(int maxWidth, int maxHeight) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        if (this.frameworkMediaFormat != null) {
            maybeSetMaxDimensionsV16(this.frameworkMediaFormat);
        }
    }

    public int getMaxVideoWidth() {
        return this.maxWidth;
    }

    public int getMaxVideoHeight() {
        return this.maxHeight;
    }

    public int hashCode() {
        if (this.hashCode == 0) {
            int i;
            if (this.mimeType == null) {
                i = 0;
            } else {
                i = this.mimeType.hashCode();
            }
            int result = ((((((((((((((((((i + 527) * 31) + this.maxInputSize) * 31) + this.width) * 31) + this.height) * 31) + Float.floatToRawIntBits(this.pixelWidthHeightRatio)) * 31) + ((int) this.durationUs)) * 31) + this.maxWidth) * 31) + this.maxHeight) * 31) + this.channelCount) * 31) + this.sampleRate;
            for (int i2 = 0; i2 < this.initializationData.size(); i2++) {
                result = (result * 31) + Arrays.hashCode((byte[]) this.initializationData.get(i2));
            }
            this.hashCode = result;
        }
        return this.hashCode;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        return equalsInternal((MediaFormat) obj, false);
    }

    public boolean equals(MediaFormat other, boolean ignoreMaxDimensions) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        return equalsInternal(other, ignoreMaxDimensions);
    }

    private boolean equalsInternal(MediaFormat other, boolean ignoreMaxDimensions) {
        if (this.maxInputSize != other.maxInputSize || this.width != other.width || this.height != other.height || this.pixelWidthHeightRatio != other.pixelWidthHeightRatio || ((!ignoreMaxDimensions && (this.maxWidth != other.maxWidth || this.maxHeight != other.maxHeight)) || this.channelCount != other.channelCount || this.sampleRate != other.sampleRate || !Util.areEqual(this.mimeType, other.mimeType) || this.initializationData.size() != other.initializationData.size())) {
            return false;
        }
        for (int i = 0; i < this.initializationData.size(); i++) {
            if (!Arrays.equals((byte[]) this.initializationData.get(i), (byte[]) other.initializationData.get(i))) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "MediaFormat(" + this.mimeType + ", " + this.maxInputSize + ", " + this.width + ", " + this.height + ", " + this.pixelWidthHeightRatio + ", " + this.channelCount + ", " + this.sampleRate + ", " + this.durationUs + ", " + this.maxWidth + ", " + this.maxHeight + ")";
    }

    @TargetApi(16)
    public final android.media.MediaFormat getFrameworkMediaFormatV16() {
        if (this.frameworkMediaFormat == null) {
            android.media.MediaFormat format = new android.media.MediaFormat();
            format.setString("mime", this.mimeType);
            maybeSetIntegerV16(format, "max-input-size", this.maxInputSize);
            maybeSetIntegerV16(format, SettingsJsonConstants.ICON_WIDTH_KEY, this.width);
            maybeSetIntegerV16(format, SettingsJsonConstants.ICON_HEIGHT_KEY, this.height);
            maybeSetIntegerV16(format, "channel-count", this.channelCount);
            maybeSetIntegerV16(format, "sample-rate", this.sampleRate);
            maybeSetFloatV16(format, KEY_PIXEL_WIDTH_HEIGHT_RATIO, this.pixelWidthHeightRatio);
            for (int i = 0; i < this.initializationData.size(); i++) {
                format.setByteBuffer("csd-" + i, ByteBuffer.wrap((byte[]) this.initializationData.get(i)));
            }
            if (this.durationUs != -1) {
                format.setLong("durationUs", this.durationUs);
            }
            maybeSetMaxDimensionsV16(format);
            this.frameworkMediaFormat = format;
        }
        return this.frameworkMediaFormat;
    }

    @SuppressLint({"InlinedApi"})
    @TargetApi(16)
    private final void maybeSetMaxDimensionsV16(android.media.MediaFormat format) {
        maybeSetIntegerV16(format, "max-width", this.maxWidth);
        maybeSetIntegerV16(format, "max-height", this.maxHeight);
    }

    @TargetApi(16)
    private static final void maybeSetIntegerV16(android.media.MediaFormat format, String key, int value) {
        if (value != NO_VALUE) {
            format.setInteger(key, value);
        }
    }

    @TargetApi(16)
    private static final void maybeSetFloatV16(android.media.MediaFormat format, String key, float value) {
        if (value != -1.0f) {
            format.setFloat(key, value);
        }
    }

    @TargetApi(16)
    private static final int getOptionalIntegerV16(android.media.MediaFormat format, String key) {
        return format.containsKey(key) ? format.getInteger(key) : NO_VALUE;
    }

    @TargetApi(16)
    private static final float getOptionalFloatV16(android.media.MediaFormat format, String key) {
        return format.containsKey(key) ? format.getFloat(key) : -1.0f;
    }
}
