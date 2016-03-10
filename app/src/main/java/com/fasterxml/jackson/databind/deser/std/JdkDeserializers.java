package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import io.fabric.sdk.android.BuildConfig;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Currency;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

public class JdkDeserializers {
    private static final HashSet<String> _classNames = new HashSet();

    public static class AtomicBooleanDeserializer extends StdScalarDeserializer<AtomicBoolean> {
        public static final AtomicBooleanDeserializer instance = new AtomicBooleanDeserializer();

        public AtomicBooleanDeserializer() {
            super(AtomicBoolean.class);
        }

        public AtomicBoolean deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return new AtomicBoolean(_parseBooleanPrimitive(jsonParser, deserializationContext));
        }
    }

    public static class AtomicReferenceDeserializer extends StdScalarDeserializer<AtomicReference<?>> implements ContextualDeserializer {
        protected final JavaType _referencedType;
        protected final JsonDeserializer<?> _valueDeserializer;

        public AtomicReferenceDeserializer(JavaType javaType) {
            this(javaType, null);
        }

        public AtomicReferenceDeserializer(JavaType javaType, JsonDeserializer<?> jsonDeserializer) {
            super(AtomicReference.class);
            this._referencedType = javaType;
            this._valueDeserializer = jsonDeserializer;
        }

        public AtomicReference<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return new AtomicReference(this._valueDeserializer.deserialize(jsonParser, deserializationContext));
        }

        public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
            return this._valueDeserializer != null ? this : new AtomicReferenceDeserializer(this._referencedType, deserializationContext.findContextualValueDeserializer(this._referencedType, beanProperty));
        }
    }

    protected static class CharsetDeserializer extends FromStringDeserializer<Charset> {
        public static final CharsetDeserializer instance = new CharsetDeserializer();

        public CharsetDeserializer() {
            super(Charset.class);
        }

        protected Charset _deserialize(String str, DeserializationContext deserializationContext) throws IOException {
            return Charset.forName(str);
        }
    }

    public static class CurrencyDeserializer extends FromStringDeserializer<Currency> {
        public static final CurrencyDeserializer instance = new CurrencyDeserializer();

        public CurrencyDeserializer() {
            super(Currency.class);
        }

        protected Currency _deserialize(String str, DeserializationContext deserializationContext) throws IllegalArgumentException {
            return Currency.getInstance(str);
        }
    }

    public static class FileDeserializer extends FromStringDeserializer<File> {
        public static final FileDeserializer instance = new FileDeserializer();

        public FileDeserializer() {
            super(File.class);
        }

        protected File _deserialize(String str, DeserializationContext deserializationContext) {
            return new File(str);
        }
    }

    protected static class InetAddressDeserializer extends FromStringDeserializer<InetAddress> {
        public static final InetAddressDeserializer instance = new InetAddressDeserializer();

        public InetAddressDeserializer() {
            super(InetAddress.class);
        }

        protected InetAddress _deserialize(String str, DeserializationContext deserializationContext) throws IOException {
            return InetAddress.getByName(str);
        }
    }

    protected static class LocaleDeserializer extends FromStringDeserializer<Locale> {
        public static final LocaleDeserializer instance = new LocaleDeserializer();

        public LocaleDeserializer() {
            super(Locale.class);
        }

        protected Locale _deserialize(String str, DeserializationContext deserializationContext) throws IOException {
            int indexOf = str.indexOf(95);
            if (indexOf < 0) {
                return new Locale(str);
            }
            String substring = str.substring(0, indexOf);
            String substring2 = str.substring(indexOf + 1);
            int indexOf2 = substring2.indexOf(95);
            if (indexOf2 < 0) {
                return new Locale(substring, substring2);
            }
            return new Locale(substring, substring2.substring(0, indexOf2), substring2.substring(indexOf2 + 1));
        }
    }

    public static class PatternDeserializer extends FromStringDeserializer<Pattern> {
        public static final PatternDeserializer instance = new PatternDeserializer();

        public PatternDeserializer() {
            super(Pattern.class);
        }

        protected Pattern _deserialize(String str, DeserializationContext deserializationContext) throws IllegalArgumentException {
            return Pattern.compile(str);
        }
    }

    public static class StackTraceElementDeserializer extends StdScalarDeserializer<StackTraceElement> {
        public static final StackTraceElementDeserializer instance = new StackTraceElementDeserializer();

        public StackTraceElementDeserializer() {
            super(StackTraceElement.class);
        }

        public StackTraceElement deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            JsonToken currentToken = jsonParser.getCurrentToken();
            if (currentToken == JsonToken.START_OBJECT) {
                String str = BuildConfig.FLAVOR;
                String str2 = BuildConfig.FLAVOR;
                String str3 = BuildConfig.FLAVOR;
                int i = -1;
                while (true) {
                    JsonToken nextValue = jsonParser.nextValue();
                    if (nextValue == JsonToken.END_OBJECT) {
                        return new StackTraceElement(str, str2, str3, i);
                    }
                    String currentName = jsonParser.getCurrentName();
                    if ("className".equals(currentName)) {
                        str = jsonParser.getText();
                    } else if ("fileName".equals(currentName)) {
                        str3 = jsonParser.getText();
                    } else if ("lineNumber".equals(currentName)) {
                        if (nextValue.isNumeric()) {
                            i = jsonParser.getIntValue();
                        } else {
                            throw JsonMappingException.from(jsonParser, "Non-numeric token (" + nextValue + ") for property 'lineNumber'");
                        }
                    } else if ("methodName".equals(currentName)) {
                        str2 = jsonParser.getText();
                    } else if (!"nativeMethod".equals(currentName)) {
                        handleUnknownProperty(jsonParser, deserializationContext, this._valueClass, currentName);
                    }
                }
            } else {
                throw deserializationContext.mappingException(this._valueClass, currentToken);
            }
        }
    }

    public static class URIDeserializer extends FromStringDeserializer<URI> {
        public static final URIDeserializer instance = new URIDeserializer();

        public URIDeserializer() {
            super(URI.class);
        }

        protected URI _deserialize(String str, DeserializationContext deserializationContext) throws IllegalArgumentException {
            return URI.create(str);
        }
    }

    public static class URLDeserializer extends FromStringDeserializer<URL> {
        public static final URLDeserializer instance = new URLDeserializer();

        public URLDeserializer() {
            super(URL.class);
        }

        protected URL _deserialize(String str, DeserializationContext deserializationContext) throws IOException {
            return new URL(str);
        }
    }

    public static class UUIDDeserializer extends FromStringDeserializer<UUID> {
        public static final UUIDDeserializer instance = new UUIDDeserializer();

        public UUIDDeserializer() {
            super(UUID.class);
        }

        protected UUID _deserialize(String str, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return UUID.fromString(str);
        }

        protected UUID _deserializeEmbedded(Object obj, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            if (obj instanceof byte[]) {
                byte[] bArr = (byte[]) obj;
                if (bArr.length != 16) {
                    deserializationContext.mappingException("Can only construct UUIDs from 16 byte arrays; got " + bArr.length + " bytes");
                }
                DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(bArr));
                return new UUID(dataInputStream.readLong(), dataInputStream.readLong());
            }
            super._deserializeEmbedded(obj, deserializationContext);
            return null;
        }
    }

    static {
        int i = 0;
        Class[] clsArr = new Class[]{UUID.class, URL.class, URI.class, File.class, Currency.class, Pattern.class, Locale.class, InetAddress.class, Charset.class, AtomicBoolean.class, Class.class, StackTraceElement.class};
        int length = clsArr.length;
        while (i < length) {
            _classNames.add(clsArr[i].getName());
            i++;
        }
    }

    @Deprecated
    public static StdDeserializer<?>[] all() {
        return new StdDeserializer[]{UUIDDeserializer.instance, URLDeserializer.instance, URIDeserializer.instance, FileDeserializer.instance, CurrencyDeserializer.instance, PatternDeserializer.instance, LocaleDeserializer.instance, InetAddressDeserializer.instance, CharsetDeserializer.instance, AtomicBooleanDeserializer.instance, ClassDeserializer.instance, StackTraceElementDeserializer.instance};
    }

    public static JsonDeserializer<?> find(Class<?> cls, String str) {
        if (!_classNames.contains(str)) {
            return null;
        }
        if (cls == URI.class) {
            return URIDeserializer.instance;
        }
        if (cls == URL.class) {
            return URLDeserializer.instance;
        }
        if (cls == File.class) {
            return FileDeserializer.instance;
        }
        if (cls == UUID.class) {
            return UUIDDeserializer.instance;
        }
        if (cls == Currency.class) {
            return CurrencyDeserializer.instance;
        }
        if (cls == Pattern.class) {
            return PatternDeserializer.instance;
        }
        if (cls == Locale.class) {
            return LocaleDeserializer.instance;
        }
        if (cls == InetAddress.class) {
            return InetAddressDeserializer.instance;
        }
        if (cls == Charset.class) {
            return CharsetDeserializer.instance;
        }
        if (cls == Class.class) {
            return ClassDeserializer.instance;
        }
        if (cls == StackTraceElement.class) {
            return StackTraceElementDeserializer.instance;
        }
        if (cls == AtomicBoolean.class) {
            return AtomicBooleanDeserializer.instance;
        }
        throw new IllegalArgumentException("Internal error: can't find deserializer for " + str);
    }
}
