package com.mobcrush.mobcrush.player;

import android.media.MediaCodec.CryptoException;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import com.google.android.exoplayer.CodecCounters;
import com.google.android.exoplayer.DummyTrackRenderer;
import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.ExoPlayer.Factory;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer;
import com.google.android.exoplayer.MediaCodecTrackRenderer.DecoderInitializationException;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TimeRange;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioTrack.InitializationException;
import com.google.android.exoplayer.audio.AudioTrack.WriteException;
import com.google.android.exoplayer.chunk.ChunkSampleSource.EventListener;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.MultiTrackChunkSource;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer.MetadataRenderer;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.TextRenderer;
import com.google.android.exoplayer.upstream.BandwidthMeter;
import com.google.android.exoplayer.util.DebugTextViewHelper.Provider;
import com.google.android.exoplayer.util.PlayerControl;
import com.mobcrush.mobcrush.Constants;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player implements com.google.android.exoplayer.ExoPlayer.Listener, EventListener, HlsSampleSource.EventListener, BandwidthMeter.EventListener, MediaCodecVideoTrackRenderer.EventListener, MediaCodecAudioTrackRenderer.EventListener, StreamingDrmSessionManager.EventListener, DashChunkSource.EventListener, TextRenderer, MetadataRenderer<Map<String, Object>>, Provider {
    public static final int DISABLED_TRACK = -1;
    public static final int PRIMARY_TRACK = 0;
    private static final int RENDERER_BUILDING_STATE_BUILDING = 2;
    private static final int RENDERER_BUILDING_STATE_BUILT = 3;
    private static final int RENDERER_BUILDING_STATE_IDLE = 1;
    public static final int RENDERER_COUNT = 4;
    public static final int STATE_BUFFERING = 3;
    public static final int STATE_ENDED = 5;
    public static final int STATE_IDLE = 1;
    public static final int STATE_PREPARING = 2;
    public static final int STATE_READY = 4;
    public static final int TYPE_AUDIO = 1;
    public static final int TYPE_METADATA = 3;
    public static final int TYPE_TEXT = 2;
    public static final int TYPE_VIDEO = 0;
    private boolean backgrounded;
    private BandwidthMeter bandwidthMeter;
    private InternalRendererBuilderCallback builderCallback;
    private CaptionListener captionListener;
    private CodecCounters codecCounters;
    private Id3MetadataListener id3MetadataListener;
    private InfoListener infoListener;
    private InternalErrorListener internalErrorListener;
    private boolean lastReportedPlayWhenReady;
    private int lastReportedPlaybackState;
    private final CopyOnWriteArrayList<Listener> listeners;
    private final Handler mainHandler;
    private MultiTrackChunkSource[] multiTrackSources;
    private final ExoPlayer player = Factory.newInstance(STATE_READY, Constants.UPDATE_COFIG_INTERVAL, BaseImageDownloader.DEFAULT_HTTP_CONNECT_TIMEOUT);
    private final PlayerControl playerControl;
    private final RendererBuilder rendererBuilder;
    private int rendererBuildingState;
    private int[] selectedTracks;
    private Surface surface;
    private String[][] trackNames;
    private Format videoFormat;
    private TrackRenderer videoRenderer;
    private int videoTrackToRestore;

    public interface Listener {
        void onError(Exception exception);

        void onStateChanged(boolean z, int i);

        void onVideoSizeChanged(int i, int i2, float f);
    }

    public interface Id3MetadataListener {
        void onId3Metadata(Map<String, Object> map);
    }

    public interface CaptionListener {
        void onCues(List<Cue> list);
    }

    public interface RendererBuilder {
        void buildRenderers(Player player, RendererBuilderCallback rendererBuilderCallback);
    }

    public interface InfoListener {
        void onAudioFormatEnabled(Format format, int i, int i2);

        void onBandwidthSample(int i, long j, long j2);

        void onDecoderInitialized(String str, long j, long j2);

        void onDroppedFrames(int i, long j);

        void onLoadCompleted(int i, long j, int i2, int i3, Format format, int i4, int i5, long j2, long j3);

        void onLoadStarted(int i, long j, int i2, int i3, Format format, int i4, int i5);

        void onSeekRangeChanged(TimeRange timeRange);

        void onVideoFormatEnabled(Format format, int i, int i2);
    }

    public interface InternalErrorListener {
        void onAudioTrackInitializationError(InitializationException initializationException);

        void onAudioTrackWriteError(WriteException writeException);

        void onCryptoError(CryptoException cryptoException);

        void onDecoderInitializationError(DecoderInitializationException decoderInitializationException);

        void onDrmSessionManagerError(Exception exception);

        void onLoadError(int i, IOException iOException);

        void onRendererInitializationError(Exception exception);
    }

    public interface RendererBuilderCallback {
        void onRenderers(String[][] strArr, MultiTrackChunkSource[] multiTrackChunkSourceArr, TrackRenderer[] trackRendererArr, BandwidthMeter bandwidthMeter);

        void onRenderersError(Exception exception);
    }

    private class InternalRendererBuilderCallback implements RendererBuilderCallback {
        private boolean canceled;

        private InternalRendererBuilderCallback() {
        }

        public void cancel() {
            this.canceled = true;
        }

        public void onRenderers(String[][] trackNames, MultiTrackChunkSource[] multiTrackSources, TrackRenderer[] renderers, BandwidthMeter bandwidthMeter) {
            if (!this.canceled) {
                Player.this.onRenderers(trackNames, multiTrackSources, renderers, bandwidthMeter);
            }
        }

        public void onRenderersError(Exception e) {
            if (!this.canceled) {
                Player.this.onRenderersError(e);
            }
        }
    }

    public Player(RendererBuilder rendererBuilder) {
        this.rendererBuilder = rendererBuilder;
        this.player.addListener(this);
        this.playerControl = new PlayerControl(this.player);
        this.mainHandler = new Handler();
        this.listeners = new CopyOnWriteArrayList();
        this.lastReportedPlaybackState = TYPE_AUDIO;
        this.rendererBuildingState = TYPE_AUDIO;
        this.selectedTracks = new int[STATE_READY];
        this.selectedTracks[TYPE_TEXT] = DISABLED_TRACK;
    }

    public PlayerControl getPlayerControl() {
        return this.playerControl;
    }

    public void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    public void setInternalErrorListener(InternalErrorListener listener) {
        this.internalErrorListener = listener;
    }

    public void setInfoListener(InfoListener listener) {
        this.infoListener = listener;
    }

    public void setCaptionListener(CaptionListener listener) {
        this.captionListener = listener;
    }

    public void setMetadataListener(Id3MetadataListener listener) {
        this.id3MetadataListener = listener;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
        pushSurface(false);
    }

    public Surface getSurface() {
        return this.surface;
    }

    public void blockingClearSurface() {
        this.surface = null;
        pushSurface(true);
    }

    public int getTrackCount(int type) {
        return !this.player.getRendererHasMedia(type) ? PRIMARY_TRACK : this.trackNames[type].length;
    }

    public String getTrackName(int type, int index) {
        return this.trackNames[type][index];
    }

    public int getSelectedTrackIndex(int type) {
        return this.selectedTracks[type];
    }

    public void selectTrack(int type, int index) {
        if (this.selectedTracks[type] != index) {
            this.selectedTracks[type] = index;
            pushTrackSelection(type, true);
            if (type == TYPE_TEXT && index == DISABLED_TRACK && this.captionListener != null) {
                this.captionListener.onCues(Collections.emptyList());
            }
        }
    }

    public void setBackgrounded(boolean backgrounded) {
        if (this.backgrounded != backgrounded) {
            this.backgrounded = backgrounded;
            if (backgrounded) {
                this.videoTrackToRestore = getSelectedTrackIndex(PRIMARY_TRACK);
                selectTrack(PRIMARY_TRACK, DISABLED_TRACK);
                blockingClearSurface();
                return;
            }
            selectTrack(PRIMARY_TRACK, this.videoTrackToRestore);
        }
    }

    public void prepare() {
        if (this.rendererBuildingState == TYPE_METADATA) {
            this.player.stop();
        }
        if (this.builderCallback != null) {
            this.builderCallback.cancel();
        }
        this.videoFormat = null;
        this.videoRenderer = null;
        this.multiTrackSources = null;
        this.rendererBuildingState = TYPE_TEXT;
        maybeReportPlayerState();
        this.builderCallback = new InternalRendererBuilderCallback();
        this.rendererBuilder.buildRenderers(this, this.builderCallback);
    }

    void onRenderers(String[][] trackNames, MultiTrackChunkSource[] multiTrackSources, TrackRenderer[] renderers, BandwidthMeter bandwidthMeter) {
        CodecCounters codecCounters = null;
        this.builderCallback = null;
        if (trackNames == null) {
            trackNames = new String[STATE_READY][];
        }
        if (multiTrackSources == null) {
            multiTrackSources = new MultiTrackChunkSource[STATE_READY];
        }
        for (int rendererIndex = PRIMARY_TRACK; rendererIndex < STATE_READY; rendererIndex += TYPE_AUDIO) {
            if (renderers[rendererIndex] == null) {
                renderers[rendererIndex] = new DummyTrackRenderer();
            }
            if (trackNames[rendererIndex] == null) {
                trackNames[rendererIndex] = new String[(multiTrackSources[rendererIndex] != null ? multiTrackSources[rendererIndex].getTrackCount() : TYPE_AUDIO)];
            }
        }
        this.trackNames = trackNames;
        this.videoRenderer = renderers[PRIMARY_TRACK];
        if (this.videoRenderer instanceof MediaCodecTrackRenderer) {
            codecCounters = ((MediaCodecTrackRenderer) this.videoRenderer).codecCounters;
        } else if (renderers[TYPE_AUDIO] instanceof MediaCodecTrackRenderer) {
            codecCounters = ((MediaCodecTrackRenderer) renderers[TYPE_AUDIO]).codecCounters;
        }
        this.codecCounters = codecCounters;
        this.multiTrackSources = multiTrackSources;
        this.bandwidthMeter = bandwidthMeter;
        pushSurface(false);
        pushTrackSelection(PRIMARY_TRACK, true);
        pushTrackSelection(TYPE_AUDIO, true);
        pushTrackSelection(TYPE_TEXT, true);
        this.player.prepare(renderers);
        this.rendererBuildingState = TYPE_METADATA;
    }

    void onRenderersError(Exception e) {
        this.builderCallback = null;
        if (this.internalErrorListener != null) {
            this.internalErrorListener.onRendererInitializationError(e);
        }
        Iterator i$ = this.listeners.iterator();
        while (i$.hasNext()) {
            ((Listener) i$.next()).onError(e);
        }
        this.rendererBuildingState = TYPE_AUDIO;
        maybeReportPlayerState();
    }

    public void setPlayWhenReady(boolean playWhenReady) {
        this.player.setPlayWhenReady(playWhenReady);
    }

    public void seekTo(long positionMs) {
        this.player.seekTo(positionMs);
    }

    public void release() {
        if (this.builderCallback != null) {
            this.builderCallback.cancel();
            this.builderCallback = null;
        }
        this.rendererBuildingState = TYPE_AUDIO;
        this.surface = null;
        this.player.release();
    }

    public int getPlaybackState() {
        if (this.rendererBuildingState == TYPE_TEXT) {
            return TYPE_TEXT;
        }
        int playerState = this.player.getPlaybackState();
        return (this.rendererBuildingState == TYPE_METADATA && this.rendererBuildingState == TYPE_AUDIO) ? TYPE_TEXT : playerState;
    }

    public Format getFormat() {
        return this.videoFormat;
    }

    public BandwidthMeter getBandwidthMeter() {
        return this.bandwidthMeter;
    }

    public CodecCounters getCodecCounters() {
        return this.codecCounters;
    }

    public long getCurrentPosition() {
        return this.player.getCurrentPosition();
    }

    public long getDuration() {
        return this.player.getDuration();
    }

    public int getBufferedPercentage() {
        return this.player.getBufferedPercentage();
    }

    public boolean getPlayWhenReady() {
        return this.player.getPlayWhenReady();
    }

    Looper getPlaybackLooper() {
        return this.player.getPlaybackLooper();
    }

    Handler getMainHandler() {
        return this.mainHandler;
    }

    public void onPlayerStateChanged(boolean playWhenReady, int state) {
        maybeReportPlayerState();
    }

    public void onPlayerError(ExoPlaybackException exception) {
        this.rendererBuildingState = TYPE_AUDIO;
        Iterator i$ = this.listeners.iterator();
        while (i$.hasNext()) {
            ((Listener) i$.next()).onError(exception);
        }
    }

    public void onVideoSizeChanged(int width, int height, float pixelWidthHeightRatio) {
        Iterator i$ = this.listeners.iterator();
        while (i$.hasNext()) {
            ((Listener) i$.next()).onVideoSizeChanged(width, height, pixelWidthHeightRatio);
        }
    }

    public void onDroppedFrames(int count, long elapsed) {
        if (this.infoListener != null) {
            this.infoListener.onDroppedFrames(count, elapsed);
        }
    }

    public void onBandwidthSample(int elapsedMs, long bytes, long bitrateEstimate) {
        if (this.infoListener != null) {
            this.infoListener.onBandwidthSample(elapsedMs, bytes, bitrateEstimate);
        }
    }

    public void onDownstreamFormatChanged(int sourceId, Format format, int trigger, int mediaTimeMs) {
        if (this.infoListener != null) {
            if (sourceId == 0) {
                this.videoFormat = format;
                this.infoListener.onVideoFormatEnabled(format, trigger, mediaTimeMs);
            } else if (sourceId == TYPE_AUDIO) {
                this.infoListener.onAudioFormatEnabled(format, trigger, mediaTimeMs);
            }
        }
    }

    public void onDrmSessionManagerError(Exception e) {
        if (this.internalErrorListener != null) {
            this.internalErrorListener.onDrmSessionManagerError(e);
        }
    }

    public void onDecoderInitializationError(DecoderInitializationException e) {
        if (this.internalErrorListener != null) {
            this.internalErrorListener.onDecoderInitializationError(e);
        }
    }

    public void onAudioTrackInitializationError(InitializationException e) {
        if (this.internalErrorListener != null) {
            this.internalErrorListener.onAudioTrackInitializationError(e);
        }
    }

    public void onAudioTrackWriteError(WriteException e) {
        if (this.internalErrorListener != null) {
            this.internalErrorListener.onAudioTrackWriteError(e);
        }
    }

    public void onCryptoError(CryptoException e) {
        if (this.internalErrorListener != null) {
            this.internalErrorListener.onCryptoError(e);
        }
    }

    public void onDecoderInitialized(String decoderName, long elapsedRealtimeMs, long initializationDurationMs) {
        if (this.infoListener != null) {
            this.infoListener.onDecoderInitialized(decoderName, elapsedRealtimeMs, initializationDurationMs);
        }
    }

    public void onLoadError(int sourceId, IOException e) {
        if (this.internalErrorListener != null) {
            this.internalErrorListener.onLoadError(sourceId, e);
        }
    }

    public void onCues(List<Cue> cues) {
        if (this.captionListener != null && this.selectedTracks[TYPE_TEXT] != DISABLED_TRACK) {
            this.captionListener.onCues(cues);
        }
    }

    public void onMetadata(Map<String, Object> metadata) {
        if (this.id3MetadataListener != null && this.selectedTracks[TYPE_METADATA] != DISABLED_TRACK) {
            this.id3MetadataListener.onId3Metadata(metadata);
        }
    }

    public void onSeekRangeChanged(TimeRange seekRange) {
        if (this.infoListener != null) {
            this.infoListener.onSeekRangeChanged(seekRange);
        }
    }

    public void onPlayWhenReadyCommitted() {
    }

    public void onDrawnToSurface(Surface surface) {
    }

    public void onLoadStarted(int sourceId, long length, int type, int trigger, Format format, int mediaStartTimeMs, int mediaEndTimeMs) {
        if (this.infoListener != null) {
            this.infoListener.onLoadStarted(sourceId, length, type, trigger, format, mediaStartTimeMs, mediaEndTimeMs);
        }
    }

    public void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format, int mediaStartTimeMs, int mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs) {
        if (this.infoListener != null) {
            this.infoListener.onLoadCompleted(sourceId, bytesLoaded, type, trigger, format, mediaStartTimeMs, mediaEndTimeMs, elapsedRealtimeMs, loadDurationMs);
        }
    }

    public void onLoadCanceled(int sourceId, long bytesLoaded) {
    }

    public void onUpstreamDiscarded(int sourceId, int mediaStartTimeMs, int mediaEndTimeMs) {
    }

    private void maybeReportPlayerState() {
        boolean playWhenReady = this.player.getPlayWhenReady();
        int playbackState = getPlaybackState();
        if (this.lastReportedPlayWhenReady != playWhenReady || this.lastReportedPlaybackState != playbackState) {
            Iterator i$ = this.listeners.iterator();
            while (i$.hasNext()) {
                ((Listener) i$.next()).onStateChanged(playWhenReady, playbackState);
            }
            this.lastReportedPlayWhenReady = playWhenReady;
            this.lastReportedPlaybackState = playbackState;
        }
    }

    private void pushSurface(boolean blockForSurfacePush) {
        if (this.videoRenderer != null) {
            if (blockForSurfacePush) {
                this.player.blockingSendMessage(this.videoRenderer, TYPE_AUDIO, this.surface);
            } else {
                this.player.sendMessage(this.videoRenderer, TYPE_AUDIO, this.surface);
            }
        }
    }

    private void pushTrackSelection(int type, boolean allowRendererEnable) {
        if (this.multiTrackSources != null) {
            int trackIndex = this.selectedTracks[type];
            if (trackIndex == DISABLED_TRACK) {
                this.player.setRendererEnabled(type, false);
            } else if (this.multiTrackSources[type] == null) {
                this.player.setRendererEnabled(type, allowRendererEnable);
            } else {
                boolean playWhenReady = this.player.getPlayWhenReady();
                this.player.setPlayWhenReady(false);
                this.player.setRendererEnabled(type, false);
                this.player.sendMessage(this.multiTrackSources[type], TYPE_AUDIO, Integer.valueOf(trackIndex));
                this.player.setRendererEnabled(type, allowRendererEnable);
                this.player.setPlayWhenReady(playWhenReady);
            }
        }
    }
}
