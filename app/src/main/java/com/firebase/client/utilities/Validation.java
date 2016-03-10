package com.firebase.client.utilities;

import com.firebase.client.FirebaseException;
import com.firebase.client.core.Path;
import com.firebase.client.core.ServerValues;
import com.firebase.client.snapshot.ChildKey;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Validation {
    private static final Pattern INVALID_KEY_REGEX = Pattern.compile("[\\[\\]\\.#\\$\\/\\u0000-\\u001F\\u007F]");
    private static final Pattern INVALID_PATH_REGEX = Pattern.compile("[\\[\\]\\.#$]");

    private static boolean isValidPathString(String pathString) {
        return !INVALID_PATH_REGEX.matcher(pathString).find();
    }

    public static void validatePathString(String pathString) throws FirebaseException {
        if (!isValidPathString(pathString)) {
            throw new FirebaseException("Invalid Firebase path: " + pathString + ". Firebase paths must not contain '.', '#', '$', '[', or ']'");
        }
    }

    public static void validateRootPathString(String pathString) throws FirebaseException {
        if (pathString.startsWith(".info")) {
            validatePathString(pathString.substring(5));
        } else if (pathString.startsWith("/.info")) {
            validatePathString(pathString.substring(6));
        } else {
            validatePathString(pathString);
        }
    }

    private static boolean isWritableKey(String key) {
        return key != null && key.length() > 0 && (key.equals(".value") || key.equals(".priority") || !(key.startsWith(".") || INVALID_KEY_REGEX.matcher(key).find()));
    }

    private static boolean isValidKey(String key) {
        return key.equals(".info") || !INVALID_KEY_REGEX.matcher(key).find();
    }

    public static void validateNullableKey(String key) throws FirebaseException {
        if (key != null && !isValidKey(key)) {
            throw new FirebaseException("Invalid key: " + key + ". Keys must not contain '/', '.', '#', '$', '[', or ']'");
        }
    }

    private static boolean isWritablePath(Path path) {
        ChildKey front = path.getFront();
        return front == null || !front.asString().startsWith(".");
    }

    public static void validateWritableObject(Object object) {
        if (object instanceof Map) {
            Map<String, Object> map = (Map) object;
            if (!map.containsKey(ServerValues.NAME_SUBKEY_SERVERVALUE)) {
                for (Entry<String, Object> entry : map.entrySet()) {
                    validateWritableKey((String) entry.getKey());
                    validateWritableObject(entry.getValue());
                }
            }
        } else if (object instanceof List) {
            for (Object child : (List) object) {
                validateWritableObject(child);
            }
        }
    }

    public static void validateWritableKey(String key) throws FirebaseException {
        if (!isWritableKey(key)) {
            throw new FirebaseException("Invalid key: " + key + ". Keys must not contain '/', '.', '#', '$', '[', or ']'");
        }
    }

    public static void validateWritablePath(Path path) throws FirebaseException {
        if (!isWritablePath(path)) {
            throw new FirebaseException("Invalid write location: " + path.toString());
        }
    }
}
