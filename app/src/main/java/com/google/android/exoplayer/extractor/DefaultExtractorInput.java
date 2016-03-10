package com.google.android.exoplayer.extractor;

import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.util.MpegAudioHeader;
import java.io.EOFException;
import java.io.IOException;

public final class DefaultExtractorInput implements ExtractorInput {
    private static final byte[] SCRATCH_SPACE = new byte[MpegAudioHeader.MAX_FRAME_SIZE_BYTES];
    private final DataSource dataSource;
    private long length;
    private long position;

    public DefaultExtractorInput(DataSource dataSource, long position, long length) {
        this.dataSource = dataSource;
        this.position = position;
        this.length = length;
    }

    public int read(byte[] target, int offset, int length) throws IOException, InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        int bytesRead = this.dataSource.read(target, offset, length);
        if (bytesRead == -1) {
            return -1;
        }
        this.position += (long) bytesRead;
        return bytesRead;
    }

    public boolean readFully(byte[] target, int offset, int length, boolean allowEndOfInput) throws IOException, InterruptedException {
        int remaining = length;
        while (remaining > 0) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            int bytesRead = this.dataSource.read(target, offset, remaining);
            if (bytesRead != -1) {
                offset += bytesRead;
                remaining -= bytesRead;
            } else if (allowEndOfInput && remaining == length) {
                return false;
            } else {
                throw new EOFException();
            }
        }
        this.position += (long) length;
        return true;
    }

    public void readFully(byte[] target, int offset, int length) throws IOException, InterruptedException {
        readFully(target, offset, length, false);
    }

    public void skipFully(int length) throws IOException, InterruptedException {
        int remaining = length;
        while (remaining > 0) {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            int bytesRead = this.dataSource.read(SCRATCH_SPACE, 0, Math.min(SCRATCH_SPACE.length, remaining));
            if (bytesRead == -1) {
                throw new EOFException();
            }
            remaining -= bytesRead;
        }
        this.position += (long) length;
    }

    public long getPosition() {
        return this.position;
    }

    public long getLength() {
        return this.length;
    }
}
