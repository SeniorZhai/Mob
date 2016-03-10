package com.fasterxml.jackson.core.io;

import android.support.v4.internal.view.SupportMenu;
import com.facebook.internal.NativeProtocol;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;

public class UTF32Reader extends BaseReader {
    protected final boolean _bigEndian;
    protected int _byteCount = 0;
    protected int _charCount = 0;
    protected final boolean _managedBuffers;
    protected char _surrogate = '\u0000';

    public /* bridge */ /* synthetic */ void close() throws IOException {
        super.close();
    }

    public /* bridge */ /* synthetic */ int read() throws IOException {
        return super.read();
    }

    public UTF32Reader(IOContext iOContext, InputStream inputStream, byte[] bArr, int i, int i2, boolean z) {
        boolean z2 = false;
        super(iOContext, inputStream, bArr, i, i2);
        this._bigEndian = z;
        if (inputStream != null) {
            z2 = true;
        }
        this._managedBuffers = z2;
    }

    public int read(char[] cArr, int i, int i2) throws IOException {
        if (this._buffer == null) {
            return -1;
        }
        if (i2 < 1) {
            return i2;
        }
        int i3;
        int i4;
        if (i < 0 || i + i2 > cArr.length) {
            reportBounds(cArr, i, i2);
        }
        int i5 = i2 + i;
        if (this._surrogate != '\u0000') {
            i3 = i + 1;
            cArr[i] = this._surrogate;
            this._surrogate = '\u0000';
        } else {
            i4 = this._length - this._ptr;
            if (i4 < 4 && !loadMore(i4)) {
                return -1;
            }
            i3 = i;
        }
        while (i3 < i5) {
            int i6 = this._ptr;
            if (this._bigEndian) {
                i6 = (this._buffer[i6 + 3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) | (((this._buffer[i6] << 24) | ((this._buffer[i6 + 1] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 16)) | ((this._buffer[i6 + 2] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8));
            } else {
                i6 = (this._buffer[i6 + 3] << 24) | (((this._buffer[i6] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) | ((this._buffer[i6 + 1] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8)) | ((this._buffer[i6 + 2] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 16));
            }
            this._ptr += 4;
            if (i6 > SupportMenu.USER_MASK) {
                if (i6 > 1114111) {
                    reportInvalid(i6, i3 - i, "(above " + Integer.toHexString(1114111) + ") ");
                }
                i6 -= NativeProtocol.MESSAGE_GET_ACCESS_TOKEN_REQUEST;
                i4 = i3 + 1;
                cArr[i3] = (char) (55296 + (i6 >> 10));
                i6 = (i6 & 1023) | 56320;
                if (i4 >= i5) {
                    this._surrogate = (char) i6;
                    break;
                }
            }
            i4 = i3;
            i3 = i4 + 1;
            cArr[i4] = (char) i6;
            if (this._ptr >= this._length) {
                i4 = i3;
                break;
            }
        }
        i4 = i3;
        i2 = i4 - i;
        this._charCount += i2;
        return i2;
    }

    private void reportUnexpectedEOF(int i, int i2) throws IOException {
        throw new CharConversionException("Unexpected EOF in the middle of a 4-byte UTF-32 char: got " + i + ", needed " + i2 + ", at char #" + this._charCount + ", byte #" + (this._byteCount + i) + ")");
    }

    private void reportInvalid(int i, int i2, String str) throws IOException {
        throw new CharConversionException("Invalid UTF-32 character 0x" + Integer.toHexString(i) + str + " at char #" + (this._charCount + i2) + ", byte #" + ((this._byteCount + this._ptr) - 1) + ")");
    }

    private boolean loadMore(int i) throws IOException {
        this._byteCount += this._length - i;
        int i2;
        if (i > 0) {
            if (this._ptr > 0) {
                for (i2 = 0; i2 < i; i2++) {
                    this._buffer[i2] = this._buffer[this._ptr + i2];
                }
                this._ptr = 0;
            }
            this._length = i;
        } else {
            this._ptr = 0;
            i2 = this._in == null ? -1 : this._in.read(this._buffer);
            if (i2 < 1) {
                this._length = 0;
                if (i2 >= 0) {
                    reportStrangeStream();
                } else if (!this._managedBuffers) {
                    return false;
                } else {
                    freeBuffers();
                    return false;
                }
            }
            this._length = i2;
        }
        while (this._length < 4) {
            int i3;
            if (this._in == null) {
                i3 = -1;
            } else {
                i3 = this._in.read(this._buffer, this._length, this._buffer.length - this._length);
            }
            if (i3 < 1) {
                if (i3 < 0) {
                    if (this._managedBuffers) {
                        freeBuffers();
                    }
                    reportUnexpectedEOF(this._length, 4);
                }
                reportStrangeStream();
            }
            this._length = i3 + this._length;
        }
        return true;
    }
}
