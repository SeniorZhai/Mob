package com.mixpanel.android.mpmetrics;

class BadDecideObjectException extends Exception {
    private static final long serialVersionUID = 4858739193395706341L;

    public BadDecideObjectException(String detailMessage) {
        super(detailMessage);
    }

    public BadDecideObjectException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
