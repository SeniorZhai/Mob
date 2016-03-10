package com.mixpanel.android.java_websocket;

import com.mixpanel.android.java_websocket.WebSocket.Role;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

public class SocketChannelIOHelper {
    public static boolean read(ByteBuffer buf, WebSocketImpl ws, ByteChannel channel) throws IOException {
        buf.clear();
        int read = channel.read(buf);
        buf.flip();
        if (read == -1) {
            ws.eot();
            return false;
        } else if (read != 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean readMore(ByteBuffer buf, WebSocketImpl ws, WrappedByteChannel channel) throws IOException {
        buf.clear();
        int read = channel.readMore(buf);
        buf.flip();
        if (read != -1) {
            return channel.isNeedRead();
        }
        ws.eot();
        return false;
    }

    public static boolean batch(WebSocketImpl ws, ByteChannel sockchannel) throws IOException {
        ByteBuffer buffer = (ByteBuffer) ws.outQueue.peek();
        WrappedByteChannel c = null;
        if (buffer != null) {
            do {
                sockchannel.write(buffer);
                if (buffer.remaining() > 0) {
                    return false;
                }
                ws.outQueue.poll();
                buffer = (ByteBuffer) ws.outQueue.peek();
            } while (buffer != null);
        } else if (sockchannel instanceof WrappedByteChannel) {
            c = (WrappedByteChannel) sockchannel;
            if (c.isNeedWrite()) {
                c.writeMore();
            }
        }
        if (ws.outQueue.isEmpty() && ws.isFlushAndClose() && ws.getDraft().getRole() == Role.SERVER) {
            synchronized (ws) {
                ws.closeConnection();
            }
        }
        if (c == null || !((WrappedByteChannel) sockchannel).isNeedWrite()) {
            return true;
        }
        return false;
    }
}
