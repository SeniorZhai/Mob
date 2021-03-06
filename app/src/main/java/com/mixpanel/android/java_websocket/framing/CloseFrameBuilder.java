package com.mixpanel.android.java_websocket.framing;

import com.mixpanel.android.java_websocket.exceptions.InvalidDataException;
import com.mixpanel.android.java_websocket.exceptions.InvalidFrameException;
import com.mixpanel.android.java_websocket.framing.Framedata.Opcode;
import com.mixpanel.android.java_websocket.util.Charsetfunctions;
import com.mobcrush.mobcrush.Constants;
import io.fabric.sdk.android.BuildConfig;
import java.nio.ByteBuffer;

public class CloseFrameBuilder extends FramedataImpl1 implements CloseFrame {
    static final ByteBuffer emptybytebuffer = ByteBuffer.allocate(0);
    private int code;
    private String reason;

    public CloseFrameBuilder() {
        super(Opcode.CLOSING);
        setFin(true);
    }

    public CloseFrameBuilder(int code) throws InvalidDataException {
        super(Opcode.CLOSING);
        setFin(true);
        setCodeAndMessage(code, BuildConfig.FLAVOR);
    }

    public CloseFrameBuilder(int code, String m) throws InvalidDataException {
        super(Opcode.CLOSING);
        setFin(true);
        setCodeAndMessage(code, m);
    }

    private void setCodeAndMessage(int code, String m) throws InvalidDataException {
        if (m == null) {
            m = BuildConfig.FLAVOR;
        }
        if (code == CloseFrame.TLS_ERROR) {
            code = CloseFrame.NOCODE;
            m = BuildConfig.FLAVOR;
        }
        if (code != CloseFrame.NOCODE) {
            byte[] by = Charsetfunctions.utf8Bytes(m);
            ByteBuffer buf = ByteBuffer.allocate(4);
            buf.putInt(code);
            buf.position(2);
            ByteBuffer pay = ByteBuffer.allocate(by.length + 2);
            pay.put(buf);
            pay.put(by);
            pay.rewind();
            setPayload(pay);
        } else if (m.length() > 0) {
            throw new InvalidDataException((int) CloseFrame.PROTOCOL_ERROR, "A close frame must have a closecode if it has a reason");
        }
    }

    private void initCloseCode() throws InvalidFrameException {
        this.code = CloseFrame.NOCODE;
        ByteBuffer payload = super.getPayloadData();
        payload.mark();
        if (payload.remaining() >= 2) {
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.position(2);
            bb.putShort(payload.getShort());
            bb.position(0);
            this.code = bb.getInt();
            if (this.code == CloseFrame.ABNORMAL_CLOSE || this.code == CloseFrame.TLS_ERROR || this.code == CloseFrame.NOCODE || this.code > 4999 || this.code < Constants.UPDATE_COFIG_INTERVAL || this.code == 1004) {
                throw new InvalidFrameException("closecode must not be sent over the wire: " + this.code);
            }
        }
        payload.reset();
    }

    public int getCloseCode() {
        return this.code;
    }

    private void initMessage() throws InvalidDataException {
        if (this.code == CloseFrame.NOCODE) {
            this.reason = Charsetfunctions.stringUtf8(super.getPayloadData());
            return;
        }
        ByteBuffer b = super.getPayloadData();
        int mark = b.position();
        try {
            b.position(b.position() + 2);
            this.reason = Charsetfunctions.stringUtf8(b);
            b.position(mark);
        } catch (Throwable e) {
            throw new InvalidFrameException(e);
        } catch (Throwable th) {
            b.position(mark);
        }
    }

    public String getMessage() {
        return this.reason;
    }

    public String toString() {
        return super.toString() + "code: " + this.code;
    }

    public void setPayload(ByteBuffer payload) throws InvalidDataException {
        super.setPayload(payload);
        initCloseCode();
        initMessage();
    }

    public ByteBuffer getPayloadData() {
        if (this.code == CloseFrame.NOCODE) {
            return emptybytebuffer;
        }
        return super.getPayloadData();
    }
}
