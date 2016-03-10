package com.google.android.exoplayer.chunk;

import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.TrackInfo;
import java.io.IOException;
import java.util.List;

public interface ChunkSource {
    void continueBuffering(long j);

    void disable(List<? extends MediaChunk> list);

    void enable();

    void getChunkOperation(List<? extends MediaChunk> list, long j, long j2, ChunkOperationHolder chunkOperationHolder);

    IOException getError();

    void getMaxVideoDimensions(MediaFormat mediaFormat);

    TrackInfo getTrackInfo();

    void onChunkLoadCompleted(Chunk chunk);

    void onChunkLoadError(Chunk chunk, Exception exception);
}
