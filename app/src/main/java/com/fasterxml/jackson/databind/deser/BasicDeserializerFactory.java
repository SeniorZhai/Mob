package com.fasterxml.jackson.databind.deser;

import com.fasterxml.jackson.databind.AbstractTypeResolver;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty.Std;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.cfg.DeserializerFactoryConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.deser.impl.CreatorCollector;
import com.fasterxml.jackson.databind.deser.std.ArrayBlockingQueueDeserializer;
import com.fasterxml.jackson.databind.deser.std.CollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.fasterxml.jackson.databind.deser.std.EnumMapDeserializer;
import com.fasterxml.jackson.databind.deser.std.EnumSetDeserializer;
import com.fasterxml.jackson.databind.deser.std.JacksonDeserializers;
import com.fasterxml.jackson.databind.deser.std.JdkDeserializers;
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers;
import com.fasterxml.jackson.databind.deser.std.ObjectArrayDeserializer;
import com.fasterxml.jackson.databind.deser.std.PrimitiveArrayDeserializers;
import com.fasterxml.jackson.databind.deser.std.StdKeyDeserializers;
import com.fasterxml.jackson.databind.deser.std.StringArrayDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringCollectionDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.AnnotatedConstructor;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.AnnotatedWithParams;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.MapLikeType;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.ClassUtil;
import com.fasterxml.jackson.databind.util.EnumResolver;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public abstract class BasicDeserializerFactory extends DeserializerFactory implements Serializable {
    private static final Class<?> CLASS_CHAR_BUFFER = CharSequence.class;
    private static final Class<?> CLASS_ITERABLE = Iterable.class;
    private static final Class<?> CLASS_OBJECT = Object.class;
    private static final Class<?> CLASS_STRING = String.class;
    static final HashMap<String, Class<? extends Collection>> _collectionFallbacks = new HashMap();
    static final HashMap<String, Class<? extends Map>> _mapFallbacks = new HashMap();
    protected final DeserializerFactoryConfig _factoryConfig;

    protected abstract DeserializerFactory withConfig(DeserializerFactoryConfig deserializerFactoryConfig);

    static {
        _mapFallbacks.put(Map.class.getName(), LinkedHashMap.class);
        _mapFallbacks.put(ConcurrentMap.class.getName(), ConcurrentHashMap.class);
        _mapFallbacks.put(SortedMap.class.getName(), TreeMap.class);
        _mapFallbacks.put("java.util.NavigableMap", TreeMap.class);
        try {
            _mapFallbacks.put(ConcurrentNavigableMap.class.getName(), ConcurrentSkipListMap.class);
        } catch (Throwable th) {
            System.err.println("Problems with (optional) types: " + th);
        }
        _collectionFallbacks.put(Collection.class.getName(), ArrayList.class);
        _collectionFallbacks.put(List.class.getName(), ArrayList.class);
        _collectionFallbacks.put(Set.class.getName(), HashSet.class);
        _collectionFallbacks.put(SortedSet.class.getName(), TreeSet.class);
        _collectionFallbacks.put(Queue.class.getName(), LinkedList.class);
        _collectionFallbacks.put("java.util.Deque", LinkedList.class);
        _collectionFallbacks.put("java.util.NavigableSet", TreeSet.class);
    }

    protected BasicDeserializerFactory(DeserializerFactoryConfig deserializerFactoryConfig) {
        this._factoryConfig = deserializerFactoryConfig;
    }

    public DeserializerFactoryConfig getFactoryConfig() {
        return this._factoryConfig;
    }

    public final DeserializerFactory withAdditionalDeserializers(Deserializers deserializers) {
        return withConfig(this._factoryConfig.withAdditionalDeserializers(deserializers));
    }

    public final DeserializerFactory withAdditionalKeyDeserializers(KeyDeserializers keyDeserializers) {
        return withConfig(this._factoryConfig.withAdditionalKeyDeserializers(keyDeserializers));
    }

    public final DeserializerFactory withDeserializerModifier(BeanDeserializerModifier beanDeserializerModifier) {
        return withConfig(this._factoryConfig.withDeserializerModifier(beanDeserializerModifier));
    }

    public final DeserializerFactory withAbstractTypeResolver(AbstractTypeResolver abstractTypeResolver) {
        return withConfig(this._factoryConfig.withAbstractTypeResolver(abstractTypeResolver));
    }

    public final DeserializerFactory withValueInstantiators(ValueInstantiators valueInstantiators) {
        return withConfig(this._factoryConfig.withValueInstantiators(valueInstantiators));
    }

    public JavaType mapAbstractType(DeserializationConfig deserializationConfig, JavaType javaType) throws JsonMappingException {
        JavaType _mapAbstractType2;
        while (true) {
            _mapAbstractType2 = _mapAbstractType2(deserializationConfig, javaType);
            if (_mapAbstractType2 == null) {
                return javaType;
            }
            Class rawClass = javaType.getRawClass();
            Class rawClass2 = _mapAbstractType2.getRawClass();
            if (rawClass != rawClass2 && rawClass.isAssignableFrom(rawClass2)) {
                javaType = _mapAbstractType2;
            }
        }
        throw new IllegalArgumentException("Invalid abstract type resolution from " + javaType + " to " + _mapAbstractType2 + ": latter is not a subtype of former");
    }

    private JavaType _mapAbstractType2(DeserializationConfig deserializationConfig, JavaType javaType) throws JsonMappingException {
        Class rawClass = javaType.getRawClass();
        if (this._factoryConfig.hasAbstractTypeResolvers()) {
            for (AbstractTypeResolver findTypeMapping : this._factoryConfig.abstractTypeResolvers()) {
                JavaType findTypeMapping2 = findTypeMapping.findTypeMapping(deserializationConfig, javaType);
                if (findTypeMapping2 != null && findTypeMapping2.getRawClass() != rawClass) {
                    return findTypeMapping2;
                }
            }
        }
        return null;
    }

    public ValueInstantiator findValueInstantiator(DeserializationContext deserializationContext, BeanDescription beanDescription) throws JsonMappingException {
        ValueInstantiator valueInstantiator;
        DeserializationConfig config = deserializationContext.getConfig();
        ValueInstantiator valueInstantiator2 = null;
        Annotated classInfo = beanDescription.getClassInfo();
        Object findValueInstantiator = deserializationContext.getAnnotationIntrospector().findValueInstantiator(classInfo);
        if (findValueInstantiator != null) {
            valueInstantiator2 = _valueInstantiatorInstance(config, classInfo, findValueInstantiator);
        }
        if (valueInstantiator2 == null) {
            valueInstantiator2 = _findStdValueInstantiator(config, beanDescription);
            if (valueInstantiator2 == null) {
                valueInstantiator2 = _constructDefaultValueInstantiator(deserializationContext, beanDescription);
            }
        }
        if (this._factoryConfig.hasValueInstantiators()) {
            valueInstantiator = valueInstantiator2;
            for (ValueInstantiators valueInstantiators : this._factoryConfig.valueInstantiators()) {
                valueInstantiator = valueInstantiators.findValueInstantiator(config, beanDescription, valueInstantiator);
                if (valueInstantiator == null) {
                    throw new JsonMappingException("Broken registered ValueInstantiators (of type " + valueInstantiators.getClass().getName() + "): returned null ValueInstantiator");
                }
            }
        }
        valueInstantiator = valueInstantiator2;
        if (valueInstantiator.getIncompleteParameter() == null) {
            return valueInstantiator;
        }
        AnnotatedParameter incompleteParameter = valueInstantiator.getIncompleteParameter();
        throw new IllegalArgumentException("Argument #" + incompleteParameter.getIndex() + " of constructor " + incompleteParameter.getOwner() + " has no property name annotation; must have name when multiple-paramater constructor annotated as Creator");
    }

    private ValueInstantiator _findStdValueInstantiator(DeserializationConfig deserializationConfig, BeanDescription beanDescription) throws JsonMappingException {
        return JacksonDeserializers.findValueInstantiator(deserializationConfig, beanDescription);
    }

    protected ValueInstantiator _constructDefaultValueInstantiator(DeserializationContext deserializationContext, BeanDescription beanDescription) throws JsonMappingException {
        CreatorCollector creatorCollector = new CreatorCollector(beanDescription, deserializationContext.canOverrideAccessModifiers());
        AnnotationIntrospector annotationIntrospector = deserializationContext.getAnnotationIntrospector();
        DeserializationConfig config = deserializationContext.getConfig();
        VisibilityChecker findAutoDetectVisibility = annotationIntrospector.findAutoDetectVisibility(beanDescription.getClassInfo(), config.getDefaultVisibilityChecker());
        _addDeserializerFactoryMethods(deserializationContext, beanDescription, findAutoDetectVisibility, annotationIntrospector, creatorCollector);
        if (beanDescription.getType().isConcrete()) {
            _addDeserializerConstructors(deserializationContext, beanDescription, findAutoDetectVisibility, annotationIntrospector, creatorCollector);
        }
        return creatorCollector.constructValueInstantiator(config);
    }

    public ValueInstantiator _valueInstantiatorInstance(DeserializationConfig deserializationConfig, Annotated annotated, Object obj) throws JsonMappingException {
        if (obj == null) {
            return null;
        }
        if (obj instanceof ValueInstantiator) {
            return (ValueInstantiator) obj;
        }
        if (obj instanceof Class) {
            Class cls = (Class) obj;
            if (cls == NoClass.class) {
                return null;
            }
            if (ValueInstantiator.class.isAssignableFrom(cls)) {
                HandlerInstantiator handlerInstantiator = deserializationConfig.getHandlerInstantiator();
                if (handlerInstantiator != null) {
                    ValueInstantiator valueInstantiatorInstance = handlerInstantiator.valueInstantiatorInstance(deserializationConfig, annotated, cls);
                    if (valueInstantiatorInstance != null) {
                        return valueInstantiatorInstance;
                    }
                }
                return (ValueInstantiator) ClassUtil.createInstance(cls, deserializationConfig.canOverrideAccessModifiers());
            }
            throw new IllegalStateException("AnnotationIntrospector returned Class " + cls.getName() + "; expected Class<ValueInstantiator>");
        }
        throw new IllegalStateException("AnnotationIntrospector returned key deserializer definition of type " + obj.getClass().getName() + "; expected type KeyDeserializer or Class<KeyDeserializer> instead");
    }

    protected void _addDeserializerConstructors(DeserializationContext deserializationContext, BeanDescription beanDescription, VisibilityChecker<?> visibilityChecker, AnnotationIntrospector annotationIntrospector, CreatorCollector creatorCollector) throws JsonMappingException {
        Annotated findDefaultConstructor = beanDescription.findDefaultConstructor();
        if (findDefaultConstructor != null && (!creatorCollector.hasDefaultCreator() || annotationIntrospector.hasCreatorAnnotation(findDefaultConstructor))) {
            creatorCollector.setDefaultCreator(findDefaultConstructor);
        }
        String[] strArr = null;
        AnnotatedConstructor annotatedConstructor = null;
        for (BeanPropertyDefinition beanPropertyDefinition : beanDescription.findProperties()) {
            if (beanPropertyDefinition.getConstructorParameter() != null) {
                AnnotatedParameter constructorParameter = beanPropertyDefinition.getConstructorParameter();
                AnnotatedWithParams owner = constructorParameter.getOwner();
                if (owner instanceof AnnotatedConstructor) {
                    AnnotatedConstructor annotatedConstructor2;
                    String[] strArr2;
                    if (annotatedConstructor == null) {
                        annotatedConstructor2 = (AnnotatedConstructor) owner;
                        strArr2 = new String[annotatedConstructor2.getParameterCount()];
                    } else {
                        AnnotatedMember annotatedMember = annotatedConstructor;
                        strArr2 = strArr;
                    }
                    strArr2[constructorParameter.getIndex()] = beanPropertyDefinition.getName();
                    annotatedConstructor = annotatedConstructor2;
                    strArr = strArr2;
                }
            }
        }
        for (AnnotatedMember annotatedMember2 : beanDescription.getConstructors()) {
            int parameterCount = annotatedMember2.getParameterCount();
            boolean z = annotationIntrospector.hasCreatorAnnotation(annotatedMember2) || annotatedMember2 == annotatedConstructor;
            boolean isCreatorVisible = visibilityChecker.isCreatorVisible(annotatedMember2);
            if (parameterCount == 1) {
                _handleSingleArgumentConstructor(deserializationContext, beanDescription, visibilityChecker, annotationIntrospector, creatorCollector, annotatedMember2, z, isCreatorVisible, annotatedMember2 == annotatedConstructor ? strArr[0] : null);
            } else if (z || isCreatorVisible) {
                Annotated annotated = null;
                int i = 0;
                int i2 = 0;
                CreatorProperty[] creatorPropertyArr = new CreatorProperty[parameterCount];
                int i3 = 0;
                while (i3 < parameterCount) {
                    Annotated parameter = annotatedMember2.getParameter(i3);
                    String str = null;
                    if (annotatedMember2 == annotatedConstructor) {
                        str = strArr[i3];
                    }
                    if (str == null) {
                        PropertyName findNameForDeserialization = parameter == null ? null : annotationIntrospector.findNameForDeserialization(parameter);
                        str = findNameForDeserialization == null ? null : findNameForDeserialization.getSimpleName();
                    }
                    Object findInjectableValueId = annotationIntrospector.findInjectableValueId(parameter);
                    if (str != null && str.length() > 0) {
                        i++;
                        creatorPropertyArr[i3] = constructCreatorProperty(deserializationContext, beanDescription, str, i3, parameter, findInjectableValueId);
                        parameter = annotated;
                    } else if (findInjectableValueId != null) {
                        i2++;
                        creatorPropertyArr[i3] = constructCreatorProperty(deserializationContext, beanDescription, str, i3, parameter, findInjectableValueId);
                        parameter = annotated;
                    } else if (annotated != null) {
                        parameter = annotated;
                    }
                    i3++;
                    annotated = parameter;
                }
                if (z || i > 0 || i2 > 0) {
                    if (i + i2 == parameterCount) {
                        creatorCollector.addPropertyCreator(annotatedMember2, creatorPropertyArr);
                    } else if (i == 0 && i2 + 1 == parameterCount) {
                        creatorCollector.addDelegatingCreator(annotatedMember2, creatorPropertyArr);
                    } else {
                        creatorCollector.addIncompeteParameter(annotated);
                    }
                }
            }
        }
    }

    protected boolean _handleSingleArgumentConstructor(DeserializationContext deserializationContext, BeanDescription beanDescription, VisibilityChecker<?> visibilityChecker, AnnotationIntrospector annotationIntrospector, CreatorCollector creatorCollector, AnnotatedConstructor annotatedConstructor, boolean z, boolean z2, String str) throws JsonMappingException {
        String simpleName;
        Annotated parameter = annotatedConstructor.getParameter(0);
        if (str == null) {
            PropertyName findNameForDeserialization = parameter == null ? null : annotationIntrospector.findNameForDeserialization(parameter);
            simpleName = findNameForDeserialization == null ? null : findNameForDeserialization.getSimpleName();
        } else {
            simpleName = str;
        }
        if (annotationIntrospector.findInjectableValueId(parameter) != null || (simpleName != null && simpleName.length() > 0)) {
            creatorCollector.addPropertyCreator(annotatedConstructor, new CreatorProperty[]{constructCreatorProperty(deserializationContext, beanDescription, simpleName, 0, parameter, r6)});
            return true;
        }
        Class rawParameterType = annotatedConstructor.getRawParameterType(0);
        if (rawParameterType == String.class) {
            if (z || z2) {
                creatorCollector.addStringCreator(annotatedConstructor);
            }
            return true;
        } else if (rawParameterType == Integer.TYPE || rawParameterType == Integer.class) {
            if (z || z2) {
                creatorCollector.addIntCreator(annotatedConstructor);
            }
            return true;
        } else if (rawParameterType == Long.TYPE || rawParameterType == Long.class) {
            if (z || z2) {
                creatorCollector.addLongCreator(annotatedConstructor);
            }
            return true;
        } else if (rawParameterType == Double.TYPE || rawParameterType == Double.class) {
            if (z || z2) {
                creatorCollector.addDoubleCreator(annotatedConstructor);
            }
            return true;
        } else if (!z) {
            return false;
        } else {
            creatorCollector.addDelegatingCreator(annotatedConstructor, null);
            return true;
        }
    }

    protected void _addDeserializerFactoryMethods(DeserializationContext deserializationContext, BeanDescription beanDescription, VisibilityChecker<?> visibilityChecker, AnnotationIntrospector annotationIntrospector, CreatorCollector creatorCollector) throws JsonMappingException {
        DeserializationConfig config = deserializationContext.getConfig();
        for (AnnotatedMethod annotatedMethod : beanDescription.getFactoryMethods()) {
            boolean hasCreatorAnnotation = annotationIntrospector.hasCreatorAnnotation(annotatedMethod);
            int parameterCount = annotatedMethod.getParameterCount();
            if (parameterCount != 0) {
                if (parameterCount == 1) {
                    Annotated parameter = annotatedMethod.getParameter(0);
                    PropertyName findNameForDeserialization = parameter == null ? null : annotationIntrospector.findNameForDeserialization(parameter);
                    String simpleName = findNameForDeserialization == null ? null : findNameForDeserialization.getSimpleName();
                    if (annotationIntrospector.findInjectableValueId(parameter) == null && (simpleName == null || simpleName.length() == 0)) {
                        _handleSingleArgumentFactory(config, beanDescription, visibilityChecker, annotationIntrospector, creatorCollector, annotatedMethod, hasCreatorAnnotation);
                    }
                } else if (!annotationIntrospector.hasCreatorAnnotation(annotatedMethod)) {
                    continue;
                }
                Annotated annotated = null;
                CreatorProperty[] creatorPropertyArr = new CreatorProperty[parameterCount];
                int i = 0;
                int i2 = 0;
                int i3 = 0;
                while (i3 < parameterCount) {
                    Annotated parameter2 = annotatedMethod.getParameter(i3);
                    PropertyName findNameForDeserialization2 = parameter2 == null ? null : annotationIntrospector.findNameForDeserialization(parameter2);
                    String simpleName2 = findNameForDeserialization2 == null ? null : findNameForDeserialization2.getSimpleName();
                    Object findInjectableValueId = annotationIntrospector.findInjectableValueId(parameter2);
                    if (simpleName2 != null && simpleName2.length() > 0) {
                        i++;
                        creatorPropertyArr[i3] = constructCreatorProperty(deserializationContext, beanDescription, simpleName2, i3, parameter2, findInjectableValueId);
                        parameter2 = annotated;
                    } else if (findInjectableValueId != null) {
                        i2++;
                        creatorPropertyArr[i3] = constructCreatorProperty(deserializationContext, beanDescription, simpleName2, i3, parameter2, findInjectableValueId);
                        parameter2 = annotated;
                    } else if (annotated != null) {
                        parameter2 = annotated;
                    }
                    i3++;
                    annotated = parameter2;
                }
                if (hasCreatorAnnotation || i > 0 || i2 > 0) {
                    if (i + i2 == parameterCount) {
                        creatorCollector.addPropertyCreator(annotatedMethod, creatorPropertyArr);
                    } else if (i == 0 && i2 + 1 == parameterCount) {
                        creatorCollector.addDelegatingCreator(annotatedMethod, creatorPropertyArr);
                    } else {
                        throw new IllegalArgumentException("Argument #" + annotated.getIndex() + " of factory method " + annotatedMethod + " has no property name annotation; must have name when multiple-paramater constructor annotated as Creator");
                    }
                }
            } else if (hasCreatorAnnotation) {
                creatorCollector.setDefaultCreator(annotatedMethod);
            }
        }
    }

    protected boolean _handleSingleArgumentFactory(DeserializationConfig deserializationConfig, BeanDescription beanDescription, VisibilityChecker<?> visibilityChecker, AnnotationIntrospector annotationIntrospector, CreatorCollector creatorCollector, AnnotatedMethod annotatedMethod, boolean z) throws JsonMappingException {
        Class rawParameterType = annotatedMethod.getRawParameterType(0);
        if (rawParameterType == String.class) {
            if (!z && !visibilityChecker.isCreatorVisible((AnnotatedMember) annotatedMethod)) {
                return true;
            }
            creatorCollector.addStringCreator(annotatedMethod);
            return true;
        } else if (rawParameterType == Integer.TYPE || rawParameterType == Integer.class) {
            if (!z && !visibilityChecker.isCreatorVisible((AnnotatedMember) annotatedMethod)) {
                return true;
            }
            creatorCollector.addIntCreator(annotatedMethod);
            return true;
        } else if (rawParameterType == Long.TYPE || rawParameterType == Long.class) {
            if (!z && !visibilityChecker.isCreatorVisible((AnnotatedMember) annotatedMethod)) {
                return true;
            }
            creatorCollector.addLongCreator(annotatedMethod);
            return true;
        } else if (rawParameterType == Double.TYPE || rawParameterType == Double.class) {
            if (!z && !visibilityChecker.isCreatorVisible((AnnotatedMember) annotatedMethod)) {
                return true;
            }
            creatorCollector.addDoubleCreator(annotatedMethod);
            return true;
        } else if (rawParameterType == Boolean.TYPE || rawParameterType == Boolean.class) {
            if (!z && !visibilityChecker.isCreatorVisible((AnnotatedMember) annotatedMethod)) {
                return true;
            }
            creatorCollector.addBooleanCreator(annotatedMethod);
            return true;
        } else if (!annotationIntrospector.hasCreatorAnnotation(annotatedMethod)) {
            return false;
        } else {
            creatorCollector.addDelegatingCreator(annotatedMethod, null);
            return true;
        }
    }

    protected CreatorProperty constructCreatorProperty(DeserializationContext deserializationContext, BeanDescription beanDescription, String str, int i, AnnotatedParameter annotatedParameter, Object obj) throws JsonMappingException {
        Std withType;
        TypeDeserializer findTypeDeserializer;
        DeserializationConfig config = deserializationContext.getConfig();
        AnnotationIntrospector annotationIntrospector = deserializationContext.getAnnotationIntrospector();
        Boolean hasRequiredMarker = annotationIntrospector == null ? null : annotationIntrospector.hasRequiredMarker(annotatedParameter);
        boolean booleanValue = hasRequiredMarker == null ? false : hasRequiredMarker.booleanValue();
        JavaType constructType = config.getTypeFactory().constructType(annotatedParameter.getParameterType(), beanDescription.bindingsForBeanType());
        Std std = new Std(str, constructType, annotationIntrospector.findWrapperName(annotatedParameter), beanDescription.getClassAnnotations(), annotatedParameter, booleanValue);
        JavaType resolveType = resolveType(deserializationContext, beanDescription, constructType, annotatedParameter);
        if (resolveType != constructType) {
            withType = std.withType(resolveType);
        } else {
            withType = std;
        }
        JsonDeserializer findDeserializerFromAnnotation = findDeserializerFromAnnotation(deserializationContext, annotatedParameter);
        constructType = modifyTypeByAnnotation(deserializationContext, annotatedParameter, resolveType);
        TypeDeserializer typeDeserializer = (TypeDeserializer) constructType.getTypeHandler();
        if (typeDeserializer == null) {
            findTypeDeserializer = findTypeDeserializer(config, constructType);
        } else {
            findTypeDeserializer = typeDeserializer;
        }
        CreatorProperty creatorProperty = new CreatorProperty(str, constructType, withType.getWrapperName(), findTypeDeserializer, beanDescription.getClassAnnotations(), annotatedParameter, i, obj, withType.isRequired());
        if (findDeserializerFromAnnotation != null) {
            return creatorProperty.withValueDeserializer(findDeserializerFromAnnotation);
        }
        return creatorProperty;
    }

    public JsonDeserializer<?> createArrayDeserializer(DeserializationContext deserializationContext, ArrayType arrayType, BeanDescription beanDescription) throws JsonMappingException {
        TypeDeserializer findTypeDeserializer;
        DeserializationConfig config = deserializationContext.getConfig();
        JavaType contentType = arrayType.getContentType();
        JsonDeserializer jsonDeserializer = (JsonDeserializer) contentType.getValueHandler();
        TypeDeserializer typeDeserializer = (TypeDeserializer) contentType.getTypeHandler();
        if (typeDeserializer == null) {
            findTypeDeserializer = findTypeDeserializer(config, contentType);
        } else {
            findTypeDeserializer = typeDeserializer;
        }
        JsonDeserializer<?> _findCustomArrayDeserializer = _findCustomArrayDeserializer(arrayType, config, beanDescription, findTypeDeserializer, jsonDeserializer);
        if (_findCustomArrayDeserializer == null) {
            if (jsonDeserializer == null) {
                Class rawClass = contentType.getRawClass();
                if (contentType.isPrimitive()) {
                    return PrimitiveArrayDeserializers.forType(rawClass);
                }
                if (rawClass == String.class) {
                    return StringArrayDeserializer.instance;
                }
            }
            if (_findCustomArrayDeserializer == null) {
                _findCustomArrayDeserializer = new ObjectArrayDeserializer(arrayType, jsonDeserializer, findTypeDeserializer);
            }
        }
        if (!this._factoryConfig.hasDeserializerModifiers()) {
            return _findCustomArrayDeserializer;
        }
        JsonDeserializer<?> jsonDeserializer2 = _findCustomArrayDeserializer;
        for (BeanDeserializerModifier modifyArrayDeserializer : this._factoryConfig.deserializerModifiers()) {
            jsonDeserializer2 = modifyArrayDeserializer.modifyArrayDeserializer(config, arrayType, beanDescription, jsonDeserializer2);
        }
        return jsonDeserializer2;
    }

    protected JsonDeserializer<?> _findCustomArrayDeserializer(ArrayType arrayType, DeserializationConfig deserializationConfig, BeanDescription beanDescription, TypeDeserializer typeDeserializer, JsonDeserializer<?> jsonDeserializer) throws JsonMappingException {
        for (Deserializers findArrayDeserializer : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> findArrayDeserializer2 = findArrayDeserializer.findArrayDeserializer(arrayType, deserializationConfig, beanDescription, typeDeserializer, jsonDeserializer);
            if (findArrayDeserializer2 != null) {
                return findArrayDeserializer2;
            }
        }
        return null;
    }

    public JsonDeserializer<?> createCollectionDeserializer(DeserializationContext deserializationContext, CollectionType collectionType, BeanDescription beanDescription) throws JsonMappingException {
        TypeDeserializer findTypeDeserializer;
        CollectionType _mapAbstractCollectionType;
        JsonDeserializer<?> jsonDeserializer;
        JavaType contentType = collectionType.getContentType();
        JsonDeserializer jsonDeserializer2 = (JsonDeserializer) contentType.getValueHandler();
        DeserializationConfig config = deserializationContext.getConfig();
        TypeDeserializer typeDeserializer = (TypeDeserializer) contentType.getTypeHandler();
        if (typeDeserializer == null) {
            findTypeDeserializer = findTypeDeserializer(config, contentType);
        } else {
            findTypeDeserializer = typeDeserializer;
        }
        JsonDeserializer<?> _findCustomCollectionDeserializer = _findCustomCollectionDeserializer(collectionType, config, beanDescription, findTypeDeserializer, jsonDeserializer2);
        if (_findCustomCollectionDeserializer == null) {
            Class rawClass = collectionType.getRawClass();
            if (jsonDeserializer2 == null && EnumSet.class.isAssignableFrom(rawClass)) {
                _findCustomCollectionDeserializer = new EnumSetDeserializer(contentType, null);
            }
        }
        if (_findCustomCollectionDeserializer == null) {
            if (collectionType.isInterface() || collectionType.isAbstract()) {
                _mapAbstractCollectionType = _mapAbstractCollectionType(collectionType, config);
                if (_mapAbstractCollectionType == null) {
                    throw new IllegalArgumentException("Can not find a deserializer for non-concrete Collection type " + collectionType);
                }
                beanDescription = config.introspectForCreation(_mapAbstractCollectionType);
            } else {
                _mapAbstractCollectionType = collectionType;
            }
            ValueInstantiator findValueInstantiator = findValueInstantiator(deserializationContext, beanDescription);
            if (!findValueInstantiator.canCreateUsingDefault() && _mapAbstractCollectionType.getRawClass() == ArrayBlockingQueue.class) {
                return new ArrayBlockingQueueDeserializer(_mapAbstractCollectionType, jsonDeserializer2, findTypeDeserializer, findValueInstantiator, null);
            }
            if (contentType.getRawClass() == String.class) {
                _findCustomCollectionDeserializer = new StringCollectionDeserializer(_mapAbstractCollectionType, jsonDeserializer2, findValueInstantiator);
            } else {
                _findCustomCollectionDeserializer = new CollectionDeserializer(_mapAbstractCollectionType, jsonDeserializer2, findTypeDeserializer, findValueInstantiator);
            }
        } else {
            _mapAbstractCollectionType = collectionType;
        }
        if (this._factoryConfig.hasDeserializerModifiers()) {
            jsonDeserializer = _findCustomCollectionDeserializer;
            for (BeanDeserializerModifier modifyCollectionDeserializer : this._factoryConfig.deserializerModifiers()) {
                jsonDeserializer = modifyCollectionDeserializer.modifyCollectionDeserializer(config, _mapAbstractCollectionType, beanDescription, jsonDeserializer);
            }
        } else {
            jsonDeserializer = _findCustomCollectionDeserializer;
        }
        return jsonDeserializer;
    }

    protected CollectionType _mapAbstractCollectionType(JavaType javaType, DeserializationConfig deserializationConfig) {
        Class cls = (Class) _collectionFallbacks.get(javaType.getRawClass().getName());
        if (cls == null) {
            return null;
        }
        return (CollectionType) deserializationConfig.constructSpecializedType(javaType, cls);
    }

    protected JsonDeserializer<?> _findCustomCollectionDeserializer(CollectionType collectionType, DeserializationConfig deserializationConfig, BeanDescription beanDescription, TypeDeserializer typeDeserializer, JsonDeserializer<?> jsonDeserializer) throws JsonMappingException {
        for (Deserializers findCollectionDeserializer : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> findCollectionDeserializer2 = findCollectionDeserializer.findCollectionDeserializer(collectionType, deserializationConfig, beanDescription, typeDeserializer, jsonDeserializer);
            if (findCollectionDeserializer2 != null) {
                return findCollectionDeserializer2;
            }
        }
        return null;
    }

    public JsonDeserializer<?> createCollectionLikeDeserializer(DeserializationContext deserializationContext, CollectionLikeType collectionLikeType, BeanDescription beanDescription) throws JsonMappingException {
        TypeDeserializer findTypeDeserializer;
        JavaType contentType = collectionLikeType.getContentType();
        JsonDeserializer jsonDeserializer = (JsonDeserializer) contentType.getValueHandler();
        DeserializationConfig config = deserializationContext.getConfig();
        TypeDeserializer typeDeserializer = (TypeDeserializer) contentType.getTypeHandler();
        if (typeDeserializer == null) {
            findTypeDeserializer = findTypeDeserializer(config, contentType);
        } else {
            findTypeDeserializer = typeDeserializer;
        }
        JsonDeserializer<?> _findCustomCollectionLikeDeserializer = _findCustomCollectionLikeDeserializer(collectionLikeType, config, beanDescription, findTypeDeserializer, jsonDeserializer);
        if (_findCustomCollectionLikeDeserializer == null || !this._factoryConfig.hasDeserializerModifiers()) {
            return _findCustomCollectionLikeDeserializer;
        }
        JsonDeserializer<?> jsonDeserializer2 = _findCustomCollectionLikeDeserializer;
        for (BeanDeserializerModifier modifyCollectionLikeDeserializer : this._factoryConfig.deserializerModifiers()) {
            jsonDeserializer2 = modifyCollectionLikeDeserializer.modifyCollectionLikeDeserializer(config, collectionLikeType, beanDescription, jsonDeserializer2);
        }
        return jsonDeserializer2;
    }

    protected JsonDeserializer<?> _findCustomCollectionLikeDeserializer(CollectionLikeType collectionLikeType, DeserializationConfig deserializationConfig, BeanDescription beanDescription, TypeDeserializer typeDeserializer, JsonDeserializer<?> jsonDeserializer) throws JsonMappingException {
        for (Deserializers findCollectionLikeDeserializer : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> findCollectionLikeDeserializer2 = findCollectionLikeDeserializer.findCollectionLikeDeserializer(collectionLikeType, deserializationConfig, beanDescription, typeDeserializer, jsonDeserializer);
            if (findCollectionLikeDeserializer2 != null) {
                return findCollectionLikeDeserializer2;
            }
        }
        return null;
    }

    public JsonDeserializer<?> createMapDeserializer(DeserializationContext deserializationContext, MapType mapType, BeanDescription beanDescription) throws JsonMappingException {
        TypeDeserializer findTypeDeserializer;
        DeserializationConfig config = deserializationContext.getConfig();
        JavaType keyType = mapType.getKeyType();
        JavaType contentType = mapType.getContentType();
        JsonDeserializer jsonDeserializer = (JsonDeserializer) contentType.getValueHandler();
        KeyDeserializer keyDeserializer = (KeyDeserializer) keyType.getValueHandler();
        TypeDeserializer typeDeserializer = (TypeDeserializer) contentType.getTypeHandler();
        if (typeDeserializer == null) {
            findTypeDeserializer = findTypeDeserializer(config, contentType);
        } else {
            findTypeDeserializer = typeDeserializer;
        }
        JsonDeserializer<?> _findCustomMapDeserializer = _findCustomMapDeserializer(mapType, config, beanDescription, keyDeserializer, findTypeDeserializer, jsonDeserializer);
        if (_findCustomMapDeserializer == null) {
            Class rawClass = mapType.getRawClass();
            if (EnumMap.class.isAssignableFrom(rawClass)) {
                Class rawClass2 = keyType.getRawClass();
                if (rawClass2 == null || !rawClass2.isEnum()) {
                    throw new IllegalArgumentException("Can not construct EnumMap; generic (key) type not available");
                }
                _findCustomMapDeserializer = new EnumMapDeserializer(mapType, null, jsonDeserializer, findTypeDeserializer);
            }
            if (_findCustomMapDeserializer == null) {
                if (mapType.isInterface() || mapType.isAbstract()) {
                    rawClass = (Class) _mapFallbacks.get(rawClass.getName());
                    if (rawClass == null) {
                        throw new IllegalArgumentException("Can not find a deserializer for non-concrete Map type " + mapType);
                    }
                    JavaType javaType = (MapType) config.constructSpecializedType(mapType, rawClass);
                    beanDescription = config.introspectForCreation(javaType);
                    keyType = javaType;
                } else {
                    keyType = mapType;
                }
                _findCustomMapDeserializer = new MapDeserializer(keyType, findValueInstantiator(deserializationContext, beanDescription), keyDeserializer, jsonDeserializer, findTypeDeserializer);
                _findCustomMapDeserializer.setIgnorableProperties(config.getAnnotationIntrospector().findPropertiesToIgnore(beanDescription.getClassInfo()));
                mapType = keyType;
            }
        }
        if (this._factoryConfig.hasDeserializerModifiers()) {
            for (BeanDeserializerModifier modifyMapDeserializer : this._factoryConfig.deserializerModifiers()) {
                _findCustomMapDeserializer = modifyMapDeserializer.modifyMapDeserializer(config, mapType, beanDescription, _findCustomMapDeserializer);
            }
        }
        return _findCustomMapDeserializer;
    }

    public JsonDeserializer<?> createMapLikeDeserializer(DeserializationContext deserializationContext, MapLikeType mapLikeType, BeanDescription beanDescription) throws JsonMappingException {
        TypeDeserializer findTypeDeserializer;
        JavaType keyType = mapLikeType.getKeyType();
        JavaType contentType = mapLikeType.getContentType();
        DeserializationConfig config = deserializationContext.getConfig();
        JsonDeserializer jsonDeserializer = (JsonDeserializer) contentType.getValueHandler();
        KeyDeserializer keyDeserializer = (KeyDeserializer) keyType.getValueHandler();
        TypeDeserializer typeDeserializer = (TypeDeserializer) contentType.getTypeHandler();
        if (typeDeserializer == null) {
            findTypeDeserializer = findTypeDeserializer(config, contentType);
        } else {
            findTypeDeserializer = typeDeserializer;
        }
        JsonDeserializer<?> _findCustomMapLikeDeserializer = _findCustomMapLikeDeserializer(mapLikeType, config, beanDescription, keyDeserializer, findTypeDeserializer, jsonDeserializer);
        if (_findCustomMapLikeDeserializer == null || !this._factoryConfig.hasDeserializerModifiers()) {
            return _findCustomMapLikeDeserializer;
        }
        JsonDeserializer<?> jsonDeserializer2 = _findCustomMapLikeDeserializer;
        for (BeanDeserializerModifier modifyMapLikeDeserializer : this._factoryConfig.deserializerModifiers()) {
            jsonDeserializer2 = modifyMapLikeDeserializer.modifyMapLikeDeserializer(config, mapLikeType, beanDescription, jsonDeserializer2);
        }
        return jsonDeserializer2;
    }

    protected JsonDeserializer<?> _findCustomMapDeserializer(MapType mapType, DeserializationConfig deserializationConfig, BeanDescription beanDescription, KeyDeserializer keyDeserializer, TypeDeserializer typeDeserializer, JsonDeserializer<?> jsonDeserializer) throws JsonMappingException {
        for (Deserializers findMapDeserializer : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> findMapDeserializer2 = findMapDeserializer.findMapDeserializer(mapType, deserializationConfig, beanDescription, keyDeserializer, typeDeserializer, jsonDeserializer);
            if (findMapDeserializer2 != null) {
                return findMapDeserializer2;
            }
        }
        return null;
    }

    protected JsonDeserializer<?> _findCustomMapLikeDeserializer(MapLikeType mapLikeType, DeserializationConfig deserializationConfig, BeanDescription beanDescription, KeyDeserializer keyDeserializer, TypeDeserializer typeDeserializer, JsonDeserializer<?> jsonDeserializer) throws JsonMappingException {
        for (Deserializers findMapLikeDeserializer : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> findMapLikeDeserializer2 = findMapLikeDeserializer.findMapLikeDeserializer(mapLikeType, deserializationConfig, beanDescription, keyDeserializer, typeDeserializer, jsonDeserializer);
            if (findMapLikeDeserializer2 != null) {
                return findMapLikeDeserializer2;
            }
        }
        return null;
    }

    public JsonDeserializer<?> createEnumDeserializer(DeserializationContext deserializationContext, JavaType javaType, BeanDescription beanDescription) throws JsonMappingException {
        JsonDeserializer<?> deserializerForCreator;
        DeserializationConfig config = deserializationContext.getConfig();
        Class rawClass = javaType.getRawClass();
        JsonDeserializer<?> _findCustomEnumDeserializer = _findCustomEnumDeserializer(rawClass, config, beanDescription);
        if (_findCustomEnumDeserializer == null) {
            for (AnnotatedMethod annotatedMethod : beanDescription.getFactoryMethods()) {
                if (deserializationContext.getAnnotationIntrospector().hasCreatorAnnotation(annotatedMethod)) {
                    if (annotatedMethod.getParameterCount() == 1 && annotatedMethod.getRawReturnType().isAssignableFrom(rawClass)) {
                        deserializerForCreator = EnumDeserializer.deserializerForCreator(config, rawClass, annotatedMethod);
                        if (deserializerForCreator == null) {
                            deserializerForCreator = new EnumDeserializer(constructEnumResolver(rawClass, config, beanDescription.findJsonValueMethod()));
                        }
                    } else {
                        throw new IllegalArgumentException("Unsuitable method (" + annotatedMethod + ") decorated with @JsonCreator (for Enum type " + rawClass.getName() + ")");
                    }
                }
            }
            deserializerForCreator = _findCustomEnumDeserializer;
            if (deserializerForCreator == null) {
                deserializerForCreator = new EnumDeserializer(constructEnumResolver(rawClass, config, beanDescription.findJsonValueMethod()));
            }
        } else {
            deserializerForCreator = _findCustomEnumDeserializer;
        }
        if (!this._factoryConfig.hasDeserializerModifiers()) {
            return deserializerForCreator;
        }
        _findCustomEnumDeserializer = deserializerForCreator;
        for (BeanDeserializerModifier modifyEnumDeserializer : this._factoryConfig.deserializerModifiers()) {
            _findCustomEnumDeserializer = modifyEnumDeserializer.modifyEnumDeserializer(config, javaType, beanDescription, _findCustomEnumDeserializer);
        }
        return _findCustomEnumDeserializer;
    }

    protected JsonDeserializer<?> _findCustomEnumDeserializer(Class<?> cls, DeserializationConfig deserializationConfig, BeanDescription beanDescription) throws JsonMappingException {
        for (Deserializers findEnumDeserializer : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> findEnumDeserializer2 = findEnumDeserializer.findEnumDeserializer(cls, deserializationConfig, beanDescription);
            if (findEnumDeserializer2 != null) {
                return findEnumDeserializer2;
            }
        }
        return null;
    }

    public JsonDeserializer<?> createTreeDeserializer(DeserializationConfig deserializationConfig, JavaType javaType, BeanDescription beanDescription) throws JsonMappingException {
        Class rawClass = javaType.getRawClass();
        JsonDeserializer<?> _findCustomTreeNodeDeserializer = _findCustomTreeNodeDeserializer(rawClass, deserializationConfig, beanDescription);
        return _findCustomTreeNodeDeserializer != null ? _findCustomTreeNodeDeserializer : JsonNodeDeserializer.getDeserializer(rawClass);
    }

    protected JsonDeserializer<?> _findCustomTreeNodeDeserializer(Class<? extends JsonNode> cls, DeserializationConfig deserializationConfig, BeanDescription beanDescription) throws JsonMappingException {
        for (Deserializers findTreeNodeDeserializer : this._factoryConfig.deserializers()) {
            JsonDeserializer<?> findTreeNodeDeserializer2 = findTreeNodeDeserializer.findTreeNodeDeserializer(cls, deserializationConfig, beanDescription);
            if (findTreeNodeDeserializer2 != null) {
                return findTreeNodeDeserializer2;
            }
        }
        return null;
    }

    public TypeDeserializer findTypeDeserializer(DeserializationConfig deserializationConfig, JavaType javaType) throws JsonMappingException {
        Collection collection = null;
        AnnotatedClass classInfo = deserializationConfig.introspectClassAnnotations(javaType.getRawClass()).getClassInfo();
        AnnotationIntrospector annotationIntrospector = deserializationConfig.getAnnotationIntrospector();
        TypeResolverBuilder findTypeResolver = annotationIntrospector.findTypeResolver(deserializationConfig, classInfo, javaType);
        if (findTypeResolver == null) {
            findTypeResolver = deserializationConfig.getDefaultTyper(javaType);
            if (findTypeResolver == null) {
                return null;
            }
        }
        collection = deserializationConfig.getSubtypeResolver().collectAndResolveSubtypes(classInfo, (MapperConfig) deserializationConfig, annotationIntrospector);
        if (findTypeResolver.getDefaultImpl() == null && javaType.isAbstract()) {
            JavaType mapAbstractType = mapAbstractType(deserializationConfig, javaType);
            if (!(mapAbstractType == null || mapAbstractType.getRawClass() == javaType.getRawClass())) {
                findTypeResolver = findTypeResolver.defaultImpl(mapAbstractType.getRawClass());
            }
        }
        return findTypeResolver.buildTypeDeserializer(deserializationConfig, javaType, collection);
    }

    public KeyDeserializer createKeyDeserializer(DeserializationContext deserializationContext, JavaType javaType) throws JsonMappingException {
        DeserializationConfig config = deserializationContext.getConfig();
        KeyDeserializer keyDeserializer = null;
        if (this._factoryConfig.hasKeyDeserializers()) {
            BeanDescription introspectClassAnnotations = config.introspectClassAnnotations(javaType.getRawClass());
            for (KeyDeserializers findKeyDeserializer : this._factoryConfig.keyDeserializers()) {
                keyDeserializer = findKeyDeserializer.findKeyDeserializer(javaType, config, introspectClassAnnotations);
                if (keyDeserializer != null) {
                    break;
                }
            }
        }
        if (keyDeserializer == null) {
            if (javaType.isEnumType()) {
                return _createEnumKeyDeserializer(deserializationContext, javaType);
            }
            keyDeserializer = StdKeyDeserializers.findStringBasedKeyDeserializer(config, javaType);
        }
        if (keyDeserializer == null || !this._factoryConfig.hasDeserializerModifiers()) {
            return keyDeserializer;
        }
        KeyDeserializer keyDeserializer2 = keyDeserializer;
        for (BeanDeserializerModifier modifyKeyDeserializer : this._factoryConfig.deserializerModifiers()) {
            keyDeserializer2 = modifyKeyDeserializer.modifyKeyDeserializer(config, javaType, keyDeserializer2);
        }
        return keyDeserializer2;
    }

    private KeyDeserializer _createEnumKeyDeserializer(DeserializationContext deserializationContext, JavaType javaType) throws JsonMappingException {
        DeserializationConfig config = deserializationContext.getConfig();
        BeanDescription introspect = config.introspect(javaType);
        JsonDeserializer findDeserializerFromAnnotation = findDeserializerFromAnnotation(deserializationContext, introspect.getClassInfo());
        if (findDeserializerFromAnnotation != null) {
            return StdKeyDeserializers.constructDelegatingKeyDeserializer(config, javaType, findDeserializerFromAnnotation);
        }
        Class rawClass = javaType.getRawClass();
        if (_findCustomEnumDeserializer(rawClass, config, introspect) != null) {
            return StdKeyDeserializers.constructDelegatingKeyDeserializer(config, javaType, findDeserializerFromAnnotation);
        }
        EnumResolver constructEnumResolver = constructEnumResolver(rawClass, config, introspect.findJsonValueMethod());
        for (AnnotatedMethod annotatedMethod : introspect.getFactoryMethods()) {
            if (config.getAnnotationIntrospector().hasCreatorAnnotation(annotatedMethod)) {
                if (annotatedMethod.getParameterCount() != 1 || !annotatedMethod.getRawReturnType().isAssignableFrom(rawClass)) {
                    throw new IllegalArgumentException("Unsuitable method (" + annotatedMethod + ") decorated with @JsonCreator (for Enum type " + rawClass.getName() + ")");
                } else if (annotatedMethod.getGenericParameterType(0) != String.class) {
                    throw new IllegalArgumentException("Parameter #0 type for factory method (" + annotatedMethod + ") not suitable, must be java.lang.String");
                } else {
                    if (config.canOverrideAccessModifiers()) {
                        ClassUtil.checkAndFixAccess(annotatedMethod.getMember());
                    }
                    return StdKeyDeserializers.constructEnumKeyDeserializer(constructEnumResolver, annotatedMethod);
                }
            }
        }
        return StdKeyDeserializers.constructEnumKeyDeserializer(constructEnumResolver);
    }

    public TypeDeserializer findPropertyTypeDeserializer(DeserializationConfig deserializationConfig, JavaType javaType, AnnotatedMember annotatedMember) throws JsonMappingException {
        AnnotationIntrospector annotationIntrospector = deserializationConfig.getAnnotationIntrospector();
        TypeResolverBuilder findPropertyTypeResolver = annotationIntrospector.findPropertyTypeResolver(deserializationConfig, annotatedMember, javaType);
        if (findPropertyTypeResolver == null) {
            return findTypeDeserializer(deserializationConfig, javaType);
        }
        return findPropertyTypeResolver.buildTypeDeserializer(deserializationConfig, javaType, deserializationConfig.getSubtypeResolver().collectAndResolveSubtypes(annotatedMember, deserializationConfig, annotationIntrospector, javaType));
    }

    public TypeDeserializer findPropertyContentTypeDeserializer(DeserializationConfig deserializationConfig, JavaType javaType, AnnotatedMember annotatedMember) throws JsonMappingException {
        AnnotationIntrospector annotationIntrospector = deserializationConfig.getAnnotationIntrospector();
        TypeResolverBuilder findPropertyContentTypeResolver = annotationIntrospector.findPropertyContentTypeResolver(deserializationConfig, annotatedMember, javaType);
        JavaType contentType = javaType.getContentType();
        if (findPropertyContentTypeResolver == null) {
            return findTypeDeserializer(deserializationConfig, contentType);
        }
        return findPropertyContentTypeResolver.buildTypeDeserializer(deserializationConfig, contentType, deserializationConfig.getSubtypeResolver().collectAndResolveSubtypes(annotatedMember, deserializationConfig, annotationIntrospector, contentType));
    }

    public JsonDeserializer<?> findDefaultDeserializer(DeserializationContext deserializationContext, JavaType javaType, BeanDescription beanDescription) throws JsonMappingException {
        Class rawClass = javaType.getRawClass();
        String name = rawClass.getName();
        if (rawClass.isPrimitive() || name.startsWith("java.")) {
            if (rawClass == CLASS_OBJECT) {
                return UntypedObjectDeserializer.instance;
            }
            if (rawClass == CLASS_STRING || rawClass == CLASS_CHAR_BUFFER) {
                return StringDeserializer.instance;
            }
            if (rawClass == CLASS_ITERABLE) {
                return createCollectionDeserializer(deserializationContext, deserializationContext.getTypeFactory().constructCollectionType(Collection.class, javaType.containedTypeCount() > 0 ? javaType.containedType(0) : TypeFactory.unknownType()), beanDescription);
            }
            JsonDeserializer<?> find = NumberDeserializers.find(rawClass, name);
            if (find != null) {
                return find;
            }
            find = DateDeserializers.find(rawClass, name);
            if (find == null) {
                return JdkDeserializers.find(rawClass, name);
            }
            return find;
        } else if (name.startsWith("com.fasterxml.")) {
            return JacksonDeserializers.find(rawClass);
        } else {
            return null;
        }
    }

    protected JsonDeserializer<Object> findDeserializerFromAnnotation(DeserializationContext deserializationContext, Annotated annotated) throws JsonMappingException {
        Object findDeserializer = deserializationContext.getAnnotationIntrospector().findDeserializer(annotated);
        if (findDeserializer == null) {
            return null;
        }
        return deserializationContext.deserializerInstance(annotated, findDeserializer);
    }

    protected <T extends JavaType> T modifyTypeByAnnotation(DeserializationContext deserializationContext, Annotated annotated, T t) throws JsonMappingException {
        AnnotationIntrospector annotationIntrospector = deserializationContext.getAnnotationIntrospector();
        Class findDeserializationType = annotationIntrospector.findDeserializationType(annotated, t);
        if (findDeserializationType != null) {
            try {
                T narrowBy = t.narrowBy(findDeserializationType);
            } catch (Throwable e) {
                throw new JsonMappingException("Failed to narrow type " + t + " with concrete-type annotation (value " + findDeserializationType.getName() + "), method '" + annotated.getName() + "': " + e.getMessage(), null, e);
            }
        }
        narrowBy = t;
        if (!narrowBy.isContainerType()) {
            return narrowBy;
        }
        T t2;
        Class findDeserializationKeyType = annotationIntrospector.findDeserializationKeyType(annotated, narrowBy.getKeyType());
        if (findDeserializationKeyType == null) {
            t2 = narrowBy;
        } else if (narrowBy instanceof MapLikeType) {
            try {
                t2 = ((MapLikeType) narrowBy).narrowKey(findDeserializationKeyType);
            } catch (Throwable e2) {
                throw new JsonMappingException("Failed to narrow key type " + narrowBy + " with key-type annotation (" + findDeserializationKeyType.getName() + "): " + e2.getMessage(), null, e2);
            }
        } else {
            throw new JsonMappingException("Illegal key-type annotation: type " + narrowBy + " is not a Map(-like) type");
        }
        JavaType keyType = t2.getKeyType();
        if (keyType != null && keyType.getValueHandler() == null) {
            KeyDeserializer keyDeserializerInstance = deserializationContext.keyDeserializerInstance(annotated, annotationIntrospector.findKeyDeserializer(annotated));
            if (keyDeserializerInstance != null) {
                t2 = ((MapLikeType) t2).withKeyValueHandler(keyDeserializerInstance);
                t2.getKeyType();
            }
        }
        Class findDeserializationContentType = annotationIntrospector.findDeserializationContentType(annotated, t2.getContentType());
        if (findDeserializationContentType != null) {
            try {
                t2 = t2.narrowContentsBy(findDeserializationContentType);
            } catch (Throwable e3) {
                throw new JsonMappingException("Failed to narrow content type " + t2 + " with content-type annotation (" + findDeserializationContentType.getName() + "): " + e3.getMessage(), null, e3);
            }
        }
        if (t2.getContentType().getValueHandler() != null) {
            return t2;
        }
        JsonDeserializer deserializerInstance = deserializationContext.deserializerInstance(annotated, annotationIntrospector.findContentDeserializer(annotated));
        if (deserializerInstance != null) {
            return t2.withContentValueHandler(deserializerInstance);
        }
        return t2;
    }

    protected JavaType resolveType(DeserializationContext deserializationContext, BeanDescription beanDescription, JavaType javaType, AnnotatedMember annotatedMember) throws JsonMappingException {
        Object findPropertyTypeDeserializer;
        if (javaType.isContainerType()) {
            AnnotationIntrospector annotationIntrospector = deserializationContext.getAnnotationIntrospector();
            if (javaType.getKeyType() != null) {
                KeyDeserializer keyDeserializerInstance = deserializationContext.keyDeserializerInstance(annotatedMember, annotationIntrospector.findKeyDeserializer(annotatedMember));
                if (keyDeserializerInstance != null) {
                    javaType = ((MapLikeType) javaType).withKeyValueHandler(keyDeserializerInstance);
                    javaType.getKeyType();
                }
            }
            JsonDeserializer deserializerInstance = deserializationContext.deserializerInstance(annotatedMember, annotationIntrospector.findContentDeserializer(annotatedMember));
            if (deserializerInstance != null) {
                javaType = javaType.withContentValueHandler(deserializerInstance);
            }
            if (annotatedMember instanceof AnnotatedMember) {
                TypeDeserializer findPropertyContentTypeDeserializer = findPropertyContentTypeDeserializer(deserializationContext.getConfig(), javaType, annotatedMember);
                if (findPropertyContentTypeDeserializer != null) {
                    javaType = javaType.withContentTypeHandler(findPropertyContentTypeDeserializer);
                }
            }
        }
        if (annotatedMember instanceof AnnotatedMember) {
            findPropertyTypeDeserializer = findPropertyTypeDeserializer(deserializationContext.getConfig(), javaType, annotatedMember);
        } else {
            findPropertyTypeDeserializer = findTypeDeserializer(deserializationContext.getConfig(), javaType);
        }
        if (findPropertyTypeDeserializer != null) {
            return javaType.withTypeHandler(findPropertyTypeDeserializer);
        }
        return javaType;
    }

    protected EnumResolver<?> constructEnumResolver(Class<?> cls, DeserializationConfig deserializationConfig, AnnotatedMethod annotatedMethod) {
        if (annotatedMethod != null) {
            Object annotated = annotatedMethod.getAnnotated();
            if (deserializationConfig.canOverrideAccessModifiers()) {
                ClassUtil.checkAndFixAccess(annotated);
            }
            return EnumResolver.constructUnsafeUsingMethod(cls, annotated);
        } else if (deserializationConfig.isEnabled(DeserializationFeature.READ_ENUMS_USING_TO_STRING)) {
            return EnumResolver.constructUnsafeUsingToString(cls);
        } else {
            return EnumResolver.constructUnsafe(cls, deserializationConfig.getAnnotationIntrospector());
        }
    }

    protected AnnotatedMethod _findJsonValueFor(DeserializationConfig deserializationConfig, JavaType javaType) {
        if (javaType == null) {
            return null;
        }
        return deserializationConfig.introspect(javaType).findJsonValueMethod();
    }
}
