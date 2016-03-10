package com.google.android.exoplayer.util;

import android.os.HandlerThread;
import android.os.Process;

public class PriorityHandlerThread extends HandlerThread {
    private final int priority;

    public PriorityHandlerThread(String name, int priority) {
        super(name);
        this.priority = priority;
    }

    public void run() {
        Process.setThreadPriority(this.priority);
        super.run();
    }
}
