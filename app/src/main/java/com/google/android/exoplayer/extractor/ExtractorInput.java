package com.google.android.exoplayer.extractor;

import java.io.IOException;

public interface ExtractorInput {
    long getLength();

    long getPosition();

    int read(byte[] bArr, int i, int i2) throws IOException, InterruptedException;

    void readFully(byte[] bArr, int i, int i2) throws IOException, InterruptedException;

    boolean readFully(byte[] bArr, int i, int i2, boolean z) throws IOException, InterruptedException;

    void skipFully(int i) throws IOException, InterruptedException;
}
