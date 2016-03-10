package com.fasterxml.jackson.core.io;

public final class NumberInput {
    static final long L_BILLION = 1000000000;
    static final String MAX_LONG_STR = String.valueOf(Long.MAX_VALUE);
    static final String MIN_LONG_STR_NO_SIGN = String.valueOf(Long.MIN_VALUE).substring(1);
    public static final String NASTY_SMALL_DOUBLE = "2.2250738585072012e-308";

    public static int parseInt(char[] cArr, int i, int i2) {
        int i3 = cArr[i] - 48;
        int i4 = i2 + i;
        int i5 = i + 1;
        if (i5 >= i4) {
            return i3;
        }
        i3 = (i3 * 10) + (cArr[i5] - 48);
        i5++;
        if (i5 >= i4) {
            return i3;
        }
        i3 = (i3 * 10) + (cArr[i5] - 48);
        i5++;
        if (i5 >= i4) {
            return i3;
        }
        i3 = (i3 * 10) + (cArr[i5] - 48);
        i5++;
        if (i5 >= i4) {
            return i3;
        }
        i3 = (i3 * 10) + (cArr[i5] - 48);
        i5++;
        if (i5 >= i4) {
            return i3;
        }
        i3 = (i3 * 10) + (cArr[i5] - 48);
        i5++;
        if (i5 >= i4) {
            return i3;
        }
        i3 = (i3 * 10) + (cArr[i5] - 48);
        i5++;
        if (i5 >= i4) {
            return i3;
        }
        i3 = (i3 * 10) + (cArr[i5] - 48);
        i5++;
        if (i5 < i4) {
            return (i3 * 10) + (cArr[i5] - 48);
        }
        return i3;
    }

    public static int parseInt(String str) {
        int i = 1;
        int charAt = str.charAt(0);
        int length = str.length();
        int i2 = charAt == 45 ? 1 : 0;
        if (i2 != 0) {
            if (length == 1 || length > 10) {
                return Integer.parseInt(str);
            }
            charAt = str.charAt(1);
            i = 2;
        } else if (length > 9) {
            return Integer.parseInt(str);
        }
        if (charAt > 57 || charAt < 48) {
            return Integer.parseInt(str);
        }
        charAt -= 48;
        if (i < length) {
            int i3 = i + 1;
            char charAt2 = str.charAt(i);
            if (charAt2 > '9' || charAt2 < '0') {
                return Integer.parseInt(str);
            }
            charAt = (charAt * 10) + (charAt2 - 48);
            if (i3 < length) {
                i = i3 + 1;
                char charAt3 = str.charAt(i3);
                if (charAt3 > '9' || charAt3 < '0') {
                    return Integer.parseInt(str);
                }
                charAt = (charAt * 10) + (charAt3 - 48);
                if (i < length) {
                    while (true) {
                        i3 = i + 1;
                        charAt2 = str.charAt(i);
                        if (charAt2 <= '9' && charAt2 >= '0') {
                            charAt = (charAt * 10) + (charAt2 - 48);
                            if (i3 >= length) {
                                break;
                            }
                            i = i3;
                        }
                    }
                    return Integer.parseInt(str);
                }
            }
        }
        return i2 != 0 ? -charAt : charAt;
    }

    public static long parseLong(char[] cArr, int i, int i2) {
        int i3 = i2 - 9;
        return ((long) parseInt(cArr, i3 + i, 9)) + (((long) parseInt(cArr, i, i3)) * L_BILLION);
    }

    public static long parseLong(String str) {
        if (str.length() <= 9) {
            return (long) parseInt(str);
        }
        return Long.parseLong(str);
    }

    public static boolean inLongRange(char[] cArr, int i, int i2, boolean z) {
        String str = z ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int length = str.length();
        if (i2 < length) {
            return true;
        }
        if (i2 > length) {
            return false;
        }
        for (int i3 = 0; i3 < length; i3++) {
            int charAt = cArr[i + i3] - str.charAt(i3);
            if (charAt != 0) {
                return charAt < 0;
            }
        }
        return true;
    }

    public static boolean inLongRange(String str, boolean z) {
        String str2 = z ? MIN_LONG_STR_NO_SIGN : MAX_LONG_STR;
        int length = str2.length();
        int length2 = str.length();
        if (length2 < length) {
            return true;
        }
        if (length2 > length) {
            return false;
        }
        for (length2 = 0; length2 < length; length2++) {
            int charAt = str.charAt(length2) - str2.charAt(length2);
            if (charAt != 0) {
                return charAt < 0;
            }
        }
        return true;
    }

    public static int parseAsInt(String str, int i) {
        int i2 = 0;
        if (str == null) {
            return i;
        }
        String trim = str.trim();
        int length = trim.length();
        if (length == 0) {
            return i;
        }
        String substring;
        int length2;
        char charAt;
        if (0 < length) {
            char charAt2 = trim.charAt(0);
            if (charAt2 == '+') {
                substring = trim.substring(1);
                length2 = substring.length();
            } else if (charAt2 == '-') {
                i2 = 1;
                length2 = length;
                substring = trim;
            }
            while (i2 < length2) {
                charAt = substring.charAt(i2);
                if (charAt <= '9' || charAt < '0') {
                    try {
                        return (int) parseDouble(substring);
                    } catch (NumberFormatException e) {
                        return i;
                    }
                }
                i2++;
            }
            return Integer.parseInt(substring);
        }
        length2 = length;
        substring = trim;
        while (i2 < length2) {
            charAt = substring.charAt(i2);
            if (charAt <= '9') {
            }
            return (int) parseDouble(substring);
        }
        try {
            return Integer.parseInt(substring);
        } catch (NumberFormatException e2) {
            return i;
        }
    }

    public static long parseAsLong(String str, long j) {
        int i = 0;
        if (str == null) {
            return j;
        }
        String trim = str.trim();
        int length = trim.length();
        if (length == 0) {
            return j;
        }
        String substring;
        int length2;
        char charAt;
        if (0 < length) {
            char charAt2 = trim.charAt(0);
            if (charAt2 == '+') {
                substring = trim.substring(1);
                length2 = substring.length();
            } else if (charAt2 == '-') {
                i = 1;
                length2 = length;
                substring = trim;
            }
            while (i < length2) {
                charAt = substring.charAt(i);
                if (charAt <= '9' || charAt < '0') {
                    try {
                        return (long) parseDouble(substring);
                    } catch (NumberFormatException e) {
                        return j;
                    }
                }
                i++;
            }
            return Long.parseLong(substring);
        }
        length2 = length;
        substring = trim;
        while (i < length2) {
            charAt = substring.charAt(i);
            if (charAt <= '9') {
            }
            return (long) parseDouble(substring);
        }
        try {
            return Long.parseLong(substring);
        } catch (NumberFormatException e2) {
            return j;
        }
    }

    public static double parseAsDouble(String str, double d) {
        if (str != null) {
            String trim = str.trim();
            if (trim.length() != 0) {
                try {
                    d = parseDouble(trim);
                } catch (NumberFormatException e) {
                }
            }
        }
        return d;
    }

    public static double parseDouble(String str) throws NumberFormatException {
        if (NASTY_SMALL_DOUBLE.equals(str)) {
            return Double.MIN_VALUE;
        }
        return Double.parseDouble(str);
    }
}
