package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonFormat.Value;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.JsonSerializableSchema;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.AnyGetterWriter;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerBuilder;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.impl.ObjectIdWriter;
import com.fasterxml.jackson.databind.ser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.mobcrush.mobcrush.helper.DBLikedChannelsHelper;
import io.fabric.sdk.android.BuildConfig;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;

public abstract class BeanSerializerBase extends StdSerializer<Object> implements ContextualSerializer, ResolvableSerializer, JsonFormatVisitable, SchemaAware {
    protected static final BeanPropertyWriter[] NO_PROPS = new BeanPropertyWriter[0];
    protected final AnyGetterWriter _anyGetterWriter;
    protected final BeanPropertyWriter[] _filteredProps;
    protected final ObjectIdWriter _objectIdWriter;
    protected final Object _propertyFilterId;
    protected final BeanPropertyWriter[] _props;
    protected final Shape _serializationShape;
    protected final AnnotatedMember _typeId;

    protected abstract BeanSerializerBase asArraySerializer();

    public abstract void serialize(Object obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException;

    protected abstract BeanSerializerBase withIgnorals(String[] strArr);

    public abstract BeanSerializerBase withObjectIdWriter(ObjectIdWriter objectIdWriter);

    protected BeanSerializerBase(JavaType javaType, BeanSerializerBuilder beanSerializerBuilder, BeanPropertyWriter[] beanPropertyWriterArr, BeanPropertyWriter[] beanPropertyWriterArr2) {
        Shape shape = null;
        super(javaType);
        this._props = beanPropertyWriterArr;
        this._filteredProps = beanPropertyWriterArr2;
        if (beanSerializerBuilder == null) {
            this._typeId = null;
            this._anyGetterWriter = null;
            this._propertyFilterId = null;
            this._objectIdWriter = null;
            this._serializationShape = null;
            return;
        }
        this._typeId = beanSerializerBuilder.getTypeId();
        this._anyGetterWriter = beanSerializerBuilder.getAnyGetter();
        this._propertyFilterId = beanSerializerBuilder.getFilterId();
        this._objectIdWriter = beanSerializerBuilder.getObjectIdWriter();
        Value findExpectedFormat = beanSerializerBuilder.getBeanDescription().findExpectedFormat(null);
        if (findExpectedFormat != null) {
            shape = findExpectedFormat.getShape();
        }
        this._serializationShape = shape;
    }

    public BeanSerializerBase(BeanSerializerBase beanSerializerBase, BeanPropertyWriter[] beanPropertyWriterArr, BeanPropertyWriter[] beanPropertyWriterArr2) {
        super(beanSerializerBase._handledType);
        this._props = beanPropertyWriterArr;
        this._filteredProps = beanPropertyWriterArr2;
        this._typeId = beanSerializerBase._typeId;
        this._anyGetterWriter = beanSerializerBase._anyGetterWriter;
        this._objectIdWriter = beanSerializerBase._objectIdWriter;
        this._propertyFilterId = beanSerializerBase._propertyFilterId;
        this._serializationShape = beanSerializerBase._serializationShape;
    }

    protected BeanSerializerBase(BeanSerializerBase beanSerializerBase, ObjectIdWriter objectIdWriter) {
        super(beanSerializerBase._handledType);
        this._props = beanSerializerBase._props;
        this._filteredProps = beanSerializerBase._filteredProps;
        this._typeId = beanSerializerBase._typeId;
        this._anyGetterWriter = beanSerializerBase._anyGetterWriter;
        this._objectIdWriter = objectIdWriter;
        this._propertyFilterId = beanSerializerBase._propertyFilterId;
        this._serializationShape = beanSerializerBase._serializationShape;
    }

    protected BeanSerializerBase(BeanSerializerBase beanSerializerBase, String[] strArr) {
        BeanPropertyWriter[] beanPropertyWriterArr = null;
        super(beanSerializerBase._handledType);
        HashSet arrayToSet = ArrayBuilders.arrayToSet(strArr);
        BeanPropertyWriter[] beanPropertyWriterArr2 = beanSerializerBase._props;
        BeanPropertyWriter[] beanPropertyWriterArr3 = beanSerializerBase._filteredProps;
        int length = beanPropertyWriterArr2.length;
        ArrayList arrayList = new ArrayList(length);
        ArrayList arrayList2 = beanPropertyWriterArr3 == null ? null : new ArrayList(length);
        for (int i = 0; i < length; i++) {
            BeanPropertyWriter beanPropertyWriter = beanPropertyWriterArr2[i];
            if (!arrayToSet.contains(beanPropertyWriter.getName())) {
                arrayList.add(beanPropertyWriter);
                if (beanPropertyWriterArr3 != null) {
                    arrayList2.add(beanPropertyWriterArr3[i]);
                }
            }
        }
        this._props = (BeanPropertyWriter[]) arrayList.toArray(new BeanPropertyWriter[arrayList.size()]);
        if (arrayList2 != null) {
            beanPropertyWriterArr = (BeanPropertyWriter[]) arrayList2.toArray(new BeanPropertyWriter[arrayList2.size()]);
        }
        this._filteredProps = beanPropertyWriterArr;
        this._typeId = beanSerializerBase._typeId;
        this._anyGetterWriter = beanSerializerBase._anyGetterWriter;
        this._objectIdWriter = beanSerializerBase._objectIdWriter;
        this._propertyFilterId = beanSerializerBase._propertyFilterId;
        this._serializationShape = beanSerializerBase._serializationShape;
    }

    protected BeanSerializerBase(BeanSerializerBase beanSerializerBase) {
        this(beanSerializerBase, beanSerializerBase._props, beanSerializerBase._filteredProps);
    }

    protected BeanSerializerBase(BeanSerializerBase beanSerializerBase, NameTransformer nameTransformer) {
        this(beanSerializerBase, rename(beanSerializerBase._props, nameTransformer), rename(beanSerializerBase._filteredProps, nameTransformer));
    }

    private static final BeanPropertyWriter[] rename(BeanPropertyWriter[] beanPropertyWriterArr, NameTransformer nameTransformer) {
        if (beanPropertyWriterArr == null || beanPropertyWriterArr.length == 0 || nameTransformer == null || nameTransformer == NameTransformer.NOP) {
            return beanPropertyWriterArr;
        }
        int length = beanPropertyWriterArr.length;
        BeanPropertyWriter[] beanPropertyWriterArr2 = new BeanPropertyWriter[length];
        for (int i = 0; i < length; i++) {
            BeanPropertyWriter beanPropertyWriter = beanPropertyWriterArr[i];
            if (beanPropertyWriter != null) {
                beanPropertyWriterArr2[i] = beanPropertyWriter.rename(nameTransformer);
            }
        }
        return beanPropertyWriterArr2;
    }

    public void resolve(SerializerProvider serializerProvider) throws JsonMappingException {
        int i;
        if (this._filteredProps == null) {
            i = 0;
        } else {
            i = this._filteredProps.length;
        }
        int length = this._props.length;
        for (int i2 = 0; i2 < length; i2++) {
            BeanProperty beanProperty = this._props[i2];
            if (!(beanProperty.willSuppressNulls() || beanProperty.hasNullSerializer())) {
                JsonSerializer findNullValueSerializer = serializerProvider.findNullValueSerializer(beanProperty);
                if (findNullValueSerializer != null) {
                    beanProperty.assignNullSerializer(findNullValueSerializer);
                    if (i2 < i) {
                        BeanPropertyWriter beanPropertyWriter = this._filteredProps[i2];
                        if (beanPropertyWriter != null) {
                            beanPropertyWriter.assignNullSerializer(findNullValueSerializer);
                        }
                    }
                }
            }
            if (!beanProperty.hasSerializer()) {
                JsonSerializer findConvertingSerializer = findConvertingSerializer(serializerProvider, beanProperty);
                if (findConvertingSerializer == null) {
                    JavaType serializationType = beanProperty.getSerializationType();
                    if (serializationType == null) {
                        serializationType = serializerProvider.constructType(beanProperty.getGenericPropertyType());
                        if (!serializationType.isFinal()) {
                            if (serializationType.isContainerType() || serializationType.containedTypeCount() > 0) {
                                beanProperty.setNonTrivialBaseType(serializationType);
                            }
                        }
                    }
                    findConvertingSerializer = serializerProvider.findValueSerializer(serializationType, beanProperty);
                    if (serializationType.isContainerType()) {
                        TypeSerializer typeSerializer = (TypeSerializer) serializationType.getContentType().getTypeHandler();
                        if (typeSerializer != null && (findConvertingSerializer instanceof ContainerSerializer)) {
                            findConvertingSerializer = ((ContainerSerializer) findConvertingSerializer).withValueTypeSerializer(typeSerializer);
                        }
                    }
                }
                beanProperty.assignSerializer(findConvertingSerializer);
                if (i2 < i) {
                    BeanPropertyWriter beanPropertyWriter2 = this._filteredProps[i2];
                    if (beanPropertyWriter2 != null) {
                        beanPropertyWriter2.assignSerializer(findConvertingSerializer);
                    }
                }
            }
        }
        if (this._anyGetterWriter != null) {
            this._anyGetterWriter.resolve(serializerProvider);
        }
    }

    protected JsonSerializer<Object> findConvertingSerializer(SerializerProvider serializerProvider, BeanPropertyWriter beanPropertyWriter) throws JsonMappingException {
        AnnotationIntrospector annotationIntrospector = serializerProvider.getAnnotationIntrospector();
        if (annotationIntrospector != null) {
            Object findSerializationConverter = annotationIntrospector.findSerializationConverter(beanPropertyWriter.getMember());
            if (findSerializationConverter != null) {
                Converter converterInstance = serializerProvider.converterInstance(beanPropertyWriter.getMember(), findSerializationConverter);
                JavaType outputType = converterInstance.getOutputType(serializerProvider.getTypeFactory());
                return new StdDelegatingSerializer(converterInstance, outputType, serializerProvider.findValueSerializer(outputType, (BeanProperty) beanPropertyWriter));
            }
        }
        return null;
    }

    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        ObjectIdWriter construct;
        String[] strArr;
        JsonSerializer withObjectIdWriter;
        Value findFormat;
        Shape shape = null;
        ObjectIdWriter objectIdWriter = this._objectIdWriter;
        AnnotationIntrospector annotationIntrospector = serializerProvider.getAnnotationIntrospector();
        Annotated member = (beanProperty == null || annotationIntrospector == null) ? null : beanProperty.getMember();
        if (member != null) {
            String[] findPropertiesToIgnore = annotationIntrospector.findPropertiesToIgnore(member);
            ObjectIdInfo findObjectIdInfo = annotationIntrospector.findObjectIdInfo(member);
            String[] strArr2;
            if (findObjectIdInfo != null) {
                findObjectIdInfo = annotationIntrospector.findObjectReferenceInfo(member, findObjectIdInfo);
                Type generatorType = findObjectIdInfo.getGeneratorType();
                JavaType javaType = serializerProvider.getTypeFactory().findTypeParameters(serializerProvider.constructType(generatorType), ObjectIdGenerator.class)[0];
                if (generatorType == PropertyGenerator.class) {
                    String propertyName = findObjectIdInfo.getPropertyName();
                    int length = this._props.length;
                    int i = 0;
                    while (i != length) {
                        BeanPropertyWriter beanPropertyWriter = this._props[i];
                        if (propertyName.equals(beanPropertyWriter.getName())) {
                            if (i > 0) {
                                System.arraycopy(this._props, 0, this._props, 1, i);
                                this._props[0] = beanPropertyWriter;
                                if (this._filteredProps != null) {
                                    BeanPropertyWriter beanPropertyWriter2 = this._filteredProps[i];
                                    System.arraycopy(this._filteredProps, 0, this._filteredProps, 1, i);
                                    this._filteredProps[0] = beanPropertyWriter2;
                                }
                            }
                            strArr2 = findPropertiesToIgnore;
                            construct = ObjectIdWriter.construct(beanPropertyWriter.getType(), null, new PropertyBasedObjectIdGenerator(findObjectIdInfo, beanPropertyWriter), findObjectIdInfo.getAlwaysAsId());
                            strArr = strArr2;
                        } else {
                            i++;
                        }
                    }
                    throw new IllegalArgumentException("Invalid Object Id definition for " + this._handledType.getName() + ": can not find property with name '" + propertyName + "'");
                }
                strArr2 = findPropertiesToIgnore;
                construct = ObjectIdWriter.construct(javaType, findObjectIdInfo.getPropertyName(), serializerProvider.objectIdGeneratorInstance(member, findObjectIdInfo), findObjectIdInfo.getAlwaysAsId());
                strArr = strArr2;
            } else if (objectIdWriter != null) {
                strArr2 = findPropertiesToIgnore;
                construct = this._objectIdWriter.withAlwaysAsId(annotationIntrospector.findObjectReferenceInfo(member, new ObjectIdInfo(BuildConfig.FLAVOR, null, null)).getAlwaysAsId());
                strArr = strArr2;
            } else {
                strArr2 = findPropertiesToIgnore;
                construct = objectIdWriter;
                strArr = strArr2;
            }
        } else {
            construct = objectIdWriter;
            strArr = null;
        }
        if (construct != null) {
            construct = construct.withSerializer(serializerProvider.findValueSerializer(construct.idType, beanProperty));
            if (construct != this._objectIdWriter) {
                withObjectIdWriter = withObjectIdWriter(construct);
                if (!(strArr == null || strArr.length == 0)) {
                    withObjectIdWriter = withObjectIdWriter.withIgnorals(strArr);
                }
                if (member != null) {
                    findFormat = annotationIntrospector.findFormat(member);
                    if (findFormat != null) {
                        shape = findFormat.getShape();
                    }
                }
                if (shape == null) {
                    shape = this._serializationShape;
                }
                if (shape != Shape.ARRAY) {
                    return withObjectIdWriter.asArraySerializer();
                }
                return withObjectIdWriter;
            }
        }
        withObjectIdWriter = this;
        withObjectIdWriter = withObjectIdWriter.withIgnorals(strArr);
        if (member != null) {
            findFormat = annotationIntrospector.findFormat(member);
            if (findFormat != null) {
                shape = findFormat.getShape();
            }
        }
        if (shape == null) {
            shape = this._serializationShape;
        }
        if (shape != Shape.ARRAY) {
            return withObjectIdWriter;
        }
        return withObjectIdWriter.asArraySerializer();
    }

    public boolean usesObjectId() {
        return this._objectIdWriter != null;
    }

    public void serializeWithType(Object obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException, JsonGenerationException {
        if (this._objectIdWriter != null) {
            _serializeWithObjectId(obj, jsonGenerator, serializerProvider, typeSerializer);
            return;
        }
        String _customTypeId = this._typeId == null ? null : _customTypeId(obj);
        if (_customTypeId == null) {
            typeSerializer.writeTypePrefixForObject(obj, jsonGenerator);
        } else {
            typeSerializer.writeCustomTypePrefixForObject(obj, jsonGenerator, _customTypeId);
        }
        if (this._propertyFilterId != null) {
            serializeFieldsFiltered(obj, jsonGenerator, serializerProvider);
        } else {
            serializeFields(obj, jsonGenerator, serializerProvider);
        }
        if (_customTypeId == null) {
            typeSerializer.writeTypeSuffixForObject(obj, jsonGenerator);
        } else {
            typeSerializer.writeCustomTypeSuffixForObject(obj, jsonGenerator, _customTypeId);
        }
    }

    protected final void _serializeWithObjectId(Object obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, boolean z) throws IOException, JsonGenerationException {
        ObjectIdWriter objectIdWriter = this._objectIdWriter;
        WritableObjectId findObjectId = serializerProvider.findObjectId(obj, objectIdWriter.generator);
        if (!findObjectId.writeAsId(jsonGenerator, serializerProvider, objectIdWriter)) {
            Object generateId = findObjectId.generateId(obj);
            if (objectIdWriter.alwaysAsId) {
                objectIdWriter.serializer.serialize(generateId, jsonGenerator, serializerProvider);
                return;
            }
            if (z) {
                jsonGenerator.writeStartObject();
            }
            findObjectId.writeAsField(jsonGenerator, serializerProvider, objectIdWriter);
            if (this._propertyFilterId != null) {
                serializeFieldsFiltered(obj, jsonGenerator, serializerProvider);
            } else {
                serializeFields(obj, jsonGenerator, serializerProvider);
            }
            if (z) {
                jsonGenerator.writeEndObject();
            }
        }
    }

    protected final void _serializeWithObjectId(Object obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException, JsonGenerationException {
        ObjectIdWriter objectIdWriter = this._objectIdWriter;
        WritableObjectId findObjectId = serializerProvider.findObjectId(obj, objectIdWriter.generator);
        if (!findObjectId.writeAsId(jsonGenerator, serializerProvider, objectIdWriter)) {
            Object generateId = findObjectId.generateId(obj);
            if (objectIdWriter.alwaysAsId) {
                objectIdWriter.serializer.serialize(generateId, jsonGenerator, serializerProvider);
                return;
            }
            String _customTypeId = this._typeId == null ? null : _customTypeId(obj);
            if (_customTypeId == null) {
                typeSerializer.writeTypePrefixForObject(obj, jsonGenerator);
            } else {
                typeSerializer.writeCustomTypePrefixForObject(obj, jsonGenerator, _customTypeId);
            }
            findObjectId.writeAsField(jsonGenerator, serializerProvider, objectIdWriter);
            if (this._propertyFilterId != null) {
                serializeFieldsFiltered(obj, jsonGenerator, serializerProvider);
            } else {
                serializeFields(obj, jsonGenerator, serializerProvider);
            }
            if (_customTypeId == null) {
                typeSerializer.writeTypeSuffixForObject(obj, jsonGenerator);
            } else {
                typeSerializer.writeCustomTypeSuffixForObject(obj, jsonGenerator, _customTypeId);
            }
        }
    }

    private final String _customTypeId(Object obj) {
        Object value = this._typeId.getValue(obj);
        if (value == null) {
            return BuildConfig.FLAVOR;
        }
        return value instanceof String ? (String) value : value.toString();
    }

    protected void serializeFields(Object obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        BeanPropertyWriter[] beanPropertyWriterArr;
        if (this._filteredProps == null || serializerProvider.getActiveView() == null) {
            beanPropertyWriterArr = this._props;
        } else {
            beanPropertyWriterArr = this._filteredProps;
        }
        int i = 0;
        try {
            int length = beanPropertyWriterArr.length;
            while (i < length) {
                BeanPropertyWriter beanPropertyWriter = beanPropertyWriterArr[i];
                if (beanPropertyWriter != null) {
                    beanPropertyWriter.serializeAsField(obj, jsonGenerator, serializerProvider);
                }
                i++;
            }
            if (this._anyGetterWriter != null) {
                this._anyGetterWriter.getAndSerialize(obj, jsonGenerator, serializerProvider);
            }
        } catch (Throwable e) {
            wrapAndThrow(serializerProvider, e, obj, i == beanPropertyWriterArr.length ? "[anySetter]" : beanPropertyWriterArr[i].getName());
        } catch (Throwable e2) {
            JsonMappingException jsonMappingException = new JsonMappingException("Infinite recursion (StackOverflowError)", e2);
            jsonMappingException.prependPath(new Reference(obj, i == beanPropertyWriterArr.length ? "[anySetter]" : beanPropertyWriterArr[i].getName()));
            throw jsonMappingException;
        }
    }

    protected void serializeFieldsFiltered(Object obj, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        BeanPropertyWriter[] beanPropertyWriterArr;
        if (this._filteredProps == null || serializerProvider.getActiveView() == null) {
            beanPropertyWriterArr = this._props;
        } else {
            beanPropertyWriterArr = this._filteredProps;
        }
        BeanPropertyFilter findFilter = findFilter(serializerProvider);
        if (findFilter == null) {
            serializeFields(obj, jsonGenerator, serializerProvider);
            return;
        }
        int i = 0;
        try {
            int length = beanPropertyWriterArr.length;
            while (i < length) {
                BeanPropertyWriter beanPropertyWriter = beanPropertyWriterArr[i];
                if (beanPropertyWriter != null) {
                    findFilter.serializeAsField(obj, jsonGenerator, serializerProvider, beanPropertyWriter);
                }
                i++;
            }
            if (this._anyGetterWriter != null) {
                this._anyGetterWriter.getAndSerialize(obj, jsonGenerator, serializerProvider);
            }
        } catch (Throwable e) {
            wrapAndThrow(serializerProvider, e, obj, 0 == beanPropertyWriterArr.length ? "[anySetter]" : beanPropertyWriterArr[0].getName());
        } catch (Throwable e2) {
            JsonMappingException jsonMappingException = new JsonMappingException("Infinite recursion (StackOverflowError)", e2);
            jsonMappingException.prependPath(new Reference(obj, 0 == beanPropertyWriterArr.length ? "[anySetter]" : beanPropertyWriterArr[0].getName()));
            throw jsonMappingException;
        }
    }

    protected BeanPropertyFilter findFilter(SerializerProvider serializerProvider) throws JsonMappingException {
        Object obj = this._propertyFilterId;
        FilterProvider filterProvider = serializerProvider.getFilterProvider();
        if (filterProvider != null) {
            return filterProvider.findFilter(obj);
        }
        throw new JsonMappingException("Can not resolve BeanPropertyFilter with id '" + obj + "'; no FilterProvider configured");
    }

    public JsonNode getSchema(SerializerProvider serializerProvider, Type type) throws JsonMappingException {
        BeanPropertyFilter findFilter;
        JsonNode createSchemaNode = createSchemaNode("object", true);
        JsonSerializableSchema jsonSerializableSchema = (JsonSerializableSchema) this._handledType.getAnnotation(JsonSerializableSchema.class);
        if (jsonSerializableSchema != null) {
            String id = jsonSerializableSchema.id();
            if (id != null && id.length() > 0) {
                createSchemaNode.put(DBLikedChannelsHelper.KEY_ID, id);
            }
        }
        JsonNode objectNode = createSchemaNode.objectNode();
        if (this._propertyFilterId != null) {
            findFilter = findFilter(serializerProvider);
        } else {
            findFilter = null;
        }
        for (BeanPropertyWriter beanPropertyWriter : this._props) {
            if (findFilter == null) {
                beanPropertyWriter.depositSchemaProperty(objectNode, serializerProvider);
            } else {
                findFilter.depositSchemaProperty(beanPropertyWriter, (ObjectNode) objectNode, serializerProvider);
            }
        }
        createSchemaNode.put("properties", objectNode);
        return createSchemaNode;
    }

    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
        int i = 0;
        JsonObjectFormatVisitor expectObjectFormat = jsonFormatVisitorWrapper == null ? null : jsonFormatVisitorWrapper.expectObjectFormat(javaType);
        if (expectObjectFormat == null) {
            return;
        }
        if (this._propertyFilterId != null) {
            BeanPropertyFilter findFilter = findFilter(jsonFormatVisitorWrapper.getProvider());
            while (i < this._props.length) {
                findFilter.depositSchemaProperty(this._props[i], expectObjectFormat, jsonFormatVisitorWrapper.getProvider());
                i++;
            }
            return;
        }
        while (i < this._props.length) {
            this._props[i].depositSchemaProperty(expectObjectFormat);
            i++;
        }
    }
}
