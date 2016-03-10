package com.mobcrush.mobcrush.player;

import android.content.Context;
import android.net.Uri;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.extractor.Extractor;
import com.google.android.exoplayer.extractor.ExtractorSampleSource;
import com.google.android.exoplayer.text.TextTrackRenderer;
import com.google.android.exoplayer.text.tx3g.Tx3gParser;
import com.google.android.exoplayer.upstream.Allocator;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.upstream.TransferListener;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.player.Player.RendererBuilder;
import com.mobcrush.mobcrush.player.Player.RendererBuilderCallback;

public class ExtractorRendererBuilder implements RendererBuilder {
    private static final int BUFFER_SEGMENT_COUNT = 160;
    private static final int BUFFER_SEGMENT_SIZE = 65536;
    private final Context context;
    private final Extractor extractor;
    private final Uri uri;
    private final String userAgent;

    public ExtractorRendererBuilder(Context context, String userAgent, Uri uri, Extractor extractor) {
        this.context = context;
        this.userAgent = userAgent;
        this.uri = uri;
        this.extractor = extractor;
    }

    public void buildRenderers(Player player, RendererBuilderCallback callback) {
        Allocator allocator = new DefaultAllocator(BUFFER_SEGMENT_SIZE);
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter(player.getMainHandler(), null);
        ExtractorSampleSource sampleSource = new ExtractorSampleSource(this.uri, new DefaultUriDataSource(this.context, (TransferListener) defaultBandwidthMeter, this.userAgent), this.extractor, allocator, 10485760);
        MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(sampleSource, null, true, 1, Constants.NOTIFICATION_BANNER_TIMEOUT, null, player.getMainHandler(), player, 50);
        MediaCodecAudioTrackRenderer audioRenderer = new MediaCodecAudioTrackRenderer(sampleSource, null, true, player.getMainHandler(), player);
        TrackRenderer textTrackRenderer = new TextTrackRenderer(sampleSource, player, player.getMainHandler().getLooper(), new Tx3gParser());
        TrackRenderer[] renderers = new TrackRenderer[4];
        renderers[0] = videoRenderer;
        renderers[1] = audioRenderer;
        renderers[2] = textTrackRenderer;
        callback.onRenderers((String[][]) null, null, renderers, defaultBandwidthMeter);
    }
}
