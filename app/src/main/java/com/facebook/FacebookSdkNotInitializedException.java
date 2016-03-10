package com.facebook;

public class FacebookSdkNotInitializedException extends FacebookException {
    static final long serialVersionUID = 1;

    public FacebookSdkNotInitializedException(String message) {
        super(message);
    }

    public FacebookSdkNotInitializedException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public FacebookSdkNotInitializedException(Throwable throwable) {
        super(throwable);
    }
}
