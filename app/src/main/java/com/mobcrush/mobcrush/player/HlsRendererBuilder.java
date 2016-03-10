package com.mobcrush.mobcrush.player;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.chunk.VideoFormatSelectorUtil;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsMasterPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.metadata.Id3Parser;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer;
import com.google.android.exoplayer.text.eia608.Eia608TrackRenderer;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.upstream.TransferListener;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.ManifestFetcher.ManifestCallback;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.player.Player.RendererBuilder;
import com.mobcrush.mobcrush.player.Player.RendererBuilderCallback;
import java.io.IOException;
import java.util.Map;

public class HlsRendererBuilder implements RendererBuilder, ManifestCallback<HlsPlaylist> {
    private static final int BUFFER_SEGMENTS = 64;
    private static final int BUFFER_SEGMENT_SIZE = 262144;
    private final AudioCapabilities audioCapabilities;
    private RendererBuilderCallback callback;
    private final Context context;
    private Player player;
    private final String url;
    private final String userAgent;

    public HlsRendererBuilder(Context context, String userAgent, String url, AudioCapabilities audioCapabilities) {
        this.context = context;
        this.userAgent = userAgent;
        this.url = url;
        this.audioCapabilities = audioCapabilities;
    }

    public void buildRenderers(Player player, RendererBuilderCallback callback) {
        this.player = player;
        this.callback = callback;
        new ManifestFetcher(this.url, new DefaultUriDataSource(this.context, this.userAgent), new HlsPlaylistParser()).singleLoad(player.getMainHandler().getLooper(), this);
    }

    public void onSingleManifestError(IOException e) {
        this.callback.onRenderersError(e);
    }

    public void onSingleManifest(HlsPlaylist manifest) {
        Handler mainHandler = this.player.getMainHandler();
        LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
        TransferListener bandwidthMeter = new DefaultBandwidthMeter();
        int[] variantIndices = null;
        if (manifest instanceof HlsMasterPlaylist) {
            try {
                variantIndices = VideoFormatSelectorUtil.selectVideoFormatsForDefaultDisplay(this.context, ((HlsMasterPlaylist) manifest).variants, null, false);
            } catch (Exception e) {
                this.callback.onRenderersError(e);
                return;
            }
        }
        HlsSampleSource sampleSource = new HlsSampleSource(new HlsChunkSource(new DefaultUriDataSource(this.context, bandwidthMeter, this.userAgent), this.url, manifest, bandwidthMeter, variantIndices, 1, this.audioCapabilities), loadControl, ViewCompat.MEASURED_STATE_TOO_SMALL, true, mainHandler, this.player, 0);
        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource, 1, Constants.NOTIFICATION_BANNER_TIMEOUT, mainHandler, this.player, 50);
        MediaCodecAudioTrackRenderer mediaCodecAudioTrackRenderer = new MediaCodecAudioTrackRenderer(sampleSource);
        MetadataTrackRenderer<Map<String, Object>> metadataTrackRenderer = new MetadataTrackRenderer(sampleSource, new Id3Parser(), this.player, mainHandler.getLooper());
        Eia608TrackRenderer eia608TrackRenderer = new Eia608TrackRenderer(sampleSource, this.player, mainHandler.getLooper());
        this.callback.onRenderers((String[][]) null, null, new TrackRenderer[]{videoRenderer, mediaCodecAudioTrackRenderer, metadataTrackRenderer, eia608TrackRenderer}, bandwidthMeter);
    }
}
