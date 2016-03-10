package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class SimpleBeanPropertyDefinition extends BeanPropertyDefinition {
    protected final AnnotationIntrospector _introspector;
    protected final AnnotatedMember _member;
    protected final String _name;

    @Deprecated
    public SimpleBeanPropertyDefinition(AnnotatedMember annotatedMember) {
        this(annotatedMember, annotatedMember.getName(), null);
    }

    @Deprecated
    public SimpleBeanPropertyDefinition(AnnotatedMember annotatedMember, String str) {
        this(annotatedMember, str, null);
    }

    private SimpleBeanPropertyDefinition(AnnotatedMember annotatedMember, String str, AnnotationIntrospector annotationIntrospector) {
        this._introspector = annotationIntrospector;
        this._member = annotatedMember;
        this._name = str;
    }

    public static SimpleBeanPropertyDefinition construct(MapperConfig<?> mapperConfig, AnnotatedMember annotatedMember) {
        return new SimpleBeanPropertyDefinition(annotatedMember, annotatedMember.getName(), mapperConfig == null ? null : mapperConfig.getAnnotationIntrospector());
    }

    public static SimpleBeanPropertyDefinition construct(MapperConfig<?> mapperConfig, AnnotatedMember annotatedMember, String str) {
        return new SimpleBeanPropertyDefinition(annotatedMember, str, mapperConfig == null ? null : mapperConfig.getAnnotationIntrospector());
    }

    public SimpleBeanPropertyDefinition withName(String str) {
        return this._name.equals(str) ? this : new SimpleBeanPropertyDefinition(this._member, str, this._introspector);
    }

    public String getName() {
        return this._name;
    }

    public String getInternalName() {
        return getName();
    }

    public PropertyName getWrapperName() {
        return this._introspector == null ? null : this._introspector.findWrapperName(this._member);
    }

    public boolean isExplicitlyIncluded() {
        return false;
    }

    public boolean hasGetter() {
        return getGetter() != null;
    }

    public boolean hasSetter() {
        return getSetter() != null;
    }

    public boolean hasField() {
        return this._member instanceof AnnotatedField;
    }

    public boolean hasConstructorParameter() {
        return this._member instanceof AnnotatedParameter;
    }

    public AnnotatedMethod getGetter() {
        if ((this._member instanceof AnnotatedMethod) && ((AnnotatedMethod) this._member).getParameterCount() == 0) {
            return (AnnotatedMethod) this._member;
        }
        return null;
    }

    public AnnotatedMethod getSetter() {
        if ((this._member instanceof AnnotatedMethod) && ((AnnotatedMethod) this._member).getParameterCount() == 1) {
            return (AnnotatedMethod) this._member;
        }
        return null;
    }

    public AnnotatedField getField() {
        return this._member instanceof AnnotatedField ? (AnnotatedField) this._member : null;
    }

    public AnnotatedParameter getConstructorParameter() {
        return this._member instanceof AnnotatedParameter ? (AnnotatedParameter) this._member : null;
    }

    public AnnotatedMember getAccessor() {
        AnnotatedMember getter = getGetter();
        if (getter == null) {
            return getField();
        }
        return getter;
    }

    public AnnotatedMember getMutator() {
        AnnotatedMember constructorParameter = getConstructorParameter();
        if (constructorParameter != null) {
            return constructorParameter;
        }
        constructorParameter = getSetter();
        if (constructorParameter == null) {
            return getField();
        }
        return constructorParameter;
    }

    public AnnotatedMember getPrimaryMember() {
        return this._member;
    }
}
