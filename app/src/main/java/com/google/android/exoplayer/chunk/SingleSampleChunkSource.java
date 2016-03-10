package com.google.android.exoplayer.chunk;

import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.TrackInfo;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import java.io.IOException;
import java.util.List;

public class SingleSampleChunkSource implements ChunkSource {
    private final DataSource dataSource;
    private final DataSpec dataSpec;
    private final long durationUs;
    private final Format format;
    private final MediaFormat mediaFormat;
    private final TrackInfo trackInfo;

    public SingleSampleChunkSource(DataSource dataSource, DataSpec dataSpec, Format format, long durationUs, MediaFormat mediaFormat) {
        this.dataSource = dataSource;
        this.dataSpec = dataSpec;
        this.format = format;
        this.durationUs = durationUs;
        this.mediaFormat = mediaFormat;
        this.trackInfo = new TrackInfo(format.mimeType, durationUs);
    }

    public TrackInfo getTrackInfo() {
        return this.trackInfo;
    }

    public void getMaxVideoDimensions(MediaFormat out) {
    }

    public void enable() {
    }

    public void continueBuffering(long playbackPositionUs) {
    }

    public void getChunkOperation(List<? extends MediaChunk> queue, long seekPositionUs, long playbackPositionUs, ChunkOperationHolder out) {
        if (queue.isEmpty()) {
            out.chunk = initChunk();
        }
    }

    public void disable(List<? extends MediaChunk> list) {
    }

    public IOException getError() {
        return null;
    }

    public void onChunkLoadCompleted(Chunk chunk) {
    }

    public void onChunkLoadError(Chunk chunk, Exception e) {
    }

    private SingleSampleMediaChunk initChunk() {
        return new SingleSampleMediaChunk(this.dataSource, this.dataSpec, 0, this.format, 0, this.durationUs, 0, true, this.mediaFormat, null, null);
    }
}
