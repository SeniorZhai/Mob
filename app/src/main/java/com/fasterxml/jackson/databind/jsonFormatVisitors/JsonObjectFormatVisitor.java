package com.fasterxml.jackson.databind.jsonFormatVisitors;

import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;

public interface JsonObjectFormatVisitor extends JsonFormatVisitorWithSerializerProvider {

    public static class Base implements JsonObjectFormatVisitor {
        protected SerializerProvider _provider;

        public Base(SerializerProvider serializerProvider) {
            this._provider = serializerProvider;
        }

        public SerializerProvider getProvider() {
            return this._provider;
        }

        public void setProvider(SerializerProvider serializerProvider) {
            this._provider = serializerProvider;
        }

        public void property(BeanProperty beanProperty) throws JsonMappingException {
        }

        public void property(String str, JsonFormatVisitable jsonFormatVisitable, JavaType javaType) throws JsonMappingException {
        }

        @Deprecated
        public void property(String str) throws JsonMappingException {
        }

        public void optionalProperty(BeanProperty beanProperty) throws JsonMappingException {
        }

        public void optionalProperty(String str, JsonFormatVisitable jsonFormatVisitable, JavaType javaType) throws JsonMappingException {
        }

        @Deprecated
        public void optionalProperty(String str) throws JsonMappingException {
        }
    }

    void optionalProperty(BeanProperty beanProperty) throws JsonMappingException;

    @Deprecated
    void optionalProperty(String str) throws JsonMappingException;

    void optionalProperty(String str, JsonFormatVisitable jsonFormatVisitable, JavaType javaType) throws JsonMappingException;

    void property(BeanProperty beanProperty) throws JsonMappingException;

    @Deprecated
    void property(String str) throws JsonMappingException;

    void property(String str, JsonFormatVisitable jsonFormatVisitable, JavaType javaType) throws JsonMappingException;
}
