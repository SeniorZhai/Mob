package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonStreamContext;
import org.apache.http.message.TokenParser;

public class JsonWriteContext extends JsonStreamContext {
    public static final int STATUS_EXPECT_NAME = 5;
    public static final int STATUS_EXPECT_VALUE = 4;
    public static final int STATUS_OK_AFTER_COLON = 2;
    public static final int STATUS_OK_AFTER_COMMA = 1;
    public static final int STATUS_OK_AFTER_SPACE = 3;
    public static final int STATUS_OK_AS_IS = 0;
    protected JsonWriteContext _child = null;
    protected String _currentName;
    protected final JsonWriteContext _parent;

    protected JsonWriteContext(int i, JsonWriteContext jsonWriteContext) {
        this._type = i;
        this._parent = jsonWriteContext;
        this._index = -1;
    }

    public static JsonWriteContext createRootContext() {
        return new JsonWriteContext(0, null);
    }

    private JsonWriteContext reset(int i) {
        this._type = i;
        this._index = -1;
        this._currentName = null;
        return this;
    }

    public final JsonWriteContext createChildArrayContext() {
        JsonWriteContext jsonWriteContext = this._child;
        if (jsonWriteContext != null) {
            return jsonWriteContext.reset(STATUS_OK_AFTER_COMMA);
        }
        jsonWriteContext = new JsonWriteContext(STATUS_OK_AFTER_COMMA, this);
        this._child = jsonWriteContext;
        return jsonWriteContext;
    }

    public final JsonWriteContext createChildObjectContext() {
        JsonWriteContext jsonWriteContext = this._child;
        if (jsonWriteContext != null) {
            return jsonWriteContext.reset(STATUS_OK_AFTER_COLON);
        }
        jsonWriteContext = new JsonWriteContext(STATUS_OK_AFTER_COLON, this);
        this._child = jsonWriteContext;
        return jsonWriteContext;
    }

    public final JsonWriteContext getParent() {
        return this._parent;
    }

    public final String getCurrentName() {
        return this._currentName;
    }

    public final int writeFieldName(String str) {
        if (this._type != STATUS_OK_AFTER_COLON || this._currentName != null) {
            return STATUS_EXPECT_VALUE;
        }
        this._currentName = str;
        return this._index < 0 ? 0 : STATUS_OK_AFTER_COMMA;
    }

    public final int writeValue() {
        if (this._type == STATUS_OK_AFTER_COLON) {
            if (this._currentName == null) {
                return STATUS_EXPECT_NAME;
            }
            this._currentName = null;
            this._index += STATUS_OK_AFTER_COMMA;
            return STATUS_OK_AFTER_COLON;
        } else if (this._type == STATUS_OK_AFTER_COMMA) {
            int i = this._index;
            this._index += STATUS_OK_AFTER_COMMA;
            return i >= 0 ? STATUS_OK_AFTER_COMMA : 0;
        } else {
            this._index += STATUS_OK_AFTER_COMMA;
            if (this._index != 0) {
                return STATUS_OK_AFTER_SPACE;
            }
            return 0;
        }
    }

    protected final void appendDesc(StringBuilder stringBuilder) {
        if (this._type == STATUS_OK_AFTER_COLON) {
            stringBuilder.append('{');
            if (this._currentName != null) {
                stringBuilder.append(TokenParser.DQUOTE);
                stringBuilder.append(this._currentName);
                stringBuilder.append(TokenParser.DQUOTE);
            } else {
                stringBuilder.append('?');
            }
            stringBuilder.append('}');
        } else if (this._type == STATUS_OK_AFTER_COMMA) {
            stringBuilder.append('[');
            stringBuilder.append(getCurrentIndex());
            stringBuilder.append(']');
        } else {
            stringBuilder.append("/");
        }
    }

    public final String toString() {
        StringBuilder stringBuilder = new StringBuilder(64);
        appendDesc(stringBuilder);
        return stringBuilder.toString();
    }
}
