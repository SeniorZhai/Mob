package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.io.NumberInput;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.http.message.TokenParser;

public class StdDateFormat extends DateFormat {
    protected static final String[] ALL_FORMATS = new String[]{DATE_FORMAT_STR_ISO8601, DATE_FORMAT_STR_ISO8601_Z, DATE_FORMAT_STR_RFC1123, DATE_FORMAT_STR_PLAIN};
    protected static final DateFormat DATE_FORMAT_ISO8601 = new SimpleDateFormat(DATE_FORMAT_STR_ISO8601);
    protected static final DateFormat DATE_FORMAT_ISO8601_Z = new SimpleDateFormat(DATE_FORMAT_STR_ISO8601_Z);
    protected static final DateFormat DATE_FORMAT_PLAIN = new SimpleDateFormat(DATE_FORMAT_STR_PLAIN);
    protected static final DateFormat DATE_FORMAT_RFC1123 = new SimpleDateFormat(DATE_FORMAT_STR_RFC1123, Locale.US);
    protected static final String DATE_FORMAT_STR_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    protected static final String DATE_FORMAT_STR_ISO8601_Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    protected static final String DATE_FORMAT_STR_PLAIN = "yyyy-MM-dd";
    protected static final String DATE_FORMAT_STR_RFC1123 = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("GMT");
    public static final StdDateFormat instance = new StdDateFormat();
    protected transient DateFormat _formatISO8601;
    protected transient DateFormat _formatISO8601_z;
    protected transient DateFormat _formatPlain;
    protected transient DateFormat _formatRFC1123;
    protected transient TimeZone _timezone;

    static {
        DATE_FORMAT_RFC1123.setTimeZone(DEFAULT_TIMEZONE);
        DATE_FORMAT_ISO8601.setTimeZone(DEFAULT_TIMEZONE);
        DATE_FORMAT_ISO8601_Z.setTimeZone(DEFAULT_TIMEZONE);
        DATE_FORMAT_PLAIN.setTimeZone(DEFAULT_TIMEZONE);
    }

    public StdDateFormat(TimeZone timeZone) {
        this._timezone = timeZone;
    }

    public static TimeZone getDefaultTimeZone() {
        return DEFAULT_TIMEZONE;
    }

    public StdDateFormat withTimeZone(TimeZone timeZone) {
        if (timeZone == null) {
            timeZone = DEFAULT_TIMEZONE;
        }
        return new StdDateFormat(timeZone);
    }

    public StdDateFormat clone() {
        return new StdDateFormat();
    }

    public static DateFormat getBlueprintISO8601Format() {
        return DATE_FORMAT_ISO8601;
    }

    public static DateFormat getISO8601Format(TimeZone timeZone) {
        return _cloneFormat(DATE_FORMAT_ISO8601, timeZone);
    }

    public static DateFormat getBlueprintRFC1123Format() {
        return DATE_FORMAT_RFC1123;
    }

    public static DateFormat getRFC1123Format(TimeZone timeZone) {
        return _cloneFormat(DATE_FORMAT_RFC1123, timeZone);
    }

    public void setTimeZone(TimeZone timeZone) {
        if (timeZone != this._timezone) {
            this._formatRFC1123 = null;
            this._formatISO8601 = null;
            this._formatISO8601_z = null;
            this._formatPlain = null;
            this._timezone = timeZone;
        }
    }

    public Date parse(String str) throws ParseException {
        String trim = str.trim();
        ParsePosition parsePosition = new ParsePosition(0);
        Date parse = parse(trim, parsePosition);
        if (parse != null) {
            return parse;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String str2 : ALL_FORMATS) {
            if (stringBuilder.length() > 0) {
                stringBuilder.append("\", \"");
            } else {
                stringBuilder.append(TokenParser.DQUOTE);
            }
            stringBuilder.append(str2);
        }
        stringBuilder.append(TokenParser.DQUOTE);
        throw new ParseException(String.format("Can not parse date \"%s\": not compatible with any of standard forms (%s)", new Object[]{trim, stringBuilder.toString()}), parsePosition.getErrorIndex());
    }

    public Date parse(String str, ParsePosition parsePosition) {
        if (looksLikeISO8601(str)) {
            return parseAsISO8601(str, parsePosition);
        }
        int length = str.length();
        char charAt;
        do {
            length--;
            if (length < 0) {
                break;
            }
            charAt = str.charAt(length);
            if (charAt < '0') {
                break;
            }
        } while (charAt <= '9');
        if (length >= 0 || !NumberInput.inLongRange(str, false)) {
            return parseAsRFC1123(str, parsePosition);
        }
        return new Date(Long.parseLong(str));
    }

    public StringBuffer format(Date date, StringBuffer stringBuffer, FieldPosition fieldPosition) {
        if (this._formatISO8601 == null) {
            this._formatISO8601 = _cloneFormat(DATE_FORMAT_ISO8601);
        }
        return this._formatISO8601.format(date, stringBuffer, fieldPosition);
    }

    protected boolean looksLikeISO8601(String str) {
        if (str.length() >= 5 && Character.isDigit(str.charAt(0)) && Character.isDigit(str.charAt(3)) && str.charAt(4) == '-') {
            return true;
        }
        return false;
    }

    protected Date parseAsISO8601(String str, ParsePosition parsePosition) {
        DateFormat dateFormat;
        int length = str.length();
        char charAt = str.charAt(length - 1);
        if (length <= 10 && Character.isDigit(charAt)) {
            dateFormat = this._formatPlain;
            if (dateFormat == null) {
                dateFormat = _cloneFormat(DATE_FORMAT_PLAIN);
                this._formatPlain = dateFormat;
            }
        } else if (charAt == 'Z') {
            dateFormat = this._formatISO8601_z;
            if (dateFormat == null) {
                dateFormat = _cloneFormat(DATE_FORMAT_ISO8601_Z);
                this._formatISO8601_z = dateFormat;
            }
            if (str.charAt(length - 4) == ':') {
                StringBuilder stringBuilder = new StringBuilder(str);
                stringBuilder.insert(length - 1, ".000");
                str = stringBuilder.toString();
            }
        } else if (hasTimeZone(str)) {
            charAt = str.charAt(length - 3);
            if (charAt == ':') {
                r0 = new StringBuilder(str);
                r0.delete(length - 3, length - 2);
                str = r0.toString();
            } else if (charAt == '+' || charAt == '-') {
                str = str + "00";
            }
            int length2 = str.length();
            if (Character.isDigit(str.charAt(length2 - 9))) {
                StringBuilder stringBuilder2 = new StringBuilder(str);
                stringBuilder2.insert(length2 - 5, ".000");
                str = stringBuilder2.toString();
            }
            dateFormat = this._formatISO8601;
            if (this._formatISO8601 == null) {
                dateFormat = _cloneFormat(DATE_FORMAT_ISO8601);
                this._formatISO8601 = dateFormat;
            }
        } else {
            r0 = new StringBuilder(str);
            if ((length - str.lastIndexOf(84)) - 1 <= 8) {
                r0.append(".000");
            }
            r0.append('Z');
            str = r0.toString();
            dateFormat = this._formatISO8601_z;
            if (dateFormat == null) {
                dateFormat = _cloneFormat(DATE_FORMAT_ISO8601_Z);
                this._formatISO8601_z = dateFormat;
            }
        }
        return dateFormat.parse(str, parsePosition);
    }

    protected Date parseAsRFC1123(String str, ParsePosition parsePosition) {
        if (this._formatRFC1123 == null) {
            this._formatRFC1123 = _cloneFormat(DATE_FORMAT_RFC1123);
        }
        return this._formatRFC1123.parse(str, parsePosition);
    }

    private static final boolean hasTimeZone(String str) {
        int length = str.length();
        if (length >= 6) {
            char charAt = str.charAt(length - 6);
            if (charAt == '+' || charAt == '-') {
                return true;
            }
            charAt = str.charAt(length - 5);
            if (charAt == '+' || charAt == '-') {
                return true;
            }
            char charAt2 = str.charAt(length - 3);
            if (charAt2 == '+' || charAt2 == '-') {
                return true;
            }
        }
        return false;
    }

    private final DateFormat _cloneFormat(DateFormat dateFormat) {
        return _cloneFormat(dateFormat, this._timezone);
    }

    private static final DateFormat _cloneFormat(DateFormat dateFormat, TimeZone timeZone) {
        DateFormat dateFormat2 = (DateFormat) dateFormat.clone();
        if (timeZone != null) {
            dateFormat2.setTimeZone(timeZone);
        }
        return dateFormat2;
    }
}
