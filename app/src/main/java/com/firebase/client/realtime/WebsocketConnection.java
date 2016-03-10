package com.firebase.client.realtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.firebase.client.core.Context;
import com.firebase.client.core.RepoInfo;
import com.firebase.client.realtime.util.StringListReader;
import com.firebase.client.utilities.LogWrapper;
import com.firebase.client.utilities.Utilities;
import com.firebase.client.utilities.encoding.JsonHelpers;
import com.firebase.tubesock.WebSocket;
import com.firebase.tubesock.WebSocketEventHandler;
import com.firebase.tubesock.WebSocketException;
import com.firebase.tubesock.WebSocketMessage;
import com.helpshift.res.values.HSConsts;
import io.fabric.sdk.android.BuildConfig;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.http.protocol.HTTP;

public class WebsocketConnection {
    private static final long CONNECT_TIMEOUT = 30000;
    private static final long KEEP_ALIVE = 45000;
    private static final int MAX_FRAME_SIZE = 16384;
    private static long connectionId = 0;
    private WSClient conn;
    private ScheduledFuture connectTimeout;
    private Context ctx;
    private Delegate delegate;
    private boolean everConnected = false;
    private StringListReader frameReader;
    private boolean isClosed = false;
    private ScheduledFuture keepAlive;
    private LogWrapper logger;
    private MapType mapType;
    private ObjectMapper mapper;
    private long totalFrames = 0;

    public interface Delegate {
        void onDisconnect(boolean z);

        void onMessage(Map<String, Object> map);
    }

    private interface WSClient {
        void close();

        void connect();

        void send(String str);
    }

    private class WSClientTubesock implements WSClient, WebSocketEventHandler {
        private WebSocket ws;

        private WSClientTubesock(WebSocket ws) {
            this.ws = ws;
            this.ws.setEventHandler(this);
        }

        public void onOpen() {
            WebsocketConnection.this.ctx.getRunLoop().scheduleNow(new Runnable() {
                public void run() {
                    WebsocketConnection.this.connectTimeout.cancel(false);
                    WebsocketConnection.this.everConnected = true;
                    if (WebsocketConnection.this.logger.logsDebug()) {
                        WebsocketConnection.this.logger.debug("websocket opened");
                    }
                    WebsocketConnection.this.resetKeepAlive();
                }
            });
        }

        public void onMessage(WebSocketMessage msg) {
            final String str = msg.getText();
            if (WebsocketConnection.this.logger.logsDebug()) {
                WebsocketConnection.this.logger.debug("ws message: " + str);
            }
            WebsocketConnection.this.ctx.getRunLoop().scheduleNow(new Runnable() {
                public void run() {
                    WebsocketConnection.this.handleIncomingFrame(str);
                }
            });
        }

        public void onClose() {
            String logMessage = "closed";
            WebsocketConnection.this.ctx.getRunLoop().scheduleNow(new Runnable() {
                public void run() {
                    if (WebsocketConnection.this.logger.logsDebug()) {
                        WebsocketConnection.this.logger.debug("closed");
                    }
                    WebsocketConnection.this.onClosed();
                }
            });
        }

        public void onError(final WebSocketException e) {
            WebsocketConnection.this.ctx.getRunLoop().scheduleNow(new Runnable() {
                public void run() {
                    String logMessage = "had an error";
                    if (WebsocketConnection.this.logger.logsDebug()) {
                        WebsocketConnection.this.logger.debug(logMessage, e);
                    }
                    if (e.getMessage().startsWith("unknown host")) {
                        if (WebsocketConnection.this.logger.logsDebug()) {
                            WebsocketConnection.this.logger.debug("If you are running on Android, have you added <uses-permission android:name=\"android.permission.INTERNET\" /> under <manifest> in AndroidManifest.xml?");
                        }
                    } else if (WebsocketConnection.this.logger.logsDebug()) {
                        WebsocketConnection.this.logger.debug("|" + e.getMessage() + "|");
                    }
                    WebsocketConnection.this.onClosed();
                }
            });
        }

        public void onLogMessage(String msg) {
            if (WebsocketConnection.this.logger.logsDebug()) {
                WebsocketConnection.this.logger.debug("Tubesock: " + msg);
            }
        }

        public void send(String msg) {
            this.ws.send(msg);
        }

        public void close() {
            this.ws.close();
        }

        private void shutdown() {
            this.ws.close();
            try {
                this.ws.blockClose();
            } catch (InterruptedException e) {
                WebsocketConnection.this.logger.error("Interrupted while shutting down websocket threads", e);
            }
        }

        public void connect() {
            try {
                this.ws.connect();
            } catch (WebSocketException e) {
                if (WebsocketConnection.this.logger.logsDebug()) {
                    WebsocketConnection.this.logger.debug("Error connecting", e);
                }
                shutdown();
            }
        }
    }

    public WebsocketConnection(Context ctx, RepoInfo repoInfo, Delegate delegate) {
        long connId = connectionId;
        connectionId = 1 + connId;
        this.mapper = JsonHelpers.getMapper();
        this.mapType = this.mapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        this.delegate = delegate;
        this.ctx = ctx;
        this.logger = ctx.getLogger("WebSocket", "ws_" + connId);
        this.conn = createConnection(repoInfo);
    }

    private WSClient createConnection(RepoInfo repoInfo) {
        URI uri = repoInfo.getConnectionURL();
        Map<String, String> extraHeaders = new HashMap();
        extraHeaders.put(HTTP.USER_AGENT, this.ctx.getUserAgent());
        return new WSClientTubesock(new WebSocket(uri, null, extraHeaders));
    }

    public void open() {
        this.conn.connect();
        this.connectTimeout = this.ctx.getRunLoop().schedule(new Runnable() {
            public void run() {
                WebsocketConnection.this.closeIfNeverConnected();
            }
        }, CONNECT_TIMEOUT);
    }

    public void start() {
    }

    public void close() {
        if (this.logger.logsDebug()) {
            this.logger.debug("websocket is being closed");
        }
        this.isClosed = true;
        this.conn.close();
        if (this.connectTimeout != null) {
            this.connectTimeout.cancel(true);
        }
        if (this.keepAlive != null) {
            this.keepAlive.cancel(true);
        }
    }

    public void send(Map<String, Object> message) {
        resetKeepAlive();
        try {
            String[] segs = Utilities.splitIntoFrames(this.mapper.writeValueAsString(message), MAX_FRAME_SIZE);
            if (segs.length > 1) {
                this.conn.send(BuildConfig.FLAVOR + segs.length);
            }
            for (String send : segs) {
                this.conn.send(send);
            }
        } catch (IOException e) {
            this.logger.error("Failed to serialize message: " + message.toString(), e);
            shutdown();
        }
    }

    private void appendFrame(String message) {
        this.frameReader.addString(message);
        this.totalFrames--;
        if (this.totalFrames == 0) {
            try {
                this.frameReader.freeze();
                Map<String, Object> decoded = (Map) this.mapper.readValue(this.frameReader, this.mapType);
                this.frameReader = null;
                if (this.logger.logsDebug()) {
                    this.logger.debug("handleIncomingFrame complete frame: " + decoded);
                }
                this.delegate.onMessage(decoded);
            } catch (IOException e) {
                this.logger.error("Error parsing frame: " + this.frameReader.toString(), e);
                close();
                shutdown();
            } catch (ClassCastException e2) {
                this.logger.error("Error parsing frame (cast error): " + this.frameReader.toString(), e2);
                close();
                shutdown();
            }
        }
    }

    private void handleNewFrameCount(int numFrames) {
        this.totalFrames = (long) numFrames;
        this.frameReader = new StringListReader();
        if (this.logger.logsDebug()) {
            this.logger.debug("HandleNewFrameCount: " + this.totalFrames);
        }
    }

    private String extractFrameCount(String message) {
        if (message.length() <= 6) {
            try {
                int frameCount = Integer.parseInt(message);
                if (frameCount > 0) {
                    handleNewFrameCount(frameCount);
                }
                return null;
            } catch (NumberFormatException e) {
            }
        }
        handleNewFrameCount(1);
        return message;
    }

    private void handleIncomingFrame(String message) {
        if (!this.isClosed) {
            resetKeepAlive();
            if (isBuffering()) {
                appendFrame(message);
                return;
            }
            String remaining = extractFrameCount(message);
            if (remaining != null) {
                appendFrame(remaining);
            }
        }
    }

    private void resetKeepAlive() {
        if (!this.isClosed) {
            if (this.keepAlive != null) {
                this.keepAlive.cancel(false);
                if (this.logger.logsDebug()) {
                    this.logger.debug("Reset keepAlive. Remaining: " + this.keepAlive.getDelay(TimeUnit.MILLISECONDS));
                }
            } else if (this.logger.logsDebug()) {
                this.logger.debug("Reset keepAlive");
            }
            this.keepAlive = this.ctx.getRunLoop().schedule(nop(), KEEP_ALIVE);
        }
    }

    private Runnable nop() {
        return new Runnable() {
            public void run() {
                if (WebsocketConnection.this.conn != null) {
                    WebsocketConnection.this.conn.send(HSConsts.STATUS_NEW);
                    WebsocketConnection.this.resetKeepAlive();
                }
            }
        };
    }

    private boolean isBuffering() {
        return this.frameReader != null;
    }

    private void onClosed() {
        if (!this.isClosed) {
            if (this.logger.logsDebug()) {
                this.logger.debug("closing itself");
            }
            shutdown();
        }
        this.conn = null;
        if (this.keepAlive != null) {
            this.keepAlive.cancel(false);
        }
    }

    private void shutdown() {
        this.isClosed = true;
        this.delegate.onDisconnect(this.everConnected);
    }

    private void closeIfNeverConnected() {
        if (!this.everConnected && !this.isClosed) {
            if (this.logger.logsDebug()) {
                this.logger.debug("timed out on connect");
            }
            this.conn.close();
        }
    }
}
