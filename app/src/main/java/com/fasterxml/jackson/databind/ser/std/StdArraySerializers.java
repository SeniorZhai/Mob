package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonArrayFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatTypes;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.helpshift.constants.MessageColumns;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;

public class StdArraySerializers {
    protected static final HashMap<String, JsonSerializer<?>> _arraySerializers = new HashMap();

    @JacksonStdImpl
    public static final class BooleanArraySerializer extends ArraySerializerBase<boolean[]> {
        private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Boolean.class);

        public BooleanArraySerializer() {
            super(boolean[].class, null);
        }

        public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
            return this;
        }

        public JavaType getContentType() {
            return VALUE_TYPE;
        }

        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        public boolean isEmpty(boolean[] zArr) {
            return zArr == null || zArr.length == 0;
        }

        public boolean hasSingleElement(boolean[] zArr) {
            return zArr.length == 1;
        }

        public void serializeContents(boolean[] zArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
            for (boolean writeBoolean : zArr) {
                jsonGenerator.writeBoolean(writeBoolean);
            }
        }

        public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
            JsonNode createSchemaNode = createSchemaNode("array", true);
            createSchemaNode.put("items", createSchemaNode("boolean"));
            return createSchemaNode;
        }

        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
            if (jsonFormatVisitorWrapper != null) {
                JsonArrayFormatVisitor expectArrayFormat = jsonFormatVisitorWrapper.expectArrayFormat(javaType);
                if (expectArrayFormat != null) {
                    expectArrayFormat.itemsFormat(JsonFormatTypes.BOOLEAN);
                }
            }
        }
    }

    @JacksonStdImpl
    public static final class ByteArraySerializer extends StdSerializer<byte[]> {
        public ByteArraySerializer() {
            super(byte[].class);
        }

        public boolean isEmpty(byte[] bArr) {
            return bArr == null || bArr.length == 0;
        }

        public void serialize(byte[] bArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
            jsonGenerator.writeBinary(serializerProvider.getConfig().getBase64Variant(), bArr, 0, bArr.length);
        }

        public void serializeWithType(byte[] bArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException, JsonGenerationException {
            typeSerializer.writeTypePrefixForScalar(bArr, jsonGenerator);
            jsonGenerator.writeBinary(serializerProvider.getConfig().getBase64Variant(), bArr, 0, bArr.length);
            typeSerializer.writeTypeSuffixForScalar(bArr, jsonGenerator);
        }

        public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
            JsonNode createSchemaNode = createSchemaNode("array", true);
            createSchemaNode.put("items", createSchemaNode("string"));
            return createSchemaNode;
        }

        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
            if (jsonFormatVisitorWrapper != null) {
                JsonArrayFormatVisitor expectArrayFormat = jsonFormatVisitorWrapper.expectArrayFormat(javaType);
                if (expectArrayFormat != null) {
                    expectArrayFormat.itemsFormat(JsonFormatTypes.STRING);
                }
            }
        }
    }

    @JacksonStdImpl
    public static final class CharArraySerializer extends StdSerializer<char[]> {
        public CharArraySerializer() {
            super(char[].class);
        }

        public boolean isEmpty(char[] cArr) {
            return cArr == null || cArr.length == 0;
        }

        public void serialize(char[] cArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
            if (serializerProvider.isEnabled(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)) {
                jsonGenerator.writeStartArray();
                _writeArrayContents(jsonGenerator, cArr);
                jsonGenerator.writeEndArray();
                return;
            }
            jsonGenerator.writeString(cArr, 0, cArr.length);
        }

        public void serializeWithType(char[] cArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException, JsonGenerationException {
            if (serializerProvider.isEnabled(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS)) {
                typeSerializer.writeTypePrefixForArray(cArr, jsonGenerator);
                _writeArrayContents(jsonGenerator, cArr);
                typeSerializer.writeTypeSuffixForArray(cArr, jsonGenerator);
                return;
            }
            typeSerializer.writeTypePrefixForScalar(cArr, jsonGenerator);
            jsonGenerator.writeString(cArr, 0, cArr.length);
            typeSerializer.writeTypeSuffixForScalar(cArr, jsonGenerator);
        }

        private final void _writeArrayContents(JsonGenerator jsonGenerator, char[] cArr) throws IOException, JsonGenerationException {
            int length = cArr.length;
            for (int i = 0; i < length; i++) {
                jsonGenerator.writeString(cArr, i, 1);
            }
        }

        public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
            JsonNode createSchemaNode = createSchemaNode("array", true);
            JsonNode createSchemaNode2 = createSchemaNode("string");
            createSchemaNode2.put(MessageColumns.TYPE, "string");
            createSchemaNode.put("items", createSchemaNode2);
            return createSchemaNode;
        }

        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
            if (jsonFormatVisitorWrapper != null) {
                JsonArrayFormatVisitor expectArrayFormat = jsonFormatVisitorWrapper.expectArrayFormat(javaType);
                if (expectArrayFormat != null) {
                    expectArrayFormat.itemsFormat(JsonFormatTypes.STRING);
                }
            }
        }
    }

    @JacksonStdImpl
    public static final class DoubleArraySerializer extends ArraySerializerBase<double[]> {
        private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Double.TYPE);

        public DoubleArraySerializer() {
            super(double[].class, null);
        }

        public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
            return this;
        }

        public JavaType getContentType() {
            return VALUE_TYPE;
        }

        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        public boolean isEmpty(double[] dArr) {
            return dArr == null || dArr.length == 0;
        }

        public boolean hasSingleElement(double[] dArr) {
            return dArr.length == 1;
        }

        public void serializeContents(double[] dArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
            for (double writeNumber : dArr) {
                jsonGenerator.writeNumber(writeNumber);
            }
        }

        public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
            JsonNode createSchemaNode = createSchemaNode("array", true);
            createSchemaNode.put("items", createSchemaNode("number"));
            return createSchemaNode;
        }

        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
            if (jsonFormatVisitorWrapper != null) {
                JsonArrayFormatVisitor expectArrayFormat = jsonFormatVisitorWrapper.expectArrayFormat(javaType);
                if (expectArrayFormat != null) {
                    expectArrayFormat.itemsFormat(JsonFormatTypes.NUMBER);
                }
            }
        }
    }

    protected static abstract class TypedPrimitiveArraySerializer<T> extends ArraySerializerBase<T> {
        protected final TypeSerializer _valueTypeSerializer;

        protected TypedPrimitiveArraySerializer(Class<T> cls) {
            super((Class) cls);
            this._valueTypeSerializer = null;
        }

        protected TypedPrimitiveArraySerializer(TypedPrimitiveArraySerializer<T> typedPrimitiveArraySerializer, BeanProperty beanProperty, TypeSerializer typeSerializer) {
            super((ArraySerializerBase) typedPrimitiveArraySerializer, beanProperty);
            this._valueTypeSerializer = typeSerializer;
        }
    }

    @JacksonStdImpl
    public static final class FloatArraySerializer extends TypedPrimitiveArraySerializer<float[]> {
        private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Float.TYPE);

        public FloatArraySerializer() {
            super(float[].class);
        }

        public FloatArraySerializer(FloatArraySerializer floatArraySerializer, BeanProperty beanProperty, TypeSerializer typeSerializer) {
            super(floatArraySerializer, beanProperty, typeSerializer);
        }

        public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
            return new FloatArraySerializer(this, this._property, typeSerializer);
        }

        public JavaType getContentType() {
            return VALUE_TYPE;
        }

        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        public boolean isEmpty(float[] fArr) {
            return fArr == null || fArr.length == 0;
        }

        public boolean hasSingleElement(float[] fArr) {
            return fArr.length == 1;
        }

        public void serializeContents(float[] fArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
            int i = 0;
            int length;
            if (this._valueTypeSerializer != null) {
                length = fArr.length;
                while (i < length) {
                    this._valueTypeSerializer.writeTypePrefixForScalar(null, jsonGenerator, Float.TYPE);
                    jsonGenerator.writeNumber(fArr[i]);
                    this._valueTypeSerializer.writeTypeSuffixForScalar(null, jsonGenerator);
                    i++;
                }
                return;
            }
            length = fArr.length;
            while (i < length) {
                jsonGenerator.writeNumber(fArr[i]);
                i++;
            }
        }

        public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
            JsonNode createSchemaNode = createSchemaNode("array", true);
            createSchemaNode.put("items", createSchemaNode("number"));
            return createSchemaNode;
        }

        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
            if (jsonFormatVisitorWrapper != null) {
                JsonArrayFormatVisitor expectArrayFormat = jsonFormatVisitorWrapper.expectArrayFormat(javaType);
                if (expectArrayFormat != null) {
                    expectArrayFormat.itemsFormat(JsonFormatTypes.NUMBER);
                }
            }
        }
    }

    @JacksonStdImpl
    public static final class IntArraySerializer extends ArraySerializerBase<int[]> {
        private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Integer.TYPE);

        public IntArraySerializer() {
            super(int[].class, null);
        }

        public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
            return this;
        }

        public JavaType getContentType() {
            return VALUE_TYPE;
        }

        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        public boolean isEmpty(int[] iArr) {
            return iArr == null || iArr.length == 0;
        }

        public boolean hasSingleElement(int[] iArr) {
            return iArr.length == 1;
        }

        public void serializeContents(int[] iArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
            for (int writeNumber : iArr) {
                jsonGenerator.writeNumber(writeNumber);
            }
        }

        public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
            JsonNode createSchemaNode = createSchemaNode("array", true);
            createSchemaNode.put("items", createSchemaNode("integer"));
            return createSchemaNode;
        }

        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
            if (jsonFormatVisitorWrapper != null) {
                JsonArrayFormatVisitor expectArrayFormat = jsonFormatVisitorWrapper.expectArrayFormat(javaType);
                if (expectArrayFormat != null) {
                    expectArrayFormat.itemsFormat(JsonFormatTypes.INTEGER);
                }
            }
        }
    }

    @JacksonStdImpl
    public static final class LongArraySerializer extends TypedPrimitiveArraySerializer<long[]> {
        private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Long.TYPE);

        public LongArraySerializer() {
            super(long[].class);
        }

        public LongArraySerializer(LongArraySerializer longArraySerializer, BeanProperty beanProperty, TypeSerializer typeSerializer) {
            super(longArraySerializer, beanProperty, typeSerializer);
        }

        public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
            return new LongArraySerializer(this, this._property, typeSerializer);
        }

        public JavaType getContentType() {
            return VALUE_TYPE;
        }

        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        public boolean isEmpty(long[] jArr) {
            return jArr == null || jArr.length == 0;
        }

        public boolean hasSingleElement(long[] jArr) {
            return jArr.length == 1;
        }

        public void serializeContents(long[] jArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
            int i = 0;
            int length;
            if (this._valueTypeSerializer != null) {
                length = jArr.length;
                while (i < length) {
                    this._valueTypeSerializer.writeTypePrefixForScalar(null, jsonGenerator, Long.TYPE);
                    jsonGenerator.writeNumber(jArr[i]);
                    this._valueTypeSerializer.writeTypeSuffixForScalar(null, jsonGenerator);
                    i++;
                }
                return;
            }
            length = jArr.length;
            while (i < length) {
                jsonGenerator.writeNumber(jArr[i]);
                i++;
            }
        }

        public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
            JsonNode createSchemaNode = createSchemaNode("array", true);
            createSchemaNode.put("items", createSchemaNode("number", true));
            return createSchemaNode;
        }

        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
            if (jsonFormatVisitorWrapper != null) {
                JsonArrayFormatVisitor expectArrayFormat = jsonFormatVisitorWrapper.expectArrayFormat(javaType);
                if (expectArrayFormat != null) {
                    expectArrayFormat.itemsFormat(JsonFormatTypes.NUMBER);
                }
            }
        }
    }

    @JacksonStdImpl
    public static final class ShortArraySerializer extends TypedPrimitiveArraySerializer<short[]> {
        private static final JavaType VALUE_TYPE = TypeFactory.defaultInstance().uncheckedSimpleType(Short.TYPE);

        public ShortArraySerializer() {
            super(short[].class);
        }

        public ShortArraySerializer(ShortArraySerializer shortArraySerializer, BeanProperty beanProperty, TypeSerializer typeSerializer) {
            super(shortArraySerializer, beanProperty, typeSerializer);
        }

        public ContainerSerializer<?> _withValueTypeSerializer(TypeSerializer typeSerializer) {
            return new ShortArraySerializer(this, this._property, typeSerializer);
        }

        public JavaType getContentType() {
            return VALUE_TYPE;
        }

        public JsonSerializer<?> getContentSerializer() {
            return null;
        }

        public boolean isEmpty(short[] sArr) {
            return sArr == null || sArr.length == 0;
        }

        public boolean hasSingleElement(short[] sArr) {
            return sArr.length == 1;
        }

        public void serializeContents(short[] sArr, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
            int i = 0;
            int length;
            if (this._valueTypeSerializer != null) {
                length = sArr.length;
                while (i < length) {
                    this._valueTypeSerializer.writeTypePrefixForScalar(null, jsonGenerator, Short.TYPE);
                    jsonGenerator.writeNumber(sArr[i]);
                    this._valueTypeSerializer.writeTypeSuffixForScalar(null, jsonGenerator);
                    i++;
                }
                return;
            }
            length = sArr.length;
            while (i < length) {
                jsonGenerator.writeNumber(sArr[i]);
                i++;
            }
        }

        public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
            JsonNode createSchemaNode = createSchemaNode("array", true);
            createSchemaNode.put("items", createSchemaNode("integer"));
            return createSchemaNode;
        }

        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
            if (jsonFormatVisitorWrapper != null) {
                JsonArrayFormatVisitor expectArrayFormat = jsonFormatVisitorWrapper.expectArrayFormat(javaType);
                if (expectArrayFormat != null) {
                    expectArrayFormat.itemsFormat(JsonFormatTypes.INTEGER);
                }
            }
        }
    }

    static {
        _arraySerializers.put(boolean[].class.getName(), new BooleanArraySerializer());
        _arraySerializers.put(byte[].class.getName(), new ByteArraySerializer());
        _arraySerializers.put(char[].class.getName(), new CharArraySerializer());
        _arraySerializers.put(short[].class.getName(), new ShortArraySerializer());
        _arraySerializers.put(int[].class.getName(), new IntArraySerializer());
        _arraySerializers.put(long[].class.getName(), new LongArraySerializer());
        _arraySerializers.put(float[].class.getName(), new FloatArraySerializer());
        _arraySerializers.put(double[].class.getName(), new DoubleArraySerializer());
    }

    protected StdArraySerializers() {
    }

    public static JsonSerializer<?> findStandardImpl(Class<?> cls) {
        return (JsonSerializer) _arraySerializers.get(cls.getName());
    }
}
