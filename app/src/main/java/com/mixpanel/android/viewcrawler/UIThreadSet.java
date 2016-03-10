package com.mixpanel.android.viewcrawler;

import android.os.Looper;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class UIThreadSet<T> {
    private Set<T> mSet = new HashSet();

    public void add(T item) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Can't add an activity when not on the UI thread");
        }
        this.mSet.add(item);
    }

    public void remove(T item) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new RuntimeException("Can't remove an activity when not on the UI thread");
        }
        this.mSet.remove(item);
    }

    public Set<T> getAll() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            return Collections.unmodifiableSet(this.mSet);
        }
        throw new RuntimeException("Can't remove an activity when not on the UI thread");
    }

    public boolean isEmpty() {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            return this.mSet.isEmpty();
        }
        throw new RuntimeException("Can't check isEmpty() when not on the UI thread");
    }
}
