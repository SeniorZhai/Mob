package com.google.android.exoplayer;

import java.io.IOException;

public interface SampleSource {
    public static final int DISCONTINUITY_READ = -5;
    public static final int END_OF_STREAM = -1;
    public static final int FORMAT_READ = -4;
    public static final int NOTHING_READ = -2;
    public static final int SAMPLE_READ = -3;

    public interface SampleSourceReader {
        boolean continueBuffering(long j) throws IOException;

        void disable(int i);

        void enable(int i, long j);

        long getBufferedPositionUs();

        int getTrackCount();

        TrackInfo getTrackInfo(int i);

        boolean prepare(long j) throws IOException;

        int readData(int i, long j, MediaFormatHolder mediaFormatHolder, SampleHolder sampleHolder, boolean z) throws IOException;

        void release();

        void seekToUs(long j);
    }

    SampleSourceReader register();
}
