package com.fasterxml.jackson.core.io;

import android.support.v7.widget.RecyclerView.SmoothScroller.Action;
import com.firebase.client.core.Constants;
import com.helpshift.res.values.HSConsts;

public final class NumberOutput {
    private static int BILLION = 1000000000;
    static final char[] FULL_TRIPLETS = new char[4000];
    static final byte[] FULL_TRIPLETS_B = new byte[4000];
    static final char[] LEADING_TRIPLETS = new char[4000];
    private static long MAX_INT_AS_LONG = 2147483647L;
    private static int MILLION = 1000000;
    private static long MIN_INT_AS_LONG = -2147483648L;
    private static final char NULL_CHAR = '\u0000';
    static final String SMALLEST_LONG = String.valueOf(Long.MIN_VALUE);
    private static long TEN_BILLION_L = 10000000000L;
    private static long THOUSAND_L = 1000;
    static final String[] sSmallIntStrs = new String[]{HSConsts.STATUS_NEW, HSConsts.STATUS_INPROGRESS, HSConsts.STATUS_RESOLVED, HSConsts.STATUS_REJECTED, "4", Constants.WIRE_PROTOCOL_VERSION, "6", "7", "8", "9", "10"};
    static final String[] sSmallIntStrs2 = new String[]{"-1", "-2", "-3", "-4", "-5", "-6", "-7", "-8", "-9", "-10"};

    static {
        int i = 0;
        for (int i2 = 0; i2 < 10; i2++) {
            char c;
            char c2 = (char) (i2 + 48);
            if (i2 == 0) {
                c = '\u0000';
            } else {
                c = c2;
            }
            int i3 = 0;
            while (i3 < 10) {
                char c3;
                char c4 = (char) (i3 + 48);
                if (i2 == 0 && i3 == 0) {
                    c3 = '\u0000';
                } else {
                    c3 = c4;
                }
                int i4 = i;
                for (i = 0; i < 10; i++) {
                    char c5 = (char) (i + 48);
                    LEADING_TRIPLETS[i4] = c;
                    LEADING_TRIPLETS[i4 + 1] = c3;
                    LEADING_TRIPLETS[i4 + 2] = c5;
                    FULL_TRIPLETS[i4] = c2;
                    FULL_TRIPLETS[i4 + 1] = c4;
                    FULL_TRIPLETS[i4 + 2] = c5;
                    i4 += 4;
                }
                i3++;
                i = i4;
            }
        }
        for (int i5 = 0; i5 < 4000; i5++) {
            FULL_TRIPLETS_B[i5] = (byte) FULL_TRIPLETS[i5];
        }
    }

    public static int outputInt(int i, char[] cArr, int i2) {
        if (i < 0) {
            if (i == Action.UNDEFINED_DURATION) {
                return outputLong((long) i, cArr, i2);
            }
            int i3 = i2 + 1;
            cArr[i2] = '-';
            i = -i;
            i2 = i3;
        }
        if (i >= MILLION) {
            Object obj = i >= BILLION ? 1 : null;
            if (obj != null) {
                i -= BILLION;
                if (i >= BILLION) {
                    i -= BILLION;
                    i3 = i2 + 1;
                    cArr[i2] = '2';
                    i2 = i3;
                } else {
                    i3 = i2 + 1;
                    cArr[i2] = '1';
                    i2 = i3;
                }
            }
            i3 = i / com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL;
            int i4 = i - (i3 * com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL);
            int i5 = i3 / com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL;
            int i6 = i3 - (i5 * com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL);
            if (obj != null) {
                i3 = outputFullTriplet(i5, cArr, i2);
            } else {
                i3 = outputLeadingTriplet(i5, cArr, i2);
            }
            return outputFullTriplet(i4, cArr, outputFullTriplet(i6, cArr, i3));
        } else if (i >= com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL) {
            i3 = i / com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL;
            return outputFullTriplet(i - (i3 * com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL), cArr, outputLeadingTriplet(i3, cArr, i2));
        } else if (i >= 10) {
            return outputLeadingTriplet(i, cArr, i2);
        } else {
            i3 = i2 + 1;
            cArr[i2] = (char) (i + 48);
            return i3;
        }
    }

    public static int outputInt(int i, byte[] bArr, int i2) {
        if (i < 0) {
            if (i == Action.UNDEFINED_DURATION) {
                return outputLong((long) i, bArr, i2);
            }
            int i3 = i2 + 1;
            bArr[i2] = ClosedCaptionCtrl.CARRIAGE_RETURN;
            i = -i;
            i2 = i3;
        }
        if (i >= MILLION) {
            Object obj = i >= BILLION ? 1 : null;
            if (obj != null) {
                i -= BILLION;
                if (i >= BILLION) {
                    i -= BILLION;
                    i3 = i2 + 1;
                    bArr[i2] = (byte) 50;
                    i2 = i3;
                } else {
                    i3 = i2 + 1;
                    bArr[i2] = (byte) 49;
                    i2 = i3;
                }
            }
            i3 = i / com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL;
            int i4 = i - (i3 * com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL);
            int i5 = i3 / com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL;
            int i6 = i3 - (i5 * com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL);
            if (obj != null) {
                i3 = outputFullTriplet(i5, bArr, i2);
            } else {
                i3 = outputLeadingTriplet(i5, bArr, i2);
            }
            return outputFullTriplet(i4, bArr, outputFullTriplet(i6, bArr, i3));
        } else if (i >= com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL) {
            i3 = i / com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL;
            return outputFullTriplet(i - (i3 * com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL), bArr, outputLeadingTriplet(i3, bArr, i2));
        } else if (i >= 10) {
            return outputLeadingTriplet(i, bArr, i2);
        } else {
            i3 = i2 + 1;
            bArr[i2] = (byte) (i + 48);
            return i3;
        }
    }

    public static int outputLong(long j, char[] cArr, int i) {
        int length;
        if (j < 0) {
            if (j > MIN_INT_AS_LONG) {
                return outputInt((int) j, cArr, i);
            }
            if (j == Long.MIN_VALUE) {
                length = SMALLEST_LONG.length();
                SMALLEST_LONG.getChars(0, length, cArr, i);
                return i + length;
            }
            length = i + 1;
            cArr[i] = '-';
            j = -j;
            i = length;
        } else if (j <= MAX_INT_AS_LONG) {
            return outputInt((int) j, cArr, i);
        }
        int calcLongStrLength = i + calcLongStrLength(j);
        length = calcLongStrLength;
        while (j > MAX_INT_AS_LONG) {
            length -= 3;
            long j2 = j / THOUSAND_L;
            outputFullTriplet((int) (j - (THOUSAND_L * j2)), cArr, length);
            j = j2;
        }
        int i2 = length;
        length = (int) j;
        while (length >= com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL) {
            int i3 = i2 - 3;
            i2 = length / com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL;
            outputFullTriplet(length - (i2 * com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL), cArr, i3);
            length = i2;
            i2 = i3;
        }
        outputLeadingTriplet(length, cArr, i);
        return calcLongStrLength;
    }

    public static int outputLong(long j, byte[] bArr, int i) {
        int length;
        int i2;
        int i3;
        if (j < 0) {
            if (j > MIN_INT_AS_LONG) {
                return outputInt((int) j, bArr, i);
            }
            if (j == Long.MIN_VALUE) {
                length = SMALLEST_LONG.length();
                i2 = 0;
                i3 = i;
                while (i2 < length) {
                    i = i3 + 1;
                    bArr[i3] = (byte) SMALLEST_LONG.charAt(i2);
                    i2++;
                    i3 = i;
                }
                return i3;
            }
            i2 = i + 1;
            bArr[i] = ClosedCaptionCtrl.CARRIAGE_RETURN;
            j = -j;
            i = i2;
        } else if (j <= MAX_INT_AS_LONG) {
            return outputInt((int) j, bArr, i);
        }
        i3 = i + calcLongStrLength(j);
        i2 = i3;
        while (j > MAX_INT_AS_LONG) {
            i2 -= 3;
            long j2 = j / THOUSAND_L;
            outputFullTriplet((int) (j - (THOUSAND_L * j2)), bArr, i2);
            j = j2;
        }
        length = i2;
        i2 = (int) j;
        while (i2 >= com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL) {
            int i4 = length - 3;
            length = i2 / com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL;
            outputFullTriplet(i2 - (length * com.mobcrush.mobcrush.Constants.UPDATE_COFIG_INTERVAL), bArr, i4);
            i2 = length;
            length = i4;
        }
        outputLeadingTriplet(i2, bArr, i);
        return i3;
    }

    public static String toString(int i) {
        if (i < sSmallIntStrs.length) {
            if (i >= 0) {
                return sSmallIntStrs[i];
            }
            int i2 = (-i) - 1;
            if (i2 < sSmallIntStrs2.length) {
                return sSmallIntStrs2[i2];
            }
        }
        return Integer.toString(i);
    }

    public static String toString(long j) {
        if (j > 2147483647L || j < -2147483648L) {
            return Long.toString(j);
        }
        return toString((int) j);
    }

    public static String toString(double d) {
        return Double.toString(d);
    }

    private static int outputLeadingTriplet(int i, char[] cArr, int i2) {
        int i3 = i << 2;
        int i4 = i3 + 1;
        char c = LEADING_TRIPLETS[i3];
        if (c != '\u0000') {
            i3 = i2 + 1;
            cArr[i2] = c;
            i2 = i3;
        }
        int i5 = i4 + 1;
        char c2 = LEADING_TRIPLETS[i4];
        if (c2 != '\u0000') {
            i3 = i2 + 1;
            cArr[i2] = c2;
            i2 = i3;
        }
        i3 = i2 + 1;
        cArr[i2] = LEADING_TRIPLETS[i5];
        return i3;
    }

    private static int outputLeadingTriplet(int i, byte[] bArr, int i2) {
        int i3 = i << 2;
        int i4 = i3 + 1;
        char c = LEADING_TRIPLETS[i3];
        if (c != '\u0000') {
            i3 = i2 + 1;
            bArr[i2] = (byte) c;
            i2 = i3;
        }
        int i5 = i4 + 1;
        char c2 = LEADING_TRIPLETS[i4];
        if (c2 != '\u0000') {
            i3 = i2 + 1;
            bArr[i2] = (byte) c2;
            i2 = i3;
        }
        i3 = i2 + 1;
        bArr[i2] = (byte) LEADING_TRIPLETS[i5];
        return i3;
    }

    private static int outputFullTriplet(int i, char[] cArr, int i2) {
        int i3 = i << 2;
        int i4 = i2 + 1;
        int i5 = i3 + 1;
        cArr[i2] = FULL_TRIPLETS[i3];
        i3 = i4 + 1;
        int i6 = i5 + 1;
        cArr[i4] = FULL_TRIPLETS[i5];
        i4 = i3 + 1;
        cArr[i3] = FULL_TRIPLETS[i6];
        return i4;
    }

    private static int outputFullTriplet(int i, byte[] bArr, int i2) {
        int i3 = i << 2;
        int i4 = i2 + 1;
        int i5 = i3 + 1;
        bArr[i2] = FULL_TRIPLETS_B[i3];
        i3 = i4 + 1;
        int i6 = i5 + 1;
        bArr[i4] = FULL_TRIPLETS_B[i5];
        i4 = i3 + 1;
        bArr[i3] = FULL_TRIPLETS_B[i6];
        return i4;
    }

    private static int calcLongStrLength(long j) {
        int i = 10;
        for (long j2 = TEN_BILLION_L; j >= j2 && i != 19; j2 = (j2 << 1) + (j2 << 3)) {
            i++;
        }
        return i;
    }
}
