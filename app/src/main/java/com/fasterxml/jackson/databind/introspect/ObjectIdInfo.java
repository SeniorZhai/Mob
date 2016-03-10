package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

public class ObjectIdInfo {
    protected final boolean _alwaysAsId;
    protected final Class<? extends ObjectIdGenerator<?>> _generator;
    protected final String _propertyName;
    protected final Class<?> _scope;

    public ObjectIdInfo(String str, Class<?> cls, Class<? extends ObjectIdGenerator<?>> cls2) {
        this(str, cls, cls2, false);
    }

    protected ObjectIdInfo(String str, Class<?> cls, Class<? extends ObjectIdGenerator<?>> cls2, boolean z) {
        this._propertyName = str;
        this._scope = cls;
        this._generator = cls2;
        this._alwaysAsId = z;
    }

    public ObjectIdInfo withAlwaysAsId(boolean z) {
        return this._alwaysAsId == z ? this : new ObjectIdInfo(this._propertyName, this._scope, this._generator, z);
    }

    public String getPropertyName() {
        return this._propertyName;
    }

    public Class<?> getScope() {
        return this._scope;
    }

    public Class<? extends ObjectIdGenerator<?>> getGeneratorType() {
        return this._generator;
    }

    public boolean getAlwaysAsId() {
        return this._alwaysAsId;
    }

    public String toString() {
        return "ObjectIdInfo: propName=" + this._propertyName + ", scope=" + (this._scope == null ? "null" : this._scope.getName()) + ", generatorType=" + (this._generator == null ? "null" : this._generator.getName()) + ", alwaysAsId=" + this._alwaysAsId;
    }
}
