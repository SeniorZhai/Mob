package com.google.android.exoplayer.chunk;

import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.drm.DrmInitData;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.util.ParsableByteArray;
import com.google.android.exoplayer.util.Util;
import java.io.IOException;

public final class SingleSampleMediaChunk extends BaseMediaChunk {
    private volatile int bytesLoaded;
    private final byte[] headerData;
    private volatile boolean loadCanceled;
    private final DrmInitData sampleDrmInitData;
    private final MediaFormat sampleFormat;
    private boolean writtenHeader;

    public SingleSampleMediaChunk(DataSource dataSource, DataSpec dataSpec, int trigger, Format format, long startTimeUs, long endTimeUs, int chunkIndex, boolean isLastChunk, MediaFormat sampleFormat, DrmInitData sampleDrmInitData, byte[] headerData) {
        super(dataSource, dataSpec, trigger, format, startTimeUs, endTimeUs, chunkIndex, isLastChunk, true);
        this.sampleFormat = sampleFormat;
        this.sampleDrmInitData = sampleDrmInitData;
        this.headerData = headerData;
    }

    public long bytesLoaded() {
        return (long) this.bytesLoaded;
    }

    public MediaFormat getMediaFormat() {
        return this.sampleFormat;
    }

    public DrmInitData getDrmInitData() {
        return this.sampleDrmInitData;
    }

    public void cancelLoad() {
        this.loadCanceled = true;
    }

    public boolean isLoadCanceled() {
        return this.loadCanceled;
    }

    public void load() throws IOException, InterruptedException {
        if (!this.writtenHeader) {
            if (this.headerData != null) {
                getOutput().sampleData(new ParsableByteArray(this.headerData), this.headerData.length);
            }
            this.writtenHeader = true;
        }
        try {
            this.dataSource.open(Util.getRemainderDataSpec(this.dataSpec, this.bytesLoaded));
            int result = 0;
            while (result != -1) {
                result = getOutput().sampleData(this.dataSource, (int) ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
                if (result != -1) {
                    this.bytesLoaded += result;
                }
            }
            int sampleSize = this.bytesLoaded;
            if (this.headerData != null) {
                sampleSize += this.headerData.length;
            }
            getOutput().sampleMetadata(this.startTimeUs, 1, sampleSize, 0, null);
        } finally {
            this.dataSource.close();
        }
    }
}
