package com.google.android.exoplayer;

import com.google.android.exoplayer.upstream.Allocator;

public interface LoadControl {
    Allocator getAllocator();

    void register(Object obj, int i);

    void trimAllocator();

    void unregister(Object obj);

    boolean update(Object obj, long j, long j2, boolean z, boolean z2);
}
