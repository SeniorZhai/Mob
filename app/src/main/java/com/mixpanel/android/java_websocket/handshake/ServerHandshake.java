package com.mixpanel.android.java_websocket.handshake;

public interface ServerHandshake extends Handshakedata {
    short getHttpStatus();

    String getHttpStatusMessage();
}
