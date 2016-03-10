package com.firebase.client.authentication.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.firebase.client.utilities.Base64;
import com.firebase.client.utilities.encoding.JsonHelpers;
import com.helpshift.HSFunnel;
import java.io.IOException;
import java.util.Map;

public class JsonWebToken {
    private final Map<String, Object> claims;
    private final Object data;
    private final Map<String, Object> header;
    private final String signature;

    private JsonWebToken(Map<String, Object> header, Map<String, Object> claims, Object data, String signature) {
        this.header = header;
        this.claims = claims;
        this.data = data;
        this.signature = signature;
    }

    public Map<String, Object> getHeader() {
        return this.header;
    }

    public Map<String, Object> getClaims() {
        return this.claims;
    }

    public Object getData() {
        return this.data;
    }

    public String getSignature() {
        return this.signature;
    }

    private static String fixLength(String str) {
        int missing = (4 - (str.length() % 4)) % 4;
        if (missing == 0) {
            return str;
        }
        StringBuilder builder = new StringBuilder(str);
        for (int i = 0; i < missing; i++) {
            builder.append("=");
        }
        return builder.toString();
    }

    public static JsonWebToken decode(String token) throws IOException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IOException("Not a valid token: " + token);
        }
        TypeReference mapRef = new TypeReference<Map<String, Object>>() {
        };
        Map<String, Object> header = (Map) JsonHelpers.getMapper().readValue(Base64.decode(fixLength(parts[0])), mapRef);
        Map<String, Object> claims = (Map) JsonHelpers.getMapper().readValue(Base64.decode(fixLength(parts[1])), mapRef);
        String signature = parts[2];
        Object data = claims.get(HSFunnel.LIBRARY_OPENED_DECOMP);
        claims.remove(HSFunnel.LIBRARY_OPENED_DECOMP);
        return new JsonWebToken(header, claims, data, signature);
    }
}
