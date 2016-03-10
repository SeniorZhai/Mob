package com.mixpanel.android.java_websocket;

import android.annotation.SuppressLint;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.mixpanel.android.java_websocket.WebSocket.READYSTATE;
import com.mixpanel.android.java_websocket.WebSocket.Role;
import com.mixpanel.android.java_websocket.drafts.Draft;
import com.mixpanel.android.java_websocket.drafts.Draft.CloseHandshakeType;
import com.mixpanel.android.java_websocket.drafts.Draft.HandshakeState;
import com.mixpanel.android.java_websocket.drafts.Draft_10;
import com.mixpanel.android.java_websocket.drafts.Draft_17;
import com.mixpanel.android.java_websocket.drafts.Draft_75;
import com.mixpanel.android.java_websocket.drafts.Draft_76;
import com.mixpanel.android.java_websocket.exceptions.IncompleteHandshakeException;
import com.mixpanel.android.java_websocket.exceptions.InvalidDataException;
import com.mixpanel.android.java_websocket.exceptions.InvalidHandshakeException;
import com.mixpanel.android.java_websocket.exceptions.WebsocketNotConnectedException;
import com.mixpanel.android.java_websocket.framing.CloseFrame;
import com.mixpanel.android.java_websocket.framing.CloseFrameBuilder;
import com.mixpanel.android.java_websocket.framing.Framedata;
import com.mixpanel.android.java_websocket.framing.Framedata.Opcode;
import com.mixpanel.android.java_websocket.handshake.ClientHandshake;
import com.mixpanel.android.java_websocket.handshake.ClientHandshakeBuilder;
import com.mixpanel.android.java_websocket.handshake.Handshakedata;
import com.mixpanel.android.java_websocket.handshake.ServerHandshake;
import com.mixpanel.android.java_websocket.server.WebSocketServer.WebSocketWorker;
import com.mixpanel.android.java_websocket.util.Charsetfunctions;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.BuildConfig;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressLint({"Assert"})
public class WebSocketImpl implements WebSocket {
    static final /* synthetic */ boolean $assertionsDisabled = (!WebSocketImpl.class.desiredAssertionStatus());
    public static boolean DEBUG = false;
    public static int RCVBUF = AccessibilityNodeInfoCompat.ACTION_COPY;
    public static final List<Draft> defaultdraftlist = new ArrayList(4);
    public ByteChannel channel;
    private Integer closecode;
    private Boolean closedremotely;
    private String closemessage;
    private Opcode current_continuous_frame_opcode;
    private Draft draft;
    private volatile boolean flushandclosestate;
    private ClientHandshake handshakerequest;
    public final BlockingQueue<ByteBuffer> inQueue;
    public SelectionKey key;
    private List<Draft> knownDrafts;
    public final BlockingQueue<ByteBuffer> outQueue;
    private READYSTATE readystate;
    private String resourceDescriptor;
    private Role role;
    private ByteBuffer tmpHandshakeBytes;
    public volatile WebSocketWorker workerThread;
    private final WebSocketListener wsl;

    static {
        defaultdraftlist.add(new Draft_17());
        defaultdraftlist.add(new Draft_10());
        defaultdraftlist.add(new Draft_76());
        defaultdraftlist.add(new Draft_75());
    }

    public WebSocketImpl(WebSocketListener listener, List<Draft> drafts) {
        this(listener, (Draft) null);
        this.role = Role.SERVER;
        if (drafts == null || drafts.isEmpty()) {
            this.knownDrafts = defaultdraftlist;
        } else {
            this.knownDrafts = drafts;
        }
    }

    public WebSocketImpl(WebSocketListener listener, Draft draft) {
        this.flushandclosestate = false;
        this.readystate = READYSTATE.NOT_YET_CONNECTED;
        this.draft = null;
        this.current_continuous_frame_opcode = null;
        this.tmpHandshakeBytes = ByteBuffer.allocate(0);
        this.handshakerequest = null;
        this.closemessage = null;
        this.closecode = null;
        this.closedremotely = null;
        this.resourceDescriptor = null;
        if (listener == null || (draft == null && this.role == Role.SERVER)) {
            throw new IllegalArgumentException("parameters must not be null");
        }
        this.outQueue = new LinkedBlockingQueue();
        this.inQueue = new LinkedBlockingQueue();
        this.wsl = listener;
        this.role = Role.CLIENT;
        if (draft != null) {
            this.draft = draft.copyInstance();
        }
    }

    @Deprecated
    public WebSocketImpl(WebSocketListener listener, Draft draft, Socket socket) {
        this(listener, draft);
    }

    @Deprecated
    public WebSocketImpl(WebSocketListener listener, List<Draft> drafts, Socket socket) {
        this(listener, (List) drafts);
    }

    public void decode(ByteBuffer socketBuffer) {
        if ($assertionsDisabled || socketBuffer.hasRemaining()) {
            if (DEBUG) {
                System.out.println("process(" + socketBuffer.remaining() + "): {" + (socketBuffer.remaining() > Constants.UPDATE_COFIG_INTERVAL ? "too big to display" : new String(socketBuffer.array(), socketBuffer.position(), socketBuffer.remaining())) + "}");
            }
            if (this.readystate != READYSTATE.NOT_YET_CONNECTED) {
                decodeFrames(socketBuffer);
            } else if (decodeHandshake(socketBuffer)) {
                if (!$assertionsDisabled && this.tmpHandshakeBytes.hasRemaining() == socketBuffer.hasRemaining() && socketBuffer.hasRemaining()) {
                    throw new AssertionError();
                } else if (socketBuffer.hasRemaining()) {
                    decodeFrames(socketBuffer);
                } else if (this.tmpHandshakeBytes.hasRemaining()) {
                    decodeFrames(this.tmpHandshakeBytes);
                }
            }
            if (!$assertionsDisabled && !isClosing() && !isFlushAndClose() && socketBuffer.hasRemaining()) {
                throw new AssertionError();
            }
            return;
        }
        throw new AssertionError();
    }

    private boolean decodeHandshake(ByteBuffer socketBufferNew) {
        ByteBuffer socketBuffer;
        if (this.tmpHandshakeBytes.capacity() == 0) {
            socketBuffer = socketBufferNew;
        } else {
            if (this.tmpHandshakeBytes.remaining() < socketBufferNew.remaining()) {
                ByteBuffer buf = ByteBuffer.allocate(this.tmpHandshakeBytes.capacity() + socketBufferNew.remaining());
                this.tmpHandshakeBytes.flip();
                buf.put(this.tmpHandshakeBytes);
                this.tmpHandshakeBytes = buf;
            }
            this.tmpHandshakeBytes.put(socketBufferNew);
            this.tmpHandshakeBytes.flip();
            socketBuffer = this.tmpHandshakeBytes;
        }
        socketBuffer.mark();
        try {
            if (this.draft == null && isFlashEdgeCase(socketBuffer) == HandshakeState.MATCHED) {
                try {
                    write(ByteBuffer.wrap(Charsetfunctions.utf8Bytes(this.wsl.getFlashPolicy(this))));
                    close(-3, BuildConfig.FLAVOR);
                } catch (InvalidDataException e) {
                    close(CloseFrame.ABNORMAL_CLOSE, "remote peer closed connection before flashpolicy could be transmitted", true);
                }
                return false;
            }
            try {
                Handshakedata tmphandshake;
                if (this.role != Role.SERVER) {
                    if (this.role == Role.CLIENT) {
                        this.draft.setParseMode(this.role);
                        tmphandshake = this.draft.translateHandshake(socketBuffer);
                        if (tmphandshake instanceof ServerHandshake) {
                            ServerHandshake handshake = (ServerHandshake) tmphandshake;
                            if (this.draft.acceptHandshakeAsClient(this.handshakerequest, handshake) == HandshakeState.MATCHED) {
                                try {
                                    this.wsl.onWebsocketHandshakeReceivedAsClient(this, this.handshakerequest, handshake);
                                    open(handshake);
                                    return true;
                                } catch (InvalidDataException e2) {
                                    flushAndClose(e2.getCloseCode(), e2.getMessage(), false);
                                    return false;
                                } catch (RuntimeException e3) {
                                    this.wsl.onWebsocketError(this, e3);
                                    flushAndClose(-1, e3.getMessage(), false);
                                    return false;
                                }
                            }
                            close(CloseFrame.PROTOCOL_ERROR, "draft " + this.draft + " refuses handshake");
                        } else {
                            flushAndClose(CloseFrame.PROTOCOL_ERROR, "wrong http function", false);
                            return false;
                        }
                    }
                    return false;
                } else if (this.draft == null) {
                    for (Draft d : this.knownDrafts) {
                        Draft d2 = d2.copyInstance();
                        try {
                            d2.setParseMode(this.role);
                            socketBuffer.reset();
                            tmphandshake = d2.translateHandshake(socketBuffer);
                            if (tmphandshake instanceof ClientHandshake) {
                                handshake = (ClientHandshake) tmphandshake;
                                if (d2.acceptHandshakeAsServer(handshake) == HandshakeState.MATCHED) {
                                    this.resourceDescriptor = handshake.getResourceDescriptor();
                                    try {
                                        write(d2.createHandshake(d2.postProcessHandshakeResponseAsServer(handshake, this.wsl.onWebsocketHandshakeReceivedAsServer(this, d2, handshake)), this.role));
                                        this.draft = d2;
                                        open(handshake);
                                        return true;
                                    } catch (InvalidDataException e22) {
                                        flushAndClose(e22.getCloseCode(), e22.getMessage(), false);
                                        return false;
                                    } catch (RuntimeException e32) {
                                        this.wsl.onWebsocketError(this, e32);
                                        flushAndClose(-1, e32.getMessage(), false);
                                        return false;
                                    }
                                }
                                continue;
                            } else {
                                flushAndClose(CloseFrame.PROTOCOL_ERROR, "wrong http function", false);
                                return false;
                            }
                        } catch (InvalidHandshakeException e4) {
                        }
                    }
                    if (this.draft == null) {
                        close(CloseFrame.PROTOCOL_ERROR, "no draft matches");
                    }
                    return false;
                } else {
                    tmphandshake = this.draft.translateHandshake(socketBuffer);
                    if (tmphandshake instanceof ClientHandshake) {
                        handshake = (ClientHandshake) tmphandshake;
                        if (this.draft.acceptHandshakeAsServer(handshake) == HandshakeState.MATCHED) {
                            open(handshake);
                            return true;
                        }
                        close(CloseFrame.PROTOCOL_ERROR, "the handshake did finaly not match");
                        return false;
                    }
                    flushAndClose(CloseFrame.PROTOCOL_ERROR, "wrong http function", false);
                    return false;
                }
            } catch (InvalidDataException e222) {
                close(e222);
            }
        } catch (IncompleteHandshakeException e5) {
            if (this.tmpHandshakeBytes.capacity() == 0) {
                socketBuffer.reset();
                int newsize = e5.getPreferedSize();
                if (newsize == 0) {
                    newsize = socketBuffer.capacity() + 16;
                } else if (!$assertionsDisabled && e5.getPreferedSize() < socketBuffer.remaining()) {
                    throw new AssertionError();
                }
                this.tmpHandshakeBytes = ByteBuffer.allocate(newsize);
                this.tmpHandshakeBytes.put(socketBufferNew);
            } else {
                this.tmpHandshakeBytes.position(this.tmpHandshakeBytes.limit());
                this.tmpHandshakeBytes.limit(this.tmpHandshakeBytes.capacity());
            }
        }
    }

    private void decodeFrames(ByteBuffer socketBuffer) {
        try {
            for (Framedata f : this.draft.translateFrame(socketBuffer)) {
                if (DEBUG) {
                    System.out.println("matched frame: " + f);
                }
                Opcode curop = f.getOpcode();
                boolean fin = f.isFin();
                if (curop == Opcode.CLOSING) {
                    int code = CloseFrame.NOCODE;
                    String reason = BuildConfig.FLAVOR;
                    if (f instanceof CloseFrame) {
                        CloseFrame cf = (CloseFrame) f;
                        code = cf.getCloseCode();
                        reason = cf.getMessage();
                    }
                    if (this.readystate == READYSTATE.CLOSING) {
                        closeConnection(code, reason, true);
                    } else if (this.draft.getCloseHandshakeType() == CloseHandshakeType.TWOWAY) {
                        close(code, reason, true);
                    } else {
                        flushAndClose(code, reason, false);
                    }
                } else if (curop == Opcode.PING) {
                    this.wsl.onWebsocketPing(this, f);
                } else if (curop == Opcode.PONG) {
                    this.wsl.onWebsocketPong(this, f);
                } else if (!fin || curop == Opcode.CONTINUOUS) {
                    if (curop != Opcode.CONTINUOUS) {
                        if (this.current_continuous_frame_opcode != null) {
                            throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "Previous continuous frame sequence not completed.");
                        }
                        this.current_continuous_frame_opcode = curop;
                    } else if (fin) {
                        if (this.current_continuous_frame_opcode == null) {
                            throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "Continuous frame sequence was not started.");
                        }
                        this.current_continuous_frame_opcode = null;
                    } else if (this.current_continuous_frame_opcode == null) {
                        throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "Continuous frame sequence was not started.");
                    }
                    try {
                        this.wsl.onWebsocketMessageFragment(this, f);
                    } catch (RuntimeException e) {
                        this.wsl.onWebsocketError(this, e);
                    }
                } else if (this.current_continuous_frame_opcode != null) {
                    throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "Continuous frame sequence not completed.");
                } else if (curop == Opcode.TEXT) {
                    try {
                        this.wsl.onWebsocketMessage((WebSocket) this, Charsetfunctions.stringUtf8(f.getPayloadData()));
                    } catch (RuntimeException e2) {
                        this.wsl.onWebsocketError(this, e2);
                    }
                } else if (curop == Opcode.BINARY) {
                    try {
                        this.wsl.onWebsocketMessage((WebSocket) this, f.getPayloadData());
                    } catch (RuntimeException e22) {
                        this.wsl.onWebsocketError(this, e22);
                    }
                } else {
                    throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "non control or continious frame expected");
                }
            }
        } catch (InvalidDataException e1) {
            this.wsl.onWebsocketError(this, e1);
            close(e1);
        }
    }

    private void close(int code, String message, boolean remote) {
        if (this.readystate != READYSTATE.CLOSING && this.readystate != READYSTATE.CLOSED) {
            if (this.readystate == READYSTATE.OPEN) {
                if (code != CloseFrame.ABNORMAL_CLOSE) {
                    if (this.draft.getCloseHandshakeType() != CloseHandshakeType.NONE) {
                        if (!remote) {
                            try {
                                this.wsl.onWebsocketCloseInitiated(this, code, message);
                            } catch (RuntimeException e) {
                                this.wsl.onWebsocketError(this, e);
                            }
                        }
                        try {
                            sendFrame(new CloseFrameBuilder(code, message));
                        } catch (InvalidDataException e2) {
                            this.wsl.onWebsocketError(this, e2);
                            flushAndClose(CloseFrame.ABNORMAL_CLOSE, "generated frame is invalid", false);
                        }
                    }
                    flushAndClose(code, message, remote);
                } else if ($assertionsDisabled || !remote) {
                    this.readystate = READYSTATE.CLOSING;
                    flushAndClose(code, message, false);
                    return;
                } else {
                    throw new AssertionError();
                }
            } else if (code != -3) {
                flushAndClose(-1, message, false);
            } else if ($assertionsDisabled || remote) {
                flushAndClose(-3, message, true);
            } else {
                throw new AssertionError();
            }
            if (code == CloseFrame.PROTOCOL_ERROR) {
                flushAndClose(code, message, remote);
            }
            this.readystate = READYSTATE.CLOSING;
            this.tmpHandshakeBytes = null;
        }
    }

    public void close(int code, String message) {
        close(code, message, false);
    }

    protected synchronized void closeConnection(int code, String message, boolean remote) {
        if (this.readystate != READYSTATE.CLOSED) {
            if (this.key != null) {
                this.key.cancel();
            }
            if (this.channel != null) {
                try {
                    this.channel.close();
                } catch (IOException e) {
                    this.wsl.onWebsocketError(this, e);
                }
            }
            try {
                this.wsl.onWebsocketClose(this, code, message, remote);
            } catch (RuntimeException e2) {
                this.wsl.onWebsocketError(this, e2);
            }
            if (this.draft != null) {
                this.draft.reset();
            }
            this.handshakerequest = null;
            this.readystate = READYSTATE.CLOSED;
            this.outQueue.clear();
        }
    }

    protected void closeConnection(int code, boolean remote) {
        closeConnection(code, BuildConfig.FLAVOR, remote);
    }

    public void closeConnection() {
        if (this.closedremotely == null) {
            throw new IllegalStateException("this method must be used in conjuction with flushAndClose");
        }
        closeConnection(this.closecode.intValue(), this.closemessage, this.closedremotely.booleanValue());
    }

    public void closeConnection(int code, String message) {
        closeConnection(code, message, false);
    }

    protected synchronized void flushAndClose(int code, String message, boolean remote) {
        if (!this.flushandclosestate) {
            this.closecode = Integer.valueOf(code);
            this.closemessage = message;
            this.closedremotely = Boolean.valueOf(remote);
            this.flushandclosestate = true;
            this.wsl.onWriteDemand(this);
            try {
                this.wsl.onWebsocketClosing(this, code, message, remote);
            } catch (RuntimeException e) {
                this.wsl.onWebsocketError(this, e);
            }
            if (this.draft != null) {
                this.draft.reset();
            }
            this.handshakerequest = null;
        }
    }

    public void eot() {
        if (getReadyState() == READYSTATE.NOT_YET_CONNECTED) {
            closeConnection(-1, true);
        } else if (this.flushandclosestate) {
            closeConnection(this.closecode.intValue(), this.closemessage, this.closedremotely.booleanValue());
        } else if (this.draft.getCloseHandshakeType() == CloseHandshakeType.NONE) {
            closeConnection((int) Constants.UPDATE_COFIG_INTERVAL, true);
        } else if (this.draft.getCloseHandshakeType() != CloseHandshakeType.ONEWAY) {
            closeConnection((int) CloseFrame.ABNORMAL_CLOSE, true);
        } else if (this.role == Role.SERVER) {
            closeConnection((int) CloseFrame.ABNORMAL_CLOSE, true);
        } else {
            closeConnection((int) Constants.UPDATE_COFIG_INTERVAL, true);
        }
    }

    public void close(int code) {
        close(code, BuildConfig.FLAVOR, false);
    }

    public void close(InvalidDataException e) {
        close(e.getCloseCode(), e.getMessage(), false);
    }

    public void send(String text) throws WebsocketNotConnectedException {
        if (text == null) {
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        }
        send(this.draft.createFrames(text, this.role == Role.CLIENT));
    }

    public void send(ByteBuffer bytes) throws IllegalArgumentException, WebsocketNotConnectedException {
        if (bytes == null) {
            throw new IllegalArgumentException("Cannot send 'null' data to a WebSocketImpl.");
        }
        send(this.draft.createFrames(bytes, this.role == Role.CLIENT));
    }

    public void send(byte[] bytes) throws IllegalArgumentException, WebsocketNotConnectedException {
        send(ByteBuffer.wrap(bytes));
    }

    private void send(Collection<Framedata> frames) {
        if (isOpen()) {
            for (Framedata f : frames) {
                sendFrame(f);
            }
            return;
        }
        throw new WebsocketNotConnectedException();
    }

    public void sendFragmentedFrame(Opcode op, ByteBuffer buffer, boolean fin) {
        send(this.draft.continuousFrame(op, buffer, fin));
    }

    public void sendFrame(Framedata framedata) {
        if (DEBUG) {
            System.out.println("send frame: " + framedata);
        }
        write(this.draft.createBinaryFrame(framedata));
    }

    public boolean hasBufferedData() {
        return !this.outQueue.isEmpty();
    }

    private HandshakeState isFlashEdgeCase(ByteBuffer request) throws IncompleteHandshakeException {
        request.mark();
        if (request.limit() > Draft.FLASH_POLICY_REQUEST.length) {
            return HandshakeState.NOT_MATCHED;
        }
        if (request.limit() < Draft.FLASH_POLICY_REQUEST.length) {
            throw new IncompleteHandshakeException(Draft.FLASH_POLICY_REQUEST.length);
        }
        int flash_policy_index = 0;
        while (request.hasRemaining()) {
            if (Draft.FLASH_POLICY_REQUEST[flash_policy_index] != request.get()) {
                request.reset();
                return HandshakeState.NOT_MATCHED;
            }
            flash_policy_index++;
        }
        return HandshakeState.MATCHED;
    }

    public void startHandshake(ClientHandshakeBuilder handshakedata) throws InvalidHandshakeException {
        if ($assertionsDisabled || this.readystate != READYSTATE.CONNECTING) {
            this.handshakerequest = this.draft.postProcessHandshakeRequestAsClient(handshakedata);
            this.resourceDescriptor = handshakedata.getResourceDescriptor();
            if ($assertionsDisabled || this.resourceDescriptor != null) {
                try {
                    this.wsl.onWebsocketHandshakeSentAsClient(this, this.handshakerequest);
                    write(this.draft.createHandshake(this.handshakerequest, this.role));
                    return;
                } catch (InvalidDataException e) {
                    throw new InvalidHandshakeException("Handshake data rejected by client.");
                } catch (RuntimeException e2) {
                    this.wsl.onWebsocketError(this, e2);
                    throw new InvalidHandshakeException("rejected because of" + e2);
                }
            }
            throw new AssertionError();
        }
        throw new AssertionError("shall only be called once");
    }

    private void write(ByteBuffer buf) {
        if (DEBUG) {
            System.out.println("write(" + buf.remaining() + "): {" + (buf.remaining() > Constants.UPDATE_COFIG_INTERVAL ? "too big to display" : new String(buf.array())) + "}");
        }
        this.outQueue.add(buf);
        this.wsl.onWriteDemand(this);
    }

    private void write(List<ByteBuffer> bufs) {
        for (ByteBuffer b : bufs) {
            write(b);
        }
    }

    private void open(Handshakedata d) {
        if (DEBUG) {
            System.out.println("open using draft: " + this.draft.getClass().getSimpleName());
        }
        this.readystate = READYSTATE.OPEN;
        try {
            this.wsl.onWebsocketOpen(this, d);
        } catch (RuntimeException e) {
            this.wsl.onWebsocketError(this, e);
        }
    }

    public boolean isConnecting() {
        if ($assertionsDisabled || !this.flushandclosestate || this.readystate == READYSTATE.CONNECTING) {
            return this.readystate == READYSTATE.CONNECTING;
        } else {
            throw new AssertionError();
        }
    }

    public boolean isOpen() {
        if ($assertionsDisabled || this.readystate != READYSTATE.OPEN || !this.flushandclosestate) {
            return this.readystate == READYSTATE.OPEN;
        } else {
            throw new AssertionError();
        }
    }

    public boolean isClosing() {
        return this.readystate == READYSTATE.CLOSING;
    }

    public boolean isFlushAndClose() {
        return this.flushandclosestate;
    }

    public boolean isClosed() {
        return this.readystate == READYSTATE.CLOSED;
    }

    public READYSTATE getReadyState() {
        return this.readystate;
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String toString() {
        return super.toString();
    }

    public InetSocketAddress getRemoteSocketAddress() {
        return this.wsl.getRemoteSocketAddress(this);
    }

    public InetSocketAddress getLocalSocketAddress() {
        return this.wsl.getLocalSocketAddress(this);
    }

    public Draft getDraft() {
        return this.draft;
    }

    public void close() {
        close((int) Constants.UPDATE_COFIG_INTERVAL);
    }

    public String getResourceDescriptor() {
        return this.resourceDescriptor;
    }
}
