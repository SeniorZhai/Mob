package com.firebase.tubesock;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.mixpanel.android.java_websocket.drafts.Draft_75;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.Arrays;
import org.apache.http.message.TokenParser;

public class Base64 {
    private static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] IA = new int[AccessibilityNodeInfoCompat.ACTION_NEXT_AT_MOVEMENT_GRANULARITY];

    static {
        Arrays.fill(IA, -1);
        int iS = CA.length;
        for (int i = 0; i < iS; i++) {
            IA[CA[i]] = i;
        }
        IA[61] = 0;
    }

    public static final char[] encodeToChar(byte[] sArr, boolean lineSep) {
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new char[0];
        }
        int eLen = (sLen / 3) * 3;
        int cCnt = (((sLen - 1) / 3) + 1) << 2;
        int dLen = cCnt + (lineSep ? ((cCnt - 1) / 76) << 1 : 0);
        char[] dArr = new char[dLen];
        int cc = 0;
        int d = 0;
        int s = 0;
        while (s < eLen) {
            int s2 = s + 1;
            s = s2 + 1;
            s2 = s + 1;
            int i = (((sArr[s] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 16) | ((sArr[s2] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8)) | (sArr[s] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
            int i2 = d + 1;
            dArr[d] = CA[(i >>> 18) & 63];
            d = i2 + 1;
            dArr[i2] = CA[(i >>> 12) & 63];
            i2 = d + 1;
            dArr[d] = CA[(i >>> 6) & 63];
            d = i2 + 1;
            dArr[i2] = CA[i & 63];
            if (lineSep) {
                cc++;
                if (cc == 19 && d < dLen - 2) {
                    i2 = d + 1;
                    dArr[d] = TokenParser.CR;
                    d = i2 + 1;
                    dArr[i2] = '\n';
                    cc = 0;
                    i2 = d;
                    d = i2;
                    s = s2;
                }
            }
            i2 = d;
            d = i2;
            s = s2;
        }
        int left = sLen - eLen;
        if (left <= 0) {
            return dArr;
        }
        i = ((sArr[eLen] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 10) | (left == 2 ? (sArr[sLen - 1] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 2 : 0);
        dArr[dLen - 4] = CA[i >> 12];
        dArr[dLen - 3] = CA[(i >>> 6) & 63];
        dArr[dLen - 2] = left == 2 ? CA[i & 63] : '=';
        dArr[dLen - 1] = '=';
        return dArr;
    }

    public static final byte[] decode(char[] sArr) {
        int sLen;
        if (sArr != null) {
            sLen = sArr.length;
        } else {
            sLen = 0;
        }
        if (sLen == 0) {
            return new byte[0];
        }
        int i;
        int sepCnt = 0;
        for (i = 0; i < sLen; i++) {
            if (IA[sArr[i]] < 0) {
                sepCnt++;
            }
        }
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }
        int pad = 0;
        i = sLen;
        while (i > 1) {
            i--;
            if (IA[sArr[i]] > 0) {
                break;
            } else if (sArr[i] == '=') {
                pad++;
            }
        }
        int len = (((sLen - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int s = 0;
        int i2 = 0;
        while (i2 < len) {
            i = 0;
            int j = 0;
            int s2 = s;
            while (j < 4) {
                s = s2 + 1;
                int c = IA[sArr[s2]];
                if (c >= 0) {
                    i |= c << (18 - (j * 6));
                } else {
                    j--;
                }
                j++;
                s2 = s;
            }
            int d = i2 + 1;
            dArr[i2] = (byte) (i >> 16);
            if (d < len) {
                i2 = d + 1;
                dArr[d] = (byte) (i >> 8);
                if (i2 < len) {
                    d = i2 + 1;
                    dArr[i2] = (byte) i;
                } else {
                    d = i2;
                }
            }
            i2 = d;
            s = s2;
        }
        return dArr;
    }

    public static final byte[] decodeFast(char[] sArr) {
        int sLen = sArr.length;
        if (sLen == 0) {
            return new byte[0];
        }
        int sepCnt;
        int i;
        int sIx = 0;
        int eIx = sLen - 1;
        while (sIx < eIx && IA[sArr[sIx]] < 0) {
            sIx++;
        }
        while (eIx > 0 && IA[sArr[eIx]] < 0) {
            eIx--;
        }
        int pad = sArr[eIx] == '=' ? sArr[eIx + -1] == '=' ? 2 : 1 : 0;
        int cCnt = (eIx - sIx) + 1;
        if (sLen > 76) {
            sepCnt = (sArr[76] == '\r' ? cCnt / 78 : 0) << 1;
        } else {
            sepCnt = 0;
        }
        int len = (((cCnt - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int cc = 0;
        int eLen = (len / 3) * 3;
        int d = 0;
        int sIx2 = sIx;
        while (d < eLen) {
            sIx = sIx2 + 1;
            sIx2 = sIx + 1;
            sIx = sIx2 + 1;
            sIx2 = sIx + 1;
            int i2 = (((IA[sArr[sIx2]] << 18) | (IA[sArr[sIx]] << 12)) | (IA[sArr[sIx2]] << 6)) | IA[sArr[sIx]];
            i = d + 1;
            dArr[d] = (byte) (i2 >> 16);
            d = i + 1;
            dArr[i] = (byte) (i2 >> 8);
            i = d + 1;
            dArr[d] = (byte) i2;
            if (sepCnt > 0) {
                cc++;
                if (cc == 19) {
                    sIx = sIx2 + 2;
                    cc = 0;
                    d = i;
                    sIx2 = sIx;
                }
            }
            sIx = sIx2;
            d = i;
            sIx2 = sIx;
        }
        if (d < len) {
            i2 = 0;
            int j = 0;
            while (sIx2 <= eIx - pad) {
                i2 |= IA[sArr[sIx2]] << (18 - (j * 6));
                j++;
                sIx2++;
            }
            int r = 16;
            while (d < len) {
                i = d + 1;
                dArr[d] = (byte) (i2 >> r);
                r -= 8;
                d = i;
            }
        }
        i = d;
        sIx = sIx2;
        return dArr;
    }

    public static final byte[] encodeToByte(byte[] sArr, boolean lineSep) {
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new byte[0];
        }
        int eLen = (sLen / 3) * 3;
        int cCnt = (((sLen - 1) / 3) + 1) << 2;
        int dLen = cCnt + (lineSep ? ((cCnt - 1) / 76) << 1 : 0);
        byte[] dArr = new byte[dLen];
        int cc = 0;
        int d = 0;
        int s = 0;
        while (s < eLen) {
            int s2 = s + 1;
            s = s2 + 1;
            s2 = s + 1;
            int i = (((sArr[s] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 16) | ((sArr[s2] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8)) | (sArr[s] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
            int i2 = d + 1;
            dArr[d] = (byte) CA[(i >>> 18) & 63];
            d = i2 + 1;
            dArr[i2] = (byte) CA[(i >>> 12) & 63];
            i2 = d + 1;
            dArr[d] = (byte) CA[(i >>> 6) & 63];
            d = i2 + 1;
            dArr[i2] = (byte) CA[i & 63];
            if (lineSep) {
                cc++;
                if (cc == 19 && d < dLen - 2) {
                    i2 = d + 1;
                    dArr[d] = Draft_75.CR;
                    d = i2 + 1;
                    dArr[i2] = (byte) 10;
                    cc = 0;
                    i2 = d;
                    d = i2;
                    s = s2;
                }
            }
            i2 = d;
            d = i2;
            s = s2;
        }
        int left = sLen - eLen;
        if (left <= 0) {
            return dArr;
        }
        i = ((sArr[eLen] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 10) | (left == 2 ? (sArr[sLen - 1] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 2 : 0);
        dArr[dLen - 4] = (byte) CA[i >> 12];
        dArr[dLen - 3] = (byte) CA[(i >>> 6) & 63];
        dArr[dLen - 2] = left == 2 ? (byte) CA[i & 63] : (byte) 61;
        dArr[dLen - 1] = (byte) 61;
        return dArr;
    }

    public static final byte[] decode(byte[] sArr) {
        int i;
        int sepCnt = 0;
        for (byte b : sArr) {
            if (IA[b & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT] < 0) {
                sepCnt++;
            }
        }
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }
        int pad = 0;
        i = sLen;
        while (i > 1) {
            i--;
            if (IA[sArr[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT] > 0) {
                break;
            } else if (sArr[i] == (byte) 61) {
                pad++;
            }
        }
        int len = (((sLen - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int s = 0;
        int i2 = 0;
        while (i2 < len) {
            i = 0;
            int j = 0;
            int s2 = s;
            while (j < 4) {
                s = s2 + 1;
                int c = IA[sArr[s2] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT];
                if (c >= 0) {
                    i |= c << (18 - (j * 6));
                } else {
                    j--;
                }
                j++;
                s2 = s;
            }
            int d = i2 + 1;
            dArr[i2] = (byte) (i >> 16);
            if (d < len) {
                i2 = d + 1;
                dArr[d] = (byte) (i >> 8);
                if (i2 < len) {
                    d = i2 + 1;
                    dArr[i2] = (byte) i;
                } else {
                    d = i2;
                }
            }
            i2 = d;
            s = s2;
        }
        return dArr;
    }

    public static final byte[] decodeFast(byte[] sArr) {
        int sLen = sArr.length;
        if (sLen == 0) {
            return new byte[0];
        }
        int sepCnt;
        int i;
        int sIx = 0;
        int eIx = sLen - 1;
        while (sIx < eIx && IA[sArr[sIx] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT] < 0) {
            sIx++;
        }
        while (eIx > 0 && IA[sArr[eIx] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT] < 0) {
            eIx--;
        }
        int pad = sArr[eIx] == (byte) 61 ? sArr[eIx + -1] == (byte) 61 ? 2 : 1 : 0;
        int cCnt = (eIx - sIx) + 1;
        if (sLen > 76) {
            sepCnt = (sArr[76] == (byte) 13 ? cCnt / 78 : 0) << 1;
        } else {
            sepCnt = 0;
        }
        int len = (((cCnt - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int cc = 0;
        int eLen = (len / 3) * 3;
        int d = 0;
        int sIx2 = sIx;
        while (d < eLen) {
            sIx = sIx2 + 1;
            sIx2 = sIx + 1;
            sIx = sIx2 + 1;
            sIx2 = sIx + 1;
            int i2 = (((IA[sArr[sIx2]] << 18) | (IA[sArr[sIx]] << 12)) | (IA[sArr[sIx2]] << 6)) | IA[sArr[sIx]];
            i = d + 1;
            dArr[d] = (byte) (i2 >> 16);
            d = i + 1;
            dArr[i] = (byte) (i2 >> 8);
            i = d + 1;
            dArr[d] = (byte) i2;
            if (sepCnt > 0) {
                cc++;
                if (cc == 19) {
                    sIx = sIx2 + 2;
                    cc = 0;
                    d = i;
                    sIx2 = sIx;
                }
            }
            sIx = sIx2;
            d = i;
            sIx2 = sIx;
        }
        if (d < len) {
            i2 = 0;
            int j = 0;
            while (sIx2 <= eIx - pad) {
                i2 |= IA[sArr[sIx2]] << (18 - (j * 6));
                j++;
                sIx2++;
            }
            int r = 16;
            while (d < len) {
                i = d + 1;
                dArr[d] = (byte) (i2 >> r);
                r -= 8;
                d = i;
            }
        }
        i = d;
        sIx = sIx2;
        return dArr;
    }

    public static final String encodeToString(byte[] sArr, boolean lineSep) {
        return new String(encodeToChar(sArr, lineSep));
    }

    public static final byte[] decode(String str) {
        int sLen;
        if (str != null) {
            sLen = str.length();
        } else {
            sLen = 0;
        }
        if (sLen == 0) {
            return new byte[0];
        }
        int i;
        int sepCnt = 0;
        for (i = 0; i < sLen; i++) {
            if (IA[str.charAt(i)] < 0) {
                sepCnt++;
            }
        }
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }
        int pad = 0;
        i = sLen;
        while (i > 1) {
            i--;
            if (IA[str.charAt(i)] > 0) {
                break;
            } else if (str.charAt(i) == '=') {
                pad++;
            }
        }
        int len = (((sLen - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int s = 0;
        int i2 = 0;
        while (i2 < len) {
            i = 0;
            int j = 0;
            int s2 = s;
            while (j < 4) {
                s = s2 + 1;
                int c = IA[str.charAt(s2)];
                if (c >= 0) {
                    i |= c << (18 - (j * 6));
                } else {
                    j--;
                }
                j++;
                s2 = s;
            }
            int d = i2 + 1;
            dArr[i2] = (byte) (i >> 16);
            if (d < len) {
                i2 = d + 1;
                dArr[d] = (byte) (i >> 8);
                if (i2 < len) {
                    d = i2 + 1;
                    dArr[i2] = (byte) i;
                } else {
                    d = i2;
                }
            }
            i2 = d;
            s = s2;
        }
        return dArr;
    }

    public static final byte[] decodeFast(String s) {
        int sLen = s.length();
        if (sLen == 0) {
            return new byte[0];
        }
        int sepCnt;
        int i;
        int sIx = 0;
        int eIx = sLen - 1;
        while (sIx < eIx && IA[s.charAt(sIx) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT] < 0) {
            sIx++;
        }
        while (eIx > 0 && IA[s.charAt(eIx) & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT] < 0) {
            eIx--;
        }
        int pad = s.charAt(eIx) == '=' ? s.charAt(eIx + -1) == '=' ? 2 : 1 : 0;
        int cCnt = (eIx - sIx) + 1;
        if (sLen > 76) {
            sepCnt = (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1;
        } else {
            sepCnt = 0;
        }
        int len = (((cCnt - sepCnt) * 6) >> 3) - pad;
        byte[] dArr = new byte[len];
        int cc = 0;
        int eLen = (len / 3) * 3;
        int d = 0;
        int sIx2 = sIx;
        while (d < eLen) {
            sIx = sIx2 + 1;
            sIx2 = sIx + 1;
            sIx = sIx2 + 1;
            sIx2 = sIx + 1;
            int i2 = (((IA[s.charAt(sIx2)] << 18) | (IA[s.charAt(sIx)] << 12)) | (IA[s.charAt(sIx2)] << 6)) | IA[s.charAt(sIx)];
            i = d + 1;
            dArr[d] = (byte) (i2 >> 16);
            d = i + 1;
            dArr[i] = (byte) (i2 >> 8);
            i = d + 1;
            dArr[d] = (byte) i2;
            if (sepCnt > 0) {
                cc++;
                if (cc == 19) {
                    sIx = sIx2 + 2;
                    cc = 0;
                    d = i;
                    sIx2 = sIx;
                }
            }
            sIx = sIx2;
            d = i;
            sIx2 = sIx;
        }
        if (d < len) {
            i2 = 0;
            int j = 0;
            while (sIx2 <= eIx - pad) {
                i2 |= IA[s.charAt(sIx2)] << (18 - (j * 6));
                j++;
                sIx2++;
            }
            int r = 16;
            while (d < len) {
                i = d + 1;
                dArr[d] = (byte) (i2 >> r);
                r -= 8;
                d = i;
            }
        }
        i = d;
        sIx = sIx2;
        return dArr;
    }
}
