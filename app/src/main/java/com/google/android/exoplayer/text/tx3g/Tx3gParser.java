package com.google.android.exoplayer.text.tx3g;

import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.Subtitle;
import com.google.android.exoplayer.text.SubtitleParser;
import com.google.android.exoplayer.util.MimeTypes;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class Tx3gParser implements SubtitleParser {
    public Subtitle parse(InputStream inputStream, String inputEncoding, long startTimeUs) throws IOException {
        return new Tx3gSubtitle(startTimeUs, new Cue(new DataInputStream(inputStream).readUTF()));
    }

    public boolean canParse(String mimeType) {
        return MimeTypes.APPLICATION_TX3G.equals(mimeType);
    }
}
