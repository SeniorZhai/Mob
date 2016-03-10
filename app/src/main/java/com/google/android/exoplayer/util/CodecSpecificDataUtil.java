package com.google.android.exoplayer.util;

import android.annotation.SuppressLint;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Pair;
import com.facebook.internal.Utility;
import com.mobcrush.mobcrush.R;
import com.nostra13.universalimageloader.utils.IoUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

public final class CodecSpecificDataUtil {
    private static final int[] AUDIO_SPECIFIC_CONFIG_CHANNEL_COUNT_TABLE = new int[]{0, 1, 2, 3, 4, 5, 6, 8};
    private static final int[] AUDIO_SPECIFIC_CONFIG_SAMPLING_RATE_TABLE = new int[]{96000, 88200, SettingsJsonConstants.SETTINGS_LOG_BUFFER_SIZE_DEFAULT, 48000, 44100, 32000, 24000, 22050, 16000, 12000, 11025, SettingsJsonConstants.ANALYTICS_MAX_BYTE_SIZE_PER_FILE_DEFAULT, 7350};
    private static final byte[] NAL_START_CODE = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1};
    private static final int SPS_NAL_UNIT_TYPE = 7;

    private CodecSpecificDataUtil() {
    }

    public static Pair<Integer, Integer> parseAacAudioSpecificConfig(byte[] audioSpecificConfig) {
        int byteOffset;
        boolean z = true;
        int audioObjectType = (audioSpecificConfig[0] >> 3) & 31;
        if (audioObjectType == 5 || audioObjectType == 29) {
            byteOffset = 1;
        } else {
            byteOffset = 0;
        }
        int frequencyIndex = ((audioSpecificConfig[byteOffset] & SPS_NAL_UNIT_TYPE) << 1) | ((audioSpecificConfig[byteOffset + 1] >> SPS_NAL_UNIT_TYPE) & 1);
        if (frequencyIndex >= 13) {
            z = false;
        }
        Assertions.checkState(z);
        return Pair.create(Integer.valueOf(AUDIO_SPECIFIC_CONFIG_SAMPLING_RATE_TABLE[frequencyIndex]), Integer.valueOf((audioSpecificConfig[byteOffset + 1] >> 3) & 15));
    }

    public static byte[] buildAacAudioSpecificConfig(int audioObjectType, int sampleRateIndex, int channelConfig) {
        return new byte[]{(byte) (((audioObjectType << 3) & 248) | ((sampleRateIndex >> 1) & SPS_NAL_UNIT_TYPE)), (byte) (((sampleRateIndex << SPS_NAL_UNIT_TYPE) & AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) | ((channelConfig << 3) & 120))};
    }

    public static byte[] buildAacAudioSpecificConfig(int sampleRate, int numChannels) {
        int i;
        int sampleRateIndex = -1;
        for (i = 0; i < AUDIO_SPECIFIC_CONFIG_SAMPLING_RATE_TABLE.length; i++) {
            if (sampleRate == AUDIO_SPECIFIC_CONFIG_SAMPLING_RATE_TABLE[i]) {
                sampleRateIndex = i;
            }
        }
        int channelConfig = -1;
        for (i = 0; i < AUDIO_SPECIFIC_CONFIG_CHANNEL_COUNT_TABLE.length; i++) {
            if (numChannels == AUDIO_SPECIFIC_CONFIG_CHANNEL_COUNT_TABLE[i]) {
                channelConfig = i;
            }
        }
        return new byte[]{(byte) ((sampleRateIndex >> 1) | 16), (byte) (((sampleRateIndex & 1) << SPS_NAL_UNIT_TYPE) | (channelConfig << 3))};
    }

    public static byte[] buildNalUnit(byte[] data, int offset, int length) {
        byte[] nalUnit = new byte[(NAL_START_CODE.length + length)];
        System.arraycopy(NAL_START_CODE, 0, nalUnit, 0, NAL_START_CODE.length);
        System.arraycopy(data, offset, nalUnit, NAL_START_CODE.length, length);
        return nalUnit;
    }

    public static byte[][] splitNalUnits(byte[] data) {
        if (!isNalStartCode(data, 0)) {
            return (byte[][]) null;
        }
        List<Integer> starts = new ArrayList();
        int nalUnitIndex = 0;
        do {
            starts.add(Integer.valueOf(nalUnitIndex));
            nalUnitIndex = findNalStartCode(data, NAL_START_CODE.length + nalUnitIndex);
        } while (nalUnitIndex != -1);
        byte[][] split = new byte[starts.size()][];
        int i = 0;
        while (i < starts.size()) {
            int startIndex = ((Integer) starts.get(i)).intValue();
            byte[] nal = new byte[((i < starts.size() + -1 ? ((Integer) starts.get(i + 1)).intValue() : data.length) - startIndex)];
            System.arraycopy(data, startIndex, nal, 0, nal.length);
            split[i] = nal;
            i++;
        }
        return split;
    }

    private static int findNalStartCode(byte[] data, int index) {
        int endIndex = data.length - NAL_START_CODE.length;
        for (int i = index; i <= endIndex; i++) {
            if (isNalStartCode(data, i)) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isNalStartCode(byte[] data, int index) {
        if (data.length - index <= NAL_START_CODE.length) {
            return false;
        }
        for (int j = 0; j < NAL_START_CODE.length; j++) {
            if (data[index + j] != NAL_START_CODE[j]) {
                return false;
            }
        }
        return true;
    }

    public static Pair<Integer, Integer> parseSpsNalUnit(byte[] spsNalUnit) {
        if (isNalStartCode(spsNalUnit, 0) && spsNalUnit.length == 8 && (spsNalUnit[5] & 31) == SPS_NAL_UNIT_TYPE) {
            return Pair.create(Integer.valueOf(parseAvcProfile(spsNalUnit)), Integer.valueOf(parseAvcLevel(spsNalUnit)));
        }
        return null;
    }

    @SuppressLint({"InlinedApi"})
    private static int parseAvcProfile(byte[] data) {
        switch (data[6] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
            case R.styleable.Theme_textColorSearchUrl /*66*/:
                return 1;
            case R.styleable.Theme_panelBackground /*77*/:
                return 2;
            case R.styleable.Theme_colorSwitchThumbNormal /*88*/:
                return 4;
            case HttpStatus.SC_CONTINUE /*100*/:
                return 8;
            case 110:
                return 16;
            case 122:
                return 32;
            case 244:
                return 64;
            default:
                return 0;
        }
    }

    @SuppressLint({"InlinedApi"})
    private static int parseAvcLevel(byte[] data) {
        switch (data[8] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
            case HTTP.HT /*9*/:
                return 2;
            case HTTP.LF /*10*/:
                return 1;
            case R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                return 4;
            case R.styleable.Toolbar_titleMargins /*12*/:
                return 8;
            case HTTP.CR /*13*/:
                return 16;
            case R.styleable.Toolbar_navigationIcon /*20*/:
                return 32;
            case R.styleable.Toolbar_navigationContentDescription /*21*/:
                return 64;
            case R.styleable.Toolbar_logoDescription /*22*/:
                return AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS;
            case R.styleable.Theme_actionModeSplitBackground /*30*/:
                return AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
            case R.styleable.Theme_actionModeCloseDrawable /*31*/:
                return AccessibilityNodeInfoCompat.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY;
            case HTTP.SP /*32*/:
                return AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT;
            case R.styleable.Theme_textAppearanceLargePopupMenu /*40*/:
                return AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT;
            case R.styleable.Theme_textAppearanceSmallPopupMenu /*41*/:
                return MpegAudioHeader.MAX_FRAME_SIZE_BYTES;
            case R.styleable.Theme_dialogTheme /*42*/:
                return Utility.DEFAULT_STREAM_BUFFER_SIZE;
            case R.styleable.Theme_buttonBarStyle /*50*/:
                return AccessibilityNodeInfoCompat.ACTION_COPY;
            case R.styleable.Theme_buttonBarButtonStyle /*51*/:
                return IoUtils.DEFAULT_BUFFER_SIZE;
            default:
                return 0;
        }
    }
}
