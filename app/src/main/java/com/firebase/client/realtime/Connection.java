package com.firebase.client.realtime;

import com.firebase.client.core.Context;
import com.firebase.client.core.RepoInfo;
import com.firebase.client.utilities.LogWrapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.protocol.HTTP;

public class Connection implements com.firebase.client.realtime.WebsocketConnection.Delegate {
    private static final String REQUEST_PAYLOAD = "d";
    private static final String REQUEST_TYPE = "t";
    private static final String REQUEST_TYPE_DATA = "d";
    private static final String SERVER_CONTROL_MESSAGE = "c";
    private static final String SERVER_CONTROL_MESSAGE_DATA = "d";
    private static final String SERVER_CONTROL_MESSAGE_HELLO = "h";
    private static final String SERVER_CONTROL_MESSAGE_RESET = "r";
    private static final String SERVER_CONTROL_MESSAGE_SHUTDOWN = "s";
    private static final String SERVER_CONTROL_MESSAGE_TYPE = "t";
    private static final String SERVER_DATA_MESSAGE = "d";
    private static final String SERVER_ENVELOPE_DATA = "d";
    private static final String SERVER_ENVELOPE_TYPE = "t";
    private static final String SERVER_HELLO_HOST = "h";
    private static final String SERVER_HELLO_TIMESTAMP = "ts";
    private static long connectionIds = 0;
    private WebsocketConnection conn;
    private Delegate delegate;
    private LogWrapper logger;
    private RepoInfo repoInfo;
    private State state = State.REALTIME_CONNECTING;

    public interface Delegate {
        void onDataMessage(Map<String, Object> map);

        void onDisconnect();

        void onKill(String str);

        void onReady(long j);
    }

    private enum State {
        REALTIME_CONNECTING,
        REALTIME_CONNECTED,
        REALTIME_DISCONNECTED
    }

    public Connection(Context ctx, RepoInfo repoInfo, Delegate delegate) {
        long connId = connectionIds;
        connectionIds = 1 + connId;
        this.repoInfo = repoInfo;
        this.delegate = delegate;
        this.logger = ctx.getLogger(HTTP.CONN_DIRECTIVE, "conn_" + connId);
        this.conn = new WebsocketConnection(ctx, repoInfo, this);
    }

    public void open() {
        if (this.logger.logsDebug()) {
            this.logger.debug("Opening a connection");
        }
        this.conn.open();
    }

    public void close() {
        if (this.state != State.REALTIME_DISCONNECTED) {
            if (this.logger.logsDebug()) {
                this.logger.debug("closing realtime connection");
            }
            this.state = State.REALTIME_DISCONNECTED;
            if (this.conn != null) {
                this.conn.close();
                this.conn = null;
            }
            this.delegate.onDisconnect();
        }
    }

    public void sendRequest(Map<String, Object> message) {
        Map<String, Object> request = new HashMap();
        request.put(SERVER_ENVELOPE_TYPE, SERVER_ENVELOPE_DATA);
        request.put(SERVER_ENVELOPE_DATA, message);
        sendData(request);
    }

    public void onMessage(Map<String, Object> message) {
        try {
            String messageType = (String) message.get(SERVER_ENVELOPE_TYPE);
            if (messageType == null) {
                if (this.logger.logsDebug()) {
                    this.logger.debug("Failed to parse server message: missing message type:" + message.toString());
                }
                close();
            } else if (messageType.equals(SERVER_ENVELOPE_DATA)) {
                onDataMessage((Map) message.get(SERVER_ENVELOPE_DATA));
            } else if (messageType.equals(SERVER_CONTROL_MESSAGE)) {
                onControlMessage((Map) message.get(SERVER_ENVELOPE_DATA));
            } else if (this.logger.logsDebug()) {
                this.logger.debug("Ignoring unknown server message type: " + messageType);
            }
        } catch (ClassCastException e) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Failed to parse server message: " + e.toString());
            }
            close();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onDisconnect(boolean r3) {
        /*
        r2 = this;
        r0 = 0;
        r2.conn = r0;
        if (r3 != 0) goto L_0x0026;
    L_0x0005:
        r0 = r2.state;
        r1 = com.firebase.client.realtime.Connection.State.REALTIME_CONNECTING;
        if (r0 != r1) goto L_0x0026;
    L_0x000b:
        r0 = r2.logger;
        r0 = r0.logsDebug();
        if (r0 == 0) goto L_0x001a;
    L_0x0013:
        r0 = r2.logger;
        r1 = "Realtime connection failed";
        r0.debug(r1);
    L_0x001a:
        r0 = r2.repoInfo;
        r0 = r0.isCacheableHost();
        if (r0 == 0) goto L_0x0022;
    L_0x0022:
        r2.close();
        return;
    L_0x0026:
        r0 = r2.logger;
        r0 = r0.logsDebug();
        if (r0 == 0) goto L_0x0022;
    L_0x002e:
        r0 = r2.logger;
        r1 = "Realtime connection lost";
        r0.debug(r1);
        goto L_0x0022;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.firebase.client.realtime.Connection.onDisconnect(boolean):void");
    }

    private void onDataMessage(Map<String, Object> data) {
        if (this.logger.logsDebug()) {
            this.logger.debug("received data message: " + data.toString());
        }
        this.delegate.onDataMessage(data);
    }

    private void onControlMessage(Map<String, Object> data) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Got control message: " + data.toString());
        }
        try {
            String messageType = (String) data.get(SERVER_ENVELOPE_TYPE);
            if (messageType == null) {
                if (this.logger.logsDebug()) {
                    this.logger.debug("Got invalid control message: " + data.toString());
                }
                close();
            } else if (messageType.equals(SERVER_CONTROL_MESSAGE_SHUTDOWN)) {
                onConnectionShutdown((String) data.get(SERVER_ENVELOPE_DATA));
            } else if (messageType.equals(SERVER_CONTROL_MESSAGE_RESET)) {
                onReset((String) data.get(SERVER_ENVELOPE_DATA));
            } else if (messageType.equals(SERVER_HELLO_HOST)) {
                onHandshake((Map) data.get(SERVER_ENVELOPE_DATA));
            } else if (this.logger.logsDebug()) {
                this.logger.debug("Ignoring unknown control message: " + messageType);
            }
        } catch (ClassCastException e) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Failed to parse control message: " + e.toString());
            }
            close();
        }
    }

    private void onConnectionShutdown(String reason) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Connection shutdown command received. Shutting down...");
        }
        this.delegate.onKill(reason);
        close();
    }

    private void onHandshake(Map<String, Object> handshake) {
        long timestamp = ((Long) handshake.get(SERVER_HELLO_TIMESTAMP)).longValue();
        this.repoInfo.internalHost = (String) handshake.get(SERVER_HELLO_HOST);
        if (this.state == State.REALTIME_CONNECTING) {
            this.conn.start();
            onConnectionReady(timestamp);
        }
    }

    private void onConnectionReady(long timestamp) {
        if (this.logger.logsDebug()) {
            this.logger.debug("realtime connection established");
        }
        this.state = State.REALTIME_CONNECTED;
        this.delegate.onReady(timestamp);
    }

    private void onReset(String host) {
        if (this.logger.logsDebug()) {
            this.logger.debug("Got a reset; killing connection to " + this.repoInfo.internalHost + "; instead connecting to " + host);
        }
        this.repoInfo.internalHost = host;
    }

    private void sendData(Map<String, Object> data) {
        if (this.state == State.REALTIME_CONNECTED) {
            if (this.logger.logsDebug()) {
                this.logger.debug("Sending data: " + data.toString());
            }
            this.conn.send(data);
        } else if (this.logger.logsDebug()) {
            this.logger.debug("Tried to send on an unconnected connection");
        }
    }
}
