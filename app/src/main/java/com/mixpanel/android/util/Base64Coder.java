package com.mixpanel.android.util;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class Base64Coder {
    private static char[] map1 = new char[64];
    private static byte[] map2 = new byte[AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS];

    static {
        int i;
        char c = 'A';
        int i2 = 0;
        while (c <= 'Z') {
            i = i2 + 1;
            map1[i2] = c;
            c = (char) (c + 1);
            i2 = i;
        }
        c = 'a';
        while (c <= 'z') {
            i = i2 + 1;
            map1[i2] = c;
            c = (char) (c + 1);
            i2 = i;
        }
        c = '0';
        while (c <= '9') {
            i = i2 + 1;
            map1[i2] = c;
            c = (char) (c + 1);
            i2 = i;
        }
        i = i2 + 1;
        map1[i2] = '+';
        i2 = i + 1;
        map1[i] = '/';
        for (i = 0; i < map2.length; i++) {
            map2[i] = (byte) -1;
        }
        for (i = 0; i < 64; i++) {
            map2[map1[i]] = (byte) i;
        }
    }

    public static String encodeString(String s) {
        return new String(encode(s.getBytes()));
    }

    public static char[] encode(byte[] in) {
        return encode(in, in.length);
    }

    public static char[] encode(byte[] in, int iLen) {
        int oDataLen = ((iLen * 4) + 2) / 3;
        char[] out = new char[(((iLen + 2) / 3) * 4)];
        int op = 0;
        int ip = 0;
        while (ip < iLen) {
            int i1;
            int i2;
            int ip2 = ip + 1;
            int i0 = in[ip] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            if (ip2 < iLen) {
                ip = ip2 + 1;
                i1 = in[ip2] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            } else {
                i1 = 0;
                ip = ip2;
            }
            if (ip < iLen) {
                ip2 = ip + 1;
                i2 = in[ip] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            } else {
                i2 = 0;
                ip2 = ip;
            }
            int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
            int o2 = ((i1 & 15) << 2) | (i2 >>> 6);
            int o3 = i2 & 63;
            int i = op + 1;
            out[op] = map1[i0 >>> 2];
            op = i + 1;
            out[i] = map1[o1];
            out[op] = op < oDataLen ? map1[o2] : '=';
            i = op + 1;
            out[i] = i < oDataLen ? map1[o3] : '=';
            op = i + 1;
            ip = ip2;
        }
        return out;
    }

    public static String decodeString(String s) {
        return new String(decode(s));
    }

    public static byte[] decode(String s) {
        return decode(s.toCharArray());
    }

    public static byte[] decode(char[] in) {
        int iLen = in.length;
        if (iLen % 4 != 0) {
            throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
        }
        while (iLen > 0 && in[iLen - 1] == '=') {
            iLen--;
        }
        int oLen = (iLen * 3) / 4;
        byte[] out = new byte[oLen];
        int op = 0;
        int ip;
        for (int ip2 = 0; ip2 < iLen; ip2 = ip) {
            int i2;
            int i3;
            ip = ip2 + 1;
            int i0 = in[ip2];
            ip2 = ip + 1;
            int i1 = in[ip];
            if (ip2 < iLen) {
                ip = ip2 + 1;
                i2 = in[ip2];
                ip2 = ip;
            } else {
                i2 = 65;
            }
            if (ip2 < iLen) {
                ip = ip2 + 1;
                i3 = in[ip2];
            } else {
                i3 = 65;
                ip = ip2;
            }
            if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            int b0 = map2[i0];
            int b1 = map2[i1];
            int b2 = map2[i2];
            int b3 = map2[i3];
            if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) {
                throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
            }
            int o1 = ((b1 & 15) << 4) | (b2 >>> 2);
            int o2 = ((b2 & 3) << 6) | b3;
            int op2 = op + 1;
            out[op] = (byte) ((b0 << 2) | (b1 >>> 4));
            if (op2 < oLen) {
                op = op2 + 1;
                out[op2] = (byte) o1;
            } else {
                op = op2;
            }
            if (op < oLen) {
                op2 = op + 1;
                out[op] = (byte) o2;
            } else {
                op2 = op;
            }
            op = op2;
        }
        return out;
    }
}
