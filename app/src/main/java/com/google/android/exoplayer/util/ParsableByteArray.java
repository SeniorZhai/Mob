package com.google.android.exoplayer.util;

import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.nio.ByteBuffer;

public final class ParsableByteArray {
    public byte[] data;
    private int limit;
    private int position;

    public ParsableByteArray(int length) {
        this.data = new byte[length];
        this.limit = this.data.length;
    }

    public ParsableByteArray(byte[] data) {
        this.data = data;
        this.limit = data.length;
    }

    public ParsableByteArray(byte[] data, int limit) {
        this.data = data;
        this.limit = limit;
    }

    public void reset(byte[] data, int limit) {
        this.data = data;
        this.limit = limit;
        this.position = 0;
    }

    public void reset() {
        this.position = 0;
        this.limit = 0;
    }

    public int bytesLeft() {
        return this.limit - this.position;
    }

    public int limit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        boolean z = limit >= 0 && limit <= this.data.length;
        Assertions.checkArgument(z);
        this.limit = limit;
    }

    public int getPosition() {
        return this.position;
    }

    public int capacity() {
        return this.data == null ? 0 : this.data.length;
    }

    public void setPosition(int position) {
        boolean z = position >= 0 && position <= this.limit;
        Assertions.checkArgument(z);
        this.position = position;
    }

    public void skipBytes(int bytes) {
        setPosition(this.position + bytes);
    }

    public void readBytes(ParsableBitArray bitArray, int length) {
        readBytes(bitArray.data, 0, length);
        bitArray.setPosition(0);
    }

    public void readBytes(byte[] buffer, int offset, int length) {
        System.arraycopy(this.data, this.position, buffer, offset, length);
        this.position += length;
    }

    public void readBytes(ByteBuffer buffer, int length) {
        buffer.put(this.data, this.position, length);
        this.position += length;
    }

    public int readUnsignedByte() {
        byte[] bArr = this.data;
        int i = this.position;
        this.position = i + 1;
        return bArr[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
    }

    public int readUnsignedShort() {
        byte[] bArr = this.data;
        int i = this.position;
        this.position = i + 1;
        int i2 = (bArr[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8;
        byte[] bArr2 = this.data;
        int i3 = this.position;
        this.position = i3 + 1;
        return i2 | (bArr2[i3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
    }

    public int readUnsignedInt24() {
        byte[] bArr = this.data;
        int i = this.position;
        this.position = i + 1;
        int i2 = (bArr[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 16;
        byte[] bArr2 = this.data;
        int i3 = this.position;
        this.position = i3 + 1;
        i2 |= (bArr2[i3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8;
        bArr2 = this.data;
        i3 = this.position;
        this.position = i3 + 1;
        return i2 | (bArr2[i3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
    }

    public long readUnsignedInt() {
        byte[] bArr = this.data;
        int i = this.position;
        this.position = i + 1;
        long j = (((long) bArr[i]) & 255) << 24;
        byte[] bArr2 = this.data;
        int i2 = this.position;
        this.position = i2 + 1;
        j |= (((long) bArr2[i2]) & 255) << 16;
        bArr2 = this.data;
        i2 = this.position;
        this.position = i2 + 1;
        j |= (((long) bArr2[i2]) & 255) << 8;
        bArr2 = this.data;
        i2 = this.position;
        this.position = i2 + 1;
        return j | (((long) bArr2[i2]) & 255);
    }

    public int readInt() {
        byte[] bArr = this.data;
        int i = this.position;
        this.position = i + 1;
        int i2 = (bArr[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 24;
        byte[] bArr2 = this.data;
        int i3 = this.position;
        this.position = i3 + 1;
        i2 |= (bArr2[i3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 16;
        bArr2 = this.data;
        i3 = this.position;
        this.position = i3 + 1;
        i2 |= (bArr2[i3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8;
        bArr2 = this.data;
        i3 = this.position;
        this.position = i3 + 1;
        return i2 | (bArr2[i3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
    }

    public long readLong() {
        byte[] bArr = this.data;
        int i = this.position;
        this.position = i + 1;
        long j = (((long) bArr[i]) & 255) << 56;
        byte[] bArr2 = this.data;
        int i2 = this.position;
        this.position = i2 + 1;
        j |= (((long) bArr2[i2]) & 255) << 48;
        bArr2 = this.data;
        i2 = this.position;
        this.position = i2 + 1;
        j |= (((long) bArr2[i2]) & 255) << 40;
        bArr2 = this.data;
        i2 = this.position;
        this.position = i2 + 1;
        j |= (((long) bArr2[i2]) & 255) << 32;
        bArr2 = this.data;
        i2 = this.position;
        this.position = i2 + 1;
        j |= (((long) bArr2[i2]) & 255) << 24;
        bArr2 = this.data;
        i2 = this.position;
        this.position = i2 + 1;
        j |= (((long) bArr2[i2]) & 255) << 16;
        bArr2 = this.data;
        i2 = this.position;
        this.position = i2 + 1;
        j |= (((long) bArr2[i2]) & 255) << 8;
        bArr2 = this.data;
        i2 = this.position;
        this.position = i2 + 1;
        return j | (((long) bArr2[i2]) & 255);
    }

    public int readUnsignedFixedPoint1616() {
        byte[] bArr = this.data;
        int i = this.position;
        this.position = i + 1;
        int i2 = (bArr[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8;
        byte[] bArr2 = this.data;
        int i3 = this.position;
        this.position = i3 + 1;
        int result = i2 | (bArr2[i3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        this.position += 2;
        return result;
    }

    public int readSynchSafeInt() {
        return (((readUnsignedByte() << 21) | (readUnsignedByte() << 14)) | (readUnsignedByte() << 7)) | readUnsignedByte();
    }

    public int readUnsignedIntToInt() {
        int result = readInt();
        if (result >= 0) {
            return result;
        }
        throw new IllegalStateException("Top bit not zero: " + result);
    }

    public long readUnsignedLongToLong() {
        long result = readLong();
        if (result >= 0) {
            return result;
        }
        throw new IllegalStateException("Top bit not zero: " + result);
    }
}
