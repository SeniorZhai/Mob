package com.google.android.exoplayer.dash;

import com.google.android.exoplayer.dash.mpd.RangedUri;
import com.google.android.exoplayer.extractor.ChunkIndex;

public class DashWrappingSegmentIndex implements DashSegmentIndex {
    private final ChunkIndex chunkIndex;
    private final long startTimeUs;
    private final String uri;

    public DashWrappingSegmentIndex(ChunkIndex chunkIndex, String uri, long startTimeUs) {
        this.chunkIndex = chunkIndex;
        this.uri = uri;
        this.startTimeUs = startTimeUs;
    }

    public int getFirstSegmentNum() {
        return 0;
    }

    public int getLastSegmentNum() {
        return this.chunkIndex.length - 1;
    }

    public long getTimeUs(int segmentNum) {
        return this.chunkIndex.timesUs[segmentNum] + this.startTimeUs;
    }

    public long getDurationUs(int segmentNum) {
        return this.chunkIndex.durationsUs[segmentNum];
    }

    public RangedUri getSegmentUrl(int segmentNum) {
        return new RangedUri(this.uri, null, this.chunkIndex.offsets[segmentNum], (long) this.chunkIndex.sizes[segmentNum]);
    }

    public int getSegmentNum(long timeUs) {
        return this.chunkIndex.getChunkIndex(timeUs - this.startTimeUs);
    }

    public boolean isExplicit() {
        return true;
    }
}
