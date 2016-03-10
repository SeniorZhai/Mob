package com.mixpanel.android.java_websocket;

import com.mixpanel.android.java_websocket.drafts.Draft;
import com.mixpanel.android.java_websocket.exceptions.InvalidDataException;
import com.mixpanel.android.java_websocket.exceptions.InvalidHandshakeException;
import com.mixpanel.android.java_websocket.framing.Framedata;
import com.mixpanel.android.java_websocket.framing.Framedata.Opcode;
import com.mixpanel.android.java_websocket.framing.FramedataImpl1;
import com.mixpanel.android.java_websocket.handshake.ClientHandshake;
import com.mixpanel.android.java_websocket.handshake.HandshakeImpl1Server;
import com.mixpanel.android.java_websocket.handshake.ServerHandshake;
import com.mixpanel.android.java_websocket.handshake.ServerHandshakeBuilder;
import java.net.InetSocketAddress;

public abstract class WebSocketAdapter implements WebSocketListener {
    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        return new HandshakeImpl1Server();
    }

    public void onWebsocketHandshakeReceivedAsClient(WebSocket conn, ClientHandshake request, ServerHandshake response) throws InvalidDataException {
    }

    public void onWebsocketHandshakeSentAsClient(WebSocket conn, ClientHandshake request) throws InvalidDataException {
    }

    public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
    }

    public void onWebsocketPing(WebSocket conn, Framedata f) {
        FramedataImpl1 resp = new FramedataImpl1(f);
        resp.setOptcode(Opcode.PONG);
        conn.sendFrame(resp);
    }

    public void onWebsocketPong(WebSocket conn, Framedata f) {
    }

    public String getFlashPolicy(WebSocket conn) throws InvalidDataException {
        InetSocketAddress adr = conn.getLocalSocketAddress();
        if (adr == null) {
            throw new InvalidHandshakeException("socket not bound");
        }
        StringBuffer sb = new StringBuffer(90);
        sb.append("<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"");
        sb.append(adr.getPort());
        sb.append("\" /></cross-domain-policy>\u0000");
        return sb.toString();
    }
}
