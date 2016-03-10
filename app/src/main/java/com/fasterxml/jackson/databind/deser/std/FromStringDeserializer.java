package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import java.io.IOException;

public abstract class FromStringDeserializer<T> extends StdScalarDeserializer<T> {
    private static final long serialVersionUID = 1;

    protected abstract T _deserialize(String str, DeserializationContext deserializationContext) throws IOException, JsonProcessingException;

    protected FromStringDeserializer(Class<?> cls) {
        super((Class) cls);
    }

    public final T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String valueAsString = jsonParser.getValueAsString();
        if (valueAsString != null) {
            if (valueAsString.length() == 0) {
                return null;
            }
            valueAsString = valueAsString.trim();
            if (valueAsString.length() == 0) {
                return null;
            }
            try {
                T _deserialize = _deserialize(valueAsString, deserializationContext);
                if (_deserialize != null) {
                    return _deserialize;
                }
            } catch (IllegalArgumentException e) {
            }
            throw deserializationContext.weirdStringException(valueAsString, this._valueClass, "not a valid textual representation");
        } else if (jsonParser.getCurrentToken() == JsonToken.VALUE_EMBEDDED_OBJECT) {
            T embeddedObject = jsonParser.getEmbeddedObject();
            if (embeddedObject == null) {
                return null;
            }
            if (this._valueClass.isAssignableFrom(embeddedObject.getClass())) {
                return embeddedObject;
            }
            return _deserializeEmbedded(embeddedObject, deserializationContext);
        } else {
            throw deserializationContext.mappingException(this._valueClass);
        }
    }

    protected T _deserializeEmbedded(Object obj, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        throw deserializationContext.mappingException("Don't know how to convert embedded Object of type " + obj.getClass().getName() + " into " + this._valueClass.getName());
    }
}
