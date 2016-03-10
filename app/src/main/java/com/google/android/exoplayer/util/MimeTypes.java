package com.google.android.exoplayer.util;

public class MimeTypes {
    public static final String APPLICATION_EIA608 = "application/eia-608";
    public static final String APPLICATION_ID3 = "application/id3";
    public static final String APPLICATION_M3U8 = "application/x-mpegURL";
    public static final String APPLICATION_SUBRIP = "application/x-subrip";
    public static final String APPLICATION_TTML = "application/ttml+xml";
    public static final String APPLICATION_TX3G = "application/x-quicktime-tx3g";
    public static final String AUDIO_AAC = "audio/mp4a-latm";
    public static final String AUDIO_AC3 = "audio/ac3";
    public static final String AUDIO_EC3 = "audio/eac3";
    public static final String AUDIO_MP4 = "audio/mp4";
    public static final String AUDIO_MPEG = "audio/mpeg";
    public static final String AUDIO_MPEG_L1 = "audio/mpeg-L1";
    public static final String AUDIO_MPEG_L2 = "audio/mpeg-L2";
    public static final String AUDIO_OPUS = "audio/opus";
    public static final String AUDIO_RAW = "audio/raw";
    public static final String AUDIO_VORBIS = "audio/vorbis";
    public static final String AUDIO_WEBM = "audio/webm";
    public static final String BASE_TYPE_APPLICATION = "application";
    public static final String BASE_TYPE_AUDIO = "audio";
    public static final String BASE_TYPE_TEXT = "text";
    public static final String BASE_TYPE_VIDEO = "video";
    public static final String TEXT_VTT = "text/vtt";
    public static final String VIDEO_H264 = "video/avc";
    public static final String VIDEO_H265 = "video/hevc";
    public static final String VIDEO_MP4 = "video/mp4";
    public static final String VIDEO_MP4V = "video/mp4v-es";
    public static final String VIDEO_VP8 = "video/x-vnd.on2.vp8";
    public static final String VIDEO_VP9 = "video/x-vnd.on2.vp9";
    public static final String VIDEO_WEBM = "video/webm";

    private MimeTypes() {
    }

    public static String getTopLevelType(String mimeType) {
        int indexOfSlash = mimeType.indexOf(47);
        if (indexOfSlash != -1) {
            return mimeType.substring(0, indexOfSlash);
        }
        throw new IllegalArgumentException("Invalid mime type: " + mimeType);
    }

    public static boolean isAudio(String mimeType) {
        return getTopLevelType(mimeType).equals(BASE_TYPE_AUDIO);
    }

    public static boolean isVideo(String mimeType) {
        return getTopLevelType(mimeType).equals(BASE_TYPE_VIDEO);
    }

    public static boolean isText(String mimeType) {
        return getTopLevelType(mimeType).equals(BASE_TYPE_TEXT);
    }

    public static boolean isApplication(String mimeType) {
        return getTopLevelType(mimeType).equals(BASE_TYPE_APPLICATION);
    }

    public static boolean isTtml(String mimeType) {
        return mimeType.equals(APPLICATION_TTML);
    }

    public static int getEncodingForMimeType(String mimeType) {
        if (AUDIO_AC3.equals(mimeType)) {
            return 5;
        }
        if (AUDIO_EC3.equals(mimeType)) {
            return 6;
        }
        return isAudio(mimeType) ? 2 : 0;
    }

    public static boolean isPassthroughAudio(String mimeType) {
        return AUDIO_AC3.equals(mimeType) || AUDIO_EC3.equals(mimeType);
    }
}
