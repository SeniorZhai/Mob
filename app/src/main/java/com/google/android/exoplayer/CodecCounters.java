package com.google.android.exoplayer;

public final class CodecCounters {
    public int codecInitCount;
    public int codecReleaseCount;
    public int droppedOutputBufferCount;
    public int outputBuffersChangedCount;
    public int outputFormatChangedCount;
    public int renderedOutputBufferCount;
    public int skippedOutputBufferCount;

    public synchronized void ensureUpdated() {
    }

    public String getDebugString() {
        ensureUpdated();
        StringBuilder builder = new StringBuilder();
        builder.append("cic:").append(this.codecInitCount);
        builder.append(" crc:").append(this.codecReleaseCount);
        builder.append(" ofc:").append(this.outputFormatChangedCount);
        builder.append(" obc:").append(this.outputBuffersChangedCount);
        builder.append(" ren:").append(this.renderedOutputBufferCount);
        builder.append(" sob:").append(this.skippedOutputBufferCount);
        builder.append(" dob:").append(this.droppedOutputBufferCount);
        return builder.toString();
    }
}
