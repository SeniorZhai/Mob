package com.google.android.exoplayer.text;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import com.google.android.exoplayer.SampleHolder;
import com.google.android.exoplayer.util.Assertions;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class SubtitleParserHelper implements Callback {
    private IOException error;
    private final Handler handler;
    private final SubtitleParser parser;
    private boolean parsing;
    private Subtitle result;
    private SampleHolder sampleHolder;

    public SubtitleParserHelper(Looper looper, SubtitleParser parser) {
        this.handler = new Handler(looper, this);
        this.parser = parser;
        flush();
    }

    public synchronized void flush() {
        this.sampleHolder = new SampleHolder(1);
        this.parsing = false;
        this.result = null;
        this.error = null;
    }

    public synchronized boolean isParsing() {
        return this.parsing;
    }

    public synchronized SampleHolder getSampleHolder() {
        return this.sampleHolder;
    }

    public synchronized void startParseOperation() {
        boolean z = true;
        synchronized (this) {
            if (this.parsing) {
                z = false;
            }
            Assertions.checkState(z);
            this.parsing = true;
            this.result = null;
            this.error = null;
            this.handler.obtainMessage(0, this.sampleHolder).sendToTarget();
        }
    }

    public synchronized Subtitle getAndClearResult() throws IOException {
        Subtitle subtitle;
        try {
            if (this.error != null) {
                throw this.error;
            }
            subtitle = this.result;
            this.error = null;
            this.result = null;
        } catch (Throwable th) {
            this.error = null;
            this.result = null;
        }
        return subtitle;
    }

    public boolean handleMessage(Message msg) {
        SampleHolder holder = msg.obj;
        try {
            Subtitle result = this.parser.parse(new ByteArrayInputStream(holder.data.array(), 0, holder.size), null, this.sampleHolder.timeUs);
            IOException error = null;
        } catch (IOException e) {
            result = null;
            error = e;
        }
        synchronized (this) {
            if (this.sampleHolder == holder) {
                this.result = result;
                this.error = error;
                this.parsing = false;
            }
        }
        return true;
    }
}
