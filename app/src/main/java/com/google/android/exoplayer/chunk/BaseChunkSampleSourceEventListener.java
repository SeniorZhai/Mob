package com.google.android.exoplayer.chunk;

import java.io.IOException;

public interface BaseChunkSampleSourceEventListener {
    void onDownstreamFormatChanged(int i, Format format, int i2, int i3);

    void onLoadCanceled(int i, long j);

    void onLoadCompleted(int i, long j, int i2, int i3, Format format, int i4, int i5, long j2, long j3);

    void onLoadError(int i, IOException iOException);

    void onLoadStarted(int i, long j, int i2, int i3, Format format, int i4, int i5);

    void onUpstreamDiscarded(int i, int i2, int i3);
}
