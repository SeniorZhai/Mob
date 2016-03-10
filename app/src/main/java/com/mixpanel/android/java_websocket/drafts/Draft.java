package com.mixpanel.android.java_websocket.drafts;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.mixpanel.android.java_websocket.WebSocket.Role;
import com.mixpanel.android.java_websocket.exceptions.IncompleteHandshakeException;
import com.mixpanel.android.java_websocket.exceptions.InvalidDataException;
import com.mixpanel.android.java_websocket.exceptions.InvalidHandshakeException;
import com.mixpanel.android.java_websocket.exceptions.LimitExedeedException;
import com.mixpanel.android.java_websocket.framing.CloseFrame;
import com.mixpanel.android.java_websocket.framing.FrameBuilder;
import com.mixpanel.android.java_websocket.framing.Framedata;
import com.mixpanel.android.java_websocket.framing.Framedata.Opcode;
import com.mixpanel.android.java_websocket.framing.FramedataImpl1;
import com.mixpanel.android.java_websocket.handshake.ClientHandshake;
import com.mixpanel.android.java_websocket.handshake.ClientHandshakeBuilder;
import com.mixpanel.android.java_websocket.handshake.HandshakeBuilder;
import com.mixpanel.android.java_websocket.handshake.HandshakeImpl1Client;
import com.mixpanel.android.java_websocket.handshake.HandshakeImpl1Server;
import com.mixpanel.android.java_websocket.handshake.Handshakedata;
import com.mixpanel.android.java_websocket.handshake.ServerHandshake;
import com.mixpanel.android.java_websocket.handshake.ServerHandshakeBuilder;
import com.mixpanel.android.java_websocket.util.Charsetfunctions;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.BuildConfig;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.apache.http.HttpHeaders;
import org.apache.http.protocol.HTTP;

public abstract class Draft {
    public static final byte[] FLASH_POLICY_REQUEST = Charsetfunctions.utf8Bytes("<policy-file-request/>\u0000");
    public static int INITIAL_FAMESIZE = 64;
    public static int MAX_FAME_SIZE = Constants.UPDATE_COFIG_INTERVAL;
    protected Opcode continuousFrameType = null;
    protected Role role = null;

    public enum CloseHandshakeType {
        NONE,
        ONEWAY,
        TWOWAY
    }

    public enum HandshakeState {
        MATCHED,
        NOT_MATCHED
    }

    public abstract HandshakeState acceptHandshakeAsClient(ClientHandshake clientHandshake, ServerHandshake serverHandshake) throws InvalidHandshakeException;

    public abstract HandshakeState acceptHandshakeAsServer(ClientHandshake clientHandshake) throws InvalidHandshakeException;

    public abstract Draft copyInstance();

    public abstract ByteBuffer createBinaryFrame(Framedata framedata);

    public abstract List<Framedata> createFrames(String str, boolean z);

    public abstract List<Framedata> createFrames(ByteBuffer byteBuffer, boolean z);

    public abstract CloseHandshakeType getCloseHandshakeType();

    public abstract ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder clientHandshakeBuilder) throws InvalidHandshakeException;

    public abstract HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake clientHandshake, ServerHandshakeBuilder serverHandshakeBuilder) throws InvalidHandshakeException;

    public abstract void reset();

    public abstract List<Framedata> translateFrame(ByteBuffer byteBuffer) throws InvalidDataException;

    public static ByteBuffer readLine(ByteBuffer buf) {
        ByteBuffer sbuf = ByteBuffer.allocate(buf.remaining());
        byte cur = (byte) 48;
        while (buf.hasRemaining()) {
            byte prev = cur;
            cur = buf.get();
            sbuf.put(cur);
            if (prev == Draft_75.CR && cur == (byte) 10) {
                sbuf.limit(sbuf.position() - 2);
                sbuf.position(0);
                return sbuf;
            }
        }
        buf.position(buf.position() - sbuf.position());
        return null;
    }

    public static String readStringLine(ByteBuffer buf) {
        ByteBuffer b = readLine(buf);
        return b == null ? null : Charsetfunctions.stringAscii(b.array(), 0, b.limit());
    }

    public static HandshakeBuilder translateHandshakeHttp(ByteBuffer buf, Role role) throws InvalidHandshakeException, IncompleteHandshakeException {
        String line = readStringLine(buf);
        if (line == null) {
            throw new IncompleteHandshakeException(buf.capacity() + AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS);
        }
        String[] firstLineTokens = line.split(MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR, 3);
        if (firstLineTokens.length != 3) {
            throw new InvalidHandshakeException();
        }
        if (role == Role.CLIENT) {
            HandshakeBuilder handshake = new HandshakeImpl1Server();
            ServerHandshakeBuilder serverhandshake = (ServerHandshakeBuilder) handshake;
            serverhandshake.setHttpStatus(Short.parseShort(firstLineTokens[1]));
            serverhandshake.setHttpStatusMessage(firstLineTokens[2]);
        } else {
            ClientHandshakeBuilder clienthandshake = new HandshakeImpl1Client();
            clienthandshake.setResourceDescriptor(firstLineTokens[1]);
            Object handshake2 = clienthandshake;
        }
        line = readStringLine(buf);
        while (line != null && line.length() > 0) {
            String[] pair = line.split(":", 2);
            if (pair.length != 2) {
                throw new InvalidHandshakeException("not an http header");
            }
            handshake.put(pair[0], pair[1].replaceFirst("^ +", BuildConfig.FLAVOR));
            line = readStringLine(buf);
        }
        if (line != null) {
            return handshake;
        }
        throw new IncompleteHandshakeException();
    }

    protected boolean basicAccept(Handshakedata handshakedata) {
        return handshakedata.getFieldValue(HttpHeaders.UPGRADE).equalsIgnoreCase("websocket") && handshakedata.getFieldValue(HTTP.CONN_DIRECTIVE).toLowerCase(Locale.ENGLISH).contains("upgrade");
    }

    public List<Framedata> continuousFrame(Opcode op, ByteBuffer buffer, boolean fin) {
        if (op == Opcode.BINARY || op == Opcode.TEXT || op == Opcode.TEXT) {
            if (this.continuousFrameType != null) {
                this.continuousFrameType = Opcode.CONTINUOUS;
            } else {
                this.continuousFrameType = op;
            }
            FrameBuilder bui = new FramedataImpl1(this.continuousFrameType);
            try {
                bui.setPayload(buffer);
                bui.setFin(fin);
                if (fin) {
                    this.continuousFrameType = null;
                } else {
                    this.continuousFrameType = op;
                }
                return Collections.singletonList(bui);
            } catch (InvalidDataException e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("Only Opcode.BINARY or  Opcode.TEXT are allowed");
    }

    public List<ByteBuffer> createHandshake(Handshakedata handshakedata, Role ownrole) {
        return createHandshake(handshakedata, ownrole, true);
    }

    public List<ByteBuffer> createHandshake(Handshakedata handshakedata, Role ownrole, boolean withcontent) {
        StringBuilder bui = new StringBuilder(100);
        if (handshakedata instanceof ClientHandshake) {
            bui.append("GET ");
            bui.append(((ClientHandshake) handshakedata).getResourceDescriptor());
            bui.append(" HTTP/1.1");
        } else if (handshakedata instanceof ServerHandshake) {
            bui.append("HTTP/1.1 101 " + ((ServerHandshake) handshakedata).getHttpStatusMessage());
        } else {
            throw new RuntimeException("unknow role");
        }
        bui.append("\r\n");
        Iterator<String> it = handshakedata.iterateHttpFields();
        while (it.hasNext()) {
            String fieldname = (String) it.next();
            String fieldvalue = handshakedata.getFieldValue(fieldname);
            bui.append(fieldname);
            bui.append(": ");
            bui.append(fieldvalue);
            bui.append("\r\n");
        }
        bui.append("\r\n");
        byte[] httpheader = Charsetfunctions.asciiBytes(bui.toString());
        byte[] content = withcontent ? handshakedata.getContent() : null;
        ByteBuffer bytebuffer = ByteBuffer.allocate((content == null ? 0 : content.length) + httpheader.length);
        bytebuffer.put(httpheader);
        if (content != null) {
            bytebuffer.put(content);
        }
        bytebuffer.flip();
        return Collections.singletonList(bytebuffer);
    }

    public Handshakedata translateHandshake(ByteBuffer buf) throws InvalidHandshakeException {
        return translateHandshakeHttp(buf, this.role);
    }

    public int checkAlloc(int bytecount) throws LimitExedeedException, InvalidDataException {
        if (bytecount >= 0) {
            return bytecount;
        }
        throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "Negative count");
    }

    public void setParseMode(Role role) {
        this.role = role;
    }

    public Role getRole() {
        return this.role;
    }
}
