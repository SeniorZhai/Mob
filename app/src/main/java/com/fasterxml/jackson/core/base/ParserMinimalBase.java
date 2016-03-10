package com.fasterxml.jackson.core.base;

import com.facebook.internal.ServerProtocol;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.io.NumberInput;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import org.apache.http.message.TokenParser;

public abstract class ParserMinimalBase extends JsonParser {
    protected static final int INT_APOSTROPHE = 39;
    protected static final int INT_ASTERISK = 42;
    protected static final int INT_BACKSLASH = 92;
    protected static final int INT_COLON = 58;
    protected static final int INT_COMMA = 44;
    protected static final int INT_CR = 13;
    protected static final int INT_LBRACKET = 91;
    protected static final int INT_LCURLY = 123;
    protected static final int INT_LF = 10;
    protected static final int INT_QUOTE = 34;
    protected static final int INT_RBRACKET = 93;
    protected static final int INT_RCURLY = 125;
    protected static final int INT_SLASH = 47;
    protected static final int INT_SPACE = 32;
    protected static final int INT_TAB = 9;
    protected static final int INT_b = 98;
    protected static final int INT_f = 102;
    protected static final int INT_n = 110;
    protected static final int INT_r = 114;
    protected static final int INT_t = 116;
    protected static final int INT_u = 117;
    protected JsonToken _currToken;
    protected JsonToken _lastClearedToken;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$fasterxml$jackson$core$JsonToken = new int[JsonToken.values().length];

        static {
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.START_OBJECT.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.START_ARRAY.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.END_OBJECT.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.END_ARRAY.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.VALUE_NUMBER_INT.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.VALUE_TRUE.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.VALUE_FALSE.ordinal()] = 7;
            } catch (NoSuchFieldError e7) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.VALUE_NULL.ordinal()] = 8;
            } catch (NoSuchFieldError e8) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.VALUE_EMBEDDED_OBJECT.ordinal()] = ParserMinimalBase.INT_TAB;
            } catch (NoSuchFieldError e9) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.VALUE_STRING.ordinal()] = ParserMinimalBase.INT_LF;
            } catch (NoSuchFieldError e10) {
            }
            try {
                $SwitchMap$com$fasterxml$jackson$core$JsonToken[JsonToken.VALUE_NUMBER_FLOAT.ordinal()] = 11;
            } catch (NoSuchFieldError e11) {
            }
        }
    }

    protected abstract void _handleEOF() throws JsonParseException;

    public abstract void close() throws IOException;

    public abstract byte[] getBinaryValue(Base64Variant base64Variant) throws IOException, JsonParseException;

    public abstract String getCurrentName() throws IOException, JsonParseException;

    public abstract JsonStreamContext getParsingContext();

    public abstract String getText() throws IOException, JsonParseException;

    public abstract char[] getTextCharacters() throws IOException, JsonParseException;

    public abstract int getTextLength() throws IOException, JsonParseException;

    public abstract int getTextOffset() throws IOException, JsonParseException;

    public abstract boolean hasTextCharacters();

    public abstract boolean isClosed();

    public abstract JsonToken nextToken() throws IOException, JsonParseException;

    public abstract void overrideCurrentName(String str);

    protected ParserMinimalBase() {
    }

    protected ParserMinimalBase(int i) {
        super(i);
    }

    public Version version() {
        return VersionUtil.versionFor(getClass());
    }

    public JsonToken getCurrentToken() {
        return this._currToken;
    }

    public boolean hasCurrentToken() {
        return this._currToken != null;
    }

    public JsonToken nextValue() throws IOException, JsonParseException {
        JsonToken nextToken = nextToken();
        if (nextToken == JsonToken.FIELD_NAME) {
            return nextToken();
        }
        return nextToken;
    }

    public JsonParser skipChildren() throws IOException, JsonParseException {
        if (this._currToken == JsonToken.START_OBJECT || this._currToken == JsonToken.START_ARRAY) {
            int i = 1;
            while (true) {
                JsonToken nextToken = nextToken();
                if (nextToken == null) {
                    _handleEOF();
                } else {
                    switch (AnonymousClass1.$SwitchMap$com$fasterxml$jackson$core$JsonToken[nextToken.ordinal()]) {
                        case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                        case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                            i++;
                            continue;
                        case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                        case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                            i--;
                            if (i == 0) {
                                break;
                            }
                            continue;
                        default:
                            continue;
                    }
                }
            }
        }
        return this;
    }

    public void clearCurrentToken() {
        if (this._currToken != null) {
            this._lastClearedToken = this._currToken;
            this._currToken = null;
        }
    }

    public JsonToken getLastClearedToken() {
        return this._lastClearedToken;
    }

    public boolean getValueAsBoolean(boolean z) throws IOException, JsonParseException {
        if (this._currToken != null) {
            switch (AnonymousClass1.$SwitchMap$com$fasterxml$jackson$core$JsonToken[this._currToken.ordinal()]) {
                case Player.STATE_ENDED /*5*/:
                    if (getIntValue() != 0) {
                        return true;
                    }
                    return false;
                case R.styleable.Toolbar_contentInsetEnd /*6*/:
                    return true;
                case DayPickerView.DAYS_PER_WEEK /*7*/:
                case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                    return false;
                case INT_TAB /*9*/:
                    Object embeddedObject = getEmbeddedObject();
                    if (embeddedObject instanceof Boolean) {
                        return ((Boolean) embeddedObject).booleanValue();
                    }
                    break;
                case INT_LF /*10*/:
                    break;
            }
            if (ServerProtocol.DIALOG_RETURN_SCOPES_TRUE.equals(getText().trim())) {
                return true;
            }
        }
        return z;
    }

    public int getValueAsInt(int i) throws IOException, JsonParseException {
        if (this._currToken == null) {
            return i;
        }
        switch (AnonymousClass1.$SwitchMap$com$fasterxml$jackson$core$JsonToken[this._currToken.ordinal()]) {
            case Player.STATE_ENDED /*5*/:
            case R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                return getIntValue();
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                return 1;
            case DayPickerView.DAYS_PER_WEEK /*7*/:
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                return 0;
            case INT_TAB /*9*/:
                Object embeddedObject = getEmbeddedObject();
                if (embeddedObject instanceof Number) {
                    return ((Number) embeddedObject).intValue();
                }
                return i;
            case INT_LF /*10*/:
                return NumberInput.parseAsInt(getText(), i);
            default:
                return i;
        }
    }

    public long getValueAsLong(long j) throws IOException, JsonParseException {
        if (this._currToken == null) {
            return j;
        }
        switch (AnonymousClass1.$SwitchMap$com$fasterxml$jackson$core$JsonToken[this._currToken.ordinal()]) {
            case Player.STATE_ENDED /*5*/:
            case R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                return getLongValue();
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                return 1;
            case DayPickerView.DAYS_PER_WEEK /*7*/:
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                return 0;
            case INT_TAB /*9*/:
                Object embeddedObject = getEmbeddedObject();
                if (embeddedObject instanceof Number) {
                    return ((Number) embeddedObject).longValue();
                }
                return j;
            case INT_LF /*10*/:
                return NumberInput.parseAsLong(getText(), j);
            default:
                return j;
        }
    }

    public double getValueAsDouble(double d) throws IOException, JsonParseException {
        if (this._currToken == null) {
            return d;
        }
        switch (AnonymousClass1.$SwitchMap$com$fasterxml$jackson$core$JsonToken[this._currToken.ordinal()]) {
            case Player.STATE_ENDED /*5*/:
            case R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                return getDoubleValue();
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                return 1.0d;
            case DayPickerView.DAYS_PER_WEEK /*7*/:
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                return 0.0d;
            case INT_TAB /*9*/:
                Object embeddedObject = getEmbeddedObject();
                if (embeddedObject instanceof Number) {
                    return ((Number) embeddedObject).doubleValue();
                }
                return d;
            case INT_LF /*10*/:
                return NumberInput.parseAsDouble(getText(), d);
            default:
                return d;
        }
    }

    public String getValueAsString(String str) throws IOException, JsonParseException {
        return (this._currToken == JsonToken.VALUE_STRING || !(this._currToken == null || this._currToken == JsonToken.VALUE_NULL || !this._currToken.isScalarValue())) ? getText() : str;
    }

    protected void _decodeBase64(String str, ByteArrayBuilder byteArrayBuilder, Base64Variant base64Variant) throws IOException, JsonParseException {
        int length = str.length();
        int i = 0;
        while (i < length) {
            int i2;
            char charAt;
            while (true) {
                i2 = i + 1;
                charAt = str.charAt(i);
                if (i2 < length) {
                    if (charAt > TokenParser.SP) {
                        break;
                    }
                    i = i2;
                } else {
                    return;
                }
            }
            int decodeBase64Char = base64Variant.decodeBase64Char(charAt);
            if (decodeBase64Char < 0) {
                _reportInvalidBase64(base64Variant, charAt, 0, null);
            }
            if (i2 >= length) {
                _reportBase64EOF();
            }
            i = i2 + 1;
            char charAt2 = str.charAt(i2);
            int decodeBase64Char2 = base64Variant.decodeBase64Char(charAt2);
            if (decodeBase64Char2 < 0) {
                _reportInvalidBase64(base64Variant, charAt2, 1, null);
            }
            i2 = (decodeBase64Char << 6) | decodeBase64Char2;
            if (i >= length) {
                if (base64Variant.usesPadding()) {
                    _reportBase64EOF();
                } else {
                    byteArrayBuilder.append(i2 >> 4);
                    return;
                }
            }
            decodeBase64Char = i + 1;
            charAt = str.charAt(i);
            decodeBase64Char2 = base64Variant.decodeBase64Char(charAt);
            char charAt3;
            if (decodeBase64Char2 < 0) {
                if (decodeBase64Char2 != -2) {
                    _reportInvalidBase64(base64Variant, charAt, 2, null);
                }
                if (decodeBase64Char >= length) {
                    _reportBase64EOF();
                }
                i = decodeBase64Char + 1;
                charAt3 = str.charAt(decodeBase64Char);
                if (!base64Variant.usesPaddingChar(charAt3)) {
                    _reportInvalidBase64(base64Variant, charAt3, 3, "expected padding character '" + base64Variant.getPaddingChar() + "'");
                }
                byteArrayBuilder.append(i2 >> 4);
            } else {
                i2 = (i2 << 6) | decodeBase64Char2;
                if (decodeBase64Char >= length) {
                    if (base64Variant.usesPadding()) {
                        _reportBase64EOF();
                    } else {
                        byteArrayBuilder.appendTwoBytes(i2 >> 2);
                        return;
                    }
                }
                i = decodeBase64Char + 1;
                charAt3 = str.charAt(decodeBase64Char);
                decodeBase64Char2 = base64Variant.decodeBase64Char(charAt3);
                if (decodeBase64Char2 < 0) {
                    if (decodeBase64Char2 != -2) {
                        _reportInvalidBase64(base64Variant, charAt3, 3, null);
                    }
                    byteArrayBuilder.appendTwoBytes(i2 >> 2);
                } else {
                    byteArrayBuilder.appendThreeBytes((i2 << 6) | decodeBase64Char2);
                }
            }
        }
    }

    protected void _reportInvalidBase64(Base64Variant base64Variant, char c, int i, String str) throws JsonParseException {
        String str2;
        if (c <= TokenParser.SP) {
            str2 = "Illegal white space character (code 0x" + Integer.toHexString(c) + ") as character #" + (i + 1) + " of 4-char base64 unit: can only used between units";
        } else if (base64Variant.usesPaddingChar(c)) {
            str2 = "Unexpected padding character ('" + base64Variant.getPaddingChar() + "') as character #" + (i + 1) + " of 4-char base64 unit: padding only legal as 3rd or 4th character";
        } else if (!Character.isDefined(c) || Character.isISOControl(c)) {
            str2 = "Illegal character (code 0x" + Integer.toHexString(c) + ") in base64 content";
        } else {
            str2 = "Illegal character '" + c + "' (code 0x" + Integer.toHexString(c) + ") in base64 content";
        }
        if (str != null) {
            str2 = str2 + ": " + str;
        }
        throw _constructError(str2);
    }

    protected void _reportBase64EOF() throws JsonParseException {
        throw _constructError("Unexpected end-of-String in base64 content");
    }

    protected void _reportUnexpectedChar(int i, String str) throws JsonParseException {
        String str2 = "Unexpected character (" + _getCharDesc(i) + ")";
        if (str != null) {
            str2 = str2 + ": " + str;
        }
        _reportError(str2);
    }

    protected void _reportInvalidEOF() throws JsonParseException {
        _reportInvalidEOF(" in " + this._currToken);
    }

    protected void _reportInvalidEOF(String str) throws JsonParseException {
        _reportError("Unexpected end-of-input" + str);
    }

    protected void _reportInvalidEOFInValue() throws JsonParseException {
        _reportInvalidEOF(" in a value");
    }

    protected void _throwInvalidSpace(int i) throws JsonParseException {
        _reportError("Illegal character (" + _getCharDesc((char) i) + "): only regular white space (\\r, \\n, \\t) is allowed between tokens");
    }

    protected void _throwUnquotedSpace(int i, String str) throws JsonParseException {
        if (!isEnabled(Feature.ALLOW_UNQUOTED_CONTROL_CHARS) || i >= INT_SPACE) {
            _reportError("Illegal unquoted character (" + _getCharDesc((char) i) + "): has to be escaped using backslash to be included in " + str);
        }
    }

    protected char _handleUnrecognizedCharacterEscape(char c) throws JsonProcessingException {
        if (!(isEnabled(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER) || (c == '\'' && isEnabled(Feature.ALLOW_SINGLE_QUOTES)))) {
            _reportError("Unrecognized character escape " + _getCharDesc(c));
        }
        return c;
    }

    protected static final String _getCharDesc(int i) {
        char c = (char) i;
        if (Character.isISOControl(c)) {
            return "(CTRL-CHAR, code " + i + ")";
        }
        if (i > SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
            return "'" + c + "' (code " + i + " / 0x" + Integer.toHexString(i) + ")";
        }
        return "'" + c + "' (code " + i + ")";
    }

    protected final void _reportError(String str) throws JsonParseException {
        throw _constructError(str);
    }

    protected final void _wrapError(String str, Throwable th) throws JsonParseException {
        throw _constructError(str, th);
    }

    protected final void _throwInternal() {
        VersionUtil.throwInternal();
    }

    protected final JsonParseException _constructError(String str, Throwable th) {
        return new JsonParseException(str, getCurrentLocation(), th);
    }
}
