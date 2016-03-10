package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.type.ClassKey;
import java.io.Serializable;
import org.apache.http.HttpStatus;

public class RootNameLookup implements Serializable {
    private static final long serialVersionUID = 1;
    protected LRUMap<ClassKey, SerializedString> _rootNames;

    public SerializedString findRootName(JavaType javaType, MapperConfig<?> mapperConfig) {
        return findRootName(javaType.getRawClass(), (MapperConfig) mapperConfig);
    }

    public synchronized SerializedString findRootName(Class<?> cls, MapperConfig<?> mapperConfig) {
        SerializedString serializedString;
        String simpleName;
        ClassKey classKey = new ClassKey(cls);
        if (this._rootNames == null) {
            this._rootNames = new LRUMap(20, HttpStatus.SC_OK);
        } else {
            serializedString = (SerializedString) this._rootNames.get(classKey);
            if (serializedString != null) {
            }
        }
        PropertyName findRootName = mapperConfig.getAnnotationIntrospector().findRootName(mapperConfig.introspectClassAnnotations((Class) cls).getClassInfo());
        if (findRootName == null || !findRootName.hasSimpleName()) {
            simpleName = cls.getSimpleName();
        } else {
            simpleName = findRootName.getSimpleName();
        }
        serializedString = new SerializedString(simpleName);
        this._rootNames.put(classKey, serializedString);
        return serializedString;
    }
}
