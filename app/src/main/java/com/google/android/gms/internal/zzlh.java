package com.google.android.gms.internal;

import android.text.TextUtils;
import com.mobcrush.mobcrush.R;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class zzlh {
    private static final Pattern zzacL = Pattern.compile("\\\\.");
    private static final Pattern zzacM = Pattern.compile("[\\\\\"/\b\f\n\r\t]");

    public static String zzcr(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        Matcher matcher = zzacM.matcher(str);
        StringBuffer stringBuffer = null;
        while (matcher.find()) {
            if (stringBuffer == null) {
                stringBuffer = new StringBuffer();
            }
            switch (matcher.group().charAt(0)) {
                case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                    matcher.appendReplacement(stringBuffer, "\\\\b");
                    break;
                case HTTP.HT /*9*/:
                    matcher.appendReplacement(stringBuffer, "\\\\t");
                    break;
                case HTTP.LF /*10*/:
                    matcher.appendReplacement(stringBuffer, "\\\\n");
                    break;
                case R.styleable.Toolbar_titleMargins /*12*/:
                    matcher.appendReplacement(stringBuffer, "\\\\f");
                    break;
                case HTTP.CR /*13*/:
                    matcher.appendReplacement(stringBuffer, "\\\\r");
                    break;
                case R.styleable.Theme_actionModePasteDrawable /*34*/:
                    matcher.appendReplacement(stringBuffer, "\\\\\\\"");
                    break;
                case R.styleable.Theme_spinnerDropDownItemStyle /*47*/:
                    matcher.appendReplacement(stringBuffer, "\\\\/");
                    break;
                case R.styleable.Theme_alertDialogCenterButtons /*92*/:
                    matcher.appendReplacement(stringBuffer, "\\\\\\\\");
                    break;
                default:
                    break;
            }
        }
        if (stringBuffer == null) {
            return str;
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    public static boolean zzd(Object obj, Object obj2) {
        if (obj == null && obj2 == null) {
            return true;
        }
        if (obj == null || obj2 == null) {
            return false;
        }
        if ((obj instanceof JSONObject) && (obj2 instanceof JSONObject)) {
            JSONObject jSONObject = (JSONObject) obj;
            JSONObject jSONObject2 = (JSONObject) obj2;
            if (jSONObject.length() != jSONObject2.length()) {
                return false;
            }
            Iterator keys = jSONObject.keys();
            while (keys.hasNext()) {
                String str = (String) keys.next();
                if (!jSONObject2.has(str)) {
                    return false;
                }
                try {
                    if (!zzd(jSONObject.get(str), jSONObject2.get(str))) {
                        return false;
                    }
                } catch (JSONException e) {
                    return false;
                }
            }
            return true;
        } else if (!(obj instanceof JSONArray) || !(obj2 instanceof JSONArray)) {
            return obj.equals(obj2);
        } else {
            JSONArray jSONArray = (JSONArray) obj;
            JSONArray jSONArray2 = (JSONArray) obj2;
            if (jSONArray.length() != jSONArray2.length()) {
                return false;
            }
            int i = 0;
            while (i < jSONArray.length()) {
                try {
                    if (!zzd(jSONArray.get(i), jSONArray2.get(i))) {
                        return false;
                    }
                    i++;
                } catch (JSONException e2) {
                    return false;
                }
            }
            return true;
        }
    }
}
