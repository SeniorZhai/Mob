package com.google.android.exoplayer.hls;

import android.util.SparseArray;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.SampleHolder;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.drm.DrmInitData;
import com.google.android.exoplayer.extractor.DefaultTrackOutput;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.extractor.ExtractorOutput;
import com.google.android.exoplayer.extractor.SeekMap;
import com.google.android.exoplayer.extractor.TrackOutput;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.util.Assertions;
import java.io.IOException;

public final class HlsExtractorWrapper implements ExtractorOutput {
    private Allocator allocator;
    private final Extractor extractor;
    public final Format format;
    private boolean prepared;
    private final SparseArray<DefaultTrackOutput> sampleQueues = new SparseArray();
    private final boolean shouldSpliceIn;
    private boolean spliceConfigured;
    public final long startTimeUs;
    private volatile boolean tracksBuilt;
    public final int trigger;

    public HlsExtractorWrapper(int trigger, Format format, long startTimeUs, Extractor extractor, boolean shouldSpliceIn) {
        this.trigger = trigger;
        this.format = format;
        this.startTimeUs = startTimeUs;
        this.extractor = extractor;
        this.shouldSpliceIn = shouldSpliceIn;
    }

    public void init(Allocator allocator) {
        this.allocator = allocator;
        this.extractor.init(this);
    }

    public boolean isPrepared() {
        if (!this.prepared && this.tracksBuilt) {
            for (int i = 0; i < this.sampleQueues.size(); i++) {
                if (!((DefaultTrackOutput) this.sampleQueues.valueAt(i)).hasFormat()) {
                    return false;
                }
            }
            this.prepared = true;
        }
        return this.prepared;
    }

    public void clear() {
        for (int i = 0; i < this.sampleQueues.size(); i++) {
            ((DefaultTrackOutput) this.sampleQueues.valueAt(i)).clear();
        }
    }

    public long getLargestParsedTimestampUs() {
        long largestParsedTimestampUs = Long.MIN_VALUE;
        for (int i = 0; i < this.sampleQueues.size(); i++) {
            largestParsedTimestampUs = Math.max(largestParsedTimestampUs, ((DefaultTrackOutput) this.sampleQueues.valueAt(i)).getLargestParsedTimestampUs());
        }
        return largestParsedTimestampUs;
    }

    public final void configureSpliceTo(HlsExtractorWrapper nextExtractor) {
        Assertions.checkState(isPrepared());
        if (!this.spliceConfigured && nextExtractor.shouldSpliceIn && nextExtractor.isPrepared()) {
            boolean spliceConfigured = true;
            for (int i = 0; i < getTrackCount(); i++) {
                spliceConfigured &= ((DefaultTrackOutput) this.sampleQueues.valueAt(i)).configureSpliceTo((DefaultTrackOutput) nextExtractor.sampleQueues.valueAt(i));
            }
            this.spliceConfigured = spliceConfigured;
        }
    }

    public int getTrackCount() {
        Assertions.checkState(isPrepared());
        return this.sampleQueues.size();
    }

    public MediaFormat getMediaFormat(int track) {
        Assertions.checkState(isPrepared());
        return ((DefaultTrackOutput) this.sampleQueues.valueAt(track)).getFormat();
    }

    public boolean getSample(int track, SampleHolder holder) {
        Assertions.checkState(isPrepared());
        return ((DefaultTrackOutput) this.sampleQueues.valueAt(track)).getSample(holder);
    }

    public void discardUntil(int track, long timeUs) {
        Assertions.checkState(isPrepared());
        ((DefaultTrackOutput) this.sampleQueues.valueAt(track)).discardUntil(timeUs);
    }

    public boolean hasSamples(int track) {
        Assertions.checkState(isPrepared());
        return !((DefaultTrackOutput) this.sampleQueues.valueAt(track)).isEmpty();
    }

    public int read(ExtractorInput input) throws IOException, InterruptedException {
        boolean z = true;
        int result = this.extractor.read(input, null);
        if (result == 1) {
            z = false;
        }
        Assertions.checkState(z);
        return result;
    }

    public TrackOutput track(int id) {
        DefaultTrackOutput sampleQueue = new DefaultTrackOutput(this.allocator);
        this.sampleQueues.put(id, sampleQueue);
        return sampleQueue;
    }

    public void endTracks() {
        this.tracksBuilt = true;
    }

    public void seekMap(SeekMap seekMap) {
    }

    public void drmInitData(DrmInitData drmInit) {
    }
}
