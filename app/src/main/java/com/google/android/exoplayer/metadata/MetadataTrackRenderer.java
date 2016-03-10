package com.google.android.exoplayer.metadata;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.MediaFormatHolder;
import com.google.android.exoplayer.SampleHolder;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.SampleSource.SampleSourceReader;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.util.Assertions;
import io.fabric.sdk.android.services.common.ResponseParser;

public class MetadataTrackRenderer<T> extends TrackRenderer implements Callback {
    private static final int MSG_INVOKE_RENDERER = 0;
    private final MediaFormatHolder formatHolder;
    private boolean inputStreamEnded;
    private final Handler metadataHandler;
    private final MetadataParser<T> metadataParser;
    private final MetadataRenderer<T> metadataRenderer;
    private T pendingMetadata;
    private long pendingMetadataTimestamp;
    private final SampleHolder sampleHolder;
    private final SampleSourceReader source;
    private int trackIndex;

    public interface MetadataRenderer<T> {
        void onMetadata(T t);
    }

    public MetadataTrackRenderer(SampleSource source, MetadataParser<T> metadataParser, MetadataRenderer<T> metadataRenderer, Looper metadataRendererLooper) {
        this.source = source.register();
        this.metadataParser = (MetadataParser) Assertions.checkNotNull(metadataParser);
        this.metadataRenderer = (MetadataRenderer) Assertions.checkNotNull(metadataRenderer);
        this.metadataHandler = metadataRendererLooper == null ? null : new Handler(metadataRendererLooper, this);
        this.formatHolder = new MediaFormatHolder();
        this.sampleHolder = new SampleHolder(1);
    }

    protected int doPrepare(long positionUs) throws ExoPlaybackException {
        try {
            if (!this.source.prepare(positionUs)) {
                return 0;
            }
            for (int i = 0; i < this.source.getTrackCount(); i++) {
                if (this.metadataParser.canParse(this.source.getTrackInfo(i).mimeType)) {
                    this.trackIndex = i;
                    return 1;
                }
            }
            return -1;
        } catch (Throwable e) {
            throw new ExoPlaybackException(e);
        }
    }

    protected void onEnabled(long positionUs, boolean joining) {
        this.source.enable(this.trackIndex, positionUs);
        seekToInternal();
    }

    protected void seekTo(long positionUs) throws ExoPlaybackException {
        this.source.seekToUs(positionUs);
        seekToInternal();
    }

    private void seekToInternal() {
        this.pendingMetadata = null;
        this.inputStreamEnded = false;
    }

    protected void doSomeWork(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
        try {
            this.source.continueBuffering(positionUs);
            if (!this.inputStreamEnded && this.pendingMetadata == null) {
                try {
                    int result = this.source.readData(this.trackIndex, positionUs, this.formatHolder, this.sampleHolder, false);
                    if (result == -3) {
                        this.pendingMetadataTimestamp = this.sampleHolder.timeUs;
                        this.pendingMetadata = this.metadataParser.parse(this.sampleHolder.data.array(), this.sampleHolder.size);
                        this.sampleHolder.data.clear();
                    } else if (result == -1) {
                        this.inputStreamEnded = true;
                    }
                } catch (Throwable e) {
                    throw new ExoPlaybackException(e);
                }
            }
            if (this.pendingMetadata != null && this.pendingMetadataTimestamp <= positionUs) {
                invokeRenderer(this.pendingMetadata);
                this.pendingMetadata = null;
            }
        } catch (Throwable e2) {
            throw new ExoPlaybackException(e2);
        }
    }

    protected void onDisabled() {
        this.pendingMetadata = null;
        this.source.disable(this.trackIndex);
    }

    protected long getDurationUs() {
        return this.source.getTrackInfo(this.trackIndex).durationUs;
    }

    protected long getBufferedPositionUs() {
        return -3;
    }

    protected boolean isEnded() {
        return this.inputStreamEnded;
    }

    protected boolean isReady() {
        return true;
    }

    private void invokeRenderer(T metadata) {
        if (this.metadataHandler != null) {
            this.metadataHandler.obtainMessage(0, metadata).sendToTarget();
        } else {
            invokeRendererInternal(metadata);
        }
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                invokeRendererInternal(msg.obj);
                return true;
            default:
                return false;
        }
    }

    private void invokeRendererInternal(T metadata) {
        this.metadataRenderer.onMetadata(metadata);
    }
}
