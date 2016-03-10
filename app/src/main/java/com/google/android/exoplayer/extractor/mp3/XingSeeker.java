package com.google.android.exoplayer.extractor.mp3;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.util.MpegAudioHeader;
import com.google.android.exoplayer.util.ParsableByteArray;
import com.google.android.exoplayer.util.Util;

final class XingSeeker implements Seeker {
    private final long durationUs;
    private final long firstFramePosition;
    private final long inputLength;
    private final long sizeBytes;
    private final long[] tableOfContents;

    public static XingSeeker create(MpegAudioHeader mpegAudioHeader, ParsableByteArray frame, long position, long inputLength) {
        int samplesPerFrame = mpegAudioHeader.samplesPerFrame;
        int sampleRate = mpegAudioHeader.sampleRate;
        long firstFramePosition = position + ((long) mpegAudioHeader.frameSize);
        if ((frame.readInt() & 7) != 7) {
            return null;
        }
        int frameCount = frame.readUnsignedIntToInt();
        if (frameCount == 0) {
            return null;
        }
        long durationUs = Util.scaleLargeTimestamp((long) frameCount, ((long) samplesPerFrame) * C.MICROS_PER_SECOND, (long) sampleRate);
        long sizeBytes = (long) frame.readUnsignedIntToInt();
        frame.skipBytes(1);
        long[] tableOfContents = new long[99];
        for (int i = 0; i < 99; i++) {
            tableOfContents[i] = (long) frame.readUnsignedByte();
        }
        return new XingSeeker(tableOfContents, firstFramePosition, sizeBytes, durationUs, inputLength);
    }

    private XingSeeker(long[] tableOfContents, long firstFramePosition, long sizeBytes, long durationUs, long inputLength) {
        this.tableOfContents = tableOfContents;
        this.firstFramePosition = firstFramePosition;
        this.sizeBytes = sizeBytes;
        this.durationUs = durationUs;
        this.inputLength = inputLength;
    }

    public boolean isSeekable() {
        return true;
    }

    public long getPosition(long timeUs) {
        float fx;
        float percent = (((float) timeUs) * 100.0f) / ((float) this.durationUs);
        if (percent <= 0.0f) {
            fx = 0.0f;
        } else if (percent >= 100.0f) {
            fx = 256.0f;
        } else {
            float fa;
            float fb;
            int a = (int) percent;
            if (a == 0) {
                fa = 0.0f;
            } else {
                fa = (float) this.tableOfContents[a - 1];
            }
            if (a < 99) {
                fb = (float) this.tableOfContents[a];
            } else {
                fb = 256.0f;
            }
            fx = fa + ((fb - fa) * (percent - ((float) a)));
        }
        long position = ((long) ((0.00390625f * fx) * ((float) this.sizeBytes))) + this.firstFramePosition;
        return this.inputLength != -1 ? Math.min(position, this.inputLength - 1) : position;
    }

    public long getTimeUs(long position) {
        long offsetByte = (256 * (position - this.firstFramePosition)) / this.sizeBytes;
        int previousIndex = Util.binarySearchFloor(this.tableOfContents, offsetByte, true, false);
        long previousTime = getTimeUsForTocIndex(previousIndex);
        if (previousIndex == 98) {
            return previousTime;
        }
        long previousByte = previousIndex == -1 ? 0 : this.tableOfContents[previousIndex];
        return previousTime + (((getTimeUsForTocIndex(previousIndex + 1) - previousTime) * (offsetByte - previousByte)) / (this.tableOfContents[previousIndex + 1] - previousByte));
    }

    public long getDurationUs() {
        return this.durationUs;
    }

    private long getTimeUsForTocIndex(int tocIndex) {
        return (this.durationUs * ((long) (tocIndex + 1))) / 100;
    }
}
