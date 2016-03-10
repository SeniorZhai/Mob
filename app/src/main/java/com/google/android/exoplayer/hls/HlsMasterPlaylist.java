package com.google.android.exoplayer.hls;

import java.util.List;

public final class HlsMasterPlaylist extends HlsPlaylist {
    public final List<Subtitle> subtitles;
    public final List<Variant> variants;

    public HlsMasterPlaylist(String baseUri, List<Variant> variants, List<Subtitle> subtitles) {
        super(baseUri, 0);
        this.variants = variants;
        this.subtitles = subtitles;
    }
}
