package com.firebase.tubesock;

import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HTTP;

class WebSocketHandshake {
    private static final String WEBSOCKET_VERSION = "13";
    private Map<String, String> extraHeaders = null;
    private String nonce = null;
    private String protocol = null;
    private URI url = null;

    public WebSocketHandshake(URI url, String protocol, Map<String, String> extraHeaders) {
        this.url = url;
        this.protocol = protocol;
        this.extraHeaders = extraHeaders;
        this.nonce = createNonce();
    }

    public byte[] getHandshake() {
        String str;
        String path = this.url.getPath();
        String query = this.url.getQuery();
        StringBuilder append = new StringBuilder().append(path);
        if (query == null) {
            str = BuildConfig.FLAVOR;
        } else {
            str = "?" + query;
        }
        path = append.append(str).toString();
        String host = this.url.getHost();
        if (this.url.getPort() != -1) {
            host = host + ":" + this.url.getPort();
        }
        LinkedHashMap<String, String> header = new LinkedHashMap();
        header.put(HTTP.TARGET_HOST, host);
        header.put(HttpHeaders.UPGRADE, "websocket");
        header.put(HTTP.CONN_DIRECTIVE, HttpHeaders.UPGRADE);
        header.put("Sec-WebSocket-Version", WEBSOCKET_VERSION);
        header.put("Sec-WebSocket-Key", this.nonce);
        if (this.protocol != null) {
            header.put("Sec-WebSocket-Protocol", this.protocol);
        }
        if (this.extraHeaders != null) {
            for (String fieldName : this.extraHeaders.keySet()) {
                if (!header.containsKey(fieldName)) {
                    header.put(fieldName, this.extraHeaders.get(fieldName));
                }
            }
        }
        String handshake = (("GET " + path + " HTTP/1.1\r\n") + generateHeader(header)) + "\r\n";
        byte[] handshakeBytes = new byte[handshake.getBytes().length];
        System.arraycopy(handshake.getBytes(), 0, handshakeBytes, 0, handshake.getBytes().length);
        return handshakeBytes;
    }

    private String generateHeader(LinkedHashMap<String, String> headers) {
        String header = new String();
        for (String fieldName : headers.keySet()) {
            header = header + fieldName + ": " + ((String) headers.get(fieldName)) + "\r\n";
        }
        return header;
    }

    private String createNonce() {
        byte[] nonce = new byte[16];
        for (int i = 0; i < 16; i++) {
            nonce[i] = (byte) rand(0, SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
        }
        return Base64.encodeToString(nonce, false);
    }

    public void verifyServerStatusLine(String statusLine) {
        int statusCode = Integer.valueOf(statusLine.substring(9, 12)).intValue();
        if (statusCode == HttpStatus.SC_PROXY_AUTHENTICATION_REQUIRED) {
            throw new WebSocketException("connection failed: proxy authentication not supported");
        } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
            throw new WebSocketException("connection failed: 404 not found");
        } else if (statusCode != HttpStatus.SC_SWITCHING_PROTOCOLS) {
            throw new WebSocketException("connection failed: unknown status code " + statusCode);
        }
    }

    public void verifyServerHandshakeHeaders(HashMap<String, String> headers) {
        if (!((String) headers.get(HttpHeaders.UPGRADE)).toLowerCase(Locale.US).equals("websocket")) {
            throw new WebSocketException("connection failed: missing header field in server handshake: Upgrade");
        } else if (!((String) headers.get(HTTP.CONN_DIRECTIVE)).toLowerCase(Locale.US).equals("upgrade")) {
            throw new WebSocketException("connection failed: missing header field in server handshake: Connection");
        }
    }

    private int rand(int min, int max) {
        return (int) ((Math.random() * ((double) max)) + ((double) min));
    }
}
