package com.firebase.client.utilities;

public class DefaultClock implements Clock {
    public long millis() {
        return System.currentTimeMillis();
    }
}
