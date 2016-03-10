package com.google.android.exoplayer.audio;

import android.annotation.TargetApi;
import java.util.Arrays;

@TargetApi(21)
public final class AudioCapabilities {
    private final int maxChannelCount;
    private final int[] supportedEncodings;

    public AudioCapabilities(int[] supportedEncodings, int maxChannelCount) {
        if (supportedEncodings != null) {
            this.supportedEncodings = Arrays.copyOf(supportedEncodings, supportedEncodings.length);
            Arrays.sort(this.supportedEncodings);
        } else {
            this.supportedEncodings = new int[0];
        }
        this.maxChannelCount = maxChannelCount;
    }

    public boolean supportsEncoding(int encoding) {
        return Arrays.binarySearch(this.supportedEncodings, encoding) >= 0;
    }

    public int getMaxChannelCount() {
        return this.maxChannelCount;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof AudioCapabilities)) {
            return false;
        }
        AudioCapabilities audioCapabilities = (AudioCapabilities) other;
        if (Arrays.equals(this.supportedEncodings, audioCapabilities.supportedEncodings) && this.maxChannelCount == audioCapabilities.maxChannelCount) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.maxChannelCount + (Arrays.hashCode(this.supportedEncodings) * 31);
    }

    public String toString() {
        return "AudioCapabilities[maxChannelCount=" + this.maxChannelCount + ", supportedEncodings=" + Arrays.toString(this.supportedEncodings) + "]";
    }
}
