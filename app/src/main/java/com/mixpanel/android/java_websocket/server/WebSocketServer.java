package com.mixpanel.android.java_websocket.server;

import android.annotation.SuppressLint;
import com.mixpanel.android.java_websocket.WebSocket;
import com.mixpanel.android.java_websocket.WebSocketAdapter;
import com.mixpanel.android.java_websocket.WebSocketFactory;
import com.mixpanel.android.java_websocket.WebSocketImpl;
import com.mixpanel.android.java_websocket.drafts.Draft;
import com.mixpanel.android.java_websocket.exceptions.InvalidDataException;
import com.mixpanel.android.java_websocket.framing.CloseFrame;
import com.mixpanel.android.java_websocket.framing.Framedata;
import com.mixpanel.android.java_websocket.handshake.ClientHandshake;
import com.mixpanel.android.java_websocket.handshake.Handshakedata;
import com.mixpanel.android.java_websocket.handshake.ServerHandshakeBuilder;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressLint({"Assert"})
public abstract class WebSocketServer extends WebSocketAdapter implements Runnable {
    static final /* synthetic */ boolean $assertionsDisabled = (!WebSocketServer.class.desiredAssertionStatus());
    public static int DECODERS = Runtime.getRuntime().availableProcessors();
    private final InetSocketAddress address;
    private BlockingQueue<ByteBuffer> buffers;
    private final Collection<WebSocket> connections;
    private List<WebSocketWorker> decoders;
    private List<Draft> drafts;
    private List<WebSocketImpl> iqueue;
    private volatile AtomicBoolean isclosed;
    private int queueinvokes;
    private AtomicInteger queuesize;
    private Selector selector;
    private Thread selectorthread;
    private ServerSocketChannel server;
    private WebSocketServerFactory wsf;

    public interface WebSocketServerFactory extends WebSocketFactory {
        WebSocketImpl createWebSocket(WebSocketAdapter webSocketAdapter, Draft draft, Socket socket);

        WebSocketImpl createWebSocket(WebSocketAdapter webSocketAdapter, List<Draft> list, Socket socket);

        ByteChannel wrapChannel(SocketChannel socketChannel, SelectionKey selectionKey) throws IOException;
    }

    public class WebSocketWorker extends Thread {
        static final /* synthetic */ boolean $assertionsDisabled = (!WebSocketServer.class.desiredAssertionStatus());
        private BlockingQueue<WebSocketImpl> iqueue = new LinkedBlockingQueue();

        public WebSocketWorker() {
            setName("WebSocketWorker-" + getId());
            setUncaughtExceptionHandler(new UncaughtExceptionHandler(WebSocketServer.this) {
                public void uncaughtException(Thread t, Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(t, e);
                }
            });
        }

        public void put(WebSocketImpl ws) throws InterruptedException {
            this.iqueue.put(ws);
        }

        public void run() {
            WebSocketImpl ws = null;
            while (true) {
                ByteBuffer buf;
                try {
                    ws = (WebSocketImpl) this.iqueue.take();
                    buf = (ByteBuffer) ws.inQueue.poll();
                    if ($assertionsDisabled || buf != null) {
                        ws.decode(buf);
                        WebSocketServer.this.pushBuffer(buf);
                    } else {
                        throw new AssertionError();
                    }
                } catch (InterruptedException e) {
                    return;
                } catch (RuntimeException e2) {
                    WebSocketServer.this.handleFatal(ws, e2);
                    return;
                } catch (Throwable th) {
                    WebSocketServer.this.pushBuffer(buf);
                }
            }
        }
    }

    public abstract void onClose(WebSocket webSocket, int i, String str, boolean z);

    public abstract void onError(WebSocket webSocket, Exception exception);

    public abstract void onMessage(WebSocket webSocket, String str);

    public abstract void onOpen(WebSocket webSocket, ClientHandshake clientHandshake);

    public WebSocketServer() throws UnknownHostException {
        this(new InetSocketAddress(80), DECODERS, null);
    }

    public WebSocketServer(InetSocketAddress address) {
        this(address, DECODERS, null);
    }

    public WebSocketServer(InetSocketAddress address, int decoders) {
        this(address, decoders, null);
    }

    public WebSocketServer(InetSocketAddress address, List<Draft> drafts) {
        this(address, DECODERS, drafts);
    }

    public WebSocketServer(InetSocketAddress address, int decodercount, List<Draft> drafts) {
        this(address, decodercount, drafts, new HashSet());
    }

    public WebSocketServer(InetSocketAddress address, int decodercount, List<Draft> drafts, Collection<WebSocket> connectionscontainer) {
        this.isclosed = new AtomicBoolean(false);
        this.queueinvokes = 0;
        this.queuesize = new AtomicInteger(0);
        this.wsf = new DefaultWebSocketServerFactory();
        if (address == null || decodercount < 1 || connectionscontainer == null) {
            throw new IllegalArgumentException("address and connectionscontainer must not be null and you need at least 1 decoder");
        }
        if (drafts == null) {
            this.drafts = Collections.emptyList();
        } else {
            this.drafts = drafts;
        }
        this.address = address;
        this.connections = connectionscontainer;
        this.iqueue = new LinkedList();
        this.decoders = new ArrayList(decodercount);
        this.buffers = new LinkedBlockingQueue();
        for (int i = 0; i < decodercount; i++) {
            WebSocketWorker ex = new WebSocketWorker();
            this.decoders.add(ex);
            ex.start();
        }
    }

    public void start() {
        if (this.selectorthread != null) {
            throw new IllegalStateException(getClass().getName() + " can only be started once.");
        }
        new Thread(this).start();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void stop(int r7) throws java.lang.InterruptedException {
        /*
        r6 = this;
        r3 = r6.isclosed;
        r4 = 0;
        r5 = 1;
        r3 = r3.compareAndSet(r4, r5);
        if (r3 != 0) goto L_0x000b;
    L_0x000a:
        return;
    L_0x000b:
        r0 = 0;
        r4 = r6.connections;
        monitor-enter(r4);
        r1 = new java.util.ArrayList;	 Catch:{ all -> 0x002d }
        r3 = r6.connections;	 Catch:{ all -> 0x002d }
        r1.<init>(r3);	 Catch:{ all -> 0x002d }
        monitor-exit(r4);	 Catch:{ all -> 0x0060 }
        r3 = r1.iterator();
    L_0x001b:
        r4 = r3.hasNext();
        if (r4 == 0) goto L_0x0030;
    L_0x0021:
        r2 = r3.next();
        r2 = (com.mixpanel.android.java_websocket.WebSocket) r2;
        r4 = 1001; // 0x3e9 float:1.403E-42 double:4.946E-321;
        r2.close(r4);
        goto L_0x001b;
    L_0x002d:
        r3 = move-exception;
    L_0x002e:
        monitor-exit(r4);	 Catch:{ all -> 0x002d }
        throw r3;
    L_0x0030:
        monitor-enter(r6);
        r3 = r6.selectorthread;	 Catch:{ all -> 0x005d }
        if (r3 == 0) goto L_0x005b;
    L_0x0035:
        r3 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x005d }
        r4 = r6.selectorthread;	 Catch:{ all -> 0x005d }
        if (r3 == r4) goto L_0x003d;
    L_0x003d:
        r3 = r6.selectorthread;	 Catch:{ all -> 0x005d }
        r4 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x005d }
        if (r3 == r4) goto L_0x005b;
    L_0x0045:
        r3 = r1.size();	 Catch:{ all -> 0x005d }
        if (r3 <= 0) goto L_0x0051;
    L_0x004b:
        r3 = r6.selectorthread;	 Catch:{ all -> 0x005d }
        r4 = (long) r7;	 Catch:{ all -> 0x005d }
        r3.join(r4);	 Catch:{ all -> 0x005d }
    L_0x0051:
        r3 = r6.selectorthread;	 Catch:{ all -> 0x005d }
        r3.interrupt();	 Catch:{ all -> 0x005d }
        r3 = r6.selectorthread;	 Catch:{ all -> 0x005d }
        r3.join();	 Catch:{ all -> 0x005d }
    L_0x005b:
        monitor-exit(r6);	 Catch:{ all -> 0x005d }
        goto L_0x000a;
    L_0x005d:
        r3 = move-exception;
        monitor-exit(r6);	 Catch:{ all -> 0x005d }
        throw r3;
    L_0x0060:
        r3 = move-exception;
        r0 = r1;
        goto L_0x002e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mixpanel.android.java_websocket.server.WebSocketServer.stop(int):void");
    }

    public void stop() throws IOException, InterruptedException {
        stop(0);
    }

    public Collection<WebSocket> connections() {
        return this.connections;
    }

    public InetSocketAddress getAddress() {
        return this.address;
    }

    public int getPort() {
        int port = getAddress().getPort();
        if (port != 0 || this.server == null) {
            return port;
        }
        return this.server.socket().getLocalPort();
    }

    public List<Draft> getDraft() {
        return Collections.unmodifiableList(this.drafts);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void run() {
        /*
        r18 = this;
        monitor-enter(r18);
        r0 = r18;
        r13 = r0.selectorthread;	 Catch:{ all -> 0x0028 }
        if (r13 == 0) goto L_0x002b;
    L_0x0007:
        r13 = new java.lang.IllegalStateException;	 Catch:{ all -> 0x0028 }
        r14 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0028 }
        r14.<init>();	 Catch:{ all -> 0x0028 }
        r15 = r18.getClass();	 Catch:{ all -> 0x0028 }
        r15 = r15.getName();	 Catch:{ all -> 0x0028 }
        r14 = r14.append(r15);	 Catch:{ all -> 0x0028 }
        r15 = " can only be started once.";
        r14 = r14.append(r15);	 Catch:{ all -> 0x0028 }
        r14 = r14.toString();	 Catch:{ all -> 0x0028 }
        r13.<init>(r14);	 Catch:{ all -> 0x0028 }
        throw r13;	 Catch:{ all -> 0x0028 }
    L_0x0028:
        r13 = move-exception;
        monitor-exit(r18);	 Catch:{ all -> 0x0028 }
        throw r13;
    L_0x002b:
        r13 = java.lang.Thread.currentThread();	 Catch:{ all -> 0x0028 }
        r0 = r18;
        r0.selectorthread = r13;	 Catch:{ all -> 0x0028 }
        r0 = r18;
        r13 = r0.isclosed;	 Catch:{ all -> 0x0028 }
        r13 = r13.get();	 Catch:{ all -> 0x0028 }
        if (r13 == 0) goto L_0x003f;
    L_0x003d:
        monitor-exit(r18);	 Catch:{ all -> 0x0028 }
    L_0x003e:
        return;
    L_0x003f:
        monitor-exit(r18);	 Catch:{ all -> 0x0028 }
        r0 = r18;
        r13 = r0.selectorthread;
        r14 = new java.lang.StringBuilder;
        r14.<init>();
        r15 = "WebsocketSelector";
        r14 = r14.append(r15);
        r0 = r18;
        r15 = r0.selectorthread;
        r16 = r15.getId();
        r0 = r16;
        r14 = r14.append(r0);
        r14 = r14.toString();
        r13.setName(r14);
        r13 = java.nio.channels.ServerSocketChannel.open();	 Catch:{ IOException -> 0x00ea }
        r0 = r18;
        r0.server = r13;	 Catch:{ IOException -> 0x00ea }
        r0 = r18;
        r13 = r0.server;	 Catch:{ IOException -> 0x00ea }
        r14 = 0;
        r13.configureBlocking(r14);	 Catch:{ IOException -> 0x00ea }
        r0 = r18;
        r13 = r0.server;	 Catch:{ IOException -> 0x00ea }
        r11 = r13.socket();	 Catch:{ IOException -> 0x00ea }
        r13 = com.mixpanel.android.java_websocket.WebSocketImpl.RCVBUF;	 Catch:{ IOException -> 0x00ea }
        r11.setReceiveBufferSize(r13);	 Catch:{ IOException -> 0x00ea }
        r0 = r18;
        r13 = r0.address;	 Catch:{ IOException -> 0x00ea }
        r11.bind(r13);	 Catch:{ IOException -> 0x00ea }
        r13 = java.nio.channels.Selector.open();	 Catch:{ IOException -> 0x00ea }
        r0 = r18;
        r0.selector = r13;	 Catch:{ IOException -> 0x00ea }
        r0 = r18;
        r13 = r0.server;	 Catch:{ IOException -> 0x00ea }
        r0 = r18;
        r14 = r0.selector;	 Catch:{ IOException -> 0x00ea }
        r0 = r18;
        r15 = r0.server;	 Catch:{ IOException -> 0x00ea }
        r15 = r15.validOps();	 Catch:{ IOException -> 0x00ea }
        r13.register(r14, r15);	 Catch:{ IOException -> 0x00ea }
    L_0x00a3:
        r0 = r18;
        r13 = r0.selectorthread;	 Catch:{ RuntimeException -> 0x01c3 }
        r13 = r13.isInterrupted();	 Catch:{ RuntimeException -> 0x01c3 }
        if (r13 != 0) goto L_0x02b4;
    L_0x00ad:
        r9 = 0;
        r5 = 0;
        r0 = r18;
        r13 = r0.selector;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r13.select();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r18;
        r13 = r0.selector;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r10 = r13.selectedKeys();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r8 = r10.iterator();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
    L_0x00c2:
        r13 = r8.hasNext();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x023a;
    L_0x00c8:
        r13 = r8.next();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r13;
        r0 = (java.nio.channels.SelectionKey) r0;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r9 = r0;
        r13 = r9.isValid();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x00c2;
    L_0x00d6:
        r13 = r9.isAcceptable();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x0150;
    L_0x00dc:
        r0 = r18;
        r13 = r0.onConnect(r9);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        if (r13 != 0) goto L_0x00f3;
    L_0x00e4:
        r9.cancel();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        goto L_0x00c2;
    L_0x00e8:
        r13 = move-exception;
        goto L_0x00a3;
    L_0x00ea:
        r7 = move-exception;
        r13 = 0;
        r0 = r18;
        r0.handleFatal(r13, r7);
        goto L_0x003e;
    L_0x00f3:
        r0 = r18;
        r13 = r0.server;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r4 = r13.accept();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r13 = 0;
        r4.configureBlocking(r13);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r18;
        r13 = r0.wsf;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r18;
        r14 = r0.drafts;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r15 = r4.socket();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r18;
        r12 = r13.createWebSocket(r0, r14, r15);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r18;
        r13 = r0.selector;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r14 = 1;
        r13 = r4.register(r13, r14, r12);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r12.key = r13;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r18;
        r13 = r0.wsf;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r14 = r12.key;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r13 = r13.wrapChannel(r4, r14);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r12.channel = r13;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r8.remove();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r18;
        r0.allocateBuffers(r12);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        goto L_0x00c2;
    L_0x0131:
        r6 = move-exception;
        r0 = r18;
        r13 = r0.decoders;
        if (r13 == 0) goto L_0x0284;
    L_0x0138:
        r0 = r18;
        r13 = r0.decoders;
        r13 = r13.iterator();
    L_0x0140:
        r14 = r13.hasNext();
        if (r14 == 0) goto L_0x0284;
    L_0x0146:
        r12 = r13.next();
        r12 = (com.mixpanel.android.java_websocket.server.WebSocketServer.WebSocketWorker) r12;
        r12.interrupt();
        goto L_0x0140;
    L_0x0150:
        r13 = r9.isReadable();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x0194;
    L_0x0156:
        r13 = r9.attachment();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r13;
        r0 = (com.mixpanel.android.java_websocket.WebSocketImpl) r0;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r5 = r0;
        r2 = r18.takeBuffer();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r13 = r5.channel;	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r13 = com.mixpanel.android.java_websocket.SocketChannelIOHelper.read(r2, r5, r13);	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x0214;
    L_0x016a:
        r13 = r2.hasRemaining();	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x01e8;
    L_0x0170:
        r13 = r5.inQueue;	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r13.put(r2);	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r0 = r18;
        r0.queue(r5);	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r8.remove();	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r13 = r5.channel;	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r13 = r13 instanceof com.mixpanel.android.java_websocket.WrappedByteChannel;	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x0194;
    L_0x0183:
        r13 = r5.channel;	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r13 = (com.mixpanel.android.java_websocket.WrappedByteChannel) r13;	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r13 = r13.isNeedRead();	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x0194;
    L_0x018d:
        r0 = r18;
        r13 = r0.iqueue;	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r13.add(r5);	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
    L_0x0194:
        r13 = r9.isWritable();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x00c2;
    L_0x019a:
        r13 = r9.attachment();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r13;
        r0 = (com.mixpanel.android.java_websocket.WebSocketImpl) r0;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r5 = r0;
        r13 = r5.channel;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r13 = com.mixpanel.android.java_websocket.SocketChannelIOHelper.batch(r5, r13);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x00c2;
    L_0x01aa:
        r13 = r9.isValid();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x00c2;
    L_0x01b0:
        r13 = 1;
        r9.interestOps(r13);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        goto L_0x00c2;
    L_0x01b6:
        r7 = move-exception;
        if (r9 == 0) goto L_0x01bc;
    L_0x01b9:
        r9.cancel();	 Catch:{ RuntimeException -> 0x01c3 }
    L_0x01bc:
        r0 = r18;
        r0.handleIOException(r9, r5, r7);	 Catch:{ RuntimeException -> 0x01c3 }
        goto L_0x00a3;
    L_0x01c3:
        r6 = move-exception;
        r13 = 0;
        r0 = r18;
        r0.handleFatal(r13, r6);	 Catch:{ all -> 0x021b }
        r0 = r18;
        r13 = r0.decoders;
        if (r13 == 0) goto L_0x02ea;
    L_0x01d0:
        r0 = r18;
        r13 = r0.decoders;
        r13 = r13.iterator();
    L_0x01d8:
        r14 = r13.hasNext();
        if (r14 == 0) goto L_0x02ea;
    L_0x01de:
        r12 = r13.next();
        r12 = (com.mixpanel.android.java_websocket.server.WebSocketServer.WebSocketWorker) r12;
        r12.interrupt();
        goto L_0x01d8;
    L_0x01e8:
        r0 = r18;
        r0.pushBuffer(r2);	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        goto L_0x0194;
    L_0x01ee:
        r6 = move-exception;
        r0 = r18;
        r0.pushBuffer(r2);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        throw r6;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
    L_0x01f5:
        r6 = move-exception;
        r0 = r18;
        r13 = r0.decoders;
        if (r13 == 0) goto L_0x029c;
    L_0x01fc:
        r0 = r18;
        r13 = r0.decoders;
        r13 = r13.iterator();
    L_0x0204:
        r14 = r13.hasNext();
        if (r14 == 0) goto L_0x029c;
    L_0x020a:
        r12 = r13.next();
        r12 = (com.mixpanel.android.java_websocket.server.WebSocketServer.WebSocketWorker) r12;
        r12.interrupt();
        goto L_0x0204;
    L_0x0214:
        r0 = r18;
        r0.pushBuffer(r2);	 Catch:{ IOException -> 0x01ee, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        goto L_0x0194;
    L_0x021b:
        r13 = move-exception;
        r0 = r18;
        r14 = r0.decoders;
        if (r14 == 0) goto L_0x0302;
    L_0x0222:
        r0 = r18;
        r14 = r0.decoders;
        r14 = r14.iterator();
    L_0x022a:
        r15 = r14.hasNext();
        if (r15 == 0) goto L_0x0302;
    L_0x0230:
        r12 = r14.next();
        r12 = (com.mixpanel.android.java_websocket.server.WebSocketServer.WebSocketWorker) r12;
        r12.interrupt();
        goto L_0x022a;
    L_0x023a:
        r0 = r18;
        r13 = r0.iqueue;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r13 = r13.isEmpty();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        if (r13 != 0) goto L_0x00a3;
    L_0x0244:
        r0 = r18;
        r13 = r0.iqueue;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r14 = 0;
        r13 = r13.remove(r14);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r0 = r13;
        r0 = (com.mixpanel.android.java_websocket.WebSocketImpl) r0;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r5 = r0;
        r3 = r5.channel;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r3 = (com.mixpanel.android.java_websocket.WrappedByteChannel) r3;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r2 = r18.takeBuffer();	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        r13 = com.mixpanel.android.java_websocket.SocketChannelIOHelper.readMore(r2, r5, r3);	 Catch:{ IOException -> 0x0277, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x0266;
    L_0x025f:
        r0 = r18;
        r13 = r0.iqueue;	 Catch:{ IOException -> 0x0277, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r13.add(r5);	 Catch:{ IOException -> 0x0277, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
    L_0x0266:
        r13 = r2.hasRemaining();	 Catch:{ IOException -> 0x0277, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        if (r13 == 0) goto L_0x027e;
    L_0x026c:
        r13 = r5.inQueue;	 Catch:{ IOException -> 0x0277, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r13.put(r2);	 Catch:{ IOException -> 0x0277, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        r0 = r18;
        r0.queue(r5);	 Catch:{ IOException -> 0x0277, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        goto L_0x023a;
    L_0x0277:
        r6 = move-exception;
        r0 = r18;
        r0.pushBuffer(r2);	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
        throw r6;	 Catch:{ CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, IOException -> 0x01b6, InterruptedException -> 0x01f5 }
    L_0x027e:
        r0 = r18;
        r0.pushBuffer(r2);	 Catch:{ IOException -> 0x0277, CancelledKeyException -> 0x00e8, ClosedByInterruptException -> 0x0131, InterruptedException -> 0x01f5 }
        goto L_0x023a;
    L_0x0284:
        r0 = r18;
        r13 = r0.server;
        if (r13 == 0) goto L_0x003e;
    L_0x028a:
        r0 = r18;
        r13 = r0.server;	 Catch:{ IOException -> 0x0293 }
        r13.close();	 Catch:{ IOException -> 0x0293 }
        goto L_0x003e;
    L_0x0293:
        r6 = move-exception;
        r13 = 0;
        r0 = r18;
        r0.onError(r13, r6);
        goto L_0x003e;
    L_0x029c:
        r0 = r18;
        r13 = r0.server;
        if (r13 == 0) goto L_0x003e;
    L_0x02a2:
        r0 = r18;
        r13 = r0.server;	 Catch:{ IOException -> 0x02ab }
        r13.close();	 Catch:{ IOException -> 0x02ab }
        goto L_0x003e;
    L_0x02ab:
        r6 = move-exception;
        r13 = 0;
        r0 = r18;
        r0.onError(r13, r6);
        goto L_0x003e;
    L_0x02b4:
        r0 = r18;
        r13 = r0.decoders;
        if (r13 == 0) goto L_0x02d2;
    L_0x02ba:
        r0 = r18;
        r13 = r0.decoders;
        r13 = r13.iterator();
    L_0x02c2:
        r14 = r13.hasNext();
        if (r14 == 0) goto L_0x02d2;
    L_0x02c8:
        r12 = r13.next();
        r12 = (com.mixpanel.android.java_websocket.server.WebSocketServer.WebSocketWorker) r12;
        r12.interrupt();
        goto L_0x02c2;
    L_0x02d2:
        r0 = r18;
        r13 = r0.server;
        if (r13 == 0) goto L_0x003e;
    L_0x02d8:
        r0 = r18;
        r13 = r0.server;	 Catch:{ IOException -> 0x02e1 }
        r13.close();	 Catch:{ IOException -> 0x02e1 }
        goto L_0x003e;
    L_0x02e1:
        r6 = move-exception;
        r13 = 0;
        r0 = r18;
        r0.onError(r13, r6);
        goto L_0x003e;
    L_0x02ea:
        r0 = r18;
        r13 = r0.server;
        if (r13 == 0) goto L_0x003e;
    L_0x02f0:
        r0 = r18;
        r13 = r0.server;	 Catch:{ IOException -> 0x02f9 }
        r13.close();	 Catch:{ IOException -> 0x02f9 }
        goto L_0x003e;
    L_0x02f9:
        r6 = move-exception;
        r13 = 0;
        r0 = r18;
        r0.onError(r13, r6);
        goto L_0x003e;
    L_0x0302:
        r0 = r18;
        r14 = r0.server;
        if (r14 == 0) goto L_0x030f;
    L_0x0308:
        r0 = r18;
        r14 = r0.server;	 Catch:{ IOException -> 0x0310 }
        r14.close();	 Catch:{ IOException -> 0x0310 }
    L_0x030f:
        throw r13;
    L_0x0310:
        r6 = move-exception;
        r14 = 0;
        r0 = r18;
        r0.onError(r14, r6);
        goto L_0x030f;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.mixpanel.android.java_websocket.server.WebSocketServer.run():void");
    }

    protected void allocateBuffers(WebSocket c) throws InterruptedException {
        if (this.queuesize.get() < (this.decoders.size() * 2) + 1) {
            this.queuesize.incrementAndGet();
            this.buffers.put(createBuffer());
        }
    }

    protected void releaseBuffers(WebSocket c) throws InterruptedException {
    }

    public ByteBuffer createBuffer() {
        return ByteBuffer.allocate(WebSocketImpl.RCVBUF);
    }

    private void queue(WebSocketImpl ws) throws InterruptedException {
        if (ws.workerThread == null) {
            ws.workerThread = (WebSocketWorker) this.decoders.get(this.queueinvokes % this.decoders.size());
            this.queueinvokes++;
        }
        ws.workerThread.put(ws);
    }

    private ByteBuffer takeBuffer() throws InterruptedException {
        return (ByteBuffer) this.buffers.take();
    }

    private void pushBuffer(ByteBuffer buf) throws InterruptedException {
        if (this.buffers.size() <= this.queuesize.intValue()) {
            this.buffers.put(buf);
        }
    }

    private void handleIOException(SelectionKey key, WebSocket conn, IOException ex) {
        if (conn != null) {
            conn.closeConnection(CloseFrame.ABNORMAL_CLOSE, ex.getMessage());
        } else if (key != null) {
            SelectableChannel channel = key.channel();
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                }
                if (WebSocketImpl.DEBUG) {
                    System.out.println("Connection closed because of" + ex);
                }
            }
        }
    }

    private void handleFatal(WebSocket conn, Exception e) {
        onError(conn, e);
        try {
            stop();
        } catch (IOException e1) {
            onError(null, e1);
        } catch (InterruptedException e12) {
            Thread.currentThread().interrupt();
            onError(null, e12);
        }
    }

    protected String getFlashSecurityPolicy() {
        return "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"" + getPort() + "\" /></cross-domain-policy>";
    }

    public final void onWebsocketMessage(WebSocket conn, String message) {
        onMessage(conn, message);
    }

    @Deprecated
    public void onWebsocketMessageFragment(WebSocket conn, Framedata frame) {
        onFragment(conn, frame);
    }

    public final void onWebsocketMessage(WebSocket conn, ByteBuffer blob) {
        onMessage(conn, blob);
    }

    public final void onWebsocketOpen(WebSocket conn, Handshakedata handshake) {
        if (addConnection(conn)) {
            onOpen(conn, (ClientHandshake) handshake);
        }
    }

    public final void onWebsocketClose(WebSocket conn, int code, String reason, boolean remote) {
        this.selector.wakeup();
        try {
            if (removeConnection(conn)) {
                onClose(conn, code, reason, remote);
            }
        } finally {
            try {
                releaseBuffers(conn);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    protected boolean removeConnection(WebSocket ws) {
        boolean removed;
        synchronized (this.connections) {
            removed = this.connections.remove(ws);
            if ($assertionsDisabled || removed) {
            } else {
                throw new AssertionError();
            }
        }
        if (this.isclosed.get() && this.connections.size() == 0) {
            this.selectorthread.interrupt();
        }
        return removed;
    }

    public ServerHandshakeBuilder onWebsocketHandshakeReceivedAsServer(WebSocket conn, Draft draft, ClientHandshake request) throws InvalidDataException {
        return super.onWebsocketHandshakeReceivedAsServer(conn, draft, request);
    }

    protected boolean addConnection(WebSocket ws) {
        if (this.isclosed.get()) {
            ws.close(CloseFrame.GOING_AWAY);
            return true;
        }
        boolean succ;
        synchronized (this.connections) {
            succ = this.connections.add(ws);
            if ($assertionsDisabled || succ) {
            } else {
                throw new AssertionError();
            }
        }
        return succ;
    }

    public final void onWebsocketError(WebSocket conn, Exception ex) {
        onError(conn, ex);
    }

    public final void onWriteDemand(WebSocket w) {
        WebSocketImpl conn = (WebSocketImpl) w;
        try {
            conn.key.interestOps(5);
        } catch (CancelledKeyException e) {
            conn.outQueue.clear();
        }
        this.selector.wakeup();
    }

    public void onWebsocketCloseInitiated(WebSocket conn, int code, String reason) {
        onCloseInitiated(conn, code, reason);
    }

    public void onWebsocketClosing(WebSocket conn, int code, String reason, boolean remote) {
        onClosing(conn, code, reason, remote);
    }

    public void onCloseInitiated(WebSocket conn, int code, String reason) {
    }

    public void onClosing(WebSocket conn, int code, String reason, boolean remote) {
    }

    public final void setWebSocketFactory(WebSocketServerFactory wsf) {
        this.wsf = wsf;
    }

    public final WebSocketFactory getWebSocketFactory() {
        return this.wsf;
    }

    protected boolean onConnect(SelectionKey key) {
        return true;
    }

    private Socket getSocket(WebSocket conn) {
        return ((SocketChannel) ((WebSocketImpl) conn).key.channel()).socket();
    }

    public InetSocketAddress getLocalSocketAddress(WebSocket conn) {
        return (InetSocketAddress) getSocket(conn).getLocalSocketAddress();
    }

    public InetSocketAddress getRemoteSocketAddress(WebSocket conn) {
        return (InetSocketAddress) getSocket(conn).getRemoteSocketAddress();
    }

    public void onMessage(WebSocket conn, ByteBuffer message) {
    }

    public void onFragment(WebSocket conn, Framedata fragment) {
    }
}
