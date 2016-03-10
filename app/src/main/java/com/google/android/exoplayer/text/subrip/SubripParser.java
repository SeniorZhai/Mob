package com.google.android.exoplayer.text.subrip;

import android.text.Html;
import android.text.TextUtils;
import com.google.android.exoplayer.ParserException;
import com.google.android.exoplayer.text.Cue;
import com.google.android.exoplayer.text.SubtitleParser;
import com.google.android.exoplayer.util.LongArray;
import com.google.android.exoplayer.util.MimeTypes;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.protocol.HTTP;

public final class SubripParser implements SubtitleParser {
    private static final Pattern SUBRIP_TIMESTAMP = Pattern.compile("(?:(\\d+):)?(\\d+):(\\d+),(\\d+)");
    private static final Pattern SUBRIP_TIMING_LINE = Pattern.compile("(.*)\\s+-->\\s+(.*)");
    private final StringBuilder textBuilder = new StringBuilder();

    public SubripSubtitle parse(InputStream inputStream, String inputEncoding, long startTimeUs) throws IOException {
        ArrayList<Cue> cues = new ArrayList();
        LongArray cueTimesUs = new LongArray();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, HTTP.UTF_8));
        while (true) {
            String currentLine = reader.readLine();
            if (currentLine != null) {
                try {
                    Integer.parseInt(currentLine);
                    currentLine = reader.readLine();
                    Matcher matcher = SUBRIP_TIMING_LINE.matcher(currentLine);
                    if (matcher.find()) {
                        cueTimesUs.add(parseTimestampUs(matcher.group(1)) + startTimeUs);
                        cueTimesUs.add(parseTimestampUs(matcher.group(2)) + startTimeUs);
                        this.textBuilder.setLength(0);
                        while (true) {
                            currentLine = reader.readLine();
                            if (TextUtils.isEmpty(currentLine)) {
                                break;
                            }
                            if (this.textBuilder.length() > 0) {
                                this.textBuilder.append("<br>");
                            }
                            this.textBuilder.append(currentLine.trim());
                        }
                        cues.add(new Cue(Html.fromHtml(this.textBuilder.toString())));
                    } else {
                        throw new ParserException("Expected timing line: " + currentLine);
                    }
                } catch (NumberFormatException e) {
                    throw new ParserException("Expected numeric counter: " + currentLine);
                }
            }
            Cue[] cuesArray = new Cue[cues.size()];
            cues.toArray(cuesArray);
            return new SubripSubtitle(startTimeUs, cuesArray, cueTimesUs.toArray());
        }
    }

    public boolean canParse(String mimeType) {
        return MimeTypes.APPLICATION_SUBRIP.equals(mimeType);
    }

    private static long parseTimestampUs(String s) throws NumberFormatException {
        Matcher matcher = SUBRIP_TIMESTAMP.matcher(s);
        if (matcher.matches()) {
            return ((((((Long.parseLong(matcher.group(1)) * 60) * 60) * 1000) + ((Long.parseLong(matcher.group(2)) * 60) * 1000)) + (Long.parseLong(matcher.group(3)) * 1000)) + Long.parseLong(matcher.group(4))) * 1000;
        }
        throw new NumberFormatException("has invalid format");
    }
}
