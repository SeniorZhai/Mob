package com.google.android.exoplayer.extractor.webm;

import com.google.android.exoplayer.extractor.ExtractorInput;
import com.google.android.exoplayer.util.Assertions;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Stack;
import org.apache.http.protocol.HTTP;

final class DefaultEbmlReader implements EbmlReader {
    private static final int ELEMENT_STATE_READ_CONTENT = 2;
    private static final int ELEMENT_STATE_READ_CONTENT_SIZE = 1;
    private static final int ELEMENT_STATE_READ_ID = 0;
    private static final int MAX_INTEGER_ELEMENT_SIZE_BYTES = 8;
    private static final int VALID_FLOAT32_ELEMENT_SIZE_BYTES = 4;
    private static final int VALID_FLOAT64_ELEMENT_SIZE_BYTES = 8;
    private long elementContentSize;
    private int elementId;
    private int elementState;
    private final Stack<MasterElement> masterElementsStack = new Stack();
    private EbmlReaderOutput output;
    private final byte[] scratch = new byte[VALID_FLOAT64_ELEMENT_SIZE_BYTES];
    private final VarintReader varintReader = new VarintReader();

    private static final class MasterElement {
        private final long elementEndPosition;
        private final int elementId;

        private MasterElement(int elementId, long elementEndPosition) {
            this.elementId = elementId;
            this.elementEndPosition = elementEndPosition;
        }
    }

    DefaultEbmlReader() {
    }

    public void init(EbmlReaderOutput eventHandler) {
        this.output = eventHandler;
    }

    public void reset() {
        this.elementState = ELEMENT_STATE_READ_ID;
        this.masterElementsStack.clear();
        this.varintReader.reset();
    }

    public boolean read(ExtractorInput input) throws IOException, InterruptedException {
        Assertions.checkState(this.output != null);
        while (true) {
            if (this.masterElementsStack.isEmpty() || input.getPosition() < ((MasterElement) this.masterElementsStack.peek()).elementEndPosition) {
                if (this.elementState == 0) {
                    long result = this.varintReader.readUnsignedVarint(input, true, false);
                    if (result == -1) {
                        return false;
                    }
                    this.elementId = (int) result;
                    this.elementState = ELEMENT_STATE_READ_CONTENT_SIZE;
                }
                if (this.elementState == ELEMENT_STATE_READ_CONTENT_SIZE) {
                    this.elementContentSize = this.varintReader.readUnsignedVarint(input, false, true);
                    this.elementState = ELEMENT_STATE_READ_CONTENT;
                }
                int type = this.output.getElementType(this.elementId);
                switch (type) {
                    case ELEMENT_STATE_READ_ID /*0*/:
                        input.skipFully((int) this.elementContentSize);
                        this.elementState = ELEMENT_STATE_READ_ID;
                    case ELEMENT_STATE_READ_CONTENT_SIZE /*1*/:
                        long elementContentPosition = input.getPosition();
                        this.masterElementsStack.add(new MasterElement(this.elementId, elementContentPosition + this.elementContentSize));
                        this.output.startMasterElement(this.elementId, elementContentPosition, this.elementContentSize);
                        this.elementState = ELEMENT_STATE_READ_ID;
                        return true;
                    case ELEMENT_STATE_READ_CONTENT /*2*/:
                        if (this.elementContentSize > 8) {
                            throw new IllegalStateException("Invalid integer size: " + this.elementContentSize);
                        }
                        this.output.integerElement(this.elementId, readInteger(input, (int) this.elementContentSize));
                        this.elementState = ELEMENT_STATE_READ_ID;
                        return true;
                    case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                        if (this.elementContentSize > 2147483647L) {
                            throw new IllegalStateException("String element size: " + this.elementContentSize);
                        }
                        this.output.stringElement(this.elementId, readString(input, (int) this.elementContentSize));
                        this.elementState = ELEMENT_STATE_READ_ID;
                        return true;
                    case VALID_FLOAT32_ELEMENT_SIZE_BYTES /*4*/:
                        this.output.binaryElement(this.elementId, (int) this.elementContentSize, input);
                        this.elementState = ELEMENT_STATE_READ_ID;
                        return true;
                    case Player.STATE_ENDED /*5*/:
                        if (this.elementContentSize == 4 || this.elementContentSize == 8) {
                            this.output.floatElement(this.elementId, readFloat(input, (int) this.elementContentSize));
                            this.elementState = ELEMENT_STATE_READ_ID;
                            return true;
                        }
                        throw new IllegalStateException("Invalid float size: " + this.elementContentSize);
                    default:
                        throw new IllegalStateException("Invalid element type " + type);
                }
            }
            this.output.endMasterElement(((MasterElement) this.masterElementsStack.pop()).elementId);
            return true;
        }
    }

    private long readInteger(ExtractorInput input, int byteLength) throws IOException, InterruptedException {
        input.readFully(this.scratch, ELEMENT_STATE_READ_ID, byteLength);
        long value = 0;
        for (int i = ELEMENT_STATE_READ_ID; i < byteLength; i += ELEMENT_STATE_READ_CONTENT_SIZE) {
            value = (value << VALID_FLOAT64_ELEMENT_SIZE_BYTES) | ((long) (this.scratch[i] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT));
        }
        return value;
    }

    private double readFloat(ExtractorInput input, int byteLength) throws IOException, InterruptedException {
        long integerValue = readInteger(input, byteLength);
        if (byteLength == VALID_FLOAT32_ELEMENT_SIZE_BYTES) {
            return (double) Float.intBitsToFloat((int) integerValue);
        }
        return Double.longBitsToDouble(integerValue);
    }

    private String readString(ExtractorInput input, int byteLength) throws IOException, InterruptedException {
        byte[] stringBytes = new byte[byteLength];
        input.readFully(stringBytes, ELEMENT_STATE_READ_ID, byteLength);
        return new String(stringBytes, Charset.forName(HTTP.UTF_8));
    }
}
