package com.google.android.exoplayer.upstream.cache;

import com.google.android.exoplayer.upstream.cache.Cache.Listener;

public interface CacheEvictor extends Listener {
    void onStartFile(Cache cache, String str, long j, long j2);
}
