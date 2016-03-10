package com.mobcrush.mobcrush.player;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecAudioTrackRenderer;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.chunk.ChunkSampleSource;
import com.google.android.exoplayer.chunk.ChunkSource;
import com.google.android.exoplayer.chunk.Format;
import com.google.android.exoplayer.chunk.FormatEvaluator;
import com.google.android.exoplayer.chunk.FormatEvaluator.AdaptiveEvaluator;
import com.google.android.exoplayer.chunk.FormatEvaluator.FixedEvaluator;
import com.google.android.exoplayer.chunk.MultiTrackChunkSource;
import com.google.android.exoplayer.chunk.VideoFormatSelectorUtil;
import com.google.android.exoplayer.dash.DashChunkSource;
import com.google.android.exoplayer.dash.mpd.AdaptationSet;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescription;
import com.google.android.exoplayer.dash.mpd.MediaPresentationDescriptionParser;
import com.google.android.exoplayer.dash.mpd.Period;
import com.google.android.exoplayer.dash.mpd.Representation;
import com.google.android.exoplayer.dash.mpd.UtcTimingElement;
import com.google.android.exoplayer.dash.mpd.UtcTimingElementResolver;
import com.google.android.exoplayer.dash.mpd.UtcTimingElementResolver.UtcTimingCallback;
import com.google.android.exoplayer.drm.MediaDrmCallback;
import com.google.android.exoplayer.drm.StreamingDrmSessionManager;
import com.google.android.exoplayer.drm.UnsupportedDrmException;
import com.google.android.exoplayer.text.SubtitleParser;
import com.google.android.exoplayer.text.TextRenderer;
import com.google.android.exoplayer.text.TextTrackRenderer;
import com.google.android.exoplayer.text.ttml.TtmlParser;
import com.google.android.exoplayer.text.webvtt.WebvttParser;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.upstream.TransferListener;
import com.google.android.exoplayer.upstream.UriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.ManifestFetcher.ManifestCallback;
import com.google.android.exoplayer.util.Util;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.player.Player.RendererBuilder;
import com.mobcrush.mobcrush.player.Player.RendererBuilderCallback;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DashRendererBuilder implements RendererBuilder, ManifestCallback<MediaPresentationDescription>, UtcTimingCallback {
    private static final int AUDIO_BUFFER_SEGMENTS = 60;
    private static final int BUFFER_SEGMENT_SIZE = 65536;
    private static final int LIVE_EDGE_LATENCY_MS = 30000;
    private static final String[] PASSTHROUGH_CODECS_PRIORITY;
    private static final int[] PASSTHROUGH_ENCODINGS_PRIORITY = new int[]{6, 5};
    private static final int SECURITY_LEVEL_1 = 1;
    private static final int SECURITY_LEVEL_3 = 3;
    private static final int SECURITY_LEVEL_UNKNOWN = -1;
    private static final String TAG = "DashRendererBuilder";
    private static final int TEXT_BUFFER_SEGMENTS = 2;
    private static final int VIDEO_BUFFER_SEGMENTS = 200;
    private final AudioCapabilities audioCapabilities;
    private RendererBuilderCallback callback;
    private final Context context;
    private final MediaDrmCallback drmCallback;
    private long elapsedRealtimeOffset;
    private MediaPresentationDescription manifest;
    private UriDataSource manifestDataSource;
    private ManifestFetcher<MediaPresentationDescription> manifestFetcher;
    private Player player;
    private final String url;
    private final String userAgent;

    static {
        String[] strArr = new String[TEXT_BUFFER_SEGMENTS];
        strArr[0] = "ec-3";
        strArr[SECURITY_LEVEL_1] = "ac-3";
        PASSTHROUGH_CODECS_PRIORITY = strArr;
    }

    public DashRendererBuilder(Context context, String userAgent, String url, MediaDrmCallback drmCallback, AudioCapabilities audioCapabilities) {
        this.context = context;
        this.userAgent = userAgent;
        this.url = url;
        this.drmCallback = drmCallback;
        this.audioCapabilities = audioCapabilities;
    }

    public void buildRenderers(Player player, RendererBuilderCallback callback) {
        this.player = player;
        this.callback = callback;
        MediaPresentationDescriptionParser parser = new MediaPresentationDescriptionParser();
        this.manifestDataSource = new DefaultUriDataSource(this.context, this.userAgent);
        this.manifestFetcher = new ManifestFetcher(this.url, this.manifestDataSource, parser);
        this.manifestFetcher.singleLoad(player.getMainHandler().getLooper(), this);
    }

    public void onSingleManifest(MediaPresentationDescription manifest) {
        this.manifest = manifest;
        if (!manifest.dynamic || manifest.utcTiming == null) {
            buildRenderers();
        } else {
            UtcTimingElementResolver.resolveTimingElement(this.manifestDataSource, manifest.utcTiming, this.manifestFetcher.getManifestLoadTimestamp(), this);
        }
    }

    public void onSingleManifestError(IOException e) {
        this.callback.onRenderersError(e);
    }

    public void onTimestampResolved(UtcTimingElement utcTiming, long elapsedRealtimeOffset) {
        this.elapsedRealtimeOffset = elapsedRealtimeOffset;
        buildRenderers();
    }

    public void onTimestampError(UtcTimingElement utcTiming, IOException e) {
        Log.e(TAG, "Failed to resolve UtcTiming element [" + utcTiming + "]", e);
        buildRenderers();
    }

    private void buildRenderers() {
        Period period = (Period) this.manifest.periods.get(0);
        Handler mainHandler = this.player.getMainHandler();
        LoadControl defaultLoadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter(mainHandler, this.player);
        boolean hasContentProtection = false;
        int videoAdaptationSetIndex = period.getAdaptationSetIndex(0);
        int audioAdaptationSetIndex = period.getAdaptationSetIndex(SECURITY_LEVEL_1);
        AdaptationSet videoAdaptationSet = null;
        AdaptationSet audioAdaptationSet = null;
        if (videoAdaptationSetIndex != SECURITY_LEVEL_UNKNOWN) {
            videoAdaptationSet = (AdaptationSet) period.adaptationSets.get(videoAdaptationSetIndex);
            hasContentProtection = false | videoAdaptationSet.hasContentProtection();
        }
        if (audioAdaptationSetIndex != SECURITY_LEVEL_UNKNOWN) {
            audioAdaptationSet = (AdaptationSet) period.adaptationSets.get(audioAdaptationSetIndex);
            hasContentProtection |= audioAdaptationSet.hasContentProtection();
        }
        if (videoAdaptationSet == null && audioAdaptationSet == null) {
            this.callback.onRenderersError(new IllegalStateException("No video or audio adaptation sets"));
            return;
        }
        MediaCodecVideoTrackRenderer videoRenderer;
        DataSource defaultUriDataSource;
        int i;
        int j;
        TrackRenderer audioRenderer;
        TrackRenderer textRenderer;
        boolean filterHdContent = false;
        StreamingDrmSessionManager drmSessionManager = null;
        if (hasContentProtection) {
            if (Util.SDK_INT < 18) {
                this.callback.onRenderersError(new UnsupportedDrmException(SECURITY_LEVEL_1));
                return;
            }
            try {
                drmSessionManager = StreamingDrmSessionManager.newWidevineInstance(this.player.getPlaybackLooper(), this.drmCallback, null, this.player.getMainHandler(), this.player);
                filterHdContent = (videoAdaptationSet == null || !videoAdaptationSet.hasContentProtection() || getWidevineSecurityLevel(drmSessionManager) == SECURITY_LEVEL_1) ? false : true;
            } catch (Exception e) {
                this.callback.onRenderersError(e);
                return;
            }
        }
        int[] videoRepresentationIndices = null;
        if (videoAdaptationSet != null) {
            try {
                videoRepresentationIndices = VideoFormatSelectorUtil.selectVideoFormatsForDefaultDisplay(this.context, videoAdaptationSet.representations, null, filterHdContent);
            } catch (Exception e2) {
                this.callback.onRenderersError(e2);
                return;
            }
        }
        if (videoRepresentationIndices == null || videoRepresentationIndices.length == 0) {
            videoRenderer = null;
        } else {
            videoRenderer = new MediaCodecVideoTrackRenderer(new ChunkSampleSource(new DashChunkSource(this.manifestFetcher, videoAdaptationSetIndex, videoRepresentationIndices, new DefaultUriDataSource(this.context, (TransferListener) defaultBandwidthMeter, this.userAgent), new AdaptiveEvaluator(defaultBandwidthMeter), Constants.BROADCASTS_LIVE_TIME, this.elapsedRealtimeOffset, mainHandler, this.player), defaultLoadControl, 13107200, true, mainHandler, this.player, 0), drmSessionManager, true, SECURITY_LEVEL_1, Constants.NOTIFICATION_BANNER_TIMEOUT, null, mainHandler, this.player, 50);
        }
        List<ChunkSource> audioChunkSourceList = new ArrayList();
        List<String> audioTrackNameList = new ArrayList();
        if (audioAdaptationSet != null) {
            defaultUriDataSource = new DefaultUriDataSource(this.context, (TransferListener) defaultBandwidthMeter, this.userAgent);
            FormatEvaluator audioEvaluator = new FixedEvaluator();
            List<Representation> audioRepresentations = audioAdaptationSet.representations;
            List<String> codecs = new ArrayList();
            for (i = 0; i < audioRepresentations.size(); i += SECURITY_LEVEL_1) {
                Format format = ((Representation) audioRepresentations.get(i)).format;
                audioTrackNameList.add(format.id + " (" + format.numChannels + "ch, " + format.audioSamplingRate + "Hz)");
                ManifestFetcher manifestFetcher = this.manifestFetcher;
                int[] iArr = new int[SECURITY_LEVEL_1];
                iArr[0] = i;
                audioChunkSourceList.add(new DashChunkSource(manifestFetcher, audioAdaptationSetIndex, iArr, defaultUriDataSource, audioEvaluator, Constants.BROADCASTS_LIVE_TIME, this.elapsedRealtimeOffset, mainHandler, this.player));
                codecs.add(format.codecs);
            }
            if (this.audioCapabilities != null) {
                i = 0;
                while (i < PASSTHROUGH_CODECS_PRIORITY.length) {
                    String codec = PASSTHROUGH_CODECS_PRIORITY[i];
                    int encoding = PASSTHROUGH_ENCODINGS_PRIORITY[i];
                    if (codecs.indexOf(codec) == SECURITY_LEVEL_UNKNOWN || !this.audioCapabilities.supportsEncoding(encoding)) {
                        i += SECURITY_LEVEL_1;
                    } else {
                        for (j = audioRepresentations.size() + SECURITY_LEVEL_UNKNOWN; j >= 0; j += SECURITY_LEVEL_UNKNOWN) {
                            if (!((Representation) audioRepresentations.get(j)).format.codecs.equals(codec)) {
                                audioTrackNameList.remove(j);
                                audioChunkSourceList.remove(j);
                            }
                        }
                    }
                }
            }
        }
        String[] audioTrackNames;
        if (audioChunkSourceList.isEmpty()) {
            audioTrackNames = null;
            MultiTrackChunkSource audioChunkSource = null;
            audioRenderer = null;
        } else {
            audioTrackNames = new String[audioTrackNameList.size()];
            audioTrackNameList.toArray(audioTrackNames);
            MultiTrackChunkSource multiTrackChunkSource = new MultiTrackChunkSource((List) audioChunkSourceList);
            audioRenderer = new MediaCodecAudioTrackRenderer(new ChunkSampleSource(multiTrackChunkSource, defaultLoadControl, 3932160, true, mainHandler, this.player, SECURITY_LEVEL_1), drmSessionManager, true, mainHandler, this.player);
        }
        defaultUriDataSource = new DefaultUriDataSource(this.context, (TransferListener) defaultBandwidthMeter, this.userAgent);
        FormatEvaluator textEvaluator = new FixedEvaluator();
        List<ChunkSource> textChunkSourceList = new ArrayList();
        List<String> textTrackNameList = new ArrayList();
        for (i = 0; i < period.adaptationSets.size(); i += SECURITY_LEVEL_1) {
            AdaptationSet adaptationSet = (AdaptationSet) period.adaptationSets.get(i);
            if (adaptationSet.type == TEXT_BUFFER_SEGMENTS) {
                List<Representation> representations = adaptationSet.representations;
                for (j = 0; j < representations.size(); j += SECURITY_LEVEL_1) {
                    textTrackNameList.add(((Representation) representations.get(j)).format.id);
                    ManifestFetcher manifestFetcher2 = this.manifestFetcher;
                    int[] iArr2 = new int[SECURITY_LEVEL_1];
                    iArr2[0] = j;
                    textChunkSourceList.add(new DashChunkSource(manifestFetcher2, i, iArr2, defaultUriDataSource, textEvaluator, Constants.BROADCASTS_LIVE_TIME, this.elapsedRealtimeOffset, mainHandler, this.player));
                }
            }
        }
        String[] textTrackNames;
        if (textChunkSourceList.isEmpty()) {
            textTrackNames = null;
            MultiTrackChunkSource textChunkSource = null;
            textRenderer = null;
        } else {
            textTrackNames = new String[textTrackNameList.size()];
            textTrackNameList.toArray(textTrackNames);
            multiTrackChunkSource = new MultiTrackChunkSource((List) textChunkSourceList);
            SampleSource textSampleSource = new ChunkSampleSource(multiTrackChunkSource, defaultLoadControl, AccessibilityNodeInfoCompat.ACTION_SET_SELECTION, true, mainHandler, this.player, TEXT_BUFFER_SEGMENTS);
            TextRenderer textRenderer2 = this.player;
            Looper looper = mainHandler.getLooper();
            SubtitleParser[] subtitleParserArr = new SubtitleParser[TEXT_BUFFER_SEGMENTS];
            subtitleParserArr[0] = new TtmlParser();
            subtitleParserArr[SECURITY_LEVEL_1] = new WebvttParser();
            TrackRenderer textTrackRenderer = new TextTrackRenderer(textSampleSource, textRenderer2, looper, subtitleParserArr);
        }
        trackNames = new String[4][];
        multiTrackChunkSources = new MultiTrackChunkSource[4];
        TrackRenderer[] renderers = new TrackRenderer[4];
        renderers[0] = videoRenderer;
        renderers[SECURITY_LEVEL_1] = audioRenderer;
        renderers[TEXT_BUFFER_SEGMENTS] = textRenderer;
        this.callback.onRenderers(trackNames, multiTrackChunkSources, renderers, defaultBandwidthMeter);
    }

    private static int getWidevineSecurityLevel(StreamingDrmSessionManager sessionManager) {
        String securityLevelProperty = sessionManager.getPropertyString("securityLevel");
        if (securityLevelProperty.equals("L1")) {
            return SECURITY_LEVEL_1;
        }
        return securityLevelProperty.equals("L3") ? SECURITY_LEVEL_3 : SECURITY_LEVEL_UNKNOWN;
    }
}
