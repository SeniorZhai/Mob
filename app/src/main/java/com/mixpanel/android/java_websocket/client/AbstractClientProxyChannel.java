package com.mixpanel.android.java_websocket.client;

import com.mixpanel.android.java_websocket.AbstractWrappedByteChannel;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import org.apache.http.protocol.HTTP;

public abstract class AbstractClientProxyChannel extends AbstractWrappedByteChannel {
    protected final ByteBuffer proxyHandshake;

    public abstract String buildHandShake();

    public AbstractClientProxyChannel(ByteChannel towrap) {
        super(towrap);
        try {
            this.proxyHandshake = ByteBuffer.wrap(buildHandShake().getBytes(HTTP.ASCII));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public int write(ByteBuffer src) throws IOException {
        if (this.proxyHandshake.hasRemaining()) {
            return super.write(this.proxyHandshake);
        }
        return super.write(src);
    }
}
