package com.firebase.client.utilities;

public class OffsetClock implements Clock {
    private final Clock baseClock;
    private long offset = 0;

    public OffsetClock(Clock baseClock, long offset) {
        this.baseClock = baseClock;
        this.offset = offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }

    public long millis() {
        return this.baseClock.millis() + this.offset;
    }
}
