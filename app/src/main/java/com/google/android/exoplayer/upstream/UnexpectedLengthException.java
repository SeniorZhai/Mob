package com.google.android.exoplayer.upstream;

import java.io.IOException;

@Deprecated
public final class UnexpectedLengthException extends IOException {
    public final long actualLength;
    public final long expectedLength;

    public UnexpectedLengthException(long expectedLength, long actualLength) {
        super("Expected: " + expectedLength + ", got: " + actualLength);
        this.expectedLength = expectedLength;
        this.actualLength = actualLength;
    }
}
