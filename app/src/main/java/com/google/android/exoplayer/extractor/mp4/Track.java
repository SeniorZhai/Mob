package com.google.android.exoplayer.extractor.mp4;

import com.google.android.exoplayer.MediaFormat;

public final class Track {
    public static final int TYPE_AUDIO = 1936684398;
    public static final int TYPE_HINT = 1751740020;
    public static final int TYPE_META = 1835365473;
    public static final int TYPE_TEXT = 1952807028;
    public static final int TYPE_TIME_CODE = 1953325924;
    public static final int TYPE_VIDEO = 1986618469;
    public final long durationUs;
    public final int id;
    public final MediaFormat mediaFormat;
    public final int nalUnitLengthFieldLength;
    public final TrackEncryptionBox[] sampleDescriptionEncryptionBoxes;
    public final long timescale;
    public final int type;

    public Track(int id, int type, long timescale, long durationUs, MediaFormat mediaFormat, TrackEncryptionBox[] sampleDescriptionEncryptionBoxes, int nalUnitLengthFieldLength) {
        this.id = id;
        this.type = type;
        this.timescale = timescale;
        this.durationUs = durationUs;
        this.mediaFormat = mediaFormat;
        this.sampleDescriptionEncryptionBoxes = sampleDescriptionEncryptionBoxes;
        this.nalUnitLengthFieldLength = nalUnitLengthFieldLength;
    }
}
