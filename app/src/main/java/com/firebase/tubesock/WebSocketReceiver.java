package com.firebase.tubesock;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.DataInputStream;
import java.io.IOException;

class WebSocketReceiver {
    private WebSocketEventHandler eventHandler = null;
    private DataInputStream input = null;
    private byte[] inputHeader = new byte[112];
    private Builder pendingBuilder;
    private volatile boolean stop = false;
    private WebSocket websocket = null;

    WebSocketReceiver(WebSocket websocket) {
        this.websocket = websocket;
    }

    void setInput(DataInputStream input) {
        this.input = input;
    }

    void run() {
        this.eventHandler = this.websocket.getEventHandler();
        while (!this.stop) {
            try {
                int offset = 0 + read(this.inputHeader, 0, 1);
                boolean fin = (this.inputHeader[0] & AccessibilityNodeInfoCompat.ACTION_CLEAR_ACCESSIBILITY_FOCUS) != 0;
                if ((this.inputHeader[0] & 112) != 0) {
                    throw new WebSocketException("Invalid frame received");
                }
                byte opcode = (byte) (this.inputHeader[0] & 15);
                offset += read(this.inputHeader, offset, 1);
                byte length = this.inputHeader[1];
                long payload_length = 0;
                if (length < (byte) 126) {
                    payload_length = (long) length;
                } else if (length == (byte) 126) {
                    offset += read(this.inputHeader, offset, 2);
                    payload_length = (long) (((this.inputHeader[2] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8) | (this.inputHeader[3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT));
                } else if (length == Byte.MAX_VALUE) {
                    payload_length = parseLong(this.inputHeader, (offset + read(this.inputHeader, offset, 8)) - 8);
                }
                byte[] payload = new byte[((int) payload_length)];
                read(payload, 0, (int) payload_length);
                if (opcode == (byte) 8) {
                    this.websocket.onCloseOpReceived();
                } else if (opcode == (byte) 10) {
                    continue;
                } else if (opcode == (byte) 1 || opcode == (byte) 2 || opcode == (byte) 9 || opcode == (byte) 0) {
                    appendBytes(fin, opcode, payload);
                } else {
                    throw new WebSocketException("Unsupported opcode: " + opcode);
                }
            } catch (IOException ioe) {
                handleError(new WebSocketException("IO Error", ioe));
            } catch (WebSocketException e) {
                handleError(e);
            }
        }
    }

    private void appendBytes(boolean fin, byte opcode, byte[] data) {
        if (opcode == (byte) 9) {
            if (fin) {
                handlePing(data);
                return;
            }
            throw new WebSocketException("PING must not fragment across frames");
        } else if (this.pendingBuilder != null && opcode != (byte) 0) {
            throw new WebSocketException("Failed to continue outstanding frame");
        } else if (this.pendingBuilder == null && opcode == (byte) 0) {
            throw new WebSocketException("Received continuing frame, but there's nothing to continue");
        } else {
            if (this.pendingBuilder == null) {
                this.pendingBuilder = MessageBuilderFactory.builder(opcode);
            }
            if (!this.pendingBuilder.appendBytes(data)) {
                throw new WebSocketException("Failed to decode frame");
            } else if (fin) {
                WebSocketMessage message = this.pendingBuilder.toMessage();
                this.pendingBuilder = null;
                if (message == null) {
                    throw new WebSocketException("Failed to decode whole message");
                }
                this.eventHandler.onMessage(message);
            }
        }
    }

    private void handlePing(byte[] payload) {
        if (payload.length <= 125) {
            this.websocket.pong(payload);
            return;
        }
        throw new WebSocketException("PING frame too long");
    }

    private long parseLong(byte[] buffer, int offset) {
        return (((((((((long) buffer[offset + 0]) << 56) + (((long) (buffer[offset + 1] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT)) << 48)) + (((long) (buffer[offset + 2] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT)) << 40)) + (((long) (buffer[offset + 3] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT)) << 32)) + (((long) (buffer[offset + 4] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT)) << 24)) + ((long) ((buffer[offset + 5] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 16))) + ((long) ((buffer[offset + 6] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 8))) + ((long) ((buffer[offset + 7] & SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) << 0));
    }

    private int read(byte[] buffer, int offset, int length) throws IOException {
        this.input.readFully(buffer, offset, length);
        return length;
    }

    void stopit() {
        this.stop = true;
    }

    boolean isRunning() {
        return !this.stop;
    }

    private void handleError(WebSocketException e) {
        stopit();
        this.websocket.handleReceiverError(e);
    }
}
