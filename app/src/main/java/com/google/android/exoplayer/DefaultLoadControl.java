package com.google.android.exoplayer;

import android.os.Handler;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.NetworkLock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DefaultLoadControl implements LoadControl {
    private static final int ABOVE_HIGH_WATERMARK = 0;
    private static final int BELOW_LOW_WATERMARK = 2;
    private static final int BETWEEN_WATERMARKS = 1;
    public static final float DEFAULT_HIGH_BUFFER_LOAD = 0.8f;
    public static final int DEFAULT_HIGH_WATERMARK_MS = 30000;
    public static final float DEFAULT_LOW_BUFFER_LOAD = 0.2f;
    public static final int DEFAULT_LOW_WATERMARK_MS = 15000;
    private final Allocator allocator;
    private int bufferState;
    private final Handler eventHandler;
    private final EventListener eventListener;
    private boolean fillingBuffers;
    private final float highBufferLoad;
    private final long highWatermarkUs;
    private final HashMap<Object, LoaderState> loaderStates;
    private final List<Object> loaders;
    private final float lowBufferLoad;
    private final long lowWatermarkUs;
    private long maxLoadStartPositionUs;
    private boolean streamingPrioritySet;
    private int targetBufferSize;

    public interface EventListener {
        void onLoadingChanged(boolean z);
    }

    private static class LoaderState {
        public final int bufferSizeContribution;
        public int bufferState = DefaultLoadControl.ABOVE_HIGH_WATERMARK;
        public boolean failed = false;
        public boolean loading = false;
        public long nextLoadPositionUs = -1;

        public LoaderState(int bufferSizeContribution) {
            this.bufferSizeContribution = bufferSizeContribution;
        }
    }

    public DefaultLoadControl(Allocator allocator) {
        this(allocator, null, null);
    }

    public DefaultLoadControl(Allocator allocator, Handler eventHandler, EventListener eventListener) {
        this(allocator, eventHandler, eventListener, DEFAULT_LOW_WATERMARK_MS, DEFAULT_HIGH_WATERMARK_MS, DEFAULT_LOW_BUFFER_LOAD, DEFAULT_HIGH_BUFFER_LOAD);
    }

    public DefaultLoadControl(Allocator allocator, Handler eventHandler, EventListener eventListener, int lowWatermarkMs, int highWatermarkMs, float lowBufferLoad, float highBufferLoad) {
        this.allocator = allocator;
        this.eventHandler = eventHandler;
        this.eventListener = eventListener;
        this.loaders = new ArrayList();
        this.loaderStates = new HashMap();
        this.lowWatermarkUs = ((long) lowWatermarkMs) * 1000;
        this.highWatermarkUs = ((long) highWatermarkMs) * 1000;
        this.lowBufferLoad = lowBufferLoad;
        this.highBufferLoad = highBufferLoad;
    }

    public void register(Object loader, int bufferSizeContribution) {
        this.loaders.add(loader);
        this.loaderStates.put(loader, new LoaderState(bufferSizeContribution));
        this.targetBufferSize += bufferSizeContribution;
    }

    public void unregister(Object loader) {
        this.loaders.remove(loader);
        this.targetBufferSize -= ((LoaderState) this.loaderStates.remove(loader)).bufferSizeContribution;
        updateControlState();
    }

    public void trimAllocator() {
        this.allocator.trim(this.targetBufferSize);
    }

    public Allocator getAllocator() {
        return this.allocator;
    }

    public boolean update(Object loader, long playbackPositionUs, long nextLoadPositionUs, boolean loading, boolean failed) {
        int loaderBufferState = getLoaderBufferState(playbackPositionUs, nextLoadPositionUs);
        LoaderState loaderState = (LoaderState) this.loaderStates.get(loader);
        boolean loaderStateChanged = (loaderState.bufferState == loaderBufferState && loaderState.nextLoadPositionUs == nextLoadPositionUs && loaderState.loading == loading && loaderState.failed == failed) ? false : true;
        if (loaderStateChanged) {
            loaderState.bufferState = loaderBufferState;
            loaderState.nextLoadPositionUs = nextLoadPositionUs;
            loaderState.loading = loading;
            loaderState.failed = failed;
        }
        int currentBufferSize = this.allocator.getTotalBytesAllocated();
        int bufferState = getBufferState(currentBufferSize);
        boolean bufferStateChanged = this.bufferState != bufferState;
        if (bufferStateChanged) {
            this.bufferState = bufferState;
        }
        if (loaderStateChanged || bufferStateChanged) {
            updateControlState();
        }
        if (currentBufferSize >= this.targetBufferSize || nextLoadPositionUs == -1 || nextLoadPositionUs > this.maxLoadStartPositionUs) {
            return false;
        }
        return true;
    }

    private int getLoaderBufferState(long playbackPositionUs, long nextLoadPositionUs) {
        if (nextLoadPositionUs == -1) {
            return ABOVE_HIGH_WATERMARK;
        }
        long timeUntilNextLoadPosition = nextLoadPositionUs - playbackPositionUs;
        if (timeUntilNextLoadPosition <= this.highWatermarkUs) {
            return timeUntilNextLoadPosition < this.lowWatermarkUs ? BELOW_LOW_WATERMARK : BETWEEN_WATERMARKS;
        } else {
            return ABOVE_HIGH_WATERMARK;
        }
    }

    private int getBufferState(int currentBufferSize) {
        float bufferLoad = ((float) currentBufferSize) / ((float) this.targetBufferSize);
        if (bufferLoad > this.highBufferLoad) {
            return ABOVE_HIGH_WATERMARK;
        }
        return bufferLoad < this.lowBufferLoad ? BELOW_LOW_WATERMARK : BETWEEN_WATERMARKS;
    }

    private void updateControlState() {
        int i;
        boolean loading = false;
        boolean failed = false;
        boolean haveNextLoadPosition = false;
        int highestState = this.bufferState;
        for (i = ABOVE_HIGH_WATERMARK; i < this.loaders.size(); i += BETWEEN_WATERMARKS) {
            LoaderState loaderState = (LoaderState) this.loaderStates.get(this.loaders.get(i));
            loading |= loaderState.loading;
            failed |= loaderState.failed;
            haveNextLoadPosition |= loaderState.nextLoadPositionUs != -1 ? BETWEEN_WATERMARKS : ABOVE_HIGH_WATERMARK;
            highestState = Math.max(highestState, loaderState.bufferState);
        }
        boolean z = (this.loaders.isEmpty() || failed || ((!loading && !haveNextLoadPosition) || (highestState != BELOW_LOW_WATERMARK && (highestState != BETWEEN_WATERMARKS || !this.fillingBuffers)))) ? false : true;
        this.fillingBuffers = z;
        if (this.fillingBuffers && !this.streamingPrioritySet) {
            NetworkLock.instance.add(ABOVE_HIGH_WATERMARK);
            this.streamingPrioritySet = true;
            notifyLoadingChanged(true);
        } else if (!(this.fillingBuffers || !this.streamingPrioritySet || loading)) {
            NetworkLock.instance.remove(ABOVE_HIGH_WATERMARK);
            this.streamingPrioritySet = false;
            notifyLoadingChanged(false);
        }
        this.maxLoadStartPositionUs = -1;
        if (this.fillingBuffers) {
            for (i = ABOVE_HIGH_WATERMARK; i < this.loaders.size(); i += BETWEEN_WATERMARKS) {
                long loaderTime = ((LoaderState) this.loaderStates.get(this.loaders.get(i))).nextLoadPositionUs;
                if (loaderTime != -1 && (this.maxLoadStartPositionUs == -1 || loaderTime < this.maxLoadStartPositionUs)) {
                    this.maxLoadStartPositionUs = loaderTime;
                }
            }
        }
    }

    private void notifyLoadingChanged(final boolean loading) {
        if (this.eventHandler != null && this.eventListener != null) {
            this.eventHandler.post(new Runnable() {
                public void run() {
                    DefaultLoadControl.this.eventListener.onLoadingChanged(loading);
                }
            });
        }
    }
}
