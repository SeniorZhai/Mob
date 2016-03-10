package com.android.volley;

import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.http.protocol.HTTP;

class InternalUtils {
    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    InternalUtils() {
    }

    private static String convertToHex(byte[] bytes) {
        char[] hexChars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            hexChars[j * 2] = HEX_CHARS[v >>> 4];
            hexChars[(j * 2) + 1] = HEX_CHARS[v & 15];
        }
        return new String(hexChars);
    }

    public static String sha1Hash(String text) {
        String hash = null;
        try {
            MessageDigest digest = MessageDigest.getInstance(CommonUtils.SHA1_INSTANCE);
            byte[] bytes = text.getBytes(HTTP.UTF_8);
            digest.update(bytes, 0, bytes.length);
            hash = convertToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e2) {
            e2.printStackTrace();
        }
        return hash;
    }
}
