package com.mixpanel.android.java_websocket.server;

import com.mixpanel.android.java_websocket.WebSocketAdapter;
import com.mixpanel.android.java_websocket.WebSocketImpl;
import com.mixpanel.android.java_websocket.WebSocketListener;
import com.mixpanel.android.java_websocket.drafts.Draft;
import com.mixpanel.android.java_websocket.server.WebSocketServer.WebSocketServerFactory;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;

public class DefaultWebSocketServerFactory implements WebSocketServerFactory {
    public WebSocketImpl createWebSocket(WebSocketAdapter a, Draft d, Socket s) {
        return new WebSocketImpl((WebSocketListener) a, d);
    }

    public WebSocketImpl createWebSocket(WebSocketAdapter a, List<Draft> d, Socket s) {
        return new WebSocketImpl((WebSocketListener) a, (List) d);
    }

    public SocketChannel wrapChannel(SocketChannel channel, SelectionKey key) {
        return channel;
    }
}
