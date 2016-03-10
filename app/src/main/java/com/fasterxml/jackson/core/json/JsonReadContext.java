package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.io.CharTypes;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import org.apache.http.message.TokenParser;

public final class JsonReadContext extends JsonStreamContext {
    protected JsonReadContext _child = null;
    protected int _columnNr;
    protected String _currentName;
    protected int _lineNr;
    protected final JsonReadContext _parent;

    public JsonReadContext(JsonReadContext jsonReadContext, int i, int i2, int i3) {
        this._type = i;
        this._parent = jsonReadContext;
        this._lineNr = i2;
        this._columnNr = i3;
        this._index = -1;
    }

    protected void reset(int i, int i2, int i3) {
        this._type = i;
        this._index = -1;
        this._lineNr = i2;
        this._columnNr = i3;
        this._currentName = null;
    }

    public static JsonReadContext createRootContext(int i, int i2) {
        return new JsonReadContext(null, 0, i, i2);
    }

    public static JsonReadContext createRootContext() {
        return new JsonReadContext(null, 0, 1, 0);
    }

    public JsonReadContext createChildArrayContext(int i, int i2) {
        JsonReadContext jsonReadContext = this._child;
        if (jsonReadContext == null) {
            jsonReadContext = new JsonReadContext(this, 1, i, i2);
            this._child = jsonReadContext;
            return jsonReadContext;
        }
        jsonReadContext.reset(1, i, i2);
        return jsonReadContext;
    }

    public JsonReadContext createChildObjectContext(int i, int i2) {
        JsonReadContext jsonReadContext = this._child;
        if (jsonReadContext == null) {
            jsonReadContext = new JsonReadContext(this, 2, i, i2);
            this._child = jsonReadContext;
            return jsonReadContext;
        }
        jsonReadContext.reset(2, i, i2);
        return jsonReadContext;
    }

    public String getCurrentName() {
        return this._currentName;
    }

    public JsonReadContext getParent() {
        return this._parent;
    }

    public JsonLocation getStartLocation(Object obj) {
        return new JsonLocation(obj, -1, this._lineNr, this._columnNr);
    }

    public boolean expectComma() {
        int i = this._index + 1;
        this._index = i;
        return this._type != 0 && i > 0;
    }

    public void setCurrentName(String str) {
        this._currentName = str;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(64);
        switch (this._type) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                stringBuilder.append("/");
                break;
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                stringBuilder.append('[');
                stringBuilder.append(getCurrentIndex());
                stringBuilder.append(']');
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                stringBuilder.append('{');
                if (this._currentName != null) {
                    stringBuilder.append(TokenParser.DQUOTE);
                    CharTypes.appendQuoted(stringBuilder, this._currentName);
                    stringBuilder.append(TokenParser.DQUOTE);
                } else {
                    stringBuilder.append('?');
                }
                stringBuilder.append('}');
                break;
        }
        return stringBuilder.toString();
    }
}
