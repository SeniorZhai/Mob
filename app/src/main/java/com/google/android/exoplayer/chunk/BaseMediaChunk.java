package com.google.android.exoplayer.chunk;

import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.drm.DrmInitData;
import com.google.android.exoplayer.extractor.DefaultTrackOutput;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;

public abstract class BaseMediaChunk extends MediaChunk {
    private int firstSampleIndex;
    public final boolean isMediaFormatFinal;
    private DefaultTrackOutput output;

    public abstract DrmInitData getDrmInitData();

    public abstract MediaFormat getMediaFormat();

    public BaseMediaChunk(DataSource dataSource, DataSpec dataSpec, int trigger, Format format, long startTimeUs, long endTimeUs, int chunkIndex, boolean isLastChunk, boolean isMediaFormatFinal) {
        super(dataSource, dataSpec, trigger, format, startTimeUs, endTimeUs, chunkIndex, isLastChunk);
        this.isMediaFormatFinal = isMediaFormatFinal;
    }

    public void init(DefaultTrackOutput output) {
        this.output = output;
        this.firstSampleIndex = output.getWriteIndex();
    }

    public final int getFirstSampleIndex() {
        return this.firstSampleIndex;
    }

    protected final DefaultTrackOutput getOutput() {
        return this.output;
    }
}
