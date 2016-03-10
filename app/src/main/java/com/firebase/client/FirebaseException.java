package com.firebase.client;

public class FirebaseException extends RuntimeException {
    public FirebaseException(String message) {
        super(message);
    }

    public FirebaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
