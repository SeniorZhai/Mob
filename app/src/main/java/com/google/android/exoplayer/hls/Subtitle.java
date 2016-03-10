package com.google.android.exoplayer.hls;

public final class Subtitle {
    public final boolean autoSelect;
    public final boolean isDefault;
    public final String language;
    public final String name;
    public final String uri;

    public Subtitle(String name, String uri, String language, boolean isDefault, boolean autoSelect) {
        this.name = name;
        this.uri = uri;
        this.language = language;
        this.autoSelect = autoSelect;
        this.isDefault = isDefault;
    }
}
