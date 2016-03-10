package com.google.android.exoplayer.text;

import java.io.IOException;
import java.io.InputStream;

public interface SubtitleParser {
    boolean canParse(String str);

    Subtitle parse(InputStream inputStream, String str, long j) throws IOException;
}
