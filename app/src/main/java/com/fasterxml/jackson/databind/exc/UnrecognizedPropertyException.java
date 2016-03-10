package com.fasterxml.jackson.databind.exc;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.util.Collection;
import java.util.Collections;
import org.apache.http.message.TokenParser;

public class UnrecognizedPropertyException extends JsonMappingException {
    private static final int MAX_DESC_LENGTH = 200;
    private static final long serialVersionUID = 1;
    protected transient String _propertiesAsString;
    protected final Collection<Object> _propertyIds;
    protected final Class<?> _referringClass;
    protected final String _unrecognizedPropertyName;

    public UnrecognizedPropertyException(String str, JsonLocation jsonLocation, Class<?> cls, String str2, Collection<Object> collection) {
        super(str, jsonLocation);
        this._referringClass = cls;
        this._unrecognizedPropertyName = str2;
        this._propertyIds = collection;
    }

    public static UnrecognizedPropertyException from(JsonParser jsonParser, Object obj, String str, Collection<Object> collection) {
        if (obj == null) {
            throw new IllegalArgumentException();
        }
        Class cls;
        if (obj instanceof Class) {
            cls = (Class) obj;
        } else {
            cls = obj.getClass();
        }
        UnrecognizedPropertyException unrecognizedPropertyException = new UnrecognizedPropertyException("Unrecognized field \"" + str + "\" (class " + cls.getName() + "), not marked as ignorable", jsonParser.getCurrentLocation(), cls, str, collection);
        unrecognizedPropertyException.prependPath(obj, str);
        return unrecognizedPropertyException;
    }

    public String getMessageSuffix() {
        String str = this._propertiesAsString;
        if (str != null || this._propertyIds == null) {
            return str;
        }
        StringBuilder stringBuilder = new StringBuilder(100);
        int size = this._propertyIds.size();
        if (size != 1) {
            stringBuilder.append(" (").append(size).append(" known properties: ");
            for (Object valueOf : this._propertyIds) {
                stringBuilder.append(", \"");
                stringBuilder.append(String.valueOf(valueOf));
                stringBuilder.append(TokenParser.DQUOTE);
                if (stringBuilder.length() > MAX_DESC_LENGTH) {
                    stringBuilder.append(" [truncated]");
                    break;
                }
            }
        }
        stringBuilder.append(" (one known property: \"");
        stringBuilder.append(String.valueOf(this._propertyIds.iterator().next()));
        stringBuilder.append(TokenParser.DQUOTE);
        stringBuilder.append("])");
        str = stringBuilder.toString();
        this._propertiesAsString = str;
        return str;
    }

    public Class<?> getReferringClass() {
        return this._referringClass;
    }

    public String getUnrecognizedPropertyName() {
        return this._unrecognizedPropertyName;
    }

    public Collection<Object> getKnownPropertyIds() {
        if (this._propertyIds == null) {
            return null;
        }
        return Collections.unmodifiableCollection(this._propertyIds);
    }
}
