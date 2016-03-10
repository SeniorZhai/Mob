package com.fasterxml.jackson.databind.deser.std;

import com.fasterxml.jackson.annotation.JsonFormat.Value;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

public class DateDeserializers {
    private static final HashSet<String> _classNames = new HashSet();

    protected static abstract class DateBasedDeserializer<T> extends StdScalarDeserializer<T> implements ContextualDeserializer {
        protected final DateFormat _customFormat;
        protected final String _formatString;

        protected abstract DateBasedDeserializer<T> withDateFormat(DateFormat dateFormat, String str);

        protected DateBasedDeserializer(Class<?> cls) {
            super((Class) cls);
            this._customFormat = null;
            this._formatString = null;
        }

        protected DateBasedDeserializer(DateBasedDeserializer<T> dateBasedDeserializer, DateFormat dateFormat, String str) {
            super(dateBasedDeserializer._valueClass);
            this._customFormat = dateFormat;
            this._formatString = str;
        }

        public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
            if (beanProperty == null) {
                return this;
            }
            Value findFormat = deserializationContext.getAnnotationIntrospector().findFormat(beanProperty.getMember());
            if (findFormat == null) {
                return this;
            }
            TimeZone timeZone = findFormat.getTimeZone();
            String pattern = findFormat.getPattern();
            if (pattern.length() > 0) {
                TimeZone timeZone2;
                Locale locale = findFormat.getLocale();
                if (locale == null) {
                    locale = deserializationContext.getLocale();
                }
                DateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
                if (timeZone == null) {
                    timeZone2 = deserializationContext.getTimeZone();
                } else {
                    timeZone2 = timeZone;
                }
                simpleDateFormat.setTimeZone(timeZone2);
                return withDateFormat(simpleDateFormat, pattern);
            } else if (timeZone == null) {
                return this;
            } else {
                DateFormat dateFormat = deserializationContext.getConfig().getDateFormat();
                if (dateFormat.getClass() == StdDateFormat.class) {
                    dateFormat = ((StdDateFormat) dateFormat).withTimeZone(timeZone);
                } else {
                    dateFormat = (DateFormat) dateFormat.clone();
                    dateFormat.setTimeZone(timeZone);
                }
                return withDateFormat(dateFormat, pattern);
            }
        }

        protected Date _parseDate(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            if (this._customFormat == null || jsonParser.getCurrentToken() != JsonToken.VALUE_STRING) {
                return super._parseDate(jsonParser, deserializationContext);
            }
            String trim = jsonParser.getText().trim();
            if (trim.length() == 0) {
                return (Date) getEmptyValue();
            }
            Date parse;
            synchronized (this._customFormat) {
                try {
                    parse = this._customFormat.parse(trim);
                } catch (ParseException e) {
                    throw new IllegalArgumentException("Failed to parse Date value '" + trim + "' (format: \"" + this._formatString + "\"): " + e.getMessage());
                }
            }
            return parse;
        }
    }

    @JacksonStdImpl
    public static class CalendarDeserializer extends DateBasedDeserializer<Calendar> {
        public static final CalendarDeserializer gregorianInstance = new CalendarDeserializer(GregorianCalendar.class);
        public static final CalendarDeserializer instance = new CalendarDeserializer();
        protected final Class<? extends Calendar> _calendarClass;

        public /* bridge */ /* synthetic */ JsonDeserializer createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
            return super.createContextual(deserializationContext, beanProperty);
        }

        public CalendarDeserializer() {
            super(Calendar.class);
            this._calendarClass = null;
        }

        public CalendarDeserializer(Class<? extends Calendar> cls) {
            super(cls);
            this._calendarClass = cls;
        }

        public CalendarDeserializer(CalendarDeserializer calendarDeserializer, DateFormat dateFormat, String str) {
            super(calendarDeserializer, dateFormat, str);
            this._calendarClass = calendarDeserializer._calendarClass;
        }

        protected CalendarDeserializer withDateFormat(DateFormat dateFormat, String str) {
            return new CalendarDeserializer(this, dateFormat, str);
        }

        public Calendar deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            Date _parseDate = _parseDate(jsonParser, deserializationContext);
            if (_parseDate == null) {
                return null;
            }
            if (this._calendarClass == null) {
                return deserializationContext.constructCalendar(_parseDate);
            }
            try {
                Calendar calendar = (Calendar) this._calendarClass.newInstance();
                calendar.setTimeInMillis(_parseDate.getTime());
                TimeZone timeZone = deserializationContext.getTimeZone();
                if (timeZone == null) {
                    return calendar;
                }
                calendar.setTimeZone(timeZone);
                return calendar;
            } catch (Throwable e) {
                throw deserializationContext.instantiationException(this._calendarClass, e);
            }
        }
    }

    public static class DateDeserializer extends DateBasedDeserializer<Date> {
        public static final DateDeserializer instance = new DateDeserializer();

        public /* bridge */ /* synthetic */ JsonDeserializer createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
            return super.createContextual(deserializationContext, beanProperty);
        }

        public DateDeserializer() {
            super(Date.class);
        }

        public DateDeserializer(DateDeserializer dateDeserializer, DateFormat dateFormat, String str) {
            super(dateDeserializer, dateFormat, str);
        }

        protected DateDeserializer withDateFormat(DateFormat dateFormat, String str) {
            return new DateDeserializer(this, dateFormat, str);
        }

        public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return _parseDate(jsonParser, deserializationContext);
        }
    }

    public static class SqlDateDeserializer extends DateBasedDeserializer<java.sql.Date> {
        public static final SqlDateDeserializer instance = new SqlDateDeserializer();

        public /* bridge */ /* synthetic */ JsonDeserializer createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
            return super.createContextual(deserializationContext, beanProperty);
        }

        public SqlDateDeserializer() {
            super(java.sql.Date.class);
        }

        public SqlDateDeserializer(SqlDateDeserializer sqlDateDeserializer, DateFormat dateFormat, String str) {
            super(sqlDateDeserializer, dateFormat, str);
        }

        protected SqlDateDeserializer withDateFormat(DateFormat dateFormat, String str) {
            return new SqlDateDeserializer(this, dateFormat, str);
        }

        public java.sql.Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            Date _parseDate = _parseDate(jsonParser, deserializationContext);
            return _parseDate == null ? null : new java.sql.Date(_parseDate.getTime());
        }
    }

    protected static class TimeZoneDeserializer extends FromStringDeserializer<TimeZone> {
        public static final TimeZoneDeserializer instance = new TimeZoneDeserializer();

        public TimeZoneDeserializer() {
            super(TimeZone.class);
        }

        protected TimeZone _deserialize(String str, DeserializationContext deserializationContext) throws IOException {
            return TimeZone.getTimeZone(str);
        }
    }

    public static class TimestampDeserializer extends DateBasedDeserializer<Timestamp> {
        public static final TimestampDeserializer instance = new TimestampDeserializer();

        public /* bridge */ /* synthetic */ JsonDeserializer createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
            return super.createContextual(deserializationContext, beanProperty);
        }

        public TimestampDeserializer() {
            super(Timestamp.class);
        }

        public TimestampDeserializer(TimestampDeserializer timestampDeserializer, DateFormat dateFormat, String str) {
            super(timestampDeserializer, dateFormat, str);
        }

        protected TimestampDeserializer withDateFormat(DateFormat dateFormat, String str) {
            return new TimestampDeserializer(this, dateFormat, str);
        }

        public Timestamp deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            return new Timestamp(_parseDate(jsonParser, deserializationContext).getTime());
        }
    }

    static {
        int i = 0;
        Class[] clsArr = new Class[]{Calendar.class, GregorianCalendar.class, java.sql.Date.class, Date.class, Timestamp.class, TimeZone.class};
        int length = clsArr.length;
        while (i < length) {
            _classNames.add(clsArr[i].getName());
            i++;
        }
    }

    @Deprecated
    public static StdDeserializer<?>[] all() {
        return new StdDeserializer[]{CalendarDeserializer.instance, DateDeserializer.instance, CalendarDeserializer.gregorianInstance, SqlDateDeserializer.instance, TimestampDeserializer.instance, TimeZoneDeserializer.instance};
    }

    public static JsonDeserializer<?> find(Class<?> cls, String str) {
        if (!_classNames.contains(str)) {
            return null;
        }
        if (cls == Calendar.class) {
            return CalendarDeserializer.instance;
        }
        if (cls == Date.class) {
            return DateDeserializer.instance;
        }
        if (cls == java.sql.Date.class) {
            return SqlDateDeserializer.instance;
        }
        if (cls == Timestamp.class) {
            return TimestampDeserializer.instance;
        }
        if (cls == TimeZone.class) {
            return TimeZoneDeserializer.instance;
        }
        if (cls == GregorianCalendar.class) {
            return CalendarDeserializer.gregorianInstance;
        }
        throw new IllegalArgumentException("Internal error: can't find deserializer for " + str);
    }
}
