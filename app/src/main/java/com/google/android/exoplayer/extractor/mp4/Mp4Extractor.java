package com.google.android.exoplayer.extractor.mp4;

import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.extractor.ExtractorOutput;
import com.google.android.exoplayer.extractor.PositionHolder;
import com.google.android.exoplayer.extractor.SeekMap;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.extractor.mp4.Atom.ContainerAtom;
import com.google.android.exoplayer.extractor.mp4.Atom.LeafAtom;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.NalUnitUtil;
import com.google.android.exoplayer.util.ParsableByteArray;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public final class Mp4Extractor implements Extractor, SeekMap {
    private static final int RELOAD_MINIMUM_SEEK_DISTANCE = 262144;
    private static final int STATE_READING_ATOM_HEADER = 0;
    private static final int STATE_READING_ATOM_PAYLOAD = 1;
    private static final int STATE_READING_SAMPLE = 2;
    private int atomBytesRead;
    private ParsableByteArray atomData;
    private final ParsableByteArray atomHeader = new ParsableByteArray(16);
    private long atomSize;
    private int atomType;
    private final Stack<ContainerAtom> containerAtoms = new Stack();
    private ExtractorOutput extractorOutput;
    private final ParsableByteArray nalLength = new ParsableByteArray(4);
    private final ParsableByteArray nalStartCode = new ParsableByteArray(NalUnitUtil.NAL_START_CODE);
    private int parserState = STATE_READING_ATOM_HEADER;
    private long rootAtomBytesRead;
    private int sampleBytesWritten;
    private int sampleCurrentNalBytesRemaining;
    private int sampleSize;
    private Mp4Track[] tracks;

    private static final class Mp4Track {
        public int sampleIndex;
        public final TrackSampleTable sampleTable;
        public final Track track;
        public final TrackOutput trackOutput;

        public Mp4Track(Track track, TrackSampleTable sampleTable, TrackOutput trackOutput) {
            this.track = track;
            this.sampleTable = sampleTable;
            this.trackOutput = trackOutput;
        }
    }

    public void init(ExtractorOutput output) {
        this.extractorOutput = output;
    }

    public void seek() {
        this.rootAtomBytesRead = 0;
        this.sampleBytesWritten = STATE_READING_ATOM_HEADER;
        this.sampleCurrentNalBytesRemaining = STATE_READING_ATOM_HEADER;
    }

    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        while (true) {
            switch (this.parserState) {
                case STATE_READING_ATOM_HEADER /*0*/:
                    if (readAtomHeader(input)) {
                        break;
                    }
                    return -1;
                case STATE_READING_ATOM_PAYLOAD /*1*/:
                    if (!readAtomPayload(input, seekPosition)) {
                        break;
                    }
                    return STATE_READING_ATOM_PAYLOAD;
                default:
                    return readSample(input, seekPosition);
            }
        }
    }

    public boolean isSeekable() {
        return true;
    }

    public long getPosition(long timeUs) {
        long earliestSamplePosition = Long.MAX_VALUE;
        for (int trackIndex = STATE_READING_ATOM_HEADER; trackIndex < this.tracks.length; trackIndex += STATE_READING_ATOM_PAYLOAD) {
            TrackSampleTable sampleTable = this.tracks[trackIndex].sampleTable;
            int sampleIndex = sampleTable.getIndexOfEarlierOrEqualSynchronizationSample(timeUs);
            if (sampleIndex == -1) {
                sampleIndex = sampleTable.getIndexOfLaterOrEqualSynchronizationSample(timeUs);
            }
            this.tracks[trackIndex].sampleIndex = sampleIndex;
            long offset = sampleTable.offsets[this.tracks[trackIndex].sampleIndex];
            if (offset < earliestSamplePosition) {
                earliestSamplePosition = offset;
            }
        }
        return earliestSamplePosition;
    }

    private boolean readAtomHeader(ExtractorInput input) throws IOException, InterruptedException {
        if (!input.readFully(this.atomHeader.data, STATE_READING_ATOM_HEADER, 8, true)) {
            return false;
        }
        this.atomHeader.setPosition(STATE_READING_ATOM_HEADER);
        this.atomSize = this.atomHeader.readUnsignedInt();
        this.atomType = this.atomHeader.readInt();
        if (this.atomSize == 1) {
            input.readFully(this.atomHeader.data, 8, 8);
            this.atomSize = this.atomHeader.readLong();
            this.rootAtomBytesRead += 16;
            this.atomBytesRead = 16;
        } else {
            this.rootAtomBytesRead += 8;
            this.atomBytesRead = 8;
        }
        if (shouldParseContainerAtom(this.atomType)) {
            if (this.atomSize == 1) {
                this.containerAtoms.add(new ContainerAtom(this.atomType, (this.rootAtomBytesRead + this.atomSize) - ((long) this.atomBytesRead)));
            } else {
                this.containerAtoms.add(new ContainerAtom(this.atomType, (this.rootAtomBytesRead + this.atomSize) - ((long) this.atomBytesRead)));
            }
            this.parserState = STATE_READING_ATOM_HEADER;
        } else if (shouldParseLeafAtom(this.atomType)) {
            boolean z;
            if (this.atomSize < 2147483647L) {
                z = true;
            } else {
                z = false;
            }
            Assertions.checkState(z);
            this.atomData = new ParsableByteArray((int) this.atomSize);
            System.arraycopy(this.atomHeader.data, STATE_READING_ATOM_HEADER, this.atomData.data, STATE_READING_ATOM_HEADER, 8);
            this.parserState = STATE_READING_ATOM_PAYLOAD;
        } else {
            this.atomData = null;
            this.parserState = STATE_READING_ATOM_PAYLOAD;
        }
        return true;
    }

    private boolean readAtomPayload(ExtractorInput input, PositionHolder positionHolder) throws IOException, InterruptedException {
        boolean seekRequired = false;
        this.parserState = STATE_READING_ATOM_HEADER;
        this.rootAtomBytesRead += this.atomSize - ((long) this.atomBytesRead);
        long atomRemainingBytes = this.atomSize - ((long) this.atomBytesRead);
        if (this.atomData == null && (this.atomSize >= 262144 || this.atomSize > 2147483647L)) {
            seekRequired = true;
        }
        if (seekRequired) {
            positionHolder.position = this.rootAtomBytesRead;
        } else if (this.atomData != null) {
            input.readFully(this.atomData.data, this.atomBytesRead, (int) atomRemainingBytes);
            if (!this.containerAtoms.isEmpty()) {
                ((ContainerAtom) this.containerAtoms.peek()).add(new LeafAtom(this.atomType, this.atomData));
            }
        } else {
            input.skipFully((int) atomRemainingBytes);
        }
        while (!this.containerAtoms.isEmpty() && ((ContainerAtom) this.containerAtoms.peek()).endByteOffset == this.rootAtomBytesRead) {
            ContainerAtom containerAtom = (ContainerAtom) this.containerAtoms.pop();
            if (containerAtom.type == Atom.TYPE_moov) {
                processMoovAtom(containerAtom);
            } else if (!this.containerAtoms.isEmpty()) {
                ((ContainerAtom) this.containerAtoms.peek()).add(containerAtom);
            }
        }
        return seekRequired;
    }

    private void processMoovAtom(ContainerAtom moov) {
        List<Mp4Track> tracks = new ArrayList();
        long earliestSampleOffset = Long.MAX_VALUE;
        for (int i = STATE_READING_ATOM_HEADER; i < moov.containerChildren.size(); i += STATE_READING_ATOM_PAYLOAD) {
            ContainerAtom atom = (ContainerAtom) moov.containerChildren.get(i);
            if (atom.type == Atom.TYPE_trak) {
                Track track = AtomParsers.parseTrak(atom, moov.getLeafAtomOfType(Atom.TYPE_mvhd));
                if (track != null && (track.type == Track.TYPE_AUDIO || track.type == Track.TYPE_VIDEO || track.type == Track.TYPE_TEXT)) {
                    TrackSampleTable trackSampleTable = AtomParsers.parseStbl(track, atom.getContainerAtomOfType(Atom.TYPE_mdia).getContainerAtomOfType(Atom.TYPE_minf).getContainerAtomOfType(Atom.TYPE_stbl));
                    if (trackSampleTable.sampleCount != 0) {
                        Mp4Track mp4Track = new Mp4Track(track, trackSampleTable, this.extractorOutput.track(i));
                        mp4Track.trackOutput.format(track.mediaFormat);
                        tracks.add(mp4Track);
                        long firstSampleOffset = trackSampleTable.offsets[STATE_READING_ATOM_HEADER];
                        if (firstSampleOffset < earliestSampleOffset) {
                            earliestSampleOffset = firstSampleOffset;
                        }
                    }
                }
            }
        }
        this.tracks = (Mp4Track[]) tracks.toArray(new Mp4Track[STATE_READING_ATOM_HEADER]);
        this.extractorOutput.endTracks();
        this.extractorOutput.seekMap(this);
        this.parserState = STATE_READING_SAMPLE;
    }

    private int readSample(ExtractorInput input, PositionHolder positionHolder) throws IOException, InterruptedException {
        int trackIndex = getTrackIndexOfEarliestCurrentSample();
        if (trackIndex == -1) {
            return -1;
        }
        Mp4Track track = this.tracks[trackIndex];
        int sampleIndex = track.sampleIndex;
        long position = track.sampleTable.offsets[sampleIndex];
        long skipAmount = (position - input.getPosition()) + ((long) this.sampleBytesWritten);
        if (skipAmount < 0 || skipAmount >= 262144) {
            positionHolder.position = position;
            return STATE_READING_ATOM_PAYLOAD;
        }
        input.skipFully((int) skipAmount);
        this.sampleSize = track.sampleTable.sizes[sampleIndex];
        int writtenBytes;
        if (track.track.nalUnitLengthFieldLength != -1) {
            byte[] nalLengthData = this.nalLength.data;
            nalLengthData[STATE_READING_ATOM_HEADER] = (byte) 0;
            nalLengthData[STATE_READING_ATOM_PAYLOAD] = (byte) 0;
            nalLengthData[STATE_READING_SAMPLE] = (byte) 0;
            int nalUnitLengthFieldLength = track.track.nalUnitLengthFieldLength;
            int nalUnitLengthFieldLengthDiff = 4 - track.track.nalUnitLengthFieldLength;
            while (this.sampleBytesWritten < this.sampleSize) {
                if (this.sampleCurrentNalBytesRemaining == 0) {
                    input.readFully(this.nalLength.data, nalUnitLengthFieldLengthDiff, nalUnitLengthFieldLength);
                    this.nalLength.setPosition(STATE_READING_ATOM_HEADER);
                    this.sampleCurrentNalBytesRemaining = this.nalLength.readUnsignedIntToInt();
                    this.nalStartCode.setPosition(STATE_READING_ATOM_HEADER);
                    track.trackOutput.sampleData(this.nalStartCode, 4);
                    this.sampleBytesWritten += 4;
                    this.sampleSize += nalUnitLengthFieldLengthDiff;
                } else {
                    writtenBytes = track.trackOutput.sampleData(input, this.sampleCurrentNalBytesRemaining);
                    this.sampleBytesWritten += writtenBytes;
                    this.sampleCurrentNalBytesRemaining -= writtenBytes;
                }
            }
        } else {
            while (this.sampleBytesWritten < this.sampleSize) {
                writtenBytes = track.trackOutput.sampleData(input, this.sampleSize - this.sampleBytesWritten);
                this.sampleBytesWritten += writtenBytes;
                this.sampleCurrentNalBytesRemaining -= writtenBytes;
            }
        }
        track.trackOutput.sampleMetadata(track.sampleTable.timestampsUs[sampleIndex], track.sampleTable.flags[sampleIndex], this.sampleSize, STATE_READING_ATOM_HEADER, null);
        track.sampleIndex += STATE_READING_ATOM_PAYLOAD;
        this.sampleBytesWritten = STATE_READING_ATOM_HEADER;
        this.sampleCurrentNalBytesRemaining = STATE_READING_ATOM_HEADER;
        return STATE_READING_ATOM_HEADER;
    }

    private int getTrackIndexOfEarliestCurrentSample() {
        int earliestSampleTrackIndex = -1;
        long earliestSampleOffset = Long.MAX_VALUE;
        for (int trackIndex = STATE_READING_ATOM_HEADER; trackIndex < this.tracks.length; trackIndex += STATE_READING_ATOM_PAYLOAD) {
            Mp4Track track = this.tracks[trackIndex];
            int sampleIndex = track.sampleIndex;
            if (sampleIndex != track.sampleTable.sampleCount) {
                long trackSampleOffset = track.sampleTable.offsets[sampleIndex];
                if (trackSampleOffset < earliestSampleOffset) {
                    earliestSampleOffset = trackSampleOffset;
                    earliestSampleTrackIndex = trackIndex;
                }
            }
        }
        return earliestSampleTrackIndex;
    }

    private static boolean shouldParseLeafAtom(int atom) {
        return atom == Atom.TYPE_mdhd || atom == Atom.TYPE_mvhd || atom == Atom.TYPE_hdlr || atom == Atom.TYPE_vmhd || atom == Atom.TYPE_smhd || atom == Atom.TYPE_stsd || atom == Atom.TYPE_avc1 || atom == Atom.TYPE_avcC || atom == Atom.TYPE_mp4a || atom == Atom.TYPE_esds || atom == Atom.TYPE_stts || atom == Atom.TYPE_stss || atom == Atom.TYPE_ctts || atom == Atom.TYPE_stsc || atom == Atom.TYPE_stsz || atom == Atom.TYPE_stco || atom == Atom.TYPE_co64 || atom == Atom.TYPE_tkhd;
    }

    private static boolean shouldParseContainerAtom(int atom) {
        return atom == Atom.TYPE_moov || atom == Atom.TYPE_trak || atom == Atom.TYPE_mdia || atom == Atom.TYPE_minf || atom == Atom.TYPE_stbl;
    }
}
