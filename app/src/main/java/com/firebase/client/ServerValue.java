package com.firebase.client;

import com.firebase.client.core.ServerValues;
import com.mobcrush.mobcrush.Constants;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerValue {
    public static final Map<String, String> TIMESTAMP = createServerValuePlaceholder(Constants.CHAT_MESSAGE_TIMESTAMP);

    private static Map<String, String> createServerValuePlaceholder(String key) {
        Map<String, String> result = new HashMap();
        result.put(ServerValues.NAME_SUBKEY_SERVERVALUE, key);
        return Collections.unmodifiableMap(result);
    }
}
