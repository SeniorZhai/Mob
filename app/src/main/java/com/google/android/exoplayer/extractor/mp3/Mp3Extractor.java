package com.google.android.exoplayer.extractor.mp3;

import android.support.v4.media.TransportMediator;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.ParserException;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.extractor.ExtractorOutput;
import com.google.android.exoplayer.extractor.PositionHolder;
import com.google.android.exoplayer.extractor.SeekMap;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.util.MpegAudioHeader;
import com.google.android.exoplayer.util.ParsableByteArray;
import com.google.android.exoplayer.util.Util;
import com.mobcrush.mobcrush.Constants;
import java.io.EOFException;
import java.io.IOException;

public final class Mp3Extractor implements Extractor {
    private static final int HEADER_MASK = -128000;
    private static final int ID3_TAG = Util.getIntegerCodeForString("ID3");
    private static final int INFO_HEADER = Util.getIntegerCodeForString("Info");
    private static final int MAX_BYTES_TO_SEARCH = 131072;
    private static final int VBRI_HEADER = Util.getIntegerCodeForString("VBRI");
    private static final int XING_HEADER = Util.getIntegerCodeForString("Xing");
    private long basisTimeUs;
    private ExtractorOutput extractorOutput;
    private final BufferingInput inputBuffer = new BufferingInput(12288);
    private int sampleBytesRemaining;
    private int samplesRead;
    private final ParsableByteArray scratch = new ParsableByteArray(4);
    private Seeker seeker;
    private final MpegAudioHeader synchronizedHeader = new MpegAudioHeader();
    private int synchronizedHeaderData;
    private TrackOutput trackOutput;

    interface Seeker extends SeekMap {
        long getDurationUs();

        long getTimeUs(long j);
    }

    public void init(ExtractorOutput extractorOutput) {
        this.extractorOutput = extractorOutput;
        this.trackOutput = extractorOutput.track(INFO_HEADER);
        extractorOutput.endTracks();
    }

    public void seek() {
        this.synchronizedHeaderData = INFO_HEADER;
        this.samplesRead = INFO_HEADER;
        this.basisTimeUs = -1;
        this.sampleBytesRemaining = INFO_HEADER;
        this.inputBuffer.reset();
    }

    public int read(ExtractorInput extractorInput, PositionHolder seekPosition) throws IOException, InterruptedException {
        if (this.synchronizedHeaderData == 0 && synchronizeCatchingEndOfInput(extractorInput) == -1) {
            return -1;
        }
        return readSample(extractorInput);
    }

    private int readSample(ExtractorInput extractorInput) throws IOException, InterruptedException {
        if (this.sampleBytesRemaining == 0) {
            if (maybeResynchronize(extractorInput) == -1) {
                return -1;
            }
            if (this.basisTimeUs == -1) {
                this.basisTimeUs = this.seeker.getTimeUs(getPosition(extractorInput, this.inputBuffer));
            }
            this.sampleBytesRemaining = this.synchronizedHeader.frameSize;
        }
        long timeUs = this.basisTimeUs + ((((long) this.samplesRead) * C.MICROS_PER_SECOND) / ((long) this.synchronizedHeader.sampleRate));
        this.sampleBytesRemaining -= this.inputBuffer.drainToOutput(this.trackOutput, this.sampleBytesRemaining);
        if (this.sampleBytesRemaining > 0) {
            this.inputBuffer.mark();
            this.sampleBytesRemaining -= this.trackOutput.sampleData(extractorInput, this.sampleBytesRemaining);
            if (this.sampleBytesRemaining > 0) {
                return INFO_HEADER;
            }
        }
        this.trackOutput.sampleMetadata(timeUs, 1, this.synchronizedHeader.frameSize, INFO_HEADER, null);
        this.samplesRead += this.synchronizedHeader.samplesPerFrame;
        this.sampleBytesRemaining = INFO_HEADER;
        return INFO_HEADER;
    }

    private long maybeResynchronize(ExtractorInput extractorInput) throws IOException, InterruptedException {
        this.inputBuffer.mark();
        if (!this.inputBuffer.readAllowingEndOfInput(extractorInput, this.scratch.data, INFO_HEADER, 4)) {
            return -1;
        }
        this.inputBuffer.returnToMark();
        this.scratch.setPosition(INFO_HEADER);
        int sampleHeaderData = this.scratch.readInt();
        if ((sampleHeaderData & HEADER_MASK) != (this.synchronizedHeaderData & HEADER_MASK) || MpegAudioHeader.getFrameSize(sampleHeaderData) == -1) {
            this.synchronizedHeaderData = INFO_HEADER;
            this.inputBuffer.skip(extractorInput, 1);
            return synchronizeCatchingEndOfInput(extractorInput);
        }
        MpegAudioHeader.populateHeader(sampleHeaderData, this.synchronizedHeader);
        return 0;
    }

    private long synchronizeCatchingEndOfInput(ExtractorInput extractorInput) throws IOException, InterruptedException {
        try {
            return synchronize(extractorInput);
        } catch (EOFException e) {
            return -1;
        }
    }

    private long synchronize(ExtractorInput extractorInput) throws IOException, InterruptedException {
        if (extractorInput.getPosition() == 0) {
            this.inputBuffer.reset();
        } else {
            this.inputBuffer.returnToMark();
        }
        long startPosition = getPosition(extractorInput, this.inputBuffer);
        if (startPosition == 0) {
            this.inputBuffer.read(extractorInput, this.scratch.data, INFO_HEADER, 3);
            this.scratch.setPosition(INFO_HEADER);
            if (this.scratch.readUnsignedInt24() == ID3_TAG) {
                extractorInput.skipFully(3);
                extractorInput.readFully(this.scratch.data, INFO_HEADER, 4);
                extractorInput.skipFully(((((this.scratch.data[INFO_HEADER] & TransportMediator.KEYCODE_MEDIA_PAUSE) << 21) | ((this.scratch.data[1] & TransportMediator.KEYCODE_MEDIA_PAUSE) << 14)) | ((this.scratch.data[2] & TransportMediator.KEYCODE_MEDIA_PAUSE) << 7)) | (this.scratch.data[3] & TransportMediator.KEYCODE_MEDIA_PAUSE));
                this.inputBuffer.reset();
                startPosition = getPosition(extractorInput, this.inputBuffer);
            } else {
                this.inputBuffer.returnToMark();
            }
        }
        this.inputBuffer.mark();
        long headerPosition = startPosition;
        int validFrameCount = INFO_HEADER;
        int candidateSynchronizedHeaderData = INFO_HEADER;
        while (headerPosition - startPosition < 131072) {
            if (!this.inputBuffer.readAllowingEndOfInput(extractorInput, this.scratch.data, INFO_HEADER, 4)) {
                return -1;
            }
            this.scratch.setPosition(INFO_HEADER);
            int headerData = this.scratch.readInt();
            if (candidateSynchronizedHeaderData == 0 || (HEADER_MASK & headerData) == (HEADER_MASK & candidateSynchronizedHeaderData)) {
                int frameSize = MpegAudioHeader.getFrameSize(headerData);
                if (frameSize != -1) {
                    if (validFrameCount == 0) {
                        MpegAudioHeader.populateHeader(headerData, this.synchronizedHeader);
                        candidateSynchronizedHeaderData = headerData;
                    }
                    validFrameCount++;
                    if (validFrameCount == 4) {
                        this.inputBuffer.returnToMark();
                        this.synchronizedHeaderData = candidateSynchronizedHeaderData;
                        if (this.seeker != null) {
                            return headerPosition;
                        }
                        setupSeeker(extractorInput, headerPosition);
                        this.extractorOutput.seekMap(this.seeker);
                        this.trackOutput.format(MediaFormat.createAudioFormat(this.synchronizedHeader.mimeType, MpegAudioHeader.MAX_FRAME_SIZE_BYTES, this.seeker.getDurationUs(), this.synchronizedHeader.channels, this.synchronizedHeader.sampleRate, null));
                        return headerPosition;
                    }
                    this.inputBuffer.skip(extractorInput, frameSize - 4);
                }
            }
            validFrameCount = INFO_HEADER;
            candidateSynchronizedHeaderData = INFO_HEADER;
            this.inputBuffer.returnToMark();
            this.inputBuffer.skip(extractorInput, 1);
            this.inputBuffer.mark();
            headerPosition++;
        }
        throw new ParserException("Searched too many bytes while resynchronizing.");
    }

    private void setupSeeker(ExtractorInput extractorInput, long headerPosition) throws IOException, InterruptedException {
        if (parseSeekerFrame(extractorInput, headerPosition, extractorInput.getLength())) {
            this.inputBuffer.mark();
            if (this.seeker == null) {
                this.inputBuffer.read(extractorInput, this.scratch.data, INFO_HEADER, 4);
                this.scratch.setPosition(INFO_HEADER);
                headerPosition += (long) this.synchronizedHeader.frameSize;
                MpegAudioHeader.populateHeader(this.scratch.readInt(), this.synchronizedHeader);
            } else {
                return;
            }
        }
        this.inputBuffer.returnToMark();
        this.seeker = new ConstantBitrateSeeker(headerPosition, this.synchronizedHeader.bitrate * Constants.UPDATE_COFIG_INTERVAL, extractorInput.getLength());
    }

    private boolean parseSeekerFrame(ExtractorInput extractorInput, long headerPosition, long inputLength) throws IOException, InterruptedException {
        int xingBase;
        this.inputBuffer.mark();
        this.seeker = null;
        ParsableByteArray frame = this.inputBuffer.getParsableByteArray(extractorInput, this.synchronizedHeader.frameSize);
        if ((this.synchronizedHeader.version & 1) == 1) {
            if (this.synchronizedHeader.channels != 1) {
                xingBase = 32;
            } else {
                xingBase = 17;
            }
        } else if (this.synchronizedHeader.channels != 1) {
            xingBase = 17;
        } else {
            xingBase = 9;
        }
        frame.setPosition(xingBase + 4);
        int headerData = frame.readInt();
        if (headerData == XING_HEADER || headerData == INFO_HEADER) {
            this.seeker = XingSeeker.create(this.synchronizedHeader, frame, headerPosition, inputLength);
            return true;
        }
        frame.setPosition(36);
        if (frame.readInt() != VBRI_HEADER) {
            return false;
        }
        this.seeker = VbriSeeker.create(this.synchronizedHeader, frame, headerPosition);
        return true;
    }

    private static long getPosition(ExtractorInput extractorInput, BufferingInput bufferingInput) {
        return extractorInput.getPosition() - ((long) bufferingInput.getAvailableByteCount());
    }
}
