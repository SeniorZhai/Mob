package com.fasterxml.jackson.databind;

import java.io.Serializable;

public class PropertyName implements Serializable {
    public static final PropertyName NO_NAME = new PropertyName(new String(_NO_NAME), null);
    public static final PropertyName USE_DEFAULT = new PropertyName(_USE_DEFAULT, null);
    private static final String _NO_NAME = "#disabled";
    private static final String _USE_DEFAULT = "";
    private static final long serialVersionUID = 7930806520033045126L;
    protected final String _namespace;
    protected final String _simpleName;

    public PropertyName(String str) {
        this(str, null);
    }

    public PropertyName(String str, String str2) {
        if (str == null) {
            str = _USE_DEFAULT;
        }
        this._simpleName = str;
        this._namespace = str2;
    }

    protected Object readResolve() {
        if (this._simpleName == null || _USE_DEFAULT.equals(this._simpleName)) {
            return USE_DEFAULT;
        }
        if (this._simpleName.equals(_NO_NAME)) {
            return NO_NAME;
        }
        return this;
    }

    public static PropertyName construct(String str, String str2) {
        if (str == null) {
            str = _USE_DEFAULT;
        }
        if (str2 == null && str.length() == 0) {
            return USE_DEFAULT;
        }
        return new PropertyName(str, str2);
    }

    public PropertyName withSimpleName(String str) {
        if (str == null) {
            str = _USE_DEFAULT;
        }
        return str.equals(this._simpleName) ? this : new PropertyName(str, this._namespace);
    }

    public PropertyName withNamespace(String str) {
        if (str == null) {
            if (this._namespace == null) {
                return this;
            }
        } else if (str.equals(this._namespace)) {
            return this;
        }
        return new PropertyName(this._simpleName, str);
    }

    public String getSimpleName() {
        return this._simpleName;
    }

    public String getNamespace() {
        return this._namespace;
    }

    public boolean hasSimpleName() {
        return this._simpleName.length() > 0;
    }

    public boolean hasNamespace() {
        return this._namespace != null;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        PropertyName propertyName = (PropertyName) obj;
        if (this._simpleName == null) {
            if (propertyName._simpleName != null) {
                return false;
            }
        } else if (!this._simpleName.equals(propertyName._simpleName)) {
            return false;
        }
        if (this._namespace != null) {
            return this._namespace.equals(propertyName._namespace);
        }
        if (propertyName._namespace != null) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        if (this._namespace == null) {
            return this._simpleName.hashCode();
        }
        return this._namespace.hashCode() ^ this._simpleName.hashCode();
    }

    public String toString() {
        if (this._namespace == null) {
            return this._simpleName;
        }
        return "{" + this._namespace + "}" + this._simpleName;
    }
}
