package com.google.android.exoplayer.extractor.mp4;

import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.Util;

final class TrackSampleTable {
    public static final int NO_SAMPLE = -1;
    public final int[] flags;
    public final long[] offsets;
    public final int sampleCount;
    public final int[] sizes;
    public final long[] timestampsUs;

    TrackSampleTable(long[] offsets, int[] sizes, long[] timestampsUs, int[] flags) {
        boolean z;
        boolean z2 = true;
        Assertions.checkArgument(sizes.length == timestampsUs.length);
        if (offsets.length == timestampsUs.length) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkArgument(z);
        if (flags.length != timestampsUs.length) {
            z2 = false;
        }
        Assertions.checkArgument(z2);
        this.offsets = offsets;
        this.sizes = sizes;
        this.timestampsUs = timestampsUs;
        this.flags = flags;
        this.sampleCount = offsets.length;
    }

    public int getIndexOfEarlierOrEqualSynchronizationSample(long timeUs) {
        int i = Util.binarySearchFloor(this.timestampsUs, timeUs, true, false);
        while (i >= 0) {
            if (this.timestampsUs[i] <= timeUs && (this.flags[i] & 1) != 0) {
                return i;
            }
            i += NO_SAMPLE;
        }
        return NO_SAMPLE;
    }

    public int getIndexOfLaterOrEqualSynchronizationSample(long timeUs) {
        int i = Util.binarySearchCeil(this.timestampsUs, timeUs, true, false);
        while (i < this.timestampsUs.length) {
            if (this.timestampsUs[i] >= timeUs && (this.flags[i] & 1) != 0) {
                return i;
            }
            i++;
        }
        return NO_SAMPLE;
    }
}
