package com.google.android.exoplayer.hls;

import com.google.android.exoplayer.hls.HlsMediaPlaylist.Segment;
import com.google.android.exoplayer.upstream.UriLoadable.Parser;
import com.helpshift.HSFunnel;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

public final class HlsPlaylistParser implements Parser<HlsPlaylist> {
    private static final String AUDIO_TYPE = "AUDIO";
    private static final String AUTOSELECT_ATTR = "AUTOSELECT";
    private static final Pattern AUTOSELECT_ATTR_REGEX = HlsParserUtil.compileBooleanAttrPattern(AUTOSELECT_ATTR);
    private static final String BANDWIDTH_ATTR = "BANDWIDTH";
    private static final Pattern BANDWIDTH_ATTR_REGEX = Pattern.compile("BANDWIDTH=(\\d+)\\b");
    private static final Pattern BYTERANGE_REGEX = Pattern.compile("#EXT-X-BYTERANGE:(\\d+(?:@\\d+)?)\\b");
    private static final String BYTERANGE_TAG = "#EXT-X-BYTERANGE";
    private static final String CLOSED_CAPTIONS_TYPE = "CLOSED-CAPTIONS";
    private static final String CODECS_ATTR = "CODECS";
    private static final Pattern CODECS_ATTR_REGEX = Pattern.compile("CODECS=\"(.+?)\"");
    private static final String DEFAULT_ATTR = "DEFAULT";
    private static final Pattern DEFAULT_ATTR_REGEX = HlsParserUtil.compileBooleanAttrPattern(DEFAULT_ATTR);
    private static final String DISCONTINUITY_TAG = "#EXT-X-DISCONTINUITY";
    private static final String ENDLIST_TAG = "#EXT-X-ENDLIST";
    private static final String IV_ATTR = "IV";
    private static final Pattern IV_ATTR_REGEX = Pattern.compile("IV=([^,.*]+)");
    private static final String KEY_TAG = "#EXT-X-KEY";
    private static final String LANGUAGE_ATTR = "LANGUAGE";
    private static final Pattern LANGUAGE_ATTR_REGEX = Pattern.compile("LANGUAGE=\"(.+?)\"");
    private static final Pattern MEDIA_DURATION_REGEX = Pattern.compile("#EXTINF:([\\d.]+),");
    private static final String MEDIA_DURATION_TAG = "#EXTINF";
    private static final Pattern MEDIA_SEQUENCE_REGEX = Pattern.compile("#EXT-X-MEDIA-SEQUENCE:(\\d+)\\b");
    private static final String MEDIA_SEQUENCE_TAG = "#EXT-X-MEDIA-SEQUENCE";
    private static final String MEDIA_TAG = "#EXT-X-MEDIA";
    private static final String METHOD_AES128 = "AES-128";
    private static final String METHOD_ATTR = "METHOD";
    private static final Pattern METHOD_ATTR_REGEX = Pattern.compile("METHOD=(NONE|AES-128)");
    private static final String METHOD_NONE = "NONE";
    private static final String NAME_ATTR = "NAME";
    private static final Pattern NAME_ATTR_REGEX = Pattern.compile("NAME=\"(.+?)\"");
    private static final String RESOLUTION_ATTR = "RESOLUTION";
    private static final Pattern RESOLUTION_ATTR_REGEX = Pattern.compile("RESOLUTION=(\\d+x\\d+)");
    private static final String STREAM_INF_TAG = "#EXT-X-STREAM-INF";
    private static final String SUBTITLES_TYPE = "SUBTITLES";
    private static final Pattern TARGET_DURATION_REGEX = Pattern.compile("#EXT-X-TARGETDURATION:(\\d+)\\b");
    private static final String TARGET_DURATION_TAG = "#EXT-X-TARGETDURATION";
    private static final String TYPE_ATTR = "TYPE";
    private static final Pattern TYPE_ATTR_REGEX = Pattern.compile("TYPE=(AUDIO|VIDEO|SUBTITLES|CLOSED-CAPTIONS)");
    private static final String URI_ATTR = "URI";
    private static final Pattern URI_ATTR_REGEX = Pattern.compile("URI=\"(.+)\"");
    private static final Pattern VERSION_REGEX = Pattern.compile("#EXT-X-VERSION:(\\d+)\\b");
    private static final String VERSION_TAG = "#EXT-X-VERSION";
    private static final String VIDEO_TYPE = "VIDEO";

    private static class LineIterator {
        private final Queue<String> extraLines;
        private String next;
        private final BufferedReader reader;

        public LineIterator(Queue<String> extraLines, BufferedReader reader) {
            this.extraLines = extraLines;
            this.reader = reader;
        }

        public boolean hasNext() throws IOException {
            if (this.next != null) {
                return true;
            }
            if (this.extraLines.isEmpty()) {
                do {
                    String readLine = this.reader.readLine();
                    this.next = readLine;
                    if (readLine == null) {
                        return false;
                    }
                    this.next = this.next.trim();
                } while (this.next.isEmpty());
                return true;
            }
            this.next = (String) this.extraLines.poll();
            return true;
        }

        public String next() throws IOException {
            if (!hasNext()) {
                return null;
            }
            String result = this.next;
            this.next = null;
            return result;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.google.android.exoplayer.hls.HlsPlaylist parse(java.lang.String r6, java.io.InputStream r7) throws java.io.IOException, com.google.android.exoplayer.ParserException {
        /*
        r5 = this;
        r2 = new java.io.BufferedReader;
        r3 = new java.io.InputStreamReader;
        r3.<init>(r7);
        r2.<init>(r3);
        r0 = new java.util.LinkedList;
        r0.<init>();
    L_0x000f:
        r1 = r2.readLine();	 Catch:{ all -> 0x0083 }
        if (r1 == 0) goto L_0x0088;
    L_0x0015:
        r1 = r1.trim();	 Catch:{ all -> 0x0083 }
        r3 = r1.isEmpty();	 Catch:{ all -> 0x0083 }
        if (r3 != 0) goto L_0x000f;
    L_0x001f:
        r3 = "#EXT-X-STREAM-INF";
        r3 = r1.startsWith(r3);	 Catch:{ all -> 0x0083 }
        if (r3 == 0) goto L_0x0037;
    L_0x0027:
        r0.add(r1);	 Catch:{ all -> 0x0083 }
        r3 = new com.google.android.exoplayer.hls.HlsPlaylistParser$LineIterator;	 Catch:{ all -> 0x0083 }
        r3.<init>(r0, r2);	 Catch:{ all -> 0x0083 }
        r3 = parseMasterPlaylist(r3, r6);	 Catch:{ all -> 0x0083 }
        r2.close();
    L_0x0036:
        return r3;
    L_0x0037:
        r3 = "#EXT-X-TARGETDURATION";
        r3 = r1.startsWith(r3);	 Catch:{ all -> 0x0083 }
        if (r3 != 0) goto L_0x006f;
    L_0x003f:
        r3 = "#EXT-X-MEDIA-SEQUENCE";
        r3 = r1.startsWith(r3);	 Catch:{ all -> 0x0083 }
        if (r3 != 0) goto L_0x006f;
    L_0x0047:
        r3 = "#EXTINF";
        r3 = r1.startsWith(r3);	 Catch:{ all -> 0x0083 }
        if (r3 != 0) goto L_0x006f;
    L_0x004f:
        r3 = "#EXT-X-KEY";
        r3 = r1.startsWith(r3);	 Catch:{ all -> 0x0083 }
        if (r3 != 0) goto L_0x006f;
    L_0x0057:
        r3 = "#EXT-X-BYTERANGE";
        r3 = r1.startsWith(r3);	 Catch:{ all -> 0x0083 }
        if (r3 != 0) goto L_0x006f;
    L_0x005f:
        r3 = "#EXT-X-DISCONTINUITY";
        r3 = r1.equals(r3);	 Catch:{ all -> 0x0083 }
        if (r3 != 0) goto L_0x006f;
    L_0x0067:
        r3 = "#EXT-X-ENDLIST";
        r3 = r1.equals(r3);	 Catch:{ all -> 0x0083 }
        if (r3 == 0) goto L_0x007f;
    L_0x006f:
        r0.add(r1);	 Catch:{ all -> 0x0083 }
        r3 = new com.google.android.exoplayer.hls.HlsPlaylistParser$LineIterator;	 Catch:{ all -> 0x0083 }
        r3.<init>(r0, r2);	 Catch:{ all -> 0x0083 }
        r3 = parseMediaPlaylist(r3, r6);	 Catch:{ all -> 0x0083 }
        r2.close();
        goto L_0x0036;
    L_0x007f:
        r0.add(r1);	 Catch:{ all -> 0x0083 }
        goto L_0x000f;
    L_0x0083:
        r3 = move-exception;
        r2.close();
        throw r3;
    L_0x0088:
        r2.close();
        r3 = new com.google.android.exoplayer.ParserException;
        r4 = "Failed to parse the playlist, could not identify any tags.";
        r3.<init>(r4);
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.google.android.exoplayer.hls.HlsPlaylistParser.parse(java.lang.String, java.io.InputStream):com.google.android.exoplayer.hls.HlsPlaylist");
    }

    private static HlsMasterPlaylist parseMasterPlaylist(LineIterator iterator, String baseUri) throws IOException {
        ArrayList<Variant> variants = new ArrayList();
        ArrayList<Subtitle> subtitles = new ArrayList();
        int bitrate = 0;
        String codecs = null;
        int width = -1;
        int height = -1;
        boolean expectingStreamInfUrl = false;
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.startsWith(MEDIA_TAG)) {
                if (SUBTITLES_TYPE.equals(HlsParserUtil.parseStringAttr(line, TYPE_ATTR_REGEX, TYPE_ATTR))) {
                    subtitles.add(new Subtitle(HlsParserUtil.parseStringAttr(line, NAME_ATTR_REGEX, NAME_ATTR), HlsParserUtil.parseStringAttr(line, URI_ATTR_REGEX, URI_ATTR), HlsParserUtil.parseOptionalStringAttr(line, LANGUAGE_ATTR_REGEX), HlsParserUtil.parseOptionalBooleanAttr(line, DEFAULT_ATTR_REGEX), HlsParserUtil.parseOptionalBooleanAttr(line, AUTOSELECT_ATTR_REGEX)));
                }
            } else if (line.startsWith(STREAM_INF_TAG)) {
                bitrate = HlsParserUtil.parseIntAttr(line, BANDWIDTH_ATTR_REGEX, BANDWIDTH_ATTR);
                codecs = HlsParserUtil.parseOptionalStringAttr(line, CODECS_ATTR_REGEX);
                String resolutionString = HlsParserUtil.parseOptionalStringAttr(line, RESOLUTION_ATTR_REGEX);
                if (resolutionString != null) {
                    String[] widthAndHeight = resolutionString.split(HSFunnel.OPEN_INBOX);
                    width = Integer.parseInt(widthAndHeight[0]);
                    if (width <= 0) {
                        width = -1;
                    }
                    height = Integer.parseInt(widthAndHeight[1]);
                    if (height <= 0) {
                        height = -1;
                    }
                } else {
                    width = -1;
                    height = -1;
                }
                expectingStreamInfUrl = true;
            } else if (!line.startsWith("#") && expectingStreamInfUrl) {
                variants.add(new Variant(variants.size(), line, bitrate, codecs, width, height));
                bitrate = 0;
                codecs = null;
                width = -1;
                height = -1;
                expectingStreamInfUrl = false;
            }
        }
        return new HlsMasterPlaylist(baseUri, Collections.unmodifiableList(variants), Collections.unmodifiableList(subtitles));
    }

    private static HlsMediaPlaylist parseMediaPlaylist(LineIterator iterator, String baseUri) throws IOException {
        int mediaSequence = 0;
        int targetDurationSecs = 0;
        int version = 1;
        boolean live = true;
        List<Segment> segments = new ArrayList();
        double segmentDurationSecs = 0.0d;
        boolean segmentDiscontinuity = false;
        long segmentStartTimeUs = 0;
        int segmentByterangeOffset = 0;
        int segmentByterangeLength = -1;
        int segmentMediaSequence = 0;
        boolean isEncrypted = false;
        String encryptionKeyUri = null;
        String encryptionIV = null;
        while (iterator.hasNext()) {
            String line = iterator.next();
            if (line.startsWith(TARGET_DURATION_TAG)) {
                targetDurationSecs = HlsParserUtil.parseIntAttr(line, TARGET_DURATION_REGEX, TARGET_DURATION_TAG);
            } else if (line.startsWith(MEDIA_SEQUENCE_TAG)) {
                mediaSequence = HlsParserUtil.parseIntAttr(line, MEDIA_SEQUENCE_REGEX, MEDIA_SEQUENCE_TAG);
                segmentMediaSequence = mediaSequence;
            } else if (line.startsWith(VERSION_TAG)) {
                version = HlsParserUtil.parseIntAttr(line, VERSION_REGEX, VERSION_TAG);
            } else if (line.startsWith(MEDIA_DURATION_TAG)) {
                segmentDurationSecs = HlsParserUtil.parseDoubleAttr(line, MEDIA_DURATION_REGEX, MEDIA_DURATION_TAG);
            } else if (line.startsWith(KEY_TAG)) {
                isEncrypted = METHOD_AES128.equals(HlsParserUtil.parseStringAttr(line, METHOD_ATTR_REGEX, METHOD_ATTR));
                if (isEncrypted) {
                    encryptionKeyUri = HlsParserUtil.parseStringAttr(line, URI_ATTR_REGEX, URI_ATTR);
                    encryptionIV = HlsParserUtil.parseOptionalStringAttr(line, IV_ATTR_REGEX);
                } else {
                    encryptionKeyUri = null;
                    encryptionIV = null;
                }
            } else if (line.startsWith(BYTERANGE_TAG)) {
                String[] splitByteRange = HlsParserUtil.parseStringAttr(line, BYTERANGE_REGEX, BYTERANGE_TAG).split("@");
                segmentByterangeLength = Integer.parseInt(splitByteRange[0]);
                if (splitByteRange.length > 1) {
                    segmentByterangeOffset = Integer.parseInt(splitByteRange[1]);
                }
            } else if (line.equals(DISCONTINUITY_TAG)) {
                segmentDiscontinuity = true;
            } else if (!line.startsWith("#")) {
                String segmentEncryptionIV;
                if (!isEncrypted) {
                    segmentEncryptionIV = null;
                } else if (encryptionIV != null) {
                    segmentEncryptionIV = encryptionIV;
                } else {
                    segmentEncryptionIV = Integer.toHexString(segmentMediaSequence);
                }
                segmentMediaSequence++;
                if (segmentByterangeLength == -1) {
                    segmentByterangeOffset = 0;
                }
                segments.add(new Segment(line, segmentDurationSecs, segmentDiscontinuity, segmentStartTimeUs, isEncrypted, encryptionKeyUri, segmentEncryptionIV, segmentByterangeOffset, segmentByterangeLength));
                segmentStartTimeUs += (long) (1000000.0d * segmentDurationSecs);
                segmentDiscontinuity = false;
                segmentDurationSecs = 0.0d;
                if (segmentByterangeLength != -1) {
                    segmentByterangeOffset += segmentByterangeLength;
                }
                segmentByterangeLength = -1;
            } else if (line.equals(ENDLIST_TAG)) {
                live = false;
                break;
            }
        }
        return new HlsMediaPlaylist(baseUri, mediaSequence, targetDurationSecs, version, live, Collections.unmodifiableList(segments));
    }
}
