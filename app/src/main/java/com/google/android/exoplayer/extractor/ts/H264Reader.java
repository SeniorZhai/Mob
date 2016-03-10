package com.google.android.exoplayer.extractor.ts;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import com.android.volley.DefaultRetryPolicy;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.NalUnitUtil;
import com.google.android.exoplayer.util.ParsableBitArray;
import com.google.android.exoplayer.util.ParsableByteArray;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class H264Reader extends ElementaryStreamReader {
    private static final int FRAME_TYPE_ALL_I = 7;
    private static final int FRAME_TYPE_I = 2;
    private static final int NAL_UNIT_TYPE_AUD = 9;
    private static final int NAL_UNIT_TYPE_IDR = 5;
    private static final int NAL_UNIT_TYPE_IFR = 1;
    private static final int NAL_UNIT_TYPE_PPS = 8;
    private static final int NAL_UNIT_TYPE_SEI = 6;
    private static final int NAL_UNIT_TYPE_SPS = 7;
    private static final String TAG = "H264Reader";
    private boolean foundFirstSample;
    private boolean hasOutputFormat;
    private final IfrParserBuffer ifrParserBuffer;
    private boolean isKeyframe;
    private final NalUnitTargetBuffer pps;
    private final boolean[] prefixFlags = new boolean[3];
    private long samplePosition;
    private long sampleTimeUs;
    private final NalUnitTargetBuffer sei;
    private final SeiReader seiReader;
    private final ParsableByteArray seiWrapper;
    private final NalUnitTargetBuffer sps;
    private long totalBytesWritten;

    private static final class IfrParserBuffer {
        private static final int DEFAULT_BUFFER_SIZE = 128;
        private static final int NOT_SET = -1;
        private byte[] ifrData = new byte[DEFAULT_BUFFER_SIZE];
        private int ifrLength;
        private boolean isFilling;
        private final ParsableBitArray scratchSliceType = new ParsableBitArray(this.ifrData);
        private int sliceType;

        public IfrParserBuffer() {
            reset();
        }

        public void reset() {
            this.isFilling = false;
            this.ifrLength = 0;
            this.sliceType = NOT_SET;
        }

        public boolean isCompleted() {
            return this.sliceType != NOT_SET;
        }

        public void startNalUnit(int nalUnitType) {
            if (nalUnitType == H264Reader.NAL_UNIT_TYPE_IFR) {
                reset();
                this.isFilling = true;
            }
        }

        public void appendToNalUnit(byte[] data, int offset, int limit) {
            if (this.isFilling) {
                int readLength = limit - offset;
                if (this.ifrData.length < this.ifrLength + readLength) {
                    this.ifrData = Arrays.copyOf(this.ifrData, (this.ifrLength + readLength) * H264Reader.FRAME_TYPE_I);
                }
                System.arraycopy(data, offset, this.ifrData, this.ifrLength, readLength);
                this.ifrLength += readLength;
                this.scratchSliceType.reset(this.ifrData, this.ifrLength);
                int len = this.scratchSliceType.peekExpGolombCodedNumLength();
                if (len != NOT_SET && len <= this.scratchSliceType.bitsLeft()) {
                    this.scratchSliceType.skipBits(len);
                    len = this.scratchSliceType.peekExpGolombCodedNumLength();
                    if (len != NOT_SET && len <= this.scratchSliceType.bitsLeft()) {
                        this.sliceType = this.scratchSliceType.readUnsignedExpGolombCodedInt();
                        this.isFilling = false;
                    }
                }
            }
        }

        public int getSliceType() {
            return this.sliceType;
        }
    }

    public H264Reader(TrackOutput output, SeiReader seiReader, boolean idrKeyframesOnly) {
        super(output);
        this.seiReader = seiReader;
        this.ifrParserBuffer = idrKeyframesOnly ? null : new IfrParserBuffer();
        this.sps = new NalUnitTargetBuffer(NAL_UNIT_TYPE_SPS, AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        this.pps = new NalUnitTargetBuffer(NAL_UNIT_TYPE_PPS, AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        this.sei = new NalUnitTargetBuffer(NAL_UNIT_TYPE_SEI, AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        this.seiWrapper = new ParsableByteArray();
    }

    public void seek() {
        this.seiReader.seek();
        NalUnitUtil.clearPrefixFlags(this.prefixFlags);
        this.sps.reset();
        this.pps.reset();
        this.sei.reset();
        if (this.ifrParserBuffer != null) {
            this.ifrParserBuffer.reset();
        }
        this.foundFirstSample = false;
        this.totalBytesWritten = 0;
    }

    public void consume(ParsableByteArray data, long pesTimeUs, boolean startOfPacket) {
        while (data.bytesLeft() > 0) {
            int offset = data.getPosition();
            int limit = data.limit();
            byte[] dataArray = data.data;
            this.totalBytesWritten += (long) data.bytesLeft();
            this.output.sampleData(data, data.bytesLeft());
            while (offset < limit) {
                int nextNalUnitOffset = NalUnitUtil.findNalUnit(dataArray, offset, limit, this.prefixFlags);
                if (nextNalUnitOffset < limit) {
                    int lengthToNalUnit = nextNalUnitOffset - offset;
                    if (lengthToNalUnit > 0) {
                        feedNalUnitTargetBuffersData(dataArray, offset, nextNalUnitOffset);
                    }
                    int nalUnitType = NalUnitUtil.getNalUnitType(dataArray, nextNalUnitOffset);
                    int bytesWrittenPastNalUnit = limit - nextNalUnitOffset;
                    switch (nalUnitType) {
                        case NAL_UNIT_TYPE_IDR /*5*/:
                            this.isKeyframe = true;
                            break;
                        case NAL_UNIT_TYPE_AUD /*9*/:
                            if (this.foundFirstSample) {
                                if (this.ifrParserBuffer != null && this.ifrParserBuffer.isCompleted()) {
                                    int sliceType = this.ifrParserBuffer.getSliceType();
                                    boolean z = this.isKeyframe;
                                    int i = (sliceType == FRAME_TYPE_I || sliceType == NAL_UNIT_TYPE_SPS) ? NAL_UNIT_TYPE_IFR : 0;
                                    this.isKeyframe = i | z;
                                    this.ifrParserBuffer.reset();
                                }
                                if (this.isKeyframe && !this.hasOutputFormat && this.sps.isCompleted() && this.pps.isCompleted()) {
                                    parseMediaFormat(this.sps, this.pps);
                                }
                                this.output.sampleMetadata(this.sampleTimeUs, this.isKeyframe ? NAL_UNIT_TYPE_IFR : 0, ((int) (this.totalBytesWritten - this.samplePosition)) - bytesWrittenPastNalUnit, bytesWrittenPastNalUnit, null);
                            }
                            this.foundFirstSample = true;
                            this.samplePosition = this.totalBytesWritten - ((long) bytesWrittenPastNalUnit);
                            this.sampleTimeUs = pesTimeUs;
                            this.isKeyframe = false;
                            break;
                    }
                    feedNalUnitTargetEnd(pesTimeUs, lengthToNalUnit < 0 ? -lengthToNalUnit : 0);
                    feedNalUnitTargetBuffersStart(nalUnitType);
                    offset = nextNalUnitOffset + 3;
                } else {
                    feedNalUnitTargetBuffersData(dataArray, offset, limit);
                    offset = limit;
                }
            }
        }
    }

    public void packetFinished() {
    }

    private void feedNalUnitTargetBuffersStart(int nalUnitType) {
        if (this.ifrParserBuffer != null) {
            this.ifrParserBuffer.startNalUnit(nalUnitType);
        }
        if (!this.hasOutputFormat) {
            this.sps.startNalUnit(nalUnitType);
            this.pps.startNalUnit(nalUnitType);
        }
        this.sei.startNalUnit(nalUnitType);
    }

    private void feedNalUnitTargetBuffersData(byte[] dataArray, int offset, int limit) {
        if (this.ifrParserBuffer != null) {
            this.ifrParserBuffer.appendToNalUnit(dataArray, offset, limit);
        }
        if (!this.hasOutputFormat) {
            this.sps.appendToNalUnit(dataArray, offset, limit);
            this.pps.appendToNalUnit(dataArray, offset, limit);
        }
        this.sei.appendToNalUnit(dataArray, offset, limit);
    }

    private void feedNalUnitTargetEnd(long pesTimeUs, int discardPadding) {
        this.sps.endNalUnit(discardPadding);
        this.pps.endNalUnit(discardPadding);
        if (this.sei.endNalUnit(discardPadding)) {
            this.seiWrapper.reset(this.sei.nalData, NalUnitUtil.unescapeStream(this.sei.nalData, this.sei.nalLength));
            this.seiWrapper.setPosition(4);
            this.seiReader.consume(this.seiWrapper, pesTimeUs, true);
        }
    }

    private void parseMediaFormat(NalUnitTargetBuffer sps, NalUnitTargetBuffer pps) {
        int i;
        Object spsData = new byte[sps.nalLength];
        Object ppsData = new byte[pps.nalLength];
        System.arraycopy(sps.nalData, 0, spsData, 0, sps.nalLength);
        System.arraycopy(pps.nalData, 0, ppsData, 0, pps.nalLength);
        List<byte[]> initializationData = new ArrayList();
        initializationData.add(spsData);
        initializationData.add(ppsData);
        NalUnitUtil.unescapeStream(sps.nalData, sps.nalLength);
        ParsableBitArray bitArray = new ParsableBitArray(sps.nalData);
        bitArray.skipBits(32);
        int profileIdc = bitArray.readBits(NAL_UNIT_TYPE_PPS);
        bitArray.skipBits(16);
        bitArray.readUnsignedExpGolombCodedInt();
        int chromaFormatIdc = NAL_UNIT_TYPE_IFR;
        if (profileIdc == 100 || profileIdc == 110 || profileIdc == 122 || profileIdc == 244 || profileIdc == 44 || profileIdc == 83 || profileIdc == 86 || profileIdc == 118 || profileIdc == 128 || profileIdc == 138) {
            chromaFormatIdc = bitArray.readUnsignedExpGolombCodedInt();
            if (chromaFormatIdc == 3) {
                bitArray.skipBits(NAL_UNIT_TYPE_IFR);
            }
            bitArray.readUnsignedExpGolombCodedInt();
            bitArray.readUnsignedExpGolombCodedInt();
            bitArray.skipBits(NAL_UNIT_TYPE_IFR);
            if (bitArray.readBit()) {
                int limit = chromaFormatIdc != 3 ? NAL_UNIT_TYPE_PPS : 12;
                i = 0;
                while (i < limit) {
                    if (bitArray.readBit()) {
                        skipScalingList(bitArray, i < NAL_UNIT_TYPE_SEI ? 16 : 64);
                    }
                    i += NAL_UNIT_TYPE_IFR;
                }
            }
        }
        bitArray.readUnsignedExpGolombCodedInt();
        long picOrderCntType = (long) bitArray.readUnsignedExpGolombCodedInt();
        if (picOrderCntType == 0) {
            bitArray.readUnsignedExpGolombCodedInt();
        } else if (picOrderCntType == 1) {
            bitArray.skipBits(NAL_UNIT_TYPE_IFR);
            bitArray.readSignedExpGolombCodedInt();
            bitArray.readSignedExpGolombCodedInt();
            long numRefFramesInPicOrderCntCycle = (long) bitArray.readUnsignedExpGolombCodedInt();
            for (i = 0; ((long) i) < numRefFramesInPicOrderCntCycle; i += NAL_UNIT_TYPE_IFR) {
                bitArray.readUnsignedExpGolombCodedInt();
            }
        }
        bitArray.readUnsignedExpGolombCodedInt();
        bitArray.skipBits(NAL_UNIT_TYPE_IFR);
        int picWidthInMbs = bitArray.readUnsignedExpGolombCodedInt() + NAL_UNIT_TYPE_IFR;
        int picHeightInMapUnits = bitArray.readUnsignedExpGolombCodedInt() + NAL_UNIT_TYPE_IFR;
        boolean frameMbsOnlyFlag = bitArray.readBit();
        int frameHeightInMbs = (2 - (frameMbsOnlyFlag ? NAL_UNIT_TYPE_IFR : 0)) * picHeightInMapUnits;
        if (!frameMbsOnlyFlag) {
            bitArray.skipBits(NAL_UNIT_TYPE_IFR);
        }
        bitArray.skipBits(NAL_UNIT_TYPE_IFR);
        int frameWidth = picWidthInMbs * 16;
        int frameHeight = frameHeightInMbs * 16;
        if (bitArray.readBit()) {
            int cropUnitX;
            int cropUnitY;
            int frameCropLeftOffset = bitArray.readUnsignedExpGolombCodedInt();
            int frameCropRightOffset = bitArray.readUnsignedExpGolombCodedInt();
            int frameCropTopOffset = bitArray.readUnsignedExpGolombCodedInt();
            int frameCropBottomOffset = bitArray.readUnsignedExpGolombCodedInt();
            if (chromaFormatIdc == 0) {
                cropUnitX = NAL_UNIT_TYPE_IFR;
                cropUnitY = 2 - (frameMbsOnlyFlag ? NAL_UNIT_TYPE_IFR : 0);
            } else {
                cropUnitX = chromaFormatIdc == 3 ? NAL_UNIT_TYPE_IFR : FRAME_TYPE_I;
                cropUnitY = (chromaFormatIdc == NAL_UNIT_TYPE_IFR ? FRAME_TYPE_I : NAL_UNIT_TYPE_IFR) * (2 - (frameMbsOnlyFlag ? NAL_UNIT_TYPE_IFR : 0));
            }
            frameWidth -= (frameCropLeftOffset + frameCropRightOffset) * cropUnitX;
            frameHeight -= (frameCropTopOffset + frameCropBottomOffset) * cropUnitY;
        }
        float pixelWidthHeightRatio = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        if (bitArray.readBit() && bitArray.readBit()) {
            int aspectRatioIdc = bitArray.readBits(NAL_UNIT_TYPE_PPS);
            if (aspectRatioIdc == SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
                int sarWidth = bitArray.readBits(16);
                int sarHeight = bitArray.readBits(16);
                if (!(sarWidth == 0 || sarHeight == 0)) {
                    pixelWidthHeightRatio = ((float) sarWidth) / ((float) sarHeight);
                }
            } else if (aspectRatioIdc < NalUnitUtil.ASPECT_RATIO_IDC_VALUES.length) {
                pixelWidthHeightRatio = NalUnitUtil.ASPECT_RATIO_IDC_VALUES[aspectRatioIdc];
            } else {
                Log.w(TAG, "Unexpected aspect_ratio_idc value: " + aspectRatioIdc);
            }
        }
        this.output.format(MediaFormat.createVideoFormat(MimeTypes.VIDEO_H264, -1, -1, frameWidth, frameHeight, pixelWidthHeightRatio, initializationData));
        this.hasOutputFormat = true;
    }

    private void skipScalingList(ParsableBitArray bitArray, int size) {
        int lastScale = NAL_UNIT_TYPE_PPS;
        int nextScale = NAL_UNIT_TYPE_PPS;
        for (int i = 0; i < size; i += NAL_UNIT_TYPE_IFR) {
            if (nextScale != 0) {
                nextScale = ((lastScale + bitArray.readSignedExpGolombCodedInt()) + AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) % AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY;
            }
            if (nextScale != 0) {
                lastScale = nextScale;
            }
        }
    }
}
