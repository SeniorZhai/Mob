package com.google.android.exoplayer.util;

import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public final class ParsableBitArray {
    private int bitOffset;
    private int byteLimit;
    private int byteOffset;
    public byte[] data;

    public ParsableBitArray(byte[] data) {
        this(data, data.length);
    }

    public ParsableBitArray(byte[] data, int limit) {
        this.data = data;
        this.byteLimit = limit;
    }

    public void reset(byte[] data) {
        reset(data, data.length);
    }

    public void reset(byte[] data, int limit) {
        this.data = data;
        this.byteOffset = 0;
        this.bitOffset = 0;
        this.byteLimit = limit;
    }

    public int bitsLeft() {
        return ((this.byteLimit - this.byteOffset) * 8) - this.bitOffset;
    }

    public int getPosition() {
        return (this.byteOffset * 8) + this.bitOffset;
    }

    public void setPosition(int position) {
        this.byteOffset = position / 8;
        this.bitOffset = position - (this.byteOffset * 8);
        assertValidOffset();
    }

    public void skipBits(int n) {
        this.byteOffset += n / 8;
        this.bitOffset += n % 8;
        if (this.bitOffset > 7) {
            this.byteOffset++;
            this.bitOffset -= 8;
        }
        assertValidOffset();
    }

    public boolean readBit() {
        return readBits(1) == 1;
    }

    public int readBits(int n) {
        if (n == 0) {
            return 0;
        }
        int returnValue = 0;
        while (n >= 8) {
            int byteValue;
            if (this.bitOffset != 0) {
                byteValue = ((this.data[this.byteOffset] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << this.bitOffset) | ((this.data[this.byteOffset + 1] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) >>> (8 - this.bitOffset));
            } else {
                byteValue = this.data[this.byteOffset];
            }
            n -= 8;
            returnValue |= (byteValue & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << n;
            this.byteOffset++;
        }
        if (n > 0) {
            int nextBit = this.bitOffset + n;
            byte writeMask = (byte) (SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT >> (8 - n));
            if (nextBit > 8) {
                returnValue |= (((this.data[this.byteOffset] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << (nextBit - 8)) | ((this.data[this.byteOffset + 1] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) >> (16 - nextBit))) & writeMask;
                this.byteOffset++;
            } else {
                returnValue |= ((this.data[this.byteOffset] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) >> (8 - nextBit)) & writeMask;
                if (nextBit == 8) {
                    this.byteOffset++;
                }
            }
            this.bitOffset = nextBit % 8;
        }
        assertValidOffset();
        return returnValue;
    }

    public int peekExpGolombCodedNumLength() {
        int initialByteOffset = this.byteOffset;
        int initialBitOffset = this.bitOffset;
        int leadingZeros = 0;
        while (this.byteOffset < this.byteLimit && !readBit()) {
            leadingZeros++;
        }
        boolean hitLimit = this.byteOffset == this.byteLimit;
        this.byteOffset = initialByteOffset;
        this.bitOffset = initialBitOffset;
        return hitLimit ? -1 : (leadingZeros * 2) + 1;
    }

    public int readUnsignedExpGolombCodedInt() {
        return readExpGolombCodeNum();
    }

    public int readSignedExpGolombCodedInt() {
        int codeNum = readExpGolombCodeNum();
        return (codeNum % 2 == 0 ? -1 : 1) * ((codeNum + 1) / 2);
    }

    private int readExpGolombCodeNum() {
        int leadingZeros = 0;
        while (!readBit()) {
            leadingZeros++;
        }
        return (leadingZeros > 0 ? readBits(leadingZeros) : 0) + ((1 << leadingZeros) - 1);
    }

    private void assertValidOffset() {
        boolean z = this.byteOffset >= 0 && this.bitOffset >= 0 && this.bitOffset < 8 && (this.byteOffset < this.byteLimit || (this.byteOffset == this.byteLimit && this.bitOffset == 0));
        Assertions.checkState(z);
    }
}
