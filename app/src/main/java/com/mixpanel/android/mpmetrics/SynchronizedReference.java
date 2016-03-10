package com.mixpanel.android.mpmetrics;

class SynchronizedReference<T> {
    private T mContents = null;

    public synchronized void set(T contents) {
        this.mContents = contents;
    }

    public synchronized T getAndClear() {
        T ret;
        ret = this.mContents;
        this.mContents = null;
        return ret;
    }

    public synchronized T get() {
        return this.mContents;
    }
}
