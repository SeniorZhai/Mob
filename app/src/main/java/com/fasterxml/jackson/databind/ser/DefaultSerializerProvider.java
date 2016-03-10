package com.fasterxml.jackson.databind.ser;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.JsonSerializer.None;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.IdentityHashMap;

public abstract class DefaultSerializerProvider extends SerializerProvider implements Serializable {
    private static final long serialVersionUID = 1;
    protected transient ArrayList<ObjectIdGenerator<?>> _objectIdGenerators;
    protected transient IdentityHashMap<Object, WritableObjectId> _seenObjectIds;

    public static final class Impl extends DefaultSerializerProvider {
        private static final long serialVersionUID = 1;

        protected Impl(SerializerProvider serializerProvider, SerializationConfig serializationConfig, SerializerFactory serializerFactory) {
            super(serializerProvider, serializationConfig, serializerFactory);
        }

        public Impl createInstance(SerializationConfig serializationConfig, SerializerFactory serializerFactory) {
            return new Impl(this, serializationConfig, serializerFactory);
        }
    }

    public abstract DefaultSerializerProvider createInstance(SerializationConfig serializationConfig, SerializerFactory serializerFactory);

    protected DefaultSerializerProvider() {
    }

    protected DefaultSerializerProvider(SerializerProvider serializerProvider, SerializationConfig serializationConfig, SerializerFactory serializerFactory) {
        super(serializerProvider, serializationConfig, serializerFactory);
    }

    public void serializeValue(JsonGenerator jsonGenerator, Object obj) throws IOException, JsonGenerationException {
        JsonSerializer defaultNullValueSerializer;
        boolean z = false;
        if (obj == null) {
            defaultNullValueSerializer = getDefaultNullValueSerializer();
        } else {
            JsonSerializer findTypedValueSerializer = findTypedValueSerializer(obj.getClass(), true, null);
            String rootName = this._config.getRootName();
            if (rootName == null) {
                z = this._config.isEnabled(SerializationFeature.WRAP_ROOT_VALUE);
                if (z) {
                    jsonGenerator.writeStartObject();
                    jsonGenerator.writeFieldName(this._rootNames.findRootName(obj.getClass(), this._config));
                    defaultNullValueSerializer = findTypedValueSerializer;
                } else {
                    defaultNullValueSerializer = findTypedValueSerializer;
                }
            } else if (rootName.length() == 0) {
                defaultNullValueSerializer = findTypedValueSerializer;
            } else {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName(rootName);
                z = true;
                defaultNullValueSerializer = findTypedValueSerializer;
            }
        }
        try {
            defaultNullValueSerializer.serialize(obj, jsonGenerator, this);
            if (z) {
                jsonGenerator.writeEndObject();
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e2) {
            Throwable th = e2;
            String message = th.getMessage();
            if (message == null) {
                message = "[no message for " + th.getClass().getName() + "]";
            }
            throw new JsonMappingException(message, th);
        }
    }

    public void serializeValue(JsonGenerator jsonGenerator, Object obj, JavaType javaType) throws IOException, JsonGenerationException {
        JsonSerializer defaultNullValueSerializer;
        boolean z;
        if (obj == null) {
            defaultNullValueSerializer = getDefaultNullValueSerializer();
            z = false;
        } else {
            if (!javaType.getRawClass().isAssignableFrom(obj.getClass())) {
                _reportIncompatibleRootType(obj, javaType);
            }
            defaultNullValueSerializer = findTypedValueSerializer(javaType, true, null);
            z = this._config.isEnabled(SerializationFeature.WRAP_ROOT_VALUE);
            if (z) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName(this._rootNames.findRootName(javaType, this._config));
            }
        }
        try {
            defaultNullValueSerializer.serialize(obj, jsonGenerator, this);
            if (z) {
                jsonGenerator.writeEndObject();
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e2) {
            Throwable th = e2;
            String message = th.getMessage();
            if (message == null) {
                message = "[no message for " + th.getClass().getName() + "]";
            }
            throw new JsonMappingException(message, th);
        }
    }

    public void serializeValue(JsonGenerator jsonGenerator, Object obj, JavaType javaType, JsonSerializer<Object> jsonSerializer) throws IOException, JsonGenerationException {
        boolean z;
        if (obj == null) {
            jsonSerializer = getDefaultNullValueSerializer();
            z = false;
        } else {
            if (!(javaType == null || javaType.getRawClass().isAssignableFrom(obj.getClass()))) {
                _reportIncompatibleRootType(obj, javaType);
            }
            if (jsonSerializer == null) {
                jsonSerializer = findTypedValueSerializer(javaType, true, null);
            }
            z = this._config.isEnabled(SerializationFeature.WRAP_ROOT_VALUE);
            if (z) {
                jsonGenerator.writeStartObject();
                jsonGenerator.writeFieldName(this._rootNames.findRootName(javaType, this._config));
            }
        }
        try {
            jsonSerializer.serialize(obj, jsonGenerator, this);
            if (z) {
                jsonGenerator.writeEndObject();
            }
        } catch (IOException e) {
            throw e;
        } catch (Throwable e2) {
            Throwable th = e2;
            String message = th.getMessage();
            if (message == null) {
                message = "[no message for " + th.getClass().getName() + "]";
            }
            throw new JsonMappingException(message, th);
        }
    }

    public JsonSchema generateJsonSchema(Class<?> cls) throws JsonMappingException {
        if (cls == null) {
            throw new IllegalArgumentException("A class must be provided");
        }
        JsonSerializer findValueSerializer = findValueSerializer((Class) cls, null);
        JsonNode schema = findValueSerializer instanceof SchemaAware ? ((SchemaAware) findValueSerializer).getSchema(this, null) : JsonSchema.getDefaultSchemaNode();
        if (schema instanceof ObjectNode) {
            return new JsonSchema((ObjectNode) schema);
        }
        throw new IllegalArgumentException("Class " + cls.getName() + " would not be serialized as a JSON object and therefore has no schema");
    }

    public void acceptJsonFormatVisitor(JavaType javaType, JsonFormatVisitorWrapper jsonFormatVisitorWrapper) throws JsonMappingException {
        if (javaType == null) {
            throw new IllegalArgumentException("A class must be provided");
        }
        jsonFormatVisitorWrapper.setProvider(this);
        findValueSerializer(javaType, null).acceptJsonFormatVisitor(jsonFormatVisitorWrapper, javaType);
    }

    public boolean hasSerializerFor(Class<?> cls) {
        try {
            return _findExplicitUntypedSerializer(cls) != null;
        } catch (JsonMappingException e) {
            return false;
        }
    }

    public int cachedSerializersCount() {
        return this._serializerCache.size();
    }

    public void flushCachedSerializers() {
        this._serializerCache.flush();
    }

    public WritableObjectId findObjectId(Object obj, ObjectIdGenerator<?> objectIdGenerator) {
        ObjectIdGenerator objectIdGenerator2;
        if (this._seenObjectIds == null) {
            this._seenObjectIds = new IdentityHashMap();
        } else {
            WritableObjectId writableObjectId = (WritableObjectId) this._seenObjectIds.get(obj);
            if (writableObjectId != null) {
                return writableObjectId;
            }
        }
        if (this._objectIdGenerators == null) {
            this._objectIdGenerators = new ArrayList(8);
            objectIdGenerator2 = null;
        } else {
            int size = this._objectIdGenerators.size();
            for (int i = 0; i < size; i++) {
                objectIdGenerator2 = (ObjectIdGenerator) this._objectIdGenerators.get(i);
                if (objectIdGenerator2.canUseFor(objectIdGenerator)) {
                    break;
                }
            }
            objectIdGenerator2 = null;
        }
        if (objectIdGenerator2 == null) {
            objectIdGenerator2 = objectIdGenerator.newForSerialization(this);
            this._objectIdGenerators.add(objectIdGenerator2);
        }
        WritableObjectId writableObjectId2 = new WritableObjectId(objectIdGenerator2);
        this._seenObjectIds.put(obj, writableObjectId2);
        return writableObjectId2;
    }

    public JsonSerializer<Object> serializerInstance(Annotated annotated, Object obj) throws JsonMappingException {
        JsonSerializer<Object> jsonSerializer = null;
        if (obj == null) {
            return null;
        }
        if (obj instanceof JsonSerializer) {
            obj = (JsonSerializer) obj;
        } else if (obj instanceof Class) {
            Class cls = (Class) obj;
            if (cls == None.class || cls == NoClass.class) {
                return null;
            }
            if (JsonSerializer.class.isAssignableFrom(cls)) {
                HandlerInstantiator handlerInstantiator = this._config.getHandlerInstantiator();
                if (handlerInstantiator != null) {
                    jsonSerializer = handlerInstantiator.serializerInstance(this._config, annotated, cls);
                }
                if (jsonSerializer == null) {
                    JsonSerializer jsonSerializer2 = (JsonSerializer) ClassUtil.createInstance(cls, this._config.canOverrideAccessModifiers());
                } else {
                    JsonSerializer<Object> jsonSerializer3 = jsonSerializer;
                }
            } else {
                throw new IllegalStateException("AnnotationIntrospector returned Class " + cls.getName() + "; expected Class<JsonSerializer>");
            }
        } else {
            throw new IllegalStateException("AnnotationIntrospector returned serializer definition of type " + obj.getClass().getName() + "; expected type JsonSerializer or Class<JsonSerializer> instead");
        }
        return _handleResolvable(obj);
    }
}
