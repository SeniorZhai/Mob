package com.fasterxml.jackson.databind.deser.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import java.io.IOException;
import java.lang.annotation.Annotation;

public final class ObjectIdValueProperty extends SettableBeanProperty {
    private static final long serialVersionUID = 1;
    protected final ObjectIdReader _objectIdReader;

    @Deprecated
    public ObjectIdValueProperty(ObjectIdReader objectIdReader) {
        this(objectIdReader, true);
    }

    public ObjectIdValueProperty(ObjectIdReader objectIdReader, boolean z) {
        super(objectIdReader.propertyName, objectIdReader.idType, null, null, null, z);
        this._objectIdReader = objectIdReader;
        this._valueDeserializer = objectIdReader.deserializer;
    }

    protected ObjectIdValueProperty(ObjectIdValueProperty objectIdValueProperty, JsonDeserializer<?> jsonDeserializer) {
        super((SettableBeanProperty) objectIdValueProperty, (JsonDeserializer) jsonDeserializer);
        this._objectIdReader = objectIdValueProperty._objectIdReader;
    }

    protected ObjectIdValueProperty(ObjectIdValueProperty objectIdValueProperty, String str) {
        super((SettableBeanProperty) objectIdValueProperty, str);
        this._objectIdReader = objectIdValueProperty._objectIdReader;
    }

    public ObjectIdValueProperty withName(String str) {
        return new ObjectIdValueProperty(this, str);
    }

    public ObjectIdValueProperty withValueDeserializer(JsonDeserializer<?> jsonDeserializer) {
        return new ObjectIdValueProperty(this, (JsonDeserializer) jsonDeserializer);
    }

    public <A extends Annotation> A getAnnotation(Class<A> cls) {
        return null;
    }

    public AnnotatedMember getMember() {
        return null;
    }

    public void deserializeAndSet(JsonParser jsonParser, DeserializationContext deserializationContext, Object obj) throws IOException, JsonProcessingException {
        deserializeSetAndReturn(jsonParser, deserializationContext, obj);
    }

    public Object deserializeSetAndReturn(JsonParser jsonParser, DeserializationContext deserializationContext, Object obj) throws IOException, JsonProcessingException {
        Object deserialize = this._valueDeserializer.deserialize(jsonParser, deserializationContext);
        deserializationContext.findObjectId(deserialize, this._objectIdReader.generator).bindItem(obj);
        SettableBeanProperty settableBeanProperty = this._objectIdReader.idProperty;
        if (settableBeanProperty != null) {
            return settableBeanProperty.setAndReturn(obj, deserialize);
        }
        return obj;
    }

    public void set(Object obj, Object obj2) throws IOException {
        setAndReturn(obj, obj2);
    }

    public Object setAndReturn(Object obj, Object obj2) throws IOException {
        SettableBeanProperty settableBeanProperty = this._objectIdReader.idProperty;
        if (settableBeanProperty != null) {
            return settableBeanProperty.setAndReturn(obj, obj2);
        }
        throw new UnsupportedOperationException("Should not call set() on ObjectIdProperty that has no SettableBeanProperty");
    }
}
