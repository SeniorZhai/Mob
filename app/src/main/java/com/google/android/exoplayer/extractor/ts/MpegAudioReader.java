package com.google.android.exoplayer.extractor.ts;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.util.MpegAudioHeader;
import com.google.android.exoplayer.util.ParsableByteArray;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

class MpegAudioReader extends ElementaryStreamReader {
    private static final int HEADER_SIZE = 4;
    private static final int STATE_FINDING_HEADER = 0;
    private static final int STATE_READING_FRAME = 2;
    private static final int STATE_READING_HEADER = 1;
    private int frameBytesRead;
    private long frameDurationUs;
    private int frameSize;
    private boolean hasOutputFormat;
    private final MpegAudioHeader header;
    private final ParsableByteArray headerScratch = new ParsableByteArray((int) HEADER_SIZE);
    private boolean lastByteWasFF;
    private int state = STATE_FINDING_HEADER;
    private long timeUs;

    public MpegAudioReader(TrackOutput output) {
        super(output);
        this.headerScratch.data[STATE_FINDING_HEADER] = (byte) -1;
        this.header = new MpegAudioHeader();
    }

    public void seek() {
        this.state = STATE_FINDING_HEADER;
        this.frameBytesRead = STATE_FINDING_HEADER;
        this.lastByteWasFF = false;
    }

    public void consume(ParsableByteArray data, long pesTimeUs, boolean startOfPacket) {
        if (startOfPacket) {
            this.timeUs = pesTimeUs;
        }
        while (data.bytesLeft() > 0) {
            switch (this.state) {
                case STATE_FINDING_HEADER /*0*/:
                    findHeader(data);
                    break;
                case STATE_READING_HEADER /*1*/:
                    readHeaderRemainder(data);
                    break;
                case STATE_READING_FRAME /*2*/:
                    readFrameRemainder(data);
                    break;
                default:
                    break;
            }
        }
    }

    public void packetFinished() {
    }

    private void findHeader(ParsableByteArray source) {
        byte[] data = source.data;
        int startOffset = source.getPosition();
        int endOffset = source.limit();
        int i = startOffset;
        while (i < endOffset) {
            boolean found;
            boolean byteIsFF = (data[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) == SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            if (this.lastByteWasFF && (data[i] & 224) == 224) {
                found = true;
            } else {
                found = false;
            }
            this.lastByteWasFF = byteIsFF;
            if (found) {
                source.setPosition(i + STATE_READING_HEADER);
                this.lastByteWasFF = false;
                this.headerScratch.data[STATE_READING_HEADER] = data[i];
                this.frameBytesRead = STATE_READING_FRAME;
                this.state = STATE_READING_HEADER;
                return;
            }
            i += STATE_READING_HEADER;
        }
        source.setPosition(endOffset);
    }

    private void readHeaderRemainder(ParsableByteArray source) {
        int bytesToRead = Math.min(source.bytesLeft(), 4 - this.frameBytesRead);
        source.readBytes(this.headerScratch.data, this.frameBytesRead, bytesToRead);
        this.frameBytesRead += bytesToRead;
        if (this.frameBytesRead >= HEADER_SIZE) {
            this.headerScratch.setPosition(STATE_FINDING_HEADER);
            if (MpegAudioHeader.populateHeader(this.headerScratch.readInt(), this.header)) {
                this.frameSize = this.header.frameSize;
                if (!this.hasOutputFormat) {
                    this.frameDurationUs = (C.MICROS_PER_SECOND * ((long) this.header.samplesPerFrame)) / ((long) this.header.sampleRate);
                    this.output.format(MediaFormat.createAudioFormat(this.header.mimeType, MpegAudioHeader.MAX_FRAME_SIZE_BYTES, -1, this.header.channels, this.header.sampleRate, null));
                    this.hasOutputFormat = true;
                }
                this.headerScratch.setPosition(STATE_FINDING_HEADER);
                this.output.sampleData(this.headerScratch, (int) HEADER_SIZE);
                this.state = STATE_READING_FRAME;
                return;
            }
            this.frameBytesRead = STATE_FINDING_HEADER;
            this.state = STATE_READING_HEADER;
        }
    }

    private void readFrameRemainder(ParsableByteArray source) {
        int bytesToRead = Math.min(source.bytesLeft(), this.frameSize - this.frameBytesRead);
        this.output.sampleData(source, bytesToRead);
        this.frameBytesRead += bytesToRead;
        if (this.frameBytesRead >= this.frameSize) {
            this.output.sampleMetadata(this.timeUs, STATE_READING_HEADER, this.frameSize, STATE_FINDING_HEADER, null);
            this.timeUs += this.frameDurationUs;
            this.frameBytesRead = STATE_FINDING_HEADER;
            this.state = STATE_FINDING_HEADER;
        }
    }
}
