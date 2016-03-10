package com.google.android.exoplayer.extractor.ts;

import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.extractor.ExtractorOutput;
import com.google.android.exoplayer.extractor.PositionHolder;
import com.google.android.exoplayer.extractor.SeekMap;
import com.google.android.exoplayer.util.ParsableByteArray;
import java.io.IOException;

public class AdtsExtractor implements Extractor, SeekMap {
    private static final int MAX_PACKET_SIZE = 200;
    private AdtsReader adtsReader;
    private boolean firstPacket;
    private final long firstSampleTimestampUs;
    private final ParsableByteArray packetBuffer;

    public AdtsExtractor() {
        this(0);
    }

    public AdtsExtractor(long firstSampleTimestampUs) {
        this.firstSampleTimestampUs = firstSampleTimestampUs;
        this.packetBuffer = new ParsableByteArray((int) MAX_PACKET_SIZE);
        this.firstPacket = true;
    }

    public void init(ExtractorOutput output) {
        this.adtsReader = new AdtsReader(output.track(0));
        output.endTracks();
        output.seekMap(this);
    }

    public void seek() {
        this.firstPacket = true;
        this.adtsReader.seek();
    }

    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException, InterruptedException {
        int bytesRead = input.read(this.packetBuffer.data, 0, MAX_PACKET_SIZE);
        if (bytesRead == -1) {
            return -1;
        }
        this.packetBuffer.setPosition(0);
        this.packetBuffer.setLimit(bytesRead);
        this.adtsReader.consume(this.packetBuffer, this.firstSampleTimestampUs, this.firstPacket);
        this.firstPacket = false;
        return 0;
    }

    public boolean isSeekable() {
        return false;
    }

    public long getPosition(long timeUs) {
        return 0;
    }
}
