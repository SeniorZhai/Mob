package com.fasterxml.jackson.core.io;

import android.support.v4.media.TransportMediator;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.facebook.internal.NativeProtocol;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.TextBuffer;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.lang.ref.SoftReference;
import org.apache.http.message.TokenParser;

public final class JsonStringEncoder {
    private static final byte[] HEX_BYTES = CharTypes.copyHexBytes();
    private static final char[] HEX_CHARS = CharTypes.copyHexChars();
    private static final int INT_0 = 48;
    private static final int INT_BACKSLASH = 92;
    private static final int INT_U = 117;
    private static final int SURR1_FIRST = 55296;
    private static final int SURR1_LAST = 56319;
    private static final int SURR2_FIRST = 56320;
    private static final int SURR2_LAST = 57343;
    protected static final ThreadLocal<SoftReference<JsonStringEncoder>> _threadEncoder = new ThreadLocal();
    protected ByteArrayBuilder _byteBuilder;
    protected final char[] _quoteBuffer = new char[6];
    protected TextBuffer _textBuffer;

    public JsonStringEncoder() {
        this._quoteBuffer[0] = TokenParser.ESCAPE;
        this._quoteBuffer[2] = '0';
        this._quoteBuffer[3] = '0';
    }

    public static JsonStringEncoder getInstance() {
        SoftReference softReference = (SoftReference) _threadEncoder.get();
        JsonStringEncoder jsonStringEncoder = softReference == null ? null : (JsonStringEncoder) softReference.get();
        if (jsonStringEncoder != null) {
            return jsonStringEncoder;
        }
        jsonStringEncoder = new JsonStringEncoder();
        _threadEncoder.set(new SoftReference(jsonStringEncoder));
        return jsonStringEncoder;
    }

    public char[] quoteAsString(String str) {
        TextBuffer textBuffer = this._textBuffer;
        if (textBuffer == null) {
            textBuffer = new TextBuffer(null);
            this._textBuffer = textBuffer;
        }
        Object emptyAndGetCurrentSegment = textBuffer.emptyAndGetCurrentSegment();
        int[] iArr = CharTypes.get7BitOutputEscapes();
        char length = iArr.length;
        int length2 = str.length();
        int i = 0;
        int i2 = 0;
        loop0:
        while (i2 < length2) {
            int i3;
            while (true) {
                char charAt = str.charAt(i2);
                if (charAt < length && iArr[charAt] != 0) {
                    break;
                }
                if (i >= emptyAndGetCurrentSegment.length) {
                    emptyAndGetCurrentSegment = textBuffer.finishCurrentSegment();
                    i3 = 0;
                } else {
                    i3 = i;
                }
                i = i3 + 1;
                emptyAndGetCurrentSegment[i3] = charAt;
                i2++;
                if (i2 >= length2) {
                    break loop0;
                }
            }
            i3 = i2 + 1;
            char charAt2 = str.charAt(i2);
            int i4 = iArr[charAt2];
            if (i4 < 0) {
                i2 = _appendNumericEscape(charAt2, this._quoteBuffer);
            } else {
                i2 = _appendNamedEscape(i4, this._quoteBuffer);
            }
            if (i + i2 > emptyAndGetCurrentSegment.length) {
                i4 = emptyAndGetCurrentSegment.length - i;
                if (i4 > 0) {
                    System.arraycopy(this._quoteBuffer, 0, emptyAndGetCurrentSegment, i, i4);
                }
                emptyAndGetCurrentSegment = textBuffer.finishCurrentSegment();
                i = i2 - i4;
                System.arraycopy(this._quoteBuffer, i4, emptyAndGetCurrentSegment, 0, i);
            } else {
                System.arraycopy(this._quoteBuffer, 0, emptyAndGetCurrentSegment, i, i2);
                i += i2;
            }
            i2 = i3;
        }
        textBuffer.setCurrentLength(i);
        return textBuffer.contentsAsArray();
    }

    public byte[] quoteAsUTF8(String str) {
        ByteArrayBuilder byteArrayBuilder = this._byteBuilder;
        if (byteArrayBuilder == null) {
            byteArrayBuilder = new ByteArrayBuilder(null);
            this._byteBuilder = byteArrayBuilder;
        }
        int length = str.length();
        byte[] resetAndGetFirstSegment = byteArrayBuilder.resetAndGetFirstSegment();
        int i = 0;
        int i2 = 0;
        loop0:
        while (i2 < length) {
            char charAt;
            int i3;
            int[] iArr = CharTypes.get7BitOutputEscapes();
            while (true) {
                charAt = str.charAt(i2);
                if (charAt <= '\u007f' && iArr[charAt] == 0) {
                    if (i >= resetAndGetFirstSegment.length) {
                        resetAndGetFirstSegment = byteArrayBuilder.finishCurrentSegment();
                        i3 = 0;
                    } else {
                        i3 = i;
                    }
                    i = i3 + 1;
                    resetAndGetFirstSegment[i3] = (byte) charAt;
                    i2++;
                    if (i2 >= length) {
                        break loop0;
                    }
                }
            }
            if (i >= resetAndGetFirstSegment.length) {
                resetAndGetFirstSegment = byteArrayBuilder.finishCurrentSegment();
                i = 0;
            }
            i3 = i2 + 1;
            charAt = str.charAt(i2);
            if (charAt <= '\u007f') {
                i = _appendByteEscape(charAt, iArr[charAt], byteArrayBuilder, i);
                resetAndGetFirstSegment = byteArrayBuilder.getCurrentSegment();
                i2 = i3;
            } else {
                byte[] bArr;
                int i4;
                int i5;
                if (charAt <= '\u07ff') {
                    i2 = i + 1;
                    resetAndGetFirstSegment[i] = (byte) ((charAt >> 6) | 192);
                    bArr = resetAndGetFirstSegment;
                    i4 = (charAt & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS;
                } else if (charAt < '\ud800' || charAt > '\udfff') {
                    i2 = i + 1;
                    resetAndGetFirstSegment[i] = (byte) ((charAt >> 12) | 224);
                    if (i2 >= resetAndGetFirstSegment.length) {
                        resetAndGetFirstSegment = byteArrayBuilder.finishCurrentSegment();
                        i = 0;
                    } else {
                        i = i2;
                    }
                    i2 = i + 1;
                    resetAndGetFirstSegment[i] = (byte) (((charAt >> 6) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                    bArr = resetAndGetFirstSegment;
                    i4 = (charAt & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS;
                } else {
                    if (charAt > '\udbff') {
                        _illegalSurrogate(charAt);
                    }
                    if (i3 >= length) {
                        _illegalSurrogate(charAt);
                    }
                    i5 = i3 + 1;
                    i3 = _convertSurrogate(charAt, str.charAt(i3));
                    if (i3 > 1114111) {
                        _illegalSurrogate(i3);
                    }
                    i2 = i + 1;
                    resetAndGetFirstSegment[i] = (byte) ((i3 >> 18) | 240);
                    if (i2 >= resetAndGetFirstSegment.length) {
                        resetAndGetFirstSegment = byteArrayBuilder.finishCurrentSegment();
                        i = 0;
                    } else {
                        i = i2;
                    }
                    i2 = i + 1;
                    resetAndGetFirstSegment[i] = (byte) (((i3 >> 12) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                    if (i2 >= resetAndGetFirstSegment.length) {
                        resetAndGetFirstSegment = byteArrayBuilder.finishCurrentSegment();
                        i = 0;
                    } else {
                        i = i2;
                    }
                    i2 = i + 1;
                    resetAndGetFirstSegment[i] = (byte) (((i3 >> 6) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                    i3 = i5;
                    byte[] bArr2 = resetAndGetFirstSegment;
                    i4 = (i3 & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS;
                    bArr = bArr2;
                }
                if (i2 >= bArr.length) {
                    bArr = byteArrayBuilder.finishCurrentSegment();
                    i2 = 0;
                }
                i5 = i2 + 1;
                bArr[i2] = (byte) i4;
                resetAndGetFirstSegment = bArr;
                i2 = i3;
                i = i5;
            }
        }
        return this._byteBuilder.completeAndCoalesce(i);
    }

    public byte[] encodeAsUTF8(String str) {
        int i;
        ByteArrayBuilder byteArrayBuilder = this._byteBuilder;
        if (byteArrayBuilder == null) {
            byteArrayBuilder = new ByteArrayBuilder(null);
            this._byteBuilder = byteArrayBuilder;
        }
        int length = str.length();
        byte[] resetAndGetFirstSegment = byteArrayBuilder.resetAndGetFirstSegment();
        int length2 = resetAndGetFirstSegment.length;
        int i2 = 0;
        int i3 = 0;
        loop0:
        while (i3 < length) {
            int i4;
            int i5 = i3 + 1;
            i3 = str.charAt(i3);
            int i6 = length2;
            byte[] bArr = resetAndGetFirstSegment;
            int i7 = i2;
            i2 = i6;
            while (i3 <= TransportMediator.KEYCODE_MEDIA_PAUSE) {
                if (i7 >= i2) {
                    bArr = byteArrayBuilder.finishCurrentSegment();
                    i2 = bArr.length;
                    i7 = 0;
                }
                i4 = i7 + 1;
                bArr[i7] = (byte) i3;
                if (i5 >= length) {
                    i = i4;
                    break loop0;
                }
                i7 = i5 + 1;
                i3 = str.charAt(i5);
                i5 = i7;
                i7 = i4;
            }
            if (i7 >= i2) {
                bArr = byteArrayBuilder.finishCurrentSegment();
                i2 = bArr.length;
                i4 = 0;
            } else {
                i4 = i7;
            }
            if (i3 < AccessibilityNodeInfoCompat.ACTION_PREVIOUS_HTML_ELEMENT) {
                i7 = i4 + 1;
                bArr[i4] = (byte) ((i3 >> 6) | 192);
                i4 = i3;
                i3 = i5;
            } else if (i3 < SURR1_FIRST || i3 > SURR2_LAST) {
                i7 = i4 + 1;
                bArr[i4] = (byte) ((i3 >> 12) | 224);
                if (i7 >= i2) {
                    bArr = byteArrayBuilder.finishCurrentSegment();
                    i2 = bArr.length;
                    i7 = 0;
                }
                i4 = i7 + 1;
                bArr[i7] = (byte) (((i3 >> 6) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                i7 = i4;
                i4 = i3;
                i3 = i5;
            } else {
                if (i3 > SURR1_LAST) {
                    _illegalSurrogate(i3);
                }
                if (i5 >= length) {
                    _illegalSurrogate(i3);
                }
                int i8 = i5 + 1;
                i3 = _convertSurrogate(i3, str.charAt(i5));
                if (i3 > 1114111) {
                    _illegalSurrogate(i3);
                }
                i7 = i4 + 1;
                bArr[i4] = (byte) ((i3 >> 18) | 240);
                if (i7 >= i2) {
                    bArr = byteArrayBuilder.finishCurrentSegment();
                    i2 = bArr.length;
                    i7 = 0;
                }
                i4 = i7 + 1;
                bArr[i7] = (byte) (((i3 >> 12) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                if (i4 >= i2) {
                    bArr = byteArrayBuilder.finishCurrentSegment();
                    i2 = bArr.length;
                    i7 = 0;
                } else {
                    i7 = i4;
                }
                i4 = i7 + 1;
                bArr[i7] = (byte) (((i3 >> 6) & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
                i7 = i4;
                i4 = i3;
                i3 = i8;
            }
            if (i7 >= i2) {
                bArr = byteArrayBuilder.finishCurrentSegment();
                i2 = bArr.length;
                i7 = 0;
            }
            i5 = i7 + 1;
            bArr[i7] = (byte) ((i4 & 63) | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
            resetAndGetFirstSegment = bArr;
            length2 = i2;
            i2 = i5;
        }
        i = i2;
        return this._byteBuilder.completeAndCoalesce(i);
    }

    private int _appendNumericEscape(int i, char[] cArr) {
        cArr[1] = 'u';
        cArr[4] = HEX_CHARS[i >> 4];
        cArr[5] = HEX_CHARS[i & 15];
        return 6;
    }

    private int _appendNamedEscape(int i, char[] cArr) {
        cArr[1] = (char) i;
        return 2;
    }

    private int _appendByteEscape(int i, int i2, ByteArrayBuilder byteArrayBuilder, int i3) {
        byteArrayBuilder.setCurrentSegmentLength(i3);
        byteArrayBuilder.append(INT_BACKSLASH);
        if (i2 < 0) {
            byteArrayBuilder.append(INT_U);
            if (i > SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
                int i4 = i >> 8;
                byteArrayBuilder.append(HEX_BYTES[i4 >> 4]);
                byteArrayBuilder.append(HEX_BYTES[i4 & 15]);
                i &= SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            } else {
                byteArrayBuilder.append(INT_0);
                byteArrayBuilder.append(INT_0);
            }
            byteArrayBuilder.append(HEX_BYTES[i >> 4]);
            byteArrayBuilder.append(HEX_BYTES[i & 15]);
        } else {
            byteArrayBuilder.append((byte) i2);
        }
        return byteArrayBuilder.getCurrentSegmentLength();
    }

    protected static int _convertSurrogate(int i, int i2) {
        if (i2 >= SURR2_FIRST && i2 <= SURR2_LAST) {
            return (NativeProtocol.MESSAGE_GET_ACCESS_TOKEN_REQUEST + ((i - SURR1_FIRST) << 10)) + (i2 - SURR2_FIRST);
        }
        throw new IllegalArgumentException("Broken surrogate pair: first char 0x" + Integer.toHexString(i) + ", second 0x" + Integer.toHexString(i2) + "; illegal combination");
    }

    protected static void _illegalSurrogate(int i) {
        throw new IllegalArgumentException(UTF8Writer.illegalSurrogateDesc(i));
    }
}
