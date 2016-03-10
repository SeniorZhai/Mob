package com.mixpanel.android.java_websocket.drafts;

import com.mixpanel.android.java_websocket.drafts.Draft.CloseHandshakeType;
import com.mixpanel.android.java_websocket.drafts.Draft.HandshakeState;
import com.mixpanel.android.java_websocket.exceptions.InvalidDataException;
import com.mixpanel.android.java_websocket.exceptions.InvalidFrameException;
import com.mixpanel.android.java_websocket.exceptions.InvalidHandshakeException;
import com.mixpanel.android.java_websocket.exceptions.LimitExedeedException;
import com.mixpanel.android.java_websocket.exceptions.NotSendableException;
import com.mixpanel.android.java_websocket.framing.CloseFrame;
import com.mixpanel.android.java_websocket.framing.FrameBuilder;
import com.mixpanel.android.java_websocket.framing.Framedata;
import com.mixpanel.android.java_websocket.framing.Framedata.Opcode;
import com.mixpanel.android.java_websocket.framing.FramedataImpl1;
import com.mixpanel.android.java_websocket.handshake.ClientHandshake;
import com.mixpanel.android.java_websocket.handshake.ClientHandshakeBuilder;
import com.mixpanel.android.java_websocket.handshake.HandshakeBuilder;
import com.mixpanel.android.java_websocket.handshake.ServerHandshake;
import com.mixpanel.android.java_websocket.handshake.ServerHandshakeBuilder;
import com.mixpanel.android.java_websocket.util.Charsetfunctions;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import org.apache.http.HttpHeaders;
import org.apache.http.protocol.HTTP;

public class Draft_75 extends Draft {
    public static final byte CR = (byte) 13;
    public static final byte END_OF_FRAME = (byte) -1;
    public static final byte LF = (byte) 10;
    public static final byte START_OF_FRAME = (byte) 0;
    protected ByteBuffer currentFrame;
    protected boolean readingState = false;
    protected List<Framedata> readyframes = new LinkedList();
    private final Random reuseableRandom = new Random();

    public HandshakeState acceptHandshakeAsClient(ClientHandshake request, ServerHandshake response) {
        return (request.getFieldValue("WebSocket-Origin").equals(response.getFieldValue("Origin")) && basicAccept(response)) ? HandshakeState.MATCHED : HandshakeState.NOT_MATCHED;
    }

    public HandshakeState acceptHandshakeAsServer(ClientHandshake handshakedata) {
        if (handshakedata.hasFieldValue("Origin") && basicAccept(handshakedata)) {
            return HandshakeState.MATCHED;
        }
        return HandshakeState.NOT_MATCHED;
    }

    public ByteBuffer createBinaryFrame(Framedata framedata) {
        if (framedata.getOpcode() != Opcode.TEXT) {
            throw new RuntimeException("only text frames supported");
        }
        ByteBuffer pay = framedata.getPayloadData();
        ByteBuffer b = ByteBuffer.allocate(pay.remaining() + 2);
        b.put((byte) 0);
        pay.mark();
        b.put(pay);
        pay.reset();
        b.put(END_OF_FRAME);
        b.flip();
        return b;
    }

    public List<Framedata> createFrames(ByteBuffer binary, boolean mask) {
        throw new RuntimeException("not yet implemented");
    }

    public List<Framedata> createFrames(String text, boolean mask) {
        FrameBuilder frame = new FramedataImpl1();
        try {
            frame.setPayload(ByteBuffer.wrap(Charsetfunctions.utf8Bytes(text)));
            frame.setFin(true);
            frame.setOptcode(Opcode.TEXT);
            frame.setTransferemasked(mask);
            return Collections.singletonList(frame);
        } catch (Throwable e) {
            throw new NotSendableException(e);
        }
    }

    public ClientHandshakeBuilder postProcessHandshakeRequestAsClient(ClientHandshakeBuilder request) throws InvalidHandshakeException {
        request.put(HttpHeaders.UPGRADE, "WebSocket");
        request.put(HTTP.CONN_DIRECTIVE, HttpHeaders.UPGRADE);
        if (!request.hasFieldValue("Origin")) {
            request.put("Origin", "random" + this.reuseableRandom.nextInt());
        }
        return request;
    }

    public HandshakeBuilder postProcessHandshakeResponseAsServer(ClientHandshake request, ServerHandshakeBuilder response) throws InvalidHandshakeException {
        response.setHttpStatusMessage("Web Socket Protocol Handshake");
        response.put(HttpHeaders.UPGRADE, "WebSocket");
        response.put(HTTP.CONN_DIRECTIVE, request.getFieldValue(HTTP.CONN_DIRECTIVE));
        response.put("WebSocket-Origin", request.getFieldValue("Origin"));
        response.put("WebSocket-Location", "ws://" + request.getFieldValue(HTTP.TARGET_HOST) + request.getResourceDescriptor());
        return response;
    }

    protected List<Framedata> translateRegularFrame(ByteBuffer buffer) throws InvalidDataException {
        while (buffer.hasRemaining()) {
            byte newestByte = buffer.get();
            if (newestByte == (byte) 0) {
                if (this.readingState) {
                    throw new InvalidFrameException("unexpected START_OF_FRAME");
                }
                this.readingState = true;
            } else if (newestByte == END_OF_FRAME) {
                if (this.readingState) {
                    if (this.currentFrame != null) {
                        this.currentFrame.flip();
                        FramedataImpl1 curframe = new FramedataImpl1();
                        curframe.setPayload(this.currentFrame);
                        curframe.setFin(true);
                        curframe.setOptcode(Opcode.TEXT);
                        this.readyframes.add(curframe);
                        this.currentFrame = null;
                        buffer.mark();
                    }
                    this.readingState = false;
                } else {
                    throw new InvalidFrameException("unexpected END_OF_FRAME");
                }
            } else if (!this.readingState) {
                return null;
            } else {
                if (this.currentFrame == null) {
                    this.currentFrame = createBuffer();
                } else if (!this.currentFrame.hasRemaining()) {
                    this.currentFrame = increaseBuffer(this.currentFrame);
                }
                this.currentFrame.put(newestByte);
            }
        }
        List<Framedata> frames = this.readyframes;
        this.readyframes = new LinkedList();
        return frames;
    }

    public List<Framedata> translateFrame(ByteBuffer buffer) throws InvalidDataException {
        List<Framedata> frames = translateRegularFrame(buffer);
        if (frames != null) {
            return frames;
        }
        throw new InvalidDataException(CloseFrame.PROTOCOL_ERROR);
    }

    public void reset() {
        this.readingState = false;
        this.currentFrame = null;
    }

    public CloseHandshakeType getCloseHandshakeType() {
        return CloseHandshakeType.NONE;
    }

    public ByteBuffer createBuffer() {
        return ByteBuffer.allocate(INITIAL_FAMESIZE);
    }

    public ByteBuffer increaseBuffer(ByteBuffer full) throws LimitExedeedException, InvalidDataException {
        full.flip();
        ByteBuffer newbuffer = ByteBuffer.allocate(checkAlloc(full.capacity() * 2));
        newbuffer.put(full);
        return newbuffer;
    }

    public Draft copyInstance() {
        return new Draft_75();
    }
}
