package com.fasterxml.jackson.core;

import com.fasterxml.jackson.core.type.TypeReference;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Iterator;

public abstract class JsonParser implements Closeable, Versioned {
    private static final int MAX_BYTE_I = 255;
    private static final int MAX_SHORT_I = 32767;
    private static final int MIN_BYTE_I = -128;
    private static final int MIN_SHORT_I = -32768;
    protected int _features;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$fasterxml$jackson$core$JsonToken = new int[JsonToken.values().length];

        static {
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.VALUE_TRUE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.VALUE_FALSE.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    public enum Feature {
        AUTO_CLOSE_SOURCE(true),
        ALLOW_COMMENTS(false),
        ALLOW_UNQUOTED_FIELD_NAMES(false),
        ALLOW_SINGLE_QUOTES(false),
        ALLOW_UNQUOTED_CONTROL_CHARS(false),
        ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER(false),
        ALLOW_NUMERIC_LEADING_ZEROS(false),
        ALLOW_NON_NUMERIC_NUMBERS(false);
        
        private final boolean _defaultState;

        public static int collectDefaults() {
            int i = 0;
            for (Feature feature : values()) {
                if (feature.enabledByDefault()) {
                    i |= feature.getMask();
                }
            }
            return i;
        }

        private Feature(boolean z) {
            this._defaultState = z;
        }

        public boolean enabledByDefault() {
            return this._defaultState;
        }

        public int getMask() {
            return 1 << ordinal();
        }
    }

    public enum NumberType {
        INT,
        LONG,
        BIG_INTEGER,
        FLOAT,
        DOUBLE,
        BIG_DECIMAL
    }

    public abstract void clearCurrentToken();

    public abstract void close() throws IOException;

    public abstract BigInteger getBigIntegerValue() throws IOException, JsonParseException;

    public abstract byte[] getBinaryValue(Base64Variant base64Variant) throws IOException, JsonParseException;

    public abstract ObjectCodec getCodec();

    public abstract JsonLocation getCurrentLocation();

    public abstract String getCurrentName() throws IOException, JsonParseException;

    public abstract JsonToken getCurrentToken();

    public abstract BigDecimal getDecimalValue() throws IOException, JsonParseException;

    public abstract double getDoubleValue() throws IOException, JsonParseException;

    public abstract Object getEmbeddedObject() throws IOException, JsonParseException;

    public abstract float getFloatValue() throws IOException, JsonParseException;

    public abstract int getIntValue() throws IOException, JsonParseException;

    public abstract JsonToken getLastClearedToken();

    public abstract long getLongValue() throws IOException, JsonParseException;

    public abstract NumberType getNumberType() throws IOException, JsonParseException;

    public abstract Number getNumberValue() throws IOException, JsonParseException;

    public abstract JsonStreamContext getParsingContext();

    public abstract String getText() throws IOException, JsonParseException;

    public abstract char[] getTextCharacters() throws IOException, JsonParseException;

    public abstract int getTextLength() throws IOException, JsonParseException;

    public abstract int getTextOffset() throws IOException, JsonParseException;

    public abstract JsonLocation getTokenLocation();

    public abstract String getValueAsString(String str) throws IOException, JsonParseException;

    public abstract boolean hasCurrentToken();

    public abstract boolean hasTextCharacters();

    public abstract boolean isClosed();

    public abstract JsonToken nextToken() throws IOException, JsonParseException;

    public abstract JsonToken nextValue() throws IOException, JsonParseException;

    public abstract void overrideCurrentName(String str);

    public abstract void setCodec(ObjectCodec objectCodec);

    public abstract JsonParser skipChildren() throws IOException, JsonParseException;

    public abstract Version version();

    protected JsonParser() {
    }

    protected JsonParser(int i) {
        this._features = i;
    }

    public Object getInputSource() {
        return null;
    }

    public void setSchema(FormatSchema formatSchema) {
        throw new UnsupportedOperationException("Parser of type " + getClass().getName() + " does not support schema of type '" + formatSchema.getSchemaType() + "'");
    }

    public FormatSchema getSchema() {
        return null;
    }

    public boolean canUseSchema(FormatSchema formatSchema) {
        return false;
    }

    public boolean requiresCustomCodec() {
        return false;
    }

    public int releaseBuffered(OutputStream outputStream) throws IOException {
        return -1;
    }

    public int releaseBuffered(Writer writer) throws IOException {
        return -1;
    }

    public JsonParser enable(Feature feature) {
        this._features |= feature.getMask();
        return this;
    }

    public JsonParser disable(Feature feature) {
        this._features &= feature.getMask() ^ -1;
        return this;
    }

    public JsonParser configure(Feature feature, boolean z) {
        if (z) {
            enable(feature);
        } else {
            disable(feature);
        }
        return this;
    }

    public boolean isEnabled(Feature feature) {
        return (this._features & feature.getMask()) != 0;
    }

    public boolean nextFieldName(SerializableString serializableString) throws IOException, JsonParseException {
        return nextToken() == JsonToken.FIELD_NAME && serializableString.getValue().equals(getCurrentName());
    }

    public String nextTextValue() throws IOException, JsonParseException {
        return nextToken() == JsonToken.VALUE_STRING ? getText() : null;
    }

    public int nextIntValue(int i) throws IOException, JsonParseException {
        return nextToken() == JsonToken.VALUE_NUMBER_INT ? getIntValue() : i;
    }

    public long nextLongValue(long j) throws IOException, JsonParseException {
        return nextToken() == JsonToken.VALUE_NUMBER_INT ? getLongValue() : j;
    }

    public Boolean nextBooleanValue() throws IOException, JsonParseException {
        switch (AnonymousClass1.$SwitchMap$com$fasterxml$jackson$core$JsonToken[nextToken().ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                return Boolean.TRUE;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                return Boolean.FALSE;
            default:
                return null;
        }
    }

    public boolean isExpectedStartArrayToken() {
        return getCurrentToken() == JsonToken.START_ARRAY;
    }

    public byte getByteValue() throws IOException, JsonParseException {
        int intValue = getIntValue();
        if (intValue >= MIN_BYTE_I && intValue <= MAX_BYTE_I) {
            return (byte) intValue;
        }
        throw _constructError("Numeric value (" + getText() + ") out of range of Java byte");
    }

    public short getShortValue() throws IOException, JsonParseException {
        int intValue = getIntValue();
        if (intValue >= MIN_SHORT_I && intValue <= MAX_SHORT_I) {
            return (short) intValue;
        }
        throw _constructError("Numeric value (" + getText() + ") out of range of Java short");
    }

    public boolean getBooleanValue() throws IOException, JsonParseException {
        JsonToken currentToken = getCurrentToken();
        if (currentToken == JsonToken.VALUE_TRUE) {
            return true;
        }
        if (currentToken == JsonToken.VALUE_FALSE) {
            return false;
        }
        throw new JsonParseException("Current token (" + currentToken + ") not of boolean type", getCurrentLocation());
    }

    public byte[] getBinaryValue() throws IOException, JsonParseException {
        return getBinaryValue(Base64Variants.getDefaultVariant());
    }

    public int readBinaryValue(OutputStream outputStream) throws IOException, JsonParseException {
        return readBinaryValue(Base64Variants.getDefaultVariant(), outputStream);
    }

    public int readBinaryValue(Base64Variant base64Variant, OutputStream outputStream) throws IOException, JsonParseException {
        _reportUnsupportedOperation();
        return 0;
    }

    public int getValueAsInt() throws IOException, JsonParseException {
        return getValueAsInt(0);
    }

    public int getValueAsInt(int i) throws IOException, JsonParseException {
        return i;
    }

    public long getValueAsLong() throws IOException, JsonParseException {
        return getValueAsLong(0);
    }

    public long getValueAsLong(long j) throws IOException, JsonParseException {
        return j;
    }

    public double getValueAsDouble() throws IOException, JsonParseException {
        return getValueAsDouble(0.0d);
    }

    public double getValueAsDouble(double d) throws IOException, JsonParseException {
        return d;
    }

    public boolean getValueAsBoolean() throws IOException, JsonParseException {
        return getValueAsBoolean(false);
    }

    public boolean getValueAsBoolean(boolean z) throws IOException, JsonParseException {
        return z;
    }

    public String getValueAsString() throws IOException, JsonParseException {
        return getValueAsString(null);
    }

    public <T> T readValueAs(Class<T> cls) throws IOException, JsonProcessingException {
        ObjectCodec codec = getCodec();
        if (codec != null) {
            return codec.readValue(this, (Class) cls);
        }
        throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
    }

    public <T> T readValueAs(TypeReference<?> typeReference) throws IOException, JsonProcessingException {
        ObjectCodec codec = getCodec();
        if (codec != null) {
            return codec.readValue(this, (TypeReference) typeReference);
        }
        throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
    }

    public <T> Iterator<T> readValuesAs(Class<T> cls) throws IOException, JsonProcessingException {
        ObjectCodec codec = getCodec();
        if (codec != null) {
            return codec.readValues(this, (Class) cls);
        }
        throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
    }

    public <T> Iterator<T> readValuesAs(TypeReference<?> typeReference) throws IOException, JsonProcessingException {
        ObjectCodec codec = getCodec();
        if (codec != null) {
            return codec.readValues(this, (TypeReference) typeReference);
        }
        throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into Java objects");
    }

    public <T extends TreeNode> T readValueAsTree() throws IOException, JsonProcessingException {
        ObjectCodec codec = getCodec();
        if (codec != null) {
            return codec.readTree(this);
        }
        throw new IllegalStateException("No ObjectCodec defined for the parser, can not deserialize JSON into JsonNode tree");
    }

    protected JsonParseException _constructError(String str) {
        return new JsonParseException(str, getCurrentLocation());
    }

    protected void _reportUnsupportedOperation() {
        throw new UnsupportedOperationException("Operation not supported by parser of type " + getClass().getName());
    }
}
