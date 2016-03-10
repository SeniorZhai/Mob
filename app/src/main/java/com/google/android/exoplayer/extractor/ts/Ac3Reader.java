package com.google.android.exoplayer.extractor.ts;

import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.util.Ac3Util;
import com.google.android.exoplayer.util.ParsableBitArray;
import com.google.android.exoplayer.util.ParsableByteArray;

final class Ac3Reader extends ElementaryStreamReader {
    private static final int HEADER_SIZE = 8;
    private static final int STATE_FINDING_SYNC = 0;
    private static final int STATE_READING_HEADER = 1;
    private static final int STATE_READING_SAMPLE = 2;
    private int bitrate;
    private int bytesRead;
    private long frameDurationUs;
    private final ParsableBitArray headerScratchBits = new ParsableBitArray(new byte[HEADER_SIZE]);
    private final ParsableByteArray headerScratchBytes = new ParsableByteArray(this.headerScratchBits.data);
    private boolean lastByteWas0B;
    private MediaFormat mediaFormat;
    private int sampleSize;
    private int state = STATE_FINDING_SYNC;
    private long timeUs;

    public Ac3Reader(TrackOutput output) {
        super(output);
    }

    public void seek() {
        this.state = STATE_FINDING_SYNC;
        this.bytesRead = STATE_FINDING_SYNC;
        this.lastByteWas0B = false;
    }

    public void consume(ParsableByteArray data, long pesTimeUs, boolean startOfPacket) {
        if (startOfPacket) {
            this.timeUs = pesTimeUs;
        }
        while (data.bytesLeft() > 0) {
            switch (this.state) {
                case STATE_FINDING_SYNC /*0*/:
                    if (!skipToNextSync(data)) {
                        break;
                    }
                    this.state = STATE_READING_HEADER;
                    this.headerScratchBytes.data[STATE_FINDING_SYNC] = (byte) 11;
                    this.headerScratchBytes.data[STATE_READING_HEADER] = (byte) 119;
                    this.bytesRead = STATE_READING_SAMPLE;
                    break;
                case STATE_READING_HEADER /*1*/:
                    if (!continueRead(data, this.headerScratchBytes.data, HEADER_SIZE)) {
                        break;
                    }
                    parseHeader();
                    this.headerScratchBytes.setPosition(STATE_FINDING_SYNC);
                    this.output.sampleData(this.headerScratchBytes, (int) HEADER_SIZE);
                    this.state = STATE_READING_SAMPLE;
                    break;
                case STATE_READING_SAMPLE /*2*/:
                    int bytesToRead = Math.min(data.bytesLeft(), this.sampleSize - this.bytesRead);
                    this.output.sampleData(data, bytesToRead);
                    this.bytesRead += bytesToRead;
                    if (this.bytesRead != this.sampleSize) {
                        break;
                    }
                    this.output.sampleMetadata(this.timeUs, STATE_READING_HEADER, this.sampleSize, STATE_FINDING_SYNC, null);
                    this.timeUs += this.frameDurationUs;
                    this.state = STATE_FINDING_SYNC;
                    break;
                default:
                    break;
            }
        }
    }

    public void packetFinished() {
    }

    private boolean continueRead(ParsableByteArray source, byte[] target, int targetLength) {
        int bytesToRead = Math.min(source.bytesLeft(), targetLength - this.bytesRead);
        source.readBytes(target, this.bytesRead, bytesToRead);
        this.bytesRead += bytesToRead;
        return this.bytesRead == targetLength;
    }

    private boolean skipToNextSync(ParsableByteArray pesBuffer) {
        while (pesBuffer.bytesLeft() > 0) {
            if (this.lastByteWas0B) {
                int secondByte = pesBuffer.readUnsignedByte();
                if (secondByte == 119) {
                    this.lastByteWas0B = false;
                    return true;
                }
                this.lastByteWas0B = secondByte == 11;
            } else {
                this.lastByteWas0B = pesBuffer.readUnsignedByte() == 11;
            }
        }
        return false;
    }

    private void parseHeader() {
        this.headerScratchBits.setPosition(STATE_FINDING_SYNC);
        this.sampleSize = Ac3Util.parseFrameSize(this.headerScratchBits);
        if (this.mediaFormat == null) {
            this.headerScratchBits.setPosition(STATE_FINDING_SYNC);
            this.mediaFormat = Ac3Util.parseFrameAc3Format(this.headerScratchBits);
            this.output.format(this.mediaFormat);
            this.bitrate = Ac3Util.getBitrate(this.sampleSize, this.mediaFormat.sampleRate);
        }
        this.frameDurationUs = (long) ((int) ((8000 * ((long) this.sampleSize)) / ((long) this.bitrate)));
    }
}
