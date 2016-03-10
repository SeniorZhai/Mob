package com.google.android.exoplayer.text.webvtt;

import android.text.Html;
import android.text.Layout.Alignment;
import android.util.Log;
import com.google.android.exoplayer.C;
import com.google.android.exoplayer.ParserException;
import com.google.android.exoplayer.text.SubtitleParser;
import com.google.android.exoplayer.util.MimeTypes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.protocol.HTTP;

public class WebvttParser implements SubtitleParser {
    private static final Pattern MEDIA_TIMESTAMP = Pattern.compile("MPEGTS:\\d+");
    private static final Pattern MEDIA_TIMESTAMP_OFFSET = Pattern.compile("OFFSET:\\-?\\d+");
    private static final String NON_NUMERIC_STRING = ".*[^0-9].*";
    private static final long SAMPLING_RATE = 90;
    private static final String TAG = "WebvttParser";
    private static final Pattern WEBVTT_CUE_IDENTIFIER = Pattern.compile(WEBVTT_CUE_IDENTIFIER_STRING);
    private static final String WEBVTT_CUE_IDENTIFIER_STRING = "^(?!.*(-->)).*$";
    private static final Pattern WEBVTT_CUE_SETTING = Pattern.compile(WEBVTT_CUE_SETTING_STRING);
    private static final String WEBVTT_CUE_SETTING_STRING = "\\S*:\\S*";
    private static final Pattern WEBVTT_FILE_HEADER = Pattern.compile(WEBVTT_FILE_HEADER_STRING);
    private static final String WEBVTT_FILE_HEADER_STRING = "^\ufeff?WEBVTT((\\u0020|\t).*)?$";
    private static final Pattern WEBVTT_METADATA_HEADER = Pattern.compile(WEBVTT_METADATA_HEADER_STRING);
    private static final String WEBVTT_METADATA_HEADER_STRING = "\\S*[:=]\\S*";
    private static final Pattern WEBVTT_TIMESTAMP = Pattern.compile(WEBVTT_TIMESTAMP_STRING);
    private static final String WEBVTT_TIMESTAMP_STRING = "(\\d+:)?[0-5]\\d:[0-5]\\d\\.\\d{3}";
    private final boolean strictParsing;
    private final StringBuilder textBuilder;

    public WebvttParser() {
        this(false);
    }

    public WebvttParser(boolean strictParsing) {
        this.strictParsing = strictParsing;
        this.textBuilder = new StringBuilder();
    }

    public WebvttSubtitle parse(InputStream inputStream, String inputEncoding, long startTimeUs) throws IOException {
        ArrayList<WebvttCue> subtitles = new ArrayList();
        long mediaTimestampUs = startTimeUs;
        long mediaTimestampOffsetUs = 0;
        BufferedReader webvttData = new BufferedReader(new InputStreamReader(inputStream, HTTP.UTF_8));
        String line = webvttData.readLine();
        if (line == null) {
            throw new ParserException("Expected WEBVTT or EXO-HEADER. Got null");
        }
        Matcher matcher;
        if (line.startsWith(C.WEBVTT_EXO_HEADER)) {
            matcher = MEDIA_TIMESTAMP_OFFSET.matcher(line);
            if (matcher.find()) {
                mediaTimestampOffsetUs = Long.parseLong(matcher.group().substring(7));
            }
            line = webvttData.readLine();
            if (line == null) {
                throw new ParserException("Expected WEBVTT. Got null");
            }
        }
        if (WEBVTT_FILE_HEADER.matcher(line).matches()) {
            while (true) {
                line = webvttData.readLine();
                if (line == null) {
                    throw new ParserException("Expected an empty line after webvtt header");
                } else if (line.isEmpty()) {
                    break;
                } else {
                    if (!WEBVTT_METADATA_HEADER.matcher(line).find()) {
                        handleNoncompliantLine(line);
                    }
                    if (line.startsWith("X-TIMESTAMP-MAP")) {
                        Matcher timestampMatcher = MEDIA_TIMESTAMP.matcher(line);
                        if (timestampMatcher.find()) {
                            mediaTimestampUs = getAdjustedStartTime(((Long.parseLong(timestampMatcher.group().substring(7)) * 1000) / SAMPLING_RATE) + mediaTimestampOffsetUs);
                        } else {
                            throw new ParserException("X-TIMESTAMP-MAP doesn't contain media timestamp: " + line);
                        }
                    }
                }
            }
            while (true) {
                line = webvttData.readLine();
                if (line == null) {
                    return new WebvttSubtitle(subtitles, mediaTimestampUs);
                }
                if (WEBVTT_CUE_IDENTIFIER.matcher(line).find()) {
                    line = webvttData.readLine();
                }
                int lineNum = -1;
                int position = -1;
                Alignment alignment = null;
                int size = -1;
                matcher = WEBVTT_TIMESTAMP.matcher(line);
                if (matcher.find()) {
                    long startTime = parseTimestampUs(matcher.group()) + mediaTimestampUs;
                    if (matcher.find()) {
                        String endTimeString = matcher.group();
                        long endTime = parseTimestampUs(endTimeString) + mediaTimestampUs;
                        matcher = WEBVTT_CUE_SETTING.matcher(line.substring(line.indexOf(endTimeString) + endTimeString.length()));
                        while (matcher.find()) {
                            String[] parts = matcher.group().split(":", 2);
                            String name = parts[0];
                            String value = parts[1];
                            try {
                                if ("line".equals(name)) {
                                    if (value.endsWith("%")) {
                                        lineNum = parseIntPercentage(value);
                                    } else if (value.matches(NON_NUMERIC_STRING)) {
                                        Log.w(TAG, "Invalid line value: " + value);
                                    } else {
                                        lineNum = Integer.parseInt(value);
                                    }
                                } else if ("align".equals(name)) {
                                    if ("start".equals(value)) {
                                        alignment = Alignment.ALIGN_NORMAL;
                                    } else if ("middle".equals(value)) {
                                        alignment = Alignment.ALIGN_CENTER;
                                    } else if ("end".equals(value)) {
                                        alignment = Alignment.ALIGN_OPPOSITE;
                                    } else if ("left".equals(value)) {
                                        alignment = Alignment.ALIGN_NORMAL;
                                    } else if ("right".equals(value)) {
                                        alignment = Alignment.ALIGN_OPPOSITE;
                                    } else {
                                        Log.w(TAG, "Invalid align value: " + value);
                                    }
                                } else if ("position".equals(name)) {
                                    position = parseIntPercentage(value);
                                } else if ("size".equals(name)) {
                                    size = parseIntPercentage(value);
                                } else {
                                    Log.w(TAG, "Unknown cue setting " + name + ":" + value);
                                }
                            } catch (NumberFormatException e) {
                                Log.w(TAG, name + " contains an invalid value " + value, e);
                            }
                        }
                        this.textBuilder.setLength(0);
                        while (true) {
                            line = webvttData.readLine();
                            if (line == null || line.isEmpty()) {
                                subtitles.add(new WebvttCue(startTime, endTime, Html.fromHtml(this.textBuilder.toString()), lineNum, position, alignment, size));
                            } else {
                                if (this.textBuilder.length() > 0) {
                                    this.textBuilder.append("<br>");
                                }
                                this.textBuilder.append(line.trim());
                            }
                        }
                        subtitles.add(new WebvttCue(startTime, endTime, Html.fromHtml(this.textBuilder.toString()), lineNum, position, alignment, size));
                    } else {
                        throw new ParserException("Expected cue end time: " + line);
                    }
                }
                throw new ParserException("Expected cue start time: " + line);
            }
        }
        throw new ParserException("Expected WEBVTT. Got " + line);
    }

    public boolean canParse(String mimeType) {
        return MimeTypes.TEXT_VTT.equals(mimeType);
    }

    protected long getAdjustedStartTime(long startTimeUs) {
        return startTimeUs;
    }

    protected void handleNoncompliantLine(String line) throws ParserException {
        if (this.strictParsing) {
            throw new ParserException("Unexpected line: " + line);
        }
    }

    private static int parseIntPercentage(String s) throws NumberFormatException {
        if (s.endsWith("%")) {
            s = s.substring(0, s.length() - 1);
            if (s.matches(NON_NUMERIC_STRING)) {
                throw new NumberFormatException(s + " contains an invalid character");
            }
            int value = Integer.parseInt(s);
            if (value >= 0 && value <= 100) {
                return value;
            }
            throw new NumberFormatException(value + " is out of range [0-100]");
        }
        throw new NumberFormatException(s + " doesn't end with '%'");
    }

    private static long parseTimestampUs(String s) throws NumberFormatException {
        if (s.matches(WEBVTT_TIMESTAMP_STRING)) {
            String[] parts = s.split("\\.", 2);
            long value = 0;
            for (String group : parts[0].split(":")) {
                value = (60 * value) + Long.parseLong(group);
            }
            return ((value * 1000) + Long.parseLong(parts[1])) * 1000;
        }
        throw new NumberFormatException("has invalid format");
    }
}
