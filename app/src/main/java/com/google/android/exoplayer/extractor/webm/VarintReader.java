package com.google.android.exoplayer.extractor.webm;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.exoplayer.extractor.ExtractorInput;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;

class VarintReader {
    private static final int STATE_BEGIN_READING = 0;
    private static final int STATE_READ_CONTENTS = 1;
    private static final int[] VARINT_LENGTH_MASKS = new int[]{AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS, 64, 32, 16, 8, 4, 2, STATE_READ_CONTENTS};
    private int length;
    private final byte[] scratch = new byte[8];
    private int state;

    public void reset() {
        this.state = STATE_BEGIN_READING;
        this.length = STATE_BEGIN_READING;
    }

    public long readUnsignedVarint(ExtractorInput input, boolean allowEndOfInput, boolean removeLengthMask) throws IOException, InterruptedException {
        int i;
        if (this.state == 0) {
            if (!input.readFully(this.scratch, STATE_BEGIN_READING, STATE_READ_CONTENTS, allowEndOfInput)) {
                return -1;
            }
            int firstByte = this.scratch[STATE_BEGIN_READING] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            this.length = -1;
            for (i = STATE_BEGIN_READING; i < VARINT_LENGTH_MASKS.length; i += STATE_READ_CONTENTS) {
                if ((VARINT_LENGTH_MASKS[i] & firstByte) != 0) {
                    this.length = i + STATE_READ_CONTENTS;
                    break;
                }
            }
            if (this.length == -1) {
                throw new IllegalStateException("No valid varint length mask found");
            }
            this.state = STATE_READ_CONTENTS;
        }
        input.readFully(this.scratch, STATE_READ_CONTENTS, this.length - 1);
        if (removeLengthMask) {
            byte[] bArr = this.scratch;
            bArr[STATE_BEGIN_READING] = (byte) (bArr[STATE_BEGIN_READING] & (VARINT_LENGTH_MASKS[this.length - 1] ^ -1));
        }
        long varint = 0;
        for (i = STATE_BEGIN_READING; i < this.length; i += STATE_READ_CONTENTS) {
            varint = (varint << 8) | ((long) (this.scratch[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT));
        }
        this.state = STATE_BEGIN_READING;
        return varint;
    }

    public int getLastLength() {
        return this.length;
    }
}
