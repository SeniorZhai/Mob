package com.fasterxml.jackson.databind.introspect;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.util.BeanUtil;
import com.fasterxml.jackson.databind.util.ClassUtil;
import io.fabric.sdk.android.BuildConfig;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class POJOPropertiesCollector {
    protected final AnnotationIntrospector _annotationIntrospector;
    protected LinkedList<AnnotatedMember> _anyGetters = null;
    protected LinkedList<AnnotatedMethod> _anySetters = null;
    protected final AnnotatedClass _classDef;
    protected final MapperConfig<?> _config;
    protected LinkedList<POJOPropertyBuilder> _creatorProperties = null;
    protected final boolean _forSerialization;
    protected HashSet<String> _ignoredPropertyNames;
    protected LinkedHashMap<Object, AnnotatedMember> _injectables;
    protected LinkedList<AnnotatedMethod> _jsonValueGetters = null;
    protected final String _mutatorPrefix;
    protected final LinkedHashMap<String, POJOPropertyBuilder> _properties = new LinkedHashMap();
    protected final JavaType _type;
    protected final VisibilityChecker<?> _visibilityChecker;

    protected POJOPropertiesCollector(MapperConfig<?> mapperConfig, boolean z, JavaType javaType, AnnotatedClass annotatedClass, String str) {
        AnnotationIntrospector annotationIntrospector = null;
        this._config = mapperConfig;
        this._forSerialization = z;
        this._type = javaType;
        this._classDef = annotatedClass;
        if (str == null) {
            str = "set";
        }
        this._mutatorPrefix = str;
        if (mapperConfig.isAnnotationProcessingEnabled()) {
            annotationIntrospector = this._config.getAnnotationIntrospector();
        }
        this._annotationIntrospector = annotationIntrospector;
        if (this._annotationIntrospector == null) {
            this._visibilityChecker = this._config.getDefaultVisibilityChecker();
        } else {
            this._visibilityChecker = this._annotationIntrospector.findAutoDetectVisibility(annotatedClass, this._config.getDefaultVisibilityChecker());
        }
    }

    public MapperConfig<?> getConfig() {
        return this._config;
    }

    public JavaType getType() {
        return this._type;
    }

    public AnnotatedClass getClassDef() {
        return this._classDef;
    }

    public AnnotationIntrospector getAnnotationIntrospector() {
        return this._annotationIntrospector;
    }

    public List<BeanPropertyDefinition> getProperties() {
        return new ArrayList(this._properties.values());
    }

    public Map<Object, AnnotatedMember> getInjectables() {
        return this._injectables;
    }

    public AnnotatedMethod getJsonValueMethod() {
        if (this._jsonValueGetters == null) {
            return null;
        }
        if (this._jsonValueGetters.size() > 1) {
            reportProblem("Multiple value properties defined (" + this._jsonValueGetters.get(0) + " vs " + this._jsonValueGetters.get(1) + ")");
        }
        return (AnnotatedMethod) this._jsonValueGetters.get(0);
    }

    public AnnotatedMember getAnyGetter() {
        if (this._anyGetters == null) {
            return null;
        }
        if (this._anyGetters.size() > 1) {
            reportProblem("Multiple 'any-getters' defined (" + this._anyGetters.get(0) + " vs " + this._anyGetters.get(1) + ")");
        }
        return (AnnotatedMember) this._anyGetters.getFirst();
    }

    public AnnotatedMethod getAnySetterMethod() {
        if (this._anySetters == null) {
            return null;
        }
        if (this._anySetters.size() > 1) {
            reportProblem("Multiple 'any-setters' defined (" + this._anySetters.get(0) + " vs " + this._anySetters.get(1) + ")");
        }
        return (AnnotatedMethod) this._anySetters.getFirst();
    }

    public Set<String> getIgnoredPropertyNames() {
        return this._ignoredPropertyNames;
    }

    public ObjectIdInfo getObjectIdInfo() {
        if (this._annotationIntrospector == null) {
            return null;
        }
        ObjectIdInfo findObjectIdInfo = this._annotationIntrospector.findObjectIdInfo(this._classDef);
        if (findObjectIdInfo != null) {
            return this._annotationIntrospector.findObjectReferenceInfo(this._classDef, findObjectIdInfo);
        }
        return findObjectIdInfo;
    }

    public Class<?> findPOJOBuilderClass() {
        return this._annotationIntrospector.findPOJOBuilder(this._classDef);
    }

    protected Map<String, POJOPropertyBuilder> getPropertyMap() {
        return this._properties;
    }

    public POJOPropertiesCollector collect() {
        this._properties.clear();
        _addFields();
        _addMethods();
        _addCreators();
        _addInjectables();
        _removeUnwantedProperties();
        _renameProperties();
        PropertyNamingStrategy _findNamingStrategy = _findNamingStrategy();
        if (_findNamingStrategy != null) {
            _renameUsing(_findNamingStrategy);
        }
        for (POJOPropertyBuilder trimByVisibility : this._properties.values()) {
            trimByVisibility.trimByVisibility();
        }
        for (POJOPropertyBuilder trimByVisibility2 : this._properties.values()) {
            trimByVisibility2.mergeAnnotations(this._forSerialization);
        }
        if (this._config.isEnabled(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME)) {
            _renameWithWrappers();
        }
        _sortProperties();
        return this;
    }

    protected void _sortProperties() {
        boolean shouldSortPropertiesAlphabetically;
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        Boolean findSerializationSortAlphabetically = annotationIntrospector == null ? null : annotationIntrospector.findSerializationSortAlphabetically(this._classDef);
        if (findSerializationSortAlphabetically == null) {
            shouldSortPropertiesAlphabetically = this._config.shouldSortPropertiesAlphabetically();
        } else {
            shouldSortPropertiesAlphabetically = findSerializationSortAlphabetically.booleanValue();
        }
        String[] findSerializationPropertyOrder = annotationIntrospector == null ? null : annotationIntrospector.findSerializationPropertyOrder(this._classDef);
        if (shouldSortPropertiesAlphabetically || this._creatorProperties != null || findSerializationPropertyOrder != null) {
            Map treeMap;
            Iterator it;
            POJOPropertyBuilder pOJOPropertyBuilder;
            int size = this._properties.size();
            if (shouldSortPropertiesAlphabetically) {
                treeMap = new TreeMap();
            } else {
                Object linkedHashMap = new LinkedHashMap(size + size);
            }
            for (POJOPropertyBuilder pOJOPropertyBuilder2 : this._properties.values()) {
                treeMap.put(pOJOPropertyBuilder2.getName(), pOJOPropertyBuilder2);
            }
            Map linkedHashMap2 = new LinkedHashMap(size + size);
            if (findSerializationPropertyOrder != null) {
                for (String str : findSerializationPropertyOrder) {
                    Object name;
                    Object obj = (POJOPropertyBuilder) treeMap.get(str);
                    if (obj == null) {
                        for (POJOPropertyBuilder pOJOPropertyBuilder3 : this._properties.values()) {
                            if (str.equals(pOJOPropertyBuilder3.getInternalName())) {
                                POJOPropertyBuilder pOJOPropertyBuilder4 = pOJOPropertyBuilder3;
                                name = pOJOPropertyBuilder3.getName();
                                obj = pOJOPropertyBuilder4;
                                break;
                            }
                        }
                    }
                    String str2 = str;
                    if (obj != null) {
                        linkedHashMap2.put(name, obj);
                    }
                }
            }
            if (this._creatorProperties != null) {
                it = this._creatorProperties.iterator();
                while (it.hasNext()) {
                    pOJOPropertyBuilder2 = (POJOPropertyBuilder) it.next();
                    linkedHashMap2.put(pOJOPropertyBuilder2.getName(), pOJOPropertyBuilder2);
                }
            }
            linkedHashMap2.putAll(treeMap);
            this._properties.clear();
            this._properties.putAll(linkedHashMap2);
        }
    }

    protected void _addFields() {
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        Object obj = (this._forSerialization || this._config.isEnabled(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)) ? null : 1;
        for (AnnotatedField annotatedField : this._classDef.fields()) {
            String str;
            String str2;
            boolean z;
            boolean z2;
            String name = annotatedField.getName();
            if (annotationIntrospector == null) {
                str = null;
            } else if (this._forSerialization) {
                r4 = annotationIntrospector.findNameForSerialization(annotatedField);
                str = r4 == null ? null : r4.getSimpleName();
            } else {
                r4 = annotationIntrospector.findNameForDeserialization(annotatedField);
                str = r4 == null ? null : r4.getSimpleName();
            }
            if (BuildConfig.FLAVOR.equals(str)) {
                str2 = name;
            } else {
                str2 = str;
            }
            if (str2 != null) {
                z = true;
            } else {
                z = false;
            }
            if (z) {
                z2 = z;
            } else {
                z2 = this._visibilityChecker.isFieldVisible(annotatedField);
            }
            if (annotationIntrospector == null || !annotationIntrospector.hasIgnoreMarker(annotatedField)) {
                z = false;
            } else {
                z = true;
            }
            if (obj == null || str2 != null || z || !Modifier.isFinal(annotatedField.getModifiers())) {
                _property(name).addField(annotatedField, str2, z2, z);
            }
        }
    }

    protected void _addCreators() {
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        if (annotationIntrospector != null) {
            int parameterCount;
            int i;
            Annotated parameter;
            PropertyName findNameForDeserialization;
            String simpleName;
            POJOPropertyBuilder _property;
            for (AnnotatedConstructor annotatedConstructor : this._classDef.getConstructors()) {
                if (this._creatorProperties == null) {
                    this._creatorProperties = new LinkedList();
                }
                parameterCount = annotatedConstructor.getParameterCount();
                for (i = 0; i < parameterCount; i++) {
                    parameter = annotatedConstructor.getParameter(i);
                    findNameForDeserialization = annotationIntrospector.findNameForDeserialization(parameter);
                    simpleName = findNameForDeserialization == null ? null : findNameForDeserialization.getSimpleName();
                    if (simpleName != null) {
                        _property = _property(simpleName);
                        _property.addCtor(parameter, simpleName, true, false);
                        this._creatorProperties.add(_property);
                    }
                }
            }
            for (AnnotatedMethod annotatedMethod : this._classDef.getStaticMethods()) {
                if (this._creatorProperties == null) {
                    this._creatorProperties = new LinkedList();
                }
                parameterCount = annotatedMethod.getParameterCount();
                for (i = 0; i < parameterCount; i++) {
                    parameter = annotatedMethod.getParameter(i);
                    findNameForDeserialization = annotationIntrospector.findNameForDeserialization(parameter);
                    simpleName = findNameForDeserialization == null ? null : findNameForDeserialization.getSimpleName();
                    if (simpleName != null) {
                        _property = _property(simpleName);
                        _property.addCtor(parameter, simpleName, true, false);
                        this._creatorProperties.add(_property);
                    }
                }
            }
        }
    }

    protected void _addMethods() {
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        for (AnnotatedMethod annotatedMethod : this._classDef.memberMethods()) {
            int parameterCount = annotatedMethod.getParameterCount();
            if (parameterCount == 0) {
                _addGetterMethod(annotatedMethod, annotationIntrospector);
            } else if (parameterCount == 1) {
                _addSetterMethod(annotatedMethod, annotationIntrospector);
            } else if (parameterCount == 2 && annotationIntrospector != null && annotationIntrospector.hasAnySetterAnnotation(annotatedMethod)) {
                if (this._anySetters == null) {
                    this._anySetters = new LinkedList();
                }
                this._anySetters.add(annotatedMethod);
            }
        }
    }

    protected void _addGetterMethod(AnnotatedMethod annotatedMethod, AnnotationIntrospector annotationIntrospector) {
        String str;
        boolean isIsGetterVisible;
        String str2 = null;
        if (annotationIntrospector != null) {
            if (annotationIntrospector.hasAnyGetterAnnotation(annotatedMethod)) {
                if (this._anyGetters == null) {
                    this._anyGetters = new LinkedList();
                }
                this._anyGetters.add(annotatedMethod);
                return;
            } else if (annotationIntrospector.hasAsValueAnnotation(annotatedMethod)) {
                if (this._jsonValueGetters == null) {
                    this._jsonValueGetters = new LinkedList();
                }
                this._jsonValueGetters.add(annotatedMethod);
                return;
            }
        }
        PropertyName findNameForSerialization = annotationIntrospector == null ? null : annotationIntrospector.findNameForSerialization(annotatedMethod);
        if (findNameForSerialization != null) {
            str2 = findNameForSerialization.getSimpleName();
        }
        if (str2 == null) {
            String okNameForRegularGetter = BeanUtil.okNameForRegularGetter(annotatedMethod, annotatedMethod.getName());
            if (okNameForRegularGetter == null) {
                okNameForRegularGetter = BeanUtil.okNameForIsGetter(annotatedMethod, annotatedMethod.getName());
                if (okNameForRegularGetter != null) {
                    str = okNameForRegularGetter;
                    isIsGetterVisible = this._visibilityChecker.isIsGetterVisible(annotatedMethod);
                } else {
                    return;
                }
            }
            str = okNameForRegularGetter;
            isIsGetterVisible = this._visibilityChecker.isGetterVisible(annotatedMethod);
        } else {
            String okNameForGetter = BeanUtil.okNameForGetter(annotatedMethod);
            if (okNameForGetter == null) {
                okNameForGetter = annotatedMethod.getName();
            }
            if (str2.length() == 0) {
                str2 = okNameForGetter;
            }
            isIsGetterVisible = true;
            str = okNameForGetter;
        }
        _property(str).addGetter(annotatedMethod, str2, isIsGetterVisible, annotationIntrospector == null ? false : annotationIntrospector.hasIgnoreMarker(annotatedMethod));
    }

    protected void _addSetterMethod(AnnotatedMethod annotatedMethod, AnnotationIntrospector annotationIntrospector) {
        String str;
        boolean isSetterVisible;
        String str2 = null;
        PropertyName findNameForDeserialization = annotationIntrospector == null ? null : annotationIntrospector.findNameForDeserialization(annotatedMethod);
        if (findNameForDeserialization != null) {
            str2 = findNameForDeserialization.getSimpleName();
        }
        if (str2 == null) {
            String okNameForMutator = BeanUtil.okNameForMutator(annotatedMethod, this._mutatorPrefix);
            if (okNameForMutator != null) {
                str = okNameForMutator;
                isSetterVisible = this._visibilityChecker.isSetterVisible(annotatedMethod);
            } else {
                return;
            }
        }
        String okNameForMutator2 = BeanUtil.okNameForMutator(annotatedMethod, this._mutatorPrefix);
        if (okNameForMutator2 == null) {
            okNameForMutator2 = annotatedMethod.getName();
        }
        if (str2.length() == 0) {
            str2 = okNameForMutator2;
        }
        isSetterVisible = true;
        str = okNameForMutator2;
        _property(str).addSetter(annotatedMethod, str2, isSetterVisible, annotationIntrospector == null ? false : annotationIntrospector.hasIgnoreMarker(annotatedMethod));
    }

    protected void _addInjectables() {
        AnnotationIntrospector annotationIntrospector = this._annotationIntrospector;
        if (annotationIntrospector != null) {
            for (AnnotatedField annotatedField : this._classDef.fields()) {
                _doAddInjectable(annotationIntrospector.findInjectableValueId(annotatedField), annotatedField);
            }
            for (AnnotatedMethod annotatedMethod : this._classDef.memberMethods()) {
                if (annotatedMethod.getParameterCount() == 1) {
                    _doAddInjectable(annotationIntrospector.findInjectableValueId(annotatedMethod), annotatedMethod);
                }
            }
        }
    }

    protected void _doAddInjectable(Object obj, AnnotatedMember annotatedMember) {
        if (obj != null) {
            if (this._injectables == null) {
                this._injectables = new LinkedHashMap();
            }
            if (((AnnotatedMember) this._injectables.put(obj, annotatedMember)) != null) {
                throw new IllegalArgumentException("Duplicate injectable value with id '" + String.valueOf(obj) + "' (of type " + (obj == null ? "[null]" : obj.getClass().getName()) + ")");
            }
        }
    }

    protected void _removeUnwantedProperties() {
        boolean z;
        Iterator it = this._properties.entrySet().iterator();
        if (this._config.isEnabled(MapperFeature.INFER_PROPERTY_MUTATORS)) {
            z = false;
        } else {
            z = true;
        }
        while (it.hasNext()) {
            POJOPropertyBuilder pOJOPropertyBuilder = (POJOPropertyBuilder) ((Entry) it.next()).getValue();
            if (pOJOPropertyBuilder.anyVisible()) {
                if (pOJOPropertyBuilder.anyIgnorals()) {
                    if (pOJOPropertyBuilder.isExplicitlyIncluded()) {
                        pOJOPropertyBuilder.removeIgnored();
                        if (!(this._forSerialization || pOJOPropertyBuilder.couldDeserialize())) {
                            _addIgnored(pOJOPropertyBuilder.getName());
                        }
                    } else {
                        it.remove();
                        _addIgnored(pOJOPropertyBuilder.getName());
                    }
                }
                pOJOPropertyBuilder.removeNonVisible(z);
            } else {
                it.remove();
            }
        }
    }

    private void _addIgnored(String str) {
        if (!this._forSerialization) {
            if (this._ignoredPropertyNames == null) {
                this._ignoredPropertyNames = new HashSet();
            }
            this._ignoredPropertyNames.add(str);
        }
    }

    protected void _renameProperties() {
        Iterator it = this._properties.entrySet().iterator();
        LinkedList linkedList = null;
        while (it.hasNext()) {
            POJOPropertyBuilder pOJOPropertyBuilder = (POJOPropertyBuilder) ((Entry) it.next()).getValue();
            String findNewName = pOJOPropertyBuilder.findNewName();
            if (findNewName != null) {
                if (linkedList == null) {
                    linkedList = new LinkedList();
                }
                linkedList.add(pOJOPropertyBuilder.withName(findNewName));
                it.remove();
            }
        }
        if (linkedList != null) {
            Iterator it2 = linkedList.iterator();
            while (it2.hasNext()) {
                pOJOPropertyBuilder = (POJOPropertyBuilder) it2.next();
                String name = pOJOPropertyBuilder.getName();
                POJOPropertyBuilder pOJOPropertyBuilder2 = (POJOPropertyBuilder) this._properties.get(name);
                if (pOJOPropertyBuilder2 == null) {
                    this._properties.put(name, pOJOPropertyBuilder);
                } else {
                    pOJOPropertyBuilder2.addAll(pOJOPropertyBuilder);
                }
                if (this._creatorProperties != null) {
                    for (int i = 0; i < this._creatorProperties.size(); i++) {
                        if (((POJOPropertyBuilder) this._creatorProperties.get(i)).getInternalName() == pOJOPropertyBuilder.getInternalName()) {
                            this._creatorProperties.set(i, pOJOPropertyBuilder);
                            break;
                        }
                    }
                }
            }
        }
    }

    protected void _renameUsing(PropertyNamingStrategy propertyNamingStrategy) {
        POJOPropertyBuilder[] pOJOPropertyBuilderArr = (POJOPropertyBuilder[]) this._properties.values().toArray(new POJOPropertyBuilder[this._properties.size()]);
        this._properties.clear();
        for (POJOPropertyBuilder pOJOPropertyBuilder : pOJOPropertyBuilderArr) {
            POJOPropertyBuilder pOJOPropertyBuilder2;
            String nameForGetterMethod;
            String name = pOJOPropertyBuilder2.getName();
            if (this._forSerialization) {
                if (pOJOPropertyBuilder2.hasGetter()) {
                    nameForGetterMethod = propertyNamingStrategy.nameForGetterMethod(this._config, pOJOPropertyBuilder2.getGetter(), name);
                } else {
                    if (pOJOPropertyBuilder2.hasField()) {
                        nameForGetterMethod = propertyNamingStrategy.nameForField(this._config, pOJOPropertyBuilder2.getField(), name);
                    }
                    nameForGetterMethod = name;
                }
            } else if (pOJOPropertyBuilder2.hasSetter()) {
                nameForGetterMethod = propertyNamingStrategy.nameForSetterMethod(this._config, pOJOPropertyBuilder2.getSetter(), name);
            } else if (pOJOPropertyBuilder2.hasConstructorParameter()) {
                nameForGetterMethod = propertyNamingStrategy.nameForConstructorParameter(this._config, pOJOPropertyBuilder2.getConstructorParameter(), name);
            } else if (pOJOPropertyBuilder2.hasField()) {
                nameForGetterMethod = propertyNamingStrategy.nameForField(this._config, pOJOPropertyBuilder2.getField(), name);
            } else {
                if (pOJOPropertyBuilder2.hasGetter()) {
                    nameForGetterMethod = propertyNamingStrategy.nameForGetterMethod(this._config, pOJOPropertyBuilder2.getGetter(), name);
                }
                nameForGetterMethod = name;
            }
            if (!nameForGetterMethod.equals(pOJOPropertyBuilder2.getName())) {
                pOJOPropertyBuilder2 = pOJOPropertyBuilder2.withName(nameForGetterMethod);
            }
            POJOPropertyBuilder pOJOPropertyBuilder3 = (POJOPropertyBuilder) this._properties.get(nameForGetterMethod);
            if (pOJOPropertyBuilder3 == null) {
                this._properties.put(nameForGetterMethod, pOJOPropertyBuilder2);
            } else {
                pOJOPropertyBuilder3.addAll(pOJOPropertyBuilder2);
            }
        }
    }

    protected void _renameWithWrappers() {
        Iterator it = this._properties.entrySet().iterator();
        LinkedList linkedList = null;
        while (it.hasNext()) {
            String simpleName;
            POJOPropertyBuilder pOJOPropertyBuilder = (POJOPropertyBuilder) ((Entry) it.next()).getValue();
            Annotated primaryMember = pOJOPropertyBuilder.getPrimaryMember();
            if (primaryMember != null) {
                PropertyName findWrapperName = this._annotationIntrospector.findWrapperName(primaryMember);
                if (findWrapperName != null && findWrapperName.hasSimpleName()) {
                    simpleName = findWrapperName.getSimpleName();
                    if (!simpleName.equals(pOJOPropertyBuilder.getName())) {
                        if (linkedList == null) {
                            linkedList = new LinkedList();
                        }
                        linkedList.add(pOJOPropertyBuilder.withName(simpleName));
                        it.remove();
                    }
                }
            }
        }
        if (linkedList != null) {
            it = linkedList.iterator();
            while (it.hasNext()) {
                pOJOPropertyBuilder = (POJOPropertyBuilder) it.next();
                simpleName = pOJOPropertyBuilder.getName();
                POJOPropertyBuilder pOJOPropertyBuilder2 = (POJOPropertyBuilder) this._properties.get(simpleName);
                if (pOJOPropertyBuilder2 == null) {
                    this._properties.put(simpleName, pOJOPropertyBuilder);
                } else {
                    pOJOPropertyBuilder2.addAll(pOJOPropertyBuilder);
                }
            }
        }
    }

    protected void reportProblem(String str) {
        throw new IllegalArgumentException("Problem with definition of " + this._classDef + ": " + str);
    }

    protected POJOPropertyBuilder _property(String str) {
        POJOPropertyBuilder pOJOPropertyBuilder = (POJOPropertyBuilder) this._properties.get(str);
        if (pOJOPropertyBuilder != null) {
            return pOJOPropertyBuilder;
        }
        pOJOPropertyBuilder = new POJOPropertyBuilder(str, this._annotationIntrospector, this._forSerialization);
        this._properties.put(str, pOJOPropertyBuilder);
        return pOJOPropertyBuilder;
    }

    private PropertyNamingStrategy _findNamingStrategy() {
        Object findNamingStrategy = this._annotationIntrospector == null ? null : this._annotationIntrospector.findNamingStrategy(this._classDef);
        if (findNamingStrategy == null) {
            return this._config.getPropertyNamingStrategy();
        }
        if (findNamingStrategy instanceof PropertyNamingStrategy) {
            return (PropertyNamingStrategy) findNamingStrategy;
        }
        if (findNamingStrategy instanceof Class) {
            Class cls = (Class) findNamingStrategy;
            if (PropertyNamingStrategy.class.isAssignableFrom(cls)) {
                HandlerInstantiator handlerInstantiator = this._config.getHandlerInstantiator();
                if (handlerInstantiator != null) {
                    PropertyNamingStrategy namingStrategyInstance = handlerInstantiator.namingStrategyInstance(this._config, this._classDef, cls);
                    if (namingStrategyInstance != null) {
                        return namingStrategyInstance;
                    }
                }
                return (PropertyNamingStrategy) ClassUtil.createInstance(cls, this._config.canOverrideAccessModifiers());
            }
            throw new IllegalStateException("AnnotationIntrospector returned Class " + cls.getName() + "; expected Class<PropertyNamingStrategy>");
        }
        throw new IllegalStateException("AnnotationIntrospector returned PropertyNamingStrategy definition of type " + findNamingStrategy.getClass().getName() + "; expected type PropertyNamingStrategy or Class<PropertyNamingStrategy> instead");
    }
}
