package com.google.android.exoplayer.text.ttml;

import android.util.Log;
import com.google.android.exoplayer.ParserException;
import com.google.android.exoplayer.text.Subtitle;
import com.google.android.exoplayer.text.SubtitleParser;
import com.google.android.exoplayer.util.MimeTypes;
import com.helpshift.HSFunnel;
import com.helpshift.constants.MessageColumns;
import io.fabric.sdk.android.BuildConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class TtmlParser implements SubtitleParser {
    private static final String ATTR_BEGIN = "begin";
    private static final String ATTR_DURATION = "dur";
    private static final String ATTR_END = "end";
    private static final Pattern CLOCK_TIME = Pattern.compile("^([0-9][0-9]+):([0-9][0-9]):([0-9][0-9])(?:(\\.[0-9]+)|:([0-9][0-9])(?:\\.([0-9]+))?)?$");
    private static final int DEFAULT_FRAMERATE = 30;
    private static final int DEFAULT_SUBFRAMERATE = 1;
    private static final int DEFAULT_TICKRATE = 1;
    private static final Pattern OFFSET_TIME = Pattern.compile("^([0-9]+(?:\\.[0-9]+)?)(h|m|s|ms|f|t)$");
    private static final String TAG = "TtmlParser";
    private final boolean strictParsing;
    private final XmlPullParserFactory xmlParserFactory;

    public TtmlParser() {
        this(false);
    }

    public TtmlParser(boolean strictParsing) {
        this.strictParsing = strictParsing;
        try {
            this.xmlParserFactory = XmlPullParserFactory.newInstance();
        } catch (XmlPullParserException e) {
            throw new RuntimeException("Couldn't create XmlPullParserFactory instance", e);
        }
    }

    public Subtitle parse(InputStream inputStream, String inputEncoding, long startTimeUs) throws IOException {
        int unsupportedNodeDepth;
        try {
            XmlPullParser xmlParser = this.xmlParserFactory.newPullParser();
            xmlParser.setInput(inputStream, inputEncoding);
            TtmlSubtitle ttmlSubtitle = null;
            LinkedList<TtmlNode> nodeStack = new LinkedList();
            unsupportedNodeDepth = 0;
            for (int eventType = xmlParser.getEventType(); eventType != DEFAULT_TICKRATE; eventType = xmlParser.getEventType()) {
                TtmlNode parent = (TtmlNode) nodeStack.peekLast();
                if (unsupportedNodeDepth == 0) {
                    String name = xmlParser.getName();
                    if (eventType == 2) {
                        if (isSupportedTag(name)) {
                            TtmlNode node = parseNode(xmlParser, parent);
                            nodeStack.addLast(node);
                            if (parent != null) {
                                parent.addChild(node);
                            }
                        } else {
                            Log.i(TAG, "Ignoring unsupported tag: " + xmlParser.getName());
                            unsupportedNodeDepth += DEFAULT_TICKRATE;
                        }
                    } else if (eventType == 4) {
                        parent.addChild(TtmlNode.buildTextNode(xmlParser.getText()));
                    } else if (eventType == 3) {
                        if (xmlParser.getName().equals(TtmlNode.TAG_TT)) {
                            ttmlSubtitle = new TtmlSubtitle((TtmlNode) nodeStack.getLast(), startTimeUs);
                        }
                        nodeStack.removeLast();
                    }
                } else if (eventType == 2) {
                    unsupportedNodeDepth += DEFAULT_TICKRATE;
                } else if (eventType == 3) {
                    unsupportedNodeDepth--;
                }
                xmlParser.next();
            }
            return ttmlSubtitle;
        } catch (ParserException e) {
            if (this.strictParsing) {
                throw e;
            }
            Log.e(TAG, "Suppressing parser error", e);
            unsupportedNodeDepth += DEFAULT_TICKRATE;
        } catch (XmlPullParserException xppe) {
            throw new ParserException("Unable to parse source", xppe);
        }
    }

    public boolean canParse(String mimeType) {
        return MimeTypes.APPLICATION_TTML.equals(mimeType);
    }

    private TtmlNode parseNode(XmlPullParser parser, TtmlNode parent) throws ParserException {
        long duration = 0;
        long startTime = -1;
        long endTime = -1;
        int attributeCount = parser.getAttributeCount();
        for (int i = 0; i < attributeCount; i += DEFAULT_TICKRATE) {
            String attr = parser.getAttributeName(i).replaceFirst("^.*:", BuildConfig.FLAVOR);
            String value = parser.getAttributeValue(i);
            if (attr.equals(ATTR_BEGIN)) {
                startTime = parseTimeExpression(value, DEFAULT_FRAMERATE, DEFAULT_TICKRATE, DEFAULT_TICKRATE);
            } else if (attr.equals(ATTR_END)) {
                endTime = parseTimeExpression(value, DEFAULT_FRAMERATE, DEFAULT_TICKRATE, DEFAULT_TICKRATE);
            } else if (attr.equals(ATTR_DURATION)) {
                duration = parseTimeExpression(value, DEFAULT_FRAMERATE, DEFAULT_TICKRATE, DEFAULT_TICKRATE);
            }
        }
        if (!(parent == null || parent.startTimeUs == -1)) {
            if (startTime != -1) {
                startTime += parent.startTimeUs;
            }
            if (endTime != -1) {
                endTime += parent.startTimeUs;
            }
        }
        if (endTime == -1) {
            if (duration > 0) {
                endTime = startTime + duration;
            } else if (!(parent == null || parent.endTimeUs == -1)) {
                endTime = parent.endTimeUs;
            }
        }
        return TtmlNode.buildNode(parser.getName(), startTime, endTime);
    }

    private static boolean isSupportedTag(String tag) {
        if (tag.equals(TtmlNode.TAG_TT) || tag.equals(TtmlNode.TAG_HEAD) || tag.equals(MessageColumns.BODY) || tag.equals(TtmlNode.TAG_DIV) || tag.equals(HSFunnel.CONVERSATION_POSTED) || tag.equals(TtmlNode.TAG_SPAN) || tag.equals(TtmlNode.TAG_BR) || tag.equals(TtmlNode.TAG_STYLE) || tag.equals(TtmlNode.TAG_STYLING) || tag.equals(TtmlNode.TAG_LAYOUT) || tag.equals(TtmlNode.TAG_REGION) || tag.equals(TtmlNode.TAG_METADATA) || tag.equals(TtmlNode.TAG_SMPTE_IMAGE) || tag.equals(TtmlNode.TAG_SMPTE_DATA) || tag.equals(TtmlNode.TAG_SMPTE_INFORMATION)) {
            return true;
        }
        return false;
    }

    private static long parseTimeExpression(String time, int frameRate, int subframeRate, int tickRate) throws ParserException {
        Matcher matcher = CLOCK_TIME.matcher(time);
        if (matcher.matches()) {
            double durationSeconds = (((double) (Long.parseLong(matcher.group(DEFAULT_TICKRATE)) * 3600)) + ((double) (Long.parseLong(matcher.group(2)) * 60))) + ((double) Long.parseLong(matcher.group(3)));
            String fraction = matcher.group(4);
            durationSeconds += fraction != null ? Double.parseDouble(fraction) : 0.0d;
            String frames = matcher.group(5);
            durationSeconds += frames != null ? ((double) Long.parseLong(frames)) / ((double) frameRate) : 0.0d;
            String subframes = matcher.group(6);
            return (long) (1000000.0d * (durationSeconds + (subframes != null ? (((double) Long.parseLong(subframes)) / ((double) subframeRate)) / ((double) frameRate) : 0.0d)));
        }
        matcher = OFFSET_TIME.matcher(time);
        if (matcher.matches()) {
            double offsetSeconds = Double.parseDouble(matcher.group(DEFAULT_TICKRATE));
            String unit = matcher.group(2);
            if (unit.equals(HSFunnel.MARKED_HELPFUL)) {
                offsetSeconds *= 3600.0d;
            } else if (unit.equals(HSFunnel.MESSAGE_ADDED)) {
                offsetSeconds *= 60.0d;
            } else if (!unit.equals(HSFunnel.PERFORMED_SEARCH)) {
                if (unit.equals("ms")) {
                    offsetSeconds /= 1000.0d;
                } else if (unit.equals(HSFunnel.READ_FAQ)) {
                    offsetSeconds /= (double) frameRate;
                } else if (unit.equals("t")) {
                    offsetSeconds /= (double) tickRate;
                }
            }
            return (long) (1000000.0d * offsetSeconds);
        }
        throw new ParserException("Malformed time expression: " + time);
    }
}
