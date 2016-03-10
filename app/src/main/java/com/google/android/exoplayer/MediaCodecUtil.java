package com.google.android.exoplayer;

import android.annotation.TargetApi;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaCodecInfo.VideoCapabilities;
import android.media.MediaCodecList;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.facebook.internal.Utility;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.MpegAudioHeader;
import com.google.android.exoplayer.util.Util;
import com.nostra13.universalimageloader.utils.IoUtils;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.HashMap;
import org.apache.http.protocol.HTTP;

@TargetApi(16)
public class MediaCodecUtil {
    private static final String TAG = "MediaCodecUtil";
    private static final HashMap<CodecKey, Pair<String, CodecCapabilities>> codecs = new HashMap();

    private static final class CodecKey {
        public final String mimeType;
        public final boolean secure;

        public CodecKey(String mimeType, boolean secure) {
            this.mimeType = mimeType;
            this.secure = secure;
        }

        public int hashCode() {
            return (((this.mimeType == null ? 0 : this.mimeType.hashCode()) + 31) * 31) + (this.secure ? 1231 : 1237);
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || obj.getClass() != CodecKey.class) {
                return false;
            }
            CodecKey other = (CodecKey) obj;
            if (TextUtils.equals(this.mimeType, other.mimeType) && this.secure == other.secure) {
                return true;
            }
            return false;
        }
    }

    public static class DecoderQueryException extends Exception {
        private DecoderQueryException(Throwable cause) {
            super("Failed to query underlying media codecs", cause);
        }
    }

    private interface MediaCodecListCompat {
        int getCodecCount();

        MediaCodecInfo getCodecInfoAt(int i);

        boolean isSecurePlaybackSupported(String str, CodecCapabilities codecCapabilities);

        boolean secureDecodersExplicit();
    }

    private static final class MediaCodecListCompatV16 implements MediaCodecListCompat {
        private MediaCodecListCompatV16() {
        }

        public int getCodecCount() {
            return MediaCodecList.getCodecCount();
        }

        public MediaCodecInfo getCodecInfoAt(int index) {
            return MediaCodecList.getCodecInfoAt(index);
        }

        public boolean secureDecodersExplicit() {
            return false;
        }

        public boolean isSecurePlaybackSupported(String mimeType, CodecCapabilities capabilities) {
            return MimeTypes.VIDEO_H264.equals(mimeType);
        }
    }

    @TargetApi(21)
    private static final class MediaCodecListCompatV21 implements MediaCodecListCompat {
        private final int codecKind;
        private MediaCodecInfo[] mediaCodecInfos;

        public MediaCodecListCompatV21(boolean includeSecure) {
            this.codecKind = includeSecure ? 1 : 0;
        }

        public int getCodecCount() {
            ensureMediaCodecInfosInitialized();
            return this.mediaCodecInfos.length;
        }

        public MediaCodecInfo getCodecInfoAt(int index) {
            ensureMediaCodecInfosInitialized();
            return this.mediaCodecInfos[index];
        }

        public boolean secureDecodersExplicit() {
            return true;
        }

        public boolean isSecurePlaybackSupported(String mimeType, CodecCapabilities capabilities) {
            return capabilities.isFeatureSupported("secure-playback");
        }

        private void ensureMediaCodecInfosInitialized() {
            if (this.mediaCodecInfos == null) {
                this.mediaCodecInfos = new MediaCodecList(this.codecKind).getCodecInfos();
            }
        }
    }

    public static DecoderInfo getDecoderInfo(String mimeType, boolean secure) throws DecoderQueryException {
        Pair<String, CodecCapabilities> info = getMediaCodecInfo(mimeType, secure);
        if (info == null) {
            return null;
        }
        return new DecoderInfo((String) info.first, isAdaptive((CodecCapabilities) info.second));
    }

    public static synchronized void warmCodec(String mimeType, boolean secure) {
        synchronized (MediaCodecUtil.class) {
            try {
                getMediaCodecInfo(mimeType, secure);
            } catch (DecoderQueryException e) {
                Log.e(TAG, "Codec warming failed", e);
            }
        }
    }

    private static synchronized Pair<String, CodecCapabilities> getMediaCodecInfo(String mimeType, boolean secure) throws DecoderQueryException {
        Pair<String, CodecCapabilities> pair;
        synchronized (MediaCodecUtil.class) {
            CodecKey key = new CodecKey(mimeType, secure);
            if (codecs.containsKey(key)) {
                pair = (Pair) codecs.get(key);
            } else {
                Pair<String, CodecCapabilities> codecInfo = getMediaCodecInfo(key, Util.SDK_INT >= 21 ? new MediaCodecListCompatV21(secure) : new MediaCodecListCompatV16());
                if (secure && codecInfo == null && Util.SDK_INT >= 21) {
                    codecInfo = getMediaCodecInfo(key, new MediaCodecListCompatV16());
                    if (codecInfo != null) {
                        Log.w(TAG, "MediaCodecList API didn't list secure decoder for: " + mimeType + ". Assuming: " + ((String) codecInfo.first));
                    }
                }
                pair = codecInfo;
            }
        }
        return pair;
    }

    private static Pair<String, CodecCapabilities> getMediaCodecInfo(CodecKey key, MediaCodecListCompat mediaCodecList) throws DecoderQueryException {
        try {
            return getMediaCodecInfoInternal(key, mediaCodecList);
        } catch (Exception e) {
            throw new DecoderQueryException(e);
        }
    }

    private static Pair<String, CodecCapabilities> getMediaCodecInfoInternal(CodecKey key, MediaCodecListCompat mediaCodecList) {
        String mimeType = key.mimeType;
        int numberOfCodecs = mediaCodecList.getCodecCount();
        boolean secureDecodersExplicit = mediaCodecList.secureDecodersExplicit();
        for (int i = 0; i < numberOfCodecs; i++) {
            MediaCodecInfo info = mediaCodecList.getCodecInfoAt(i);
            String codecName = info.getName();
            if (isCodecUsableDecoder(info, codecName, secureDecodersExplicit)) {
                String[] supportedTypes = info.getSupportedTypes();
                for (String supportedType : supportedTypes) {
                    if (supportedType.equalsIgnoreCase(mimeType)) {
                        CodecCapabilities capabilities = info.getCapabilitiesForType(supportedType);
                        boolean secure = mediaCodecList.isSecurePlaybackSupported(key.mimeType, capabilities);
                        if (secureDecodersExplicit) {
                            codecs.put(key.secure == secure ? key : new CodecKey(mimeType, secure), Pair.create(codecName, capabilities));
                        } else {
                            Object codecKey;
                            HashMap hashMap = codecs;
                            if (key.secure) {
                                codecKey = new CodecKey(mimeType, false);
                            } else {
                                CodecKey codecKey2 = key;
                            }
                            hashMap.put(codecKey, Pair.create(codecName, capabilities));
                            if (secure) {
                                codecs.put(key.secure ? key : new CodecKey(mimeType, true), Pair.create(codecName + ".secure", capabilities));
                            }
                        }
                        if (codecs.containsKey(key)) {
                            return (Pair) codecs.get(key);
                        }
                    }
                }
                continue;
            }
        }
        return null;
    }

    private static boolean isCodecUsableDecoder(MediaCodecInfo info, String name, boolean secureDecodersExplicit) {
        if (info.isEncoder() || !name.startsWith("OMX.")) {
            return false;
        }
        if (!secureDecodersExplicit && name.endsWith(".secure")) {
            return false;
        }
        if (Util.SDK_INT == 16 && (("dlxu".equals(Util.DEVICE) || "protou".equals(Util.DEVICE) || "C6602".equals(Util.DEVICE) || "C6603".equals(Util.DEVICE)) && name.equals("OMX.qcom.audio.decoder.mp3"))) {
            return false;
        }
        if (Util.SDK_INT <= 19 && Util.DEVICE != null && Util.DEVICE.startsWith("serrano") && "samsung".equals(Util.MANUFACTURER) && name.equals("OMX.SEC.vp8.dec")) {
            return false;
        }
        return true;
    }

    private static boolean isAdaptive(CodecCapabilities capabilities) {
        if (Util.SDK_INT >= 19) {
            return isAdaptiveV19(capabilities);
        }
        return false;
    }

    @TargetApi(19)
    private static boolean isAdaptiveV19(CodecCapabilities capabilities) {
        return capabilities.isFeatureSupported("adaptive-playback");
    }

    @TargetApi(21)
    public static boolean isSizeAndRateSupportedV21(String mimeType, boolean secure, int width, int height, double frameRate) throws DecoderQueryException {
        boolean z;
        boolean z2 = true;
        if (Util.SDK_INT >= 21) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        Pair<String, CodecCapabilities> info = getMediaCodecInfo(mimeType, secure);
        if (info == null) {
            return false;
        }
        VideoCapabilities videoCapabilities = ((CodecCapabilities) info.second).getVideoCapabilities();
        if (videoCapabilities == null || !videoCapabilities.areSizeAndRateSupported(width, height, frameRate)) {
            z2 = false;
        }
        return z2;
    }

    public static boolean isH264ProfileSupported(int profile, int level) throws DecoderQueryException {
        Pair<String, CodecCapabilities> info = getMediaCodecInfo(MimeTypes.VIDEO_H264, false);
        if (info == null) {
            return false;
        }
        CodecCapabilities capabilities = info.second;
        for (CodecProfileLevel profileLevel : capabilities.profileLevels) {
            if (profileLevel.profile == profile && profileLevel.level >= level) {
                return true;
            }
        }
        return false;
    }

    public static int maxH264DecodableFrameSize() throws DecoderQueryException {
        int i = 0;
        Pair<String, CodecCapabilities> info = getMediaCodecInfo(MimeTypes.VIDEO_H264, false);
        if (info != null) {
            i = 0;
            CodecCapabilities capabilities = info.second;
            for (CodecProfileLevel profileLevel : capabilities.profileLevels) {
                i = Math.max(avcLevelToMaxFrameSize(profileLevel.level), i);
            }
        }
        return i;
    }

    private static int avcLevelToMaxFrameSize(int avcLevel) {
        switch (avcLevel) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return 25344;
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                return 101376;
            case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                return 101376;
            case HTTP.SP /*32*/:
                return 101376;
            case SettingsJsonConstants.SETTINGS_MAX_CUSTOM_KEY_VALUE_PAIRS_DEFAULT /*64*/:
                return 202752;
            case AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS /*128*/:
                return 414720;
            case AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY /*256*/:
                return 414720;
            case AccessibilityNodeInfoCompat.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY /*512*/:
                return 921600;
            case AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT /*1024*/:
                return 1310720;
            case AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT /*2048*/:
                return AccessibilityNodeInfoCompat.ACTION_SET_TEXT;
            case MpegAudioHeader.MAX_FRAME_SIZE_BYTES /*4096*/:
                return AccessibilityNodeInfoCompat.ACTION_SET_TEXT;
            case Utility.DEFAULT_STREAM_BUFFER_SIZE /*8192*/:
                return 2228224;
            case AccessibilityNodeInfoCompat.ACTION_COPY /*16384*/:
                return 5652480;
            case IoUtils.DEFAULT_BUFFER_SIZE /*32768*/:
                return 9437184;
            default:
                return -1;
        }
    }
}
