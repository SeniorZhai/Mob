package com.google.android.exoplayer.extractor.mp4;

import android.support.v4.media.TransportMediator;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Pair;
import com.android.volley.DefaultRetryPolicy;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.extractor.mp4.Atom.ContainerAtom;
import com.google.android.exoplayer.extractor.mp4.Atom.LeafAtom;
import com.google.android.exoplayer.util.Ac3Util;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.CodecSpecificDataUtil;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.NalUnitUtil;
import com.google.android.exoplayer.util.ParsableByteArray;
import com.google.android.exoplayer.util.Util;
import com.mobcrush.mobcrush.R;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.protocol.HTTP;

final class AtomParsers {

    private static final class StsdDataHolder {
        public MediaFormat mediaFormat;
        public int nalUnitLengthFieldLength = -1;
        public final TrackEncryptionBox[] trackEncryptionBoxes;

        public StsdDataHolder(int numberOfEntries) {
            this.trackEncryptionBoxes = new TrackEncryptionBox[numberOfEntries];
        }
    }

    public static Track parseTrak(ContainerAtom trak, LeafAtom mvhd) {
        ContainerAtom mdia = trak.getContainerAtomOfType(Atom.TYPE_mdia);
        int trackType = parseHdlr(mdia.getLeafAtomOfType(Atom.TYPE_hdlr).data);
        if (trackType != Track.TYPE_AUDIO && trackType != Track.TYPE_VIDEO && trackType != Track.TYPE_TEXT && trackType != Track.TYPE_TIME_CODE) {
            return null;
        }
        long durationUs;
        Pair<Integer, Long> header = parseTkhd(trak.getLeafAtomOfType(Atom.TYPE_tkhd).data);
        int id = ((Integer) header.first).intValue();
        long duration = ((Long) header.second).longValue();
        long movieTimescale = parseMvhd(mvhd.data);
        if (duration == -1) {
            durationUs = -1;
        } else {
            durationUs = Util.scaleLargeTimestamp(duration, C.MICROS_PER_SECOND, movieTimescale);
        }
        ContainerAtom stbl = mdia.getContainerAtomOfType(Atom.TYPE_minf).getContainerAtomOfType(Atom.TYPE_stbl);
        long mediaTimescale = parseMdhd(mdia.getLeafAtomOfType(Atom.TYPE_mdhd).data);
        StsdDataHolder stsdData = parseStsd(stbl.getLeafAtomOfType(Atom.TYPE_stsd).data, durationUs);
        if (stsdData.mediaFormat == null) {
            return null;
        }
        return new Track(id, trackType, mediaTimescale, durationUs, stsdData.mediaFormat, stsdData.trackEncryptionBoxes, stsdData.nalUnitLengthFieldLength);
    }

    public static TrackSampleTable parseStbl(Track track, ContainerAtom stblAtom) {
        ParsableByteArray stsz = stblAtom.getLeafAtomOfType(Atom.TYPE_stsz).data;
        LeafAtom chunkOffsetsAtom = stblAtom.getLeafAtomOfType(Atom.TYPE_stco);
        if (chunkOffsetsAtom == null) {
            chunkOffsetsAtom = stblAtom.getLeafAtomOfType(Atom.TYPE_co64);
        }
        ParsableByteArray chunkOffsets = chunkOffsetsAtom.data;
        ParsableByteArray stsc = stblAtom.getLeafAtomOfType(Atom.TYPE_stsc).data;
        ParsableByteArray stts = stblAtom.getLeafAtomOfType(Atom.TYPE_stts).data;
        LeafAtom stssAtom = stblAtom.getLeafAtomOfType(Atom.TYPE_stss);
        ParsableByteArray stss = stssAtom != null ? stssAtom.data : null;
        LeafAtom cttsAtom = stblAtom.getLeafAtomOfType(Atom.TYPE_ctts);
        ParsableByteArray ctts = cttsAtom != null ? cttsAtom.data : null;
        stsz.setPosition(12);
        int fixedSampleSize = stsz.readUnsignedIntToInt();
        int sampleCount = stsz.readUnsignedIntToInt();
        long[] offsets = new long[sampleCount];
        int[] sizes = new int[sampleCount];
        long[] timestamps = new long[sampleCount];
        int[] flags = new int[sampleCount];
        if (sampleCount == 0) {
            return new TrackSampleTable(offsets, sizes, timestamps, flags);
        }
        long offsetBytes;
        chunkOffsets.setPosition(12);
        int chunkCount = chunkOffsets.readUnsignedIntToInt();
        stsc.setPosition(12);
        int remainingSamplesPerChunkChanges = stsc.readUnsignedIntToInt() - 1;
        Assertions.checkState(stsc.readInt() == 1, "stsc first chunk must be 1");
        int samplesPerChunk = stsc.readUnsignedIntToInt();
        stsc.skipBytes(4);
        int nextSamplesPerChunkChangeChunkIndex = -1;
        if (remainingSamplesPerChunkChanges > 0) {
            nextSamplesPerChunkChangeChunkIndex = stsc.readUnsignedIntToInt() - 1;
        }
        int chunkIndex = 0;
        int remainingSamplesInChunk = samplesPerChunk;
        stts.setPosition(12);
        int remainingTimestampDeltaChanges = stts.readUnsignedIntToInt() - 1;
        int remainingSamplesAtTimestampDelta = stts.readUnsignedIntToInt();
        int timestampDeltaInTimeUnits = stts.readUnsignedIntToInt();
        int remainingSamplesAtTimestampOffset = 0;
        int remainingTimestampOffsetChanges = 0;
        int timestampOffset = 0;
        if (ctts != null) {
            ctts.setPosition(12);
            remainingTimestampOffsetChanges = ctts.readUnsignedIntToInt() - 1;
            remainingSamplesAtTimestampOffset = ctts.readUnsignedIntToInt();
            timestampOffset = ctts.readInt();
        }
        int nextSynchronizationSampleIndex = -1;
        int remainingSynchronizationSamples = 0;
        if (stss != null) {
            stss.setPosition(12);
            remainingSynchronizationSamples = stss.readUnsignedIntToInt();
            nextSynchronizationSampleIndex = stss.readUnsignedIntToInt() - 1;
        }
        if (chunkOffsetsAtom.type == Atom.TYPE_stco) {
            offsetBytes = chunkOffsets.readUnsignedInt();
        } else {
            offsetBytes = chunkOffsets.readUnsignedLongToLong();
        }
        long timestampTimeUnits = 0;
        for (int i = 0; i < sampleCount; i++) {
            int readUnsignedIntToInt;
            offsets[i] = offsetBytes;
            if (fixedSampleSize == 0) {
                readUnsignedIntToInt = stsz.readUnsignedIntToInt();
            } else {
                readUnsignedIntToInt = fixedSampleSize;
            }
            sizes[i] = readUnsignedIntToInt;
            timestamps[i] = ((long) timestampOffset) + timestampTimeUnits;
            flags[i] = stss == null ? 1 : 0;
            if (i == nextSynchronizationSampleIndex) {
                flags[i] = 1;
                remainingSynchronizationSamples--;
                if (remainingSynchronizationSamples > 0) {
                    nextSynchronizationSampleIndex = stss.readUnsignedIntToInt() - 1;
                }
            }
            timestampTimeUnits += (long) timestampDeltaInTimeUnits;
            remainingSamplesAtTimestampDelta--;
            if (remainingSamplesAtTimestampDelta == 0 && remainingTimestampDeltaChanges > 0) {
                remainingSamplesAtTimestampDelta = stts.readUnsignedIntToInt();
                timestampDeltaInTimeUnits = stts.readUnsignedIntToInt();
                remainingTimestampDeltaChanges--;
            }
            if (ctts != null) {
                remainingSamplesAtTimestampOffset--;
                if (remainingSamplesAtTimestampOffset == 0 && remainingTimestampOffsetChanges > 0) {
                    remainingSamplesAtTimestampOffset = ctts.readUnsignedIntToInt();
                    timestampOffset = ctts.readInt();
                    remainingTimestampOffsetChanges--;
                }
            }
            remainingSamplesInChunk--;
            if (remainingSamplesInChunk == 0) {
                chunkIndex++;
                if (chunkIndex < chunkCount) {
                    if (chunkOffsetsAtom.type == Atom.TYPE_stco) {
                        offsetBytes = chunkOffsets.readUnsignedInt();
                    } else {
                        offsetBytes = chunkOffsets.readUnsignedLongToLong();
                    }
                }
                if (chunkIndex == nextSamplesPerChunkChangeChunkIndex) {
                    samplesPerChunk = stsc.readUnsignedIntToInt();
                    stsc.skipBytes(4);
                    remainingSamplesPerChunkChanges--;
                    if (remainingSamplesPerChunkChanges > 0) {
                        nextSamplesPerChunkChangeChunkIndex = stsc.readUnsignedIntToInt() - 1;
                    }
                }
                if (chunkIndex < chunkCount) {
                    remainingSamplesInChunk = samplesPerChunk;
                }
            } else {
                offsetBytes += (long) sizes[i];
            }
        }
        Util.scaleLargeTimestampsInPlace(timestamps, C.MICROS_PER_SECOND, track.timescale);
        Assertions.checkArgument(remainingSynchronizationSamples == 0);
        Assertions.checkArgument(remainingSamplesAtTimestampDelta == 0);
        Assertions.checkArgument(remainingSamplesInChunk == 0);
        Assertions.checkArgument(remainingTimestampDeltaChanges == 0);
        Assertions.checkArgument(remainingTimestampOffsetChanges == 0);
        return new TrackSampleTable(offsets, sizes, timestamps, flags);
    }

    private static long parseMvhd(ParsableByteArray mvhd) {
        int i = 8;
        mvhd.setPosition(8);
        if (Atom.parseFullAtomVersion(mvhd.readInt()) != 0) {
            i = 16;
        }
        mvhd.skipBytes(i);
        return mvhd.readUnsignedInt();
    }

    private static Pair<Integer, Long> parseTkhd(ParsableByteArray tkhd) {
        long duration;
        int durationByteCount = 4;
        tkhd.setPosition(8);
        int version = Atom.parseFullAtomVersion(tkhd.readInt());
        tkhd.skipBytes(version == 0 ? 8 : 16);
        int trackId = tkhd.readInt();
        tkhd.skipBytes(4);
        boolean durationUnknown = true;
        int durationPosition = tkhd.getPosition();
        if (version != 0) {
            durationByteCount = 8;
        }
        for (int i = 0; i < durationByteCount; i++) {
            if (tkhd.data[durationPosition + i] != (byte) -1) {
                durationUnknown = false;
                break;
            }
        }
        if (durationUnknown) {
            tkhd.skipBytes(durationByteCount);
            duration = -1;
        } else {
            duration = version == 0 ? tkhd.readUnsignedInt() : tkhd.readUnsignedLongToLong();
        }
        return Pair.create(Integer.valueOf(trackId), Long.valueOf(duration));
    }

    private static int parseHdlr(ParsableByteArray hdlr) {
        hdlr.setPosition(16);
        return hdlr.readInt();
    }

    private static long parseMdhd(ParsableByteArray mdhd) {
        int i = 8;
        mdhd.setPosition(8);
        if (Atom.parseFullAtomVersion(mdhd.readInt()) != 0) {
            i = 16;
        }
        mdhd.skipBytes(i);
        return mdhd.readUnsignedInt();
    }

    private static StsdDataHolder parseStsd(ParsableByteArray stsd, long durationUs) {
        stsd.setPosition(12);
        int numberOfEntries = stsd.readInt();
        StsdDataHolder holder = new StsdDataHolder(numberOfEntries);
        for (int i = 0; i < numberOfEntries; i++) {
            int childStartPosition = stsd.getPosition();
            int childAtomSize = stsd.readInt();
            Assertions.checkArgument(childAtomSize > 0, "childAtomSize should be positive");
            int childAtomType = stsd.readInt();
            if (childAtomType == Atom.TYPE_avc1 || childAtomType == Atom.TYPE_avc3 || childAtomType == Atom.TYPE_encv || childAtomType == Atom.TYPE_mp4v || childAtomType == Atom.TYPE_hvc1 || childAtomType == Atom.TYPE_hev1) {
                parseVideoSampleEntry(stsd, childStartPosition, childAtomSize, durationUs, holder, i);
            } else if (childAtomType == Atom.TYPE_mp4a || childAtomType == Atom.TYPE_enca || childAtomType == Atom.TYPE_ac_3) {
                parseAudioSampleEntry(stsd, childAtomType, childStartPosition, childAtomSize, durationUs, holder, i);
            } else if (childAtomType == Atom.TYPE_TTML) {
                holder.mediaFormat = MediaFormat.createTextFormat(MimeTypes.APPLICATION_TTML);
            } else if (childAtomType == Atom.TYPE_tx3g) {
                holder.mediaFormat = MediaFormat.createTextFormat(MimeTypes.APPLICATION_TX3G);
            }
            stsd.setPosition(childStartPosition + childAtomSize);
        }
        return holder;
    }

    private static void parseVideoSampleEntry(ParsableByteArray parent, int position, int size, long durationUs, StsdDataHolder out, int entryIndex) {
        parent.setPosition(position + 8);
        parent.skipBytes(24);
        int width = parent.readUnsignedShort();
        int height = parent.readUnsignedShort();
        float pixelWidthHeightRatio = DefaultRetryPolicy.DEFAULT_BACKOFF_MULT;
        parent.skipBytes(50);
        List<byte[]> initializationData = null;
        int childPosition = parent.getPosition();
        String mimeType = null;
        while (childPosition - position < size) {
            parent.setPosition(childPosition);
            int childStartPosition = parent.getPosition();
            int childAtomSize = parent.readInt();
            if (childAtomSize == 0 && parent.getPosition() - position == size) {
                break;
            }
            Assertions.checkArgument(childAtomSize > 0, "childAtomSize should be positive");
            int childAtomType = parent.readInt();
            if (childAtomType == Atom.TYPE_avcC) {
                Assertions.checkState(mimeType == null);
                mimeType = MimeTypes.VIDEO_H264;
                Pair<List<byte[]>, Integer> avcCData = parseAvcCFromParent(parent, childStartPosition);
                initializationData = avcCData.first;
                out.nalUnitLengthFieldLength = ((Integer) avcCData.second).intValue();
            } else if (childAtomType == Atom.TYPE_hvcC) {
                Assertions.checkState(mimeType == null);
                mimeType = MimeTypes.VIDEO_H265;
                Pair<List<byte[]>, Integer> hvcCData = parseHvcCFromParent(parent, childStartPosition);
                initializationData = hvcCData.first;
                out.nalUnitLengthFieldLength = ((Integer) hvcCData.second).intValue();
            } else if (childAtomType == Atom.TYPE_esds) {
                Assertions.checkState(mimeType == null);
                Pair<String, byte[]> mimeTypeAndInitializationData = parseEsdsFromParent(parent, childStartPosition);
                mimeType = mimeTypeAndInitializationData.first;
                initializationData = Collections.singletonList(mimeTypeAndInitializationData.second);
            } else if (childAtomType == Atom.TYPE_sinf) {
                out.trackEncryptionBoxes[entryIndex] = parseSinfFromParent(parent, childStartPosition, childAtomSize);
            } else if (childAtomType == Atom.TYPE_pasp) {
                pixelWidthHeightRatio = parsePaspFromParent(parent, childStartPosition);
            }
            childPosition += childAtomSize;
        }
        if (mimeType != null) {
            out.mediaFormat = MediaFormat.createVideoFormat(mimeType, -1, durationUs, width, height, pixelWidthHeightRatio, initializationData);
        }
    }

    private static Pair<List<byte[]>, Integer> parseAvcCFromParent(ParsableByteArray parent, int position) {
        parent.setPosition((position + 8) + 4);
        int nalUnitLengthFieldLength = (parent.readUnsignedByte() & 3) + 1;
        if (nalUnitLengthFieldLength == 3) {
            throw new IllegalStateException();
        }
        int j;
        List<byte[]> initializationData = new ArrayList();
        int numSequenceParameterSets = parent.readUnsignedByte() & 31;
        for (j = 0; j < numSequenceParameterSets; j++) {
            initializationData.add(NalUnitUtil.parseChildNalUnit(parent));
        }
        int numPictureParameterSets = parent.readUnsignedByte();
        for (j = 0; j < numPictureParameterSets; j++) {
            initializationData.add(NalUnitUtil.parseChildNalUnit(parent));
        }
        return Pair.create(initializationData, Integer.valueOf(nalUnitLengthFieldLength));
    }

    private static Pair<List<byte[]>, Integer> parseHvcCFromParent(ParsableByteArray parent, int position) {
        int i;
        parent.setPosition((position + 8) + 21);
        int lengthSizeMinusOne = parent.readUnsignedByte() & 3;
        int numberOfArrays = parent.readUnsignedByte();
        int csdLength = 0;
        int csdStartPosition = parent.getPosition();
        for (i = 0; i < numberOfArrays; i++) {
            int j;
            parent.skipBytes(1);
            int numberOfNalUnits = parent.readUnsignedShort();
            for (j = 0; j < numberOfNalUnits; j++) {
                int nalUnitLength = parent.readUnsignedShort();
                csdLength += nalUnitLength + 4;
                parent.skipBytes(nalUnitLength);
            }
        }
        parent.setPosition(csdStartPosition);
        byte[] buffer = new byte[csdLength];
        int bufferPosition = 0;
        for (i = 0; i < numberOfArrays; i++) {
            parent.skipBytes(1);
            numberOfNalUnits = parent.readUnsignedShort();
            for (j = 0; j < numberOfNalUnits; j++) {
                nalUnitLength = parent.readUnsignedShort();
                System.arraycopy(NalUnitUtil.NAL_START_CODE, 0, buffer, bufferPosition, NalUnitUtil.NAL_START_CODE.length);
                bufferPosition += NalUnitUtil.NAL_START_CODE.length;
                System.arraycopy(parent.data, parent.getPosition(), buffer, bufferPosition, nalUnitLength);
                bufferPosition += nalUnitLength;
                parent.skipBytes(nalUnitLength);
            }
        }
        return Pair.create(csdLength == 0 ? null : Collections.singletonList(buffer), Integer.valueOf(lengthSizeMinusOne + 1));
    }

    private static TrackEncryptionBox parseSinfFromParent(ParsableByteArray parent, int position, int size) {
        int childPosition = position + 8;
        TrackEncryptionBox trackEncryptionBox = null;
        while (childPosition - position < size) {
            parent.setPosition(childPosition);
            int childAtomSize = parent.readInt();
            int childAtomType = parent.readInt();
            if (childAtomType == Atom.TYPE_frma) {
                parent.readInt();
            } else if (childAtomType == Atom.TYPE_schm) {
                parent.skipBytes(4);
                parent.readInt();
                parent.readInt();
            } else if (childAtomType == Atom.TYPE_schi) {
                trackEncryptionBox = parseSchiFromParent(parent, childPosition, childAtomSize);
            }
            childPosition += childAtomSize;
        }
        return trackEncryptionBox;
    }

    private static float parsePaspFromParent(ParsableByteArray parent, int position) {
        parent.setPosition(position + 8);
        return ((float) parent.readUnsignedIntToInt()) / ((float) parent.readUnsignedIntToInt());
    }

    private static TrackEncryptionBox parseSchiFromParent(ParsableByteArray parent, int position, int size) {
        boolean defaultIsEncrypted = true;
        int childPosition = position + 8;
        while (childPosition - position < size) {
            parent.setPosition(childPosition);
            int childAtomSize = parent.readInt();
            if (parent.readInt() == Atom.TYPE_tenc) {
                parent.skipBytes(4);
                int firstInt = parent.readInt();
                if ((firstInt >> 8) != 1) {
                    defaultIsEncrypted = false;
                }
                int defaultInitVectorSize = firstInt & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
                byte[] defaultKeyId = new byte[16];
                parent.readBytes(defaultKeyId, 0, defaultKeyId.length);
                return new TrackEncryptionBox(defaultIsEncrypted, defaultInitVectorSize, defaultKeyId);
            }
            childPosition += childAtomSize;
        }
        return null;
    }

    private static void parseAudioSampleEntry(ParsableByteArray parent, int atomType, int position, int size, long durationUs, StsdDataHolder out, int entryIndex) {
        parent.setPosition(position + 8);
        parent.skipBytes(16);
        int channelCount = parent.readUnsignedShort();
        int sampleSize = parent.readUnsignedShort();
        parent.skipBytes(4);
        int sampleRate = parent.readUnsignedFixedPoint1616();
        String mimeType = null;
        if (atomType == Atom.TYPE_ac_3) {
            mimeType = MimeTypes.AUDIO_AC3;
        } else if (atomType == Atom.TYPE_ec_3) {
            mimeType = MimeTypes.AUDIO_EC3;
        }
        byte[] initializationData = null;
        int childPosition = parent.getPosition();
        while (childPosition - position < size) {
            parent.setPosition(childPosition);
            int childStartPosition = parent.getPosition();
            int childAtomSize = parent.readInt();
            Assertions.checkArgument(childAtomSize > 0, "childAtomSize should be positive");
            int childAtomType = parent.readInt();
            if (atomType == Atom.TYPE_mp4a || atomType == Atom.TYPE_enca) {
                if (childAtomType == Atom.TYPE_esds) {
                    Pair<String, byte[]> mimeTypeAndInitializationData = parseEsdsFromParent(parent, childStartPosition);
                    mimeType = mimeTypeAndInitializationData.first;
                    initializationData = mimeTypeAndInitializationData.second;
                    if (MimeTypes.AUDIO_AAC.equals(mimeType)) {
                        Pair<Integer, Integer> audioSpecificConfig = CodecSpecificDataUtil.parseAacAudioSpecificConfig(initializationData);
                        sampleRate = ((Integer) audioSpecificConfig.first).intValue();
                        channelCount = ((Integer) audioSpecificConfig.second).intValue();
                    }
                } else if (childAtomType == Atom.TYPE_sinf) {
                    out.trackEncryptionBoxes[entryIndex] = parseSinfFromParent(parent, childStartPosition, childAtomSize);
                }
            } else if (atomType == Atom.TYPE_ac_3 && childAtomType == Atom.TYPE_dac3) {
                parent.setPosition(childStartPosition + 8);
                out.mediaFormat = Ac3Util.parseAnnexFAc3Format(parent);
                return;
            } else if (atomType == Atom.TYPE_ec_3 && childAtomType == Atom.TYPE_dec3) {
                parent.setPosition(childStartPosition + 8);
                out.mediaFormat = Ac3Util.parseAnnexFEAc3Format(parent);
                return;
            }
            childPosition += childAtomSize;
        }
        if (mimeType != null) {
            out.mediaFormat = MediaFormat.createAudioFormat(mimeType, sampleSize, durationUs, channelCount, sampleRate, initializationData == null ? null : Collections.singletonList(initializationData));
        }
    }

    private static Pair<String, byte[]> parseEsdsFromParent(ParsableByteArray parent, int position) {
        String mimeType;
        parent.setPosition((position + 8) + 4);
        parent.skipBytes(1);
        int varIntByte = parent.readUnsignedByte();
        while (varIntByte > TransportMediator.KEYCODE_MEDIA_PAUSE) {
            varIntByte = parent.readUnsignedByte();
        }
        parent.skipBytes(2);
        int flags = parent.readUnsignedByte();
        if ((flags & AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) != 0) {
            parent.skipBytes(2);
        }
        if ((flags & 64) != 0) {
            parent.skipBytes(parent.readUnsignedShort());
        }
        if ((flags & 32) != 0) {
            parent.skipBytes(2);
        }
        parent.skipBytes(1);
        varIntByte = parent.readUnsignedByte();
        while (varIntByte > TransportMediator.KEYCODE_MEDIA_PAUSE) {
            varIntByte = parent.readUnsignedByte();
        }
        switch (parent.readUnsignedByte()) {
            case HTTP.SP /*32*/:
                mimeType = MimeTypes.VIDEO_MP4V;
                break;
            case R.styleable.Theme_actionModeCopyDrawable /*33*/:
                mimeType = MimeTypes.VIDEO_H264;
                break;
            case R.styleable.Theme_actionModeSelectAllDrawable /*35*/:
                mimeType = MimeTypes.VIDEO_H265;
                break;
            case SettingsJsonConstants.SETTINGS_MAX_CUSTOM_KEY_VALUE_PAIRS_DEFAULT /*64*/:
                mimeType = MimeTypes.AUDIO_AAC;
                break;
            case R.styleable.Theme_switchStyle /*107*/:
                return Pair.create(MimeTypes.AUDIO_MPEG, null);
            case 165:
                mimeType = MimeTypes.AUDIO_AC3;
                break;
            case 166:
                mimeType = MimeTypes.AUDIO_EC3;
                break;
            default:
                mimeType = null;
                break;
        }
        parent.skipBytes(12);
        parent.skipBytes(1);
        varIntByte = parent.readUnsignedByte();
        int varInt = varIntByte & TransportMediator.KEYCODE_MEDIA_PAUSE;
        while (varIntByte > TransportMediator.KEYCODE_MEDIA_PAUSE) {
            varIntByte = parent.readUnsignedByte();
            varInt = (varInt << 8) | (varIntByte & TransportMediator.KEYCODE_MEDIA_PAUSE);
        }
        byte[] initializationData = new byte[varInt];
        parent.readBytes(initializationData, 0, varInt);
        return Pair.create(mimeType, initializationData);
    }

    private AtomParsers() {
    }
}
