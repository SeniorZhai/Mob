package com.google.android.exoplayer.extractor.ts;

import android.util.Pair;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.util.CodecSpecificDataUtil;
import com.google.android.exoplayer.util.MimeTypes;
import com.google.android.exoplayer.util.ParsableBitArray;
import com.google.android.exoplayer.util.ParsableByteArray;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.Collections;

class AdtsReader extends ElementaryStreamReader {
    private static final int CRC_SIZE = 2;
    private static final int HEADER_SIZE = 5;
    private static final int STATE_FINDING_SYNC = 0;
    private static final int STATE_READING_HEADER = 1;
    private static final int STATE_READING_SAMPLE = 2;
    private final ParsableBitArray adtsScratch = new ParsableBitArray(new byte[7]);
    private int bytesRead;
    private long frameDurationUs;
    private boolean hasCrc;
    private boolean hasOutputFormat;
    private boolean lastByteWasFF;
    private int sampleSize;
    private int state = STATE_FINDING_SYNC;
    private long timeUs;

    public AdtsReader(TrackOutput output) {
        super(output);
    }

    public void seek() {
        this.state = STATE_FINDING_SYNC;
        this.bytesRead = STATE_FINDING_SYNC;
        this.lastByteWasFF = false;
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
                    this.bytesRead = STATE_FINDING_SYNC;
                    this.state = STATE_READING_HEADER;
                    break;
                case STATE_READING_HEADER /*1*/:
                    if (!continueRead(data, this.adtsScratch.data, this.hasCrc ? 7 : HEADER_SIZE)) {
                        break;
                    }
                    parseHeader();
                    this.bytesRead = STATE_FINDING_SYNC;
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
                    this.bytesRead = STATE_FINDING_SYNC;
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
        byte[] adtsData = pesBuffer.data;
        int startOffset = pesBuffer.getPosition();
        int endOffset = pesBuffer.limit();
        int i = startOffset;
        while (i < endOffset) {
            boolean found;
            boolean byteIsFF = (adtsData[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) == SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            if (this.lastByteWasFF && !byteIsFF && (adtsData[i] & 240) == 240) {
                found = true;
            } else {
                found = false;
            }
            this.lastByteWasFF = byteIsFF;
            if (found) {
                boolean z;
                if ((adtsData[i] & STATE_READING_HEADER) == 0) {
                    z = true;
                } else {
                    z = false;
                }
                this.hasCrc = z;
                pesBuffer.setPosition(i + STATE_READING_HEADER);
                this.lastByteWasFF = false;
                return true;
            }
            i += STATE_READING_HEADER;
        }
        pesBuffer.setPosition(endOffset);
        return false;
    }

    private void parseHeader() {
        this.adtsScratch.setPosition(STATE_FINDING_SYNC);
        if (this.hasOutputFormat) {
            this.adtsScratch.skipBits(10);
        } else {
            int audioObjectType = this.adtsScratch.readBits(STATE_READING_SAMPLE) + STATE_READING_HEADER;
            int sampleRateIndex = this.adtsScratch.readBits(4);
            this.adtsScratch.skipBits(STATE_READING_HEADER);
            byte[] audioSpecificConfig = CodecSpecificDataUtil.buildAacAudioSpecificConfig(audioObjectType, sampleRateIndex, this.adtsScratch.readBits(3));
            Pair<Integer, Integer> audioParams = CodecSpecificDataUtil.parseAacAudioSpecificConfig(audioSpecificConfig);
            MediaFormat mediaFormat = MediaFormat.createAudioFormat(MimeTypes.AUDIO_AAC, -1, ((Integer) audioParams.second).intValue(), ((Integer) audioParams.first).intValue(), Collections.singletonList(audioSpecificConfig));
            this.frameDurationUs = 1024000000 / ((long) mediaFormat.sampleRate);
            this.output.format(mediaFormat);
            this.hasOutputFormat = true;
        }
        this.adtsScratch.skipBits(4);
        this.sampleSize = (this.adtsScratch.readBits(13) - 2) - 5;
        if (this.hasCrc) {
            this.sampleSize -= 2;
        }
    }
}
