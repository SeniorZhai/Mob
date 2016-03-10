package com.firebase.tubesock;

public class WebSocketException extends RuntimeException {
    private static final long serialVersionUID = 1;

    public WebSocketException(String message) {
        super(message);
    }

    public WebSocketException(String message, Throwable t) {
        super(message, t);
    }
}
