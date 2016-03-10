package com.mixpanel.android.viewcrawler;

import android.util.Log;
import com.helpshift.constants.MessageColumns;
import com.mixpanel.android.java_websocket.client.WebSocketClient;
import com.mixpanel.android.java_websocket.drafts.Draft_17;
import com.mixpanel.android.java_websocket.exceptions.NotSendableException;
import com.mixpanel.android.java_websocket.exceptions.WebsocketNotConnectedException;
import com.mixpanel.android.java_websocket.framing.Framedata.Opcode;
import com.mixpanel.android.java_websocket.handshake.ServerHandshake;
import com.mixpanel.android.mpmetrics.MPConfig;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.nio.ByteBuffer;
import org.json.JSONException;
import org.json.JSONObject;

class EditorConnection {
    private static final int CONNECT_TIMEOUT = 5000;
    private static final ByteBuffer EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0);
    private static final String LOGTAG = "MixpanelAPI.EditorCnctn";
    private final EditorClient mClient;
    private final Editor mService;
    private final URI mURI;

    public interface Editor {
        void bindEvents(JSONObject jSONObject);

        void cleanup();

        void clearEdits(JSONObject jSONObject);

        void performEdit(JSONObject jSONObject);

        void sendDeviceInfo();

        void sendSnapshot(JSONObject jSONObject);

        void setTweaks(JSONObject jSONObject);
    }

    private class EditorClient extends WebSocketClient {
        public EditorClient(URI uri, int connectTimeout, Socket sslSocket) throws InterruptedException {
            super(uri, new Draft_17(), null, connectTimeout);
            setSocket(sslSocket);
        }

        public void onOpen(ServerHandshake handshakedata) {
            if (MPConfig.DEBUG) {
                Log.v(EditorConnection.LOGTAG, "Websocket connected");
            }
        }

        public void onMessage(String message) {
            if (MPConfig.DEBUG) {
                Log.v(EditorConnection.LOGTAG, "Received message from editor:\n" + message);
            }
            try {
                JSONObject messageJson = new JSONObject(message);
                String type = messageJson.getString(MessageColumns.TYPE);
                if (type.equals("device_info_request")) {
                    EditorConnection.this.mService.sendDeviceInfo();
                } else if (type.equals("snapshot_request")) {
                    EditorConnection.this.mService.sendSnapshot(messageJson);
                } else if (type.equals("change_request")) {
                    EditorConnection.this.mService.performEdit(messageJson);
                } else if (type.equals("event_binding_request")) {
                    EditorConnection.this.mService.bindEvents(messageJson);
                } else if (type.equals("clear_request")) {
                    EditorConnection.this.mService.clearEdits(messageJson);
                } else if (type.equals("tweak_request")) {
                    EditorConnection.this.mService.setTweaks(messageJson);
                }
            } catch (JSONException e) {
                Log.e(EditorConnection.LOGTAG, "Bad JSON received:" + message, e);
            }
        }

        public void onClose(int code, String reason, boolean remote) {
            if (MPConfig.DEBUG) {
                Log.v(EditorConnection.LOGTAG, "WebSocket closed. Code: " + code + ", reason: " + reason + "\nURI: " + EditorConnection.this.mURI);
            }
            EditorConnection.this.mService.cleanup();
        }

        public void onError(Exception ex) {
            if (ex == null || ex.getMessage() == null) {
                Log.e(EditorConnection.LOGTAG, "Unknown websocket error occurred");
            } else {
                Log.e(EditorConnection.LOGTAG, "Websocket Error: " + ex.getMessage());
            }
        }
    }

    public class EditorConnectionException extends IOException {
        private static final long serialVersionUID = -1884953175346045636L;

        public EditorConnectionException(Throwable cause) {
            super(cause.getMessage());
        }
    }

    private class WebSocketOutputStream extends OutputStream {
        private WebSocketOutputStream() {
        }

        public void write(int b) throws EditorConnectionException {
            write(new byte[]{(byte) b}, 0, 1);
        }

        public void write(byte[] b) throws EditorConnectionException {
            write(b, 0, b.length);
        }

        public void write(byte[] b, int off, int len) throws EditorConnectionException {
            try {
                EditorConnection.this.mClient.sendFragmentedFrame(Opcode.TEXT, ByteBuffer.wrap(b, off, len), false);
            } catch (WebsocketNotConnectedException e) {
                throw new EditorConnectionException(e);
            } catch (NotSendableException e2) {
                throw new EditorConnectionException(e2);
            }
        }

        public void close() throws EditorConnectionException {
            try {
                EditorConnection.this.mClient.sendFragmentedFrame(Opcode.TEXT, EditorConnection.EMPTY_BYTE_BUFFER, true);
            } catch (WebsocketNotConnectedException e) {
                throw new EditorConnectionException(e);
            } catch (NotSendableException e2) {
                throw new EditorConnectionException(e2);
            }
        }
    }

    public EditorConnection(URI uri, Editor service, Socket sslSocket) throws EditorConnectionException {
        this.mService = service;
        this.mURI = uri;
        try {
            this.mClient = new EditorClient(uri, CONNECT_TIMEOUT, sslSocket);
            this.mClient.connectBlocking();
        } catch (InterruptedException e) {
            throw new EditorConnectionException(e);
        }
    }

    public boolean isValid() {
        return (this.mClient.isClosed() || this.mClient.isClosing() || this.mClient.isFlushAndClose()) ? false : true;
    }

    public BufferedOutputStream getBufferedOutputStream() {
        return new BufferedOutputStream(new WebSocketOutputStream());
    }
}
