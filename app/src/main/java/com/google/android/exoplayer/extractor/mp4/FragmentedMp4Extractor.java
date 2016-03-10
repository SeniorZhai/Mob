package com.google.android.exoplayer.extractor.mp4;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.drm.DrmInitData.Mapped;
import com.google.android.exoplayer.extractor.ChunkIndex;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.extractor.ExtractorOutput;
import com.google.android.exoplayer.extractor.PositionHolder;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.extractor.mp4.Atom.ContainerAtom;
import com.google.android.exoplayer.extractor.mp4.Atom.LeafAtom;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.NalUnitUtil;
import com.google.android.exoplayer.util.ParsableByteArray;
import com.google.android.exoplayer.util.Util;
import com.mobcrush.mobcrush.Constants;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public final class FragmentedMp4Extractor implements Extractor {
    private static final byte[] PIFF_SAMPLE_ENCRYPTION_BOX_EXTENDED_TYPE = new byte[]{(byte) -94, (byte) 57, (byte) 79, (byte) 82, (byte) 90, (byte) -101, (byte) 79, ClosedCaptionCtrl.MISC_CHAN_1, (byte) -94, (byte) 68, (byte) 108, (byte) 66, (byte) 124, (byte) 100, (byte) -115, (byte) -12};
    private static final int STATE_READING_ATOM_HEADER = 0;
    private static final int STATE_READING_ATOM_PAYLOAD = 1;
    private static final int STATE_READING_ENCRYPTION_DATA = 2;
    private static final int STATE_READING_SAMPLE_CONTINUE = 4;
    private static final int STATE_READING_SAMPLE_START = 3;
    public static final int WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME = 1;
    private ParsableByteArray atomData;
    private final ParsableByteArray atomHeader;
    private int atomSize;
    private int atomType;
    private final Stack<ContainerAtom> containerAtoms;
    private final ParsableByteArray encryptionSignalByte;
    private final byte[] extendedTypeScratch;
    private DefaultSampleValues extendsDefaults;
    private ExtractorOutput extractorOutput;
    private final TrackFragment fragmentRun;
    private final ParsableByteArray nalLength;
    private final ParsableByteArray nalStartCode;
    private int parserState;
    private int rootAtomBytesRead;
    private int sampleBytesWritten;
    private int sampleCurrentNalBytesRemaining;
    private int sampleIndex;
    private int sampleSize;
    private Track track;
    private TrackOutput trackOutput;
    private final int workaroundFlags;

    public FragmentedMp4Extractor() {
        this(STATE_READING_ATOM_HEADER);
    }

    public FragmentedMp4Extractor(int workaroundFlags) {
        this.workaroundFlags = workaroundFlags;
        this.atomHeader = new ParsableByteArray(8);
        this.nalStartCode = new ParsableByteArray(NalUnitUtil.NAL_START_CODE);
        this.nalLength = new ParsableByteArray((int) STATE_READING_SAMPLE_CONTINUE);
        this.encryptionSignalByte = new ParsableByteArray((int) WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME);
        this.extendedTypeScratch = new byte[16];
        this.containerAtoms = new Stack();
        this.fragmentRun = new TrackFragment();
        this.parserState = STATE_READING_ATOM_HEADER;
    }

    public void setTrack(Track track) {
        this.extendsDefaults = new DefaultSampleValues(STATE_READING_ATOM_HEADER, STATE_READING_ATOM_HEADER, STATE_READING_ATOM_HEADER, STATE_READING_ATOM_HEADER);
        this.track = track;
    }

    public void init(ExtractorOutput output) {
        this.extractorOutput = output;
        this.trackOutput = output.track(STATE_READING_ATOM_HEADER);
        this.extractorOutput.endTracks();
    }

    public void seek() {
        this.containerAtoms.clear();
        this.rootAtomBytesRead = STATE_READING_ATOM_HEADER;
        this.parserState = STATE_READING_ATOM_HEADER;
    }

    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        while (true) {
            switch (this.parserState) {
                case STATE_READING_ATOM_HEADER /*0*/:
                    if (readAtomHeader(input)) {
                        break;
                    }
                    return -1;
                case WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME /*1*/:
                    readAtomPayload(input);
                    break;
                case STATE_READING_ENCRYPTION_DATA /*2*/:
                    readEncryptionData(input);
                    break;
                default:
                    if (!readSample(input)) {
                        break;
                    }
                    return STATE_READING_ATOM_HEADER;
            }
        }
    }

    private boolean readAtomHeader(ExtractorInput input) throws IOException, InterruptedException {
        if (!input.readFully(this.atomHeader.data, STATE_READING_ATOM_HEADER, 8, true)) {
            return false;
        }
        this.rootAtomBytesRead += 8;
        this.atomHeader.setPosition(STATE_READING_ATOM_HEADER);
        this.atomSize = this.atomHeader.readInt();
        this.atomType = this.atomHeader.readInt();
        if (this.atomType == Atom.TYPE_mdat) {
            if (this.fragmentRun.sampleEncryptionDataNeedsFill) {
                this.parserState = STATE_READING_ENCRYPTION_DATA;
            } else {
                this.parserState = STATE_READING_SAMPLE_START;
            }
            return true;
        }
        if (!shouldParseAtom(this.atomType)) {
            this.atomData = null;
            this.parserState = WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME;
        } else if (shouldParseContainerAtom(this.atomType)) {
            this.parserState = STATE_READING_ATOM_HEADER;
            this.containerAtoms.add(new ContainerAtom(this.atomType, (long) ((this.rootAtomBytesRead + this.atomSize) - 8)));
        } else {
            this.atomData = new ParsableByteArray(this.atomSize);
            System.arraycopy(this.atomHeader.data, STATE_READING_ATOM_HEADER, this.atomData.data, STATE_READING_ATOM_HEADER, 8);
            this.parserState = WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME;
        }
        return true;
    }

    private void readAtomPayload(ExtractorInput input) throws IOException, InterruptedException {
        int payloadLength = this.atomSize - 8;
        if (this.atomData != null) {
            input.readFully(this.atomData.data, 8, payloadLength);
            this.rootAtomBytesRead += payloadLength;
            onLeafAtomRead(new LeafAtom(this.atomType, this.atomData), input.getPosition());
        } else {
            input.skipFully(payloadLength);
            this.rootAtomBytesRead += payloadLength;
        }
        while (!this.containerAtoms.isEmpty() && ((ContainerAtom) this.containerAtoms.peek()).endByteOffset == ((long) this.rootAtomBytesRead)) {
            onContainerAtomRead((ContainerAtom) this.containerAtoms.pop());
        }
        if (this.containerAtoms.isEmpty()) {
            this.rootAtomBytesRead = STATE_READING_ATOM_HEADER;
        }
        this.parserState = STATE_READING_ATOM_HEADER;
    }

    private void onLeafAtomRead(LeafAtom leaf, long inputPosition) {
        if (!this.containerAtoms.isEmpty()) {
            ((ContainerAtom) this.containerAtoms.peek()).add(leaf);
        } else if (leaf.type == Atom.TYPE_sidx) {
            this.extractorOutput.seekMap(parseSidx(leaf.data, inputPosition));
        }
    }

    private void onContainerAtomRead(ContainerAtom container) {
        if (container.type == Atom.TYPE_moov) {
            onMoovContainerAtomRead(container);
        } else if (container.type == Atom.TYPE_moof) {
            onMoofContainerAtomRead(container);
        } else if (!this.containerAtoms.isEmpty()) {
            ((ContainerAtom) this.containerAtoms.peek()).add(container);
        }
    }

    private void onMoovContainerAtomRead(ContainerAtom moov) {
        List<LeafAtom> moovChildren = moov.leafChildren;
        int moovChildrenSize = moovChildren.size();
        Mapped drmInitData = null;
        for (int i = STATE_READING_ATOM_HEADER; i < moovChildrenSize; i += WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) {
            LeafAtom child = (LeafAtom) moovChildren.get(i);
            if (child.type == Atom.TYPE_pssh) {
                if (drmInitData == null) {
                    drmInitData = new Mapped(MimeTypes.VIDEO_MP4);
                }
                byte[] psshData = child.data.data;
                drmInitData.put(PsshAtomUtil.parseUuid(psshData), psshData);
            }
        }
        if (drmInitData != null) {
            this.extractorOutput.drmInitData(drmInitData);
        }
        this.extendsDefaults = parseTrex(moov.getContainerAtomOfType(Atom.TYPE_mvex).getLeafAtomOfType(Atom.TYPE_trex).data);
        this.track = AtomParsers.parseTrak(moov.getContainerAtomOfType(Atom.TYPE_trak), moov.getLeafAtomOfType(Atom.TYPE_mvhd));
        Assertions.checkState(this.track != null);
        this.trackOutput.format(this.track.mediaFormat);
    }

    private void onMoofContainerAtomRead(ContainerAtom moof) {
        this.fragmentRun.reset();
        parseMoof(this.track, this.extendsDefaults, moof, this.fragmentRun, this.workaroundFlags, this.extendedTypeScratch);
        this.sampleIndex = STATE_READING_ATOM_HEADER;
    }

    private static DefaultSampleValues parseTrex(ParsableByteArray trex) {
        trex.setPosition(16);
        return new DefaultSampleValues(trex.readUnsignedIntToInt() - 1, trex.readUnsignedIntToInt(), trex.readUnsignedIntToInt(), trex.readInt());
    }

    private static void parseMoof(Track track, DefaultSampleValues extendsDefaults, ContainerAtom moof, TrackFragment out, int workaroundFlags, byte[] extendedTypeScratch) {
        parseTraf(track, extendsDefaults, moof.getContainerAtomOfType(Atom.TYPE_traf), out, workaroundFlags, extendedTypeScratch);
    }

    private static void parseTraf(Track track, DefaultSampleValues extendsDefaults, ContainerAtom traf, TrackFragment out, int workaroundFlags, byte[] extendedTypeScratch) {
        long decodeTime;
        if (traf.getLeafAtomOfType(Atom.TYPE_tfdt) == null) {
            decodeTime = 0;
        } else {
            decodeTime = parseTfdt(traf.getLeafAtomOfType(Atom.TYPE_tfdt).data);
        }
        DefaultSampleValues defaultSampleValues = extendsDefaults;
        DefaultSampleValues fragmentHeader = parseTfhd(defaultSampleValues, traf.getLeafAtomOfType(Atom.TYPE_tfhd).data);
        out.sampleDescriptionIndex = fragmentHeader.sampleDescriptionIndex;
        Track track2 = track;
        int i = workaroundFlags;
        parseTrun(track2, fragmentHeader, decodeTime, i, traf.getLeafAtomOfType(Atom.TYPE_trun).data, out);
        LeafAtom saiz = traf.getLeafAtomOfType(Atom.TYPE_saiz);
        if (saiz != null) {
            parseSaiz(track.sampleDescriptionEncryptionBoxes[fragmentHeader.sampleDescriptionIndex], saiz.data, out);
        }
        LeafAtom senc = traf.getLeafAtomOfType(Atom.TYPE_senc);
        if (senc != null) {
            parseSenc(senc.data, out);
        }
        int childrenSize = traf.leafChildren.size();
        for (int i2 = STATE_READING_ATOM_HEADER; i2 < childrenSize; i2 += WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) {
            LeafAtom atom = (LeafAtom) traf.leafChildren.get(i2);
            if (atom.type == Atom.TYPE_uuid) {
                parseUuid(atom.data, out, extendedTypeScratch);
            }
        }
    }

    private static void parseSaiz(TrackEncryptionBox encryptionBox, ParsableByteArray saiz, TrackFragment out) {
        int vectorSize = encryptionBox.initializationVectorSize;
        saiz.setPosition(8);
        if ((Atom.parseFullAtomFlags(saiz.readInt()) & WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) == WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) {
            saiz.skipBytes(8);
        }
        int defaultSampleInfoSize = saiz.readUnsignedByte();
        int sampleCount = saiz.readUnsignedIntToInt();
        if (sampleCount != out.length) {
            throw new IllegalStateException("Length mismatch: " + sampleCount + ", " + out.length);
        }
        int totalSize = STATE_READING_ATOM_HEADER;
        if (defaultSampleInfoSize == 0) {
            boolean[] sampleHasSubsampleEncryptionTable = out.sampleHasSubsampleEncryptionTable;
            for (int i = STATE_READING_ATOM_HEADER; i < sampleCount; i += WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) {
                int sampleInfoSize = saiz.readUnsignedByte();
                totalSize += sampleInfoSize;
                sampleHasSubsampleEncryptionTable[i] = sampleInfoSize > vectorSize;
            }
        } else {
            totalSize = STATE_READING_ATOM_HEADER + (defaultSampleInfoSize * sampleCount);
            Arrays.fill(out.sampleHasSubsampleEncryptionTable, STATE_READING_ATOM_HEADER, sampleCount, defaultSampleInfoSize > vectorSize);
        }
        out.initEncryptionData(totalSize);
    }

    private static DefaultSampleValues parseTfhd(DefaultSampleValues extendsDefaults, ParsableByteArray tfhd) {
        tfhd.setPosition(8);
        int flags = Atom.parseFullAtomFlags(tfhd.readInt());
        tfhd.skipBytes(STATE_READING_SAMPLE_CONTINUE);
        if ((flags & WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) != 0) {
            tfhd.skipBytes(8);
        }
        return new DefaultSampleValues((flags & STATE_READING_ENCRYPTION_DATA) != 0 ? tfhd.readUnsignedIntToInt() - 1 : extendsDefaults.sampleDescriptionIndex, (flags & 8) != 0 ? tfhd.readUnsignedIntToInt() : extendsDefaults.duration, (flags & 16) != 0 ? tfhd.readUnsignedIntToInt() : extendsDefaults.size, (flags & 32) != 0 ? tfhd.readUnsignedIntToInt() : extendsDefaults.flags);
    }

    private static long parseTfdt(ParsableByteArray tfdt) {
        tfdt.setPosition(8);
        return Atom.parseFullAtomVersion(tfdt.readInt()) == WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME ? tfdt.readUnsignedLongToLong() : tfdt.readUnsignedInt();
    }

    private static void parseTrun(Track track, DefaultSampleValues defaultSampleValues, long decodeTime, int workaroundFlags, ParsableByteArray trun, TrackFragment out) {
        trun.setPosition(8);
        int flags = Atom.parseFullAtomFlags(trun.readInt());
        int sampleCount = trun.readUnsignedIntToInt();
        if ((flags & WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) != 0) {
            trun.skipBytes(STATE_READING_SAMPLE_CONTINUE);
        }
        boolean firstSampleFlagsPresent = (flags & STATE_READING_SAMPLE_CONTINUE) != 0;
        int firstSampleFlags = defaultSampleValues.flags;
        if (firstSampleFlagsPresent) {
            firstSampleFlags = trun.readUnsignedIntToInt();
        }
        boolean sampleDurationsPresent = (flags & AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY) != 0;
        boolean sampleSizesPresent = (flags & AccessibilityNodeInfoCompat.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY) != 0;
        boolean sampleFlagsPresent = (flags & AccessibilityNodeInfoCompat.ACTION_NEXT_HTML_ELEMENT) != 0;
        boolean sampleCompositionTimeOffsetsPresent = (flags & AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT) != 0;
        out.initTables(sampleCount);
        int[] sampleSizeTable = out.sampleSizeTable;
        int[] sampleCompositionTimeOffsetTable = out.sampleCompositionTimeOffsetTable;
        long[] sampleDecodingTimeTable = out.sampleDecodingTimeTable;
        boolean[] sampleIsSyncFrameTable = out.sampleIsSyncFrameTable;
        long timescale = track.timescale;
        long cumulativeTime = decodeTime;
        boolean workaroundEveryVideoFrameIsSyncFrame = track.type == 1986618469 && (workaroundFlags & WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) == WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME;
        int i = STATE_READING_ATOM_HEADER;
        while (i < sampleCount) {
            int sampleDuration = sampleDurationsPresent ? trun.readUnsignedIntToInt() : defaultSampleValues.duration;
            int sampleSize = sampleSizesPresent ? trun.readUnsignedIntToInt() : defaultSampleValues.size;
            int sampleFlags = (i == 0 && firstSampleFlagsPresent) ? firstSampleFlags : sampleFlagsPresent ? trun.readInt() : defaultSampleValues.flags;
            if (sampleCompositionTimeOffsetsPresent) {
                sampleCompositionTimeOffsetTable[i] = (int) (((long) (trun.readInt() * Constants.UPDATE_COFIG_INTERVAL)) / timescale);
            } else {
                sampleCompositionTimeOffsetTable[i] = STATE_READING_ATOM_HEADER;
            }
            sampleDecodingTimeTable[i] = (1000 * cumulativeTime) / timescale;
            sampleSizeTable[i] = sampleSize;
            boolean z = ((sampleFlags >> 16) & WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) == 0 && (!workaroundEveryVideoFrameIsSyncFrame || i == 0);
            sampleIsSyncFrameTable[i] = z;
            cumulativeTime += (long) sampleDuration;
            i += WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME;
        }
    }

    private static void parseUuid(ParsableByteArray uuid, TrackFragment out, byte[] extendedTypeScratch) {
        uuid.setPosition(8);
        uuid.readBytes(extendedTypeScratch, STATE_READING_ATOM_HEADER, 16);
        if (Arrays.equals(extendedTypeScratch, PIFF_SAMPLE_ENCRYPTION_BOX_EXTENDED_TYPE)) {
            parseSenc(uuid, 16, out);
        }
    }

    private static void parseSenc(ParsableByteArray senc, TrackFragment out) {
        parseSenc(senc, STATE_READING_ATOM_HEADER, out);
    }

    private static void parseSenc(ParsableByteArray senc, int offset, TrackFragment out) {
        senc.setPosition(offset + 8);
        int flags = Atom.parseFullAtomFlags(senc.readInt());
        if ((flags & WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) != 0) {
            throw new IllegalStateException("Overriding TrackEncryptionBox parameters is unsupported");
        }
        boolean subsampleEncryption;
        if ((flags & STATE_READING_ENCRYPTION_DATA) != 0) {
            subsampleEncryption = true;
        } else {
            subsampleEncryption = false;
        }
        int sampleCount = senc.readUnsignedIntToInt();
        if (sampleCount != out.length) {
            throw new IllegalStateException("Length mismatch: " + sampleCount + ", " + out.length);
        }
        Arrays.fill(out.sampleHasSubsampleEncryptionTable, STATE_READING_ATOM_HEADER, sampleCount, subsampleEncryption);
        out.initEncryptionData(senc.bytesLeft());
        out.fillEncryptionData(senc);
    }

    private static ChunkIndex parseSidx(ParsableByteArray atom, long inputPosition) {
        long earliestPresentationTime;
        atom.setPosition(8);
        int version = Atom.parseFullAtomVersion(atom.readInt());
        atom.skipBytes(STATE_READING_SAMPLE_CONTINUE);
        long timescale = atom.readUnsignedInt();
        long offset = inputPosition;
        if (version == 0) {
            earliestPresentationTime = atom.readUnsignedInt();
            offset += atom.readUnsignedInt();
        } else {
            earliestPresentationTime = atom.readUnsignedLongToLong();
            offset += atom.readUnsignedLongToLong();
        }
        atom.skipBytes(STATE_READING_ENCRYPTION_DATA);
        int referenceCount = atom.readUnsignedShort();
        int[] sizes = new int[referenceCount];
        long[] offsets = new long[referenceCount];
        long[] durationsUs = new long[referenceCount];
        long[] timesUs = new long[referenceCount];
        long time = earliestPresentationTime;
        long timeUs = Util.scaleLargeTimestamp(time, C.MICROS_PER_SECOND, timescale);
        for (int i = STATE_READING_ATOM_HEADER; i < referenceCount; i += WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) {
            int firstInt = atom.readInt();
            if ((Action.UNDEFINED_DURATION & firstInt) != 0) {
                throw new IllegalStateException("Unhandled indirect reference");
            }
            long referenceDuration = atom.readUnsignedInt();
            sizes[i] = ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED & firstInt;
            offsets[i] = offset;
            timesUs[i] = timeUs;
            time += referenceDuration;
            timeUs = Util.scaleLargeTimestamp(time, C.MICROS_PER_SECOND, timescale);
            durationsUs[i] = timeUs - timesUs[i];
            atom.skipBytes(STATE_READING_SAMPLE_CONTINUE);
            offset += (long) sizes[i];
        }
        return new ChunkIndex(sizes, offsets, durationsUs, timesUs);
    }

    private void readEncryptionData(ExtractorInput input) throws IOException, InterruptedException {
        this.fragmentRun.fillEncryptionData(input);
        this.parserState = STATE_READING_SAMPLE_START;
    }

    private boolean readSample(ExtractorInput input) throws IOException, InterruptedException {
        if (this.sampleIndex >= this.fragmentRun.length) {
            this.parserState = STATE_READING_ATOM_HEADER;
            return false;
        }
        if (this.parserState == STATE_READING_SAMPLE_START) {
            this.sampleSize = this.fragmentRun.sampleSizeTable[this.sampleIndex];
            if (this.fragmentRun.definesEncryptionData) {
                this.sampleBytesWritten = appendSampleEncryptionData(this.fragmentRun.sampleEncryptionData);
                this.sampleSize += this.sampleBytesWritten;
            } else {
                this.sampleBytesWritten = STATE_READING_ATOM_HEADER;
            }
            this.sampleCurrentNalBytesRemaining = STATE_READING_ATOM_HEADER;
            this.parserState = STATE_READING_SAMPLE_CONTINUE;
        }
        if (this.track.nalUnitLengthFieldLength != -1) {
            byte[] nalLengthData = this.nalLength.data;
            nalLengthData[STATE_READING_ATOM_HEADER] = (byte) 0;
            nalLengthData[WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME] = (byte) 0;
            nalLengthData[STATE_READING_ENCRYPTION_DATA] = (byte) 0;
            int nalUnitLengthFieldLength = this.track.nalUnitLengthFieldLength;
            int nalUnitLengthFieldLengthDiff = 4 - this.track.nalUnitLengthFieldLength;
            while (this.sampleBytesWritten < this.sampleSize) {
                if (this.sampleCurrentNalBytesRemaining == 0) {
                    input.readFully(this.nalLength.data, nalUnitLengthFieldLengthDiff, nalUnitLengthFieldLength);
                    this.nalLength.setPosition(STATE_READING_ATOM_HEADER);
                    this.sampleCurrentNalBytesRemaining = this.nalLength.readUnsignedIntToInt();
                    this.nalStartCode.setPosition(STATE_READING_ATOM_HEADER);
                    this.trackOutput.sampleData(this.nalStartCode, (int) STATE_READING_SAMPLE_CONTINUE);
                    this.sampleBytesWritten += STATE_READING_SAMPLE_CONTINUE;
                    this.sampleSize += nalUnitLengthFieldLengthDiff;
                } else {
                    int writtenBytes = this.trackOutput.sampleData(input, this.sampleCurrentNalBytesRemaining);
                    this.sampleBytesWritten += writtenBytes;
                    this.sampleCurrentNalBytesRemaining -= writtenBytes;
                }
            }
        } else {
            while (this.sampleBytesWritten < this.sampleSize) {
                this.sampleBytesWritten += this.trackOutput.sampleData(input, this.sampleSize - this.sampleBytesWritten);
            }
        }
        this.trackOutput.sampleMetadata(this.fragmentRun.getSamplePresentationTime(this.sampleIndex) * 1000, (this.fragmentRun.definesEncryptionData ? STATE_READING_ENCRYPTION_DATA : STATE_READING_ATOM_HEADER) | (this.fragmentRun.sampleIsSyncFrameTable[this.sampleIndex] ? WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME : STATE_READING_ATOM_HEADER), this.sampleSize, STATE_READING_ATOM_HEADER, this.fragmentRun.definesEncryptionData ? this.track.sampleDescriptionEncryptionBoxes[this.fragmentRun.sampleDescriptionIndex].keyId : null);
        this.sampleIndex += WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME;
        this.parserState = STATE_READING_SAMPLE_START;
        return true;
    }

    private int appendSampleEncryptionData(ParsableByteArray sampleEncryptionData) {
        int vectorSize = this.track.sampleDescriptionEncryptionBoxes[this.fragmentRun.sampleDescriptionIndex].initializationVectorSize;
        boolean subsampleEncryption = this.fragmentRun.sampleHasSubsampleEncryptionTable[this.sampleIndex];
        this.encryptionSignalByte.data[STATE_READING_ATOM_HEADER] = (byte) ((subsampleEncryption ? AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS : STATE_READING_ATOM_HEADER) | vectorSize);
        this.encryptionSignalByte.setPosition(STATE_READING_ATOM_HEADER);
        this.trackOutput.sampleData(this.encryptionSignalByte, (int) WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME);
        this.trackOutput.sampleData(sampleEncryptionData, vectorSize);
        if (!subsampleEncryption) {
            return vectorSize + WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME;
        }
        int subsampleCount = sampleEncryptionData.readUnsignedShort();
        sampleEncryptionData.skipBytes(-2);
        int subsampleDataLength = (subsampleCount * 6) + STATE_READING_ENCRYPTION_DATA;
        this.trackOutput.sampleData(sampleEncryptionData, subsampleDataLength);
        return (vectorSize + WORKAROUND_EVERY_VIDEO_FRAME_IS_SYNC_FRAME) + subsampleDataLength;
    }

    private static boolean shouldParseAtom(int atom) {
        return atom == Atom.TYPE_avc1 || atom == Atom.TYPE_avc3 || atom == Atom.TYPE_esds || atom == Atom.TYPE_hdlr || atom == Atom.TYPE_mdat || atom == Atom.TYPE_mdhd || atom == Atom.TYPE_moof || atom == Atom.TYPE_moov || atom == Atom.TYPE_mp4a || atom == Atom.TYPE_mvhd || atom == Atom.TYPE_sidx || atom == Atom.TYPE_stsd || atom == Atom.TYPE_tfdt || atom == Atom.TYPE_tfhd || atom == Atom.TYPE_tkhd || atom == Atom.TYPE_traf || atom == Atom.TYPE_trak || atom == Atom.TYPE_trex || atom == Atom.TYPE_trun || atom == Atom.TYPE_mvex || atom == Atom.TYPE_mdia || atom == Atom.TYPE_minf || atom == Atom.TYPE_stbl || atom == Atom.TYPE_pssh || atom == Atom.TYPE_saiz || atom == Atom.TYPE_uuid || atom == Atom.TYPE_senc || atom == Atom.TYPE_pasp;
    }

    private static boolean shouldParseContainerAtom(int atom) {
        return atom == Atom.TYPE_moov || atom == Atom.TYPE_trak || atom == Atom.TYPE_mdia || atom == Atom.TYPE_minf || atom == Atom.TYPE_stbl || atom == Atom.TYPE_avcC || atom == Atom.TYPE_moof || atom == Atom.TYPE_traf || atom == Atom.TYPE_mvex;
    }
}
