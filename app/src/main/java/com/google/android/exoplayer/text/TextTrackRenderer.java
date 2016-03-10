package com.google.android.exoplayer.text;

import android.annotation.TargetApi;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
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
import java.util.Collections;
import java.util.List;

@TargetApi(16)
public class TextTrackRenderer extends TrackRenderer implements Callback {
    private static final int MSG_UPDATE_OVERLAY = 0;
    private final MediaFormatHolder formatHolder;
    private boolean inputStreamEnded;
    private Subtitle nextSubtitle;
    private int nextSubtitleEventIndex;
    private SubtitleParserHelper parserHelper;
    private int parserIndex;
    private HandlerThread parserThread;
    private final SampleSourceReader source;
    private Subtitle subtitle;
    private final SubtitleParser[] subtitleParsers;
    private final TextRenderer textRenderer;
    private final Handler textRendererHandler;
    private int trackIndex;

    public TextTrackRenderer(SampleSource source, TextRenderer textRenderer, Looper textRendererLooper, SubtitleParser... subtitleParsers) {
        this.source = source.register();
        this.textRenderer = (TextRenderer) Assertions.checkNotNull(textRenderer);
        this.textRendererHandler = textRendererLooper == null ? null : new Handler(textRendererLooper, this);
        this.subtitleParsers = (SubtitleParser[]) Assertions.checkNotNull(subtitleParsers);
        this.formatHolder = new MediaFormatHolder();
    }

    protected int doPrepare(long positionUs) throws ExoPlaybackException {
        try {
            if (!this.source.prepare(positionUs)) {
                return 0;
            }
            for (int i = 0; i < this.subtitleParsers.length; i++) {
                for (int j = 0; j < this.source.getTrackCount(); j++) {
                    if (this.subtitleParsers[i].canParse(this.source.getTrackInfo(j).mimeType)) {
                        this.parserIndex = i;
                        this.trackIndex = j;
                        return 1;
                    }
                }
            }
            return -1;
        } catch (Throwable e) {
            throw new ExoPlaybackException(e);
        }
    }

    protected void onEnabled(long positionUs, boolean joining) {
        this.source.enable(this.trackIndex, positionUs);
        this.parserThread = new HandlerThread("textParser");
        this.parserThread.start();
        this.parserHelper = new SubtitleParserHelper(this.parserThread.getLooper(), this.subtitleParsers[this.parserIndex]);
        seekToInternal();
    }

    protected void seekTo(long positionUs) {
        this.source.seekToUs(positionUs);
        seekToInternal();
    }

    private void seekToInternal() {
        this.inputStreamEnded = false;
        this.subtitle = null;
        this.nextSubtitle = null;
        this.parserHelper.flush();
        clearTextRenderer();
    }

    protected void doSomeWork(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
        try {
            this.source.continueBuffering(positionUs);
            if (this.nextSubtitle == null) {
                try {
                    this.nextSubtitle = this.parserHelper.getAndClearResult();
                } catch (Throwable e) {
                    throw new ExoPlaybackException(e);
                }
            }
            boolean z = false;
            long subtitleNextEventTimeUs = Long.MAX_VALUE;
            if (this.subtitle != null) {
                subtitleNextEventTimeUs = getNextEventTime();
                while (subtitleNextEventTimeUs <= positionUs) {
                    this.nextSubtitleEventIndex++;
                    subtitleNextEventTimeUs = getNextEventTime();
                    z = true;
                }
            }
            if (subtitleNextEventTimeUs == Long.MAX_VALUE && this.nextSubtitle != null && this.nextSubtitle.getStartTime() <= positionUs) {
                this.subtitle = this.nextSubtitle;
                this.nextSubtitle = null;
                this.nextSubtitleEventIndex = this.subtitle.getNextEventTimeIndex(positionUs);
                z = true;
            }
            if (z && getState() == 3) {
                updateTextRenderer(this.subtitle.getCues(positionUs));
            }
            if (!this.inputStreamEnded && this.nextSubtitle == null && !this.parserHelper.isParsing()) {
                try {
                    SampleHolder sampleHolder = this.parserHelper.getSampleHolder();
                    sampleHolder.clearData();
                    int result = this.source.readData(this.trackIndex, positionUs, this.formatHolder, sampleHolder, false);
                    if (result == -3) {
                        this.parserHelper.startParseOperation();
                    } else if (result == -1) {
                        this.inputStreamEnded = true;
                    }
                } catch (Throwable e2) {
                    throw new ExoPlaybackException(e2);
                }
            }
        } catch (Throwable e22) {
            throw new ExoPlaybackException(e22);
        }
    }

    protected void onDisabled() {
        this.subtitle = null;
        this.nextSubtitle = null;
        this.parserThread.quit();
        this.parserThread = null;
        this.parserHelper = null;
        clearTextRenderer();
        this.source.disable(this.trackIndex);
    }

    protected void onReleased() {
        this.source.release();
    }

    protected long getDurationUs() {
        return this.source.getTrackInfo(this.trackIndex).durationUs;
    }

    protected long getBufferedPositionUs() {
        return -3;
    }

    protected boolean isEnded() {
        return this.inputStreamEnded && (this.subtitle == null || getNextEventTime() == Long.MAX_VALUE);
    }

    protected boolean isReady() {
        return true;
    }

    private long getNextEventTime() {
        return (this.nextSubtitleEventIndex == -1 || this.nextSubtitleEventIndex >= this.subtitle.getEventTimeCount()) ? Long.MAX_VALUE : this.subtitle.getEventTime(this.nextSubtitleEventIndex);
    }

    private void updateTextRenderer(List<Cue> cues) {
        if (this.textRendererHandler != null) {
            this.textRendererHandler.obtainMessage(0, cues).sendToTarget();
        } else {
            invokeRendererInternalCues(cues);
        }
    }

    private void clearTextRenderer() {
        updateTextRenderer(Collections.emptyList());
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                invokeRendererInternalCues((List) msg.obj);
                return true;
            default:
                return false;
        }
    }

    private void invokeRendererInternalCues(List<Cue> cues) {
        this.textRenderer.onCues(cues);
    }
}
