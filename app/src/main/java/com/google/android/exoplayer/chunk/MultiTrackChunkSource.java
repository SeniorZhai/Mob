package com.google.android.exoplayer.chunk;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer.ExoPlayerComponent;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.TrackInfo;
import com.google.android.exoplayer.util.Assertions;
import java.io.IOException;
import java.util.List;

public class MultiTrackChunkSource implements ChunkSource, ExoPlayerComponent {
    public static final int MSG_SELECT_TRACK = 1;
    private final ChunkSource[] allSources;
    private boolean enabled;
    private ChunkSource selectedSource;

    public MultiTrackChunkSource(ChunkSource... sources) {
        this.allSources = sources;
        this.selectedSource = sources[0];
    }

    public MultiTrackChunkSource(List<ChunkSource> sources) {
        this(toChunkSourceArray(sources));
    }

    public int getTrackCount() {
        return this.allSources.length;
    }

    public TrackInfo getTrackInfo() {
        return this.selectedSource.getTrackInfo();
    }

    public void enable() {
        this.selectedSource.enable();
        this.enabled = true;
    }

    public void disable(List<? extends MediaChunk> queue) {
        this.selectedSource.disable(queue);
        this.enabled = false;
    }

    public void continueBuffering(long playbackPositionUs) {
        this.selectedSource.continueBuffering(playbackPositionUs);
    }

    public void getChunkOperation(List<? extends MediaChunk> queue, long seekPositionUs, long playbackPositionUs, ChunkOperationHolder out) {
        this.selectedSource.getChunkOperation(queue, seekPositionUs, playbackPositionUs, out);
    }

    public IOException getError() {
        return null;
    }

    public void getMaxVideoDimensions(MediaFormat out) {
        this.selectedSource.getMaxVideoDimensions(out);
    }

    public void handleMessage(int what, Object msg) throws ExoPlaybackException {
        Assertions.checkState(!this.enabled);
        if (what == MSG_SELECT_TRACK) {
            this.selectedSource = this.allSources[((Integer) msg).intValue()];
        }
    }

    public void onChunkLoadCompleted(Chunk chunk) {
        this.selectedSource.onChunkLoadCompleted(chunk);
    }

    public void onChunkLoadError(Chunk chunk, Exception e) {
        this.selectedSource.onChunkLoadError(chunk, e);
    }

    private static ChunkSource[] toChunkSourceArray(List<ChunkSource> sources) {
        ChunkSource[] chunkSourceArray = new ChunkSource[sources.size()];
        sources.toArray(chunkSourceArray);
        return chunkSourceArray;
    }
}
