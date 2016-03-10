package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitable;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonMapFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContainerSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap;
import com.fasterxml.jackson.databind.ser.impl.PropertySerializerMap.SerializerAndMapResult;
import com.fasterxml.jackson.databind.type.TypeFactory;
import io.fabric.sdk.android.BuildConfig;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

@JacksonStdImpl
public class MapSerializer extends ContainerSerializer<Map<?, ?>> implements ContextualSerializer {
    protected static final JavaType UNSPECIFIED_TYPE = TypeFactory.unknownType();
    protected PropertySerializerMap _dynamicValueSerializers;
    protected final HashSet<String> _ignoredEntries;
    protected JsonSerializer<Object> _keySerializer;
    protected final JavaType _keyType;
    protected final BeanProperty _property;
    protected JsonSerializer<Object> _valueSerializer;
    protected final JavaType _valueType;
    protected final boolean _valueTypeIsStatic;
    protected final TypeSerializer _valueTypeSerializer;

    protected MapSerializer(HashSet<String> hashSet, JavaType javaType, JavaType javaType2, boolean z, TypeSerializer typeSerializer, JsonSerializer<?> jsonSerializer, JsonSerializer<?> jsonSerializer2) {
        super(Map.class, false);
        this._ignoredEntries = hashSet;
        this._keyType = javaType;
        this._valueType = javaType2;
        this._valueTypeIsStatic = z;
        this._valueTypeSerializer = typeSerializer;
        this._keySerializer = jsonSerializer;
        this._valueSerializer = jsonSerializer2;
        this._dynamicValueSerializers = PropertySerializerMap.emptyMap();
        this._property = null;
    }

    protected MapSerializer(MapSerializer mapSerializer, BeanProperty beanProperty, JsonSerializer<?> jsonSerializer, JsonSerializer<?> jsonSerializer2, HashSet<String> hashSet) {
        super(Map.class, false);
        this._ignoredEntries = hashSet;
        this._keyType = mapSerializer._keyType;
        this._valueType = mapSerializer._valueType;
        this._valueTypeIsStatic = mapSerializer._valueTypeIsStatic;
        this._valueTypeSerializer = mapSerializer._valueTypeSerializer;
        this._keySerializer = jsonSerializer;
        this._valueSerializer = jsonSerializer2;
        this._dynamicValueSerializers = mapSerializer._dynamicValueSerializers;
        this._property = beanProperty;
    }

    protected MapSerializer(MapSerializer mapSerializer, TypeSerializer typeSerializer) {
        super(Map.class, false);
        this._ignoredEntries = mapSerializer._ignoredEntries;
        this._keyType = mapSerializer._keyType;
        this._valueType = mapSerializer._valueType;
        this._valueTypeIsStatic = mapSerializer._valueTypeIsStatic;
        this._valueTypeSerializer = typeSerializer;
        this._keySerializer = mapSerializer._keySerializer;
        this._valueSerializer = mapSerializer._valueSerializer;
        this._dynamicValueSerializers = mapSerializer._dynamicValueSerializers;
        this._property = mapSerializer._property;
    }

    public MapSerializer _withValueTypeSerializer(TypeSerializer typeSerializer) {
        return new MapSerializer(this, typeSerializer);
    }

    public MapSerializer withResolved(BeanProperty beanProperty, JsonSerializer<?> jsonSerializer, JsonSerializer<?> jsonSerializer2, HashSet<String> hashSet) {
        return new MapSerializer(this, beanProperty, jsonSerializer, jsonSerializer2, hashSet);
    }

    public static MapSerializer construct(String[] strArr, JavaType javaType, boolean z, TypeSerializer typeSerializer, JsonSerializer<Object> jsonSerializer, JsonSerializer<Object> jsonSerializer2) {
        JavaType javaType2;
        JavaType javaType3;
        boolean z2;
        HashSet toSet = toSet(strArr);
        if (javaType == null) {
            javaType2 = UNSPECIFIED_TYPE;
            javaType3 = javaType2;
        } else {
            javaType3 = javaType.getKeyType();
            javaType2 = javaType.getContentType();
        }
        if (z) {
            z2 = z;
        } else {
            boolean z3 = javaType2 != null && javaType2.isFinal();
            z2 = z3;
        }
        return new MapSerializer(toSet, javaType3, javaType2, z2, typeSerializer, jsonSerializer, jsonSerializer2);
    }

    private static HashSet<String> toSet(String[] strArr) {
        if (strArr == null || strArr.length == 0) {
            return null;
        }
        HashSet<String> hashSet = new HashSet(strArr.length);
        for (Object add : strArr) {
            hashSet.add(add);
        }
        return hashSet;
    }

    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty) throws JsonMappingException {
        JsonSerializer serializerInstance;
        JsonSerializer findValueSerializer;
        Collection collection;
        AnnotationIntrospector annotationIntrospector;
        String[] findPropertiesToIgnore;
        HashSet hashSet;
        JsonSerializer jsonSerializer = null;
        if (beanProperty != null) {
            Annotated member = beanProperty.getMember();
            if (member != null) {
                AnnotationIntrospector annotationIntrospector2 = serializerProvider.getAnnotationIntrospector();
                Object findKeySerializer = annotationIntrospector2.findKeySerializer(member);
                if (findKeySerializer != null) {
                    serializerInstance = serializerProvider.serializerInstance(member, findKeySerializer);
                } else {
                    serializerInstance = null;
                }
                Object findContentSerializer = annotationIntrospector2.findContentSerializer(member);
                JsonSerializer jsonSerializer2;
                if (findContentSerializer != null) {
                    jsonSerializer2 = serializerInstance;
                    serializerInstance = serializerProvider.serializerInstance(member, findContentSerializer);
                    jsonSerializer = jsonSerializer2;
                } else {
                    jsonSerializer2 = serializerInstance;
                    serializerInstance = null;
                    jsonSerializer = jsonSerializer2;
                }
                if (serializerInstance == null) {
                    serializerInstance = this._valueSerializer;
                }
                serializerInstance = findConvertingContentSerializer(serializerProvider, beanProperty, serializerInstance);
                if (serializerInstance != null) {
                    if (this._valueTypeIsStatic || hasContentTypeAnnotation(serializerProvider, beanProperty)) {
                        findValueSerializer = serializerProvider.findValueSerializer(this._valueType, beanProperty);
                    }
                    findValueSerializer = serializerInstance;
                } else {
                    if (serializerInstance instanceof ContextualSerializer) {
                        findValueSerializer = ((ContextualSerializer) serializerInstance).createContextual(serializerProvider, beanProperty);
                    }
                    findValueSerializer = serializerInstance;
                }
                if (jsonSerializer != null) {
                    serializerInstance = this._keySerializer;
                } else {
                    serializerInstance = jsonSerializer;
                }
                if (serializerInstance == null) {
                    serializerInstance = serializerProvider.findKeySerializer(this._keyType, beanProperty);
                } else if (serializerInstance instanceof ContextualSerializer) {
                    serializerInstance = ((ContextualSerializer) serializerInstance).createContextual(serializerProvider, beanProperty);
                }
                collection = this._ignoredEntries;
                annotationIntrospector = serializerProvider.getAnnotationIntrospector();
                if (!(annotationIntrospector == null || beanProperty == null)) {
                    findPropertiesToIgnore = annotationIntrospector.findPropertiesToIgnore(beanProperty.getMember());
                    if (findPropertiesToIgnore != null) {
                        hashSet = collection != null ? new HashSet() : new HashSet(collection);
                        for (Object add : findPropertiesToIgnore) {
                            hashSet.add(add);
                        }
                        return withResolved(beanProperty, serializerInstance, findValueSerializer, hashSet);
                    }
                }
                hashSet = collection;
                return withResolved(beanProperty, serializerInstance, findValueSerializer, hashSet);
            }
        }
        serializerInstance = null;
        if (serializerInstance == null) {
            serializerInstance = this._valueSerializer;
        }
        serializerInstance = findConvertingContentSerializer(serializerProvider, beanProperty, serializerInstance);
        if (serializerInstance != null) {
            if (serializerInstance instanceof ContextualSerializer) {
                findValueSerializer = ((ContextualSerializer) serializerInstance).createContextual(serializerProvider, beanProperty);
            }
            findValueSerializer = serializerInstance;
        } else {
            findValueSerializer = serializerProvider.findValueSerializer(this._valueType, beanProperty);
        }
        if (jsonSerializer != null) {
            serializerInstance = jsonSerializer;
        } else {
            serializerInstance = this._keySerializer;
        }
        if (serializerInstance == null) {
            serializerInstance = serializerProvider.findKeySerializer(this._keyType, beanProperty);
        } else if (serializerInstance instanceof ContextualSerializer) {
            serializerInstance = ((ContextualSerializer) serializerInstance).createContextual(serializerProvider, beanProperty);
        }
        collection = this._ignoredEntries;
        annotationIntrospector = serializerProvider.getAnnotationIntrospector();
        findPropertiesToIgnore = annotationIntrospector.findPropertiesToIgnore(beanProperty.getMember());
        if (findPropertiesToIgnore != null) {
            if (collection != null) {
            }
            while (r2 < r5) {
                hashSet.add(add);
            }
            return withResolved(beanProperty, serializerInstance, findValueSerializer, hashSet);
        }
        hashSet = collection;
        return withResolved(beanProperty, serializerInstance, findValueSerializer, hashSet);
    }

    public JavaType getContentType() {
        return this._valueType;
    }

    public JsonSerializer<?> getContentSerializer() {
        return this._valueSerializer;
    }

    public boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public boolean hasSingleElement(Map<?, ?> map) {
        return map.size() == 1;
    }

    public JsonSerializer<?> getKeySerializer() {
        return this._keySerializer;
    }

    public void serialize(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        jsonGenerator.writeStartObject();
        if (!map.isEmpty()) {
            if (serializerProvider.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)) {
                map = _orderEntries(map);
            }
            if (this._valueSerializer != null) {
                serializeFieldsUsing(map, jsonGenerator, serializerProvider, this._valueSerializer);
            } else {
                serializeFields(map, jsonGenerator, serializerProvider);
            }
        }
        jsonGenerator.writeEndObject();
    }

    public void serializeWithType(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, TypeSerializer typeSerializer) throws IOException, JsonGenerationException {
        typeSerializer.writeTypePrefixForObject(map, jsonGenerator);
        if (!map.isEmpty()) {
            if (serializerProvider.isEnabled(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)) {
                map = _orderEntries(map);
            }
            if (this._valueSerializer != null) {
                serializeFieldsUsing(map, jsonGenerator, serializerProvider, this._valueSerializer);
            } else {
                serializeFields(map, jsonGenerator, serializerProvider);
            }
        }
        typeSerializer.writeTypeSuffixForObject(map, jsonGenerator);
    }

    public void serializeFields(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        if (this._valueTypeSerializer != null) {
            serializeTypedFields(map, jsonGenerator, serializerProvider);
            return;
        }
        Object obj;
        JsonSerializer jsonSerializer = this._keySerializer;
        HashSet hashSet = this._ignoredEntries;
        if (serializerProvider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES)) {
            obj = null;
        } else {
            obj = 1;
        }
        PropertySerializerMap propertySerializerMap = this._dynamicValueSerializers;
        PropertySerializerMap propertySerializerMap2 = propertySerializerMap;
        for (Entry entry : map.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();
            if (key == null) {
                serializerProvider.findNullKeySerializer(this._keyType, this._property).serialize(null, jsonGenerator, serializerProvider);
            } else if ((obj == null || value != null) && (hashSet == null || !hashSet.contains(key))) {
                jsonSerializer.serialize(key, jsonGenerator, serializerProvider);
            }
            if (value == null) {
                serializerProvider.defaultSerializeNull(jsonGenerator);
                propertySerializerMap = propertySerializerMap2;
            } else {
                JsonSerializer jsonSerializer2;
                Class cls = value.getClass();
                JsonSerializer serializerFor = propertySerializerMap2.serializerFor(cls);
                JsonSerializer jsonSerializer3;
                if (serializerFor == null) {
                    if (this._valueType.hasGenericTypes()) {
                        serializerFor = _findAndAddDynamic(propertySerializerMap2, serializerProvider.constructSpecializedType(this._valueType, cls), serializerProvider);
                    } else {
                        serializerFor = _findAndAddDynamic(propertySerializerMap2, cls, serializerProvider);
                    }
                    jsonSerializer3 = serializerFor;
                    propertySerializerMap = this._dynamicValueSerializers;
                    jsonSerializer2 = jsonSerializer3;
                } else {
                    jsonSerializer3 = serializerFor;
                    propertySerializerMap = propertySerializerMap2;
                    jsonSerializer2 = jsonSerializer3;
                }
                try {
                    jsonSerializer2.serialize(value, jsonGenerator, serializerProvider);
                } catch (Throwable e) {
                    wrapAndThrow(serializerProvider, e, (Object) map, BuildConfig.FLAVOR + key);
                }
            }
            propertySerializerMap2 = propertySerializerMap;
        }
    }

    protected void serializeFieldsUsing(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider, JsonSerializer<Object> jsonSerializer) throws IOException, JsonGenerationException {
        JsonSerializer jsonSerializer2 = this._keySerializer;
        HashSet hashSet = this._ignoredEntries;
        TypeSerializer typeSerializer = this._valueTypeSerializer;
        Object obj = !serializerProvider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES) ? 1 : null;
        for (Entry entry : map.entrySet()) {
            Object value = entry.getValue();
            Object key = entry.getKey();
            if (key == null) {
                serializerProvider.findNullKeySerializer(this._keyType, this._property).serialize(null, jsonGenerator, serializerProvider);
            } else if ((obj == null || value != null) && (hashSet == null || !hashSet.contains(key))) {
                jsonSerializer2.serialize(key, jsonGenerator, serializerProvider);
            }
            if (value == null) {
                serializerProvider.defaultSerializeNull(jsonGenerator);
            } else if (typeSerializer == null) {
                try {
                    jsonSerializer.serialize(value, jsonGenerator, serializerProvider);
                } catch (Throwable e) {
                    wrapAndThrow(serializerProvider, e, (Object) map, BuildConfig.FLAVOR + key);
                }
            } else {
                jsonSerializer.serializeWithType(value, jsonGenerator, serializerProvider, typeSerializer);
            }
        }
    }

    protected void serializeTypedFields(Map<?, ?> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        JsonSerializer jsonSerializer = this._keySerializer;
        HashSet hashSet = this._ignoredEntries;
        Object obj = !serializerProvider.isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES) ? 1 : null;
        Object obj2 = null;
        JsonSerializer jsonSerializer2 = null;
        for (Entry entry : map.entrySet()) {
            Class cls;
            JsonSerializer jsonSerializer3;
            Object value = entry.getValue();
            Object key = entry.getKey();
            if (key == null) {
                serializerProvider.findNullKeySerializer(this._keyType, this._property).serialize(null, jsonGenerator, serializerProvider);
            } else if ((obj == null || value != null) && (hashSet == null || !hashSet.contains(key))) {
                jsonSerializer.serialize(key, jsonGenerator, serializerProvider);
            }
            if (value == null) {
                serializerProvider.defaultSerializeNull(jsonGenerator);
                cls = obj2;
                jsonSerializer3 = jsonSerializer2;
            } else {
                cls = value.getClass();
                if (cls == obj2) {
                    cls = obj2;
                    jsonSerializer3 = jsonSerializer2;
                } else {
                    jsonSerializer2 = serializerProvider.findValueSerializer(cls, this._property);
                    jsonSerializer3 = jsonSerializer2;
                }
                try {
                    jsonSerializer2.serializeWithType(value, jsonGenerator, serializerProvider, this._valueTypeSerializer);
                } catch (Throwable e) {
                    wrapAndThrow(serializerProvider, e, (Object) map, BuildConfig.FLAVOR + key);
                }
            }
            jsonSerializer2 = jsonSerializer3;
            obj2 = cls;
        }
    }

    public JsonNode getSchema(SerializerProvider serializerProvider, Type type) {
        return createSchemaNode("object", true);
    }

    public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper jsonFormatVisitorWrapper, JavaType javaType) throws JsonMappingException {
        JsonMapFormatVisitor expectMapFormat = jsonFormatVisitorWrapper == null ? null : jsonFormatVisitorWrapper.expectMapFormat(javaType);
        if (expectMapFormat != null) {
            expectMapFormat.keyFormat(this._keySerializer, this._keyType);
            JsonFormatVisitable jsonFormatVisitable = this._valueSerializer;
            if (jsonFormatVisitable == null) {
                jsonFormatVisitable = _findAndAddDynamic(this._dynamicValueSerializers, this._valueType, jsonFormatVisitorWrapper.getProvider());
            }
            expectMapFormat.valueFormat(jsonFormatVisitable, this._valueType);
        }
    }

    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap propertySerializerMap, Class<?> cls, SerializerProvider serializerProvider) throws JsonMappingException {
        SerializerAndMapResult findAndAddSerializer = propertySerializerMap.findAndAddSerializer((Class) cls, serializerProvider, this._property);
        if (propertySerializerMap != findAndAddSerializer.map) {
            this._dynamicValueSerializers = findAndAddSerializer.map;
        }
        return findAndAddSerializer.serializer;
    }

    protected final JsonSerializer<Object> _findAndAddDynamic(PropertySerializerMap propertySerializerMap, JavaType javaType, SerializerProvider serializerProvider) throws JsonMappingException {
        SerializerAndMapResult findAndAddSerializer = propertySerializerMap.findAndAddSerializer(javaType, serializerProvider, this._property);
        if (propertySerializerMap != findAndAddSerializer.map) {
            this._dynamicValueSerializers = findAndAddSerializer.map;
        }
        return findAndAddSerializer.serializer;
    }

    protected Map<?, ?> _orderEntries(Map<?, ?> map) {
        return map instanceof SortedMap ? map : new TreeMap(map);
    }
}
