package com.mixpanel.android.java_websocket.handshake;

public interface ClientHandshakeBuilder extends HandshakeBuilder, ClientHandshake {
    void setResourceDescriptor(String str);
}
