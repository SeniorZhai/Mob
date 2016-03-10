package com.firebase.tubesock;

import android.support.v4.internal.view.SupportMenu;
import android.support.v4.media.TransportMediator;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

class WebSocketWriter extends Thread {
    private WritableByteChannel channel;
    private boolean closeSent = false;
    private BlockingQueue<ByteBuffer> pendingBuffers;
    private final Random random = new Random();
    private volatile boolean stop = false;
    private WebSocket websocket;

    WebSocketWriter(WebSocket websocket, String threadBaseName, int clientId) {
        setName(threadBaseName + "Writer-" + clientId);
        this.websocket = websocket;
        this.pendingBuffers = new LinkedBlockingQueue();
    }

    void setOutput(OutputStream output) {
        this.channel = Channels.newChannel(output);
    }

    private ByteBuffer frameInBuffer(byte opcode, boolean masking, byte[] data) throws IOException {
        int headerLength = 2;
        if (masking) {
            headerLength = 2 + 4;
        }
        int length = data.length;
        if (length >= TransportMediator.KEYCODE_MEDIA_PLAY) {
            if (length <= SupportMenu.USER_MASK) {
                headerLength += 2;
            } else {
                headerLength += 8;
            }
        }
        ByteBuffer frame = ByteBuffer.allocate(data.length + headerLength);
        frame.put((byte) (Byte.MIN_VALUE | opcode));
        if (length < TransportMediator.KEYCODE_MEDIA_PLAY) {
            if (masking) {
                length |= AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS;
            }
            frame.put((byte) length);
        } else if (length <= SupportMenu.USER_MASK) {
            length_field = TransportMediator.KEYCODE_MEDIA_PLAY;
            if (masking) {
                length_field = TransportMediator.KEYCODE_MEDIA_PLAY | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS;
            }
            frame.put((byte) length_field);
            frame.putShort((short) length);
        } else {
            length_field = TransportMediator.KEYCODE_MEDIA_PAUSE;
            if (masking) {
                length_field = TransportMediator.KEYCODE_MEDIA_PAUSE | AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS;
            }
            frame.put((byte) length_field);
            frame.putInt(0);
            frame.putInt(length);
        }
        if (masking) {
            byte[] mask = generateMask();
            frame.put(mask);
            for (int i = 0; i < data.length; i++) {
                frame.put((byte) (data[i] ^ mask[i % 4]));
            }
        }
        frame.flip();
        return frame;
    }

    private byte[] generateMask() {
        byte[] mask = new byte[4];
        this.random.nextBytes(mask);
        return mask;
    }

    synchronized void send(byte opcode, boolean masking, byte[] data) throws IOException {
        ByteBuffer frame = frameInBuffer(opcode, masking, data);
        if (!this.stop || (!this.closeSent && opcode == (byte) 8)) {
            if (opcode == (byte) 8) {
                this.closeSent = true;
            }
            this.pendingBuffers.add(frame);
        } else {
            throw new WebSocketException("Shouldn't be sending");
        }
    }

    public void run() {
        while (!this.stop && !Thread.interrupted()) {
            try {
                writeMessage();
            } catch (IOException e) {
                handleError(new WebSocketException("IO Exception", e));
                return;
            } catch (InterruptedException e2) {
                return;
            }
        }
        for (int i = 0; i < this.pendingBuffers.size(); i++) {
            writeMessage();
        }
    }

    private void writeMessage() throws InterruptedException, IOException {
        this.channel.write((ByteBuffer) this.pendingBuffers.take());
    }

    void stopIt() {
        this.stop = true;
    }

    private void handleError(WebSocketException e) {
        this.websocket.handleReceiverError(e);
    }
}
