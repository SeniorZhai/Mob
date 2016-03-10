package com.firebase.tubesock;

import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.StrictHostnameVerifier;
import org.apache.http.protocol.HTTP;

public class WebSocket extends Thread {
    static final byte OPCODE_BINARY = (byte) 2;
    static final byte OPCODE_CLOSE = (byte) 8;
    static final byte OPCODE_NONE = (byte) 0;
    static final byte OPCODE_PING = (byte) 9;
    static final byte OPCODE_PONG = (byte) 10;
    static final byte OPCODE_TEXT = (byte) 1;
    private static final String THREAD_BASE_NAME = "TubeSock";
    private static final Charset UTF8 = Charset.forName(HTTP.UTF_8);
    private static final AtomicInteger clientCount = new AtomicInteger(0);
    private final int clientId;
    private WebSocketEventHandler eventHandler;
    private final WebSocketHandshake handshake;
    private final WebSocketReceiver receiver;
    private volatile Socket socket;
    private volatile State state;
    private final URI url;
    private final WebSocketWriter writer;

    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$firebase$tubesock$WebSocket$State = new int[State.values().length];

        static {
            try {
                $SwitchMap$com$firebase$tubesock$WebSocket$State[State.NONE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$firebase$tubesock$WebSocket$State[State.CONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$firebase$tubesock$WebSocket$State[State.CONNECTED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$firebase$tubesock$WebSocket$State[State.DISCONNECTING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
            try {
                $SwitchMap$com$firebase$tubesock$WebSocket$State[State.DISCONNECTED.ordinal()] = 5;
            } catch (NoSuchFieldError e5) {
            }
        }
    }

    private enum State {
        NONE,
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }

    public WebSocket(URI url) {
        this(url, null);
    }

    public WebSocket(URI url, String protocol) {
        this(url, protocol, null);
    }

    public WebSocket(URI url, String protocol, Map<String, String> extraHeaders) {
        this.state = State.NONE;
        this.socket = null;
        this.eventHandler = null;
        this.clientId = clientCount.incrementAndGet();
        this.url = url;
        this.handshake = new WebSocketHandshake(url, protocol, extraHeaders);
        this.receiver = new WebSocketReceiver(this);
        this.writer = new WebSocketWriter(this, THREAD_BASE_NAME, this.clientId);
    }

    public void setEventHandler(WebSocketEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    WebSocketEventHandler getEventHandler() {
        return this.eventHandler;
    }

    public synchronized void connect() {
        if (this.state != State.NONE) {
            this.eventHandler.onError(new WebSocketException("connect() already called"));
            close();
        } else {
            setName("TubeSockReader-" + this.clientId);
            this.state = State.CONNECTING;
            start();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        /*
        r22 = this;
        r16 = r22.createSocket();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        monitor-enter(r22);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r16;
        r1 = r22;
        r1.socket = r0;	 Catch:{ all -> 0x0038 }
        r0 = r22;
        r0 = r0.state;	 Catch:{ all -> 0x0038 }
        r18 = r0;
        r19 = com.firebase.tubesock.WebSocket.State.DISCONNECTED;	 Catch:{ all -> 0x0038 }
        r0 = r18;
        r1 = r19;
        if (r0 != r1) goto L_0x004d;
    L_0x0019:
        r0 = r22;
        r0 = r0.socket;	 Catch:{ IOException -> 0x002f }
        r18 = r0;
        r18.close();	 Catch:{ IOException -> 0x002f }
        r18 = 0;
        r0 = r18;
        r1 = r22;
        r1.socket = r0;	 Catch:{ all -> 0x0038 }
        monitor-exit(r22);	 Catch:{ all -> 0x0038 }
        r22.close();
    L_0x002e:
        return;
    L_0x002f:
        r4 = move-exception;
        r18 = new java.lang.RuntimeException;	 Catch:{ all -> 0x0038 }
        r0 = r18;
        r0.<init>(r4);	 Catch:{ all -> 0x0038 }
        throw r18;	 Catch:{ all -> 0x0038 }
    L_0x0038:
        r18 = move-exception;
        monitor-exit(r22);	 Catch:{ all -> 0x0038 }
        throw r18;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
    L_0x003b:
        r17 = move-exception;
        r0 = r22;
        r0 = r0.eventHandler;	 Catch:{ all -> 0x00fd }
        r18 = r0;
        r0 = r18;
        r1 = r17;
        r0.onError(r1);	 Catch:{ all -> 0x00fd }
        r22.close();
        goto L_0x002e;
    L_0x004d:
        monitor-exit(r22);	 Catch:{ all -> 0x0038 }
        r9 = new java.io.DataInputStream;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = r16.getInputStream();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r18;
        r9.<init>(r0);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r14 = r16.getOutputStream();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r22;
        r0 = r0.handshake;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = r0;
        r18 = r18.getHandshake();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r18;
        r14.write(r0);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r5 = 0;
        r12 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r3 = new byte[r12];	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r15 = 0;
        r6 = new java.util.ArrayList;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r6.<init>();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
    L_0x0077:
        if (r5 != 0) goto L_0x012c;
    L_0x0079:
        r2 = r9.read();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = -1;
        r0 = r18;
        if (r2 != r0) goto L_0x00ba;
    L_0x0083:
        r18 = new com.firebase.tubesock.WebSocketException;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r19 = "Connection closed before handshake was complete";
        r18.<init>(r19);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        throw r18;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
    L_0x008b:
        r10 = move-exception;
        r0 = r22;
        r0 = r0.eventHandler;	 Catch:{ all -> 0x00fd }
        r18 = r0;
        r19 = new com.firebase.tubesock.WebSocketException;	 Catch:{ all -> 0x00fd }
        r20 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00fd }
        r20.<init>();	 Catch:{ all -> 0x00fd }
        r21 = "error while connecting: ";
        r20 = r20.append(r21);	 Catch:{ all -> 0x00fd }
        r21 = r10.getMessage();	 Catch:{ all -> 0x00fd }
        r20 = r20.append(r21);	 Catch:{ all -> 0x00fd }
        r20 = r20.toString();	 Catch:{ all -> 0x00fd }
        r0 = r19;
        r1 = r20;
        r0.<init>(r1, r10);	 Catch:{ all -> 0x00fd }
        r18.onError(r19);	 Catch:{ all -> 0x00fd }
        r22.close();
        goto L_0x002e;
    L_0x00ba:
        r0 = (byte) r2;
        r18 = r0;
        r3[r15] = r18;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r15 = r15 + 1;
        r18 = r15 + -1;
        r18 = r3[r18];	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r19 = 10;
        r0 = r18;
        r1 = r19;
        if (r0 != r1) goto L_0x0102;
    L_0x00cd:
        r18 = r15 + -2;
        r18 = r3[r18];	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r19 = 13;
        r0 = r18;
        r1 = r19;
        if (r0 != r1) goto L_0x0102;
    L_0x00d9:
        r13 = new java.lang.String;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = UTF8;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r18;
        r13.<init>(r3, r0);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = r13.trim();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r19 = "";
        r18 = r18.equals(r19);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        if (r18 == 0) goto L_0x00f3;
    L_0x00ee:
        r5 = 1;
    L_0x00ef:
        r3 = new byte[r12];	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r15 = 0;
        goto L_0x0077;
    L_0x00f3:
        r18 = r13.trim();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r18;
        r6.add(r0);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        goto L_0x00ef;
    L_0x00fd:
        r18 = move-exception;
        r22.close();
        throw r18;
    L_0x0102:
        r18 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r0 = r18;
        if (r15 != r0) goto L_0x0077;
    L_0x0108:
        r13 = new java.lang.String;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = UTF8;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r18;
        r13.<init>(r3, r0);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = new com.firebase.tubesock.WebSocketException;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r19 = new java.lang.StringBuilder;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r19.<init>();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r20 = "Unexpected long line in handshake: ";
        r19 = r19.append(r20);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r19;
        r19 = r0.append(r13);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r19 = r19.toString();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18.<init>(r19);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        throw r18;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
    L_0x012c:
        r0 = r22;
        r0 = r0.handshake;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r19 = r0;
        r18 = 0;
        r0 = r18;
        r18 = r6.get(r0);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = (java.lang.String) r18;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r19;
        r1 = r18;
        r0.verifyServerStatusLine(r1);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = 0;
        r0 = r18;
        r6.remove(r0);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r7 = new java.util.HashMap;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r7.<init>();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r8 = r6.iterator();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
    L_0x0153:
        r18 = r8.hasNext();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        if (r18 == 0) goto L_0x017b;
    L_0x0159:
        r13 = r8.next();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r13 = (java.lang.String) r13;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = ": ";
        r19 = 2;
        r0 = r18;
        r1 = r19;
        r11 = r13.split(r0, r1);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = 0;
        r18 = r11[r18];	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r19 = 1;
        r19 = r11[r19];	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r18;
        r1 = r19;
        r7.put(r0, r1);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        goto L_0x0153;
    L_0x017b:
        r0 = r22;
        r0 = r0.handshake;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = r0;
        r0 = r18;
        r0.verifyServerHandshakeHeaders(r7);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r22;
        r0 = r0.writer;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = r0;
        r0 = r18;
        r0.setOutput(r14);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r22;
        r0 = r0.receiver;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = r0;
        r0 = r18;
        r0.setInput(r9);	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = com.firebase.tubesock.WebSocket.State.CONNECTED;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r18;
        r1 = r22;
        r1.state = r0;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r22;
        r0 = r0.writer;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = r0;
        r18.start();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r22;
        r0 = r0.eventHandler;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = r0;
        r18.onOpen();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r0 = r22;
        r0 = r0.receiver;	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r18 = r0;
        r18.run();	 Catch:{ WebSocketException -> 0x003b, IOException -> 0x008b }
        r22.close();
        goto L_0x002e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.firebase.tubesock.WebSocket.run():void");
    }

    public synchronized void send(String data) {
        send(OPCODE_TEXT, data.getBytes(UTF8));
    }

    public synchronized void send(byte[] data) {
        send(OPCODE_BINARY, data);
    }

    synchronized void pong(byte[] data) {
        send(OPCODE_PONG, data);
    }

    private synchronized void send(byte opcode, byte[] data) {
        if (this.state != State.CONNECTED) {
            this.eventHandler.onError(new WebSocketException("error while sending data: not connected"));
        } else {
            try {
                this.writer.send(opcode, true, data);
            } catch (IOException e) {
                this.eventHandler.onError(new WebSocketException("Failed to send frame", e));
                close();
            }
        }
    }

    void handleReceiverError(WebSocketException e) {
        this.eventHandler.onError(e);
        if (this.state == State.CONNECTED) {
            close();
        }
    }

    public synchronized void close() {
        switch (AnonymousClass1.$SwitchMap$com$firebase$tubesock$WebSocket$State[this.state.ordinal()]) {
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                this.state = State.DISCONNECTED;
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                closeSocket();
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                sendCloseHandshake();
                break;
        }
    }

    void onCloseOpReceived() {
        closeSocket();
    }

    private synchronized void closeSocket() {
        if (this.state != State.DISCONNECTED) {
            this.receiver.stopit();
            this.writer.stopIt();
            if (this.socket != null) {
                try {
                    this.socket.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            this.state = State.DISCONNECTED;
            this.eventHandler.onClose();
        }
    }

    private void sendCloseHandshake() {
        try {
            this.state = State.DISCONNECTING;
            this.writer.stopIt();
            this.writer.send(OPCODE_CLOSE, true, new byte[0]);
        } catch (IOException e) {
            this.eventHandler.onError(new WebSocketException("Failed to send close frame", e));
        }
    }

    private Socket createSocket() {
        String scheme = this.url.getScheme();
        String host = this.url.getHost();
        int port = this.url.getPort();
        if (scheme != null && scheme.equals("ws")) {
            if (port == -1) {
                port = 80;
            }
            try {
                return new Socket(host, port);
            } catch (UnknownHostException uhe) {
                throw new WebSocketException("unknown host: " + host, uhe);
            } catch (IOException ioe) {
                throw new WebSocketException("error while creating socket to " + this.url, ioe);
            }
        } else if (scheme == null || !scheme.equals("wss")) {
            throw new WebSocketException("unsupported protocol: " + scheme);
        } else {
            if (port == -1) {
                port = com.mixpanel.android.java_websocket.WebSocket.DEFAULT_WSS_PORT;
            }
            try {
                Socket socket = SSLSocketFactory.getDefault().createSocket(host, port);
                verifyHost((SSLSocket) socket, host);
                return socket;
            } catch (UnknownHostException uhe2) {
                throw new WebSocketException("unknown host: " + host, uhe2);
            } catch (IOException ioe2) {
                throw new WebSocketException("error while creating secure socket to " + this.url, ioe2);
            }
        }
    }

    private void verifyHost(SSLSocket socket, String host) throws SSLException {
        new StrictHostnameVerifier().verify(host, socket.getSession().getPeerCertificates()[0]);
    }

    public void blockClose() throws InterruptedException {
        if (this.writer.getState() != java.lang.Thread.State.NEW) {
            this.writer.join();
        }
        join();
    }
}
