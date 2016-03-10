package com.google.android.exoplayer.util;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.exoplayer.MediaFormat;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.R;
import org.apache.http.HttpStatus;

public final class Ac3Util {
    private static final int[] BITRATES = new int[]{32, 40, 48, 56, 64, 80, 96, 112, AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS, 160, 192, 224, AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY, 320, 384, 448, AccessibilityNodeInfoCompat.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY, 576, Constants.MAX_PROFILE_IMAGE_SIZE};
    private static final int[] CHANNEL_COUNTS = new int[]{2, 1, 2, 3, 3, 4, 4, 5};
    private static final int[] FRMSIZECOD_TO_FRAME_SIZE_44_1 = new int[]{69, 87, R.styleable.Theme_radioButtonStyle, 121, 139, 174, 208, 243, 278, 348, HttpStatus.SC_EXPECTATION_FAILED, 487, 557, 696, 835, 975, 1114, 1253, 1393};
    private static final int[] SAMPLE_RATES = new int[]{48000, 44100, 32000};

    public static MediaFormat parseAnnexFAc3Format(ParsableByteArray data) {
        int sampleRate = SAMPLE_RATES[(data.readUnsignedByte() & 192) >> 6];
        int nextByte = data.readUnsignedByte();
        int channelCount = CHANNEL_COUNTS[(nextByte & 56) >> 3];
        if ((nextByte & 4) != 0) {
            channelCount++;
        }
        return MediaFormat.createAudioFormat(MimeTypes.AUDIO_AC3, -1, channelCount, sampleRate, null);
    }

    public static MediaFormat parseAnnexFEAc3Format(ParsableByteArray data) {
        data.skipBytes(2);
        int sampleRate = SAMPLE_RATES[(data.readUnsignedByte() & 192) >> 6];
        int nextByte = data.readUnsignedByte();
        int channelCount = CHANNEL_COUNTS[(nextByte & 14) >> 1];
        if ((nextByte & 1) != 0) {
            channelCount++;
        }
        return MediaFormat.createAudioFormat(MimeTypes.AUDIO_EC3, -1, channelCount, sampleRate, null);
    }

    public static MediaFormat parseFrameAc3Format(ParsableBitArray data) {
        int i = 1;
        data.skipBits(32);
        int fscod = data.readBits(2);
        data.skipBits(14);
        int acmod = data.readBits(3);
        if (!((acmod & 1) == 0 || acmod == 1)) {
            data.skipBits(2);
        }
        if ((acmod & 4) != 0) {
            data.skipBits(2);
        }
        if (acmod == 2) {
            data.skipBits(2);
        }
        boolean lfeon = data.readBit();
        String str = MimeTypes.AUDIO_AC3;
        int i2 = CHANNEL_COUNTS[acmod];
        if (!lfeon) {
            i = 0;
        }
        return MediaFormat.createAudioFormat(str, -1, i + i2, SAMPLE_RATES[fscod], null);
    }

    public static int parseFrameSize(ParsableBitArray data) {
        data.skipBits(32);
        int fscod = data.readBits(2);
        int frmsizecod = data.readBits(6);
        int sampleRate = SAMPLE_RATES[fscod];
        int bitrate = BITRATES[frmsizecod / 2];
        if (sampleRate == 32000) {
            return bitrate * 6;
        }
        if (sampleRate == 44100) {
            return (FRMSIZECOD_TO_FRAME_SIZE_44_1[frmsizecod / 2] + (frmsizecod % 2)) * 2;
        }
        return bitrate * 4;
    }

    public static int getBitrate(int bufferSize, int sampleRate) {
        return (768000 + ((bufferSize * 8) * sampleRate)) / 1536000;
    }

    private Ac3Util() {
    }
}
