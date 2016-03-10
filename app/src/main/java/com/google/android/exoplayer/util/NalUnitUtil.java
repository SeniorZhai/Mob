package com.google.android.exoplayer.util;

import android.support.v4.media.TransportMediator;
import com.android.volley.DefaultRetryPolicy;
import java.util.Arrays;

public final class NalUnitUtil {
    public static final float[] ASPECT_RATIO_IDC_VALUES = new float[]{DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 1.0909091f, 0.90909094f, 1.4545455f, 1.2121212f, 2.1818182f, 1.8181819f, 2.909091f, 2.4242425f, 1.6363636f, 1.3636364f, 1.939394f, 1.6161616f, 1.3333334f, 1.5f, 2.0f};
    public static final int EXTENDED_SAR = 255;
    public static final byte[] NAL_START_CODE = new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 1};
    private static int[] scratchEscapePositions = new int[10];
    private static final Object scratchEscapePositionsLock = new Object();

    public static int unescapeStream(byte[] data, int limit) {
        synchronized (scratchEscapePositionsLock) {
            int position = 0;
            int scratchEscapeCount = 0;
            while (position < limit) {
                try {
                    position = findNextUnescapeIndex(data, position, limit);
                    if (position < limit) {
                        if (scratchEscapePositions.length <= scratchEscapeCount) {
                            scratchEscapePositions = Arrays.copyOf(scratchEscapePositions, scratchEscapePositions.length * 2);
                        }
                        int scratchEscapeCount2 = scratchEscapeCount + 1;
                        try {
                            scratchEscapePositions[scratchEscapeCount] = position;
                            position += 3;
                            scratchEscapeCount = scratchEscapeCount2;
                        } catch (Throwable th) {
                            Throwable th2 = th;
                            throw th2;
                        }
                    }
                } catch (Throwable th3) {
                    th2 = th3;
                    scratchEscapeCount2 = scratchEscapeCount;
                }
            }
            int unescapedLength = limit - scratchEscapeCount;
            int escapedPosition = 0;
            int unescapedPosition = 0;
            for (int i = 0; i < scratchEscapeCount; i++) {
                int copyLength = scratchEscapePositions[i] - escapedPosition;
                System.arraycopy(data, escapedPosition, data, unescapedPosition, copyLength);
                unescapedPosition += copyLength;
                int i2 = unescapedPosition + 1;
                data[unescapedPosition] = (byte) 0;
                unescapedPosition = i2 + 1;
                data[i2] = (byte) 0;
                escapedPosition += copyLength + 3;
            }
            System.arraycopy(data, escapedPosition, data, unescapedPosition, unescapedLength - unescapedPosition);
            return unescapedLength;
        }
    }

    public static byte[] parseChildNalUnit(ParsableByteArray atom) {
        int length = atom.readUnsignedShort();
        int offset = atom.getPosition();
        atom.skipBytes(length);
        return CodecSpecificDataUtil.buildNalUnit(atom.data, offset, length);
    }

    public static int getNalUnitType(byte[] data, int offset) {
        return data[offset + 3] & 31;
    }

    public static int getH265NalUnitType(byte[] data, int offset) {
        return (data[offset + 3] & TransportMediator.KEYCODE_MEDIA_PLAY) >> 1;
    }

    public static int findNalUnit(byte[] data, int startOffset, int endOffset, boolean[] prefixFlags) {
        boolean z;
        boolean z2 = true;
        int length = endOffset - startOffset;
        if (length >= 0) {
            z = true;
        } else {
            z = false;
        }
        Assertions.checkState(z);
        if (length == 0) {
            return endOffset;
        }
        if (prefixFlags != null) {
            if (prefixFlags[0]) {
                clearPrefixFlags(prefixFlags);
                return startOffset - 3;
            } else if (length > 1 && prefixFlags[1] && data[startOffset] == (byte) 1) {
                clearPrefixFlags(prefixFlags);
                return startOffset - 2;
            } else if (length > 2 && prefixFlags[2] && data[startOffset] == (byte) 0 && data[startOffset + 1] == (byte) 1) {
                clearPrefixFlags(prefixFlags);
                return startOffset - 1;
            }
        }
        int limit = endOffset - 1;
        int i = startOffset + 2;
        while (i < limit) {
            if ((data[i] & 254) == 0) {
                if (data[i - 2] == (byte) 0 && data[i - 1] == (byte) 0 && data[i] == (byte) 1) {
                    if (prefixFlags != null) {
                        clearPrefixFlags(prefixFlags);
                    }
                    return i - 2;
                }
                i -= 2;
            }
            i += 3;
        }
        if (prefixFlags == null) {
            return endOffset;
        }
        z = length > 2 ? data[endOffset + -3] == (byte) 0 && data[endOffset - 2] == (byte) 0 && data[endOffset - 1] == (byte) 1 : length == 2 ? prefixFlags[2] && data[endOffset - 2] == (byte) 0 && data[endOffset - 1] == (byte) 1 : prefixFlags[1] && data[endOffset - 1] == (byte) 1;
        prefixFlags[0] = z;
        z = length > 1 ? data[endOffset + -2] == (byte) 0 && data[endOffset - 1] == (byte) 0 : prefixFlags[2] && data[endOffset - 1] == (byte) 0;
        prefixFlags[1] = z;
        if (data[endOffset - 1] != (byte) 0) {
            z2 = false;
        }
        prefixFlags[2] = z2;
        return endOffset;
    }

    public static void clearPrefixFlags(boolean[] prefixFlags) {
        prefixFlags[0] = false;
        prefixFlags[1] = false;
        prefixFlags[2] = false;
    }

    private static int findNextUnescapeIndex(byte[] bytes, int offset, int limit) {
        int i = offset;
        while (i < limit - 2) {
            if (bytes[i] == (byte) 0 && bytes[i + 1] == (byte) 0 && bytes[i + 2] == (byte) 3) {
                return i;
            }
            i++;
        }
        return limit;
    }

    private NalUnitUtil() {
    }
}
