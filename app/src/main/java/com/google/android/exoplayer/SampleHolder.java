package com.google.android.exoplayer;

import java.nio.ByteBuffer;

public final class SampleHolder {
    public static final int BUFFER_REPLACEMENT_MODE_DIRECT = 2;
    public static final int BUFFER_REPLACEMENT_MODE_DISABLED = 0;
    public static final int BUFFER_REPLACEMENT_MODE_NORMAL = 1;
    private final int bufferReplacementMode;
    public final CryptoInfo cryptoInfo = new CryptoInfo();
    public ByteBuffer data;
    public int flags;
    public int size;
    public long timeUs;

    public SampleHolder(int bufferReplacementMode) {
        this.bufferReplacementMode = bufferReplacementMode;
    }

    public boolean replaceBuffer(int capacity) {
        switch (this.bufferReplacementMode) {
            case BUFFER_REPLACEMENT_MODE_NORMAL /*1*/:
                this.data = ByteBuffer.allocate(capacity);
                return true;
            case BUFFER_REPLACEMENT_MODE_DIRECT /*2*/:
                this.data = ByteBuffer.allocateDirect(capacity);
                return true;
            default:
                return false;
        }
    }

    public boolean isEncrypted() {
        return (this.flags & BUFFER_REPLACEMENT_MODE_DIRECT) != 0;
    }

    public boolean isDecodeOnly() {
        return (this.flags & C.SAMPLE_FLAG_DECODE_ONLY) != 0;
    }

    public boolean isSyncFrame() {
        return (this.flags & BUFFER_REPLACEMENT_MODE_NORMAL) != 0;
    }

    public void clearData() {
        if (this.data != null) {
            this.data.clear();
        }
    }
}
