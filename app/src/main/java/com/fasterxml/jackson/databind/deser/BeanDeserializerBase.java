package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.annotation.JsonFormat.Value;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.BeanProperty.Std;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler;
import com.fasterxml.jackson.databind.deser.impl.ExternalTypeHandler.Builder;
import com.fasterxml.jackson.databind.deser.impl.InnerClassProperty;
import com.fasterxml.jackson.databind.deser.impl.ManagedReferenceProperty;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdValueProperty;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedObjectIdGenerator;
import com.fasterxml.jackson.databind.deser.impl.UnwrappedPropertyHandler;
import com.fasterxml.jackson.databind.deser.impl.ValueInjector;
import com.fasterxml.jackson.databind.deser.std.ContainerDeserializerBase;
import com.fasterxml.jackson.databind.deser.std.StdDelegatingDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.ObjectIdInfo;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.type.ClassKey;
import com.fasterxml.jackson.databind.util.Annotations;
import com.fasterxml.jackson.databind.util.ArrayBuilders;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class BeanDeserializerBase extends StdDeserializer<Object> implements ContextualDeserializer, ResolvableDeserializer, Serializable {
    private static final long serialVersionUID = -2038793552422727904L;
    protected SettableAnyProperty _anySetter;
    protected final Map<String, SettableBeanProperty> _backRefs;
    protected final BeanPropertyMap _beanProperties;
    protected final JavaType _beanType;
    private final transient Annotations _classAnnotations;
    protected JsonDeserializer<Object> _delegateDeserializer;
    protected ExternalTypeHandler _externalTypeIdHandler;
    protected final HashSet<String> _ignorableProps;
    protected final boolean _ignoreAllUnknown;
    protected final ValueInjector[] _injectables;
    protected final boolean _needViewProcesing;
    protected boolean _nonStandardCreation;
    protected final ObjectIdReader _objectIdReader;
    protected PropertyBasedCreator _propertyBasedCreator;
    protected final Shape _serializationShape;
    protected transient HashMap<ClassKey, JsonDeserializer<Object>> _subDeserializers;
    protected UnwrappedPropertyHandler _unwrappedPropertyHandler;
    protected final ValueInstantiator _valueInstantiator;
    protected boolean _vanillaProcessing;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$fasterxml$jackson$core$JsonParser$NumberType = new int[NumberType.values().length];

        static {
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonParser$NumberType[NumberType.INT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonParser$NumberType[NumberType.LONG.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonParser$NumberType[NumberType.FLOAT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonParser$NumberType[NumberType.DOUBLE.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    protected abstract Object _deserializeUsingPropertyBased(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException;

    protected abstract BeanDeserializerBase asArrayDeserializer();

    public abstract Object deserializeFromObject(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException;

    public abstract JsonDeserializer<Object> unwrappingDeserializer(NameTransformer nameTransformer);

    public abstract BeanDeserializerBase withIgnorableProperties(HashSet<String> hashSet);

    public abstract BeanDeserializerBase withObjectIdReader(ObjectIdReader objectIdReader);

    protected BeanDeserializerBase(BeanDeserializerBuilder beanDeserializerBuilder, BeanDescription beanDescription, BeanPropertyMap beanPropertyMap, Map<String, SettableBeanProperty> map, HashSet<String> hashSet, boolean z, boolean z2) {
        boolean z3;
        boolean z4 = true;
        Shape shape = null;
        super(beanDescription.getType());
        this._classAnnotations = beanDescription.getClassInfo().getAnnotations();
        this._beanType = beanDescription.getType();
        this._valueInstantiator = beanDeserializerBuilder.getValueInstantiator();
        this._beanProperties = beanPropertyMap;
        this._backRefs = map;
        this._ignorableProps = hashSet;
        this._ignoreAllUnknown = z;
        this._anySetter = beanDeserializerBuilder.getAnySetter();
        List injectables = beanDeserializerBuilder.getInjectables();
        ValueInjector[] valueInjectorArr = (injectables == null || injectables.isEmpty()) ? null : (ValueInjector[]) injectables.toArray(new ValueInjector[injectables.size()]);
        this._injectables = valueInjectorArr;
        this._objectIdReader = beanDeserializerBuilder.getObjectIdReader();
        if (this._unwrappedPropertyHandler != null || this._valueInstantiator.canCreateUsingDelegate() || this._valueInstantiator.canCreateFromObjectWith() || !this._valueInstantiator.canCreateUsingDefault()) {
            z3 = true;
        } else {
            z3 = false;
        }
        this._nonStandardCreation = z3;
        Value findExpectedFormat = beanDescription.findExpectedFormat(null);
        if (findExpectedFormat != null) {
            shape = findExpectedFormat.getShape();
        }
        this._serializationShape = shape;
        this._needViewProcesing = z2;
        if (this._nonStandardCreation || this._injectables != null || this._needViewProcesing || this._objectIdReader == null) {
            z4 = false;
        }
        this._vanillaProcessing = z4;
    }

    protected BeanDeserializerBase(BeanDeserializerBase beanDeserializerBase) {
        this(beanDeserializerBase, beanDeserializerBase._ignoreAllUnknown);
    }

    protected BeanDeserializerBase(BeanDeserializerBase beanDeserializerBase, boolean z) {
        super(beanDeserializerBase._beanType);
        this._classAnnotations = beanDeserializerBase._classAnnotations;
        this._beanType = beanDeserializerBase._beanType;
        this._valueInstantiator = beanDeserializerBase._valueInstantiator;
        this._delegateDeserializer = beanDeserializerBase._delegateDeserializer;
        this._propertyBasedCreator = beanDeserializerBase._propertyBasedCreator;
        this._beanProperties = beanDeserializerBase._beanProperties;
        this._backRefs = beanDeserializerBase._backRefs;
        this._ignorableProps = beanDeserializerBase._ignorableProps;
        this._ignoreAllUnknown = z;
        this._anySetter = beanDeserializerBase._anySetter;
        this._injectables = beanDeserializerBase._injectables;
        this._objectIdReader = beanDeserializerBase._objectIdReader;
        this._nonStandardCreation = beanDeserializerBase._nonStandardCreation;
        this._unwrappedPropertyHandler = beanDeserializerBase._unwrappedPropertyHandler;
        this._needViewProcesing = beanDeserializerBase._needViewProcesing;
        this._serializationShape = beanDeserializerBase._serializationShape;
        this._vanillaProcessing = beanDeserializerBase._vanillaProcessing;
    }

    protected BeanDeserializerBase(BeanDeserializerBase beanDeserializerBase, NameTransformer nameTransformer) {
        super(beanDeserializerBase._beanType);
        this._classAnnotations = beanDeserializerBase._classAnnotations;
        this._beanType = beanDeserializerBase._beanType;
        this._valueInstantiator = beanDeserializerBase._valueInstantiator;
        this._delegateDeserializer = beanDeserializerBase._delegateDeserializer;
        this._propertyBasedCreator = beanDeserializerBase._propertyBasedCreator;
        this._backRefs = beanDeserializerBase._backRefs;
        this._ignorableProps = beanDeserializerBase._ignorableProps;
        boolean z = nameTransformer != null || beanDeserializerBase._ignoreAllUnknown;
        this._ignoreAllUnknown = z;
        this._anySetter = beanDeserializerBase._anySetter;
        this._injectables = beanDeserializerBase._injectables;
        this._objectIdReader = beanDeserializerBase._objectIdReader;
        this._nonStandardCreation = beanDeserializerBase._nonStandardCreation;
        UnwrappedPropertyHandler unwrappedPropertyHandler = beanDeserializerBase._unwrappedPropertyHandler;
        if (nameTransformer != null) {
            if (unwrappedPropertyHandler != null) {
                unwrappedPropertyHandler = unwrappedPropertyHandler.renameAll(nameTransformer);
            }
            this._beanProperties = beanDeserializerBase._beanProperties.renameAll(nameTransformer);
        } else {
            this._beanProperties = beanDeserializerBase._beanProperties;
        }
        this._unwrappedPropertyHandler = unwrappedPropertyHandler;
        this._needViewProcesing = beanDeserializerBase._needViewProcesing;
        this._serializationShape = beanDeserializerBase._serializationShape;
        this._vanillaProcessing = false;
    }

    public BeanDeserializerBase(BeanDeserializerBase beanDeserializerBase, ObjectIdReader objectIdReader) {
        super(beanDeserializerBase._beanType);
        this._classAnnotations = beanDeserializerBase._classAnnotations;
        this._beanType = beanDeserializerBase._beanType;
        this._valueInstantiator = beanDeserializerBase._valueInstantiator;
        this._delegateDeserializer = beanDeserializerBase._delegateDeserializer;
        this._propertyBasedCreator = beanDeserializerBase._propertyBasedCreator;
        this._backRefs = beanDeserializerBase._backRefs;
        this._ignorableProps = beanDeserializerBase._ignorableProps;
        this._ignoreAllUnknown = beanDeserializerBase._ignoreAllUnknown;
        this._anySetter = beanDeserializerBase._anySetter;
        this._injectables = beanDeserializerBase._injectables;
        this._nonStandardCreation = beanDeserializerBase._nonStandardCreation;
        this._unwrappedPropertyHandler = beanDeserializerBase._unwrappedPropertyHandler;
        this._needViewProcesing = beanDeserializerBase._needViewProcesing;
        this._serializationShape = beanDeserializerBase._serializationShape;
        this._vanillaProcessing = beanDeserializerBase._vanillaProcessing;
        this._objectIdReader = objectIdReader;
        if (objectIdReader == null) {
            this._beanProperties = beanDeserializerBase._beanProperties;
            return;
        }
        this._beanProperties = beanDeserializerBase._beanProperties.withProperty(new ObjectIdValueProperty(objectIdReader, true));
    }

    public BeanDeserializerBase(BeanDeserializerBase beanDeserializerBase, HashSet<String> hashSet) {
        super(beanDeserializerBase._beanType);
        this._classAnnotations = beanDeserializerBase._classAnnotations;
        this._beanType = beanDeserializerBase._beanType;
        this._valueInstantiator = beanDeserializerBase._valueInstantiator;
        this._delegateDeserializer = beanDeserializerBase._delegateDeserializer;
        this._propertyBasedCreator = beanDeserializerBase._propertyBasedCreator;
        this._backRefs = beanDeserializerBase._backRefs;
        this._ignorableProps = hashSet;
        this._ignoreAllUnknown = beanDeserializerBase._ignoreAllUnknown;
        this._anySetter = beanDeserializerBase._anySetter;
        this._injectables = beanDeserializerBase._injectables;
        this._nonStandardCreation = beanDeserializerBase._nonStandardCreation;
        this._unwrappedPropertyHandler = beanDeserializerBase._unwrappedPropertyHandler;
        this._needViewProcesing = beanDeserializerBase._needViewProcesing;
        this._serializationShape = beanDeserializerBase._serializationShape;
        this._vanillaProcessing = beanDeserializerBase._vanillaProcessing;
        this._objectIdReader = beanDeserializerBase._objectIdReader;
        this._beanProperties = beanDeserializerBase._beanProperties;
    }

    public void resolve(DeserializationContext deserializationContext) throws JsonMappingException {
        Builder builder;
        SettableBeanProperty settableBeanProperty;
        boolean z = false;
        if (this._valueInstantiator.canCreateFromObjectWith()) {
            this._propertyBasedCreator = PropertyBasedCreator.construct(deserializationContext, this._valueInstantiator, this._valueInstantiator.getFromObjectArguments(deserializationContext.getConfig()));
            builder = null;
            for (SettableBeanProperty settableBeanProperty2 : this._propertyBasedCreator.properties()) {
                if (settableBeanProperty2.hasValueTypeDeserializer()) {
                    TypeDeserializer valueTypeDeserializer = settableBeanProperty2.getValueTypeDeserializer();
                    if (valueTypeDeserializer.getTypeInclusion() == As.EXTERNAL_PROPERTY) {
                        if (builder == null) {
                            builder = new Builder();
                        }
                        builder.addExternal(settableBeanProperty2, valueTypeDeserializer);
                    }
                }
            }
        } else {
            builder = null;
        }
        Iterator it = this._beanProperties.iterator();
        UnwrappedPropertyHandler unwrappedPropertyHandler = null;
        Builder builder2 = builder;
        while (it.hasNext()) {
            SettableBeanProperty withValueDeserializer;
            settableBeanProperty2 = (SettableBeanProperty) it.next();
            JsonDeserializer createContextual;
            if (settableBeanProperty2.hasValueDeserializer()) {
                JsonDeserializer valueDeserializer = settableBeanProperty2.getValueDeserializer();
                if (valueDeserializer instanceof ContextualDeserializer) {
                    createContextual = ((ContextualDeserializer) valueDeserializer).createContextual(deserializationContext, settableBeanProperty2);
                    if (createContextual != valueDeserializer) {
                        withValueDeserializer = settableBeanProperty2.withValueDeserializer(createContextual);
                    }
                }
                withValueDeserializer = settableBeanProperty2;
            } else {
                createContextual = findConvertingDeserializer(deserializationContext, settableBeanProperty2);
                if (createContextual == null) {
                    createContextual = findDeserializer(deserializationContext, settableBeanProperty2.getType(), settableBeanProperty2);
                }
                withValueDeserializer = settableBeanProperty2.withValueDeserializer(createContextual);
            }
            withValueDeserializer = _resolveManagedReferenceProperty(deserializationContext, withValueDeserializer);
            SettableBeanProperty _resolveUnwrappedProperty = _resolveUnwrappedProperty(deserializationContext, withValueDeserializer);
            if (_resolveUnwrappedProperty != null) {
                UnwrappedPropertyHandler unwrappedPropertyHandler2;
                if (unwrappedPropertyHandler == null) {
                    unwrappedPropertyHandler2 = new UnwrappedPropertyHandler();
                } else {
                    unwrappedPropertyHandler2 = unwrappedPropertyHandler;
                }
                unwrappedPropertyHandler2.addProperty(_resolveUnwrappedProperty);
                unwrappedPropertyHandler = unwrappedPropertyHandler2;
            } else {
                _resolveUnwrappedProperty = _resolveInnerClassValuedProperty(deserializationContext, withValueDeserializer);
                if (_resolveUnwrappedProperty != settableBeanProperty2) {
                    this._beanProperties.replace(_resolveUnwrappedProperty);
                }
                if (_resolveUnwrappedProperty.hasValueTypeDeserializer()) {
                    TypeDeserializer valueTypeDeserializer2 = _resolveUnwrappedProperty.getValueTypeDeserializer();
                    if (valueTypeDeserializer2.getTypeInclusion() == As.EXTERNAL_PROPERTY) {
                        if (builder2 == null) {
                            builder = new Builder();
                        } else {
                            builder = builder2;
                        }
                        builder.addExternal(_resolveUnwrappedProperty, valueTypeDeserializer2);
                        this._beanProperties.remove(_resolveUnwrappedProperty);
                        builder2 = builder;
                    }
                }
            }
        }
        if (!(this._anySetter == null || this._anySetter.hasValueDeserializer())) {
            this._anySetter = this._anySetter.withValueDeserializer(findDeserializer(deserializationContext, this._anySetter.getType(), this._anySetter.getProperty()));
        }
        if (this._valueInstantiator.canCreateUsingDelegate()) {
            JavaType delegateType = this._valueInstantiator.getDelegateType(deserializationContext.getConfig());
            if (delegateType == null) {
                throw new IllegalArgumentException("Invalid delegate-creator definition for " + this._beanType + ": value instantiator (" + this._valueInstantiator.getClass().getName() + ") returned true for 'canCreateUsingDelegate()', but null for 'getDelegateType()'");
            }
            PropertyName propertyName = null;
            this._delegateDeserializer = findDeserializer(deserializationContext, delegateType, new Std(null, delegateType, propertyName, this._classAnnotations, this._valueInstantiator.getDelegateCreator(), false));
        }
        if (builder2 != null) {
            this._externalTypeIdHandler = builder2.build();
            this._nonStandardCreation = true;
        }
        this._unwrappedPropertyHandler = unwrappedPropertyHandler;
        if (unwrappedPropertyHandler != null) {
            this._nonStandardCreation = true;
        }
        if (this._vanillaProcessing && !this._nonStandardCreation) {
            z = true;
        }
        this._vanillaProcessing = z;
    }

    protected JsonDeserializer<Object> findConvertingDeserializer(DeserializationContext deserializationContext, SettableBeanProperty settableBeanProperty) throws JsonMappingException {
        AnnotationIntrospector annotationIntrospector = deserializationContext.getAnnotationIntrospector();
        if (annotationIntrospector != null) {
            Object findDeserializationConverter = annotationIntrospector.findDeserializationConverter(settableBeanProperty.getMember());
            if (findDeserializationConverter != null) {
                Converter converterInstance = deserializationContext.converterInstance(settableBeanProperty.getMember(), findDeserializationConverter);
                JavaType inputType = converterInstance.getInputType(deserializationContext.getTypeFactory());
                return new StdDelegatingDeserializer(converterInstance, inputType, deserializationContext.findContextualValueDeserializer(inputType, settableBeanProperty));
            }
        }
        return null;
    }

    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
        ObjectIdReader objectIdReader;
        Object[] objArr;
        JsonDeserializer jsonDeserializer;
        Shape shape;
        ObjectIdReader objectIdReader2 = this._objectIdReader;
        AnnotationIntrospector annotationIntrospector = deserializationContext.getAnnotationIntrospector();
        Annotated member = (beanProperty == null || annotationIntrospector == null) ? null : beanProperty.getMember();
        if (beanProperty == null || annotationIntrospector == null) {
            objectIdReader = objectIdReader2;
            objArr = null;
        } else {
            String[] findPropertiesToIgnore = annotationIntrospector.findPropertiesToIgnore(member);
            ObjectIdInfo findObjectIdInfo = annotationIntrospector.findObjectIdInfo(member);
            if (findObjectIdInfo != null) {
                SettableBeanProperty findProperty;
                JavaType type;
                ObjectIdGenerator propertyBasedObjectIdGenerator;
                ObjectIdInfo findObjectReferenceInfo = annotationIntrospector.findObjectReferenceInfo(member, findObjectIdInfo);
                Class generatorType = findObjectReferenceInfo.getGeneratorType();
                if (generatorType == PropertyGenerator.class) {
                    String propertyName = findObjectReferenceInfo.getPropertyName();
                    findProperty = findProperty(propertyName);
                    if (findProperty == null) {
                        throw new IllegalArgumentException("Invalid Object Id definition for " + getBeanClass().getName() + ": can not find property with name '" + propertyName + "'");
                    }
                    type = findProperty.getType();
                    propertyBasedObjectIdGenerator = new PropertyBasedObjectIdGenerator(findObjectReferenceInfo.getScope());
                } else {
                    JavaType javaType = deserializationContext.getTypeFactory().findTypeParameters(deserializationContext.constructType(generatorType), ObjectIdGenerator.class)[0];
                    propertyBasedObjectIdGenerator = deserializationContext.objectIdGeneratorInstance(member, findObjectReferenceInfo);
                    type = javaType;
                    findProperty = null;
                }
                objectIdReader = ObjectIdReader.construct(type, findObjectReferenceInfo.getPropertyName(), propertyBasedObjectIdGenerator, deserializationContext.findRootValueDeserializer(type), findProperty);
                objArr = findPropertiesToIgnore;
            } else {
                objectIdReader = objectIdReader2;
                String[] strArr = findPropertiesToIgnore;
            }
        }
        if (objectIdReader == null || objectIdReader == this._objectIdReader) {
            jsonDeserializer = this;
        } else {
            jsonDeserializer = withObjectIdReader(objectIdReader);
        }
        if (!(objArr == null || objArr.length == 0)) {
            jsonDeserializer = jsonDeserializer.withIgnorableProperties(ArrayBuilders.setAndArray(jsonDeserializer._ignorableProps, objArr));
        }
        if (member != null) {
            Value findFormat = annotationIntrospector.findFormat(member);
            if (findFormat != null) {
                shape = findFormat.getShape();
                if (shape == null) {
                    shape = this._serializationShape;
                }
                if (shape != Shape.ARRAY) {
                    return jsonDeserializer.asArrayDeserializer();
                }
                return jsonDeserializer;
            }
        }
        shape = null;
        if (shape == null) {
            shape = this._serializationShape;
        }
        if (shape != Shape.ARRAY) {
            return jsonDeserializer;
        }
        return jsonDeserializer.asArrayDeserializer();
    }

    protected SettableBeanProperty _resolveManagedReferenceProperty(DeserializationContext deserializationContext, SettableBeanProperty settableBeanProperty) {
        String managedReferenceName = settableBeanProperty.getManagedReferenceName();
        if (managedReferenceName == null) {
            return settableBeanProperty;
        }
        SettableBeanProperty findBackReference;
        JsonDeserializer valueDeserializer = settableBeanProperty.getValueDeserializer();
        boolean z = false;
        if (valueDeserializer instanceof BeanDeserializerBase) {
            findBackReference = ((BeanDeserializerBase) valueDeserializer).findBackReference(managedReferenceName);
        } else if (valueDeserializer instanceof ContainerDeserializerBase) {
            valueDeserializer = ((ContainerDeserializerBase) valueDeserializer).getContentDeserializer();
            if (valueDeserializer instanceof BeanDeserializerBase) {
                findBackReference = ((BeanDeserializerBase) valueDeserializer).findBackReference(managedReferenceName);
                z = true;
            } else {
                throw new IllegalArgumentException("Can not handle managed/back reference '" + managedReferenceName + "': value deserializer is of type ContainerDeserializerBase, but content type is not handled by a BeanDeserializer " + " (instead it's of type " + (valueDeserializer == null ? "NULL" : valueDeserializer.getClass().getName()) + ")");
            }
        } else if (valueDeserializer instanceof AbstractDeserializer) {
            findBackReference = ((AbstractDeserializer) valueDeserializer).findBackReference(managedReferenceName);
        } else {
            throw new IllegalArgumentException("Can not handle managed/back reference '" + managedReferenceName + "': type for value deserializer is not BeanDeserializer or ContainerDeserializerBase, but " + valueDeserializer.getClass().getName());
        }
        if (findBackReference == null) {
            throw new IllegalArgumentException("Can not handle managed/back reference '" + managedReferenceName + "': no back reference property found from type " + settableBeanProperty.getType());
        }
        JavaType javaType = this._beanType;
        JavaType type = findBackReference.getType();
        if (type.getRawClass().isAssignableFrom(javaType.getRawClass())) {
            return new ManagedReferenceProperty(settableBeanProperty, managedReferenceName, findBackReference, this._classAnnotations, z);
        }
        throw new IllegalArgumentException("Can not handle managed/back reference '" + managedReferenceName + "': back reference type (" + type.getRawClass().getName() + ") not compatible with managed type (" + javaType.getRawClass().getName() + ")");
    }

    protected SettableBeanProperty _resolveUnwrappedProperty(DeserializationContext deserializationContext, SettableBeanProperty settableBeanProperty) {
        AnnotatedMember member = settableBeanProperty.getMember();
        if (member != null) {
            NameTransformer findUnwrappingNameTransformer = deserializationContext.getAnnotationIntrospector().findUnwrappingNameTransformer(member);
            if (findUnwrappingNameTransformer != null) {
                JsonDeserializer valueDeserializer = settableBeanProperty.getValueDeserializer();
                JsonDeserializer unwrappingDeserializer = valueDeserializer.unwrappingDeserializer(findUnwrappingNameTransformer);
                if (!(unwrappingDeserializer == valueDeserializer || unwrappingDeserializer == null)) {
                    return settableBeanProperty.withValueDeserializer(unwrappingDeserializer);
                }
            }
        }
        return null;
    }

    protected SettableBeanProperty _resolveInnerClassValuedProperty(DeserializationContext deserializationContext, SettableBeanProperty settableBeanProperty) {
        JsonDeserializer valueDeserializer = settableBeanProperty.getValueDeserializer();
        if (!(valueDeserializer instanceof BeanDeserializerBase) || ((BeanDeserializerBase) valueDeserializer).getValueInstantiator().canCreateUsingDefault()) {
            return settableBeanProperty;
        }
        Class rawClass = settableBeanProperty.getType().getRawClass();
        Class outerClass = ClassUtil.getOuterClass(rawClass);
        if (outerClass == null || outerClass != this._beanType.getRawClass()) {
            return settableBeanProperty;
        }
        for (Constructor constructor : rawClass.getConstructors()) {
            Class[] parameterTypes = constructor.getParameterTypes();
            if (parameterTypes.length == 1 && parameterTypes[0] == outerClass) {
                if (deserializationContext.getConfig().canOverrideAccessModifiers()) {
                    ClassUtil.checkAndFixAccess(constructor);
                }
                return new InnerClassProperty(settableBeanProperty, constructor);
            }
        }
        return settableBeanProperty;
    }

    public boolean isCachable() {
        return true;
    }

    public ObjectIdReader getObjectIdReader() {
        return this._objectIdReader;
    }

    public boolean hasProperty(String str) {
        return this._beanProperties.find(str) != null;
    }

    public boolean hasViews() {
        return this._needViewProcesing;
    }

    public int getPropertyCount() {
        return this._beanProperties.size();
    }

    public Collection<Object> getKnownPropertyNames() {
        Collection arrayList = new ArrayList();
        Iterator it = this._beanProperties.iterator();
        while (it.hasNext()) {
            arrayList.add(((SettableBeanProperty) it.next()).getName());
        }
        return arrayList;
    }

    public final Class<?> getBeanClass() {
        return this._beanType.getRawClass();
    }

    public JavaType getValueType() {
        return this._beanType;
    }

    public Iterator<SettableBeanProperty> properties() {
        if (this._beanProperties != null) {
            return this._beanProperties.iterator();
        }
        throw new IllegalStateException("Can only call after BeanDeserializer has been resolved");
    }

    public Iterator<SettableBeanProperty> creatorProperties() {
        if (this._propertyBasedCreator == null) {
            return Collections.emptyList().iterator();
        }
        return this._propertyBasedCreator.properties().iterator();
    }

    public SettableBeanProperty findProperty(String str) {
        SettableBeanProperty find = this._beanProperties == null ? null : this._beanProperties.find(str);
        if (find != null || this._propertyBasedCreator == null) {
            return find;
        }
        return this._propertyBasedCreator.findCreatorProperty(str);
    }

    public SettableBeanProperty findBackReference(String str) {
        if (this._backRefs == null) {
            return null;
        }
        return (SettableBeanProperty) this._backRefs.get(str);
    }

    public ValueInstantiator getValueInstantiator() {
        return this._valueInstantiator;
    }

    public void replaceProperty(SettableBeanProperty settableBeanProperty, SettableBeanProperty settableBeanProperty2) {
        this._beanProperties.replace(settableBeanProperty2);
    }

    public final Object deserializeWithType(JsonParser jsonParser, DeserializationContext deserializationContext, TypeDeserializer typeDeserializer) throws IOException, JsonProcessingException {
        if (this._objectIdReader != null) {
            JsonToken currentToken = jsonParser.getCurrentToken();
            if (currentToken != null && currentToken.isScalarValue()) {
                return deserializeFromObjectId(jsonParser, deserializationContext);
            }
        }
        return typeDeserializer.deserializeTypedFromObject(jsonParser, deserializationContext);
    }

    protected Object deserializeWithObjectId(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        String str = this._objectIdReader.propertyName;
        if (str.equals(jsonParser.getCurrentName())) {
            return deserializeFromObject(jsonParser, deserializationContext);
        }
        TokenBuffer tokenBuffer = new TokenBuffer(jsonParser.getCodec());
        TokenBuffer tokenBuffer2 = null;
        while (jsonParser.getCurrentToken() != JsonToken.END_OBJECT) {
            String currentName = jsonParser.getCurrentName();
            if (tokenBuffer2 != null) {
                tokenBuffer2.writeFieldName(currentName);
                jsonParser.nextToken();
                tokenBuffer2.copyCurrentStructure(jsonParser);
            } else if (str.equals(currentName)) {
                tokenBuffer2 = new TokenBuffer(jsonParser.getCodec());
                tokenBuffer2.writeFieldName(currentName);
                jsonParser.nextToken();
                tokenBuffer2.copyCurrentStructure(jsonParser);
                tokenBuffer2.append(tokenBuffer);
                tokenBuffer = null;
            } else {
                tokenBuffer.writeFieldName(currentName);
                jsonParser.nextToken();
                tokenBuffer.copyCurrentStructure(jsonParser);
            }
            jsonParser.nextToken();
        }
        if (tokenBuffer2 != null) {
            tokenBuffer = tokenBuffer2;
        }
        tokenBuffer.writeEndObject();
        JsonParser asParser = tokenBuffer.asParser();
        asParser.nextToken();
        return deserializeFromObject(asParser, deserializationContext);
    }

    protected Object deserializeFromObjectId(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        Object deserialize = this._objectIdReader.deserializer.deserialize(jsonParser, deserializationContext);
        Object obj = deserializationContext.findObjectId(deserialize, this._objectIdReader.generator).item;
        if (obj != null) {
            return obj;
        }
        throw new IllegalStateException("Could not resolve Object Id [" + deserialize + "] (for " + this._beanType + ") -- unresolved forward-reference?");
    }

    protected Object deserializeFromObjectUsingNonDefault(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer != null) {
            return this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
        }
        if (this._propertyBasedCreator != null) {
            return _deserializeUsingPropertyBased(jsonParser, deserializationContext);
        }
        if (this._beanType.isAbstract()) {
            throw JsonMappingException.from(jsonParser, "Can not instantiate abstract type " + this._beanType + " (need to add/enable type information?)");
        }
        throw JsonMappingException.from(jsonParser, "No suitable constructor found for type " + this._beanType + ": can not instantiate from JSON object (need to add/enable type information?)");
    }

    public Object deserializeFromNumber(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        if (this._objectIdReader != null) {
            return deserializeFromObjectId(jsonParser, deserializationContext);
        }
        Object createUsingDelegate;
        switch (AnonymousClass1.$SwitchMap$com$fasterxml$jackson$core$JsonParser$NumberType[jsonParser.getNumberType().ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                if (this._delegateDeserializer == null || this._valueInstantiator.canCreateFromInt()) {
                    return this._valueInstantiator.createFromInt(deserializationContext, jsonParser.getIntValue());
                }
                createUsingDelegate = this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
                if (this._injectables == null) {
                    return createUsingDelegate;
                }
                injectValues(deserializationContext, createUsingDelegate);
                return createUsingDelegate;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                if (this._delegateDeserializer == null || this._valueInstantiator.canCreateFromInt()) {
                    return this._valueInstantiator.createFromLong(deserializationContext, jsonParser.getLongValue());
                }
                createUsingDelegate = this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
                if (this._injectables == null) {
                    return createUsingDelegate;
                }
                injectValues(deserializationContext, createUsingDelegate);
                return createUsingDelegate;
            default:
                if (this._delegateDeserializer != null) {
                    createUsingDelegate = this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
                    if (this._injectables == null) {
                        return createUsingDelegate;
                    }
                    injectValues(deserializationContext, createUsingDelegate);
                    return createUsingDelegate;
                }
                throw deserializationContext.instantiationException(getBeanClass(), "no suitable creator method found to deserialize from JSON integer number");
        }
    }

    public Object deserializeFromString(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        if (this._objectIdReader != null) {
            return deserializeFromObjectId(jsonParser, deserializationContext);
        }
        if (this._delegateDeserializer == null || this._valueInstantiator.canCreateFromString()) {
            return this._valueInstantiator.createFromString(deserializationContext, jsonParser.getText());
        }
        Object createUsingDelegate = this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
        if (this._injectables == null) {
            return createUsingDelegate;
        }
        injectValues(deserializationContext, createUsingDelegate);
        return createUsingDelegate;
    }

    public Object deserializeFromDouble(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        switch (AnonymousClass1.$SwitchMap$com$fasterxml$jackson$core$JsonParser$NumberType[jsonParser.getNumberType().ordinal()]) {
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                if (this._delegateDeserializer == null || this._valueInstantiator.canCreateFromDouble()) {
                    return this._valueInstantiator.createFromDouble(deserializationContext, jsonParser.getDoubleValue());
                }
                Object createUsingDelegate = this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
                if (this._injectables == null) {
                    return createUsingDelegate;
                }
                injectValues(deserializationContext, createUsingDelegate);
                return createUsingDelegate;
            default:
                if (this._delegateDeserializer != null) {
                    return this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
                }
                throw deserializationContext.instantiationException(getBeanClass(), "no suitable creator method found to deserialize from JSON floating-point number");
        }
    }

    public Object deserializeFromBoolean(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer == null || this._valueInstantiator.canCreateFromBoolean()) {
            return this._valueInstantiator.createFromBoolean(deserializationContext, jsonParser.getCurrentToken() == JsonToken.VALUE_TRUE);
        }
        Object createUsingDelegate = this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
        if (this._injectables == null) {
            return createUsingDelegate;
        }
        injectValues(deserializationContext, createUsingDelegate);
        return createUsingDelegate;
    }

    public Object deserializeFromArray(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        if (this._delegateDeserializer != null) {
            try {
                Object createUsingDelegate = this._valueInstantiator.createUsingDelegate(deserializationContext, this._delegateDeserializer.deserialize(jsonParser, deserializationContext));
                if (this._injectables != null) {
                    injectValues(deserializationContext, createUsingDelegate);
                }
                return createUsingDelegate;
            } catch (Throwable e) {
                wrapInstantiationProblem(e, deserializationContext);
            }
        }
        throw deserializationContext.mappingException(getBeanClass());
    }

    protected void injectValues(DeserializationContext deserializationContext, Object obj) throws IOException, JsonProcessingException {
        for (ValueInjector inject : this._injectables) {
            inject.inject(deserializationContext, obj);
        }
    }

    protected void handleUnknownProperty(JsonParser jsonParser, DeserializationContext deserializationContext, Object obj, String str) throws IOException, JsonProcessingException {
        if (this._ignoreAllUnknown || (this._ignorableProps != null && this._ignorableProps.contains(str))) {
            jsonParser.skipChildren();
        } else {
            super.handleUnknownProperty(jsonParser, deserializationContext, obj, str);
        }
    }

    protected Object handleUnknownProperties(DeserializationContext deserializationContext, Object obj, TokenBuffer tokenBuffer) throws IOException, JsonProcessingException {
        tokenBuffer.writeEndObject();
        JsonParser asParser = tokenBuffer.asParser();
        while (asParser.nextToken() != JsonToken.END_OBJECT) {
            String currentName = asParser.getCurrentName();
            asParser.nextToken();
            handleUnknownProperty(asParser, deserializationContext, obj, currentName);
        }
        return obj;
    }

    protected void handleUnknownVanilla(JsonParser jsonParser, DeserializationContext deserializationContext, Object obj, String str) throws IOException, JsonProcessingException {
        if (this._ignorableProps != null && this._ignorableProps.contains(str)) {
            jsonParser.skipChildren();
        } else if (this._anySetter != null) {
            try {
                this._anySetter.deserializeAndSet(jsonParser, deserializationContext, obj, str);
            } catch (Throwable e) {
                wrapAndThrow(e, obj, str, deserializationContext);
            }
        } else {
            handleUnknownProperty(jsonParser, deserializationContext, obj, str);
        }
    }

    protected Object handlePolymorphic(JsonParser jsonParser, DeserializationContext deserializationContext, Object obj, TokenBuffer tokenBuffer) throws IOException, JsonProcessingException {
        JsonDeserializer _findSubclassDeserializer = _findSubclassDeserializer(deserializationContext, obj, tokenBuffer);
        Object deserialize;
        if (_findSubclassDeserializer != null) {
            if (tokenBuffer != null) {
                tokenBuffer.writeEndObject();
                JsonParser asParser = tokenBuffer.asParser();
                asParser.nextToken();
                deserialize = _findSubclassDeserializer.deserialize(asParser, deserializationContext, obj);
            } else {
                deserialize = obj;
            }
            if (jsonParser != null) {
                return _findSubclassDeserializer.deserialize(jsonParser, deserializationContext, deserialize);
            }
            return deserialize;
        }
        if (tokenBuffer != null) {
            deserialize = handleUnknownProperties(deserializationContext, obj, tokenBuffer);
        } else {
            deserialize = obj;
        }
        if (jsonParser != null) {
            return deserialize(jsonParser, deserializationContext, deserialize);
        }
        return deserialize;
    }

    protected JsonDeserializer<Object> _findSubclassDeserializer(DeserializationContext deserializationContext, Object obj, TokenBuffer tokenBuffer) throws IOException, JsonProcessingException {
        JsonDeserializer<Object> jsonDeserializer;
        synchronized (this) {
            jsonDeserializer = this._subDeserializers == null ? null : (JsonDeserializer) this._subDeserializers.get(new ClassKey(obj.getClass()));
        }
        if (jsonDeserializer == null) {
            jsonDeserializer = deserializationContext.findRootValueDeserializer(deserializationContext.constructType(obj.getClass()));
            if (jsonDeserializer != null) {
                synchronized (this) {
                    if (this._subDeserializers == null) {
                        this._subDeserializers = new HashMap();
                    }
                    this._subDeserializers.put(new ClassKey(obj.getClass()), jsonDeserializer);
                }
            }
        }
        return jsonDeserializer;
    }

    public void wrapAndThrow(Throwable th, Object obj, String str, DeserializationContext deserializationContext) throws IOException {
        throw JsonMappingException.wrapWithPath(throwOrReturnThrowable(th, deserializationContext), obj, str);
    }

    public void wrapAndThrow(Throwable th, Object obj, int i, DeserializationContext deserializationContext) throws IOException {
        throw JsonMappingException.wrapWithPath(throwOrReturnThrowable(th, deserializationContext), obj, i);
    }

    private Throwable throwOrReturnThrowable(Throwable th, DeserializationContext deserializationContext) throws IOException {
        Throwable th2 = th;
        while ((th2 instanceof InvocationTargetException) && th2.getCause() != null) {
            th2 = th2.getCause();
        }
        if (th2 instanceof Error) {
            throw ((Error) th2);
        }
        Object obj = (deserializationContext == null || deserializationContext.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS)) ? 1 : null;
        if (th2 instanceof IOException) {
            if (obj == null || !(th2 instanceof JsonProcessingException)) {
                throw ((IOException) th2);
            }
        } else if (obj == null && (th2 instanceof RuntimeException)) {
            throw ((RuntimeException) th2);
        }
        return th2;
    }

    protected void wrapInstantiationProblem(Throwable th, DeserializationContext deserializationContext) throws IOException {
        Throwable th2 = th;
        while ((th2 instanceof InvocationTargetException) && th2.getCause() != null) {
            th2 = th2.getCause();
        }
        if (th2 instanceof Error) {
            throw ((Error) th2);
        }
        Object obj = (deserializationContext == null || deserializationContext.isEnabled(DeserializationFeature.WRAP_EXCEPTIONS)) ? 1 : null;
        if (th2 instanceof IOException) {
            throw ((IOException) th2);
        } else if (obj == null && (th2 instanceof RuntimeException)) {
            throw ((RuntimeException) th2);
        } else {
            throw deserializationContext.instantiationException(this._beanType.getRawClass(), th2);
        }
    }
}
