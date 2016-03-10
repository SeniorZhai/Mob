package com.google.android.exoplayer.extractor.ts;

import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.util.ParsableByteArray;

abstract class ElementaryStreamReader {
    protected final TrackOutput output;

    public abstract void consume(ParsableByteArray parsableByteArray, long j, boolean z);

    public abstract void packetFinished();

    public abstract void seek();

    protected ElementaryStreamReader(TrackOutput output) {
        this.output = output;
    }
}
