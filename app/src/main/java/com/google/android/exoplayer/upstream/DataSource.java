package com.google.android.exoplayer.upstream;

import java.io.IOException;

public interface DataSource {
    void close() throws IOException;

    long open(DataSpec dataSpec) throws IOException;

    int read(byte[] bArr, int i, int i2) throws IOException;
}
