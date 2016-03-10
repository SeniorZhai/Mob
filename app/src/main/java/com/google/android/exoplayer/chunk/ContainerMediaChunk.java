package com.google.android.exoplayer.chunk;

import android.util.Log;
import com.google.android.exoplayer.MediaFormat;
import com.google.android.exoplayer.chunk.ChunkExtractorWrapper.SingleTrackOutput;
import com.google.android.exoplayer.drm.DrmInitData;
import com.google.android.exoplayer.extractor.DefaultExtractorInput;
import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.extractor.SeekMap;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.util.ParsableByteArray;
import com.google.android.exoplayer.util.Util;
import java.io.IOException;

public class ContainerMediaChunk extends BaseMediaChunk implements SingleTrackOutput {
    private static final String TAG = "ContainerMediaChunk";
    private volatile int bytesLoaded;
    private DrmInitData drmInitData;
    private final ChunkExtractorWrapper extractorWrapper;
    private volatile boolean loadCanceled;
    private MediaFormat mediaFormat;
    private final long sampleOffsetUs;

    public ContainerMediaChunk(DataSource dataSource, DataSpec dataSpec, int trigger, Format format, long startTimeUs, long endTimeUs, int chunkIndex, boolean isLastChunk, long sampleOffsetUs, ChunkExtractorWrapper extractorWrapper, MediaFormat mediaFormat, DrmInitData drmInitData, boolean isMediaFormatFinal) {
        super(dataSource, dataSpec, trigger, format, startTimeUs, endTimeUs, chunkIndex, isLastChunk, isMediaFormatFinal);
        this.extractorWrapper = extractorWrapper;
        this.sampleOffsetUs = sampleOffsetUs;
        this.mediaFormat = mediaFormat;
        this.drmInitData = drmInitData;
    }

    public long bytesLoaded() {
        return (long) this.bytesLoaded;
    }

    public MediaFormat getMediaFormat() {
        return this.mediaFormat;
    }

    public DrmInitData getDrmInitData() {
        return this.drmInitData;
    }

    public void seekMap(SeekMap seekMap) {
        Log.w(TAG, "Ignoring unexpected seekMap");
    }

    public void drmInitData(DrmInitData drmInitData) {
        this.drmInitData = drmInitData;
    }

    public void format(MediaFormat mediaFormat) {
        this.mediaFormat = mediaFormat;
    }

    public int sampleData(ExtractorInput input, int length) throws IOException, InterruptedException {
        return getOutput().sampleData(input, length);
    }

    public void sampleData(ParsableByteArray data, int length) {
        getOutput().sampleData(data, length);
    }

    public void sampleMetadata(long timeUs, int flags, int size, int offset, byte[] encryptionKey) {
        getOutput().sampleMetadata(this.sampleOffsetUs + timeUs, flags, size, offset, encryptionKey);
    }

    public void cancelLoad() {
        this.loadCanceled = true;
    }

    public boolean isLoadCanceled() {
        return this.loadCanceled;
    }

    public void load() throws IOException, InterruptedException {
        ExtractorInput input;
        DataSpec loadDataSpec = Util.getRemainderDataSpec(this.dataSpec, this.bytesLoaded);
        try {
            input = new DefaultExtractorInput(this.dataSource, loadDataSpec.absoluteStreamPosition, this.dataSource.open(loadDataSpec));
            if (this.bytesLoaded == 0) {
                this.extractorWrapper.init(this);
            }
            int result = 0;
            while (result == 0) {
                if (!this.loadCanceled) {
                    result = this.extractorWrapper.read(input);
                }
            }
            this.bytesLoaded = (int) (input.getPosition() - this.dataSpec.absoluteStreamPosition);
            this.dataSource.close();
        } catch (Throwable th) {
            this.dataSource.close();
        }
    }
}
