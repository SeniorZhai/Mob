package com.google.android.exoplayer.chunk;

import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.util.Assertions;

public abstract class MediaChunk extends Chunk {
    public final int chunkIndex;
    public final long endTimeUs;
    public final boolean isLastChunk;
    public final long startTimeUs;

    public MediaChunk(DataSource dataSource, DataSpec dataSpec, int trigger, Format format, long startTimeUs, long endTimeUs, int chunkIndex, boolean isLastChunk) {
        super(dataSource, dataSpec, 1, trigger, format);
        Assertions.checkNotNull(format);
        this.startTimeUs = startTimeUs;
        this.endTimeUs = endTimeUs;
        this.chunkIndex = chunkIndex;
        this.isLastChunk = isLastChunk;
    }
}
